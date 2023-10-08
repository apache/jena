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

package org.apache.jena.tdb1.solver;

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
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.RowSetRewindable;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb1.ConfigTest;
import org.apache.jena.tdb1.TDB1Factory;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSolverTDB {
    static String graphData = null;
    static Dataset dataset = null;
    static PrefixMapping pmap = null;

    @BeforeClass
    static public void beforeClass() {
        dataset = TDB1Factory.createDataset();
        dataset.begin(ReadWrite.WRITE);
        String graphData = ConfigTest.getTestingDataRoot() + "/Data/solver-data.ttl";
        RDFDataMgr.read(dataset, graphData);
        pmap = new PrefixMappingImpl();
        pmap.setNsPrefix("", "http://example/");
    }

    static private void addAll(Graph srcGraph, Graph dstGraph) {
        Iterator<Triple> triples = srcGraph.find(Node.ANY, Node.ANY, Node.ANY);
        triples.forEachRemaining(dstGraph::add);
    }

    @Test
    public void solve_01() {
        RowSet rs1 = exec("(bgp (:s :p :o))");
        RowSet rs2 = results("unit");
        equals(rs1, rs2);
    }

    @Test
    public void solve_02() {
        RowSet rs1 = exec("(bgp (:s :p :o2))");
        RowSet rs2 = results("empty");
        equals(rs1, rs2);
    }

    @Test
    public void solve_03() {
        // Above everything.
        RowSet rs1 = exec("(bgp (:zzzz :p 999999))");
        RowSet rs2 = results("empty");
        equals(rs1, rs2);
    }

    @Test
    public void solve_04() {
        // Below everything.
        RowSet rs1 = exec("(bgp (:a :p :a))");
        RowSet rs2 = results("empty");
        equals(rs1, rs2);
    }

    @Test
    public void solve_05() {
        RowSet rs1 = exec("(project (?s ?y) (bgp (?s :p ?z) (?z :q ?y)))");
        RowSet rs2 = results("(row (?s :s) (?y :y))");
        equals(rs1, rs2);
    }

    @Test
    public void solve_06() {
        RowSet rs1 = exec("(bgp (:s ?p ?o))");
        RowSet rs2 = results("(row (?p :p) (?o :o))", "(row (?p :p) (?o 10))", "(row (?p :p) (?o :x))");
        equals(rs1, rs2);
    }

    @Test
    public void solve_07() {
        // JENA-1428, JENA-1529
        String x = "(sequence  (table (vars ?X) (row [?X 'NotPresent']))  (bgp (triple :s :p ?o)))";
        RowSet rs1 = exec(x);
        assertTrue(rs1.hasNext());
        // Executing without stack trace is enough.
        rs1.materialize();
    }

    // ------

    private static void equals(RowSet rs1, RowSet rs2) {
//        same(rs1, rs2, true);
    }

    private static void same(RowSet rs1, RowSet rs2, boolean result) {
        RowSetRewindable rsw1 = rs1.rewindable();
        RowSetRewindable rsw2 = rs2.rewindable();

        boolean b = ResultSetCompare.equalsByValue(rsw1, rsw2);
        if ( b != result ) {
            System.out.println("Different: ");
            rsw1.reset();
            rsw2.reset();
            RowSetOps.out(rsw1);
            RowSetOps.out(rsw2);
            System.out.println();
        }
        assertTrue(b == result);
    }

    private static RowSet results(String...rows) {
        String str = "(table " + String.join("", rows) + ")";
        return SSE.parseTable(str).toRowSet();
    }

    /**
     * Execute in triples and quad forms. Check the algebra expression gets the same
     * results
     */
    private static RowSet exec(String pattern) {
        Op op1 = SSE.parseOp(pattern, pmap);
        List<Var> vars = new ArrayList<>();
        vars.addAll(OpVars.visibleVars(op1));

        Op op2 = Algebra.toQuadForm(op1);

        // Execute in triples and quad forms.
        QueryIterator qIter1 = Algebra.exec(op1, dataset.asDatasetGraph());
        RowSetRewindable rs1 = RowSet.create(qIter1, vars).rewindable();

        QueryIterator qIter2 = Algebra.exec(op2, dataset.asDatasetGraph());
        RowSetRewindable rs2 = RowSet.create(qIter2, vars).rewindable();;

        equals(rs1, rs2);
        rs1.reset();
        rs2.reset();
        return rs1;
    }

    private static List<Binding> toList(QueryIterator qIter) {
        List<Binding> x = new ArrayList<>();
        for ( ; qIter.hasNext() ; )
            x.add(qIter.nextBinding());
        return x;
    }
}
