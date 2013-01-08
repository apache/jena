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

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;

import junit.framework.TestSuite;

/**
     Tests for recursive sub-graph extraction.
*/
public class TestGraphExtract extends GraphTestBase
	{
	public TestGraphExtract(String name)
		{ super( name ); }
	
	public static TestSuite suite()
		{ return new TestSuite( TestGraphExtract.class ); }

    public void testExtractNothing()
        {
        testExtract( "", "x", "" );
        testExtract( "", "x", "a R b" );
        testExtract( "", "x", "a R x" );
        testExtract( "","x", "a x y" );
        }
    
    public void testExtractOneLevel()
        {
        testExtract( "a R b", "a", "a R b" );
        testExtract("a R b; a R c", "a", "a R b; a R c" );
        testExtract( "a R b; a S d", "a", "a R b; a S d" );
        }
    
    public void testNoJunk()
        {
        testExtract( "a R b", "a", "a R b; x R y" );
        }

    public void testExtractTwoLevels()
        {
        testExtract( "a R b; b S c", "a", "a R b; b S c" );
        testExtract( "a R b; b S c", "a", "a R b; b S c; x P y" );
        testExtract( "a R b; b S c; b T d", "a", "a R b; b S c; b T d" );
        testExtract( "a R b; b S c; a T d", "a", "a R b; a T d; b S c" );
        }
    
    public void testExtractSeveralLevels()
        {
        testExtract( "a R b; b S c; c T d; d U e", "a", "a R b; b S c; c T d; d U e" );
        }
    
    public void testExtractNoLoop()
        {
        testExtract( "a R a", "a", "a R a" );
        testExtract( "a R b; b R a", "a", "a R b; b R a; z P a" );
        testExtract( "a R b; b S c; c T a", "a", "a R b; b S c; c T a; junk P junk" );
        }
    
    public void testTripleFilter()
        {
        assertTrue( TripleBoundary.stopAtAnonObject.stopAt( triple( "a R _b" ) ) );
        assertFalse( TripleBoundary.stopAtAnonObject.stopAt( triple( "a R b" ) ) );
        assertFalse( TripleBoundary.stopAtAnonObject.stopAt( triple( "a _R b" ) ) );
        assertFalse( TripleBoundary.stopAtAnonObject.stopAt( triple( "_a R b" ) ) );
        }
    
    public void testExtractBoundary()
        {
        testExtract( "a R b; b S _c", "a", "a R b; b S _c; _c T d", TripleBoundary.stopAtAnonObject );
        }
    
    /**
         This test exposed that the update-existing-graph functionality was broken
         if the target graph already contained any statements with a subject S
         appearing as subject in the source graph - no further Spo statements were
         added.
    */
    public void testPartialUpdate()
        {
        Graph source = graphWith( "a R b; b S e" );
        Graph dest = graphWith( "b R d" );
        GraphExtract e = new GraphExtract( TripleBoundary.stopNowhere );
        e.extractInto( dest, node( "a" ), source );
        assertIsomorphic( graphWith( "a R b; b S e; b R d" ), dest );
        }

    public void testExtract( String wanted, String node, String source )
        {
        testExtract( wanted, node, source, TripleBoundary.stopNowhere );
        }
        
    /**
    */
    private void testExtract( String wanted, String node, String source, TripleBoundary b )
        {
        assertIsomorphic( graphWith( wanted ), extract( node( node ), b, graphWith( source ) ) );
        }

    public Graph extract( Node node, TripleBoundary b, Graph graph )
        { return new GraphExtract( b ).extract( node, graph ); }

    }
