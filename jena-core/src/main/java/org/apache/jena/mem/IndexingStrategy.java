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

import org.apache.jena.graph.Graph;

/**
 * Indexing strategies supported by {@link org.apache.jena.mem.store.indexed.IndexedSetTripleStore}
 * and {@link org.apache.jena.mem.store.roaring.RoaringTripleStore}.
 * The indexing strategy determines how (and when) the auxiliary
 * subject/predicate/object index is maintained for pattern-matching operations.
 * <p>
 * The graph always keeps a flat set of triples. Operations that do not involve
 * pattern matching (size, iterating all triples, lookup of a fully concrete
 * triple, etc.) are evaluated directly against this set and are unaffected by
 * the indexing strategy.
 * <p>
 * Pattern matching refers to {@link Graph#find}, {@link Graph#remove} or
 * {@link Graph#contains} called with a triple pattern such as
 * {@code S__}, {@code SP_}, {@code S_O}, {@code _P_}, {@code _PO} or
 * {@code __O} (where {@code _} denotes a wildcard).
 * Lookups for fully concrete triples ({@code SPO}) are always answered
 * directly from the triple set and never use the index.
 */
public enum IndexingStrategy {

    /**
     * The index is always present.
     * {@link Graph#add}, {@link Graph#delete} and {@link Graph#clear()} update
     * the index immediately. Calling {@code clearIndex} simply discards the
     * existing index, which is then rebuilt from the triple set.
     */
    EAGER,

    /**
     * The index is built on demand the first time a pattern match is requested.
     * Once built, behaves like {@link #EAGER}. Calling {@code clearIndex}
     * discards the index; it will be rebuilt on demand the next time a
     * pattern match is performed.
     */
    LAZY,

    /**
     * Like {@link #LAZY}, but the on-demand index build uses parallel
     * processing for faster initialization on large graphs.
     */
    LAZY_PARALLEL,

    /**
     * The index is never built automatically. Pattern-match operations throw
     * an {@link UnsupportedOperationException} until the index is initialized
     * explicitly (e.g. via
     * {@link org.apache.jena.mem.GraphMemIndexedSet#initializeIndex()}).
     * After initialization, behaves like {@link #EAGER}.
     */
    MANUAL,

    /**
     * No index is built. Pattern-match operations are evaluated by linearly
     * filtering the triple set, which is space-efficient but slower for large
     * graphs. The index can be initialized explicitly to switch to eager
     * behavior; calling {@code clearIndex} reverts to filtering again.
     */
    MINIMAL
}