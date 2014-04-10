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

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.expr.ExprTransform;
import com.hp.hpl.jena.sparql.sse.SSE;

/**
 * Tests for the {@link ExprTransformConstantFold}
 */
public class TestTransformConstantFolding {

    private ExprTransform transform = new ExprTransformConstantFold();

    private void testNoTransform(String input, ExprTransform transform) {
        test(input, null, transform);
    }

    private void test(String input, String expected, ExprTransform transform) {
        Op opOrig = SSE.parseOp(input);
        Op opExpected = SSE.parseOp(expected != null ? expected : input);

        Op opOptimized = Transformer.transform(new TransformCopy(), transform, opOrig);

        Assert.assertEquals(opExpected, opOptimized);
    }

    @Test
    public void constant_fold_extend_01() {
        test("(extend (?x (+ 1 2)) (table unit))", "(extend (?x 3) (table unit))", transform);
    }

    @Test
    public void constant_fold_extend_02() {
        test("(extend (?x (+ (+ 1 2) 3)) (table unit))", "(extend (?x 6) (table unit))", transform);
    }

    @Test
    public void constant_fold_extend_03() {
        test("(extend (?x (/ 1 2)) (table unit))", "(extend (?x 0.5) (table unit))", transform);
    }

    @Test
    public void constant_fold_extend_04() {
        // When an error occurs we don't fold
        testNoTransform("(extend (?x (/ 1 0)) (table unit))", transform);
    }

    @Test
    public void constant_fold_extend_05() {
        test("(extend (?x (abs -1)) (table unit))", "(extend (?x 1) (table unit))", transform);
    }

    @Test
    public void constant_fold_extend_06() {
        test("(extend (?x (regex 'something' 'thing')) (table unit))", "(extend (?x true) (table unit))", transform);
    }

    @Test
    public void constant_fold_extend_07() {
        // Constant folding does not take advantage of any knowledge about the
        // specific function being folded
        // In this case the first expression to coalesce will always error and
        // so could be removed entirely but isn't currently
        testNoTransform("(extend (?x (coalesce (/ 1 0) 0)) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_01() {
        test("(filter (exprlist (+ 1 2)) (table unit))", "(filter (exprlist 3) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_02() {
        test("(filter (exprlist (+ (+ 1 2) 3)) (table unit))", "(filter (exprlist 6) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_03() {
        test("(filter (exprlist (/ 1 2)) (table unit))", "(filter (exprlist 0.5) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_04() {
        // When an error occurs we don't fold
        testNoTransform("(filter (exprlist (/ 1 0)) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_05() {
        test("(filter (exprlist (abs -1)) (table unit))", "(filter (exprlist 1) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_06() {
        test("(filter (regex 'something' 'thing') (table unit))", "(filter (exprlist true) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_07() {
        testNoTransform("(filter (exprlist (coalesce (/ 1 0) 0)) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_08() {
        test("(filter (exists (filter (exprlist (+ 1 2)) (table unit))) (table unit))", "(filter (exists (filter (exprlist 3) (table unit))) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_09() {
        test("(filter (exprlist (= ?x (+ 1 2))) (table unit))", "(filter (exprlist (= ?x 3)) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_10() {
        test("(filter (exprlist (+ 1 (* (+ 5 6 ) (+ 8 9)))) (table unit))", "(filter (exprlist 188) (table unit))", transform);
    }
    
    @Test
    public void constant_fold_filter_11() {
        test("(filter (exprlist (* ?y (+ (* ?x 4) (* ?z 6 )))) (table unit))", null, transform);
    }
    
    @Test
    public void constant_fold_group_01() {
        test("(project (?count) (extend ((?count ?.0)) (group () ((?.0 (count (+ 1 2)))) (table unit))))", "(project (?count) (extend ((?count ?.0)) (group () ((?.0 (count 3))) (table unit))))", transform);
    }
    
    @Test
    public void constant_fold_leftjoin_01() {
        test("(leftjoin (table unit) (table unit) (+ 1 2))", "(leftjoin (table unit) (table unit) (exprlist 3))", transform);
    }
}
