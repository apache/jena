/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: TestQueryNode.java,v 1.2 2005-07-24 09:34:59 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.QueryNode;

import junit.framework.TestSuite;

public class TestQueryNode extends QueryTestBase
    {
    public TestQueryNode(String name)
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestQueryNode.class ); }
    
    public void testNoIndex()
        { assertTrue( QueryNode.NO_INDEX < 0 ); }
    
    public void testFixed()
        { 
        Node fixed = Node.create( "fixed" );
        QueryNode n = new QueryNode.Fixed( fixed );
        assertSame( fixed, n.node );
        assertEquals( QueryNode.NO_INDEX, n.index );
        }
    
    public void testBind()
        {
        Node bind = Node.create( "?bind" );
        final int index = 7;
        QueryNode n = new QueryNode.Bind( bind, index );
        assertSame( bind, n.node );
        assertEquals( index, n.index );
        }
    
    public void testBound()
        {
        Node bound = Node.create( "?bound" );
        final int index = 3;
        QueryNode n = new QueryNode.Bound( bound, index );
        assertSame( bound, n.node );
        assertEquals( index, n.index );
        }
    
    public void testJustBound()
        {
        Node just = Node.create( "?jBound" );
        final int index = 1;
        QueryNode n = new QueryNode.JustBound( just, index );
        assertSame( just, n.node );
        assertEquals( index, n.index );
        }
    
    public void testAny()
        {
        QueryNode n = new QueryNode.Any();
        assertSame( Node.ANY, n.node );
        assertEquals( QueryNode.NO_INDEX, n.index );
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