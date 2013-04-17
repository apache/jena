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

import org.junit.Assert ;
import junit.framework.JUnit4TestAdapter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformExpandOneOf ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterDisjunction ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterEquality ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterPlacement ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Tests of transforms related to filters */
public class TestTransformFilters
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestTransformFilters.class) ;
    }
    
    private Transform t_equality    = new TransformFilterEquality() ;
    private Transform t_disjunction = new TransformFilterDisjunction() ;
    private Transform t_placement   = new TransformFilterPlacement() ;
    private Transform t_expandOneOf = new TransformExpandOneOf() ;
    
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
    
    // There is an expression we can't do anything about.
    @Test public void disjunction05()
    {
        test("(filter (exprlist (|| (= ?x <y>) (!= ?x <x>)) (lang ?x))    (bgp ( ?s ?p ?x)) )",
             t_disjunction,
//             "(filter (lang ?x)", 
//             "  (disjunction",
//             "    (assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
//             "    (filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
//             "))"
             "(filter (exprlist (|| (= ?x <y>) (!= ?x <x>)) (lang ?x))    (bgp ( ?s ?p ?x)) )"
        ) ;
    }
    
    // There is an expression we can't do anything about.
    @Test public void disjunction06()
    {
        test("(filter (exprlist (lang ?x) (|| (= ?x <y>) (!= ?x <x>)))    (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(filter (exprlist (lang ?x) (|| (= ?x <y>) (!= ?x <x>)))    (bgp ( ?s ?p ?x)) )"
//             "(filter (lang ?x)", 
//             "  (disjunction",
//             "    (assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
//             "    (filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
//             "))"
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
