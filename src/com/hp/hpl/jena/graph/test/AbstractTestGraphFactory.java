/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: AbstractTestGraphFactory.java,v 1.3 2003-05-05 10:30:59 chris-dollin Exp $
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

public abstract class AbstractTestGraphFactory extends GraphTestBase
    {
    public AbstractTestGraphFactory( String name )
            { super( name ); };
            
    public abstract GraphFactory getGraphFactory();
    
    private GraphFactory gf;
    
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
    
    /**
        Test that we can't create a graph with the same name twice. 
    */    
    public void testCannotCreateTwice()
        {
        String name = "bonsai";
        Graph g1 = gf.createGraph( name );
        try
            {
            Graph g2 = gf.createGraph( name );
            fail( "should not be able to create " + name + " twice" );
            }
        catch (AlreadyExistsException e)
            {}
        }
    
    /**
        Test that we cannot open a graph that does not exist.
    */    
    public void testCannotOpenUncreated()
        {
        testDoesNotExist( "noSuchGraph" );
        }
    
    /**
        Utility - test that a graph with the given name exists. Hackwork.
     */    
    private void testExists( String name )
        { 
        try { gf.openGraph( name ); }
        catch (DoesNotExistException e) { fail( name + " should exist" ); }
        }
        
    /**
        Utility - test that no graphb with the given name eixsts. Hackwork.
     */
    private void testDoesNotExist( String name )
        { 
        try { gf.createGraph( name ); }
        catch (AlreadyExistsException e) { fail( name + " should not exist" ); }
        }
            
    /**
        Test that we can find a graph once its been created. We need to know
        if two graphs are "the same" here, which is tricky, because the RDB
        factory produces non-== graphs that are "the same": we have a temporary
        work-around but it is not sound.
     *
     */
    public void testCanFindCreatedGraph()
        {
        String alpha = "alpha", beta = "beta";
        Graph g1 = gf.createGraph( alpha );
        Graph h1 = gf.createGraph( beta );
        Graph g2 = gf.openGraph( alpha );
        Graph h2 = gf.openGraph( beta );
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
        String alpha = "bingo", beta = "brillo";
        Graph g1 = gf.createGraph( alpha );
        Graph g2 = gf.createGraph( beta );
        testExists( alpha );
        testExists( beta );
        gf.removeGraph( alpha );
        testExists( beta );
        testDoesNotExist( alpha );
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