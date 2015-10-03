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

package org.apache.jena.atlas.lib ;

import org.apache.jena.atlas.lib.cache.* ;

public class CacheFactory {
    /**
     * Create a cache which has space for up to a certain number of objects.
     * This is an LRU cache, or similar.
     * The cache returns null for a cache miss.
     * The cache is thread-safe for single operations.
     */
    public static <Key, Value> Cache<Key, Value> createCache(int maxSize) {
        return new CacheGuava<>(maxSize) ;
    }

    /** Create a null cache */
    public static <Key, Value> Cache<Key, Value> createNullCache() {
        return new Cache0<>() ;
    }

    /** Create a lightweight cache (e.g. slot replacement) */
    public static <Key, Value> Cache<Key, Value> createSimpleCache(int size) {
        return new CacheSimple<>(size) ;
    }

    /** One slot cache */
    public static <Key, Value> Cache<Key, Value> createOneSlotCache() {
        return new Cache1<>() ;
    }

    /**
     * Create set-cache, rather than a map-cache.
     * The cache is thread-safe for single operations.
     * 
     * @see Pool
     */
    public static <Obj> CacheSet<Obj> createCacheSet(int size) {
        Cache<Obj, Object> c = createCache(size) ;
        return new CacheSetImpl<Obj>(c) ;
    }

    /** Add a synchronization wrapper to an existing set-cache */
    public static <Obj> CacheSet<Obj> createSync(CacheSet<Obj> cache) {
        if ( cache instanceof CacheSetSync<? > )
            return cache ;
        return new CacheSetSync<>(cache) ;
    }

}
