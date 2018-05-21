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
import org.apache.jena.sparql.graph.GraphZero;

/** An always empty {@link DatasetGraph}. 
 * One graph (the default graph) with zero triples.
 * No changes allowed - this is not a sink.
 * @see DatasetGraphSink
 */
public class DatasetGraphZero extends DatasetGraphBaseFind {

    // Invariant DatasetGraph; it does have tarnsaction state so new object here.
    public static DatasetGraph create() { return new DatasetGraphZero(); }
    
    private Graph dftGraph = GraphZero.instance();

    public DatasetGraphZero() {}
    
    private Transactional txn                           = TransactionalNull.create();
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
    public void add(Quad quad) { unsupportedMethod(this, "add") ; } 
    
    @Override
    public void delete(Quad quad) { unsupportedMethod(this, "delete") ; }
    
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        throw new UnsupportedOperationException("deleteAny");
    }

    @Override
    public void setDefaultGraph(Graph g) {
        throw new UnsupportedOperationException("setDefaultGraph");
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        throw new UnsupportedOperationException("addGraph");
    }

    @Override
    public void removeGraph(Node graphName) {
        throw new UnsupportedOperationException("removeGraph");
    }
}
