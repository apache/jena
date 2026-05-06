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
package org.apache.jena.mem.store.indexed;

import org.apache.jena.atlas.lib.Copyable;

/**
 * Append-only list of {@code int} triple indices, used by the eager indexing
 * strategy as the value type of the per-node index lists ("for the subject
 * node N, here are the indices of all triples whose subject is N").
 * <p>
 * Backed by an int array that grows by factor 1.5.
 * Removal is constant-time swap-with-last so callers must
 * keep an external reverse-index array in sync (see
 * {@link org.apache.jena.mem.store.indexed.EagerStoreStrategy}).
 */
public class IndexList implements Copyable<IndexList> {

    private static final int INITIAL_SIZE = 2;

    private int pos = -1;
    private int[] elements;

    /**
     * Creates an empty list with the default initial capacity.
     */
    public IndexList() {
        elements = new int[INITIAL_SIZE];
    }

    /**
     * Copy constructor. The new list contains the same indices as
     * {@code bunchToCopy}; its backing array is sized to fit exactly,
     * but can grow further if needed.
     *
     * @param bunchToCopy the source list
     */
    public IndexList(final IndexList bunchToCopy) {
        // ensures min size of INITIAL_SIZE, so the new list can grow
        this.elements = new int[Math.max(bunchToCopy.size(), INITIAL_SIZE)];
        System.arraycopy(bunchToCopy.elements, 0, this.elements, 0, bunchToCopy.size());
        this.pos = bunchToCopy.pos;
    }

    /**
     * @return the number of indices currently stored
     */
    public int size() {
        return pos + 1;
    }

    /**
     * @return the index of the last stored element, or {@code -1} if empty
     */
    public int lastPos() {
        return pos;
    }

    /**
     * @return {@code true} if the list contains no indices
     */
    public boolean isEmpty() {
        return this.pos == -1;
    }

    /**
     * Returns the underlying int array. Only the first {@link #size()}
     * entries are valid. Exposed as a raw array to allow callers (e.g.
     * iterators and intersection routines) to avoid bounds-checked accessors
     * in tight loops.
     *
     * @return the backing array
     */
    public int[] getIndices() {
        return elements;
    }

    /**
     * @param pos a position {@code 0 &le; pos &le; lastPos()}
     * @return the index stored at the given position
     */
    public int getIndexAt(final int pos) {
        return this.elements[pos];
    }

    /**
     * Append the given index to the list.
     *
     * @param element the triple index to append
     * @return the position at which {@code element} was stored (i.e. its
     *         "reverse index"); callers track this so they can remove it
     *         later in O(1)
     */
    public int add(final int element) {
        if (++pos == elements.length) grow();
        elements[pos] = element;
        return pos;
    }

    /**
     * Grows the backing array. Grows by factor 1.5.
     * This requires a minimum size of 2 to work.
     */
    private void grow() {
        final var oldElements = elements;
        var newSize = (elements.length >> 1) + elements.length;
        if (newSize < 0) { // catches overflow
            newSize = Integer.MAX_VALUE;
        }
        elements = new int[newSize];
        System.arraycopy(oldElements, 0, elements, 0, pos);
    }

    /**
     * Remove the index at the given position by swapping the last element
     * into its place ("swap-with-last"). The caller is responsible for
     * updating any external reverse-index that points at the moved element.
     *
     * @param position the position of the index to remove
     * @return the triple index of the element that was moved into
     *         {@code position} (so the caller can update its reverse index),
     *         or {@code -1} if the removed element was the last one and
     *         nothing was moved
     */
    public int removeAt(final int position) {
        if(pos == position) {
            pos--;
            return -1;
        } else {
            elements[position] = elements[pos--];
            return elements[position];
        }
    }

    /**
     * Returns an independent copy of this list.
     *
     * @return a deep copy
     */
    @Override
    public IndexList copy() {
        return new IndexList(this);
    }

    /**
     * Test whether two index lists share at least one common triple index.
     * The lists are not assumed to be sorted; this implementation iterates
     * the shorter list and checks each entry against the larger list using
     * the larger list's reverse-index array, giving {@code O(min(|a|,|b|))}.
     *
     * @param a               first list
     * @param reverseIndicesA reverse index for {@code a}: maps a triple index
     *                        to its position in {@code a.getIndices()}
     * @param b               second list
     * @param reverseIndicesB reverse index for {@code b}
     * @return {@code true} if {@code a} and {@code b} share any element
     */
    public static boolean intersects(final IndexList a, final int[] reverseIndicesA, final IndexList b, final int[] reverseIndicesB) {
        if (a.size() < b.size()) {
            return intersectsSmallerWithLarger(a, b, reverseIndicesB);
        } else {
            return intersectsSmallerWithLarger(b, a, reverseIndicesA);
        }
    }

    private static boolean intersectsSmallerWithLarger(final IndexList smaller, final IndexList larger, final int[] reverseIndicesLarger) {
        final var largerSize = larger.size();
        var pos = smaller.lastPos();
        while (-1 < pos) {
            final var tripleIndex = smaller.elements[pos--];
            final var potentialIndexInLarger = reverseIndicesLarger[tripleIndex];
            if(potentialIndexInLarger < largerSize) {
                if(tripleIndex == larger.elements[potentialIndexInLarger]) {
                    return true;
                }
            }
        }
        return false;
    }
}
