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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.jena.atlas.lib.Cache;
import org.junit.Test;

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
		Cache<String, Integer> cache = new CacheSimple<>(10);
		assertEquals("Aa".hashCode(), "BB".hashCode());
		cache.put("Aa", 1);
		assertFalse("Keys with same hash code should not be considered equal", cache.containsKey("BB"));
	}
}
