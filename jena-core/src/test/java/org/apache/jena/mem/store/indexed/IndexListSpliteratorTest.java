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
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link IndexListSpliterator}: walks an {@link IndexList},
 * dereferences each index against a {@link TripleSet}, and supports
 * recursive splitting for parallel traversal.
 */
public class IndexListSpliteratorTest {

    private TripleSet triples;
    private IndexList list;
    private List<Triple> expected;

    @Before
    public void setUp() {
        triples = new TripleSet();
        list = new IndexList();
        expected = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final var t = triple("s" + i + " p o");
            expected.add(t);
            list.add(triples.addAndGetIndex(t));
        }
    }

    @Test
    public void tryAdvanceVisitsEveryTriple() {
        final var sp = new IndexListSpliterator(triples, list);
        final var collected = new ArrayList<Triple>();
        //noinspection StatementWithEmptyBody
        while (sp.tryAdvance(collected::add)) { /* noop */ }
        assertEquals(new HashSet<>(expected), new HashSet<>(collected));
        assertEquals(expected.size(), collected.size());
    }

    @Test
    public void forEachRemainingVisitsEveryTriple() {
        final var sp = new IndexListSpliterator(triples, list);
        final var collected = new ArrayList<Triple>();
        sp.forEachRemaining(collected::add);
        assertEquals(new HashSet<>(expected), new HashSet<>(collected));
    }

    @Test
    public void streamYieldsAllTriples() {
        final var sp = new IndexListSpliterator(triples, list);
        final var collected = StreamSupport.stream(sp, false).toList();
        assertEquals(new HashSet<>(expected), new HashSet<>(collected));
    }

    @Test
    public void parallelStreamYieldsAllTriples() {
        final var sp = new IndexListSpliterator(triples, list);
        final var collected = StreamSupport.stream(sp, true).toList();
        assertEquals(new HashSet<>(expected), new HashSet<>(collected));
    }

    @Test
    public void trySplitProducesNonOverlappingHalves() {
        final var sp = new IndexListSpliterator(triples, list);
        final var prefix = sp.trySplit();
        assertNotNull("a list of 8 must split", prefix);

        final var firstHalf = new ArrayList<Triple>();
        final var secondHalf = new ArrayList<Triple>();
        prefix.forEachRemaining(firstHalf::add);
        sp.forEachRemaining(secondHalf::add);

        // Sums must equal the full set
        final var combined = new ArrayList<>(firstHalf);
        combined.addAll(secondHalf);
        assertEquals(expected.size(), combined.size());
        assertEquals(new HashSet<>(expected), new HashSet<>(combined));

        // The two halves are disjoint
        final var asSetA = new HashSet<>(firstHalf);
        for (final var t : secondHalf) {
            assertFalse("split must be disjoint", asSetA.contains(t));
        }
    }

    @Test
    public void trySplitReturnsNullWhenNothingLeftToSplit() {
        final var single = new IndexList();
        single.add(triples.addAndGetIndex(triple("only s p o")));
        final var sp = new IndexListSpliterator(triples, single);
        // Single element cannot be split
        assertNull(sp.trySplit());
    }

    @Test
    public void characteristicsAdvertiseDistinctSizedNonNull() {
        final var sp = new IndexListSpliterator(triples, list);
        final int chars = sp.characteristics();
        assertTrue((chars & Spliterator.DISTINCT) != 0);
        assertTrue((chars & Spliterator.SIZED) != 0);
        assertTrue((chars & Spliterator.SUBSIZED) != 0);
        assertTrue((chars & Spliterator.NONNULL) != 0);
    }

    @Test
    public void estimateSizeAndExactSizeMatchRemainingCount() {
        final var sp = new IndexListSpliterator(triples, list);
        assertEquals(expected.size(), sp.estimateSize());
        assertEquals(expected.size(), sp.getExactSizeIfKnown());

        // After consuming one element, both must drop
        sp.tryAdvance(t -> {});
        assertEquals(expected.size() - 1L, sp.estimateSize());
        assertEquals(expected.size() - 1L, sp.getExactSizeIfKnown());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void tryAdvanceDetectsConcurrentModification() {
        final var sp = new IndexListSpliterator(triples, list);
        triples.addAndGetIndex(triple("s99 p o"));
        sp.tryAdvance(t -> {});
    }

    @Test(expected = ConcurrentModificationException.class)
    public void forEachRemainingDetectsConcurrentModification() {
        final var sp = new IndexListSpliterator(triples, list);
        triples.addAndGetIndex(triple("s99 p o"));
        sp.forEachRemaining(t -> {});
    }
}
