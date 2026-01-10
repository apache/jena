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

package org.apache.jena.rdfs.engine;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * A {@link Match} view over a {@link Graph}.
 * This class is final. Use {@link MatchWrapper} to modify match behavior.
 */
public final class MatchGraph
    implements Match<Node, Triple>
{
    private Graph base;

    public MatchGraph(Graph base) {
        super();
        this.base = Objects.requireNonNull(base);
    }

    public Graph getGraph() {
        return base;
    }

    @Override
    public Stream<Triple> match(Node s, Node p, Node o) {
        return base.stream(s, p, o);
    }

    @Override
    public MapperX<Node, Triple> getMapper() {
        return Mappers.mapperTriple();
    }
}
