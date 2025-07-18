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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.atlas.lib.Cache;

/**
 * Light-weight LRU cache based on {@link LinkedHashMap}.
 * This cache implementation is for single-threaded use only.
 */
public class CachePlainLRU<K, V> implements Cache<K, V> {

    private final Map<K, V> cache;
    private final float loadFactor = 0.75f;
    private final float initialCapacityFactory = 0.75f;


    public CachePlainLRU(final int maxCapacity) {
        int initialCapacity = (int)(maxCapacity / initialCapacityFactory + 1);
        this.cache = new LinkedHashMap<K, V>(initialCapacity, loadFactor, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxCapacity;
            }
        };
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }


    @Override
    public V getIfPresent(K key) {
        return cache.get(key);
    }


    @Override
    public V get(K key, Function<K, V> function) {
        return CacheOps.getOrFill(this, key, function);
    }

    @Override
    public void put(K key, V thing) {
        cache.put(key, thing);
    }


    @Override
    public void remove(K key) {
        this.remove(key);
    }


    @Override
    public Iterator<K> keys() {
        return cache.keySet().iterator();
    }


    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }


    @Override
    public void clear() {}


    @Override
    public long size() {
        return 0;
    }


//
//    @Override
//    public boolean containsKey(final K key) {
//        return cache.containsKey(key);
//    }
//
//    @Override
//    public V get(final K key) {
//        return cache.get(key);
//    }
//
//    @Override
//    public void put(final K key, V value) {
//        cache.put(key, value);
//    }
//
//    public long size() {
//        return cache.size();
//    }



}
