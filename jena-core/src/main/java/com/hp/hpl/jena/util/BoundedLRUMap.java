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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bounded Map with a maximum size.
 * <p>
 * On insertion of entries beyond the maximum size, the eldest accessed entry is
 * removed.
 * 
 * @param <K>
 *            Type of keys
 * @param <V>
 *            Type of values
 */
public class BoundedLRUMap<K,V> extends LinkedHashMap<K, V> implements Map<K,V> {		
	
	private static final long serialVersionUID = -1424511852972661771L;
	private int maxEntries;

	/**
	 * Construct a BoundedLRUMap
	 * 
	 * @param maxEntries Maximum number of entries
	 */
	public BoundedLRUMap(int maxEntries) {
		super(Math.max(maxEntries/16, 16), 0.75f, true);
		if (maxEntries <= 0) {
			throw new IllegalArgumentException("maxEntries <= 0");
		}
		this.maxEntries = maxEntries;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return size() > maxEntries;
	}
}
