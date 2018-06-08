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

package org.apache.jena.tdb2.store;

import static org.apache.jena.sparql.util.graph.GraphUtils.triples2quads ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.TransactionalMonitor;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.lib.NodeLib;
import org.apache.jena.tdb2.setup.StoreParams;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.sys.StoreConnection;
/** This is the class that provides creates a dataset over the storage via
 *  TripleTable, QuadTable and prefixes.
 *  This is the coordination of the storage.
 *  @see DatasetGraphTxn for the Transaction form.
 */
final
public class DatasetGraphTDB extends DatasetGraphTriplesQuads
                             implements DatasetGraphTxn, Sync, Closeable
{
    private StorageTDB storage; 
//    // SWITCHING.
//    private TripleTable tripleTable ;
//    private QuadTable quadTable ;
//    private DatasetPrefixStorage prefixes ;
//    private Location location ;
//    private StoreParams storeParams ;
//    // SWITCHING.
    private TransactionalSystem txnSystem ;
    private final ReorderTransformation transform ;
    
    private GraphTDB defaultGraphTDB ;
    private final boolean checkForChange = false ;
    private boolean closed = false ;

    /** Application should not create a {@code DatasetGraphTDB} directly */
    public DatasetGraphTDB(TransactionalSystem txnSystem, 
                           TripleTable tripleTable, QuadTable quadTable, DatasetPrefixesTDB prefixes,
                           ReorderTransformation transform, Location location, StoreParams params) {
        reset(txnSystem, tripleTable, quadTable, prefixes, location, params) ;
        this.transform = transform ;
        this.defaultGraphTDB = getDefaultGraphTDB() ;
    }

    public void reset(TransactionalSystem txnSystem,
                      TripleTable tripleTable, QuadTable quadTable, DatasetPrefixesTDB prefixes,
                      Location location, StoreParams params) {
        this.txnSystem = txnSystem ;
        this.storage = new StorageTDB(tripleTable, quadTable, prefixes, location, params);
        this.defaultGraphTDB = getDefaultGraphTDB();
    }
    
    public QuadTable getQuadTable()         { checkNotClosed(); return storage.quadTable; }
    public TripleTable getTripleTable()     { checkNotClosed(); return storage.tripleTable; }

    /** Low level manipulation. */
    public StorageTDB getStorage()              { return storage; }
    /** Low level manipulation. 
     * <b>Do not use unless in exclusive mode.</b>
     */
    public void setStorage(StorageTDB storage)  { this.storage = storage; }
    
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        checkNotClosed() ;
        return isolate(triples2quadsDftGraph(getTripleTable().find(s, p, o))) ;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        checkNotClosed();
        return isolate(getQuadTable().find(g, s, p, o));
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        checkNotClosed();
        return isolate(getQuadTable().find(Node.ANY, s, p, o));
    }

    protected Iterator<Quad> triples2quadsDftGraph(Iterator<Triple> iter)
    { return isolate(triples2quads(Quad.defaultGraphIRI, iter)); }
 
    private static final boolean CHECK_TXN = true; 
    
    private <T> Iterator<T> isolate(Iterator<T> iterator) {
        if ( txnSystem.isInTransaction() ) {
            TxnId txnId = txnSystem.getThreadTransaction().getTxnId();
            // Add transaction protection.
            return new IteratorTxnTracker<>(iterator, txnSystem, txnId);
        }
        // Risk the hidden arraylist is copied on growth.
        return Iter.iterator(iterator);
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o) { 
        checkNotClosed() ;
        requireWriteTxn() ;
        notifyAdd(null, s, p, o) ;
        getTripleTable().add(s,p,o) ;
    }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {
        checkNotClosed() ;
        requireWriteTxn() ;
        notifyAdd(g, s, p, o) ;
        getQuadTable().add(g, s, p, o) ; 
    }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {
        checkNotClosed() ;
        requireWriteTxn() ;
        notifyDelete(null, s, p, o) ;
        getTripleTable().delete(s, p, o) ;
    }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {
        checkNotClosed() ;
        requireWriteTxn() ;
        notifyDelete(g, s, p, o) ;
        getQuadTable().delete(g, s, p, o) ;
    }

    // Promotion
    /*package*/ void requireWriteTxn() {
        Transaction txn = txnSystem.getThreadTransaction() ;
        if ( txn == null )
            throw new TransactionException("Not in a transaction") ;
        if ( txn.isWriteTxn() )
            return ;
        boolean b = promote() ;
        if ( !b )
            throw new TransactionException("Can't write") ;
    }

    // TODO ?? Optimize by integrating with add/delete operations.
    private final void notifyAdd(Node g, Node s, Node p, Node o) {
        if ( monitor == null )
            return ;
        QuadAction action = QuadAction.ADD ;
        if ( checkForChange ) {
            if ( contains(g,s,p,o) )
                action = QuadAction.NO_ADD ;
        }
        monitor.change(action, g, s, p, o);
    }

    private final void notifyDelete(Node g, Node s, Node p, Node o) {
        if ( monitor == null )
            return ;
        QuadAction action = QuadAction.DELETE ;
        if ( checkForChange ) {
            if ( ! contains(g,s,p,o) )
                action = QuadAction.NO_DELETE ;
        }
        monitor.change(action, g, s, p, o);
    }
    
    /** No-op. There is no need to close datasets.
     *  Use {@link StoreConnection#release(Location)}.
     *  (Datasets can not be reopened on MS Windows). 
     */
    @Override
    public void close() {
        if ( closed )
            return ;
        closed = true ;
    }
    
    private void checkNotClosed() {
        if ( closed )
            throw new TDBException("dataset closed") ;
    }
    
    /** Release resources.
     *  Do not call directly - this is called from StoreConnection.
     *  Use {@link StoreConnection#release(Location)}. 
     */
    public void shutdown() {
        close();
        storage.tripleTable.close() ;
        storage.quadTable.close() ;
        storage.prefixes.close();
        txnSystem.getTxnMgr().shutdown(); 
    }
    
    @Override
    // Empty graphs don't "exist" 
    public boolean containsGraph(Node graphNode) {
        checkNotClosed() ; 
        if ( Quad.isDefaultGraphExplicit(graphNode) || Quad.isUnionGraph(graphNode)  )
            return true ;
        return _containsGraph(graphNode) ; 
    }

    private boolean _containsGraph(Node graphNode) {
        // Have to look explicitly, which is a bit of a nuisance.
        // But does not normally happen for GRAPH <g> because that's rewritten to quads.
        // Only pattern with complex paths go via GRAPH. 
        Iterator<Tuple<NodeId>> x = storage.quadTable.getNodeTupleTable().findAsNodeIds(graphNode, null, null, null) ;
        if ( x == null )
            return false ; 
        boolean result = x.hasNext() ;
        return result ;
    }
    
    @Override
    public void addGraph(Node graphName, Graph graph) {
        checkNotClosed() ; 
        removeGraph(graphName) ;
        GraphUtil.addInto(getGraph(graphName), graph) ;
    }

    @Override
    public final void removeGraph(Node graphName) {
        checkNotClosed() ; 
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY) ;
    }

    @Override
    public Graph getDefaultGraph() {
        checkNotClosed() ; 
        return new GraphTDB(this, null) ; 
    }

    public GraphTDB getDefaultGraphTDB() {
        checkNotClosed();
        return (GraphTDB)getDefaultGraph();
    }

    @Override
    public Graph getUnionGraph() {
        return getGraph(Quad.unionGraph);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        checkNotClosed();
        return new GraphTDB(this, graphNode);
    }

    public GraphTDB getGraphTDB(Node graphNode) {
        checkNotClosed();
        return (GraphTDB)getGraph(graphNode);
    }

    public StoreParams getStoreParams() {
        checkNotClosed();
        return storage.storeParams;
    }

    public ReorderTransformation getReorderTransform() {
        checkNotClosed();
        return transform;
    }

    public DatasetPrefixStorage getPrefixes() {
        checkNotClosed();
        // Need for requireWriteTxn
        storage.prefixes.setDatasetGraphTDB(this);
        return storage.prefixes;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        checkNotClosed();
        Iterator<Tuple<NodeId>> x = storage.quadTable.getNodeTupleTable().findAll();
        Iterator<NodeId> z = Iter.iter(x).map(t -> t.get(0)).distinct();
        return NodeLib.nodes(storage.quadTable.getNodeTupleTable().getNodeTable(), z);
    }

    @Override
    public long size() {
        checkNotClosed();
        return Iter.count(listGraphNodes());
    }

    @Override
    public boolean isEmpty() {
        checkNotClosed();
        return getTripleTable().isEmpty() && getQuadTable().isEmpty();
    }

    @Override
    public void clear() {
        checkNotClosed() ; 
        // Leave the node table alone.
        getTripleTable().clearTriples() ;
        getQuadTable().clearQuads() ;
    }
    
    public NodeTupleTable chooseNodeTupleTable(Node graphNode) {
        checkNotClosed() ; 

        if ( graphNode == null || Quad.isDefaultGraph(graphNode) )
            return getTripleTable().getNodeTupleTable() ;
        else
            // Includes Node.ANY and union graph
            return getQuadTable().getNodeTupleTable() ;
    }
    
    private static final int sliceSize = 1000 ;
    
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        // Delete in batches.
        // That way, there is no active iterator when a delete
        // from the indexes happens.
        checkNotClosed() ;
        
        if ( monitor != null ) {
            // Need to do by nodes because we will log the deletes.
            super.deleteAny(g, s, p, o); 
            return ;
        }

        // Not logging - do by working as NodeIds.
        NodeTupleTable t = chooseNodeTupleTable(g) ;
        @SuppressWarnings("unchecked")
        Tuple<NodeId>[] array = (Tuple<NodeId>[])new Tuple<?>[sliceSize] ;

        while (true) { // Convert/cache s,p,o?
            // The Node Cache will catch these so don't worry unduely.
            Iterator<Tuple<NodeId>> iter = null ;
            if ( g == null )
                iter = t.findAsNodeIds(s, p, o) ;
            else
                iter = t.findAsNodeIds(g, s, p, o) ;

            if ( iter == null || ! iter.hasNext() )
                return ;

            // Get a slice
            int len = 0 ;
            for (; len < sliceSize; len++) {
                if ( !iter.hasNext() )
                    break ;
                array[len] = iter.next() ;
            }
            
            // Delete the NodeId Tuples
            for (int i = 0; i < len; i++) {
                t.getTupleTable().delete(array[i]) ;
                array[i] = null ;
            }
            // Finished?
            if ( len < sliceSize )
                break ;
        }
    }
    
    public Location getLocation()       { return storage.location ; }

    /**
     * Cause an exception to be thrown if sync is called.
     * For TDB2, which is transactional only, so sync isn't a useful operation.
     * It is implemented for completness and no more.
     */
    public static boolean exceptionOnSync = true ;

    @Override
    public void sync() {
        if ( exceptionOnSync )
            throw new JenaTransactionException("sync called");
        checkNotClosed();
        syncStorage();
    }

    // Sync for internal puposes.
    public void syncStorage() {
        storage.tripleTable.sync();
        storage.quadTable.sync();
        storage.prefixes.sync();
    }

    @Override
    public void setDefaultGraph(Graph g) { 
        throw new UnsupportedOperationException("Can't set default graph on a TDB-backed dataset") ;
    }

    @Override
    public boolean isInTransaction() {
        return txnSystem.isInTransaction() ;
    }
    
    @Override
    public ReadWrite transactionMode() {
        return txnSystem.transactionMode(); 
    }

    @Override
    public TxnType transactionType() {
        return txnSystem.transactionType();
    }

    // txnSystem with monitor?
    @Override
    public void begin(TxnType txnType) {
        if ( txnMonitor != null ) txnMonitor.startBegin(txnType); 
        txnSystem.begin(txnType);
        if ( txnMonitor != null ) txnMonitor.finishBegin(txnType);
    }
    
    @Override
    public void begin(ReadWrite readWrite) {
        begin(TxnType.convert(readWrite));
    }

    @Override
    public boolean promote() {
        if ( txnMonitor != null ) txnMonitor.startPromote();
        try { 
            return txnSystem.promote() ;
        } finally { 
            if ( txnMonitor != null ) txnMonitor.finishPromote();
        }
    }

    @Override
    public boolean promote(Promote txnType) {
        if ( txnMonitor != null ) txnMonitor.startPromote();
        try { 
            return txnSystem.promote(txnType) ;
        } finally { 
            if ( txnMonitor != null ) txnMonitor.finishPromote();
        }
    }

    @Override
    public void commit() {
        if ( txnMonitor != null ) txnMonitor.startCommit();
        txnSystem.commit();
        if ( txnMonitor != null ) txnMonitor.finishCommit();  
    }

    @Override
    public void abort() {
        if ( txnMonitor != null ) txnMonitor.startAbort() ; 
        txnSystem.abort();
        if ( txnMonitor != null ) txnMonitor.finishAbort() ;  
    }

    @Override
    public void end() {
        if ( txnMonitor != null ) txnMonitor.startEnd(); 
        txnSystem.end() ;
        if ( txnMonitor != null ) txnMonitor.finishEnd(); 
    }

    public TransactionalSystem getTxnSystem() {
        return txnSystem ;
    }

    // Watching changes (add, delete, deleteAny) 
    
    private DatasetChanges monitor = null ;
    public void setMonitor(DatasetChanges changes) {
        monitor = changes ;
    }

    public void removeMonitor(DatasetChanges changes) {
        if ( monitor != changes )
            throw new InternalErrorException() ;
        monitor = null ;
    }
    
    // Watching Transactional
    
    private TransactionalMonitor txnMonitor = null ;
    public void setTransactionalMonitor(TransactionalMonitor changes) {
        txnMonitor = changes ;
    }

    public void removeTransactionalMonitor(TransactionalMonitor changes) {
        if ( txnMonitor != changes )
            throw new InternalErrorException() ;
        txnMonitor = null ;
    }
    
}
