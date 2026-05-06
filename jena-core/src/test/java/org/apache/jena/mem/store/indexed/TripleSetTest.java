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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link TripleSet}: hash set of triples extended with a
 * grow-hook that fires when the underlying keys array is enlarged. Used as
 * the canonical triple collection inside {@link IndexedSetTripleStore}.
 */
public class TripleSetTest {

    @Test
    public void newSetIsEmpty() {
        final var set = new TripleSet();
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    @Test
    public void addAndGetIndexAssignsStableIndices() {
        final var set = new TripleSet();
        final var t1 = triple("a b c");
        final var t2 = triple("d e f");
        final int i1 = set.addAndGetIndex(t1);
        final int i2 = set.addAndGetIndex(t2);
        assertNotEquals(i1, i2);
        assertEquals(t1, set.getKeyAt(i1));
        assertEquals(t2, set.getKeyAt(i2));

        assertEquals(i1, set.indexOf(t1));
        assertEquals(i2, set.indexOf(t2));
        assertEquals(-1, set.indexOf(triple("NOT_A NOT_B NOT_C")));

        // Re-adding the same triple returns the bitwise-complement of the
        // existing index.
        final int reAdd = set.addAndGetIndex(t1);
        assertTrue("re-add must return ~existingIndex", reAdd < 0);
        assertEquals(i1, ~reAdd);
        assertEquals(2, set.size());
    }

    @Test
    public void containsKeyReturnsTrueForAddedTriple() {
        final var set = new TripleSet();
        final var t = triple("x y z");
        set.tryAdd(t);
        assertTrue(set.containsKey(t));
        assertFalse(set.containsKey(triple("x y NOT_Z")));
    }

    @Test
    public void onKeysGrowHookFiresWhenInternalArrayResizes() {
        final var set = new TripleSet();
        final AtomicInteger callCount = new AtomicInteger();
        final AtomicInteger lastReportedSize = new AtomicInteger();
        set.setOnKeysGrowHook(newSize -> {
            callCount.incrementAndGet();
            lastReportedSize.set(newSize);
        });

        // Add enough elements to force at least one growth of the keys array.
        for (int i = 0; i < 200; i++) {
            set.addAndGetIndex(triple("s" + i + " p o"));
        }

        assertTrue("hook must fire at least once when growing", callCount.get() > 0);
        assertTrue("reported size must be a positive integer (the new array length)",
                lastReportedSize.get() > 0);
    }

    @Test
    public void hookCanBeDisabledByPassingNull() {
        final var set = new TripleSet();
        final AtomicInteger callCount = new AtomicInteger();
        set.setOnKeysGrowHook(newSize -> callCount.incrementAndGet());
        // disable
        set.setOnKeysGrowHook(null);
        for (int i = 0; i < 200; i++) {
            set.addAndGetIndex(triple("s" + i + " p o"));
        }
        assertEquals(0, callCount.get());
    }

    @Test
    public void copyContainsSameTriplesAndIsIndependent() {
        final var src = new TripleSet();
        for (int i = 0; i < 5; i++) {
            src.tryAdd(triple("s" + i + " p o"));
        }

        final var copy = src.copy();
        assertNotSame(src, copy);
        assertEquals(src.size(), copy.size());
        for (int i = 0; i < 5; i++) {
            assertTrue(copy.containsKey(triple("s" + i + " p o")));
        }

        // mutating the copy must not affect the source
        copy.tryAdd(triple("extra p o"));
        assertEquals(6, copy.size());
        assertEquals(5, src.size());
        assertFalse(src.containsKey(triple("extra p o")));
    }

    @Test
    public void copyDoesNotPropagateGrowHook() {
        final var src = new TripleSet();
        final AtomicInteger srcHookCalls = new AtomicInteger();
        src.setOnKeysGrowHook(n -> srcHookCalls.incrementAndGet());

        // Populate enough to force growth in src and reset counter
        for (int i = 0; i < 100; i++) {
            src.tryAdd(triple("s" + i + " p o"));
        }
        srcHookCalls.set(0);

        // The copy must NOT inherit the hook; pumping more triples into it
        // must not invoke the source's hook.
        final var copy = src.copy();
        for (int i = 100; i < 300; i++) {
            copy.tryAdd(triple("s" + i + " p o"));
        }
        assertEquals(0, srcHookCalls.get());
    }

    @Test
    public void removeUpdatesSize() {
        final var set = new TripleSet();
        final var t1 = triple("a b c");
        final var t2 = triple("d e f");
        set.tryAdd(t1);
        set.tryAdd(t2);

        assertTrue(set.tryRemove(t1));
        assertEquals(1, set.size());
        assertFalse(set.containsKey(t1));
        assertTrue(set.containsKey(t2));

        // removing again is a no-op
        assertFalse(set.tryRemove(t1));
    }

    @Test
    public void streamProducesAllTriples() {
        final var set = new TripleSet();
        for (int i = 0; i < 10; i++) {
            set.tryAdd(triple("s" + i + " p o"));
        }
        final long count = set.keyStream().count();
        assertEquals(10, count);
    }

    @Test
    public void clearResetsToEmpty() {
        final var set = new TripleSet();
        set.tryAdd(triple("a b c"));
        set.tryAdd(triple("d e f"));
        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }
}
