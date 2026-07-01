/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.exec.*;
import org.apache.jena.sparql.resultset.ResultsCompare;

/** Path tests using queries. */
public class TestPathQuery {

    static String graphStr1 = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
            ,":s :p 123 ."
            ,":s :p 456 ."
            ,":s :q 'abc'."
            ,":s :q 'def'."

            ,":x :p :s ."
            ,":x :q :s ."
            );

    static Graph graph1 = RDFParser.fromString(graphStr1, Lang.TTL).toGraph();

    @Test public void testPathByQuery_unboundEnds_01() {
        // Query with path
        String qsPath     = "PREFIX : <http://example/> SELECT * { ?s :p|:q ?o }";
        // The same results in this case.
        String qsExpected = "PREFIX : <http://example/> SELECT * { { ?s :p ?o } UNION { ?s :q ?o } }";
        // Run both and compare.
        test(graph1, qsPath, qsExpected);
    }

    @Test public void testPathByQuery_unboundEnds_02() {
        String qsPath     = "PREFIX : <http://example/> SELECT * { ?s (^:p)|(^:q) ?o }";
        String qsExpected = "PREFIX : <http://example/> SELECT * { { ?s ^:p ?o } UNION { ?s ^:q ?o } }";
        test(graph1, qsPath, qsExpected);
    }

    @Test public void testPathByQuery_unboundEnds_03() {
        String qsPath     = "PREFIX : <http://example/> SELECT * { ?s ^(:p|:q) ?o }";
        String qsExpected = "PREFIX : <http://example/> SELECT * { { ?s ^:p ?o } UNION { ?s ^:q ?o } }";
        test(graph1, qsPath, qsExpected);
    }

    @Test public void testPathByQuery_unboundEnds_04() {
        String qsPath     = "PREFIX : <http://example/> SELECT * { ?s (:p/:q)+ ?o }";
        // Same results for the test data, not in general.
        String qsExpected = "PREFIX : <http://example/> SELECT ?s ?o { ?s :p [ :q ?o ] }";
        test(graph1, qsPath, qsExpected);
    }

    @Test public void testPathByQuery_unboundEnds_05() {
        String qsPath     = "PREFIX : <http://example/> SELECT ?s ?o { ?s :p{1,3} ?o }";
        // Same results for the test data, not in general.
        String qsExpected = "PREFIX : <http://example/> SELECT ?s ?o { { ?s :p ?o } UNION { ?s :p [:p ?o]} }";
        test(graph1, qsPath, qsExpected);
    }

    @Test public void testZeroPath_emptyGraph_01() {
        // ?v <p>{0} ?v with VALUES ?v {1} on empty graph. Should be 0 rows returned.
        String qs = "SELECT * WHERE { VALUES ?v { 1 } ?v <http://example.com/p>{0} ?v }";
        assertEquals(0, rowCount(GraphMemFactory.createDefaultGraph(), qs));
    }

    @Test public void testZeroPath_emptyGraph_02() {
        // ?v <p>? ?v with VALUES ?v {1} on empty graph. Should be be 0 rows returned.
        String qs = "SELECT * WHERE { VALUES ?v { 1 } ?v <http://example.com/p>? ?v }";
        assertEquals(0, rowCount(GraphMemFactory.createDefaultGraph(), qs));
    }

    @Test public void testZeroPath_emptyGraph_03() {
        // ?v <p>* ?v with VALUES ?v {1} on empty graph. Should be be 0 rows returned.
        String qs = "SELECT * WHERE { VALUES ?v { 1 } ?v <http://example.com/p>* ?v }";
        assertEquals(0, rowCount(GraphMemFactory.createDefaultGraph(), qs));
    }

    @Test public void testZeroPath_nodeInGraph_01() {
        String qs = "PREFIX : <http://example/> SELECT * WHERE { VALUES ?v { :s } ?v <http://example.com/p>? ?v }";
        assertEquals(1, rowCount(graph1, qs));
    }

    @Test public void testZeroPath_nodeInGraph_02() {
        String qs = "PREFIX : <http://example/> SELECT * WHERE { VALUES ?v { :s } ?v <http://example.com/p>* ?v }";
        assertEquals(1, rowCount(graph1, qs));
    }

    @Test public void testZeroPath_nodeInGraph_03() {
        String qs = "PREFIX : <http://example/> SELECT * WHERE { VALUES ?v { :s } ?v <http://example.com/p>{0} ?v }";
        assertEquals(1, rowCount(graph1, qs));
    }

    @Test public void testZeroPath_nodeAsObjectOnly_01() {
        // Node is present only as an object in the graph.
        // graph1 contains ":s :p 123", so 123 should appear only as an object.
        String qs = "SELECT * WHERE { VALUES ?v { 123 } ?v <http://example.com/p>? ?v }";
        assertEquals(1, rowCount(graph1, qs));
    }

    @Test public void testZeroPath_emptyGraph_04() {
        // {0,3} has min=0 so it is a zero-length path.
        String qs = "SELECT * WHERE { VALUES ?v { 1 } ?v <http://example.com/p>{0,3} ?v }";
        assertEquals(0, rowCount(GraphMemFactory.createDefaultGraph(), qs));
    }

    @Test public void testZeroPath_emptyGraph_05() {
        // p?|q has a zero-length branch.
        String qs = "SELECT * WHERE { VALUES ?v { 1 } ?v (<http://example.com/p>?|<http://example.com/q>) ?v }";
        assertEquals(0, rowCount(GraphMemFactory.createDefaultGraph(), qs));
    }

    @Test public void testNonZeroPath_selfLoop_returnsResult() {
        // A non-zero-length path that reaches s from s via a self-loop
        // must not be zeroed out by the zero-length fix.
        Graph g = GraphMemFactory.createDefaultGraph();
        g.add(Triple.create(
            NodeFactory.createURI("http://example/s"),
            NodeFactory.createURI("http://example/p"),
            NodeFactory.createURI("http://example/s")));
        String qs = "PREFIX : <http://example/> SELECT * WHERE { VALUES ?v { :s } ?v :p{1} ?v }";
        assertEquals(1, rowCount(g, qs));
    }

    private long rowCount(Graph graph, String queryString) {
        Query query = QueryFactory.create(queryString);
        RowSet rs = QueryExec.graph(graph).query(query).select();
        return RowSetOps.count(rs);
    }

    private void test(Graph graph12, String qsPath, String qsExpected) {
        RowSetRewindable rs1 = exec(graph1, qsPath);
        RowSetRewindable rs2 = exec(graph1, qsExpected);
        boolean same = rowSetEquals(rs1, rs2);

        // Debug. If the test is failing, leave some information around.
        if ( ! same ) {
            System.out.println(qsPath);
            print("Actual", rs1);
            print("Expected", rs2);
            rowSetEquals(rs1, rs2);
        }
        assertTrue(same);
    }

    private RowSetRewindable exec(Graph g, String qs) {
        Query query = QueryFactory.create(qs);
        RowSet rowset = QueryExec.graph(g).query(query).select();
        return RowSetMem.create(rowset);
    }

    private boolean rowSetEquals(RowSetRewindable rowset1, RowSetRewindable rowset2) {
        return ResultsCompare.equalsExact(rowset1, rowset2);
    }

    private void print(String label, RowSetRewindable rowset) {
        if ( label != null )
            System.out.println(label);
        rowset.reset();
        RowSetOps.out(rowset);
        rowset.reset();
    }
}
