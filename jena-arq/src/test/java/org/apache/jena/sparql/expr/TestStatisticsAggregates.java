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

package org.apache.jena.sparql.expr;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.* ;
import static org.apache.jena.query.Syntax.* ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry ;
import org.apache.jena.sparql.expr.aggregate.lib.StandardAggregates ;
import org.apache.jena.sparql.sse.SSE ;
import static org.junit.Assert.* ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestStatisticsAggregates {
    
    static String NL = "\n" ;
    static String PRE = StrUtils.strjoinNL
        ("PREFIX agg:     <http://jena.apache.org/ARQ/function/aggregate#>") ;
    
    static DatasetGraph ds      = SSE.parseDatasetGraph("(dataset (graph (:x :p1 -1) (:x :p2 2) (:x :p3 3) (:x :p4 3) ))") ;
    static DatasetGraph dsEmpty = SSE.parseDatasetGraph("(dataset)") ;
    static DatasetGraph ds1 = SSE.parseDatasetGraph("(dataset (graph (:x :p -1)) )") ;
    
    @BeforeClass public static void setupClass() { 
        StandardAggregates.register();
    }
    
    static double VALUEstdev_samp   = 1.8929694486000912 ;
    static double VALUEstdev_sampd  = 2.0816659994661326 ;

    static double VALUEstdev_pop    = 1.6393596310755 ;
    static double VALUEstdev_popd   = 1.699673171197595 ;

    static double VALUEvar_samp     = 3.5833333333333333 ;
    static double VALUEvar_sampd    = 4.333333333333333 ;

    static double VALUEvar_pop      = 2.6875 ;
    static double VALUEvar_popd     = 2.888888888888889 ;

    // For each aggregate function, there are the following tests. 
    //    The aggregate <uri>() / SPARQL 1.1
    //    The DISTINCT aggregate / SPARQL 1.1
    //    ARQ keyword
    //    ARQ DISTINCT keyword
    //    AGG <uri>() / ARQ
    //    AGG DISTINCT <uri>() / ARQ 

    // ---- stdev
    
    @Test public void agg_stat_stdev_uri() {
        test("agg:stdev(?x)", VALUEstdev_samp, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_stdev_uri_distinct() {
        test("agg:stdev(DISTINCT ?x)", VALUEstdev_sampd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_stdev_kw() {
        test("STDEV(?x)", VALUEstdev_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdev_kw_distinct() {
        test("STDEV(DISTINCT ?x)", VALUEstdev_sampd, syntaxARQ) ;
    }
    
    @Test public void agg_stat_stdev_agg() {
        test("AGG agg:stdev(?x)", VALUEstdev_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdev_agg_distinct() {
        test("AGG agg:stdev(DISTINCT ?x)", VALUEstdev_sampd, syntaxARQ) ;
    }
    
    // ---- stdev_samp
    
    @Test public void agg_stat_stdev_samp_uri() {
        test("agg:stdev_samp(?x)", VALUEstdev_samp, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_stdev_samp_uri_distinct() {
        test("agg:stdev_samp(DISTINCT ?x)", VALUEstdev_sampd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_stdev_samp_kw() {
        test("STDEV_SAMP(?x)", VALUEstdev_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdev_samp_kw_distinct() {
        test("STDEV_SAMP(DISTINCT ?x)", VALUEstdev_sampd, syntaxARQ) ;
    }

    @Test public void agg_stat_stdev_samp_agg() {
        test("AGG agg:stdev_samp(?x)", VALUEstdev_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdev_samp_agg_distinct() {
        test("AGG agg:stdev_samp(DISTINCT ?x)", VALUEstdev_sampd, syntaxARQ) ;
    }

    // ---- stdev_pop
    
    @Test public void agg_stat_stdevp_uri() {
        test("agg:stdev_pop(?x)", VALUEstdev_pop, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_stdev_pop_uri_distinct() {
        test("agg:stdev_pop(DISTINCT ?x)", VALUEstdev_popd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_stdev_pop_kw() {
        test("STDEV_POP(?x)", VALUEstdev_pop, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdev_pop_kw_distinct() {
        test("STDEV_POP(DISTINCT ?x)", VALUEstdev_popd, syntaxARQ) ;
    }
    
    @Test public void agg_stat_stdev_pop_agg() {
        test("AGG agg:stdev_pop(?x)", VALUEstdev_pop, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdev_pop_agg_distinct() {
        test("AGG agg:stdev_pop(DISTINCT ?x)", VALUEstdev_popd, syntaxARQ) ;
    }
    
    // ---- variance
    
    @Test public void agg_stat_var_uri() {
        test("agg:variance(?x)", VALUEvar_samp, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_var_uri_distinct() {
        test("agg:variance(DISTINCT ?x)", VALUEvar_sampd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_var_kw() {
        test("VARIANCE(?x)", VALUEvar_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_var_kw_distinct() {
        test("VARIANCE(DISTINCT ?x)", VALUEvar_sampd, syntaxARQ) ;
    }
    
    @Test public void agg_stat_var_agg() {
        test("AGG agg:variance(?x)", VALUEvar_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_var_agg_distinct() {
        test("AGG agg:variance(DISTINCT ?x)", VALUEvar_sampd, syntaxARQ) ;
    }
    
    // ---- var_samp
    
    @Test public void agg_stat_var_samp_uri() {
        test("agg:var_samp(?x)", VALUEvar_samp, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_var_samp_uri_distinct() {
        test("agg:var_samp(DISTINCT ?x)", VALUEvar_sampd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_var_samp_kw() {
        test("VAR_SAMP(?x)", VALUEvar_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_var_samp_kw_distinct() {
        test("VAR_SAMP(DISTINCT ?x)", VALUEvar_sampd, syntaxARQ) ;
    }
    
    @Test public void agg_stat_var_samp_agg() {
        test("AGG agg:var_samp(?x)", VALUEvar_samp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_var_samp_agg_distinct() {
        test("AGG agg:var_samp(DISTINCT ?x)", VALUEvar_sampd, syntaxARQ) ;
    }
    
    // ---- var_pop
    
    @Test public void agg_stat_var_pop_uri() {
        test("agg:var_pop(?x)", VALUEvar_pop, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_var_pop_uri_distinct() {
        test("agg:var_pop(DISTINCT ?x)", VALUEvar_popd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_var_pop_kw() {
        test("VAR_POP(?x)", VALUEvar_pop, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_var_pop_kw_distinct() {
        test("VAR_POP(DISTINCT ?x)", VALUEvar_popd, syntaxARQ) ;
    }
    
    @Test public void agg_stat_var_pop_agg() {
        test("AGG agg:var_pop(?x)", VALUEvar_pop, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_var_pop_agg_distinct() {
        test("AGG agg:var_pop(DISTINCT ?x)", VALUEvar_popd, syntaxARQ) ;
    }
    
    //<http://jena.apache.org/ARQ/function/aggregate#>
    
    @Test public void agg_stat_stdev_full_uri() {
        test("<http://jena.apache.org/ARQ/function/aggregate#stdev>(?x)", VALUEstdev_samp, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_registry() {
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#stdev")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#stdev_samp")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#stdev_pop")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#variance")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#var_samp")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#var_pop")) ;
        
        assertFalse(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function#e")) ;  // Not an aggregate
    }
    
    // ---- Corner cases. 0 and 1 matches.
    
    // Empty -> error 
    @Test public void agg_stat_stdev_empty() {
        testEmpty("agg:stdev(?x)", dsEmpty, syntaxSPARQL_11) ; 
    }
    
    // Empty -> error 
    @Test public void agg_stat_stdev_samp_empty() {
        testEmpty("agg:stdev_samp(?x)", dsEmpty, syntaxSPARQL_11) ; 
    }
    
    // Empty -> error 
    @Test public void agg_stat_stdev_pop_empty() {
        testEmpty("agg:stdev_pop(?x)", dsEmpty, syntaxSPARQL_11) ; 
    }
    
    // Sample of one -> error
    @Test public void agg_stat_stdev_size_one() {
        testErr("agg:stdev(?x)", ds1, syntaxSPARQL_11) ; 
    }
    
    // Sample of one -> error
    @Test public void agg_stat_stdev_samp_size_one() {
        testErr("agg:stdev_samp(?x)", ds1, syntaxSPARQL_11) ; 
    }

    // Sample of one -> error
    @Test public void agg_stat_var_size_one_1() {
        testErr("agg:var_samp(?x)", ds1, syntaxSPARQL_11) ; 
    }

    // Population of one -> 0e0
    @Test public void agg_stat_stdev_pop_size_one() {
        Query query = buildGroupBy("agg:stdev_pop(?x)", syntaxSPARQL_11) ; 
        test(query, 0e0, ds1) ; 
    }
    
    // Population of one -> 0e0
    @Test public void agg_stat_var_pop_size_one() {
        Query query = buildGroupBy("agg:var_pop(?x)", syntaxSPARQL_11) ; 
        test(query, 0e0, ds1) ; 
    }
    
    // By keyword
    
    private static void test(String qsAgg, double expected, Syntax syntax) {
        test(qsAgg, expected, syntax, ds) ;
    }

    private static void test(String qsAgg, double expected, Syntax syntax,  DatasetGraph dsg) {
        Query query = buildGroupBy(qsAgg, syntax) ; 
        test(query, expected, dsg) ;
    }

    private static void test(Query query, double expected, DatasetGraph dsg) {
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(dsg)) ) {
            Literal literal = qExec.execSelect().next().getLiteral("X") ;
            double result = literal.getDouble() ;
            assertEquals(expected, result, 0.001);
        }
    }

    private static Query buildGroupBy(String qsAgg, Syntax syntax) {
        String NL = "\n" ;
        String qs = PRE+NL+"SELECT ("+qsAgg+NL+"AS ?X) WHERE {?s ?p ?x} GROUP BY ?s" ;
        Query query = QueryFactory.create(qs, syntax) ;
        return query ;
    }
    
    private static Query buildNoGroupBy(String qsAgg, Syntax syntax) {
        String NL = "\n" ;
        String qs = PRE+NL+"SELECT ("+qsAgg+NL+"AS ?X) WHERE {?s ?p ?x}" ;
        Query query = QueryFactory.create(qs, syntax) ;
        return query ;
    }
    
    // Error in calculation (e.g. 2+ needed)
    private void testErr(String qsAgg, DatasetGraph ds, Syntax syntax) {
        Query query = buildGroupBy(qsAgg, syntax) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(ds)) ) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue(rs.getResultVars().contains("X")) ;
            Binding b = rs.nextBinding() ;
            assertFalse(b.contains(Var.alloc("X"))) ;
        }
    }
    
    // Behaviour on empty
    private void testEmpty(String qsAgg, DatasetGraph ds, Syntax syntax) {
        testEmptyNoGroupBy(qsAgg, ds, syntax);
        testEmptyGroupBy(qsAgg, ds, syntax);
    }
    
    // Behaviour on empty - aggregate and no GROUP BY
    private void testEmptyNoGroupBy(String qsAgg, DatasetGraph ds, Syntax syntax) {
        Query query = buildNoGroupBy(qsAgg, syntax) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(ds)) ) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue(rs.hasNext()) ;
            assertTrue(rs.getResultVars().contains("X")) ;
            Binding b = rs.nextBinding() ;
            assertFalse(b.contains(Var.alloc("X"))) ;
        }
    }
    
    // Behaviour on empty - GROUP BY present
    private void testEmptyGroupBy(String qsAgg, DatasetGraph ds, Syntax syntax) {
        Query query = buildGroupBy(qsAgg, syntax) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(ds)) ) {
            ResultSet rs = qExec.execSelect() ;
            assertFalse(rs.hasNext()) ;
        }
    }
}
