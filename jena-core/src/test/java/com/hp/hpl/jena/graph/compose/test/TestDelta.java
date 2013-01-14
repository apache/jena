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
import com.hp.hpl.jena.graph.compose.Delta ;

@SuppressWarnings("deprecation")
public class TestDelta extends TestDyadic 
	{
		
	public TestDelta( String name )
		{ super( name ); }
		
	public static TestSuite suite()
    	{ return new TestSuite( TestDelta.class ); }			
    	
	@Override
	public Graph getGraph()
	{
		Graph gBase = graphWith( "" );
        return new Delta( gBase ); 
	}
	
	public void testDelta() 
		{
		Graph x = graphWith( "x R y" );
		assertContains( "x", "x R y", x );
		x.delete( triple( "x R y" ) );
		assertOmits( "x", x, "x R y" );
	/* */	
		Graph base = graphWith( "x R y; p S q; I like cheese; pins pop balloons" );
		Graph save = graphWith( "x R y; p S q; I like cheese; pins pop balloons" );
        Delta delta = new Delta( base );
		assertContainsAll( "Delta", delta, "x R y; p S q; I like cheese; pins pop balloons" );
		assertContainsAll( "Delta", base, "x R y; p S q; I like cheese; pins pop balloons" );
	/* */
		delta.add( triple( "pigs fly winglessly" ) );
		delta.delete( triple( "I like cheese" ) );
	/* */
		assertContainsAll( "changed Delta", delta, "x R y; p S q; pins pop balloons; pigs fly winglessly" );
		assertOmits( "changed delta", delta, "I like cheese" );
		assertContains( "delta additions", "pigs fly winglessly", delta.getAdditions() );
		assertOmits( "delta additions", delta.getAdditions(), "I like cheese" );
		assertContains( "delta deletions", "I like cheese", delta.getDeletions() );
		assertOmits( "delta deletions", delta.getDeletions(), "pigs fly winglessly" );
		}
	}
