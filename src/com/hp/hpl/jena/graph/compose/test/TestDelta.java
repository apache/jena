/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestDelta.java,v 1.4 2003-08-27 13:01:01 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Delta;
import com.hp.hpl.jena.graph.test.*;

import junit.framework.*;

/**
	@author kers
*/
public class TestDelta extends GraphTestBase 
	{
		
	public TestDelta( String name )
		{ super( name ); }
		
	public static TestSuite suite()
    	{ return new TestSuite( TestDelta.class ); }			
    	
	public void testDelta() 
		{
		Graph x = graphWith( "x R y" );
		assertContains( "x", "x R y", x );
		x.delete( triple( "x R y" ) );
		assertOmits( "x", x, "x R y" );
	/* */	
		Graph base = graphWith( "x R y; p S q; I like cheese; pins pop balloons" );
		Graph save = graphWith( "x R y; p S q; I like cheese; pins pop balloons" );
		Delta delta = new Delta( base );
		assertContainsAll( "Delta", delta, "x R y; p S q; I like cheese; pins pop balloons" );
		assertContainsAll( "Delta", base, "x R y; p S q; I like cheese; pins pop balloons" );
	/* */
		delta.add( triple( "pigs fly winglessly" ) );
		delta.delete( triple( "I like cheese" ) );
	/* */
		assertContainsAll( "changed Delta", delta, "x R y; p S q; pins pop balloons; pigs fly winglessly" );
		assertOmits( "changed delta", delta, "I like cheese" );
		assertContains( "delta additions", "pigs fly winglessly", delta.getAdditions() );
		assertOmits( "delta additions", delta.getAdditions(), "I like cheese" );
		assertContains( "delta deletions", "I like cheese", delta.getDeletions() );
		assertOmits( "delta deletions", delta.getDeletions(), "pigs fly winglessly" );
		}
	}

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
