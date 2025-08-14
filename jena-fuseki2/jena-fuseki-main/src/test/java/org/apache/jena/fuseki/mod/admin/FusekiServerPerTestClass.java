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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.ctl.ActionSleep;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.fuseki.mod.FusekiServerModules;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.awaitility.Awaitility;

/**
 * Framework for running tests on a Fuseki server, with a single server for all tests.
 */
public class FusekiServerPerTestClass {

    private static String serverURL = null;
    private static FusekiServer server = null;

    @BeforeAll public static void logging() {
        FusekiLogging.setLogging();
    }

    @BeforeAll public static void startServer() {
        System.setProperty("FUSEKI_BASE", serverArea());
        FileOps.clearAll(serverArea());

        server = createServerForTest();
        serverURL = server.serverURL();
    }

    protected static String serverArea() {
        return "target/run";
    }

    protected static DataAccessPointRegistry serverRegistry() {
        return server.getDataAccessPointRegistry();
    }

    @AfterAll public static void stopServer() {
        if ( server != null )
            server.stop();
        serverURL = null;
        FusekiServerCtl.clearUpSystemState();
    }

    @BeforeAll public static void setLogging() {
        LogCtl.setLevel(Fuseki.backupLogName, "ERROR");
        LogCtl.setLevel(Fuseki.compactLogName,"ERROR");
        Awaitility.setDefaultPollDelay(20,TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollInterval(50,TimeUnit.MILLISECONDS);
    }

    @AfterAll public static void unsetLogging() {
        LogCtl.setLevel(Fuseki.backupLogName, "WARN");
        LogCtl.setLevel(Fuseki.compactLogName,"WARN");
    }

    // For the one-per-class setup, include the usual modules for jena-fuseki-server.
    private static FusekiModules modulesSetup() {
        return FusekiServerModules.serverModules();
    }

    private static FusekiServer createServerForTest() {
        FusekiModules modules = modulesSetup();
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer testServer = FusekiServer.create()
                .fusekiModules(modules)
                .port(0)
                // Add a database.
                .add(datasetName(), dsg)
                // Action used for testing.
                .addServlet("/$/sleep/*", new ActionSleep())
                .build()
                .start();
        return testServer;
    }

    protected static String urlRoot() {
        return serverURL;
    }

    protected static String adminURL() {
        return serverURL + "$/";
    }

    protected static String datasetName() {
        return "dataset";
    }

    protected FusekiServerPerTestClass() {}

    // One server per test.

    protected void withServer(Consumer<FusekiServer> action) {
        action.accept(server);
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
