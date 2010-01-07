/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import org.junit.Test ;
import atlas.test.BaseTest ;

import com.hp.hpl.jena.shared.PrefixMapping ;

public abstract class TestPrefixMapping2 extends BaseTest
{
    static final String defaultPrefixURI  = "" ;

    /** Create a fresh PrefixMapping */
    protected abstract PrefixMapping create() ;
    /** Create a fresh view over the same storage as last create() */
    protected abstract PrefixMapping view() ; 
    
    @Test public void prefix1()
    {
        PrefixMapping pmap = create() ;
    }
    
    @Test public void prefix2()
    {
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", "http://example/") ;
        assertNotNull(pmap.getNsPrefixURI("ex")) ;
    }
    
    @Test public void prefix3()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        // Create a second view onto the same storage.
        
        PrefixMapping pmap2 = view() ;
        String x = pmap2.getNsPrefixURI("ex") ;
        
        assertNotNull(x) ;
        assertEquals(uri,x) ;
    }
        
    @Test public void prefix4()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals("ex", pmap.getNsURIPrefix("http://example/")) ;
    }
        
    @Test public void prefix5()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals(uri+"foo", pmap.expandPrefix("ex:foo")) ;
    }

    @Test public void prefix6()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals("ex:foo", pmap.qnameFor("http://example/foo")) ;
    }

    @Test public void prefix7()
    {
        String uri1 = "http://example/" ;
        String uri2 = "http://example/ns#" ;
        
        PrefixMapping pmap = create() ;
        pmap.setNsPrefix("ex1", uri1) ;
        pmap.setNsPrefix("ex2", uri2) ;
        assertEquals("ex2:foo", pmap.qnameFor("http://example/ns#foo")) ;
    }
    
    @Test public void prefix8()
    {
        PrefixMapping pmap = create() ;
        String x = "scheme:i_do_not_exist" ;
        
        assertEquals(x, pmap.expandPrefix(x)) ;
        // Call again - used to cause problems. 
        assertEquals(x, pmap.expandPrefix(x)) ;
    }
    
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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