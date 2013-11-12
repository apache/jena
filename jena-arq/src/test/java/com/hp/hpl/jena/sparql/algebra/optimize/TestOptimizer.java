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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestOptimizer extends BaseTest
{
    // A lot of the optimizer is tested by using the scripted queries.
    
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
            "  (distinct\n" +
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
        // Per JENA-587 not safe to transform if a SELECT *
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o"  ;  
        String opExpectedString = 
            "(distinct\n" + 
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
    
    @Test public void distinct_to_reduced_03()
    {
        // Per JENA-587 this is safe to transform since all project variables 
        // appear in the ORDER BY
        // Ordering of variables in the ORDER BY is irrelevant as long as they appear
        // before any non-projected variables
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY ?p ?o"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (project (?p)\n" +
            "    (order (?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_04()
    {
        try {
            // Must be turned off or will apply in favour of the specific optimization we are trying to test
            ARQ.getContext().set(ARQ.optOrderByDistinctApplication, false);
            
            // Per JENA-587 this is safe to transform since all project variables 
            // appear in the ORDER BY
            // Ordering of variables in the ORDER BY is irrelevant as long as they appear
            // before any non-projected variables
            assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
            String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?p ?o"  ;  
            String opExpectedString = 
                "(reduced\n" + 
                "  (project (?p ?o)\n" +
                "    (order (?p ?o)\n" +
                "      (bgp (triple ?s ?p ?o)))))" ; 
            check(queryString, opExpectedString) ;
        } finally {
            ARQ.getContext().unset(ARQ.optOrderByDistinctApplication);
        }
    }
    
    @Test public void distinct_to_reduced_05()
    {
        try {
            // Must be turned off or will apply in favour of the specific optimization we are trying to test
            ARQ.getContext().set(ARQ.optOrderByDistinctApplication, false);
            
            // Per JENA-587 this is safe to transform since all project variables 
            // appear in the ORDER BY
            // Ordering of variables in the ORDER BY is irrelevant as long as they appear
            // before any non-projected variables
            assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
            String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?o ?p"  ;  
            String opExpectedString = 
                "(reduced\n" + 
                "  (project (?p ?o)\n" +
                "    (order (?o ?p)\n" +
                "      (bgp (triple ?s ?p ?o)))))" ; 
            check(queryString, opExpectedString) ;
        } finally {
            ARQ.getContext().unset(ARQ.optOrderByDistinctApplication);
        }
    }
    
    @Test public void distinct_to_reduced_06()
    {
        // Per JENA-587 this is safe to transform since all project variables 
        // appear in the ORDER BY
        // Ordering of variables in the ORDER BY is irrelevant as long as they appear
        // before any non-projected variables
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?o ?p ?s"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?o ?p ?s)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_07()
    {
        // Per JENA-587 this is safe to transform since all project variables 
        // appear in the ORDER BY
        // Ordering of variables in the ORDER BY is irrelevant as long as they appear
        // before any non-projected variables
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?p ?o ?s"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?p ?o ?s)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_08()
    {
        // Per JENA-587 this is unsafe to transform since a non-project variable 
        // appears before all the projected variables are seen in the ORDER BY
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?s ?p ?o"  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?s ?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_09()
    {
        // Per JENA-587 this is unsafe to transform since a non-project variable 
        // appears before all the projected variables are seen in the ORDER BY
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?p ?s ?o"  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?p ?s ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_01()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY ?p";
        String opExpectedString =
            "(order (?p)\n" +
            "  (distinct\n" +
            "    (project (?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_02()
    {
        try {
            ARQ.setFalse(ARQ.optOrderByDistinctApplication) ;
            ARQ.setFalse(ARQ.optDistinctToReduced) ;
            assertTrue(ARQ.isFalse(ARQ.optOrderByDistinctApplication)) ;
            String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY ?p";
            String opExpectedString =
                "(distinct\n" +
                "  (project (?p)\n" +
                "    (order (?p)\n" +
                "      (bgp (triple ?s ?p ?o)))))" ;
            check(queryString, opExpectedString) ;
        } finally {
            ARQ.unset(ARQ.optOrderByDistinctApplication);
            ARQ.unset(ARQ.optDistinctToReduced);
        }
    }
    
    @Test public void distinct_order_by_application_03()
    {
        // Evaluation reordering optimization doesn't apply if it's a SELECT *
        // Also per JENA-587 DISTINCT -> REDUCED transformation cannot apply either
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p";
        String opExpectedString =
            "  (distinct\n" +
            "    (order (?p)\n" +
            "      (bgp (triple ?s ?p ?o))))" ;
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_04()
    {
        // The optimization still applies when order conditions are not simple variables
        // provided every variable used in an expression appears in the project list
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY LCASE(STR(?p))";
        String opExpectedString =
            "(order ((lcase (str (?p))))\n" +
            "  (distinct\n" +
            "    (project (?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_05()
    {
        // The optimization still applies when order conditions are not simple variables
        // provided every variable used in an expression appears in the project list
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT ?s ?p { ?s ?p ?o } ORDER BY LCASE(CONCAT(?s, ?p))";
        String opExpectedString =
            "(order ((lcase (concat ?s ?p)))\n" +
            "  (distinct\n" +
            "    (project (?s ?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        check(queryString, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_06()
    {
        // The optimization can apply when order conditions are not simple variables
        // provided every variable used in an expression appears in the project list
        // In this case it should not apply because the condition used a variable that
        // does not appear in the project list
        // Per JENA-587 the DISTINCT to REDUCED optimization also does not apply
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY LCASE(CONCAT(?s, ?p))";
        String opExpectedString =
            "  (distinct\n" +
            "    (project (?p)\n" +
            "      (order ((lcase (concat ?s ?p)))\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        check(queryString, opExpectedString) ;
    }
    
    @Test public void reduced_order_by_application_01()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT REDUCED ?p { ?s ?p ?o } ORDER BY ?p";
        String opExpectedString =
            "(order (?p)\n" +
            "  (reduced\n" +
            "    (project (?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        check(queryString, opExpectedString) ;
    }
    
    @Test public void reduced_order_by_application_02()
    {
        try {
            ARQ.setFalse(ARQ.optOrderByDistinctApplication) ;
            assertTrue(ARQ.isFalse(ARQ.optOrderByDistinctApplication)) ;
            String queryString = "SELECT REDUCED ?p { ?s ?p ?o } ORDER BY ?p";
            String opExpectedString =
                "(reduced\n" +
                "  (project (?p)\n" +
                "    (order (?p)\n" +
                "      (bgp (triple ?s ?p ?o)))))" ;
            check(queryString, opExpectedString) ;
        } finally {
            ARQ.unset(ARQ.optOrderByDistinctApplication);
        }
    }
    
    @Test public void subQueryProject_01() {
        String qs = StrUtils.strjoinNL
            ( "SELECT *"
            , "WHERE {"
            , "    ?test ?p1 ?X." 
            , "    { SELECT ?s1 ?test { ?test ?p2 ?o2 } }"
            , "}") ; 
        
        String ops = StrUtils.strjoinNL
            ("(sequence"
            ,"  (bgp (triple ?test ?p1 ?X))"
            ,"  (project (?s1 ?test)"
            ,"    (bgp (triple ?test ?/p2 ?/o2))))"
            ) ;
        check(qs, ops) ;
    }

    @Test public void subQueryProject_02() {
        String qs = StrUtils.strjoinNL
            ( "SELECT *"
            , "WHERE {"
            , "    ?test ?p1 ?X." 
            , "    { SELECT ?s1 { ?test ?p2 ?o2 } }"
            , "}") ; 
        
        String ops = StrUtils.strjoinNL
            ("(sequence"
            ,"  (bgp (triple ?test ?p1 ?X))"
            ,"  (project (?s1)"
            ,"    (bgp (triple ?/test ?/p2 ?/o2))))"
            ) ;
        check(qs, ops) ;
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
    
    @Test public void combine_extend_01()
    {
        Op extend = OpExtend.extendDirect(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        extend = OpExtend.extendDirect(extend, new VarExprList(Var.alloc("y"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(extend ((?x 1) (?y 2))",
                                            "  (table unit))");
        
        check(extend, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_extend_02()
    {
        Op extend = OpExtend.extendDirect(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        extend = OpExtend.extendDirect(extend, new VarExprList(Var.alloc("y"), new ExprVar("x")));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(extend ((?x 1) (?y ?x))",
                                            "  (table unit))");
        
        check(extend, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_extend_03()
    {
        // Technically illegal SPARQL here but useful to validate that the optimizer doesn't do the wrong thing
        Op extend = OpExtend.extendDirect(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        extend = OpExtend.extendDirect(extend, new VarExprList(Var.alloc("x"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(extend ((?x 2))",
                                            "  (extend ((?x 1))",
                                            "    (table unit)))");
        
        check(extend, new TransformExtendCombine(), opExpectedString);
    }
        
    @Test public void combine_assign_01()
    {
        Op assign = OpAssign.assignDirect(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        assign = OpAssign.assignDirect(assign, new VarExprList(Var.alloc("y"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(assign ((?x 1) (?y 2))",
                                            "  (table unit))");
        
        check(assign, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_assign_02()
    {
        Op assign = OpAssign.assignDirect(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        assign = OpAssign.assignDirect(assign, new VarExprList(Var.alloc("y"), new ExprVar("x")));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(assign ((?x 1) (?y ?x))",
                                            "  (table unit))");
        
        check(assign, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_assign_03()
    {
        Op assign = OpAssign.assignDirect(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        assign = OpAssign.assignDirect(assign, new VarExprList(Var.alloc("x"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(assign ((?x 2))",
                                            "  (assign ((?x 1))",
                                            "    (table unit)))");
        
        check(assign, new TransformExtendCombine(), opExpectedString);
    }
    
    public static void check(String queryString, String opExpectedString)
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
    
    private static void check(Op opToOptimize, Transform additionalOptimizer, String opExpectedString) {
        Op opOptimize = Algebra.optimize(opToOptimize) ;
        opOptimize = Transformer.transform(additionalOptimizer, opOptimize) ;
        Op opExpected = SSE.parseOp(opExpectedString) ;
        assertEquals(opExpected, opOptimize) ;
    }
    
    private static void checkAlgebra(String algString, String opExpectedString)
    {
        Op algebra = SSE.parseOp(algString);
        algebra = Algebra.optimize(algebra);
        Op opExpexpected = SSE.parseOp(opExpectedString);
        assertEquals(opExpexpected, algebra);
    }
    
}
