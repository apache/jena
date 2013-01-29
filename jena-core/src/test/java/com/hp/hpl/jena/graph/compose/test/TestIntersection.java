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
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.compose.Intersection ;

@SuppressWarnings("deprecation")
public class TestIntersection extends TestDyadic
	{
	public TestIntersection( String name )
		{ super( name ); }
		
	public static TestSuite suite()
    	{ return new TestSuite( TestIntersection.class ); }	
   
	@Override
	public Graph getGraph()
	{
		Graph gBase = graphWith( "" ), g1 = graphWith( "" );
        return new Intersection( gBase, g1 ); 
	}
	
	public void testIntersection()
		{
        Graph g1 = graphWith( "x R y; p R q" );
        Graph g2 = graphWith( "r A s; x R y" );
        Intersection i = new Intersection( g1, g2 );
        assertContains( "Intersection", "x R y", i );
 		assertOmits( "Intersection", i, "p R q" );
		assertOmits( "Intersection", i, "r A s" );
        if (i.size() != 1)
            fail( "oops: size of intersection is not 1" );
        i.add( triple( "cats eat cheese" ) );
        assertContains( "Intersection.L", "cats eat cheese", g1 );
        assertContains( "Intersection.R", "cats eat cheese", g2 );
        }
    
    public void testDeleteDoesNotUpdateR()
        {
    	Graph L = graphWith( "a pings b; b pings c; c pings a" );
    	Graph R = graphWith( "c pings a; b pings c; x captures y" );
    	Graph join = new Intersection( L, R );
    	GraphUtil.deleteFrom(L, R) ;
        assertIsomorphic( "R should not change", graphWith( "c pings a; b pings c; x captures y" ), R );
        assertIsomorphic( graphWith( "a pings b" ), L );
		}
	}
