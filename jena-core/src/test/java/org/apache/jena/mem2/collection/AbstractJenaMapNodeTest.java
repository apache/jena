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

package org.apache.jena.mem2.collection;

import org.apache.jena.graph.Node;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public abstract class AbstractJenaMapNodeTest {

    protected JenaMap<Node, Object> sut;

    protected abstract JenaMap<Node, Object> createNodeMap();

    @Before
    public void setUp() throws Exception {
        sut = createNodeMap();
    }

    @Test
    public void isEmpty() {
        assertTrue(sut.isEmpty());
        sut.tryPut(node("s"), null);
        assertFalse(sut.isEmpty());
    }

    @Test
    public void testTryPut() {
        assertTrue(sut.tryPut(node("s"), 1));
        assertEquals(1, sut.size());
        assertFalse(sut.tryPut(node("s"), 2));
        assertEquals(1, sut.size());
    }

    @Test
    public void testTryPutOverridesValue() {
        sut.tryPut(node("s"), 1);
        assertEquals(1, sut.get(node("s")));
        sut.tryPut(node("s"), 2);
        assertEquals(2, sut.get(node("s")));
    }

    @Test
    public void testPut() {
        sut.put(node("s"), 1);
        assertEquals(1, sut.size());
        sut.put(node("s"), 2);
        assertEquals(1, sut.size());
    }

    @Test
    public void testPutOverridesValue() {
        sut.put(node("s"), 1);
        assertEquals(1, sut.get(node("s")));
        sut.put(node("s"), 2);
        assertEquals(2, sut.get(node("s")));
    }

    @Test
    public void testTryRemove() {
        sut.put(node("s"), null);
        assertTrue(sut.tryRemove(node("s")));
        assertEquals(0, sut.size());
        assertFalse(sut.tryRemove(node("s")));
    }

    @Test
    public void testRemoveUnchecked() {
        sut.put(node("s"), null);
        sut.removeUnchecked(node("s"));
        assertEquals(0, sut.size());
    }

    @Test
    public void testGet() {
        sut.put(node("s"), 1);
        assertEquals(1, sut.get(node("s")));
        assertNull(sut.get(node("s2")));
    }

    @Test
    public void testGet2() {
        sut.put(node("s"), 1);
        sut.put(node("s2"), 2);
        assertEquals(1, sut.get(node("s")));
        assertEquals(2, sut.get(node("s2")));
    }

    @Test
    public void testGetOrDefault() {
        sut.put(node("s"), 1);
        assertEquals(1, sut.getOrDefault(node("s"), 2));
        assertEquals(2, sut.getOrDefault(node("s2"), 2));
    }

    @Test
    public void testComputeIfAbsent() {
        sut.computeIfAbsent(node("s"), () -> 1);
        assertEquals(1, sut.get(node("s")));
        sut.computeIfAbsent(node("s"), () -> 2);
        assertEquals(1, sut.get(node("s")));
    }

    @Test
    public void testCompute() {
        sut.compute(node("s"), (v) -> {
            assertNull(v);
            return 1;
        });
        assertEquals(1, sut.get(node("s")));
        sut.compute(node("s"), (v) -> {
            assertEquals(1, v);
            return 2;
        });
        assertEquals(2, sut.get(node("s")));
        sut.compute(node("s"), (v) -> {
            assertEquals(2, v);
            return null;
        });
        assertNull(sut.get(node("s")));
        assertTrue(sut.isEmpty());
    }

    @Test
    public void testClear() {
        sut.put(node("s"), null);
        sut.clear();
        assertEquals(0, sut.size());
    }

    @Test
    public void testContainKey() {
        assertFalse(sut.containsKey(node("s")));
        sut.put(node("s"), null);
        assertTrue(sut.containsKey(node("s")));
        assertFalse(sut.containsKey(node("s2")));
    }

    @Test
    public void testKeyIteratorEmpty() {
        var iter = sut.keyIterator();
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, () -> iter.next());
    }

    @Test
    public void testValueIteratorEmpty2() {
        var iter = sut.valueIterator();
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, () -> iter.next());
    }

    @Test
    public void testKeyIteratorNextThrowsConcurrentModificationException() {
        sut.put(node("s"), null);
        var iter = sut.keyIterator();
        sut.put(node("s2"), null);
        assertThrows(ConcurrentModificationException.class, iter::next);
    }

    @Test
    public void testKeySpliteratorAdvanceThrowsConcurrentModificationException() {
        sut.put(node("s"), null);
        var spliterator = sut.keySpliterator();
        sut.put(node("s2"), null);
        assertThrows(ConcurrentModificationException.class, () -> spliterator.tryAdvance(t -> {
        }));
    }

    @Test
    public void testValueIteratorNextThrowsConcurrentModificationException() {
        sut.put(node("s"), null);
        var iter = sut.keyIterator();
        sut.put(node("s2"), null);
        assertThrows(ConcurrentModificationException.class, iter::next);
    }

    @Test
    public void testValueSpliteratorAdvanceThrowsConcurrentModificationException() {
        sut.put(node("s"), null);
        var spliterator = sut.valueSpliterator();
        sut.put(node("s2"), null);
        assertThrows(ConcurrentModificationException.class, () -> spliterator.tryAdvance(t -> {
        }));
    }

    @Test
    public void testKeyIterator1() {
        final var n0 = node("s");

        sut.put(n0, null);

        final var iter = sut.keyIterator();
        assertThat(iter.toList(), IsIterableContainingInAnyOrder.containsInAnyOrder(n0));
    }

    @Test
    public void testValueIterator1() {
        final var n0 = node("s");

        sut.put(n0, 1);

        final var iter = sut.valueIterator();
        assertThat(iter.toList(), IsIterableContainingInAnyOrder.containsInAnyOrder(1));

    }

    @Test
    public void testKeyIterator2() {
        final var n0 = node("s");
        final var n1 = node("s2");

        sut.put(n0, null);
        sut.put(n1, null);

        final var iter = sut.keyIterator();
        assertThat(iter.toList(), IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1));
    }

    @Test
    public void testValueIterator2() {
        final var n0 = node("s");
        final var n1 = node("s2");

        sut.put(n0, 1);
        sut.put(n1, 2);

        final var iter = sut.valueIterator();
        assertThat(iter.toList(), IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2));
    }

    @Test
    public void testKeyIterator3() {
        final var n0 = node("s");
        final var n1 = node("s2");
        final var n2 = node("s3");

        sut.put(n0, null);
        sut.put(n1, null);
        sut.put(n2, null);

        final var iter = sut.keyIterator();
        assertThat(iter.toList(), IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1, n2));
    }

    @Test
    public void testValueIterator3() {
        final var n0 = node("s");
        final var n1 = node("s2");
        final var n2 = node("s3");

        sut.put(n0, 1);
        sut.put(n1, 2);
        sut.put(n2, 3);

        final var iter = sut.valueIterator();
        assertThat(iter.toList(), IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2, 3));
    }

    @Test
    public void testKeySpliteratorEmpty() {
        var spliterator = sut.keySpliterator();
        assertFalse(spliterator.tryAdvance(t -> fail()));
    }

    @Test
    public void testValueSpliteratorEmpty() {
        var spliterator = sut.valueSpliterator();
        assertFalse(spliterator.tryAdvance(t -> fail()));
    }

    @Test
    public void testKeySpliterator1() {
        final var n0 = node("s");

        sut.put(n0, null);

        final var spliterator = sut.keySpliterator();
        final var list = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
        assertThat(list, IsIterableContainingInAnyOrder.containsInAnyOrder(n0));
    }

    @Test
    public void testValueSpliterator1() {
        sut.put(node("s"), 1);

        final var spliterator = sut.valueSpliterator();
        final var list = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
        assertThat(list, IsIterableContainingInAnyOrder.containsInAnyOrder(1));
    }

    @Test
    public void testKeySpliterator2() {
        final var n0 = node("s");
        final var n1 = node("s2");

        sut.put(n0, null);
        sut.put(n1, null);

        final var spliterator = sut.keySpliterator();
        final var list = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
        assertThat(list, IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1));
    }

    @Test
    public void testValueSpliterator2() {
        sut.put(node("s"), 1);
        sut.put(node("s2"), 2);

        final var spliterator = sut.valueSpliterator();
        final var list = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
        assertThat(list, IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2));
    }

    @Test
    public void testKeySpliterator3() {
        final var n0 = node("s");
        final var n1 = node("s2");
        final var n2 = node("s3");

        sut.put(n0, null);
        sut.put(n1, null);
        sut.put(n2, null);

        final var spliterator = sut.keySpliterator();
        final var list = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
        assertThat(list, IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1, n2));
    }

    @Test
    public void testValueSpliterator3() {
        sut.put(node("s"), 1);
        sut.put(node("s2"), 2);
        sut.put(node("s3"), 3);

        final var spliterator = sut.valueSpliterator();
        final var list = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
        assertThat(list, IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2, 3));
    }

    @Test
    public void testKeyStreamEmpty() {
        var stream = sut.keyStream();
        assertThat(stream.collect(Collectors.toList()), IsEmptyCollection.empty());
    }

    @Test
    public void testValueStreamEmpty() {
        var stream = sut.valueStream();
        assertThat(stream.collect(Collectors.toList()), IsEmptyCollection.empty());
    }

    @Test
    public void testKeyStream1() {
        final var n0 = node("s");

        sut.put(n0, null);

        final var stream = sut.keyStream();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(n0));
    }

    @Test
    public void testValueStream1() {
        sut.put(node("s"), 1);

        final var stream = sut.valueStream();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(1));
    }

    @Test
    public void testKeyStream2() {
        final var n0 = node("s");
        final var n1 = node("s2");

        sut.put(n0, null);
        sut.put(n1, null);

        final var stream = sut.keyStream();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1));
    }

    @Test
    public void testValueStream2() {
        sut.put(node("s"), 1);
        sut.put(node("s2"), 2);

        final var stream = sut.valueStream();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2));
    }

    @Test
    public void testKeyStream3() {
        final var n0 = node("s");
        final var n1 = node("s2");
        final var n2 = node("s3");

        sut.put(n0, null);
        sut.put(n1, null);
        sut.put(n2, null);

        final var stream = sut.keyStream();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1, n2));
    }

    @Test
    public void testValueStream3() {
        sut.put(node("s"), 1);
        sut.put(node("s2"), 2);
        sut.put(node("s3"), 3);

        final var stream = sut.valueStream();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2, 3));
    }

    @Test
    public void testKeyStreamParallelEmpty() {
        var stream = sut.keyStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsEmptyCollection.empty());
    }

    @Test
    public void testValueStreamParallelEmpty() {
        var stream = sut.valueStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsEmptyCollection.empty());
    }

    @Test
    public void testKeyStreamParallel1() {
        final var n0 = node("s");

        sut.put(n0, null);

        final var stream = sut.keyStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(n0));
    }

    @Test
    public void testValueStreamParallel1() {
        sut.put(node("s"), 1);

        final var stream = sut.valueStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(1));
    }

    @Test
    public void testKeyStreamParallel2() {
        final var n0 = node("s");
        final var n1 = node("s2");

        sut.put(n0, null);
        sut.put(n1, null);

        final var stream = sut.keyStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1));
    }

    @Test
    public void testValueStreamParallel2() {
        sut.put(node("s"), 1);
        sut.put(node("s2"), 2);

        final var stream = sut.valueStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2));
    }

    @Test
    public void testKeyStreamParallel3() {
        final var n0 = node("s");
        final var n1 = node("s2");
        final var n2 = node("s3");

        sut.put(n0, null);
        sut.put(n1, null);
        sut.put(n2, null);

        final var stream = sut.keyStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1, n2));
    }

    @Test
    public void testValueStreamParallel3() {
        sut.put(node("s"), 1);
        sut.put(node("s2"), 2);
        sut.put(node("s3"), 3);

        final var stream = sut.valueStreamParallel();
        assertThat(stream.collect(Collectors.toList()), IsIterableContainingInAnyOrder.containsInAnyOrder(1, 2, 3));
    }

    @Test
    public void testSize() {
        assertEquals(0, sut.size());
        sut.put(node("s"), null);
        assertEquals(1, sut.size());
        sut.put(node("s2"), null);
        assertEquals(2, sut.size());
        sut.put(node("s3"), null);
        assertEquals(3, sut.size());
    }

    @Test
    public void testAnyMatch() {
        assertFalse(sut.anyMatch(t -> true));
        assertFalse(sut.anyMatch(t -> false));

        sut.put(node("s"), null);
        assertTrue(sut.anyMatch(t -> true));
        assertFalse(sut.anyMatch(t -> false));
    }

    @Test
    public void testAnyMatchIsCalledForEveryElement() {
        final var n0 = node("s");
        final var n1 = node("s2");
        final var n2 = node("s3");

        sut.put(n0, null);
        sut.put(n1, null);
        sut.put(n2, null);

        final var list = new ArrayList<Node>();
        sut.anyMatch(n -> {
            list.add(n);
            return false;
        });
        assertThat(list, IsIterableContainingInAnyOrder.containsInAnyOrder(n0, n1, n2));
    }

    @Test
    public void put1000Nodes() {
        for (int i = 0; i < 1000; i++) {
            sut.put(node("s" + i), null);
        }
        assertEquals(1000, sut.size());
    }

    @Test
    public void tryPut1000Nodes() {
        for (int i = 0; i < 1000; i++) {
            sut.tryPut(node("s" + i), null);
        }
        assertEquals(1000, sut.size());
    }

    @Test
    public void computeIfAbsend1000Nodes() {
        for (int i = 0; i < 1000; i++) {
            sut.computeIfAbsent(node("s" + i), () -> new Object());
        }
        assertEquals(1000, sut.size());
    }

    @Test
    public void putAndRemoveUnchecked1000Triples() {
        final var nodes = new ArrayList<Node>();
        for (int i = 0; i < 1000; i++) {
            final var n = node("s" + i);
            sut.put(n, null);
            nodes.add(n);
        }
        assertEquals(1000, sut.size());
        Collections.shuffle(nodes);
        for (final var t : nodes) {
            sut.removeUnchecked(t);
        }
        assertTrue(sut.isEmpty());
    }

    @Test
    public void tryPutAndTryRemove1000Triples() {
        final var nodes = new ArrayList<Node>();
        for (int i = 0; i < 1000; i++) {
            final var n = node("s" + i);
            sut.tryPut(n, null);
            nodes.add(n);
        }
        assertEquals(1000, sut.size());
        Collections.shuffle(nodes);
        for (final var t : nodes) {
            assertTrue(sut.tryRemove(t));
        }
        assertTrue(sut.isEmpty());
    }


    private static class HashCommonNodeMap extends HashCommonMap<Node, Object> {
        public HashCommonNodeMap() {
            super(10);
        }

        @Override
        protected Node[] newKeysArray(int size) {
            return new Node[size];
        }

        @Override
        public void clear() {
            super.clear(10);
        }

        @Override
        protected Object[] newValuesArray(int size) {
            return new Object[size];
        }
    }

    private static class FastNodeHashMap extends FastHashMap<Node, Object> {
        @Override
        protected Node[] newKeysArray(int size) {
            return new Node[size];
        }

        @Override
        protected Object[] newValuesArray(int size) {
            return new Object[size];
        }
    }
}