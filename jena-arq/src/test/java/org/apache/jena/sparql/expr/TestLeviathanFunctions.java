/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.expr;

import static org.apache.jena.sparql.expr.LibTestExpr.*;
import static org.apache.jena.sparql.expr.LibTestExpr.testDoubleIsNaN;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TestLeviathanFunctions {

    private static final double DELTA = 0.0000000001d;
    static boolean warnOnBadLexicalForms = true;

    @BeforeAll
    public static void beforeClass() {
        warnOnBadLexicalForms = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false;
    }

    @AfterAll
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
    
    @Test public void pow_10()          { test("lfn:pow('INF'^^xsd:double, 1)", "'INF'^^xsd:double"); }
    @Test public void pow_11()          { test("lfn:pow(1, 'INF'^^xsd:double)", "1"); }
    @Test public void pow_12()          { test("lfn:pow(1e0, 'INF'^^xsd:double)", "'1.0e0'^^xsd:double"); }

    @Test public void pow_13()          { test("lfn:pow('INF'^^xsd:double,0)", "'1.0e0'^^xsd:double"); }
    @Test public void pow_14()          { test("lfn:pow('-INF'^^xsd:double, 0)", "'1.0e0'^^xsd:double"); }
    @Test public void pow_15()          { testDoubleIsNaN("lfn:pow('NaN'^^xsd:double, 1)"); }
    @Test public void pow_16()          { testDoubleIsNaN("lfn:pow(1, 'NaN'^^xsd:double)"); }

    @Test public void pow_17()          { test("lfn:pow(0e0, -3)", "'INF'^^xsd:double"); }
    @Test public void pow_18()          { test("lfn:pow(-0e0, -3)", "'-INF'^^xsd:double"); }
    @Test public void pow_19()          { test("lfn:pow(01, 'INF'^^xsd:double)", "1"); }
    @Test public void pow_20()          { test("lfn:pow(-1e0, '-INF'^^xsd:double)", "'1.0e0'^^xsd:double"); }
    @Test public void pow_21()          { testDoubleIsNaN("lfn:pow(-2.5e0, 2.00000001e0)"); }
    @Test public void pow_22()          { test("lfn:pow(0e0, 3.0e0)", "0e0"); }
    @Test public void pow_23()          { test("lfn:pow(-0e0, 3.0e0)", "-0e0"); }

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

    @Test
    public void factorial_05() {
        testError("lfn:factorial(-1)", ExprEvalException.class);
    }

    @Test
    public void factorial_06() {
        testError("lfn:factorial(5.4)", ExprEvalException.class);
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
        assertTrue(NodeValue.nvDoubleNaN.equals(actual));
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

    public static Stream<Arguments> outOfRangeInputs() {
        return Stream.of(Arguments.of("lfn:factorial(2000000000)"),
                         Arguments.of("lfn:pow(100, 100)"),
                         Arguments.of("lfn:sq(100.0e100)"),
                         Arguments.of("lfn:cube(100.0e100)"),
                         Arguments.of("lfn:ten(2000000000)"),
                         Arguments.of("lfn:cartesian(0, 0, 1, 100.0e100)"),
                         Arguments.of("lfn:cartesian(0, 0, 100.0e100, 1)"),
                         Arguments.of("lfn:cartesian(0, 0, 0, 1, 1, 100.0e100)"),
                         Arguments.of("lfn:cartesian(0, 0, 0, 1, 100.0e100, 1)"),
                         Arguments.of("lfn:cartesian(0, 0, 0, 100.0e100, 1, 1)"),
                         Arguments.of("lfn:pythagoras(2.0e128, 2)"),
                         Arguments.of("lfn:root(2" + StringUtils.repeat('0', 64) + ", 1)"),
                         Arguments.of("lfn:e(2000000000)")
        );
    }

    @ParameterizedTest
    @MethodSource("outOfRangeInputs")
    public void givenOutOfRangeInputsInExpr_whenExecuting_thenErrors(String expr) {
        ExprEvalException error = testError(expr, ExprEvalException.class);
        assertNotNull(error);
        assertTrue(Strings.CI.containsAny(error.getMessage(), "too many digits", "outside double range"));
    }
}
