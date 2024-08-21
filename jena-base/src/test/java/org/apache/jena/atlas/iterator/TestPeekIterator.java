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

package org.apache.jena.atlas.iterator;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestPeekIterator {


    @Test
    public void iter_01() {
        List<String> data = List.of("x", "y", "z");
        Iter<String> iter = Iter.iter(data);
        iter = iter.append(data.iterator());
        test(iter, "x", "y", "z", "x", "y", "z");
    }

    private static void test(Iter<? > iter, Object...items) {
        for ( Object x : items ) {
            assertTrue(iter.hasNext());
            assertEquals(x, iter.next());
        }
        assertFalse(iter.hasNext());
    }

    private static PeekIterator<String> create(String...a) {
        // Use ArrayList to allow nulls.
        List<String> x = new ArrayList<>();
        for ( String str : a )
            x.add(str);
        return new PeekIterator<>(x.iterator());
    }

    @Test
    public void peek_1() {
        PeekIterator<String> peek = create("a", "b", "c");
        assertEquals("a", peek.peek());
        test(Iter.iter(peek), "a", "b", "c");
    }

    @Test
    public void peek_2() {
        PeekIterator<String> peek = create();
        assertFalse(peek.hasNext());
    }

    @Test
    public void peek_3() {
        PeekIterator<String> peek = create("a");
        assertEquals("a", peek.peek());
    }

    @Test
    public void peek_4() {
        PeekIterator<String> peek = create("a");
        assertEquals("a", peek.peek());
        assertEquals("a", peek.peek());
        assertEquals("a", peek.next());
        assertFalse(peek.hasNext());
    }

    @Test
    public void peek_5() {
        PeekIterator<String> peek = create("a", "b");
        assertEquals("a", peek.peek());
        assertEquals("a", peek.peek());
        assertEquals("a", peek.next());
        assertTrue(peek.hasNext());
        assertEquals("b", peek.peek());
        assertEquals("b", peek.peek());
        assertEquals("b", peek.next());
        assertFalse(peek.hasNext());
    }

    @Test
    public void peek_6() {
        PeekIterator<String> peek = create("a", null, "b");
        assertEquals("a", peek.peek());
        assertEquals("a", peek.next());

        assertNull(peek.peek());
        assertTrue(peek.slotIsValid());
        assertTrue(peek.hasNext());
        assertNull(peek.next());

        assertEquals("b", peek.peek());
        assertEquals("b", peek.next());
        assertFalse(peek.hasNext());

        assertNull(peek.peek());
        assertFalse(peek.slotIsValid());
    }

    @Test
    public void peek_7() {
        PeekIterator<String> peek = create("a", "b", null);
        assertEquals("a", peek.peek());
        assertEquals("a", peek.next());
        assertEquals("b", peek.peek());
        assertEquals("b", peek.next());

        assertNull(peek.peek());
        assertTrue(peek.slotIsValid());
        assertTrue(peek.hasNext());

        assertNull(peek.next());
        assertFalse(peek.hasNext());
    }

    @Test
    public void peek_8() {
        PeekIterator<String> peek = create(null, "a", "b");
        assertNull(peek.peek());
        assertTrue(peek.slotIsValid());
        assertTrue(peek.hasNext());
        assertEquals(null, peek.next());

        assertEquals("a", peek.peek());
        assertEquals("a", peek.next());
        assertEquals("b", peek.peek());
        assertEquals("b", peek.next());

        assertNull(peek.peek());
        assertFalse(peek.slotIsValid());
        assertFalse(peek.hasNext());
    }
}
