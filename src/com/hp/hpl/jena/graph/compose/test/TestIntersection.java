/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestIntersection.java,v 1.1 2003-03-04 17:51:44 ian_dickinson Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Intersection;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.*;

import junit.framework.*;

public class TestIntersection extends GraphTestBase 
	{
	public TestIntersection( String name )
		{ super( name ); }
		
	public static TestSuite suite()
    	{ return new TestSuite( TestIntersection.class ); }	
    	
	public void testIntersection()
		{
        Graph g1 = graphWith( "x R y; p R q" );
        Graph g2 = graphWith( "r A s; x R y" );
        Intersection i = new Intersection( g1, g2 );
        assertContains( "Intersection", i, "x R y" );
 		assertOmits( "Intersection", i, "p R q" );
		assertOmits( "Intersection", i, "r A s" );
        if (i.size() != 1)
            fail( "oops: size of intersection is not 1" );
        i.add( triple( "cats eat cheese" ) );
        assertContains( "Intersection.L", g1, "cats eat cheese" );
        assertContains( "Intersection.R", g2, "cats eat cheese" );
    /* */
    	Graph L = graphWith( "a pings b; b pings c; c pings a" );
    	Graph R = graphWith( "c pings a; b pings c; x captures y" );
    	Graph join = new Intersection( L, R );
    	Model mJoin = new ModelMem( join );
    	Model mL = new ModelMem( L );
    	mL.remove( mJoin );
    	// mustHaveNone( L, "L after removal", "c pings a; b pings c; x captures y" );
    	// mustHave( L, "L after removal", "a pings b" );
    	if (!new ModelMem( R ).isIsomorphicWith( (Model)new ModelMem( graphWith( "c pings a; b pings c; x captures y" ) ) ))
    		{
    		show( "oops: R has changed", R );
    		fail( "" );
    		}
    	if (!mL.isIsomorphicWith( (Model)new ModelMem( graphWith( "a pings b" ) ) ))
    		{
    		show( "oops: L is", L );
    		fail( "oops: mL should be `a pings b`" );
    		}
		}
	}
/*
    (c) Copyright Hewlett-Packard Company 2002
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
