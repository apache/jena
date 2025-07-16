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

import static org.apache.jena.fuseki.mgt.ServerMgtConst.opDatasets;
import static org.apache.jena.http.HttpOp.httpPost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.base.Sys;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.web.HttpSC;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests of the admin functionality on an empty server and using the template mechanism.
 * See also {@link TestAdmin}.
 */
public class TestAdminAddDatasetTemplate extends FusekiServerPerTestClass {

    @BeforeAll public static void loggingAdmin() {
        LogCtl.setLevel(Fuseki.adminLogName, "ERROR");
    }

    @AfterAll public static void resetLoggingAdmin() {
        LogCtl.setLevel(Fuseki.adminLogName, "ERROR");
    }

    @Test public void add_dataset_01() {
        testAddDataset("db_1");
    }

    @Test public void add_dataset_02() {
        testAddDataset( "/db_2");
    }

    @Test public void add_dataset_bad_01() {
        String dbName = "db_bad_01";
        testAddDataset(dbName);
        // This should fail.
        FusekiTestLib.expect409(()->testAddDataset(dbName));
    }

    @Test public void add_dataset_bad_02() {
        badAddDataserverRequest("bad_2 illegal");
    }

    @Test public void add_dataset_bad_03() {
        badAddDataserverRequest("bad_3/path");
    }

    @Test public void add_dataset_bad_04() {
        badAddDataserverRequest("");
    }

    @Test public void add_dataset_bad_05() {
        badAddDataserverRequest("   ");
    }

    @Test public void add_dataset_bad_06() {
        badAddDataserverRequest("bad_6_AB CD");
    }

    @Test public void add_dataset_bad_07() {
        badAddDataserverRequest("..");
    }

    @Test public void add_dataset_bad_08() {
        badAddDataserverRequest("/..");
    }

    @Test public void add_dataset_bad_09() {
        badAddDataserverRequest("/../elsewhere");
    }

    @Test public void add_dataset_bad_10() {
        badAddDataserverRequest("//bad_10");
    }

    @Test
    public void noOverwriteExistingConfigFile() {
        withServer(server->{
            try {
                var workingDir = Paths.get(serverArea()).toAbsolutePath();
                var path = workingDir.resolve("configuration/test-ds0-empty.ttl");
                var dbConfig = path.toFile();
                dbConfig.createNewFile();
                try {
                    // refresh the file system so that the file exists
                    dbConfig = path.toFile();
                    assertTrue (dbConfig.exists());
                    assertEquals(0, dbConfig.length());

                    // Try to override the file with a new configuration.
                    String ct = WebContent.contentTypeHTMLForm;
                    String body = "dbName=test-ds0-empty&dbType=mem";
                    HttpException ex = assertThrows(org.apache.jena.atlas.web.HttpException.class,
                                                    ()-> httpPost(server.serverURL() + "$/" + opDatasets, ct, body));
                    assertEquals(HttpSC.CONFLICT_409, ex.getStatusCode());
                    // refresh the file system
                    dbConfig = path.toFile();
                    assertTrue(dbConfig.exists());
                    assertEquals(0, dbConfig.length(), "File should be still empty");
                }
                finally {
                    // Clean up the file.
                    if (Files.exists(path)) {
                        Files.delete(path);
                    }
                }
            } catch (IOException ex) { throw IOX.exception(ex); }
        });
    }

    // add-delete

    @Test public void add_delete_mem_1() {
        testAddDeleteAdd("db_add_delete_1", "mem", false, false);
    }

    @Test public void add_delete_tdb_1() {
        if ( Sys.isWindows  )
            return;
        testAddDeleteAdd("db_add_delete_tdb_1", "tdb2", false, true);
    }

    @Test public void add_delete_tdb_2() {
        if ( Sys.isWindows  )
            return;
        String dbName = "db_add_delete_tdb_2";
        testAddDeleteAdd(dbName, "tdb2", false, true);
    }

    // Attempt to add a in-memory dataset. Used to test the name checking.
    private void testAddDataset(String dbName) {
        withServer(server->{
            String datasetURL = server.datasetURL(dbName);
            Params params = Params.create().add("dbName", dbName).add("dbType", "mem");
            // Use the template
            HttpOp.httpPostForm(adminURL()+"datasets", params);
            assertTrue(exists(datasetURL));
        });
    }

    private void testAddDeleteAdd(String dbName, String dbType, boolean alreadyExists, boolean hasFiles) {
        withServer(server->{
            String datasetURL = server.datasetURL(dbName);
            Params params = Params.create().add("dbName", dbName).add("dbType", dbType);

            if ( alreadyExists )
                assertTrue(exists(datasetURL));
            else
                assertFalse(exists(datasetURL));

            // Use the template
            HttpOp.httpPostForm(adminURL()+"datasets", params);

            RDFConnection conn = RDFConnection.connect(server.datasetURL(dbName));
            conn.update("INSERT DATA { <x:s> <x:p> 123 }");
            int x1 = count(conn);
            assertEquals(1, x1);

            Path pathDB = FusekiServerCtl.dirDatabases.resolve(dbName);

            if ( hasFiles )
                assertTrue(Files.exists(pathDB));

            HttpOp.httpDelete(adminURL()+"datasets/"+dbName);

            assertFalse(exists(datasetURL));

            //if ( hasFiles )
                assertFalse(Files.exists(pathDB));

            // Recreate : no contents.
            HttpOp.httpPostForm(adminURL()+"datasets", params);
            assertTrue(exists(datasetURL), ()->"false: exists("+datasetURL+")");
            int x2 = count(conn);
            assertEquals(0, x2);
            if ( hasFiles )
                assertTrue(Files.exists(pathDB));
        });
    }

    private void badAddDataserverRequest(String dbName) {
        FusekiTestLib.expect400(()->testAddDataset(dbName));
    }

    private static boolean exists(String url) {
        try ( TypedInputStream in = HttpOp.httpGet(url) ) {
            return true;
        } catch (HttpException ex) {
            if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 )
                return false;
            throw ex;
        }
    }

    static int count(RDFConnection conn) {
        try ( QueryExecution qExec = conn.query("SELECT (count(*) AS ?C) { ?s ?p ?o }")) {
            return qExec.execSelect().next().getLiteral("C").getInt();
        }
    }
}

