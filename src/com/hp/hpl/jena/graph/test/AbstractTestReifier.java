/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: AbstractTestReifier.java,v 1.20 2004-11-04 15:05:38 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.db.impl.DBReifier;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Abstract base class for reification tests. 
 	@author kers
*/
public abstract class AbstractTestReifier extends GraphTestBase
    {
    protected static final ReificationStyle Minimal = ReificationStyle.Minimal;
    protected static final ReificationStyle Standard = ReificationStyle.Standard;
    protected static final ReificationStyle Convenient = ReificationStyle.Convenient;

    protected static final Triple ALL = Triple.create( "?? ?? ??" );
    
    public AbstractTestReifier(String name)
        { super(name); }
        
    public Graph getGraph()
        { return getGraph( Minimal ); }
    
    public abstract Graph getGraph( ReificationStyle style );

    protected final Graph getGraphWith( String facts )
        {
        Graph result = getGraph();
        graphAdd( result, facts );
        return result;
        }
        
    /**
        Answer the empty graph if cond is false, otherwise the graph with the given facts.
    */
    protected final Graph graphWithUnless( boolean cond, String facts )
        { return graphWith( cond ? "" : facts ); }
          
    protected final Graph graphWithIf( boolean cond, String facts )
        { return graphWithUnless( !cond, facts ); }
            
    public void testStyle()
        {
        assertSame( Minimal, getGraph( Minimal ).getReifier().getStyle() );    
        assertSame( Standard, getGraph( Standard ).getReifier().getStyle() );    
        assertSame( Convenient, getGraph( Convenient ).getReifier().getStyle() );    
        }
        
    public void testEmptyReifiers()
        {
        assertFalse( getGraphWith( "x R y" ).getReifier().findExposed( ALL ).hasNext() );
        assertFalse( getGraphWith( "x R y; p S q" ).getReifier().findExposed( ALL ).hasNext() );
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
        Graph g = getGraph( Convenient );
        Reifier r = g.getReifier();
        Node S = node( "sub" ), O = node( "obj" );
        Node RS = node( "http://example.org/type" );
    /* */
        assertFalse( "reifier must not intercept quadlet", r.handledAdd( Triple.create( S, RDF.Nodes.type,  RS )  ) );
        assertFalse( "reifier must not intercept quadlet", r.handledAdd( Triple.create( S, S,  RDF.Nodes.subject )  ) );
        assertFalse( "reifier must not intercept quadlet", r.handledAdd( Triple.create( S, S,  RDF.Nodes.type )  ) );
    /* */
        assertTrue( "reifier must intercept quadlet", r.handledAdd( Triple.create( S, RDF.Nodes.predicate, O ) ) );
        assertTrue( "reifier must intercept quadlet", r.handledAdd( Triple.create( S, RDF.Nodes.type,  RDF.Nodes.Statement )  ) );
        }

    /**
        Check that the standard reifier will note, but not hide, reification quads.
    */
    public void testStandard()
        {
        Graph g = getGraph( Standard );
        assertFalse( g.getReifier().hasTriple( triple( "s p o" ) ) );
        g.add( Triple.create( "x rdf:subject s" ) );
        assertEquals( 1, g.size() );
        g.add( Triple.create( "x rdf:predicate p" ) );
        assertEquals( 2, g.size() );  
        g.add( Triple.create( "x rdf:object o" ) );
        assertEquals( 3, g.size() );            
        g.add( Triple.create( "x rdf:type rdf:Statement" ) );
        assertEquals( 4, g.size() );
        assertTrue( g.getReifier().hasTriple( triple( "s p o" ) ) );                      
        }
        
    /**
        Test that the Standard reifier will expose implicit quads arising from reifyAs().
    */
    public void testStandardExplode()
        {
        Graph g = getGraph( Standard );
        g.getReifier().reifyAs( node( "a" ), triple( "p Q r" ) );
        Graph r = Factory.createDefaultGraph( Minimal );
        graphAdd( r, "a rdf:type rdf:Statement; a rdf:subject p; a rdf:predicate Q; a rdf:object r" );
        assertEquals( 4, g.size() );
        assertIsomorphic( r, g );
        }
        
    public void testMinimalExplode()
        {
        Graph g = getGraph( Minimal );
        g.getReifier().reifyAs( node( "a" ), triple( "p Q r" ) );
        assertEquals( 0, g.size() );
        }
        
    public void testReificationTriplesConvenient()
        { testReificationTriples( Convenient ); }
        
    public void testReificationTriplesStandard()
        { testReificationTriples( Standard ); }
    
    public void testReificationQuadletsMinimal()
        { testReificationTriples( Minimal ); }
        
    /**
        test that a reifier with the given style sees [or not, if it's minimal] the reification quads
        that are inserted through its graph.
    */
    protected void testReificationTriples( ReificationStyle style )
        {
        Graph g = getGraph( style );
        Graph quadlets = getReificationTriples( g.getReifier() );
        String S1 = "SSS rdf:predicate PPP", S2 = "SSS rdf:subject SSS";
        g.add( triple( S1 ) );
        assertIsomorphic( graphWithUnless( style == Minimal, S1 ), quadlets );
        g.add( triple( S2 ) );
        assertIsomorphic( graphWithUnless( style == Minimal, S1 + "; " + S2 ), quadlets );
        assertEquals( "convenient hides quadlets", style == Convenient, g.size() == 0 );
        }
        
    /**
         Ensure that over-specifying a reification means that we don't get a triple
         back. Goodness knows why this test wasn't in right from the beginning.
    */
    public void testOverspecificationSuppressesReification()
        {
        Graph g = getGraph( Standard );
        Reifier r = g.getReifier();
        graphAdd( g, "x rdf:subject A; x rdf:predicate P; x rdf:object O; x rdf:type rdf:Statement" );
        assertEquals( triple( "A P O" ), r.getTriple( node( "x" ) ) );
        try 
            { graphAdd( g, "x rdf:subject BOOM" ); 
            assertEquals( null, r.getTriple( node( "x" ) ) ); }
        catch (AlreadyReifiedException e) 
            {
            if (r instanceof DBReifier) { System.err.println( "! Db reifier must fix over-specification problem" ); }
            else throw e;
            }
        }
    
    public void testReificationSubjectClash()
        {
        testReificationClash( "x rdf:subject SS" );
        }    
    
    public void testReificationPredicateClash()
        {
        testReificationClash( "x rdf:predicate PP" );
        }    
    
    public void testReificationObjectClash()
        {
        testReificationClash( "x rdf:object OO" );
        }
        
    /**
     * @param clashingStatement
     */
    protected void testReificationClash( String clashingStatement )
        {
        Graph g = getGraph( Standard );
        Triple SPO = Triple.create( "S P O" );
        g.getReifier().reifyAs( node( "x" ), SPO );
        assertTrue( g.getReifier().hasTriple( SPO ) );
        try
            {
            graphAdd( g,  clashingStatement );
            assertEquals( null, g.getReifier().getTriple( node( "x" ) ) );
            assertFalse( g.getReifier().hasTriple( SPO ) );
            }
        catch (AlreadyReifiedException e)
            {
            if (g.getReifier() instanceof DBReifier) { System.err.println( "! Db reifier must fix over-specification problem" ); }
            else throw e;
            }
        }

    public void testManifestQuadsStandard()
        { testManifestQuads( Standard ); }
        
    public void testManifestQuadsConvenient()
        { testManifestQuads( Convenient ); }
        
    public void testManifestQuadsMinimal()
        { testManifestQuads( Minimal ); }
        
    /**
        Test that reifying a triple explicitly has some effect on the graph only for Standard
        reifiers.
     */
    public void testManifestQuads( ReificationStyle style )
        {
        Graph g = getGraph( style );   
        Reifier r = g.getReifier();
        r.reifyAs( node( "A" ), triple( "S P O" ) );
        String reified = "A rdf:type rdf:Statement; A rdf:subject S; A rdf:predicate P; A rdf:object O";
        assertIsomorphic( graphWithIf( style == Standard, reified ), g );
        }
        
    public void testHiddenVsReificationMinimal()
        { testHiddenVsReification( Minimal ); }
        
    public void testHiddenVsStandard()
        { testHiddenVsReification( Standard ); }
        
    public void testHiddenVsReificationConvenient()
        { testHiddenVsReification( Convenient ); }
        
    public void testHiddenVsReification( ReificationStyle style )
        {
        Graph g = getGraph( style );
        Reifier r = g.getReifier();
        r.reifyAs( node( "A" ), triple( "S P O" ) );
        assertEquals( style == Standard, r.findEither( ALL, false ).hasNext() );    
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
        assertFalse( "node is known unbound", R.hasTriple( Node.createURI( "any:thing" ) ) );
    /* */
//      Graph GR = R.getReifiedTriples();
//      assertTrue( "reified triples", getGraphWith( "x R y; p S q" ).isIsomorphicWith(GR) );
//      assertTrue( "untouched graph", getGraph().isIsomorphicWith(G) );
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
        
    public void testRemoveFromNothing()
        {
        Graph G = getGraph();
        Reifier R = G.getReifier();
        G.delete( triple( "quint rdf:subject S" ) );
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
        R.reifyAs( X, triple( "x R y" ) );
        try { R.reifyAs( X, triple( "x R z" ) ); fail( "did not detect already reified node" ); }
        catch (AlreadyReifiedException e) { }      
        }
        
    public void testKevinCaseA()
        {
        Graph G = getGraph( Standard );
        Node X = node( "x" ), a = node( "a" ), b = node( "b" ), c = node( "c" );
        G.add( Triple.create( X, RDF.Nodes.type, RDF.Nodes.Statement ) );
        G.getReifier().reifyAs( X, Triple.create( a, b, c ) ); 
        }
        
    public void testKevinCaseB()
        {
        Graph G = getGraph( Standard );
        Node X = node( "x" ), Y = node( "y" );
        Node a = node( "a" ), b = node( "b" ), c = node( "c" );
        G.add( Triple.create( X, RDF.Nodes.subject, Y ) );
        try
            {
            G.getReifier().reifyAs( X, Triple.create( a, b, c ) );
            fail( "X already has subject Y: cannot make it a" );
            }
        catch (CannotReifyException e)
            { pass(); }
        }

    /**
        Test that the hidden triples graph is dynamic, not static.
    */
    public void testDynamicHiddenTriples()
        {
        Graph g = getGraph();
        Reifier r = g.getReifier();
        Graph h = getHiddenTriples( r );
        Graph wanted = graphWith
            ( 
            "x rdf:type rdf:Statement" 
            + "; x rdf:subject a"
            + "; x rdf:predicate B"
            + "; x rdf:object c"
            );
        assertTrue( h.isEmpty() );
        r.reifyAs( node( "x" ), triple( "a B c" ) );
        assertIsomorphic( wanted, h );
        }
    
    protected Graph getHiddenTriples( final Reifier r )
        {
        return new GraphBase() 
            {
            public ExtendedIterator graphBaseFind( TripleMatch m ) { return r.findEither( m, true ); }
            };
        }
            
	public void testQuadRemove()
		{
		Graph g = getGraph( Standard );
		assertEquals( 0, g.size() );
	
		Triple s = Triple.create( "x rdf:subject s" );
		Triple p = Triple.create( "x rdf:predicate p" );
		Triple o = Triple.create( "x rdf:object o" );
		Triple t = Triple.create( "x rdf:type rdf:Statement");
		g.add(s); g.add(p); g.add(o); g.add(t);
		assertEquals( 4, g.size() );

		g.delete(s); g.delete(p); g.delete(o); g.delete(t);
		assertEquals( 0, g.size() );
		}

    public void testReifierSize()
        {
        Graph g = getGraph();
        Reifier r = g.getReifier();
        assertEquals( 0, r.size() );
        }
    
    public void testReifierEmptyFind()
        {
        Graph g = getGraph( Standard );
        Reifier r = g.getReifier();
        assertEquals( HashUtils.createSet(), iteratorToSet( r.findExposed( triple( "?? ?? ??" ) ) ) );
        }

    public void testReifierFindSubject()
        { testReifierFind( "x rdf:subject S" ); }
    
    public void testReifierFindObject()
        { testReifierFind( "x rdf:object O" ); }
    
    public void testReifierFindPredicate()
        { testReifierFind( "x rdf:predicate P" ); }
    
    public void testReifierFindComplete()
        { testReifierFind( "x rdf:predicate P; x rdf:subject S; x rdf:object O; x rdf:type rdf:Statement" ); }
    
    public void testReifierFindFilter()
        { 
        Graph g = getGraph( Standard );
        Reifier r = g.getReifier();
        graphAdd( g, "s rdf:subject S" );
        assertEquals( tripleSet( "" ), iteratorToSet( r.findExposed( triple( "s otherPredicate S" ) ) ) );
        }

    protected void testReifierFind( String triples )
        { testReifierFind( triples, "?? ?? ??" ); }
    
    protected void testReifierFind( String triples, String pattern )
        {
        Graph g = getGraph( Standard );
        Reifier r = g.getReifier();
        graphAdd( g, triples );
        assertEquals(  tripleSet( triples ), iteratorToSet( r.findExposed( triple( pattern ) ) ) );
        }

//    public void testKevinCaseC()
//        {
//        Graph G = GraphBase.withReification( getGraph() );
//        Node X = node( "x" ), Y = node( "y" );
//        Node a = node( "a" ), b = node( "b" ), c = node( "c" );
//        G.getReifier().reifyAs( X, Triple.create( a, b, c ) );         
//        try
//            {
//            G.add( Triple.create( X, Reifier.subject, Y ) );
//            fail( "X already reifies (a, b, c): cannot give it subject Y" );
//            }
//        catch (Reifier.CannotReifyException e)
//            { /* as requried */ }
//        }
            
//    public void testQuads()
//        {
//        Node A = node( "a" ), B = node( "b" );
//        Graph G = getGraph();
//        Graph quads = getGraphWith
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
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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