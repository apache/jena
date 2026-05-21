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

public class TestDifference extends TestDyadic
{
    public TestDifference( String name )
    { super( name ); }

    public static TestSuite suite()
    { return new TestSuite( TestDifference.class ); }

    @Override
    public Graph getNewGraph()
    {
        Graph gBase = GraphTestLib.graphWith( "" ), g1 = GraphTestLib.graphWith( "" );
        return new Difference( gBase, g1 );
    }

    public Difference differenceOf(String s1, String s2) {
        return new Difference( GraphTestLib.graphWith( s1 ), GraphTestLib.graphWith( s2 ) );
    }

    public void testStaticDifference() {
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), differenceOf( "", "" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), differenceOf( "x R y", "" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), differenceOf( "", "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), differenceOf( "x R y", "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p R q" ), differenceOf( "x R y; p R q", "r A s; x R y" ) );
    }

    public void testDifferenceReflectsChangesToOperands() {
        Graph l = GraphTestLib.graphWith( "x R y" );
        Graph r = GraphTestLib.graphWith( "x R y" );
        Difference diff = new Difference( l, r );
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith( "" ), diff);
        r.delete( GraphTestLib.triple( "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), diff );
        l.add( GraphTestLib.triple( "x R z" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; x R z" ), diff );
        r.add( GraphTestLib.triple( "x R z" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), diff );
    }

    public void testAdd() {
        Graph l = GraphTestLib.graphWith( "x R y" );
        Graph r = GraphTestLib.graphWith( "x R y; x R z" );
        Difference diff = new Difference( l, r );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), diff );

        // case 1: add to the left operand
        diff.add( GraphTestLib.triple( "p S q" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "p S q" ), diff );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; p S q" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; x R z" ), r );

        // case 2: remove from the right, and add to the left operand
        diff.add( GraphTestLib.triple( "x R z" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R z; p S q" ), diff );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; x R z; p S q" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), r );

        // case 3: remove from the right operand
        diff.add( GraphTestLib.triple( "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; x R z; p S q" ), diff );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; x R z; p S q" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), r );
    }

    public void testDelete() {
        Graph l = GraphTestLib.graphWith( "x R y; x R z" );
        Graph r = GraphTestLib.graphWith( "x R y" );
        Difference diff = new Difference( l, r );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R z" ), diff );

        // case 1: remove non-existent triple is a no-op
        diff.delete( GraphTestLib.triple( "p S q" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R z" ), diff );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y; x R z" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), r );

        // case 2: remove triple that exists in both - removes from left
        diff.delete( GraphTestLib.triple( "x R y" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R z" ), diff );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R z" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), r );

        // case 3: remove triple that exists in left is removed
        diff.delete( GraphTestLib.triple( "x R z" ) );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), diff );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "" ), l );
        GraphTestLib.assertIsomorphic( GraphTestLib.graphWith( "x R y" ), r );
    }
}
