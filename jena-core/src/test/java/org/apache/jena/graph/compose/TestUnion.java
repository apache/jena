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

public class TestUnion extends TestDyadic {
    public TestUnion(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(TestUnion.class);
    }

    @Override
    public Graph getNewGraph() {
        Graph gBase = GraphTestLib.graphWith(""), g1 = GraphTestLib.graphWith("");
        return new Union(gBase, g1);
    }

    public Union unionOf(String s1, String s2) {
        return new Union(GraphTestLib.graphWith(s1), GraphTestLib.graphWith(s2));
    }

    public void testStaticUnion() {
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), unionOf("", ""));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), unionOf("x R y", ""));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), unionOf("", "x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; x R z"), unionOf("x R y", "x R z"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), unionOf("x R y", "x R y"));
    }

    public void testUnionReflectsChangesToOperands() {
        Graph l = GraphTestLib.graphWith("x R y");
        Graph r = GraphTestLib.graphWith("x R y");
        Union u = new Union(l, r);

        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), u);

        l.add(GraphTestLib.triple("x R z"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; x R z"), u);

        l.delete(GraphTestLib.triple("x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; x R z"), u);

        r.add(GraphTestLib.triple("p S q"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; x R z; p S q"), u);

        r.delete(GraphTestLib.triple("x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R z; p S q"), u);
    }

    public void testAdd() {
        Graph l = GraphTestLib.graphWith("x R y");
        Graph r = GraphTestLib.graphWith("x R y; p S q");
        Union u = new Union(l, r);

        u.add(GraphTestLib.triple("x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), l);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; p S q"), r);

        u.add(GraphTestLib.triple("p S q"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; p S q"), l);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; p S q"), r);

        u.add(GraphTestLib.triple("r A s"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; p S q; r A s"), l);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; p S q"), r);
    }

    public void testDelete() {
        Graph l = GraphTestLib.graphWith("x R y; x R z");
        Graph r = GraphTestLib.graphWith("x R y; p S q");
        Union u = new Union(l, r);

        u.delete(GraphTestLib.triple("r A s"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; x R z"), l);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; p S q"), r);

        u.delete(GraphTestLib.triple("x R z"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), l);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y; p S q"), r);

        u.delete(GraphTestLib.triple("p S q"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), l);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith("x R y"), r);

        u.delete(GraphTestLib.triple("x R y"));
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), l);
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(""), r);
    }
}
