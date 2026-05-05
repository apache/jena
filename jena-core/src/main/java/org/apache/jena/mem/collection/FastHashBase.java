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
import java.util.function.Predicate;

/**
 * Base class for {@link FastHashSet} and {@link FastHashMap}.
 * The collection grows on demand but never shrinks. It does not guarantee any
 * iteration order (although the implementation does produce a stable order
 * for a given insertion/deletion history). It does not allow {@code null}
 * keys and is not thread-safe.
 * <h2>Internal layout</h2>
 * <ul>
 *   <li><b>positions</b>: power-of-two sized array used as the open-addressing
 *   probe table (like in {@link java.util.HashMap}). It is indexed by
 *   {@code hashCode &amp; (positions.length - 1)}. A value of {@code 0} marks
 *   an empty slot - faster to test than a {@code null} reference. Non-empty
 *   slots store the bitwise complement ({@code ~}) of the index of the entry
 *   in the {@code keys}/{@code hashCodesOrDeletedIndices} arrays, so a real
 *   stored index of {@code 0} encodes as {@code -1} and is therefore distinct
 *   from "empty".</li>
 *   <li><b>keys</b>: dense array of keys, generally filled from index 0 up to
 *   {@code keysPos}. Slots emptied by deletion become {@code null} and are
 *   reused before the array is grown. The dense layout enables fast iteration.</li>
 *   <li><b>hashCodesOrDeletedIndices</b>: parallel array to {@code keys}. For
 *   live entries it stores the cached hash code of the key. For deleted slots
 *   it stores the index of the previously deleted slot, forming a freelist
 *   whose head is {@code lastDeletedIndex} ({@code -1} if empty).</li>
 *   <li><b>keysPos</b> / <b>removedKeysCount</b>: high-water mark and freelist
 *   length, respectively; the live size is {@code keysPos - removedKeysCount}.</li>
 * </ul>
 * The {@code keys} and {@code hashCodesOrDeletedIndices} arrays grow together
 * by approximately a factor of 1.5 (similar to {@link java.util.ArrayList}).
 * <p>
 * Once a key is inserted, its index in the {@code keys} array never changes
 * until it is removed. The index can therefore be used as a stable handle for
 * O(1) random access, e.g. to coordinate parallel arrays of associated data.
 *
 * @param <K> the type of the keys
 */
public abstract class FastHashBase<K> implements JenaMapSetCommon<K> {
    /** Initial size of the {@link #positions} probe table. */
    protected static final int MINIMUM_HASHES_SIZE = 16;
    /** Initial size of the {@link #keys} / {@link #hashCodesOrDeletedIndices} arrays. */
    protected static final int MINIMUM_ELEMENTS_SIZE = 8;
    /** High-water mark in {@link #keys}; one past the largest slot ever used. */
    protected int keysPos = 0;
    /** Dense array of stored keys; {@code null} marks a freed slot. */
    protected K[] keys;
    /**
     * For live entries: cached {@link Object#hashCode()} of the corresponding key.
     * For freed slots: index of the previously freed slot (singly-linked freelist
     * whose head is {@link #lastDeletedIndex}).
     */
    protected int[] hashCodesOrDeletedIndices;
    /** Head of the freelist of removed slots, or {@code -1} if the freelist is empty. */
    protected int lastDeletedIndex = -1;
    /** Number of freelist entries (i.e. slots in {@link #keys} currently {@code null}). */
    protected int removedKeysCount = 0;

    /**
     * Probe table mapping a hash bucket to an entry index in {@link #keys}.
     * A slot's value is the bitwise complement ({@code ~}) of the entry index;
     * a value of {@code 0} marks an empty slot.
     */
    protected int[] positions;

    /**
     * Creates a base collection sized to hold at least {@code initialSize}
     * entries before growing.
     *
     * @param initialSize the initial capacity of the keys array; the probe
     *                    table is sized to the next power of two at least
     *                    twice as large
     */
    protected FastHashBase(final int initialSize) {
        var positionsSize = Integer.highestOneBit(initialSize << 1);
        if (positionsSize < initialSize << 1) {
            positionsSize <<= 1;
        }
        this.positions = new int[positionsSize];
        this.keys = newKeysArray(initialSize);
        this.hashCodesOrDeletedIndices = new int[initialSize];
    }

    /**
     * Creates a base collection with the default minimum capacities
     * ({@link #MINIMUM_HASHES_SIZE} for the probe table and
     * {@link #MINIMUM_ELEMENTS_SIZE} for the keys array).
     */
    protected FastHashBase() {
        this.positions = new int[MINIMUM_HASHES_SIZE];
        this.keys = newKeysArray(MINIMUM_ELEMENTS_SIZE);
        this.hashCodesOrDeletedIndices = new int[MINIMUM_ELEMENTS_SIZE];
    }

    /**
     * Copy constructor.
     * The new map will contain all the same keys of the map to copy.
     *
     * @param baseToCopy instance to copy
     */
    protected <T extends FastHashBase<K>> FastHashBase(final T baseToCopy)  {
        this.positions = new int[baseToCopy.positions.length];
        System.arraycopy(baseToCopy.positions, 0, this.positions, 0, baseToCopy.positions.length);

        this.hashCodesOrDeletedIndices = new int[baseToCopy.hashCodesOrDeletedIndices.length];
        System.arraycopy(baseToCopy.hashCodesOrDeletedIndices, 0, this.hashCodesOrDeletedIndices, 0, baseToCopy.keysPos);

        this.keys = newKeysArray(baseToCopy.keys.length);
        System.arraycopy(baseToCopy.keys, 0, this.keys, 0, baseToCopy.keysPos);

        this.keysPos = baseToCopy.keysPos;
        this.lastDeletedIndex = baseToCopy.lastDeletedIndex;
        this.removedKeysCount = baseToCopy.removedKeysCount;
    }

    /**
     * Gets a new array of keys with the given size.
     *
     * @param size the size of the array
     * @return the new array
     */
    protected abstract K[] newKeysArray(int size);

    /**
     * Calculates a position in the positions array by the hashCode.
     *
     * @param hashCode the hashCode
     * @return the start index in the positions array to search for the key
     */
    protected final int calcStartIndexByHashCode(final int hashCode) {
        return hashCode & (positions.length - 1);
    }

    /**
     * Calculates the new size of the positions array, if it needs to be grown.
     *
     * @return the new size or -1 if it does not need to be grown
     */
    private int calcNewPositionsSize() {
        if (keysPos << 1 > positions.length) { /*grow*/
            final var newLength = positions.length << 1;
            return newLength < 0 ? Integer.MAX_VALUE : newLength;
        }
        return -1;
    }

    private void fillPositionsArray(int newSize) {
        this.positions = new int[newSize];
        var pos = keysPos - 1;
        while (-1 < pos) {
            if (null != keys[pos]) {
                this.positions[findEmptySlotWithoutEqualityCheck(hashCodesOrDeletedIndices[pos])] = ~pos;
            }
            pos--;
        }
    }

    /**
     * Grows the positions array if needed.
     */
    protected final void growPositionsArrayIfNeeded() {
        final var newSize = calcNewPositionsSize();
        if (newSize < 0) {
            return;
        }
        fillPositionsArray(newSize);
    }

    /**
     * Grow the positions array if needed.
     *
     * @return true if the positions array was grown
     */
    protected final boolean tryGrowPositionsArrayIfNeeded() {
        final var newSize = calcNewPositionsSize();
        if (newSize < 0) {
            return false;
        }
        fillPositionsArray(newSize);
        return true;
    }

    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this collection
     */
    @Override
    public int size() {
        return keysPos - removedKeysCount;
    }

    /**
     * Finds the next free slot in the keys array.
     * If the keys array needs to be grown, it is grown.
     * If there are deleted keys, the index of the last deleted key is returned.
     *
     * @return the index of the next free slot
     */
    protected final int getFreeKeyIndex() {
        final int index;
        if (lastDeletedIndex == -1) {
            index = keysPos++;
            if (index == keys.length) {
                growKeysAndHashCodeArrays();
            }
        } else {
            index = lastDeletedIndex;
            lastDeletedIndex = hashCodesOrDeletedIndices[lastDeletedIndex];
            removedKeysCount--;
        }
        return index;
    }

    /**
     * Grow the keys and hashCodes arrays.
     */
    protected void growKeysAndHashCodeArrays() {
        var newSize = (keys.length >> 1) + keys.length;
        if (newSize < 0) {
            newSize = Integer.MAX_VALUE;
        }
        final var oldKeys = this.keys;
        this.keys = newKeysArray(newSize);
        System.arraycopy(oldKeys, 0, keys, 0, oldKeys.length);
        final var oldHashCodes = this.hashCodesOrDeletedIndices;
        this.hashCodesOrDeletedIndices = new int[newSize];
        System.arraycopy(oldHashCodes, 0, hashCodesOrDeletedIndices, 0, oldHashCodes.length);
    }

    @Override
    public final boolean tryRemove(K o) {
        return tryRemove(o, o.hashCode());
    }

    public final boolean tryRemove(K e, int hashCode) {
        final var index = findPosition(e, hashCode);
        if (index < 0) {
            return false;
        }
        removeFrom(index);
        return true;
    }

    /**
     * Remove the given element and return the index it occupied before removal.
     *
     * @param e the element to remove
     * @return the former index of the element, or {@code -1} if it was not present
     */
    public final int removeAndGetIndex(final K e) {
        return removeAndGetIndex(e, e.hashCode());
    }

    /**
     * Remove the given element and return the index it occupied before removal.
     * Lets the caller supply the precomputed hash code to avoid an extra
     * {@code hashCode()} call.
     *
     * @param e        the element to remove
     * @param hashCode {@code e.hashCode()}
     * @return the former index of the element, or {@code -1} if it was not present
     */
    public final int removeAndGetIndex(final K e, final int hashCode) {
        final var pIndex = findPosition(e, hashCode);
        if (pIndex < 0) {
            return -1;
        }
        final var eIndex = ~positions[pIndex];
        removeFrom(pIndex);
        return eIndex;
    }

    @Override
    public final void removeUnchecked(K e) {
        removeUnchecked(e, e.hashCode());
    }

    public final void removeUnchecked(K e, int hashCode) {
        removeFrom(findPosition(e, hashCode));
    }

    /**
     * Removes the entry referenced by the {@code positions} slot at index
     * {@code here} and rehashes the affected probe chain.
     * <p>
     * This is an implementation of Knuth's Algorithm R from <em>The Art of
     * Computer Programming</em>, vol. 3, p. 527, with the roles of {@code i}
     * and {@code j} swapped so they can be usefully renamed to <i>here</i>
     * and <i>scan</i>.
     * <p>
     * It relies on linear probing but doesn't require a distinguished
     * {@code REMOVED} sentinel. Since the table is resized once it gets
     * fullish, the overhead of linear probing is not a concern.
     *
     * @param here the index in the {@link #positions} array of the slot to clear
     */
    protected void removeFrom(int here) {
        final var pIndex = ~positions[here];
        hashCodesOrDeletedIndices[pIndex] = lastDeletedIndex;
        lastDeletedIndex = pIndex;
        removedKeysCount++;
        keys[pIndex] = null;
        while (true) {
            positions[here] = 0;
            int scan = here;
            while (true) {
                if (--scan < 0) scan += positions.length;
                if (positions[scan] == 0) return;
                int r = calcStartIndexByHashCode(hashCodesOrDeletedIndices[~positions[scan]]);
                if ((scan > r || r >= here) && (r >= here || here >= scan) && (here >= scan || scan > r)) {
                    positions[here] = positions[scan];
                    here = scan;
                    break;
                }
            }
        }
    }

    /**
     * Returns {@code true} if this collection contains no elements.
     *
     * @return {@code true} if this collection contains no elements
     */
    @Override
    public final boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public final boolean containsKey(K o) {
        final int hashCode = o.hashCode();
        var pIndex = calcStartIndexByHashCode(hashCode);
        while (true) {
            if (0 == positions[pIndex]) {
                return false;
            } else {
                final var eIndex = ~positions[pIndex];
                if (hashCode == hashCodesOrDeletedIndices[eIndex] && o.equals(keys[eIndex])) {
                    return true;
                } else if (--pIndex < 0) {
                    pIndex += positions.length;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Iterates the keys in dense (insertion-order-ish) order. This is fast when
     * matches are rare or expected near the end of the array, but can be slow
     * when matches are clustered at the start of the array. For workloads
     * where many matches are expected, prefer {@link #anyMatchRandomOrder(Predicate)},
     * which scans in probe-table order and tends to find matches sooner when
     * they are abundant.
     */
    @Override
    public final boolean anyMatch(Predicate<K> predicate) {
        var pos = keysPos - 1;
        while (-1 < pos) {
            if (null != keys[pos] && predicate.test(keys[pos])) {
                return true;
            }
            pos--;
        }
        return false;
    }

    /**
     * Like {@link #anyMatch(Predicate)} but scans the probe table rather than
     * the dense {@code keys} array, yielding a roughly hash-based order.
     * <p>
     * This is faster than {@link #anyMatch(Predicate)} when many matches are
     * expected (the predicate is more likely to short-circuit early), but
     * slower when no or only a single match exists (each iteration must
     * test against an empty slot first).
     *
     * @param predicate the predicate to apply
     * @return {@code true} if any element matches the predicate
     */
    public final boolean anyMatchRandomOrder(Predicate<K> predicate) {
        var pIndex = positions.length - 1;
        while (-1 < pIndex) {
            if (0 != positions[pIndex] && predicate.test(keys[~positions[pIndex]])) {
                return true;
            }
            pIndex--;
        }
        return false;
    }

    @Override
    public final ExtendedIterator<K> keyIterator() {
        return new SparseArrayIterator<>(keys, keysPos, this);
    }

    /**
     * Locates the slot in {@link #positions} that holds {@code e} (with the
     * given precomputed hash code).
     * <p>
     * If the key is present, returns the (non-negative) probe-table slot
     * index. If the key is absent, returns the bitwise complement of the
     * empty probe-table slot at which the key would be inserted, allowing
     * insertion to proceed without a second probe walk.
     *
     * @param e        the key to locate
     * @param hashCode {@code e.hashCode()}
     * @return the position index if found, or {@code ~insertionPosition} if not
     */
    protected final int findPosition(final K e, final int hashCode) {
        var pIndex = calcStartIndexByHashCode(hashCode);
        while (true) {
            if (0 == positions[pIndex]) {
                return ~pIndex;
            } else {
                final var pos = ~positions[pIndex];
                if (hashCode == hashCodesOrDeletedIndices[pos] && e.equals(keys[pos])) {
                    return pIndex;
                } else if (--pIndex < 0) {
                    pIndex += positions.length;
                }
            }
        }
    }

    /**
     * Locates the next empty slot in {@link #positions} along the probe chain
     * for the given hash code, without checking any existing entries for
     * equality. Used after a positions-array resize, when no duplicates can
     * exist in the rebuilt table.
     *
     * @param hashCode the hash code being placed
     * @return the index of an empty slot in the probe table
     */
    protected final int findEmptySlotWithoutEqualityCheck(final int hashCode) {
        var pIndex = calcStartIndexByHashCode(hashCode);
        while (true) {
            if (0 == positions[pIndex]) {
                return pIndex;
            } else if (--pIndex < 0) {
                pIndex += positions.length;
            }
        }
    }

    /**
     * Removes all the elements from this collection (optional operation).
     * The collection will be empty after this method returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *                                       is not supported by this collection
     */
    @Override
    public void clear() {
        positions = new int[MINIMUM_HASHES_SIZE];
        keys = newKeysArray(MINIMUM_ELEMENTS_SIZE);
        hashCodesOrDeletedIndices = new int[MINIMUM_ELEMENTS_SIZE];
        keysPos = 0;
        lastDeletedIndex = -1;
        removedKeysCount = 0;
    }

    @Override
    public final Spliterator<K> keySpliterator() {
        return new SparseArraySpliterator<>(keys, keysPos, this);
    }

    /**
     * Gets the key at the given index.
     * Array bounds are not checked. The caller must ensure the index is valid and corresponds to a non-null key.
     *
     * @param i the index
     * @return the key at the given index
     */
    public K getKeyAt(int i) {
        return keys[i];
    }

    /**
     * Returns the index of the entry holding {@code key}, or {@code -1} if not present.
     *
     * @param key the key to look up
     * @return the entry index, or {@code -1} if the key is absent
     */
    public int indexOf(K key) {
        final var pIndex = findPosition(key, key.hashCode());
        if (pIndex < 0) {
            return -1;
        } else {
            return ~positions[pIndex];
        }
    }

    /**
     * Functional interface used by {@link #forEachKey} to receive each live
     * key along with the stable index it occupies.
     *
     * @param <K> the key type
     */
    @FunctionalInterface
    public interface KeyAndIndexConsumer<K> {
        /**
         * Receive a single key and its index.
         *
         * @param key   the key
         * @param index the stable index of the key in the underlying array
         */
        void accept(K key, int index);
    }

    /**
     * Sequentially invokes {@code consumer} for every live key with its index.
     * Skips freed slots.
     *
     * @param consumer receives each key/index pair
     */
    public void forEachKey(KeyAndIndexConsumer<K> consumer) {
        for (int i = 0; i < keysPos; i++) {
            if(keys[i] != null) {
                consumer.accept(keys[i], i);
            }
        }
    }
}
