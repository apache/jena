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

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathCompiler;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTransformPathFlatten {
    private static String pre = "(prefix ((: <http://example/>))";
    private static String post =  ")";
    
    private static Prologue prologue;

    @BeforeClass public static void beforeClass() {
        prologue = new Prologue();
        prologue.getPrefixMapping().setNsPrefix("", "http://example/");
    }


    @Before public void before() {
        // Reset the variable allocator. 
        PathCompiler.resetForTest();
        TransformPathFlattenAlgebra.resetForTest();
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

    @Test public void pathFlatten_alt_01() {
        Op op1 = path("?x", ":p1|:p2", ":T1");
        // Basic flatten does not flatten alternative paths
        test(op1, op1);
    }

    @Test public void pathFlatten_alt_02() {
        Op op1 = path("?x", ":p1|:p2", ":T1");
        // Extended flatten does flatten alternative paths
        Op expected = op(
                "(union",
                        "  (triple ?x :p1 :T1)",
                        "  (triple ?x :p2 :T1)",
                        ")");
        testAlgebra(op1, expected);
    }

    @Test public void pathFlatten_alt_03() {
        Op op1 = path("?x", ":p1|^:p2", ":T1");
        // Extended flatten does flatten alternative paths
        Op expected = op(
                "(union",
                "  (triple ?x :p1 :T1)",
                "  (triple :T1 :p2 ?x)",
                ")");
        testAlgebra(op1, expected);
    }

    @Test public void pathFlatten_alt_04() {
        Op op1 = path("?x", ":p1|:p2|(:p3*)", ":T1");
        // Extended flatten does flatten alternative paths
        Op expected = op(
                "(union",
                "  (union",
                "    (triple ?x :p1 :T1)",
                "    (triple ?x :p2 :T1))",
                "  (path ?x (path* :p3) :T1)",
                ")");
        testAlgebra(op1, expected);
    }

    @Test public void pathFlatten_alt_05() {
        Op op1 = path("?x", ":p1|:p2|(:p3{2})", ":T1");
        // Extended flatten does flatten alternative paths
        Op expected = op(
                "(union",
                "  (union",
                "    (triple ?x :p1 :T1)",
                "    (triple ?x :p2 :T1))",
                "  (join",
                "    (triple ?x :p3 ??Q0)",
                "    (triple ??Q0 :p3 :T1))",
                ")");
        testAlgebra(op1, expected);
    }

    @Test public void pathFlatten_alt_05b() {
        Op op1 = path("?x", ":p1|:p2|(:p3{2})", ":T1");
        Op expected = op(
                "(union",
                "  (union",
                "    (triple ?x :p1 :T1)",
                "    (triple ?x :p2 :T1))",
                "  (bgp",
                "    (triple ?x :p3 ??Q0)",
                "    (triple ??Q0 :p3 :T1))",
                ")"
        );
        Context ctx = new Context();
        ctx.set(ARQ.optPathFlattenAlgebra, true);
        testOptimise(op1, expected, ctx);
    }

    @Test public void pathFlatten_reverse_01() {
        Op op1 = path("?x", "^:p1+", ":T1");
        Op expected = op(
                "(path :T1 (path+ :p1) ?x)"
        );

        test(op1, expected);
    }

    @Test public void pathFlatten_reverse_algebra_01() {
        Op op1 = path("?x", "^:p1+", ":T1");
        Op expected = op(
                "(path :T1 (path+ :p1) ?x)"
        );
        testAlgebra(op1, expected);
    }

    @Test public void pathFlatten_n_to_m_01() {
        Op op1 = path("?x", ":p{2,}", ":T1");
        Op expected = op(
          "(sequence",
                 "  (path ??P0 (pathN* :p) :T1)",
                 "  (bgp",
                 "    (triple ?x :p ??P1)",
                 "    (triple ??P1 :p ??P0)",
                 "))"
        );
        test(op1, expected);
    }

    @Test public void pathFlatten_n_to_m_01_algebra() {
        Op op1 = path("?x", ":p{2,}", ":T1");
        Op expected = op(
                "(sequence",
                "  (join",
                "    (triple ??Q0 :p ??Q1)",
                "    (triple ??Q1 :p :T1))",
                "  (path ?x (pathN* :p) ??Q0)",
                ")"
        );
        testAlgebra(op1, expected);
    }

    @Test public void pathFlatten_n_to_m_01b_algebra() {
        Op op1 = path("?x", ":p{2,}", ":T1");
        Op expected = op(
                "(sequence",
                "  (bgp",
                "    (triple ??Q0 :p ??Q1)",
                "    (triple ??Q1 :p :T1))",
                "  (path ?x (pathN* :p) ??Q0)",
                ")"
        );
        Context ctx = new Context();
        ctx.set(ARQ.optPathFlattenAlgebra, true);
        testOptimise(op1, expected, ctx);
    }

    @Test public void pathFlatten_n_to_m_02() {
        Op op1 = path(":T1", ":p{2,}", "?x");
        Op expected = op(
                "(sequence",
                "  (bgp",
                "    (triple :T1 :p ??P1)",
                "    (triple ??P1 :p ??P0))",
                "  (path ??P0 (pathN* :p) ?x)",
                ")"
        );
        test(op1, expected);
    }

    @Test public void pathFlatten_n_to_m_02_algebra() {
        Op op1 = path(":T1", ":p{2,}", "?x");
        Op expected = op(
                "(sequence",
                "  (join",
                "    (triple :T1 :p ??Q1)",
                "    (triple ??Q1 :p ??Q0))",
                "  (path ??Q0 (pathN* :p) ?x)",
                ")"
        );
        testAlgebra(op1, expected);
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
        testPathTransform(opInput, opExpected, new TransformPathFlattern());
    }

    private static void testPathTransform(Op opInput, Op opExpected, Transform transform) {
        Op op = Transformer.transform(transform, opInput);
        verifyTransforms(opInput, opExpected, op);
    }

    private static void verifyTransforms(Op opInput, Op opExpected, Op opTransformed) {
        System.out.println(opTransformed.toString(prologue.getPrefixMapping()));
        if ( opExpected == null ) {
            // Expect no transformation to be applied so input should be same as transformation output
            assertEquals(opInput, opTransformed);
        } else {
            // Expect transformation to have been applied
            assertEquals(opExpected, opTransformed);
        }
    }

    private static void testAlgebra(Op opInput, Op opExpected) {
        testPathTransform(opInput, opExpected, new TransformPathFlattenAlgebra());
    }

    private static void testOptimise(Op opInput, Op opExpected, Context context) {
        OptimizerStd optimizer = new OptimizerStd(context);
        Op op = optimizer.rewrite(opInput);
        verifyTransforms(opInput, opExpected, op);
    }
}
