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
import com.hp.hpl.jena.graph.compose.Union ;

public class TestUnion extends TestDyadic 
	{
	public TestUnion( String name )
		{ super( name ); }
			
	public static TestSuite suite()
    	{ return new TestSuite( TestUnion.class ); }
    	
	@Override
	public Graph getGraph()
	{
		Graph gBase = graphWith( "" ), g1 = graphWith( "" );
        return new Union( gBase, g1 ); 
	}
	
	public void testUnion() 
		{
        Graph g1 = graphWith( "x R y; p R q" );
        Graph g2 = graphWith( "r A s; x R y" );
        Union u = new Union( g1, g2 );
        assertContains( "Union", "x R y", u );
        assertContains( "Union", "p R q", u );
        assertContains( "Union", "r A s", u );
        if (u.size() != 3)
            fail( "oops: size of union is not 3" );
        u.add( triple( "cats eat cheese" ) );
        assertContains( "Union", "cats eat cheese", u );
        if 
        	(
        	contains( g1, "cats eat cheese" ) == false
        	&& contains( g2, "cats eat cheese" ) == false
        	)
            fail( "oops: neither g1 nor g2 contains `cats eat cheese`" );
		}
	}
