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

import java.util.Iterator ;
import java.util.Map;
import java.util.concurrent.Callable ;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.logging.Log;


/**
 * A simple fixed size cache that uses the hash code to address a slot.
 * Clash policy is to overwrite.
 * No object creation during lookup or insert.
 */

public class CacheSimple<K,V> implements Cache<K,V>
{
	private Map<K,V> internalCache;
	
    /**
     * NO OP unless set by {@link #setDropHandler(BiConsumer)}
     */
    private BiConsumer<K,V> dropHandler = (k, v) -> {}  ;
    
    public CacheSimple(int size)
    { 
    		this.internalCache = new ConcurrentHashMap<>(size);
    }
    
    @Override
    public void clear()
    { 
    		internalCache.clear();
    }

    @Override
    public boolean containsKey(K key)
    {
    		return internalCache.containsKey(key);
    }

    @Override
    public V getIfPresent(K key)
    {
        return internalCache.get(key);
    }

	@Override
	public V getOrFill(K key, Callable<V> callable) {
		final Function<K, V> f = new Function<K, V>() {

			@Override
			public V apply(K dummy) {
				try {
					return callable.call();
				} catch (Exception e) {
					Log.warn(CacheSimple.class, "Execution exception filling cache", e) ;
		            return null ;
				}
			}
		};
		return internalCache.computeIfAbsent(key, f);
	}

    @Override
    public void put(K key, V thing)
    {
        internalCache.put(key, thing);
    }

    @Override
    public void remove(K key)
    {
    		dropHandler.accept(key, internalCache.remove(key));
    }

    @Override
    public long size()
    {
        return internalCache.size() ;
    }

    @Override
    public Iterator<K> keys()
    {
        return internalCache.keySet().iterator();
    }

    @Override
    public boolean isEmpty()
    {
        return internalCache.isEmpty() ;
    }

    /** Callback for entries when dropped from the cache */
    @Override
    public void setDropHandler(BiConsumer<K,V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }
}
