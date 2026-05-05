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
package org.apache.jena.mem.store.fast;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.spliterator.ArraySpliterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Linear-scan implementation of {@link FastTripleBunch} backed by a packed
 * {@link Triple} array. Used as long as a bunch stays small; once it grows
 * past the configured threshold (see {@link FastTripleStore}) it is replaced
 * with a {@link FastHashedTripleBunch}.
 * <p>
 * The array grows by a factor of two when full. Equality of triples within a
 * bunch is delegated to {@link #areEqual(Triple, Triple)}, which subclasses
 * specialize to compare only the two nodes that are <em>not</em> already
 * implied by the enclosing map's key. This avoids redundant equality checks
 * on the shared subject/predicate/object.
 * <p>
 * Not thread-safe.
 */
public abstract class FastArrayBunch implements FastTripleBunch {

    private static final int INITIAL_SIZE = 4;

    /** Number of valid entries in {@link #elements}. */
    protected int size = 0;
    /** Packed array of triples; entries from {@code 0} to {@code size-1} are live. */
    protected Triple[] elements;

    /**
     * Creates an empty bunch with the default initial capacity.
     */
    protected FastArrayBunch() {
        elements = new Triple[INITIAL_SIZE];
    }

    /**
     * Copy constructor. The new bunch contains the same triples as
     * {@code bunchToCopy}; its backing array is sized to fit exactly,
     * but can grow further if needed.
     *
     * @param bunchToCopy the source bunch
     */
    protected FastArrayBunch(final FastArrayBunch bunchToCopy) {
        this.elements = new Triple[bunchToCopy.size];
        System.arraycopy(bunchToCopy.elements, 0, this.elements, 0, bunchToCopy.size);
        this.size = bunchToCopy.size;
    }

    /**
     * Compare two triples for equality within this bunch.
     * <p>
     * Subclasses specialize this to skip the already-shared component
     * (subject, predicate or object) and compare only the remaining two.
     *
     * @param a first triple
     * @param b second triple
     * @return {@code true} if the triples are considered equal in this bunch
     */
    protected abstract boolean areEqual(final Triple a, final Triple b);

    @Override
    public boolean containsKey(Triple t) {
        int i = size;
        while (i > 0) if (areEqual(t, elements[--i])) return true;
        return false;
    }

    @Override
    public boolean anyMatch(final Predicate<Triple> predicate) {
        int i = size;
        while (i > 0) if (predicate.test(elements[--i])) return true;
        return false;
    }

    @Override
    public boolean anyMatchRandomOrder(Predicate<Triple> predicate) {
        return anyMatch(predicate);
    }

    @Override
    public void clear() {
        this.elements = new Triple[INITIAL_SIZE];
        this.size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean tryAdd(final Triple t) {
        if (this.containsKey(t)) return false;
        if (size == elements.length) grow();
        elements[size++] = t;
        return true;
    }

    @Override
    public void addUnchecked(final Triple t) {
        if (size == elements.length) grow();
        elements[size++] = t;
    }

    /**
     * Note: linear growth is suboptimal (order n<sup>2</sup>) normally, but
     * ArrayBunch's are meant for <i>small</i> sets and are replaced by some
     * sort of hash- or tree- set when they get big; currently "big" means more
     * than 9 elements, so that's only one growth spurt anyway.
     */
    protected void grow() {
        final var oldElements = elements;
        elements = new Triple[size << 1];
        System.arraycopy(oldElements, 0, elements, 0, size);
    }

    @Override
    public boolean tryRemove(final Triple t) {
        for (int i = 0; i < size; i++) {
            if (areEqual(t, elements[i])) {
                elements[i] = elements[--size];
                elements[size] = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeUnchecked(final Triple t) {
        for (int i = 0; i < size; i++) {
            if (areEqual(t, elements[i])) {
                elements[i] = elements[--size];
                elements[size] = null;
                return;
            }
        }
    }

    @Override
    public ExtendedIterator<Triple> keyIterator() {
        return new NiceIterator<>() {
            private final int initialSize = size;

            private int i = size;

            @Override
            public boolean hasNext() {
                return 0 < i;
            }

            @Override
            public Triple next() {
                if (size != initialSize) throw new ConcurrentModificationException();
                if (i == 0) throw new NoSuchElementException();
                return elements[--i];
            }

            @Override
            public void forEachRemaining(Consumer<? super Triple> action) {
                while (0 < i--) action.accept(elements[i]);
                if (size != initialSize) throw new ConcurrentModificationException();
            }
        };

    }


    @Override
    public Spliterator<Triple> keySpliterator() {
        return new ArraySpliterator<>(elements, size, this);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean tryAdd(Triple key, int hashCode) {
        return tryAdd(key);
    }

    @Override
    public void addUnchecked(Triple key, int hashCode) {
        addUnchecked(key);
    }

    @Override
    public boolean tryRemove(Triple key, int hashCode) {
        return tryRemove(key);
    }

    @Override
    public void removeUnchecked(Triple key, int hashCode) {
        removeUnchecked(key);
    }
}
