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

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * A Graph view over a {@link Match}. A graph can be specified as a delegate
 * for all functionality that is not covered by the Match.
 */
public class GraphMatch extends GraphWrapper {
    private final Match<Node, Triple> source;

    public GraphMatch(Graph graph, Match<Node, Triple> match) {
        super(graph);
        this.source = match;
    }

    /**
     * Wrap a base graph such that its find() and contains() methods
     * are delegated to the match.
     * Other methods, such as those for updates, go to the base graph.
     */
    public static <X, T> Graph adapt(Graph baseGraph, Match<X, T> match) {
        return new GraphMatch(baseGraph, new MatchAdapter<>(match, match.getMapper()));
    }

    public Match<Node, Triple> getMatch() {
        return source;
    }

    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        return find(m.getSubject(), m.getPredicate(), m.getObject());
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Stream<Triple> stream = source.match(s, p, o);
        ExtendedIterator<Triple> iter = WrappedIterator.ofStream(stream);
        return iter;
    }

    @Override
    public Stream<Triple> stream(Node s, Node p, Node o) {
        return source.match(s, p, o);
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return source.contains(s, p, o);
    }

    @Override
    public boolean contains(Triple t) {
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }

    @Override
    public int size() {
        // Report the size of the underlying graph.
        // Even better, don't ask.
        return super.size();
    }
}
