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
import atlas.test.BaseTest;

@RunWith(Parameterized.class)
public class TestCache extends BaseTest
{
    private static interface CacheMaker<K,V> { Cache<K,V> make() ; } 

    private static CacheMaker<Integer, Integer> simple = 
        new CacheMaker<Integer, Integer>()
        { public Cache<Integer, Integer> make() { return CacheFactory.createSimpleCache(10) ; } } ;
            
     private static CacheMaker<Integer, Integer> standard = 
         new CacheMaker<Integer, Integer>()
         { public Cache<Integer, Integer> make() { return CacheFactory.createCache(10) ; } } ;
    
    @Parameters
    public static Collection<Object[]> cacheMakers()
    {
        return Arrays.asList(new Object[][] { { simple } , { standard } } ) ; 
    }
    
    Cache<Integer, Integer> cache ;
    CacheMaker<Integer,Integer> cacheMaker ;
    
    public TestCache(CacheMaker<Integer,Integer> cacheMaker)
    {
        this.cacheMaker = cacheMaker ;
    }
    
    @Before public void before() { cache = cacheMaker.make() ; }
    
    @Test public void cache_00()
    {
        assertEquals(0, cache.size()) ;
        assertTrue(cache.isEmpty()) ;
    }
    
    @Test public void cache_01()
    {
        cache.put(7, 7) ;
        assertEquals(1, cache.size()) ;
        assertTrue(cache.containsKey(7)) ;
        assertEquals(Integer.valueOf(7), cache.get(7)) ;
    }
    
    @Test public void cache_02()
    {
        cache.put(7, 7) ;
        cache.put(8, 8) ;
        assertEquals(2, cache.size()) ;
        assertTrue(cache.containsKey(7)) ;
        assertTrue(cache.containsKey(8)) ;
        assertEquals(Integer.valueOf(7), cache.get(7)) ;
        assertEquals(Integer.valueOf(8), cache.get(8)) ;
    }
    
    @Test public void cache_03()
    {
        cache.put(7, 7) ;
        cache.put(7, 18) ;
        assertEquals(1, cache.size()) ;
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