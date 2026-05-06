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

package org.apache.jena.mem.store.indexed;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.IndexingStrategy;
import org.apache.jena.mem.pattern.PatternClassifier;
import org.apache.jena.mem.store.TripleStore;
import org.apache.jena.mem.store.strategies.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.SingletonIterator;

import java.util.stream.Stream;

/**
 * {@link TripleStore} that stores all triples in a single
 * {@link TripleSet} and delegates pattern-matching to a configurable
 * {@link StoreStrategy}. The strategy is selected via an
 * {@link IndexingStrategy} and may swap itself out at runtime (e.g. a
 * {@link LazyStoreStrategy} replaces itself with an
 * {@link EagerStoreStrategy} as soon as the first pattern lookup is
 * performed).
 * <p>
 * The triples themselves are kept in {@code triples}; each triple has a
 * stable index in that set, which the strategy uses to maintain
 * subject/predicate/object indices of integer indices rather than triple
 * references.
 */
public class IndexedSetTripleStore implements TripleStore {

    /** The flat set of stored triples. Each element has a stable integer index. */
    private final TripleSet triples;
    private StoreStrategy currentStrategy;
    private final IndexingStrategy indexingStrategy;

    /**
     * Creates an indexed store with the {@link IndexingStrategy#EAGER}
     * default indexing strategy.
     */
    public IndexedSetTripleStore() {
        this(IndexingStrategy.EAGER);
    }

    /**
     * Creates an indexed store using the given indexing strategy.
     *
     * @param indexingStrategy the indexing strategy to use
     */
    public IndexedSetTripleStore(final IndexingStrategy indexingStrategy) {
        this.triples = new TripleSet();
        this.indexingStrategy = indexingStrategy;
        this.currentStrategy = createStoreStrategy(indexingStrategy);
    }

    /**
     * Copy constructor used by {@link #copy()}. If the source store has its
     * eager index built, the copy reuses the index data structures (without
     * rebuilding them); otherwise the copy starts from the configured
     * indexing strategy.
     *
     * @param storeToCopy the source store
     */
    private IndexedSetTripleStore(final IndexedSetTripleStore storeToCopy) {
        this.triples = storeToCopy.triples.copy();
        this.indexingStrategy = storeToCopy.indexingStrategy;
        if(storeToCopy.currentStrategy instanceof EagerStoreStrategy eagerStoreStrategy) {
            currentStrategy = new EagerStoreStrategy(triples, eagerStoreStrategy); // Copy the indices from the original strategy
        } else {
            currentStrategy = createStoreStrategy(indexingStrategy);
        }
    }


    private StoreStrategy createStoreStrategy(final IndexingStrategy indexingStrategy) {
        return switch (indexingStrategy) {
            case EAGER
                    -> new EagerStoreStrategy(triples);
            case LAZY
                    -> new LazyStoreStrategy(this::setCurrentStrategyToNewEagerStoreStrategy);
            case LAZY_PARALLEL
                    -> new LazyStoreStrategy(this::setCurrentStrategyToNewEagerStoreStrategyParallel);
            case MANUAL
                    -> new ManualStoreStrategy();
            case MINIMAL
                    -> new MinimalStoreStrategy(triples);
        };
    }

    private EagerStoreStrategy setCurrentStrategyToNewEagerStoreStrategy() {
        final var eagerStoreStrategy= new EagerStoreStrategy(triples, false);
        this.currentStrategy = eagerStoreStrategy;
        return eagerStoreStrategy;
    }

    private EagerStoreStrategy setCurrentStrategyToNewEagerStoreStrategyParallel() {
        final var eagerStoreStrategy= new EagerStoreStrategy(triples, true);
        this.currentStrategy = eagerStoreStrategy;
        return eagerStoreStrategy;
    }

    /**
     * Check if the index has been initialized and all triples are indexed.
     *
     * @return true if the index is initialized, false otherwise
     */
    public boolean isIndexInitialized() {
        return currentStrategy.isIndexInitialized();
    }

    /**
     * Get the indexing strategy of this store.
     *
     * @return the indexing strategy
     */
    public IndexingStrategy getIndexingStrategy() {
        return indexingStrategy;
    }

    /**
     * Reset the current strategy to the initial one.
     */
    public void resetIndexingStrategy() {
        this.currentStrategy = createStoreStrategy(indexingStrategy);
    }

    /**
     * Initialize the index for this store.
     */
    public void initializeIndex() {
        currentStrategy = new EagerStoreStrategy(this.triples, false);
    }

    /**
     * Initialize the index for this store in parallel.
     * This will index all triples in parallel, which can be faster for large datasets.
     */
    public void initializeIndexParallel() {
        currentStrategy = new EagerStoreStrategy(this.triples, true);
    }

    @Override
    public void add(final Triple triple) {
        final var index = triples.addAndGetIndex(triple);
        if (index < 0) { /*triple already exists*/
            return;
        }
        currentStrategy.addToIndex(triple, index);
    }

    @Override
    public void remove(final Triple triple) {
        final var index = triples.removeAndGetIndex(triple);
        if (index < 0) { /*triple does not exist*/
            return;
        }
        currentStrategy.removeFromIndex(triple, index);
    }

    @Override
    public void clear() {
        this.triples.clear();
        this.currentStrategy.clearIndex();
    }

    @Override
    public int countTriples() {
        return this.triples.size();
    }

    @Override
    public boolean isEmpty() {
        return this.triples.isEmpty();
    }

    @Override
    public boolean contains(Triple tripleMatch) {
        return switch (PatternClassifier.classify(tripleMatch)) {

            case SUB_PRE_OBJ -> this.triples.containsKey(tripleMatch);

            case SUB_ANY_ANY -> currentStrategy.containsSubAnyAny(tripleMatch.getSubject());
            case ANY_PRE_ANY -> currentStrategy.containsAnyPreAny(tripleMatch.getPredicate());
            case ANY_ANY_OBJ -> currentStrategy.containsAnyAnyObj(tripleMatch.getObject());

            case SUB_PRE_ANY -> currentStrategy.containsSubPreAny(tripleMatch.getSubject(), tripleMatch.getPredicate());
            case SUB_ANY_OBJ -> currentStrategy.containsSubAnyObj(tripleMatch.getSubject(), tripleMatch.getObject());
            case ANY_PRE_OBJ -> currentStrategy.containsAnyPreObj(tripleMatch.getPredicate(), tripleMatch.getObject());

            case ANY_ANY_ANY -> !this.isEmpty();
        };
    }

    @Override
    public Stream<Triple> stream() {
        return this.triples.keyStream();
    }

    @Override
    public Stream<Triple> stream(Triple tripleMatch) {
        return switch (PatternClassifier.classify(tripleMatch)) {

            case SUB_PRE_OBJ ->
                    this.triples.containsKey(tripleMatch) ? Stream.of(tripleMatch) : Stream.empty();

            case SUB_ANY_ANY -> currentStrategy.streamSubAnyAny(tripleMatch.getSubject());
            case ANY_PRE_ANY -> currentStrategy.streamAnyPreAny(tripleMatch.getPredicate());
            case ANY_ANY_OBJ -> currentStrategy.streamAnyAnyObj(tripleMatch.getObject());

            case SUB_PRE_ANY -> currentStrategy.streamSubPreAny(tripleMatch.getSubject(), tripleMatch.getPredicate());
            case SUB_ANY_OBJ -> currentStrategy.streamSubAnyObj(tripleMatch.getSubject(), tripleMatch.getObject());
            case ANY_PRE_OBJ -> currentStrategy.streamAnyPreObj(tripleMatch.getPredicate(), tripleMatch.getObject());

            case ANY_ANY_ANY -> this.triples.keyStream();
        };
    }

    @Override
    public ExtendedIterator<Triple> find(Triple tripleMatch) {
        return switch (PatternClassifier.classify(tripleMatch)) {

            case SUB_PRE_OBJ ->
                    this.triples.containsKey(tripleMatch)
                            ? new SingletonIterator<>(tripleMatch)
                            : NiceIterator.emptyIterator();

            case SUB_ANY_ANY -> currentStrategy.findSubAnyAny(tripleMatch.getSubject());
            case ANY_PRE_ANY -> currentStrategy.findAnyPreAny(tripleMatch.getPredicate());
            case ANY_ANY_OBJ -> currentStrategy.findAnyAnyObj(tripleMatch.getObject());

            case SUB_PRE_ANY -> currentStrategy.findSubPreAny(tripleMatch.getSubject(), tripleMatch.getPredicate());
            case SUB_ANY_OBJ -> currentStrategy.findSubAnyObj(tripleMatch.getSubject(), tripleMatch.getObject());
            case ANY_PRE_OBJ -> currentStrategy.findAnyPreObj(tripleMatch.getPredicate(), tripleMatch.getObject());

            case ANY_ANY_ANY -> this.triples.keyIterator();

        };
    }

    @Override
    public IndexedSetTripleStore copy() {
        return new IndexedSetTripleStore(this);
    }
}
