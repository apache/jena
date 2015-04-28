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
import org.apache.jena.graph.compose.Union ;

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

    public Union unionOf( String s1, String s2 )
    {
        return new Union( graphWith( s1 ), graphWith ( s2 ) );
    }

    public void testStaticUnion()
    {
        assertIsomorphic(graphWith( "" ), unionOf( "", "" ));
        assertIsomorphic(graphWith( "x R y" ), unionOf( "x R y", "" ) );
        assertIsomorphic(graphWith( "x R y" ), unionOf( "", "x R y" ) );
        assertIsomorphic(graphWith( "x R y; x R z" ), unionOf( "x R y", "x R z" ) );
        assertIsomorphic(graphWith( "x R y" ), unionOf( "x R y", "x R y" ) );
    }

    public void testUnionReflectsChangesToOperands() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "x R y" );
        Union u = new Union( l, r );

        assertIsomorphic( graphWith( "x R y" ), u );

        l.add( triple( "x R z" ) );
        assertIsomorphic( graphWith( "x R y; x R z" ), u );

        l.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "x R y; x R z" ), u );

        r.add( triple( "p S q" ) );
        assertIsomorphic( graphWith( "x R y; x R z; p S q" ), u );

        r.delete( triple( "x R y" ) );
        assertIsomorphic( graphWith( "x R z; p S q" ), u );
    }

    public void testAdd() {
        Graph l = graphWith( "x R y" );
        Graph r = graphWith( "x R y; p S q" );
        Union u = new Union( l, r );

        u.add( triple("x R y") );
        assertIsomorphic( graphWith( "x R y" ), l);
        assertIsomorphic( graphWith( "x R y; p S q" ), r);

        u.add( triple("p S q") );
        assertIsomorphic( graphWith( "x R y; p S q" ), l);
        assertIsomorphic( graphWith( "x R y; p S q" ), r);

        u.add( triple("r A s") );
        assertIsomorphic( graphWith( "x R y; p S q; r A s" ), l);
        assertIsomorphic( graphWith( "x R y; p S q" ), r);
    }

    public void testDelete() {
        Graph l = graphWith( "x R y; x R z" );
        Graph r = graphWith( "x R y; p S q" );
        Union u = new Union( l, r );

        u.delete( triple( "r A s" ) );
        assertIsomorphic( graphWith( "x R y; x R z" ), l);
        assertIsomorphic( graphWith( "x R y; p S q" ), r);

        u.delete( triple( "x R z" ) );
        assertIsomorphic( graphWith( "x R y" ), l);
        assertIsomorphic( graphWith( "x R y; p S q" ), r);

        u.delete( triple ( "p S q" ) );
        assertIsomorphic( graphWith( "x R y" ), l);
        assertIsomorphic( graphWith( "x R y" ), r);

        u.delete( triple ( "x R y" ) );
        assertIsomorphic( graphWith( "" ), l);
        assertIsomorphic( graphWith( "" ), r);
    }
}
