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

package org.apache.jena.web;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public abstract class AbstractTestDatasetGraphAccessor extends BaseTest
{
    protected static final String gn1       = "http://graph/1" ;
    protected static final String gn2       = "http://graph/2" ;
    protected static final String gn99      = "http://graph/99" ;
    
    protected static final Node n1          = NodeFactory.createURI("http://graph/1") ;
    protected static final Node n2          = NodeFactory.createURI("http://graph/2") ;
    protected static final Node n99         = NodeFactory.createURI("http://graph/99") ;
    
    protected static final Graph graph1     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))") ;
    protected static final Graph graph2     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))") ;
    
    protected static final Model model1     = ModelFactory.createModelForGraph(graph1) ;
    protected static final Model model2     = ModelFactory.createModelForGraph(graph2) ;
    
    // return a DatasetGraphAccessor backed by an empty dataset
    protected abstract DatasetGraphAccessor getDatasetUpdater() ;
    
    private static void assertNullOrEmpty(Graph graph)
    {
        if ( graph == null ) return ; 
        if ( ! graph.isEmpty() ) {
            System.out.println("----") ;
            RDFDataMgr.write(System.out, graph, Lang.TTL) ;
        }
        
        assertTrue(graph.isEmpty()) ;
    }
    
    @Test public void get_01()
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        Graph graph = updater.httpGet() ;
        assertNullOrEmpty(graph) ;
        Graph graph2 = updater.httpGet(n1) ;
    }
    
    @Test public void get_02()
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        Graph graph = updater.httpGet(n1) ;
        assertNullOrEmpty(graph) ;
    }
    
    @Test public void put_01()
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        updater.httpPut(graph1) ;
        
        Graph graph = updater.httpGet() ;
        assertNotNull("Graph is null", graph) ;
        assertTrue(graph.isIsomorphicWith(graph1)) ;
    }

    
    @Test public void put_02()
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        updater.httpPut(n1, graph1) ;
        
        Graph graph = updater.httpGet() ;
        assertNullOrEmpty(graph) ;
        
        graph = updater.httpGet(n1) ;
        assertNotNull("Graph is null", graph) ;
        assertTrue(graph.isIsomorphicWith(graph1)) ;
    }

    @Test public void post_01()
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        updater.httpPost(graph1) ;
        updater.httpPost(graph2) ;
        Graph graph = updater.httpGet() ;
        
        Graph graph3 = GraphFactory.createDefaultGraph() ;
        GraphUtil.addInto(graph3, graph1) ;
        GraphUtil.addInto(graph3, graph2) ;
        assertTrue(graph.isIsomorphicWith(graph3)) ;
        assertFalse(graph.isIsomorphicWith(graph1)) ;
        assertFalse(graph.isIsomorphicWith(graph2)) ;
    }
    
    @Test public void post_02()
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        updater.httpPost(n1, graph1) ;
        updater.httpPost(n1, graph2) ;
        Graph graph = updater.httpGet(n1) ;
        Graph graph3 = GraphFactory.createDefaultGraph() ;
        GraphUtil.addInto(graph3, graph1) ;
        GraphUtil.addInto(graph3, graph2) ;
        assertTrue(graph.isIsomorphicWith(graph3)) ;
        assertFalse(graph.isIsomorphicWith(graph1)) ;
        assertFalse(graph.isIsomorphicWith(graph2)) ;
        
        graph = updater.httpGet() ;
        assertFalse(graph.isIsomorphicWith(graph3)) ;
    }

    // Default graph
    @Test public void delete_01()
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        updater.httpDelete() ;
        Graph graph = updater.httpGet() ;
        assertTrue(graph.isEmpty()) ;
        
        updater.httpPut(graph1) ;
        graph = updater.httpGet() ;
        assertFalse(graph.isEmpty()) ;
        
        updater.httpDelete() ;
        graph = updater.httpGet() ;
        assertTrue(graph.isEmpty()) ;
    }
    
    // Named graph, no side effects.
    @Test public void delete_02() 
    {
        DatasetGraphAccessor updater = getDatasetUpdater() ;
        //updater.httpDelete(n1) ;
        Graph graph = updater.httpGet(n1) ;
        assertNullOrEmpty(graph) ;

        updater.httpPut(graph2) ;
        updater.httpPut(n1, graph1) ;
        
        updater.httpDelete() ;
        graph = updater.httpGet() ;
        assertTrue(graph.isEmpty()) ;
        updater.httpPut(graph2) ;

        graph = updater.httpGet(n1) ;
        assertFalse(graph.isEmpty()) ;
        
        updater.httpDelete(n1) ;
        graph = updater.httpGet(n1) ;
        assertNullOrEmpty(graph) ;
        graph = updater.httpGet() ;
        assertFalse(graph.isEmpty()) ;
    }

//    @Test public void compound_01() {}
//    @Test public void compound_02() {}
}
