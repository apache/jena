/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util.test;

import junit.framework.*;
import org.apache.commons.logging.*;

import com.hp.hpl.jena.util.LocationMapper;

/** com.hp.hpl.jena.brql.util.test.TestFileManager
 * 
 * @author Andy Seaborne
 * @version $Id: TestLocationMapper.java,v 1.3 2005-02-21 12:19:22 andy_seaborne Exp $
 */

public class TestLocationMapper extends TestCase
{
    static Log log = LogFactory.getLog(TestLocationMapper.class) ;
    static final String testingDir = TestFileManager.testingDir ;
    static final String filename1 = "file:test" ; 
    static final String notFilename = "zzzz" ;
    static final String filename2 = "file:"+testingDir+"/location-mapping-test-file" ;
    static final String mapping = "location-mapping-test.n3;"+
                                  testingDir+"/location-mapping-test.n3" ;

    
    public TestLocationMapper( String name )
    {
        super(name);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite( TestLocationMapper.class );
    }

    public void testLocationMapping()
    {
        LocationMapper locMap = new LocationMapper(mapping) ;
        String alt = locMap.altMapping(filename1) ;
        assertNotNull(alt) ;
        assertEquals(alt, filename2) ;
    }

    public void testLocationMappingMiss()
    {
        LocationMapper locMap = new LocationMapper(mapping) ;
        String alt = locMap.altMapping(notFilename) ;
        assertNotNull(alt) ;
        assertEquals(alt, notFilename) ;
    }

    public void testLocationMappingURLtoFile()
    {
        LocationMapper locMap = new LocationMapper(mapping) ;
        String alt = locMap.altMapping("http://example.org/file") ;
        assertNotNull(alt) ;
        assertEquals(alt, "file:"+testingDir+"/location-mapping-test-file") ;
    }
    
}

/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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