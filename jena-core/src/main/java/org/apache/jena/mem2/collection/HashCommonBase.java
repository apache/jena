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
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Predicate;

/**
 * Common code for hash tables and sets.
 * The hash table is a lookup table.
 * The hash codes are not stored. When the table grows, all hash codes need to be recalculated.
 *
 * @param <E> the element type
 */
public abstract class HashCommonBase<E> {
    /**
     * Jeremy suggests, from his experiments, that load factors more than
     * 0.6 leave the table too dense, and little advantage is gained below 0.4.
     * Although that was with a quadratic probe, I'm borrowing the same
     * plausible range, and use 0.5 by default.
     */
    protected static final double LOAD_FACTOR = 0.5;
    // Hash tables are 0.25 to 0.5 full so these numbers
    // are for storing about 1/3 of that number of items.
    // The larger sizes are added so that the system has "soft failure"
    // rather implying guaranteed performance.
    // https://primes.utm.edu/lists/small/millions/
    static final int[] primes = {7, 19, 37, 79, 149, 307, 617, 1237, 2477, 4957, 9923, 19_853, 39_709, 79_423, 158_849,
            317_701, 635_413, 1_270_849, 2_541_701, 5_083_423, 10_166_857, 20_333_759, 40_667_527, 81_335_047,
            162_670_111, 325_340_233, 650_680_469, 982_451_653 // 50 millionth prime - Largest at primes.utm.edu.
    };
    /**
     * The keys of whatever table it is we're implementing. Since we share code
     * for triple sets and for node->bunch maps, it has to be an Object array; we
     * take the casting hit.
     */
    protected E[] keys;
    /**
     * The threshold number of elements above which we resize the table;
     * equal to the capacity times the load factor.
     */
    protected int threshold;
    /**
     * The number of active elements in the table, maintained incrementally.
     */
    protected int size = 0;

    protected HashCommonBase(int initialCapacity) {
        keys = newKeysArray(initialCapacity);
        threshold = (int) (keys.length * LOAD_FACTOR);
    }

    protected static int nextSize(int atLeast) {
        for (int prime : primes) {
            if (prime > atLeast) return prime;
        }
        //return atLeast ;        // Input is 2*current capacity.
        // There are some very large numbers in the primes table.
        throw new JenaException("Failed to find a 'next size': atLeast = " + atLeast);
    }

    protected void clear(int initialCapacity) {
        size = 0;
        keys = newKeysArray(initialCapacity);
        threshold = (int) (keys.length * LOAD_FACTOR);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }


    /**
     * Subclasses must implement to answer a new Key[size] array.
     */
    protected abstract E[] newKeysArray(int size);

    /**
     * Answer the initial index for the object <code>key</code> in the table.
     * With luck, this will be the final position for that object. The initial index
     * will always be non-negative and less than <code>capacity</code>.
     * <p>
     * Implementation note: do <i>not</i> use <code>Math.abs</code> to turn a
     * hashcode into a positive value; there is a single specific integer on which
     * it does not work. (Hence, here, the use of bitmasks.)
     */
    protected final int initialIndexFor(int hashOfKey) {
        return (improveHashCode(hashOfKey) & 0x7fffffff) % keys.length;
    }

    /**
     * Answer the transformed hash code, intended to be an improvement
     * on the objects own hashcode. The magic number 127 is performance
     * voodoo to (try to) eliminate problems experienced by Wolfgang.
     */
    protected int improveHashCode(int hashCode) {
        return hashCode * 127;
    }

    protected abstract void grow();

    /**
     * Work out the capacity and threshold sizes for a new improved bigger
     * table (bigger by a factor of two, at present).
     */
    protected int calcGrownCapacityAndSetThreshold() {
        final var capacity = HashCommonBase.nextSize(keys.length * 2);
        threshold = (int) (capacity * LOAD_FACTOR);
        return capacity;
    }

    protected abstract void removeFrom(int here);

    /**
     * Remove the object <code>key</code> from this hash's keys if it
     * is present (if it's absent, do nothing).
     */
    public boolean tryRemove(final E key) {
        int slot = findSlot(key);
        if (slot < 0) {
            removeFrom(~slot);
            return true;
        }
        return false;
    }

    /**
     * Remove the object <code>key</code> from this hash's keys if it
     * is present (if it's absent, do nothing).
     */
    public void removeUnchecked(final E key) {
        int slot = findSlot(key);
        if (slot < 0) {
            removeFrom(~slot);
        }
    }

    /**
     * Search for the slot in which <code>key</code> is found. If it is absent,
     * return the index of the free slot in which it could be placed. If it is present,
     * return the bitwise complement of the index of the slot it appears in. Hence,
     * negative values imply present, positive absent, and there's no confusion
     * around 0.
     */
    protected int findSlot(E key) {
        int index = initialIndexFor(key.hashCode());
        while (true) {
            E current = keys[index];
            if (current == null) return index;
            if (key.equals(current)) return ~index;
            if (--index < 0) index += keys.length;
        }
    }

    public boolean containsKey(final E key) {
        return findSlot(key) < 0;
    }

    public boolean anyMatch(final Predicate<E> predicate) {
        var pos = keys.length - 1;
        while (-1 < pos) {
            if (null != keys[pos] && predicate.test(keys[pos])) {
                return true;
            }
            pos--;
        }
        return false;
    }

    public ExtendedIterator<E> keyIterator() {
        final var initialSize = size;
        final Runnable checkForConcurrentModification = () -> {
            if (size != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArrayIterator<>(keys, checkForConcurrentModification);
    }

    public Spliterator<E> keySpliterator() {
        final var initialSize = size;
        final Runnable checkForConcurrentModification = () -> {
            if (size != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArraySpliterator<>(keys, checkForConcurrentModification);
    }
}
