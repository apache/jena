/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public abstract class TestNodeTableBase extends BaseTest
{
    protected abstract NodeTable createEmptyNodeTable() ;
    
    static protected final Node n1 = NodeFactory.parseNode("<http://example/x>") ;
    static protected final Node n2 = NodeFactory.parseNode("1") ;
    
    protected void testNode(String str)
    {
        testNode(NodeFactory.parseNode(str)) ;
    }
    
    protected void testNode(Node n)
    {
        NodeTable nt = createEmptyNodeTable() ;
        NodeId nodeId = nt.getAllocateNodeId(n) ;
        assertNotNull(nodeId) ;
        assertNotEquals(NodeId.NodeDoesNotExist, nodeId) ;
        assertNotEquals(NodeId.NodeIdAny, nodeId) ;
        
        Node n2 = nt.getNodeForNodeId(nodeId) ;
        assertEquals(n, n2) ;
        
        NodeId nodeId2 = nt.getNodeIdForNode(n) ;
        assertEquals(nodeId, nodeId2) ;
    }
    
    @Test public void nodetable_01()    { testNode("<http://example/x>") ; }
    @Test public void nodetable_02()    { testNode("1") ; }
    @Test public void nodetable_03()    { testNode("_:x") ; }
    @Test public void nodetable_04()    { testNode("'x'") ; }
    @Test public void nodetable_05()    { testNode("'x'@en") ; }
    @Test public void nodetable_06()    { testNode("'x'^^<http://example/dt>") ; }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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