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

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestOptimizer extends AbstractTestTransform
{
    // These test call the whole optimzier.
    // A lot of the optimizer is tested by using the scripted queries.
    // Theer are many tests of individual transforms.
    
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
        Op extend = OpExtend.create(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        extend = OpExtend.create(extend, new VarExprList(Var.alloc("y"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(extend ((?x 1) (?y 2))",
                                            "  (table unit))");
        
        check(extend, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_extend_02()
    {
        Op extend = OpExtend.create(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        extend = OpExtend.create(extend, new VarExprList(Var.alloc("y"), new ExprVar("x")));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(extend ((?x 1) (?y ?x))",
                                            "  (table unit))");
        
        check(extend, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_extend_03()
    {
        // Technically illegal SPARQL here but useful to validate that the optimizer doesn't do the wrong thing
        Op extend = OpExtend.create(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        extend = OpExtend.create(extend, new VarExprList(Var.alloc("x"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(extend ((?x 2))",
                                            "  (extend ((?x 1))",
                                            "    (table unit)))");
        
        check(extend, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_extend_04()
    {
        String opString = StrUtils.strjoinNL
            ("(extend ((?x 2))"
            ,"  (extend ((?y 3))"
            ,"    (distinct"
            ,"      (extend ((?a 'A') (?b 'B'))"
            ,"        (extend ((?c 'C'))"
            ,"          (table unit)"
            ,"        )))))"
            );
        String opExpectedString = StrUtils.strjoinNL
            ("(extend ((?y 3) (?x 2))"
            ,"  (distinct"
            ,"    (extend ((?c 'C') (?a 'A') (?b 'B'))" 
            ,"      (table unit))))");
        
        Op op = SSE.parseOp(opString) ;
        check(op, new TransformExtendCombine(), opExpectedString);
    }

        
    @Test public void combine_assign_01()
    {
        Op assign = OpAssign.create(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        assign = OpAssign.create(assign, new VarExprList(Var.alloc("y"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(assign ((?x 1) (?y 2))",
                                            "  (table unit))");
        
        check(assign, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_assign_02()
    {
        Op assign = OpAssign.create(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        assign = OpAssign.create(assign, new VarExprList(Var.alloc("y"), new ExprVar("x")));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(assign ((?x 1) (?y ?x))",
                                            "  (table unit))");
        
        check(assign, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_assign_03()
    {
        Op assign = OpAssign.create(OpTable.unit(), new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        assign = OpAssign.create(assign, new VarExprList(Var.alloc("x"), new NodeValueInteger(2)));
        
        String opExpectedString = StrUtils.strjoinNL(
                                            "(assign ((?x 2))",
                                            "  (assign ((?x 1))",
                                            "    (table unit)))");
        
        check(assign, new TransformExtendCombine(), opExpectedString);
    }
    
    @Test public void combine_assign_04()
    {
        String opString = StrUtils.strjoinNL
            ("(assign ((?x 2))"
            ,"  (assign ((?y 3))"
            ,"    (distinct"
            ,"      (assign ((?a 'A') (?b 'B'))"
            ,"        (assign ((?c 'C'))"
            ,"          (table unit)"
            ,"        )))))"
            );
        String opExpectedString = StrUtils.strjoinNL
            ("(assign ((?y 3) (?x 2))"
            ,"  (distinct"
            ,"    (assign ((?c 'C') (?a 'A') (?b 'B'))" 
            ,"      (table unit))))");
        
        Op op = SSE.parseOp(opString) ;
        check(op, new TransformExtendCombine(), opExpectedString);
    }

}
