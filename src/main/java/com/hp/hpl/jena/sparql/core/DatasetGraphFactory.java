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

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;

public class DatasetGraphFactory
{
    /** Create a DatasetGraph based on an existing one;
     *  this is a structure copy of the dataset struture 
     *  but graphs are shared 
     */ 
    public static DatasetGraph create(DatasetGraph dsg)
    { 
        // Fixed - requires explicit "add graph"
        return new DatasetGraphMap(dsg) ;
//        DatasetGraph dsg2 = createMem() ;
//        copyOver(dsg2, dsg2) ;
//        return dsg2 ;
    }
    
    private static void copyOver(DatasetGraph dsgDest, DatasetGraph dsgSrc)
    {
        dsgDest.setDefaultGraph(dsgSrc.getDefaultGraph()) ;
        for ( Iterator<Node> names = dsgSrc.listGraphNodes() ; names.hasNext() ; )
        {
            Node gn = names.next() ;
            dsgDest.addGraph(gn, dsgSrc.getGraph(gn)) ;
        }
    }

    /**
     * Create a DatasetGraph starting with a single graph.
     * New graphs must be explicitly added.
     */
    public static DatasetGraph create(Graph graph)
    {
        DatasetGraph dsg2 = createMemFixed() ;
        dsg2.setDefaultGraph(graph) ;
        return dsg2 ;
    }
    
    /**
     * Create a DatasetGraph which only ever has a single default graph.
     */
    public static DatasetGraph createOneGraph(Graph graph) { return new DatasetGraphOne(graph) ; }

    /** Interface for makign graphs when a dataset needs to add a new graph.
     *  Return null for no graph created.
     */ 
    public interface GraphMaker { public Graph create() ; }

    /** A graph maker that doesn't make graphs */
    public static GraphMaker graphMakerNull = new GraphMaker() {
        @Override
        public Graph create()
        {
            return null ;
        } } ;
    
    private static GraphMaker memGraphMaker = new GraphMaker()
    {
        @Override
        public Graph create()
        {
            return GraphFactory.createDefaultGraph() ;
        }
    } ;
    
    /**
     * Create a DatasetGraph which has all graphs in memory.
     */

    public static DatasetGraph createMem() { return new DatasetGraphMaker(memGraphMaker) ; }
    
    public static DatasetGraph createMemFixed() { return new DatasetGraphMap(GraphFactory.createDefaultGraph()) ; }
}
