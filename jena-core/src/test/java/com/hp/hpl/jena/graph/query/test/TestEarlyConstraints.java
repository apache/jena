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

package com.hp.hpl.jena.graph.query.test;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

/**
	TestEarlyConstraints

	@author kers
*/
public class TestEarlyConstraints extends QueryTestBase 
    {
	public TestEarlyConstraints(String name)
        { super( name ); }
        
	public static TestSuite suite()
        { return new TestSuite( TestEarlyConstraints.class ); }
        
    public void testEarlyConstraint()
        {
        final int [] count = {0};
        GraphQuery q = new GraphQuery()
            .addMatch( GraphQuery.S, node( "eg:p1" ), GraphQuery.O )
            .addMatch( GraphQuery.X, node( "eg:p2" ), GraphQuery.Y )
            .addConstraint( notEqual( GraphQuery.S, GraphQuery.O ) )
            ;
        Graph gBase = graphWith( "a eg:p1 a; c eg:p1 d; x eg:p2 y" );
        Graph g = new WrappedGraph( gBase )
            {
            @Override public QueryHandler queryHandler()
                { return new SimpleQueryHandler( this ); }
            
            @Override public ExtendedIterator<Triple> find( Node S, Node P, Node O ) 
                {
                if (P.equals( node( "eg:p2" ) )) count[0] += 1;
                return super.find( S, P, O ); 
                }
            
            @Override public ExtendedIterator<Triple> find( TripleMatch tm ) 
                {
                if (tm.getMatchPredicate().equals( node( "eg:p2" ) )) count[0] += 1;
                return super.find( tm ); 
                }
            };
        Set<Node> s = q.executeBindings( g, new Node[] {GraphQuery.S} ) .mapWith ( getFirst ).toSet();
        assertEquals( nodeSet( "c" ), s );
        assertEquals( 1, count[0] );
        }
    }
