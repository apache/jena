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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test shall test only the parts of the {@link FastHashSet} which are not tested by the {@link AbstractJenaSetTripleTest}.
 */
public class FastHashSetTest2 {

    private FastHashSet<String> sut;

    @Before
    public void setUp() throws Exception {
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
        assertEquals(0, sut.addAndGetIndex("a", 0));
        assertEquals(1, sut.addAndGetIndex("b", 0));
        assertEquals(2, sut.addAndGetIndex("c", 0));

        assertEquals(~0, sut.addAndGetIndex("a", 0));
        assertEquals(~1, sut.addAndGetIndex("b", 0));
        assertEquals(~2, sut.addAndGetIndex("c", 0));
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


    private static class FastObjectHashSet extends FastHashSet<Object> {

        public FastObjectHashSet() {
            super();
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