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

import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.cache.* ;

public class CacheFactory {
    /**
     * Create a cache which has space for up to a certain number of objects.
     * This is an LRU cache, or similar.
     * The cache returns null for a cache miss.
     * The cache is thread-safe for single operations.
     */
    public static <Key, Value> Cache<Key, Value> createCache(int maxSize) {
        return createCache(maxSize, null) ;
    }

    /**
     * Create a cache which has space for up to a certain number of objects.
     * This is an LRU cache, or similar.
     * The cache returns null for a cache miss.
     * The cache is thread-safe for single operations.
     */
    public static <Key, Value> Cache<Key, Value> createCache(int maxSize, BiConsumer<Key, Value> dropHandler) {
        // Choice point. Add a Guava dependency and ...
        //return new CacheGuava<>(maxSize, dropHandler) ;
        return new CacheCaffeine<>(maxSize, dropHandler) ;
    }

    public static <Key, Value> Cache<Key, Value> wrap(com.github.benmanes.caffeine.cache.Cache<Key,Value> caffeine) {
        // Use a configured and built Caffeine cache with this API.
        return new CacheCaffeine<>(caffeine) ;
    }

    /**
     * Create a null cache.
     * This cache never retains a value and always
     * evaluates in {@link Cache#getOrFill}.
     */
    public static <Key, Value> Cache<Key, Value> createNullCache() {
        return new Cache0<>() ;
    }

    /** Create a lightweight cache (e.g. slot replacement) */
    public static <Key, Value> Cache<Key, Value> createSimpleCache(int size) {
        return new CacheSimple<>(size, null) ;
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
        return new CacheSetImpl<>(c) ;
    }

    /** Add a synchronization wrapper to an existing set-cache */
    public static <Obj> CacheSet<Obj> createSync(CacheSet<Obj> cache) {
        if ( cache instanceof CacheSetSync<? > )
            return cache ;
        return new CacheSetSync<>(cache) ;
    }

}
