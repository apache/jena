/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestDisjointUnion.java,v 1.3 2004-12-01 07:22:22 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.compose.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.DisjointUnion;
import com.hp.hpl.jena.graph.test.GraphTestBase;

/**
     TestDisjointUnion - test that DisjointUnion works, as well as we can.
     @author kers
*/
public class TestDisjointUnion extends GraphTestBase
    {
    public TestDisjointUnion( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestDisjointUnion.class ); }
    
    public void testEmptyUnion()
        { 
        DisjointUnion du = new DisjointUnion( Graph.emptyGraph, Graph.emptyGraph );
        assertEquals( true, du.isEmpty() );
        }
    
    public void testLeftUnion()
        {
        Graph g = graphWith( "" );
        testSingleComponent( g, new DisjointUnion( g, Graph.emptyGraph ) );
        }
    
    public void testRightUnion()
        {
        Graph g = graphWith( "" );
        testSingleComponent( g, new DisjointUnion( Graph.emptyGraph, g ) );
        }

    protected void testSingleComponent( Graph g, DisjointUnion du )
        {
        graphAdd( g, "x R y; a P b; x Q b" );
        assertIsomorphic( g, du );
        graphAdd( g, "roses growOn you" );
        assertIsomorphic( g, du );
        g.delete( triple( "a P b" ) );
        assertIsomorphic( g, du );
        }
    
    public void testBothComponents()
        {
        Graph L = graphWith( "" ), R = graphWith( "" );
        Graph du = new DisjointUnion( L, R );
        assertIsomorphic( Graph.emptyGraph, du );
        L.add( triple( "x P y" ) );
        assertIsomorphic( graphWith( "x P y" ), du );
        R.add( triple( "A rdf:type Route" ) );
        assertIsomorphic( graphWith( "x P y; A rdf:type Route" ), du );
        }
    
    public void testRemoveBoth()
        {
        Graph L = graphWith( "x R y; a P b" ), R = graphWith( "x R y; p Q r" );
        Graph du = new DisjointUnion( L, R );
        du.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "a P b" ), L );
        assertIsomorphic( graphWith( "p Q r" ), R );
        }
    
    public void testAddLeftOnlyIfNecessary()
        {
        Graph L = graphWith( "" ), R = graphWith( "x R y" );
        Graph du = new DisjointUnion( L, R );
        graphAdd( du, "x R y" );
        assertEquals( true, L.isEmpty() );
        graphAdd( du, " a P b" );
        assertIsomorphic( graphWith( "a P b" ), L );
        assertIsomorphic( graphWith( "x R y" ), R );
        }
    }


/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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