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

import org.apache.jena.atlas.lib.cache.* ;

public class CacheFactory
{
    /** Create a cache which has space for up to a certain number of objects.
     *  This is an LRU cache, or similar. 
     * The cache returns null for a cache miss.
     */
    public static <Key, Value> Cache<Key, Value> createCache(int maxSize)
    {
        return createCache(0.75f, maxSize) ;
    }

    /** Create a cache which has space for up to a certain number of objects.
     * The cache has an explicit loadfactor.
     * The cache returns null for a cache miss.
     */
    public static <Key, Value> Cache<Key, Value> createCache(float loadFactor, int maxSize)
    {
        return new CacheLRU<>(loadFactor, maxSize) ;
    }
    
    /** Create a cache which has space for upto a certain number of objects. 
     * Call the getter when the cache has a miss.
     */
    public static <Key, Value> Cache<Key, Value> createCache(Getter<Key, Value> getter, int maxSize)
    {
        Cache<Key, Value> cache = createCache(0.75f, maxSize) ;
        return createCacheWithGetter(cache, getter) ;
    }

    /** Create a null cache */
    public static <Key, Value> Cache<Key, Value> createNullCache()
    {
        return new Cache0<>() ;
    }

    /** Create a cache which has unbounded space */
    public static <Key, Value> Cache<Key, Value> createCacheUnbounded()
    {
        return new CacheUnbounded<>() ;
    }
    

    /* Add a getter wrapper to an existing cache */
    public static <Key, Value> Cache<Key, Value> createCacheWithGetter(Cache<Key, Value> cache, Getter<Key, Value> getter)
    {
        return new CacheWithGetter<>(cache, getter) ;
    }

    /** Create a lightweight cache (e.g. slot replacement) */  
    public static <Key, Value> Cache<Key, Value> createSimpleCache(int size)
    {
        return new CacheSimple<>(size) ;
    }
    
    /** One slot cache */
    public static <Key, Value> Cache<Key, Value> createOneSlotCache()
    {
        return new Cache1<>() ;
    }

    /** Add a statistics wrapper to an existing cache */
    public static <Key, Value> CacheStats<Key, Value> createStats(Cache<Key, Value> cache)
    {
        if ( cache instanceof CacheStats<?,?>)
            return (CacheStats<Key, Value>) cache ;
        return new CacheStatsAtomic<>(cache) ;
    }

    /** Create set-cache, rather than a map-cache.
     * @see Pool
     */
    public static <Obj> CacheSet<Obj> createCacheSet(int size)
    {
        return new CacheSetLRU<>(size) ;
    }

    /** Add a synchronization wrapper to an existing set-cache */
    public static <Obj> CacheSet<Obj> createSync(CacheSet<Obj> cache)
    {
        if ( cache instanceof CacheSetSync<?>)
            return cache ;
        return new CacheSetSync<>(cache) ;
    }

}
