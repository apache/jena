  /*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: AbstractTestQuery.java,v 1.32 2004-11-02 15:57:11 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.impl.*;

import java.util.*;
import junit.framework.*;

/**
    Abstract tests for graph query, parameterised on getGraph().
 	@author kers
*/
public abstract class AbstractTestQuery extends QueryTestBase
    {
    public AbstractTestQuery(String name)
        { super(name); }

    public abstract Graph getGraph();

    protected Query Q;
    protected Node O = node( "?O" );
    protected Graph empty;
    protected Graph single;
    
    protected final Node [] none = new Node[] {};
    
    protected final Node [] justX = new Node [] {Query.X};
    
    public static TestSuite suite()
        { return new TestSuite( QueryTest.class ); }
        
    public Graph getGraphWith( String facts )
        { return graphAdd( getGraph(), facts ); }
        
    public void setUp()
        {
		Q = new Query();
		empty = getGraphWith( "" );
		single = getGraphWith( "spindizzies drive cities" );
		}

    private void testTreeQuery( String title, String content, String pattern, String correct )
        {
        Graph gc = getGraphWith( content ), gp = getGraphWith( pattern );
        Graph answer = gc.queryHandler().prepareTree( gp ).executeTree();
        if (title.equals( "" )) title = "checking {" + content + "} against {" + pattern + "} should give {" + correct + "}" + " not " + answer;
        assertIsomorphic( title, getGraphWith( correct ), answer );
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
        
    public void testEmptyIterator()
        {
        Graph empty = getGraph();
        Query q = new Query().addMatch( X, Y, Z );
        BindingQueryPlan bqp = empty.queryHandler().prepareBindings( q, justX );
        try
            {
            bqp.executeBindings().next();
            fail( "there are no bindings; next() should fail" );    
            }    
        catch (NoSuchElementException e)
            { pass(); }
        }
        
    public void testBinding1( )
        {
        Graph single = getGraphWith( "rice grows quickly" );
        Query q = new Query();
        Node V1 = node( "?v1" ), V3 = node( "?v3" );
        BindingQueryPlan qp = single.queryHandler().prepareBindings( q.addMatch( V1, node("grows"), V3 ), new Node[] {V1, V3} );
        Domain binding = (Domain) qp.executeBindings().next();
        assertEquals( "binding subject to rice", binding.get(0), node("rice") ); 
        assertEquals( "binding object to quickly", binding.get(1), node("quickly") ); 
        }

	public void testBinding2() { 
		Graph several = getGraphWith("rice grows quickly; time isan illusion");
		String[][] answers = { { "time", "isan", "illusion" }, {
				"rice", "grows", "quickly" }
		};
		boolean[] found = { false, false };
		Query q = new Query();
		Node V1 = node("?v1"), V2 = node("?v2"), V3 = node("?v3");
		BindingQueryPlan qp =
			several.queryHandler().prepareBindings(
				q.addMatch(V1, V2, V3),
				new Node[] { V1, V2, V3 });
		Iterator bindings = qp.executeBindings();
		for (int i = 0; i < answers.length; i += 1) {
			if (bindings.hasNext() == false)
				fail("wanted some more results");
			Domain bound = (Domain) bindings.next();
			for (int k = 0; k < answers.length; k++) {
				if (found[k])
					continue;
				boolean match = true;
				for (int j = 0; j < 3; j += 1) {
					if (!bound.get(j).equals(node(answers[k][j]))) {
						match = false;
						break;
					}
				}
				if (match) {
					found[k] = true;
					break;
				}
			}
		}
		for (int k = 0; k < answers.length; k++) {
			if (!found[k])
				assertTrue("binding failure", false);
		}
		assertFalse("iterator should be empty", bindings.hasNext());
	}


    public void testMultiplePatterns()
        {
        Graph bookish = getGraphWith( "ben wrote Clayface; Starfish ingenre SF; Clayface ingenre Geology; bill wrote Starfish" );
        Query q = new Query();
        Node A = node( "?A" ); 
        q.addMatch(  X, node("wrote"), A ).addMatch(  A, node("ingenre"), node("SF") );
        BindingQueryPlan qp = bookish.queryHandler().prepareBindings( q, justX );
        Iterator bindings = qp.executeBindings();
        if (bindings.hasNext())
            {
            Domain it = (Domain) bindings.next();
            if (it.size() > 0)
                {
                if (it.get(0).equals( node("bill") ))
                    {
                    if (bindings.hasNext())
                        System.out.println( "! failed: more than one multiple pattern answer: " + bindings.next() );
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
    
    /**
    	Utility. Run the query <code>q</code> over the graph <code>g</code> 
        requesting the output variables <code>nodes</code>.
     */
    protected ExtendedIterator eb( Graph g, Query q, Node [] nodes )
        { return g.queryHandler().prepareBindings( q, nodes ).executeBindings(); }
    
    protected List ebList( Graph g, Query q, Node [] nodes )
        { return iteratorToList( eb( g, q, nodes ) ); }
    
    protected Set ebSet( Graph g, Query q, Node [] nodes )
        { return iteratorToSet( eb( g, q, nodes ) ); }
        
    public void testNodeVariablesA()
        {
        Graph mine = getGraphWith( "storms hit England" );
        Node spoo = node( "?spoo" );
        Q.addMatch( spoo, node("hit"), node("England") );
        ClosableIterator it = eb( mine, Q, new Node[] {spoo} ); 
        assertTrue( "tnv: it has a solution", it.hasNext() );
        assertEquals( "", node("storms"), ((List) it.next()).get(0) );
        assertFalse( "tnv: just the one solution", it.hasNext() );
        }
   
    public void testNodeVariablesB()
        {
        Graph mine = getGraphWith( "storms hit England" );
        Node spoo = node( "?spoo" ), flarn = node( "?flarn" );
        Q.addMatch( spoo, node("hit"), flarn );
        ClosableIterator it = eb( mine, Q, new Node[] {flarn, spoo} );
        assertTrue( "tnv: it has a solution", it.hasNext() );
        List answer = (List) it.next();
        assertEquals( "tnvB", node("storms"), answer.get(1) );
        assertEquals( "tnvB", node("England"), answer.get(0) );
        assertFalse( "tnv: just the one solution", it.hasNext() );
        }

    public void testBindingQuery()
        {
        Graph empty = getGraphWith( "" );
        Graph base = getGraphWith( "pigs might fly; cats chase mice; dogs chase cars; cats might purr" );
    /* */
        Query any = new Query().addMatch( Query.ANY, Query.ANY, Query.ANY );
        assertFalse( "empty graph, no bindings", eb( empty, any, none ).hasNext() );
        assertTrue( "full graph, > 0 bindings", eb( base, new Query(), none ).hasNext() );
        }

    public void testEmpty()
        {
        List bindings = ebList( empty, Q, none );
        assertEquals( "testEmpty: select [] from {} => 1 empty binding [size]", bindings.size(), 1 );
        Domain d = (Domain) bindings.get( 0 );
        assertEquals( "testEmpty: select [] from {} => 1 empty binding [width]", d.size(), 0 );
        }
        
    public void testOneMatch()
        {
        Q.addMatch( X, Query.ANY, Query.ANY );
        List bindings = ebList( single, Q, justX ); 
        assertEquals( "select X from {spindizzies drive cities} => 1 binding [size]", bindings.size(), 1 );
        Domain d = (Domain) bindings.get( 0 );
        assertEquals( "select X from {spindizzies drive cities} => 1 binding [width]", d.size(), 1 );
        assertTrue( "select X from {spindizzies drive cities} => 1 binding [value]", d.get( 0 ).equals( node( "spindizzies" ) ) );
        }
        
    public void testMismatch()
        {
        Q.addMatch( X, X, X );
        List bindings = ebList( single, Q, justX );
        assertEquals( "bindings mismatch (X X X)", bindings.size(), 0 );
        }
        
    public void testXXXMatch1()
        {
        Q.addMatch( X, X, X );
        Graph xxx = getGraphWith( "ring ring ring" );
        List bindings = ebList( xxx, Q, justX ); 
        assertEquals( "bindings match (X X X)", bindings.size(), 1 );       
        }

    public void testXXXMatch3()
        {
        Q.addMatch( X, X, X );
        Graph xxx = getGraphWith( "ring ring ring; ding ding ding; ping ping ping" );
        List bindings = ebList( xxx, Q, justX ); 
        assertEquals( "bindings match (X X X)", bindings.size(), 3 );       
    /* */
        Set found = HashUtils.createSet();
        for (int i = 0; i < bindings.size(); i += 1) 
            {
            Domain d = (Domain) bindings.get( i );
            assertEquals( "one bound variable", d.size(), 1 );
            found.add( d.get( 0 ) );
            }
        Set wanted = nodeSet( "ring ding ping" ); 
        assertEquals( "testMatch getting {ring ding ping}", found, wanted );
        }
        
    public void testTwoPatterns()
        {
        Node reads = node("reads"), inGenre = node("inGenre");
        Graph g = getGraphWith( "chris reads blish; blish inGenre SF" );
        // System.err.println( "| X = " + X + ", Y = " + Y + ", Z = " + Z );
        Q.addMatch( X, reads, Y );
        Q.addMatch( Y, inGenre, Z );
        List bindings = ebList( g, Q, new Node [] {X, Z} ); 
        assertTrue( "testTwoPatterns: one binding", bindings.size() == 1 );
        Domain  d = (Domain) bindings.get( 0 );
        // System.out.println( "* width = " + d.width() );
        assertTrue( "testTwoPatterns: width 2", d.size() >= 2 );
        assertEquals( "testTwoPatterns: X = chris", d.get(0), node("chris") );
        assertEquals( "testTwoPatterns: Y = SF", d.get(1), node("SF") );
        }
        
    public void testGraphQuery()
        {
        Graph pattern = getGraphWith( "?X reads ?Y; ?Y inGenre ?Z" );
        Graph target = getGraphWith( "chris reads blish; blish inGenre SF" );
        // System.err.println( "| pattern: " + pattern );
        Query q = new Query( pattern );
        List bindings = ebList( target, q, new Node [] {node("?X"), node("?Z")} ); 
        assertEquals( "testTwoPatterns: one binding", 1, bindings.size() );
        Domain  d = (Domain) bindings.get( 0 );
        // System.out.println( "* width = " + d.width() );
        assertTrue( "testTwoPatterns: width 2", d.size() >= 2 );
        assertEquals( "testTwoPatterns: X = chris", d.get(0), node("chris") );
        assertEquals( "testTwoPatterns: Y = SF", d.get(1), node("SF") );        
        }
                
    public void testTwoGraphs()
        {
        Graph a = getGraphWith( "chris reads blish; chris reads norton; chris eats curry" );
        Graph b = getGraphWith( "blish inGenre SF; curry inGenre food" );
        Node reads = node("reads"), inGenre = node("inGenre");
        Q.addMatch( "a", X, reads, Y ).addMatch( "b", Y, inGenre, Z );
        NamedGraphMap args = Q.args().put( "a", a ).put( "b", b );
        List bindings = iteratorToList( Q.executeBindings( args, new Node [] {X, Z} ) ); // TODO
        assertEquals( "testTwoGraphs: one binding", bindings.size(), 1 );
        Domain  d = (Domain) bindings.get( 0 );
        assertTrue( "testTwoGraphs: width 2", d.size() >= 2 );
        assertEquals( "testTwoGraphs: X = chris", d.get(0), node("chris") );
        assertEquals( "testTwoGraphs: Y = SF", d.get(1), node("SF") );     
        }
                
    public void testGraphConstraints( String title, Expression constraint, String wanted )
        { 
        Query Q = new Query()
            .addMatch( Query.ANY, Query.ANY, O )
            .addConstraint( constraint );
        Graph G = getGraphWith( "pigs fly south; dogs fly badly; plans fly flat" );
        Set results = iteratorToSet( eb( G, Q, new Node[] {O} ).mapWith( getFirst ) );
        assertEquals( "tgs", nodeSet( wanted ), results );
        }
        
     public void testGraphConstraints()
        {
        Node badly = node( "badly" ), flat = node( "flat" );
        testGraphConstraints( "tgs A", Expression.TRUE, "south flat badly" );
        testGraphConstraints( "tgs B", notEqual( O, badly ), "south flat" );
        testGraphConstraints( "tgs C", Dyadic.and( notEqual( O, badly ), notEqual( O, flat ) ), "south" );
        }
        
    private void helpConstraint( String title, Expression constraints, int n )
        {
        Query q = new Query();
        Graph g = getGraphWith( "blish wrote CIF; blish wrote VOR; hambly wrote Darwath; feynman mechanicked quanta" );
        q.addMatch( X, node("wrote"), Query.ANY );
        q.addConstraint( constraints );
        List bindings = ebList( g, q, justX ); 
        assertEquals( "testConstraint " + title + ": number of bindings", n, bindings.size() );
        }
        
    public void testConstraint()
        {
        helpConstraint( "none", Expression.TRUE, 3 );
        helpConstraint( "X /= blish", notEqual( X, node( "blish" ) ), 1 ); 
        helpConstraint( "X /= blish & X /= hambly", Dyadic.and( notEqual( X, node( "blish" ) ), notEqual( X, node( "hambly" ) ) ), 0 ); 
        }

    private void helpConstraintThree( String title, Expression c, int n )
        {           
        Query q = new Query();
        Graph g = getGraphWith( "brust wrote jhereg; hedgehog hacked code; angel age 230; brust wrote 230" );
        q.addConstraint( c );
        q.addMatch( X, Y, Z );
        List bindings = ebList( g, q, new Node [] {X, Z} );
        assertEquals( "testConstraint " + title + ": number of bindings", n, bindings.size() );         
        }
        
    public void testConstraintThree()
        {
        helpConstraintThree( "testConstraintThree 1:", areEqual( X, node( "brust" ) ), 2 );
        helpConstraintThree( "testConstraintThree 2:", areEqual( Y, node( "hacked" ) ), 1 );
        helpConstraintThree( "testConstraintThree 3:", areEqual( Z, node( "230" ) ), 2 );
       }
       
   public void testConstraintFour()
        {
        Query q = new Query();
        Graph g = getGraphWith( "bill pinged ben; ben pinged weed; weed pinged weed; bill ignored bill" );
        q.addMatch( X, node("pinged"), Y );
        q.addConstraint( notEqual( X, Y ) );
        Set bindings = iteratorToSet( eb( g, q, justX ).mapWith(getFirst) );
        assertEquals( arrayToSet( new Node[] {node("bill"), node("ben")} ), bindings );
        }
       
   /**
        Test that the MATCHES constraint works.
    */
   public void testMatchConstraint()
        {
        Set expected = HashUtils.createSet();
        expected.add( node( "beta" ) );
        Query q = new Query()  
            .addMatch( X, node( "ppp" ), Y ).addConstraint( matches( Y, node( "'ell'" ) ) ) 
            ;
        Graph g = getGraphWith( "alpha ppp beta; beta ppp 'hello'; gamma ppp 'goodbye'" );
        Set bindings = iteratorToSet( eb( g, q, justX ).mapWith( getFirst ) ); 
        assertEquals( expected, bindings );
        }
        
    /**
        Test that a PatternStage extracts appropriate parts of a constraint set.
    */
    public void testExtractConstraint()
        {
            // Surely there should be something here?
        }
        
    public void testStringResults()
        {
        Graph g = getGraphWith( "ding dong dilly" );
        Query q = new Query() .addMatch( X, Y, Query.ANY );
        List bindings = ebList( g, q, new Node [] {X, Y} );
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
        Graph g = getGraphWith( "x y z" );
        List bindings = ebList( g, Q, new Node [] {X, Y} ); 
        List L = (List) bindings.get(0);
        assertEquals( "undefined variables get null", null, L.get( 0 ) );
        }
        
    /**
        More of an example than a test, for a query with "disconnected" triples.
    */
    public void testDisconnected()
        {
        Graph g = getGraphWith( "x pred1 foo; y pred2 bar" );
        Query q = new Query( getGraphWith( "?X ?? foo; ?Y ?? bar" ) );
        List bindings = ebList( g, q, nodes( "?X ?Y" ) );
        assertEquals( 1, bindings.size() );
        assertEquals( node( "x" ), ((Domain) bindings.get(0)).get(0) );
        assertEquals( node( "y" ), ((Domain) bindings.get(0)).get(1) );
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
            public ExtendedIterator graphBaseFind( TripleMatch tm )
                { return NullIterator.instance; }
            public QueryHandler queryHandler()
                {
                return new SimpleQueryHandler( this )
                    {
                    public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] t )
                        {
                        if (t.length > 1) tripleses[0] = t;
                        return super.patternStage( map, constraints, t );
                        }
                    }
                    ;
                }
            };
        for (int i = 0; i < desired.size(); i += 1) q.addMatch( (Triple) desired.get(i) );
        eb( g, q, none ); 
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
        Graph g = getGraphWith( "" );
        Query q = new Query();
        Triple [] triples = tripleArray( query );
        for (int i = 0; i < triples.length; i += 1) q.addMatch( triples[i] );
        // eb( g, q, nodes( vars ) ); 
        q.executeBindings( g, nodes( vars ) );
        assertEquals( expected, q.getVariableCount() );
        }
        
    /**
        test that unbound constraint variables are handled "nicely".
    */  
    public void testQueryConstraintUnbound()
        {
        Query q = new Query()
            .addConstraint( notEqual( X, Z ) )
            .addMatch( X, Query.ANY, X )
            ;
        Graph g = getGraphWith( "x R x; x R y" );
        try
            {
            ExtendedIterator it = eb( g, q, justX );
            fail( "should spot unbound variable" );
            }
        catch (Query.UnboundVariableException b) { pass(); }
        } 
        
    public void testCloseQuery()
        {
        Graph g = getGraphWith( "x R y; a P b; i L j; d X f; h S g; no more heroes" );
        for (int n = 0; n < 1000; n += 1) graphAdd( g, "ping pong X" + n );
        Query q = new Query().addMatch( Query.S, Query.P, Query.O );
        List stages = new ArrayList();
        ExtendedIterator it = eb( g, q, nodes( "?P" ) ); 
        /* eat one answer to poke pipe */ it.next();
        for (int i = 0; i < stages.size(); i += 1) assertFalse( ((Stage) stages.get(i)).isClosed() );
        it.close();
        for (int i = 0; i < stages.size(); i += 1) assertTrue( ((Stage) stages.get(i)).isClosed() );
        }
        
    public void testRewriteStartswithExpression()
        {
        Query q = new Query();
        Expression L = constant( "x" );
        Expression R = createSimplePattern( "^begins" );
        Expression provided = dyadic( L, "Q_StringMatch", R );
        Expression desired = dyadic( L, "J_startsWith", constant( "begins" ) );
        q.addConstraint( provided );
        Expression e2 = (Expression) q.getConstraints().iterator().next();
        assertEquals( desired, e2 );
        }

    public void testRewriteStartswithInsensitiveExpression()
        {
        Query q = new Query();
        Expression L = constant( "x" );
        Expression R = createModifiedPattern( "^begins", "i" );
        Expression provided = dyadic( L, "Q_StringMatch", R );
        Expression desired = dyadic( L, "J_startsWithInsensitive", constant( "begins" ) );
        q.addConstraint( provided );
        Expression e2 = (Expression) q.getConstraints().iterator().next();
        assertEquals( desired, e2 );
        }

    public void testRewriteEndswithExpression()
        {
        Query q = new Query();
        Expression L = constant( "x" );
        Expression R = createSimplePattern( "ends$" );
        Expression provided = dyadic( L, "Q_StringMatch", R );
        Expression desired = dyadic( L, "J_endsWith", constant( "ends" ) );
        q.addConstraint( provided );
        Expression e2 = (Expression) q.getConstraints().iterator().next();
        assertEquals( desired, e2 );
        }
   
    public void testRewriteEndswithInsensitiveExpression()
        {
        Query q = new Query();
        Expression L = constant( "x" );
        Expression R = createModifiedPattern( "ends$", "i" );
        Expression provided = dyadic( L, "Q_StringMatch", R );
        Expression desired = dyadic( L, "J_endsWithInsensitive", constant( "ends" ) );
        q.addConstraint( provided );
        Expression e2 = (Expression) q.getConstraints().iterator().next();
        assertEquals( desired, e2 );
        }
   
    public void testRewriteContainsExpression()
        {
        Query q = new Query();
        Expression L = constant( "x" );
        Expression R = createSimplePattern( "contains" );
        Expression provided = dyadic( L, "Q_StringMatch", R );
        Expression desired = dyadic( L, "J_contains", constant( "contains" ) );
        q.addConstraint( provided );
        Expression e2 = (Expression) q.getConstraints().iterator().next();
        assertEquals( desired, e2 );
        }    
    
    public void testRewriteContainsInsensitiveExpression()
        {
        Query q = new Query();
        Expression L = constant( "x" );
        Expression R = createModifiedPattern( "coNtaIns", "i" );
        Expression provided = dyadic( L, "Q_StringMatch", R );
        Expression desired = dyadic( L, "J_containsInsensitive", constant( "contains" ) );
        q.addConstraint( provided );
        Expression e2 = (Expression) q.getConstraints().iterator().next();
        assertEquals( desired, e2 );
        }
    
    protected static class PL extends Expression.Fixed implements PatternLiteral
        {
        protected String modifiers = "";
        public PL( String content ) { super( content ); }
        public PL( String content, String modifiers ) { super( content ); this.modifiers = modifiers; }
        public String getPatternString() { return (String) value; }
        public String getPatternModifiers() { return modifiers; }
        public String getPatternLanguage() { return rdql; }
        }
    
    public Expression createSimplePattern( final String p )
        { return new PL( p ); }
    
    public Expression createModifiedPattern( String content, String modifiers )
        { return new PL( content, modifiers ); }
    
    private Expression constant( final Object it )
        { return new Expression.Fixed( it ); }

    private Expression dyadic( Expression l, String op, Expression r )
        {
        final String f = ExpressionFunctionURIs.prefix + op;
        return new Dyadic( l, f, r )
            {
            public boolean evalBool( Object l, Object r )
                { return false; }
            };
        }

    /**
        Test that a variety of triple-sorters make no difference to the results of a query
        over a moderately interesting graph.
    */
    public void testTripleSorting()
        {
        Graph g = dataGraph();
        Map answer = getAnswer( g, TripleSorter.dontSort );
        assertEquals( 1, answer.size() );
        assertEquals( new Integer(1), answer.get( Arrays.asList( nodes( "a d" ) ) ) );
    /* */
        assertEquals( answer, getAnswer( g, TripleSorter.dontSort ) );
        assertEquals( answer, getAnswer( g, fiddle( 0, 2, 1 ) ) );
        assertEquals( answer, getAnswer( g, fiddle( 1, 0, 2 ) ) );
        assertEquals( answer, getAnswer( g, fiddle( 1, 2, 0 ) ) );
        assertEquals( answer, getAnswer( g, fiddle( 2, 1, 0 ) ) );
        assertEquals( answer, getAnswer( g, fiddle( 2, 0, 1 ) ) );
        }
        
    protected TripleSorter fiddle( final int a, final int b, final int c )
        {
        return new TripleSorter()
            {
            public Triple [] sort( Triple [] triples )
                { return new Triple[] {triples[a], triples[b], triples[c]}; }    
            };    
        }

    protected Graph dataGraph()
        {
        Graph result = getGraph();
        graphAdd( result, "a SPOO d; a X b; b Y c" );
        return result;
        }
        
    protected Map getAnswer( Graph g, TripleSorter sorter )
        {
        Map result = HashUtils.createMap();
        Query q = new Query();
        q.addMatch( triple( "?a ?? ?d " ) ).addMatch( triple( "?a X ?b" ) ).addMatch( triple( "?b Y ?c" ) );
        q.addConstraint( notEqual( node( "?d" ), node( "?b" ) ) );
        Node [] answers = nodes( "?a ?d" );
        q.setTripleSorter( sorter );
        ExtendedIterator it = eb( g, q, answers );     
        while (it.hasNext()) addAnswer( result, (List) it.next(), answers.length );
        return result;
        }
        
    protected void addAnswer( Map result, List bindings, int limit )
        {
        List key = bindings.subList( 0, limit );
        Integer already = (Integer) result.get( key );
        if (already == null) already = new Integer( 0 );
        result.put( key, new Integer( already.intValue() + 1 ) );  
        }
    
    public void testQueryOptimisation()
        {
        int dontCount = queryCount( TripleSorter.dontSort );
        int optimCount = queryCount( new SimpleTripleSorter() );
        // System.err.println( ">> dontCount=" + dontCount + " optimCount=" + optimCount );
        if (optimCount > dontCount) 
            fail( "optimisation " + optimCount + " yet plain " + dontCount );   
        }
      
    int queryCount( TripleSorter sort )
        {
        CountingGraph g = bigCountingGraph();
        for (int a = 0; a < 10; a += 1)
            for (int b = 0; b < 10; b += 1)
                for (int X = 0; X < 3; X += 1)
                    graphAdd( g, "a" + a + " X" + (X == 0 ? "" : X + "") + " b" + b );
        graphAdd( g, "a SPOO d; a X b; b Y c" );
        getAnswer( g, sort );
        return g.getCount();
        }
        
    static class CountingGraph extends WrappedGraph
        {
        int counter;
        private QueryHandler qh;
        public QueryHandler queryHandler( ) { return qh; }
        CountingGraph( Graph base ) 
            { super( base ); qh = new SimpleQueryHandler( this ); }
        public ExtendedIterator find( Node s, Node p, Node o ) 
            { return find( Triple.createMatch( s, p, o )  ); }
        public ExtendedIterator find( TripleMatch tm )
            { return count( base.find( tm ) ); }
        ExtendedIterator count( ExtendedIterator it )
            {
            return new WrappedIterator( it )
                { 
                public Object next() { try { return super.next(); } finally { counter += 1; } }
                };        
            }
        int getCount() 
            { return counter; }
        public String toString()
            { return base.toString(); }
        }
        
    CountingGraph bigCountingGraph()
        {
        Graph bigGraph = getGraph();
        return new CountingGraph( bigGraph );    
        }
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