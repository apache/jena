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

package org.apache.jena.fuseki.mod.admin;

import static org.apache.jena.fuseki.mgt.ServerMgtConst.opBackup;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opCompact;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opDatasets;
import static org.apache.jena.fuseki.mgt.ServerMgtConst.opListBackups;
import static org.apache.jena.fuseki.server.ServerConst.opStats;
import static org.apache.jena.http.HttpOp.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.commons.lang3.SystemUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.ctl.JsonConstCtl;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/**
 *  Tests of the admin functionality adding and deleting datasets dynamically.
 *  See also {@link TestAdminAddDatasetTemplate}.
 */
public class TestAdminDatabaseOps extends FusekiServerPerTest {
    // Name of the dataset in the assembler file.
    private static String dsTest      = "test-ds1";

    @BeforeAll public static void logging() {
        FusekiLogging.setLogging();
    }

    @Override
    protected FusekiModules modulesSetup() {
        return FusekiModules.create(FMod_Admin.create());
    }

    @Override
    protected void customizerServer(FusekiServer.Builder builder) {
        builder.add(datasetName(), DatasetGraphFactory.createTxnMem());
    }

    private String datasetName() {
        return "dsg";
    }

    // ---- Backup

    @Test public void create_backup_1() {
        withServer(server -> {
            String id = null;
            try {
                JsonValue v = httpPostRtnJSON(server.serverURL() + "$/" + opBackup + "/" + datasetName());
                id = v.getAsObject().getString("taskId");
            } finally {
                waitForTasksToFinish(server, 1000, 10, 20000);
            }
            assertNotNull(id);
            checkInTasks(server, id);

            // Check a backup was created
            try ( TypedInputStream in = httpGet(server.serverURL()+"$/"+opListBackups) ) {
                assertEqualsContentType(WebContent.contentTypeJSON, in.getContentType());
                JsonValue v = JSON.parseAny(in);
                assertNotNull(v.getAsObject().get("backups"));
                JsonArray a = v.getAsObject().get("backups").getAsArray();
                assertEquals(1, a.size());
            }

            JsonValue task = getTask(server, id);
            assertNotNull(id);
            // Expect task success
            assertTrue(task.getAsObject().getBoolean(JsonConstCtl.success), "Expected task to be marked as successful");
        });
    }

    @Test public void create_backup_2() {
        withServer(server -> {
            HttpTest.expect400(()->{
                JsonValue v = httpPostRtnJSON(server.serverURL() + "$/" + opBackup + "/noSuchDataset");
            });
        });
    }

    @Test public void list_backups_1() {
        withServer(server -> {
            try ( TypedInputStream in = httpGet(server.serverURL()+"$/"+opListBackups) ) {
                assertEqualsContentType(WebContent.contentTypeJSON, in.getContentType());
                JsonValue v = JSON.parseAny(in);
                assertNotNull(v.getAsObject().get("backups"));
            }
        });
    }

    // ---- Compact

    @Test public void compact_01() {
        withServer(server -> {
            assumeNotWindows();

            String testDB = "dsg-tdb2";
            try {
                checkNotThere(server, testDB);
                addTestDatasetTDB2(server, testDB);
                checkExists(server, testDB);

                String id = null;
                try {
                    JsonValue v = httpPostRtnJSON(server.serverURL() + "$/" + opCompact + "/" + testDB);
                    id = v.getAsObject().getString(JsonConstCtl.taskId);
                } finally {
                    waitForTasksToFinish(server, 1000, 500, 20_000);
                }
                assertNotNull(id);
                checkInTasks(server, id);

                JsonValue task = getTask(server, id);
                // ----
                // The result assertion is throwing NPE occasionally on some heavily loaded CI servers.
                // This may be because of server or test code encountering a very long wait.
                // These next statements check the assumed structure of the return.
                assertNotNull(task, "Task value");
                JsonObject obj = task.getAsObject();
                assertNotNull(obj, "Task.getAsObject()");
                // Provoke code to get a stacktrace.
                obj.getBoolean(JsonConstCtl.success);
                // ----
                // The assertion we really wanted to check.
                // Check task success
                assertTrue(task.getAsObject().getBoolean(JsonConstCtl.success),
                        "Expected task to be marked as successful");
            } finally {
                deleteDataset(server, testDB);
            }
        });
    }

    @Test public void compact_02() {
        withServer(server -> {
            HttpTest.expect400(()->{
                JsonValue v = httpPostRtnJSON(server.serverURL() + "$/" + opCompact + "/noSuchDataset");
            });
        });
    }

    private void assumeNotWindows() {
        assumeFalse(SystemUtils.IS_OS_WINDOWS, "Test may be unstable on Windows due to inability to delete memory-mapped files");
    }

    // ---- Server

    // ---- Stats

    @Test public void stats_1() {
        withServer(server -> {
            JsonValue v = execGetJSON(server.serverURL()+"$/"+opStats);
            checkJsonStatsAll(v);
        });
    }

    @Test public void stats_2() {
        withServer(server -> {
            addTestDatasetWithName(server, dsTest);
            JsonValue v = execGetJSON(server.serverURL()+"$/"+opStats+"/"+dsTest);
            checkJsonStatsAll(v);
            deleteDataset(server, dsTest);
        });
    }

    @Test public void stats_3() {
        withServer(server -> {
            addTestDatasetWithName(server, dsTest);
            HttpTest.expect404(()-> execGetJSON(server.serverURL()+"$/"+opStats+"/DoesNotExist"));
            deleteDataset(server, dsTest);
        });
    }

    @Test public void stats_4() {
        withServer(server -> {
            JsonValue v = execPostJSON(server.serverURL()+"$/"+opStats);
            checkJsonStatsAll(v);
        });
    }

    @Test public void stats_5() {
        withServer(server -> {
            addTestDatasetWithName(server, dsTest);
            JsonValue v = execPostJSON(server.serverURL()+"$/"+opStats+"/"+dsTest);
            checkJsonStatsAll(v);
            deleteDataset(server, dsTest);
        });
    }

    // --- List all datasets

    @Test public void list_datasets_1() {
        withServer(server->{
            try ( TypedInputStream in = httpGet(urlRoot(server)+"$/"+opDatasets); ) {
                IO.skipToEnd(in);
            }
        });
    }

    @Test public void list_datasets_2() {
        withServer(server->{
            try ( TypedInputStream in = httpGet(urlRoot(server)+"$/"+opDatasets) ) {
                assertEqualsContentType(WebContent.contentTypeJSON, in.getContentType());
                JsonValue v = JSON.parseAny(in);
                assertNotNull(v.getAsObject().get("datasets"));
                checkJsonDatasetsAll(v);
            }
        });
    }

    // Specific dataset
    @Test public void list_datasets_3() {
        withServer( server->checkExists(server, datasetName()) );
    }

    // Specific dataset
    @Test public void list_datasets_4() {
        withServer( server->{
            HttpTest.expect404( () -> getDatasetDescription(server, "does-not-exist") );
        });
    }

    // Specific dataset
    @Test public void list_datasets_5() {
        withServer( server->{
            JsonValue v = getDatasetDescription(server, datasetName());
            checkJsonDatasetsOne(v.getAsObject());
        });
    }

    private String urlRoot(FusekiServer server) {
        return server.serverURL();
    }

    private void deleteDataset(FusekiServer server, String name) {
        httpDelete(server.serverURL()+"$/"+opDatasets+"/"+name);
    }

    private JsonValue getTask(FusekiServer server, String taskId) {
        String url = server.serverURL()+"$/tasks/"+taskId;
        return httpGetJson(url);
    }

    private JsonValue getDatasetDescription(FusekiServer server, String dsName) {
        if ( dsName.startsWith("/") )
            dsName = dsName.substring(1);
        try (TypedInputStream in = httpGet(server.serverURL() + "$/" + opDatasets + "/" + dsName)) {
            assertEqualsContentType(WebContent.contentTypeJSON, in.getContentType());
            JsonValue v = JSON.parse(in);
            return v;
        }
    }

    private void addTestDatasetWithName(FusekiServer server, String dsName) {
        addTestDatasetWithName(server, dsName, "mem");
    }

    private void addTestDatasetWithName(FusekiServer server, String dsName, String dbType) {
        String URL = server.serverURL()+"$/"+opDatasets+"?dbName="+dsName+"&dbType="+dbType;
        String ct = WebContent.contentTypeTurtle;
        httpPost(URL);
    }

    private void addTestDatasetTDB2(FusekiServer server, String DBname) {
        Objects.nonNull(DBname);
        addTestDatasetWithName(server, DBname, "TDB2");
    }

    private void checkTask(FusekiServer server, JsonValue v) {
        assertNotNull(v);
        assertTrue(v.isObject());
        // System.out.println(v);
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

    private void checkInTasks(FusekiServer server, String x) {
        String url = server.serverURL()+"$/tasks";
        JsonValue v = httpGetJson(url);
        assertTrue(v.isArray());
        JsonArray array = v.getAsArray();
        int found = 0;
        for ( int i = 0; i < array.size(); i++ ) {
            JsonValue jv = array.get(i);
            assertTrue(jv.isObject());
            JsonObject obj = jv.getAsObject();
            checkTask(server, obj);
            if ( obj.getString("taskId").equals(x) ) {
                found++;
            }
        }
        assertEquals(1, found, "Occurrence of taskId count");
    }

    private List<String> runningTasks(FusekiServer server, String... x) {
        String url = server.serverURL()+"$/tasks";
        JsonValue v = httpGetJson(url);
        assertTrue(v.isArray());
        JsonArray array = v.getAsArray();
        List<String> running = new ArrayList<>();
        for ( int i = 0; i < array.size(); i++ ) {
            JsonValue jv = array.get(i);
            assertTrue(jv.isObject());
            JsonObject obj = jv.getAsObject();
            if ( isRunning(server, obj) )
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
    private boolean waitForTasksToFinish(FusekiServer server, int pauseMillis, int pollInterval, int maxWaitMillis) {
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
            List<String> x = runningTasks(server);
            if ( x.isEmpty() )
                return true;
            Lib.sleep(pollInterval);
            now = System.currentTimeMillis();
        }
        return false;
    }

    private boolean isRunning(FusekiServer server, JsonObject taskObj) {
        checkTask(server, taskObj);
        return taskObj.hasKey("started") &&  ! taskObj.hasKey("finished");
    }

    private void askPing(FusekiServer server, String name) {
        if ( name.startsWith("/") )
            name = name.substring(1);
        try ( TypedInputStream in = httpGet(server.serverURL()+name+"/sparql?query=ASK%7B%7D") ) {
            IO.skipToEnd(in);
        }
    }

    private void adminPing(FusekiServer server, String name) {
        try ( TypedInputStream in = httpGet(server.serverURL()+"$/"+opDatasets+"/"+name) ) {
            IO.skipToEnd(in);
        }
    }

    private void checkExists(FusekiServer server, String name)  {
        adminPing(server, name);
        askPing(server, name);
    }

    private void checkNotThere(FusekiServer server, String name) {
        String n = (name.startsWith("/")) ? name.substring(1) : name;
        // Check gone exists.
        HttpTest.expect404(()->  adminPing(server, n));
        HttpTest.expect404(() -> askPing(server, n));
    }

    private void checkJsonStatsAll(JsonValue v) {
        assertNotNull(v.getAsObject().get("datasets"));
        JsonObject a = v.getAsObject().get("datasets").getAsObject();
        for ( String dsname : a.keys() ) {
            JsonValue obj = a.get(dsname).getAsObject();
            checkJsonStatsOne(obj);
        }
    }

    private void checkJsonStatsOne(JsonValue v) {
        checkJsonStatsCounters(v);
        JsonObject obj1 = v.getAsObject().get("endpoints").getAsObject();
        for ( String srvName : obj1.keys() ) {
            JsonObject obj2 = obj1.get(srvName).getAsObject();
            assertTrue(obj2.hasKey("description"));
            assertTrue(obj2.hasKey("operation"));
            checkJsonStatsCounters(obj2);
        }
    }

    private void checkJsonStatsCounters(JsonValue v) {
        JsonObject obj = v.getAsObject();
        assertTrue(obj.hasKey("Requests"));
        assertTrue(obj.hasKey("RequestsGood"));
        assertTrue(obj.hasKey("RequestsBad"));
    }

    private JsonValue execGetJSON(String url) {
        try ( TypedInputStream in = httpGet(url) ) {
            assertEqualsContentType(WebContent.contentTypeJSON, in.getContentType());
            return JSON.parse(in);
        }
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

    private JsonValue execPostJSON(String url) {
        try ( TypedInputStream in = httpPostStream(url, null, null, null) ) {
            assertEqualsContentType(WebContent.contentTypeJSON, in.getContentType());
            return JSON.parse(in);
        }
    }
}
