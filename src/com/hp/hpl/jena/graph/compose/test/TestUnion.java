/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestUnion.java,v 1.2 2003-04-04 11:31:08 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Union;


import junit.framework.*;

/**
	@author kers.
*/

public class TestUnion extends GraphTestBase 
	{
	public TestUnion( String name )
		{ super( name ); }
			
	public static TestSuite suite()
    	{ return new TestSuite( TestUnion.class ); }
    	
	public void testUnion() 
		{
        Graph g1 = graphWith( "x R y; p R q" );
        Graph g2 = graphWith( "r A s; x R y" );
        Union u = new Union( g1, g2 );
        assertContains( "Union", "x R y", u );
        assertContains( "Union", "p R q", u );
        assertContains( "Union", "r A s", u );
        if (u.size() != 3)
            fail( "oops: size of union is not 3" );
        u.add( triple( "cats eat cheese" ) );
        assertContains( "Union", "cats eat cheese", u );
        if 
        	(
        	contains( g1, "cats eat cheese" ) == false
        	&& contains( g2, "cats eat cheese" ) == false
        	)
            fail( "oops: neither g1 nor g2 contains `cats eat cheese`" );
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
