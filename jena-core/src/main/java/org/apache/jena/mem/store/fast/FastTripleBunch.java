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

package org.apache.jena.mem.store.fast;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.collection.JenaMapSetCommon;
import org.apache.jena.mem.collection.JenaSetHashOptimized;

import java.util.function.Predicate;

/**
 * Set-like container for a "bunch" of triples that share some useful
 * property - typically they all have the same subject, predicate or object,
 * because the bunch is the value of a node-keyed map in a
 * {@link FastTripleStore}.
 * <p>
 * The interface is a stripped-down set with a few extras tuned for the
 * triple-store hot path; concrete implementations are
 * {@link FastArrayBunch} (linear scan, used while the bunch is small) and
 * {@link FastHashedTripleBunch} (hashed, used once the bunch grows past a
 * threshold).
 */
public interface FastTripleBunch extends JenaSetHashOptimized<Triple>, Copyable<FastTripleBunch> {
    /**
     * Answer {@code true} iff this bunch is backed by a flat array (i.e. is
     * a {@link FastArrayBunch}). Exposed as an explicit method so callers can
     * avoid {@code instanceof} checks on this hot path.
     *
     * @return {@code true} if this bunch is array-backed
     */
    boolean isArray();

    /**
     * Predicate test that scans elements in hash-table order rather than
     * dense insertion order. Tuned for {@code _PO} (any-predicate-object)
     * matches.
     * <p>
     * {@link JenaMapSetCommon#anyMatch(Predicate)} is faster when matches
     * are rare or absent; this method is faster when many matches exist and
     * the dense ordering would force scanning past clustered non-matches
     * before finding a hit. Both variants short-circuit on the first match.
     *
     * @param predicate the predicate to test against each triple
     * @return {@code true} if any triple in the bunch satisfies the predicate
     */
    boolean anyMatchRandomOrder(Predicate<Triple> predicate);
}
