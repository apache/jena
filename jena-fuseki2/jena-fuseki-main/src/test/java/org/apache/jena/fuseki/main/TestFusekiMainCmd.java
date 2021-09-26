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

package org.apache.jena.fuseki.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.junit.After;
import org.junit.Test;

/** Test features */
public class TestFusekiMainCmd {

    private static final String DATABASES="target/Databases";
    // Fuseki Main server
    private FusekiServer server = null;
    private String serverURL = null;

    static { FusekiLogging.setLogging(); }

    private void server(String... cmdline) {
        String[] a = Stream.concat(
            Stream.of("--port=0"),
            Arrays.stream(cmdline))
            .toArray(String[]::new);

        FusekiServer server = FusekiMain.build(a);
        server.start();
        int port = server.getPort();
        serverURL = "http://localhost:"+port;
    }

    @After public void after() {
        if ( server != null )
            server.stop();
    }

    @Test public void general_01() {
        server("--general=/q", "--empty");
        String string = "SELECT * { VALUES ?x {1 2 3} }";
        try ( RDFConnection conn = RDFConnectionFactory.connect(serverURL+"/q", null, null) ) {
            conn.queryResultSet(string, rs-> {
                long count = ResultSetFormatter.consume(rs);
                assertEquals(3,count);
            });
        }
    }

    @Test public void ping_01() {
        server("--mem", "--ping", "/ds");
        String x = HttpOp.httpGetString(serverURL+"/$/ping");
        assertNotNull(x);
    }

    @Test public void stats_01() {
        server("--mem", "--stats", "/ds");
        String x = HttpOp.httpGetString(serverURL+"/$/stats");
        assertNotNull(x);
        JSON.parse(x);
    }

    @Test public void metrics_01() {
        server("--mem", "--metrics", "/ds");
        String x = HttpOp.httpGetString(serverURL+"/$/metrics");
        assertNotNull(x);
    }

    @Test public void compact_01() throws IOException {
        String DB_DIR = DATABASES+"/DB-compact1";
        FileOps.ensureDir(DB_DIR);
        FileOps.clearAll(DB_DIR);
        server("--loc="+DATABASES+"/DB-compact1", "--tdb2", "--compact", "/ds");
        try(TypedInputStream x0 = HttpOp.httpPostStream(serverURL+"/$/compact/ds", "application/json")) {
            assertNotNull(x0);
            assertNotEquals(0, x0.readAllBytes().length);
        }

        String x1 = HttpOp.httpGetString(serverURL+"/$/tasks");
        assertNotNull(x1);
        JSON.parseAny(x1);
        // Leaves "DB-compact" behind.
    }

    @Test public void compact_02() throws IOException {
        String DB_DIR = DATABASES+"/DB-compact2";
        FileOps.ensureDir(DB_DIR);
        FileOps.clearAll(DB_DIR);
        server("--loc="+DATABASES+"/DB-compact2", "--tdb2", "--compact", "/ds");
        try(TypedInputStream x0 = HttpOp.httpPostStream(serverURL+"/$/compact/ds?deleteOld", "application/json")) {
            assertNotNull(x0);
            assertNotEquals(0, x0.readAllBytes().length);
        }
        String x1 = HttpOp.httpGetString(serverURL+"/$/tasks");
        assertNotNull(x1);
        // Leaves "DB-compact" behind.
    }
}
