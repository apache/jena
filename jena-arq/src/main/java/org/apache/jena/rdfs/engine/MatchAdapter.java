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

package org.apache.jena.rdfs.engine;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * This is the bridge between the Node/Triple level and some lower level such as one based on NodeIds.
 */
public final class MatchAdapter<X, T>
    implements Match<Node, Triple>
{
    private Match<X, T> below;
    private MapperX<X, T> mapper;

    public MatchAdapter(Match<X, T> below, MapperX<X, T> mapper) {
        super();
        this.mapper = Objects.requireNonNull(mapper);
        this.below = Objects.requireNonNull(below);
    }

    @Override
    public Stream<Triple> match(Node s, Node p, Node o) {
        X sd = down(s);
        X pd = down(p);
        X od = down(o);
        return below.match(sd, pd, od).map(this::up);
    }

    private X down(Node node) {
        return mapper.fromNode(node);
    }

    private Triple up(T tuple) {
        X sd = mapper.subject(tuple);
        X pd = mapper.predicate(tuple);
        X od = mapper.object(tuple);
        Node s = mapper.toNode(sd);
        Node p = mapper.toNode(pd);
        Node o = mapper.toNode(od);
        return dstCreate(s, p, o);
    }

    private Triple dstCreate(Node s, Node p, Node o) {
        return Triple.create(s, p, o);
    }

    @Override
    public MapperX<Node, Triple> getMapper() {
        return Mappers.mapperTriple();
    }
}
