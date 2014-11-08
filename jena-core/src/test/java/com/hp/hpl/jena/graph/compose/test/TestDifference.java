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

    public Difference differenceOf(String s1, String s2) {
        return new Difference( graphWith( s1 ), graphWith( s2 ) );
    }

    public void testStaticDifference() {
        assertIsomorphic( differenceOf( "", "" ), graphWith( "" ) );
        assertIsomorphic( differenceOf( "x R y", "" ), graphWith( "x R y" ) );
        assertIsomorphic( differenceOf( "", "x R y" ), graphWith( "" ) );
        assertIsomorphic( differenceOf( "x R y", "x R y" ), graphWith( "" ) );
        assertIsomorphic( differenceOf( "x R y; p R q", "r A s; x R y" ), graphWith( "p R q" ) );
    }
    
    public void testDifferenceReflectsChangesToOperands() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "x R y" );
        Difference diff = new Difference( l, r );
        assertIsomorphic( diff, graphWith( "" ) );
        r.delete( triple( "x R y" ) );
        assertIsomorphic( diff, graphWith( "x R y" ) );
        l.add( triple( "x R z" ) );
        assertIsomorphic( diff, graphWith( "x R y; x R z" ) );
        r.add( triple( "x R z" ) );
        assertIsomorphic( diff, graphWith( "x R y" ) );
    }

    public void testAdd() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "x R y; x R z" );
        Difference diff = new Difference( l, r );
        assertIsomorphic( diff, graphWith( "" ) );
        
        // case 1: add to the left operand
        diff.add( triple( "p S q" ) );
        assertIsomorphic( diff, graphWith( "p S q" ) );
        assertIsomorphic( l, graphWith( "x R y; p S q" ) );
        assertIsomorphic( r, graphWith( "x R y; x R z" ) );
        
        // case 2: remove from the right, and add to the left operand
        diff.add( triple( "x R z" ) );
        assertIsomorphic( diff, graphWith( "x R z; p S q" ) );
        assertIsomorphic( l, graphWith( "x R y; x R z; p S q" ) );
        assertIsomorphic( r, graphWith( "x R y" ) );
        
        // case 3: remove from the right operand
        diff.add( triple( "x R y" ) );
        assertIsomorphic( diff, graphWith( "x R y; x R z; p S q" ) );
        assertIsomorphic( l, graphWith( "x R y; x R z; p S q" ) );
        assertIsomorphic( r, graphWith( "" ) );
    }
    
    public void testDelete() {
        Graph l = graphWith( "x R y; x R z" );
        Graph r = graphWith( "x R y" );
        Difference diff = new Difference( l, r );
        assertIsomorphic( diff, graphWith( "x R z" ) );
        
        // case 1: remove non-existent triple is a no-op
        diff.delete( triple( "p S q" ) );
        assertIsomorphic( diff, graphWith( "x R z" ) );
        assertIsomorphic( l, graphWith( "x R y; x R z" ) );
        assertIsomorphic( r, graphWith( "x R y" ) );
        
        // case 2: remove triple that exists in both - removes from left
        diff.delete( triple( "x R y" ) );
        assertIsomorphic( diff, graphWith( "x R z" ) );
        assertIsomorphic( l, graphWith( "x R z" ) );
        assertIsomorphic( r, graphWith( "x R y" ) );
        
        // case 3: remove triple that exists in left is removed
        diff.delete( triple( "x R z" ) );
        assertIsomorphic( diff, graphWith( "" ) );
        assertIsomorphic( l, graphWith( "" ) );
        assertIsomorphic( r, graphWith( "x R y" ) );
    }
}
