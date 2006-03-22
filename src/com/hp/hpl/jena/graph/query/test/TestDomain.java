/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestDomain.java,v 1.2 2006-03-22 13:53:36 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import java.util.*;

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
        List L = new ArrayList();
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


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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