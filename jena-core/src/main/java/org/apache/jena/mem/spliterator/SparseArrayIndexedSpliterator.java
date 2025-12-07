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

package org.apache.jena.mem.spliterator;

import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.jena.mem.collection.FastHashSet;

/**
 * A spliterator for sparse arrays. This spliterator will iterate over the array
 * skipping null entries.
 * This spliterator returns elements as {@link FastHashSet.IndexedKey} objects,
 * which contain both the index and the value of the element.
 * <p>
 * This spliterator works in ascending order, starting from the given start up to the specified exclusive index.
 * <p>
 * This spliterator supports splitting into sub-spliterators.
 * <p>
 * The spliterator will check for concurrent modifications by invoking a {@link Runnable}
 * before each action.
 *
 * @param <E> the type of the array elements
 */
@SuppressWarnings("all")
public class SparseArrayIndexedSpliterator<E> implements Spliterator<FastHashSet.IndexedKey<E>> {

    private final E[] entries;
    private int currentPositionMinusOne;
    private final int toIndexExclusive;
    private final Runnable checkForConcurrentModification;

    /**
     * Create a spliterator for the given array, with the given size.
     *
     * @param entries                        the array
     * @param fromIndexInclusive             the index of the first element, inclusive
     * @param toIndexExclusive               the index of the last element, exclusive
     * @param checkForConcurrentModification runnable to check for concurrent modifications
     */
    public SparseArrayIndexedSpliterator(final E[] entries, final int fromIndexInclusive, final int toIndexExclusive, final Runnable checkForConcurrentModification) {
        this.entries = entries;
        this.currentPositionMinusOne = fromIndexInclusive-1; // Start at fromIndexInclusive - 1, so that the first call to tryAdvance will increment pos to fromIndexInclusive
        this.toIndexExclusive = toIndexExclusive;
        this.checkForConcurrentModification = checkForConcurrentModification;
    }

    /**
     * Create a spliterator for the given array, with the given size.
     *
     * @param entries                        the array
     * @param toIndexExclusive               the index of the last element, exclusive
     * @param checkForConcurrentModification runnable to check for concurrent modifications
     */
    public SparseArrayIndexedSpliterator(final E[] entries, final int toIndexExclusive, final Runnable checkForConcurrentModification) {
        this(entries, 0, toIndexExclusive, checkForConcurrentModification);
    }

    /**
     * Create a spliterator for the given array, with the given size.
     *
     * @param entries                        the array
     * @param checkForConcurrentModification runnable to check for concurrent modifications
     */
    public SparseArrayIndexedSpliterator(final E[] entries, final Runnable checkForConcurrentModification) {
        this(entries, entries.length, checkForConcurrentModification);
    }


    @Override
    public boolean tryAdvance(Consumer<? super FastHashSet.IndexedKey<E>> action) {
        this.checkForConcurrentModification.run();
        while (toIndexExclusive > ++currentPositionMinusOne) {
            if (null != entries[currentPositionMinusOne]) {
                action.accept(new FastHashSet.IndexedKey<>(currentPositionMinusOne, entries[currentPositionMinusOne]));
                return true;
            }
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super FastHashSet.IndexedKey<E>> action) {
        while (toIndexExclusive > ++currentPositionMinusOne) {
            if (null != entries[currentPositionMinusOne]) {
                action.accept(new FastHashSet.IndexedKey<>(currentPositionMinusOne, entries[currentPositionMinusOne]));
            }
        }
        this.checkForConcurrentModification.run();
    }

    @Override
    public Spliterator<FastHashSet.IndexedKey<E>> trySplit() {
        final var nextPos = currentPositionMinusOne + 1;
        final var remaining = toIndexExclusive - nextPos;
        if ( remaining < 2) {
            return null;
        }
        final var mid = nextPos + ( remaining >>> 1);
        final var fromIndexInclusive = nextPos;
        this.currentPositionMinusOne = mid-1;
        return new SparseArrayIndexedSpliterator<>(entries, fromIndexInclusive, mid, checkForConcurrentModification);
    }

    @Override
    public long estimateSize() { return (long) toIndexExclusive - currentPositionMinusOne; }

    @Override
    public int characteristics() {
        return DISTINCT | NONNULL | IMMUTABLE;
    }
}
