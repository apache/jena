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
import static org.apache.jena.http.HttpOp.httpDelete;
import static org.apache.jena.http.HttpOp.httpGet;
import static org.apache.jena.http.HttpOp.httpPost;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.FileNotFoundException;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.SystemUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mgt.FusekiAdmin;
import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *  Tests of the admin functionality adding and deleting datasets dynamically.
 *  See also {@link TestAdminAddDatasetTemplate}.
 */
public class TestAdminAddDatasetsConfigFile extends FusekiServerPerTest {
    // Name of the dataset in the assembler file.
    static String dsTest      = "test-ds1";
    static String dsTestInf   = "test-ds4";

    static final String dsTestTdb2a = "test-tdb2a";
    static final String dsTestTdb2b = "test-tdb2b";
    static final String dsTestTdb2c = "test-tdb2c";
    static String fileBase    = "testing/Config/";

    // Exactly the module under test
    @Override
    protected FusekiModules modulesSetup() {
        return FusekiModules.create(FMod_Admin.create());
    }

    @BeforeAll public static void loggingAdmin() {
        LogCtl.setLevel(Fuseki.adminLogName, "ERROR");
    }

    @AfterAll public static void resetLoggingAdmin() {
        LogCtl.setLevel(Fuseki.adminLogName, "ERROR");
    }

    protected void withServerFileEnabled(Consumer<FusekiServer> action) {
        System.setProperty(FusekiAdmin.allowConfigFileProperty, "true");
        try {
            super.withServer(null, action);
        } finally {
            System.getProperties().remove(FusekiAdmin.allowConfigFileProperty);
        }
    }

    @Test public void add_block_dataset_1() {
        // Blocked by default.
        withServer(server -> {
            FusekiTestLib.expect400(()->addTestDatasetByFile(server, "config-ds-plain-1.ttl"));
        });
    }

    @Test public void add_unblocked_dataset_1() {
        // Blocked by default.
        withServerFileEnabled(server -> {
            addTestDatasetByFile(server, "config-ds-plain-1.ttl");
        });
    }

    // Try to add twice
    @Test public void add_add_dataset_1() {
        withServerFileEnabled(server -> {
            checkNotThere(server, dsTest);

            addTestDatasetByFile(server, "config-ds-plain-1.ttl");
            checkExists(server, dsTest);

            // Second try should fail.
            FusekiTestLib.expect409(()->addTestDatasetByFile(server, "config-ds-plain-1.ttl"));

            // Check still exists.
            checkExists(server, dsTest);
            // Delete-able.
            deleteDataset(server, dsTest);
            checkNotThere(server, dsTest);
        });
    }

    @Test public void add_delete_dataset_1() {
        withServerFileEnabled(server -> {

            checkNotThere(server, dsTest);
            checkNotThere(server, dsTestInf);
            addTestDatasetByFile(server, "config-ds-inf.ttl");
            checkNotThere(server, dsTest);
            checkExists(server, dsTestInf);

            deleteDataset(server, dsTestInf);

            checkNotThere(server, dsTestInf);
            addTestDatasetByFile(server, "config-ds-inf.ttl");
            checkExists(server, dsTestInf);
            deleteDataset(server, dsTestInf);
        });
    }

    @Test public void add_delete_dataset_2() {
        withServerFileEnabled(server -> {
            // New style operations : cause two fuseki:names
            addTestDatasetByFile(server, "config-ds-plain-2.ttl");
            checkExists(server, "test-ds2");
        });
    }

    @Test public void add_delete_dataset_TDB_1() {
        withServerFileEnabled(server -> {

            String testDB = dsTestTdb2a;
            assumeNotWindows();

            checkNotThere(server, testDB);

            addTestDatasetTDB2(server, testDB);

            // Check exists.
            checkExists(server, testDB);

            // Remove it.
            deleteDataset(server, testDB);
            checkNotThere(server, testDB);
        });
    }

    @Test public void add_delete_dataset_TDB_2() {
        withServerFileEnabled(server -> {
            // This has location "--mem--"
            String testDB = dsTestTdb2b;
            checkNotThere(server, testDB);
            addTestDatasetTDB2(server, testDB);
            // Check exists.
            checkExists(server, testDB);
            // Remove it.
            deleteDataset(server, testDB);
            checkNotThere(server, testDB);
        });
    }

    @Test public void add_dataset_error_1() {
        withServerFileEnabled(server -> {
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetByFile(server, "config-ds-bad-name-1.ttl"));
        });
    }

    @Test public void add_dataset_error_2() {
        withServerFileEnabled(server -> {
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetByFile(server, "config-ds-bad-name-2.ttl"));
        });
    }

    @Test public void add_dataset_error_3() {
        withServerFileEnabled(server -> {
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetByFile(server, "config-ds-bad-name-3.ttl"));
        });
    }

    @Test public void add_dataset_error_4() {
        withServerFileEnabled(server -> {
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetByFile(server, "config-ds-bad-name-4.ttl"));
        });
    }

    @Test public void add_dataset_error_5() {
        withServerFileEnabled(server -> {
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetBodyPublisher(server, BodyPublishers.noBody()));
        });
    }

    @Test public void add_dataset_error_6() {
        withServerFileEnabled(server -> {
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetBodyPublisher(server, BodyPublishers.ofString("")));
        });
    }

    @Test public void add_dataset_error_7() {
        withServerFileEnabled(server -> {
            String level = LogCtl.getLevel(Fuseki.adminLog);
            LogCtl.setLevel(Fuseki.adminLog, "FATAL");
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetBodyPublisher(server, BodyPublishers.ofString("JUNK")));
            LogCtl.setLevel(Fuseki.adminLog, level);
        });
    }

    @Test public void add_dataset_error_8() {
        withServerFileEnabled(server -> {
            String level = LogCtl.getLevel(Fuseki.adminLog);
            LogCtl.setLevel(Fuseki.adminLog, "FATAL");
            HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                                           ()-> addTestDatasetTDB2(server, dsTestTdb2c));
            LogCtl.setLevel(Fuseki.adminLog, level);
        });
    }

    @Test public void delete_dataset_1() {
        withServerFileEnabled(server -> {
            String name = "NoSuchDataset";
            HttpTest.expect404( ()-> httpDelete(server.serverURL()+"$/"+opDatasets+"/"+name) );
        });
    }

    // ---- Backup

    private void assumeNotWindows() {
        assumeFalse(SystemUtils.IS_OS_WINDOWS, "Test may be unstable on Windows due to inability to delete memory-mapped files");
    }

    private void deleteDataset(FusekiServer server, String name) {
        httpDelete(server.serverURL()+"$/"+opDatasets+"/"+name);
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
        switch(DBname) {
            case dsTestTdb2a-> addTestDatasetByFile(server, "config-tdb2a.ttl");
            case dsTestTdb2b-> addTestDatasetByFile(server, "config-tdb2b.ttl");
            case dsTestTdb2c-> addTestDatasetByFile(server, "config-tdb2c.ttl");
            default->throw new IllegalArgumentException("No configuration for "+DBname);
        }
    }

    private void addTestDatasetByFile(FusekiServer server, String filename) {
        try {
            Path f = Path.of(fileBase+filename);
            BodyPublisher body = BodyPublishers.ofFile(f);
            addTestDatasetBodyPublisher(server, body);
        } catch (FileNotFoundException e) {
            IO.exception(e);
        }
    }

    private void addTestDatasetBodyPublisher(FusekiServer server, BodyPublisher body) {
        String ct = WebContent.contentTypeTurtle;
        httpPost(server.serverURL()+"$/"+opDatasets, ct, body);
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
}
