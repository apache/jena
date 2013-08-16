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

package com.hp.hpl.jena.sparql.syntax;


import junit.framework.TestCase ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel ;
import com.hp.hpl.jena.sparql.algebra.op.OpNull ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.expr.E_IsNumeric;
import com.hp.hpl.jena.sparql.expr.E_SameTerm;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderNode ;

public class TestSSE_Builder extends TestCase
{
    @Test public void test_01() { SSE.parseTriple("[triple ?s ?p ?o]") ; }
    @Test public void test_02() { SSE.parseTriple("[?s ?p ?o]") ; }
    @Test public void test_03() { SSE.parseTriple("[?s ?p ?o]") ; }
    @Test public void test_04() { SSE.parseTriple("(?s ?p ?o)") ; }
    @Test public void test_05() { SSE.parseQuad("(_ ?s ?p ?o)") ; }
    @Test public void test_06() { SSE.parseQuad("(quad _ ?s ?p ?o)") ; }
    
    @Test public void test_07() { SSE.parseExpr("1") ; }
    @Test public void test_08() { SSE.parseExpr("(+ 1 2)") ; }
    
    @Test public void testOp_01() { opSame("(null)") ; }
    @Test public void testOp_02() { opSame("(null)", OpNull.create()) ; }
    @Test public void testOp_03() { opSame("(bgp [triple ?s ?p ?o])") ; }

    @Test public void testOp_04() { opSame("(label 'ABC' (table unit))", OpLabel.create("ABC", OpTable.unit())) ; }
    
    private static void opSame(String str) {
        opSame(str, SSE.parseOp(str)) ;
    }

    private static void opSame(String str, Op other) {
        Op op = SSE.parseOp(str) ;
        assertEquals(op, other) ;
    }

    @Test
    public void testBuildInt_01() {
        Item item = SSE.parseItem("1") ;
        int i = BuilderNode.buildInt(item) ;
        assertEquals(1, i) ;
    }

    @Test
    public void testBuildInt_02() {
        Item item = SSE.parseItem("1") ;
        int i = BuilderNode.buildInt(item, 23) ;
        assertEquals(1, i) ;
    }

    @Test
    public void testBuildInt_03() {
        Item item = SSE.parseItem("_") ;
        int i = BuilderNode.buildInt(item, 23) ;
        assertEquals(23, i) ;
    }

    @Test
    public void testBuildLong_01() {
        Item item = SSE.parseItem("100000000000") ;
        long i = BuilderNode.buildLong(item) ;
        assertEquals(100000000000L, i) ;
    }

    @Test
    public void testBuildLong_02() {
        Item item = SSE.parseItem("100000000000") ;
        long i = BuilderNode.buildLong(item, 23) ;
        assertEquals(100000000000L, i) ;
    }

    @Test
    public void testBuildLong_03() {
        Item item = SSE.parseItem("_") ;
        long i = BuilderNode.buildLong(item, 23) ;
        assertEquals(23, i) ;
    }

    @Test
    public void testBuildExpr_01() {
        Expr e = SSE.parseExpr("(sameTerm (?x) (?y))") ;
        assertTrue(e instanceof E_SameTerm) ;
    }

    @Test
    public void testBuildExpr_02() {
        Expr e = SSE.parseExpr("(isNumeric ?x)") ;
        assertTrue(e instanceof E_IsNumeric) ;
    }
    
    private static void testExprForms(String str1, String str2) {
        Expr e1 = SSE.parseExpr(str1) ;
        Expr e2 = SSE.parseExpr(str2) ;
        assertEquals(str1+" "+str2, e1, e2) ;
    }
    
    @Test
    public void testBuildExpr_03()  { 
        testExprForms("(add ?x ?y)",
                      "(+ ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_04() {
        testExprForms("(subtract ?x ?y)",
                      "(- ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_05() {
        testExprForms("(multiply ?x ?y)",
                      "(* ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_06() {
        testExprForms("(divide ?x ?y)", 
                      "(/ ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_07() {
        testExprForms("(lt ?x ?y)", 
                      "(< ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_08() {
        testExprForms("(le ?x ?y)", 
                      "(<= ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_09() {
        testExprForms("(gt ?x ?y)", 
                      "(> ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_10() {
        testExprForms("(ge ?x ?y)", 
                      "(>= ?x ?y)") ;
    }
    
    @Test
    public void testBuildExpr_11() {
        testExprForms("(unaryplus ?x)", 
                      "(+ ?x)") ;
    }

    @Test
    public void testBuildExpr_12() {
        testExprForms("(unaryminus ?x)", 
                      "(- ?x)") ;
    }

    @Test
    public void testBuildExpr_13() {
        testExprForms("(eq ?x ?y)", 
                      "(= ?x ?y)") ;
    }

    @Test
    public void testBuildExpr_14() {
        testExprForms("(ne ?x ?y)", 
                      "(!= ?x ?y)") ;
    }

}
