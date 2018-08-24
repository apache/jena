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

package org.apache.jena.fuseki.access;

import static org.apache.jena.fuseki.access.GraphData.s0;
import static org.apache.jena.fuseki.access.GraphData.s1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.eclipse.jetty.security.PropertyUserStore;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestSecurityFilterFuseki {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        Object[] obj1 = { "TDB", "data1" };
        Object[] obj2 = { "TDB2", "data2" };
        return Arrays.asList(obj1, obj2);
    }

    private final String baseUrl;
    private static final DatasetGraph testdsg1 =  TDBFactory.createDatasetGraph();
    private static final DatasetGraph testdsg2 =  DatabaseMgr.createDatasetGraph();
    private static FusekiServer fusekiServer;

    // Set up Fuseki with two datasets, "data1" backed by TDB and "data2" backed by TDB2.
    @BeforeClass public static void beforeClass() {
        int port = FusekiLib.choosePort();
        GraphData.fill(testdsg1);
        GraphData.fill(testdsg2);
        
        SecurityRegistry reg = new SecurityRegistry();
        reg.put("userNone", SecurityPolicy.NONE);
        reg.put("userDft", SecurityPolicy.DFT_GRAPH);
        reg.put("user0", new SecurityPolicy(Quad.defaultGraphIRI.getURI()));
        reg.put("user1", new SecurityPolicy("http://test/g1", Quad.defaultGraphIRI.getURI()));
        reg.put("user2", new SecurityPolicy("http://test/g1", "http://test/g2", "http://test/g3"));
        
        // XXXX Also need wrapped tests
        DataAccessCtl.controlledDataset(testdsg1, reg);
        DataAccessCtl.controlledDataset(testdsg2, reg);

        UserStore userStore = userStore();
        SecurityHandler sh = JettyLib.makeSecurityHandler("/*", "DatasetRealm", userStore);
        
        fusekiServer = DataAccessCtl.fusekiBuilder(sh,  DataAccessCtl.requestUserServlet)
            .port(port)
            .add("data1", testdsg1)
            .add("data2", testdsg2)
            .build();
        fusekiServer.start();
    }

    @AfterClass public static void afterClass() {
        fusekiServer.stop();
    }

    private static UserStore userStore() {
        PropertyUserStore propertyUserStore = new PropertyUserStore();
        String[] roles = new String[]{"**"};
        addUserPassword(propertyUserStore, "userNone", "pwNone", roles);
        addUserPassword(propertyUserStore, "userDft",  "pwDft",  roles);
        addUserPassword(propertyUserStore, "user0",    "pw0",    roles);
        addUserPassword(propertyUserStore, "user1",    "pw1",    roles);
        addUserPassword(propertyUserStore, "user2",    "pw2",    roles);
        return propertyUserStore;
    }

    private static void addUserPassword(PropertyUserStore propertyUserStore, String user, String password, String[] roles) {
        Credential cred  = new Password(password);
        propertyUserStore.addUser(user, cred, roles);
    }

    public TestSecurityFilterFuseki(String label, String dsName) {
        int port = fusekiServer.getPort();
        baseUrl = "http://localhost:"+port+"/"+dsName;
    }

    private static void assertSeen(Set<Node> visible, Node ... expected) {
        Set<Node> expectedNodes = new HashSet<>(Arrays.asList(expected));
        assertEquals(expectedNodes, visible);
    }

    private static String queryAll        = "SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
    private static String queryDft        = "SELECT * { ?s ?p ?o }";
    private static String queryNamed      = "SELECT * { GRAPH ?g { ?s ?p ?o } }";

    private static String queryG2         = "SELECT * { GRAPH <http://test/graph2> { ?s ?p ?o } }";
    private static String queryGraphNames = "SELECT * { GRAPH ?g { } }";

    private Set<Node> query(String user, String password, String queryString) {
        Set<Node> results = new HashSet<>();
        try (RDFConnection conn = RDFConnectionFactory.connectPW(baseUrl, user, password)) {
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

    private void query401(String user, String password, String queryString) {
        queryHttp(401, user, password, queryString); 
    }

    private void query403(String user, String password, String queryString) {
        queryHttp(403, user, password, queryString); 
    }

    private void queryHttp(int statusCode, String user, String password, String queryString) {
        try {
            query(user, password, queryString);
            if ( statusCode < 200 && statusCode > 299 ) 
                fail("Should have responded with "+statusCode);
        } catch (QueryExceptionHTTP ex) {
            assertEquals(statusCode, ex.getResponseCode());
        }
    }
    
    @Test public void query_userDft() {
        Set<Node> results = query("userDft", "pwDft", queryAll);
        assertSeen(results, s0);
    }

    @Test public void query_userNone() {
        Set<Node> results = query("userNone", "pwNone", queryAll);
        assertSeen(results);
    }

    @Test public void query_user0() {
        Set<Node> results = query("user0", "pw0", queryAll);
        assertSeen(results, s0);
    }
    
    @Test public void query_user1() {
        Set<Node> results = query("user1", "pw1", queryAll);
        assertSeen(results, s0, s1);
    }
    
    @Test public void query_bad_user() {
        query401("userX", "pwX", queryAll);
    }

    @Test public void query_bad_password() {
        query401("user0", "not-the-password", queryAll);
    }

    private Set<Node> gsp(String user, String password, String graphName) {
        Set<Node> results = new HashSet<>();
        try (RDFConnection conn = RDFConnectionFactory.connectPW(baseUrl, user, password)) {
            Model model = graphName == null ? conn.fetch() : conn.fetch(graphName);
            // Extract subjects.
            Set<Node> seen = 
                SetUtils.toSet(
                    Iter.asStream(model.listSubjects())
                        .map(r->r.asNode())
                        );
            return seen;
        }
    }

    private void gsp401(String user, String password, String graphName) {
        gspHttp(401, user, password, graphName); 
    }
    
    private void gsp403(String user, String password, String graphName) {
        gspHttp(403, user, password, graphName);
    }

    private void gsp404(String user, String password, String graphName) {
        gspHttp(404, user, password, graphName);
    }

    private void gspHttp(int statusCode, String user, String password, String queryString) {
        try {
            gsp(user, password, queryString);
            if ( statusCode < 200 && statusCode > 299 ) 
                fail("Should have responded with "+statusCode);
        } catch (HttpException ex) {
            assertEquals(statusCode, ex.getResponseCode());
        }
    }
    
    // When a graph is not visible, it should return 404 except 
    // for the default graph which should be empty.

    @Test public void gsp_dft_userDft() {
        Set<Node> results = gsp("userDft", "pwDft", null);
        assertSeen(results, s0);
    }
    
    @Test public void gsp_dft_userNone() {
        Set<Node> results = gsp("userNone", "pwNone", null);
        assertSeen(results);
    }
    
    @Test public void gsp_dft_user0() {
        Set<Node> results = gsp("user0", "pw0", null);
        assertSeen(results, s0);
    }

    @Test public void gsp_dft_user1() {
        Set<Node> results = gsp("user1", "pw1", null);
        assertSeen(results, s0);
    }
    
    @Test public void gsp_dft_user2() {
        Set<Node> results = gsp("user2", "pw2", null);
        assertSeen(results);
    }
    
    @Test public void gsp_graph1_userDft() {
        gsp404("userDft", "pwDft", "http://test/g1");
    }
    
    @Test public void gsp_graph1_userNone() {
        gsp404("userNone", "pwNone", "http://test/g1");
    }
    
    @Test public void gsp_graph1_user0() {
        gsp404("user0", "pw0", "http://test/g1");
    }

    @Test public void gsp_graph1_user1() {
        Set<Node> results = gsp("user1", "pw1", "http://test/g1");
        assertSeen(results, s1);
    }
    
    @Test public void gsp_graph1_user2() {
        gsp404("user2", "pw2", "http://test/g1");
    }
    
    // No such graph.
    @Test public void gsp_graphX_userDft() {
        gsp404("userDft", "pwDft", "http://test/gX");
    }
    
    @Test public void gsp_graphX_userNone() {
        gsp404("userNone", "pwNone", "http://test/gX");
    }
    
    @Test public void gsp_graphX_user0() {
        gsp404("user0", "pw0", "http://test/gX");
    }

    @Test public void gsp_graphX_user1() {
        gsp404("user1", "pw1", "http://test/g1X");
    }
    
    @Test public void gsp_graphX_user2() {
        gsp404("user2", "pw2", "http://test/gX");
    }
    
    @Test public void gsp_bad_user() {
        gsp401("userX", "pwX", null);
    }

    @Test public void gsp_bad_password() {
        gsp401("user0", "not-the-password", null);
    }
}
