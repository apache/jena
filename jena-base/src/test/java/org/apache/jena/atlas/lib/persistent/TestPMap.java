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

package org.apache.jena.atlas.lib.persistent;

import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class TestPMap {

	@Test
	public void containsAddedElement() {
		final TestMap testMap = new TestMap();
		assertFalse(testMap.containsKey("key"));
		final TestMap nextMap = testMap.plus("key", "value");
		assertTrue(nextMap.containsKey("key"));
		assertEquals(Optional.of("value"), nextMap.get("key"));
		final TestMap nextNextMap = nextMap.minus("key");
		assertFalse(nextNextMap.containsKey("key"));
	}

	@Test
	public void streaming() {
		TestMap testMap = new TestMap().plus("key1", "value1").plus("key2", "value2");
		final Stream<Entry<String, String>> testStream = testMap.entryStream();
		final Map<String, String> recoveredMap = testStream.collect(toMap(Entry::getKey, Entry::getValue));
		for (final Entry<String, String> e : recoveredMap.entrySet()) {
			assertEquals(e.getValue(), testMap.get(e.getKey()).orElseThrow());
			testMap = testMap.minus(e.getKey());
		}
		assertEquals(0, testMap.entryStream().count());
	}

	private static class TestMap extends PMap<String, String, TestMap> {

		/**
		 * @param wrappedMap
		 */
		TestMap(final com.github.andrewoma.dexx.collection.Map<String, String> wrappedMap) {
			super(wrappedMap);
		}

		TestMap() {
			super();
		}

		@Override
		protected TestMap wrap(final com.github.andrewoma.dexx.collection.Map<String, String> wrapped) {
			return new TestMap(wrapped);
		}
	}
}
