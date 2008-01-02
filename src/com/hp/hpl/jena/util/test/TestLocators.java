/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestLocators.java,v 1.3 2008-01-02 12:08:35 andy_seaborne Exp $
*/

package com.hp.hpl.jena.util.test;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.*;

public class TestLocators extends ModelTestBase
    {
    private static final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    private static final ClassLoader otherClassLoader = new ClassLoader() {};

    public TestLocators( String name )
        { super( name ); }
    
    public void testClassLoaderLocatorEquality()
        {
        Locator A1 = new LocatorClassLoader( systemClassLoader );
        Locator A2 = new LocatorClassLoader( systemClassLoader );
        Locator B = new LocatorClassLoader( otherClassLoader );
        testLocatorEquality( A1, A2, B );
        }

    /**
        A1 and A2 should be equal, but both different from B.
    */
    private void testLocatorEquality( Locator A1, Locator A2, Locator B )
        {
        assertEquals( A1, A1 );
        assertEquals( A2, A2 );
        assertEquals( A1, A2 );
        assertEquals( A2, A1 );
        assertEquals( B, B );
        assertDiffer( A1, B );
        assertDiffer( B, A1 );
        }
    
    public void testClassLoaderLocatorHashcode()
        {
        assertEquals( systemClassLoader.hashCode(), new LocatorClassLoader( systemClassLoader ).hashCode() );
        assertEquals( otherClassLoader.hashCode(), new LocatorClassLoader( otherClassLoader ).hashCode() );
        }
    
    public void testLocatorFileEquality()
        {
        Locator A1 = new LocatorFile( "foo/bar" );
        Locator A2 = new LocatorFile( "foo/bar" );
        Locator B = new LocatorFile( "bill/ben" );
        testLocatorEquality( A1, A2, B );
        }
    
    public void testLocatorFileHashcode()
        {
        testLocatorFileHashCode( "foo/bar" );
        testLocatorFileHashCode( "bill/ben" );
        testLocatorFileHashCode( "another/night" );
        }

    private void testLocatorFileHashCode( String dirName )
        {
        assertEquals( dirName.hashCode(), new LocatorFile( dirName ).hashCode() );
        }
    
    public void testLocatorURLEquality()
        {
        Locator A1 = new LocatorURL();
        Locator A2 = new LocatorURL();
        assertEquals( A1, A2 );
        assertDiffer( A1, "" );
        }
    
    /**
        There all equal. Pick a value that will at least be discriminating among
        other types (so `0` isn't a good answer).
     */
    public void testLocatorURLHashcode()
        {
        assertEquals( LocatorURL.class.hashCode(), new LocatorURL().hashCode() );
        }
    }

/*
 *  (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
