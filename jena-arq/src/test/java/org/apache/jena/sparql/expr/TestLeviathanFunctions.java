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

package org.apache.jena.sparql.expr;

import static org.apache.jena.sparql.expr.LibTestExpr.test ;
import static org.apache.jena.sparql.expr.LibTestExpr.testDouble ;
import static org.apache.jena.sparql.expr.LibTestExpr.testError ;
import static org.junit.Assert.assertTrue;

import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLeviathanFunctions {

    private static final double DELTA = 0.0000000001d;
    static boolean warnOnBadLexicalForms = true;

    @BeforeClass
    public static void beforeClass() {
        warnOnBadLexicalForms = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false;
    }

    @AfterClass
    public static void afterClass() {
        NodeValue.VerboseWarnings = warnOnBadLexicalForms;
    }

    @Test
    public void sq_01() {
        LibTestExpr.test("lfn:sq(2)", "4");
    }

    @Test
    public void sq_02() {
        LibTestExpr.test("lfn:sq(3)", "9");
    }

    @Test
    public void sq_03() {
        LibTestExpr.test("lfn:sq(0.5)", "0.25");
    }

    @Test
    public void cube_01() {
        LibTestExpr.test("lfn:cube(2)", "8");
    }

    @Test
    public void cube_02() {
        LibTestExpr.test("lfn:cube(3)", "27");
    }

    @Test
    public void cube_03() {
        LibTestExpr.test("lfn:cube(0.5)", "0.125");
    }

    @Test
    public void e_01() {
        test("lfn:e(2)", NodeFactoryExtra.doubleToNode(Math.exp(2d)));
    }

    @Test
    public void pow_01() {
        LibTestExpr.test("lfn:pow(2, 4)", "16");
    }

    @Test
    public void pow_02() {
        LibTestExpr.test("lfn:pow(0.5, 3)", "0.125");
    }

    @Test
    public void factorial_01() {
        LibTestExpr.test("lfn:factorial(0)", "1");
    }

    @Test
    public void factorial_02() {
        LibTestExpr.test("lfn:factorial(1)", "1");
    }

    @Test
    public void factorial_03() {
        LibTestExpr.test("lfn:factorial(3)", "6");
    }

    @Test
    public void factorial_04() {
        LibTestExpr.test("lfn:factorial(5)", "120");
    }

    @Test(expected = ExprEvalException.class)
    public void factorial_05() {
        testError("lfn:factorial(-1)");
    }

    @Test(expected = ExprEvalException.class)
    public void factorial_06() {
        testError("lfn:factorial(5.4)");
    }

    @Test
    public void log_01() {
        LibTestExpr.test("lfn:log(1)", "0e0");
    }

    @Test
    public void log_02() {
        LibTestExpr.test("lfn:log(10)", "1e0");
    }

    @Test
    public void log_03() {
        NodeValue actual = LibTestExpr.eval("lfn:log(-1)");
        // Test the object, not the value.
        assertTrue(NodeValue.nvNaN.equals(actual));
    }

    @Test
    public void log_04() {
        LibTestExpr.test("lfn:log(4, 2)", "2e0");
    }

    @Test
    public void log_05() {
        LibTestExpr.test("lfn:log(4, 16)", "0.5e0");
    }

    @Test
    public void log_06() {
        LibTestExpr.test("lfn:log(16, 4)", "2e0");
    }

    @Test
    public void reciprocal_01() {
        LibTestExpr.test("lfn:reciprocal(1)", "1e0");
    }

    @Test
    public void reciprocal_02() {
        LibTestExpr.test("lfn:reciprocal(2)", "0.5e0");
    }

    @Test
    public void reciprocal_03() {
        LibTestExpr.test("lfn:reciprocal(lfn:reciprocal(2))", "2e0");
    }

    @Test
    public void root_01() {
        LibTestExpr.test("lfn:root(4,2)", "2e0");
    }

    @Test
    public void root_02() {
        LibTestExpr.test("lfn:root(2,1)", "2e0");
    }

    @Test
    public void root_03() {
        testDouble("lfn:root(64,3)", "4", DELTA);
    }

    @Test
    public void sqrt_01() {
        LibTestExpr.test("lfn:sqrt(4)", "2e0");
    }

    @Test
    public void sqrt_02() {
        LibTestExpr.test("lfn:sqrt(144)", "12e0");
    }

    @Test
    public void cartesian_01() {
        LibTestExpr.test("lfn:cartesian(0, 0, 0, 0)", "0e0");
    }

    @Test
    public void cartesian_02() {
        LibTestExpr.test("lfn:cartesian(0, 0, 3, 4)", "5e0");
    }

    @Test
    public void cartesian_03() {
        LibTestExpr.test("lfn:cartesian(0, 0, 0, 3, 4, 0)", "5e0");
    }

    @Test
    public void cartesian_04() {
        LibTestExpr.test("lfn:cartesian(0, 0, 0, 0, 3, 4)", "5e0");
    }

    @Test
    public void cartesian_05() {
        LibTestExpr.test("lfn:cartesian(0, 0, 0, 3, 0, 4)", "5e0");
    }

    @Test
    public void cos_01() {
        testDouble("lfn:cos(lfn:degrees-to-radians(60))", "0.5", DELTA);
    }

    @Test
    public void acos_01() {
        testDouble("lfn:radians-to-degrees(lfn:cos-1(lfn:cos(lfn:degrees-to-radians(60))))", "60", DELTA);
    }
}
