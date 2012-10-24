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

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestOptimizer extends BaseTest
{
    // A lot of the optimizer is tested by using the scripted queries.
    
    @Test public void query_rename_01()
    {
        String queryString =  
            "SELECT ?x { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }" ;
        String opExpectedString =
            "(project (?x)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (project (?v)\n" + 
            "        (join\n" + 
            "          (bgp (triple ?/x ?/y ?v))\n" + 
            "          (project (?/w)\n" + 
            "            (bgp (triple ?//a ?//y ?/w))))))))";
        check(queryString, opExpectedString) ;
    }

    @Test public void query_rename_02()
    {
        String queryString = 
            "SELECT ?x { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT * { ?a ?y ?w }}} LIMIT 50 } }"  ;  
        String opExpectedString = 
            "(project (?x)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (project (?v)\n" + 
            "(bgp (triple ?/x ?/y ?v) (triple ?/a ?/y ?/w))" +
            "))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void query_rename_03()
    {
        String queryString = "SELECT ?x { ?s ?p ?o . { SELECT * { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }" ;  
        String opExpectedString = 
            "(project (?x)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (join\n" + 
            "        (bgp (triple ?x ?y ?v))\n" + 
            "        (project (?w)\n" + 
            "          (bgp (triple ?/a ?/y ?w)))))))" ;
        check(queryString, opExpectedString) ;
    }

    @Test public void query_rename_04()
    {
        String queryString = "SELECT * { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }" ;  
        String opExpectedString = 
            "(join\n" + 
            "  (bgp (triple ?s ?p ?o))\n" + 
            "  (slice _ 50\n" + 
            "    (project (?v)\n" + 
            "      (join\n" + 
            "        (bgp (triple ?/x ?/y ?v))\n" + 
            "        (project (?/w)\n" + 
            "          (bgp (triple ?//a ?//y ?/w)))))))" ;
        check(queryString, opExpectedString) ;
    }

    @Test public void query_rename_05()
    {
        String queryString = "SELECT ?v { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }"    ;  
        String opExpectedString = 
            "(project (?v)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (project (?v)\n" + 
            "        (join\n" + 
            "          (bgp (triple ?/x ?/y ?v))\n" + 
            "          (project (?/w)\n" + 
            "            (bgp (triple ?//a ?//y ?/w))))))))" ;
        check(queryString, opExpectedString) ;
    }

    @Test public void query_rename_06()
    {
        String queryString = "SELECT ?w { ?s ?p ?o . { SELECT ?w { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} } } LIMIT 50" ;  
        String opExpectedString = 
            "(slice _ 50\n" + 
            "  (project (?w)\n" + 
            "    (join\n" + 
            "      (bgp (triple ?s ?p ?o))\n" + 
            "      (project (?w)\n" + 
            "        (join\n" + 
            "          (bgp (triple ?/x ?/y ?/v))\n" + 
            "          (project (?w)\n" + 
            "            (bgp (triple ?//a ?//y ?w))))))))\n" + 
            "" ;
        check(queryString, opExpectedString) ;
    }

    @Test public void query_rename_07()
    {
        String queryString = "SELECT * { ?s ?p ?o . { SELECT ?w { ?x ?y ?v }}}"  ;  
        String opExpectedString = 
            "(join\n" + 
            "  (bgp (triple ?s ?p ?o))\n" + 
            "  (project (?w)\n" + 
            "    (bgp (triple ?/x ?/y ?/v))))" ;
        check(queryString, opExpectedString) ;
    }
    
    @Test public void slice_order_to_topn_01()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 42"  ;  
        String opExpectedString = 
            "(top (42 ?p ?o)\n" + 
            "  (bgp (triple ?s ?p ?o)))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_02()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 4242"  ;  
        String opExpectedString = 
        	"(slice _ 4242\n" + 
        	"  (order (?p ?o)\n" +
            "    (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_03()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT * { ?s ?p ?o } ORDER BY ?p ?o OFFSET 4242 LIMIT 10"  ;  
        String opExpectedString = 
            "(slice 4242 10\n" + 
            "  (order (?p ?o)\n" +
            "    (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_04()
    {
        try {
            ARQ.setFalse(ARQ.optTopNSorting) ;
            assertTrue(ARQ.isFalse(ARQ.optTopNSorting)) ;
            String queryString = "SELECT * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 42"  ;  
            String opExpectedString = 
                "(slice _ 42\n" + 
                "  (order (?p ?o)\n" +
                "    (bgp (triple ?s ?p ?o))))" ; 
            check(queryString, opExpectedString) ;
        } finally {
            ARQ.unset(ARQ.optTopNSorting) ;
        }
    }

    @Test public void slice_order_to_topn_05()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 42"  ;  
        String opExpectedString = 
            "(top (42 ?p ?o)\n" + 
            "  (distinct\n" +
            "     (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_06()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o OFFSET 24 LIMIT 42"  ;  
        String opExpectedString = 
            "(slice 24 _\n" + 
            "  (top (66 ?p ?o)\n" + 
            "    (distinct\n" +
            "       (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_07()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT REDUCED * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 42"  ;  
        String opExpectedString = 
            "(top (42 ?p ?o)\n" + 
            "  (distinct\n" +
            "     (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_08()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 4242"  ;  
        String opExpectedString = 
            "(slice _ 4242\n" + 
            "  (reduced\n" +
            "    (order (?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_09()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT REDUCED * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 4242"  ;  
        String opExpectedString = 
            "(slice _ 4242\n" + 
            "  (reduced\n" +
            "    (order (?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }
    
    @Test public void slice_order_to_topn_10()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT * { ?s ?p ?o } ORDER BY ?p ?o OFFSET 1 LIMIT 5"  ;  
        String opExpectedString = 
            "(slice 1 _\n" +
            "  (top (6 ?p ?o)\n" +
            "    (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_11()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT ?s { ?s ?p ?o } ORDER BY ?p ?o OFFSET 1 LIMIT 5"  ;  
        String opExpectedString = 
            "(slice 1 _\n" +
            "  (project (?s)\n" + 
            "    (top (6 ?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_12()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT ?s { ?s ?p ?o } ORDER BY ?p ?o LIMIT 42"  ;  
        String opExpectedString = 
            "(project (?s)\n" + 
            "  (top (42 ?p ?o)\n" + 
            "    (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void distinct_to_reduced_01()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (order (?p ?o)\n" +
            "    (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void distinct_to_reduced_02()
    {
        try {
            ARQ.setFalse(ARQ.optDistinctToReduced) ;
            assertTrue(ARQ.isFalse(ARQ.optDistinctToReduced)) ;
            String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o"  ;  
            String opExpectedString = 
                "(distinct\n" + 
                "  (order (?p ?o)\n" +
                "    (bgp (triple ?s ?p ?o))))" ; 
            check(queryString, opExpectedString) ;
        } finally {
            ARQ.unset(ARQ.optDistinctToReduced) ;
        }
    }
    
    @Test public void optimize_01()
    { 
        String queryString = "SELECT * { { ?s ?p ?x } UNION { ?s1 ?p1 ?x } FILTER(?x = <urn:x1> || ?x = <urn:x2>) }" ;
        String opExpectedString =  StrUtils.strjoinNL(
                                            "(disjunction",
                                            "    (assign ((?x <urn:x1>))" ,
                                            "      (union" ,
                                            "        (bgp (triple ?s ?p <urn:x1>))" ,
                                            "        (bgp (triple ?s1 ?p1 <urn:x1>))))" ,
                                            "    (assign ((?x <urn:x2>))" ,
                                            "      (union" ,
                                            "        (bgp (triple ?s ?p <urn:x2>))" ,
                                            "        (bgp (triple ?s1 ?p1 <urn:x2>)))))" ) ;
        check(queryString, opExpectedString) ; 
    }

    
    private static void check(String queryString, String opExpectedString)
    {
        queryString = "PREFIX : <http://example/>\n"+queryString ;
        Query query = QueryFactory.create(queryString) ;
        Op opQuery = Algebra.compile(query) ;
        check(opQuery, opExpectedString) ;
    }

    private static void check(Op opToOptimize, String opExpectedString)
    {
        Op opOptimize = Algebra.optimize(opToOptimize) ;
        Op opExpected = SSE.parseOp(opExpectedString) ;
        assertEquals(opExpected, opOptimize) ;
    }
    
}
