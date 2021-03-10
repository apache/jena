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

package org.apache.jena.tdb2.solver;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.junit.TL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSolverTDB {
    static Dataset dataset = null;
    static PrefixMapping pmap = null;

    @BeforeClass
    static public void beforeClass() {
        dataset = TL.createTestDatasetMem();
        dataset.begin(ReadWrite.WRITE);
        String graphData = ConfigTest.getTestingDataRoot() + "/Data/solver-data.ttl";
        RDFDataMgr.read(dataset, graphData);
        pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("", "http://example/");
    }

    @AfterClass
    static public void afterClass() {
        dataset.abort();
        TL.expel(dataset);
    }

    static private void addAll(Graph srcGraph, Graph dstGraph) {
        Iterator<Triple> triples = srcGraph.find(Node.ANY, Node.ANY, Node.ANY);
        triples.forEachRemaining(dstGraph::add);
    }

    @Test
    public void solve_01() {
        ResultSet rs1 = exec("(bgp (:s :p :o))");
        ResultSet rs2 = results("unit");
        equals(rs1, rs2);
    }

    @Test
    public void solve_02() {
        ResultSet rs1 = exec("(bgp (:s :p :o2))");
        ResultSet rs2 = results("empty");
        equals(rs1, rs2);
    }

    @Test
    public void solve_03() {
        // Above everything.
        ResultSet rs1 = exec("(bgp (:zzzz :p 999999))");
        ResultSet rs2 = results("empty");
        equals(rs1, rs2);
    }

    @Test
    public void solve_04() {
        // Below everything.
        ResultSet rs1 = exec("(bgp (:a :p :a))");
        ResultSet rs2 = results("empty");
        equals(rs1, rs2);
    }

    @Test
    public void solve_05() {
        ResultSet rs1 = exec("(project (?s ?y) (bgp (?s :p ?z) (?z :q ?y)))");
        ResultSet rs2 = results("(row (?s :s) (?y :y))");
        equals(rs1, rs2);
    }

    @Test
    public void solve_06() {
        ResultSet rs1 = exec("(bgp (:s ?p ?o))");
        ResultSet rs2 = results("(row (?p :p) (?o :o))", "(row (?p :p) (?o 10))", "(row (?p :p) (?o :x))");
        equals(rs1, rs2);
    }

    @Test
    public void solve_07() {
        // JENA-1428, JENA-1529
        String x = "(sequence  (table (vars ?X) (row [?X 'NotPresent']))  (bgp (triple :s :p ?o)))";
        ResultSet rs1 = exec(x);
        assertTrue(rs1.hasNext());
        // Executing without stack trace is enough.
        ResultSetFormatter.consume(rs1);
    }

    // ------

    private static void equals(ResultSet rs1, ResultSet rs2) {
        same(rs1, rs2, true);
    }

    private static void same(ResultSet rs1, ResultSet rs2, boolean result) {
        ResultSetRewindable rsw1 = ResultSetFactory.makeRewindable(rs1);
        ResultSetRewindable rsw2 = ResultSetFactory.makeRewindable(rs2);
        boolean b = ResultSetCompare.equalsByValue(rsw1, rsw2);
        if ( b != result ) {
            System.out.println("Different: ");
            rsw1.reset();
            rsw2.reset();
            ResultSetFormatter.out(rsw1);
            ResultSetFormatter.out(rsw2);
            System.out.println();
        }

        assertTrue(b == result);
    }

    private static ResultSet results(String...rows) {
        String str = "(table " + String.join("", rows) + ")";
        return SSE.parseTable(str).toResultSet();
    }

    private static ResultSet exec(String pattern) {
        Op op = SSE.parseOp(pattern, pmap);
        List<Var> vars = new ArrayList<>();
        vars.addAll(OpVars.visibleVars(op));
        // op = Algebra.toQuadForm(op);
        QueryIterator qIter = Algebra.exec(op, dataset.asDatasetGraph());
        // Will go via the StageGeneratorDirectTDB for TDB2
        // QueryIterator qIter = Algebra.exec(op,
        // dataset.asDatasetGraph().getDefaultGraph());
        return ResultSetFactory.create(qIter, Var.varNames(vars));
    }

    private static List<Binding> toList(QueryIterator qIter) {
        List<Binding> x = new ArrayList<>();
        for ( ; qIter.hasNext() ; )
            x.add(qIter.nextBinding());
        return x;
    }
}
