/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.graph.test;

import java.util.Collections;

import com.hp.hpl.jena.db.impl.DBReifier;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.shared.*;
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

    protected static final Triple ALL = Triple.ANY;
    
    public AbstractTestReifier( String name )
        { super( name ); }
        
    public abstract Graph getGraph();
    
    public abstract Graph getGraph( ReificationStyle style );

    protected final Graph getGraphWith( String facts )
        {
        Graph result = getGraph();
        graphAdd( result, facts );
        return result;
        }

    /**
        Answer true iff g's reifier has style Standard.
    */
    private boolean isStandard( Graph g )
        { return g.getReifier().getStyle() == Standard; }
        
    /**
        Answer the empty graph if cond is false, otherwise the graph with the given facts.
    */
    protected final Graph graphWithUnless( boolean cond, String facts )
        { return graphWith( cond ? "" : facts ); }
          
    protected final Graph graphWithIf( boolean cond, String facts )
        { return graphWithUnless( !cond, facts ); }
            
    public void testGetGraphNotNull()
        { assertNotNull( getGraph()  ); }
    
    public void testGotGraphReifierNotNull()
        { assertNotNull( getGraph().getReifier() ); }
    
    public void testGotGraphReifierStyleNotNull()
        { assertNotNull( getGraph().getReifier().getStyle() ); }
    
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
    
    public void testReifierClosed()
        {
        Graph g = getGraph();
        Reifier r = g.getReifier();
        g.close();
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
        g.add( NodeCreateUtils.createTriple( "x rdf:subject s" ) );
        assertEquals( 1, g.size() );
        g.add( NodeCreateUtils.createTriple( "x rdf:predicate p" ) );
        assertEquals( 2, g.size() );  
        g.add( NodeCreateUtils.createTriple( "x rdf:object o" ) );
        assertEquals( 3, g.size() );            
        g.add( NodeCreateUtils.createTriple( "x rdf:type rdf:Statement" ) );
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
        
    /**
        test that a reifier with the given style sees [or not, if it's minimal] the reification quads
        that are inserted through its graph.
    */
    public void testReificationTriples()
        {
        Graph g = getGraph();
        ReificationStyle style = g.getReifier().getStyle();
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
        Graph g = getGraph();
        if (isStandard( g ))
            {
            Reifier r = g.getReifier();
            graphAdd( g, "x rdf:subject A; x rdf:predicate P; x rdf:object O; x rdf:type rdf:Statement" );
            assertEquals( triple( "A P O" ), r.getTriple( node( "x" ) ) );
            try 
                { graphAdd( g, "x rdf:subject BOOM" ); 
                assertEquals( null, r.getTriple( node( "x" ) ) ); }
            catch (AlreadyReifiedException e) 
                {
                if (r instanceof DBReifier) { /* System.err.println( "! Db reifier must fix over-specification problem" ); */ }
                else throw e;
                }
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
        
    protected void testReificationClash( String clashingStatement )
        {
        Graph g = getGraph();
        if (isStandard( g ))
            {            
            Triple SPO = NodeCreateUtils.createTriple( "S P O" );
            g.getReifier().reifyAs( node( "x" ), SPO );
            assertTrue( g.getReifier().hasTriple( SPO ) );
            try
                {
                graphAdd( g,  clashingStatement );
                assertEquals( null, g.getReifier().getTriple( node( "x" ) ) );
                // System.err.println( ">> tRC: clashing = " + clashingStatement );
                assertFalse( g.getReifier().hasTriple( SPO ) );
                }
            catch (AlreadyReifiedException e)
                {
                if (g.getReifier() instanceof DBReifier) { /* System.err.println( "! Db reifier must fix over-specification problem" ); */ }
                else throw e;
                }
            }
        }

//    public void testManifestQuadsStandard()
//        { testManifestQuads( Standard ); }
//        
//    public void testManifestQuadsConvenient()
//        { testManifestQuads( Convenient ); }
//        
//    public void testManifestQuadsMinimal()
//        { testManifestQuads( Minimal ); }
        
    /**
        Test that reifying a triple explicitly has some effect on the graph only for Standard
        reifiers.
     */
    public void testManifestQuads()
        {
        Graph g = getGraph();   
        Reifier r = g.getReifier(); 
        ReificationStyle style = r.getStyle(); 
        r.reifyAs( node( "A" ), triple( "S P O" ) );
        String reified = "A rdf:type rdf:Statement; A rdf:subject S; A rdf:predicate P; A rdf:object O";
        assertIsomorphic( graphWithIf( style == Standard, reified ), g );
        }
        
//    public void testHiddenVsReificationMinimal()
//        { testHiddenVsReification( Minimal ); }
//        
//    public void testHiddenVsStandard()
//        { testHiddenVsReification( Standard ); }
//        
//    public void testHiddenVsReificationConvenient()
//        { testHiddenVsReification( Convenient ); }
        
    public void testHiddenVsReification()
        {
        Graph g = getGraph();
        ReificationStyle style = g.getReifier().getStyle();
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
        Graph G = getGraph();
        if (isStandard( G ))
            {
            Node X = node( "x" ), a = node( "a" ), b = node( "b" ), c = node( "c" );
            G.add( Triple.create( X, RDF.Nodes.type, RDF.Nodes.Statement ) );
            G.getReifier().reifyAs( X, Triple.create( a, b, c ) ); 
            }
        }
        
    public void testKevinCaseB()
        {
        Graph G = getGraph();
        if (isStandard( G ))
            {
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
        }

    /**
        Test that the hidden triples graph is dynamic, not static.
    */
    public void testDynamicHiddenTriples()
        {
        Graph g = getGraph( Minimal );
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
            @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) 
                { return r.findEither( m, true ); }
            };
        }
            
	public void testQuadRemove()
		{
		Graph g = getGraph();
		if (isStandard( g ))
		    {
    		assertEquals( 0, g.size() );
    		Triple s = NodeCreateUtils.createTriple( "x rdf:subject s" );
    		Triple p = NodeCreateUtils.createTriple( "x rdf:predicate p" );
    		Triple o = NodeCreateUtils.createTriple( "x rdf:object o" );
    		Triple t = NodeCreateUtils.createTriple( "x rdf:type rdf:Statement");
    		g.add(s); g.add(p); g.add(o); g.add(t);
    		assertEquals( 4, g.size() );
    		g.delete(s); g.delete(p); g.delete(o); g.delete(t);
    		assertEquals( 0, g.size() );
		    }
		}

    public void testReifierSize()
        {
        assertEquals( 0, getGraph().getReifier().size() );
        }
    
    public void testEmpty()
        {
        Graph g = getGraph();
        if (g.getReifier().getStyle() != Convenient)
            {
            assertTrue( g.isEmpty() );
            graphAdd( g, "x rdf:type rdf:Statement" ); assertFalse( g.isEmpty() );
            graphAdd( g, "x rdf:subject Deconstruction" ); assertFalse( g.isEmpty() );
            graphAdd( g, "x rdf:predicate rdfs:subTypeOf" ); assertFalse( g.isEmpty() );
            graphAdd( g, "x rdf:object LiteraryCriticism" ); assertFalse( g.isEmpty() );
            }
        }
    
    public void testReifierEmptyFind()
        {
        Graph g = getGraph();
        Reifier r = g.getReifier();
        assertEquals( tripleSet( "" ), r.findExposed( Triple.ANY ).toSet() );
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
        Graph g = getGraph();
        Reifier r = g.getReifier();
        graphAdd( g, "s rdf:subject S" );
        assertEquals( tripleSet( "" ), r.findExposed( triple( "s otherPredicate S" ) ).toSet() );
        }

    protected void testReifierFind( String triples )
        { testReifierFind( triples, "?? ?? ??" ); }

    /**
        Only applies to style Standard.
    */
    protected void testReifierFind( String triples, String pattern )
        {
        Graph g = getGraph();
        Reifier r = g.getReifier();
        graphAdd( g, triples );
        if (r.getStyle() == Standard)
            assertEquals(  tripleSet( triples ), r.findExposed( triple( pattern ) ).toSet() );
        }

    public void testQuintetBug()
        {
        String spec = "rs rdf:type rdf:Statement; foo rdf:value rs; rs rdf:subject X; rs rdf:predicate P; rs rdf:object O1; rs rdf:object O2";
        Graph g = getGraph( Standard );
        Reifier r = g.getReifier();
        try {
        graphAdd( g, spec );
        Graph wanted = getGraph( Minimal );
        graphAdd( wanted, spec );
        assertIsomorphic( wanted, g );
        }
        catch (AlreadyReifiedException e) 
        {
        if (r instanceof DBReifier) { /* System.err.println( "! Db reifier must fix over-specification problem" ); */ }
        else throw e;
        }
        }
    
    public void testBulkClearReificationTriples()
        {
        Graph g = getGraphWith( "x rdf:subject S" );
        g.getBulkUpdateHandler().removeAll();
        assertEquals( "oops: " + g.getClass(), Collections.EMPTY_SET, g.find( Node.ANY, Node.ANY, Node.ANY ).toSet() );        
        }
    
    public void testBulkClearReificationTriples2()
        {
        Graph g = getGraphWith( "x rdf:subject S; x rdf:predicate P; x rdf:object O; x rdf:type rdf:Statement" );
        g.getBulkUpdateHandler().removeAll();
        assertEquals( Collections.EMPTY_SET, g.find( Node.ANY, Node.ANY, Node.ANY ).toSet() );        
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
