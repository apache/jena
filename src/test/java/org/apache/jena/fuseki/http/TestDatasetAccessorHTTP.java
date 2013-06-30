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

package org.apache.jena.fuseki.http;

import static org.apache.jena.fuseki.ServerTest.* ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.fuseki.ServerTest ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.web.HttpSC ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.query.DatasetAccessor ;
import com.hp.hpl.jena.query.DatasetAccessorFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;


public class TestDatasetAccessorHTTP extends BaseTest 
{
    //Model level testing.
    
    static final String datasetURI_not_1    = "http://localhost:"+port+"/junk" ;
    static final String datasetURI_not_2    = serviceREST+"/not" ;
    static final String datasetURI_not_3    = "http://localhost:"+port+datasetPath+"/not/data" ;
    
    @BeforeClass public static void beforeClass()   { ServerTest.allocServer() ; }
    @AfterClass public static void afterClass()     { ServerTest.freeServer() ; }
    @Before public void before()                    { ServerTest.resetServer() ; }
    
    @Test(expected=HttpException.class)
    public void test_ds_1()
    {
        // Can't GET the dataset service.
        try {
            HttpOp.execHttpGet(serviceREST) ;
        } catch (HttpException ex) {
            assertTrue(HttpSC.isClientError(ex.getResponseCode())) ;
            throw ex ;
        }
    }
    
    @Test(expected=HttpException.class)
    public void test_ds_2()
    {
        try {
            HttpOp.execHttpGet(datasetURI_not_1) ;
        } catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
            throw ex ;
        }
    }

    @Test(expected=HttpException.class)
    public void test_ds_3()
    {
        try {
            HttpOp.execHttpGet(datasetURI_not_2) ;
        } catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
            throw ex ;
        }
    }

    @Test
    public void test_404_1()
    {
        // Not the right service.
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(datasetURI_not_1) ;
        Model graph = du.getModel(gn99) ;
        assertNull(graph) ; 
    }

    @Test
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
