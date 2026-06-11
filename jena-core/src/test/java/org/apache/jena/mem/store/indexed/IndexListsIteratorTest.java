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

import org.apache.jena.graph.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IndexListsIterator}: lazily walks the intersection
 * of two {@link IndexList}s using their reverse-index arrays.
 */
public class IndexListsIteratorTest {

    private TripleSet triples;
    private int[] reverseA;
    private int[] reverseB;

    @BeforeEach
    public void setUp() {
        triples = new TripleSet();
        // pre-size reverse arrays generously; the test triples will get
        // small indices.
        reverseA = new int[64];
        reverseB = new int[64];
    }

    private int addTriple(final String spec) {
        return triples.addAndGetIndex(triple(spec));
    }

    private static void appendTo(final IndexList list, final int[] reverse, final int tripleIndex) {
        final int pos = list.add(tripleIndex);
        reverse[tripleIndex] = pos;
    }

    @Test
    public void iteratesIntersectionOfTwoLists() {
        final int idx1 = addTriple("s p1 o1");
        final int idx2 = addTriple("s p2 o2");
        final int idx3 = addTriple("s p3 o3");
        final int idx4 = addTriple("x p4 o4");
        final int idx5 = addTriple("y p5 o5");

        final var listA = new IndexList();
        appendTo(listA, reverseA, idx1);
        appendTo(listA, reverseA, idx2);
        appendTo(listA, reverseA, idx3);

        final var listB = new IndexList();
        appendTo(listB, reverseB, idx2);
        appendTo(listB, reverseB, idx3);
        appendTo(listB, reverseB, idx4);
        appendTo(listB, reverseB, idx5);

        final var it = new IndexListsIterator(triples,
                listA, reverseA, listB, reverseB);
        final var collected = new HashSet<Triple>();
        while (it.hasNext()) {
            collected.add(it.next());
        }
        final var expected = new HashSet<Triple>();
        expected.add(triple("s p2 o2"));
        expected.add(triple("s p3 o3"));
        assertEquals(expected, collected);
    }

    @Test
    public void emptyIntersectionYieldsNoElements() {
        final int idx1 = addTriple("a b c");
        final int idx2 = addTriple("d e f");
        final int idx3 = addTriple("g h i");

        final var listA = new IndexList();
        appendTo(listA, reverseA, idx1);

        final var listB = new IndexList();
        appendTo(listB, reverseB, idx2);
        appendTo(listB, reverseB, idx3);

        final var it = new IndexListsIterator(triples,
                listA, reverseA, listB, reverseB);
        assertFalse(it.hasNext());
    }

    @Test
    public void nextThrowsWhenIntersectionExhausted() {
        final int idx1 = addTriple("a b c");
        final int idx2 = addTriple("d e f");

        final var listA = new IndexList();
        appendTo(listA, reverseA, idx1);
        final var listB = new IndexList();
        appendTo(listB, reverseB, idx2);

        final var it = new IndexListsIterator(triples,
                listA, reverseA, listB, reverseB);
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    public void hasNextIsIdempotent() {
        final int idx1 = addTriple("a b c");

        final var listA = new IndexList();
        appendTo(listA, reverseA, idx1);
        final var listB = new IndexList();
        appendTo(listB, reverseB, idx1);

        final var it = new IndexListsIterator(triples,
                listA, reverseA, listB, reverseB);
        assertTrue(it.hasNext());
        // calling hasNext repeatedly must not advance
        //noinspection ConstantValue
        assertTrue(it.hasNext());
        //noinspection ConstantValue
        assertTrue(it.hasNext());
        assertEquals(triple("a b c"), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void forEachRemainingVisitsIntersectionOnly() {
        final int idx1 = addTriple("a b c");
        final int idx2 = addTriple("d e f");
        final int idx3 = addTriple("g h i");
        final int idx4 = addTriple("j k l");

        final var listA = new IndexList();
        appendTo(listA, reverseA, idx1);
        appendTo(listA, reverseA, idx2);
        appendTo(listA, reverseA, idx3);

        final var listB = new IndexList();
        appendTo(listB, reverseB, idx2);
        appendTo(listB, reverseB, idx3);
        appendTo(listB, reverseB, idx4);

        final var it = new IndexListsIterator(triples,
                listA, reverseA, listB, reverseB);
        final var collected = new ArrayList<Triple>();
        it.forEachRemaining(collected::add);
        final var expected = new HashSet<Triple>();
        expected.add(triple("d e f"));
        expected.add(triple("g h i"));
        assertEquals(expected, new HashSet<>(collected));
    }

    @Test
    public void forEachRemainingFlushesPrefetchedHasNextElement() {
        final int idx1 = addTriple("a b c");
        final int idx2 = addTriple("d e f");

        final var listA = new IndexList();
        appendTo(listA, reverseA, idx1);
        appendTo(listA, reverseA, idx2);

        final var listB = new IndexList();
        appendTo(listB, reverseB, idx1);
        appendTo(listB, reverseB, idx2);

        final var it = new IndexListsIterator(triples,
                listA, reverseA, listB, reverseB);
        // prime the look-ahead buffer
        assertTrue(it.hasNext());

        final var collected = new ArrayList<Triple>();
        it.forEachRemaining(collected::add);
        // Both intersected triples must be reported, including the pre-fetched one.
        assertEquals(new HashSet<>(Arrays.asList(triple("a b c"), triple("d e f"))),
                new HashSet<>(collected));
    }

    @Test
    public void nextDetectsConcurrentModification() {
        final int idx1 = addTriple("a b c");

        final var listA = new IndexList();
        appendTo(listA, reverseA, idx1);
        final var listB = new IndexList();
        appendTo(listB, reverseB, idx1);

        final var it = new IndexListsIterator(triples,
                listA, reverseA, listB, reverseB);
        triples.addAndGetIndex(triple("z z z"));
        assertThrows(ConcurrentModificationException.class, () -> it.next());
    }
}
