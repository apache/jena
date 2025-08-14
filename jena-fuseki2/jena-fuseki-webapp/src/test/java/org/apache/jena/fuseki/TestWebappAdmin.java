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

package org.apache.jena.fuseki;

import static org.apache.jena.fuseki.mgt.ServerMgtConst.opBackup;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opDatasets;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opListBackups;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opServer;
import static org.apache.jena.fuseki.server.ServerConst.opPing;
import static org.apache.jena.http.HttpOp.httpGet;
import static org.apache.jena.http.HttpOp.httpGetJson;
import static org.apache.jena.http.HttpOp.httpPost;
import static org.apache.jena.http.HttpOp.httpPostRtnJSON;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.ctl.JsonConstCtl;
import org.apache.jena.fuseki.mgt.ServerMgtConst;
import org.apache.jena.fuseki.server.ServerConst;
import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;
import org.awaitility.Awaitility;

/** Tests of the admin functionality */
public class TestWebappAdmin extends AbstractFusekiWebappTest {

    // Name of the dataset in the assembler file.
    static String fileBase    = "testing/";

    @Before public void setLogging() {
        LogCtl.setLevel(Fuseki.backupLogName, "ERROR");
        LogCtl.setLevel(Fuseki.compactLogName,"ERROR");
        Awaitility.setDefaultPollDelay(20,TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollInterval(50,TimeUnit.MILLISECONDS);
    }

    @After public void unsetLogging() {
        LogCtl.setLevel(Fuseki.backupLogName, "WARN");
        LogCtl.setLevel(Fuseki.compactLogName,"WARN");
    }

    // --- Ping

    @Test public void ping_1() {
        httpGet(ServerCtl.urlRoot()+"$/"+opPing);
    }

    @Test public void ping_2() {
        httpPost(ServerCtl.urlRoot()+"$/"+opPing);
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
        httpPost(ServerCtl.urlRoot()+"$/"+opServer);
    }

    // --- List all datasets

    @Test public void list_datasets_1() {
        try ( TypedInputStream in = httpGet(ServerCtl.urlRoot()+"$/"+opDatasets); ) {
            IO.skipToEnd(in);
        }
    }

    @Test public void list_datasets_2() {
        try ( TypedInputStream in = httpGet(ServerCtl.urlRoot()+"$/"+opDatasets) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parseAny(in);
            assertNotNull(v.getAsObject().get("datasets"));
            checkJsonDatasetsAll(v);
        }
    }

    // Specific dataset
    @Test public void list_datasets_3() {
        checkExists(ServerCtl.datasetName());
    }

    // Specific dataset
    @Test public void list_datasets_4() {
        HttpTest.expect404( () -> getDatasetDescription("does-not-exist") );
    }

    // Specific dataset
    @Test public void list_datasets_5() {
        JsonValue v = getDatasetDescription(ServerCtl.datasetName());
        checkJsonDatasetsOne(v.getAsObject());
    }

    // ---- Backup

    @Test public void create_backup_1() {
        String id = null;
        try {
            JsonValue v = httpPostRtnJSON(ServerCtl.urlRoot() + "$/" + opBackup + "/" + ServerCtl.datasetName());
            id = v.getAsObject().getString("taskId");
        } finally {
            waitForTasksToFinish(1000, 10, 20000);
        }
        Assert.assertNotNull(id);
        checkInTasks(id);

        // Check a backup was created
        try ( TypedInputStream in = httpGet(ServerCtl.urlRoot()+"$/"+opListBackups) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parseAny(in);
            assertNotNull(v.getAsObject().get("backups"));
            JsonArray a = v.getAsObject().get("backups").getAsArray();
            Assert.assertEquals(1, a.size());
        }

        JsonValue task = getTask(id);
        Assert.assertNotNull(id);
        // Expect task success
        Assert.assertTrue("Expected task to be marked as successful", task.getAsObject().getBoolean(JsonConstCtl.success));
    }

    @Test
    public void create_backup_2() {
        HttpTest.expect400(()->{
            JsonValue v = httpPostRtnJSON(ServerCtl.urlRoot() + "$/" + opBackup + "/noSuchDataset");
        });
    }

    @Test public void list_backups_1() {
        try ( TypedInputStream in = httpGet(ServerCtl.urlRoot()+"$/"+opListBackups) ) {
            assertEqualsIgnoreCase(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parseAny(in);
            assertNotNull(v.getAsObject().get("backups"));
        }
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
        HttpTest.expect404(()->httpGetJson(url) );
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
        await().timeout(500,TimeUnit.MILLISECONDS).until(() -> runningTasks().size() > 1);
        await().timeout(2000, TimeUnit.MILLISECONDS).until(() -> runningTasks().isEmpty());
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
            waitForTasksToFinish(1000, 250, 4000);
        }
    }

    private void assertEqualsIgnoreCase(String contenttypejson, String contentType) {}

    private static JsonValue getTask(String taskId) {
        String url = ServerCtl.urlRoot()+"$/tasks/"+taskId;
        return httpGetJson(url);
    }

    private static JsonValue getDatasetDescription(String dsName) {
        if ( dsName.startsWith("/") )
            dsName = dsName.substring(1);
        try (TypedInputStream in = httpGet(ServerCtl.urlRoot() + "$/" + opDatasets + "/" + dsName)) {
            assertEqualsContentType(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parse(in);
            return v;
        }
    }

    private static String execSleepTask(String name, int millis) {
        String url = ServerCtl.urlRoot()+"$/sleep";
        if ( name != null ) {
            if ( name.startsWith("/") )
                name = name.substring(1);
            url = url + "/"+name;
        }

        JsonValue v = httpPostRtnJSON(url+"?interval="+millis);
        String id = v.getAsObject().getString("taskId");
        return id;
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
    * @param pollInterval
    * @param maxWaitMillis
    * @return
    */
   private static boolean waitForTasksToFinish(int pauseMillis, int pollInterval, int maxWaitMillis) {
       // Wait for them to finish.
       // Divide into chunks
       if ( pauseMillis > 0 )
           Lib.sleep(pauseMillis);
       long start = System.currentTimeMillis();
       long endTime = start + maxWaitMillis;
       final int intervals = maxWaitMillis/pollInterval;
       long now = start;
       for (int i = 0 ; i < intervals ; i++ ) {
           // May have waited (much) longer than the pollInterval : heavily loaded build systems.
           if ( now-start > maxWaitMillis )
               break;
           List<String> x = runningTasks();
           if ( x.isEmpty() )
               return true;
           Lib.sleep(pollInterval);
           now = System.currentTimeMillis();
       }
       return false;
   }

   private static boolean isRunning(JsonObject taskObj) {
       checkTask(taskObj);
       return taskObj.hasKey("started") &&  ! taskObj.hasKey("finished");
   }

    private static void askPing(String name) {
        if ( name.startsWith("/") )
            name = name.substring(1);
        try ( TypedInputStream in = httpGet(ServerCtl.urlRoot()+name+"/sparql?query=ASK%7B%7D") ) {
            IO.skipToEnd(in);
        }
    }

    private static void adminPing(String name) {
        try ( TypedInputStream in = httpGet(ServerCtl.urlRoot()+"$/"+opDatasets+"/"+name) ) {
            IO.skipToEnd(in);
        }
    }

    private static void checkExists(String name)  {
        adminPing(name);
        askPing(name);
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

    /** Expect two strings to be non-null and be {@link String#equalsIgnoreCase} */
    protected static void assertEqualsContentType(String expected, String actual) {
        if ( expected == null && actual == null )
            return;
        if ( expected == null || actual == null )
            fail("Expected: "+expected+" Got: "+actual);
        if ( ! expected.equalsIgnoreCase(actual) )
            fail("Expected: "+expected+" Got: "+actual);
    }
}

