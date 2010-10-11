/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class TestDatasetHTTP extends BaseServerTest 
{
    //Model level testing.
    
    static final String datasetURI_not_1    = "http://localhost:"+port+"/junk" ;
    static final String datasetURI_not_2    = serviceREST+"/not" ;
    static final String datasetURI_not_3    = "http://localhost:"+port+datasetPath+"/not/data" ;
    
    @BeforeClass public static void beforeClass()
    { }
    
    @AfterClass public static void afterClass()
    { }

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

    @Test(expected=FusekiNotFoundException.class)
    public void test_404_1()
    {
        // Not the right service.
        DS_Updater du = DatasetUpdaterFactory.createHTTP(datasetURI_not_1) ;
        Model graph = du.getModel(gn99) ;
    }

    @Test(expected=FusekiNotFoundException.class)
    public void test_404_2()
    {
        DS_Updater du = DatasetUpdaterFactory.createHTTP(datasetURI_not_2) ;
        Model graph = du.getModel(gn99) ;
    }

    @Test
    public void test_404_3()
    {
        // All graphs "exist"
        DS_Updater du = DatasetUpdaterFactory.createHTTP(serviceREST) ;
        Model graph = du.getModel(gn99) ;
        assertNull(graph) ;
    }

    @Test public void head_01()
    {
        DS_Updater du = create() ;
        boolean b = du.containsModel(gn1) ;
        assertFalse("Blank remote dataset as a named graph", b) ;
    }

    @Test public void head_02()
    {
        DS_Updater du = create() ;
        du.putModel(gn1, graph1) ;
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
        DS_Updater du = create() ;
        Model graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }
    
    @Test public void get_02()
    {
        DS_Updater du = create() ;
        Model graph = du.getModel(gn1) ;
        assertNull(graph) ;
    }

    @Test public void delete_01()
    {
        DS_Updater du = create() ;
        du.deleteDefault() ;
    }

    @Test public void delete_02()
    {
        DS_Updater du = create() ;
        du.deleteModel(gn1) ;
        boolean exists = du.containsModel(gn1) ;
        assertFalse("Expected gn1 not to exist", exists) ;
    }

    @Test public void put_01()
    {
        DS_Updater du = create() ;
        du.putModel(graph1) ;
        Model graph = du.getModel() ;
        assertTrue(graph.isIsomorphicWith(graph1)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }
    
    @Test public void put_02()
    {
        DS_Updater du = create() ;
        du.putModel(gn1, graph1) ;
        boolean exists = du.containsModel(gn1) ;
        assertTrue(exists) ;
        exists = du.containsModel(gn2) ;
        assertFalse("Expected gn2 not to exist", exists) ;
        
        Model graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
        graph = du.getModel(gn1) ;
        assertTrue(graph.isIsomorphicWith(graph1)) ;
        
        du.deleteModel(gn1) ;
        exists = du.containsModel(gn1) ;
        assertFalse("Expected gn1 not to exist", exists) ;
        
        graph = du.getModel(gn1) ;
        assertNull(graph) ;
    }

    @Test public void put_03()
    {
        DS_Updater du = create() ;
        du.putModel(graph1) ;
        du.putModel(graph2) ;  // PUT overwrites
        Model graph = du.getModel() ;
        assertFalse(graph.isIsomorphicWith(graph1)) ;
        assertTrue(graph.isIsomorphicWith(graph2)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }

    @Test public void post_01()
    {
        DS_Updater du = create() ;
        du.putModel(graph1) ;
        du.add(graph2) ;  // POST appends
        Model graph = du.getModel() ;
        
        Model graph3 = ModelFactory.createDefaultModel() ;
        graph3.add(graph1) ;
        graph3.add(graph2) ;
        
        assertFalse(graph.isIsomorphicWith(graph1)) ;
        assertFalse(graph.isIsomorphicWith(graph2)) ;
        assertTrue(graph.isIsomorphicWith(graph3)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }

    @Test public void post_02()
    {
        DS_Updater du = create() ;
        du.add(graph1) ;
        du.add(graph2) ;
        Model graph = du.getModel() ;
        
        Model graph3 = ModelFactory.createDefaultModel() ;
        graph3.add(graph1) ;
        graph3.add(graph2) ;
        
        assertFalse(graph.isIsomorphicWith(graph1)) ;
        assertFalse(graph.isIsomorphicWith(graph2)) ;
        assertTrue(graph.isIsomorphicWith(graph3)) ;
        // Empty it.
        du.deleteDefault() ;
        graph = du.getModel() ;
        assertTrue(graph.isEmpty()) ;
    }
    
    @Test public void clearup_1()
    {
        DS_Updater du = create() ;
        du.deleteDefault() ;
        du.deleteModel(gn1) ;
        du.deleteModel(gn2) ;
        du.deleteModel(gn99) ;
    }

    static DS_Updater create()
    {
        return DatasetUpdaterFactory.createHTTP(serviceREST) ;
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