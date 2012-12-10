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

package com.hp.hpl.jena.mem.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.mem.GraphMem ;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TestGraphMem extends AbstractTestGraph
    {
    public TestGraphMem( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestGraphMem.class ); }
    
    @Override public Graph getGraph()
        { return new GraphMem(); }   

    public void testSizeAfterRemove() 
        {
        Graph g = getGraphWith( "x p y" );
        ExtendedIterator<Triple> it = g.find( triple( "x ?? ??" ) );
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
        assertEquals( 1L, h.getStatistic( node( "a" ), Node.ANY, Node.ANY ) );
        assertEquals( 0L, h.getStatistic( node( "x" ), Node.ANY, Node.ANY ) );
    //
        assertEquals( 1L, h.getStatistic( Node.ANY, node( "P" ), Node.ANY ) );
        assertEquals( 0L, h.getStatistic( Node.ANY, node( "Q" ), Node.ANY ) );
    //
        assertEquals( 1L, h.getStatistic( Node.ANY, Node.ANY, node( "b" ) ) );
        assertEquals( 0L, h.getStatistic( Node.ANY, Node.ANY, node( "y" ) ) );
        }
    
    public void testSingletonStatisticsWithSeveralTriples()
        {
        Graph g = getGraphWith( "a P b; a P c; a Q b; x S y" );
        GraphStatisticsHandler h = g.getStatisticsHandler();
        assertNotNull( h );
        assertEquals( 3L, h.getStatistic( node( "a" ), Node.ANY, Node.ANY ) );
        assertEquals( 1L, h.getStatistic( node( "x" ), Node.ANY, Node.ANY ) );
        assertEquals( 0L, h.getStatistic( node( "y" ), Node.ANY, Node.ANY ) );
    //
        assertEquals( 2L, h.getStatistic( Node.ANY, node( "P" ), Node.ANY ) );
        assertEquals( 1L, h.getStatistic( Node.ANY, node( "Q" ), Node.ANY ) );
        assertEquals( 0L, h.getStatistic( Node.ANY, node( "R" ), Node.ANY ) );
    //
        assertEquals( 2L, h.getStatistic( Node.ANY, Node.ANY, node( "b" ) ) );
        assertEquals( 1L, h.getStatistic( Node.ANY, Node.ANY, node( "c" ) ) );
        assertEquals( 0L, h.getStatistic( Node.ANY, Node.ANY, node( "d" ) ) );
        }
    
    public void testDoubletonStatisticsWithTriples()
        {
        Graph g = getGraphWith( "a P b; a P c; a Q b; x S y" );
        GraphStatisticsHandler h = g.getStatisticsHandler();
        assertNotNull( h );
        assertEquals( -1L, h.getStatistic( node( "a" ), node( "P" ), Node.ANY ) );
        assertEquals( -1L, h.getStatistic( Node.ANY, node( "P" ), node( "b"  ) ) );
        assertEquals( -1L, h.getStatistic( node( "a" ), Node.ANY, node( "b" ) ) );
    //
        assertEquals( 0L, h.getStatistic( node( "no" ), node( "P" ), Node.ANY ) );
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
        assertEquals( g.size(), h.getStatistic( Node.ANY, Node.ANY, Node.ANY ) );
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
        assertEquals( expect, h.getStatistic( t.getSubject(), t.getPredicate(), t.getObject() ) );
        }

    protected final class GraphMemWithoutFind extends GraphMem
        {
        @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch t )
            { throw new JenaException( "find is Not Allowed" ); }
        }
    }
