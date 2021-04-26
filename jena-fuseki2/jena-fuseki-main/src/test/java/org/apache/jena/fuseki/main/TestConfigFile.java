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

import java.io.IOException;

import static org.apache.jena.fuseki.test.FusekiTest.expect400;
import static org.apache.jena.fuseki.test.FusekiTest.expect404;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.base.Sys;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

/** Test server configuration by configuration file */
public class TestConfigFile {

    private static final String DIR = "testing/Config/";

    private static final String PREFIXES = StrUtils.strjoinNL
        ("PREFIX afn: <http://jena.apache.org/ARQ/function#>"
        ,"PREFIX fuseki: <http://jena.apache.org/fuseki#>"
        ,"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        ,"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
        , ""
        );

    private static RDFConnection namedServices(String baseURL) {
        return RDFConnectionRemote.create()
            .destination(baseURL)
            .queryEndpoint("sparql")
            .updateEndpoint("update")
            .gspEndpoint("data")
            .build();
    }

    @Test public void basic () {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "basic.ttl");
        server.start();
        try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:"+port+"/ds") ) {
            assertCxtValueNotNull (conn, "CONTEXT:SERVER");
            assertCxtValue        (conn, "CONTEXT:SERVER", "server");
        } finally {
            server.stop();
        }
    }

    @Test public void context() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "context.ttl");
        server.start();
        try {
            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:"+port+"/ds-server") ) {
                assertCxtValue     (conn, "CONTEXT:SERVER",   "server");
                assertCxtValueNull (conn, "CONTEXT:DATASET");
                assertCxtValueNull (conn, "CONTEXT:ENDPOINT");
                assertCxtValue     (conn, "CONTEXT:ABC",      "server-abc");
            }
            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:"+port+"/ds-dataset") ) {
                assertCxtValue     (conn, "CONTEXT:SERVER",   "server");
                assertCxtValue     (conn, "CONTEXT:DATASET",  "dataset");
                assertCxtValueNull (conn, "CONTEXT:ENDPOINT");
                assertCxtValue     (conn, "CONTEXT:ABC",      "dataset-abc");
            }
            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:"+port+"/ds-endpoint") ) {
                assertCxtValue     (conn, "CONTEXT:SERVER",   "server");
                assertCxtValue     (conn, "CONTEXT:DATASET",  "dataset");
                assertCxtValue     (conn, "CONTEXT:ENDPOINT", "endpoint");
                assertCxtValue     (conn, "CONTEXT:ABC",      "endpoint-abc");
            }
        } finally { server.stop(); }
    }

    @Test public void stdServicesNamed () {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "std-named.ttl");
        String serverURL = "http://localhost:"+port+"/ds-named";
        server.start();
        try {
            try ( RDFConnection conn = namedServices(serverURL) ) {
                // Try each operation. The test is whether the operations can be called.
                conn.update("INSERT DATA { <x:s> <x:p> 123 }");
                conn.queryAsk("ASK{}");
                Graph g = conn.fetch().getGraph();
                assertEquals(1, g.size());
            }

            // These should not work because there is a blocking dataset service (no-op).
            try ( RDFConnection conn =  RDFConnectionFactory.connect(serverURL) ) {
                expect400(()->conn.update("INSERT DATA { <x:s> <x:p> 123 }"));
                expect400(()->conn.queryAsk("ASK{}"));
                expect400(()->conn.fetch());
            }
            // Does not cover file upload which is UI-only for HTML file upload.
        } finally {
            server.stop();
        }
    }

    @Test public void stdServicesDirect() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "std-dataset.ttl");
        String serverURL = "http://localhost:"+port+"/ds-direct";
        server.start();
        try {
//            try ( RDFConnection conn =  RDFConnectionFactory.connect(serverURL) ) {
//                conn.update("INSERT DATA { <x:s> <x:p> 123 }");
//                conn.queryAsk("ASK{}");
//                Graph g = conn.fetch().getGraph();
//                assertEquals(1, g.size());
//            }

            // No named endpoints.
            try ( RDFConnection conn = namedServices(serverURL) ) {
                expect404(()->conn.update("INSERT DATA { <x:s> <x:p> 123 }"));
                expect404(()->conn.queryAsk("ASK{}"));
                expect404(()->conn.fetch());
            }

        } finally {
            server.stop();
        }
    }

    @Test public void stdServicesNoConfig() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "std-empty.ttl");
        String serverURL = "http://localhost:"+port+"/ds-no-ep";

        server.start();
        try {
            try ( RDFConnection conn =  RDFConnectionFactory.connect(serverURL) ) {
                // 400 if on the dataset
                expect400(()->conn.queryAsk("ASK{}"));
            }
            RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .destination(serverURL)
                .queryEndpoint("sparql")
                .updateEndpoint("update")
                .gspEndpoint("data");
            try ( RDFConnection conn = builder.build() ) {
                // 404 if on an absent named service
                expect404(()->conn.queryAsk("ASK{}"));
            }
        } finally {
            server.stop();
        }
    }

    // Named services mirrored onto the dataset
    @Test public void stdServicesOldStyle() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "std-old-style.ttl");
        String serverURL = "http://localhost:"+port+"/ds0";

        server.start();
        try {
            RDFConnectionRemoteBuilder builderUnamedServices = RDFConnectionRemote.create()
                .destination(serverURL)
                .updateEndpoint("")
                .queryEndpoint("");
            RDFConnectionRemoteBuilder builderNamedServices = RDFConnectionRemote.create()
                .destination(serverURL)
                .queryEndpoint("sparql")
                .updateEndpoint("update")
                .gspEndpoint("data");

            try ( RDFConnection conn = builderNamedServices.build() ) {
                conn.update("INSERT DATA { <x:s> <x:p> 123 }");
                Graph g = conn.fetch().getGraph();
                assertEquals(1, g.size());
            }

            try ( RDFConnection conn = builderNamedServices.build() ) {
                conn.queryAsk("ASK{}");
                Graph g = conn.fetch().getGraph();
                assertEquals(1, g.size());
            }

            try ( RDFConnection conn = builderUnamedServices.build() ) {
                conn.update("INSERT DATA { <x:s> <x:p> 456 }");
                conn.queryAsk("ASK{}");
                Graph g = conn.fetch().getGraph();
                assertEquals(2, g.size());
            }
        } finally {
            server.stop();
        }
    }

    @Test public void serverMisc() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "server.ttl");
        server.start();
        try {
            String x1 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/ping");
            assertNotNull(x1);
            String x2 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/stats");
            assertNotNull(x2);
            String x3 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/metrics");
            assertNotNull(x3);
        } finally {
            server.stop();
        }
    }

    @Test public void serverTDB2() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "server-tdb2.ttl");
        server.start();
        try {
            String x1 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/ping");
            assertNotNull(x1);
            String x2 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/stats");
            assertNotNull(x2);
            String x3 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/metrics");
            assertNotNull(x3);
        } finally {
            server.stop();
        }
    }

    @Test public void serverTDB2_compact0() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "server-tdb2_compact0.ttl");
        server.start();
        try {
            String x1= HttpOp.execHttpGetString("http://localhost:"+port+"/$/tasks");
            assertNotNull(x1);
            try(TypedInputStream x2 = HttpOp.execHttpPostStream("http://localhost:"+port+"/$/compact/ds", null, "application/json")) {
                assertNotNull(x2);
                assertNotEquals(0, x2.readAllBytes().length);
            } catch (IOException ex) {
                IO.exception(ex);
            }
            String x3 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/tasks/1");
            assertNotNull(x3);
        } finally {
            server.stop();
        }
    }

    @Test public void serverTDB2_compact1() {
        if ( Sys.isWindows ) {
            // NOTE: Skipping deletion test for windows
            return;
        }

        int port = WebLib.choosePort();
        FusekiServer server = server(port, "server-tdb2_compact1.ttl");
        server.start();
        try {
            String x1= HttpOp.execHttpGetString("http://localhost:"+port+"/$/tasks");
            assertNotNull(x1);
            try(TypedInputStream x2 = HttpOp.execHttpPostStream("http://localhost:"+port+"/$/compact/ds?deleteOld", null, "application/json")) {
                assertNotNull(x2);
                assertNotEquals(0, x2.readAllBytes().length);
            } catch (IOException ex) {
                IO.exception(ex);
            }
            String x3 = HttpOp.execHttpGetString("http://localhost:"+port+"/$/tasks/1");
            assertNotNull(x3);
        } finally {
            server.stop();
        }
    }

    @Test public void unionGraph1() {
        unionGraph("tdb1-endpoints.ttl","/ds-tdb1");
    }

    @Test public void unionGraph2() {
        unionGraph("tdb2-endpoints.ttl","/ds-tdb2");
    }

    @Test public void setupOpsSameName() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "setup1.ttl");
        String serverURL = "http://localhost:"+port+"/ds";
        server.start();
        try {
            try ( RDFConnection conn =  RDFConnectionFactory.connect(serverURL+"/sparql") ) {
                conn.update("INSERT DATA { <x:s> <x:p> 1,2,3 }");
                int x = countDftGraph(conn);
                assertEquals(3, x);
            }
        } finally {
            server.stop();
        }
    }

    @Test public void setupRootDataset() {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, "setup2.ttl");
        String serverURL = "http://localhost:"+port+"/";
        server.start();
        try {
            try ( RDFConnection conn =  RDFConnectionFactory.connect(serverURL) ) {
                conn.update("INSERT DATA { <x:s> <x:p> 1,2,3,4 }");
                int x = countDftGraph(conn);
                assertEquals(4, x);
            }
        } finally {
            server.stop();
        }
    }

    private static String NL = "\n";

    private void unionGraph(String fnConfig, String dbName) {
        int port = WebLib.choosePort();
        FusekiServer server = server(port, fnConfig);
        String serverURL = "http://localhost:"+port+dbName;
        server.start();

        try {
            try ( RDFConnection conn =  RDFConnectionFactory.connect(serverURL) ) {
                conn.update("INSERT DATA {"+NL+
                    "<x:s> <x:p> 'dft'"+NL+
                    "GRAPH <x:g1> { <x:s> <x:p> 'g1' }"+NL+
                    "GRAPH <x:g2> { <x:s> <x:p> 'g2' }"+NL+
                    " }");
                int x = countDftGraph(conn);
                assertEquals(1, x);
            }
            // Query union endpoint.
            RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .destination(serverURL)
                .queryEndpoint("sparql-union");
            try ( RDFConnection conn = builder.build() ) {
                int x = countDftGraph(conn);
                assertEquals(2, x);
            }
        } finally {
            server.stop();
        }
    }

    private static int countDftGraph(RDFConnection conn) {
        try ( QueryExecution qExec = conn.query("SELECT (count(*) AS ?C) { ?s ?p ?o }") ) {
            Object obj = qExec.execSelect().nextBinding().get(Var.alloc("C")).getIndexingValue();
            int x = ((Number)obj).intValue();
            return x;
        }
    }



    private static void assertCxtValue(RDFConnection conn, String contextSymbol, String value) {
        String actual =
            conn.query(PREFIXES+"SELECT ?V { BIND(afn:context('"+contextSymbol+"') AS ?V) }")
            .execSelect()
            .nextBinding().get(Var.alloc("V"))
            .getLiteralLexicalForm();
        assertEquals(value, actual);
    }

    private static void assertCxtValueNotNull(RDFConnection conn, String contextSymbol) {
        boolean b = conn.queryAsk(PREFIXES+"ASK { FILTER (afn:context('"+contextSymbol+"') != '' ) }");
        assertTrue(contextSymbol, b);
    }

    private static void assertCxtValueNull(RDFConnection conn, String contextSymbol) {
        boolean b = conn.queryAsk(PREFIXES+"ASK { FILTER (afn:context('"+contextSymbol+"') = '' ) }");
        assertTrue("Not null: "+contextSymbol, b);
    }
    private static void assertQueryTrue(RDFConnection conn, String qs) {
        boolean b = conn.queryAsk(PREFIXES+qs);
        assertTrue(qs, b);
    }

    private static void assertQueryFalse(RDFConnection conn, String qs) {
        boolean b = conn.queryAsk(PREFIXES+qs);
        assertFalse(qs, b);
    }

    private FusekiServer server(int port, String configFile) {
        FusekiServer server = FusekiServer.create()
            .parseConfigFile(DIR+configFile)
            .port(port)
            .build();
        return server;
    }
}
