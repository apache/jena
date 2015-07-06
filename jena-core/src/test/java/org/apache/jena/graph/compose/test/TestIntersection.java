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

import junit.framework.TestSuite;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.compose.Intersection ;

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

    public Intersection intersectionOf(String s1, String s2) {
        return new Intersection( graphWith( s1 ), graphWith( s2 ) );
    }

    public void testStaticIntersection() {
        assertIsomorphic( graphWith( "" ), intersectionOf( "", "" ) );
        assertIsomorphic( graphWith( "" ), intersectionOf( "x R y", "" ) );
        assertIsomorphic( graphWith( "" ), intersectionOf( "", "x R y" ) );
        assertIsomorphic( graphWith( "x R y" ), intersectionOf( "x R y", "x R y" ) );
        assertIsomorphic( graphWith( "x R y" ), intersectionOf( "x R y; p R q", "r A s; x R y" ) );
    }

    public void testIntersectionReflectsChangesToOperands() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "p S q" );
        Intersection isec = new Intersection( l, r );
        assertIsomorphic( graphWith( "" ), isec );

        // add to the left what is already in the right
        l.add( triple( "p S q" ) );
        assertIsomorphic( graphWith( "p S q" ), isec );

        // add to the right what is already in the left
        r.add( triple( "x R y" ) );
        assertIsomorphic( graphWith( "p S q; x R y" ), isec );

        // add to a single graph is not reflected
        l.add( triple( "p S o" ) );
        r.add( triple( "x R z" ) );
        assertIsomorphic( graphWith( "p S q; x R y" ), isec );

        // remove from the left
        l.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "p S q" ), isec );

        // remove from the right
        r.delete( triple( "p S q" ) );
        assertIsomorphic( graphWith( "" ), isec );
    }

    public void testAdd() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "p S q" );
        Intersection isec = new Intersection( l, r );
        assertIsomorphic( graphWith( "" ), isec );

        isec.add( triple( "r A s" ) );
        assertIsomorphic( graphWith( "r A s" ), isec );
        assertIsomorphic( graphWith( "x R y; r A s" ), l );
        assertIsomorphic( graphWith( "p S q; r A s" ), r );

        isec.add( triple ( "x R y" ) );
        assertIsomorphic( graphWith( "r A s; x R y" ), isec );
        assertIsomorphic( graphWith( "x R y; r A s" ), l );
        assertIsomorphic( graphWith( "p S q; r A s; x R y" ), r );

        isec.add( triple ( "p S q" ) );
        assertIsomorphic( graphWith( "p S q; r A s; x R y" ), isec );
        assertIsomorphic( graphWith( "p S q; r A s; x R y" ), l );
        assertIsomorphic( graphWith( "p S q; r A s; x R y" ), r );
    }
    
    public void testDelete() {
        Graph l = graphWith( "r A s; x R y" );
        Graph r = graphWith( "x R y; p S q" );
        Intersection isec = new Intersection( l, r );
        assertIsomorphic( graphWith( "x R y" ), isec );

        // removing non-contained triples is a no-op
        isec.delete( triple( "r A s" ) );
        assertIsomorphic( graphWith( "r A s; x R y" ), l);
        isec.delete( triple( "p S q" ) );
        assertIsomorphic( graphWith( "x R y; p S q" ), r);

        // removing a contained triple removes it from the left operand
        isec.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "" ), isec );
        assertIsomorphic( graphWith( "r A s" ), l );
        assertIsomorphic( graphWith( "x R y; p S q" ), r );
    }
}
