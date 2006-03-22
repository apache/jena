/*
    (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: TestQueryNode.java,v 1.7 2006-03-22 13:53:36 andy_seaborne Exp $
*/
package com.hp.hpl.jena.graph.query.test;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.*;

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
        Node fixed = Node.create( "fixed" );
        QueryNode n = new QueryNode.Fixed( fixed );
        assertSame( fixed, n.node );
        assertEquals( QueryNode.NO_INDEX, n.index );
        assertEquals( false, n.mustMatch() );
        assertSame( fixed, n.finder( null ) );
        }
    
    public void testBind()
        {
        final int index = 7;
        Node bind = Node.create( "?bind" );
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
        Node bound = Node.create( "?bound" );
        QueryNode n = new QueryNode.Bound( bound, index );
        assertSame( bound, n.node );
        assertEquals( index, n.index );
        assertEquals( false, n.mustMatch() );
        Domain d = new Domain( index + 1 );
        Node item = Node.create( "'anItem'" );
        d.setElement( index, item );
        assertSame( item, n.finder( d ) );
        }
    
    public void testJustBound()
        {
        final int index = 1;
        Node just = Node.create( "?jBound" );
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
        Node fixed = Node.create( "someURI" );
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
        testClassifyBind( Node.create( "?bind" ), m, 0 );
        }    
    
    public void testClassifySecondBind()
        {
        Mapping m = new Mapping( new Node[0] );
        m.newIndex( Node.create( "?other" ) );
        testClassifyBind( Node.create( "?bind" ), m, 1 );
        }

    protected void testClassifyBind( Node bind, Mapping m, int index )
        {
        QueryNode n = QueryNode.classify( F, m, new HashSet(), bind );
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
        Node bound = Node.create( "?bound" );
        Mapping m = getPreloadedMapping( index );
        m.newIndex( bound );
        QueryNode n = QueryNode.classify( F, m, new HashSet(), bound );
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
        Node recent = Node.create( "?recent" );
        Mapping m = getPreloadedMapping( index );
        m.newIndex( recent );
        Set withRecent = new HashSet();
        withRecent.add( recent );
        QueryNode n = QueryNode.classify( F, m, withRecent, recent );
        assertInstanceOf( QueryNode.JustBound.class, n );
        assertSame( recent, n.node );
        assertEquals( index, n.index );
        }
    
    public void testBindingSetsJustBound()
        {
        Node X = Node.create( "?X" );
        Mapping m = getPreloadedMapping( 0 );
        Set s = new HashSet();
        QueryNode n = QueryNode.classify( F, m, s, X );
        assertTrue( s.contains( X ) );
        }
    
    public void testBindingSetsJustBoundTwice()
        {
        Node X = Node.create( "?X" ), Y = Node.create( "?Y" );
        Mapping m = getPreloadedMapping( 0 );
        Set s = new HashSet();
        QueryNode.classify( F, m, s, X );
        QueryNode.classify( F, m, s, Y );
        assertTrue( s.contains( X ) );
        assertTrue( s.contains( Y ) );
        }
    
    protected Mapping getPreloadedMapping( int count )
        {
        Mapping m = new Mapping( new Node[0] );
        for (int i = 0; i < count; i += 1) m.newIndex( Node.create( "?bound-" + i ) );
        return m;
        }
    
    public void testMatchFixed()
        {
        Node fixed = Node.create( "_anon" );
        QueryNode n = new QueryNode.Fixed( fixed );
        try { n.match( null, Node.create( "named" ) ); fail( "Fixed should not be matching" ); }
        catch (QueryNode.MustNotMatchException e) { pass(); }
        }
    
    public void testMatchBound()
        {
        Node bound = Node.create( "?xx" );
        QueryNode n = new QueryNode.Bound( bound, 1 );
        try { n.match( null, Node.create( "_anon" ) ); fail( "Bound should not be matching" ); }
        catch (QueryNode.MustNotMatchException e) { pass(); }
        }
    
    public void testMatchAny()
        {
        QueryNode n = new QueryNode.Any();
        try { n.match( null, Node.create( "17" ) ); fail( "Any should not be matching" ); }
        catch (QueryNode.MustNotMatchException e) { pass(); }
        }
    
    public void testMatchBind()
        {
        Node v = Node.create( "?v" );
        Node x = Node.create( "elephant" ), y = Node.create( "hedgehog" );
        QueryNode n = new QueryNode.Bind( v, 1 );
        Domain d = new Domain(3);
        assertTrue( n.match( d, x ) );
        assertSame( x, d.getElement( n.index ) );
        assertTrue( n.match( d, y ) );
        assertSame( y, d.getElement( n.index ) );
        }
    
    public void testMatchJustBound()
        {
        Node v = Node.create( "?who" );
        Node A = Node.create( "A" ), B = Node.create( "B" );
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
/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/