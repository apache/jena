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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.nodevalue.NodeValueOps;
import org.apache.jena.sparql.sse.SSE;

public class TestStringArgCompatibility {

    @Test public void arg_01()     { testArgGood("'abc'", "'abc'"); }
    @Test public void arg_02()     { testArgGood("'abc'@en", "'abc'@en"); }
    @Test public void arg_03()     { testArgGood("'abc'@en--ltr", "'abc'@en--ltr"); }

    @Test public void arg_04()     { testArgBad("123"); }
    @Test public void arg_05()     { testArgBad("<123>"); }

    @Test public void args2_01()   { test2ArgsGood("'abc'", "''"); }
    @Test public void args2_02()   { test2ArgsGood("'abc'", "'ab'"); }
    @Test public void args2_03()   { test2ArgsGood("'abc'@en", "'ab'"); }
    @Test public void args2_04()   { test2ArgsGood("'abc'@en--ltr", "'ab'"); }
    @Test public void args2_05()   { test2ArgsGood("'abc'@en", "'ab'@en"); }
    @Test public void args2_06()   { test2ArgsGood("'abc'@en--ltr", "'ab'@en--ltr"); }

    @Test public void args2_20()   { test2ArgsBad("123", "123"); }  // Not strings.
    @Test public void args2_21()   { test2ArgsBad("'abc'@en", "'a'@fr"); }
    @Test public void args2_22()   { test2ArgsBad("'abc'@en--ltr", "'a'@en"); }
    @Test public void args2_23()   { test2ArgsBad("'abc'@en", "'a'@en--ltr"); }

    private void testArgGood(String arg, String expected) {
        Node n = SSE.parseNode(arg);
        NodeValue nv = NodeValue.makeNode(n);
        Node r = NodeValueOps.checkAndGetStringLiteral("test", nv);
        Node nExpected = SSE.parseNode(expected);
        assertEquals(nExpected, r);
    }

    private void testArgBad(String arg) {
        Node n = SSE.parseNode(arg);
        NodeValue nv = NodeValue.makeNode(n);
        assertThrows(ExprEvalException.class, ()->NodeValueOps.checkAndGetStringLiteral("test", nv));
    }

    private void test2ArgsGood(String arg1, String arg2) {
        Node n1 = SSE.parseNode(arg1);
        Node n2 = SSE.parseNode(arg2);
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        NodeValueOps.checkTwoArgumentStringLiterals("test", nv1, nv2);
    }

    private void test2ArgsBad(String arg1, String arg2) {
        Node n1 = SSE.parseNode(arg1);
        Node n2 = SSE.parseNode(arg2);
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertThrows(ExprEvalException.class, ()->NodeValueOps.checkTwoArgumentStringLiterals("test", nv1, nv2));
    }

}
