/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestReifier.java,v 1.5 2003-05-10 15:56:39 wkw Exp $
*/

package com.hp.hpl.jena.db.test;

import java.util.ArrayList;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.*;
import junit.framework.*;

/**
	@author kers, modified to work with GraphRDB by csayers.
*/

public class TestReifier extends GraphTestBase {
	private ArrayList theGraphs = new ArrayList();
	private IDBConnection theConnection;

	public TestReifier(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestReifier.class);
	}


	public void setUp() {
		theConnection = TestConnection.makeAndCleanTestConnection();
	}

	public void tearDown() {
		try {
			theConnection.cleanDB();
			theGraphs.clear();
			theConnection.close();
		} catch (Exception e) { throw new RuntimeException(e.toString());}
	}

	public Graph getGraph() {
		// in the following, we specify the same reification behaviour as the
		// current GraphMem (so we can copy the GraphMem reification test code).
		GraphRDB g = new GraphRDB(theConnection, "name"+theGraphs.size(), 
				theConnection.getDefaultModelProperties().getGraph(), 
				GraphRDB.OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS, true);
		theGraphs.add(g);
		return g;		
	}
	
    public void testEmptyReifiers()
        {
        assertEquals( "no reified triples", 0, graphWith( "x R y" ).getReifier().getHiddenTriples().size() );
        assertEquals( "no reified triples", 0, graphWith( "x R y; p S q" ).getReifier().getHiddenTriples().size() );
        }
        
    public void testSameReifier()
        {
        Graph G = getGraph();
        Reifier R1 = G.getReifier();
        G.add( triple( "x R y" ) );
        assertTrue( "same reifier", R1 == G.getReifier() );
        }
        
    public void testParent()
        {
        Graph G = getGraph(), H = getGraph();
        assertTrue( "correct reifier (G)", G == G.getReifier().getParentGraph() );
        assertTrue( "correct reifier (H)", H == H.getReifier().getParentGraph() );
        }
        
    public void testIntercept()
        {
        Graph g = getGraph();
        Reifier r = g.getReifier();
        Node S = node( "sub" ), O = node( "obj" );
        Node RS = node( "http://example.org/type" );
    /* */
        assertFalse( "reifier must not intercept quadlet", r.handledAdd( new Triple( S, Reifier.type,  RS )  ) );
        assertFalse( "reifier must not intercept quadlet", r.handledAdd( new Triple( S, S,  Reifier.subject )  ) );
        assertFalse( "reifier must not intercept quadlet", r.handledAdd( new Triple( S, S,  Reifier.type )  ) );
    /* */
        assertTrue( "reifier must intercept quadlet", r.handledAdd( new Triple( S, Reifier.predicate, O ) ) );
        assertTrue( "reifier must intercept quadlet", r.handledAdd( new Triple( S, Reifier.type,  Reifier.Statement )  ) );
        }
        
    public void testHiddenTriples()
        {
        Graph g =  getGraph() ;
        Reifier r = g.getReifier();
        Node S = node( "SSS" ), P = node( "PPP" ), O = node( "OOO " );
        g.add( new Triple( S, Reifier.predicate, P ) );
        assertEquals( "graph must still be empty", 0, g.size() );
        assertEquals( "reifier must have the triple", 1, r.getHiddenTriples().size() );
        assertContains( "xxx", "SSS rdf:predicate PPP", r.getHiddenTriples() );
        g.add( new Triple( S, Reifier.subject, S) );
        assertContains( "xxx", "SSS rdf:subject SSS", r.getHiddenTriples() );
        }
               
    public void testRetrieveTriplesByNode()
        {
        Graph G = getGraph();
        Reifier R = G.getReifier();
        Node N = Node.createAnon(), M = Node.createAnon();
        R.reifyAs( N, triple( "x R y" ) );
        assertEquals( "gets correct triple", triple( "x R y" ), R.getTriple( N ) );
        R.reifyAs( M, triple( "p S q" ) );
        assertDiffer( "the anon nodes must be distinct", N, M );
        assertEquals( "gets correct triple", triple( "p S q" ), R.getTriple( M ) );
    /* */
    	assertTrue( "node is known bound", R.hasTriple( M ) );
    	assertTrue( "node is known bound", R.hasTriple( N ) );
    	// TODO check if this is valid use of Node.ANY in find
//    	assertFalse( "node is known unbound", R.hasTriple( Node.ANY ) );
    	assertFalse( "node is known unbound", R.hasTriple( Node.createURI( "any:thing" ) ) );
    /* */
//    	Graph GR = R.getReifiedTriples();
//    	assertTrue( "reified triples", graphWith( "x R y; p S q" ).isIsomorphicWith(GR) );
//    	assertTrue( "untouched graph", getGraph().isIsomorphicWith(G) );
        }
        
    public void testRetrieveTriplesByTriple()
        {
        Graph G = getGraph();
        Reifier R = G.getReifier();
        Triple T = triple( "x R y" ), T2 = triple( "y R x" );
        Node N = node( "someNode" );
        R.reifyAs( N, T );
        assertTrue( "R must have T", R.hasTriple( T ) );
        assertFalse( "R must not have T2", R.hasTriple( T2 ) );
        }   
             
    public void testReifyAs()
    	{
    	Graph G = getGraph();
    	Reifier R = G.getReifier();
    	Node X = Node.createURI( "some:uri" );
    	assertEquals( "node used", X, R.reifyAs( X, triple( "x R y" ) ) );
    	assertEquals( "retrieves correctly", triple( "x R y" ), R.getTriple( X ) );
    	}
    	
    public void testAllNodes()
        {
        Reifier R = getGraph().getReifier();
        R.reifyAs( node("x"), triple( "cows eat grass" ) );
        R.reifyAs( node("y"), triple( "pigs can fly" ) );
        R.reifyAs( node("z"), triple( "dogs may bark" ) );
        assertEquals( "", nodeSet( "z y x" ), iteratorToSet( R.allNodes() ) );
        }
        
 	public void testRemoveByNode()
 		{
 		Graph G = getGraph();
 		Reifier R = G.getReifier();
 		Node X = node( "x" ), Y = node( "y" );
 		R.reifyAs( X, triple( "x R a" ) );
 		R.reifyAs( Y, triple( "y R a" ) );
 		R.remove( X, triple( "x R a" ) );
 		assertFalse( "triple X has gone", R.hasTriple( X ) );
 		assertEquals( "triple Y still there", triple( "y R a" ), R.getTriple( Y ) );
 		}
        
//    public void testRemoveByTriple()
//        {
//        Graph G = getGraph();
//        Reifier R = G.getReifier();
//        Node X = node( "x" ), Y = node( "y" );
//        R.reifyAs( X, triple( "x R a" ) );
//        R.reifyAs( Y, triple( "y R a" ) );
//        R.remove( triple( "x R a" ) );
//        assertFalse( "triple X has gone", R.hasTriple( X ) );
//        assertEquals( "triple Y still there", triple( "y R a" ), R.getTriple( Y ) );            
//        }
        
    public void testException()
        {
        Graph G = getGraph();
        Reifier R = G.getReifier();
        Node X = node( "x" );
        R.reifyAs( X, triple( "x R y" ) );
        // TODO  should this throw an already reified exception?
//        R.reifyAs( X, triple( "x R y" ) );
        try { R.reifyAs( X, triple( "x R z" ) ); fail( "did not detected already reified node" ); }
        catch (Reifier.AlreadyReifiedException e) { }      
        }
        
//    public void testQuads()
//        {
//        Node A = node( "a" ), B = node( "b" );
//        Graph G = getGraph();
//        Graph quads = graphWith
//            ( 
//            "a " + RDF.type + " " + RDF.Statement
//            + "; a " + RDF.subject + " x"
//            + "; a " + RDF.predicate + " R"
//            + "; a " + RDF.object + " y"
//            + "; b " + RDF.type + " " + RDF.Statement
//            + "; b " + RDF.subject + " p"
//            + "; b " + RDF.predicate + " S"
//            + "; b " + RDF.object + " q"
//            );
//        Reifier R = G.getReifier();
//        R.reifyAs( A, triple( "x R y") );
//        R.reifyAs( B, triple( "p S q" ) );
//        assertEquals( "same", quads, R.getReificationQuads() );
//        }
        
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
