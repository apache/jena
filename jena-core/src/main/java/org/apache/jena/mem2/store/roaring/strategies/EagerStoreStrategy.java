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

package org.apache.jena.mem2.store.roaring.strategies;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.pattern.MatchPattern;
import org.apache.jena.mem2.pattern.PatternClassifier;
import org.apache.jena.mem2.store.roaring.NodesToBitmapsMap;
import org.apache.jena.mem2.store.roaring.RoaringBitmapTripleIterator;
import org.apache.jena.mem2.store.roaring.TripleSet;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.roaringbitmap.FastAggregation;
import org.roaringbitmap.ImmutableBitmapDataProvider;
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
    private static final String UNSUPPORTED_PATTERN_CLASSIFIER = "Unsupported pattern classifier: %s";

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
        triples.indexedKeyIterator().forEachRemaining(entry ->
                addToIndex(entry.key(), entry.index()));
    }

    /**
     * Index all triples in the store in parallel.
     * This method will add all triples to the index in parallel,
     * creating bitmaps for subjects, predicates, and objects.
     */
    private void indexAllParallel() {
        final var futureIndexSubjects = CompletableFuture.runAsync(() ->
                triples.indexedKeyIterator().forEachRemaining(entry ->
                        addIndex(spoBitmaps[0], entry.key().getSubject(), entry.index())));

        final var futureIndexPredicates = CompletableFuture.runAsync(() ->
                triples.indexedKeyIterator().forEachRemaining(entry ->
                        addIndex(spoBitmaps[1], entry.key().getPredicate(), entry.index())));

        triples.indexedKeyIterator().forEachRemaining(entry ->
                addIndex(spoBitmaps[2], entry.key().getObject(), entry.index()));

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
    public boolean containsMatch(final Triple tripleMatch, final MatchPattern pattern) {
        switch (pattern) {

            case SUB_ANY_ANY:
                return spoBitmaps[0].containsKey(tripleMatch.getSubject());
            case ANY_PRE_ANY:
                return spoBitmaps[1].containsKey(tripleMatch.getPredicate());
            case ANY_ANY_OBJ:
                return spoBitmaps[2].containsKey(tripleMatch.getObject());

            case SUB_PRE_ANY: {
                final var subjectBitmap = spoBitmaps[0].get(tripleMatch.getSubject());
                if (null == subjectBitmap)
                    return false;

                final var predicateBitmap = spoBitmaps[1].get(tripleMatch.getPredicate());
                if (null == predicateBitmap)
                    return false;

                return RoaringBitmap.intersects(subjectBitmap, predicateBitmap);
            }

            case ANY_PRE_OBJ: {
                final var predicateBitmap = spoBitmaps[1].get(tripleMatch.getPredicate());
                if (null == predicateBitmap)
                    return false;

                final var objectBitmap = spoBitmaps[2].get(tripleMatch.getObject());
                if (null == objectBitmap)
                    return false;

                return RoaringBitmap.intersects(objectBitmap, predicateBitmap);
            }

            case SUB_ANY_OBJ: {
                final var subjectBitmap = spoBitmaps[0].get(tripleMatch.getSubject());
                if (null == subjectBitmap)
                    return false;

                final var objectBitmap = spoBitmaps[2].get(tripleMatch.getObject());
                if (null == objectBitmap)
                    return false;

                return RoaringBitmap.intersects(subjectBitmap, objectBitmap);
            }

            default:
                throw new IllegalStateException(String.format(UNSUPPORTED_PATTERN_CLASSIFIER, PatternClassifier.classify(tripleMatch)));
        }
    }

    @Override
    public Stream<Triple> streamMatch(final Triple tripleMatch, final MatchPattern pattern) {
        return this.getBitmapForMatch(tripleMatch, pattern)
                .stream().mapToObj(triples::getKeyAt);
    }

    @Override
    public ExtendedIterator<Triple> findMatch(final Triple tripleMatch, final MatchPattern pattern) {
        return new RoaringBitmapTripleIterator(this.getBitmapForMatch(tripleMatch, pattern), triples);
    }

    /**
     * Get the bitmap for the given triple match and pattern.
     * This method retrieves the appropriate bitmap based on the match pattern.
     *
     * @param tripleMatch  the triple to match
     * @param matchPattern the pattern to match against
     * @return the bitmap for the match
     */
    private ImmutableBitmapDataProvider getBitmapForMatch(final Triple tripleMatch, final MatchPattern matchPattern) {
        switch (matchPattern) {

            case SUB_ANY_ANY:
                return spoBitmaps[0].getOrDefault(tripleMatch.getSubject(), EMPTY_BITMAP);
            case ANY_PRE_ANY:
                return spoBitmaps[1].getOrDefault(tripleMatch.getPredicate(), EMPTY_BITMAP);
            case ANY_ANY_OBJ:
                return spoBitmaps[2].getOrDefault(tripleMatch.getObject(), EMPTY_BITMAP);

            case SUB_PRE_ANY: {
                final var subjectBitmap = spoBitmaps[0].get(tripleMatch.getSubject());
                if (null == subjectBitmap)
                    return EMPTY_BITMAP;

                final var predicateBitmap = spoBitmaps[1].get(tripleMatch.getPredicate());
                if (null == predicateBitmap)
                    return EMPTY_BITMAP;

                return FastAggregation.naive_and(subjectBitmap, predicateBitmap);
            }

            case ANY_PRE_OBJ: {
                final var predicateBitmap = spoBitmaps[1].get(tripleMatch.getPredicate());
                if (null == predicateBitmap)
                    return EMPTY_BITMAP;

                final var objectBitmap = spoBitmaps[2].get(tripleMatch.getObject());
                if (null == objectBitmap)
                    return EMPTY_BITMAP;

                return FastAggregation.naive_and(predicateBitmap, objectBitmap);
            }

            case SUB_ANY_OBJ: {
                final var subjectBitmap = spoBitmaps[0].get(tripleMatch.getSubject());
                if (null == subjectBitmap)
                    return EMPTY_BITMAP;

                final var objectBitmap = spoBitmaps[2].get(tripleMatch.getObject());
                if (null == objectBitmap)
                    return EMPTY_BITMAP;

                return FastAggregation.naive_and(subjectBitmap, objectBitmap);
            }

            default:
                throw new IllegalStateException(String.format(UNSUPPORTED_PATTERN_CLASSIFIER, PatternClassifier.classify(tripleMatch)));
        }
    }

}
