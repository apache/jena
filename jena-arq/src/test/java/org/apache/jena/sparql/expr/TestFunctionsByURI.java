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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sys.JenaSystem;

/**
 * Test that SPARQL functions can be called by their URI as given in the SPARQL 1.2
 * namespace <a href="https://www.w3.org/ns/sparql#">http://www.w3.org/ns/sparql#</a>.
 */
public class TestFunctionsByURI
{
    private static boolean warnOnBadLexicalForms;

    static { JenaSystem.init(); }

    @BeforeAll
    public static void beforeClass() {
        warnOnBadLexicalForms = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false;
    }

    @AfterAll
    public static void afterClass() {
        NodeValue.VerboseWarnings = warnOnBadLexicalForms;
    }

    @Test public void sparql_function_uri_1()  { test("sparql:unary-minus(123)",                    "-123"); }
    @Test public void sparql_function_uri_2()  { test("sparql:subtract(12, sparql:multiply(5,2))",  "2"); }
    @Test public void sparql_function_uri_3()  { test("sparql:not(123)",   "false"); }          // EBV - effective boolean value
    // --------

    private static PrefixMapping pmap = ARQConstants.getGlobalPrefixMap();

    private static void test(String string, String result) {
        Expr expr = ExprUtils.parse(string, pmap);
        NodeValue nv = expr.eval(null, new FunctionEnvBase());
        NodeValue nvr = SSE.parseNodeValue(result);
        boolean b = NodeValue.sameValueAs(nvr, nv);
        assertTrue(b, "Not same value: Expected: " + nvr + " : Actual = " + nv);
    }
}
