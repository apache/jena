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

import static org.apache.jena.sparql.util.graph.GraphUtils.triples2quadsDftGraph;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.NullIterator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.sparql.graph.GraphOps;
import org.apache.jena.sparql.graph.GraphZero;

/** DatasetGraph of a single graph as default graph.
 * <p>
 *  Fixed as one graph (the default) - named graphs can notbe added nor the default graph changed, only the contents modified. 
 *  <p>
 *  Ths dataset passes transactions down to a nominated backing {@link DatasetGraph}
 *  <p>
 *  It is particular suitable for use with an interference graph.
 */
public class DatasetGraphOne extends DatasetGraphBaseFind {
    private final Graph graph;
    private final DatasetGraph backingDGS;
    private final Transactional txn;
    private final boolean supportsAbort;

    public static DatasetGraph create(Graph graph) {
        // Find the deepest graph, the one that may be attached to a DatasetGraph.
        Graph graph2 = unwrap(graph);
        if ( graph2 instanceof GraphView ) {
            // This becomes a simple class that passes all transaction operations the
            // underlying dataset and masks the fact here are other graphs in the storage.
            return new DatasetGraphOne(graph, ((GraphView)graph2).getDataset());
        }
        // Didn't find a GraphView so no backing DatasetGraph; work on the graph as given.
        return new DatasetGraphOne(graph);
    }
    
    private static Graph unwrap(Graph graph) {
        for (;;) {
            if ( graph instanceof InfGraph ) {
                graph = ((InfGraph)graph).getRawGraph();
                continue;
            }
            Graph graph2 = GraphOps.unwrapOne(graph);
            if ( graph2 == graph )
                return graph;
            graph = graph2;
        }
    }
    
    private DatasetGraphOne(Graph graph, DatasetGraph backing) {
        this.graph = graph;
        backingDGS = backing;
        supportsAbort = backing.supportsTransactionAbort();
        txn = backing;
    }
    
    private DatasetGraphOne(Graph graph) {
        // Not GraphView which was hanled in create(Graph). 
        this.graph = graph;
        txn = new TxnDataset2Graph(graph);
        //txn = TransactionalLock.createMRSW();
        backingDGS = null;
        // Don't advertise the fact but TxnDataset2Graph tries to provide abort.
        // We can not guarantee it though because a plain, non-TIM, 
        // memory graph does not support abort.
        supportsAbort = false;
    }
    
    @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
    @Override public void commit()                      { txn.commit(); }
    @Override public boolean promote(Promote txnType)   { return txn.promote(txnType); }
    @Override public void abort()                       { txn.abort(); }
    @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
    @Override public void end()                         { txn.end(); }
    @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
    @Override public TxnType transactionType()          { return txn.transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return supportsAbort; }
    
    @Override
    public boolean containsGraph(Node graphNode) {
        if ( isDefaultGraph(graphNode) )
            return true;
        return false;
    }

    @Override
    public Graph getDefaultGraph() {
        return graph;
    }
    
    @Override
    public Graph getUnionGraph() {
        return GraphZero.instance();
    }

    @Override
    public Graph getGraph(Node graphNode) {
        if ( isDefaultGraph(graphNode) )
            return getDefaultGraph();
        if ( Quad.isUnionGraph(graphNode) )
            return getUnionGraph();
        return null;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return new NullIterator<>();
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(g) )
            graph.add(new Triple(s, p, o));
        else
            unsupportedMethod(this, "add(named graph)");
    }

    @Override
    public void add(Quad quad) {
        if ( isDefaultGraph(quad) )
            graph.add(quad.asTriple());
        else
            unsupportedMethod(this, "add(named graph)");
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(g) )
            graph.delete(new Triple(s, p, o));
        else
            unsupportedMethod(this, "delete(named graph)");
    }

    @Override
    public void delete(Quad quad) {
        if ( isDefaultGraph(quad) )
            graph.delete(quad.asTriple());
        else
            unsupportedMethod(this, "delete(named graph)");
    }

    @Override
    public void setDefaultGraph(Graph g) {
        unsupportedMethod(this, "setDefaultGraph");
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        unsupportedMethod(this, "addGraph");
    }

    @Override
    public void removeGraph(Node graphName) {
        unsupportedMethod(this, "removeGraph");
    }

    // -- Not needed -- implement find(g,s,p,o) directly.
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        return triples2quadsDftGraph(graph.find(s, p, o));
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        // There are no named graphs
        return Iter.nullIterator();
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        // There are no named graphs
        return Iter.nullIterator();
    }

    protected static boolean isDefaultGraph(Quad quad) {
        return isDefaultGraph(quad.getGraph());
    }

    protected static boolean isDefaultGraph(Node quadGraphNode) {
        return (quadGraphNode == null || Quad.isDefaultGraph(quadGraphNode));
    }

    // It's just easier and more direct ...
    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        if ( isWildcard(g) || isDefaultGraph(g) )
            return triples2quadsDftGraph(graph.find(s, p, o));
        else
            return new NullIterator<>();
    }

    @Override
    public void close() {
        graph.close();
        super.close();
    }
}
