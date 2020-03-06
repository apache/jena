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

package org.apache.jena.sparql.function.library;

import static org.apache.jena.sparql.expr.LibTestExpr.test;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

public class TestFnFunctionsNumeric {

    static { JenaSystem.init(); }

    @Test public void exprRound_01()    { test("fn:round(123)",   NodeValue.makeInteger(123)) ; }
    @Test public void exprRound_02()    { test("fn:round(123.5)",  NodeValue.makeDecimal(124)) ; }
    @Test public void exprRound_03()    { test("fn:round(-0.5e0)", NodeValue.makeDouble(0.0e0)) ; }
    @Test public void exprRound_04()    { test("fn:round(-1.5)",   NodeValue.makeDecimal(-1)) ; }
    // !! I don't think that this is working correctly also if the test is passing... need to check!
    @Test public void exprRound_05()    { test("fn:round(-0)",     NodeValue.makeInteger("-0")) ; }
    @Test public void exprRound_06()    { test("fn:round(1.125, 2)",     NodeValue.makeDecimal(1.13)) ; }
    @Test public void exprRound_07()    { test("fn:round(8452, -2)",     NodeValue.makeInteger(8500)) ; }
    @Test public void exprRound_08()    { test("fn:round(3.1415e0, 2)",     NodeValue.makeDouble(3.14e0)) ; }
    // counter-intuitive -- would fail if float/double not translated to decimal
    @Test public void exprRound_09()    { test("fn:round(35.425e0, 2)",     NodeValue.makeDouble(35.42)) ; }

    @Test public void exprRoundHalfEven_01()    { test("fn:round-half-to-even(0.5)",   NodeValue.makeDecimal(0)) ; }
    @Test public void exprRoundHalfEven_02()    { test("fn:round-half-to-even(1.5)",  NodeValue.makeDecimal(2)) ; }
    @Test public void exprRoundHalfEven_03()    { test("fn:round-half-to-even(2.5)", NodeValue.makeDecimal(2)) ; }
    @Test public void exprRoundHalfEven_04()    { test("fn:round-half-to-even(3.567812e+3, 2)",   NodeValue.makeDouble(3567.81e0)) ; }
    // !! I don't think that this is working correctly also if the test is passing... need to check!
    @Test public void exprRoundHalfEven_05()    { test("fn:round-half-to-even(-0)",     NodeValue.makeInteger(-0)) ; }
    @Test public void exprRoundHalfEven_06()    { test("fn:round-half-to-even(4.7564e-3, 2)",     NodeValue.makeDouble(0.0e0)) ; }
    @Test public void exprRoundHalfEven_07()    { test("fn:round-half-to-even(35612.25, -2)",     NodeValue.makeDecimal(35600)) ; }
    // counter-intuitive -- would fail if float/double not translated to decimal
    @Test public void exprRoundHalfEven_08()    { test("fn:round-half-to-even('150.015'^^xsd:float, 2)",     NodeValue.makeFloat((float)150.01)) ; }
}
