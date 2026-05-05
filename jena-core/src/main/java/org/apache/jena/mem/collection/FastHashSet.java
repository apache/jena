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

package org.apache.jena.mem.collection;

/**
 * Hash set specialization built on top of {@link FastHashBase}.
 * Grows on demand but never shrinks, does not guarantee iteration order,
 * does not allow {@code null} elements, and is not thread-safe.
 * <p>
 * Optimized for fast {@code add} / {@code containsKey} / {@code stream} /
 * iterate operations. Removal is somewhat slower than in
 * {@link java.util.HashSet} because of the back-shifting performed on the
 * probe table. Iteration speed does not recover after many removals because
 * the dense {@code keys} array is not compacted.
 *
 * @param <K> the element type
 */
public abstract class FastHashSet<K> extends FastHashBase<K> implements JenaSetIndexed<K> {

    /**
     * Creates a set with the given initial key-array capacity.
     *
     * @param initialSize the initial capacity of the keys array
     */
    public FastHashSet(final int initialSize) {
        super(initialSize);
    }

    /**
     * Creates a set with the default initial capacity.
     */
    public FastHashSet() {
        super();
    }

    /**
     * Copy constructor. The new set contains the same elements as
     * {@code setToCopy}.
     *
     * @param setToCopy the source set
     */
    protected FastHashSet(final FastHashSet<K> setToCopy) {
        super(setToCopy);
    }

    @Override
    public boolean tryAdd(K key) {
        return tryAdd(key, key.hashCode());
    }

    @Override
    public boolean tryAdd(K key, int hashCode) {
        growPositionsArrayIfNeeded();
        final var pIndex = findPosition(key, hashCode);
        if (pIndex < 0) {
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            positions[~pIndex] = ~eIndex;
            return true;
        }
        return false;
    }

    /**
     * Add an element and return the index it was stored at.
     * If the element is already present, returns the bitwise complement
     * ({@code ~existingIndex}) of the existing index, so callers can
     * distinguish "newly inserted" from "already present" while still
     * recovering the index in both cases.
     *
     * @param key the element to add
     * @return the new index, or {@code ~existingIndex} if already present
     */
    @Override
    public int addAndGetIndex(K key) {
        growPositionsArrayIfNeeded();
        final var hashCode = key.hashCode();
        final var pIndex = findPosition(key, hashCode);
        if (pIndex < 0) {
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            positions[~pIndex] = ~eIndex;
            return eIndex;
        } else {
            return positions[pIndex];
        }
    }

    @Override
    public void addUnchecked(K key) {
        addUnchecked(key, key.hashCode());
    }

    @Override
    public void addUnchecked(K value, int hashCode) {
        growPositionsArrayIfNeeded();
        final var eIndex = getFreeKeyIndex();
        keys[eIndex] = value;
        hashCodesOrDeletedIndices[eIndex] = hashCode;
        positions[findEmptySlotWithoutEqualityCheck(hashCode)] = ~eIndex;
    }

    /**
     * Gets the key at the given index.
     *
     * @param i the index
     * @return the key at the given index
     */
    public K getKeyAt(int i) {
        return keys[i];
    }
}
