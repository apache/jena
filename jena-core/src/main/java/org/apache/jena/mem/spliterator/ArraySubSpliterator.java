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

package org.apache.jena.mem.spliterator;

import org.apache.jena.mem.collection.Sized;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Sub-range spliterator over a contiguous array slice {@code [fromIndex, toIndex)},
 * iterating from high index to low. Produced by splitting an
 * {@link ArraySpliterator} (or another {@link ArraySubSpliterator}); supports
 * further recursive splits for parallel traversal.
 * <p>
 * Detects concurrent modifications by snapshotting {@code set.size()} at
 * construction time and rechecking it at each advance/forEach boundary.
 * Throws {@link ConcurrentModificationException} if the size has changed.
 *
 * @param <E> the element type
 */
public class ArraySubSpliterator<E> implements Spliterator<E> {

    private final E[] entries;
    private final int fromIndex;
    private final Sized set;
    private final int sizeOfSetAtStart;
    private int pos;

    /**
     * Create a spliterator over {@code entries[fromIndex .. toIndex)}.
     *
     * @param entries   the backing array (not copied)
     * @param fromIndex inclusive lower bound on the iterated slice
     * @param toIndex   exclusive upper bound on the iterated slice
     * @param set       the owning collection used to detect concurrent modifications
     */
    public ArraySubSpliterator(final E[] entries, final int fromIndex, final int toIndex, final Sized set) {
        this.entries = entries;
        this.fromIndex = fromIndex;
        this.pos = toIndex;
        this.set = set;
        this.sizeOfSetAtStart = set.size();
    }

    /**
     * Create a spliterator over the entire array.
     *
     * @param entries the backing array (not copied)
     * @param set     the owning collection used to detect concurrent modifications
     */
    public ArraySubSpliterator(final E[] entries, final Sized set) {
        this(entries, 0, entries.length, set);
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (sizeOfSetAtStart != set.size()) throw new ConcurrentModificationException();
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
        if (sizeOfSetAtStart != set.size()) throw new ConcurrentModificationException();
    }

    @Override
    public Spliterator<E> trySplit() {
        final int entriesCount = pos - fromIndex;
        if (entriesCount < 2) {
            return null;
        }
        final int toIndexOfSubIterator = this.pos;
        this.pos = fromIndex + (entriesCount >>> 1);
        return new ArraySubSpliterator<>(entries, this.pos, toIndexOfSubIterator, set);
    }

    @Override
    public long estimateSize() {
        return pos - fromIndex;
    }

    @Override
    public int characteristics() {
        return DISTINCT | SIZED | SUBSIZED | NONNULL ;
    }
}