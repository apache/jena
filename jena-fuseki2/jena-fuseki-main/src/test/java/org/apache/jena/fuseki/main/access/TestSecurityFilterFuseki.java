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

package org.apache.jena.fuseki.main.access;

import static org.apache.jena.fuseki.main.access.AccessTestLib.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.access.SecurityContext;
import org.apache.jena.fuseki.access.SecurityContextView;
import org.apache.jena.fuseki.access.SecurityRegistry;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.JettySecurityLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.system.G;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;

public class TestSecurityFilterFuseki {

    private static Stream<Arguments> provideTestArgs() {
        return Stream.of(
//          Arguments.of("TDB",  "data1"),
//          Arguments.of("TDB2", "data2"),
          Arguments.of("TIM",  "data3")
        );
    }

    private String baseUrl(String dsName) {
        return fusekiServer.datasetURL(dsName);
    }

    @SuppressWarnings("removal")
    private static DatasetGraph testdsg1 =  TDB1Factory.createDatasetGraph();
    private static DatasetGraph testdsg2 =  DatabaseMgr.createDatasetGraph();
    private static DatasetGraph testdsg3 =  DatasetGraphFactory.createTxnMem();

    private static FusekiServer fusekiServer;

    // Set up Fuseki with two datasets, "data1" backed by TDB and "data2" backed by TDB2.
    @BeforeAll public static void beforeClass() {
        addTestData(testdsg1);
        addTestData(testdsg2);
        addTestData(testdsg3);

        SecurityRegistry reg = new SecurityRegistry();
        reg.put("userNone", SecurityContext.NONE);
        reg.put("userDft", SecurityContextView.DFT_GRAPH);
        reg.put("user0", new SecurityContextView(Quad.defaultGraphIRI.getURI()));
        reg.put("user1", new SecurityContextView("http://test/g1", Quad.defaultGraphIRI.getURI()));
        reg.put("user2", new SecurityContextView("http://test/g1", "http://test/g2", "http://test/g3"));
        reg.put("user3", new SecurityContextView(Quad.defaultGraphIRI.getURI(), "http://test/g2", "http://test/g3"));

        testdsg1 = DataAccessCtl.controlledDataset(testdsg1, reg);
        testdsg2 = DataAccessCtl.controlledDataset(testdsg2, reg);
        testdsg3 = DataAccessCtl.controlledDataset(testdsg3, reg);

        UserStore userStore = userStore();
        ConstraintSecurityHandler sh = JettySecurityLib.makeSecurityHandler("*", userStore);
        JettySecurityLib.addPathConstraint(sh, "/*");

        // If used, also check log4j2.properties.
        //FusekiLogging.setLogging();
        fusekiServer = FusekiServer.create()
            .securityHandler(sh)
            .port(0)
            //.verbose(true)
            .add("data1", testdsg1)
            .add("data2", testdsg2)
            .add("data3", testdsg3)
            .build();
        fusekiServer.start();
    }

    @AfterAll public static void afterClass() {
        fusekiServer.stop();
    }

    private static UserStore userStore() {
        UserStore userStore = new UserStore();
        String[] roles = new String[]{"**"};
        addUserPassword(userStore, "userNone", "pwNone", roles);
        addUserPassword(userStore, "userDft",  "pwDft",  roles);
        addUserPassword(userStore, "user0",    "pw0",    roles);
        addUserPassword(userStore, "user1",    "pw1",    roles);
        addUserPassword(userStore, "user2",    "pw2",    roles);
        addUserPassword(userStore, "user3",    "pw3",    roles);
        return userStore;
    }

    private static void addUserPassword(UserStore propertyUserStore, String user, String password, String[] roles) {
        Credential cred  = new Password(password);
        propertyUserStore.addUser(user, cred, roles);
    }

    public TestSecurityFilterFuseki() {}

    private static String queryAll        = "SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
//    private static String queryDft        = "SELECT * { ?s ?p ?o }";
//    private static String queryNamed      = "SELECT * { GRAPH ?g { ?s ?p ?o } }";
//
//    private static String queryG2         = "SELECT * { GRAPH <http://test/graph2> { ?s ?p ?o } }";
//    private static String queryGraphNames = "SELECT * { GRAPH ?g { } }";

    private Set<Node> query(String user, String password, String dsName, String queryString) {
        Set<Node> results = new HashSet<>();
        try (RDFConnection conn = RDFConnection.connectPW(baseUrl(dsName), user, password)) {
            conn.queryResultSet(queryString, rs->{
                List<QuerySolution> list = Iter.toList(rs);
                list.stream()
                    .map(qs->qs.get("s"))
                    .filter(Objects::nonNull)
                    .map(RDFNode::asNode)
                    .forEach(n->results.add(n));
            });
        }
        return results;
    }

    private void query401(String user, String password, String dsName, String queryString) {
        queryHttp(401, user, password, dsName, queryString);
    }

    private void query403(String user, String password, String dsName, String queryString) {
        queryHttp(403, user, password, dsName, queryString);
    }

    private void queryHttp(int statusCode, String user, String password, String dsName, String queryString) {
        try {
            query(user, password, dsName, queryString);
            if ( statusCode < 200 && statusCode > 299 )
                fail("Should have responded with "+statusCode);
        } catch (QueryExceptionHTTP ex) {
            assertEquals(statusCode, ex.getStatusCode());
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_userDft(String label, String dsName) {
        Set<Node> results = query("userDft", "pwDft", dsName, queryAll);
        assertSeen(results, s0);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_userNone(String label, String dsName) {
        Set<Node> results = query("userNone", "pwNone", dsName, queryAll);
        assertSeen(results);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_user0(String label, String dsName) {
        Set<Node> results = query("user0", "pw0", dsName, queryAll);
        assertSeen(results, s0);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_user1(String label, String dsName) {
        Set<Node> results = query("user1", "pw1", dsName, queryAll);
        assertSeen(results, s0, s1);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_bad_user(String label, String dsName) {
        query401("userX", "pwX", dsName, queryAll);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_bad_password(String label, String dsName) {
        query401("user0", "not-the-password", dsName, queryAll);
    }

    // Visibility of data.

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_dyn_1(String label, String dsName) {
        Set<Node> results = query("user1", "pw1", dsName, "SELECT * FROM <http://test/g1> { ?s ?p ?o }");
        assertSeen(results, s1);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_dyn_2(String label, String dsName) {
        Set<Node> results = query("user1", "pw1", dsName, "SELECT * FROM <http://test/g2> { ?s ?p ?o }");
        assertSeen(results);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_dyn_3(String label, String dsName) {
        Set<Node> results = query("user1", "pw1", dsName, "SELECT * FROM <http://test/g1> FROM <http://test/g2> { ?s ?p ?o }");
        assertSeen(results,s1);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_dyn_4(String label, String dsName) {
        Set<Node> results = query("user3", "pw3", dsName, "SELECT * FROM <"+Quad.unionGraph.getURI()+"> { ?s ?p ?o }");
        assertSeen(results, s2, s3);
        Set<Node> results2 = query("user3", "pw3", dsName, "SELECT * { GRAPH <"+Quad.unionGraph.getURI()+"> { ?s ?p ?o } }");
        assertEquals(results, results2);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void query_dyn_5(String label, String dsName) {
        Set<Node> results = query("user3", "pw3", dsName, "SELECT * FROM NAMED <http://test/g1> { ?s ?p ?o }");
        assertSeen(results);
        Set<Node> results2 = query("user3", "pw3", dsName, "SELECT * { GRAPH <http://test/g1> { ?s ?p ?o } }");
        assertEquals(results, results2);
    }

    private Set<Node> gsp(String user, String password, String dsName, String graphName) {
        Set<Node> results = new HashSet<>();
        String baseURL = baseUrl(dsName);
        try (RDFLink conn = RDFLink.connectPW(baseUrl(dsName), user, password)) {
            Graph graph = (graphName == null) ? conn.get() : conn.get(graphName);
            // Extract subjects.
            Set<Node> seen = Iter.toSet(G.iterSubjects(graph));
            return seen;
        }
    }

    private void gsp401(String user, String password, String dsName, String graphName) {
        gspHttp(401, user, password, dsName, graphName);
    }

    private void gsp403(String user, String password, String dsName, String graphName) {
        gspHttp(403, user, password, dsName, graphName);
    }

    private void gsp404(String user, String password, String dsName, String graphName) {
        gspHttp(404, user, password, dsName, graphName);
    }

    private void gspHttp(int statusCode, String user, String password, String dsName, String graphName) {
        try {
            gsp(user, password, dsName, graphName);
            if ( statusCode < 200 && statusCode > 299 )
                fail("Should have responded with "+statusCode);
        } catch (HttpException ex) {
            assertEquals(statusCode, ex.getStatusCode());
        }
    }

    // When a graph is not visible, it should return 404 except
    // for the default graph which should be empty.

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_dft_userDft(String label, String dsName) {
        Set<Node> results = gsp("userDft", "pwDft", dsName, null);
        assertSeen(results, s0);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_dft_userNone(String label, String dsName) {
        Set<Node> results = gsp("userNone", "pwNone", dsName, null);
        assertSeen(results);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_dft_user0(String label, String dsName) {
        Set<Node> results = gsp("user0", "pw0", dsName, null);
        assertSeen(results, s0);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_dft_user1(String label, String dsName) {
        Set<Node> results = gsp("user1", "pw1", dsName, null);
        assertSeen(results, s0);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_dft_user2(String label, String dsName) {
        Set<Node> results = gsp("user2", "pw2", dsName, null);
        assertSeen(results);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graph1_userDft(String label, String dsName) {
        gsp404("userDft", "pwDft", dsName, "http://test/g1");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graph1_userNone(String label, String dsName) {
        gsp404("userNone", "pwNone", dsName, "http://test/g1");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graph1_user0(String label, String dsName) {
        gsp404("user0", "pw0", dsName, "http://test/g1");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graph1_user1(String label, String dsName) {
        Set<Node> results = gsp("user1", "pw1", dsName, "http://test/g1");
        assertSeen(results, s1);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graph1_user2(String label, String dsName) {
        gsp404("user2", "pw2", dsName, "http://test/g1");
    }

    // No such graph.
    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graphX_userDft(String label, String dsName) {
        gsp404("userDft", "pwDft", dsName, "http://test/gX");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graphX_userNone(String label, String dsName) {
        gsp404("userNone", "pwNone", dsName, "http://test/gX");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graphX_user0(String label, String dsName) {
        gsp404("user0", "pw0", dsName, "http://test/gX");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graphX_user1(String label, String dsName) {
        gsp404("user1", "pw1", dsName, "http://test/g1X");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_graphX_user2(String label, String dsName) {
        gsp404("user2", "pw2", dsName, "http://test/gX");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_bad_user(String label, String dsName) {
        gsp401("userX", "pwX", dsName, null);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestArgs")
    public void gsp_bad_password(String label, String dsName) {
        gsp401("user0", "not-the-password", dsName, null);
    }
}
