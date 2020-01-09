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

package org.apache.jena.tdb2.store.nodetable;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.index.Index;
import org.apache.jena.graph.Node;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.lib.NodeLib;
import org.apache.jena.tdb2.store.Hash;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;

/** A framework for a NodeTable based on native storage (string file and an index).
 *  This class manages the index, and delegates the node storage.
 */
public abstract class NodeTableNative implements NodeTable
{
    protected Index nodeHashToId;        // hash -> int
    private boolean syncNeeded = false;

    public NodeTableNative(Index nodeToId) {
        this.nodeHashToId = nodeToId;
    }
    // ---- Public interface for Node <==> NodeId

    /** Get the Node for this NodeId, or null if none */
    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        return _retrieveNodeByNodeId(id);
    }

    /** Find the NodeId for a node, or return NodeId.NodeDoesNotExist */
    @Override
    public NodeId getNodeIdForNode(Node node)  { return _idForNode(node, false); }

    /** Find the NodeId for a node, allocating a new NodeId if the Node does not yet have a NodeId */
    @Override
    public NodeId getAllocateNodeId(Node node)  { return _idForNode(node, true); }

    @Override
    public boolean containsNode(Node node) {
        NodeId x = getNodeIdForNode(node);
        return NodeId.isDoesNotExist(x);
    }

    @Override
    public boolean containsNodeId(NodeId nodeId) {
        Node x = getNodeForNodeId(nodeId);
        return x == null;
    }

    @Override
    public List<NodeId> bulkNodeToNodeId(List<Node> nodes, boolean withAllocation) {
        return NodeTableOps.bulkNodeToNodeIdImpl(this, nodes, withAllocation);
    }

    @Override
    public List<Node> bulkNodeIdToNode(List<NodeId> nodeIds) {
        return NodeTableOps.bulkNodeIdToNodeImpl(this, nodeIds);
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
            return null;
        if ( NodeId.isAny(id) )
            return null;
        synchronized (this) {
            Node n = readNodeFromTable(id);
            return n;
        }
    }

    // ----------------

    // Node to NodeId worker
    // Find a node, possibly placing it in the node file as well
    private NodeId _idForNode(Node node, boolean allocate)
    {
        if ( node == Node.ANY )
            return NodeId.NodeIdAny;

        // synchronized in accessIndex
        NodeId nodeId = accessIndex(node, allocate);
        return nodeId;
    }

    protected final NodeId accessIndex(Node node, boolean create)
    {
        Hash hash = new Hash(nodeHashToId.getRecordFactory().keyLength());
        NodeLib.setHash(hash, node);
        byte k[] = hash.getBytes();
        // Key only.
        Record r = nodeHashToId.getRecordFactory().create(k);

        synchronized (this)  // Pair to readNodeFromTable.
        {
            // Key and value, or null
            Record r2 = nodeHashToId.find(r);
            if ( r2 != null )
            {
                // Found.  Get the NodeId.
                NodeId id = NodeIdFactory.get(r2.getValue(), 0);
                return id;
            }

            // Not found.
            if ( ! create )
                return NodeId.NodeDoesNotExist;
            // Write the node, which allocates an id for it.
            syncNeeded = true;
            NodeId id = writeNodeToTable(node);

            // Update the r record with the new id.
            // r.value := id bytes;
            NodeIdFactory.set(id, r.getValue(), 0);

            // Put in index - may appear because of concurrency
            if ( ! nodeHashToId.insert(r) )
                throw new TDBException("NodeTableBase::nodeToId - record mysteriously appeared");
            return id;
        }
    }

    // -------- NodeId<->Node
    // Synchronization:
    //   write: in accessIndex
    //   read: in _retrieveNodeByNodeId
    // Only places for accessing the StringFile.

    abstract protected NodeId writeNodeToTable(Node node);
    abstract protected Node readNodeFromTable(NodeId id);
    abstract protected void syncSub();
    abstract protected void closeSub();

    // -------- NodeId<->Node

    @Override
    public synchronized void close()
    {
        // Close once.  This may be shared (e.g. triples table and quads table).
        if ( nodeHashToId != null )
        {
            nodeHashToId.close();
            closeSub();
            nodeHashToId = null;
        }
    }

    // Not synchronized
    @Override
    public Iterator<Pair<NodeId, Node>> all() { return all2(); }

    private Iterator<Pair<NodeId, Node>> all2()
    {
        throw new NotImplemented();
    }

    @Override
    public void sync()
    {
        if ( syncNeeded )
        {
            syncSub();
            if ( nodeHashToId != null )
                nodeHashToId.sync();
            syncNeeded = false;
        }
    }

    @Override
    public boolean isEmpty()
    {
        return nodeHashToId.isEmpty();
    }

    @Override
    public NodeTable wrapped() {
        return null;
    }
}
