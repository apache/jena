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

package org.apache.jena.fuseki.server;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.fuseki.mod.FusekiServerModules;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpOp;

/**
 * Test for the whole Fuseki server, not components.
 */
public class TestFusekiServer {

    private static String serverBase =  "target/runBase";

    @BeforeAll static void beforeAll() {
        FusekiModules.restoreSystemDefault();
        FusekiServerCtl.clearUpSystemState();
        FusekiLogging.setLogging();
    }

    @AfterEach void afterEach() {
        FusekiModules.restoreSystemDefault();
        FusekiServerCtl.clearUpSystemState();
    }

    @Test
    public void runCmdLine() {
        String runBase = serverBase+"1";
        setup(runBase);
        // Build-run command line
        FusekiServer server = FusekiServerRunner.runAsync("--port=0", "--empty");
        try {
            int port = server.getPort();
            assertNotEquals(0, port, "Port is zero after async start");
        } finally {
            FusekiMain.resetCustomisers();
            server.stop();
            tearDown(runBase);
        }
    }

    @Test
    public void buildRun() {
        String runBase = serverBase+"2";
        setup(runBase);
        // Build-run programmatically.
        FusekiModules serverModules = FusekiServerModules.serverModules();
        FusekiServer server = FusekiServer.create().port(0).fusekiModules(serverModules).build();
        server.start();
        try {
            int port = server.getPort();
            assertNotEquals(0, port, "Port is zero after async start");
            // check it has a UI.
            HttpOp.httpGetString(server.serverURL()+"#");
        } finally {
            server.stop();
            tearDown(runBase);
        }
    }

    private void setup(String runBase) {
        System.setProperty(FusekiServerCtl.envFusekiBase, runBase);
        FileOps.clearAll(runBase);
    }

    private void tearDown(String runBase) {
        FileOps.clearAll(runBase);
    }
}
