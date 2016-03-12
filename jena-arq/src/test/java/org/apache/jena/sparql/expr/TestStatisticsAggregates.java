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
import org.apache.jena.sparql.expr.aggregate.lib.StandardCustomAggregates ;
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
        StandardCustomAggregates.register();
    }
    
    static double VALUEstdev    = 1.8929694486000912 ;
    static double VALUEstdevd   = 2.0816659994661326 ;

    static double VALUEstdevp    = 1.6393596310755 ;
    static double VALUEstdevpd   = 1.699673171197595 ;

    static double VALUEvar    = 3.5833333333333333 ;
    static double VALUEvard   = 4.333333333333333 ;

    static double VALUEvarp    = 2.6875 ;
    static double VALUEvarpd   = 2.888888888888889 ;

    // For each aggregate function, there are the following tests. 
    // _uri             The aggregate <uri>() / SPARQL 1.1
    // _uri_d           The distinct aggregate <uri_distinct>() by URI / SPARQL 1.1
    // _uri_distinct    The DISTINCT aggregate / SPARQL 1.1
    // _agg             AGG <uri>() / ARQ
    // _agg_distinct    AGG DISTINCT <uri>() / ARQ 

    // ---- stdev
    
    @Test public void agg_stat_stdev_uri() {
        test("agg:stdev(?x)", VALUEstdev, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_stdev_uri_d() {
        test("agg:stdevd(?x)", VALUEstdevd, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_stdev_uri_distinct() {
        test("agg:stdev(DISTINCT ?x)", VALUEstdevd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_stdev_agg() {
        test("AGG agg:stdev(?x)", VALUEstdev, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdev_agg_distinct() {
        test("AGG agg:stdev(DISTINCT ?x)", VALUEstdevd, syntaxARQ) ;
    }
    
    // ---- stdevp
    
    @Test public void agg_stat_stdevp_uri() {
        test("agg:stdevp(?x)", VALUEstdevp, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_stdevp_uri_d() {
        test("agg:stdevpd(?x)", VALUEstdevpd, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_stdevp_uri_distinct() {
        test("agg:stdevp(DISTINCT ?x)", VALUEstdevpd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_stdevp_agg() {
        test("AGG agg:stdevp(?x)", VALUEstdevp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_stdevp_agg_distinct() {
        test("AGG agg:stdevp(DISTINCT ?x)", VALUEstdevpd, syntaxARQ) ;
    }
    
    // ---- var
    
    @Test public void agg_stat_var_uri() {
        test("agg:var(?x)", VALUEvar, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_var_uri_d() {
        test("agg:vard(?x)", VALUEvard, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_var_uri_distinct() {
        test("agg:var(DISTINCT ?x)", VALUEvard, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_var_agg() {
        test("AGG agg:var(?x)", VALUEvar, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_var_agg_distinct() {
        test("AGG agg:var(DISTINCT ?x)", VALUEvard, syntaxARQ) ;
    }
    
    // ---- varp
    
    @Test public void agg_stat_varp_uri() {
        test("agg:varp(?x)", VALUEvarp, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_varp_uri_d() {
        test("agg:varpd(?x)", VALUEvarpd, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_varp_uri_distinct() {
        test("agg:varp(DISTINCT ?x)", VALUEvarpd, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_varp_agg() {
        test("AGG agg:varp(?x)", VALUEvarp, syntaxARQ) ; 
    }
    
    @Test public void agg_stat_varp_agg_distinct() {
        test("AGG agg:varp(DISTINCT ?x)", VALUEvarpd, syntaxARQ) ;
    }
    
    //<http://jena.hpl.hp.com/ARQ/function/aggregate#>
    
    @Test public void agg_stat_stdev_full_uri() {
        test("<http://jena.apache.org/ARQ/function/aggregate#stdev>(?x)", VALUEstdev, syntaxSPARQL_11) ; 
    }

    @Test public void agg_stat_registry() {
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#stdev")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#stdevp")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#var")) ;
        assertTrue(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function/aggregate#varp")) ;
        assertFalse(AggregateRegistry.isRegistered("http://jena.apache.org/ARQ/function#e")) ;  // Not an aggregate
    }
    
    // ---- Corner cases. 0 and 1 matches.
    
    @Test public void agg_stat_stdev_empty_1() {
        // Empty -> error 
        testErr("agg:stdev(?x)", dsEmpty, syntaxSPARQL_11) ; 
    }
    
    @Test public void agg_stat_stdev_empty_2() {
        // Empty -> error 
        testErr("agg:stdevp(?x)", dsEmpty, syntaxSPARQL_11) ; 
    }
    
    // Sample of one -> error
    @Test public void agg_stat_size_one_1() {
        testErr("agg:stdev(?x)", ds1, syntaxSPARQL_11) ; 
    }
    
    // Population of one -> 0e0
    @Test public void agg_stat_size_one_2() {
        Query query = build("agg:stdevp(?x)", syntaxSPARQL_11) ; 
        test(query, 0e0, ds1) ; 
    }
    
    private static void test(String qsAgg, double expected, Syntax syntax) {
        test(qsAgg, expected, syntax, ds) ;
    }

    private static void test(String qsAgg, double expected, Syntax syntax,  DatasetGraph dsg) {
        Query query = build(qsAgg, syntax) ; 
        test(query, expected, dsg) ;
    }

    private static void test(Query query, double expected, DatasetGraph dsg) {
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(dsg)) ) {
            Literal literal = qExec.execSelect().next().getLiteral("X") ;
            double result = literal.getDouble() ;
            assertEquals(expected, result, 0.001);
        }
    }

    private static Query build(String qsAgg, Syntax syntax) {
        String NL = "\n" ;
        String qs = PRE+NL+"SELECT ("+qsAgg+NL+"AS ?X) WHERE {?s ?p ?x} GROUP BY ?s" ;
        Query query = QueryFactory.create(qs, syntax) ;
        return query ;
    }
    
    private void testErr(String qsAgg, DatasetGraph ds, Syntax syntax) {
        Query query = build(qsAgg, syntax) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(ds)) ) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue(rs.hasNext()) ;
            assertTrue(rs.getResultVars().contains("X")) ;
            Binding b = rs.nextBinding() ;
            assertFalse(b.contains(Var.alloc("X"))) ;
        }
    }
}
