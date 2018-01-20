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

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker ;
import org.apache.jena.sparql.graph.GraphUnionRead ;

/** Implementation of a DatasetGraph as an extensible set of graphs.
 *  <p>
 *  Graphs are held by reference. Care is needed when manipulating their contents
 *  especially if they are also in another {@code DatasetGraph}.
 *  <p> 
 *  See {@link DatasetGraphMap} for an implementation that copies graphs
 *  and so providing better isolation.
 *  <p>
 *  This class is best used for creating views  
 *  
 *  @see DatasetGraphMap
 */
public class DatasetGraphMapLink extends DatasetGraphCollection
{
    
    private final GraphMaker graphMaker ;
    private final Map<Node, Graph> graphs = new HashMap<>() ;

    private Graph defaultGraph ;

    /**
     * Create a new {@code DatasetGraph} that copies the dataset structure of default
     * graph and named graph and links to the graphs of the original {@code DatasetGraph}.
     * Any new graphs needed are separate from the original dataset and created in-memory. 
     */
    public static DatasetGraph cloneStructure(DatasetGraph dsg) {
        return new DatasetGraphMapLink(dsg);
    }
    
    /**
     * Create a new {@code DatasetGraph} that copies the dataset structure of default
     * graph and named graph and links to the graphs of the original {@code DatasetGraph}
     * Any new graphs needed are separate from the original dataset and created according
     * to the {@link GraphMaker}.
     */
    public static DatasetGraph cloneStructure(DatasetGraph dsg, GraphMaker graphMaker) {
        return new DatasetGraphMapLink(dsg, graphMaker);
    }

    /** Create a new DatasetGraph that initially shares the graphs of the
     * given DatasetGraph.  Adding/removing graphs will only affect this
     * object, not the argument DatasetGraph but changes to shared
     * graphs are seen by both objects.
     */
    private DatasetGraphMapLink(DatasetGraph dsg, GraphMaker graphMaker) {
        this.graphMaker = graphMaker ; 
        this.defaultGraph = dsg.getDefaultGraph() ;
        for ( Iterator<Node> names = dsg.listGraphNodes() ; names.hasNext() ; ) {
            Node gn = names.next() ;
            addGraph(gn, dsg.getGraph(gn)) ;
        }
    }

    /** 
     *  A {@code DatasetGraph} with graphs for default and named graphs as given
     *  but new graphs are created in memory.
     */
    private DatasetGraphMapLink(DatasetGraph dsg) {
        this(dsg, DatasetGraphFactory.graphMakerMem) ;
    }

    private DatasetGraphMapLink(Graph dftGraph, GraphMaker graphMaker) {
        this.graphMaker = graphMaker;
        this.defaultGraph = dftGraph ;
    }
    
//    /** A {@code DatasetGraph} with in-memory graphs for default and named graphs as needed */ 
//    private DatasetGraphMapLink() {
//        this(DatasetGraphFactory.memGraphMaker) ; 
//    }
    
    /**
     * A {@code DatasetGraph} with graph from the gve {@link GraphMaker} for default and
     * named graphs as needed. This is the constructor used for
     * DatasetFactory.createGeneral.
     */
    /*package*/ DatasetGraphMapLink(GraphMaker graphMaker) {
        this(graphMaker.create(null), graphMaker) ;
    }

    /** A {@code DatasetGraph} that uses the given graph for the default graph
     *  and create in-memory graphs for named graphs as needed
     */
    public DatasetGraphMapLink(Graph dftGraph) {
        this.defaultGraph = dftGraph ;
        this.graphMaker = DatasetGraphFactory.graphMakerMem ;
    }

    // ----
    private final Transactional txn                     = TransactionalLock.createMRSW() ;

    @Override
    public void commit() {
        SystemARQ.sync(this);
        txn.commit() ;
    }

    @Override public void begin()                       { txn.begin(); }
    @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
    @Override public boolean promote()                  { return txn.promote(); }
    //@Override public void commit()                      { txn.commit(); }
    @Override public void abort()                       { txn.abort(); }
    @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
    @Override public void end()                         { txn.end(); }
    @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
    @Override public TxnType transactionType()          { return txn.transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return false; }
    // ----

    @Override
    public boolean containsGraph(Node graphNode) {
        if ( Quad.isDefaultGraph(graphNode) || Quad.isUnionGraph(graphNode) )
            return true;
        return graphs.containsKey(graphNode);
    }

    @Override
    public Graph getDefaultGraph() {
        return defaultGraph;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        // Same as DatasetMap.getGraph but we inherit differently.
        if ( Quad.isUnionGraph(graphNode) ) 
            return new GraphUnionRead(this) ;
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
     * Return null for "nothing created as a graph"
     */
    protected Graph getGraphCreate(Node graphNode) { 
        return graphMaker.create(graphNode) ;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        graphs.put(graphName, graph);
    }

    @Override
    public void removeGraph(Node graphName) {
        graphs.remove(graphName);
    }

    @Override
    public void setDefaultGraph(Graph g) {
        defaultGraph = g;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return graphs.keySet().iterator();
    }

    @Override
    public long size() {
        return graphs.size();
    }

    @Override
    public void close() {
        defaultGraph.close();
        for ( Graph graph : graphs.values() )
            graph.close();
        super.close();
    }
}
