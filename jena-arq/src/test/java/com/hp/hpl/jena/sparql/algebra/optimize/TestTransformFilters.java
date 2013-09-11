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
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Tests of transforms related to filters */
public class TestTransformFilters
{
    private Transform t_equality    = new TransformFilterEquality() ;
    private Transform t_disjunction = new TransformFilterDisjunction() ;
    private Transform t_placement   = new TransformFilterPlacement() ;
    private Transform t_expandOneOf = new TransformExpandOneOf() ;
    private Transform t_implicitJoin = new TransformFilterImplicitJoin() ;
    private Transform t_implicitLeftJoin = new TransformImplicitLeftJoin() ;
    
    @Test public void equality01()
    {
        test("(filter (= ?x <x>) (bgp ( ?s ?p ?x)) )",
             t_equality,
             "(assign ((?x <x>)) (bgp ( ?s ?p <x>)) )") ;
    }
    
    @Test public void equality02()
    {
        // Not safe on strings
        test("(filter (= ?x 'x') (bgp ( ?s ?p ?x)) )",
             t_equality,
             (String[])null) ;
    }

    @Test public void equality03()
    {
        // Not safe on numbers
        test("(filter (= ?x 123) (bgp ( ?s ?p ?x)) )",
             t_equality,
             (String[])null) ;
    }
    
    @Test public void equality04()
    {
        // Unused
        test("(filter (= ?UNUSED <x>) (bgp ( ?s ?p ?x)) )",
             t_equality,
             "(table empty)") ;
    }
    
    @Test public void equality05()
    {
        // Can't optimize if filter does not cover vars in LHS 
        test("(filter (= ?x2 <x>) (conditional (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))",
             t_equality,
             "(filter (= ?x2 <x>) (conditional (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))") ;
    }
    
    
    @Test public void equality06()
    {
        test("(filter (= ?x <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x))))",
             t_equality,
             "(assign((?x <x>)) (conditional (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }
    
    @Test public void equality07()
    {
        test("(filter (= ?x <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
             t_equality,
             "(assign((?x <x>)) (conditional (bgp ( ?s ?p <x>))  (bgp ( ?s ?p ?x1))))") ;
    }
    
    @Test public void equality08()
    {
        test("(filter (= ?x1 <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
             t_equality,
             "(filter (= ?x1 <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))") ;
    }
    
    @Test public void equality09()
    {
        // Can't optimize if filter does not cover vars in LHS 
        test("(filter (= ?x2 <x>) (leftjoin (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))",
             t_equality,
             "(filter (= ?x2 <x>) (leftjoin (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))") ;
    }
    
    @Test public void equality10()
    {
        test("(filter (= ?x <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x))))",
             t_equality,
             "(assign((?x <x>)) (leftjoin (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }
    
    @Test public void equality11()
    {
        test("(filter (= ?x <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
             t_equality,
             "(assign((?x <x>)) (leftjoin (bgp ( ?s ?p <x>))  (bgp ( ?s ?p ?x1))))") ;
    }
    
    @Test public void equality12()
    {
        test("(filter (= ?x1 <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
             t_equality,
             "(filter (= ?x1 <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))") ;
    }

    @Test public void equality13()
    {
        test("(filter (= ?x1 <x>) (join (bgp ( ?s ?p ?x1))  (bgp ( ?s ?p ?x1))))",
             t_equality,
             "(assign((?x1 <x>))  (join (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }

    @Test public void equality14()
    {
        test("(filter (= ?x1 <x>) (union (bgp ( ?s ?p ?x1))  (bgp ( ?s ?p ?x1))))",
             t_equality,
             "(assign((?x1 <x>))  (union (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }

    @Test public void equality15()
    {
        // assign-push-in optimization.
        test("(filter (= ?x1 <x>) (leftjoin (leftjoin (table unit) (bgp ( ?s ?p ?x1)) ) (bgp ( ?s ?p ?x1)) ))", 
             t_equality,
             "(filter (= ?x1 <x>)", 
             "   (leftjoin",
             "     (leftjoin",
             "       (table unit)", 
             "       (assign ((?x1 <x>)) (bgp (triple ?s ?p <x>)))",
             "     )",
             "     (assign ((?x1 <x>)) (bgp (triple ?s ?p <x>)))",
             "   ))" ) ;
    }
    
    // JENA-432 (simplified)
    @Test public void equality16()
    {
        /*
        SELECT *
        WHERE {
          ?test ?p1 ?o1.
          FILTER ( ?test = <http://localhost/t2> )
          OPTIONAL {
            SELECT ?s1
            { ?s1 ?p2 ?o2 }
          }
        } */
        String qs = StrUtils.strjoinNL
            ( "(filter (= ?test <http://localhost/t2>)"
            , "  (leftjoin"
            , "    (bgp (triple ?test ?p1 ?o1))"
            , "      (project (?s1)"
            , "       (bgp (triple ?s1 ?p2 ?o2)))))"
            ) ;
        test(qs,
             t_equality,
             "(assign ((?test <http://localhost/t2>))" ,
             "  (leftjoin" ,
             "    (bgp (triple <http://localhost/t2> ?p1 ?o1))" ,
             "    (project (?s1)" ,
             "      (bgp (triple ?s1 ?p2 ?o2)))))"
            ) ;
    }
    
    @Test public void equality17() {
        // Conflicting constraints should result in no optimization
        test("(filter ((= ?x <http://constant1>) (= ?x <http://constant2>)) (join (bgp (?x <http://p1> ?o1)) (bgp (?x <http://p2> ?o2))))",
             t_equality,
             (String[])null);
    }

    // Related to JENA-432
    @Test public void optionalEqualitySubQuery_01() {
        // Presence of ?test in the projection blocks the rewrite.
        // (this is actually over cautious).
        String qs = StrUtils.strjoinNL
            ( "SELECT *"
            , "WHERE {"
            , "    ?test ?p1 ?X." 
            , "    FILTER ( ?test = <http://localhost/t1> )"
            , "    { SELECT ?s1 ?test { ?test ?p2 ?o2 } }"
            , "}") ; 
        
        String ops = StrUtils.strjoinNL
            ("(filter (= ?test <http://localhost/t1>)"
            ,"    (sequence"
            ,"      (bgp (triple ?test ?p1 ?X))"
            ,"      (project (?s1 ?test)"
            ,"        (bgp (triple ?test ?/p2 ?/o2)))))"
            ) ;
        TestOptimizer.check(qs, ops) ;
    }
    
    // Related to JENA-432
    @Test public void optionalEqualitySubQuery_02() {
        String qs = StrUtils.strjoinNL
            ( "SELECT *"
            , "WHERE {"
            , "    ?test ?p1 ?X." 
            , "    FILTER ( ?test = <http://localhost/t1> )"
            , "    { SELECT ?s1 { ?test ?p2 ?o2 } }"
            , "}") ;
        String ops = StrUtils.strjoinNL
            ( "  (assign ((?test <http://localhost/t1>))"
            , "    (sequence"
            , "       (bgp (triple <http://localhost/t1> ?p1 ?X))"
            , "       (project (?s1)"
            , "         (bgp (triple ?/test ?/p2 ?/o2))) ))"
            ) ;
        
        TestOptimizer.check(qs, ops) ;
    }
    
    // JENA-383, simplified.
    @Test public void optionalEquality_01() {
        // Not optimized because the TransformFilterEquality does not notice
        // ?x is fixed in the expression by the join.  
        String qs = StrUtils.strjoinNL
            ( "PREFIX : <http://example/> SELECT * {"
              , "    OPTIONAL { ?x :q ?o }"
              , "    FILTER(?x = :x)"
              , "    ?x :p ?o2"
              , "}"
                ) ;
        String ops = StrUtils.strjoinNL
            ( "(filter (= ?x <http://example/x>)"
            , "  (sequence"
            , "     (conditional"
            , "        (table unit)"
            , "        (bgp (triple ?x <http://example/q> ?o)))"
            , "     (bgp (triple ?x <http://example/p> ?o2))"
            , " ))" 
            ) ;
        TestOptimizer.check(qs, ops) ;
    }
    
    @Test public void optionalEqualityScope_01() {
        String qs = StrUtils.strjoinNL
            ( "PREFIX : <http://example/> SELECT * {"
              , "    OPTIONAL { ?x :q ?o }"
              , "    FILTER(?x = :x)"
              , "    ?x :p ?o2"
              , "}"
                ) ;
        // Possible transformation:
        // Safe to transform:  This (sequence) always defined ?x 
        String ops = StrUtils.strjoinNL
            ("(assign ((?x <http://example/x>))"
            , "   (sequence"
            , "       (conditional"
            , "         (table unit)"
            , "         (bgp (triple <http://example/x> <http://example/q> ?o)))"
            , "       (bgp (triple <http://example/x> <http://example/p> ?o2))))"
            ) ;
        // Currently :
        String ops1 = StrUtils.strjoinNL
            ("(filter (= ?x <http://example/x>)"
            ,"  (sequence"
            ,"    (conditional"
            ,"      (table unit)"
            ,"      (bgp (triple ?x <http://example/q> ?o)) )"
            ,"    (bgp (triple ?x <http://example/p> ?o2)) ))"
            ) ;
        
        TestOptimizer.check(qs, ops1) ;
    }

    // JENA-294 part II
    @Test public void optionalEqualityScope_02() {
        String qs = StrUtils.strjoinNL
            ( "PREFIX : <http://example/> SELECT * {"
              , "    ?x :p ?o2"
              , "    OPTIONAL { ?x :q ?o }"
              , "    FILTER(?x = :x)"
              , "}"
                ) ;
        // Safe to transform:  ?x is fixed. 
        String ops = StrUtils.strjoinNL
            ( "(assign ((?x <http://example/x>))"
            , "   (conditional"
            , "     (bgp (triple <http://example/x> <http://example/p> ?o2))"
            , "     (bgp (triple <http://example/x> <http://example/q> ?o))"
            , "   ))"
            ) ;
        TestOptimizer.check(qs, ops) ;
    }
    
    // JENA-294 part II
    @Test public void optionalEqualityScope_03() {
        String qs = StrUtils.strjoinNL
            ( "PREFIX : <http://example/> SELECT * {"
              , "    ?z :p ?o2"
              , "    OPTIONAL { ?x :q ?o }"
              , "    FILTER(?x = :x)"
              , "}"
                ) ;
        // Unsafe to transform:  ?x is optional. 
        String ops = StrUtils.strjoinNL
            ( "(filter (= ?x <http://example/x>)"
            , "   (conditional"
            , "     (bgp (triple ?z <http://example/p> ?o2))"
            , "     (bgp (triple ?x <http://example/q> ?o))"
            , "))"
            ) ;
        TestOptimizer.check(qs, ops) ;
    }

    // Scope of variable (optional, defined) cases 
    @Test public void test_OptEqualityScope_04() {
        String qs = StrUtils.strjoinNL
            ( "PREFIX : <http://example/> SELECT * {"
              , "    OPTIONAL { ?x :q ?o }"
              , "    FILTER(?x = :x)"
              , "}"
                ) ;
        // Unsafe to transform:  This may not defined ?x, then FILTER -> unbound -> error -> false
        String ops1 = StrUtils.strjoinNL
            ("(filter (= ?x <http://example/x>)"
            ,"    (conditional"
            ,"      (table unit)"
            ,"      (assign ((?x <http://example/x>))"
            ,"        (bgp (triple <http://example/x> <http://example/q> ?o)))))"
            ) ;
        
        TestOptimizer.check(qs, ops1) ;
    }

    
    @Test public void disjunction01()
    {
        test("(filter (|| (= ?x <x>) (= ?x <y>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(disjunction ",
               "(assign ((?x <x>)) (bgp ( ?s ?p <x>)))",
               "(assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
             ")") ;
    }
    
    @Test public void disjunction02()
    {
        test("(filter (|| (= ?x <x>) (!= ?x <y>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(disjunction ",
               "(assign ((?x <x>)) (bgp ( ?s ?p <x>)))",
               "(filter (!= ?x <y>) (bgp ( ?s ?p ?x)))",
             ")") ;
    }
    
    @Test public void disjunction03()
    {
        test("(filter (|| (!= ?x <x>) (= ?x <y>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             // Note - reording of disjunction terms.
             "(disjunction ",
               "(assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
               "(filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
             ")") ;
    }
    
    @Test public void disjunction04()
    {
        test("(filter (|| (!= ?x <y>) (!= ?x <x>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             (String[])null) ;
    }

    @Test public void disjunction05()
    {
        test("(filter (exprlist (|| (= ?x <y>) (!= ?x <x>)))    (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "  (disjunction",
             "    (assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
             "    (filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
             ")"
        ) ;
    }

    @Test public void disjunction06()
    {
        test("(filter (exprlist (lang ?x) (|| (= ?x <y>) (!= ?x <x>)))    (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(filter (lang ?x)",   
             "  (disjunction",
             "    (assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
             "    (filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
             "))"
        ) ;
    }
    
    @Test public void disjunction07()
    {
        test("(filter (exprlist (|| (= ?x <y>) (!= ?x <x>)) (lang ?x) )    (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(filter (lang ?x)",   
             "  (disjunction",
             "    (assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
             "    (filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
             "))"
        ) ;
    }

    @Test public void placement01()
    {
        test("(filter (= ?x 1) (bgp ( ?s ?p ?x)))",
             t_placement,
             "(filter (= ?x 1) (bgp ( ?s ?p ?x)))") ;
        	
    }
    
    @Test public void placement02()
    {
        test("(filter (= ?x 1) (bgp (?s ?p ?x) (?s1 ?p1 ?x1) ))",
             t_placement,
             "(sequence (filter (= ?x 1) (bgp ( ?s ?p ?x))) (bgp (?s1 ?p1 ?x1)))") ;
            
    }

    @Test public void placement03()
    {
        test("(filter (= ?x 1) (bgp (?s ?p ?x) (?s1 ?p1 ?x) ))",
             t_placement,
             "(sequence (filter (= ?x 1) (bgp ( ?s ?p ?x))) (bgp (?s1 ?p1 ?x)))") ;
    }

    @Test public void placement04()
    {
        test("(filter (= ?XX 1) (bgp (?s ?p ?x) (?s1 ?p1 ?XX) ))",
             t_placement,
             "(filter (= ?XX 1) (bgp (?s ?p ?x) (?s1 ?p1 ?XX) ))") ;
    }
    
    @Test public void placement10()
    {
        // Unbound
        test("(filter (= ?x ?unbound) (bgp (?s ?p ?x)))",
             t_placement,
             "(filter (= ?x ?unbound) (bgp (?s ?p ?x)))") ;
    }
    
    @Test public void placement11()
    {
        Op op1 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x)))") ;
        OpFilter f = (OpFilter)op1 ;
        Op op2 = TransformFilterPlacement.transform(f.getExprs(), ((OpBGP)f.getSubOp()).getPattern()) ;
        Op op3 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x)))") ;
        Assert.assertEquals(op3, op2) ;
    }

    @Test public void placement12()
    {
        Op op1 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x) (?s1 ?p1 ?XX)))") ;
        OpFilter f = (OpFilter)op1 ;
        Op op2 = TransformFilterPlacement.transform(f.getExprs(), ((OpBGP)f.getSubOp()).getPattern()) ;
        Op op3 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x) (?s1 ?p1 ?XX)))") ;
        Assert.assertEquals(op3, op2) ;
    }
    
    
    @Test public void placement20()
    {
        // conditional
        test("(filter (= ?x 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             t_placement,
             "(conditional (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?z)) )") ;
    }

    @Test public void placement21()
    {
        // conditional
        test("(filter (= ?z 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             t_placement,
             "(filter (= ?z 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))") ;
    }

    @Test public void placement22()
    {
        // conditional
        test("(filter (= ?x 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?x)) ))",
             t_placement,
             "(conditional (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x)) )") ;
    }

    
    @Test public void oneOf1()
    {
        test(
             "(filter (in ?x <x> 2 3) (bgp (?s ?p ?x)))",
             t_expandOneOf,
             "(filter (|| ( || (= ?x <x>) (= ?x 2)) (= ?x 3)) (bgp (?s ?p ?x)))") ;
    }

    @Test public void oneOf2()
    {
        test(
             "(filter (exprlist (= ?x 99) (in ?x <x> 2 3)) (bgp (?s ?p ?x)))",
             t_expandOneOf,
             "(filter (exprlist (= ?x 99) (|| ( || (= ?x <x>) (= ?x 2)) (= ?x 3))) (bgp (?s ?p ?x)))") ;
    }
    
    @Test public void oneOf3()
    {
        test(
             "(filter (notin ?x <x> 2 3) (bgp (?s ?p ?x)))",
             t_expandOneOf,
             "(filter (exprlist (!= ?x <x>) (!= ?x 2) (!= ?x 3)) (bgp (?s ?p ?x)))") ;
    }
    
    @Test public void implicitJoin1()
    {
        test(
             "(filter (= ?x ?y) (bgp (?x ?p ?o)(?y ?p1 ?o1)))",
             t_implicitJoin,
             "(assign ((?x ?y)) (bgp (?y ?p ?o)(?y ?p1 ?o1)))");
    }
    
    @Test public void implicitJoin2()
    {
        test(
             "(filter (= ?x ?y) (bgp (?x ?p ?o)))",
             t_implicitJoin,
             "(table empty)");
    }
    
    @Test public void implicitJoin3()
    {
        // Still safe to transform as at least one is guaranteed non-literal
        test(
             "(filter (= ?x ?y) (bgp (?x ?p ?o)(?a ?b ?y)))",
             t_implicitJoin,
             "(assign ((?x ?y)) (bgp (?y ?p ?o)(?a ?b ?y)))");
    }
    
    @Test public void implicitJoin4()
    {
        // Not safe to transform as both may be literals
        test(
             "(filter (= ?x ?y) (bgp (?a ?b ?x)(?c ?d ?y)))",
             t_implicitJoin,
             "(filter (= ?x ?y) (bgp (?a ?b ?x)(?c ?d ?y)))");
    }
    
    @Test public void implicitJoin5()
    {
        // Safe to transform as although both may be literals we are using sameTerm so already relying on term equality
        test(
             "(filter (sameTerm ?x ?y) (bgp (?a ?b ?x)(?c ?d ?y)))",
             t_implicitJoin,
             "(assign ((?x ?y)) (bgp (?a ?b ?y)(?c ?d ?y)))");
    }
    
    @Test public void implicitJoin6()
    {
        // Not safe to transform as equality on same variable
        test(
             "(filter (= ?x ?x) (bgp (?x ?p ?o)(?y ?p1 ?o1)))",
             t_implicitJoin,
             (String[])null);
    }
    
    @Test public void implicitJoin7()
    {
        test(
             "(filter ((= ?x ?y) (= ?x ?z)) (bgp (?x ?p ?o)(?y ?p1 ?z)))",
             t_implicitJoin,
             (String[])null);
    }
    
    @Test public void implicitJoin8()
    {
        test(
             "(filter (= ?x ?y) (join (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1))))",
             t_implicitJoin,
             "(assign ((?x ?y)) (join (bgp (?y ?p ?o)) (bgp (?y ?p1 ?o1))))");
        
        test(
                "(filter (= ?y ?x) (join (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1))))",
                t_implicitJoin,
                "(assign ((?y ?x)) (join (bgp (?x ?p ?o)) (bgp (?x ?p1 ?o1))))");
    }
    
    @Test public void implicitJoin9()
    {
        // A variable introduced by an assign cannot be considered safe
        test(
             "(filter (= ?x ?y) (assign ((?x <http://constant>)) (bgp (?y ?p ?o))))",
             t_implicitJoin,
             (String[])null);
    }
    
    @Test public void implicitJoin10()
    {
        // A variable not necessarily fixed makes the transform unsafe
        test(
            "(filter (= ?x ?y) (leftjoin (leftjoin (bgp (?s <http://pred> ?o)) (bgp (?x ?p ?o))) (bgp (?y ?p1 ?o1))))",
            t_implicitJoin,
            (String[])null);
    }
    
    @Test public void implictJoin11()
    {
        // Test case related to JENA-500
        // Detect that the expression (= ?prebound ?y) is always 'error' because 
        // ?prebound is undef.  Therefore the whole thing can be removed as there
        // can be no solutions.
        test(
            "(filter (= ?prebound ?y) (extend ((?y (ex:someFunction ?x))) (table unit)))",
            t_implicitJoin,
            "(table empty)");
    }
        
    @Test public void implicitLeftJoin1()
    {
        // Possible to optimize some cases where it's an implicit left join
        
        // Covers the case with one variable on left and other on right
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))))");
        
        // Swapping the order of the equality expression should make no difference
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1)) ((= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))))");
    }
    
    @Test public void implicitLeftJoin2()
    {
        // Possible to optimize some cases where it's an implicit left join
        
        // Covers the case with one variable on left and other on right
        test(
             "(leftjoin (bgp (?y ?p ?o)) (bgp (?x ?p1 ?o1)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?y ?p ?o)) (assign ((?x ?y)) (bgp (?y ?p1 ?o1))))");
        
        // Swapping the order of the equality expression should make no difference
        test(
                "(leftjoin (bgp (?y ?p ?o)) (bgp (?x ?p1 ?o1)) ((= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?y ?p ?o)) (assign ((?x ?y)) (bgp (?y ?p1 ?o1))))");
    }
    
    @Test public void implicitLeftJoin3()
    {
        // Possible to optimize some cases where it's an implicit left join
        
        // Covers the case with one variable on left and both on right
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?x <http://type> ?type)(?y ?p1 ?o1)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x <http://type> ?type)(?x ?p1 ?o1))))");
        
        // Swapping the order of the equality expression should make no difference
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?x <http://type> ?type)(?y ?p1 ?o1)) ((= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x <http://type> ?type)(?x ?p1 ?o1))))");
    }
    
    @Test public void implicitLeftJoin4()
    {
        // Possible to optimize some cases where it's an implicit left join
        
        // Covers the case with one variable on left and both on right
        test(
             "(leftjoin (bgp (?y ?p ?o)) (bgp (?x <http://type> ?type)(?y ?p1 ?o1)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?y ?p ?o)) (assign ((?x ?y)) (bgp (?y <http://type> ?type)(?y ?p1 ?o1))))");
        
        // Swapping the order of the equality expression should make no difference
        test(
                "(leftjoin (bgp (?y ?p ?o)) (bgp (?x <http://type> ?type)(?y ?p1 ?o1)) ((= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?y ?p ?o)) (assign ((?x ?y)) (bgp (?y <http://type> ?type)(?y ?p1 ?o1))))");
    }
    
    @Test public void implicitLeftJoin5()
    {
        // Possible to optimize some cases where it's an implicit left join
        
        // Covers the case of both variables on left and only one on right
        test(
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (bgp (?y ?p1 ?o1)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))))");
        
        // Swapping the order of the equality expression should make no difference
        test(
                "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (bgp (?y ?p1 ?o1)) ((= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))))");
    }
    
    @Test public void implicitLeftJoin6()
    {
        // Possible to optimize some cases where it's an implicit left join
        
        // Covers the case of both variables on left and only one on right
        test(
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (bgp (?x ?p1 ?o1)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (assign ((?x ?y)) (bgp (?y ?p1 ?o1))))");
        
        // Swapping the order of the equality expression should make no difference
        test(
                "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (bgp (?x ?p1 ?o1)) ((= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (assign ((?x ?y)) (bgp (?y ?p1 ?o1))))");
    }
    
    @Test public void implicitLeftJoin7()
    {
        // Possible to optimize some cases where it's an implicit left join
        
        // Covers the case of both variables on both sides
        test(
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (bgp (?x <http://type> ?type)(?y ?p1 ?o1)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (assign ((?x ?y)) (bgp (?y <http://type> ?type)(?y ?p1 ?o1))))");
        
        // Swapping the order of the equality expression will make a difference in this case
        test(
                "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (bgp (?x <http://type> ?type)(?y ?p1 ?o1)) ((= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (assign ((?y ?x)) (bgp (?x <http://type> ?type)(?x ?p1 ?o1))))");
    }
    
    @Test public void implicitLeftJoin8()
    {
        // We don't currently optimize the case where the filter will evaluate to false
        // for all solutions because neither variable in on the RHS
        test(
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (bgp (?a ?b ?c)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             (String[])null);
    }
        
    @Test public void implicitLeftJoin9()
    {
        // && means both conditions must hold so can optimize out the implicit join
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1)) (&& (= ?x ?y) (> ?o1 ?o2)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))) (> ?o1 ?o2))");
        
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1)) (&& (> ?o1 ?o2) (= ?x ?y)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))) (> ?o1 ?o2))");
    }
    
    @Test public void implicitLeftJoin10()
    {
        // Unsafe to optimize
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1)) (|| (= ?x ?y) (> ?o1 ?o2)))",
             t_implicitLeftJoin,
             (String[])null);
    }
    
    @Test public void implicitLeftJoin11()
    {
        // Unsafe to optimize because cannot guarantee that substituted variable is fixed on RHS
        test(
             "(leftjoin (bgp (?x ?p ?o)) (leftjoin (bgp (?y ?p1 ?o1)) (bgp (?x ?p3 ?y))) (= ?x ?y))",
             t_implicitLeftJoin,
             (String[])null);
        
        // Swapping the order of equality expressions should still leave it unsafe
        test(
                "(leftjoin (bgp (?x ?p ?o)) (leftjoin (bgp (?y ?p1 ?o1)) (bgp (?x ?p3 ?y))) (= ?y ?x))",
                t_implicitLeftJoin,
                (String[])null);
    }
    
    @Test public void implicitLeftJoin12()
    {
        // Unlike implicit join overlapping conditions can be safely optimized since the left join
        // optimizer is smart enough to apply the optimizations in an appropriate order
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((= ?x ?y) (= ?x ?z)))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x))))");
        
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((= ?y ?x) (= ?x ?z)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x))))");
        
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((= ?x ?y) (= ?z ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x))))");
    }
    
    @Test public void implicitLeftJoin13()
    {
        // There are some overlapping conditions that are safe to optimize however they may end up introducing
        // additional unnecessary assign operators
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((= ?x ?y) (= ?x ?z) (= ?y ?z)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?z)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x)))))");
        
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((= ?z ?y) (= ?x ?z) (= ?x ?y)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?z ?x) (?y ?x)) (assign ((?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x)))))");
        
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((= ?z ?y) (= ?z ?x) (= ?y ?x)))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?z ?x) (?y ?x)) (assign ((?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x)))))");
    }
    
    @Test public void implicitLeftJoin14()
    {
        // The optimizer is capable of eliminating the && entirely where appropriate
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((&& (= ?x ?y) (= ?x ?z))))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x))))");
    }
    
    @Test public void implicitLeftJoin15()
    {
        // The optimizer is capable of going any depth into nested && to find conditions to apply since it uses ExprList.splitConjunction()
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((&& (&& (= ?x ?y) (> ?o1 10)) (= ?x ?z))))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x))) (> ?o1 10))");
        
        test(
                "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((&& (&& (> ?o1 10) (= ?x ?y)) (= ?x ?z))))",
                t_implicitLeftJoin,
                "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x))) (> ?o1 10))");
    }
    
    @Test public void implicitLeftJoin16()
    {
        // The optimizer is capable of going any depth into nested && to find conditions to apply since it uses ExprList.splitConjunction()
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((&& (&& (< ?o1 20) (&& (= ?x ?y) (> ?o1 10))) (= ?x ?z))))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x))) ((< ?o1 20) (> ?o1 10)))");
    }
    
    @Test public void implicitLeftJoin17()
    {
        // The optimizer is capable of going any depth into nested && to find conditions to apply since it uses ExprList.splitConjunction()
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((&& (&& (= ?x ?y) (> ?o1 10)) (< ?o1 20))))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?z))) ((> ?o1 10) (< ?o1 20)))");
    }
    
    @Test public void implicitLeftJoin18()
    {
        // The optimizer is capable of going any depth into nested && to find conditions to apply since it uses ExprList.splitConjunction()
        test(
             "(leftjoin (bgp (?x ?p ?o)) (bgp (?y ?p1 ?o1) (?y ?p2 ?z)) ((&& (&& (= ?x ?y) (= ?y ?z)) (= ?x ?z))))",
             t_implicitLeftJoin,
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x) (?z ?x)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1) (?x ?p2 ?x)))))");
    }
    
    @Test public void implicitLeftJoin19()
    {
        // Covers the case of both variables on left and neither on right
        test(
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (table unit) ((= ?x ?y)))",
             t_implicitLeftJoin,
             (String[])null);
        
        // Swapping the order of the equality expression should make no difference
        test(
             "(leftjoin (bgp (?x ?p ?o)(?x <http://pred> ?y)) (table unit) ((= ?y ?x)))",
             t_implicitLeftJoin,
             (String[])null);
    }
    
    @Test public void implicitLeftJoin20()
    {
        // Covers the case of one variable on left and neither on right
        test(
             "(leftjoin (bgp (?x ?p ?o)) (table unit) ((= ?x ?y)))",
             t_implicitLeftJoin,
             (String[])null);
        
        // Swapping the order of the equality expression should make no difference
        test(
             "(leftjoin (bgp (?x ?p ?o)) (table unit) ((= ?y ?x)))",
             t_implicitLeftJoin,
             (String[])null);
    }
    
    @Test public void implicitLeftJoin21()
    {
        // Covers the case of one variable on left and neither on right
        test(
             "(leftjoin (bgp (?y ?p ?o)) (table unit) ((= ?x ?y)))",
             t_implicitLeftJoin,
             (String[])null);
        
        // Swapping the order of the equality expression should make no difference
        test(
             "(leftjoin (bgp (?y ?p ?o)) (table unit) ((= ?y ?x)))",
             t_implicitLeftJoin,
             (String[])null);
    }
    
    @Test public void implicitLeftJoin22()
    {
        // Covers the case of no variables actually being relevant
        test(
             "(leftjoin (bgp (?s ?p ?o)) (table unit) ((= ?x ?y)))",
             t_implicitLeftJoin,
             (String[])null);
        
        // Swapping the order of the equality expression should make no difference
        test(
             "(leftjoin (bgp (?s ?p ?o)) (table unit) ((= ?y ?x)))",
             t_implicitLeftJoin,
             (String[])null);
    }
    
    @Test public void implicitLeftJoin23()
    {
        // Covers the case of neither variable on left and both on right
        test(
             "(leftjoin (table unit) (bgp (?x ?p ?o)(?x <http://pred> ?y)) ((= ?x ?y)))",
             t_implicitLeftJoin,
             "(leftjoin (table unit) (assign ((?x ?y)) (bgp (?y ?p ?o)(?y <http://pred> ?y))))");
        
        // Swapping the order of the equality will make a difference in this case
        test(
             "(leftjoin (table unit) (bgp (?x ?p ?o)(?x <http://pred> ?y)) ((= ?y ?x)))",
             t_implicitLeftJoin,
             "(leftjoin (table unit) (assign ((?y ?x)) (bgp (?x ?p ?o)(?x <http://pred> ?x))))");
    }
    
    @Test public void implicitLeftJoinConditional1()
    {
        // Can be optimized because not all assigns block linearization
        test(
             "(leftjoin (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))))",
             new TransformJoinStrategy(),
             "(conditional (bgp (?x ?p ?o)) (assign ((?y ?x)) (bgp (?x ?p1 ?o1))))");
    }
            
    public static void test(String input, Transform transform, String... output)
    {
        Op op1 = SSE.parseOp(input) ;
        Op op2 = Transformer.transform(transform, op1) ;
        if ( output == null )
        {
            // No transformation.
            Assert.assertEquals(op1, op2) ;
            return ;
        }
        
        Op op3 = SSE.parseOp(StrUtils.strjoinNL(output)) ;
        Assert.assertEquals(op3, op2) ;
    }
        
}
