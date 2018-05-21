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

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.graph.GraphSink;


/** 
 * An always empty {@link DatasetGraph} that accepts changes but ignores them.
 * 
 * @see DatasetGraphZero - a DSG that does not allow changes.
 */
public class DatasetGraphSink extends DatasetGraphBaseFind {

    public static DatasetGraph create() { return new DatasetGraphSink(); }
    
    private Graph dftGraph = GraphSink.instance();
    
    public DatasetGraphSink() {}
    
    private Transactional txn                           = TransactionalNull.create();
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
    @Override public boolean supportsTransactionAbort() { return true; }
    
    @Override
    public Iterator<Node> listGraphNodes() {
        return Iter.nullIterator();
    }
    
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        return Iter.nullIterator();
    }
    
    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        return Iter.nullIterator();
    }
    
    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        return Iter.nullIterator();
    }
    
    @Override
    public Graph getDefaultGraph() {
        return dftGraph;
    }
    @Override
    public Graph getGraph(Node graphNode) {
        return null;
    }
    @Override
    public void add(Quad quad) { /* ignore */ } 
    
    @Override
    public void delete(Quad quad) { /* ignore */ }
    
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) { /* ignore */ }
    
    @Override
    public void setDefaultGraph(Graph g) { /* ignore */ }

    @Override
    public void addGraph(Node graphName, Graph graph) { /* ignore */ }

    @Override
    public void removeGraph(Node graphName) { /* ignore */ }
}
