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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IndexList}: append-only int list with O(1)
 * swap-with-last removal, used as the value type of the per-node index
 * lists in the eager indexing strategy.
 */
public class IndexListTest {

    @Test
    public void newListIsEmpty() {
        final var list = new IndexList();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertEquals(-1, list.lastPos());
    }

    @Test
    public void addReturnsSequentialPositions() {
        final var list = new IndexList();
        assertEquals(0, list.add(10));
        assertEquals(1, list.add(20));
        assertEquals(2, list.add(30));

        assertFalse(list.isEmpty());
        assertEquals(3, list.size());
        assertEquals(2, list.lastPos());
        assertEquals(10, list.getIndexAt(0));
        assertEquals(20, list.getIndexAt(1));
        assertEquals(30, list.getIndexAt(2));
    }

    @Test
    public void addGrowsBackingArrayBeyondInitialCapacity() {
        final var list = new IndexList();
        // initial backing array has length 2 (INITIAL_SIZE), so adding more
        // than two elements forces at least one grow().
        for (int i = 0; i < 100; i++) {
            assertEquals(i, list.add(i * 7));
        }
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i * 7, list.getIndexAt(i));
        }
        assertTrue(list.getIndices().length >= 100, "backing array must have grown");
    }

    @Test
    public void getIndicesExposesRawArray() {
        final var list = new IndexList();
        list.add(7);
        list.add(11);
        final var raw = list.getIndices();
        // first list.size() entries must contain the values in insertion order
        assertEquals(7, raw[0]);
        assertEquals(11, raw[1]);
    }

    @Test
    public void removeAtLastReturnsMinusOne() {
        final var list = new IndexList();
        list.add(10);
        list.add(20);

        // removing the last element does not move anything
        assertEquals(-1, list.removeAt(1));
        assertEquals(1, list.size());
        assertEquals(10, list.getIndexAt(0));
    }

    @Test
    public void removeAtMiddleSwapsWithLast() {
        final var list = new IndexList();
        list.add(10);
        list.add(20);
        list.add(30);

        // removing position 0 swaps the last element (30) into position 0.
        // The return value tells the caller that the triple-index 30 moved.
        assertEquals(30, list.removeAt(0));
        assertEquals(2, list.size());
        assertEquals(30, list.getIndexAt(0));
        assertEquals(20, list.getIndexAt(1));
    }

    @Test
    public void removeUntilEmpty() {
        final var list = new IndexList();
        list.add(1);
        list.add(2);
        list.add(3);

        list.removeAt(2);
        list.removeAt(1);
        list.removeAt(0);

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertEquals(-1, list.lastPos());
    }

    @Test
    public void copyConstructorIsIndependent() {
        final var src = new IndexList();
        src.add(1);
        src.add(2);
        src.add(3);

        final var copy = new IndexList(src);
        assertEquals(src.size(), copy.size());
        for (int i = 0; i < src.size(); i++) {
            assertEquals(src.getIndexAt(i), copy.getIndexAt(i));
        }

        // mutating the copy must not affect the source
        copy.add(99);
        assertEquals(4, copy.size());
        assertEquals(3, src.size());
    }

    @Test
    public void copyEqualsCopyConstructor() {
        final var src = new IndexList();
        src.add(11);
        src.add(22);
        src.add(33);

        final var clone = src.copy();
        assertNotSame(src, clone);
        assertEquals(src.size(), clone.size());
        for (int i = 0; i < src.size(); i++) {
            assertEquals(src.getIndexAt(i), clone.getIndexAt(i));
        }
    }

    @Test
    public void growAfterCopy() {
        final var src = new IndexList();

        {   // empty source list: clone should be empty but independent
            final var clone = src.copy();
            assertNotSame(src, clone);
            assertEquals(src.size(), clone.size());

            clone.add(11);
            clone.add(22);
            clone.add(33);
            assertEquals(3, clone.size());
            assertTrue(src.isEmpty());
        }

        src.add(111); // add first element

        {   // non-empty source list: clone should have same content but be independent
            final var srcSizeBefore = src.size();
            final var clone = src.copy();
            assertNotSame(src, clone);
            assertEquals(src.size(), clone.size());

            clone.add(11);
            clone.add(22);
            clone.add(33);
            assertEquals(3+srcSizeBefore, clone.size());
            assertEquals(srcSizeBefore, src.size());
        }

        src.add(222); // add second element

        {   // non-empty source list: clone should have same content but be independent
            final var srcSizeBefore = src.size();
            final var clone = src.copy();
            assertNotSame(src, clone);
            assertEquals(src.size(), clone.size());

            clone.add(11);
            clone.add(22);
            clone.add(33);
            assertEquals(3+srcSizeBefore, clone.size());
            assertEquals(srcSizeBefore, src.size());
        }
    }

    @Test
    public void intersectsReturnsTrueForCommonElement() {
        final var a = new IndexList();
        final var b = new IndexList();
        // Triple indices 0..9 used; reverseIndices arrays must be at least
        // that long.
        final int universe = 16;
        final int[] reverseA = new int[universe];
        final int[] reverseB = new int[universe];

        addToList(a, reverseA, 1);
        addToList(a, reverseA, 4);
        addToList(a, reverseA, 7);

        addToList(b, reverseB, 7);
        addToList(b, reverseB, 9);

        assertTrue(IndexList.intersects(a, reverseA, b, reverseB));
        // symmetric
        assertTrue(IndexList.intersects(b, reverseB, a, reverseA));
    }

    @Test
    public void intersectsReturnsFalseForDisjointLists() {
        final var a = new IndexList();
        final var b = new IndexList();
        final int universe = 16;
        final int[] reverseA = new int[universe];
        final int[] reverseB = new int[universe];

        addToList(a, reverseA, 1);
        addToList(a, reverseA, 2);
        addToList(a, reverseA, 3);

        addToList(b, reverseB, 4);
        addToList(b, reverseB, 5);
        addToList(b, reverseB, 6);

        assertFalse(IndexList.intersects(a, reverseA, b, reverseB));
        assertFalse(IndexList.intersects(b, reverseB, a, reverseA));
    }

    @Test
    public void intersectsHandlesEmptyList() {
        final var a = new IndexList();
        final var b = new IndexList();
        final int[] reverseA = new int[8];
        final int[] reverseB = new int[8];

        addToList(b, reverseB, 1);
        addToList(b, reverseB, 2);

        assertFalse(IndexList.intersects(a, reverseA, b, reverseB));
        assertFalse(IndexList.intersects(b, reverseB, a, reverseA));
    }

    /** Helper that mirrors how EagerStoreStrategy keeps its reverse-index in sync. */
    private static void addToList(final IndexList list, final int[] reverseIndices, final int tripleIndex) {
        final int pos = list.add(tripleIndex);
        reverseIndices[tripleIndex] = pos;
    }
}
