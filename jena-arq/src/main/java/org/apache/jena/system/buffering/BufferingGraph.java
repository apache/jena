/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.system.buffering;

import static org.apache.jena.riot.other.G.containsBySameTerm;
import static org.apache.jena.riot.other.G.execTxn;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphPlain;
import org.apache.jena.query.TxnType;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * A graph that buffers changes (including prefixes changes) until {@link #flush} or
 * {@link #flushDirect} is called.
 */
public class BufferingGraph extends GraphWrapper implements BufferingCtl {

    // Controls whether to check the underlying graph to check whether to record a change or not.
    // It takes more memory but means the underlying graph is not touched for add() and delete().
    private final static boolean CHECK = true;

    private final Graph addedGraph;
    private final Set<Triple> deletedTriples = new HashSet<>();

    private final BufferingPrefixMapping prefixMapping;

    private Transactional transactional = TransactionalLock.createMRSW();

    public static BufferingGraph create(Graph graph) {
        if ( graph instanceof BufferingGraph )
            Log.warn(BufferingGraph.class, "Creating a BufferingGraph over a BufferingGraph");
        return new BufferingGraph(graph);
    }

    public BufferingGraph(Graph graph) {
        super(graph);
        prefixMapping = new BufferingPrefixMapping(graph.getPrefixMapping());
        if ( graph.getCapabilities().handlesLiteralTyping())
            addedGraph = GraphMemFactory.createDefaultGraph();
        else
            addedGraph = GraphPlain.plain();
    }

    public Graph base() { return get(); }

    /** Flush the changes to the base graph, using a Graph transaction if possible. */
    @Override
    public void flush() {
        Graph base = get();
        execTxn(base, ()-> flushDirect(base));
    }

    /** Flush the changes directly to the base graph. */
    public void flushDirect() {
        transactional.begin(TxnType.WRITE);
        // So that get() is called exactly once per call.
        Graph base = get();
        flushDirect(base);
        transactional.commit();
        transactional.end();
    }

    private void flushDirect(Graph base) {
        deletedTriples.forEach(base::delete);
        addedGraph.find().forEachRemaining(base::add);
        deletedTriples.clear();
        addedGraph.clear();
        prefixMapping.flush();
    }

    private void updateOperation() {
    }

    private void readOperation() {
    }


    @Override
    public void add(Triple t) {
        execAdd(t);
    }

    @Override
    public void delete(Triple t) {
        execDelete(t);
    }

    private void execAdd(Triple triple) {
        updateOperation();
        Graph base = get();
        deletedTriples.remove(triple);
        if (containsBySameTerm(addedGraph, triple) )
            return ;
        if ( CHECK && containsBySameTerm(base, triple) )
            // Already in base graph
            // No action.
            return;
        addedGraph.add(triple);
    }

    private void execDelete(Triple triple) {
        updateOperation();
        Graph base = get();
        addedGraph.delete(triple);

        if ( CHECK && ! containsBySameTerm(base, triple) )
            return;
        deletedTriples.add(triple);
    }

    public Graph getAdded() {
        return addedGraph;
    }

    public Set<Triple> getDeleted() {
        return deletedTriples;
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return contains(Triple.create(s, p, o));
    }

    @Override
    public boolean contains(Triple triple) {
        readOperation();
        if ( addedGraph.contains(triple) )
            return true;
        Graph base = get();
        ExtendedIterator<Triple> iter = base.find(triple).filterDrop(t->deletedTriples.contains(t));
        try { return iter.hasNext(); }
        finally { iter.close(); }
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        readOperation();
        Iterator<Triple> extra = findInAdded(s, p, o);
        Iter<Triple> iter =
            Iter.iter(get().find(s, p, o))
                .filter(t->! deletedTriples.contains(t))
                .append(extra);
        if ( ! CHECK )
            iter = iter.distinct();
        return WrappedIterator.create(iter);
    }

    private Iterator<Triple> findInAdded(Node s, Node p, Node o) {
        return addedGraph.find(s,p,o);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        return find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject());
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public boolean isEmpty() {
        readOperation();
        if (!addedGraph.isEmpty())
            return false;
        Graph base = get();
        if (deletedTriples.isEmpty())
            return base.isEmpty();
        // Go through the full machinery.
        return ! contains(Triple.ANY);
    }

    @Override
    public int size() {
        readOperation();
        if ( CHECK )
            return super.size() - deletedTriples.size() + addedGraph.size();
        // If we have been recording actions, not changes, need to be more careful.
        return (int)(Iter.count(find(Triple.ANY)));
    }
}
