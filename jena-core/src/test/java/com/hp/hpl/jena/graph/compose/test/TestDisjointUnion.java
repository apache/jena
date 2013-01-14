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

package com.hp.hpl.jena.graph.compose.test;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.compose.DisjointUnion ;

/**
     TestDisjointUnion - test that DisjointUnion works, as well as we can.
*/
public class TestDisjointUnion extends TestDyadic
    {
    public TestDisjointUnion( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestDisjointUnion.class ); }
    
    
    @Override
	public Graph getGraph()
	{
		Graph gBase = graphWith( "" ), g1 = graphWith( "" );
        return new DisjointUnion( gBase, g1 ); 
	}
	
    
    public void testEmptyUnion()
        { 
        DisjointUnion du = new DisjointUnion( Graph.emptyGraph, Graph.emptyGraph );
        assertEquals( true, du.isEmpty() );
        }
    
    public void testLeftUnion()
        {
        Graph g = graphWith( "" );
        testSingleComponent( g, new DisjointUnion( g, Graph.emptyGraph ) );
        }
    
    public void testRightUnion()
        {
        Graph g = graphWith( "" );
        testSingleComponent( g, new DisjointUnion( Graph.emptyGraph, g ) );
        }

    protected void testSingleComponent( Graph g, DisjointUnion du )
        {
        graphAdd( g, "x R y; a P b; x Q b" );
        assertIsomorphic( g, du );
        graphAdd( g, "roses growOn you" );
        assertIsomorphic( g, du );
        g.delete( triple( "a P b" ) );
        assertIsomorphic( g, du );
        }
    
    public void testBothComponents()
        {
        Graph L = graphWith( "" ), R = graphWith( "" );
        Graph du = new DisjointUnion( L, R );
        assertIsomorphic( Graph.emptyGraph, du );
        L.add( triple( "x P y" ) );
        assertIsomorphic( graphWith( "x P y" ), du );
        R.add( triple( "A rdf:type Route" ) );
        assertIsomorphic( graphWith( "x P y; A rdf:type Route" ), du );
        }
    
    public void testRemoveBoth()
        {
        Graph L = graphWith( "x R y; a P b" ), R = graphWith( "x R y; p Q r" );
        Graph du = new DisjointUnion( L, R );
        du.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "a P b" ), L );
        assertIsomorphic( graphWith( "p Q r" ), R );
        }
    
    public void testAddLeftOnlyIfNecessary()
        {
        Graph L = graphWith( "" ), R = graphWith( "x R y" );
        Graph du = new DisjointUnion( L, R );
        graphAdd( du, "x R y" );
        assertEquals( true, L.isEmpty() );
        graphAdd( du, " a P b" );
        assertIsomorphic( graphWith( "a P b" ), L );
        assertIsomorphic( graphWith( "x R y" ), R );
        }
    }
