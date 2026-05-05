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

import org.apache.jena.mem.iterator.SparseArrayIterator;
import org.apache.jena.mem.spliterator.SparseArraySpliterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Hash map specialization built on top of {@link FastHashBase}.
 * Grows on demand but never shrinks, does not guarantee iteration order,
 * does not allow {@code null} keys, and is not thread-safe.
 * <p>
 * Optimized for fast {@code add} / {@code containsKey} / {@code stream} /
 * iterate operations. Removal is somewhat slower than in
 * {@link java.util.HashMap} because of the back-shifting performed on the
 * probe table. Iteration speed does not recover after many removals because
 * the dense {@code keys} array is not compacted.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class FastHashMap<K, V> extends FastHashBase<K> implements JenaMapIndexed<K, V> {

    /**
     * Parallel array to {@code keys} holding the value associated with each
     * stored key. {@code values[i]} is the value for {@code keys[i]} when
     * {@code keys[i]} is non-null.
     */
    protected V[] values;

    /**
     * Creates a map with the given initial key-array capacity.
     *
     * @param initialSize the initial capacity of the keys/values arrays
     */
    protected FastHashMap(int initialSize) {
        super(initialSize);
        this.values = newValuesArray(keys.length);
    }

    /**
     * Creates a map with the default initial capacity.
     */
    protected FastHashMap() {
        super();
        this.values = newValuesArray(keys.length);
    }

    /**
     * Copy constructor. The new map contains the same keys and the same
     * value references as {@code mapToCopy}.
     *
     * @param mapToCopy the source map
     */
    protected FastHashMap(final FastHashMap<K, V> mapToCopy) {
        super(mapToCopy);
        this.values = newValuesArray(keys.length);
        System.arraycopy(mapToCopy.values, 0, this.values, 0, mapToCopy.values.length);
    }

    /**
     * Copy constructor that transforms each value via {@code valueProcessor}.
     * Useful when the values are mutable and need to be deep-copied to keep
     * the new map independent from the source.
     *
     * @param mapToCopy      the source map
     * @param valueProcessor function applied to every non-null value to obtain
     *                       the value to put in the new map
     */
    protected FastHashMap(final FastHashMap<K, V> mapToCopy, final UnaryOperator<V> valueProcessor) {
        super(mapToCopy);
        this.values = newValuesArray(keys.length);
        for (int i = 0; i < mapToCopy.values.length; i++) {
            final var value = mapToCopy.values[i];
            if (value != null) {
                this.values[i] = valueProcessor.apply(value);
            }
        }
    }

    /**
     * Gets a new array of values with the given size.
     *
     * @param size the size of the array
     * @return the new array
     */
    protected abstract V[] newValuesArray(int size);

    @Override
    protected void growKeysAndHashCodeArrays() {
        super.growKeysAndHashCodeArrays();
        final var oldValues = values;
        values = newValuesArray(keys.length);
        System.arraycopy(oldValues, 0, values, 0, oldValues.length);
    }

    @Override
    protected void removeFrom(int here) {
        values[~positions[here]] = null;
        super.removeFrom(here);
    }

    @Override
    public void clear() {
        super.clear();
        values = newValuesArray(keys.length);
    }

    @Override
    public boolean tryPut(K key, V value) {
        growPositionsArrayIfNeeded();
        final var hashCode = key.hashCode();
        final var pIndex = findPosition(key, hashCode);
        if (pIndex < 0) {
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            values[eIndex] = value;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            positions[~pIndex] = ~eIndex;
            return true;
        } else {
            values[~positions[pIndex]] = value;
            return false;
        }
    }

    @Override
    public void put(K key, V value) {
        growPositionsArrayIfNeeded();
        final var hashCode = key.hashCode();
        final var pIndex = findPosition(key, hashCode);
        if (pIndex < 0) {
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            values[eIndex] = value;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            positions[~pIndex] = ~eIndex;
        } else {
            values[~positions[pIndex]] = value;
        }
    }

    @Override
    public int putAndGetIndex(K key, V value) {
        growPositionsArrayIfNeeded();
        final int hashCode = key.hashCode();
        final var pIndex = findPosition(key, hashCode);
        final int eIndex;
        if (pIndex < 0) {
            eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            positions[~pIndex] = ~eIndex;
        } else {
            eIndex = ~positions[pIndex];
        }
        values[eIndex] = value;
        return eIndex;
    }

    /**
     * Returns the value at the given index.
     * Array bounds are not checked. The caller must ensure the index is valid and corresponds to a non-null key.
     *
     * @param i index
     * @return value
     */
    public V getValueAt(int i) {
        return values[i];
    }

    @Override
    public V get(K key) {
        var pIndex = findPosition(key, key.hashCode());
        if (pIndex < 0) {
            return null;
        } else {
            return values[~positions[pIndex]];
        }
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        var pIndex = findPosition(key, key.hashCode());
        if (pIndex < 0) {
            return defaultValue;
        } else {
            return values[~positions[pIndex]];
        }
    }

    @Override
    public V computeIfAbsent(K key, Supplier<V> absentValueSupplier) {
        final var hashCode = key.hashCode();
        var pIndex = findPosition(key, hashCode);
        if (pIndex < 0) {
            if (tryGrowPositionsArrayIfNeeded()) {
                pIndex = ~findEmptySlotWithoutEqualityCheck(hashCode);
            }
            final var value = absentValueSupplier.get();
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            values[eIndex] = value;
            positions[~pIndex] = ~eIndex;
            return value;
        } else {
            return values[~positions[pIndex]];
        }
    }

    @Override
    public void compute(K key, UnaryOperator<V> valueProcessor) {
        final var hashCode = key.hashCode();
        var pIndex = findPosition(key, hashCode);
        if (pIndex < 0) {
            final var value = valueProcessor.apply(null);
            if (value == null)
                return;
            if(tryGrowPositionsArrayIfNeeded()) {
                pIndex = ~findEmptySlotWithoutEqualityCheck(hashCode);
            }
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            values[eIndex] = value;
            positions[~pIndex] = ~eIndex;
        } else {
            var eIndex = ~positions[pIndex];
            final var value = valueProcessor.apply(values[eIndex]);
            if (value == null) {
                removeFrom(pIndex);
            } else {
                values[eIndex] = value;
            }
        }
    }

    @Override
    public ExtendedIterator<V> valueIterator() {
        return new SparseArrayIterator<>(values, keysPos, this);
    }

    @Override
    public Spliterator<V> valueSpliterator() {
        return new SparseArraySpliterator<>(values, keysPos, this);
    }
}
