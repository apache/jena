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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Test a controlled Dataset with access by TDB filter or general DatasetGraphFiltered. */
@RunWith(Parameterized.class)
public class TestSecurityFilterLocal {
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        // By filtering on the TDB database
        Creator<DatasetGraph> c1 = TDB1Factory::createDatasetGraph;
        Creator<DatasetGraph> c2 = DatabaseMgr::createDatasetGraph;
        Creator<DatasetGraph> c3 = DatasetGraphFactory::createTxnMem;
        Creator<DatasetGraph> c4 = DatasetGraphFactory::create;

        Object[] obj1 = { "TDB/db", c1, true};
        Object[] obj2 = { "TDB2/db", c2, true };

        // By adding the general, but slower, DatasetGraphFilter
        Object[] obj3 = { "TDB/filtered", c1, false };
        Object[] obj4 = { "TDB2/filtered", c2, false };
        Object[] obj5 = { "TIM/filtered", c3, false };
        Object[] obj6 = { "Plain/filtered", c4, false };

        List<Object[]> x = new ArrayList<>();
        return Arrays.asList(obj1, obj2, obj3, obj4, obj5, obj6);
//        x.add(obj1);
//        x.add(obj3);
//        x.add(obj5);
//        return x;
    }

    private final DatasetGraph testdsg;
    private SecurityRegistry reg = new SecurityRegistry();
    private final boolean applyFilterDSG;
    private final boolean applyFilterTDB;

    public TestSecurityFilterLocal(String name, Creator<DatasetGraph> source, boolean applyFilterTDB) {
        DatasetGraph dsgBase = source.create();
        addTestData(dsgBase);
        reg.put("userNone", SecurityContext.NONE);
        reg.put("userDft", SecurityContextView.DFT_GRAPH);
        reg.put("user0", new SecurityContextView(Quad.defaultGraphIRI.getURI()));
        reg.put("user1", new SecurityContextView("http://test/g1", Quad.defaultGraphIRI.getURI()));
        reg.put("user2", new SecurityContextView("http://test/g1", "http://test/g2", "http://test/g3"));

        // and users "*", "_"
        reg.put("*", new SecurityContextView("http://test/g1"));
        reg.put("_", new SecurityContextView("http://test/g1"));

        testdsg = DataAccessCtl.controlledDataset(dsgBase, reg);
        this.applyFilterTDB = applyFilterTDB;
        this.applyFilterDSG = ! applyFilterTDB;
    }

    private static String queryAll        = "SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
    private static String queryDft        = "SELECT * { ?s ?p ?o }";
    private static String queryNamed      = "SELECT * { GRAPH ?g { ?s ?p ?o } }";

    private static String queryG2         = "SELECT * { GRAPH <http://test/graph2> { ?s ?p ?o } }";
    private static String queryGraphNames = "SELECT * { GRAPH ?g { } }";

    private Set<Node> subjects(DatasetGraph dsg, String queryString, SecurityContext sCxt) {
        final DatasetGraph dsg1 = applyFilterDSG
            ? DataAccessCtl.filteredDataset(dsg, sCxt)
            : dsg;
        Dataset ds = DatasetFactory.wrap(dsg1);
        return
            Txn.calculateRead(ds, ()->{
                try(QueryExecution qExec = QueryExecutionFactory.create(queryString, ds)) {
//                    if ( applyFilterTDB && ! sCxt.equals(SecurityContext.NONE)) {
//                        ((SecurityContextView)sCxt).filterTDB(dsg1, qExec);
//                    }
                    if ( applyFilterTDB )
                        sCxt.filterTDB(dsg1, qExec);
                    List<QuerySolution> results = Iter.toList(qExec.execSelect());
                    Stream<Node> stream = results.stream()
                        .map(qs->qs.get("s"))
                        .filter(Objects::nonNull)
                        .map(RDFNode::asNode);
                    return SetUtils.toSet(stream);
                }
            });
    }

    private Set<Node> subjects(DatasetGraph dsg,  Function<DatasetGraph, Graph> graphChoice, String queryString, SecurityContext sCxt) {
        final DatasetGraph dsg1 = applyFilterDSG
            ? DataAccessCtl.filteredDataset(dsg, sCxt)
            : dsg;
        Graph graph = graphChoice.apply(dsg1);
        if ( graph == null )
            // Can't see the graph.
            return Collections.emptySet();
        Model model = ModelFactory.createModelForGraph(graph);
        return
            Txn.calculateRead(testdsg, ()->{
                try(QueryExecution qExec = QueryExecutionFactory.create(queryString, model)) {
                    if ( applyFilterTDB )
                        sCxt.filterTDB(dsg1, qExec);
                    List<QuerySolution> results = Iter.toList(qExec.execSelect());
                    Stream<Node> stream = results.stream().map(qs->qs.get("s")).filter(Objects::nonNull).map(RDFNode::asNode);
                    return SetUtils.toSet(stream);
                }
            });
    }

    private Set<Node> graphs(DatasetGraph dsg, SecurityContext sCxt) {
        // Either applyFilterDSG or applyFilterTDB
        final DatasetGraph dsg1 = applyFilterDSG
            ? DataAccessCtl.filteredDataset(dsg, sCxt)
            : dsg;
        Dataset ds = DatasetFactory.wrap(dsg1);
        return
            Txn.calculateRead(ds, ()->{
                try(QueryExecution qExec = QueryExecutionFactory.create(queryGraphNames, ds)) {
                    if ( applyFilterTDB )
                        sCxt.filterTDB(dsg1, qExec);
                    List<QuerySolution> results = Iter.toList(qExec.execSelect());
                    Stream<Node> stream = results.stream().map(qs->qs.get("g")).filter(Objects::nonNull).map(RDFNode::asNode);
                    return SetUtils.toSet(stream);
                }
            });
    }

    @Test public void filter_setup() {
        Set<Node> visible = subjects(testdsg, queryAll, SecurityContext.NONE);
        assertEquals(0, visible.size());
        assertSeen(visible);
    }

    // QueryExecution
    private void filter_user(String user, Node ... expected) {
        SecurityContext sCxt = reg.get(user);
        Set<Node> visible = subjects(testdsg, queryAll, sCxt);
        assertSeen(visible, expected);
    }

    @Test public void filter_userNone() {
        filter_user("userNone");
    }

    @Test public void filter_userDft() {
        filter_user("userDft", s0);
    }

    @Test public void filter_user0() {
        filter_user("user0", s0);
    }

    @Test public void filter_user1() {
        filter_user("user1", s0, s1);
    }

    @Test public void filter_user2() {
        filter_user("user2", s1, s2, s3);
    }

    @Test public void filter_userX() {
        filter_user("userX");
    }

    // "Access Denied"
    @Test public void no_access_user1() {
        SecurityContext sCxt = reg.get("user1");
        Set<Node> visible = subjects(testdsg, queryG2, sCxt);
        assertTrue(visible.isEmpty());
    }

    @Test public void graph_names_userNone() {
        SecurityContext sCxt = reg.get("userNone");
        Set<Node> visible = graphs(testdsg, sCxt);
        assertSeen(visible);
    }

    @Test public void graph_names_userDft() {
        SecurityContext sCxt = reg.get("userDft");
        Set<Node> visible = graphs(testdsg, sCxt);
        assertSeen(visible);
    }

    @Test public void graph_names_user0() {
        SecurityContext sCxt = reg.get("user0");
        Set<Node> visible = graphs(testdsg, sCxt);
        assertSeen(visible);
    }

    @Test public void graph_names_user1() {
        SecurityContext sCxt = reg.get("user1");
        Set<Node> visible = graphs(testdsg, sCxt);
        assertSeen(visible, g1);
    }

    @Test public void graph_names_user2() {
        SecurityContext sCxt = reg.get("user2");
        Set<Node> visible = graphs(testdsg, sCxt);
        assertSeen(visible, g1, g2, g3);
    }

    @Test public void graph_names_userX() {
        SecurityContext sCxt = reg.get("userX");
        Set<Node> visible = graphs(testdsg, sCxt);
        assertSeen(visible);
    }

    // QueryExecution w/ Union default graph
    private void filter_union_user(String user, Node ... expected) {
        SecurityContext sCxt = reg.get(user);
        Set<Node> visible;
        if ( applyFilterTDB ) {
            // TDB special version. Set the TDB flags for union default graph
            try {
                testdsg.getContext().set(TDB1.symUnionDefaultGraph, true);
                testdsg.getContext().set(TDB2.symUnionDefaultGraph, true);
                visible = subjects(testdsg, queryDft, sCxt);
            } finally {
                // And unset them.
                testdsg.getContext().unset(TDB1.symUnionDefaultGraph);
                testdsg.getContext().unset(TDB2.symUnionDefaultGraph);
            }
        } else {
            visible = subjects(testdsg, dsg->dsg.getUnionGraph(), queryDft, sCxt);
        }
        assertSeen(visible, expected);
    }

    @Test public void filter_union_userNone() {
        filter_union_user("userNone");
    }

    @Test public void filter_union_userDft() {
        // Storage default graph not visible with a union query.
        filter_union_user("userDft");
    }

    @Test public void filter_union_user0() {
        // Storage default graph not visible with a union query.
        filter_union_user("user0");
    }

    @Test public void filter_union_user1() {
        filter_union_user("user1", s1);
    }

    @Test public void filter_union_user2() {
        filter_union_user("user2", s1, s2, s3);
    }

    @Test public void filter_union_userX() {
        filter_union_user("userX");
    }


    // Graph/Model
    @Test public void query_model_userNone() {
        query_model_user(testdsg, dsg->dsg.getDefaultGraph(), "userNone");
    }

    @Test public void query_model_userDft() {
        query_model_user(testdsg, dsg->dsg.getDefaultGraph(), "userDft", s0);
    }

    @Test public void query_model_user0() {
        query_model_user(testdsg, dsg->dsg.getDefaultGraph(), "user0", s0);
    }

    @Test public void query_model_user1() {
        query_model_user(testdsg, dsg->dsg.getDefaultGraph(), "user1", s0);
    }

    @Test public void query_model_user2() {
        query_model_user(testdsg, dsg->dsg.getDefaultGraph(), "user2");
    }

    @Test public void query_model_ng_userNone() {
        query_model_user(testdsg, dsg->dsg.getGraph(g1), "userNone");
    }

    @Test public void query_model_ng_user11() {
        query_model_user(testdsg, dsg->dsg.getGraph(g1), "user1", s1);
    }

    @Test public void query_model_ng_user21() {
        query_model_user(testdsg, dsg->dsg.getGraph(g1), "user2", s1);
    }

    @Test public void query_model_ng_user12() {
        query_model_user(testdsg, dsg->dsg.getGraph(g2), "user1");
    }

    @Test public void query_model_ng_user22() {
        query_model_user(testdsg, dsg->dsg.getGraph(g2), "user2", s2);
    }

    @Test public void query_model_userXa() {
        query_model_user(testdsg, dsg->dsg.getDefaultGraph(), "userX");
    }

    @Test public void query_model_userXb() {
        query_model_user(testdsg, dsg->dsg.getGraph(g1), "userX");
    }

    private void query_model_user(DatasetGraph dsg, Function<DatasetGraph, Graph> graphChoice, String user, Node ... expected) {
        SecurityContext sCxt = reg.get(user);
        Set<Node> visible = subjects(dsg, graphChoice, queryDft, sCxt);
        assertSeen(visible, expected);
    }

    private static String dataStr = StrUtils.strjoinNL
        ("PREFIX : <http://test/>"
            ,""
            ,":s0 :p 0 ."
            ,":g1 { :s1 :p 1 }"
            ,":g2 { :s2 :p 2 }"
            ,":g3 { :s3 :p 3 }"
            ,":g4 { :s4 :p 4 }"
            );


    public static Node s0 = SSE.parseNode("<http://test/s0>");
    public static Node s1 = SSE.parseNode("<http://test/s1>");
    public static Node s2 = SSE.parseNode("<http://test/s2>");
    public static Node s3 = SSE.parseNode("<http://test/s3>");
    public static Node s4 = SSE.parseNode("<http://test/s4>");

    public static Node g1 = SSE.parseNode("<http://test/g1>");
    public static Node g2 = SSE.parseNode("<http://test/g2>");
    public static Node g3 = SSE.parseNode("<http://test/g3>");
    public static Node g4 = SSE.parseNode("<http://test/g4>");

    public static void addTestData(DatasetGraph dsg) {
        Txn.executeWrite(dsg, ()->{
            RDFParser.create().fromString(dataStr).lang(Lang.TRIG).parse(dsg);
        });
    }

    public static void assertSeen(Set<Node> visible, Node ... expected) {
        Set<Node> expectedNodes = new HashSet<>(Arrays.asList(expected));
        assertEquals(expectedNodes, visible);
    }
}
