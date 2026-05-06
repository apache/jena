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

package org.apache.jena.mem.store.roaring;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.IndexingStrategy;
import org.apache.jena.mem.pattern.PatternClassifier;
import org.apache.jena.mem.store.TripleStore;
import org.apache.jena.mem.store.strategies.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.SingletonIterator;
import org.roaringbitmap.FastAggregation;
import org.roaringbitmap.RoaringBitmap;

import java.util.stream.Stream;

/**
 * A triple store that is ideal for handling extremely large graphs.
 * With the new indexing strategies, it also works well for very small graphs,
 * where pattern matching is not needed.
 * <p>
 * This store supports different indexing strategies to balance RAM usage and performance for various operations.
 * See {@link IndexingStrategy} for details on the available strategies.
 * <p>
 * Internal structure:
 * <ul>
 *     <li> One indexed hash set (same as GraphMem2Fast uses) that holds all triples
 *     <li> The index consists of three hash maps indexed by subjects, predicates, and objects
 *          with RoaringBitmaps as values
 *     <li> The bitmaps contain the indices of the triples in the central hash set
 * </ul>
 * <p>
 * The bitmaps are used to quickly find triples that match a given pattern.
 * The bitmap operations like {@link FastAggregation#naive_and(RoaringBitmap...)} and
 * {@link RoaringBitmap#intersects(RoaringBitmap, RoaringBitmap)} are used to find matches for the pattern
 * S_O, SP_, and _PO pretty fast, even in large graphs.
 */
public class RoaringTripleStore implements TripleStore {

    final TripleSet triples; // In this special set, each element has an index
    private StoreStrategy currentStrategy;
    private final IndexingStrategy indexingStrategy;

    /**
     * Create a new RoaringTripleStore with the default indexing strategy (EAGER).
     * <p>
     * The default strategy is EAGER, because of backwards compatibility.
     * This is not necessarily the best strategy for all use cases,
     * but it reflects the behavior before introducing the indexing strategies.
     */
    public RoaringTripleStore() {
        this(IndexingStrategy.EAGER);
    }

    /**
     * Create a new RoaringTripleStore with the given indexing strategy.
     *
     * @param indexingStrategy the indexing strategy to use
     */
    public RoaringTripleStore(final IndexingStrategy indexingStrategy) {
        this.triples = new TripleSet();
        this.indexingStrategy = indexingStrategy;
        this.currentStrategy = createStoreStrategy(indexingStrategy);
    }
    /**
     * Copy constructor to create a new RoaringTripleStore instance
     */
    private RoaringTripleStore(final RoaringTripleStore storeToCopy) {
        this.triples = storeToCopy.triples.copy();
        this.indexingStrategy = storeToCopy.indexingStrategy;
        if(storeToCopy.currentStrategy instanceof EagerStoreStrategy eagerStoreStrategy) {
            currentStrategy = new EagerStoreStrategy(triples, eagerStoreStrategy); // Copy the bitmaps from the original strategy
        } else {
            currentStrategy = createStoreStrategy(indexingStrategy);
        }
    }


    /**
     * Create a new RoaringTripleStore with the given indexing strategy and an initial capacity.
     *
     * @param indexingStrategy the indexing strategy to use
     * @return a new RoaringTripleStore instance
     */
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
     * Check if the index of this store is initialized.
     * This will return true if the current strategy is EagerStoreStrategy,
     * which means that the index has been initialized and all triples are indexed.
     *
     * @return true if the index is initialized, false otherwise
     */
    public boolean isIndexInitialized() {
        return currentStrategy instanceof EagerStoreStrategy;
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
     * Clear the index of this store.
     * This will remove all triples from the index and reset the current strategy to the initial one.
     */
    public void clearIndex() {
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
    public RoaringTripleStore copy() {
        return new RoaringTripleStore(this);
    }
}
