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

package org.apache.jena.fuseki;

import static org.apache.jena.fuseki.ServerTest.datasetPath ;
import static org.apache.jena.fuseki.ServerTest.urlRoot ;
import static org.apache.jena.fuseki.mgt.MgtConst.opDatasets ;
import static org.apache.jena.fuseki.mgt.MgtConst.opPing ;
import static org.apache.jena.riot.web.HttpOp.execHttpDelete ;
import static org.apache.jena.riot.web.HttpOp.execHttpGet ;
import static org.apache.jena.riot.web.HttpOp.execHttpPost ;

import java.io.File ;
import java.io.IOException ;

import org.apache.http.HttpEntity ;
import org.apache.http.entity.FileEntity ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonArray ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.web.HttpSC ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/** Tests of the admin functionality */
public class TestAdmin extends BaseTest {
    @BeforeClass
    public static void beforeClass() {
        ServerTest.allocServer() ;
        //ServerTest.resetServer() ;
    }

    @AfterClass
    public static void afterClass() {
        ServerTest.freeServer() ;
    }
    
    @Before public void beforeTest() {
        ServerTest.resetServer() ;
    }
    
    // --- Ping 
    
    @Test public void ping_1() {
        execHttpGet(ServerTest.urlRoot+"$/"+opPing) ;
    }
    
    @Test public void ping_2() {
        execHttpPost(ServerTest.urlRoot+"$/"+opPing, null) ;
    }
    
    // --- List all datasets
    
    @Test public void list_datasets_1() {
        TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets) ;
        try { in.close() ; }
        catch (IOException e) { IO.exception(e); }
    }
    
    @Test public void list_datasets_2() {
        TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets) ;
        assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType()) ;
        JsonValue v = JSON.parse(in) ;
        assertNotNull(v.getAsObject().get("datasets")) ; 
        JsonArray a = v.getAsObject().get("datasets").getAsArray() ;
        
        JsonObject obj = a.get(0).getAsObject() ;
        checkOne(obj) ;
        try { in.close() ; }
        catch (IOException e) { IO.exception(e); }
    }

    private void checkOne(JsonObject obj) {
        assertNotNull(obj.get("ds.name")) ;
        assertNotNull(obj.get("ds.services")) ;
        assertTrue(obj.get("ds.services").isArray()) ;
    }
    
    // Specific dataset
    @Test public void list_datasets_5() {
        TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets+datasetPath) ;
        try { in.close() ; }
        catch (IOException e) { IO.exception(e); }
    }
    
    // Specific dataset
    @Test public void list_datasets_6() {
        checkNotThere("does-not-exist") ;

        try {
            TypedInputStream in = execHttpGet(ServerTest.urlRoot+"$/"+opDatasets+"/does-not-exist") ;
        } catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
    }
    
    // Specific dataset
    @Test public void list_datasets_7() {
        TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets+datasetPath) ;
        JsonValue v = JSON.parse(in) ;
        checkOne(v.getAsObject()) ;
        try { in.close() ; }
        catch (IOException e) { IO.exception(e); }
    }

    // -- Add
    
    // Specific dataset
    @Test public void add_dataset_1() {
        File f = new File("testing/config-ds-1.ttl") ;
        org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8) ;
        HttpEntity e = new FileEntity(f, ct) ;
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets, e) ;
        
        // Check exists.
        execHttpGet(urlRoot+"$/"+opDatasets+"/test-ds2") ;
        
        // Remove it.
        execHttpDelete(ServerTest.urlRoot+"$/"+opDatasets+"/test-ds2") ;
        checkNotThere("test-ds") ;
    }

    private static void checkNotThere(String name) {
        if ( name.startsWith("/") )
            name = name.substring(1) ;
        // Check gone exists.
        try { 
            TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets+"/"+name) ; 
            IO.close(in) ;
        }
        catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
        
        try { 
            TypedInputStream in = execHttpGet(urlRoot+name+"/sparql?query=ASK%7B%7D") ;
            IO.close(in) ;
        }
        catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
        
    }
    
    /*
        GET     /$/ping 
        POST    /$/ping 
        POST    /$/datasets/    
        GET     /$/datasets/
        DELETE  /$/datasets/*{name}*    
        GET     /$/datasets/*{name}*    
        POST    /$/datasets/*{name}*?state=dormant  
        POST    /$/datasets/*{name}*?state=active   
        POST    /$/backup/*{name}*  
        GET     /$/server   
        POST    /$/server/shutdown  
        GET     /$/stats/   
        GET     /$/stats/*{name}*
     */
}

