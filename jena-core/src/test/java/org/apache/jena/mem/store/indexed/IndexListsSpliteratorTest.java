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
import java.util.stream.StreamSupport;

import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IndexListsSpliterator}: walks the intersection of
 * two {@link IndexList}s and supports recursive splitting.
 */
public class IndexListsSpliteratorTest {

    private TripleSet triples;
    private int[] reverseA;
    private int[] reverseB;
    private IndexList listA;
    private IndexList listB;
    private List<Triple> commonTriples;

    @BeforeEach
    public void setUp() {
        triples = new TripleSet();
        reverseA = new int[64];
        reverseB = new int[64];

        listA = new IndexList();
        listB = new IndexList();
        commonTriples = new ArrayList<>();

        // Eight triples in A, last six also in B.
        for (int i = 0; i < 8; i++) {
            final var t = triple("a" + i + " p o");
            final int idx = triples.addAndGetIndex(t);
            appendTo(listA, reverseA, idx);
            if (i >= 2) {
                appendTo(listB, reverseB, idx);
                commonTriples.add(t);
            }
        }
        // Two extra triples in B only
        for (int i = 0; i < 2; i++) {
            final var t = triple("b" + i + " p o");
            final int idx = triples.addAndGetIndex(t);
            appendTo(listB, reverseB, idx);
        }
    }

    private static void appendTo(final IndexList list, final int[] reverse, final int tripleIndex) {
        final int pos = list.add(tripleIndex);
        reverse[tripleIndex] = pos;
    }

    @Test
    public void tryAdvanceVisitsExactlyTheIntersection() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        final var collected = new HashSet<Triple>();
        //noinspection StatementWithEmptyBody
        while (sp.tryAdvance(collected::add)) { /* noop */ }
        assertEquals(new HashSet<>(commonTriples), collected);
    }

    @Test
    public void forEachRemainingVisitsIntersectionOnly() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        final var collected = new HashSet<Triple>();
        sp.forEachRemaining(collected::add);
        assertEquals(new HashSet<>(commonTriples), collected);
    }

    @Test
    public void streamYieldsIntersection() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        final var collected = StreamSupport.stream(sp, false).toList();
        assertEquals(new HashSet<>(commonTriples), new HashSet<>(collected));
        assertEquals(commonTriples.size(), collected.size());
    }

    @Test
    public void parallelStreamYieldsIntersection() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        final var collected = StreamSupport.stream(sp, true).toList();
        assertEquals(new HashSet<>(commonTriples), new HashSet<>(collected));
    }

    @Test
    public void trySplitDividesIntersectionAcrossHalves() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        final var prefix = sp.trySplit();
        assertNotNull(prefix);

        final var firstHalf = new ArrayList<Triple>();
        final var secondHalf = new ArrayList<Triple>();
        prefix.forEachRemaining(firstHalf::add);
        sp.forEachRemaining(secondHalf::add);

        final var combined = new ArrayList<>(firstHalf);
        combined.addAll(secondHalf);
        assertEquals(new HashSet<>(commonTriples), new HashSet<>(combined));
        assertEquals(commonTriples.size(), combined.size());

        final var asSetA = new HashSet<>(firstHalf);
        for (final var t : secondHalf) {
            assertFalse(asSetA.contains(t), "split must be disjoint");
        }
    }

    @Test
    public void trySplitReturnsNullWhenSmallerListCannotBeSplit() {
        // Use a tiny smaller list (size 1) to force trySplit to return null.
        final var triplesLocal = new TripleSet();
        final int[] revA = new int[8];
        final int[] revB = new int[8];
        final var a = new IndexList();
        final var b = new IndexList();
        final int idx = triplesLocal.addAndGetIndex(triple("only s p o"));
        appendTo(a, revA, idx);
        appendTo(b, revB, idx);
        final var sp = new IndexListsSpliterator(triplesLocal, a, revA, b, revB);
        assertNull(sp.trySplit());
    }

    @Test
    public void characteristicsAdvertiseDistinctNonNullButNotSized() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        final int chars = sp.characteristics();
        assertTrue((chars & Spliterator.DISTINCT) != 0);
        assertTrue((chars & Spliterator.NONNULL) != 0);
        // Intersection size is not known up front, so SIZED must NOT be set.
        assertEquals(0, chars & Spliterator.SIZED);
    }

    @Test
    public void getExactSizeIfKnownReportsUnknown() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        assertEquals(-1, sp.getExactSizeIfKnown());
    }

    @Test
    public void estimateSizeIsAtMostSmallerListSize() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        // estimateSize is the remaining range of the smaller list, which is
        // an upper bound on the intersection.
        final long est = sp.estimateSize();
        assertTrue(est >= commonTriples.size(), "estimate must bound actual intersection");
    }

    @Test
    public void tryAdvanceDetectsConcurrentModification() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        triples.addAndGetIndex(triple("z z z"));
        assertThrows(ConcurrentModificationException.class, () -> sp.tryAdvance(t -> {}));
    }

    @Test
    public void forEachRemainingDetectsConcurrentModification() {
        final var sp = new IndexListsSpliterator(triples, listA, reverseA, listB, reverseB);
        triples.addAndGetIndex(triple("z z z"));
        assertThrows(ConcurrentModificationException.class, () -> sp.forEachRemaining(t -> {}));
    }
}
