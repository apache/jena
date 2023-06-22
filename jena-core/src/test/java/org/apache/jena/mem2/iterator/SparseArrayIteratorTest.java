/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.mem2.iterator;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class SparseArrayIteratorTest {

    private SparseArrayIterator<String> iterator;

    @Test
    public void testHasNextAndNextWithNonNullEntries() {
        String[] entries = new String[]{"first", "second", "third"};
        iterator = new SparseArrayIterator<>(entries, () -> {
        });

        assertTrue(iterator.hasNext());
        assertEquals("third", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("second", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testConstrucorWithToIndexConstraint3() {
        String[] entries = new String[]{"first", "second", "third"};
        iterator = new SparseArrayIterator<>(entries, 3, () -> {
        });

        assertTrue(iterator.hasNext());
        assertEquals("third", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("second", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testConstrucorWithToIndexConstraint2() {
        String[] entries = new String[]{"first", "second", "third"};
        iterator = new SparseArrayIterator<>(entries, 2, () -> {
        });

        assertTrue(iterator.hasNext());
        assertEquals("second", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testConstrucorWithToIndexConstraint1() {
        String[] entries = new String[]{"first", "second", "third"};
        iterator = new SparseArrayIterator<>(entries, 1, () -> {
        });

        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testConstrucorWithToIndexConstraint0() {
        String[] entries = new String[]{"first", "second", "third"};
        iterator = new SparseArrayIterator<>(entries, 0, () -> {
        });

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    public void testHasNextAndNextWithNullEntries() {
        String[] entries = new String[]{"first", null, "third", null, "fifth"};
        iterator = new SparseArrayIterator<>(entries, () -> {
        });

        assertTrue(iterator.hasNext());
        assertEquals("fifth", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("third", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testHasNextAndNextWithNoElements() {
        String[] entries = new String[]{};
        iterator = new SparseArrayIterator<>(entries, () -> {
        });

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    public void testForEachRemaining() {
        String[] entries = new String[]{"first", null, "third", null, "fifth"};
        iterator = new SparseArrayIterator<>(entries, () -> {
        });
        int[] count = new int[]{0};
        iterator.forEachRemaining(entry -> {
            assertNotNull(entry);
            count[0]++;
        });
        assertEquals(3, count[0]);
    }
}

