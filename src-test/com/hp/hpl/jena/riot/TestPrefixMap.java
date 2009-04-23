/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.riot.PrefixMap;

import org.junit.Test;
import atlas.test.BaseTest;

public class TestPrefixMap extends BaseTest
{
    static IRIFactory factory = IRIFactory.iriImplementation() ; 
    
    @Test public void prefixMap1()
    {
        PrefixMap pmap = new PrefixMap() ;
        add(pmap, "", "http://example/") ;
        String x = pmap.expand("", "x") ;
        assertEquals("http://example/x", x) ;
    }

    @Test public void prefixMap2()
    {
        PrefixMap pmap = new PrefixMap() ;
        add(pmap, "ex", "http://example/") ;
        String x = pmap.expand("", "x") ;
        assertNull(x) ;
    }
    
    @Test public void prefixMap3()
    {
        PrefixMap pmap = new PrefixMap() ;
        add(pmap, "ex", "http://example/") ;
        add(pmap, "ex", "http://elsewhere/ns#") ;
        String x = pmap.expand("ex", "x") ;
        assertEquals("http://elsewhere/ns#x", x) ;
    }

    // PrefixMap2
    
    static void add(PrefixMap pmap, String prefix, String uri)
    {
        pmap.add(prefix, factory.create(uri)) ; 
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