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

package com.hp.hpl.jena.sparql.algebra.optimize ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestTransformFilterPlacement extends BaseTest { //extends AbstractTestTransform {
    
    // ** Filter
    
    @Test public void place_bgp_01() {
        test("(filter (= ?x 1) (bgp ( ?s ?p ?x)))", "(filter (= ?x 1) (bgp ( ?s ?p ?x)))") ;
    }

    @Test public void place_bgp_02() {
        test("(filter (= ?x 1) (bgp (?s ?p ?x) (?s1 ?p1 ?x1) ))",
             "(sequence (filter (= ?x 1) (bgp ( ?s ?p ?x))) (bgp (?s1 ?p1 ?x1)))") ;
    }

    @Test public void place_bgp_03() {
        test("(filter (= ?x 1) (bgp (?s ?p ?x) (?s1 ?p1 ?x) ))",
             "(sequence (filter (= ?x 1) (bgp ( ?s ?p ?x))) (bgp (?s1 ?p1 ?x)))") ;
    }

    @Test public void place_bgp_03a() {
        testNoBGP("(filter (= ?x 1) (bgp (?s ?p ?x) (?s1 ?p1 ?x) ))",
             null) ;
    }

    @Test public void place_bgp_04() {
        test("(filter (= ?XX 1) (bgp (?s ?p ?x) (?s1 ?p1 ?XX) ))", "(filter (= ?XX 1) (bgp (?s ?p ?x) (?s1 ?p1 ?XX) ))") ;
    }
    
    @Test public void place_bgp_05() {
        test("(filter (= ?x 123) (bgp (?s ?p ?x) (?s ?p ?x1) (?s ?p ?x2)) )",
             "(sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x1) (?s ?p ?x2)) )") ;
    }

    @Test public void place_bgp_05a() {
        // Won't push and break up the BGP
        testNoBGP("(filter (= ?x 123) (bgp (?s ?p ?x) (?s ?p ?x1) (?s ?p ?x2)) )",
                null) ;
    }

    @Test public void place_no_match_01() {
        // Unbound
        test("(filter (= ?x ?unbound) (bgp (?s ?p ?x)))", null) ;
    }

    @Test public void place_no_match_02() {
        test("(filter (= ?x ?unbound) (bgp (?s ?p ?x) (?s ?p ?x)))", null) ;
    }

    @Test public void place_no_match_03() {
        test("(filter (= ?x ?unbound) (bgp (?s ?p ?x) (?s1 ?p1 ?XX)))", null) ;
    }

    @Test public void place_sequence_01() {
        test("(filter (= ?x 123) (sequence (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             "(sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?z)) )") ;
    }

    @Test public void place_sequence_02() {
        // Given the sequence flows left into right, only need to filter in the
        // LHS.  The RHS can't introduce ?x because it would not be a legal sequence
        // if, for example, it had a BIND in it.
        test("(filter (= ?x 123) (sequence (bgp (?s ?p ?x)) (bgp (?s ?p ?x)) ))",
             "(sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x)) )") ;
    }

    @Test public void place_sequence_03() {
        test("(filter (= ?x 123) (sequence  (bgp (?s ?p ?x)) (bgp (?s ?p ?x1)) (bgp (?s ?p ?x2)) ))",
             "(sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x1)) (bgp (?s ?p ?x2)) )") ;
    }

    @Test public void place_sequence_04() {
        test("(filter (= ?x 123) (sequence (bgp (?s ?p ?x1)) (bgp (?s ?p ?x)) (bgp (?s ?p ?x2)) ))",
             "(sequence (bgp (?s ?p ?x1)) (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x2)) )") ;
    }

    @Test public void place_sequence_04a() {
        testNoBGP("(filter (= ?x 123) (sequence (bgp (?s ?p ?x1)) (bgp (?s ?p ?x)) (bgp (?s ?p ?x2)) ))",
                "(sequence (bgp (?s ?p ?x1)) (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x2)))") ;
    }

    @Test public void place_sequence_05() {
        test("(filter (= ?x 123) (sequence (bgp (?s ?p ?x) (?s ?p ?x1)) (bgp (?s ?p ?x2)) ))",
            "(sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x1)) (bgp (?s ?p ?x2)) )") ;
    }

    @Test public void place_sequence_05a() {
        testNoBGP("(filter (= ?x 123) (sequence (bgp (?s ?p ?x) (?s ?p ?x1)) (bgp (?s ?p ?x2)) ))",
                "(sequence (filter (= ?x 123) (bgp (?s ?p ?x) (?s ?p ?x1))) (bgp (?s ?p ?x2)))") ;
    }


    @Test public void place_sequence_06() {
        test("(filter (= ?x 123) (sequence (bgp (?s ?p ?x1) (?s ?p ?x2)) (bgp (?s ?p ?x)) ))",
             // If push filter to last element ... which is of no benefit
            //"(sequence (bgp (?s ?p ?x1) (?s ?p ?x2)) (filter (= ?x 123) (bgp (?s ?p ?x))) )") ;
             null) ;
    }

    @Test public void place_sequence_07() {
        test("(filter (= ?A 123) (sequence (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             null) ;
    }

    @Test public void place_sequence_08() {
        test("(sequence (bgp (?s ?p ?x)) (filter (= ?z 123) (bgp (?s ?p ?z))) )",
             null) ;
    }
    
    @Test public void place_sequence_09() {
        test("(filter (= ?x 123) (sequence (bgp (?s ?p ?x1) (?s ?p ?x)) (bgp (?s ?p ?x2)) ))",
             "(sequence (filter (= ?x 123) (bgp (?s ?p ?x1) (?s ?p ?x))) (bgp (?s ?p ?x2)) )") ;
    }

    @Test public void place_sequence_09a() {
        testNoBGP("(filter (= ?x 123) (sequence (bgp (?s ?p ?x1) (?s ?p ?x)) (bgp (?s ?p ?x2)) ))",
                "(sequence (filter (= ?x 123) (bgp (?s ?p ?x1) (?s ?p ?x))) (bgp (?s ?p ?x2)) )") ;
    }
    
    // Join : one sided push.
    @Test public void place_join_01() {
        test("(filter (= ?x 123) (join (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             "(join (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?z)) )") ;
    }

    // Join : two side push
    @Test public void place_join_02() {
        test("(filter (= ?x 123) (join (bgp (?s ?p ?x)) (bgp (?s ?p ?x)) ))",
             "(join  (filter (= ?x 123) (bgp (?s ?p ?x))) (filter (= ?x 123) (bgp (?s ?p ?x))) )") ;
    }

    @Test public void place_join_03() {
        String x = StrUtils.strjoinNL
            ("(filter ((= 13 14) (> ?o1 12) (< ?o 56) (< (+ ?o ?o1) 999))",
             "   (join", 
             "      (bgp (triple ?s ?p ?o))" ,
             "      (bgp (triple ?s ?p1 ?o1))))") ;

        // Everything pushed down once. 
        String y = StrUtils.strjoinNL
            ("(filter (< (+ ?o ?o1) 999)",
             "  (join",
             "    (filter ((= 13 14) (< ?o 56))", 
             "      (bgp (triple ?s ?p ?o)))", 
             "    (filter ((= 13 14) (> ?o1 12))", 
             "      (bgp (triple ?s ?p1 ?o1)))))") ;
        // Recursive push in - causes (= 13 14) to go into BGP
        String y1 = StrUtils.strjoinNL
            ("(filter (< (+ ?o ?o1) 999)",
             "  (join",
             "  (filter (< ?o 56)",
             "    (sequence",
             "      (filter (= 13 14)",
             "        (table unit))",
             "      (bgp (triple ?s ?p ?o))))",
             "  (filter (> ?o1 12)",
             "    (sequence",
             "      (filter (= 13 14)",
             "        (table unit))",
             "      (bgp (triple ?s ?p1 ?o1))))",
             "   ))") ;
        test(x, y1) ;
    }


    @Test public void place_conditional_01() {
        test("(filter (= ?x 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             "(conditional (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?z)) )") ;
    }

    @Test public void place_conditional_02() {
        test("(filter (= ?z 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             "(filter (= ?z 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))") ;
    }

    @Test public void place_conditional_03() {
        test("(filter (= ?x 123) (conditional (bgp (?s ?p ?x)) (bgp (?s ?p ?x)) ))",
             "(conditional (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x)) )") ;
    }

    @Test public void place_leftjoin_01() {
        // conditional
        test("(filter (= ?x 123) (leftjoin (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             "(leftjoin (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?z)) )") ;
    }

    @Test public void place_leftjoin_02() {
        // conditional
        test("(filter (= ?z 123) (leftjoin (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))",
             "(filter (= ?z 123) (leftjoin (bgp (?s ?p ?x)) (bgp (?s ?p ?z)) ))") ;
    }

    @Test public void place_leftjoin_03() {
        // conditional
        test("(filter (= ?x 123) (leftjoin (bgp (?s ?p ?x)) (bgp (?s ?p ?x)) ))",
             "(leftjoin (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?x)) )") ;
    }

    @Test public void place_project_01() {
        test("(filter (= ?x 123) (project (?x) (bgp (?s ?p ?x)) ))",
             "(project (?x) (filter (= ?x 123) (bgp (?s ?p ?x)) ))") ;
    }

    @Test public void place_project_02() {
        test("(filter (= ?x 123) (project (?s) (bgp (?s ?p ?x)) ))",
             null) ;
    }
    
    @Test public void place_project_03() {
        test("(filter (= ?x 123) (project (?x) (bgp (?s ?p ?x) (?s ?p ?z) ) ))",
             "(project (?x) (sequence (filter (= ?x 123) (bgp (?s ?p ?x)) ) (bgp (?s ?p ?z))) )") ;
    }

    @Test public void place_extend_01() {
        test("(filter (= ?x 123) (extend ((?z 123)) (bgp (?s ?p ?x)) ))",
             "(extend ((?z 123)) (filter (= ?x 123) (bgp (?s ?p ?x)) ))") ;
    }
    
    @Test public void place_extend_02() {
        test("(filter ((= ?x1 123) (= ?x2 456)) (extend (?z 789) (bgp (?s ?p ?x1)) ))",
             "(filter (= ?x2 456) (extend (?z 789) (filter (= ?x1 123) (bgp (?s ?p ?x1)) )))") ;
    }
    
    @Test public void place_extend_03() { // Blocked
        test("(filter (= ?x 123) (extend ((?x 123)) (bgp (?s ?p ?z)) ))",
             null) ;
    }

    @Test public void place_extend_04() {
        test("(filter (= ?x 123) (extend ((?x1 123)) (filter (< ?x 456) (bgp (?s ?p ?x) (?s ?p ?z))) ))",
             "(extend (?x1 123) (sequence (filter ((= ?x 123) (< ?x 456)) (bgp (?s ?p ?x))) (bgp (?s ?p ?z))) )") ;
    }

    @Test public void place_extend_05() {
        // Filter further out than one place. 
    	test("(filter (= ?z 1) (sequence (extend (?x1 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?z))))",
    	     null) ;
    }

    @Test public void place_extend_06() {
        // Filter further out than one place. 
        test("(filter (= ?z 1) (join (extend (?x1 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?z))))" ,
             "(join (extend (?x1 123) (bgp (?s ?p ?x))) (filter (= ?z 1) (bgp (?s ?p ?z))) )") ;
    }

    @Test public void place_assign_01() {
        test("(filter (= ?x 123) (assign ((?z 123)) (bgp (?s ?p ?x)) ))",
             "(assign ((?z 123)) (filter (= ?x 123) (bgp (?s ?p ?x)) ))") ;
    }
    
    @Test public void place_assign_02() {
        test("(filter ((= ?x1 123) (= ?x2 456)) (assign (?z 789) (bgp (?s ?p ?x1)) ))",
             "(filter (= ?x2 456) (assign (?z 789) (filter (= ?x1 123) (bgp (?s ?p ?x1)) )))") ;
    }
    
    @Test public void place_assign_03() { // Blocked
        test("(filter (= ?x 123) (assign ((?x 123)) (bgp (?s ?p ?z)) ))",
             null) ;
    }

    @Test public void place_assign_04() {
        // Caution - OpFilter equality is sensitive to the order of expressions 
        test("(filter (= ?x 123) (assign ((?x1 123)) (filter (< ?x 456) (bgp (?s ?p ?x) (?s ?p ?z))) ))",
             "(assign (?x1 123) (sequence (filter ((= ?x 123) (< ?x 456)) (bgp (?s ?p ?x))) (bgp (?s ?p ?z))) )") ;
    }
    
    @Test public void place_assign_05() {
        // Even with No BGP we can still wrap a BGP without breaking it
        testNoBGP("(filter (= ?x 123) (assign ((?z 123)) (bgp (?s ?p ?x)) ))",
             "(assign ((?z 123)) (filter (= ?x 123) (bgp (?s ?p ?x)) ))") ;
    }
    
    @Test public void place_assign_06() {
        test("(filter (= ?x 123) (assign ((?z 123)) (bgp (?s ?p ?x) (?s ?p ?x1) )))",
             "(assign ((?z 123)) (sequence (filter (= ?x 123) (bgp (?s ?p ?x)) ) (bgp (?s ?p ?x1)) ) )") ;
    }
    
    @Test public void place_assign_06a() {
        // With No BGP we won't break up the BGP but we will still push the filter down
        testNoBGP("(filter (= ?x 123) (assign ((?z 123)) (bgp (?s ?p ?x) (?s ?p ?x1) )))",
             "(assign ((?z 123)) (filter (= ?x 123) (bgp (?s ?p ?x) (?s ?p ?x1)) ) )") ;
    }

    @Test public void place_filter_01() {
        test("(filter (= ?x 123) (filter (= ?y 456) (bgp (?s ?p ?x) (?s ?p ?y)) ))" , 
             "(filter (= ?y 456) (sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?y)) ))" ) ;
    }

    @Test public void place_filter_02() {
        test("(filter (= ?x 123) (filter (= ?y 456) (bgp (?s ?p ?x) (?s ?p ?y) (?s ?p ?z) )))" , 
             "(sequence (filter (= ?y 456) (sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?y)))) (bgp (?s ?p ?z)))") ;
    }

    @Test public void place_union_01() {
        test("(filter (= ?x 123) (union (bgp (?s ?p ?x) (?s ?p ?y)) (bgp (?s ?p ?z)  (?s1 ?p1 ?x)) ))",
             "(union  (sequence (filter (= ?x 123) (bgp (?s ?p ?x))) (bgp (?s ?p ?y))) "+
                      "(filter (= ?x 123) (bgp (?s ?p ?z)  (?s1 ?p1 ?x)) ))") ;
    }
    
    @Test public void place_union_02() {
        test("(filter 1 (union (bgp (triple ?s ?p ?o)) (filter 0 (table unit))) )",
             "(union (sequence (filter 1 (table unit)) (bgp (triple ?s ?p ?o))) (filter (exprlist 1 0) (table unit)))");
    }
    
    @Test public void place_union_02a() {
        testNoBGP("(filter 1 (union (bgp (triple ?s ?p ?o)) (filter 0 (table unit))) )",
             "(union (filter 1 (bgp (triple ?s ?p ?o))) (filter (exprlist 1 0) (table unit)))");
    }
    
    @Test public void place_union_03() {
        test("(slice _ 1 (project (?s ?p ?o) (filter 1 (union (bgp (?s ?p ?o)) (filter 0 (table unit))))))",
             "(slice _ 1 (project (?s ?p ?o) (union (sequence (filter 1 (table unit)) (bgp (?s ?p ?o))) (filter (exprlist 1 0) (table unit)))))");
    }
    
    @Test public void place_union_03a() {
        testNoBGP("(slice _ 1 (project (?s ?p ?o) (filter 1 (union (bgp (?s ?p ?o)) (filter 0 (table unit))))))",
             "(slice _ 1 (project (?s ?p ?o) (union (filter 1 (bgp (?s ?p ?o))) (filter (exprlist 1 0) (table unit)))))");
    }
    
    @Test public void place_union_04() {
        test("(filter (= 1 1) (union (bgp (triple ?s ?p ?o)) (filter (!= 0 0) (table unit))))",
             "(union (sequence (filter (= 1 1) (table unit)) (bgp (triple ?s ?p ?o))) (filter (exprlist (= 1 1) (!= 0 0)) (table unit)))");
    }
    
    @Test public void place_union_04a() {
        testNoBGP("(filter (= 1 1) (union (bgp (triple ?s ?p ?o)) (filter (!= 0 0) (table unit))))",
             "(union (filter (= 1 1) (bgp (triple ?s ?p ?o))) (filter (exprlist (= 1 1) (!= 0 0)) (table unit)))");
    }
        
    public static void test(String input, String output) {
        test$(input, output, true) ;
    }

    public static void testNoBGP(String input , String output ) {
        test$(input, output, false) ;
    }
        
    public static void test$(String input, String output, boolean includeBGPs) {
        Transform t_placement = new TransformFilterPlacement(includeBGPs) ;
        Op op1 = SSE.parseOp(input) ;
        Op op2 = Transformer.transform(t_placement, op1) ;
        if ( output == null ) {
            // No transformation.
            Assert.assertEquals(op1, op2) ;
            return ;
        }

        Op op3 = SSE.parseOp(output) ;
        Assert.assertEquals(op3, op2) ;
        
    }
}
