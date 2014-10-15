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

package com.hp.hpl.jena.tdb.transaction;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexMap ;
import com.hp.hpl.jena.tdb.setup.Build ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public abstract class AbstractTestNodeTableTrans extends BaseTest
{
    abstract protected ObjectFile createObjectFile() ;
    abstract protected Location getLocation() ;
    
    private NodeTableTrans create(Transaction txn, Node...nodes)
    {
        NodeTable base = Build.makeNodeTable(getLocation()) ;
        for ( Node n : nodes )
            base.getAllocateNodeId(n) ;
        return create(txn, base) ;
    }
    
    private NodeTableTrans create(Transaction txn, NodeTable base)
    {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
        Index idx = new IndexMap(recordFactory) ;
        ObjectFile objectFile = createObjectFile() ;
        NodeTableTrans ntt = new NodeTableTrans(txn, "test", base, idx, objectFile) ;
        return ntt ;
    }
    
    protected static Node node1 = NodeFactoryExtra.parseNode("<x>") ;
    protected static Node node2 = NodeFactoryExtra.parseNode("<y>") ;
    protected static Node node3 = NodeFactoryExtra.parseNode("<z>") ;

    static void contains(NodeTable nt, Node...nodes)
    {
        for ( Node n : nodes)
        {
            NodeId nodeId = nt.getNodeIdForNode(n) ;
            assertFalse(NodeId.isDoesNotExist(nodeId)) ;
        }
    }
    
    Transaction createTxn(long id) 
    {
        return new Transaction(null, ReadWrite.WRITE, id, null, null) ; 
    }
    
    @Test public void nodetrans_01()
    {
        Transaction txn = createTxn(11) ; 
        NodeTableTrans ntt = create(txn) ;
        ntt.begin(txn) ;
        ntt.abort(txn) ;
    }

    
    @Test public void nodetrans_02()
    {
        Transaction txn = createTxn(11) ; 
        NodeTableTrans ntt = create(txn) ;
        NodeTable nt0 = ntt.getBaseNodeTable() ;
        
        ntt.begin(txn) ;
        // Add a node
        NodeId nodeId = ntt.getAllocateNodeId(node1) ;
        // Check not in the base.
        assertNull(nt0.getNodeForNodeId(nodeId)) ;
        // Check is in the transaction node table.
        assertEquals(NodeId.NodeDoesNotExist, nt0.getNodeIdForNode(node1)) ;
        assertEquals(node1, ntt.getNodeForNodeId(nodeId)) ;
        
        ntt.commitPrepare(txn) ;
        ntt.commitEnact(txn) ;
        // Check it is now in the base.
        assertEquals(node1, nt0.getNodeForNodeId(nodeId)) ;
        assertEquals(nodeId, nt0.getNodeIdForNode(node1)) ;
        ntt.commitClearup(txn) ;
    }

    @Test public void nodetrans_03()
    {
        Transaction txn = createTxn(11) ; 
        NodeTableTrans ntt = create(txn) ;
        NodeTable nt0 = ntt.getBaseNodeTable() ;
         
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
        ntt.commitClearup(txn) ;
    }
    
    @Test public void nodetrans_04()
    {
        Transaction txn = createTxn(11) ; 
        NodeTableTrans ntt = create(txn, node1) ;
        NodeTable nt0 = ntt.getBaseNodeTable() ;
        ntt.begin(txn) ;
        // Add a node
        NodeId nodeId = ntt.getAllocateNodeId(node2) ;
        // Not here
        assertEquals(NodeId.NodeDoesNotExist, nt0.getNodeIdForNode(node2)) ;
        // Is here
        assertEquals(nodeId, ntt.getNodeIdForNode(node2)) ;
        ntt.commitPrepare(txn) ;
        ntt.commitEnact(txn) ;
        assertEquals(nodeId, nt0.getNodeIdForNode(node2)) ;
        ntt.commitClearup(txn) ;
    }
    
    
    @Test 
    public void nodetrans_05()
    {   
        // 2 transactions - no blocking reader - create a second NodeTableTrans
        Transaction txn1 = createTxn(11) ; 
        NodeTableTrans ntt1 = create(txn1, node1) ;
        NodeId nodeId1 = ntt1.getBaseNodeTable().getNodeIdForNode(node1) ;
        
        ntt1.begin(txn1) ;
        NodeId nodeId2 = ntt1.getAllocateNodeId(node2) ;
        ntt1.commitPrepare(txn1) ;
        ntt1.commitEnact(txn1) ;
        ntt1.commitClearup(txn1) ;
        
        Transaction txn2 = createTxn(12) ;
        NodeTableTrans ntt2 = create(txn2, ntt1.getBaseNodeTable()) ;
        ntt2.begin(txn2) ;
        assertEquals(nodeId1, ntt2.getNodeIdForNode(node1)) ;
        assertEquals(nodeId2, ntt2.getNodeIdForNode(node2)) ;
        NodeId nodeId3 = ntt2.getAllocateNodeId(node3) ;
        assertEquals(nodeId3, ntt2.getNodeIdForNode(node3)) ;
        ntt2.commitPrepare(txn2) ;
        ntt2.commitEnact(txn2) ;
        ntt2.commitClearup(txn2) ;

        assertEquals(nodeId1, ntt1.getBaseNodeTable().getNodeIdForNode(node1)) ;
        assertEquals(nodeId2, ntt1.getBaseNodeTable().getNodeIdForNode(node2)) ;
        assertEquals(nodeId3, ntt1.getBaseNodeTable().getNodeIdForNode(node3)) ;
    }

    @Test 
    public void nodetrans_06()
    {   
        // 2 transactions - blocking reader - create a second NodeTableTrans
        Transaction txn1 = createTxn(11) ; 
        NodeTableTrans ntt1 = create(txn1, node1) ;
        NodeId nodeId1 = ntt1.getBaseNodeTable().getNodeIdForNode(node1) ;
        
        ntt1.begin(txn1) ;
        NodeId nodeId2 = ntt1.getAllocateNodeId(node2) ;
        ntt1.commitPrepare(txn1) ;
        
        // READ - don't enact
        Transaction txn2 = createTxn(12) ; 
        NodeTableTrans ntt2 = create(txn2, ntt1.getBaseNodeTable()) ;
        ntt2.begin(txn2) ;
        assertEquals(nodeId1, ntt2.getNodeIdForNode(node1)) ;
        assertEquals(nodeId2, ntt2.getNodeIdForNode(node2)) ;
        
        NodeId nodeId3 = ntt2.getAllocateNodeId(node3) ;
        assertEquals(nodeId3, ntt2.getNodeIdForNode(node3)) ;
        ntt2.commitPrepare(txn2) ;

        
        // READ ends.
        
        ntt1.commitEnact(txn1) ;
        ntt1.commitClearup(txn1) ;
        
        ntt2.commitEnact(txn2) ;
        ntt2.commitClearup(txn2) ;

        assertEquals(nodeId1, ntt1.getBaseNodeTable().getNodeIdForNode(node1)) ;
        assertEquals(nodeId2, ntt1.getBaseNodeTable().getNodeIdForNode(node2)) ;
        assertEquals(nodeId3, ntt1.getBaseNodeTable().getNodeIdForNode(node3)) ;
    }
    
    @Test 
    public void nodetrans_07()
    { 
        Transaction txn = createTxn(13) ;
        NodeTableTrans ntt = create(txn) ;

        ntt.begin(txn) ;
        assertEquals(NodeId.NodeIdAny, ntt.getNodeIdForNode(Node.ANY)) ;
        assertEquals(Node.ANY, ntt.getNodeForNodeId(NodeId.NodeIdAny)) ;
        
        assertTrue(ntt.isEmpty()) ;
        
        ntt.commitPrepare(txn) ;
        ntt.commitEnact(txn) ;
        ntt.commitClearup(txn) ;
    }

}
