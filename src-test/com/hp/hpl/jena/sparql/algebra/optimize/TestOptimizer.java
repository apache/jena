/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.optimize;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

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
            "        (sequence\n" + 
            "          (bgp (triple ?/x ?/y ?v))\n" + 
            "          (bgp (triple ?/a ?/y ?/w)))))))" ; 
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

    @Test public void slice_order_to_topn_04()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optTopNSorting)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o LIMIT 42"  ;  
        String opExpectedString = 
            "(top (42 ?p ?o)\n" + 
            "  (distinct\n" +
            "  (bgp (triple ?s ?p ?o))))" ; 
        check(queryString, opExpectedString) ;
    }

    @Test public void slice_order_to_topn_05()
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */