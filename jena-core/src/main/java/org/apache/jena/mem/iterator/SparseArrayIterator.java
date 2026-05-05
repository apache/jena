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

package org.apache.jena.mem.iterator;

import org.apache.jena.mem.collection.Sized;
import org.apache.jena.util.iterator.NiceIterator;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Iterator over a sparse array, walking from high index to low and skipping
 * {@code null} entries. Detects concurrent modifications by snapshotting
 * {@code set.size()} at construction time and rechecking it on each call to
 * {@link #next()} / {@link #forEachRemaining(Consumer)}; throws
 * {@link ConcurrentModificationException} if the size has changed.
 *
 * @param <E> the type of the array elements
 */
public class SparseArrayIterator<E> extends NiceIterator<E> {

    private final E[] entries;
    private final Sized set;
    private final int sizeOfSetAtStart;
    private int pos;
    private boolean hasNext = false;

    /**
     * Iterate over the whole array.
     *
     * @param entries the backing array (not copied)
     * @param set     the owning collection used to detect concurrent modifications
     */
    public SparseArrayIterator(final E[] entries, final Sized set) {
        this.entries = entries;
        this.pos = entries.length - 1;
        this.set = set;
        this.sizeOfSetAtStart = set.size();
    }

    /**
     * Iterate over {@code entries[0 .. toIndexExclusive)} (in reverse order).
     *
     * @param entries          the backing array (not copied)
     * @param toIndexExclusive exclusive upper bound on the iterated slice
     * @param set              the owning collection used to detect concurrent modifications
     */
    public SparseArrayIterator(final E[] entries, int toIndexExclusive, final Sized set) {
        this.entries = entries;
        this.pos = toIndexExclusive - 1;
        this.set = set;
        this.sizeOfSetAtStart = set.size();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        while (-1 < pos) {
            if (null != entries[pos]) {
                return hasNext = true;
            }
            pos--;
        }
        return hasNext = false;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public E next() {
        if (sizeOfSetAtStart != set.size()) throw new ConcurrentModificationException();
        if (hasNext || hasNext()) {
            hasNext = false;
            return entries[pos--];
        }
        throw new NoSuchElementException();
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        while (-1 < pos) {
            if (null != entries[pos]) {
                action.accept(entries[pos]);
            }
            pos--;
        }
        if (sizeOfSetAtStart != set.size()) throw new ConcurrentModificationException();
    }
}
