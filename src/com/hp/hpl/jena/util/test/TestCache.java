/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestCache.java,v 1.1.1.1 2002-12-19 19:21:26 bwm Exp $
*/

package com.hp.hpl.jena.util.test;

/**
	@author bwm out of kers
*/

import com.hp.hpl.jena.util.cache.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestCache extends TestCase
    {    
        
   public TestCache(String name)
       { super( name ); }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Cache");
        
        suite.addTest( new CacheTestCase(CacheManager.RAND));
 
        return suite;
    }   
            
    public void assertFalse( String name, boolean b )
        { assertTrue( name, !b ); }
    
    private void assertDiffer( String title, Object x, Object y )
        { assertFalse( title, x.equals( y ) ); }
        
    
    static class CacheTestCase extends TestCase {
        String cacheType;
        
        CacheTestCase(String cacheType) {
            super( cacheType );
            this.cacheType = cacheType;
        }

        protected void runTest() {
            testCache();
        }
        
    public void testCache() {        
        testCacheCreation(cacheType);
        testCacheSimpleReturn(cacheType);
        testFillTheCache(cacheType);
    }
        
    public void testCacheCreation(String type) {
        Cache c1 = CacheManager.createCache(type, "c1", 100);
        try {
            Cache c2 = CacheManager.createCache(type, "c2", 1);
            assertTrue("Missing error on bad cache size: " + type, false);
        } catch (Error e) {}
    }
    
    public void testCacheSimpleReturn(String type) {
        
        int size = 100;
        // this test does not fill the cache
        Cache c1 = CacheManager.createCache(type, "c1", size);
        
        String  k1 = "one";
        String  k2 = k1;
        String  k3 = k2;
        Integer v1 = new Integer(-1);
        Integer v2 = v1;
        Integer v3 = v2;
        c1.put(k1, v1);

        for (int i=0; i<size; i++) {
            k1 = k2;
            v1 = v2;
            Object o = c1.get(k1);
            assertTrue("expected a hit", o != null);
            assertEquals("should be the expected object", o, v1);
            k2 = k3;
            v2 = v3;
            o = c1.get(k2);
            assertTrue("expected a hit", o != null);
            assertEquals("should be the expected object", o, v2);
            
            k3 = "T" + i;
            v3 = new Integer(i);
            c1.put(k3,v3);
        }
    }
    
    public void testFillTheCache(String type) {
        final int size = 100;
        Cache c1 = CacheManager.createCache(type, "c1", size);
        String[] k = new String[size];
        String[] v = new String[size];
        
        for (int i=0; i<size; i++) {
            k[i] = "K" + i;
            v[i] = "V" + i;
            c1.put(k[i], v[i]);
        }
        
        int count = 0;
        
        for (int i=0; i<size; i++) {
            if (c1.get(k[i]) != null) {
                count++;
            }
        }
        
        assertTrue("too low a hit rate: " + type + " = " + count, 
                                                               count > size/2);
        assertEquals("count puts", size, c1.getPuts());
        assertEquals("count gets", size, c1.getGets());
        assertEquals("count hits", count, c1.getHits());
    }
    }
        
}
/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
