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

package com.hp.hpl.jena.sparql.algebra.optimize;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestTransformMergeBGPs extends BaseTest
{
    
    String pre = "(prefix ((: <http://example/>))" ;
    String post =  ")" ;
    
    @Test public void collapse_01() { test("(bgp (:x :p  :z))") ; }
    @Test public void collapse_02() { test("(table unit)") ; }

    // Join.
    @Test public void collapse_10() { test("(join (bgp (:x :p :z1)) (table unit) )") ; } 
    @Test public void collapse_11() { test("(join (table unit) (bgp (:x :p :z1)) )") ; } 
                                           
    @Test public void collapse_12() { 
        test("(join (bgp (:x :p :z1)) (bgp (:x :p :z2)) )", 
             "(bgp (:x :p :z1) (:x :p :z2))") ; }
    
    @Test public void collapse_13() { 
        test("(join (bgp (:x :p :z1)) (join (bgp (:x :p :z2)) (bgp (:x :p :z3))) )", 
             "(bgp (:x :p :z1) (:x :p :z2) (:x :p :z3) )") ; }

    @Test public void collapse_14() { 
        test("(join (join (bgp (:x :p :z1)) (bgp (:x :p :z2))) (bgp (:x :p :z3)) )", 
             "(bgp (:x :p :z1) (:x :p :z2) (:x :p :z3) )") ; }
    
    // Sequence
    @Test public void collapse_20() { test("(sequence (bgp (:x :p :z1)) (bgp (:x :p :z2)) )", 
                                           "(bgp (:x :p :z1) (:x :p :z2))") ; }
    
    @Test public void collapse_21() { test("(sequence (bgp (:x :p :z1)) (bgp (:x :p :z2)) (bgp (:x :p :z3)) )", 
        "(bgp (:x :p :z1) (:x :p :z2) (:x :p :z3))") ; }
    
    @Test public void collapse_22() { test("(sequence (table unit) (bgp (:x :p :z1)) (bgp (:x :p :z3)) )",
                                           "(sequence (table unit) (bgp (:x :p :z1) (:x :p :z3)) )") ; } 
    @Test public void collapse_23() { test("(sequence (bgp (:x :p :z1)) (table unit) (bgp (:x :p :z3)) )") ; } 
    
    @Test public void collapse_24() { test("(sequence (bgp (:x :p :z1)) (bgp (:x :p :z3)) (table unit) )",
                                           "(sequence (bgp (:x :p :z1) (:x :p :z3)) (table unit) )") ; } 
    
    // Compound
    @Test public void collapse_30() { test("(join "+
                                            "(sequence (bgp (:x :p :z1)) (bgp (:x :p :z2)) (bgp (:x :p :z3)) )"+
                                            "(sequence (bgp (:x :p :z4)) (bgp (:x :p :z5)))"+
                                            ")",
                                            "(bgp (:x :p :z1) (:x :p :z2) (:x :p :z3) (:x :p :z4) (:x :p :z5) )") ; }
    
    @Test public void collapse_31() { test("(join "+
                                              "(table unit)"+
                                              "(sequence (bgp (:x :p :z4)) (bgp (:x :p :z5)))"+
                                            ")",
                                            "(join (table unit) (bgp (:x :p :z4) (:x :p :z5)))") ; }
    
    @Test public void collapse_32() { test("(join "+
                                            "(sequence (bgp (:x :p :z4)) (bgp (:x :p :z5)))"+
                                            "(table unit)"+
                                           ")",
                                           "(join (bgp (:x :p :z4) (:x :p :z5)) (table unit) )") ; }
    

    // Input = output
    private void test(String input)
    {
        test(input, input) ;
    }

    private void test(String input, String output)
    {
        input = pre+input+post ;
        output = pre+output+post ;
        
        Op op1 = SSE.parseOp(input) ;
        //op1 = Transformer.transform(new TransformPathFlattern(), op1) ;
        Op op3 = Transformer.transform(new TransformMergeBGPs(), op1) ;
        Op expected = SSE.parseOp(output) ;
        assertEquals(expected, op3) ;
    }
}
