/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: AbstractTestGraphMaker.java,v 1.8 2003-07-24 15:29:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;

/**
    Abstract base class for testing graph factories. Subclasses define the
    method <code>getGraphFactory()</code> which supplies a new graph
    factory to be tested: ATGF invokes that during <code>setUp</code>
    and closes it in <code>tearDown</code>.
<p>
    This bunch of tests is not remotely exhaustive, but it was sufficent to
    drive the development of the first full graph factory. (Although at the time
    it wasn't abstract.)
    
 	@author hedgehog
*/

public abstract class AbstractTestGraphMaker extends GraphTestBase
    {
    public AbstractTestGraphMaker( String name )
            { super( name ); }
            
    public abstract GraphMaker getGraphFactory();
    
    private GraphMaker gf;
    
    public void setUp()
        { gf = getGraphFactory(); }
        
    public void tearDown()
        { gf.close(); }

    /**
        A trivial test that getGraph delivers a proper graph, not cheating with null.
    */
    public void testGetGraph()
        {
        Graph g1 = gf.getGraph();
        assertFalse( "should deliver a Graph", g1 == null );
        g1.close();
        }
    
    public void testAnyName()
        {
        gf.createGraph( "plain" ).close();
        gf.createGraph( "with.dot" ).close();
        gf.createGraph( "http://electric-hedgehog.net/topic#marker" ).close();
        }
        
    /**
        Test that we can't create a graph with the same name twice. 
    */    
    public void testCannotCreateTwice()
        {
        String name = jName( "bonsai" );
        gf.createGraph( name, true );
        try
            {
            gf.createGraph( name, true );
            fail( "should not be able to create " + name + " twice" );
            }
        catch (AlreadyExistsException e)
            {}
        }
        
    private String jName( String name )
        { return "jena_test_" + name; }
        
    public void testCanCreateTwice()
        {
        String name = jName( "bridge" );
        Graph g1 = gf.createGraph( name, true );
        Graph g2 = gf.createGraph( name, false );
        assertTrue( "graphs should be the same", sameGraph( g1, g2 ) );
        Graph g3 = gf.createGraph( name );
        assertTrue( "graphs should be the same", sameGraph( g1, g3 ) );
        }
    
    /**
        Test that we cannot open a graph that does not exist.
    */    
    public void testCannotOpenUncreated()
        {
        String name = jName( "noSuchGraph" );
        try { gf.openGraph( name, true );  fail( name + " should not exist" ); }
        catch (DoesNotExistException e) { }  
        }
        
    /**
        Test that we *can* open a graph that hasn't been created
    */
    public void testCanOpenUncreated()
        {
        String name = jName( "willBeCreated" );
        Graph g1 = gf.openGraph( name );
        g1.close();
        gf.openGraph( name, true );
        }
    
    /**
        Utility - test that a graph with the given name exists.
     */    
    private void testExists( String name )
        { assertTrue( name + " should exist", gf.hasGraph( name ) ); }
     
        
    /**
        Utility - test that no graph with the given name exists.
     */
    private void testDoesNotExist( String name )
        {  assertFalse( name + " should exist", gf.hasGraph( name ) ); }
            
    /**
        Test that we can find a graph once its been created. We need to know
        if two graphs are "the same" here, which is tricky, because the RDB
        factory produces non-== graphs that are "the same": we have a temporary
        work-around but it is not sound.
     *
     */
    public void testCanFindCreatedGraph()
        {
        String alpha = jName( "alpha" ), beta = jName( "beta" );
        Graph g1 = gf.createGraph( alpha, true );
        Graph h1 = gf.createGraph( beta, true );
        Graph g2 = gf.openGraph( alpha, true );
        Graph h2 = gf.openGraph( beta, true );
        assertTrue( "should find alpha", sameGraph( g1, g2 ) );
        assertTrue( "should find beta", sameGraph( h1, h2 ) );
        }
        
    /**
        Weak test for "same graph": adding this to one is visible in t'other.
        Stopgap for use in testCanFindCreatedGraph.
        TODO: clean that test up (need help from DB group)
    */
    private boolean sameGraph( Graph g1, Graph g2 )
        {
        Node S = node( "S" ), P = node( "P" ), O = node( "O" );
        g1.add( new Triple( S, P, O ) );
        g2.add( new Triple( O, P, S ) );
        return g2.contains( S, P, O ) && g1.contains( O, P, S );
        }
        
    /**
        Test that we can remove a graph from the factory without disturbing
        another graph's binding.
     */
    public void testCanRemoveGraph()
        {
        String alpha = jName( "bingo" ), beta = jName( "brillo" );
        gf.createGraph( alpha, true );
        gf.createGraph( beta, true );
        testExists( alpha );
        testExists( beta );
        gf.removeGraph( alpha );
        testExists( beta );
        testDoesNotExist( alpha );
        }
        
    public void testHasnt()
        {
        assertFalse( "no such graph", gf.hasGraph( "john" ) );
        assertFalse( "no such graph", gf.hasGraph( "paul" ) );
        assertFalse( "no such graph", gf.hasGraph( "george" ) );
    /* */
        gf.createGraph( "john", true );
        assertTrue( "john now exists", gf.hasGraph( "john" ) );
        assertFalse( "no such graph", gf.hasGraph( "paul" ) );
        assertFalse( "no such graph", gf.hasGraph( "george" ) );
    /* */    
        gf.createGraph( "paul", true );
        assertTrue( "john still exists", gf.hasGraph( "john" ) );
        assertTrue( "paul now exists", gf.hasGraph( "paul" ) );
        assertFalse( "no such graph", gf.hasGraph( "george" ) );
    /* */    
        gf.removeGraph( "john" );
        assertFalse( "john has been removed", gf.hasGraph( "john" ) );
        assertTrue( "paul still exists", gf.hasGraph( "paul" ) );
        assertFalse( "no such graph", gf.hasGraph( "george" ) );
        }
        
    public void testCarefulClose()
        {
        Graph x = gf.createGraph( "x" );
        Graph y = gf.openGraph( "x" );
        x.add( triple( "a BB c" ) );
        x.close();
        y.add( triple( "p RR q" ) );
        y.close();
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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