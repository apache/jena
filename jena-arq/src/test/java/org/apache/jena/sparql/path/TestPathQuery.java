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

package org.apache.jena.sparql.path;

import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.exec.*;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.junit.Test;

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

    static Graph graph1 = RDFParser.fromString(graphStr1).lang(Lang.TTL).toGraph();

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
        String qsPath     = "PREFIX : <http://example/> SELECT * { ?s :p{1,3} ?o }";
        // Same results for the test data, not in general.
        String qsExpected = "PREFIX : <http://example/> SELECT ?s ?o { { ?s :p ?o } UNION { ?s :p [:p ?o]} }";
        test(graph1, qsPath, qsExpected);
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
        }
        assertTrue(same);
    }

    private RowSetRewindable exec(Graph g, String qs) {
        Query query = QueryFactory.create(qs);
        RowSet rowset = QueryExec.graph(g).query(query).select();
        return RowSetMem.create(rowset);
    }

    private boolean rowSetEquals(RowSetRewindable rowset1, RowSetRewindable rowset2) {
        return ResultSetCompare.equalsByTerm(rowset1, rowset2);
    }

    private void print(String label, RowSetRewindable rowset) {
        if ( label != null )
            System.out.println(label);
        rowset.reset();
        RowSetOps.out(rowset);
        rowset.reset();
    }
}
