/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.runner.FusekiRunner;
import org.apache.jena.http.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sys.JenaSystem;

/** Testing starting Fuseki */
public class TestFusekiStart {
    // See also TestFusekiServerBuild
    // See also TestFusekiArgs
    // See also TestFusekiSetupInternal

    static { JenaSystem.init(); }

    @Test public void start_build() {
        FusekiServer fusekiServer = FusekiServer.create().port(0).build().start();
        fusekiServer.stop();
    }

    @Test public void start_run() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer fusekiServer = FusekiServer.run(0, "/ds", dsg);
        try {
            String URL = "http://localhost:"+fusekiServer.getPort()+"/ds";
            GSP.service(URL).defaultGraph().GET();
        } finally {
            fusekiServer.stop();
        }
    }

    @Test public void start_make() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer fusekiServer = FusekiServer.make(0, "/ds", dsg).start();
        try {
            String URL = "http://localhost:"+fusekiServer.getPort()+"/ds";
            GSP.service(URL).defaultGraph().GET();
            fusekiServer.stop();
        } finally {
            fusekiServer.stop();
        }
    }

    @Test public void runner_01() {
        FusekiServer server = FusekiRunner.basic().runAsync("--port=0", "--empty");
        server.stop();
    }

    @Test public void runner_02() {
        FusekiServer server = FusekiRunner.main().runAsync("--port=0", "--empty");
        server.stop();
    }

    @Test public void runner_03() {
        FusekiServer server = FusekiRunner.serverPlain().runAsync("--empty");
        server.stop();
    }

    @Test public void runner_04() {
        FusekiServer server = FusekiRunner.serverUI().runAsync("--port=0");
        server.stop();
    }

    // Server URL with trailing slash.
    private static boolean pingServer(String URL) {
        String pingURL = URL+"$/ping";
        try {
            String x = HttpOp.httpGetString(pingURL);
            if ( x == null )
                // 404 - server exists, no pingURL.
                return true;
            return true;
        } catch (HttpException ex) {
            return false;
        }
    }

    // Server URL with trailing slash.
    private static void testDefaultPort(int expectedPort, Supplier<FusekiServer> source) {
        String URL = "http://localhost:"+expectedPort+"/";
        assertFalse(pingServer(URL));

        FusekiServer server = source.get();
        int httpPort = server.getHttpPort();
        assertEquals(expectedPort, httpPort);
        try {
            assertTrue(pingServer(URL));
        } finally { server.stop(); }
        assertFalse(pingServer(URL));
    }

    private static Object lock = new Object();
    private static String adminTestArea = "target/startAdmin";

    @Test public void defaultPort_builder() {
        testDefaultPort(3030, ()->FusekiServer.builder("--empty").start());
    }

    @Test public void defaultPort_basic() {
        synchronized(lock) {
            FileOps.clearAll(adminTestArea);
            testDefaultPort(3030, ()->FusekiRunner.runAsyncBasic("--mem", "/ds"));
            FileOps.clearAll(adminTestArea);
        }
    }

    @Test public void defaultPort_server() {
        synchronized(lock) {
            FileOps.ensureDir(adminTestArea);
            FileOps.clearAll(adminTestArea);
            testDefaultPort(3030, ()->FusekiRunner.runAsyncServerPlain("--empty"));
            FileOps.clearAll(adminTestArea);
        }
    }

    @Test public void defaultPort_serverUI() {
        synchronized(lock) {
            FileOps.ensureDir(adminTestArea);
            FileOps.clearAll(adminTestArea);
            testDefaultPort(3030, ()->FusekiRunner.runAsyncServerUI("--adminBase="+adminTestArea));
            FileOps.clearAll(adminTestArea);
        }
    }
}
