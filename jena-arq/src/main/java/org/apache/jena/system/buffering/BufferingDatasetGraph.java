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

import static org.apache.jena.sparql.core.Match.match;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.*;

/**
 * BufferingDatasetGraph - stores operations as adds/deletes of "triples"
 * (default graph) and "quads" (named graphs)
 */
public class BufferingDatasetGraph extends DatasetGraphTriplesQuads implements DatasetGraphBuffering {

    /*
     * When a buffering dataset is created, it is in state NONE. When operations
     * start (add, delete or any read operation like find()), there is a
     * PROMOTE_READ_COMMITTED on the underlying dataset. When flush is called, it
     * goes into WRITE and does a dataset transaction for the changes. When it
     * commits the transaction, goes into state NONE again.
     */
    private enum AccessState {
        NONE,
        READ,
        WRITE
    }

    private DatasetGraph baseDSG;
    protected DatasetGraph get()  { return baseDSG; }
    protected DatasetGraph getT() { return baseDSG; }

    // Track where we are in the none - read/write - none cycle.
    private AccessState accessState = AccessState.NONE;

    private int writeTxnCount = 0;
    private final int writeTxnLimit;

    private Set<Triple> addedTriples   = new HashSet<>();
    private Set<Triple> deletedTriples = new HashSet<>();

    private Set<Quad>   addedQuads     = new HashSet<>();
    private Set<Quad>   deletedQuads   = new HashSet<>();

    public Set<Triple> getAddedTriples()     { return addedTriples; }
    public Set<Triple> getDeletedTriples()   { return deletedTriples; }
    public Set<Quad>   getAddedQuads()       { return addedQuads; }
    public Set<Quad>   getDeletedQuads()     { return deletedQuads; }

    private BufferingPrefixMap prefixes;

    // True  -> read-optimized.
    // False -> write-optimized
    private static final boolean UNIQUE = true;

    /**
     * Create a BufferingDatasetGraph which buffers 1 transaction.
     */
    public BufferingDatasetGraph(DatasetGraph dsg) {
        this(dsg, 1);
    }

    /**
     * Create a BufferingDatasetGraph which buffers upto {@code txnBuffering} transactions.
     */
    public BufferingDatasetGraph(DatasetGraph dsg, int txnBuffering) {
        baseDSG = dsg;
        prefixes = new BufferingPrefixMap(dsg.prefixes());
        writeTxnLimit = txnBuffering;
    }

    public DatasetGraph base() { return baseDSG; }

    private void readOperation() {
        switch (accessState) {
            case NONE :
                if ( txn().isInTransaction() ) {
                    switch (txn().transactionMode()) {
                        case READ :
                            accessState = AccessState.READ;
                            break;
                        case WRITE :
                            accessState = AccessState.WRITE;
                            break;
                    }
                    return ;

                }
                // Start, outside a transaction. Start one, assuming updates will happen.
                base().begin(TxnType.READ_COMMITTED_PROMOTE);
                accessState = AccessState.READ;
                break;
            case READ :
            case WRITE :
                break;
        }
    }

    private void updateOperation() {
        switch (accessState) {
            case NONE :
                if ( txn().isInTransaction() ) {
                    accessState = AccessState.WRITE;
                    return ;
                }
                // Start, outside a transaction. Start one.
                base().begin(TxnType.WRITE);
                accessState = AccessState.WRITE;
                break;
            case READ :
                boolean b = base().promote();
                if ( !b )
                    throw new JenaTransactionException("Failed to promote transaction");
                accessState = AccessState.WRITE;
                break;
            case WRITE :
                break;
        }
    }

    public AccessState accessState() { return accessState; }

    /**
     * Send the changes to the underlying store and end the buffering session.
     */
    @Override
    public void flush() {
        switch(accessState) {
            case NONE :
                return;
            case READ :
                getT().end();
                break;
            case WRITE :
                flushToDB();
        }
        accessState = AccessState.NONE;
    }

    /**
     * Send the changes to the underlying store, drop the buffered changes that have
     * been flushed, but do not end the buffering session.
     */
    public void flushToDB() {
        base().executeWrite(()->{
            Graph dftGraph = base().getDefaultGraph();

            addedTriples.forEach(dftGraph::add);
            deletedTriples.forEach(dftGraph::delete);
            addedQuads.forEach(baseDSG::add);
            deletedQuads.forEach(baseDSG::delete);

            addedTriples.clear();
            deletedTriples.clear();
            addedQuads.clear();
            deletedQuads.clear();
            prefixes.flush();
            writeTxnCount = 0;
        });
    }

    public String state() {
        StringBuilder sb = new StringBuilder();
        sb.append("Triples").append("\n");
        sb.append("  Added:   "+addedTriples).append("\n");
        sb.append("  Deleted: "+deletedTriples).append("\n");
        sb.append("Quads").append("\n");
        sb.append("  Added:   "+addedQuads).append("\n");
        sb.append("  Deleted: "+addedQuads).append("\n");
        String x = prefixes.state();
        sb.append(x);
        return sb.toString();
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o) {
        updateOperation();
        Triple triple = Triple.create(s,p,o);
        DatasetGraph base = get();
        deletedTriples.remove(triple);
        if ( UNIQUE && base.getDefaultGraph().contains(triple) )
            return ;
        addedTriples.add(triple);
    }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {
        updateOperation();
        Quad quad = Quad.create(g,s,p,o);
        DatasetGraph base = get();
        deletedQuads.remove(quad);
        if ( UNIQUE && base.contains(quad) )
            return ;
        addedQuads.add(quad);
    }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {
        updateOperation();
        Triple triple = Triple.create(s,p,o);
        DatasetGraph base = get();
        addedTriples.remove(triple);
        if ( UNIQUE && ! base.getDefaultGraph().contains(triple) )
            return ;
        deletedTriples.add(triple);
    }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {
        updateOperation();
        Quad quad = Quad.create(g,s,p,o);
        DatasetGraph base = get();
        addedQuads.remove(quad);
        if ( UNIQUE && ! base.contains(quad) )
            return ;
        deletedQuads.add(quad);
    }

    // Via find() if not implemented
    @Override
    public boolean contains(Quad quad) {
        readOperation();
        return contains$(quad, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    // Via find() if not implemented
    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        readOperation();
        return contains$(null, g, s, p, o);
    }

    // Avoid recreating quads
    private boolean contains$(Quad quad, Node g, Node s, Node p, Node o) {
        // The find() pattern.
        if ( Quad.isDefaultGraph(g))
            return containedInDftGraph(g, s, p, o) ;
        if ( ! isWildcard(g) )
            return containedInNG(quad, g, s, p, o) ;
        return containedInAny(quad, g, s, p, o) ;
    }

    private boolean containedInDftGraph(Node g, Node s, Node p, Node o) {
        Triple t = Triple.create(s, p, o);
        if ( addedTriples.contains(t) )
            return true;
        if ( deletedTriples.contains(t) )
            return false;
        return get().contains(g,s,p,o);
    }

    private boolean containedInNG(Quad quad, Node g, Node s, Node p, Node o) {
        if ( quad == null )
            quad = Quad.create(g, s, p, o);
        if ( addedQuads.contains(quad) )
            return true;
        if ( deletedQuads.contains(quad) )
            return false;
        return get().contains(g,s,p,o);
    }

    private boolean containedInAny(Quad quad, Node g, Node s, Node p, Node o) {
        // Sufficient work that less point being locally clever.
        Iterator<Quad> iter = findAny(s,p,o);
        return iter.hasNext();
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        readOperation();
        DatasetGraph base = get();
        Iterator<Quad> extra = findInAddedTriples(s, p, o);
        Iter<Quad> iter =
            Iter.iter(base.find(Quad.defaultGraphIRI, s, p, o))
                .filter(q->! deletedQuads.contains(q))
                .append(extra);
        if ( ! UNIQUE )
            iter = iter.distinct();
        return iter;
    }

    private Iterator<Quad> findInAddedTriples(Node s, Node p, Node o) {
        return Iter.iter(addedTriples.iterator())
                    .filter(t->match(t,s,p,o))
                    .map(t->Quad.create(Quad.defaultGraphIRI,t));
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        readOperation();
        return findQuads(g, s, p, o);
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        readOperation();
        return findQuads(Node.ANY, s, p, o);
    }

    private Iterator<Quad> findQuads(Node g, Node s, Node p, Node o) {
        DatasetGraph base = get();
        Iterator<Quad> extra = findInAddedQuads(g, s, p, o);
        Iter<Quad> iter =
            Iter.iter(base.find(g, s, p, o))
                .filter(q->! deletedQuads.contains(q))
                .append(extra);
        if ( ! UNIQUE )
            iter = iter.distinct();
        return iter;
    }

    private Iterator<Quad> findInAddedQuads(Node g, Node s, Node p, Node o) {
        return Iter.iter(addedQuads.iterator())
                    .filter(t->match(t,g,s,p,o));
    }

    // Graphs: read/write operations will come back to the dataset.
    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(baseDSG, graphNode);
    }

    @Override
    public Graph getUnionGraph() {
        return GraphView.createUnionGraph(baseDSG);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        // Imperfect : contains "empty" graphs.
        Iterator<Node> iter1 = base().listGraphNodes();
        Set<Node> deleted = deletedQuads.stream().map(q->q.getGraph()).distinct().collect(Collectors.toSet());
        return Iter.iter(addedQuads).map(q->q.getGraph()).distinct();
    }

    @Override
    public PrefixMap prefixes() {
        // XXX Needs to share access state?
        return prefixes;
    }

    private final Transactional txn                     = TransactionalLock.createMRSW() ;
    protected final Transactional txn()                 { return getT(); }
    @Override public void begin()                       { txn().begin(); }
    @Override public void begin(TxnType txnType)        { txn().begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn().begin(mode); }

    @Override public void commit()                      {
        if ( txn().isInTransaction() && txn().transactionMode() == ReadWrite.WRITE )
            commitW();
        txn().commit();
    }

    private void commitW() {
        writeTxnCount++;
        if ( writeTxnCount >= writeTxnLimit ) {
            flush();
        }
    }
    @Override public boolean promote(Promote mode)      { return txn().promote(mode); }
    @Override public void abort()                       { txn().abort(); }
    @Override public boolean isInTransaction()          { return txn().isInTransaction(); }
    @Override public void end()                         { txn().end(); }
    @Override public ReadWrite transactionMode()        { return txn().transactionMode(); }
    @Override public TxnType transactionType()          { return txn().transactionType(); }
    @Override public boolean supportsTransactions()     { return get().supportsTransactions(); }
    @Override public boolean supportsTransactionAbort() { return get().supportsTransactionAbort(); }
}
