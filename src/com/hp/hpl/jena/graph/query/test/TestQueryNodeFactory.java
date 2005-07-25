/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestQueryNodeFactory.java,v 1.1 2005-07-25 11:16:08 chris-dollin Exp $
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
        assertTrue( F instanceof QueryNode.Fixed );
        assertSame( f, F.node );
        }
    
    public void testAny()
        {
        QueryNode A = qnf.createAny();
        assertTrue( A instanceof QueryNode.Any );
        assertSame( Node.ANY, A.node );
        }
    
    public void testBind()
        {
        Node b = Node.create( "?x" );
        QueryNode B = qnf.createBind( b, 1 );
        assertTrue( B instanceof QueryNode.Bind );
        assertSame( b, B.node );
        }
    
    public void testJustBound()
        {
        Node j = Node.create( "?y" );
        QueryNode J = qnf.createJustBound( j, 1 );
        assertTrue( J instanceof QueryNode.JustBound );
        assertSame( j, J.node );
        }
    
    public void testBound()
        {
        Node u = Node.create( "?z" );
        QueryNode U = qnf.createBound( u, 2 );
        assertTrue( U instanceof QueryNode.Bound );
        assertSame( u, U.node );
        }

    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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