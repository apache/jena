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

package org.apache.jena.test.rdfconnection;

import static org.apache.jena.fuseki.main.ConfigureTests.OneServerPerTestSuite;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.ConfigureTests;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.AbstractTestRDFConnection;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.web.HttpSC.Code;

public class TestRDFConnectionRemote extends AbstractTestRDFConnection {
    protected static FusekiServer server;
    private static DatasetGraph serverdsg = DatasetGraphFactory.createTxnMem();
    private static boolean localOneServerPerTestSuite = OneServerPerTestSuite;

    // ==== Common code: TestFusekiStdSetup, TestFusekiStdReadOnlySetup, TestFusekiShaclValidation

    private static Object lock = new Object();

    private static void sync(Runnable action) {
        synchronized(lock) {
            action.run();
        }
    }

    @BeforeAll
    public static void beforeClass() {
        if ( localOneServerPerTestSuite ) {
            server = createServer().start();
        }
    }

    @AfterAll
    public static void afterClass() {
        if ( localOneServerPerTestSuite )
            stopServer(server);
    }

    // ====

    @BeforeEach
    public void beforeTest() {
        if ( !ConfigureTests.OneServerPerTestSuite )
            server = createServer();
        // Clear server
        Txn.executeWrite(serverdsg, ()->serverdsg.clear());
    }

    // ====

    @AfterEach
      public void afterTest() {
          if ( !ConfigureTests.OneServerPerTestSuite ) {
              finishWithServer(server);
              server = null;
          }
      }

    @FunctionalInterface
    interface Action { void run(String datasetURL); }

    protected void withServer(Action action) {
        FusekiServer server = server();
        try {
            String datasetURL = server.datasetURL("/ds");
            sync(()-> {
                action.run(datasetURL);
            });
        } finally {
            finishWithServer(server);
        }
    }

    private static FusekiServer createServer() {
        serverdsg = DatasetGraphFactory.createTxnMem();
        server = FusekiServer.create().loopback(true)
                .verbose(true)
                .port(0)
                .add("/ds", serverdsg)
                .build();
        server.start();
        return server;
    }

    private FusekiServer server() {
        if ( localOneServerPerTestSuite )
            return server;
        else
            return createServer().start();
    }

    private void finishWithServer(FusekiServer server) {
        if ( ConfigureTests.OneServerPerTestSuite )
            return;
        stopServer(server);
    }

    private static void stopServer(FusekiServer server) {
        if ( ! ConfigureTests.CloseTestServers )
            return;
        sync(()->server.stop());
    }

    private static void clearAll(RDFConnection conn) {
        if ( !ConfigureTests.OneServerPerTestSuite )
            try { conn.update("CLEAR ALL"); } catch (Throwable th) {}
    }

    // ====

    @Override
    protected boolean supportsAbort() { return false; }

    @Override
    protected RDFConnection connection() {
        return RDFConnection.connect(server.datasetURL("/ds"));
    }

    // Additional tests
    // This is highly depend on the HTTP stack and how it might encode and decode
    // to the point where the other end might receive %253E (the "%" is itself
    // encoded). At least at the time of writing, the other end did receive
    // the encoded "<" the Jetty request has decoded %3E.

    @Test public void named_graph_load_remote_1() {
        withServer((datasetURL)->{
            test_named_graph_load_remote_200(connection(), "http://host/abc%3E");
        });
    }

    @Test public void named_graph_load_remote_2() {
        withServer((datasetURL)->{
            test_named_graph_load_remote_200(connection(), "http://host/abc%20xyz");
        });
    }

    @Test public void named_graph_load_remote_3() {
        withServer((datasetURL)->{
            test_named_graph_load_remote_400(connection(), "http://host/abc<");
        });
    }

    @Test public void named_graph_load_remote_4() {
        withServer((datasetURL)->{
            test_named_graph_load_remote_400(connection(), "http://host/abc def");
        });
    }

    @Test
    public void non_standard_syntax_0() {
        withServer((datasetURL)->{
            // Default setup - local checking.
            try ( RDFConnection conn = connection() ) {
                assertThrows(QueryParseException.class, ()->{
                    conn.query("FOOBAR");
                });
            }
        });
    }

    @Test
    public void non_standard_syntax_1() {
        withServer((datasetURL)->{
            RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(true).build();
            try ( conn ) {
                assertThrows(QueryParseException.class, ()->{
                    conn.query("FOOBAR");
                });
            }
        });
    }

    @Test
    public void non_standard_syntax_2() {
        withServer((datasetURL)->{
            // This should result in a 400 from Fuseki - and not a parse-check before sending.
            RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build();
            try ( conn ) {
                String level = LogCtl.getLevel(Fuseki.actionLog);
                try {
                    LogCtl.setLevel(Fuseki.actionLog, "ERROR");
                    Runnable action = ()-> {
                        try( QueryExecution qExec = conn.query("FOOBAR") ) {
                            qExec.execSelect();
                        }};
                        FusekiTestLib.expectQueryFail(action, Code.BAD_REQUEST);
                } finally {
                    LogCtl.setLevel(Fuseki.actionLog, level);
                }
            }
        });
    }

    /** Non-standard query syntax on remote connection with parse check enabled is expected to fail. */
    @Test
    public void non_standard_syntax_query_remote_1a() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(true).build() ) {
                assertThrows(QueryParseException.class, ()->{
                    try (QueryExecution qe = conn.newQuery().query("FOOBAR").build()) { }
                });
            }
        });
    }

    /** Non-standard query syntax on remote connection with parse flag overridden on the builder is expected to work. */
    @Test
    public void non_standard_syntax_query_remote_1b() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(true).build() ) {
                try (QueryExecution qe = conn.newQuery().parseCheck(false).query("FOOBAR").build()) { }
            }
        });
    }

    /** Non-standard query syntax on remote connection with parse check disabled is expected to work. */
    @Test
    public void non_standard_syntax_query_remote_2a() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                try (QueryExecution qe = conn.newQuery().query("FOOBAR").build()) { }
            }
        });
    }

    /** Non-standard query syntax on remote connection with parse flag overridden on the builder is expected to fail. */
    @Test
    public void non_standard_syntax_query_remote_2b() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                assertThrows(QueryParseException.class, ()->{
                    try (QueryExecution qe = conn.newQuery().parseCheck(true).query("FOOBAR").build()) { }
                });
            }
        });
    }

    /** Non-standard update syntax on remote connection with parse check enabled is expected to fail. */
    @Test
    public void non_standard_syntax_update_remote_1a() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(true).build() ) {
                assertThrows(QueryParseException.class, ()->{
                    conn.newUpdate().update("FOOBAR");
                });
            }
        });
    }

    /** Non-standard update syntax on remote connection with parse flag overridden on the builder is expected to work. */
    @Test
    public void non_standard_syntax_update_remote_1b() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(true).build() ) {
                conn.newUpdate().parseCheck(false).update("FOOBAR").build();
            }
        });
    }

    /** Non-standard update syntax on remote connection with parse check disabled is expected to work. */
    @Test
    public void non_standard_syntax_update_remote_2a() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                conn.newUpdate().update("FOOBAR").build();
            }
        });
    }

    /** Non-standard update syntax on remote connection with parse flag overridden on the builder is expected to fail. */
    @Test
    public void non_standard_syntax_update_remote_2b() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                assertThrows(QueryParseException.class, ()->{
                    conn.newUpdate().parseCheck(true).update("FOOBAR").build();
                });
            }
        });
    }

    /** Non-standard update syntax on remote connection with parse check disabled is expected to work. */
    @Test
    public void non_standard_syntax_update_remote_3a() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                conn.newUpdate().update("FOO").update("BAR").build();
            }
        });
    }

    /** Non-standard update syntax on remote connection with parse flag disabled on the builder is expected to work. */
    public void non_standard_syntax_update_remote_3b() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(true).build() ) {
                conn.newUpdate().parseCheck(false).update("FOO").update("BAR").build();
            }
        });
    }

    /** Non-standard update syntax with substitution should fail on build. */
    @Test
    public void non_standard_syntax_update_remote_3c() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                var builder = conn.newUpdate()
                        .parseCheck(false)
                        .update("FOO")
                        .update("BAR")
                        .substitution("foo", RDF.type);
                assertThrows(UpdateException.class, ()->{
                    builder.build();
                });
            }
        });
    }

    /** Standard update syntax with substitution should work. */
    @Test
    public void standard_syntax_update_remote_1a() {
        withServer((datasetURL)->{
            RDFNode FALSE = ResourceFactory.createTypedLiteral(false);
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                conn.newUpdate()
                .update("INSERT { <a> <b> <c> } WHERE { FILTER(?foo) }")
                .update("INSERT { <x> <y> <z> } WHERE { FILTER(?foo) }")
                .substitution("foo", FALSE)
                .build()
                .execute();
            }
        });
    }

    /** Standard update syntax with substitution should work when comments are involved. */
    @Test
    public void standard_syntax_update_remote_2b() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnectionRemote.service(datasetURL).parseCheckSPARQL(false).build() ) {
                conn.newUpdate()
                .update("# <update1>\n INSERT { <a> <urn:b> <c> } WHERE { FILTER(false) } # </update1>")
                .update("# <update2>\n INSERT { <d> <urn:e> <f> } WHERE { FILTER(false) } # </update2>")
                .update("# <update3>\n INSERT { <g> <urn:h> <i> } WHERE { FILTER(false) } # </update3>")
                .build()
                .execute();
            }
        });
    }

    // Should work.
    private void test_named_graph_load_remote_200(RDFConnection connection, String target) {
        String testDataFile = DIR+"data.ttl";
        try ( RDFConnection conn = connection ) {
            conn.load(target, testDataFile);
            Model m = conn.fetch(target);
            assertNotNull(m);
        }
    }

    // Should be a bad request.

    private static void test_named_graph_load_remote_400(RDFConnection connection, String target) {
        String logLevel = LogCtl.getLevel(Fuseki.actionLogName);
        LogCtl.setLevel(Fuseki.actionLogName, "ERROR");
        try {
            FusekiTestLib.expect400(()->{
                //Like named_graph_load_1 but unhelpful URI.
                String testDataFile = DIR+"data.ttl";
                try ( RDFConnection conn = connection ) {
                    conn.load(target, testDataFile);
                }
            });
        } finally {
            LogCtl.setLevel(Fuseki.actionLogName, logLevel);
        }
    }
}
