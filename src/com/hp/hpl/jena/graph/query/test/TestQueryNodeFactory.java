/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestQueryNodeFactory.java,v 1.4 2006-03-22 13:53:36 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.*;

public class TestQueryNodeFactory extends QueryTestBase
    {
    public TestQueryNodeFactory( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestQueryNodeFactory.class ); }

    QueryNodeFactory qnf = QueryNode.factory;
    
    public void testFixed()
        { 
        Node f = Node.create( "constant" );
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
        Node b = Node.create( "?x" );
        QueryNode B = qnf.createBind( b, 1 );
        assertInstanceOf( QueryNode.Bind.class, B );
        assertSame( b, B.node );
        }
    
    public void testJustBound()
        {
        Node j = Node.create( "?y" );
        QueryNode J = qnf.createJustBound( j, 1 );
        assertInstanceOf( QueryNode.JustBound.class, J );
        assertSame( j, J.node );
        }
    
    public void testBound()
        {
        Node u = Node.create( "?z" );
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