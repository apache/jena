/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestRDFS.java,v 1.1 2003-03-04 17:51:44 ian_dickinson Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

/**
	@author kers
*/

import junit.framework.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.compose.RDFS;
import com.hp.hpl.jena.graph.compose.RDFSOld;

import java.util.*;

public class TestRDFS extends GraphTestBase
    {
    public TestRDFS( String name )
        { super( name ); }

	public static TestSuite suite()
    	{ return new TestSuite( TestRDFS.class ); }
    	
    Graph base;
    RDFSOld derived;
    RDFS inferred;
    
    public void setUp()
        {
        base = GraphTestBase.graphWith( "miles commands mercenaries; commands rdfs:domain Leader; commands rdfs:range Troops" );
        derived = new RDFSOld( base );
        inferred = new RDFS( base );
        }
        
    public void assertHas( String title, Graph g, Triple triple )
        { assertTrue( title + ": must contain " + triple, g.contains( triple ) ); }
        
    public void assertHasnt( String title, Graph g, Triple triple )
        { assertTrue( title + ": must not contain " + triple, !g.contains( triple ) ); }
        
    public void assertHas( String title, Graph g, String t )
        { assertHas( title, g, triple( t ) ); }

    public void assertHasnt( String title, Graph g, String t )
        { assertHasnt( title, g, triple( t ) ); }
        
    public void assertHas( Graph g, Triple triple )
        {
        assertTrue( "must contain " + triple, g.contains( triple ) ); 
        }
                
    public void assertHasnt( Graph g, Triple triple )
        { assertTrue( "mustn't contain " + triple, !g.contains( triple ) ); }
        
    public void assertHas( Graph g, String triple )
        { assertHas( g, GraphTestBase.triple( triple ) ); }
        
    public void assertHasnt( Graph g, String triple )
    	{ assertHasnt( g, GraphTestBase.triple( triple ) ); }
        
    protected void checkHas( Triple triple )
        { assertHas( derived, triple ); }
        
    protected void checkHas( String triple )
        { checkHas( GraphTestBase.triple( triple ) ); }
        
    protected void checkHasnt( String triple )
        { assertHasnt( derived, triple ); }
        
    protected void add( Triple triple )
    	{ derived.add( triple ); }
        
    protected void add( String triple )
    	{ add( GraphTestBase.triple( triple ) ); }
    	
    public void testHasBasis()
        {
        checkHas( "miles commands mercenaries" );
        }
        
    public void testAddWorks()
        {
        Triple a = GraphTestBase.triple( "illyan commands intelligence" );
        Triple b = GraphTestBase.triple( "cordelia goes shopping" );
        Triple c = GraphTestBase.triple( "solettas warm planets" );
        Triple d = GraphTestBase.triple( "pilots have implants" );
        add( a );
        add( b );
        add( c );
        add( d );
        checkHas( a );
        checkHas( b );
        checkHas( c );
        checkHas( d );
        assertHasnt( derived, "illyan commands mercenaries" );
        assertHasnt( derived, "pilots warm shopping" );
        assertHasnt( derived, "spindizzies drive cities" );
        }
        
    public void testRDFSaxioms( String title, Graph x )
        {
        assertHas( title, x, "rdfs:Resource rdf:type rdfs:Class" );
        assertHas( title, x, "rdfs:Literal rdf:type rdfs:Class" );
        assertHas( title, x, "rdfs:Class rdf:type rdfs:Class" );
        assertHas( title, x, "rdf:Property rdf:type rdfs:Class" );
        assertHas( title, x, "rdf:Seq rdf:type rdfs:Class" );
        assertHas( title, x, "rdf:Bag rdf:type rdfs:Class" );
        assertHas( title, x, "rdf:Alt rdf:type rdfs:Class" );
        assertHas( title, x, "rdf:Statement rdf:type rdfs:Class" );
        assertHas( title, x, "rdf:type rdf:type rdf:Property" );
        assertHas( title, x, "rdf:type rdfs:domain rdfs:Resource" );
        assertHas( title, x, "rdf:type rdfs:range rdfs:Class" );
        assertHas( title, x, "rdfs:domain rdf:type rdf:Property" );
        assertHas( title, x, "rdfs:domain rdfs:domain rdf:Property" );
        assertHas( title, x, "rdfs:domain rdfs:range rdfs:Class" );
        assertHas( title, x, "rdfs:range rdf:type rdf:Property" );
        assertHas( title, x, "rdfs:range rdfs:domain rdf:Property" );
        assertHas( title, x, "rdfs:range rdfs:range rdfs:Class" );
        assertHas( title, x, "rdfs:subPropertyOf rdf:type rdf:Property" );
        assertHas( title, x, "rdfs:subPropertyOf rdfs:domain rdf:Property" );
        assertHas( title, x, "rdfs:subPropertyOf rdfs:range rdf:Property" );
        assertHas( title, x, "rdfs:subClassOf rdf:type rdf:Property" );
        assertHas( title, x, "rdfs:subClassOf rdfs:domain rdfs:Class" );
        assertHas( title, x, "rdfs:subClassOf rdfs:range rdfs:Class" );
        assertHas( title, x, "rdf:subject rdf:type rdf:Property" );
        assertHas( title, x, "rdf:subject rdfs:domain rdf:Statement" );
        assertHas( title, x, "rdf:predicate rdf:type rdf:Property" );
        assertHas( title, x, "rdf:predicate rdfs:domain rdf:Statement" );
        assertHas( title, x, "rdf:object rdf:type rdf:Property" );
        assertHas( title, x, "rdf:object rdfs:domain rdf:Statement" );
         }
         
     public void testRDFSaxioms()
        {
        testRDFSaxioms( "old RDFS", derived );
        testRDFSaxioms( "new RDFS", inferred );
        }
        
     private HashSet setFrom( String names )
     	{
     	HashSet result = new HashSet();
     	StringTokenizer st = new StringTokenizer( names );
        while (st.hasMoreTokens()) result.add( node( st.nextToken() ) );
     	return result;
     	}
     	
     public void testSuperClassMapping()
     	{
     	add( "Spinach rdfs:subClassOf Vegetable" );
     	HashMap map = derived.superClasses();
     	//System.err.println( "| subclass map is " + map );
     	assertEquals( "", setFrom( "Vegetable rdfs:Resource" ), map.get( node("Spinach") )  );
     	}
     	
     public void testImplicitProperty()
     	{
        assertHas( "old RDFS", derived, "commands rdf:type rdf:Property" );
        assertHas( "new RDFS", inferred, "commands rdf:type rdf:Property" );
     	assertHasnt( "old RDFS", derived, "miles rdf:type rdf:Property" );
        assertHasnt( "new RDFS", inferred, "miles rdf:type rdf:Property" );
     	}
     	
     public void testAddSpotsImplicitProperty()
     	{
     	assertHasnt( "old RDFS", derived, "goes rdf:type rdf:Property" );
        assertHasnt( "new RDFS", inferred, "goes rdf:type rdf:Property" );
     	add( "cordelia goes shopping" );
     	assertHas( "old RDFS", derived, "goes rdf:type rdf:Property" );
        assertHas( "new RDFS", inferred, "goes rdf:type rdf:Property" );
     	}
     	
  	public void testForResources( String title, Graph g )
  		{
 		assertHasnt( title, g, "unmentioned rdf:type rdfs:Resource" );
  		assertHas( title, g, "miles rdf:type rdfs:Resource" );
  		assertHas( title, g, "commands rdf:type rdfs:Resource" );
  		assertHas( title, g, "mercenaries rdf:type rdfs:Resource" );
  		}
        
    public void testForResources()
        {
        testForResources( "old RDFS", derived );
        testForResources( "new RDFS", inferred );
        }
        
    public void testSubPropertyTransitive( String title, Graph g )
        {
        assertHasnt( title, g, "alpha rdfs:subPropertyOf gamma" );
        add( "alpha rdfs:subPropertyOf beta" );
        add( "beta rdfs:subPropertyOf gamma" );
        assertHas( title, g, "alpha rdfs:subPropertyOf beta" );
        assertHas( title, g, "beta rdfs:subPropertyOf gamma" );
        assertHas( title, g, "alpha rdfs:subPropertyOf gamma" );
        }
        
    public void testSubPropertyTransitive()
        {
        testSubPropertyTransitive( "old RDFS", derived );
        // testSubPropertyTransitive( "new RDFS", inferred );
        }
        
    public void testSubClassTransitive()
        {
        checkHasnt( "alpha rdfs:subClassOf gamma" );
        add( "alpha rdfs:subClassOf beta" );
        add( "beta rdfs:subClassOf gamma" );
        checkHas( "alpha rdfs:subClassOf beta" );
        checkHas( "beta rdfs:subClassOf gamma" );
        checkHas( "alpha rdfs:subClassOf gamma" );          
        }
        
    public void testDomain()
        {
        checkHas( "miles rdf:type Leader" );
        }        
        
    private ArrayList toArray( Iterator it )
    	{
    	ArrayList result = new ArrayList();
    	while (it.hasNext()) result.add( it.next() );
    	return result;
    	}
        
    private static class CI extends NiceIterator implements ClosableIterator
    	{
    	private Iterator it;
    	public CI( Iterator it ) { this.it = it; }
    	public boolean hasNext() { return it.hasNext(); }
    	public Object next() { return it.next(); }
    	public void remove() { it.remove(); }
    	public void close() {}
    	}
    	
    private static class XY { int x; String y; XY(int x, String y) { this.x = x; this.y = y; } }
    
    public void testMultiply()
    	{
    	XY [] vals = 
    		{ 
    		new XY(0, "a"), 
    		new XY(1,"b"), 
    		new XY(2,"c"), 
    		new XY(1,"d"), 
    		new XY(2,"e"), 
    		new XY(0,"f") 
    		};
    	List wanted = Arrays.asList( new Object [] {"b", "c", "c", "d", "e", "e"} );
	/* */
    	MapFiller sm = new MapFiller()
    		{
    		public boolean refill( Object x, ArrayList a )
    			{
    			XY xy = (XY) x;
    			for (int i = 0; i < xy.x; i += 1) a.add( xy.y );
    			return true;
    			}
    		};
    	ArrayList them = toArray( new MapMany( new CI(Arrays.asList( vals ).iterator()), sm ) );
    	assertEquals( "multiple value iterator map failed", wanted, them );
    	}
    	
    public void testDomainExtraction()
    	{
		Graph g = GraphTestBase.graphWith( "bill watches ben; watches rdfs:domain Watcher" );
		Triple that = new Triple( GraphTestBase.node("bill"), RDFSOld.rdfType, GraphTestBase.node("Watcher") );
		ClosableIterator it = RDFSOld.typedByDomain( g );
		Set expected = new HashSet( Arrays.asList( new Triple [] {that} ) );
		Set found = new HashSet();
		while (it.hasNext()) found.add( it.next() );
		assertEquals( "simple domain extraction", expected, found );		
    	}
    	
    public void testRangeExtraction()
    	{
		Graph g = GraphTestBase.graphWith( "bill watches ben; watches rdfs:range Target" );
		Triple that = new Triple( GraphTestBase.node("ben"), RDFSOld.rdfType, GraphTestBase.node("Target") );
		ClosableIterator it = RDFSOld.typedByRange( g );
		Set expected = new HashSet( Arrays.asList( new Triple [] {that} ) );
		Set found = new HashSet();
		while (it.hasNext()) found.add( it.next() );
		assertEquals( "simple range extraction", expected, found );		
    	}
    	
    public void testRange()
        {
        checkHas( "mercenaries rdf:type Troops" );
        }
        
    public void testSubclass()
    	{
		add( "Spinach rdfs:subClassOf Vegetable" );
		add( "Popeye's rdfs:subClassOf Spinach" );
		add( "this rdf:type Spinach" );    	
		add( "that rdf:type Popeye's" );
		assertContainsAll( "", derived, "this rdf:type Vegetable; that rdf:type Vegetable; that rdf:type Spinach" );
		assertOmits( "", derived, "this rdf:type Popeye's" );
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
