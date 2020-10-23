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

package org.apache.jena.sdb.store;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.other.G;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.graph.GraphSDB ;
import org.apache.jena.sdb.util.StoreUtils ;
import org.apache.jena.shared.Lock ;
import org.apache.jena.shared.LockMRSW ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.util.Context ;

public class DatasetGraphSDB extends DatasetGraphTriplesQuads 
    implements DatasetGraph, Closeable
    /** SDB uses JDBC transactions, not Dataset transactions*/
{
    private final Store store ;
    private Lock lock = new LockMRSW() ;
    private final Context context ;
    private GraphSDB defaultGraph;
    
    public DatasetGraphSDB(Store store, Context context) {
        this(store, new GraphSDB(store), context);
    }

    public DatasetGraphSDB(Store store, GraphSDB graph, Context context) {
        this.store = store;
        // Force the "default" graph
        this.defaultGraph = graph;
        this.context = context;
    }

    public Store getStore() {
        return store;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return StoreUtils.storeGraphNames(store);
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        return StoreUtils.containsGraph(store, graphNode);
    }

    @Override
    public Graph getDefaultGraph() {
        return defaultGraph;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return new GraphSDB(store, graphNode);
    }
    
    // Use unsubtle versions (the bulk loader copes with large additions).
    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    { getDefaultGraph().add(new Triple(s, p, o)) ; }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    { getGraph(g).add(new Triple(s, p, o)) ; }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o)
    { getDefaultGraph().delete(new Triple(s, p, o)) ; }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o)
    { getGraph(g).delete(new Triple(s, p, o)) ; }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    { return G.triples2quadsDftGraph(LibSDB.findTriplesInDftGraph(this, s, p, o)) ; }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    { return LibSDB.findInQuads(this, Node.ANY, s, p, o) ; } 

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    { return LibSDB.findInQuads(this, g, s, p, o) ; }
    
    @Override
    public void close()
    { store.close() ; }

    // Transactions for SDB are an aspect of the JDBC connection not the dataset. 
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
    @Override public boolean supportsTransactions()     { return false; }
    @Override public boolean supportsTransactionAbort() { return false; }
}
