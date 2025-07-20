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

package org.apache.jena.atlas.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestRefCountingMap {
	
    private static final String key1     = "key1";
    private static final String value1   = "value1";
    private static final String value1_1 = "value1_1";
	
	private final RefCountingMap<String,String> map = new RefCountingMap<>();
	public TestRefCountingMap() {} 
	
	@Test
	public void add0() {	
		assertEquals(0, map.refCount(key1));
		String result = map.get(key1);
		assertEquals(null, result);
	}
	
	@Test
	public void add1() {		
		map.add(key1, value1);
		String result = map.get(key1);
		assertEquals(value1, result);
		assertEquals(1, map.refCount(key1));
	}
	
	@Test
	public void add2() {
	    assertEquals(0, map.refCount(key1)) ;
		map.add(key1, value1);
        assertEquals(1, map.refCount(key1)) ;
		map.add(key1, value1);
		assertEquals(2, map.refCount(key1)) ;
	}
	
	@Test
	public void testRemove1() {
		map.remove(key1);
		String result = map.get(key1);
		assertEquals(null, result);
		assertEquals(0, map.refCount(key1));		
	}
	
    @Test
    public void add1Remove1() {
        map.add(key1, value1);
        assertEquals(1, map.refCount(key1));        
        map.remove(key1);
        assertEquals(0, map.refCount(key1));
        String result = map.get(key1);
        assertEquals(null, result);
    }

    @Test
	public void add2Remove1() {
		map.add(key1, value1);
		map.add(key1, value1);
		map.remove(key1);
		String result = map.get(key1);
		assertEquals(value1, result);
		assertEquals(1, map.refCount(key1));		
	}
	
	@Test
	public void add2Remove2() {
		map.add(key1, value1);
		map.add(key1, value1);
		map.remove(key1);
		map.remove(key1);
		String result = map.get(key1);
		assertEquals(null, result);
		assertEquals(0, map.refCount(key1));		
	}
	
	@Test
	public void add2Remove3() {
		map.add(key1, value1);
		map.add(key1, value1);
		map.remove(key1);
		map.remove(key1);
		map.remove(key1);
		String result = map.get(key1);
		assertEquals(null, result);
		assertEquals(0, map.refCount(key1));		
	}
	
	@Test
	public void add2Remove3Add1() {
		map.add(key1, value1);
		map.add(key1, value1);
		map.remove(key1);
		map.remove(key1);
		map.remove(key1);
		map.add(key1, value1);
		String result = map.get(key1);
		assertEquals(value1, result);
		assertEquals(1, map.refCount(key1));		
	}
	
	@Test
	public void add1Replace1() {
		map.add(key1, value1);
		map.add(key1, value1_1);
		String result = map.get(key1);
		assertEquals(value1_1, result);
		assertEquals(1, map.refCount(key1));		
	}
	
	@Test
	public void add2ForceRemove() {
		map.add(key1, value1);
		map.add(key1, value1);
		map.removeAll(key1);
		String result = map.get(key1);
		assertEquals(null, result);
		assertEquals(0, map.refCount(key1));		
	}
	
	@Test
	public void add2DifferentValueObjects() {
		String value1Copy = new String(value1);
		map.add(key1, value1);
		map.add(key1, value1Copy);
		String result = map.get(key1);
		assertEquals(value1, result);
		assertEquals(2, map.refCount(key1));
	}
}
