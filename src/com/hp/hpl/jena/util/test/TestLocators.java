/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestLocators.java,v 1.2 2007-01-02 11:53:25 andy_seaborne Exp $
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

