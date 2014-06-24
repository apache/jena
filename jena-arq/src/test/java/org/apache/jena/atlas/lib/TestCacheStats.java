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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.CacheStats ;
import org.apache.jena.atlas.lib.cache.CacheStatsAtomic ;
import org.junit.Before ;
import org.junit.Test ;

public class TestCacheStats extends BaseTest
{
    Cache<Integer, Integer> cache ;

    @Before public void before() { cache = CacheFactory.createSimpleCache(2) ; }
    
    @Test public void stats_01()
    {
        cache.clear() ;
        cache = new CacheStatsAtomic<>(cache) ;
        CacheStats<Integer, Integer> cs = (CacheStats<Integer, Integer>)cache ;
        assertEquals(0,cs.getCacheEntries()) ;
        assertEquals(0,cs.getCacheMisses()) ;
        assertEquals(0,cs.getCacheHits()) ;
        assertEquals(0,cs.getCacheEjects()) ;
    }
    
    @Test public void stats_02()
    {
        cache.clear() ;
        cache = new CacheStatsAtomic<>(cache) ;
        CacheStats<Integer, Integer> cs = (CacheStats<Integer, Integer>)cache ;
        cache.put(7,77) ;
        assertEquals(1,cs.getCacheEntries()) ;
        cache.remove(7) ;
        assertEquals(0,cs.getCacheEntries()) ;
        cache.clear() ;
        assertEquals(0,cs.getCacheEntries()) ;
    }
    
    @Test public void stats_03()
    {
        cache.clear() ;
        cache = new CacheStatsAtomic<>(cache) ;
        CacheStats<Integer, Integer> cs = (CacheStats<Integer, Integer>)cache ;
        cache.put(7,77) ;
        cache.put(8,88) ;
        assertEquals(2,cs.getCacheEntries()) ;
        cache.remove(7) ;
        assertEquals(1,cs.getCacheEntries()) ;
        cache.clear() ;
        assertEquals(0,cs.getCacheEntries()) ;
    }

    @Test public void stats_04()
    {
        cache.clear() ;
        cache = new CacheStatsAtomic<>(cache) ;
        CacheStats<Integer, Integer> cs = (CacheStats<Integer, Integer>)cache ;
        cache.put(7,77) ;
        cache.get(7) ;
        assertEquals(1,cs.getCacheHits()) ;
        cache.get(7) ;
        assertEquals(2,cs.getCacheHits()) ;
    }
}
