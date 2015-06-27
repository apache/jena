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

package org.apache.jena.graph.compose.test;

import junit.framework.TestSuite ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.compose.Difference ;

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
        assertIsomorphic( graphWith( "" ), differenceOf( "", "" ) );
        assertIsomorphic( graphWith( "x R y" ), differenceOf( "x R y", "" ) );
        assertIsomorphic( graphWith( "" ), differenceOf( "", "x R y" ) );
        assertIsomorphic( graphWith( "" ), differenceOf( "x R y", "x R y" ) );
        assertIsomorphic( graphWith( "p R q" ), differenceOf( "x R y; p R q", "r A s; x R y" ) );
    }
    
    public void testDifferenceReflectsChangesToOperands() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "x R y" );
        Difference diff = new Difference( l, r );
        assertIsomorphic(graphWith( "" ), diff);
        r.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "x R y" ), diff );
        l.add( triple( "x R z" ) );
        assertIsomorphic( graphWith( "x R y; x R z" ), diff );
        r.add( triple( "x R z" ) );
        assertIsomorphic( graphWith( "x R y" ), diff );
    }

    public void testAdd() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "x R y; x R z" );
        Difference diff = new Difference( l, r );
        assertIsomorphic( graphWith( "" ), diff );
        
        // case 1: add to the left operand
        diff.add( triple( "p S q" ) );
        assertIsomorphic( graphWith( "p S q" ), diff );
        assertIsomorphic( graphWith( "x R y; p S q" ), l );
        assertIsomorphic( graphWith( "x R y; x R z" ), r );
        
        // case 2: remove from the right, and add to the left operand
        diff.add( triple( "x R z" ) );
        assertIsomorphic( graphWith( "x R z; p S q" ), diff );
        assertIsomorphic( graphWith( "x R y; x R z; p S q" ), l );
        assertIsomorphic( graphWith( "x R y" ), r );
        
        // case 3: remove from the right operand
        diff.add( triple( "x R y" ) );
        assertIsomorphic( graphWith( "x R y; x R z; p S q" ), diff );
        assertIsomorphic( graphWith( "x R y; x R z; p S q" ), l );
        assertIsomorphic( graphWith( "" ), r );
    }
    
    public void testDelete() {
        Graph l = graphWith( "x R y; x R z" );
        Graph r = graphWith( "x R y" );
        Difference diff = new Difference( l, r );
        assertIsomorphic( graphWith( "x R z" ), diff );
        
        // case 1: remove non-existent triple is a no-op
        diff.delete( triple( "p S q" ) );
        assertIsomorphic( graphWith( "x R z" ), diff );
        assertIsomorphic( graphWith( "x R y; x R z" ), l );
        assertIsomorphic( graphWith( "x R y" ), r );
        
        // case 2: remove triple that exists in both - removes from left
        diff.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "x R z" ), diff );
        assertIsomorphic( graphWith( "x R z" ), l );
        assertIsomorphic( graphWith( "x R y" ), r );
        
        // case 3: remove triple that exists in left is removed
        diff.delete( triple( "x R z" ) );
        assertIsomorphic( graphWith( "" ), diff );
        assertIsomorphic( graphWith( "" ), l );
        assertIsomorphic( graphWith( "x R y" ), r );
    }
}
