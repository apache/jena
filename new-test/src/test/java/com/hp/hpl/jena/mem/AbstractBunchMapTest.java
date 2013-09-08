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

package com.hp.hpl.jena.mem;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Test triple bunch implementations - NOT YET FINISHED
 */
public abstract class AbstractBunchMapTest {

	protected abstract BunchMap getBunchMap();

	@Test
	public void testClear() {
		BunchMap map = getBunchMap();
		for (int i = 0; i < 5; i++) {
			map.put(i, mock(TripleBunch.class));
		}
		assertEquals(5, map.size());

		map.clear();
		assertEquals(0, map.size());
	}

	@Test
	public void testSize() {
		BunchMap map = getBunchMap();
		assertEquals(0, map.size());
		for (int i = 0; i < 5; i++) {
			map.put(i, mock(TripleBunch.class));
			assertEquals(i + 1, map.size());
		}
	}

	@Test
	public void testGet() {
		BunchMap map = getBunchMap();
		List<TripleBunch> lst = new ArrayList<TripleBunch>();
		for (int i = 0; i < 5; i++) {
			TripleBunch tb = mock(TripleBunch.class);
			lst.add(tb);
			map.put(i, tb);
		}
		for (int i = 0; i < 5; i++) {
			assertEquals(lst.get(i), map.get(i));
		}
	}

	@Test
	public void testPut() {
		BunchMap map = getBunchMap();
		List<TripleBunch> lst = new ArrayList<TripleBunch>();
		for (int i = 0; i < 5; i++) {
			map.put(i, mock(TripleBunch.class));
		}
		for (int i = 0; i < 5; i++) {
			TripleBunch tb = mock(TripleBunch.class);
			lst.add(tb);
			map.put(i, tb);
		}
		for (int i = 0; i < 5; i++) {
			assertEquals(lst.get(i), map.get(i));
		}
	}

	@Test
	public void testRemove() {
		BunchMap map = getBunchMap();
		List<TripleBunch> lst = new ArrayList<TripleBunch>();
		for (int i = 0; i < 5; i++) {
			TripleBunch tb = mock(TripleBunch.class);
			lst.add(tb);
			map.put(i, tb);
		}

		map.remove(0);
		assertNull(map.get(0));
		for (int i = 1; i < 5; i++) {
			assertEquals(lst.get(i), map.get(i));
		}
		assertEquals(4, map.size());

		map.remove(2);
		assertNull(map.get(0));
		assertEquals(lst.get(1), map.get(1));
		assertNull(map.get(2));
		assertEquals(lst.get(3), map.get(3));
		assertEquals(lst.get(4), map.get(4));
		assertEquals(3, map.size());

		map.remove(4);
		assertNull(map.get(0));
		assertEquals(lst.get(1), map.get(1));
		assertNull(map.get(2));
		assertEquals(lst.get(3), map.get(3));
		assertNull(map.get(4));
		assertEquals(2, map.size());

	}

	@Test
	public void testKeyIterator() {
		BunchMap map = getBunchMap();
		List<Integer> lst = new ArrayList<Integer>();
		for (int i = 0; i < 5; i++) {
			lst.add(i);
			map.put(i, mock(TripleBunch.class));
		}
		ExtendedIterator<Object> iter = map.keyIterator();
		while (iter.hasNext()) {
			assertFalse("List is empty", lst.isEmpty());
			Integer i = (Integer) iter.next();
			assertTrue("Missing index: " + i, lst.contains(i));
			lst.remove(i);
		}
		assertTrue("List is not empty", lst.isEmpty());
	}
}
