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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpOp;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.web.HttpSC;
import org.junit.Test;
import org.slf4j.Logger;

public class TestFusekiServerBuild {
    // Most testing happens by use in the test suite.

    @Test public void fuseki_build_1() {
        FusekiServer server = FusekiServer.create().port(3456).build();
        // Not started. Port not assigned.
        assertTrue(server.getHttpPort() == 3456 );
        assertTrue(server.getHttpsPort() == -1 );
    }

    @Test public void fuseki_build_2() {
        FusekiServer server = FusekiServer.create().port(0).build();
        // Not started. Port not assigned.
        assertTrue(server.getHttpPort() == 0 );
        assertTrue(server.getHttpsPort() == -1 );
        server.start();
        try {
            assertFalse(server.getHttpPort() == 0 );
            assertTrue(server.getHttpsPort() == -1 );
        } finally { server.stop(); }
    }

    // The port in "testing/jetty.xml" is 1077

    @Test public void fuseki_ext_jetty_xml_1() {
        FusekiServer server = FusekiServer.create()
                .jettyServerConfig("testing/jetty.xml")
                .add("/ds", DatasetGraphFactory.createTxnMem())
                .build();
        server.start();
        try {
            assertEquals(1077, server.getHttpPort());
            assertEquals(1077, server.getPort());
            String URL = "http://localhost:1077/ds";
            assertEquals(URL, server.datasetURL("ds"));
            try ( RDFConnection conn = RDFConnection.connect(URL) ) {
                boolean b = conn.queryAsk("ASK{}");
            }

        } finally { server.stop(); }
    }

    private static final String DIR = "testing/FusekiEmbedded/";

    // Build with defaults.
    @Test public void fuseki_build_dft_port_01() {
        DatasetGraph dsg = dataset();
        int port = 3330;   // Default port.
        FusekiServer server = FusekiServer.create()
                .add("/ds", dsg)
                .build();
        try {
            assertTrue(server.getDataAccessPointRegistry().isRegistered("/ds"));
            server.start();
            query("http://localhost:"+port+"/ds/query", "SELECT * { ?s ?p ?o}", qExec-> {
                RowSet rs = qExec.select();
                assertFalse(rs.hasNext());
            });
        } finally {
            server.stop();
        }
    }

    // Build - naming does make a difference.
    @Test public void fuseki_build_registry_02() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.make(0, "/ds2", dsg);
        DataAccessPointRegistry registry = server.getDataAccessPointRegistry();
        // But no /ds
        assertEquals(1, registry.size());
        assertTrue(registry.isRegistered("/ds2"));
        assertFalse(registry.isRegistered("/ds"));
    }

    @Test public void fuseki_build_add_data_03() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds1", dsg)
            .build();
        server.start();
        int port = server.getPort();
        try {
            // Add while live.
            Txn.executeWrite(dsg,  ()->{
                Quad q = SSE.parseQuad("(_ :s :p _:b)");
                dsg.add(q);
            });
            query("http://localhost:"+port+"/ds1/query", "SELECT * { ?s ?p ?o}", qExec->{
                RowSet rs = qExec.select();
                long x = Iter.count(rs);
                assertEquals(1, x);
            });
        } finally { server.stop(); }
    }


    @Test public void dataservice_01() {
        DatasetGraph dsg = dataset();
        Txn.executeWrite(dsg,  ()->{
            Quad q = SSE.parseQuad("(_ :s :p _:b)");
            dsg.add(q);
        });

        DataService dataService = DataService.newBuilder(dsg)
            .addEndpoint(Operation.GSP_RW)
            .addEndpoint(Operation.Query)
            .addEndpoint(Operation.Update)
            .build();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/data", dataService)
            .build();
        server.start();
        int port = server.getPort();
        try {
            // Put data in.
            String data = "(graph (:s :p 1) (:s :p 2) (:s :p 3))";
            Graph g = SSE.parseGraph(data);

            // POST (This is posint to the GSP_RW service with no name -> quads operation.
            String destination = "http://localhost:"+port+"/data";
            HttpRDF.httpPutGraph(HttpEnv.getDftHttpClient(), destination, g, RDFFormat.NT);
            // GET
            Graph g2 = HttpRDF.httpGetGraph(destination);
            assertTrue(g.isIsomorphicWith(g2));

            // Query.
            query("http://localhost:"+port+"/data", "SELECT * { ?s ?p ?o}", qExec->{
                RowSet rs = qExec.select();
                long x = Iter.count(rs);
                assertEquals(3, x);
            });
            // Update
            UpdateExecution.service("http://localhost:"+port+"/data").update("CLEAR DEFAULT").execute();
            // Query again.
            query("http://localhost:"+port+"/data", "SELECT * { ?s ?p ?o}", qExec-> {
                RowSet rs = qExec.select();
                assertFalse(rs.hasNext());
            });
        } finally { server.stop(); }
    }

    @Test public void dataservice_read_op_02() {
        DatasetGraph dsg = dataset();
        DataService dSrv = DataService.newBuilder()
            .dataset(dsg)
            .addEndpoint(Operation.Query, "q")
            .addEndpoint(Operation.GSP_R, "gsp")
            .build();

        FusekiServer server = FusekiServer.create()
            .add("/dsrv1", dSrv)
            .port(0)
            .build();
        server.start();
        int port = server.getPort();
        try {
            query("http://localhost:"+port+"/dsrv1/q","ASK{}",x->{});
            String x1 = HttpOp.httpGetString("http://localhost:"+port+"/dsrv1/gsp?default");
            assertNotNull(x1);
        } finally { server.stop(); }
    }

    @Test public void dataservice_static_content_03() {
        DatasetGraph dsg = dataset();
        DataService dSrv = DataService.newBuilder(dsg)
            .addEndpoint(Operation.Query, "q")
            .addEndpoint(Operation.GSP_R, "gsp")
            .build();
        FusekiServer server = FusekiServer.create()
            .add("/dsrv1", dSrv)
            .staticFileBase(DIR)
            .port(0)
            .build();
        server.start();
        int port = server.getPort();

        try {
            query("http://localhost:"+port+"/dsrv1/q","ASK{}",x->{});
            String x1 = HttpOp.httpGetString("http://localhost:"+port+"/dsrv1/gsp?default");
            assertNotNull(x1);
            // Static
            String x2 = HttpOp.httpGetString("http://localhost:"+port+"/test.txt");
            assertNotNull(x2);
        } finally { server.stop(); }
    }

    @Test public void dataservice_dft_op_01() {
        DatasetGraph dsg = dataset();
        DataService dSrv = DataService.newBuilder(dsg)
            .addEndpoint(Operation.Query, "x")
            .addEndpoint(Operation.Update, "x")
            .setPlainOperationChooser((a,eps)->Operation.NoOp)
            .build();
        FusekiServer server = FusekiServer.create()
            .add("/dsrv1", dSrv)
            .staticFileBase(DIR)
            .port(0)
            .build();
        server.start();
        int port = server.getPort();
        try {
            String x2 = HttpOp.httpGetString("http://localhost:"+port+"/dsrv1/x");
            fail("httpGetString should not return");
        } catch (HttpException ex) {
            // Unregistered operation.
            assertEquals(400, ex.getStatusCode());
        }
        finally { server.stop(); }
    }

    @Test public void fuseki_build_no_stats() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds0", dsg)
            .build();
        server.start();
        int port = server.getPort();
        try {
            // No server services
            String x1 = HttpOp.httpGetString("http://localhost:"+port+"/$/ping");
            assertNull(x1);

            String x2 = HttpOp.httpGetString("http://localhost:"+port+"/$/stats");
            assertNull(x2);

            String x3 = HttpOp.httpGetString("http://localhost:"+port+"/$/metrics");
            assertNull(x3);

            HttpException ex = assertThrows(HttpException.class,
                () -> HttpOp.httpPostStream("http://localhost:"+port+"/$/compact/ds", "application/json"));
            assertEquals(404, ex.getStatusCode());
        } finally { server.stop(); }
    }

    @Test public void fuseki_build_ping() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds0", dsg)
            .enablePing(true)
            .build();
        server.start();
        int port = server.getPort();

        String x = HttpOp.httpGetString("http://localhost:"+port+"/$/ping");
        assertNotNull(x);
        server.stop();
    }

    @Test public void fuseki_build_stats() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds0", dsg)
            .enableStats(true)
            .build();
        server.start();
        int port = server.getPort();
        String x = HttpOp.httpGetString("http://localhost:"+port+"/$/stats");
        assertNotNull(x);
        server.stop();
    }

    @Test public void fuseki_build_metrics() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds0", dsg)
            .enableMetrics(true)
            .build();
        server.start();
        int port = server.getPort();
        String x = HttpOp.httpGetString("http://localhost:"+port+"/$/metrics");
        assertNotNull(x);
        server.stop();
    }

    @Test public void fuseki_build_compact() throws IOException {
        FusekiServer server = FusekiServer.create()
            .port(0)
            .parseConfigFile(DIR+"tdb2-config.ttl")
            .enableCompact(true)
            .build();
        server.start();
        int port = server.getPort();
        try(TypedInputStream x0 = HttpOp.httpPostStream("http://localhost:"+port+"/$/compact/FuTest", "application/json")) {
            assertNotNull(x0);
            assertNotEquals(0, x0.readAllBytes().length);

            String x1 = HttpOp.httpGetString("http://localhost:"+port+"/$/tasks");
            assertNotNull(x1);
        } finally {
            server.stop();
        }
    }

    @Test public void fuseki_build_tasks() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds0", dsg)
            .enableTasks(true)
            .build();
        server.start();
        int port = server.getPort();
        String x = HttpOp.httpGetString("http://localhost:"+port+"/$/tasks");
        assertNotNull(x);
        server.stop();
    }

    // Context path.
    @Test public void fuseki_build_08() {
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .contextPath("/ABC")
            .add("/ds", dsg)
            .build();
        server.start();
        int port = server.getPort();
        try {
            String x1 = HttpOp.httpGetString("http://localhost:"+port+"/ds");
            assertNull(x1);
            String x2 = HttpOp.httpGetString("http://localhost:"+port+"/ABC/ds");
            assertNotNull(x2);
        } finally { server.stop(); }
    }

    @Test public void fuseki_build_09() {
        FusekiServer server = FusekiServer.create()
            .port(0)
            .parseConfigFile(DIR+"config.ttl")
            .build();
        server.start();
        int port = server.getPort();
        try {
            query("http://localhost:"+port+"/FuTest", "SELECT * {}", x->{});
        } finally { server.stop(); }
    }

    @Test public void fuseki_build_10() {
        FusekiServer server = FusekiServer.create()
            .port(0)
            .contextPath("/ABC")
            .parseConfigFile(DIR+"config.ttl")
            .build();
        server.start();
        int port = server.getPort();
        try {
            try {
                query("http://localhost:"+port+"/FuTest", "ASK{}", x->{});
            } catch (HttpException ex) {
                assertEquals(HttpSC.METHOD_NOT_ALLOWED_405, ex.getStatusCode());
            }

            query("http://localhost:"+port+"/ABC/FuTest","ASK{}",x->{});
        } finally { server.stop(); }
    }

    // Errors in the configuration file.

    @Test public void fuseki_configFile_01() {
        silent(()->{
            FusekiServer server = FusekiServer.create()
                    .parseConfigFile(DIR+"config-bad-svc.ttl")
                    .build();
            assertEquals(0, server.getDataAccessPointRegistry().size());
        });
    }

    @Test public void fuseki_configFile_02() {
        silent(()->{
            FusekiServer server = FusekiServer.create()
                    .parseConfigFile(DIR+"config-bad-ep.ttl")
                    .build();

            // IF a dataset, then no endpoints.
            server.getDataAccessPointRegistry().forEach((n,dap)->{
                System.err.println(dap.getName());
                System.err.println(dap.getDataService().getEndpoints());
                assertEquals(0, dap.getDataService().getEndpoints().size());
            });

            // No endpoints.
            assertEquals(0, server.getDataAccessPointRegistry().size());
        });
    }

    private static void silent(Runnable action) {
        Logger logger = Fuseki.configLog ;
        String level = LogCtl.getLevel(logger);
        try {
            LogCtl.disable(logger);
            action.run();
        } finally { LogCtl.setLevel(logger, level); }
    }

    /*package*/ static DatasetGraph dataset() {
        return DatasetGraphFactory.createTxnMem();
    }

    /*package*/ static void query(String URL, String query, Consumer<QueryExec> body) {
        try (QueryExec qExec = QueryExecHTTP.newBuilder().endpoint(URL).queryString(query).build() ) {
            body.accept(qExec);
        }
    }

}
