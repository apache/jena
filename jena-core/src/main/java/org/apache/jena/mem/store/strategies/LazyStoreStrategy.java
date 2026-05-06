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
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * {@link StoreStrategy} that defers index construction until the first
 * pattern lookup. Add/remove are no-ops while the index is absent (the
 * triples are still maintained in the enclosing
 * {@link org.apache.jena.mem.store.TripleStore} but no
 * subject/predicate/object index is updated). On the first
 * {@code containsMatch}/{@code streamMatch}/{@code findMatch} call, the
 * supplied callback is invoked to build (and install) an
 * {@link org.apache.jena.mem.IndexingStrategy#EAGER} implementation;
 * the lookup is then forwarded to it.
 * <p>
 * Used to back both {@link org.apache.jena.mem.IndexingStrategy#LAZY} and
 * {@link org.apache.jena.mem.IndexingStrategy#LAZY_PARALLEL}; the
 * sequential / parallel choice is encoded in the supplied callback.
 */
public class LazyStoreStrategy implements StoreStrategy {

    private final Supplier<StoreStrategy> setCurrentStrategyToNewEagerStoreStrategy;

    /**
     * @param setCurrentStrategyToNewEagerStoreStrategy callback that builds
     *        an {@link org.apache.jena.mem.IndexingStrategy#EAGER} strategy, installs it as the enclosing
     *        store's current strategy, and returns it so this strategy can
     *        delegate the triggering lookup to it
     */
    public LazyStoreStrategy(final Supplier<StoreStrategy> setCurrentStrategyToNewEagerStoreStrategy) {
        this.setCurrentStrategyToNewEagerStoreStrategy = setCurrentStrategyToNewEagerStoreStrategy;
    }

    @Override
    public void addToIndex(final Triple triple, final int index) {
        // No-op, as there is no index to add to.
    }

    @Override
    public void removeFromIndex(final Triple triple, final int index) {
        // No-op, as there is no index to add to.
    }

    @Override
    public void clearIndex() {
        // No-op, as there is no index to add to.
    }

    @Override
    public boolean containsSubAnyAny(Node s) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .containsSubAnyAny(s);
    }

    @Override
    public boolean containsAnyPreAny(Node p) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .containsAnyPreAny(p);
    }

    @Override
    public boolean containsAnyAnyObj(Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .containsAnyAnyObj(o);
    }

    @Override
    public boolean containsSubPreAny(Node s, Node p) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .containsSubPreAny(s, p);
    }

    @Override
    public boolean containsSubAnyObj(Node s, Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .containsSubAnyObj(s, o);
    }

    @Override
    public boolean containsAnyPreObj(Node p, Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .containsAnyPreObj(p, o);
    }

    @Override
    public Stream<Triple> streamSubAnyAny(Node s) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .streamSubAnyAny(s);
    }

    @Override
    public Stream<Triple> streamAnyPreAny(Node p) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .streamAnyPreAny(p);
    }

    @Override
    public Stream<Triple> streamAnyAnyObj(Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .streamAnyAnyObj(o);
    }

    @Override
    public Stream<Triple> streamSubPreAny(Node s, Node p) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .streamSubPreAny(s, p);
    }

    @Override
    public Stream<Triple> streamSubAnyObj(Node s, Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .streamSubAnyObj(s, o);
    }

    @Override
    public Stream<Triple> streamAnyPreObj(Node p, Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .streamAnyPreObj(p, o);
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyAny(Node s) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .findSubAnyAny(s);
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreAny(Node p) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .findAnyPreAny(p);
    }

    @Override
    public ExtendedIterator<Triple> findAnyAnyObj(Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .findAnyAnyObj(o);
    }

    @Override
    public ExtendedIterator<Triple> findSubPreAny(Node s, Node p) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .findSubPreAny(s, p);
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyObj(Node s, Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .findSubAnyObj(s, o);
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreObj(Node p, Node o) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .findAnyPreObj(p, o);
    }
}
