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
    
    private static void opSame(String str)
    {
        opSame(str, SSE.parseOp(str)) ;
    }
    
    private static void opSame(String str , Op other)
    {
        Op op = SSE.parseOp(str) ;
        assertEquals(op, other) ;
    }
    
    @Test public void testBuildInt_01()
    { 
        Item item = SSE.parseItem("1") ;
        int i = BuilderNode.buildInt(item) ;
        assertEquals(1, i) ;
    }

    @Test public void testBuildInt_02()
    { 
        Item item = SSE.parseItem("1") ;
        int i = BuilderNode.buildInt(item, 23) ;
        assertEquals(1, i) ;
    }

    @Test public void testBuildInt_03()
    { 
        Item item = SSE.parseItem("_") ;
        int i = BuilderNode.buildInt(item, 23) ;
        assertEquals(23, i) ;
    }
    
    @Test public void testBuildLong_01()
    { 
        Item item = SSE.parseItem("100000000000") ;
        long i = BuilderNode.buildLong(item) ;
        assertEquals(100000000000L, i) ;
    }

    @Test public void testBuildLong_02()
    { 
        Item item = SSE.parseItem("100000000000") ;
        long i = BuilderNode.buildLong(item, 23) ;
        assertEquals(100000000000L, i) ;
    }

    @Test public void testBuildLong_03()
    { 
        Item item = SSE.parseItem("_") ;
        long i = BuilderNode.buildLong(item, 23) ;
        assertEquals(23, i) ;
    }

}
