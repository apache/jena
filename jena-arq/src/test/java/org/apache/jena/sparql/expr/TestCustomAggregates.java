/**
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

package org.apache.jena.sparql.expr;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.aggregate.* ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestCustomAggregates extends BaseTest {
    
    public static final String aggIRI = "http://example.test/agg" ;
    public static final String aggIRI2 = "http://example.test/aggUnRegistered" ;
    
    static AccumulatorFactory myAccumulatorFactory = new AccumulatorFactory() {
        @Override
        public Accumulator createAccumulator(AggCustom agg, boolean distinct) { return new MyAccumulator(agg, distinct) ; }
    } ;
    
    static class MyAccumulator implements Accumulator {
        int count = 0 ;
        private AggCustom agg ;
        MyAccumulator(AggCustom agg, boolean ignored) { this.agg = agg ; }

        @Override
        public void accumulate(Binding binding, FunctionEnv functionEnv) {
            ExprList exprList = agg.getExprList() ;
            for(Expr expr: exprList) {
                try {
                    NodeValue nv = expr.eval(binding, functionEnv) ;
                    // Evaluation succeeded.
                    if ( nv.isLiteral())
                        count ++ ;
                } catch (ExprEvalException ex) {}
            }
        }

        @Override
        public NodeValue getValue() {
            return NodeValue.makeInteger(count) ;
        }}


    @BeforeClass public static void setup() { 
        AggregateRegistry.register(aggIRI, myAccumulatorFactory, NodeConst.nodeMinusOne);
    }

    @AfterClass public static void clearup() { 
        AggregateRegistry.unregister(aggIRI);
    }

    @Test public void customAgg_1() {
        assertTrue(AggregateRegistry.isRegistered(aggIRI)) ;
    }
    
    @Test public void customAgg_2() {
        assertFalse(AggregateRegistry.isRegistered(aggIRI2)) ;
    }
    
    @Test public void customAgg_10() {
        String qs = "SELECT (AGG <"+aggIRI+">(?o) AS ?x) {?s ?p ?o } GROUP BY ?s" ;
        Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        String qs2 = q.serialize(Syntax.syntaxARQ) ;
        Query q2 = QueryFactory.create(qs2, Syntax.syntaxARQ) ;
        assertEquals(q, q2) ;
    }

    @Test public void customAgg_11() {
        String qs = "SELECT (<"+aggIRI+">(?o) AS ?x) {?s ?p ?o } GROUP BY ?s" ;
        Query q = QueryFactory.create(qs) ;
        String qs2 = q.serialize() ;
        Query q2 = QueryFactory.create(qs2) ;
        assertEquals(q, q2) ;
    }


    @Test public void customAgg_12() {
        LogCtl.setError(AggregatorFactory.class);
        try {
            String qs = "SELECT (AGG <"+aggIRI2+">(?o) AS ?x) {?s ?p ?o } GROUP BY ?s" ;
            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
            String qs2 = q.serialize(Syntax.syntaxARQ) ;
            Query q2 = QueryFactory.create(qs2, Syntax.syntaxARQ) ;
            assertEquals(q, q2) ;
        } finally {
            LogCtl.setInfo(AggregatorFactory.class);
        }
    }

    @Test public void customAgg_20() {
        Graph g = SSE.parseGraph("(graph (:s :p :o) (:s :p 1))") ;
        Model m = ModelFactory.createModelForGraph(g) ;
        String qs = "SELECT (<"+aggIRI+">(?o) AS ?x) {?s ?p ?o } GROUP BY ?s" ;
        Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        try (QueryExecution qExec = QueryExecutionFactory.create(q, m) ) {
            ResultSet rs = qExec.execSelect() ;
            QuerySolution soln = rs.nextSolution() ;
            assertFalse(rs.hasNext());
            int v = soln.getLiteral("x").getInt() ;
            assertEquals(1, v) ;
        }
    }

    @Test public void customAgg_21() {
        // No GROUP BY, no match => default value
        Graph g = SSE.parseGraph("(graph (:s :p :o) (:s :p 1))") ;
        Model m = ModelFactory.createModelForGraph(g) ;
        String qs = "SELECT (<"+aggIRI+">(?o) AS ?x) {?s ?p ?o FILTER (false) }" ;
        Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        try (QueryExecution qExec = QueryExecutionFactory.create(q, m) ) {
            ResultSet rs = qExec.execSelect() ;
            QuerySolution soln = rs.nextSolution() ;
            assertFalse(rs.hasNext());
            int v = soln.getLiteral("x").getInt() ;
            assertEquals(-1, v) ;
        }
    }
    
    @Test public void customAgg_22() {
        // GROUP BY, no match +. no rows.
        Graph g = SSE.parseGraph("(graph (:s :p :o) (:s :p 1))") ;
        Model m = ModelFactory.createModelForGraph(g) ;
        String qs = "SELECT (<"+aggIRI+">(?o) AS ?x) {?s ?p ?o FILTER (false) } GROUP BY ?s" ;
        
        Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        try (QueryExecution qExec = QueryExecutionFactory.create(q, m) ) {
            ResultSet rs = qExec.execSelect() ;
            assertFalse(rs.hasNext());
        }
    }
    
    @Test public void customAgg_23() {
        String qs = "SELECT (<"+aggIRI+">(?o) AS ?x) {?s ?p ?o }" ;
        Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op op = Algebra.compile(q) ;
        String x = StrUtils.strjoinNL
            ("(project (?x)"
            ,"   (extend ((?x ?.0))"
            ,"       (group () ((?.0 (agg <http://example.test/agg> ?o)))"
            ,"         (bgp (triple ?s ?p ?o)))))"
             ) ;
        Op op2 = SSE.parseOp(x) ;
        assertEquals(op2, op); 
    }

}

