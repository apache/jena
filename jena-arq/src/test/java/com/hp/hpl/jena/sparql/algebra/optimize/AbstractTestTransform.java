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

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public abstract class AbstractTestTransform extends BaseTest {

    public void testOptimize(String input, String... output) {
        Query q = QueryFactory.create(input) ;
        Op op = Algebra.compile(q) ;
        check(op, StrUtils.strjoinNL(output)) ;
    }

    
    public void testQuery(String input, Transform transform, String... output) {
        Query q = QueryFactory.create(input) ;
        Op op = Algebra.compile(q) ;
        test(op, transform, output) ;
    }

    public void testOp(String input, Transform transform, String... output) {
        Op op1 = SSE.parseOp(input) ;
        test(op1, transform, output);
    }

    public void test(Op op1, Transform transform, String... output) {
        Op op2 = Transformer.transform(transform, op1) ;
        if ( output == null ) {
            // No transformation.
            Assert.assertEquals(op1, op2) ;
            return ;
        }

        Op op3 = SSE.parseOp(StrUtils.strjoinNL(output)) ;
        Assert.assertEquals(op3, op2) ;
    }

    public static void check(String queryString, String opExpectedString) {
        queryString = "PREFIX : <http://example/>\n" + queryString ;
        Query query = QueryFactory.create(queryString) ;
        Op opQuery = Algebra.compile(query) ;
        check(opQuery, opExpectedString) ;
    }

    private static void check(Op opToOptimize, String opExpectedString) {
        Op opOptimize = Algebra.optimize(opToOptimize) ;
        Op opExpected = SSE.parseOp(opExpectedString) ;
        assertEquals(opExpected, opOptimize) ;
    }

    public static void check(Op opToOptimize, Transform additionalOptimizer, String opExpectedString) {
        Op opOptimize = Algebra.optimize(opToOptimize) ;
        opOptimize = Transformer.transform(additionalOptimizer, opOptimize) ;
        Op opExpected = SSE.parseOp(opExpectedString) ;
        assertEquals(opExpected, opOptimize) ;
    }
    
    public static void checkAlgebra(String algString, Transform additionalOptimizer, String opExpectedString) {
        Op algebra = SSE.parseOp(algString) ;
        algebra = Algebra.optimize(algebra) ;
        algebra = Transformer.transform(additionalOptimizer, algebra);
        Op opExpected = SSE.parseOp(opExpectedString != null ? opExpectedString : algString);
        assertEquals(opExpected, algebra) ;
    }

    public static void checkAlgebra(String algString, String opExpectedString) {
        Op algebra = SSE.parseOp(algString) ;
        algebra = Algebra.optimize(algebra) ;
        Op opExpected = SSE.parseOp(opExpectedString != null ? opExpectedString : algString);
        assertEquals(opExpected, algebra) ;
    }

}
