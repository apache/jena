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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sys.JenaSystem;

/**
 * Tests for TRIPLE, SUBJECT, PREDICATE, OBJECT, isTRIPLE
 */
public class TestExprTripleTerms {

    static { JenaSystem.init(); }

    @Test public void tripleTerm_Create1() {
        Node r = eval("triple(:s1, :p1, :o1)");
        assertNotNull(r);
    }

    @Test
    public void tripleTerm_Bad1() {
        assertThrows(ExprEvalException.class, ()-> eval("triple(:s1, 'bc', :o1)") );
    }

    @Test
    public void tripleTerm_Access1() {
        test("subject(triple(:s1, :p1, :o1))", ":s1");
    }

    @Test
    public void tripleTerm_Access2() {
        test("predicate(triple(:s1, :p1, :o1))", ":p1");
    }

    @Test
    public void tripleTerm_Access3() {
        test("object(triple(:s1, :p1, :o1))", ":o1");
    }

    @Test
    public void tripleTerm_Test1() {
        test("isTriple(triple(:s1, :p1, :o1))", "true");
    }

    @Test
    public void tripleTerm_Test2() {
        test("isTriple(:x)", "false");
    }

    private static Node eval(String string) {
        Expr expr = ExprUtils.parse(string, pmap);
        NodeValue nv = expr.eval(null, new FunctionEnvBase());
        return nv.getNode();
    }

    private static PrefixMapping pmap = SSE.getPrefixMapRead();

    private static void test(String string, String result) {
        Node got = eval(string);
        Node expected = NodeFactoryExtra.parseNode(result);
        assertEquals(expected, got);
    }
}
