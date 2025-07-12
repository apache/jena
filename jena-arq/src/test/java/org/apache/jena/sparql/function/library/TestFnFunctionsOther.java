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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sys.JenaSystem;

/** "Other" functions */
public class TestFnFunctionsOther {

    static { JenaSystem.init(); }

    @Test public void apply_1() {
        test("fn:apply(math:sqrt, 9)", "3.0e0");
    }

    // Under-arity
    @Test
    public void apply_2() {
        assertThrows(ExprEvalException.class,()-> test("fn:apply(math:sqrt)", "3.0e0") );
    }

    // Over-arity
    @Test
    public void apply_3() {
        assertThrows(ExprEvalException.class,()-> test("fn:apply(math:sqrt, 9, 10)", "3.0e0") );
    }

    // Not a URI.
    @Test
    public void apply_4() {
        assertThrows(ExprEvalException.class,()-> test("fn:apply('bicycle', 9, 10)", "3.0e0") );
    }

    @Test
    public void apply_5() {
        assertThrows(VariableNotBoundException.class,()-> test("fn:apply(?var)", "3.0e0") );
    }

    @Test
    public void apply_6() {
        assertThrows(ExprEvalException.class,()-> test("fn:apply(<x:unregistered>)", "false") );
    }

    @Test
    public void apply_7() {
        assertThrows(ExprException.class,()-> test("fn:apply()", "false") );
    }
}
