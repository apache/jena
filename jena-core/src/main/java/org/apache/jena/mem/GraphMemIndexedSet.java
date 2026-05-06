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

package org.apache.jena.mem;

import org.apache.jena.mem.store.indexed.IndexedSetTripleStore;

/**
 * In-memory {@link GraphMem} implementation that stores all triples in a single
 * indexed set ({@link IndexedSetTripleStore}). This class is not thread-safe.
 * <p>
 * Different {@link IndexingStrategy indexing strategies} can be selected to
 * balance memory usage and lookup performance. The triples themselves always live
 * in a flat set; only the auxiliary subject/predicate/object indices are
 * controlled by the strategy. See {@link IndexingStrategy} for the trade-offs of
 * each variant.
 * <p>
 * While the index has not been built (e.g. with {@link IndexingStrategy#LAZY},
 * {@link IndexingStrategy#LAZY_PARALLEL}, {@link IndexingStrategy#MANUAL} or
 * {@link IndexingStrategy#MINIMAL}) the memory footprint is very low and the
 * following operations are particularly fast:
 * <ul>
 *     <li>{@link GraphMem#add} - adds a triple to the graph</li>
 *     <li>{@link GraphMem#delete} - removes a triple from the graph</li>
 * </ul>
 * A typical bulk-load pattern is to start without an index, add all triples and
 * then call {@link #initializeIndexParallel()} to build the index in parallel.
 */
public class GraphMemIndexedSet extends GraphMem {

    private final IndexedSetTripleStore indexedSetTripleStore;

    /**
     * Creates a new graph using the {@link IndexingStrategy#EAGER} default
     * indexing strategy.
     */
    public GraphMemIndexedSet() {
        this(IndexingStrategy.EAGER);
    }

    /**
     * Creates a new graph that uses the given indexing strategy.
     *
     * @param indexingStrategy the indexing strategy to use; controls when the
     *                         subject/predicate/object index is built and how
     *                         pattern lookups are evaluated
     */
    public GraphMemIndexedSet(IndexingStrategy indexingStrategy) {
        this(new IndexedSetTripleStore(indexingStrategy));
    }

    /**
     * Internal constructor used by {@link #copy()} to wrap an already populated
     * triple store.
     *
     * @param tripleStore the triple store to wrap (must be an
     *                    {@link IndexedSetTripleStore})
     */
    private GraphMemIndexedSet(final IndexedSetTripleStore tripleStore) {
        super(tripleStore);
        this.indexedSetTripleStore = tripleStore;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns an independent copy that preserves the indexing strategy and,
     * if the source has its index built, copies the index data structures
     * directly to avoid rebuilding them.
     */
    @Override
    public GraphMemIndexedSet copy() {
        return new GraphMemIndexedSet(this.indexedSetTripleStore.copy());
    }

    /**
     * Returns the indexing strategy this graph was created with.
     * The strategy is fixed for the lifetime of the graph; clearing or
     * initializing the index does not change it.
     *
     * @return the indexing strategy
     */
    public IndexingStrategy getIndexingStrategy() {
        return indexedSetTripleStore.getIndexingStrategy();
    }

    /**
     * Drops the current subject/predicate/object index and reverts to the
     * initial strategy. Subsequent pattern lookups will trigger (re)building
     * the index according to the configured {@link IndexingStrategy}.
     */
    public void resetIndexingStrategy() {
        indexedSetTripleStore.resetIndexingStrategy();
    }

    /**
     * Build (or rebuild) the index sequentially.
     * After this call, pattern lookups will be served by the eager strategy
     * regardless of the originally configured indexing strategy.
     */
    public void initializeIndex() {
        indexedSetTripleStore.initializeIndex();
    }

    /**
     * Build (or rebuild) the index in parallel.
     * This can be substantially faster than {@link #initializeIndex()} for
     * larger graphs. After this call, pattern lookups will be served by the
     * eager strategy regardless of the originally configured indexing strategy.
     */
    public void initializeIndexParallel() {
        indexedSetTripleStore.initializeIndexParallel();
    }

    /**
     * Reports whether the index is currently built and ready to serve pattern
     * lookups directly. For graphs configured with a non-eager strategy this
     * may flip from {@code false} to {@code true} as soon as the first lookup
     * is performed (or when {@link #initializeIndex()} is called explicitly).
     *
     * @return {@code true} iff the index is initialized
     */
    public boolean isIndexInitialized() {
        return indexedSetTripleStore.isIndexInitialized();
    }
}