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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.mem.GraphMemFast;
import org.apache.jena.mem.GraphMemLegacy;
import org.apache.jena.mem.GraphMemRoaring;
import org.apache.jena.memvalue.GraphMemValue;

/**
 * The graph implementations the general graph tests run against.
 * <p>
 * The JUnit 3 suite built one test case per test method per implementation, by
 * reflection. A parameterized test class covers all five in one place.
 */
@SuppressWarnings("deprecation")
public class GraphCreators {

    private static Arguments named(String name, Supplier<Graph> maker) {
        return Arguments.of(Named.of(name, maker));
    }

    /** The five implementations the general graph tests are run against. */
    public static Stream<Arguments> graphs() {
        return Stream.of(named("GraphMemValue",   GraphMemValue::new),
                         named("WrappedGraphMem", ()->new WrappedGraph(GraphMemFactory.createDefaultGraph())),
                         named("GraphMemFast",    GraphMemFast::new),
                         named("GraphMemLegacy",  GraphMemLegacy::new),
                         named("GraphMemRoaring", GraphMemRoaring::new));
    }

    /** Just {@code GraphMemFast} - the implementation {@link TestGraphListener} uses for its copy. */
    public static Stream<Arguments> graphMemFast() {
        return Stream.of(named("GraphMemFast", GraphMemFast::new));
    }
}
