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

import org.apache.jena.atlas.lib.Cache;
import org.junit.Test;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.*;

/**
 * Simple test to ensure that {@link CacheSimple} evidences the fixed-size
 * behavior we desire.
 */
public class CacheSimpleTest {

	@Test
	public void testFixedSize() {
		final int maxSize = 5;
		final int submittedEntries = 10;
		final Cache<Integer, Object> testCache = new CacheSimple<>(maxSize);
		rangeClosed(1, submittedEntries).boxed().collect(toMap(k -> k, v -> 1))
				.forEach(testCache::put);
		assertEquals("Test cache failed to maintain fixed size!", maxSize, testCache.size());
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
