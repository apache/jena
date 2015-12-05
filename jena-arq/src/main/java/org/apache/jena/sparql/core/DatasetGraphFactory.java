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

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.graph.GraphFactory ;

public class DatasetGraphFactory
{
    /** Create an in-memory, non-transactional {@link DatasetGraph}.
     * This implementation copies the triples of an added graph into the dataset. 
     * <p>
     * See also {@link #createTxnMem()}
     * <br/>
     * See also {@link #createGeneral()}
     * 
     * @see #createTxnMem
     * @see #createGeneral
     */
    
    public static DatasetGraph create() {
        return new DatasetGraphCopyAdd(createGeneral()) ;
    }

    /**
     * @return a DatasetGraph which features transactional in-memory operation
     */
    public static DatasetGraph createTxnMem() { return new DatasetGraphInMemory(); }

    /** Create an in-memory, non-transactional DatasetGraph.
     * <p>
     * See also {@link #createTxnMem()} for a transactional dataset.
     * <p>
     * Use {@link #createGeneral()} when needing to add graphs with mixed characteristics, 
     * e.g. inference graphs, specific graphs from TDB.
     * <p>    
     * <em>This operation is marked "deprecated" because the general purpose 
     * "add named graph of any implementation"
     * feature will be removed; this feature is now provided by {@link #createGeneral()}.
     * </em>
     * @deprecated Prefer {@link #createGeneral()} or {@link #createTxnMem()}
     */
    @Deprecated
    public static DatasetGraph createMem() { return createGeneral() ; }
    
    /** Create a DatasetGraph based on an existing one;
     *  this is a structure copy of the dataset struture
     *  but graphs are shared
     *  @deprecated Use {@link #cloneStructure}
     */
    @Deprecated
    public static DatasetGraph create(DatasetGraph dsg) {
        return cloneStructure(dsg) ;
    }
    
    public static DatasetGraph cloneStructure(DatasetGraph dsg) {
        return new DatasetGraphMaker(dsg, memGraphMaker) ;
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
    public static DatasetGraph create(Graph graph)
    {
        final DatasetGraph dsg2 = createMemFixed() ;
        dsg2.setDefaultGraph(graph) ;
        return dsg2 ;
    }

    /**
     * Create a DatasetGraph which only ever has a single default graph.
     */
    public static DatasetGraph createOneGraph(Graph graph) { return new DatasetGraphOne(graph) ; }

    /** Interface for making graphs when a dataset needs to add a new graph.
     *  Return null for no graph created.
     */
    public interface GraphMaker { public Graph create() ; }

    /** A graph maker that doesn't make graphs */
    public static GraphMaker graphMakerNull = () -> null ;

    private static GraphMaker memGraphMaker = () -> GraphFactory.createDefaultGraph() ;

    /**
     * Create a general-purpose, non-transactional Dataset.<br/>
     * 
     * This dataset can contain graphs from any source when added via {@link Dataset#addNamedModel}.
     * Any graphs needed are in-memory unless explicitly added with {@link DatasetGraph#addGraph}.
     * </p>
     * These are held as links to the supplied graph and not copied.
     * <p> 
     * This dataset does not support transactions. 
     * <p>
     * 
     * @return a general-purpose DatasetGraph
     */
    public static DatasetGraph createGeneral() { return new DatasetGraphMaker(memGraphMaker) ; }

    
    //@Deprecated
    public static DatasetGraph createMemFixed() { return new DatasetGraphMap(GraphFactory.createDefaultGraph()) ; }
}
