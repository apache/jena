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

package org.apache.jena.mem.store.indexed;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.collection.FastHashSet;

import java.util.function.IntConsumer;

/**
 * {@link FastHashSet} of {@link Triple}s used as the canonical triple
 * collection inside {@link IndexedSetTripleStore}. Adds a hook that fires
 * whenever the underlying keys array grows, so that indexes built on top of
 * this set (such as the reverse-index arrays in
 * {@link org.apache.jena.mem.store.indexed.EagerStoreStrategy})
 * can resize their parallel data structures in lock-step.
 */
public class TripleSet
        extends FastHashSet<Triple>
        implements Copyable<TripleSet>, IndexedTripleSource {

    private IntConsumer onKeysGrowHook = null;

    /**
     * Register a callback that is invoked after the keys array grows; the
     * callback receives the new array length. Setting this to {@code null}
     * disables notifications.
     *
     * @param onKeysGrowHook callback receiving the new {@code keys.length}
     */
    public void setOnKeysGrowHook(IntConsumer onKeysGrowHook) {
        this.onKeysGrowHook = onKeysGrowHook;
    }

    /**
     * Creates an empty triple set.
     */
    public TripleSet() {
        super();
    }

    /**
     * Copy constructor.
     * The {@code onKeysGrowHook} is not copied. Any grow-hook registered on the source set will not be registered on
     * the copy. {@code setOnKeysGrowHook} must be called separately on the copy if a grow-hook is desired.
     *
     * @param setToCopy the source set
     */
    @SuppressWarnings("CopyConstructorMissesField")
    private TripleSet(final TripleSet setToCopy) {
        super(setToCopy);
    }

    @Override
    protected Triple[] newKeysArray(int size) {
        return new Triple[size];
    }

    @Override
    protected void growKeysAndHashCodeArrays() {
        super.growKeysAndHashCodeArrays();
        if(onKeysGrowHook != null) {
            onKeysGrowHook.accept(keys.length);
        }
    }

    /**
     * Returns an independent copy of this set. The grow-hook from the source
     * is <em>not</em> propagated to the copy.
     *
     * @return a new {@link TripleSet} with the same triples
     */
    @Override
    public TripleSet copy() {
        return new TripleSet(this);
    }

    /**
     * Returns the current length of the underlying {@code keys} array.
     * This is the upper bound on the indices that may currently be valid;
     * useful for callers that maintain parallel arrays keyed by entry index.
     *
     * @return the current capacity of the {@code keys} array
     */
    public int getInternalKeysLength() {
        return keys.length;
    }

    @Override
    public Triple[] getTriples() {
        return keys;
    }
}