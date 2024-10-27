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

package org.apache.jena.atlas.lib.cache;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.*;

import org.apache.jena.atlas.lib.Cache;
import org.junit.Test;

/**
 * Tests of CacheSimple
 */
public class TestCacheSimple {

    /**
     * Simple test to ensure that {@link CacheSimple} evidences
     * the fixed-size behaviour we desire.
     */
    @Test
    public void testFixedSize() {
        final int maxSize = 8;
        final int submittedEntries = 10;
        final Cache<Integer, Object> testCache = new CacheSimple<>(maxSize);
        rangeClosed(1, submittedEntries)
            .boxed()
            .collect(toMap(k -> k, v -> 1))
            .forEach(testCache::put);
        assertEquals("Test cache failed to maintain fixed size!", maxSize, testCache.size());
    }

    @Test
    public void testReplace() {
        final Integer key = 1;
        final String value1 = "A";
        final String value2 = "B";

        final Cache<Integer, Object> testCache = new CacheSimple<>(5);
        testCache.put(key, value1);
        testCache.put(key, value2);
        assertEquals("Wrong size", 1, testCache.size());
        assertEquals("Wrong slot contents", value2, testCache.getIfPresent(key));
    }

    @Test
    public void testSameHash() {
        CompoundKey key1 = new CompoundKey(1, 1);
        CompoundKey key2 = new CompoundKey(1, 2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1, key2);
        Cache<CompoundKey, Integer> cache = new CacheSimple<>(10);
        cache.put(key1, 1);
        assertTrue("Same key, expected to be in cache", cache.containsKey(key1));
        assertFalse("Keys with same hash code should not be considered equal", cache.containsKey(key2));
    }

    @Test
    public void testKeyEquality() {
        CompoundKey key1 = new CompoundKey(1, 1);
        CompoundKey key2 = new CompoundKey(1, 1);
        assertNotSame(key1, key2);
        assertEquals(key1, key2);
        Cache<CompoundKey, Integer> cache = new CacheSimple<>(10);
        cache.put(key1, 1);
        assertTrue("Equal key, expected to be found", cache.containsKey(key2));
    }

    @Test
    public void testPutSameHashOverridesValue() {
        CompoundKey key1 = new CompoundKey(1, 1);
        CompoundKey key2 = new CompoundKey(1, 2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1, key2);

        var value1 = "value1";
        var value2 = "value2";

        Cache<CompoundKey, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());
        cache.put(key1, value1);
        assertEquals(1, cache.size());
        assertEquals(value1, cache.getIfPresent(key1));
        assertNull(cache.getIfPresent(key2));

        //this should override the slot
        cache.put(key2, value2);
        assertEquals(1, cache.size());
        assertNull(cache.getIfPresent(key1));
        assertEquals(value2, cache.getIfPresent(key2));
    }

    @Test
    public void testPutSameHashOverridesSameValue() {
        CompoundKey key1 = new CompoundKey(1, 1);
        CompoundKey key2 = new CompoundKey(1, 2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1, key2);

        var value1 = "value";
        var value2 = "value";

        Cache<CompoundKey, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());
        cache.put(key1, value1);
        assertEquals(1, cache.size());
        assertEquals(value1, cache.getIfPresent(key1));
        assertNull(cache.getIfPresent(key2));

        //this should override the slot
        cache.put(key2, value2);
        assertEquals(1, cache.size());
        assertNull(cache.getIfPresent(key1));
        assertEquals(value2, cache.getIfPresent(key2));
    }

    @Test
    public void testGetSameHashOverridesValue() {
        CompoundKey key1 = new CompoundKey(1, 1);
        CompoundKey key2 = new CompoundKey(1, 2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1, key2);

        var value1 = "value1";
        var value2 = "value2";

        Cache<CompoundKey, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());
        cache.get(key1, k -> value1);
        assertEquals(1, cache.size());
        assertEquals(value1, cache.getIfPresent(key1));
        assertNull(cache.getIfPresent(key2));

        //this should override the slot
        cache.get(key2, k -> value2);
        assertEquals(1, cache.size());
        assertNull(cache.getIfPresent(key1));
        assertEquals(value2, cache.getIfPresent(key2));
    }

    @Test
    public void removeKeyByPutingNullValue() {
        Cache<String, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());

        {
            final var key = "key0";
            final var value = "value0";

            cache.put(key, value);

            assertTrue(cache.containsKey(key));
            assertEquals(value, cache.getIfPresent(key));
            assertEquals(1, cache.size());

            //removing entry by writing null value
            cache.put(key, null);

            assertEquals(0, cache.size());
            assertFalse(cache.containsKey(key));
            assertNull(value, cache.getIfPresent(key));
        }
    }

    @Test
    public void testRemove() {
        Cache<String, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());

        {
            final var key = "key0";
            final var value = "value0";

            //trying to remove non-existing key
            cache.remove(key);
            assertEquals(0, cache.size());
            assertFalse(cache.containsKey(key));
            assertNull(value, cache.getIfPresent(key));

            cache.put(key, value);

            assertTrue(cache.containsKey(key));
            assertEquals(value, cache.getIfPresent(key));
            assertEquals(1, cache.size());

            //removing entry by writing null value
            cache.remove(key);

            assertEquals(0, cache.size());
            assertFalse(cache.containsKey(key));
            assertNull(value, cache.getIfPresent(key));
        }
    }

    @Test
    public void testRemoveSameHash() {
        CompoundKey key1 = new CompoundKey(1, 1);
        CompoundKey key2 = new CompoundKey(1, 2);
        String value1 = "v1";
        String value2 = "v2";

        Cache<CompoundKey, String> cache = new CacheSimple<>(10);

        cache.put(key1, value1);
        // Delete - different key
        cache.put(key2, null);
        String s = cache.getIfPresent(key1);

        assertEquals(1, cache.size());
        assertEquals(value1, s);
    }

    @Test
    public void testGet() {
        Cache<String, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());

        final var key = "key0";
        final var value = "value0";

        assertFalse(cache.containsKey(key));
        assertNull(cache.getIfPresent(key));

        assertEquals(value, cache.get(key, k -> value));

        assertTrue(cache.containsKey(key));
        assertEquals(value, cache.getIfPresent(key));
        assertEquals(1, cache.size());
    }

    @Test
    public void testGetWithSameValue() {
        Cache<String, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());

        final var key = "key1";
        final var value = "value1";

        assertFalse(cache.containsKey(key));
        assertNull(cache.getIfPresent(key));

        cache.put(key, value);
        //get with same value
        assertEquals(value, cache.get(key, k -> value));

        assertTrue(cache.containsKey(key));
        assertEquals(value, cache.getIfPresent(key));
        assertEquals(1, cache.size());
    }

    @Test
    public void testGetWithDifferentValue() {
        Cache<String, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());

        final var key = "key2";
        final var value = "value2";
        final var differentValue = "differentValue2";

        assertFalse(cache.containsKey(key));
        assertNull(cache.getIfPresent(key));

        cache.put(key, value);
        //get with different value should not override existing value
        assertEquals(value, cache.get(key, k -> differentValue));

        assertTrue(cache.containsKey(key));
        assertEquals(value, cache.getIfPresent(key));
        assertEquals(1, cache.size());
    }

    @Test
    public void testGetExistingWithNullValue() {
        Cache<String, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());

        final var key = "key3";
        final var value = "value3";
        assertFalse(cache.containsKey(key));
        assertNull(cache.getIfPresent(key));

        cache.put(key, value);

        assertEquals(1, cache.size());
        //returning null should not change anyting
        assertEquals(value, cache.get(key, k -> null));
        assertEquals(1, cache.size());

        assertTrue(cache.containsKey(key));
        assertEquals(value, cache.getIfPresent(key));
    }

    @Test
    public void testGetNonExistingWithNullValue() {
        Cache<String, String> cache = new CacheSimple<>(10);
        assertEquals(0, cache.size());

        final var key = "key4";

        assertFalse(cache.containsKey(key));
        assertNull(cache.getIfPresent(key));
        assertEquals(0, cache.size());
        assertNull(cache.get(key, k -> null));
        assertEquals(0, cache.size());
        assertFalse(cache.containsKey(key));
        assertNull(cache.getIfPresent(key));
    }

    @Test
    public void testAllocatedSize() {
        var cache = new CacheSimple<>(2);
        assertEquals(2, cache.getAllocatedSize());

        cache = new CacheSimple<>(3);
        assertEquals(4, cache.getAllocatedSize());

        cache = new CacheSimple<>(4);
        assertEquals(4, cache.getAllocatedSize());

        cache = new CacheSimple<>(6);
        assertEquals(8, cache.getAllocatedSize());

        cache = new CacheSimple<>(8);
        assertEquals(8, cache.getAllocatedSize());

        cache = new CacheSimple<>(10);
        assertEquals(16, cache.getAllocatedSize());
    }

    // Compound key for tests.
    private static final class CompoundKey {
        private final int a;
        private final int b;

        private CompoundKey(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompoundKey that = (CompoundKey) o;
            return a == that.a && b == that.b; // Checks both "a" and "b"
        }

        @Override
        public int hashCode() {
            return a; // Doesn't depend on "b"
        }
    }
}
