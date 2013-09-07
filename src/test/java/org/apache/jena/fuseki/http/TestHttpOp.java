/**
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

import java.io.IOException ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.fuseki.ServerTest ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.system.IRILib ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.web.HttpSC ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.engine.http.Params ;
import com.hp.hpl.jena.util.FileUtils ;

// This a mixture of testing HttpOp and testing basic operation of the SPARQL server
// especially error cases abnd unusual usage that the higher level APIs don't use.
public class TestHttpOp extends BaseTest {
    
    static String pingURL = ServerTest.urlRoot+"ping.txt" ;
    @BeforeClass public static void beforeClass() { ServerTest.allocServer() ; }
    @AfterClass  public static void afterClass()  { ServerTest.freeServer() ; }
    
    static String graphURL = ServerTest.serviceREST+"?default" ;
    static String queryURL = ServerTest.serviceQuery ;
    static String updateURL = ServerTest.serviceUpdate ;
    
    static String simpleQuery = queryURL+"?query="+IRILib.encodeUriComponent("ASK{}") ;
    
    // Basic operations
    
    @Test public void httpGet_01() {
        TypedInputStream in = HttpOp.execHttpGet(pingURL) ;
        IO.close(in) ;
    }
    
    @Test(expected=HttpException.class) 
    public void httpGet_02() {
        try {
            TypedInputStream in = HttpOp.execHttpGet(ServerTest.urlRoot+"does-not-exist") ;
            IO.close(in) ;
        } catch(HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
            throw ex ;
        }
    }

    @Test public void httpGet_03() throws IOException {
        String x = HttpOp.execHttpGetString(pingURL) ;
        String y = FileUtils.readWholeFileAsUTF8("pages/ping.txt") ;
        assertEquals(y,x) ;
    }   
    
    @Test public void httpGet_04() {
        String x = HttpOp.execHttpGetString(ServerTest.urlRoot+"does-not-exist") ;
        assertNull(x) ;
    }
    
    @Test public void httpGet_05() {
        TypedInputStream in = HttpOp.execHttpGet(simpleQuery) ;
        IO.close(in) ;
    }
    
    // SPARQL Query
    
    @Test public void queryGet_01() {
        TypedInputStream in = HttpOp.execHttpGet(simpleQuery) ;
        IO.close(in) ;
    }

    @Test(expected=HttpException.class)
    public void queryGet_02() {
        try {
            // No query.
            TypedInputStream in = HttpOp.execHttpGet(queryURL+"?query=") ;
            IO.close(in) ;
        } catch (HttpException ex) {
            assertEquals(ex.getResponseCode(), HttpSC.BAD_REQUEST_400) ;
            throw ex ; 
        }
    }

//    @Test(expected=HttpException.class)
//    public void queryGet_03() {
//        try {
//            // Service description.
//            TypedInputStream in = HttpOp.execHttpGet(queryURL) ;
//            IO.close(in) ;
//        } catch (HttpException ex) {
//            assertEquals(ex.getResponseCode(), HttpSC.NOT_FOUND_404) ;
//            throw ex ; 
//        }
//    }

    @Test(expected=HttpException.class)
    public void httpPost_01() {
        try {
            HttpOp.execHttpPost(queryURL, "ASK{}", "text/plain") ;
        } catch (HttpException ex) {
            assertEquals(ex.getResponseCode(), HttpSC.UNSUPPORTED_MEDIA_TYPE_415) ;
            throw ex ;
        }
    }
    
    @Test(expected=HttpException.class)
    public void httpPost_02() {
        try {
            HttpOp.execHttpPost(queryURL, "ASK{}", WebContent.contentTypeSPARQLQuery) ;
        } catch (HttpException ex) {
            assertEquals(ex.getResponseCode(), HttpSC.UNSUPPORTED_MEDIA_TYPE_415) ;
            throw ex ;
        }
    }
    
    @Test(expected=HttpException.class)
    public void httpPost_03() {
        try {
            HttpOp.execHttpPost(queryURL, "ASK{}", WebContent.contentTypeOctets) ;
        } catch (HttpException ex) {
            assertEquals(ex.getResponseCode(), HttpSC.UNSUPPORTED_MEDIA_TYPE_415) ;
            throw ex ;
        }
    }
        
    @Test public void httpPost_04() {
        Params params = new Params() ;
        params.addParam("query", "ASK{}") ;
        TypedInputStream in = HttpOp.execHttpPostFormStream(queryURL, params, WebContent.contentTypeResultsJSON) ;
        IO.close(in) ;
    }
    
    @Test(expected=HttpException.class)
    public void httpPost_05() {
        Params params = new Params() ;
        params.addParam("query", "ASK{}") ;
        TypedInputStream in = null ;
        try {
            // Query to Update 
            in = HttpOp.execHttpPostFormStream(updateURL, params, WebContent.contentTypeResultsJSON) ;
        } catch (HttpException ex) {
            assertEquals(ex.getResponseCode(), HttpSC.BAD_REQUEST_400) ;
            throw ex ;
        }
        finally { IO.close(in) ; }
    }
    
    @Test public void httpPost_06() {
        Params params = new Params() ;
        params.addParam("request", "CLEAR ALL") ;
        HttpOp.execHttpPostForm(updateURL, params) ;
    }
    
    // GSP
    @Test public void gsp_01() {
        String x = HttpOp.execHttpGetString(graphURL, "application/rdf+xml") ;
        assertTrue(x.contains("</")) ;
        assertTrue(x.contains(":RDF")) ;
    }

    @Test public void gsp_02() {
        String x = HttpOp.execHttpGetString(graphURL, "application/n-triples") ;
        assertTrue(x.isEmpty()) ;
    }
    
    static String graphString = "@prefix : <http://example/> . :s :p :o ." ;
    
    @Test public void gsp_03() {
        HttpOp.execHttpPut(graphURL, WebContent.contentTypeTurtle, graphString) ;
    }
    
    @Test public void gsp_04() {
        HttpOp.execHttpPut(graphURL, WebContent.contentTypeTurtle, graphString) ;
        String s1 = HttpOp.execHttpGetString(graphURL, WebContent.contentTypeNTriples) ;
        assertFalse(s1.isEmpty()) ;
        HttpOp.execHttpDelete(graphURL) ;
        String s2 = HttpOp.execHttpGetString(graphURL, WebContent.contentTypeNTriples) ;
        assertTrue(s2.isEmpty()) ;
    }
    
    @Test public void gsp_05() {
        HttpOp.execHttpDelete(graphURL) ;
        
        HttpOp.execHttpPost(graphURL, WebContent.contentTypeTurtle, graphString) ;
        String s1 = HttpOp.execHttpGetString(graphURL, WebContent.contentTypeNTriples) ;
        assertFalse(s1.isEmpty()) ;
        HttpOp.execHttpDelete(graphURL) ;
        String s2 = HttpOp.execHttpGetString(graphURL, WebContent.contentTypeNTriples) ;
        assertTrue(s2.isEmpty()) ;
    }
}

