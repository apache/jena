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
import static org.apache.jena.fuseki.mgt.MgtConst.opListBackups ;
import static org.apache.jena.fuseki.mgt.MgtConst.opPing ;
import static org.apache.jena.fuseki.mgt.MgtConst.opServer ;
import static org.apache.jena.fuseki.mgt.MgtConst.opStats ;
import static org.apache.jena.riot.web.HttpOp.execHttpDelete ;
import static org.apache.jena.riot.web.HttpOp.execHttpGet ;
import static org.apache.jena.riot.web.HttpOp.execHttpPost ;

import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.NameValuePair ;
import org.apache.http.client.entity.UrlEncodedFormEntity ;
import org.apache.http.entity.FileEntity ;
import org.apache.http.message.BasicNameValuePair ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonArray ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.fuseki.mgt.JsonConst ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.riot.web.HttpResponseHandler ;
import org.apache.jena.web.HttpSC ;
import org.junit.After ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.Test ;

/** Tests of the admin functionality */
public class TestAdmin extends BaseTest {
    
    // Name of the dataset in the assembler file.
    static String dsTest = "test-ds2" ;
    
    @Before public void beforeTest() {
        ServerTest.allocServer() ;
        ServerTest.resetServer() ;
    }
    
    @After public void afterTest() {
        ServerTest.freeServer() ;
    }

    @AfterClass public static void afterClass() {
        ServerTest.teardownServer() ;
    }
  
    // --- Ping 
    
    @Test public void ping_1() {
        execHttpGet(ServerTest.urlRoot+"$/"+opPing) ;
    }
    
    @Test public void ping_2() {
        execHttpPost(ServerTest.urlRoot+"$/"+opPing, null) ;
    }
    
    // --- Server status 
    
    @Test public void server_1() {
        JsonValue jv = httpGetJson(ServerTest.urlRoot+"$/"+opServer) ;
        JsonObject obj = jv.getAsObject() ;
        assertTrue(obj.hasKey(JsonConst.admin)) ;
        assertTrue(obj.hasKey(JsonConst.datasets)) ;
        assertTrue(obj.hasKey(JsonConst.uptime)) ;
        assertTrue(obj.hasKey(JsonConst.startDT)) ;
    }

    @Test public void server_2() {
        execHttpPost(ServerTest.urlRoot+"$/"+opServer, null) ;
    }
    
    // --- List all datasets
    
    @Test public void list_datasets_1() {
        try ( TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets) ; ) { }
    }
    
    @Test public void list_datasets_2() {
        try ( TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType()) ;
            JsonValue v = JSON.parseAny(in) ;
            assertNotNull(v.getAsObject().get("datasets")) ; 
            checkJsonDatasetsAll(v);
        }
    }
    
    // Specific dataset
    @Test public void list_datasets_3() {
        checkExists(datasetPath) ;
    }
    
    // Specific dataset
    @Test public void list_datasets_4() {
        try {
            getDatasetDescription("does-not-exist") ;
        } catch (HttpException ex) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
    }
    
    // Specific dataset
    @Test public void list_datasets_5() {
        JsonValue v = getDatasetDescription(datasetPath) ;
        checkJsonDatasetsOne(v.getAsObject()) ;
    }

    // Specific dataset
    @Test public void add_delete_dataset_1() {
        checkNotThere(dsTest) ;

        addTestDataset() ;
        
        // Check exists.
        checkExists(dsTest) ;
        
        // Remove it.
        deleteDataset(dsTest) ;
        checkNotThere(dsTest) ;
    }

    // Try to add twice
    @Test public void add_delete_dataset_2() {
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
        deleteDataset(dsTest) ;
    }
    
    @Test public void add_delete_dataset_3() throws Exception {
        String name = "MEMTEST" ;
        //String args = "dbType=mem&dbName="+name ;
        
        List<NameValuePair> args = new ArrayList<NameValuePair>() ;
        args.add(new BasicNameValuePair("dbType", "mem")) ;
        args.add(new BasicNameValuePair("dbName", name)) ;
        
        HttpEntity e = new UrlEncodedFormEntity(args) ;
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets, e) ;
        checkExists(name) ;
        deleteDataset(name) ;
    }
    
    @Test public void delete_dataset_1() {
        String name = "NoSuchDataset" ;
        try {
            execHttpDelete(ServerTest.urlRoot+"$/"+opDatasets+"/"+name) ;
            fail("delete did not cause an Http Exception") ;
        } catch ( HttpException ex ) {
            assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ;
        }
    }

    // ---- Active/Offline.

    @Test public void state_1() {
        // Add one
        addTestDataset() ;
        checkExists(dsTest) ;

        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/"+dsTest+"?state=offline", null) ;

        checkExistsNotActive(dsTest); 
        
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/"+dsTest+"?state=active", null) ;
        
        checkExists(dsTest) ;
        deleteDataset(dsTest) ;
    }
    
    @Test public void state_2() {
        addTestDataset() ;
        execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/"+dsTest+"?state=offline", null) ;
        deleteDataset(dsTest) ;
        checkNotThere(dsTest) ;
    }

    @Test public void state_3() {
        addTestDataset() ;
        try {
            execHttpPost(ServerTest.urlRoot+"$/"+opDatasets+"/DoesNotExist?state=offline", null) ;
        } catch (HttpException ex) { assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()) ; }
        deleteDataset(dsTest) ;
    }
    
    // ---- Backup

    // ---- Server
    
    // ---- Stats
    
    @Test public void stats_1() {
        JsonValue v = execGetJSON(urlRoot+"$/"+opStats) ;
        checkJsonStatsAll(v); 
    }
    
    @Test public void stats_2() {
        addTestDataset() ;
        JsonValue v = execGetJSON(urlRoot+"$/"+opStats+datasetPath) ;
        checkJsonStatsAll(v); 
        deleteDataset(dsTest) ;
    }

    @Test public void stats_3() {
        addTestDataset() ;
        try {
            JsonValue v = execGetJSON(urlRoot+"$/"+opStats+"/DoesNotExist") ;
            checkJsonStatsAll(v);
        } catch (HttpException ex) { assertEquals(HttpSC.NOT_FOUND_404, ex.getResponseCode()); }
        deleteDataset(dsTest) ;
    }

    // Sync task testing
    
    @Test public void task_1() {
        String x = execSleepTask(null, 10) ;
        assertNotNull(x) ;
        Integer.parseInt(x) ;
    }
    
    @Test public void task_2() {
        String x = "NoSuchTask" ;
        String url = urlRoot+"$/tasks/"+x ;
        try {
            httpGetJson(url) ;
        } catch (HttpException ex) {
            assertEquals(404, ex.getResponseCode()) ;
        }
        try { 
            checkInTasks(x) ;
            fail("No failure!") ;
        } catch (AssertionError ex) {}
    }

    
    @Test public void task_3() {
        // Timing dependent.
        // Create a "long" running task so we can find it.  
        String x = execSleepTask(null, 100) ;
        checkTask(x) ;
        checkInTasks(x) ;
        assertNotNull(x) ;
        Integer.parseInt(x) ;
    }

    @Test public void task_4() {
        // Timing dependent.
        // Create a "short" running task  
        String x = execSleepTask(null, 1) ;
        // Check exists in the list of all tasks (should be "finished")
        checkInTasks(x) ;
        String url = urlRoot+"$/tasks/"+x ;
        
        boolean finished = false ; 
        for ( int i = 0 ; i < 10 ; i++ ) {
            if ( i != 0 )
                Lib.sleep(25) ;
            JsonValue v = httpGetJson(url) ;
            checkTask(v) ;
            if ( v.getAsObject().hasKey("finished") ) {
                finished = true ;
                break ;
            }
        }
        if ( ! finished )
            fail("Task has not finished") ;
    }
    
    @Test public void task_5() {
        // Short running task - still in info API call.
        String x = execSleepTask(null, 1) ;
        checkInTasks(x) ;
    }

    @Test public void list_backups_1() {
        try ( TypedInputStream in = execHttpGet(urlRoot+"$/"+opListBackups) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType()) ;
            JsonValue v = JSON.parseAny(in) ;
            assertNotNull(v.getAsObject().get("backups")) ; 
        }
    }

    private JsonValue getTask(String taskId) {
        String url = urlRoot+"$/tasks/"+taskId ;
        return httpGetJson(url) ;
    }

    private static JsonValue getDatasetDescription(String dsName) {
    try ( TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets+"/"+dsName) ) {
        assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType()) ;
        JsonValue v = JSON.parse(in) ;
        return v ;
    }
}

// -- Add

private static void addTestDataset() {
    File f = new File("testing/config-ds-1.ttl") ;
    org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8) ;
    HttpEntity e = new FileEntity(f, ct) ;
    execHttpPost(ServerTest.urlRoot+"$/"+opDatasets, e) ;
}

private static void deleteDataset(String name) {
    execHttpDelete(ServerTest.urlRoot+"$/"+opDatasets+"/"+name) ;
}


    static class JsonResponseHandler implements HttpResponseHandler {

        private JsonValue result = null ;
        
        public JsonValue getJSON() {
            return result ;
        }
        
        @Override
        public void handle(String baseIRI, HttpResponse response) throws IOException {
            try ( InputStream in = response.getEntity().getContent() ) {
                result = JSON.parseAny(in) ;
            }
        }
        
    }
    
    
    
    private String execSleepTask(String name, int millis) {
        String url = urlRoot+"$/sleep" ;
        if ( name != null ) {
            if ( name.startsWith("/") )
                name = name.substring(1) ;
            url = url + "/"+name ; 
        }
        
        JsonResponseHandler x = new JsonResponseHandler() ; 
        HttpOp.execHttpPost(url+"?interval="+millis, null, WebContent.contentTypeJSON, x) ;
        JsonValue v = x.getJSON() ;
        String id = v.getAsObject().get("taskId").getAsString().value() ;
        return id ;
    }

    private JsonValue httpGetJson(String url) {
        JsonResponseHandler x = new JsonResponseHandler() ; 
        HttpOp.execHttpGet(url, WebContent.contentTypeJSON, x) ;
        return x.getJSON() ;
    }
    
    private void checkTask(String x) {
        String url = urlRoot+"$/tasks/"+x ;
        JsonValue v = httpGetJson(url) ;
        checkTask(v) ;
    }    
    
    private void checkTask(JsonValue v) {
        assertNotNull(v) ;
        assertTrue(v.isObject()) ;
        //System.out.println(v) ;
        JsonObject obj = v.getAsObject() ;
        try {
            assertTrue(obj.hasKey("task")) ;
            assertTrue(obj.hasKey("taskId")) ;
            // Not present until it runs : "started"
        } catch (AssertionError ex) { 
            System.out.println(obj) ;
            throw ex ; 
        }
    }
        
   private void checkInTasks(String x) {
       String url = urlRoot+"$/tasks" ;
       JsonValue v = httpGetJson(url) ;
       assertTrue(v.isArray()) ;
       JsonArray array = v.getAsArray() ; 
       int found = 0 ;
       for ( int i = 0 ; i < array.size() ; i++ ) {
           JsonValue jv = array.get(i) ;
           assertTrue(jv.isObject()) ;
           JsonObject obj = jv.getAsObject() ;
           checkTask(obj) ;
           if ( obj.get("taskId").getAsString().value().equals(x) ) {
               found++ ;
           }
        }
       assertEquals("Occurence of taskId count", 1, found) ;
    }

    // Auxilary
    
    private static void askPing(String name) {
        if ( name.startsWith("/") )
            name = name.substring(1) ;
        try ( TypedInputStream in = execHttpGet(urlRoot+name+"/sparql?query=ASK%7B%7D") ) {}  
    }
    
    private static void adminPing(String name) {
        try ( TypedInputStream in = execHttpGet(urlRoot+"$/"+opDatasets+"/"+name) ) {} 
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
        JsonValue v = getDatasetDescription(name) ;
        assertFalse(v.getAsObject().get("ds.state").getAsBoolean().value()) ;
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
        assertNotNull(obj.get("ds.state")) ;
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
        checkJsonStatsCounters(v) ;
        JsonObject obj1 = v.getAsObject().get("endpoints").getAsObject() ;
        for ( String srvName : obj1.keys() ) {
            JsonObject obj2 = obj1.get(srvName).getAsObject() ; 
            assertTrue(obj2.hasKey("description"));
            assertTrue(obj2.hasKey("operation"));
            checkJsonStatsCounters(obj2);
        }
    }

    private static void checkJsonStatsCounters(JsonValue v) {
        JsonObject obj = v.getAsObject() ;
        assertTrue(obj.hasKey("Requests")) ;
        assertTrue(obj.hasKey("RequestsGood")) ;
        assertTrue(obj.hasKey("RequestsBad")) ;
    }
    
    private static JsonValue execGetJSON(String url) {
        try ( TypedInputStream in = execHttpGet(url) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType()) ;
            return JSON.parse(in) ; 
        }
    }
    
    /*
        GET     /$/ping 
        POST    /$/ping 
        POST    /$/datasets/    
        GET     /$/datasets/
        DELETE  /$/datasets/*{name}*    
        GET     /$/datasets/*{name}*    
        POST    /$/datasets/*{name}*?state=offline  
        POST    /$/datasets/*{name}*?state=active   
        POST    /$/backup/*{name}*  
        GET     /$/server   
        POST    /$/server/shutdown  
        GET     /$/stats/   
        GET     /$/stats/*{name}*
     */
}

