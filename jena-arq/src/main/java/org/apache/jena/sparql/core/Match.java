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

package org.apache.jena.sparql.core;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/** Match triples, quads, with wildcard rules (null or {@link Node#ANY} are wildcards).  */
public class Match {
    /**
     * Match a quad. A quad matches g/s/p/o if each component matches the corresponding node.
     */
    public static boolean match(Quad quad, Node g, Node s, Node p, Node o) {
        return
            match(quad.getGraph(), g) &&
            match(quad.getSubject(), s) &&
            match(quad.getPredicate(), p) &&
            match(quad.getObject(), o);
    }

    /**
     * Match a triple. A triple matches s/p/o if each component matches the corresponding node.
     */
    public static boolean match(Triple triple, Node s, Node p, Node o) {
        return
            match(triple.getSubject(), s) &&
            match(triple.getPredicate(), p) &&
            match(triple.getObject(), o);
    }

    /**
     * Match a node (non-null) with a pattern node.
     * Returns true if:
     * <ul>
     * <li>pattern is null
     * <li>pattern is {@code Node.ANY}
     * <li>pattern is concrete and .equals the node.
     * </ul>
     */
    public static boolean match(Node node, Node pattern) {
        return pattern == null || pattern == Node.ANY || pattern.equals(node);
    }

    /**
     * Match a node (non-null) with a pattern node.
     * Returns true if:
     * <ul>
     * <li>pattern is null
     * <li>pattern is {@code Node.ANY}
     * <li>pattern is concrete and sameValueAs the node.
     * </ul>
     */
    public static boolean matchValue(Node node, Node pattern) {
        Objects.requireNonNull(node);
        return pattern == null || pattern == Node.ANY || pattern.sameValueAs(node);
    }

    /** Return a filter stream of matches for triples in the collection. */
    public static Stream<Triple> match(Collection<Triple> triples, Node s, Node p, Node o) {
        return triples.stream().filter(t -> match(t, s, p, o));
    }

    /** Return a filter stream of matches for quads in the collection. */
    public static Stream<Quad> match(Collection<Quad> quads, Node g, Node s, Node p, Node o) {
        return quads.stream().filter(q -> match(q, g, s, p, o));
    }
}
