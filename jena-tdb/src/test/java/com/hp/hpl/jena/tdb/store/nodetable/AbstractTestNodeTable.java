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

package com.hp.hpl.jena.tdb.store.nodetable;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

public abstract class AbstractTestNodeTable extends BaseTest
{
    protected abstract NodeTable createEmptyNodeTable() ;
    
    static protected final Node n1 = NodeFactoryExtra.parseNode("<http://example/x>") ;
    static protected final Node n2 = NodeFactoryExtra.parseNode("1") ;
    
    protected void testNode(String str)
    {
        testNode(NodeFactoryExtra.parseNode(str)) ;
    }
    
    protected void testNode(Node n)
    {
        NodeTable nt = createEmptyNodeTable() ;
        writeNode(nt, n) ;
    }
    
    protected static void writeNode(NodeTable nt, String str)
    {
        writeNode(nt, NodeFactoryExtra.parseNode(str)) ;
    }
    
    protected static void writeNode(NodeTable nt, Node n)
    {
        NodeId nodeId = nt.getAllocateNodeId(n) ;
        assertNotNull(nodeId) ;
        assertNotEquals(NodeId.NodeDoesNotExist, nodeId) ;
        assertNotEquals(NodeId.NodeIdAny, nodeId) ;
        
        Node n2 = nt.getNodeForNodeId(nodeId) ;
        assertEquals(n, n2) ;
        
        NodeId nodeId2 = nt.getNodeIdForNode(n) ;
        assertEquals(nodeId, nodeId2) ;
    }
    
    protected static void writeBadNode(NodeTable nt, Node badNode)
    {
        NodeId id1 = nt.allocOffset() ;
        try {
            NodeId nodeId = nt.getAllocateNodeId(badNode) ;
            fail("Expected exception for bad node: "+badNode) ;
        } catch (TDBException ex) { }
        NodeId id2 = nt.allocOffset() ;
        assertEquals(id1, id2) ;
    }
    
    @Test public void nodetable_01()    { testNode("<http://example/x>") ; }
    @Test public void nodetable_02()    { testNode("1") ; }
    @Test public void nodetable_03()    { testNode("_:x") ; }
    @Test public void nodetable_04()    { testNode("'x'") ; }
    @Test public void nodetable_05()    { testNode("'x'@en") ; }
    @Test public void nodetable_06()    { testNode("'x'^^<http://example/dt>") ; }
    @Test public void nodetable_07()    { testNode("'نواف'") ; }
    
    static Node badNode1 = com.hp.hpl.jena.graph.NodeFactory.createLiteral("abc", "99bad", null) ;
    
    @Test public void nodetable_bad_01()    { testNodeBad(badNode1) ; }
    @Test public void nodetable_bad_02()    
    { 
        NodeTable nt = createEmptyNodeTable() ;
        writeNode(nt, "'x'") ;
        NodeId id1 = nt.allocOffset() ;
        writeBadNode(nt, badNode1) ; 
        NodeId id2 = nt.allocOffset() ;
        assertEquals(id1, id2) ;
        writeNode(nt, "<http://example/x>") ;
        
    }

    protected void testNodeBad(Node badNode)
    {
        NodeTable nt = createEmptyNodeTable() ;
        writeBadNode(nt, badNode) ;
    }

}
