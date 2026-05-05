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
package org.apache.jena.mem.collection;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.junit.Assert.*;

/**
 * This test shall test only the parts of the {@link FastHashSet} which are not tested by the {@link AbstractJenaSetTripleTest}.
 */
public class FastHashSetTest2 {

    private FastHashSet<String> sut;

    @Before
    public void setUp() {
        sut = new FastStringHashSet();
    }

    @Test
    public void testAddAndGetIndex() {
        assertEquals(0, sut.addAndGetIndex("a"));
        assertEquals(1, sut.addAndGetIndex("b"));
        assertEquals(2, sut.addAndGetIndex("c"));

        assertEquals(~0, sut.addAndGetIndex("a"));
        assertEquals(~1, sut.addAndGetIndex("b"));
        assertEquals(~2, sut.addAndGetIndex("c"));
    }

    @Test
    public void testAddAndGetIndexWithSameHashCode() {
        final var a = new Object() {
            @Override
            public int hashCode() {
                return 0;
            }
        };
        final var b = new Object() {
            @Override
            public int hashCode() {
                return 0;
            }
        };
        final var c = new Object() {
            @Override
            public int hashCode() {
                return 0;
            }
        };

        var objectHashSet = new FastObjectHashSet();
        assertEquals(0, objectHashSet.addAndGetIndex(a));
        assertEquals(1, objectHashSet.addAndGetIndex(b));
        assertEquals(2, objectHashSet.addAndGetIndex(c));

        assertEquals(~0, objectHashSet.addAndGetIndex(a));
        assertEquals(~1, objectHashSet.addAndGetIndex(b));
        assertEquals(~2, objectHashSet.addAndGetIndex(c));
    }

    @Test
    public void testAddAndContainsKeyWithSameHashCode() {
        final var a = new Object() {
            @Override
            public int hashCode() {
                return 0;
            }
        };
        final var b = new Object() {
            @Override
            public int hashCode() {
                return 0;
            }
        };
        final var c = new Object() {
            @Override
            public int hashCode() {
                return 0;
            }
        };
        final var d = new Object() {
            @Override
            public int hashCode() {
                return 0;
            }
        };

        var objectHashSet = new FastObjectHashSet();
        assertEquals(0, objectHashSet.addAndGetIndex(a));
        assertEquals(1, objectHashSet.addAndGetIndex(b));
        assertEquals(2, objectHashSet.addAndGetIndex(c));

        assertTrue(objectHashSet.containsKey(a));
        assertTrue(objectHashSet.containsKey(b));
        assertTrue(objectHashSet.containsKey(c));
        assertFalse(objectHashSet.containsKey(d));
    }

    @Test
    public void testAddAndGetIndexWithInitialSize() {
        sut = new FastStringHashSet(3);
        assertEquals(0, sut.addAndGetIndex("a"));
        assertEquals(1, sut.addAndGetIndex("b"));
        assertEquals(2, sut.addAndGetIndex("c"));
    }

    @Test
    public void testRemoveAndGetIndex() {
        assertEquals(0, sut.addAndGetIndex("a"));
        assertEquals(1, sut.addAndGetIndex("b"));
        assertEquals(2, sut.addAndGetIndex("c"));
        assertEquals(1, sut.removeAndGetIndex("b"));
        assertEquals(1, sut.addAndGetIndex("d"));
        assertEquals(~1, sut.addAndGetIndex("d"));
        assertEquals(-1, sut.removeAndGetIndex("b"));
    }

    @Test
    public void testGetKeyAt() {
        assertEquals(0, sut.addAndGetIndex("a"));
        assertEquals(1, sut.addAndGetIndex("b"));
        assertEquals(2, sut.addAndGetIndex("c"));
        assertEquals("a", sut.getKeyAt(0));
        assertEquals("b", sut.getKeyAt(1));
        assertEquals("c", sut.getKeyAt(2));
    }

    @Test
    public void testAnyMatchRandomOrder() {
        sut.addUnchecked("a");
        sut.addUnchecked("b");
        sut.addUnchecked("c");

        assertTrue(sut.anyMatchRandomOrder(k -> k.equals("a")));
        assertTrue(sut.anyMatchRandomOrder(k -> k.equals("b")));
        assertTrue(sut.anyMatchRandomOrder(k -> k.equals("c")));
        assertFalse(sut.anyMatchRandomOrder(k -> k.equals("d")));
    }

    @Test
    public void testCopyConstructor() {
        var original = new FastObjectHashSet();
        original.addAndGetIndex(node("s"));
        original.addAndGetIndex(node("s1"));
        original.addAndGetIndex(node("s2"));
        assertEquals(3, original.size());

        var copy = new FastObjectHashSet(original);
        assertEquals(3, copy.size());
        assertTrue(copy.containsKey(node("s")));
        assertTrue(copy.containsKey(node("s1")));
        assertTrue(copy.containsKey(node("s2")));
        assertFalse(copy.containsKey(node("s3")));
    }

    @Test
    public void testCopyConstructorAddAndDeleteHasNoSideEffects() {
        var original = new FastObjectHashSet();
        original.addAndGetIndex(node("s"));
        original.addAndGetIndex(node("s1"));
        original.addAndGetIndex(node("s2"));
        assertEquals(3, original.size());

        var copy = new FastObjectHashSet(original);
        copy.removeAndGetIndex(node("s1"));
        copy.addAndGetIndex(node("s3"));
        copy.addAndGetIndex(node("s4"));

        assertEquals(4, copy.size());
        assertTrue(copy.containsKey(node("s")));
        assertFalse(copy.containsKey(node("s1")));
        assertTrue(copy.containsKey(node("s2")));
        assertTrue(copy.containsKey(node("s3")));
        assertTrue(copy.containsKey(node("s4")));


        assertEquals(3, original.size());
        assertTrue(original.containsKey(node("s")));
        assertTrue(original.containsKey(node("s1")));
        assertTrue(original.containsKey(node("s2")));
        assertFalse(original.containsKey(node("s3")));
    }

    @Test
    public void testForEachKeyVisitsEveryKeyWithItsIndex() {
        var items = List.of("a", "b", "c", "d", "e");
        sut = new FastStringHashSet(3);
        for (String item : items) {
            sut.addAndGetIndex(item);
        }

        final java.util.Map<String, Integer> seen = new java.util.HashMap<>();
        sut.forEachKey(seen::put);

        assertEquals(items.size(), seen.size());
        for (var i = 0; i < items.size(); i++) {
            // forEachKey reports the stable insertion index per key.
            assertEquals(Integer.valueOf(i), seen.get(items.get(i)));
        }
    }

    @Test
    public void testForEachKeyOnEmptySetIsNoOp() {
        sut = new FastStringHashSet(3);
        sut.forEachKey((k, i) -> fail("consumer must not be called on empty set"));
    }

    @Test
    public void testForEachKeySkipsRemovedSlots() {
        sut = new FastStringHashSet(3);
        sut.addAndGetIndex("a");
        sut.addAndGetIndex("b");
        sut.addAndGetIndex("c");
        // Remove the middle entry; forEachKey must not visit the freed slot.
        sut.tryRemove("b");

        final var keys = new java.util.ArrayList<String>();
        sut.forEachKey((k, i) -> keys.add(k));
        assertEquals(2, keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("c"));
        assertFalse(keys.contains("b"));
    }

    private static class FastObjectHashSet extends FastHashSet<Object> {

        public FastObjectHashSet() {
            super();
        }

        public FastObjectHashSet(FastHashSet<Object> setToCopy) {
            super(setToCopy);
        }

        @Override
        protected Object[] newKeysArray(int size) {
            return new Object[size];
        }

    }

    private static class FastStringHashSet extends FastHashSet<String> {

        public FastStringHashSet(int initialSize) {
            super(initialSize);
        }

        public FastStringHashSet() {
            super();
        }

        @Override
        protected String[] newKeysArray(int size) {
            return new String[size];
        }

    }
}