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

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.web.HttpNames;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.apache.jena.http.HttpLib.execute;
import static org.apache.jena.http.HttpLib.toRequestURI;
import static org.apache.jena.riot.web.HttpNames.METHOD_OPTIONS;
import static org.junit.Assert.*;

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
        try ( RDFConnection conn = RDFConnection.queryConnect(serverURL+"/q") ) {
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

    @Test public void test_CORS_default_success() {
        // given
        String defaultHeaders = "X-Requested-With, Content-Type, Accept, Origin, Last-Modified, Authorization";
        Map<String,String> headersToPass = Map.of("Access-Control-Request-Method", "POST",
                                                  "Origin", "localhost:12345",
                                                  "Access-Control-Request-Headers", defaultHeaders);
        String expectedAllowedHeaders = "X-Requested-With,Content-Type,Accept,Origin,Last-Modified,Authorization";
        server("--mem", "/ds");
        // when
        HttpResponse<InputStream> response = makeOptionsCall(serverURL + "/ds", headersToPass);
        // then
        assertNotNull(response);
        assertEquals(response.statusCode(), 200);
        String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
        assertNotNull("Expecting valid headers", actualAllowedHeaders);
        assertEquals(expectedAllowedHeaders, actualAllowedHeaders);
    }

    @Test public void test_CORS_default_fail() {
        // given
        String unrecognisedHeader = "Content-Type, unknown-header";
        Map<String,String> headersToPass = Map.of("Access-Control-Request-Method", "POST","Access-Control-Request-Headers", unrecognisedHeader);
        server("--mem", "/ds");
        // when
        HttpResponse<InputStream> response = makeOptionsCall(serverURL + "/ds", headersToPass);
        // then
        assertNotNull(response);
        assertEquals(response.statusCode(), 200);
        String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
        assertNull("No headers expected given invalid request", actualAllowedHeaders);
    }

    @Test public void test_CORS_config_success() {
        // given
        String nonDefaultAllowedHeader = "Content-Type, Custom-Header";
        Map<String,String> headersToPass = Map.of("Access-Control-Request-Method", "POST",
                                                  "Origin", "http://localhost:5173",
                                                  "Access-Control-Request-Headers", nonDefaultAllowedHeader);
        String expectedAllowedHeaders = "X-Requested-With,Content-Type,Accept,Origin,Last-Modified,Authorization,Custom-Header";
        server("--mem", "--CORS=testing/Config/cors.properties","/ds");
        // when
        HttpResponse<InputStream> response = makeOptionsCall(serverURL + "/ds", headersToPass);
        // then
        assertNotNull(response);
        assertEquals(response.statusCode(), 200);
        String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
        assertNotNull("Expecting valid headers", actualAllowedHeaders);
        assertEquals(expectedAllowedHeaders, actualAllowedHeaders);
    }

    @Test public void test_CORS_noConfig() {
        // given
        String defaultHeader = "Content-Type";
        Map<String,String> headersToPass = Map.of("Access-Control-Request-Method", "POST","Access-Control-Request-Headers", defaultHeader);
        server("--mem", "--noCORS", "/ds");
        // when
        HttpResponse<InputStream> response = makeOptionsCall(serverURL + "/ds", headersToPass);
        // then
        assertNotNull(response);
        assertEquals(response.statusCode(), 200);
        String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
        assertNull("No headers expected given invalid request", actualAllowedHeaders);
    }

    private HttpResponse<InputStream> makeOptionsCall(String url, Map<String,String> headers) {
        HttpRequest.Builder builder =
                HttpLib.requestBuilderFor(url).uri(toRequestURI(url))
                       .method(METHOD_OPTIONS, HttpRequest.BodyPublishers.noBody());
        for (Map.Entry<String,String> entry : headers.entrySet()){
            builder.header(entry.getKey(), entry.getValue());
        }
        HttpRequest request = builder.build();
        return execute(HttpEnv.getDftHttpClient(), request);
    }
}
