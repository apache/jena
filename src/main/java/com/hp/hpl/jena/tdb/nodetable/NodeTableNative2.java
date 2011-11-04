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

package com.hp.hpl.jena.tdb.nodetable;

import static com.hp.hpl.jena.tdb.lib.NodeLib.setHash ;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Bytes;
import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.store.Hash ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class NodeTableNative2 implements NodeTable
{
    // Assumes an StringFile and an Indexer, which may be an Index but allows
    // this to be overriden for a direct use of BDB.

    protected ObjectFile objects ;
    protected Index nodeHashToOffset ;        // hash -> int
    private boolean syncNeeded = false ;
    
    // Delayed construction - must call init explicitly.
    protected NodeTableNative2() {}
    
    // Combined into one constructor.
    public NodeTableNative2(Index nodeToId, ObjectFile objectFile)
    {
        this() ;
        init(nodeToId, objectFile) ;
    }
    
    protected void init(Index nodeToId, ObjectFile objectFile)
    {
        this.nodeHashToOffset = nodeToId ;
        this.objects = objectFile;
    }

    // ---- Public interface for Node <==> NodeId

    /** Get the Node for this NodeId, or null if none */
    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        return _retrieveNodeByNodeId(id) ;
    }

    /** Find the NodeId for a node, or return NodeId.NodeDoesNotExist */ 
    @Override
    public NodeId getNodeIdForNode(Node node)  { return _idForNode(node, false) ; }

    /** Find the NodeId for a node, allocating a new NodeId if the Node does not yet have a NodeId */ 
    @Override
    public NodeId getAllocateNodeId(Node node)  { return _idForNode(node, true) ; }

    // ---- The worker functions
    // Synchronization:
    // accesIndex and readNodeFromTable
    
    // Cache around this class further out in NodeTableCache are synchronized
    // to maintain cache validatity which indirectly sync access to the NodeTable.
    // But to be sure, we provide MRSW guarantees on this class.
    // (otherwise if no cache => disaster)
    // synchonization happens in accessIndex() and readNodeByNodeId
    
    // NodeId to Node worker.
    private Node _retrieveNodeByNodeId(NodeId id)
    {
        if ( NodeId.doesNotExist(id) )
            return null ;
        if ( NodeId.isAny(id) )
            return null ;
        
        Node n = readNodeFromTable(id) ;
        return n ;
    }

    // ----------------
    
    // Node to NodeId worker
    // Find a node, possibly placing it in the node file as well
    private NodeId _idForNode(Node node, boolean allocate)
    {
        if ( node == Node.ANY )
            return NodeId.NodeIdAny ;
        
        // synchronized in accessIndex
        NodeId nodeId = accessIndex(node, allocate) ;
        return nodeId ;
    }
    
    protected final NodeId accessIndex(Node node, boolean create)
    {
        Hash hash = new Hash(nodeHashToOffset.getRecordFactory().keyLength()) ;
        setHash(hash, node) ;
        byte k[] = hash.getBytes() ;        
        // Key only.
        Record r = nodeHashToOffset.getRecordFactory().create(k) ;
        
        synchronized (this)  // Pair to readNodeFromTable.
        {
            // Key and value, or null
            Record r2 = nodeHashToOffset.find(r) ;
            if ( r2 != null )
            {
                // Found.  Get the NodeId.
            	NodeId id = NodeId.create(k, 0) ;
                return id ;
            }

            // Not found.
            if ( ! create )
                return NodeId.NodeDoesNotExist ;

            // Write the node, which allocates an id for it.
            long offset = writeNodeToTable(node) ;

            // Update the r record with the new id.
            // r.value := id bytes ; 
            Bytes.setLong(offset, r.getValue(), 0) ;

            // Put in index - may appear because of concurrency
            if ( ! nodeHashToOffset.add(r) )
                throw new TDBException("NodeTableBase::nodeToId - record mysteriously appeared") ;
            
            NodeId id = NodeId.create(k, 0) ;
            return id ;
        }
    }
    
    // -------- NodeId<->Node
    // Synchronization:
    //   write: in accessIndex
    //   read: synchronized here.
    // Only places for accessing the StringFile.
    
    private final long writeNodeToTable(Node node)
    {
        syncNeeded = true ;
        // Synchroized in accessIndex
        long offset = NodeLib.encodeStore(node, getObjects()) ;
        return offset;
    }
    

    private final Node readNodeFromTable(NodeId id)
    {
        synchronized (this) // Pair to accessIndex
        {
            byte k[] = Bytes.packLong(id.getId()) ;        
            // Key only.
            Record r = nodeHashToOffset.getRecordFactory().create(k) ;
        	
            Record r2 = nodeHashToOffset.find(r) ;
            if ( r2 != null )
            {
                long offset = Bytes.getLong(r2.getValue()) ;
                if ( offset >= getObjects().length() )
                    return null ;
                return NodeLib.fetchDecode(offset, getObjects()) ;
            } 
            else
            	return null ;
        }
    }
    // -------- NodeId<->Node

    @Override
    public synchronized void close()
    {
        // Close once.  This may be shared (e.g. triples table and quads table). 
        if ( nodeHashToOffset != null )
        {
            nodeHashToOffset.close() ;
            nodeHashToOffset = null ;
        }
        if ( getObjects() != null )
        {
            getObjects().close() ;
            objects = null ;
        }
    }

    @Override
    public NodeId allocOffset()
    {
        return NodeId.create(getObjects().length()) ;
    }
    
    // Not synchronized
    @Override
    public Iterator<Pair<NodeId, Node>> all() { return all1() ; }
    
    private Iterator<Pair<NodeId, Node>> all1()
    
    {
        Iterator<Record> iter = nodeHashToOffset.iterator() ; ;

        Transform<Record, Pair<NodeId, Node>> transform = new Transform<Record, Pair<NodeId, Node>>() {
            @Override
            public Pair<NodeId, Node> convert(Record item)
            {
            	NodeId id = NodeId.create(item.getKey()) ;
            	long offset = Bytes.getLong(item.getValue()) ;
                Node n = NodeLib.fetchDecode(offset, getObjects()) ;
                return new Pair<NodeId, Node>(id, n) ;
            }};
        return Iter.map(iter, transform) ;
    }

    @Override
    public void sync() 
    { 
        if ( syncNeeded )
        {
            if ( nodeHashToOffset != null )
                nodeHashToOffset.sync() ;
            if ( getObjects() != null )
                getObjects().sync() ;
            syncNeeded = false ;
        }
    }

    public ObjectFile getObjects()
    {
        return objects;
    }
    
    @Override
    public String toString() { return objects.getLabel() ; }

    @Override
    public boolean isEmpty()
    {
        return false ;
    }
}
