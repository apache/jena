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
package org.apache.jena.mem.collection;

import org.apache.jena.graph.Triple;
import org.junit.Test;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.*;


public class HashCommonSetTest extends AbstractJenaSetTripleTest {

    @Override
    protected JenaSet<Triple> createTripleSet() {
        return new HashCommonTripleSet();
    }

    @Test
    public void testCopyConstructor() {
        var original = new HashCommonTripleSet();
        original.tryAdd(triple("s p o"));
        original.tryAdd(triple("s1 p1 o1"));
        original.tryAdd(triple("s2 p2 o2"));
        assertEquals(3, original.size());

        var copy = new HashCommonTripleSet(original);
        assertEquals(3, copy.size());
        assertTrue(copy.containsKey(triple("s p o")));
        assertTrue(copy.containsKey(triple("s1 p1 o1")));
        assertTrue(copy.containsKey(triple("s2 p2 o2")));
        assertFalse(copy.containsKey(triple("s3 p3 o3")));
    }

    @Test
    public void testCopyConstructorAddAndDeleteHasNoSideEffects() {
        var original = new HashCommonTripleSet();
        original.tryAdd(triple("s p o"));
        original.tryAdd(triple("s1 p1 o1"));
        original.tryAdd(triple("s2 p2 o2"));
        assertEquals(3, original.size());

        var copy = new HashCommonTripleSet(original);
        copy.tryRemove(triple("s1 p1 o1"));
        copy.tryAdd(triple("s3 p3 o3"));
        copy.tryAdd(triple("s4 p4 o4"));

        assertEquals(4, copy.size());
        assertTrue(copy.containsKey(triple("s p o")));
        assertFalse(copy.containsKey(triple("s1 p1 o1")));
        assertTrue(copy.containsKey(triple("s2 p2 o2")));
        assertTrue(copy.containsKey(triple("s3 p3 o3")));
        assertTrue(copy.containsKey(triple("s4 p4 o4")));


        assertEquals(3, original.size());
        assertTrue(original.containsKey(triple("s p o")));
        assertTrue(original.containsKey(triple("s1 p1 o1")));
        assertTrue(original.containsKey(triple("s2 p2 o2")));
        assertFalse(original.containsKey(triple("s3 p3 o3")));
    }

    private static class HashCommonTripleSet extends HashCommonSet<Triple> {
        public HashCommonTripleSet() {
            super(10);
        }

        public HashCommonTripleSet(HashCommonSet<Triple> setToCopy) {
            super(setToCopy);
        }

        @Override
        protected Triple[] newKeysArray(int size) {
            return new Triple[size];
        }

        @Override
        public void clear() {
            super.clear(10);
        }
    }
}