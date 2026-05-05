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

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.collection.FastHashSet;
import org.apache.jena.mem.collection.JenaSet;

/**
 * Hashed implementation of {@link FastTripleBunch} built on top of
 * {@link FastHashSet}. Used by {@link FastTripleStore} once a bunch grows
 * past the size threshold at which a linear-scan {@link FastArrayBunch}
 * stops being faster.
 */
public class FastHashedTripleBunch extends FastHashSet<Triple> implements FastTripleBunch {

    /**
     * Create a new hashed bunch pre-populated from the given set of triples.
     * The initial capacity is chosen at 1.5x the source size, so the new bunch
     * fits the existing triples and has some headroom for growth before it
     * needs to rehash.
     *
     * @param set the source set of triples (typically the array bunch being
     *            promoted)
     */
    public FastHashedTripleBunch(final JenaSet<Triple> set) {
        super((set.size() >> 1) + set.size()); //it should not only fit but also have some space for growth
        set.keyIterator().forEachRemaining(this::addUnchecked);
    }

    /**
     * Copy constructor. The new bunch contains the same triples as
     * {@code bunchToCopy}.
     *
     * @param bunchToCopy the source bunch
     */
    private FastHashedTripleBunch(final FastHashedTripleBunch bunchToCopy) {
        super(bunchToCopy);
    }

    /**
     * Creates an empty hashed bunch with the default initial capacity.
     */
    public FastHashedTripleBunch() {
        super();
    }

    @Override
    protected Triple[] newKeysArray(int size) {
        return new Triple[size];
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public FastHashedTripleBunch copy() {
        return new FastHashedTripleBunch(this);
    }
}
