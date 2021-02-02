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

package org.apache.jena.sparql.core;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Map.Entry;

import org.apache.jena.atlas.iterator.IteratorConcat ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker ;
import org.apache.jena.sparql.graph.GraphOps;

/** Implementation of a {@code DatasetGraph} as an extensible set of graphs.
 *  Subclasses need to manage any implicit graph creation.
 *  <p>
 *  This implementation provides copy-in, copy-out for {@link #addGraph}.
 *  <p>See {@link DatasetGraphMapLink} for a {@code DatasetGraph}
 *  that holds graphs as provided.
 *
 *  @see DatasetGraphMapLink
 */
public class DatasetGraphMap extends DatasetGraphTriplesQuads
{
    private final GraphMaker graphMaker ;
    private final Map<Node, Graph> graphs = new HashMap<>() ;
    private final Graph defaultGraph ;
    private final PrefixMap prefixes ;

    /**  DatasetGraphMap defaulting to storage in memory.
     */
    public DatasetGraphMap() {
        this(DatasetGraphFactory.graphMakerNamedGraphMem) ;
    }

    /**  DatasetGraphMap with a specific policy for graph creation.
     *   This allows control over the storage.
     */
    public DatasetGraphMap(GraphMaker graphMaker) {
        this(graphMaker.create(null), graphMaker) ;
    }

    private DatasetGraphMap(Graph defaultGraph, GraphMaker graphMaker) {
        this.defaultGraph = defaultGraph ;
        this.graphMaker = graphMaker ;
        // Preserves legacy behaviour of "getDefaultPraph" having prefixes.
        this.prefixes = Prefixes.adapt(defaultGraph);
    }

    // ----
    private final Transactional txn                     = TransactionalLock.createMRSW() ;
    private final Transactional txn()                   { return txn; }
    @Override public void begin()                       { txn().begin(); }
    @Override public void begin(TxnType txnType)        { txn().begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn().begin(mode); }
    @Override public boolean promote(Promote txnType)   { return txn().promote(txnType); }
    @Override public void commit()                      { txn().commit(); }
    @Override public void abort()                       { txn().abort(); }
    @Override public boolean isInTransaction()          { return txn().isInTransaction(); }
    @Override public void end()                         { txn().end(); }
    @Override public ReadWrite transactionMode()        { return txn().transactionMode(); }
    @Override public TxnType transactionType()          { return txn().transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return false; }
    // ----

    @Override
    public Iterator<Node> listGraphNodes() {
        // Hide empty graphs.
        return graphs.entrySet().stream().filter(e->!e.getValue().isEmpty()).map(Entry::getKey).iterator();
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        // Hide empty graphs.
        if ( Quad.isDefaultGraph(graphNode) )
            return true;
        if ( Quad.isUnionGraph(graphNode) )
            return true;
        Graph g = graphs.get(graphNode);
        return g != null && !g.isEmpty();
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o) {
        getDefaultGraph().add(Triple.create(s, p, o)) ;
    }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {
        getGraph(g).add(Triple.create(s, p, o)) ;
    }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {
        getDefaultGraph().delete(Triple.create(s, p, o)) ;
    }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {
        getGraph(g).delete(Triple.create(s, p, o)) ;
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        Iterator<Triple> iter = getDefaultGraph().find(s, p, o) ;
        return G.triples2quadsDftGraph(iter)  ;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        Iterator<Triple> iter = getGraph(g).find(s, p, o) ;
        return G.triples2quads(g, iter) ;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        Iterator<Node> gnames = listGraphNodes() ;
        IteratorConcat<Quad> iter = new IteratorConcat<>() ;

        // Named graphs
        for ( ; gnames.hasNext() ; ) {
            Node gn = gnames.next();
            Iterator<Quad> qIter = findInSpecificNamedGraph(gn, s, p, o) ;
            if ( qIter != null )
                iter.add(qIter) ;
        }
        return iter ;
    }

    @Override
    public Graph getDefaultGraph() {
        return defaultGraph;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        if ( Quad.isUnionGraph(graphNode) )
            return GraphOps.unionGraph(this) ;
        if ( Quad.isDefaultGraph(graphNode))
            return getDefaultGraph() ;
        // Not a special case.
        Graph g = graphs.get(graphNode);
        if ( g == null ) {
            g = getGraphCreate(graphNode);
            if ( g != null )
                graphs.put(graphNode, g);
        }
        return g;
    }

    /** Called from getGraph when a nonexistent graph is asked for.
     * Return null for "nothing created as a graph".
     * Sub classes can reimplement this.
     */
    protected Graph getGraphCreate(Node graphNode) {
        Graph g = graphMaker.create(graphNode) ;
        if ( g == null )
            throw new ARQException("Can't make new graphs") ;
        return g ;
    }

    @Override
    public long size() {
        return graphs.size();
    }
}
