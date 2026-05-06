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

package org.apache.jena.mem.store;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * Storage abstraction used by the {@code mem2} in-memory graph implementations.
 * A {@code TripleStore} is a set-like collection of {@link Triple}s that also
 * supports pattern-based lookup ({@link #find}, {@link #stream(Triple)},
 * {@link #contains}). Implementations are expected to be efficient for the
 * lookup patterns described in
 * {@link org.apache.jena.mem.pattern.MatchPattern}.
 * <p>
 * Implementations are not required to be thread-safe.
 */
public interface TripleStore extends Copyable<TripleStore> {

    /**
     * Add a triple to the store. Does nothing if the triple is already present.
     *
     * @param triple the triple to add
     */
    void add(final Triple triple);

    /**
     * Remove a triple from the store. Does nothing if the triple is not present.
     *
     * @param triple the triple to remove
     */
    void remove(final Triple triple);

    /**
     * Remove all triples from the store. After this call, {@link #isEmpty()}
     * returns {@code true} and any associated indices are emptied.
     */
    void clear();

    /**
     * Returns the number of triples in the store.
     *
     * @return the number of triples
     */
    int countTriples();

    /**
     * Returns {@code true} if the store contains no triples.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();


    /**
     * Returns {@code true} if the store contains any triple matching the given
     * pattern. The pattern may contain wildcards (e.g. {@code Node.ANY}).
     *
     * @param tripleMatch the triple pattern to match
     * @return {@code true} if at least one matching triple exists
     */
    boolean contains(final Triple tripleMatch);

    /**
     * Returns a {@link Stream} of all triples in the store.
     * The returned stream supports {@link Stream#parallel()}.
     *
     * @return a stream over every triple in this store
     */
    Stream<Triple> stream();

    /**
     * Returns a {@link Stream} of every triple in the store matching the
     * given pattern. The returned stream supports {@link Stream#parallel()}.
     *
     * @param tripleMatch the triple pattern to match (may contain wildcards)
     * @return a stream over the matching triples
     */
    Stream<Triple> stream(final Triple tripleMatch);

    /**
     * Returns an {@link ExtendedIterator} over every triple in the store
     * matching the given pattern.
     *
     * @param tripleMatch the triple pattern to match (may contain wildcards)
     * @return an iterator over the matching triples
     */
    ExtendedIterator<Triple> find(final Triple tripleMatch);

    /**
     * Returns an independent copy of this store.
     * Since {@link org.apache.jena.graph.Node}s and {@link Triple}s are
     * immutable, the copy may share node and triple instances with the
     * original; only the container/index data structures are duplicated so
     * that mutations in either store do not affect the other.
     *
     * @return an independent copy of this store
     */
    @Override
    TripleStore copy();
}