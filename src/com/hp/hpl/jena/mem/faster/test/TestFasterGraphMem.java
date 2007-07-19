/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestFasterGraphMem.java,v 1.6 2007-07-19 13:25:50 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TestFasterGraphMem extends AbstractTestGraph
    {
    public TestFasterGraphMem( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestFasterGraphMem.class ); }
    
    public Graph getGraph()
        { return new GraphMemFaster(); }   
    
    public void testRemoveAllDoesntUseFind()
        {
        Graph g = new GraphMemWithoutFind();
        graphAdd( g, "x P y; a Q b" );
        g.getBulkUpdateHandler().removeAll();
        assertEquals( 0, g.size() );
        }
    
    public void testSizeAfterRemove() 
        {
        Graph g = getGraphWith( "x p y" );
        ExtendedIterator it = g.find( triple( "x ?? ??" ) );
        it.removeNext();
        assertEquals( 0, g.size() );        
        }
    
    public void testContainsConcreteDoesntUseFind()
        {
        Graph g = new GraphMemWithoutFind();
        graphAdd( g, "x P y; a Q b" );
        assertTrue( g.contains( triple( "x P y" ) ) );
        assertTrue( g.contains( triple( "a Q b" ) ) );
        assertFalse( g.contains( triple( "a P y" ) ) );
        assertFalse( g.contains( triple( "y R b" ) ) );
        }    
    
    public void testSingletonStatisticsWithSingleTriple()
        {
        Graph g = getGraphWith( "a P b" );
        GraphStatisticsHandler h = g.getStatisticsHandler();
        assertNotNull( h );
        assertEquals( 1L, h.getStatistics( node( "a" ), Node.ANY, Node.ANY ) );
        assertEquals( 0L, h.getStatistics( node( "x" ), Node.ANY, Node.ANY ) );
    //
        assertEquals( 1L, h.getStatistics( Node.ANY, node( "P" ), Node.ANY ) );
        assertEquals( 0L, h.getStatistics( Node.ANY, node( "Q" ), Node.ANY ) );
    //
        assertEquals( 1L, h.getStatistics( Node.ANY, Node.ANY, node( "b" ) ) );
        assertEquals( 0L, h.getStatistics( Node.ANY, Node.ANY, node( "y" ) ) );
        }
    
    public void testSingletonStatisticsWithSeveralTriples()
        {
        Graph g = getGraphWith( "a P b; a P c; a Q b; x S y" );
        GraphStatisticsHandler h = g.getStatisticsHandler();
        assertNotNull( h );
        assertEquals( 3L, h.getStatistics( node( "a" ), Node.ANY, Node.ANY ) );
        assertEquals( 1L, h.getStatistics( node( "x" ), Node.ANY, Node.ANY ) );
        assertEquals( 0L, h.getStatistics( node( "y" ), Node.ANY, Node.ANY ) );
    //
        assertEquals( 2L, h.getStatistics( Node.ANY, node( "P" ), Node.ANY ) );
        assertEquals( 1L, h.getStatistics( Node.ANY, node( "Q" ), Node.ANY ) );
        assertEquals( 0L, h.getStatistics( Node.ANY, node( "R" ), Node.ANY ) );
    //
        assertEquals( 2L, h.getStatistics( Node.ANY, Node.ANY, node( "b" ) ) );
        assertEquals( 1L, h.getStatistics( Node.ANY, Node.ANY, node( "c" ) ) );
        assertEquals( 0L, h.getStatistics( Node.ANY, Node.ANY, node( "d" ) ) );
        }
    
    public void testDoubletonStatisticsWithTriples()
        {
        Graph g = getGraphWith( "a P b; a P c; a Q b; x S y" );
        GraphStatisticsHandler h = g.getStatisticsHandler();
        assertNotNull( h );
        assertEquals( -1L, h.getStatistics( node( "a" ), node( "P" ), Node.ANY ) );
        assertEquals( -1L, h.getStatistics( Node.ANY, node( "P" ), node( "b"  ) ) );
        assertEquals( -1L, h.getStatistics( node( "a" ), Node.ANY, node( "b" ) ) );
    //
        assertEquals( 0L, h.getStatistics( node( "no" ), node( "P" ), Node.ANY ) );
        }
    
    public void testStatisticsWithOnlyVariables()
        {
        testStatsWithAllVariables( "" );
        testStatsWithAllVariables( "a P b" );
        testStatsWithAllVariables( "a P b; a P c" );
        testStatsWithAllVariables( "a P b; a P c; a Q b; x S y" );
        }

    private void testStatsWithAllVariables( String triples )
        {
        Graph g = getGraphWith( triples );
        GraphStatisticsHandler h = g.getStatisticsHandler();
        assertEquals( g.size(), h.getStatistics( Node.ANY, Node.ANY, Node.ANY ) );
        }
    
    public void testStatsWithConcreteTriple()
        {
        testStatsWithConcreteTriple( 0, "x P y", "" );
        }
    
    private void testStatsWithConcreteTriple( int expect, String triple, String graph )
        {
        Graph g = getGraphWith( graph );
        GraphStatisticsHandler h = g.getStatisticsHandler();
        Triple t = triple( triple );
        assertEquals( expect, h.getStatistics( t.getSubject(), t.getPredicate(), t.getObject() ) );
        }

    protected final class GraphMemWithoutFind extends GraphMemFaster
        {
        public ExtendedIterator graphBaseFind( TripleMatch t )
            { throw new JenaException( "find is Not Allowed" ); }
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/