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

package com.hp.hpl.jena.graph.query.test;

import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.Domain;

import junit.framework.*;

/**
    Post-hoc tests for Domain, added because Domain::equals was broken: it
    didn't work for comparing against a non-Domain List.
    @author kers
*/
public class TestDomain extends QueryTestBase
    {
    public TestDomain( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestDomain.class ); }

    public void testDomainGet()
        {
        Domain d = domain( "a 'b' 17 _x" );
        assertEquals( node( "a" ), d.get( 0 ) );
        assertEquals( node( "'b'" ), d.get( 1 ) );
        assertEquals( node( "17" ), d.get( 2 ) );
        assertEquals( node( "_x" ), d.get( 3 ) );
        }
    
    public void testDomainGetElement()
        {
        Domain d = domain( "X 'why' 42 _z9m9z" );
        assertEquals( node( "X" ), d.getElement( 0 ) );
        assertEquals( node( "'why'" ), d.getElement( 1 ) );
        assertEquals( node( "42" ), d.getElement( 2 ) );
        assertEquals( node( "_z9m9z" ), d.getElement( 3 ) );
        }
    
    public void testSetElement()
        {
        Domain d = domain( "A B C D" );
        d.setElement( 0, node( "X" ) );
        assertEquals( node( "X" ), d.getElement( 0 ) );
        d.setElement( 2, node( "Z" ) );
        assertEquals( node( "Z" ), d.getElement( 2 ) );
        assertEquals( node( "X" ), d.getElement( 0 ) );
        }
    
    public void testEqualsList()
        {
        Domain d = new Domain( 2 );
        List<Node> L = new ArrayList<Node>();
        d.setElement( 0, node( "a" ) ); L.add( node( "a" ) );
        d.setElement( 1, node( "b" ) ); L.add( node( "b" ) );
        assertEquals( L, d );
        assertEquals( d, L );
        }
    
    public void testSize()
        {
        assertEquals( 0, domain( "" ).size() );
        assertEquals( 1, domain( "X" ).size() );
        assertEquals( 5, domain( "a song in the wind" ).size() );
        }
    
    public void testCopiesDistinctButEqual()
        {
        Domain d = domain( "a lot of bottle" );
        assertNotSame( d, d.copy() );
        assertEquals( d, d.copy() );
        }

    private Domain domain( String string )
        { return new Domain( nodeArray( string ) ); }
    }
