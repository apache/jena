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

package org.apache.jena.sparql.algebra.optimize;

import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.OpAssign ;
import org.apache.jena.sparql.algebra.op.OpExtend ;
import org.apache.jena.sparql.algebra.op.OpTable ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test ;

public class TestOptimizer extends AbstractTestTransform
{
    static { JenaSystem.init(); }

    // These test calls of the whole optimzier.
    // A lot of the optimizer is tested by using the scripted queries.
    // There are many tests of individual transforms.

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
            "  (order (?p ?o)\n" +
            "    (distinct\n" +
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
        String queryString = "SELECT * { { ?s ?p ?x } UNION { ?s1 ?p1 ?x } FILTER(?x = <urn:ex:1> || ?x = <urn:ex:2>) }" ;
        String opExpectedString =  StrUtils.strjoinNL(
                                            "(disjunction",
                                            "    (assign ((?x <urn:ex:1>))" ,
                                            "      (union" ,
                                            "        (bgp (triple ?s ?p <urn:ex:1>))" ,
                                            "        (bgp (triple ?s1 ?p1 <urn:ex:1>))))" ,
                                            "    (assign ((?x <urn:ex:2>))" ,
                                            "      (union" ,
                                            "        (bgp (triple ?s ?p <urn:ex:2>))" ,
                                            "        (bgp (triple ?s1 ?p1 <urn:ex:2>)))))" ) ;
        check(queryString, opExpectedString) ;
    }

    // JENA-1235
    @Test public void optimize_02() {
        String in = StrUtils.strjoinNL
            (
             "(filter (exprlist (|| (= ?var3 'ABC') (= ?var3 'XYZ')) (&& (regex ?var4 'pat1') (!= ?VAR 123)))"
             ,"    (bgp"
             ,"      (triple ?var2 :p1 ?var4)"
             ,"      (triple ?var2 :p2 ?var3)"
             ,"    ))") ;

        // Answer when  reorder BGPs before general filter placements.
        String expected = StrUtils.strjoinNL
            ("(filter (!= ?VAR 123)"
            ,"  (disjunction"
            ,"      (assign ((?var3 'ABC'))"
            ,"        (filter (regex ?var4 'pat1')"
            ,"          (bgp"
            ,"            (triple ?var2 <http://example/p2> 'ABC')"
            ,"            (triple ?var2 <http://example/p1> ?var4)"
            ,"          )))"
            ,"      (assign ((?var3 'XYZ'))"
            ,"        (filter (regex ?var4 'pat1')"
            ,"          (bgp"
            ,"           (triple ?var2 <http://example/p2> 'XYZ')"
            ,"            (triple ?var2 <http://example/p1> ?var4)"
            ,"         )))))"
            );

        checkAlgebra(in, expected) ;

        // Before JENA-2317 when BGP reordering was done in the algebra optimization phase.
//        String out = StrUtils.strjoinNL
//                ("(filter (!= ?VAR 123)"
//                 ," (disjunction"
//                 ,"   (assign ((?var3 'ABC'))"
//                 ,"     (sequence"
//                 ,"       (filter (regex ?var4 'pat1')"
//                 ,"         (bgp (triple ?var2 :p1 ?var4)))"
//                 ,"       (bgp (triple ?var2 :p2 'ABC'))))"
//                 ,"   (assign ((?var3 'XYZ'))"
//                 ,"     (sequence"
//                 ,"       (filter (regex ?var4 'pat1')"
//                 ,"         (bgp (triple ?var2 :p1 ?var4)))"
//                 ,"       (bgp (triple ?var2 :p2 'XYZ'))))))"
//                 ) ;
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

    @Test public void combine_extend_05()
    {
        // JENA-809 : check no changes to input.
        String x = "(project (?x) (extend ((?bar 2)) (extend ((?foo 1)) (table unit))))" ;
        String y = "(project (?x) (extend ((?foo 1) (?bar 2)) (table unit)))" ;
        checkAlgebra(x, new TransformExtendCombine(), y);
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

    @Test public void combine_assign_05()
    {
        // JENA-809 : check no changes to input.
        String x = "(project (?x) (assign ((?bar 2)) (assign ((?foo 1)) (table unit))))" ;
        String y = "(project (?x) (assign ((?foo 1) (?bar 2)) (table unit)))" ;
        AbstractTestTransform.checkAlgebra(x, new TransformExtendCombine(), y);
    }

    // Nested
/*
 *    String qs = StrUtils.strjoinNL
            ("select *",
             "where {",
             "  { select * { ?id ?p ?label } order by ?label limit 5 }",
             "  OPTIONAL { OPTIONAL { ?s ?p ?label }}",
             "}"
                );
 */
    // Derived from JENA-1041 (inner TopN)
    @Test public void subselect_01() {
        String qs = StrUtils.strjoinNL
            ("select *",
             "where {",
             "  { select * { ?id ?p ?label } order by ?label limit 5 }",
             "  ?s ?p ?label",
             "}"
                );
        String expected = StrUtils.strjoinNL
            ("(sequence"
            ,"  (top (5 ?label)"
            ,"    (bgp (triple ?id ?p ?label)))"
            ,"  (bgp (triple ?s ?p ?label)))") ;
        check(qs, expected) ;
    }

    // Derived from JENA-1041 (inner TopN)
    @Test public void subselect_02() {
        // Has a blocking optional pattern for the join strategy.
        String qs = StrUtils.strjoinNL
            ("select *",
             "where {",
             "  { select * { ?id ?p ?label } order by ?label limit 5 }",
             "  OPTIONAL { OPTIONAL { ?s ?p ?label }}",
             "}"
                );
        String expected = StrUtils.strjoinNL
            ("(leftjoin"
            ,"  (top (5 ?label)"
            ,"    (bgp (triple ?id ?p ?label)))"
            ,"  (conditional"
            ,"    (table unit)"
            ,"    (bgp (triple ?s ?p ?label))))") ;
        check(qs, expected) ;
    }

    // JENA-1280 : Test that variables in FILTER EXISTS do not block sequence
    @Test public void joinSequence_01() {
        String queryString = StrUtils.strjoinNL(
                               "SELECT * {"
                               , "  ?s ?p ?o"
                               , "  GRAPH ?g {"
                               , "    ?s1 ?p ?o1 ."
                               , "    FILTER EXISTS {  [] ?p ?unique } ."
                               , "  }"
                               , "}"
            );
        // For reference, which is ...
        String x = StrUtils.strjoinNL(
            "(join"
            ,"    (bgp (triple ?s ?p ?o))"
            ,"    (graph ?g"
            ,"      (filter (exists (bgp (triple ??0 ?p ?unique)))"
            ,"        (bgp (triple ?s1 ?p ?o1)))))"
            );

        String optimized = StrUtils.strjoinNL(
                                      "(sequence"
                                      ,"    (bgp (triple ?s ?p ?o))"
                                      ,"    (graph ?g"
                                      ,"      (filter (exists (bgp (triple ??0 ?p ?unique)))"
                                      ,"        (bgp (triple ?s1 ?p ?o1)))))"
                                      );

        check(queryString, optimized);
    }
}
