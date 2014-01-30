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
    
    private void test(String input, ExprTransform transform) {
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
        test("(extend (?x (/ 1 0)) (table unit))", "(extend (?x (/ 1 0)) (table unit))", transform);
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
        test("(extend (?x (coalesce (/ 1 0) 0)) (table unit))", transform);
    }
}
