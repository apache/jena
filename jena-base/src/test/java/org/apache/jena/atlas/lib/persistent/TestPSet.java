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

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TestPSet extends Assert {

	@Test
	public void plusAndMinusWorkCorrectly() {
		final Object testObject = new Object();
		final PersistentSet<Object> testSet = PSet.empty();
		assertFalse(testSet.contains(testObject));
		final PersistentSet<Object> nextSet = testSet.plus(testObject);
		assertTrue(nextSet.contains(testObject));
		final PersistentSet<Object> nextNextSet = nextSet.minus(testObject);
		assertFalse(nextNextSet.contains(testObject));
	}

	@Test
	public void streaming() {
		final Object testObject1 = new Object();
		final Object testObject2 = new Object();
		final PersistentSet<Object> testSet = PSet.empty().plus(testObject1).plus(testObject2);
		assertEquals(Set.of(testObject1, testObject2), testSet.stream().collect(toSet()));
	}
}
