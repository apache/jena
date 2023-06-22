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
package org.apache.jena.mem2.collection;

import org.apache.jena.mem2.iterator.SparseArrayIterator;
import org.apache.jena.mem2.spliterator.SparseArraySpliterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Predicate;

/**
 * This is the base class for {@link FastHashSet} and {@link FastHashSet}.
 * It only grows but never shrinks.
 * This map does not guarantee any order. Although due to the way it is implemented the elements have a certain order.
 * This map does not allow null keys.
 * This map is not thread safe.
 * <p>
 * The positions array stores negative indices to the entries and hashCode arrays.
 * The positions array is implemented as a power of two sized array. (like in {@link java.util.HashMap}) This allows
 * to use a fast modulo operation to calculate the index. The indices of the positions array are derived from the
 * hashCodes.
 * Any position 0 indicates an empty element. The comparison with 0 is faster than comparing elements with null.
 * <p>
 * The keys are stored in a keys array and the hashCodesOrDeletedIndices array
 * stores the hashCodes of the keys.
 * hashCodesOrDeletedIndices is also used to store the indices of the deleted keys to save memory. It works like a
 * linked list of deleted keys. The index of the previously deleted key is stored in the hashCodesOrDeletedIndices
 * array. lastDeletedIndex is the index of the last deleted key in the hashCodesOrDeletedIndices array and serves as
 * the head of the linked list of deleted keys.
 * These two arrays grow together. They grow like {@link java.util.ArrayList} with a factor of 1.5.
 * <p>
 * keysPos is the index of the next free position in the keys array.
 * The keys array is usually completely filled from index 0 to keysPos. Exceptions are the deleted keys.
 * Indices that have been deleted are reused for new keys before the keys array is extended.
 * The dense nature of the keys array enables fast iteration.
 * <p>
 * The index of a key in the keys array never changes. So the index of a key can be used as a handle to the key and
 * for random access.
 *
 * @param <K> the type of the keys
 */
public abstract class FastHashBase<K> implements JenaMapSetCommon<K> {
    protected static final int MINIMUM_HASHES_SIZE = 16;
    protected static final int MINIMUM_ELEMENTS_SIZE = 8;
    protected int keysPos = 0;
    protected K[] keys;
    protected int[] hashCodesOrDeletedIndices;
    protected int lastDeletedIndex = -1;
    protected int removedKeysCount = 0;

    /**
     * The negative indices to the entries and hashCode arrays.
     * The indices of the positions array are derived from the hashCodes.
     * Any position 0 indicates an empty element.
     */
    protected int[] positions;

    protected FastHashBase(int initialSize) {
        var positionsSize = Integer.highestOneBit(initialSize << 1);
        if (positionsSize < initialSize << 1) {
            positionsSize <<= 1;
        }
        this.positions = new int[positionsSize];
        this.keys = newKeysArray(initialSize);
        this.hashCodesOrDeletedIndices = new int[initialSize];
    }

    protected FastHashBase() {
        this.positions = new int[MINIMUM_HASHES_SIZE];
        this.keys = newKeysArray(MINIMUM_ELEMENTS_SIZE);
        this.hashCodesOrDeletedIndices = new int[MINIMUM_ELEMENTS_SIZE];

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

    /**
     * Grows the positions array if needed.
     */
    protected final void growPositionsArrayIfNeeded() {
        final var newSize = calcNewPositionsSize();
        if (newSize < 0) {
            return;
        }
        final var oldPositions = this.positions;
        this.positions = new int[newSize];
        for (int oldPosition : oldPositions) {
            if (0 != oldPosition) {
                this.positions[findEmptySlotWithoutEqualityCheck(hashCodesOrDeletedIndices[~oldPosition])] = oldPosition;
            }
        }
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
        final var oldPositions = this.positions;
        this.positions = new int[newSize];
        for (int oldPosition : oldPositions) {
            if (0 != oldPosition) {
                this.positions[findEmptySlotWithoutEqualityCheck(hashCodesOrDeletedIndices[~oldPosition])] = oldPosition;
            }
        }
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
     * Removes the element at the given position.
     *
     * @param e the element
     * @return the index of the removed element or -1 if the element was not found
     */
    public final int removeAndGetIndex(final K e) {
        return removeAndGetIndex(e, e.hashCode());
    }

    /**
     * Removes the element at the given position.
     *
     * @param e        the element
     * @param hashCode the hash code of the element. This is a performance optimization.
     * @return the index of the removed element or -1 if the element was not found
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
     * Removes the element at the given position.
     * <p>
     * This is an implementation of Knuth's Algorithm R from tAoCP vol3, p 527,
     * with exchanging of the roles of i and j so that they can be usefully renamed
     * to <i>here</i> and <i>scan</i>.
     * <p>
     * It relies on linear probing but doesn't require a distinguished REMOVED
     * value. Since we resize the table when it gets fullish, we don't worry [much]
     * about the overhead of the linear probing.
     * <p>
     *
     * @param here the index in the positions array
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
     * Attentions: Due to the ordering of the keys, this method may be slow
     * if matching elements are at the start of the list.
     * Try to use {@link #anyMatchRandomOrder(Predicate)} instead.
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
     * This method can be faster than {@link #anyMatch(Predicate)} if one expects
     * to find many matches. But it is slower if one expects to find no matches or just a single one.
     *
     * @param predicate the predicate to apply to elements of this collection
     * @return {@code true} if any element of the collection matches the predicate
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
        final var initialSize = size();
        final Runnable checkForConcurrentModification = () ->
        {
            if (size() != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArrayIterator<>(keys, keysPos, checkForConcurrentModification);
    }

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
        final var initialSize = this.size();
        final Runnable checkForConcurrentModification = () ->
        {
            if (this.size() != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArraySpliterator<>(keys, keysPos, checkForConcurrentModification);
    }
}
