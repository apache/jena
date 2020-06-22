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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.junit.Before ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)
public class TestCache
{
    // Tests do not apply to cache1.
    private static interface CacheMaker<K,V> { Cache<K,V> make(int size) ; String name() ; } 

    private static CacheMaker<Integer, Integer> simple = 
        new CacheMaker<Integer, Integer>()
        { 
        @Override
        public Cache<Integer, Integer> make(int size) { return CacheFactory.createSimpleCache(size) ; }
        @Override
        public String name() { return "Simple" ; } 
        } 
    ;

    private static CacheMaker<Integer, Integer> standard = 
        new CacheMaker<Integer, Integer>()
        {
        @Override
        public Cache<Integer, Integer> make(int size) { return CacheFactory.createCache(size) ; }
        @Override
        public String name() { return "Standard" ; } 
        }
    ;

    @Parameters
    public static Collection<Object[]> cacheMakers()
    {
        return Arrays.asList(new Object[][] {
            { simple , 10 }
            , { simple , 2 } 
            , { simple , 1 }
            , { standard , 10 }
            , { standard , 2 }
            , { standard , 1 }
        } ) ; 
    }

    Cache<Integer, Integer> cache ;
    CacheMaker<Integer,Integer> cacheMaker ;
    int size ;
    
    
    public TestCache(CacheMaker<Integer,Integer> cacheMaker, int size)
    {
        this.cacheMaker = cacheMaker ;
        this.size = size ;
        
    }
    
    @Before public void before() { cache = cacheMaker.make(size) ; }
    
    @Test public void cache_00()
    {
        assertEquals(0, cache.size()) ;
        assertTrue(cache.isEmpty()) ;
    }
    
    @Test public void cache_01()
    {
        Integer x = cache.getIfPresent(7) ;
        cache.put(7, 7) ;
        assertEquals(1, cache.size()) ;
        assertNull(x) ;
        assertTrue(cache.containsKey(7)) ;
        assertEquals(Integer.valueOf(7), cache.getIfPresent(7)) ;
    }
    
    @Test public void cache_02()
    {
        cache.put(7, 7) ;
        cache.put(8, 8) ;
        // Not true for Cache1.
        if ( size > 2 )
            assertEquals(2, cache.size()) ;
        if ( size > 2 )
            assertTrue(cache.containsKey(7)) ;
        
        if ( size > 2 )
            assertEquals(Integer.valueOf(7), cache.getIfPresent(7)) ;
        
        assertTrue(cache.containsKey(8)) ;
        assertEquals(Integer.valueOf(8), cache.getIfPresent(8)) ;
    }
    
    @Test public void cache_03()
    {
        cache.put(7, 7) ;
        Integer x1 = cache.getIfPresent(7) ;
        cache.put(7, 18) ;
        assertEquals(1, cache.size()) ;
        assertEquals(7, x1.intValue()) ;
        assertTrue(cache.containsKey(7)) ;
        assertEquals(Integer.valueOf(18), cache.getIfPresent(7)) ;
    }
    
    @Test public void cache_04()
    {
        cache.clear() ;
        cache.put(7, 77) ;
        List<Integer> x = Iter.toList(cache.keys()) ;
        assertEquals(1, x.size()) ;
        assertEquals(Integer.valueOf(7), x.get(0)) ;
    }
    
    @Test public void cache_05()
    {
        cache.clear() ;
        cache.put(7, 77) ;
        cache.clear() ;
        assertEquals(0, cache.size()) ; 
        assertTrue(cache.isEmpty()) ;
    }
}
