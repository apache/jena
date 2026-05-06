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

import java.util.stream.Stream;

/**
 * {@link StoreStrategy} that never builds an index automatically.
 * Add/remove/clear are no-ops on the index side; pattern-match operations
 * throw {@link UnsupportedOperationException} until the user explicitly
 * initializes the index (typically via
 * {@link org.apache.jena.mem.GraphMemIndexedSet#initializeIndex()} or
 * {@link org.apache.jena.mem.GraphMemIndexedSet#initializeIndexParallel()}),
 * which swaps this strategy out for an {@link org.apache.jena.mem.IndexingStrategy#EAGER} strategy.
 * <p>
 * Used to back {@link org.apache.jena.mem.IndexingStrategy#MANUAL}.
 */
public class ManualStoreStrategy implements StoreStrategy {
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
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public boolean containsAnyPreAny(Node p) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public boolean containsAnyAnyObj(Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public boolean containsSubPreAny(Node s, Node p) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public boolean containsSubAnyObj(Node s, Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public boolean containsAnyPreObj(Node p, Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public Stream<Triple> streamSubAnyAny(Node s) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public Stream<Triple> streamAnyPreAny(Node p) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public Stream<Triple> streamAnyAnyObj(Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public Stream<Triple> streamSubPreAny(Node s, Node p) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public Stream<Triple> streamSubAnyObj(Node s, Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public Stream<Triple> streamAnyPreObj(Node p, Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyAny(Node s) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreAny(Node p) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public ExtendedIterator<Triple> findAnyAnyObj(Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public ExtendedIterator<Triple> findSubPreAny(Node s, Node p) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyObj(Node s, Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreObj(Node p, Node o) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }
}
