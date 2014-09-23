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

import static com.hp.hpl.jena.tdb.lib.NodeLib.setHash ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.store.Hash ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** A concrete NodeTable based on native storage (string file and an index) */ 
public class NodeTableNative implements NodeTable
{
    // TODO Split into a general accessor (get and put (node,NodeId) pairs)
    // Abstracts the getAllocateNodeId requirements.
    
    protected ObjectFile objects ;
    protected Index nodeHashToId ;        // hash -> int
    private boolean syncNeeded = false ;
    
    // Delayed construction - must call init explicitly.
    protected NodeTableNative() {}
    
    // Combined into one constructor.
    public NodeTableNative(Index nodeToId, ObjectFile objectFile)
    {
        this() ;
        init(nodeToId, objectFile) ;
    }
    
    protected void init(Index nodeToId, ObjectFile objectFile)
    {
        this.nodeHashToId = nodeToId ;
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

    @Override
    public boolean containsNode(Node node) {
        NodeId x = getNodeIdForNode(node) ;
        return NodeId.isDoesNotExist(x) ;
    }

    @Override
    public boolean containsNodeId(NodeId nodeId) {
        Node x = getNodeForNodeId(nodeId) ;
        return x == null ;
    }

    // ---- The worker functions
    // Synchronization:
    // accessIndex and readNodeFromTable
    
    // Cache around this class further out in NodeTableCache are synchronized
    // to maintain cache validatity which indirectly sync access to the NodeTable.
    // But to be sure, we provide MRSW guarantees on this class.
    // (otherwise if no cache => disaster)
    // synchonization happens in accessIndex() and readNodeByNodeId
    
    // NodeId to Node worker.
    private Node _retrieveNodeByNodeId(NodeId id)
    {
        if ( NodeId.isDoesNotExist(id) )
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
        Hash hash = new Hash(nodeHashToId.getRecordFactory().keyLength()) ;
        setHash(hash, node) ;
        byte k[] = hash.getBytes() ;        
        // Key only.
        Record r = nodeHashToId.getRecordFactory().create(k) ;
        
        synchronized (this)  // Pair to readNodeFromTable.
        {
            // Key and value, or null
            Record r2 = nodeHashToId.find(r) ;
            if ( r2 != null )
            {
                // Found.  Get the NodeId.
                NodeId id = NodeId.create(r2.getValue(), 0) ;
                return id ;
            }

            // Not found.
            if ( ! create )
                return NodeId.NodeDoesNotExist ;
            // Write the node, which allocates an id for it.
            NodeId id = writeNodeToTable(node) ;

            // Update the r record with the new id.
            // r.value := id bytes ; 
            id.toBytes(r.getValue(), 0) ;

            // Put in index - may appear because of concurrency
            if ( ! nodeHashToId.add(r) )
                throw new TDBException("NodeTableBase::nodeToId - record mysteriously appeared") ;
            return id ;
        }
    }
    
    // -------- NodeId<->Node
    // Synchronization:
    //   write: in accessIndex
    //   read: synchronized here.
    // Only places for accessing the StringFile.
    
    private final NodeId writeNodeToTable(Node node)
    {
        syncNeeded = true ;
        // Synchronized in accessIndex
        long x = NodeLib.encodeStore(node, getObjects()) ;
        return NodeId.create(x);
    }
    

    private final Node readNodeFromTable(NodeId id)
    {
        synchronized (this) // Pair to accessIndex
        {
            if ( id.getId() >= getObjects().length() )
                return null ;
            return NodeLib.fetchDecode(id.getId(), getObjects()) ;
        }
    }
    // -------- NodeId<->Node

    @Override
    public synchronized void close()
    {
        // Close once.  This may be shared (e.g. triples table and quads table). 
        if ( nodeHashToId != null )
        {
            nodeHashToId.close() ;
            nodeHashToId = null ;
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
    public Iterator<Pair<NodeId, Node>> all() { return all2() ; }
    
    private Iterator<Pair<NodeId, Node>> all1()
    
    {
        // Could be quicker by hoping down the objects files.
        Iterator<Record> iter = nodeHashToId.iterator() ;

        Transform<Record, Pair<NodeId, Node>> transform = new Transform<Record, Pair<NodeId, Node>>() {
            @Override
            public Pair<NodeId, Node> convert(Record item)
            {
                NodeId id = NodeId.create(item.getValue(), 0) ;
                Node n = NodeLib.fetchDecode(id.getId(), getObjects()) ;
                return new Pair<>(id, n) ;
            }};
        return Iter.map(iter, transform) ;
    }

    private Iterator<Pair<NodeId, Node>> all2()
    {
        Iterator<Pair<Long, ByteBuffer>> objs = objects.all() ; 
        
        Transform<Pair<Long, ByteBuffer>, Pair<NodeId, Node>> transform = new Transform<Pair<Long, ByteBuffer>, Pair<NodeId, Node>>() {
            @Override
            public Pair<NodeId, Node> convert(Pair<Long, ByteBuffer> item)
            {
                NodeId id = NodeId.create(item.car().longValue()) ;
                ByteBuffer bb = item.cdr();
                Node n = NodeLib.decode(bb) ;
                return new Pair<>(id, n) ;
            }
        };
        return Iter.map(objs, transform) ;
    }

    @Override
    public void sync() 
    { 
        if ( syncNeeded )
        {
            if ( nodeHashToId != null )
                nodeHashToId.sync() ;
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
        return getObjects().isEmpty() ;
    }
}
