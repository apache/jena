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

import org.apache.jena.graph.Node;
import org.junit.Test;

import static org.apache.jena.junit.GraphHelper.node;
import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link FastHashedBunchMap}: a {@link Node}-keyed map of
 * {@link FastTripleBunch}es, with a deep-copying constructor.
 */
public class FastHashedBunchMapTest {

    @Test
    public void newMapIsEmpty() {
        final var map = new FastHashedBunchMap();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void putAndGetReturnSameBunch() {
        final var map = new FastHashedBunchMap();
        final Node key = node("s");
        final FastTripleBunch bunch = new FastHashedTripleBunch();
        bunch.tryAdd(triple("s p o"));
        bunch.tryAdd(triple("s p o2"));

        map.put(key, bunch);
        assertEquals(1, map.size());
        assertSame(bunch, map.get(key));
    }

    @Test
    public void getReturnsNullForMissingKey() {
        final var map = new FastHashedBunchMap();
        assertNull(map.get(node("missing")));
    }

    @Test
    public void copyClonesEveryBunch() {
        final var src = new FastHashedBunchMap();
        final var bunchA = new FastHashedTripleBunch();
        bunchA.tryAdd(triple("a p1 o1"));
        bunchA.tryAdd(triple("a p2 o2"));
        final var bunchB = new FastHashedTripleBunch();
        bunchB.tryAdd(triple("b p1 o1"));
        src.put(node("a"), bunchA);
        src.put(node("b"), bunchB);

        final var copy = src.copy();
        assertEquals(2, copy.size());

        final var copiedA = copy.get(node("a"));
        final var copiedB = copy.get(node("b"));
        assertNotNull(copiedA);
        assertNotNull(copiedB);

        // Bunches are deep-copied: same content, different identity.
        assertNotSame(bunchA, copiedA);
        assertNotSame(bunchB, copiedB);
        assertEquals(bunchA.size(), copiedA.size());
        assertEquals(bunchB.size(), copiedB.size());
        assertTrue(copiedA.containsKey(triple("a p1 o1")));
        assertTrue(copiedA.containsKey(triple("a p2 o2")));
        assertTrue(copiedB.containsKey(triple("b p1 o1")));
    }

    @Test
    public void copyIsIndependentOfSource() {
        final var src = new FastHashedBunchMap();
        final var bunch = new FastHashedTripleBunch();
        bunch.tryAdd(triple("s p o"));
        src.put(node("s"), bunch);

        final var copy = src.copy();
        // mutating the source bunch must not be visible in the copy
        bunch.tryAdd(triple("s p o2"));
        assertEquals(2, src.get(node("s")).size());
        assertEquals(1, copy.get(node("s")).size());
        assertFalse(copy.get(node("s")).containsKey(triple("s p o2")));
    }

    @Test
    public void computeIfAbsentInsertsAndReusesValues() {
        final var map = new FastHashedBunchMap();
        final var produced = new FastHashedTripleBunch();
        final FastTripleBunch first = map.computeIfAbsent(node("k"), () -> produced);
        assertSame(produced, first);

        // calling again must return the same bunch without invoking the supplier
        final FastTripleBunch second = map.computeIfAbsent(node("k"),
                () -> { throw new AssertionError("supplier must not be called when key is present"); });
        assertSame(produced, second);
    }

    @Test
    public void putReplacesExistingBunch() {
        final var map = new FastHashedBunchMap();
        final var b1 = new FastHashedTripleBunch();
        b1.tryAdd(triple("s p o"));
        final var b2 = new FastHashedTripleBunch();
        b2.tryAdd(triple("s p o2"));

        map.put(node("k"), b1);
        map.put(node("k"), b2);
        assertEquals(1, map.size());
        assertSame(b2, map.get(node("k")));
    }

    @Test
    public void removeRemovesEntry() {
        final var map = new FastHashedBunchMap();
        map.put(node("k"), new FastHashedTripleBunch());
        assertTrue(map.tryRemove(node("k")));
        assertEquals(0, map.size());
        assertNull(map.get(node("k")));
        // removing again is a no-op
        assertFalse(map.tryRemove(node("k")));
    }
}
