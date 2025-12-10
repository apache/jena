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

package org.apache.jena.mem.store.roaring.strategies;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.pattern.MatchPattern;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * The store strategy defines how triples are indexed and how matches are found.
 * It is used to implement different indexing strategies like Eager, Lazy, Manual, and Minimal.
 * For the matching operations, only matches for the patterns SUB_ANY_ANY, ANY_PRE_ANY, ANY_ANY_OBJ,
 * SUB_PRE_ANY, ANY_PRE_OBJ, and SUB_ANY_OBJ are supported.
 * The patterns SUB_PRE_OBJ and ANY_ANY_ANY are not supported by the store strategies.
 */
public interface StoreStrategy {
    /**
     * Add a triple to the index if the cuurent strategy supports indexing.
     *
     * @param triple the triple to add
     * @param index  the index of the triple in the store
     */
    void addToIndex(Triple triple, int index);

    /**
     * Remove a triple from the index if the current strategy supports indexing.
     *
     * @param triple the triple to remove
     * @param index  the index of the triple in the store
     */
    void removeFromIndex(Triple triple, int index);

    /**
     * Clear the index of this store if the current strategy supports indexing.
     * This will remove all triples from the index.
     */
    void clearIndex();

    /**
     * Check if the index contains a match for the given triple and pattern.
     * This is used to quickly check if a triple matches a given pattern without retrieving the triples.
     *
     * @param tripleMatch the triple to match
     * @param pattern     the pattern to match against
     * @return true if there is a match, false otherwise
     */
    boolean containsMatch(Triple tripleMatch, MatchPattern pattern);

    /**
     * Stream the triples that match the given triple and pattern.
     * This is used to retrieve the triples that match a given pattern.
     *
     * @param tripleMatch the triple to match
     * @param pattern     the pattern to match against
     * @return a stream of triples that match the given pattern
     */
    Stream<Triple> streamMatch(Triple tripleMatch, MatchPattern pattern);

    /**
     * Find the triples that match the given triple and pattern.
     * This is used to retrieve the triples that match a given pattern as an iterator.
     *
     * @param tripleMatch the triple to match
     * @param pattern     the pattern to match against
     * @return an iterator over the triples that match the given pattern
     */
    ExtendedIterator<Triple> findMatch(Triple tripleMatch, MatchPattern pattern);
}
