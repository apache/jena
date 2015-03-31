/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package com.hp.hpl.jena.util;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Test;

public class TestBoundedLRUMap extends TestCase {
	public static TestSuite suite() {
		return new TestSuite(TestBoundedLRUMap.class, "TestBoundedLRUMap");
	}
	
	@Test
	public void testBoundedMap() throws Exception {
		BoundedLRUMap<Integer, Integer> map = new BoundedLRUMap<Integer, Integer>(4);
		map.put(1, 1);
		map.put(2, 2);
		map.put(3, 3);
		map.put(4, 4);
		assertEquals(4, map.size());
		map.put(5, 5);
		assertEquals(4, map.size());
		// 1 was the oldest
		assertFalse(map.containsKey(1));
		for (int i=2; i<=5; i++) {
			assertTrue(map.containsKey(i));
		}
		map.get(2);
		map.put(6, 6);
		assertEquals(4, map.size());
		// 2 should not have been removed as we just .get it
		assertTrue(map.containsKey(2));
		// while 3 would now have been the oldest
		assertFalse(map.containsKey(3));
		assertEquals(4, map.size());
		map.remove(2);
		map.put(7, 7);
		assertEquals(4, map.size());
		
		
	}

}
