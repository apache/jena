/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.util;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

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
