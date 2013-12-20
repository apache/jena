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
import static org.apache.jena.fuseki.mgt.MgtConst.opStats ;
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
    
    // Name of the dataset in the assembler file.
    static String dsTest = "test-ds2" ;

    
    
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
        try {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType()) ;
            JsonValue v = JSON.parse(in) ;
            assertNotNull(v.getAsObject().get("datasets")) ; 
            checkJsonDatasetsAll(v);
        } finally { IO.close(in) ; }
    }

    // Specific dataset
    @Test public void list_datasets_5() {
        checkExists(datasetPath) ;
    }
    
    // Specific dataset
    @Test public void list_datasets_6() {
        try {
            TypedInputStream in = execHttpGet(ServerTest.urlRoot+"$/"+opDatasets+"/does-not-exist") ;
        } catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
    }
    
    // Specific dataset
    @Test public void list_datasets_7() {
        JsonValue v = execGetJSON(urlRoot+"$/"+opDatasets+datasetPath) ;
        checkJsonDatasetsOne(v.getAsObject()) ;
    }

    // -- Add
    
    private static void addTestDataset() {
        File f = new File("testing/config-ds-1.ttl") ;
        org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8) ;
        HttpEntity e = new FileEntity(f, ct) ;
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets, e) ;
    }
    
    private static void deleteTestDataset() {
        execHttpDelete(ServerTest.urlRoot+"$/"+opDatasets+"/"+dsTest) ;
    }

    // Specific dataset
    @Test public void add_dataset_1() {
        checkNotThere(dsTest) ;

        addTestDataset() ;
        
        // Check exists.
        checkExists(dsTest) ;
        
        // Remove it.
        deleteTestDataset() ;
        checkNotThere(dsTest) ;
    }

    // Try to add twice
    @Test public void add_dataset_2() {
        checkNotThere(dsTest) ;

        File f = new File("testing/config-ds-1.ttl") ;
        { 
            org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8) ;
            HttpEntity e = new FileEntity(f, ct) ;
            execHttpPost(ServerTest.urlRoot+"$/"+opDatasets, e) ;
        }
        // Check exists.
        checkExists(dsTest) ;
        try {
            org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8) ;
            HttpEntity e = new FileEntity(f, ct) ;
            execHttpPost(ServerTest.urlRoot+"$/"+opDatasets, e) ;
        } catch (HttpException ex) {
            assertEquals(HttpSC.CONFLICT_409, ex.getResponseCode()) ;
        }
        // Check exists.
        checkExists(dsTest) ;
    }
    
    // ---- Active/dormant.

    @Test public void state_1() {
        // Add one
        addTestDataset() ;
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/"+dsTest+"?state=dormant", null) ;

        checkExistsNotActive(dsTest); 
        
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/"+dsTest+"?state=active", null) ;
        
        checkExists(dsTest) ;
        deleteTestDataset() ;
    }
    
    @Test public void state_2() {
        addTestDataset() ;
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/"+dsTest+"?state=dormant", null) ;
        deleteTestDataset() ;
        checkNotThere(dsTest) ;
    }

    @Test public void state_3() {
        addTestDataset() ;
        try {
            execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/DoesNotExist?state=dormant", null) ;
        } catch (HttpException ex) { assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ; }
    }
    
    // ---- Backup

    // ---- Server
    
    // ---- Stats
    
    @Test public void stats_1() {
        JsonValue v = execGetJSON(urlRoot+"$/"+opStats) ;
        checkJsonStatsAll(v); 
    }
    
    @Test public void stats_2() {
        JsonValue v = execGetJSON(urlRoot+"$/"+opStats+datasetPath) ;
        checkJsonStatsAll(v); 
    }

    @Test public void stats_3() {
        try {
            JsonValue v = execGetJSON(urlRoot+"$/"+opStats+"/DoesNotExist") ;
            checkJsonStatsAll(v);
        } catch (HttpException ex) { assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()); }
    }

    // Auxilary
    
    private static void askPing(String name) {
        if ( name.startsWith("/") )
            name = name.substring(1) ;
        TypedInputStream in = execHttpGet(urlRoot+name+"/sparql?query=ASK%7B%7D") ; 
        IO.close(in) ;
    }
    
    private static void adminPing(String name) {
        TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets+"/"+name) ; 
        IO.close(in) ;
    }

    private static void checkExists(String name)  {
        adminPing(name) ;
        askPing(name) ;
    }
    
    private static void checkExistsNotActive(String name)  {
        adminPing(name) ;
        try { askPing(name) ; 
            fail("askPing did not cause an Http Exception") ;
        } catch ( HttpException ex ) {}
    }

    private static void checkNotThere(String name) {
        if ( name.startsWith("/") )
            name = name.substring(1) ;
        // Check gone exists.
        try { adminPing(name) ; }
        catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
        
        try { askPing(name) ; }
        catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
    }

    private static void checkJsonDatasetsAll(JsonValue v) {
        assertNotNull(v.getAsObject().get("datasets")) ; 
        JsonArray a = v.getAsObject().get("datasets").getAsArray() ;
        for ( JsonValue v2 : a )
            checkJsonDatasetsOne(v2) ;
    }
    
    private static void checkJsonDatasetsOne(JsonValue v) {
        assertTrue(v.isObject()) ;
        JsonObject obj = v.getAsObject() ;
        assertNotNull(obj.get("ds.name")) ;
        assertNotNull(obj.get("ds.services")) ;
        assertTrue(obj.get("ds.services").isArray()) ;
    }
    
    private static void checkJsonStatsAll(JsonValue v) {
        assertNotNull(v.getAsObject().get("datasets")) ; 
        JsonObject a = v.getAsObject().get("datasets").getAsObject() ;
        for ( String dsname : a.keys() ) {
            JsonValue obj = a.get(dsname).getAsObject() ;
            checkJsonStatsOne(obj);
        }
    }
    
    private static void checkJsonStatsOne(JsonValue v) {
        JsonObject obj = v.getAsObject() ;
        assertTrue(obj.hasKey("Requests")) ;
        assertTrue(obj.hasKey("RequestsGood")) ;
        assertTrue(obj.hasKey("RequestsBad")) ;
        assertTrue(obj.hasKey("services")) ;
        JsonObject obj2 = obj.get("services").getAsObject() ;
        // More
    }

    private static JsonValue execGetJSON(String url) {
        TypedInputStream in = execHttpGet(url) ;
        try { 
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType()) ;
            return JSON.parse(in) ; 
        } finally { IO.close(in) ; }
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

