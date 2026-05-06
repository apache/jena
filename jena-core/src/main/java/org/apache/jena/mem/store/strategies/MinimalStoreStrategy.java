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

package org.apache.jena.mem.store.strategies;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.collection.FastHashSet;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * {@link StoreStrategy} that never builds an index but still answers
 * pattern-match operations - by linearly filtering the triple set. Useful
 * when the dataset is small or when memory is more precious than match-time
 * performance.
 * <p>
 * Used to back {@link org.apache.jena.mem.IndexingStrategy#MINIMAL}. The
 * user can switch to eager indexing at any time by calling
 * {@link org.apache.jena.mem.GraphMemIndexedSet#initializeIndex()}; calling
 * {@code clearIndex} reverts to filtering again.
 */
public class MinimalStoreStrategy implements StoreStrategy {
    private final FastHashSet<Triple> triples;

    /**
     * @param triples the canonical triple set to filter against
     */
    public MinimalStoreStrategy(final FastHashSet<Triple> triples) {
        this.triples = triples;
    }

    @Override
    public void addToIndex(final Triple triple, final int index) {
        // No-op, as we do not store any indices
    }

    @Override
    public void removeFromIndex(final Triple triple, final int index) {
        // No-op, as we do not store any indices
    }

    @Override
    public void clearIndex() {
        // No-op, as we do not store any indices
    }

    @Override
    public boolean containsSubAnyAny(Node s) {
        return containsMatch(Triple.create(s, Node.ANY, Node.ANY));
    }

    @Override
    public boolean containsAnyPreAny(Node p) {
        return containsMatch(Triple.create(Node.ANY, p, Node.ANY));
    }

    @Override
    public boolean containsAnyAnyObj(Node o) {
        return containsMatch(Triple.create(Node.ANY, Node.ANY, o));
    }

    @Override
    public boolean containsSubPreAny(Node s, Node p) {
        return containsMatch(Triple.create(s, p, Node.ANY));
    }

    @Override
    public boolean containsSubAnyObj(Node s, Node o) {
        return containsMatch(Triple.create(s, Node.ANY, o));
    }

    @Override
    public boolean containsAnyPreObj(Node p, Node o) {
        return containsMatch(Triple.create(Node.ANY, p, o));
    }

    @Override
    public Stream<Triple> streamSubAnyAny(Node s) {
        return streamMatch(Triple.create(s, Node.ANY, Node.ANY));
    }

    @Override
    public Stream<Triple> streamAnyPreAny(Node p) {
        return streamMatch(Triple.create(Node.ANY, p, Node.ANY));
    }

    @Override
    public Stream<Triple> streamAnyAnyObj(Node o) {
        return streamMatch(Triple.create(Node.ANY, Node.ANY, o));
    }

    @Override
    public Stream<Triple> streamSubPreAny(Node s, Node p) {
        return streamMatch(Triple.create(s, p, Node.ANY));
    }

    @Override
    public Stream<Triple> streamSubAnyObj(Node s, Node o) {
        return streamMatch(Triple.create(s, Node.ANY, o));
    }

    @Override
    public Stream<Triple> streamAnyPreObj(Node p, Node o) {
        return streamMatch(Triple.create(Node.ANY, p, o));
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyAny(Node s) {
        return findMatch(Triple.create(s, Node.ANY, Node.ANY));
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreAny(Node p) {
        return findMatch(Triple.create(Node.ANY, p, Node.ANY));
    }

    @Override
    public ExtendedIterator<Triple> findAnyAnyObj(Node o) {
        return findMatch(Triple.create(Node.ANY, Node.ANY, o));
    }

    @Override
    public ExtendedIterator<Triple> findSubPreAny(Node s, Node p) {
        return findMatch(Triple.create(s, p, Node.ANY));
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyObj(Node s, Node o) {
        return findMatch(Triple.create(s, Node.ANY, o));
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreObj(Node p, Node o) {
        return findMatch(Triple.create(Node.ANY, p, o));
    }

    private boolean containsMatch(final Triple tripleMatch) {
        return this.triples.anyMatch(tripleMatch::matches);
    }

    private Stream<Triple> streamMatch(final Triple tripleMatch) {
        return this.triples.keyStream().filter(tripleMatch::matches);
    }

    private ExtendedIterator<Triple> findMatch(final Triple tripleMatch) {
        return this.triples.keyIterator().filterKeep(tripleMatch::matches);
    }
}
