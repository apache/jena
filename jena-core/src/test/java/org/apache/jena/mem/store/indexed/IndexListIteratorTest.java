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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.NoSuchElementException;

import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link IndexListIterator}: iterates an {@link IndexList} of
 * triple indices, dereferencing each index against a {@link TripleSet}.
 */
public class IndexListIteratorTest {

    private TripleSet triples;
    private IndexList list;
    private Triple t1;
    private Triple t2;
    private Triple t3;
    private int idx2;

    @Before
    public void setUp() {
        triples = new TripleSet();
        list = new IndexList();
        t1 = triple("s1 p1 o1");
        t2 = triple("s2 p2 o2");
        t3 = triple("s3 p3 o3");
        var idx1 = triples.addAndGetIndex(t1);
        idx2 = triples.addAndGetIndex(t2);
        var idx3 = triples.addAndGetIndex(t3);
        list.add(idx1);
        list.add(idx2);
        list.add(idx3);
    }

    @Test
    public void iteratesAllTriplesInReverseInsertionOrder() {
        final var it = new IndexListIterator(triples, list);
        // The iterator walks from lastPos back to position 0
        assertTrue(it.hasNext());
        assertEquals(t3, it.next());
        assertTrue(it.hasNext());
        assertEquals(t2, it.next());
        assertTrue(it.hasNext());
        assertEquals(t1, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void iteratorOverEmptyListYieldsNothing() {
        final var emptyList = new IndexList();
        final var it = new IndexListIterator(triples, emptyList);
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void nextThrowsWhenExhausted() {
        final var emptyList = new IndexList();
        final var it = new IndexListIterator(triples, emptyList);
        it.next();
    }

    @Test
    public void forEachRemainingVisitsEveryTriple() {
        final var collected = new HashSet<Triple>();
        final var it = new IndexListIterator(triples, list);
        it.forEachRemaining(collected::add);
        final var expected = new HashSet<Triple>();
        expected.add(t1);
        expected.add(t2);
        expected.add(t3);
        assertEquals(expected, collected);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void nextDetectsConcurrentModification() {
        final var it = new IndexListIterator(triples, list);
        // Adding a new triple to the canonical set after constructing the
        // iterator must invalidate it.
        triples.addAndGetIndex(triple("s4 p4 o4"));
        it.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void forEachRemainingDetectsConcurrentModification() {
        final var it = new IndexListIterator(triples, list);
        triples.addAndGetIndex(triple("s5 p5 o5"));
        it.forEachRemaining(t -> {});
    }

    @Test
    public void singleElementListIteratesExactlyOnce() {
        final var single = new IndexList();
        single.add(idx2);
        final var it = new IndexListIterator(triples, single);
        final var collected = new ArrayList<Triple>();
        while (it.hasNext()) {
            collected.add(it.next());
        }
        assertEquals(1, collected.size());
        assertEquals(t2, collected.get(0));
    }
}
