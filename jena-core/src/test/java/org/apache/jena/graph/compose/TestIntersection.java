/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.graph.compose;

import junit.framework.TestSuite;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphTestLib;

public class TestIntersection extends TestDyadic
{
    public TestIntersection( String name )
    { super( name ); }

    public static TestSuite suite()
    { return new TestSuite( TestIntersection.class ); }

    @Override
    public Graph getNewGraph()
    {
        Graph gBase = GraphTestLib.graphWith( "" ), g1 = GraphTestLib.graphWith( "" );
        return new Intersection( gBase, g1 );
    }

    public Intersection intersectionOf(String s1, String s2) {
        return new Intersection( GraphTestLib.graphWith( s1 ), GraphTestLib.graphWith( s2 ) );
    }

    public void testStaticIntersection() {
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), intersectionOf( "", "" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), intersectionOf( "x R y", "" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), intersectionOf( "", "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), intersectionOf( "x R y", "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), intersectionOf( "x R y; p R q", "r A s; x R y" ) );
    }

    public void testIntersectionReflectsChangesToOperands() {
        Graph l = GraphTestLib.graphWith( "x R y" );
        Graph r = GraphTestLib.graphWith( "p S q" );
        Intersection isec = new Intersection( l, r );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), isec );

        // add to the left what is already in the right
        l.add( GraphTestLib.triple( "p S q" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q" ), isec );

        // add to the right what is already in the left
        r.add( GraphTestLib.triple( "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q; x R y" ), isec );

        // add to a single graph is not reflected
        l.add( GraphTestLib.triple( "p S o" ) );
        r.add( GraphTestLib.triple( "x R z" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q; x R y" ), isec );

        // remove from the left
        l.delete( GraphTestLib.triple( "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q" ), isec );

        // remove from the right
        r.delete( GraphTestLib.triple( "p S q" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), isec );
    }

    public void testAdd() {
        Graph l = GraphTestLib.graphWith( "x R y" );
        Graph r = GraphTestLib.graphWith( "p S q" );
        Intersection isec = new Intersection( l, r );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), isec );

        isec.add( GraphTestLib.triple( "r A s" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "r A s" ), isec );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; r A s" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q; r A s" ), r );

        isec.add( GraphTestLib.triple ( "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "r A s; x R y" ), isec );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; r A s" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q; r A s; x R y" ), r );

        isec.add( GraphTestLib.triple ( "p S q" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q; r A s; x R y" ), isec );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q; r A s; x R y" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q; r A s; x R y" ), r );
    }

    public void testDelete() {
        Graph l = GraphTestLib.graphWith( "r A s; x R y" );
        Graph r = GraphTestLib.graphWith( "x R y; p S q" );
        Intersection isec = new Intersection( l, r );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), isec );

        // removing non-contained triples is a no-op
        isec.delete( GraphTestLib.triple( "r A s" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "r A s; x R y" ), l);
        isec.delete( GraphTestLib.triple( "p S q" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; p S q" ), r);

        // removing a contained triple removes it from the left operand
        isec.delete( GraphTestLib.triple( "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), isec );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "r A s" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; p S q" ), r );
    }
}
