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

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

import junit.framework.TestSuite;

public class TestQueryNode extends QueryTestBase
    {
    public TestQueryNode(String name)
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestQueryNode.class ); }
    
    public static final QueryNodeFactory F = QueryNode.factory;
    
    public void testNoIndex()
        { assertTrue( QueryNode.NO_INDEX < 0 ); }
    
    public void testFixed()
        { 
        Node fixed = NodeCreateUtils.create( "fixed" );
        QueryNode n = new QueryNode.Fixed( fixed );
        assertSame( fixed, n.node );
        assertEquals( QueryNode.NO_INDEX, n.index );
        assertEquals( false, n.mustMatch() );
        assertSame( fixed, n.finder( null ) );
        }
    
    public void testBind()
        {
        final int index = 7;
        Node bind = NodeCreateUtils.create( "?bind" );
        QueryNode n = new QueryNode.Bind( bind, index );
        assertSame( bind, n.node );
        assertEquals( index, n.index );
        assertEquals( true, n.mustMatch() );
        assertSame( Node.ANY, n.finder( null ) );
        }
    
    public void testBound()
        {
        testBoundAt( 0 );
        testBoundAt( 3 );
        testBoundAt( 7 );
        }

    protected void testBoundAt( final int index )
        {
        Node bound = NodeCreateUtils.create( "?bound" );
        QueryNode n = new QueryNode.Bound( bound, index );
        assertSame( bound, n.node );
        assertEquals( index, n.index );
        assertEquals( false, n.mustMatch() );
        Domain d = new Domain( index + 1 );
        Node item = NodeCreateUtils.create( "'anItem'" );
        d.setElement( index, item );
        assertSame( item, n.finder( d ) );
        }
    
    public void testJustBound()
        {
        final int index = 1;
        Node just = NodeCreateUtils.create( "?jBound" );
        QueryNode n = new QueryNode.JustBound( just, index );
        assertSame( just, n.node );
        assertEquals( index, n.index );
        assertEquals( true, n.mustMatch() );
        assertEquals( Node.ANY, n.finder( null ) );
        }
    
    public void testAny()
        {
        QueryNode n = new QueryNode.Any();
        assertSame( Node.ANY, n.node );
        assertEquals( QueryNode.NO_INDEX, n.index );
        assertEquals( false, n.mustMatch() );
        assertSame( Node.ANY, n.finder( null ) );
        }

    public void testClassifyFixed()
        {
        Node fixed = NodeCreateUtils.create( "someURI" );
        QueryNode n = QueryNode.classify( F, null, null, fixed );
        assertInstanceOf( QueryNode.Fixed.class, n );
        assertEquals( QueryNode.NO_INDEX, n.index );
        assertSame( fixed, n.node );
        }
    
    public void testClassifyAny()
        {
        QueryNode n = QueryNode.classify( F, null, null, Node.ANY );
        assertInstanceOf( QueryNode.Any.class, n );
        assertEquals( QueryNode.NO_INDEX, n.index );
        assertSame( Node.ANY, n.node );
        }
    
    public void testClassifyFirstBind()
        {
        Mapping m = new Mapping( new Node[0] );
        testClassifyBind( NodeCreateUtils.create( "?bind" ), m, 0 );
        }    
    
    public void testClassifySecondBind()
        {
        Mapping m = new Mapping( new Node[0] );
        m.newIndex( NodeCreateUtils.create( "?other" ) );
        testClassifyBind( NodeCreateUtils.create( "?bind" ), m, 1 );
        }

    protected void testClassifyBind( Node bind, Mapping m, int index )
        {
        QueryNode n = QueryNode.classify( F, m, new HashSet<Node>(), bind );
        assertInstanceOf( QueryNode.Bind.class, n );
        assertSame( n.node, bind );
        assertEquals( index, n.index );
        }
    
    public void testClassifyBound()
        {
        testClassifyBound( 0 );
        testClassifyBound( 1 );
        testClassifyBound( 4 );
        testClassifyBound( 17 );
        }

    protected void testClassifyBound( int index )
        {
        Node bound = NodeCreateUtils.create( "?bound" );
        Mapping m = getPreloadedMapping( index );
        m.newIndex( bound );
        QueryNode n = QueryNode.classify( F, m, new HashSet<Node>(), bound );
        assertInstanceOf( QueryNode.Bound.class, n );
        assertSame( n.node, bound );
        assertEquals( index, n.index );
        }

    public void testClassifyJustBound()
        {
        testClassifyJustBound( 0 );
        testClassifyJustBound( 1 );
        testClassifyJustBound( 17 );
        testClassifyJustBound( 42 );
        }

    protected void testClassifyJustBound( int index )
        {
        Node recent = NodeCreateUtils.create( "?recent" );
        Mapping m = getPreloadedMapping( index );
        m.newIndex( recent );
        Set<Node> withRecent = new HashSet<Node>();
        withRecent.add( recent );
        QueryNode n = QueryNode.classify( F, m, withRecent, recent );
        assertInstanceOf( QueryNode.JustBound.class, n );
        assertSame( recent, n.node );
        assertEquals( index, n.index );
        }
    
    public void testBindingSetsJustBound()
        {
        Node X = NodeCreateUtils.create( "?X" );
        Mapping m = getPreloadedMapping( 0 );
        Set<Node> s = new HashSet<Node>();
        QueryNode n = QueryNode.classify( F, m, s, X );
        assertTrue( s.contains( X ) );
        }
    
    public void testBindingSetsJustBoundTwice()
        {
        Node X = NodeCreateUtils.create( "?X" ), Y = NodeCreateUtils.create( "?Y" );
        Mapping m = getPreloadedMapping( 0 );
        Set<Node> s = new HashSet<Node>();
        QueryNode.classify( F, m, s, X );
        QueryNode.classify( F, m, s, Y );
        assertTrue( s.contains( X ) );
        assertTrue( s.contains( Y ) );
        }
    
    protected Mapping getPreloadedMapping( int count )
        {
        Mapping m = new Mapping( new Node[0] );
        for (int i = 0; i < count; i += 1) m.newIndex( NodeCreateUtils.create( "?bound-" + i ) );
        return m;
        }
    
    public void testMatchFixed()
        {
        Node fixed = NodeCreateUtils.create( "_anon" );
        QueryNode n = new QueryNode.Fixed( fixed );
        try { n.match( null, NodeCreateUtils.create( "named" ) ); fail( "Fixed should not be matching" ); }
        catch (QueryNode.MustNotMatchException e) { pass(); }
        }
    
    public void testMatchBound()
        {
        Node bound = NodeCreateUtils.create( "?xx" );
        QueryNode n = new QueryNode.Bound( bound, 1 );
        try { n.match( null, NodeCreateUtils.create( "_anon" ) ); fail( "Bound should not be matching" ); }
        catch (QueryNode.MustNotMatchException e) { pass(); }
        }
    
    public void testMatchAny()
        {
        QueryNode n = new QueryNode.Any();
        try { n.match( null, NodeCreateUtils.create( "17" ) ); fail( "Any should not be matching" ); }
        catch (QueryNode.MustNotMatchException e) { pass(); }
        }
    
    public void testMatchBind()
        {
        Node v = NodeCreateUtils.create( "?v" );
        Node x = NodeCreateUtils.create( "elephant" ), y = NodeCreateUtils.create( "hedgehog" );
        QueryNode n = new QueryNode.Bind( v, 1 );
        Domain d = new Domain(3);
        assertTrue( n.match( d, x ) );
        assertSame( x, d.getElement( n.index ) );
        assertTrue( n.match( d, y ) );
        assertSame( y, d.getElement( n.index ) );
        }
    
    public void testMatchJustBound()
        {
        Node v = NodeCreateUtils.create( "?who" );
        Node A = NodeCreateUtils.create( "A" ), B = NodeCreateUtils.create( "B" );
        QueryNode n = new QueryNode.JustBound( v, 1 );
        Domain d = new Domain(3);
        d.setElement( n.index, A );
        assertTrue( n.match( d, A ) );
        assertFalse( n.match( d, B ) );
        d.setElement( n.index, B );
        assertFalse( n.match( d, A ) );
        assertTrue( n.match( d, B ) );
        }
    
    }
