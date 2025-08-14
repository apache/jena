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

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Framework for running tests on a Fuseki server, with a fresh server for each test.
 */
public class FusekiServerPerTest {

    @BeforeAll public static void logging() {
        FusekiLogging.setLogging();
    }

    protected FusekiServerPerTest() {}

    // One server per test.

    protected void withServer(Consumer<FusekiServer> action) {
        withServer(null, action);
    }

    protected void withServer(String configFile, Consumer<FusekiServer> action) {
        FusekiModules modules = modulesSetup();
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer.Builder builder = FusekiServer.create().port(0);

        if ( modules != null )
            builder.fusekiModules(modules);

        if ( configFile != null )
            builder.parseConfigFile(configFile);

        customizerServer(builder);

        FusekiServer testServer = builder.start();
        try {
            action.accept(testServer);
        } finally {
            testServer.stop();
            FusekiServerCtl.clearUpSystemState();
        }
    }

    protected void customizerServer(FusekiServer.Builder builder) {}

    protected FusekiModules modulesSetup() { return null; }

    @BeforeEach public void cleanStart() {
        System.setProperty("FUSEKI_BASE", "target/run");
        FileOps.clearAll("target/run");
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
