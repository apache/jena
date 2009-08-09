/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import atlas.iterator.Iter;
import atlas.lib.cache.CacheStatsAtomic ;
import atlas.lib.cache.CacheStatsSimple ;
import atlas.test.BaseTest;

@RunWith(Parameterized.class)
public class TestCache extends BaseTest
{
    // Test do not apply to cache1.
    private static interface CacheMaker<K,V> { Cache<K,V> make(int size) ; String name() ; } 

    private static CacheMaker<Integer, Integer> simple = 
        new CacheMaker<Integer, Integer>()
        { 
        public Cache<Integer, Integer> make(int size) { return CacheFactory.createSimpleCache(size) ; }
        public String name() { return "Simple" ; } 
        } 
    ;

    private static CacheMaker<Integer, Integer> standard = 
        new CacheMaker<Integer, Integer>()
        {
        public Cache<Integer, Integer> make(int size) { return CacheFactory.createCache(size) ; }
        public String name() { return "Standard" ; } 
        }
    ;

    private static CacheMaker<Integer, Integer> stats = 
        new CacheMaker<Integer, Integer>()
        {
        public Cache<Integer, Integer> make(int size)
        { 
            Cache<Integer, Integer> c = CacheFactory.createCache(size) ;
            return new CacheStatsSimple<Integer, Integer>(c) ;
        }
        public String name() { return "Stats" ; }
        }
    ;

    private static CacheMaker<Integer, Integer> statsAtomic = 
        new CacheMaker<Integer, Integer>()
        {
        public Cache<Integer, Integer> make(int size)
        { 
            Cache<Integer, Integer> c = CacheFactory.createCache(size) ;
            return new CacheStatsAtomic<Integer, Integer>(c) ;
        }
        public String name() { return "StatsAtomic" ; }
        }
    ;
           
    @Parameters
    public static Collection<Object[]> cacheMakers()
    {
        return Arrays.asList(new Object[][] {
//          { simple , 1 }

                                              { simple , 10 }
                                            , { simple , 2 } 
                                            , { simple , 1 }
                                            , { standard , 10 }
                                            , { standard , 2 }
                                            , { standard , 1 }
                                            , { stats , 10 }
                                            , { stats , 2 }
                                            , { stats , 1 }
                                            , { statsAtomic , 10 }
                                            , { statsAtomic , 2 }
                                            , { statsAtomic , 1 }
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
        Integer x = cache.put(7, 7) ;
        assertEquals(1, cache.size()) ;
        assertNull(x) ;
        assertTrue(cache.containsKey(7)) ;
        assertEquals(Integer.valueOf(7), cache.get(7)) ;
    }
    
    @Test public void cache_02()
    {
        Integer x1 = cache.put(7, 7) ;
        Integer x2 = cache.put(8, 8) ;
        // Not true for Cache1.
        if ( size > 2 )
            assertEquals(2, cache.size()) ;
        assertNull(x1) ;
        if ( size > 2 )
            assertNull(x2) ;
        // else don't know.
        
        if ( size > 2 )
            assertTrue(cache.containsKey(7)) ;
        
        if ( size > 2 )
            assertEquals(Integer.valueOf(7), cache.get(7)) ;
        
        assertTrue(cache.containsKey(8)) ;
        assertEquals(Integer.valueOf(8), cache.get(8)) ;
    }
    
    @Test public void cache_03()
    {
        Integer x1 = cache.put(7, 7) ;
        Integer x2 = cache.put(7, 18) ;
        assertEquals(1, cache.size()) ;
        assertEquals(7, x2.intValue()) ;
        assertTrue(cache.containsKey(7)) ;
        assertEquals(Integer.valueOf(18), cache.get(7)) ;
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