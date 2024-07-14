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

import static org.junit.Assert.assertNotNull;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer ;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.AbstractTestRDFConnection;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.system.Txn ;
import org.apache.jena.update.UpdateException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.web.HttpSC.Code;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test;

public class TestRDFConnectionRemote extends AbstractTestRDFConnection {
    protected static FusekiServer server ;
    private static DatasetGraph serverdsg = DatasetGraphFactory.createTxnMem() ;

    @BeforeClass
    public static void beforeClass() {
        // Enable server output.
        //LogCtl.setLevel(Fuseki.actionLog, "INFO");
        server = FusekiServer.create().loopback(true)
                .verbose(true)
                .port(0)
                .add("/ds", serverdsg)
                .build() ;
        server.start() ;
    }

    @Before
    public void beforeTest() {
        // Clear server
        Txn.executeWrite(serverdsg, ()->serverdsg.clear()) ;
    }

//  @After
//  public void afterTest() {}

    @AfterClass
    public static void afterClass() {
        server.stop();
    }

    @Override
    protected boolean supportsAbort() { return false ; }

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
        test_named_graph_load_remote_200(connection(), "http://host/abc%3E");
    }

    @Test public void named_graph_load_remote_2() {
        test_named_graph_load_remote_200(connection(), "http://host/abc%20xyz");
    }

    @Test public void named_graph_load_remote_3() {
        test_named_graph_load_remote_400(connection(), "http://host/abc<");
    }

    @Test public void named_graph_load_remote_4() {
        test_named_graph_load_remote_400(connection(), "http://host/abc def");
    }

    @Test(expected=QueryParseException.class)
    public void non_standard_syntax_0() {
        // Default setup - local checking.
        try ( RDFConnection conn = connection() ) {
            ResultSet rs = conn.query("FOOBAR").execSelect();
        }
    }

    @Test(expected=QueryParseException.class)
    public void non_standard_syntax_1() {
        RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(true).build();
        try ( conn ) {
            ResultSet rs = conn.query("FOOBAR").execSelect();
        }
    }

    @Test
    public void non_standard_syntax_2() {
        // This should result in a 400 from Fuseki - and not a parse-check before sending.
        RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build();
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
    }

    /** Non-standard query syntax on remote connection with parse check enabled is expected to fail. */
    @Test(expected = QueryParseException.class)
    public void non_standard_syntax_query_remote_1a() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(true).build() ) {
            try (QueryExecution qe = conn.newQuery().query("FOOBAR").build()) { }
        }
    }

    /** Non-standard query syntax on remote connection with parse flag overridden on the builder is expected to work. */
    @Test
    public void non_standard_syntax_query_remote_1b() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(true).build() ) {
            try (QueryExecution qe = conn.newQuery().parseCheck(false).query("FOOBAR").build()) { }
        }
    }

    /** Non-standard query syntax on remote connection with parse check disabled is expected to work. */
    @Test
    public void non_standard_syntax_query_remote_2a() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build() ) {
            try (QueryExecution qe = conn.newQuery().query("FOOBAR").build()) { }
        }
    }

    /** Non-standard query syntax on remote connection with parse flag overridden on the builder is expected to fail. */
    @Test(expected = QueryParseException.class)
    public void non_standard_syntax_query_remote_2b() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build() ) {
            try (QueryExecution qe = conn.newQuery().parseCheck(true).query("FOOBAR").build()) { }
        }
    }

    /** Non-standard update syntax on remote connection with parse check enabled is expected to fail. */
    @Test(expected = QueryParseException.class)
    public void non_standard_syntax_update_remote_1a() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(true).build() ) {
            conn.newUpdate().update("FOOBAR").build();
        }
    }

    /** Non-standard update syntax on remote connection with parse flag overridden on the builder is expected to work. */
    @Test
    public void non_standard_syntax_update_remote_1b() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(true).build() ) {
            conn.newUpdate().parseCheck(false).update("FOOBAR").build();
        }
    }

    /** Non-standard update syntax on remote connection with parse check disabled is expected to work. */
    @Test
    public void non_standard_syntax_update_remote_2a() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build() ) {
            conn.newUpdate().update("FOOBAR").build();
        }
    }

    /** Non-standard update syntax on remote connection with parse flag overridden on the builder is expected to fail. */
    @Test(expected = QueryParseException.class)
    public void non_standard_syntax_update_remote_2b() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build() ) {
            conn.newUpdate().parseCheck(true).update("FOOBAR").build();
        }
    }

    /** Non-standard update syntax on remote connection with parse check disabled is expected to work. */
    @Test
    public void non_standard_syntax_update_remote_3a() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build() ) {
            conn.newUpdate().update("FOO").update("BAR").build();
        }
    }

    /** Non-standard update syntax on remote connection with parse flag disabled on the builder is expected to work. */
    public void non_standard_syntax_update_remote_3b() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(true).build() ) {
            conn.newUpdate().parseCheck(false).update("FOO").update("BAR").build();
        }
    }

    /** Non-standard update syntax with substitution should fail on build. */
    @Test(expected = UpdateException.class)
    public void non_standard_syntax_update_remote_3c() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build() ) {
            conn.newUpdate().parseCheck(false).update("FOO").update("BAR")
                .substitution("foo", RDF.type).build();
        }
    }

    /** Standard update syntax with substitution should work. */
    @Test
    public void standard_syntax_update_remote_1a() {
        try ( RDFConnection conn = RDFConnectionRemote.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build() ) {
            conn.newUpdate()
                .update("INSERT DATA { <a> <b> <c> }")
                .update("INSERT DATA { <x> <y> <z> }")
                .substitution("foo", RDF.type).build();
        }
    }

    // Should work.
    private static void test_named_graph_load_remote_200(RDFConnection connection, String target) {
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

