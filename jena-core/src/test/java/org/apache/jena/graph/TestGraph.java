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

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.impl.WrappedGraph;

/**
 * Tests that check GraphMem and WrappedGraph for correctness against the Graph
 * and reifier test suites.
 * <p>
 * The suites themselves are {@link MetaTestGraph}, {@link TestReifier} and
 * {@link TestGraphListener}, parameterized over {@link GraphCreators#graphs}.
 */
public class TestGraph {

    /**
     * Trivial [incomplete] test that a Wrapped graph pokes through to the underlying
     * graph.
     */
    @Test
    public void testWrappedSame() {
        Graph m = GraphMemFactory.createDefaultGraph();
        Graph w = new WrappedGraph(m);
        GraphTestLib.graphAdd(m, "a trumps b; c eats d");
        GraphTestLib.assertIsomorphic(m, w);
        GraphTestLib.graphAdd(w, "i write this; you read that");
        GraphTestLib.assertIsomorphic(w, m);
    }
}
