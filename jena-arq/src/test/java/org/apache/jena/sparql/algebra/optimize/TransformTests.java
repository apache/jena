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

package org.apache.jena.sparql.algebra.optimize;

import static org.junit.Assert.assertEquals;

import java.util.Objects;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Assert;

public class TransformTests {

    private TransformTests() {}

    static void testOptimize(String input, String...output) {
        Query q = QueryFactory.create(input);
        Op op = Algebra.compile(q);
        check(op, StrUtils.strjoinNL(output));
    }

    static void testQuery(String input, Transform transform, String...output) {
        Query q = QueryFactory.create(input);
        Op op = Algebra.compile(q);
        testApplyTransform(op, transform, output);
    }

    static void testOp(String input, Transform transform, String...output) {
        Op op1 = SSE.parseOp(input);
        testApplyTransform(op1, transform, output);
    }

    static void testApplyTransform(Op op1, Transform transform, String...output) {
        Op op2 = Transformer.transform(transform, op1);
        if ( output == null ) {
            // No transformation.
            Assert.assertEquals(op1, op2);
            return;
        }

        Op op3 = SSE.parseOp(StrUtils.strjoinNL(output));
        Assert.assertEquals(op3, op2);
    }

    static void check(String queryString, String opExpectedString) {
        queryString = "PREFIX : <http://example/>\n" + queryString;
        Query query = QueryFactory.create(queryString);
        Op opQuery = Algebra.compile(query);
        Op op1 = Algebra.compile(query);   // Safe copy
        check(opQuery, opExpectedString);
        assertEquals("Modification of input during optimization", op1, opQuery);
    }

    private static void check(Op opToOptimize, String opExpectedString) {
        Op opOptimize = Algebra.optimize(opToOptimize);
        Op opExpected = SSE.parseOp(opExpectedString);
        if ( false ) {
            // Hook for more detail during development.
            boolean b = Objects.equals(opExpected, opOptimize);
            if ( !b ) {
                System.err.println("** Input:");
                System.err.print(opToOptimize);
                System.err.println("** Expected:");
                System.err.print(opExpected);
                System.err.println("** Actual:");
                System.err.print(opOptimize);
                System.err.println("-------------------");
            }
        }
        assertEquals(opExpected, opOptimize);
    }

    static void check(Op opToOptimize, Transform additionalOptimizer, String opExpectedString) {
        Op opOptimize = Algebra.optimize(opToOptimize);
        opOptimize = Transformer.transform(additionalOptimizer, opOptimize);
        Op opExpected = SSE.parseOp(opExpectedString);
        assertEquals(opExpected, opOptimize);
    }

    static void checkAlgebra(String algString, Transform additionalOptimizer, String opExpectedString) {
        Op algebra = SSE.parseOp(algString);
        Op algebra1 = SSE.parseOp(algString);  // Safe copy
        Op optimized = Algebra.optimize(algebra);
        if ( additionalOptimizer != null )
            optimized = Transformer.transform(additionalOptimizer, optimized);
        Op opExpected = SSE.parseOp(opExpectedString != null ? opExpectedString : algString);
        assertEquals(opExpected, optimized);
        assertEquals("Modification of input during optimization", algebra1, algebra);
    }

    static void checkAlgebra(String algString, String opExpectedString) {
        checkAlgebra(algString, null, opExpectedString);
    }
}
