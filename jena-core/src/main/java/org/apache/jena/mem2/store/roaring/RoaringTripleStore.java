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

package org.apache.jena.mem2.store.roaring;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.IndexingStrategy;
import org.apache.jena.mem2.pattern.PatternClassifier;
import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.mem2.store.roaring.strategies.*;
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

    private static final String UNKNOWN_PATTERN_CLASSIFIER = "Unknown pattern classifier: %s";
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
     * @returns a new RoaringTripleStore instance
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
            default
                    -> throw new IllegalArgumentException("Unknown indexing strategy: " + indexingStrategy);
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
        final var matchPattern = PatternClassifier.classify(tripleMatch);
        switch (matchPattern) {

            case SUB_ANY_ANY,
                 ANY_PRE_ANY,
                 ANY_ANY_OBJ,
                 SUB_PRE_ANY,
                 ANY_PRE_OBJ,
                 SUB_ANY_OBJ:
                return currentStrategy.containsMatch(tripleMatch, matchPattern);

            case SUB_PRE_OBJ:
                return this.triples.containsKey(tripleMatch);

            case ANY_ANY_ANY:
                return !this.isEmpty();

            default:
                throw new IllegalStateException(String.format(UNKNOWN_PATTERN_CLASSIFIER, PatternClassifier.classify(tripleMatch)));
        }
    }

    @Override
    public Stream<Triple> stream() {
        return this.triples.keyStream();
    }

    @Override
    public Stream<Triple> stream(Triple tripleMatch) {
        var pattern = PatternClassifier.classify(tripleMatch);
        switch (pattern) {

            case SUB_PRE_OBJ:
                return this.triples.containsKey(tripleMatch) ? Stream.of(tripleMatch) : Stream.empty();

            case SUB_PRE_ANY,
                 SUB_ANY_OBJ,
                 SUB_ANY_ANY,
                 ANY_PRE_OBJ,
                 ANY_PRE_ANY,
                 ANY_ANY_OBJ:
                return this.currentStrategy.streamMatch(tripleMatch, pattern);

            case ANY_ANY_ANY:
                return this.stream();

            default:
                throw new IllegalStateException("Unknown pattern classifier: " + PatternClassifier.classify(tripleMatch));
        }
    }

    @Override
    public ExtendedIterator<Triple> find(Triple tripleMatch) {
        var pattern = PatternClassifier.classify(tripleMatch);
        switch (pattern) {

            case SUB_PRE_OBJ:
                return this.triples.containsKey(tripleMatch) ? new SingletonIterator<>(tripleMatch) : NiceIterator.emptyIterator();

            case SUB_PRE_ANY,
                 SUB_ANY_OBJ,
                 SUB_ANY_ANY,
                 ANY_PRE_OBJ,
                 ANY_PRE_ANY,
                 ANY_ANY_OBJ:
                return currentStrategy.findMatch(tripleMatch, pattern);

            case ANY_ANY_ANY:
                return this.triples.keyIterator();

            default:
                throw new IllegalStateException("Unknown pattern classifier: " + PatternClassifier.classify(tripleMatch));
        }
    }

    @Override
    public RoaringTripleStore copy() {
        return new RoaringTripleStore(this);
    }
}
