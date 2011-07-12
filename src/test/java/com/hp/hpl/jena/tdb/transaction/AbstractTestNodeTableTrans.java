/**
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

package com.hp.hpl.jena.tdb.transaction;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.transaction.NodeTableTrans ;
import com.hp.hpl.jena.tdb.transaction.Transaction ;

public abstract class AbstractTestNodeTableTrans extends BaseTest
{
    abstract protected NodeTableTrans create(Node... nodes) ;
    
    protected static Node node1 = NodeFactory.parseNode("<x>") ;
    protected static Node node2 = NodeFactory.parseNode("<y>") ;

    static void contains(NodeTable nt, Node...nodes)
    {
        for ( Node n : nodes)
        {
            NodeId nodeId = nt.getNodeIdForNode(n) ;
            assertFalse(NodeId.doesNotExist(nodeId)) ;
        }
    }
    
    @Test public void nodetrans_01()
    {
        NodeTableTrans ntt = create() ;
        Transaction txn = new Transaction(null, null, 11, null) ; 
        ntt.begin(txn) ;
        ntt.abort(txn) ;
    }

    
    @Test public void nodetrans_02()
    {
        NodeTableTrans ntt = create() ;
        NodeTable nt0 = ntt.getBaseNodeTable() ;
        
        Transaction txn = new Transaction(null, null, 11, null) ; 
        ntt.begin(txn) ;
        // Add a node
        NodeId nodeId = ntt.getAllocateNodeId(node1) ;
        // Check not in the base.
        assertNull(nt0.getNodeForNodeId(nodeId)) ;
        // Check is in the transaction node table.
        assertEquals(NodeId.NodeDoesNotExist, nt0.getNodeIdForNode(node1)) ;
        assertEquals(node1, ntt.getNodeForNodeId(nodeId)) ;
        
        ntt.commit(txn) ;
        // Check it is now in the base.
        assertEquals(node1, nt0.getNodeForNodeId(nodeId)) ;
        assertEquals(nodeId, nt0.getNodeIdForNode(node1)) ;
    }

    @Test public void nodetrans_03()
    {
        NodeTableTrans ntt = create() ;
        NodeTable nt0 = ntt.getBaseNodeTable() ;
        
        Transaction txn = new Transaction(null, null, 11, null) ; 
        ntt.begin(txn) ;
        // Add a node
        NodeId nodeId = ntt.getAllocateNodeId(node1) ;
        // Check not in the base.
        assertEquals(NodeId.NodeDoesNotExist, nt0.getNodeIdForNode(node1)) ;
        assertNull(nt0.getNodeForNodeId(nodeId)) ;
        // Check is in the transaction node table.
        assertEquals(node1, ntt.getNodeForNodeId(nodeId)) ;
        
        ntt.abort(txn) ;
        // Check it is not in the base.
        assertEquals(NodeId.NodeDoesNotExist, nt0.getNodeIdForNode(node1)) ;
        assertNull(nt0.getNodeForNodeId(nodeId)) ;
    }
    
    @Test public void nodetrans_04()
    {
        NodeTableTrans ntt = create(node1) ;
        NodeTable nt0 = ntt.getBaseNodeTable() ;
        Transaction txn = new Transaction(null, null, 11, null) ; 
        ntt.begin(txn) ;
        // Add a node
        NodeId nodeId = ntt.getAllocateNodeId(node2) ;
        // Not here
        assertEquals(NodeId.NodeDoesNotExist, nt0.getNodeIdForNode(node2)) ;
        // Is here
        assertEquals(nodeId, ntt.getNodeIdForNode(node2)) ;
        ntt.commit(txn) ;
        assertEquals(nodeId, nt0.getNodeIdForNode(node2)) ;
    }

}

