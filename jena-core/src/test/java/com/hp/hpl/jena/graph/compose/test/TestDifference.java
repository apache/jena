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
import com.hp.hpl.jena.graph.compose.Difference ;

@SuppressWarnings("deprecation")
public class TestDifference extends TestDyadic 
	{
	public TestDifference( String name )
		{ super( name ); }
			
	public static TestSuite suite()
    	{ return new TestSuite( TestDifference.class ); }	
    	
	@Override
	public Graph getGraph()
	{
		Graph gBase = graphWith( "" ), g1 = graphWith( "" );
        return new Difference( gBase, g1 ); 
	}
	
    public void testDifference()
		{
        Graph g1 = graphWith( "x R y; p R q" );
        Graph g2 = graphWith( "r A s; x R y" );
        Difference d = new Difference( g1, g2 );
        assertOmits( "Difference", d, "x R y" );
        assertContains( "Difference", "p R q", d ); 
		assertOmits( "Difference", d, "r A s" );
        if (d.size() != 1)
            fail( "oops: size of difference is not 1" );
        d.add( triple( "cats eat cheese" ) );
        assertContains( "Difference.L", "cats eat cheese", g1 );
        assertOmits( "Difference.R", g2, "cats eat cheese" );
		}
	}
