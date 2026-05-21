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

package org.apache.jena.graph.compose;

import junit.framework.TestSuite;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphTestLib;

/**
 * TestDisjointUnion - test that DisjointUnion works, as well as we can.
 */
public class TestDisjointUnion extends TestDyadic {
    public TestDisjointUnion(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(TestDisjointUnion.class);
    }

    @Override
    public Graph getNewGraph() {
        Graph gBase = GraphTestLib.graphWith(""), g1 = GraphTestLib.graphWith("");
        return new DisjointUnion(gBase, g1);
    }

    public void testEmptyUnion() {
        DisjointUnion du = new DisjointUnion(Graph.emptyGraph, Graph.emptyGraph);
        assertEquals(true, du.isEmpty());
    }

    public void testLeftUnion() {
        Graph g = GraphTestLib.graphWith("");
        testSingleComponent(g, new DisjointUnion(g, Graph.emptyGraph));
    }

    public void testRightUnion() {
        Graph g = GraphTestLib.graphWith("");
        testSingleComponent(g, new DisjointUnion(Graph.emptyGraph, g));
    }

    protected void testSingleComponent(Graph g, DisjointUnion du) {
        GraphTestLib.graphAdd(g, "x R y; a P b; x Q b");
        GraphTestLib.assertIsomorphic(g, du);
        GraphTestLib.graphAdd(g, "roses growOn you");
        GraphTestLib.assertIsomorphic(g, du);
        g.delete(GraphTestLib.triple("a P b"));
        GraphTestLib.assertIsomorphic(g, du);
    }

    public void testBothComponents() {
        Graph L = GraphTestLib.graphWith(""), R = GraphTestLib.graphWith("");
        Graph du = new DisjointUnion(L, R);
        GraphTestLib.assertIsomorphic(Graph.emptyGraph, du);
        L.add(GraphTestLib.triple("x P y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x P y"), du);
        R.add(GraphTestLib.triple("A rdf:type Route"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x P y; A rdf:type Route"), du);
    }

    public void testRemoveBoth() {
        Graph L = GraphTestLib.graphWith("x R y; a P b"), R = GraphTestLib.graphWith("x R y; p Q r");
        Graph du = new DisjointUnion(L, R);
        du.delete(GraphTestLib.triple("x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("a P b"), L);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("p Q r"), R);
    }

    public void testAddLeftOnlyIfNecessary() {
        Graph L = GraphTestLib.graphWith(""), R = GraphTestLib.graphWith("x R y");
        Graph du = new DisjointUnion(L, R);
        GraphTestLib.graphAdd(du, "x R y");
        assertEquals(true, L.isEmpty());
        GraphTestLib.graphAdd(du, " a P b");
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("a P b"), L);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), R);
    }
}
