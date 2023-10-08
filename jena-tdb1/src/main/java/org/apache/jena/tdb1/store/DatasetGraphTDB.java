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

package org.apache.jena.tdb1.store;


import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraphTriplesQuads ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Transactional ;
import org.apache.jena.sparql.core.TransactionalNotSupported ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.lib.NodeLib;
import org.apache.jena.tdb1.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb1.transaction.DatasetGraphTransaction;
import org.apache.jena.tdb1.transaction.DatasetGraphTxn;

/** This is the class that creates a dataset over the storage.
 *  The name is historical. "{@code TDBStorage}" might be better nowadays.
 * <p> This class is not {@code Transactional}. It is used within the TDB transaction system.
 * <p>
 *  See also:
 *  <ul>
 *  <li>{@link DatasetGraphTxn} &ndash; the subclass that provides a single transaction</li>
 *  <li>{@link DatasetGraphTransaction} &ndash; class that provides the application with the right DatasetGraphTDB (base or transaction).</li>
 *  </ul>
 */
final
public class DatasetGraphTDB extends DatasetGraphTriplesQuads
                             implements /*DatasetGraph,*/ Sync, Closeable
{
    private TripleTable tripleTable ;
    private QuadTable quadTable ;
    private DatasetPrefixesTDB prefixes ;
    private final ReorderTransformation transform ;
    private final StorageConfig config ;

    private boolean closed = false ;

    public DatasetGraphTDB(TripleTable tripleTable, QuadTable quadTable, DatasetPrefixesTDB prefixes,
                           ReorderTransformation transform, StorageConfig config) {
        this.tripleTable = tripleTable ;
        this.quadTable = quadTable ;
        this.prefixes = prefixes ;
        this.transform = transform ;
        this.config = config ;
    }

    public QuadTable getQuadTable()         { return quadTable ; }
    public TripleTable getTripleTable()     { return tripleTable ; }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    { return G.triples2quadsDftGraph(getTripleTable().find(s, p, o)) ; }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    { return getQuadTable().find(g, s, p, o) ; }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    { return getQuadTable().find(Node.ANY, s, p, o) ; }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    { getTripleTable().add(s,p,o) ; }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    { getQuadTable().add(g, s, p, o) ; }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o)
    { getTripleTable().delete(s,p,o) ; }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o)
    { getQuadTable().delete(g, s, p, o) ; }

    public GraphTDB getDefaultGraphTDB()
    { return (GraphTDB)getDefaultGraph() ; }

    public GraphTDB getGraphTDB(Node graphNode)
    { return (GraphTDB)getGraph(graphNode) ; }

    @Override
    public void close() {
        if ( closed )
            return ;
        closed = true ;

        tripleTable.close() ;
        quadTable.close() ;
        prefixes.close();
        // Which will cause reuse to throw exceptions early.
        tripleTable = null ;
        quadTable = null ;
        prefixes = null ;
    }

    @Override
    // Empty graphs don't "exist"
    public boolean containsGraph(Node graphNode) {
        if ( Quad.isDefaultGraph(graphNode) || Quad.isUnionGraph(graphNode)  )
            return true ;
        // Have to look explicitly, which is a bit of a nuisance.
        // But does not normally happen for GRAPH <g> because that's rewritten to quads.
        // Only pattern with complex paths go via GRAPH.
        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().findAsNodeIds(graphNode, null, null, null) ;
        if ( x == null )
            return false ;

//        NodeId graphNodeId = quadTable.getNodeTupleTable().getNodeTable().getNodeIdForNode(graphNode) ;
//        Tuple<NodeId> pattern = Tuple.create(graphNodeId, null, null, null) ;
//        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().getTupleTable().find(pattern) ;
        boolean result = x.hasNext() ;
        return result ;
    }

    // These should not be called in normal use - they are intercepted by DatasetGraphTransaction.

    @Override
    public Graph getDefaultGraph()
    { return new GraphNonTxnTDB(this, null) ; }

    @Override
    public Graph getUnionGraph()
    { return getGraph(Quad.unionGraph); }

    @Override
    public Graph getGraph(Node graphNode)
    { return new GraphNonTxnTDB(this, graphNode) ; }

    @Override
    public void removeGraph(Node graphNode) {
        deleteAny(graphNode, Node.ANY, Node.ANY, Node.ANY) ;
        if ( graphNode.isURI() )
            getStoragePrefixes().removeAllFromPrefixMap(graphNode.getURI());
    }

    public StorageConfig getConfig()                        { return config ; }

    public ReorderTransformation getReorderTransform()      { return transform ; }

    @Override
    public PrefixMap prefixes() {
        return getStoragePrefixes().getPrefixMap();
    }

    public DatasetPrefixesTDB getStoragePrefixes()                 { return prefixes ; }

    @Override
    public Iterator<Node> listGraphNodes()
    {
        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().findAll() ;
        Iterator<NodeId> z =  Iter.iter(x).map(t -> t.get(0)).distinct() ;
        return NodeLib.nodes(quadTable.getNodeTupleTable().getNodeTable(), z) ;
    }

    @Override
    public long size()                   { return Iter.count(listGraphNodes()) ; }

    @Override
    public boolean isEmpty()            { return getTripleTable().isEmpty() && getQuadTable().isEmpty() ; }

    @Override
    public void clear()
    {
        // Leave the node table alone.
        getTripleTable().clearTriples() ;
        getQuadTable().clearQuads() ;
    }

    public NodeTupleTable chooseNodeTupleTable(Node graphNode)
    {
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

        boolean isDftGraph = (g==null) || Quad.isDefaultGraph(g);

        NodeTupleTable t = chooseNodeTupleTable(g) ;
        @SuppressWarnings("unchecked")
        Tuple<NodeId>[] array = (Tuple<NodeId>[])new Tuple<?>[sliceSize] ;

        while (true) { // Convert/cache s,p,o?
            // The Node Cache will cache these so don't worry unduly.
            Iterator<Tuple<NodeId>> iter = null ;
            if ( isDftGraph )
                iter = t.findAsNodeIds(s, p, o) ;
            else
                iter = t.findAsNodeIds(g, s, p, o) ;

            if ( iter == null )
                // Finished?
                return ;

            // Get a slice
            int len = 0 ;
            for (; len < sliceSize; len++) {
                if ( !iter.hasNext() )
                    break ;
                array[len] = iter.next() ;
            }

            // Delete them.
            for (int i = 0; i < len; i++) {
                t.getTupleTable().delete(array[i]) ;
                array[i] = null ;
            }
            // Finished?
            if ( len < sliceSize )
                break ;
        }
    }

    public Location getLocation()       { return config.location ; }

    @Override
    public void sync()
    {
        tripleTable.sync() ;
        quadTable.sync() ;
        prefixes.sync() ;
    }

    @Override
    public String toString() {
        return "TDB1: "+getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this));
    }

    private final Transactional txn                     = new TransactionalNotSupported() ;
    @Override public void begin()                       { txn.begin(); }
    @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
    @Override public boolean promote(Promote txnType)   { return txn.promote(txnType); }
    @Override public void commit()                      { txn.commit(); }
    @Override public void abort()                       { txn.abort(); }
    @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
    @Override public void end()                         { txn.end(); }
    @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
    @Override public TxnType transactionType()          { return txn.transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return false; }
}
