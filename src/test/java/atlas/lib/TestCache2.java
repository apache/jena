/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import org.junit.Test ;
import atlas.lib.cache.Cache1 ;
import atlas.lib.cache.Getter ;
import atlas.test.BaseTest ;

// Non-parameterized tests
public class TestCache2 extends BaseTest
{
    // Cache1
    @Test public void cache_10()
    {
        Cache<Integer, String> cache = new Cache1<Integer, String>() ;
        String str = cache.get(1) ;
        assertNull(str) ;

        cache.put(1, "1") ;
        str = cache.get(1) ;
        assertEquals("1", str) ;

        cache.put(2, "2") ;
        str = cache.get(1) ;
        assertNull(str) ;
        
        cache.put(1, "1") ;
        str = cache.get(2) ;
        assertNull(str) ;
        str = cache.get(1) ;
        assertEquals("1", str) ;
    }
    
    static Getter<Integer, String> getter = new Getter<Integer, String>() {
        public String get(Integer key)
        { return key.toString() ; }
    } ;

    // Cache + getters
    @Test public void cacheGetter_1()
    {
        Cache<Integer, String> cache = CacheFactory.createCache(getter, 2) ;
        String str = cache.get(1) ;
        assertEquals("1", str) ;
    }
    
    // Cache + getters
    @Test public void cacheGetter_2()
    {
        Cache<Integer, String> cache = CacheFactory.createCache(getter, 2) ;
        String str1 = cache.get(1) ;
        String str2 = cache.get(2) ;
        String str3 = cache.get(3) ;
        assertEquals("1", str1) ;
        assertEquals("2", str2) ;
        assertEquals("3", str3) ;
        cache.put(1, "10") ;
        str1 = cache.get(1) ;
        assertEquals("10", str1) ;
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