/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestRDFClosure.java,v 1.1 2003-02-21 15:45:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;
import junit.framework.*;

/**
	@author kers
*/

public class TestRDFClosure extends GraphTestBase 
	{
	public TestRDFClosure( String name )
		{ super( name ); }
		
	public static TestSuite suite()
    	{ return new TestSuite( TestRDFClosure.class ); }	
			
	public void testRDFClosure()
		{
		Graph g = new RDFClosure( graphWith( "x R y; a S b; c R d" ) );
		assertContains( "RDFClosure", g, "x R y" );
		assertContains( "RDFClosure", g, "a S b" );
		assertContains( "RDFClosure", g, "R rdf:type rdf:Property" );
		assertContains( "RDFClosure", g, "rdf:type rdf:type rdf:Property" );
		assertOmits( "RDFClosure", g, "x S y" );
		assertOmits( "RDFClosure", g, "x rdf:type rdf:Property" );
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
