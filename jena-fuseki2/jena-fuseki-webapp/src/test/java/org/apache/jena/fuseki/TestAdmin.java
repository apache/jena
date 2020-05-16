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

import static org.apache.jena.fuseki.mgt.ServerMgtConst.opDatasets;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opListBackups;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opServer;
import static org.apache.jena.fuseki.server.ServerConst.opPing;
import static org.apache.jena.fuseki.server.ServerConst.opStats;
import static org.apache.jena.riot.web.HttpOp.execHttpDelete;
import static org.apache.jena.riot.web.HttpOp.execHttpGet;
import static org.apache.jena.riot.web.HttpOp.execHttpPost;
import static org.apache.jena.riot.web.HttpOp.execHttpPostStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.mgt.ServerMgtConst;
import org.apache.jena.fuseki.server.ServerConst;
import org.apache.jena.fuseki.test.FusekiTest;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpResponseHandler;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

/** Tests of the admin functionality */
public class TestAdmin extends AbstractFusekiTest {

    // Name of the dataset in the assembler file.
    static String dsTest    = "test-ds1";
    static String dsTestInf = "test-ds4";
    static String fileBase  = "testing/";

    // --- Ping

    @Test public void ping_1() {
        execHttpGet(ServerCtl.urlRoot()+"$/"+opPing);
    }

    @Test public void ping_2() {
        execHttpPost(ServerCtl.urlRoot()+"$/"+opPing, null);
    }

    // --- Server status

    @Test public void server_1() {
        JsonValue jv = httpGetJson(ServerCtl.urlRoot()+"$/"+opServer);
        JsonObject obj = jv.getAsObject();
        // Now optional : assertTrue(obj.hasKey(JsonConst.admin));
        assertTrue(obj.hasKey(ServerConst.datasets));
        assertTrue(obj.hasKey(ServerMgtConst.uptime));
        assertTrue(obj.hasKey(ServerMgtConst.startDT));
    }

    @Test public void server_2() {
        execHttpPost(ServerCtl.urlRoot()+"$/"+opServer, null);
    }

    // --- List all datasets

    @Test public void list_datasets_1() {
        try ( TypedInputStream in = execHttpGet(ServerCtl.urlRoot()+"$/"+opDatasets); ) { }
    }

    @Test public void list_datasets_2() {
        try ( TypedInputStream in = execHttpGet(ServerCtl.urlRoot()+"$/"+opDatasets) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parseAny(in);
            assertNotNull(v.getAsObject().get("datasets"));
            checkJsonDatasetsAll(v);
        }
    }

    // Specific dataset
    @Test public void list_datasets_3() {
        checkExists(ServerCtl.datasetPath());
    }

    // Specific dataset
    @Test public void list_datasets_4() {
        FusekiTest.expect404( () -> getDatasetDescription("does-not-exist") );
    }

    // Specific dataset
    @Test public void list_datasets_5() {
        JsonValue v = getDatasetDescription(ServerCtl.datasetPath());
        checkJsonDatasetsOne(v.getAsObject());
    }

    // Specific dataset
    @Test public void add_delete_dataset_1() {
        checkNotThere(dsTest);

        addTestDataset();

        // Check exists.
        checkExists(dsTest);

        // Remove it.
        deleteDataset(dsTest);
        checkNotThere(dsTest);
    }

    // Try to add twice
    @Test public void add_delete_dataset_2() {
        checkNotThere(dsTest);

        File f = new File(fileBase+"config-ds-plain-1.ttl");
        {
            org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8);
            HttpEntity e = new FileEntity(f, ct);
            execHttpPost(ServerCtl.urlRoot()+"$/"+opDatasets, e);
        }
        // Check exists.
        checkExists(dsTest);
        try {
            org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8);
            HttpEntity e = new FileEntity(f, ct);
            execHttpPost(ServerCtl.urlRoot()+"$/"+opDatasets, e);
        } catch (HttpException ex) {
            assertEquals(HttpSC.CONFLICT_409, ex.getStatusCode());
        }
        // Check exists.
        checkExists(dsTest);
        deleteDataset(dsTest);
    }

    @Test public void add_delete_dataset_3() {
        checkNotThere(dsTest);
        addTestDataset();
        checkExists(dsTest);
        deleteDataset(dsTest);
        checkNotThere(dsTest);
        addTestDataset();
        checkExists(dsTest);
        deleteDataset(dsTest);
    }

    @Test public void add_delete_dataset_4() {
        checkNotThere(dsTest);
        checkNotThere(dsTestInf);
        addTestDatasetInf();
        checkNotThere(dsTest);
        checkExists(dsTestInf);

        deleteDataset(dsTestInf);
        checkNotThere(dsTestInf);
        addTestDatasetInf();
        checkExists(dsTestInf);
        deleteDataset(dsTestInf);
    }

    @Test public void add_delete_dataset_5() {
        // New style operations : cause two fuseki:names
        addTestDataset(fileBase+"config-ds-plain-2.ttl");
        checkExists("test-ds2");
    }

    @Test public void add_error_1() {
        FusekiTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                         ()-> addTestDataset(fileBase+"config-ds-bad-name-1.ttl"));
    }

    @Test public void add_error_2() {
        FusekiTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                         ()-> addTestDataset(fileBase+"config-ds-bad-name-2.ttl"));
    }

    @Test public void add_error_3() {
        FusekiTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                         ()-> addTestDataset(fileBase+"config-ds-bad-name-3.ttl"));
    }

    @Test public void add_error_4() {
        FusekiTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                         ()-> addTestDataset(fileBase+"config-ds-bad-name-4.ttl"));
    }

    @Test public void delete_dataset_1() {
        String name = "NoSuchDataset";
        FusekiTest.expect404( ()-> execHttpDelete(ServerCtl.urlRoot()+"$/"+opDatasets+"/"+name) );
    }

    // ---- Active/Offline.

    @Test public void state_1() {
        // Add one
        addTestDataset();
        try {
            checkExists(dsTest);

            execHttpPost(ServerCtl.urlRoot()+"$/"+opDatasets+"/"+dsTest+"?state=offline", null);

            checkExistsNotActive(dsTest);

            execHttpPost(ServerCtl.urlRoot()+"$/"+opDatasets+"/"+dsTest+"?state=active", null);

            checkExists(dsTest);
        } finally {
            deleteDataset(dsTest);
        }
    }

    @Test public void state_2() {
        addTestDataset();
        execHttpPost(ServerCtl.urlRoot()+"$/"+opDatasets+"/"+dsTest+"?state=offline", null);
        deleteDataset(dsTest);
        checkNotThere(dsTest);
    }

    @Test public void state_3() {
        addTestDataset();
        FusekiTest.expect404(()->execHttpPost(ServerCtl.urlRoot()+"$/"+opDatasets+"/DoesNotExist?state=offline", null));
        deleteDataset(dsTest);
    }

    // ---- Backup

    // ---- Server

    // ---- Stats

    @Test public void stats_1() {
        JsonValue v = execGetJSON(ServerCtl.urlRoot()+"$/"+opStats);
        checkJsonStatsAll(v);
    }

    @Test public void stats_2() {
        addTestDataset();
        JsonValue v = execGetJSON(ServerCtl.urlRoot()+"$/"+opStats+ServerCtl.datasetPath());
        checkJsonStatsAll(v);
        deleteDataset(dsTest);
    }

    @Test public void stats_3() {
        addTestDataset();
        FusekiTest.expect404(()-> execGetJSON(ServerCtl.urlRoot()+"$/"+opStats+"/DoesNotExist"));
        deleteDataset(dsTest);
    }

    @Test public void stats_4() {
        JsonValue v = execPostJSON(ServerCtl.urlRoot()+"$/"+opStats);
        checkJsonStatsAll(v);
    }

    @Test public void stats_5() {
        addTestDataset();
        JsonValue v = execPostJSON(ServerCtl.urlRoot()+"$/"+opStats+ServerCtl.datasetPath());
        checkJsonStatsAll(v);
        deleteDataset(dsTest);
    }

    @Test public void sleep_1() {
        String x = execSleepTask(null, 1);
    }

    @Test public void sleep_2() {
        try {
            String x = execSleepTask(null, -1);
            fail("Sleep call unexpectedly succeed");
        } catch (HttpException ex) {
            assertEquals(400, ex.getStatusCode());
        }
    }

    @Test public void sleep_3() {
        try {
            String x = execSleepTask(null, 20*1000+1);
            fail("Sleep call unexpectedly succeed");
        } catch (HttpException ex) {
            assertEquals(400, ex.getStatusCode());
        }
    }

    // Async task testing

    @Test public void task_1() {
        String x = execSleepTask(null, 10);
        assertNotNull(x);
        Integer.parseInt(x);
    }

    @Test public void task_2() {
        String x = "NoSuchTask";
        String url = ServerCtl.urlRoot()+"$/tasks/"+x;
        FusekiTest.expect404(()->httpGetJson(url) );
        try {
            checkInTasks(x);
            fail("No failure!");
        } catch (AssertionError ex) {}
    }


    @Test public void task_3() {
        // Timing dependent.
        // Create a "long" running task so we can find it.
        String x = execSleepTask(null, 100);
        checkTask(x);
        checkInTasks(x);
        assertNotNull(x);
        Integer.parseInt(x);
    }

    @Test public void task_4() {
        // Timing dependent.
        // Create a "short" running task
        String x = execSleepTask(null, 1);
        // Check exists in the list of all tasks (should be "finished")
        checkInTasks(x);
        String url = ServerCtl.urlRoot()+"$/tasks/"+x;

        boolean finished = false;
        for ( int i = 0; i < 10; i++ ) {
            if ( i != 0 )
                Lib.sleep(25);
            JsonValue v = httpGetJson(url);
            checkTask(v);
            if ( v.getAsObject().hasKey("finished") ) {
                finished = true;
                break;
            }
        }
        if ( ! finished )
            fail("Task has not finished");
    }

    @Test public void task_5() {
        // Short running task - still in info API call.
        String x = execSleepTask(null, 1);
        checkInTasks(x);
    }

    @Test public void task_6() {
        String x1 = execSleepTask(null, 1000);
        String x2 = execSleepTask(null, 1000);
        List<String> running = runningTasks();
        assertTrue(running.size()>1);
        waitForTasksToFinish(1000, 2000);
    }

    @Test public void task_7() {
        try {
            String x1 = execSleepTask(null, 1000);
            String x2 = execSleepTask(null, 1000);
            String x3 = execSleepTask(null, 1000);
            String x4 = execSleepTask(null, 1000);
            try {
                // Try to make test more stable on a loaded CI server.
                // Unloaded the first sleep will fail but due to slowness/burstiness
                // some tasks above may have completed.
                String x5 = execSleepTask(null, 4000);
                String x6 = execSleepTask(null, 4000);
                String x7 = execSleepTask(null, 4000);
                String x8 = execSleepTask(null, 10);
                fail("Managed to add a 5th test");
            } catch (HttpException ex) {
                assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
            }
        } finally {
            waitForTasksToFinish(1000, 4000);
        }
    }

    @Test public void list_backups_1() {
        try ( TypedInputStream in = execHttpGet(ServerCtl.urlRoot()+"$/"+opListBackups) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parseAny(in);
            assertNotNull(v.getAsObject().get("backups"));
        }
    }

    private static JsonValue getTask(String taskId) {
        String url = ServerCtl.urlRoot()+"$/tasks/"+taskId;
        return httpGetJson(url);
    }

    private static JsonValue getDatasetDescription(String dsName) {
        try (TypedInputStream in = execHttpGet(ServerCtl.urlRoot() + "$/" + opDatasets + "/" + dsName)) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parse(in);
            return v;
        }
    }

    // -- Add

    private static void addTestDataset() {
        addTestDataset(fileBase+"config-ds-plain-1.ttl");
    }

    private static void addTestDatasetInf() {
        addTestDataset(fileBase+"config-ds-inf.ttl");
    }

    private static void addTestDataset(String filename) {
        File f = new File(filename);
        org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(WebContent.contentTypeTurtle+"; charset="+WebContent.charsetUTF8);
        HttpEntity e = new FileEntity(f, ct);
        execHttpPost(ServerCtl.urlRoot()+"$/"+opDatasets, e);
    }

    private static void deleteDataset(String name) {
        execHttpDelete(ServerCtl.urlRoot()+"$/"+opDatasets+"/"+name);
    }

    static class JsonResponseHandler implements HttpResponseHandler {

        private JsonValue result = null;

        public JsonValue getJSON() {
            return result;
        }

        @Override
        public void handle(String baseIRI, HttpResponse response) throws IOException {
            try ( InputStream in = response.getEntity().getContent() ) {
                result = JSON.parseAny(in);
            }
        }
    }

    private static String execSleepTask(String name, int millis) {
        String url = ServerCtl.urlRoot()+"$/sleep";
        if ( name != null ) {
            if ( name.startsWith("/") )
                name = name.substring(1);
            url = url + "/"+name;
        }

        JsonResponseHandler x = new JsonResponseHandler();
        HttpOp.execHttpPost(url+"?interval="+millis, null, WebContent.contentTypeJSON, x);
        JsonValue v = x.getJSON();
        String id = v.getAsObject().getString("taskId");
        return id;
    }

    private static JsonValue httpGetJson(String url) {
        JsonResponseHandler x = new JsonResponseHandler();
        HttpOp.execHttpGet(url, WebContent.contentTypeJSON, x);
        return x.getJSON();
    }

    private static void checkTask(String x) {
        String url = ServerCtl.urlRoot()+"$/tasks/"+x;
        JsonValue v = httpGetJson(url);
        checkTask(v);
    }

    private static void checkTask(JsonValue v) {
        assertNotNull(v);
        assertTrue(v.isObject());
        //System.out.println(v);
        JsonObject obj = v.getAsObject();
        try {
            assertTrue(obj.hasKey("task"));
            assertTrue(obj.hasKey("taskId"));
            // Not present until it runs : "started"
        } catch (AssertionError ex) {
            System.out.println(obj);
            throw ex;
        }
    }

   private static void checkInTasks(String x) {
       String url = ServerCtl.urlRoot()+"$/tasks";
       JsonValue v = httpGetJson(url);
       assertTrue(v.isArray());
       JsonArray array = v.getAsArray();
       int found = 0;
       for ( int i = 0; i < array.size(); i++ ) {
           JsonValue jv = array.get(i);
           assertTrue(jv.isObject());
           JsonObject obj = jv.getAsObject();
           checkTask(obj);
           if ( obj.getString("taskId").equals(x) ) {
               found++;
           }
        }
       assertEquals("Occurrence of taskId count", 1, found);
    }

   private static List<String> runningTasks(String... x) {
       String url = ServerCtl.urlRoot()+"$/tasks";
       JsonValue v = httpGetJson(url);
       assertTrue(v.isArray());
       JsonArray array = v.getAsArray();
       List<String> running = new ArrayList<>();
       for ( int i = 0; i < array.size(); i++ ) {
           JsonValue jv = array.get(i);
           assertTrue(jv.isObject());
           JsonObject obj = jv.getAsObject();
           if ( isRunning(obj) )
               running.add(obj.getString("taskId"));
       }
       return running;
   }

   /**
    * Wait for tasks to all finish.
    * Algorithm: wait for {@code pause}, then start polling for upto {@code maxWaitMillis}.
    * Intervals in milliseconds.
    * @param pauseMillis
    * @param maxWaitMillis
    * @return
    */
   private static boolean waitForTasksToFinish(int pauseMillis, int maxWaitMillis) {
       // Wait for them to finish.
       // Divide into chunks
       if ( pauseMillis > 0 )
           Lib.sleep(pauseMillis);
       int waited = 0;
       final int INTERVALS = 10;
       for (int i = 0 ; i < INTERVALS ; i++ ) {
           //System.err.println("Wait: "+i);
           List<String> x = runningTasks();
           if ( x.isEmpty() )
               return true;
           Lib.sleep(maxWaitMillis/INTERVALS);
       }
       return false;
   }

   private static boolean isRunning(JsonObject taskObj) {
       checkTask(taskObj);
       return taskObj.hasKey("started") &&  ! taskObj.hasKey("finished");
   }

    // Auxilary

    private static void askPing(String name) {
        if ( name.startsWith("/") )
            name = name.substring(1);
        try ( TypedInputStream in = execHttpGet(ServerCtl.urlRoot()+name+"/sparql?query=ASK%7B%7D") ) {}
    }

    private static void adminPing(String name) {
        try ( TypedInputStream in = execHttpGet(ServerCtl.urlRoot()+"$/"+opDatasets+"/"+name) ) {}
    }

    private static void checkExists(String name)  {
        adminPing(name);
        askPing(name);
    }

    private static void checkExistsNotActive(String name)  {
        adminPing(name);
        try { askPing(name);
            fail("askPing did not cause an Http Exception");
        } catch ( HttpException ex ) {}
        JsonValue v = getDatasetDescription(name);
        assertFalse(v.getAsObject().get("ds.state").getAsBoolean().value());
    }

    private static void checkNotThere(String name) {
        String n = (name.startsWith("/")) ? name.substring(1) : name;
        // Check gone exists.
        FusekiTest.expect404(()->  adminPing(n) );
        FusekiTest.expect404(() -> askPing(n) );
    }

    private static void checkJsonDatasetsAll(JsonValue v) {
        assertNotNull(v.getAsObject().get("datasets"));
        JsonArray a = v.getAsObject().get("datasets").getAsArray();
        for ( JsonValue v2 : a )
            checkJsonDatasetsOne(v2);
    }

    private static void checkJsonDatasetsOne(JsonValue v) {
        assertTrue(v.isObject());
        JsonObject obj = v.getAsObject();
        assertNotNull(obj.get("ds.name"));
        assertNotNull(obj.get("ds.services"));
        assertNotNull(obj.get("ds.state"));
        assertTrue(obj.get("ds.services").isArray());
    }

    private static void checkJsonStatsAll(JsonValue v) {
        assertNotNull(v.getAsObject().get("datasets"));
        JsonObject a = v.getAsObject().get("datasets").getAsObject();
        for ( String dsname : a.keys() ) {
            JsonValue obj = a.get(dsname).getAsObject();
            checkJsonStatsOne(obj);
        }
    }

    private static void checkJsonStatsOne(JsonValue v) {
        checkJsonStatsCounters(v);
        JsonObject obj1 = v.getAsObject().get("endpoints").getAsObject();
        for ( String srvName : obj1.keys() ) {
            JsonObject obj2 = obj1.get(srvName).getAsObject();
            assertTrue(obj2.hasKey("description"));
            assertTrue(obj2.hasKey("operation"));
            checkJsonStatsCounters(obj2);
        }
    }

    private static void checkJsonStatsCounters(JsonValue v) {
        JsonObject obj = v.getAsObject();
        assertTrue(obj.hasKey("Requests"));
        assertTrue(obj.hasKey("RequestsGood"));
        assertTrue(obj.hasKey("RequestsBad"));
    }

    private static JsonValue execGetJSON(String url) {
        try ( TypedInputStream in = execHttpGet(url) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            return JSON.parse(in);
        }
    }

    private static JsonValue execPostJSON(String url) {
        try ( TypedInputStream in = execHttpPostStream(url, null, null, null) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            return JSON.parse(in);
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

