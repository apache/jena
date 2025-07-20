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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List ;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;

@ParameterizedClass(name="{index}: {0}")
@MethodSource("provideArgs")
public class TestCache
{
    // Tests do not apply to cache1.
    private static interface CacheMaker<K,V> { Cache<K,V> make(int size) ; }

    private static CacheMaker<Integer, Integer> oneSlot = (int size)->CacheFactory.createOneSlotCache();
    private static CacheMaker<Integer, Integer> simple = (int size)->CacheFactory.createSimpleCache(size);
    private static CacheMaker<Integer, Integer> standard = (int size)->CacheFactory.createCache(size);
    private static CacheMaker<Integer, Integer> plainLRU = (int size)->CacheFactory.createCache(size);

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of("Simple(10)", simple , 10)
                 , Arguments.of( "Simple(2)", simple , 2)
                 , Arguments.of( "Simple(1)" , simple ,1)
                 , Arguments.of( "Plain(10)", plainLRU , 10)
                 , Arguments.of( "Plain(2)", plainLRU , 2)
                 , Arguments.of( "Plain(1)" , plainLRU , 1)
                 , Arguments.of( "Standard(10)" , standard, 10)
                 , Arguments.of( "Standard(2)"  , standard, 2)
                 , Arguments.of( "Standard(1)"  , standard, 1)
                 , Arguments.of( "SingleSlot"  , oneSlot, 1)
                        );
        return x.stream();
    }

    Cache<Integer, Integer> cache;
    CacheMaker<Integer, Integer> cacheMaker;
    int size;

    public TestCache(String name, CacheMaker<Integer, Integer> cacheMaker, int size) {
        this.cacheMaker = cacheMaker;
        this.size = size;
    }

    @BeforeEach
    public void before() {
        cache = cacheMaker.make(size);
    }

    @Test
    public void cache_00() {
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
    }

    @Test
    public void cache_01() {
        Integer x = cache.getIfPresent(7);
        cache.put(7, 7);
        assertEquals(1, cache.size());
        assertNull(x);
        assertTrue(cache.containsKey(7));
        assertEquals(Integer.valueOf(7), cache.getIfPresent(7));
    }

    @Test
    public void cache_02() {
        cache.put(7, 7);
        cache.put(8, 8);
        // Not true for Cache1.
        if ( size > 2 )
            assertEquals(2, cache.size());
        if ( size > 2 )
            assertTrue(cache.containsKey(7));

        if ( size > 2 )
            assertEquals(Integer.valueOf(7), cache.getIfPresent(7));

        assertTrue(cache.containsKey(8));
        assertEquals(Integer.valueOf(8), cache.getIfPresent(8));
    }

    @Test
    public void cache_03() {
        cache.put(7, 7);
        Integer x1 = cache.getIfPresent(7);
        cache.put(7, 18);
        assertEquals(1, cache.size());
        assertEquals(7, x1.intValue());
        assertTrue(cache.containsKey(7));
        assertEquals(Integer.valueOf(18), cache.getIfPresent(7));
    }

    @Test
    public void cache_04() {
        cache.clear();
        cache.put(7, 77);
        List<Integer> x = Iter.toList(cache.keys());
        assertEquals(1, x.size());
        assertEquals(Integer.valueOf(7), x.get(0));
    }

    @Test
    public void cache_05() {
        cache.clear();
        cache.put(7, 77);
        cache.clear();
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
    }
}
