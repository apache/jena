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

package com.hp.hpl.jena.graph.query.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

public class TestQueryNodeFactory extends QueryTestBase
    {
    public TestQueryNodeFactory( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestQueryNodeFactory.class ); }

    QueryNodeFactory qnf = QueryNode.factory;
    
    public void testFixed()
        { 
        Node f = NodeCreateUtils.create( "constant" );
        QueryNode F = qnf.createFixed( f );
        assertInstanceOf( QueryNode.Fixed.class, F );
        assertSame( f, F.node );
        }
    
    public void testAny()
        {
        QueryNode A = qnf.createAny();
        assertInstanceOf( QueryNode.Any.class, A );
        assertSame( Node.ANY, A.node );
        }
    
    public void testBind()
        {
        Node b = NodeCreateUtils.create( "?x" );
        QueryNode B = qnf.createBind( b, 1 );
        assertInstanceOf( QueryNode.Bind.class, B );
        assertSame( b, B.node );
        }
    
    public void testJustBound()
        {
        Node j = NodeCreateUtils.create( "?y" );
        QueryNode J = qnf.createJustBound( j, 1 );
        assertInstanceOf( QueryNode.JustBound.class, J );
        assertSame( j, J.node );
        }
    
    public void testBound()
        {
        Node u = NodeCreateUtils.create( "?z" );
        QueryNode U = qnf.createBound( u, 2 );
        assertInstanceOf( QueryNode.Bound.class, U );
        assertSame( u, U.node );
        }
    
    public void testTriple()
        {
        QueryNode S = qnf.createBound( node( "?x" ), 0 );
        QueryNode P = qnf.createFixed( node( "P" ) );
        QueryNode O = qnf.createBind( node( "?y" ), 1 );
        QueryTriple t = qnf.createTriple( S, P, O );
        assertSame( t.S, S );
        assertSame( t.P, P );
        assertSame( t.O, O );
        }
    
    public void testArray()
        {
        QueryTriple [] a = qnf.createArray( 42 );
        assertEquals( 42, a.length );
        }

    }
