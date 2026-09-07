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

package org.apache.jena.graph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.junit.NodeCreateUtils;

public class TestFindLiterals {


    private void runTest(String graph, int size, String search, String results) {
        Graph g = GraphTestLib.graphWith(graph);
        Node literal = NodeCreateUtils.create(search);
        assertEquals(size, g.size(), "graph has wrong size");
        Set<Node> got = g.find(Node.ANY, Node.ANY, literal).mapWith(t -> t.getObject()).toSet();
        assertEquals(GraphTestLib.nodeSet(results), got);
    }

    @Test
    public void test01() {
        runTest("a P 'simple'", 1, "'simple'", "'simple'");
    }

    @Test
    public void test02() {
        runTest("a P 'simple'xsd:string", 1, "'simple'", "'simple'xsd:string");
    }

    @Test
    public void test03() {
        runTest("a P 'simple'", 1, "'simple'xsd:string", "'simple'");
    }

    @Test
    public void test04() {
        runTest("a P 'simple'xsd:string", 1, "'simple'xsd:string", "'simple'xsd:string");
    }

    private final int expected = 1; // 2 for RDF 1.0
    @Test
    public void test05() {
        runTest("a P 'simple'; a P 'simple'xsd:string", expected, "'simple'", "'simple' 'simple'xsd:string");
    }

    @Test
    public void test06() {
        runTest("a P 'simple'; a P 'simple'xsd:string", expected, "'simple'xsd:string", "'simple' 'simple'xsd:string");
    }

    @Test
    public void test07() {
        runTest("a P 1", 1, "1", "1");
    }

    @Test
    public void test08() {
        runTest("a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float");
    }

    @Test
    public void test09() {
        runTest("a P '1'xsd:double", 1, "'1'xsd:double", "'1'xsd:double");
    }

    @Test
    public void test10() {
        runTest("a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float");
    }

    @Test
    public void test11() {
        runTest("a P '1.1'xsd:float", 1, "'1'xsd:float", "");
    }

    @Test
    public void test12() {
        runTest("a P '1'xsd:double", 1, "'1'xsd:int", "");

    }

    @Test
    public void test13() {
        runTest("a P 'abc'rdf:XMLLiteral", 1, "'abc'", "");
    }

    @Test
    public void test14() {
        runTest("a P 'abc'", 1, "'abc'rdf:XMLLiteral", "");
    }

    //
    // floats & doubles are not compatible
    //
    @Test
    public void test15() {
        runTest("a P '1'xsd:float", 1, "'1'xsd:double", "");
    }

    @Test
    public void test16() {
        runTest("a P '1'xsd:double", 1, "'1'xsd:float", "");
    }

    @Test
    public void test17() {
        runTest("a P 1", 1, "'1'", "");
    }

    @Test
    public void test18() {
        runTest("a P 1", 1, "'1'xsd:integer", "'1'xsd:integer");
    }

    @Test
    public void test19() {
        runTest("a P 1", 1, "'1'", "");
    }

    @Test
    public void test20() {
        runTest("a P '1'xsd:short", 1, "'1'xsd:integer", "'1'xsd:short");
    }

    @Test
    public void test21() {
        runTest("a P '1'xsd:int", 1, "'1'xsd:integer", "'1'xsd:int");
    }

    @Test
    public void testFloatVsDouble() {
        Node A = NodeCreateUtils.create("'1'xsd:float");
        Node B = NodeCreateUtils.create("'1'xsd:double");
        assertFalse(A.equals(B));
        assertFalse(A.sameValueAs(B));
        assertFalse(B.sameValueAs(A));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testProgrammaticValues() {
        Node ab = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral((byte)42));
        Node as = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral((short)42));
        Node ai = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(42));
        Node al = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral((long)42));
        Graph g = GraphTestLib.graphWith("");
        Node SB = NodeCreateUtils.create("SB");
        Node SS = NodeCreateUtils.create("SS");
        Node SI = NodeCreateUtils.create("SI");
        Node SL = NodeCreateUtils.create("SL");
        Node P = NodeCreateUtils.create("P");
        g.add(Triple.create(SB, P, ab));
        g.add(Triple.create(SS, P, as));
        g.add(Triple.create(SI, P, ai));
        g.add(Triple.create(SL, P, al));
        assertEquals(4, Iter.toSet(g.find(Node.ANY, P, NodeCreateUtils.create("42"))).size());
    }
}
