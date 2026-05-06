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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.store.strategies.StoreStrategy;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.roaringbitmap.FastAggregation;
import org.roaringbitmap.RoaringBitmap;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Eager store strategy that indexes all triples immediately.
 * This strategy is used when the indexing strategy is set to EAGER.
 * It builds the index by adding all triples to the index at once.
 */
public class EagerStoreStrategy implements StoreStrategy {
    private static final RoaringBitmap EMPTY_BITMAP = new RoaringBitmap();

    final NodesToBitmapsMap[] spoBitmaps;
    final TripleSet triples;

    /**
     * Create a new EagerStoreStrategy and initialize the index.
     */
    public EagerStoreStrategy(final TripleSet triples, boolean parallel) {
        this(triples);
        if (parallel) {
            indexAllParallel();
        } else {
            indexAll();
        }
    }

    /**
     * Default constructor for EagerStoreStrategy.
     * Initializes the bitmaps for subjects, predicates, and objects.
     * Note: This constructor does not index any triples.
     */
    public EagerStoreStrategy(final TripleSet triples) {
        this.triples = triples;
        this.spoBitmaps = new NodesToBitmapsMap[]{
                new NodesToBitmapsMap(), // Subject bitmaps
                new NodesToBitmapsMap(), // Predicate bitmaps
                new NodesToBitmapsMap()  // Object bitmaps
        };
    }

    /**
     * Copy constructor for EagerStoreStrategy.
     * Creates a new EagerStoreStrategy that is a copy of the given strategy but with a new reference
     * to a set of triples. This set should be a copy of the original set to ensure independence of the new store.
     *
     * @param triples                   the set of triples of the new store
     * @param strategyToCopyBitmapsFrom the strategy to copy bitmaps from
     */
    public EagerStoreStrategy(final TripleSet triples, EagerStoreStrategy strategyToCopyBitmapsFrom) {
        this.triples = triples;
        this.spoBitmaps = new NodesToBitmapsMap[]{
                strategyToCopyBitmapsFrom.spoBitmaps[0].copy(), // Subject bitmaps
                strategyToCopyBitmapsFrom.spoBitmaps[1].copy(), // Predicate bitmaps
                strategyToCopyBitmapsFrom.spoBitmaps[2].copy()  // Object bitmaps
        };
    }

    /**
     * Index all triples in the store.
     * This method will add all triples to the index, creating bitmaps for subjects, predicates, and objects.
     */
    private void indexAll() {
        // Initialize the index by adding all triples to the index
        triples.forEachKey(this::addToIndex);
    }

    /**
     * Index all triples in the store in parallel.
     * This method will add all triples to the index in parallel,
     * creating bitmaps for subjects, predicates, and objects.
     */
    private void indexAllParallel() {
        final var futureIndexSubjects = CompletableFuture.runAsync(() ->
                triples.forEachKey((triple, index) ->
                        addIndex(spoBitmaps[0], triple.getSubject(), index)));

        final var futureIndexPredicates = CompletableFuture.runAsync(() ->
                triples.forEachKey((triple, index) ->
                        addIndex(spoBitmaps[1], triple.getPredicate(), index)));

        triples.forEachKey((triple, index) ->
                addIndex(spoBitmaps[2], triple.getObject(), index));

        CompletableFuture.allOf(futureIndexSubjects, futureIndexPredicates).join();
    }

    /**
     * Add an index for a given node and index in the specified map.
     * If the node does not exist in the map, it will be created.
     *
     * @param map   the map to add the index to
     * @param node  the node to add
     * @param index the index to add for the node
     */
    private static void addIndex(final NodesToBitmapsMap map, final Node node, final int index) {
        final var bitmap = map.computeIfAbsent(node, RoaringBitmap::new);
        bitmap.add(index);
    }

    /**
     * Remove an index for a given node and index in the specified map.
     * If the bitmap for the node becomes empty, the node will be removed from the map.
     *
     * @param map   the map to remove the index from
     * @param node  the node to remove
     * @param index the index to remove for the node
     */
    private static void removeIndex(final NodesToBitmapsMap map, final Node node, final int index) {
        final var bitmap = map.get(node);
        bitmap.remove(index);
        if (bitmap.isEmpty()) {
            map.removeUnchecked(node);
        }
    }

    @Override
    public void addToIndex(final Triple triple, final int index) {
        addIndex(spoBitmaps[0], triple.getSubject(), index);
        addIndex(spoBitmaps[1], triple.getPredicate(), index);
        addIndex(spoBitmaps[2], triple.getObject(), index);
    }

    @Override
    public void removeFromIndex(final Triple triple, final int index) {
        removeIndex(spoBitmaps[0], triple.getSubject(), index);
        removeIndex(spoBitmaps[1], triple.getPredicate(), index);
        removeIndex(spoBitmaps[2], triple.getObject(), index);

    }

    @Override
    public void clearIndex() {
        for (var bitmapMap : spoBitmaps) {
            bitmapMap.clear();
        }
    }

    @Override
    public boolean containsSubAnyAny(Node s) {
        return spoBitmaps[0].containsKey(s);
    }

    @Override
    public boolean containsAnyPreAny(Node p) {
        return spoBitmaps[1].containsKey(p);
    }

    @Override
    public boolean containsAnyAnyObj(Node o) {
        return spoBitmaps[2].containsKey(o);
    }

    @Override
    public boolean containsSubPreAny(Node s, Node p) {
        final var subjectBitmap = spoBitmaps[0].get(s);
        if (null == subjectBitmap)
            return false;

        final var predicateBitmap = spoBitmaps[1].get(p);
        if (null == predicateBitmap)
            return false;

        return RoaringBitmap.intersects(subjectBitmap, predicateBitmap);
    }

    @Override
    public boolean containsSubAnyObj(Node s, Node o) {
        final var subjectBitmap = spoBitmaps[0].get(s);
        if (null == subjectBitmap)
            return false;

        final var objectBitmap = spoBitmaps[2].get(o);
        if (null == objectBitmap)
            return false;

        return RoaringBitmap.intersects(subjectBitmap, objectBitmap);
    }

    @Override
    public boolean containsAnyPreObj(Node p, Node o) {
        final var predicateBitmap = spoBitmaps[1].get(p);
        if (null == predicateBitmap)
            return false;

        final var objectBitmap = spoBitmaps[2].get(o);
        if (null == objectBitmap)
            return false;

        return RoaringBitmap.intersects(predicateBitmap, objectBitmap);
    }

    @Override
    public Stream<Triple> streamSubAnyAny(Node s) {
        final var tArray = triples.getTriples();
        return spoBitmaps[0].getOrDefault(s, EMPTY_BITMAP)
                .stream().mapToObj(i -> tArray[i]);
    }

    @Override
    public Stream<Triple> streamAnyPreAny(Node p) {
        final var tArray = triples.getTriples();
        return spoBitmaps[1].getOrDefault(p, EMPTY_BITMAP)
                .stream().mapToObj(i -> tArray[i]);
    }

    @Override
    public Stream<Triple> streamAnyAnyObj(Node o) {
        final var tArray = triples.getTriples();
        return spoBitmaps[2].getOrDefault(o, EMPTY_BITMAP)
                .stream().mapToObj(i -> tArray[i]);
    }

    @Override
    public Stream<Triple> streamSubPreAny(Node s, Node p) {
        final var subjectBitmap = spoBitmaps[0].get(s);
        if (null == subjectBitmap)
            return Stream.empty();

        final var predicateBitmap = spoBitmaps[1].get(p);
        if (null == predicateBitmap)
            return Stream.empty();

        final var tArray = triples.getTriples();
        return FastAggregation.naive_and(subjectBitmap, predicateBitmap)
                .stream().mapToObj(i -> tArray[i]);
    }

    @Override
    public Stream<Triple> streamSubAnyObj(Node s, Node o) {
        final var subjectBitmap = spoBitmaps[0].get(s);
        if (null == subjectBitmap)
            return Stream.empty();

        final var objectBitmap = spoBitmaps[2].get(o);
        if (null == objectBitmap)
            return Stream.empty();

        final var tArray = triples.getTriples();
        return FastAggregation.naive_and(subjectBitmap, objectBitmap)
                .stream().mapToObj(i -> tArray[i]);
    }

    @Override
    public Stream<Triple> streamAnyPreObj(Node p, Node o) {
        final var predicateBitmap = spoBitmaps[1].get(p);
        if (null == predicateBitmap)
            return Stream.empty();

        final var objectBitmap = spoBitmaps[2].get(o);
        if (null == objectBitmap)
            return Stream.empty();

        final var tArray = triples.getTriples();
        return FastAggregation.naive_and(predicateBitmap, objectBitmap)
                .stream().mapToObj(i -> tArray[i]);
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyAny(Node s) {
        return new RoaringBitmapTripleIterator(spoBitmaps[0].getOrDefault(s, EMPTY_BITMAP), triples);
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreAny(Node p) {
        return new RoaringBitmapTripleIterator(spoBitmaps[1].getOrDefault(p, EMPTY_BITMAP), triples);
    }

    @Override
    public ExtendedIterator<Triple> findAnyAnyObj(Node o) {
        return new RoaringBitmapTripleIterator(spoBitmaps[2].getOrDefault(o, EMPTY_BITMAP), triples);
    }

    @Override
    public ExtendedIterator<Triple> findSubPreAny(Node s, Node p) {
        final var subjectBitmap = spoBitmaps[0].get(s);
        if (null == subjectBitmap)
            return NiceIterator.emptyIterator();

        final var predicateBitmap = spoBitmaps[1].get(p);
        if (null == predicateBitmap)
            return NiceIterator.emptyIterator();

        return new RoaringBitmapTripleIterator(FastAggregation.naive_and(subjectBitmap, predicateBitmap), triples);
    }

    @Override
    public ExtendedIterator<Triple> findSubAnyObj(Node s, Node o) {
        final var subjectBitmap = spoBitmaps[0].get(s);
        if (null == subjectBitmap)
            return NiceIterator.emptyIterator();

        final var objectBitmap = spoBitmaps[2].get(o);
        if (null == objectBitmap)
            return NiceIterator.emptyIterator();

        return new RoaringBitmapTripleIterator(FastAggregation.naive_and(subjectBitmap, objectBitmap), triples);
    }

    @Override
    public ExtendedIterator<Triple> findAnyPreObj(Node p, Node o) {
        final var predicateBitmap = spoBitmaps[1].get(p);
        if (null == predicateBitmap)
            return NiceIterator.emptyIterator();

        final var objectBitmap = spoBitmaps[2].get(o);
        if (null == objectBitmap)
            return NiceIterator.emptyIterator();

        return new RoaringBitmapTripleIterator(FastAggregation.naive_and(predicateBitmap, objectBitmap), triples);
    }
}
