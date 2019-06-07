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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.web.HttpOp;
import org.junit.After;
import org.junit.Test;

/** Test features */
public class TestFusekiMainCmd {

    // Fuseki Main server
    private FusekiServer server = null;
    private String serverURL = null;

    private void server(String... cmdline) {
        int port = WebLib.choosePort();

        String[] a = Stream.concat(
            Stream.of("--port="+port),
            Arrays.stream(cmdline))
            .toArray(String[]::new);

        FusekiServer server = FusekiMain.build(a);
        server.start();
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
        String x = HttpOp.execHttpGetString(serverURL+"/$/ping");
        assertNotNull(x);
    }

    @Test public void stats_01() {
        server("--mem", "--stats", "/ds");
        String x = HttpOp.execHttpGetString(serverURL+"/$/stats");
        assertNotNull(x);
        JSON.parse(x);
    }
}
