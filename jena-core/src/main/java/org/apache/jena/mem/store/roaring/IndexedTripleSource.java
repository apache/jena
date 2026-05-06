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
import org.apache.jena.mem.store.indexed.*;
import org.apache.jena.mem.store.indexed.TripleSet;

/**
 * Minimal view onto an indexed-by-int triple collection, used by the
 * shared iteration primitives ({@link IndexListIterator},
 * {@link IndexListSpliterator}, {@link IndexListsIterator},
 * {@link IndexListsSpliterator}) so they can resolve integer triple
 * indices back to {@link Triple} instances without coupling to a single
 * concrete type.
 * <p>
 * Implemented by {@link TripleSet} (the indexed-set's triple
 * collection). Keeping the interface
 * intentionally tiny (just {@code size()} and {@code getKeyAt(int)})
 * lets the iterators stay implementation-agnostic.
 */
public interface IndexedTripleSource {

    /**
     * @return the number of live triples (used by iterators to detect
     * concurrent modification by snapshotting at construction time).
     */
    int size();

    /**
     * @return the raw triples array
     */
    Triple[] getTriples();
}
