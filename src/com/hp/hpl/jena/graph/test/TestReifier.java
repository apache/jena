/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestReifier.java,v 1.2 2003-01-28 16:20:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import junit.framework.*;

/**
	@author kers
*/

public class TestReifier extends GraphTestBase
    {
    public TestReifier( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestReifier.class ); }   
                
    public void testEmptyReifiers()
        {
        assertEquals( "no reified triples", 0, graphWith( "x R y" ).getReifier().getReifiedTriples().size() );
        assertEquals( "no reified triples", 0, graphWith( "x R y; p S q" ).getReifier().getReifiedTriples().size() );
        }
        
    public void testSameReifier()
        {
        Graph G = graphWith( "" );
        Reifier R1 = G.getReifier();
        G.add( triple( "x R y" ) );
        assertTrue( "same reifier", R1 == G.getReifier() );
        }
        
    public void testParent()
        {
        Graph G = graphWith( "" ), H = graphWith( "" );
        assertTrue( "correct reifier (G)", G == G.getReifier().getParentGraph() );
        assertTrue( "correct reifier (H)", H == H.getReifier().getParentGraph() );
        }
        
    public void testInsertTriples()
        {
        Graph G = graphWith( "" );
        Reifier R = G.getReifier();
        Node N = R.reify( triple( "x R y" ) );
        assertTrue( "auto-allocated node must be blank", N.isBlank() );
        assertTrue( "has one triple", graphWith( "x R y" ).isIsomorphicWith(R.getReifiedTriples()) );
        Node M = R.reify( triple( "p S q" ) );
        assertTrue( "auto-allocated node must be blank", M.isBlank() );
        assertTrue( "has two triples", graphWith( "x R y; p S q" ).isIsomorphicWith(R.getReifiedTriples()) );
        assertTrue( "old graph still empty", G.isIsomorphicWith(graphWith( "" ) ) );
        }
        
    public void testRetrieveTriples()
        {
        Graph G = graphWith( "" );
        Reifier R = G.getReifier();
        Node N = R.reify( triple( "x R y" ) );
        Triple T = R.getTriple( N );
        assertEquals( "gets correct triple", triple( "x R y" ), T );
        Node M = R.reify( triple( "p S q" ) );
        assertDiffer( "different triples need different tags", N, M );
        assertEquals( "gets correct triple", triple( "p S q" ), R.getTriple( M ) );
    /* */
    	assertTrue( "node is known bound", R.hasTriple( M ) );
    	assertTrue( "node is known bound", R.hasTriple( N ) );
    	assertFalse( "node is known unbound", R.hasTriple( Node.ANY ) );
    	assertFalse( "node is known unbound", R.hasTriple( Node.makeURI( "any:thing" ) ) );
    /* */
    	Graph GR = R.getReifiedTriples();
    	assertTrue( "reified triples", graphWith( "x R y; p S q" ).isIsomorphicWith(GR) );
    	assertTrue( "untouched graph", graphWith( "" ).isIsomorphicWith(G) );
        }
        
    public void testReifyAs()
    	{
    	Graph G = graphWith( "" );
    	Reifier R = G.getReifier();
    	Node X = Node.makeURI( "some:uri" );
    	assertEquals( "node used", X, R.reifyAs( X, triple( "x R y" ) ) );
    	assertEquals( "retrieves correctly", triple( "x R y" ), R.getTriple( X ) );
    	}
    	
    public void testAllNodes()
        {
        Reifier R = graphWith( "" ).getReifier();
        R.reifyAs( node("x"), triple( "cows eat grass" ) );
        R.reifyAs( node("y"), triple( "pigs can fly" ) );
        R.reifyAs( node("z"), triple( "dogs may bark" ) );
        assertEquals( "", nodeSet( "z y x" ), iteratorToSet( R.allNodes() ) );
        }
        
 	public void testRemove()
 		{
 		Graph G = graphWith( "" );
 		Reifier R = G.getReifier();
 		Node X = node( "x" ), Y = node( "y" );
 		R.reifyAs( X, triple( "x R a" ) );
 		R.reifyAs( Y, triple( "y R a" ) );
 		R.remove( X );
 		assertFalse( "triple X has gone", R.hasTriple( X ) );
 		assertEquals( "triple Y still there", triple( "y R a" ), R.getTriple( Y ) );
 		}
        
    public void testException()
        {
        Graph G = graphWith( "" );
        Reifier R = G.getReifier();
        Node X = node( "x" );
        R.reifyAs( X, triple( "x R y" ) );
        R.reifyAs( X, triple( "x R y" ) );
        try { R.reifyAs( X, triple( "x R z" ) ); fail( "did not detected already reified node" ); }
        catch (Reifier.AlreadyReifiedException e) { }      
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
