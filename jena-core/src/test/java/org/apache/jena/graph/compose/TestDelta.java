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
import org.apache.jena.graph.AbstractTestGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.Triple;

public class TestDelta extends AbstractTestGraph {

    private static final String DEFAULT_TRIPLES = "x R y; p S q";

    public TestDelta(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(TestDelta.class);
    }

    @Override
    public Graph getNewGraph() {
        Graph gBase = GraphTestLib.graphWith("");
        return new Delta(gBase);
    }

    public void testDeltaMirrorsBase() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        GraphTestLib.assertIsomorphic(base, delta);
    }

    public void testAddGoesToAdditions() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        delta.add(GraphTestLib.triple("x R z"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), base);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R z"), delta.getAdditions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getDeletions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES + "; x R z"), delta);
    }

    public void testDeleteGoesToDeletions() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        delta.delete(GraphTestLib.triple("x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), base);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), delta.getDeletions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("p S q"), delta);
    }

    public void testRedundantAddNoOp() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        delta.add(GraphTestLib.triple("x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), base);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getAdditions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getDeletions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), delta);
    }

    public void testRedundantDeleteNoOp() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        delta.delete(GraphTestLib.triple("a T b"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), base);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getAdditions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getDeletions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), delta);
    }

    public void testAddThenDelete() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        delta.add(GraphTestLib.triple("a T b"));
        delta.delete(GraphTestLib.triple("a T b"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), base);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getAdditions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getDeletions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), delta);
    }

    public void testDeleteThenAdd() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        delta.delete(GraphTestLib.triple("p S q"));
        delta.add(GraphTestLib.triple("p S q"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), base);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getAdditions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), delta.getDeletions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), delta);
    }

    public void testAddAndDelete() {
        Graph base = GraphTestLib.graphWith(DEFAULT_TRIPLES);
        Delta delta = new Delta(base);
        delta.delete(GraphTestLib.triple("a T b"));
        delta.add(GraphTestLib.triple("x R z"));
        delta.delete(GraphTestLib.triple("p S q"));
        delta.add(GraphTestLib.triple("a T b"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(DEFAULT_TRIPLES), base);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("a T b; x R z"), delta.getAdditions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("p S q"), delta.getDeletions());
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; x R z; a T b"), delta);
    }

    public void testTerms1() {
        Triple t1 = GraphTestLib.triple("s p 1");
        Triple t01 = GraphTestLib.triple("s p 01");
        Graph base = GraphTestLib.newGraph();
        base.add(t1);
        Delta delta = new Delta(base);

        delta.add(t01);

        assertTrue(delta.getAdditions().contains(GraphTestLib.triple("s p 01")));
        assertFalse(delta.getAdditions().contains(GraphTestLib.triple("s p 1")));
        assertTrue(delta.contains(t1));
        assertTrue(delta.contains(t01));
    }

    public void testTerms2() {
        Triple t1 = GraphTestLib.triple("s p 1");
        Triple t01 = GraphTestLib.triple("s p 01");
        Graph base = GraphTestLib.newGraph();
        base.add(t1);
        Delta delta = new Delta(base);

        delta.delete(t01);

        assertFalse(delta.getDeletions().contains(GraphTestLib.triple("s p 01")));
        assertFalse(delta.getAdditions().contains(GraphTestLib.triple("s p 1")));
    }

    public void testTerms3() {
        Triple t1 = GraphTestLib.triple("s p 1");
        Triple t01 = GraphTestLib.triple("s p 01");
        Graph base = GraphTestLib.newGraph();
        base.add(t1);
        Delta delta = new Delta(base);

        delta.add(t01);
        delta.delete(t01);
        delta.delete(t1);

        assertFalse(delta.getDeletions().contains(t01));
        assertTrue(delta.getDeletions().contains(t1));
        assertFalse(delta.getDeletions().contains(t01));
        assertFalse(delta.getAdditions().contains(t01));
    }

    public void testTerms4() {
        Triple t1 = GraphTestLib.triple("s p 1");
        Triple t01 = GraphTestLib.triple("s p 01");
        Graph base = GraphTestLib.newGraph();
        Delta delta = new Delta(base);

        delta.add(t1);
        delta.delete(t01);

        assertFalse(delta.getDeletions().contains(GraphTestLib.triple("s p 01")));
        assertTrue(delta.getDeletions().isEmpty());

        assertTrue(delta.getAdditions().contains(GraphTestLib.triple("s p 1")));
        assertFalse(delta.getAdditions().isEmpty());
    }
}
