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

import org.apache.jena.graph.Node;
import org.junit.Test;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link NodesToIndices}: {@link Node}-keyed
 * {@link org.apache.jena.mem.collection.FastHashMap} of {@link IndexList}s,
 * with a deep-cloning copy constructor.
 */
public class NodesToIndicesTest {

    @Test
    public void newMapIsEmpty() {
        final var map = new NodesToIndices();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void putAndGetReturnSameList() {
        final var map = new NodesToIndices();
        final Node n = node("s");
        final var list = new IndexList();
        list.add(7);
        list.add(11);

        map.put(n, list);
        assertEquals(1, map.size());
        assertSame(list, map.get(n));
    }

    @Test
    public void getReturnsNullForMissingKey() {
        final var map = new NodesToIndices();
        assertNull(map.get(node("missing")));
    }

    @Test
    public void copyClonesEveryIndexList() {
        final var src = new NodesToIndices();
        final var listA = new IndexList();
        listA.add(1);
        listA.add(2);
        final var listB = new IndexList();
        listB.add(3);
        src.put(node("a"), listA);
        src.put(node("b"), listB);

        final var copy = src.copy();
        assertEquals(2, copy.size());

        final var copiedA = copy.get(node("a"));
        final var copiedB = copy.get(node("b"));
        assertNotNull(copiedA);
        assertNotNull(copiedB);

        // Lists must be independent: same content, different identity
        assertNotSame(listA, copiedA);
        assertNotSame(listB, copiedB);
        assertEquals(listA.size(), copiedA.size());
        assertEquals(listB.size(), copiedB.size());
        assertEquals(1, copiedA.getIndexAt(0));
        assertEquals(2, copiedA.getIndexAt(1));
        assertEquals(3, copiedB.getIndexAt(0));
    }

    @Test
    public void copyIsIndependentOfSource() {
        final var src = new NodesToIndices();
        final var list = new IndexList();
        list.add(42);
        src.put(node("k"), list);

        final var copy = src.copy();
        // Mutate the source's list — copy must be unaffected.
        list.add(99);
        assertEquals(2, src.get(node("k")).size());
        assertEquals(1, copy.get(node("k")).size());
        assertEquals(42, copy.get(node("k")).getIndexAt(0));

        // Add a key only to the copy: src must be unaffected.
        copy.put(node("only-in-copy"), new IndexList());
        assertNull(src.get(node("only-in-copy")));
    }

    @Test
    public void computeIfAbsentInsertsAndReusesValues() {
        final var map = new NodesToIndices();
        final Node n = node("x");
        final var created = map.computeIfAbsent(n, IndexList::new);
        assertNotNull(created);
        // Second call with the same key must return the SAME list.
        final var second = map.computeIfAbsent(n, IndexList::new);
        assertSame(created, second);
    }
}
