/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import atlas.lib.FileOps;
import org.junit.Test;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.tdb.ConfigTest;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetPrefixes;

public class TestPrefixMappingTDB extends TestPrefixMapping2
{
    static DatasetPrefixes datasetPrefixes = DatasetPrefixes.testing() ; 
    
    @Override
    protected PrefixMapping create()
    {
        return datasetPrefixes.getPrefixMapping() ;
    }

    @Test public void multiple1()
    {
        DatasetPrefixes prefixes = DatasetPrefixes.testing() ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping() ;
        PrefixMapping pmap2 = prefixes.getPrefixMapping("http://graph/") ;
        pmap1.setNsPrefix("x", "http://foo/") ;
        assertNull(pmap2.getNsPrefixURI("x")) ;
        assertNotNull(pmap1.getNsPrefixURI("x")) ;
    }
    
    @Test public void multiple2()
    {
        DatasetPrefixes prefixes = DatasetPrefixes.testing() ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping("http://graph/") ;  // Same
        PrefixMapping pmap2 = prefixes.getPrefixMapping("http://graph/") ;
        pmap1.setNsPrefix("x", "http://foo/") ;
        assertNotNull(pmap2.getNsPrefixURI("x")) ;
        assertNotNull(pmap1.getNsPrefixURI("x")) ;
    }
    
    // Persistent.
    @Test public void persistent1()
    {
        String dir = ConfigTest.getTestingDir() ;
        FileOps.clearDirectory(dir) ;
        
        DatasetPrefixes prefixes = DatasetPrefixes.create(new Location(dir)) ;
        PrefixMapping pmap1 = prefixes.getPrefixMapping() ;
        pmap1.setNsPrefix("x", "http://foo/") ;
        prefixes.close() ;
        
        // This fails when run in the complete test suite, but not run individually. 
        prefixes = DatasetPrefixes.create(new Location(dir)) ;
        assertEquals("http://foo/", pmap1.getNsPrefixURI("x")) ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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