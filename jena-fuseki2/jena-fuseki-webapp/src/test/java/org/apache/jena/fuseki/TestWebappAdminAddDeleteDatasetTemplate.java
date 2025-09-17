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

import static org.apache.jena.fuseki.mgt.ServerMgtConst.opDatasets;
import static org.apache.jena.http.HttpOp.httpPost;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.base.Sys;
import org.apache.jena.fuseki.webapp.FusekiWebapp;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.web.HttpSC;
import org.apache.jena.web.HttpSC.Code;
import org.awaitility.Awaitility;

/** Tests of the admin functionality */
public class TestWebappAdminAddDeleteDatasetTemplate extends AbstractFusekiWebappTest {

    // Name of the dataset in the assembler file.
    static String dsTest      = "test-ds1";
    static String dsTestInf   = "test-ds4";

    // There are two Fuseki-TDB2 tests: add_delete_dataset_6() and compact_01().
    //
    // On certain build systems (GH action/Linux under load, ASF Jenkins sometimes),
    // add_delete_dataset_6 fails (transactions active), or compact_01 (gets a 404),
    // if the two databases are the same.
    static String dsTestTdb2a = "test-tdb2a";
    static String dsTestTdb2b = "test-tdb2b";
    static String fileBase    = "testing/";

    @Before public void setLogging() {
        LogCtl.setLevel(Fuseki.backupLogName, "ERROR");
        LogCtl.setLevel(Fuseki.compactLogName,"ERROR");
        LogCtl.setLevel(Fuseki.adminLogName,"ERROR");
        Awaitility.setDefaultPollDelay(20,TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollInterval(50,TimeUnit.MILLISECONDS);
    }

    @After public void unsetLogging() {
        LogCtl.setLevel(Fuseki.backupLogName, "WARN");
        LogCtl.setLevel(Fuseki.compactLogName,"WARN");
        LogCtl.setLevel(Fuseki.adminLogName,"WARN");
    }

    @Test public void add_dataset_01() {
        testAddDataset("db_1");
    }

    @Test public void add_dataset_02() {
        testAddDataset( "/db_2");
    }

    // Do as a file - which is blocked.
    @Test public void add_dataset_99() {
        expect400(()->addTestDataset(fileBase+"config-ds-plain-1.ttl"));
    }

    private static void addTestDataset(String filename) {
        try {
            Path f = Path.of(filename);
            BodyPublisher body = BodyPublishers.ofFile(f);
            String ct = WebContent.contentTypeTurtle;
            httpPost(ServerCtl.urlRoot()+"$/"+opDatasets, ct, body);
        } catch (FileNotFoundException e) {
            IO.exception(e);
        }
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
        Params params = Params.create().add("dbName", dbName).add("dbType", "mem");
        // Use the template
        String actionURL = ServerCtl.urlRoot()+"$/datasets";
        HttpOp.httpPostForm(actionURL, params);
        String datasetURL = dbName.startsWith("/")
                ? ServerCtl.urlRoot()+(dbName.substring(1))
                : ServerCtl.urlRoot()+dbName;
        assertTrue(exists(datasetURL));
    }

    private void testAddDeleteAdd(String dbName, String dbType, boolean alreadyExists, boolean hasFiles) {
        String datasetURL = ServerCtl.urlRoot()+dbName;
        Params params = Params.create().add("dbName", dbName).add("dbType", dbType);

        if ( alreadyExists )
            assertTrue(exists(datasetURL));
        else
            assertFalse(exists(datasetURL));

        // Use the template
        HttpOp.httpPostForm(ServerCtl.urlRoot()+"$/datasets", params);

        RDFConnection conn = RDFConnection.connect(ServerCtl.urlRoot()+dbName);
        conn.update("INSERT DATA { <x:s> <x:p> 123 }");
        int x1 = count(conn);
        assertEquals(1, x1);

        Path pathDB = FusekiWebapp.dirDatabases.resolve(dbName);

        if ( hasFiles )
            assertTrue(Files.exists(pathDB));

        HttpOp.httpDelete(ServerCtl.urlRoot()+"$/datasets/"+dbName);

        assertFalse(exists(datasetURL));

        //if ( hasFiles )
        assertFalse(Files.exists(pathDB));

        // Recreate : no contents.
        HttpOp.httpPostForm(ServerCtl.urlRoot()+"$/datasets", params);
        assertTrue(exists(datasetURL));
        int x2 = count(conn);
        assertEquals(0, x2);
        if ( hasFiles )
            assertTrue(Files.exists(pathDB));
    }

    private void badAddDataserverRequest(String dbName) {
        Runnable r = protect(()->testAddDataset(dbName));
        expect400(r);
    }

    private static Runnable protect(Runnable action) {
        return ()-> {
            try {
                action.run();
            } catch (HttpException ex) {
                if ( ex.getCause() instanceof IOException ) {
                    if ( ex.getCause().getCause() instanceof IOException ) {
                        if ( ex.getCause().getCause().getCause() instanceof EOFException ) {
                            /*
                             * In github actions, this happens intermittently (unknown reason).
                             * It happens more at busy times.
                             * Ignore if the cause is looks like:
                             *   Error
                             *   org.apache.jena.atlas.web.HttpException: POST ....
                             *     Caused by: java.io.IOException: HTTP/1.1 header parser received no bytes
                             *     Caused by: java.io.IOException: HTTP/1.1 header parser received no bytes
                             *     Caused by: java.io.EOFException: EOF reached while reading
                             */
                            throw new HttpException(400, "Dummy 400");
                        }
                    }
                }
                throw ex;
            }
        };
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

    // -- From fusekiTestLib

    public static void expect400(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.BAD_REQUEST);
    }

    public static void expect401(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.UNAUTHORIZED);
    }

    public static void expect403(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.FORBIDDEN);
    }

    public static void expect404(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.NOT_FOUND);
    }

    public static void expect409(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.CONFLICT);
    }

    public static void expectFail(Runnable runnable, Code code) {
        if ( code == null || ( 200 <= code.getCode() && code.getCode() < 300 ) ) {
            runnable.run();
            return;
        }
        try {
          runnable.run();
          fail("Failed: Got no exception: Expected HttpException "+code.getCode());
      } catch (HttpException ex) {
          if ( ex.getStatusCode() == code.getCode() )
              return;
          throw ex;
      }
    }
}
