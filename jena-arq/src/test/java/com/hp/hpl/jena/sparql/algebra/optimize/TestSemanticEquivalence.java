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

package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.ArrayList ;
import java.util.List ;

import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.resultset.TextOutput ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.Symbol ;

/**
 * Tests for verifying that a query returns the same results both with and
 * without a given optimizer enabled
 * 
 */
public class TestSemanticEquivalence {

    private static Dataset implJoin;

    @BeforeClass
    public static void setup() {
        // Dataset for implicit join tests
        implJoin = DatasetFactory.createMem();

        Node a = NodeFactory.createURI("http://a");
        Node b = NodeFactory.createURI("http://b");
        Node c = NodeFactory.createURI("http://c");
        Node p1 = NodeFactory.createURI("http://p1");
        Node p2 = NodeFactory.createURI("http://p2");
        Node pSelf = NodeFactory.createURI("http://self");
        Node o = NodeFactory.createLiteral("object");

        DatasetGraph dsg = implJoin.asDatasetGraph();
        dsg.add(Quad.defaultGraphNodeGenerated, a, p1, o);
        dsg.add(Quad.defaultGraphNodeGenerated, a, p2, o);
        dsg.add(Quad.defaultGraphNodeGenerated, b, p1, o);
        dsg.add(Quad.defaultGraphNodeGenerated, b, p2, o);
        dsg.add(Quad.defaultGraphNodeGenerated, c, p1, o);
        //dsg.add(Quad.defaultGraphNodeGenerated, a, pSelf, a);
        
        // Currently these optimizations are off by default
        Assert.assertFalse(ARQ.isFalse(ARQ.optFilterImplicitJoin));
        Assert.assertFalse(ARQ.isFalse(ARQ.optImplicitLeftJoin));
    }

    @AfterClass
    public static void teardown() {
        if (implJoin != null) {
            implJoin.close();
            implJoin = null;
        }
        
        // Currently these optimizations are off by default
        Assert.assertFalse(ARQ.isFalse(ARQ.optFilterImplicitJoin));
        Assert.assertFalse(ARQ.isFalse(ARQ.optImplicitLeftJoin));
    }

    @Test
    public void implicitJoinEvaluation1() {
        String query = "SELECT * WHERE { ?x <http://p1> ?o1 . ?y <http://p2> ?o2 . FILTER(?x = ?y) }";
        test(query, implJoin, ARQ.optFilterImplicitJoin, 2);

        String alg1 = "(filter (= ?x ?y) (bgp (?x <http://p1> ?o1)(?y <http://p2> ?o2)))";
        testAsAlgebra(alg1, implJoin, ARQ.optFilterImplicitJoin, 2);

        String alg2 = "(filter (= ?y ?x) (bgp (?x <http://p1> ?o1)(?y <http://p2> ?o2)))";
        testAsAlgebra(alg2, implJoin, ARQ.optFilterImplicitJoin, 2);
    }

    @Test
    public void implicitJoinEvaluation2() {
        String query = "SELECT * WHERE { ?x <http://p1> ?o1 . ?y <http://noSuchPredicate> ?o2 . FILTER(?x = ?y) }";
        test(query, implJoin, ARQ.optFilterImplicitJoin, 0);

        String alg1 = "(filter (= ?x ?y) (bgp (?x <http://p1> ?o1)(?y <http://noSuchPredicate> ?o2)))";
        testAsAlgebra(alg1, implJoin, ARQ.optFilterImplicitJoin, 0);

        String alg2 = "(filter (= ?y ?x) (bgp (?x <http://p1> ?o1)(?y <http://noSuchPredicate> ?o2)))";
        testAsAlgebra(alg1, implJoin, ARQ.optFilterImplicitJoin, 0);
    }

    @Test
    public void implicitJoinEvaluation3() {
        String query = "SELECT * WHERE { ?x <http://p1> ?o1 . FILTER(?x = ?y) }";
        test(query, implJoin, ARQ.optFilterImplicitJoin, 0);

        String alg1 = "(filter (= ?x ?y) (bgp (?x <http://p1> ?o1)))";
        testAsAlgebra(alg1, implJoin, ARQ.optFilterImplicitJoin, 0);

        String alg2 = "(filter (= ?y ?x) (bgp (?x <http://p1> ?o1)))";
        testAsAlgebra(alg1, implJoin, ARQ.optFilterImplicitJoin, 0);
    }

    @Test
    public void implicitLeftJoinEvaluation1() {
        String query = "SELECT * WHERE { ?x <http://p1> ?o1 . OPTIONAL { ?y <http://p2> ?o2 . FILTER(?x = ?y) } }";
        test(query, implJoin, ARQ.optImplicitLeftJoin, 3);

        String alg1 = "(leftjoin (bgp (?x <http://p1> ?o1)) (bgp (?y <http://p2> ?o2)) (= ?x ?y))";
        testAsAlgebra(alg1, implJoin, ARQ.optImplicitLeftJoin, 3);

        String alg2 = "(leftjoin (bgp (?x <http://p1> ?o1)) (bgp (?y <http://p2> ?o2)) (= ?y ?x))";
        testAsAlgebra(alg2, implJoin, ARQ.optImplicitLeftJoin, 3);
    }

    @Test
    public void implicitLeftJoinEvaluation2() {
        String query = "SELECT * WHERE { ?x <http://p1> ?o1 . OPTIONAL { ?y <http://p2> ?o2 . FILTER(?x = ?y && ?o1 >= ?o2) } }";
        test(query, implJoin, ARQ.optImplicitLeftJoin, 3);

        String alg1 = "(leftjoin (bgp (?x <http://p1> ?o1)) (bgp (?y <http://p2> ?o2)) (&& (= ?x ?y)(> ?o1 ?o2)))";
        testAsAlgebra(alg1, implJoin, ARQ.optImplicitLeftJoin, 3);

        String alg2 = "(leftjoin (bgp (?x <http://p1> ?o1)) (bgp (?y <http://p2> ?o2)) (&& (= ?y ?x)(> ?o1 ?o2)))";
        testAsAlgebra(alg2, implJoin, ARQ.optImplicitLeftJoin, 3);
    }
    
    @Test
    public void implicitLeftJoinEvaluation3() {
        String query = "SELECT * WHERE { ?x ?p ?o . OPTIONAL { ?y ?p1 ?o1 . ?y ?p2 ?z . FILTER(?x = ?y) FILTER(?x = ?z) FILTER(?y = ?z) } }";
        test(query, implJoin, ARQ.optImplicitLeftJoin, 5);
        
        String alg1 = "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((= ?x ?y) (= ?x ?z) (= ?y ?z)))";
        testAsAlgebra(alg1, implJoin, ARQ.optImplicitLeftJoin, 5);
    }
    
    /**
     * Tests whether a query gives the same results when run both with and
     * without a given optimizer
     * 
     * @param queryStr
     *            Query
     * @param ds
     *            Dataset
     * @param opt
     *            Optimizer
     * @param expected
     *            Expected number of results
     */
    public static void test(String queryStr, Dataset ds, Symbol opt, int expected) {
        Query q = QueryFactory.create(queryStr);

        if (!q.isSelectType())
            Assert.fail("Only SELECT queries are testable with this method");
        
        Op op = Algebra.compile(q);
        // Track current state
        boolean isEnabled = ARQ.isTrue(opt);
        boolean isDisabled = ARQ.isFalse(opt);

        try {
            // Run first without optimization
            ARQ.set(opt, false);
            ResultSetRewindable rs ;
            try(QueryExecution qe = QueryExecutionFactory.create(q, ds)) {
                rs = ResultSetFactory.makeRewindable(qe.execSelect());
                if (expected != rs.size()) {
                    System.err.println("Non-optimized results not as expected");
                    TextOutput output = new TextOutput((SerializationContext)null);
                    output.format(System.out, rs);
                    rs.reset();
                }
                Assert.assertEquals(expected, rs.size());
            }

            // Run with optimization
            ARQ.set(opt, true);
            ResultSetRewindable rsOpt ;
            try(QueryExecution qeOpt = QueryExecutionFactory.create(q, ds)) {
                    rsOpt = ResultSetFactory.makeRewindable(qeOpt.execSelect());
                if (expected != rsOpt.size()) {
                    System.err.println("Optimized results not as expected");
                    TextOutput output = new TextOutput((SerializationContext)null);
                    output.format(System.out, rsOpt);
                    rsOpt.reset();
                }
                Assert.assertEquals(expected, rsOpt.size());
            }
            Assert.assertTrue(ResultSetCompare.isomorphic(rs, rsOpt));
        } finally {
            // Restore previous state
            if (isEnabled) {
                ARQ.set(opt, true);
            } else if (isDisabled) {
                ARQ.set(opt, false);
            } else {
                ARQ.unset(opt);
            }
        }
    }

    /**
     * Tests whether an algebra expression gives the same results when run both
     * with and without a given optimizer
     * 
     * @param algStr
     *            Algebra
     * @param ds
     *            Dataset
     * @param opt
     *            Optimizer
     * @param expected
     *            Expected number of results
     */
    public static void testAsAlgebra(String algStr, Dataset ds, Symbol opt, int expected) {
        Op op = SSE.parseOp(algStr);
        List<String> vars = new ArrayList<>();
        for (Var v : OpVars.visibleVars(op)) {
            vars.add(v.getName());
        }

        // Track current state
        boolean isEnabled = ARQ.isTrue(opt);
        boolean isDisabled = ARQ.isFalse(opt);

        try {
            // Run first without optimization
            ARQ.set(opt, false);
            QueryEngineMain engine = new QueryEngineMain(op, ds.asDatasetGraph(), BindingFactory.binding(), ARQ.getContext());
            QueryIterator iter = engine.eval(op, ds.asDatasetGraph(), BindingFactory.binding(), ARQ.getContext());
            ResultSetRewindable rs = ResultSetFactory.makeRewindable(new ResultSetStream(vars, ModelFactory.createDefaultModel(),
                    iter));
            if (expected != rs.size()) {
                System.err.println("Non-optimized results not as expected");
                TextOutput output = new TextOutput((SerializationContext)null);
                output.format(System.out, rs);
                rs.reset();
            }
            Assert.assertEquals(expected, rs.size());
            iter.close();

            // Run with optimization
            ARQ.set(opt, true);
            engine = new QueryEngineMain(op, ds.asDatasetGraph(), BindingFactory.binding(), ARQ.getContext());
            QueryIterator iterOpt = engine.eval(op, ds.asDatasetGraph(), BindingFactory.binding(), ARQ.getContext());
            ResultSetRewindable rsOpt = ResultSetFactory.makeRewindable(new ResultSetStream(vars, ModelFactory
                    .createDefaultModel(), iterOpt));
            if (expected != rsOpt.size()) {
                System.err.println("Optimized results not as expected");
                TextOutput output = new TextOutput((SerializationContext)null);
                output.format(System.out, rsOpt);
                rsOpt.reset();
            }
            Assert.assertEquals(expected, rsOpt.size());
            iterOpt.close();

            Assert.assertTrue(ResultSetCompare.isomorphic(rs, rsOpt));
        } finally {
            // Restore previous state
            if (isEnabled) {
                ARQ.set(opt, true);
            } else if (isDisabled) {
                ARQ.set(opt, false);
            } else {
                ARQ.unset(opt);
            }
        }
    }
}
