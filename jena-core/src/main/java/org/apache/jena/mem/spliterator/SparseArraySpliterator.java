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
 * Top-level spliterator over a sparse array slice {@code [0, toIndex)},
 * iterating from high index to low and skipping {@code null} entries.
 * Produced for backing arrays such as those of
 * {@link org.apache.jena.mem.collection.FastHashBase}, where removed slots
 * are represented by {@code null}.
 * <p>
 * Supports splitting into {@link SparseArraySubSpliterator} children for
 * parallel traversal. Detects concurrent modifications by snapshotting
 * {@code set.size()} at construction time and rechecking it at each
 * advance/forEach boundary; throws {@link ConcurrentModificationException}
 * if the size has changed.
 *
 * @param <E> the type of the array elements
 */
public class SparseArraySpliterator<E> implements Spliterator<E> {

    private final E[] entries;
    private int pos;
    private final Sized set;
    private final int sizeOfSetAtStart;

    /**
     * Create a spliterator over {@code entries[0 .. toIndex)}, skipping nulls.
     *
     * @param entries the backing array (not copied)
     * @param toIndex exclusive upper bound on the iterated slice
     * @param set     the owning collection used to detect concurrent modifications
     */
    public SparseArraySpliterator(final E[] entries, final int toIndex, final Sized set) {
        this.entries = entries;
        this.pos = toIndex;
        this.set = set;
        this.sizeOfSetAtStart = set.size();
    }

    /**
     * Create a spliterator over the entire array, skipping nulls.
     *
     * @param entries the backing array (not copied)
     * @param set     the owning collection used to detect concurrent modifications
     */
    public SparseArraySpliterator(final E[] entries, final Sized set) {
        this(entries, entries.length, set);
    }


    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (sizeOfSetAtStart != set.size()) throw new ConcurrentModificationException();
        while (-1 < --pos) {
            if (null != entries[pos]) {
                action.accept(entries[pos]);
                return true;
            }
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        pos--;
        while (-1 < pos) {
            if (null != entries[pos]) {
                action.accept(entries[pos]);
            }
            pos--;
        }
        if (sizeOfSetAtStart != set.size()) throw new ConcurrentModificationException();
    }

    @Override
    public Spliterator<E> trySplit() {
        if (pos < 2) {
            return null;
        }
        final int toIndexOfSubIterator = this.pos;
        this.pos = pos >>> 1;
        return new SparseArraySubSpliterator<>(entries, this.pos, toIndexOfSubIterator, set);
    }

    @Override
    public long estimateSize() { return pos; }

    @Override
    public int characteristics() {
        return DISTINCT | NONNULL | IMMUTABLE;
    }
}
