/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.http;

import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.fuseki.BaseServerTest ;
import org.openjena.fuseki.DatasetAccessor ;
import org.openjena.fuseki.DatasetAccessorFactory ;
import org.openjena.fuseki.ServerTest ;
import org.openjena.fuseki.WebTest ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class TestDatasetAccessorHTTP extends BaseServerTest 
{
    //Model level testing.
    
    static final String datasetURI_not_1    = "http://localhost:"+ServerTest.port+"/junk" ;
    static final String datasetURI_not_2    = serviceREST+"/not" ;
    static final String datasetURI_not_3    = "http://localhost:"+ServerTest.port+datasetPath+"/not/data" ;
    
    @BeforeClass public static void beforeClass()   { ServerTest.allocServer() ; }
    @AfterClass public static void afterClass()     { ServerTest.freeServer() ; }
    @Before public void before()                    { ServerTest.resetServer() ; }
    
    @Test public void test_ds_1()
    {
        // Can't GET the dataset
        WebTest.exec_get(serviceREST, 400) ;
    }
    
    @Test public void test_ds_2()
    {
        // Random other URI
        WebTest.exec_get(datasetURI_not_1, 404) ;
    }

    @Test public void test_ds_3()
    {
        // Longer path URI.
        WebTest.exec_get(datasetURI_not_2, 404) ;
    }

    @Test public void test_ds_4()
    {
        // Longer path URI.
        WebTest.exec_get(datasetURI_not_2, 404) ;
    }

    @Test //(expected=FusekiNotFoundException.class)
    public void test_404_1()
    {
        // Not the right service.
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(datasetURI_not_1) ;
        Model graph = du.getModel(gn99) ;
        assertNull(graph) ; 
    }

    @Test //(expected=FusekiNotFoundException.class)
    public void test_404_2()
    {
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(datasetURI_not_2) ;
        Model graph = du.getModel(gn99) ;
        assertNull(graph) ;
    }

    @Test
    public void test_404_3()
    {
        // Right service, wrong graph
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        Model graph = du.getModel(gn99) ;
        assertNull(graph) ;
    }

    @Test public void head_01()
    {
        DatasetAccessor du = create() ;
        boolean b = du.containsModel(gn1) ;
        assertFalse("Blank remote dataset as a named graph", b) ;
    }

    @Test public void head_02()
    {
        DatasetAccessor du = create() ;
        du.putModel(gn1, model1) ;
        boolean exists = du.containsModel(gn1) ;
        assertTrue(exists) ;
        exists = du.containsModel(gn2) ;
        assertFalse("Expected gn2 not to exist (1)", exists) ;

        exists = du.containsModel(gn2) ;
        assertFalse("Expected gn2 not to exist (2)", exists) ;
        // Clearup
        du.deleteModel(gn1) ;
    }

    @Test public void get_01()
    {
        DatasetAccessor du = create() ;
        Model graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }
    
    @Test public void get_02()
    {
        DatasetAccessor du = create() ;
        Model graph = du.getModel(gn1) ;
        assertNull(graph) ;
    }

    @Test public void delete_01()
    {
        DatasetAccessor du = create() ;
        du.deleteDefault() ;
    }

    @Test public void delete_02()
    {
        DatasetAccessor du = create() ;
        du.deleteModel(gn1) ;
        boolean exists = du.containsModel(gn1) ;
        assertFalse("Expected gn1 not to exist", exists) ;
    }

    @Test public void put_01()
    {
        DatasetAccessor du = create() ;
        du.putModel(model1) ;
        Model graph = du.getModel() ;
        assertTrue(graph.isIsomorphicWith(model1)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }
    
    @Test public void put_02()
    {
        DatasetAccessor du = create() ;
        du.putModel(gn1, model1) ;
        boolean exists = du.containsModel(gn1) ;
        assertTrue(exists) ;
        exists = du.containsModel(gn2) ;
        assertFalse("Expected gn2 not to exist", exists) ;
        
        Model graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
        graph = du.getModel(gn1) ;
        assertTrue(graph.isIsomorphicWith(model1)) ;
        
        du.deleteModel(gn1) ;
        exists = du.containsModel(gn1) ;
        assertFalse("Expected gn1 not to exist", exists) ;
        
        graph = du.getModel(gn1) ;
        assertNull(graph) ;
    }

    @Test public void put_03()
    {
        DatasetAccessor du = create() ;
        du.putModel(model1) ;
        du.putModel(model2) ;  // PUT overwrites
        Model graph = du.getModel() ;
        assertFalse(graph.isIsomorphicWith(model1)) ;
        assertTrue(graph.isIsomorphicWith(model2)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }

    @Test public void post_01()
    {
        DatasetAccessor du = create() ;
        du.putModel(model1) ;
        du.add(model2) ;  // POST appends
        Model graph = du.getModel() ;
        
        Model graph3 = ModelFactory.createDefaultModel() ;
        graph3.add(model1) ;
        graph3.add(model2) ;
        
        assertFalse(graph.isIsomorphicWith(model1)) ;
        assertFalse(graph.isIsomorphicWith(model2)) ;
        assertTrue(graph.isIsomorphicWith(graph3)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }

    @Test public void post_02()
    {
        DatasetAccessor du = create() ;
        du.add(model1) ;
        du.add(model2) ;
        Model graph = du.getModel() ;
        
        Model graph3 = ModelFactory.createDefaultModel() ;
        graph3.add(model1) ;
        graph3.add(model2) ;
        
        assertFalse(graph.isIsomorphicWith(model1)) ;
        assertFalse(graph.isIsomorphicWith(model2)) ;
        assertTrue(graph.isIsomorphicWith(graph3)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }
    
    @Test public void clearup_1()
    {
        DatasetAccessor du = create() ;
        du.deleteDefault() ;
        du.deleteModel(gn1) ;
        du.deleteModel(gn2) ;
        du.deleteModel(gn99) ;
    }

    static DatasetAccessor create()
    {
        return DatasetAccessorFactory.createHTTP(ServerTest.serviceREST) ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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