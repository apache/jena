/*
  (c) Copyright 2002, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestDyadic.java,v 1.7 2004-11-01 16:38:26 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Dyadic;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.mem.GraphMem;


import java.util.*;
import junit.framework.*;

/**
	@author kers
*/
public class TestDyadic extends GraphTestBase
	{
	public TestDyadic( String name )
		{ super( name ); }
		
	public static TestSuite suite()
    	{ return new TestSuite( TestDyadic.class ); }
    	
	static private ExtendedIterator things( final String x ) 
		{
		return new NiceIterator()
			{
			private StringTokenizer tokens = new StringTokenizer( x );
			public boolean hasNext() { return tokens.hasMoreTokens(); }
			public Object next() { return tokens.nextToken(); }
			};
		}
		
	public void testDyadic() 
		{
		ExtendedIterator it1 = things( "now is the time" );
		ExtendedIterator it2 = things( "now is the time" );
		ExtendedIterator mt1 = things( "" );
		ExtendedIterator mt2 = things( "" );
		assertEquals( "mt1.hasNext()", false, mt1.hasNext() );
		assertEquals( "mt2.hasNext()", false, mt2.hasNext() );
		assertEquals( "andThen(mt1,mt2).hasNext()", false, mt1.andThen( mt2 ).hasNext() ); 		
		assertEquals( "butNot(it1,it2).hasNext()", false, Dyadic.butNot( it1, it2 ).hasNext() );
		assertEquals( "x y z @butNot z", true, Dyadic.butNot( things( "x y z" ), things( "z" ) ).hasNext() );
		assertEquals( "x y z @butNot a", true, Dyadic.butNot( things( "x y z" ), things( "z" ) ).hasNext() );
		}
    
    public void testDyadicOperands()
        {
        Graph g = new GraphMem(), h = new GraphMem();
        Dyadic d = new Dyadic( g, h )
            {
            public ExtendedIterator graphBaseFind( TripleMatch m ) { return null; }
            };
        assertSame( g, d.getL() );
        assertSame( h, d.getR() );
        }
	}

/*
    (c) Copyright 2002, 2004 Hewlett-Packard Development Company, LP
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
