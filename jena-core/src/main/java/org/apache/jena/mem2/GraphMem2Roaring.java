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

package org.apache.jena.mem2;

import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.mem2.store.roaring.RoaringTripleStore;

/**
 * A graph that stores triples in memory. This class is not thread-safe.
 * This in-memory graph supports different indexing strategies to balance
 * RAM usage and performance for various operations.
 * See {@link IndexingStrategy} for details on the available strategies.
 * <p>
 * As long as the index has not been initialized, the memory consumption
 * is very low and the following operations are extremely fast:
 * <ul>
 *     <li>{@link GraphMem2#add} - adds a triple to the graph</li>
 *     <li>{@link GraphMem2#delete} - removes a triple from the graph</li>
 *</ul>
 * One could start without the index, add all triples, and then initialize the index using
 * {@link #initializeIndexParallel()} for maximum performance.
 * <p>
 * Purpose: GraphMem2Roaring is ideal for handling extremely large graphs. If you frequently work with such massive
 * data structures, this implementation could be your top choice.
 * In this case, you should not use the {@link IndexingStrategy#MINIMAL} strategy, as it is not suitable for
 * large graphs.
 * With the new strategies, this graph also works well for very small graphs, where pattern matching is not needed.
 * <p>
 * Graph#contains is faster than {@link GraphMem2Fast}.
 * Removing triples is a bit slower than {@link GraphMem2Legacy} when the index is initialized.
 * Better performance than GraphMem2Fast for operations with triple matches for the pattern S_O, SP_, and _PO on
 * large graphs, due to bit-operations to find intersecting triples.
 * Memory consumption is about 7-99% higher than {@link GraphMem2Legacy} when the index is initialized.
 * Suitable for really large graphs like bsbm-5m.nt.gz, bsbm-25m.nt.gz, and possibly even larger.
 * <p>
 * Internal structure:
 * <ul>
 *     <li>One indexed hash set (same as GraphMem2Fast uses) that holds all triples.
 *     <li>The index for pattern matching consists of three hash maps indexed by
 *         subjects, predicates, and objects with RoaringBitmaps as values.
 *     <li>The bitmaps contain the indices of the triples in the central hash set.
 * </ul>
 */
public class GraphMem2Roaring extends GraphMem2 {

    /**
     * Constructs a new GraphMem2Roaring with a default RoaringTripleStore.
     * This constructor initializes the graph with an empty triple store.
     * <p>
     * The default strategy is EAGER, because of backwards compatibility.
     * This is not necessarily the best strategy for all use cases,
     * but it reflects the behavior before introducing the indexing strategies.
     */
    public GraphMem2Roaring() {
        this(IndexingStrategy.EAGER);
    }

    /**
     * Constructs a new GraphMem2Roaring with the specified indexing strategy.
     *
     * @param indexingStrategy the indexing strategy to use for this graph
     */
    public GraphMem2Roaring(IndexingStrategy indexingStrategy) {
        super(new RoaringTripleStore(indexingStrategy));
    }

    private GraphMem2Roaring(final TripleStore tripleStore) {
        super(tripleStore);
    }

    @Override
    public GraphMem2Roaring copy() {
        return new GraphMem2Roaring(this.tripleStore.copy());
    }

    /**
     * Returns the underlying RoaringTripleStore used by this graph.
     * This method is used to access the specific features of the RoaringTripleStore.
     *
     * @return the RoaringTripleStore instance
     */
    private RoaringTripleStore getRoaringTripleStore() {
        return (RoaringTripleStore) this.tripleStore;
    }

    /**
     * Returns the indexing strategy used by this graph.
     *
     * @return the indexing strategy
     */
    public IndexingStrategy getIndexingStrategy() {
        return getRoaringTripleStore().getIndexingStrategy();
    }

    /**
     * Clear the index of this graph.
     * This will remove all triples from the index and reset the current strategy to the initial one.
     */
    public void clearIndex() {
        getRoaringTripleStore().clearIndex();
    }

    /**
     * Initialize the index of this graph.
     * This will build the index based on the current set of triples.
     * After this call, the graph will behave like an EAGER indexed graph.
     */
    public void initializeIndex() {
        getRoaringTripleStore().initializeIndex();
    }

    /**
     * Initialize the index of this graph in parallel.
     * This will build the index based on the current set of triples using parallel processing.
     * After this call, the graph will behave like an EAGER indexed graph.
     */
    public void initializeIndexParallel() {
        getRoaringTripleStore().initializeIndexParallel();
    }

    /**
     * Check if the index of this graph is initialized.
     * This method returns true if the index has been initialized and is ready for use.
     *
     * @return true if the index is initialized, false otherwise
     */
    public boolean isIndexInitialized() {
        return getRoaringTripleStore().isIndexInitialized();
    }
}
