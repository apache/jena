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

import static org.apache.jena.atlas.lib.StrUtils.strjoinNL;
import static org.junit.Assert.assertEquals;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathCompiler;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTransformPathFlatten {
    private static String pre = "(prefix ((: <http://example/>))" ;
    private static String post =  ")" ;
    
    private static Prologue prologue;

    @BeforeClass public static void beforeClass() {
        prologue = new Prologue();
        prologue.getPrefixMapping().setNsPrefix("", "http://example/");
    }


    @Before public void before() {
        // Reset the variable allocator. 
        PathCompiler.resetForTest();
    }
    
    @Test public void pathFlatten_00() {
        Op op1 = path(":x0", ":p0", ":T0");
        Op op2 = op("(bgp (triple :x0 :p0 :T0))");
        test(op1, op2);
    }
    
    @Test public void pathFlatten_01() {
        Op op1 = path(":x1", ":q1/:p1*", ":T1");
        Op op2 = op("(sequence"
                   ,"  (bgp (triple :x1 :q1 ??P0))"
                   ,"  (path ??P0 (path* :p1) :T1))"
                   );
        test(op1, op2);
    }
    
    // JENA-1918 : order of sequence is grounded first.
    @Test public void pathFlatten_02() { 
        Op op1 = path("?x", ":q1/:p1*", ":T1");
        Op op2 = op("(sequence"
                   ,"  (path ??P0 (path* :p1) :T1)"
                   ,"  (bgp (triple ?x :q1 ??P0)) )"
                   );
        test(op1, op2);
    }

    // JENA-1918 : order of sequence is grounded first.
    @Test public void pathFlatten_10() { 
        Op op1 = path("?x", ":p1{2}", ":T1");
        Op op2 = op("(bgp"
            ,"  (triple ?x :p1 ??P0)"
            ,"  (triple ??P0 :p1 :T1)"
            ,")"
          );
        test(op1, op2);
    }

    @Test public void pathFlatten_11() { 
        Op op1 = path("?x", ":p1{2,}", ":T1");
        Op op2 = op
            ("(sequence"
            ,"    (path ??P0 (pathN* :p1) :T1)"
            ,"    (bgp"
            ,"      (triple ?x :p1 ??P1)"
            ,"      (triple ??P1 :p1 ??P0)"
            ,"   ))");
        test(op1, op2);
    }
    
    private static Op path(String s, String pathStr, String o) {
        Path path = PathParser.parse(pathStr, prologue);
        TriplePath tp = new TriplePath(SSE.parseNode(s), path, SSE.parseNode(o));
        return new OpPath(tp);
    }
    
    private static Op op(String...opStr) {
        String s = strjoinNL(opStr);
        String input = pre + s + post;
        return SSE.parseOp(input);
    }
    
    private static void test(Op opInput, Op opExpected) {
        Op op = Transformer.transform(new TransformPathFlattern(), opInput);
        if ( opExpected == null ) {
            System.out.print(opInput);
            System.out.println("  ==>");
            System.out.print(op);
            System.out.println();
            return;
        }
        
        assertEquals(opExpected, op);
    }
}
