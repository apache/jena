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

package org.apache.jena.mem2.spliterator;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A spliterator for arrays. This spliterator will iterate over the array
 * entries within the given boundaries.
 * <p>
 * This spliterator supports splitting into sub-spliterators.
 * <p>
 * The spliterator will check for concurrent modifications by invoking a {@link Runnable}
 * before each action.
 *
 * @param <E>
 */
public class ArraySubSpliterator<E> implements Spliterator<E> {

    private final E[] entries;
    private final int fromIndex;
    private final Runnable checkForConcurrentModification;
    private int pos;

    /**
     * Create a spliterator for the given array, with the given size.
     *
     * @param entries                        the array
     * @param fromIndex                      the index of the first element, inclusive
     * @param toIndex                        the index of the last element, exclusive
     * @param checkForConcurrentModification runnable to check for concurrent modifications
     */
    public ArraySubSpliterator(final E[] entries, final int fromIndex, final int toIndex, final Runnable checkForConcurrentModification) {
        this.entries = entries;
        this.fromIndex = fromIndex;
        this.pos = toIndex;
        this.checkForConcurrentModification = checkForConcurrentModification;
    }

    /**
     * Create a spliterator for the given array, with the given size.
     *
     * @param entries                        the array
     * @param checkForConcurrentModification runnable to check for concurrent modifications
     */
    public ArraySubSpliterator(final E[] entries, final Runnable checkForConcurrentModification) {
        this(entries, 0, entries.length, checkForConcurrentModification);
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        this.checkForConcurrentModification.run();
        if (fromIndex <= --pos) {
            action.accept(entries[pos]);
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        while (fromIndex <= --pos) {
            action.accept(entries[pos]);
        }
        this.checkForConcurrentModification.run();
    }

    @Override
    public Spliterator<E> trySplit() {
        final int entriesCount = pos - fromIndex;
        if (entriesCount < 2) {
            return null;
        }
        final int toIndexOfSubIterator = this.pos;
        this.pos = fromIndex + (entriesCount >>> 1);
        return new ArraySubSpliterator<>(entries, this.pos, toIndexOfSubIterator, checkForConcurrentModification);
    }

    @Override
    public long estimateSize() {
        return pos - fromIndex;
    }

    @Override
    public int characteristics() {
        return DISTINCT | SIZED | SUBSIZED | NONNULL | IMMUTABLE;
    }
}
