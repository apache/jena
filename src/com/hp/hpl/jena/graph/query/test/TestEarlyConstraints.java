/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestEarlyConstraints.java,v 1.4 2005-02-21 11:52:33 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

/**
	TestEarlyConstraints

	@author kers
*/
public class TestEarlyConstraints extends QueryTestBase 
    {
	public TestEarlyConstraints(String name)
        { super( name ); }
        
	public static TestSuite suite()
        { return new TestSuite( TestEarlyConstraints.class ); }
        
    public void testEarlyConstraint()
        {
        final int [] count = {0};
        Query q = new Query()
            .addMatch( Query.S, node( "eg:p1" ), Query.O )
            .addMatch( Query.X, node( "eg:p2" ), Query.Y )
            .addConstraint( notEqual( Query.S, Query.O ) )
            ;
        Graph gBase = graphWith( "a eg:p1 a; c eg:p1 d; x eg:p2 y" );
        Graph g = new WrappedGraph( gBase )
            {
            public QueryHandler queryHandler()
                { return new SimpleQueryHandler( this ); }
            
            public ExtendedIterator find( TripleMatch tm ) 
                {
                if (tm.getMatchPredicate().equals( node( "eg:p2" ) )) count[0] += 1;
                return super.find( tm ); 
                }
            };
        Set s = iteratorToSet( q.executeBindings( g, new Node[] {Query.S} ) .mapWith ( getFirst ) );
        assertEquals( nodeSet( "c" ), s );
        assertEquals( 1, count[0] );
        }
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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