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

import static org.apache.jena.sparql.expr.LibTestExpr.test;
import static org.apache.jena.sparql.expr.LibTestExpr.testSSE;

import org.apache.jena.query.ARQ;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

public class TestExpressions4 {

    static { JenaSystem.init(); }

    @Test public void adjust_fn_1() {
        strict(()->test("fn:adjust-dateTime-to-timezone(xsd:dateTime('2022-12-21T05:05:07'), '-PT10H'^^xsd:duration)",
                          "'2022-12-21T05:05:07-10:00'^^xsd:dateTime"));
    }

    @Test(expected=ExprEvalException.class)
    public void adjust_fn_2() {
        strict(()->test("fn:adjust-dateTime-to-timezone(xsd:date('2022-12-21'), '-PT10H'^^xsd:duration)",
                        "'2022-12-21-10:00'^^xsd:date"));
    }

    @Test public void adjust_fn_3() {
        // ARQ addition
        test("afn:adjust-to-timezone(xsd:date('2022-12-21'), '-PT10H'^^xsd:duration)", "'2022-12-21-10:00'^^xsd:date");
    }

    // Run in strict mode.
    private static void strict(Runnable action) {
        boolean b = ARQ.isStrictMode();
        ARQ.setStrictMode();
        try { action.run(); }
        finally {
            if ( !b )
                ARQ.setNormalMode();
        }
    }

    // ---- op:numeric-integer-divide
    // Operator, function and fn:function, op:function forms.

    @Test public void idiv_1()          { test("7 idiv 3", "2"); }
    @Test public void idiv_2()          { test("idiv(7, 3)", "2"); }
    @Test public void idiv_3()          { test("fn:numeric-integer-divide(7, 3)", "2"); }
    @Test public void idiv_4()          { test("op:numeric-integer-divide(7, 3)", "2"); }
    @Test public void idiv_5()          { testSSE("(idiv 7 3)", "2"); }

    // Examples from F&O

    @Test public void idiv_10() { test("IDIV(10 , 3)", "3"); }
    @Test public void idiv_11() { test("IDIV(3 , -2)", "-1"); }
    @Test public void idiv_12() { test("IDIV(-3 , 2)", "-1"); }
    @Test public void idiv_13() { test("IDIV(-3 , -2)", "1"); }
    @Test public void idiv_14() { test("IDIV(9.0 , 3)", "3"); }
    @Test public void idiv_15() { test("IDIV(-3.5 , 3)", "-1"); }
    @Test public void idiv_16() { test("IDIV(3.0 , 4)", "0"); }
    @Test public void idiv_17() { test("IDIV(3.1E1 , 6)", "5"); }
    @Test public void idiv_18() { test("IDIV(3.1E1 , 7)", "4"); }

    @Test(expected = ExprEvalException.class)
    public void idiv_20() { test("IDIV(3 , 0)", "4"); }
    @Test(expected = ExprEvalException.class)
    public void idiv_21() { test("IDIV(3.1 , 0.0)", "4"); }
    @Test(expected = ExprEvalException.class)
    public void idiv_22() { test("IDIV(3.1E1 , 0e0)", "4"); }

    // ---- op:numeric-mod
    // Operator, function and fn:function, op:function forms.

    @Test public void mod_1()           { test("5 mod 3", "2"); }
    @Test public void mod_2()           { test("mod(5, 3)", "2"); }
    @Test public void mod_3()           { test("fn:numeric-mod(5, 3)", "2"); }
    @Test public void mod_4()           { test("op:numeric-mod(5, 3)", "2"); }
    @Test public void mod_5()           { testSSE("(mod 5 3)", "2"); }

    // Examples from F&O
    @Test public void mod_10() { test("MOD(10 , 3)", "1"); }
    @Test public void mod_11() { test("MOD(6 , -2)", "0"); }
    @Test public void mod_12() { test("MOD(4.5 , 1.2)", "0.9"); }
    @Test public void mod_13() { test("MOD(1.23E2 , 0.6E1)", "3.0E0"); }

    // Sign of result is sign of dividend (left argument)
    @Test public void mod_14() { test("MOD(7 , -2)", "1"); }
    @Test public void mod_15() { test("MOD(7.0 , -2.0)", "1.0"); }
    @Test public void mod_16() { test("MOD(7e0 , -2e0)", "1.0e0"); }

    @Test public void mod_17() { test("MOD(-7 , -2)", "-1"); }
    @Test public void mod_18() { test("MOD(-7.0 , -2.0)", "-1.0"); }
    @Test public void mod_19() { test("MOD(-7e0 , -2e0)", "-1.0e0"); }

    @Test(expected = ExprEvalException.class)
    public void mod_20() { test("MOD(123 , 0)", "3"); }
    @Test(expected = ExprEvalException.class)
    public void mod_21() { test("MOD(12.3 , 0.0)", "3.0"); }
    @Test(expected = ExprEvalException.class)
    public void mod_22() { test("MOD(1.23E2 , 0.0e0)", "3.0E0"); }

}
