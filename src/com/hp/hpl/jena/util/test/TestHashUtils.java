/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestHashUtils.java,v 1.1 2004-06-30 12:58:02 chris-dollin Exp $
*/
package com.hp.hpl.jena.util.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.HashUtils;

/**
 	TestHashUtils - test that the hash utility returns a map.
 	@author kers
*/
public class TestHashUtils extends ModelTestBase
    {
    public TestHashUtils( String name )
    	{ super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestHashUtils.class ); }

    public void testHashMapExists()
        {
        Map map = HashUtils.createMap();
        assertTrue( map instanceof Map );
        }
    
    public void testHashMapSized()
        {
        Map map = HashUtils.createMap( 42 );
        assertTrue( map instanceof Map );
        }
    
    public void testHashMapCopy()
        {
        Map map = new HashMap();
        map.put( "here", "Bristol" );
        map.put( "there", "Oxford" );
        Map copy = HashUtils.createMap( map );
        assertEquals( map, copy );
        }
    
    public void testHashSetExists()
        {
        Set set = HashUtils.createSet();
        assertTrue( set instanceof Set );
        }
    
    public void testHashSetCopy()
        {
        Set s = new HashSet();
        s.add( "jelly" );
        s.add( "concrete" );
        Set copy = HashUtils.createSet( s );
        assertEquals( s, copy );
        }
    }


/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
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