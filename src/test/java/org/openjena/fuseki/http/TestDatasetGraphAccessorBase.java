/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file] 
 */

package org.openjena.fuseki.http;

import org.junit.Test ;
import org.openjena.fuseki.BaseServerTest ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public abstract class TestDatasetGraphAccessorBase extends BaseServerTest
{
    // return a DatasetGraphAccessor backed by an empty dataset
    protected abstract DatasetGraphAccessor getDatasetUpdater() ;
    
    private static void assertNullOrEmpty(Graph graph)
    {
        if ( graph == null ) return ; 
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
        graph3.getBulkUpdateHandler().add(graph1) ;
        graph3.getBulkUpdateHandler().add(graph2) ;
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
        graph3.getBulkUpdateHandler().add(graph1) ;
        graph3.getBulkUpdateHandler().add(graph2) ;
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * (c) Copyright 2010 Talis Systems Ltd.
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */