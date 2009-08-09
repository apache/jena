/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import org.junit.Before ;
import org.junit.Test ;
import atlas.lib.cache.CacheStatsAtomic ;
import atlas.test.BaseTest ;

public class TestCacheStats extends BaseTest
{
    Cache<Integer, Integer> cache ;

    @Before public void before() { cache = CacheFactory.createSimpleCache(2) ; }
    
    @Test public void stats_01()
    {
        cache.clear() ;
        cache = new CacheStatsAtomic<Integer, Integer>(cache) ;
        CacheStats<Integer, Integer> cs = (CacheStats<Integer, Integer>)cache ;
        assertEquals(0,cs.getCacheEntries()) ;
        assertEquals(0,cs.getCacheMisses()) ;
        assertEquals(0,cs.getCacheHits()) ;
        assertEquals(0,cs.getCacheEjects()) ;
    }
    
    @Test public void stats_02()
    {
        cache.clear() ;
        cache = new CacheStatsAtomic<Integer, Integer>(cache) ;
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
        cache = new CacheStatsAtomic<Integer, Integer>(cache) ;
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
        cache = new CacheStatsAtomic<Integer, Integer>(cache) ;
        CacheStats<Integer, Integer> cs = (CacheStats<Integer, Integer>)cache ;
        cache.put(7,77) ;
        cache.get(7) ;
        assertEquals(1,cs.getCacheHits()) ;
        cache.get(7) ;
        assertEquals(2,cs.getCacheHits()) ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */