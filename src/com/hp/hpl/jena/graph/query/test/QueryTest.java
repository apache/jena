/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: QueryTest.java,v 1.15 2003-07-18 11:02:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.impl.*;

import java.util.*;
import junit.framework.*;

/**
    test cases for Graph queries. There should be many more comments
    in this file.
*/

public class QueryTest extends GraphTestBase
    {
    private Query Q;
    private Node X = node( "?x" );
    private Node Y = node( "?y" );
    private Node Z = node( "?z" );
    private Graph empty;
    private Graph single;
    
	public QueryTest( String name )
		{ super( name ); }
		
    public static TestSuite suite()
    	{ return new TestSuite( QueryTest.class ); }
    	
    public void setUp()
        {
        Q = new Query();
        empty = graphWith( "" );
        single = graphWith( "spindizzies drive cities" );
        }
        
	private void testTreeQuery( String title, String content, String pattern, String correct )
		{
		Graph gc = graphWith( content ), gp = graphWith( pattern );
        Graph answer = gc.queryHandler().prepareTree( gp ).executeTree();
		if (title.equals( "" )) title = "checking {" + content + "} against {" + pattern + "} should give {" + correct + "}" + " not " + answer;
		assertEquals( title, graphWith( correct ), answer );
		}
		
	private void testTreeQuery( String content, String pattern, String answer )
		{
		testTreeQuery( "checking", content, pattern, answer );
		}
		
	private static final String [][] tests =
		{
			{ "", "pigs might fly", "", "" },
			{ "", "", "pigs might fly", "" },
			{ "", "a pings b; b pings c", "a pings _x; _x pings c", "a pings b; b pings c" },
			{ "", "a pings b; b pings c; a pings x; x pings c", "a pings _x; _x pings c", "a pings b; b pings c; a pings x; x pings c" }
		};
		
	public void testManyThings()
		{
		for (int i = 0; i < tests.length; i += 1) 
			testTreeQuery( tests[i][0], tests[i][1], tests[i][2], tests[i][3] );
		}
		
	public void testAtomicTreeQuery()
		{
		testTreeQuery( "pigs might fly; birds will joke; cats must watch", "birds will joke", "birds will joke" );
		}
		
	public void testCompositeTreeQuery()
		{
		testTreeQuery
			( "pigs might fly; birds will joke; cats must watch", "birds will joke; pigs might fly", "pigs might fly; birds will joke" );
		}
		
	public void testChainedTreeQuery()
		{
		testTreeQuery( "a pings b; b pings c; c pings d", "a pings b; b pings c", "a pings b; b pings c" );
		}
		
    public void testBinding1( )
        {
        Graph single = graphWith( "rice grows quickly" );
        Query q = new Query();
        Node V1 = node( "?v1" ), V3 = node( "?v3" );
        BindingQueryPlan qp = single.queryHandler().prepareBindings( q.addMatch( V1, node("grows"), V3 ), new Node[] {V1, V3} );
        Domain binding = (Domain) qp.executeBindings().next();
        assertEquals( "binding subject to rice", binding.get(0), node("rice") ); 
        assertEquals( "binding object to quickly", binding.get(1), node("quickly") ); 
        }

    public void testBinding2( )
        { // TODO - relies on ordering of results; this is UNSAFE
        Graph several = graphWith( "rice grows quickly; time isan illusion" );
        String [][] answers = { {"time", "isan", "illusion"}, {"rice", "grows", "quickly"} };
        Query q = new Query();
        Node V1 = node( "?v1" ), V2 = node( "?v2" ), V3 = node( "?v3" );
        BindingQueryPlan qp = several.queryHandler().prepareBindings( q.addMatch( V1, V2, V3 ),  new Node[] {V1, V2, V3} );
        Iterator bindings = qp.executeBindings();
        for (int i = 0; i < answers.length; i += 1)
        	{
        	if (bindings.hasNext() == false) fail( "wanted some more results" );
        	Domain bound = (Domain) bindings.next();
        	for (int j = 0; j < 3; j += 1)
        		assertEquals( "binding failure", bound.get(j), node( answers[i][j] ) );
        	}
       	assertFalse( "iterator should be empty", bindings.hasNext() ); 
        }

	public void testMultiplePatterns()
		{
		Graph bookish = graphWith( "ben wrote Clayface; Starfish ingenre SF; Clayface ingenre Geology; bill wrote Starfish" );
		Query q = new Query();
		Node X = node( "?X" ); 
        Node A = node( "?A" ); 
		q.addMatch(  X, node("wrote"), A ).addMatch(  A, node("ingenre"), node("SF") );
		BindingQueryPlan qp = bookish.queryHandler().prepareBindings( q, new Node [] {X} );
		Iterator bindings = qp.executeBindings();
		if (bindings.hasNext())
			{
			Domain it = (Domain) bindings.next();
			if (it.size() == 1)
				{
				if (it.get(0).equals( node("bill") ))
					{
					if (bindings.hasNext())
						System.out.println( "! failed: more than one multiple pattern answer: " + (Domain) bindings.next() );
					}
				else
					System.out.println( "! failed: multiple pattern answer should be 'bill'" );
				}
			else
				System.out.println( "! failed: multiple pattern answer should have one element" );
			}
		else
			System.out.println( "! failed: multiple pattern query should have an answer" );
		}
		
    public void testNodeVariablesA()
        {
        Graph mine = graphWith( "storms hit England" );
        Node spoo = node( "?spoo" );
        Q.addMatch( spoo, node("hit"), node("England") );
        ClosableIterator it = mine.queryHandler().prepareBindings( Q, new Node[] {spoo} ).executeBindings();
        assertTrue( "tnv: it has a solution", it.hasNext() );
        assertEquals( "", node("storms"), ((List) it.next()).get(0) );
        assertFalse( "tnv: just the one solution", it.hasNext() );
        }
   
    public void testNodeVariablesB()
        {
        Graph mine = graphWith( "storms hit England" );
        Node spoo = node( "?spoo" ), flarn = node( "?flarn" );
        Q.addMatch( spoo, node("hit"), flarn );
        ClosableIterator it = mine.queryHandler().prepareBindings( Q, new Node[] {flarn, spoo} ).executeBindings();
        assertTrue( "tnv: it has a solution", it.hasNext() );
        List answer = (List) it.next();
        assertEquals( "tnvB", node("storms"), answer.get(1) );
        assertEquals( "tnvB", node("England"), answer.get(0) );
        assertFalse( "tnv: just the one solution", it.hasNext() );
        }
        
    public Set nodeSet( String s )
        {
        HashSet result = new HashSet();
        StringTokenizer st = new StringTokenizer( s );
        while (st.hasMoreTokens()) result.add( node( st.nextToken() ) );
        return result;
        }
        
    public void testGraphConstraints( String title, String constraint, String wanted )
        { 
        Map1 get0 = new Map1() { public Object map1( Object x ) { return ((List) x).get(0); } };
        Node O = node( "?O" );
        Query Q = new Query();
        Q.addMatch( Query.ANY, Query.ANY, O );
        Graph G = graphWith( "pigs fly south; dogs fly badly; plans fly flat" );
        Q.addConstraint( graphWith( constraint ) );
        Set results = iteratorToSet( G.queryHandler().prepareBindings( Q, new Node[] {O} ).executeBindings().mapWith( get0 ) );
        assertEquals( "tgs", nodeSet( wanted ), results );
        }
        
     public void testGraphConstraints()
        {
        testGraphConstraints( "tgs A", "", "south flat badly" );
        testGraphConstraints( "tgs B", "?O &ne badly", "south flat" );
        testGraphConstraints( "tgs C", "?O &ne badly; ?O &ne flat", "south" );
        }
 
    public void testSeveralGraphConstraints( )
        {
        Map1 get0 = new Map1() { public Object map1( Object x ) { return ((List) x).get(0); } };
        Node O = node( "?O" );
        Query Q = new Query();
        Q.addMatch( Query.ANY, Query.ANY, O );
        Graph G = graphWith( "pigs fly south; dogs fly badly; plans fly flat" );
        Q.addConstraint( graphWith( "?O &ne badly" ) );
        Q.addConstraint( graphWith( "?O &ne flat" ) );
        Set results = iteratorToSet( G.queryHandler().prepareBindings( Q, new Node[] {O} ).executeBindings().mapWith( get0 ) );
        assertEquals( "tsgs", nodeSet( "south" ), results );
        }

    public void testBindingQuery()
    	{
    	Graph empty = graphWith( "" );
       	Graph base = graphWith( "pigs might fly; cats chase mice; dogs chase cars; cats might purr" );
	/* */
        Node [] none = new Node [] {};
	    assertFalse( "empty graph, no bindings", empty.queryHandler().prepareBindings( new Query().addMatch( Query.ANY, Query.ANY, Query.ANY ), none ).executeBindings().hasNext() );
	    assertTrue( "full graph, > 0 bindings", base.queryHandler().prepareBindings( new Query(), none ).executeBindings().hasNext() );
    	}

    public static List iteratorToList( ClosableIterator L )
        {
        ArrayList result = new ArrayList();
        while (L.hasNext()) result.add( L.next() );
        return result;
        }
        
    public void testEmpty()
        {
        List bindings = iteratorToList( Q.executeBindings( empty, new Node [] {} ) );
        assertEquals( "testEmpty: select [] from {} => 1 empty binding [size]", bindings.size(), 1 );
        Domain d = (Domain) bindings.get( 0 );
        assertEquals( "testEmpty: select [] from {} => 1 empty binding [width]", d.size(), 0 );
        }
        
    public void testOneMatch()
        {
        Q.addMatch( X, Query.ANY, Query.ANY );
        List bindings = iteratorToList( Q.executeBindings( single, new Node [] {X} ) );
        assertEquals( "select X from {spindizzies drive cities} => 1 binding [size]", bindings.size(), 1 );
        Domain d = (Domain) bindings.get( 0 );
        assertEquals( "select X from {spindizzies drive cities} => 1 binding [width]", d.size(), 1 );
        assertTrue( "select X from {spindizzies drive cities} => 1 binding [value]", d.get( 0 ).equals( node( "spindizzies" ) ) );
        }
        
    public void testMismatch()
        {
        Q.addMatch( X, X, X );
        List bindings = iteratorToList( Q.executeBindings( single, new Node [] {X} ) );
        assertEquals( "bindings mismatch (X X X)", bindings.size(), 0 );
        }
        
    public void testXXXMatch1()
        {
        Q.addMatch( X, X, X );
        Graph xxx = graphWith( "ring ring ring" );
        List bindings = iteratorToList( Q.executeBindings( xxx, new Node[] {X} ) );
        assertEquals( "bindings match (X X X)", bindings.size(), 1 );       
        }
        
    public HashSet setFrom( Node [] nodes )
        {
        HashSet result = new HashSet( nodes.length );
        for (int i = 0; i < nodes.length; i += 1) result.add( nodes[i] );
        return result;
        }
        
    public void testXXXMatch3()
        {
        Q.addMatch( X, X, X );
        Graph xxx = graphWith( "ring ring ring; ding ding ding; ping ping ping" );
        List bindings = iteratorToList( Q.executeBindings( xxx, new Node[] {X} ) );
        assertEquals( "bindings match (X X X)", bindings.size(), 3 );       
    /* */
        HashSet found = new HashSet();
        for (int i = 0; i < bindings.size(); i += 1) 
            {
            Domain d = (Domain) bindings.get( i );
            assertEquals( "one bound variable", d.size(), 1 );
            found.add( d.get( 0 ) );
            }
        HashSet wanted = setFrom( new Node[] {node("ring"), node("ding"), node("ping")} );
        assertEquals( "testMatch getting {ring ding ping}", found, wanted );
        }
        
    public void testTwoPatterns()
        {
        Node reads = node("reads"), inGenre = node("inGenre");
        Graph g = graphWith( "chris reads blish; blish inGenre SF" );
        // System.err.println( "| X = " + X + ", Y = " + Y + ", Z = " + Z );
        Q.addMatch( X, reads, Y );
        Q.addMatch( Y, inGenre, Z );
        List bindings = iteratorToList( Q.executeBindings( g, new Node[] {X, Z} ) );
        assertTrue( "testTwoPatterns: one binding", bindings.size() == 1 );
        Domain  d = (Domain) bindings.get( 0 );
        // System.out.println( "* width = " + d.width() );
        assertEquals( "testTwoPatterns: width 2", d.size(), 2 );
        assertEquals( "testTwoPatterns: X = chris", d.get(0), node("chris") );
        assertEquals( "testTwoPatterns: Y = SF", d.get(1), node("SF") );
        }
        
    public void testGraphQuery()
        {
        Graph pattern = graphWith( "?X reads ?Y; ?Y inGenre ?Z" );
        Graph target = graphWith( "chris reads blish; blish inGenre SF" );
        // System.err.println( "| pattern: " + pattern );
        Query q = new Query( pattern );
        List bindings = iteratorToList( q.executeBindings( target, new Node[] {node("?X"), node("?Z")} ) );
        assertEquals( "testTwoPatterns: one binding", 1, bindings.size() );
        Domain  d = (Domain) bindings.get( 0 );
        // System.out.println( "* width = " + d.width() );
        assertEquals( "testTwoPatterns: width 2", d.size(), 2 );
        assertEquals( "testTwoPatterns: X = chris", d.get(0), node("chris") );
        assertEquals( "testTwoPatterns: Y = SF", d.get(1), node("SF") );        
        }
                
    public void testTwoGraphs()
        {
        Graph a = graphWith( "chris reads blish; chris reads norton; chris eats curry" );
        Graph b = graphWith( "blish inGenre SF; curry inGenre food" );
        Node reads = node("reads"), inGenre = node("inGenre");
        Q.addMatch( "a", X, reads, Y ).addMatch( "b", Y, inGenre, Z );
        Query.ArgMap args = Q.args().put( "a", a ).put( "b", b );
        List bindings = iteratorToList( Q.executeBindings( args, new Node [] {X, Z} ) );
        assertEquals( "testTwoGraphs: one binding", bindings.size(), 1 );
        Domain  d = (Domain) bindings.get( 0 );
        assertEquals( "testTwoGraphs: width 2", d.size(), 2 );
        assertEquals( "testTwoGraphs: X = chris", d.get(0), node("chris") );
        assertEquals( "testTwoGraphs: Y = SF", d.get(1), node("SF") );     
        }
        
    private void helpConstraint( String title, Graph constraints, int n )
        {
        Query q = new Query();
        Graph g = graphWith( "blish wrote CIF; blish wrote VOR; hambly wrote Darwath; feynman mechanicked quanta" );
        q.addMatch( X, node("wrote"), Query.ANY );
        q.addConstraint( constraints );
        List bindings = iteratorToList( q.executeBindings( g, new Node [] {X} ) );
        assertEquals( "testConstraint " + title + ": number of bindings", n, bindings.size() );
        }
        
    public void testConstraint()
        {
        helpConstraint( "none", graphWith( "" ), 3 );
        helpConstraint( "X /= blish", graphWith( "?x &ne blish" ), 1 ); 
        helpConstraint( "X /= blish & X /= hambly", graphWith( "?x &ne blish; ?x &ne hambly" ), 0 ); 
        }
        
    public void testConstraintTwo()
        {
        Graph g = graphWith( "blish wrote CIF; blish wrote VOR; hambly wrote Darwath; feynman mechanicked quanta" );
        Q.addMatch( X, node( "wrote" ), Y );    
        Q.addConstraint( graphWith( "?x &ne ?y" ) );
        List bindings = iteratorToList( Q.executeBindings( g, new Node [] {X} ) );
        assertEquals( "testConstraint " + "Two" + ": number of bindings", bindings.size(), 3 );
        }
        
    private void helpConstraintThree( String title, Graph c, int n )
        {           
        Query q = new Query();
        Graph g = graphWith( "brust wrote jhereg; hedgehog hacked code; angel age 230; brust wrote 230" );
        q.addConstraint( c );
        q.addMatch( X, Y, Z );
        List bindings = iteratorToList( q.executeBindings( g, new Node [] {X, Z} ) );
        assertEquals( "testConstraint " + title + ": number of bindings", n, bindings.size() );         
        }
        
    public void testConstraintThree()
        {
        helpConstraintThree( "testConstraintThree 1:", graphWith( "?x &eq brust" ), 2 );
        helpConstraintThree( "testConstraintThree 2:", graphWith( "?y &eq hacked" ), 1 );
        helpConstraintThree( "testConstraintThree 3:", graphWith( "?z &eq 230" ), 2 );
        helpConstraintThree( "testConstraintThree 4:", graphWith( "?z &eq 230" ), 2 ); 
       }
       
   public void testConstraintFour()
        {
        Map1 getFirst = new Map1(){ public Object map1(Object x) { return ((List) x).get(0); }};
        Query q = new Query();
        Graph g = graphWith( "bill pinged ben; ben pinged weed; weed pinged weed; bill ignored bill" );
        q.addMatch( X, node("pinged"), Y );
        q.addConstraint( X, Query.NE, Y );
        Set bindings = iteratorToSet( q.executeBindings( g, new Node [] {X} ).mapWith(getFirst) );
        assertEquals( "", setFrom( new Node[] {node("bill"), node("ben")} ), bindings );
        }
       
    public void testStringResults()
        {
        Graph g = graphWith( "ding dong dilly" );
        Query q = new Query() .addMatch( X, Y, Query.ANY );
        List bindings = iteratorToList( q.executeBindings( g, new Node [] {X, Y} ) );
        assertEquals( "one result back by name", bindings.size(), 1 );
        assertEquals( "x = ding", ((Domain) bindings.get(0)).get(0), node("ding") );
        }
        
    /**
        this possible failure mode discovered by Andy when building a fast-path
        RDQL engine over the graph.query SPI.
    <br>
        test that we get a sensible result when unbound variables are used in the
        query result selector.
    */
    public void testMissingVariable()
        {
        Graph g = graphWith( "x y z" );
        Query q = new Query();
        List bindings = iteratorToList( q.executeBindings( g, new Node [] {X, Y} ) );
        List L = (List) q.executeBindings( g, new Node [] {X, Y} ).next();
        assertEquals( "undefined variables get null", null, L.get( 0 ) );
        }
        
    /**
        More of an example than a test, for a query with "disconnected" triples.
    */
    public void testDisconnected()
        {
        Graph g = graphWith( "x pred1 foo; y pred2 bar" );
        Query q = new Query( graphWith( "?X ?? foo; ?Y ?? bar" ) );
        List bindings = iteratorToList( q.executeBindings( g, nodes( "?X ?Y" ) ) );
        assertEquals( 1, bindings.size() );
        assertEquals( node( "x" ), ((List) bindings.get(0)).get(0) );
        assertEquals( node( "y" ), ((List) bindings.get(0)).get(1) );
        }
        
     /**
        Test that the default engine does not re-order triples.
     */
    public void testQueryTripleOrder()
        {
        Triple t1 = Triple.create( "A B C" ), t2 = Triple.create( "D E F" );
        List desired = Arrays.asList( new Triple[] {t1, t2} );
        List obtained = getTriplesFromQuery( desired );
        assertEquals( desired, obtained );
        }
        
    /**
        This horror to extract the order in which the triples are handed to
        patternStage illustrates that the Query code needs some refactoring
        to make it more testable.
        TODO make the Query code more testable. 
    */
    private List getTriplesFromQuery( List desired )
        {
        Query q = new Query();
        final Triple [][] tripleses = new Triple[1][];
        final Graph g = new GraphBase()
            {
            public ExtendedIterator find( TripleMatch tm )
                { return new NiceIterator(); }
            public QueryHandler queryHandler()
                {
                return new SimpleQueryHandler( this )
                    {
                    public Stage patternStage( Mapping map, Graph constraints, Triple [] t )
                        {
                        if (t.length > 1) tripleses[0] = t;
                        return super.patternStage( map, constraints, t );
                        }
                    }
                    ;
                }
            };
        for (int i = 0; i < desired.size(); i += 1) q.addMatch( (Triple) desired.get(i) );
        q.executeBindings( g, nodes( "" ) );
        return Arrays.asList( tripleses[0] );
        }
        
    /**
        test that we can correctly deduce the variable count for some queries.
     */
    public void testVariableCount()
        {
        assertCount( 0, "" );
        assertCount( 0, "x R y" );
        assertCount( 1, "?x R y" );
        assertCount( 1, "?x R y", "?x" );
        assertCount( 2, "?x R y", "?z" );
        assertCount( 1, "?x R ?x" );
        assertCount( 2, "?x R ?y" );
        assertCount( 3, "?x R ?y", "?z" );
        assertCount( 3, "?x ?R ?y" );
        assertCount( 6, "?x ?R ?y; ?a ?S ?c" );
        assertCount( 6, "?x ?R ?y; ?a ?S ?c", "?x" );
        assertCount( 6, "?x ?R ?y; ?a ?S ?c", "?x ?c" );
        assertCount( 6, "?x ?R ?y; ?a ?S ?c", "?x ?y ?c" );
        assertCount( 7, "?x ?R ?y; ?a ?S ?c", "?dog" );
        assertCount( 8, "?x ?R ?y; ?a ?S ?c", "?dog ?cat ?x" );
        assertCount( 18, "?a ?b ?c; ?d ?e ?f; ?g ?h ?i; ?j ?k ?l; ?m ?n ?o; ?p ?q ?r" );
        }
    
    public void assertCount( int expected, String query )
        { assertCount( expected, query, "" ); }
        
    public void assertCount( int expected, String query, String vars )
        {
        Graph g = graphWith( "" );
        Query q = new Query();
        Triple [] triples = tripleArray( query );
        for (int i = 0; i < triples.length; i += 1) q.addMatch( triples[i] );
        q.executeBindings( g, nodes( vars ) );
        assertEquals( expected, q.getVariableCount() );
        }
        
    /**
        test that unbound constraint variables are handled "nicely".
    */  
    public void testQueryConstraintUnbound()
        {
        Query q = new Query()
            .addConstraint( Query.X, Query.NE, Query.Y )
            .addMatch( Query.X, Query.ANY, Query.X )
            ;
        Graph g = graphWith( "x R x; x R y" );
        try
            {
            ExtendedIterator it = q.executeBindings( g, new Node[] {Query.X} );
            fail( "should spot unbound variable" );
            }
        catch (Query.UnboundVariableException b) { /* as required */ }
        } 
        
    public void testCloseQuery()
        {
        Graph g = graphWith( "x R y; a P b; i L j; d X f; h S g; no more heroes" );
        for (int n = 0; n < 1000; n += 1) graphAdd( g, "ping pong X" + n );
        Query q = new Query().addMatch( Query.S, Query.P, Query.O );
        List stages = new ArrayList();
        ExtendedIterator it = q.executeBindings( g, stages, new Node [] {Query.P} );
        /* eat one answer to poke pipe */ it.next();
        for (int i = 0; i < stages.size(); i += 1) assertFalse( ((Stage) stages.get(i)).isClosed() );
        it.close();
        for (int i = 0; i < stages.size(); i += 1) assertTrue( ((Stage) stages.get(i)).isClosed() );
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
