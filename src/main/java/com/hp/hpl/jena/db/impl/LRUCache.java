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

//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports
import java.util.*;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.CollectionFactory;


//=======================================================================
/**
* As simple LRU cache based on LinkedHashMap. otherwise, pretty much
* the same as SimpleCache.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/

public class LRUCache<K,T> implements ICache<K,T>
{
	// Predates LinkedHashMap
	protected Map<K, T> keyCache;
	protected Map<T, K> valCache;

	protected K Keys[];
	protected Random rand;

	public LRUCache(int max) {
		rand = new Random();
		resize(max);
	}
	
	@SuppressWarnings("unchecked")
    protected void resize ( int max ) {
		maxCount = max;
		keyCache = CollectionFactory.createHashedMap(max);
		valCache = CollectionFactory.createHashedMap(max);
		Keys = (K[]) new Object[max];
		count = 0;
	}

	protected int maxCount;
	protected int count;

	@Override
    public T get(K id) {
		return keyCache.get(id);
	}
	
	public Object getByValue(String val) {
		return valCache.get(val);
	}


	@Override
    public void put(K id, T val) {
		synchronized (this) {
			int curSize = keyCache.size();
			keyCache.put(id, val);
			valCache.put(val,id);
			if (keyCache.size() > curSize) {
				int ix = count++;
				if (count > maxCount) {
					// pick an entry at random and remove it.
					// not exactly LRU
					ix = rand.nextInt(maxCount);
					Object keyval = keyCache.get(Keys[ix]);
					if ( (keyval == null) || (keyCache.remove(Keys[ix]) == null) )
						throw new JenaException("LRUCache keyCache corrupted");
					if ( valCache.remove(keyval) == null )
						throw new JenaException("LRUCache valCache corrupted");
					count--;
					Keys[ix] = id;
					if (keyCache.size() > maxCount)
						throw new JenaException("LRUCache exceeds threshold");
				}
				Keys[ix] = id;
			}
		}
	}

    public void clear() {
    	keyCache.clear();
    	valCache.clear();
    	count = 0;
    }
    
	@Override
    public void setLimit(int max) {
		resize(max);
	}

	@Override
    public int getLimit() {
		return maxCount;
	}

}
