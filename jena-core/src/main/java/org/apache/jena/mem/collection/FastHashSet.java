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

package org.apache.jena.mem.collection;

import org.apache.jena.mem.iterator.SparseArrayIndexedIterator;
import org.apache.jena.mem.spliterator.SparseArrayIndexedSpliterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Set which grows, if needed but never shrinks.
 * This set does not guarantee any order. Although due to the way it is implemented the elements have a certain order.
 * This set does not allow null values.
 * This set is not thread safe.
 * It´s purpose is to support fast add, remove, contains and stream / iterate operations.
 * Only remove operations are not as fast as in {@link java.util.HashSet}
 * Iterating over this set not get much faster again after removing elements because the set is not compacted.
 */
public abstract class FastHashSet<K> extends FastHashBase<K> implements JenaSetHashOptimized<K> {

    protected FastHashSet(int initialSize) {
        super(initialSize);
    }

    protected FastHashSet() {
        super();
    }

    /**
     * Copy constructor.
     * The new set will contain all the same keys of the set to copy.
     *
     * @param setToCopy
     */
    protected FastHashSet(final FastHashSet<K> setToCopy) {
        super(setToCopy);
    }

    @Override
    public boolean tryAdd(K key) {
        return tryAdd(key, key.hashCode());
    }

    @Override
    public boolean tryAdd(K value, int hashCode) {
        growPositionsArrayIfNeeded();
        var pIndex = findPosition(value, hashCode);
        if (pIndex < 0) {
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = value;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            positions[~pIndex] = ~eIndex;
            return true;
        }
        return false;
    }

    /**
     * Add and get the index of the added element.
     *
     * @param value the value to add
     * @return the index of the added element or the inverse (~) index of the existing element
     */
    public int addAndGetIndex(K value) {
        return addAndGetIndex(value, value.hashCode());
    }

    /**
     * Add and get the index of the added element.
     *
     * @param value    the value to add
     * @param hashCode the hash code of the value. This is a performance optimization.
     * @return the index of the added element or the inverse (~) index of the existing element
     */
    public int addAndGetIndex(final K value, final int hashCode) {
        growPositionsArrayIfNeeded();
        final var pIndex = findPosition(value, hashCode);
        if (pIndex < 0) {
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = value;
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

    /**
     * Entry pairing a key with its index in the set.
     * @param index index of the key in the set
     * @param key the key
     * @param <K> the type of the key
     */
    public record IndexedKey<K>(int index, K key) {}

    /**
     * Get an iterator over pairs of keys and their indices in the set.
     * The iterator is not thread safe.
     *
     * @return an iterator over pairs of keys and their indices in the set
     */
    public final ExtendedIterator<IndexedKey<K>> indexedKeyIterator() {
        final var initialSize = size();
        final Runnable checkForConcurrentModification = () ->
        {
            if (size() != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArrayIndexedIterator<>(keys, keysPos, checkForConcurrentModification);
    }

    /**
     * Get a spliterator over pairs of keys and their indices in the set.
     * The spliterator is not thread safe.
     *
     * @return a spliterator over pairs of keys and their indices in the set
     */
    public final Spliterator<IndexedKey<K>> indexedKeySpliterator() {
        final var initialSize = this.size();
        final Runnable checkForConcurrentModification = () ->
        {
            if (this.size() != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArrayIndexedSpliterator<>(keys, keysPos, checkForConcurrentModification);
    }

    /**
     * Get a stream over pairs of keys and their indices in the set.
     * The stream is not thread safe.
     *
     * @return a stream over pairs of keys and their indices in the set
     */
    public final Stream<IndexedKey<K>> indexedKeyStream() {
        return StreamSupport.stream(indexedKeySpliterator(), false);
    }

    /**
     * Get a parallel stream over pairs of keys and their indices in the set.
     * The stream is not thread safe.
     *
     * @return a parallel stream over pairs of keys and their indices in the set
     */
    public final Stream<IndexedKey<K>> indexedKeyStreamParallel() {
        return StreamSupport.stream(indexedKeySpliterator(), true);
    }
}
