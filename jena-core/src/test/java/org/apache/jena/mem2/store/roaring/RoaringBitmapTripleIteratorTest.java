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
package org.apache.jena.mem2.store.roaring;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.collection.FastHashSet;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.NoSuchElementException;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class RoaringBitmapTripleIteratorTest {


    private static FastHashSet<Triple> createTripleSet() {
        return new FastHashSet<Triple>() {

            @Override
            protected Triple[] newKeysArray(int size) {
                return new Triple[size];
            }
        };
    }

    @Test
    public void testEmpty() {
        final var bitmap = new RoaringBitmap();
        final var set = createTripleSet();
        final var sut = new RoaringBitmapTripleIterator(bitmap, set);
        assertFalse(sut.hasNext());
        assertThrows(NoSuchElementException.class, () -> sut.next());
    }

    @Test
    public void testSingle() {
        final var bitmap = new RoaringBitmap();
        final var set = createTripleSet();
        bitmap.add(set.addAndGetIndex(triple("s P o")));
        final var sut = new RoaringBitmapTripleIterator(bitmap, set);
        assertTrue(sut.hasNext());
        assertNotNull(sut.next());
        assertFalse(sut.hasNext());
    }

    @Test
    public void testMultiple() {
        final var bitmap = new RoaringBitmap();
        final var set = createTripleSet();
        bitmap.add(set.addAndGetIndex(triple("s P o")));
        bitmap.add(set.addAndGetIndex(triple("t Q s")));
        bitmap.add(set.addAndGetIndex(triple("u R t")));
        final var sut = new RoaringBitmapTripleIterator(bitmap, set);
        assertThat(sut.toList(), IsIterableContainingInAnyOrder.containsInAnyOrder(
                triple("s P o"),
                triple("t Q s"),
                triple("u R t")
        ));
    }

    @Test
    public void testNextAndThenForEachRemaining() {
        final var triples = new HashSet<Triple>();
        for (int i = 0; i < 100; i++) {
            triples.add(triple("s" + i + " P" + i + " o" + i));
        }

        final var bitmap = new RoaringBitmap();
        final var set = createTripleSet();

        for (Triple t : triples) {
            bitmap.add(set.addAndGetIndex(t));
        }
        final var sut = new RoaringBitmapTripleIterator(bitmap, set);
        assertTrue(sut.hasNext());
        assertTrue(triples.remove(sut.next()));
        sut.forEachRemaining(t -> {
            assertTrue(triples.remove(t));
        });
        assertFalse(sut.hasNext());
        assertTrue(triples.isEmpty());
    }


    @Test
    public void testNextConcurrentModification() {
        final var bitmap = new RoaringBitmap();
        final var set = createTripleSet();
        bitmap.add(set.addAndGetIndex(triple("s P o")));
        final var sut = new RoaringBitmapTripleIterator(bitmap, set);
        set.removeUnchecked(triple("s P o"));
        assertTrue(sut.hasNext());
        assertThrows(ConcurrentModificationException.class, () -> sut.next());
    }

    @Test
    public void testForEachRemainingConcurrentModification() {
        final var bitmap = new RoaringBitmap();
        final var set = createTripleSet();
        bitmap.add(set.addAndGetIndex(triple("s P o")));
        set.addUnchecked(triple("s P o1"));
        final var sut = new RoaringBitmapTripleIterator(bitmap, set);
        set.removeUnchecked(triple("s P o1"));
        assertThrows(ConcurrentModificationException.class, () -> sut.forEachRemaining(t -> {
        }));
    }

    @Test
    public void testMoreThanInBuffer() {
        final var bitmap = new RoaringBitmap();
        final var set = createTripleSet();
        for (int i = 0; i < RoaringBitmapTripleIterator.BUFFER_SIZE + 1; i++) {
            bitmap.add(set.addAndGetIndex(triple("s P o" + i)));
        }
        final var sut = new RoaringBitmapTripleIterator(bitmap, set);
        var counter = 0;
        while (sut.hasNext()) {
            assertNotNull(sut.next());
            counter++;
        }
        assertEquals(RoaringBitmapTripleIterator.BUFFER_SIZE + 1, counter);
    }
}