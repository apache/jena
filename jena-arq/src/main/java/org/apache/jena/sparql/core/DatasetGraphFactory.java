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

import java.util.Iterator ;
import java.util.Objects ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.graph.GraphFactory ;

public class DatasetGraphFactory
{
    /** Create an in-memory {@link Dataset}.
     * <p>
     * See also {@link #createTxnMem()} for a transactional dataset.
     * <p>
     * This implementation copies models when {@link Dataset#addNamedModel(String, Model)} is called.
     * <p>
     * This implementation provides "best effort" transactions; it only provides MRSW locking.
     * Use {@link #createTxnMem} for a proper in-memory transactional {@code DatasetGraph}.
     * 
     * @see #createTxnMem
     */
    public static DatasetGraph create() {
        return new DatasetGraphMap() ;
    }

    /**
     * Create an in-memory, transactional {@link Dataset}.
     * <p> 
     * This fully supports transactions, including abort to roll-back changes.
     * It provides "autocommit" if operations are performed
     * outside a transaction but with a performance impact
     * (the implementation adds a begin/commit around each add or delete
     * so overheads can accumulate).
     * 
     * @return a transactional, in-memory, modifiable DatasetGraph
     */
    public static DatasetGraph createTxnMem() { return new DatasetGraphInMemory(); }

    /**
     * Create a general-purpose  {@link Dataset}.<br/>
     * Any graphs needed are in-memory unless explicitly added with {@link Dataset#addNamedModel}.
     * </p>
     * This dataset type can contain graphs from any source when added via {@link Dataset#addNamedModel}.
     * These are held as links to the supplied graph and not copied.
     * <p> 
     * <em>This dataset does not support the graph indexing feature of jena-text.</em>
     * <p>
     * This dataset does not support serialized transactions (it only provides MRSW locking). 
     * <p>
     * 
     * @see #createTxnMem
     * @return a general-purpose Dataset
     */
    public static DatasetGraph createGeneral() { 
        return new DatasetGraphMapLink(graphMakerMem.create(null), graphMakerMem) ;
    }

    /** Create an in-memory {@link Dataset}.
     * <p>
     * See also {@link #createTxnMem()} for a transactional dataset.
     * <p>
     * Use {@link #createGeneral()} when needing to add graphs with mixed characteristics, 
     * e.g. inference graphs, or specific graphs from TDB.
     * <p>    
     * <em>It does not support the graph indexing feature of jena-text.</em>
     * <p>
     * <em>This factory operation is marked "deprecated" because the general purpose "add named graph of any implementation"
     * feature will be removed; this feature is now provided by {@link #createGeneral()}.
     * </em>
     * @deprecated Prefer {@link #createTxnMem()} or {@link #create()} or, for special cases, {@link #createGeneral()}.
     * @see #createTxnMem
     */
   @Deprecated
    public static DatasetGraph createMem() { return createGeneral() ; }
    
    /** Create a DatasetGraph based on an existing one;
     *  this is a structure copy of the dataset structure
     *  but graphs are shared
     *  @deprecated Use {@link #cloneStructure}
     */
    @Deprecated
    public static DatasetGraph create(DatasetGraph dsg) {
        return cloneStructure(dsg) ;
    }
    
    /** 
     * Clone the structure of a {@link DatasetGraph}.
     */
    public static DatasetGraph cloneStructure(DatasetGraph dsg) {
        Objects.requireNonNull(dsg, "DatasetGraph must be provided") ;
        DatasetGraphMapLink dsg2 = new DatasetGraphMapLink(dsg.getDefaultGraph()) ;
        for ( Iterator<Node> names = dsg.listGraphNodes() ; names.hasNext() ; ) {
            Node gn = names.next() ;
            dsg2.addGraph(gn, dsg.getGraph(gn)) ;
        }
        return dsg2 ;
    }

    private static void copyOver(DatasetGraph dsgDest, DatasetGraph dsgSrc)
    {
        dsgDest.setDefaultGraph(dsgSrc.getDefaultGraph()) ;
        for ( final Iterator<Node> names = dsgSrc.listGraphNodes() ; names.hasNext() ; )
        {
            final Node gn = names.next() ;
            dsgDest.addGraph(gn, dsgSrc.getGraph(gn)) ;
        }
    }

    /**
     * Create a DatasetGraph starting with a single graph.
     * New graphs must be explicitly added.
     */
    public static DatasetGraph create(Graph graph) {
        return new DatasetGraphMapLink(graph) ;
    }

    /**
     * Create a DatasetGraph which only ever has a single default graph.
     */
    public static DatasetGraph wrap(Graph graph) { return DatasetGraphOne.create(graph) ; }

    
    /**
     * Create a DatasetGraph which only ever has a single default graph.
     * @deprecated Use {#wrap(Graph)} 
     */
    @Deprecated
    public static DatasetGraph createOneGraph(Graph graph) { return wrap(graph) ; }

    /** Interface for making graphs when a dataset needs to add a new graph.
     *  Return null for no graph created.
     */
    public interface GraphMaker { public Graph create(Node name) ; }

    /** A graph maker that doesn't make graphs. */
    public static GraphMaker graphMakerNull = (name) -> null ;

    /** A graph maker that creates unnamed Jena default graphs */ 
    public static GraphMaker graphMakerMem = (name) -> GraphFactory.createDefaultGraph() ;
    
    /** A graph maker that creates {@link NamedGraph}s around a Jena default graphs */ 
    public static GraphMaker graphMakerNamedGraphMem = (name) -> {
        Graph g = GraphFactory.createDefaultGraph() ;
        return new NamedGraphWrapper(name, g);
    };
}
