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

import static org.apache.jena.fuseki.main.FusekiTestLib.expect400;
import static org.apache.jena.fuseki.main.FusekiTestLib.expect404;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery400;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery404;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
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
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecution;
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

    private static final String DIR = "testing/FusekiBuild/";

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
            boolean b = queryASK("http://localhost:"+port+"/dsrv1/q","ASK{}");
            assertTrue(b);

            HttpOp.httpGetDiscard("http://localhost:"+port+"/dsrv1/gsp?default");
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
            boolean b = queryASK("http://localhost:"+port+"/dsrv1/q","ASK{}");
            assertTrue(b);

            HttpOp.httpGetDiscard("http://localhost:"+port+"/dsrv1/gsp?default");
            HttpOp.httpGetDiscard("http://localhost:"+port+"/test.txt");
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
            expect400(()-> HttpOp.httpGetDiscard("http://localhost:"+port+"/dsrv1/x"));
        } finally { server.stop(); }
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
            expect404(()-> HttpOp.httpGetDiscard("http://localhost:"+port+"/$/ping") );

            expect404(()-> HttpOp.httpGetDiscard("http://localhost:"+port+"/$/stats") );

            expect404(()-> HttpOp.httpGetDiscard("http://localhost:"+port+"/$/metrics") );

            expect404(()->
                   HttpOp.httpPostStream("http://localhost:"+port+"/$/compact/ds", "application/json").close() );
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

        try {
            HttpOp.httpGetDiscard("http://localhost:"+port+"/$/ping");
        } finally { server.stop(); }
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
        try {
            HttpOp.httpGetDiscard("http://localhost:"+port+"/$/stats");
        } finally { server.stop(); }
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
        try {
            HttpOp.httpGetDiscard("http://localhost:"+port+"/$/metrics");
        } finally { server.stop(); }
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

            HttpOp.httpGetDiscard("http://localhost:"+port+"/$/tasks");
        } finally { server.stop(); }
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
        try {
            HttpOp.httpGetDiscard("http://localhost:"+port+"/$/tasks");
        } finally { server.stop(); }
    }

    @Test public void fuseki_build_cxtpath_01() {
        // Context path
        DatasetGraph dsg = dataset();
        FusekiServer server = FusekiServer.create()
            .port(0)
            .contextPath("/ABC")
            .add("/ds", dsg)
            .build();
        server.start();
        int port = server.getPort();
        try {
            expect404(()-> HttpOp.httpGetDiscard("http://localhost:"+port+"/ds")) ;
            HttpOp.httpGetDiscard("http://localhost:"+port+"/ABC/ds");
        } finally { server.stop(); }
    }

    @Test public void fuseki_build_cxtpath_02() {
        DatasetGraph dsg = dataset();
        DataService dataService = DataService.newBuilder(dsg)
                                             .addEndpoint(Operation.GSP_R, "get")
                                             .build();
        FusekiServer server = FusekiServer.create()
            .port(0)
            // If the default is explicitly set.
            .contextPath("/")
            .add("/ds", dataService)
            .build();
        server.start();
        int port = server.getPort();

        try {
            expect400(()-> HttpOp.httpGetDiscard("http://localhost:"+port+"/ds") );
            expect400(()-> HttpOp.httpGetDiscard("http://localhost:"+port+"/ds/") );
            // The only endpoint.
            HttpOp.httpGetDiscard("http://localhost:"+port+"/ds/get?default");
            // Again, but as a higher level request.
            Graph g = GSP.service("http://localhost:"+port+"/ds/get").defaultGraph().GET();
            assertNotNull(g);

        } finally { server.stop(); }
    }

    @Test public void fuseki_build_cxtpath_03() {
        // Config file
        FusekiServer server = FusekiServer.create()
            .port(0)
            .parseConfigFile(DIR+"config-plain.ttl")
            .build();
        server.start();
        int port = server.getPort();
        try {
            expectQuery400(()->{
                // Nothing on /FuTest
                query("http://localhost:"+port+"/FuTest", "SELECT * {}", x->x.select());
            });
            query("http://localhost:"+port+"/FuTest/sparql", "SELECT * {}", x->x.select());
        } finally { server.stop(); }
    }

    @Test public void fuseki_build_cxtpath_04() {
        // Context path and config file
        FusekiServer server = FusekiServer.create()
            .port(0)
            .contextPath("/ABC")
            .parseConfigFile(DIR+"config-plain.ttl")
            .build();
        server.start();
        int port = server.getPort();
        try {
            expect404(()->{
                // Low level!
                String url = "http://localhost:"+port+"/FuTest/sparql?query=ASK%7B%7D";
                HttpOp.httpPost(url);
            });
            expectQuery404(()->{
                // No such endpoint. No context path given.
                boolean b = queryASK("http://localhost:"+port+"/FuTest/sparql","ASK{}");
                assertTrue(b);
            });
            boolean b = queryASK("http://localhost:"+port+"/ABC/FuTest/sparql","ASK{}");
            assertTrue(b);
        } finally { server.stop(); }
    }

    @Test public void fuseki_build_cxtpath_05() {
        // Config file with context path
        FusekiServer server = FusekiServer.create()
            .port(0)
            .parseConfigFile(DIR+"config-context-path.ttl")
            .build();
        server.start();
        int port = server.getPort();
        try {
            // Context setting is /ABC
            expectQuery404(()->{
                queryASK("http://localhost:"+port+"/FuTest/sparql", "ASK{}");
            });
            expectQuery400(()->{
                // Dataset exists - no operations -> Bad request
                queryASK("http://localhost:"+port+"/ABC/FuTest", "ASK{}");
            });
            queryASK("http://localhost:"+port+"/ABC/FuTest/sparql","ASK{}");
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
//                System.err.println(dap.getName());
//                System.err.println(dap.getDataService().getEndpoints());
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

    /*package*/ static boolean queryASK(String URL, String query) {
        try (QueryExec qExec = QueryExecHTTP.newBuilder().endpoint(URL).queryString(query).build() ) {
            return qExec.ask();
        }
    }

    /*package*/ static void query(String URL, String query, Consumer<QueryExec> body) {
        try (QueryExec qExec = QueryExecHTTP.newBuilder().endpoint(URL).queryString(query).build() ) {
            body.accept(qExec);
        }
    }

}
