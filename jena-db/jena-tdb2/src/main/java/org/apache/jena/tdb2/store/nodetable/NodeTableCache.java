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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionListener;
import org.apache.jena.graph.Node;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.store.NodeId;

/**
 * Cache wrapper around a NodeTable. Assumes all access goes through this
 * wrapper. Read-cache - write caching is done via the object file used by the
 * base NodeTable.
 */
public class NodeTableCache implements NodeTable, TransactionListener {
    // These caches are updated together.
    // See synchronization in _retrieveNodeByNodeId and _idForNode.
    // The cache is assumed to be single operation-thread-safe.
    // The buffering is for updates so that if it aborts, the changes are not made;
    // the underlying node table, being transactional, also does not make the changes.
    //
    // It does not matter if a readers can see nodes from a completed now-finished
    // writer transaction. Nodes in the node table do not mean triples exist and only triples detemine
    // the state of the data.
    //
    // Where there are only readers active the ThreadBufferingCache caches act as
    // pass-through and the not-present cache can be updated by any reader.
    //
    // When there is an active writer, the ThreadBufferingCache caches add a
    // write-visible-only caching and only the writer can update the "not-present"
    // cache. Because the node table is append-only (nodes are not deleted), it can
    // mean a node which was not-present is added and the not-present cache now does
    // not catch that for a previous version reader. This does not matter, the small
    // not-present cache is only a speed-up and does not have to be correct
    // for missing nodes (it can't have entries for nodes that do exist in visible
    // data).

    private ThreadBufferingCache<Node, NodeId> node2id_Cache = null;
    private ThreadBufferingCache<NodeId, Node> id2node_Cache = null;

    // A small cache of "known unknowns" to speed up searching for impossible things.
    private Cache<Node, Object> notPresent    = null;
    private NodeTable           baseTable;
    private final Object        lock          = new Object();
    private volatile Thread     writingThread;

    public static NodeTable create(NodeTable nodeTable, StoreParams params) {
        int nodeToIdCacheSize = params.getNode2NodeIdCacheSize();
        int idToNodeCacheSize = params.getNodeId2NodeCacheSize();
        if ( nodeToIdCacheSize <= 0 && idToNodeCacheSize <= 0 )
            return nodeTable;
        return create(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, params.getNodeMissCacheSize());
    }

    private static NodeTable create(NodeTable nodeTable, int nodeToIdCacheSize, int idToNodeCacheSize, int nodeMissesCacheSize) {
        if ( nodeToIdCacheSize <= 0 && idToNodeCacheSize <= 0 )
            return nodeTable;
        return new NodeTableCache(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, nodeMissesCacheSize);
    }

    private NodeTableCache(NodeTable baseTable, int nodeToIdCacheSize, int idToNodeCacheSize, int nodeMissesCacheSize) {
        this.baseTable = baseTable;
        if ( nodeToIdCacheSize > 0 )
            node2id_Cache = createCache("nodeToId", nodeToIdCacheSize, 1000);
        if ( idToNodeCacheSize > 0 )
            id2node_Cache = createCache("idToNode", idToNodeCacheSize, 1000);
        if ( nodeMissesCacheSize > 0 )
            notPresent = CacheFactory.createCache(nodeMissesCacheSize);
    }

    private static <Key, Value> ThreadBufferingCache<Key, Value> createCache(String label, int mainCachesize, int bufferSize) {
        Cache<Key, Value> cache = CacheFactory.createCache(mainCachesize);
        return new ThreadBufferingCache<>(label, cache, bufferSize);
    }

    // ---- Cache access, no going to underlying table.

    public Node getNodeForNodeIdCache(NodeId id) {
        return id2node_Cache.getIfPresent(id);
    }

    public NodeId getNodeIdForNodeCache(Node node) {
        return node2id_Cache.getIfPresent(node);
    }

    public boolean isCachedNodeId(NodeId id) {
        return getNodeForNodeIdCache(id) != null;
    }

    public boolean isCachedNode(Node node) {
        return getNodeIdForNodeCache(node) != null;
    }

    // ---- Cache access

    @Override
    public final NodeTable wrapped() {
        return baseTable;
    }

    /** Get the Node for this NodeId, or null if none */
    @Override
    public Node getNodeForNodeId(NodeId id) {
        return _retrieveNodeByNodeId(id);
    }

    /** Find the NodeId for a node, or return NodeId.NodeDoesNotExist */
    @Override
    public NodeId getNodeIdForNode(Node node) {
        return _idForNode(node, false);
    }

    /**
     * Find the NodeId for a node, allocating a new NodeId if the Node does not
     * yet have a NodeId
     */
    @Override
    public NodeId getAllocateNodeId(Node node) {
        return _idForNode(node, true);
    }

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
    public List<NodeId> bulkNodeToNodeId(List<Node> required, boolean withAllocation) {
        synchronized(lock) {
            List<Node> nodes = new ArrayList<>();
            for ( Node n : required ) {
                //
                if ( getNodeIdForNodeCache(n) == null )
                    nodes.add(n);
            }
            // Check bulk access.
            List<NodeId> x = baseTable.bulkNodeToNodeId(nodes, true);
            for ( int i = 0; i < nodes.size() ; i++ ) {
                Node n = nodes.get(i);
                NodeId nid = x.get(i);
                cacheUpdate(n ,nid);
            }
            return x;
        }
    }

    @Override
    public List<Node> bulkNodeIdToNode(List<NodeId> nodeIds) {
        return NodeTableOps.bulkNodeIdToNodeImpl(this, nodeIds);
    }

    // ---- The worker functions
    // NodeId ==> Node
    private Node _retrieveNodeByNodeId(NodeId id) {
        if ( NodeId.isDoesNotExist(id) )
            return null;
        if ( NodeId.isAny(id) )
            return null;
        // Try once outside the synchronized
        // (Cache access is thread-safe)
        Node n = cacheLookup(id);
        if ( n != null )
            return n;

        synchronized (lock) {
            // Lock to update two caches consistently.
            // Verify cache miss
            n = cacheLookup(id);
            if ( n != null )
                return n;

            n = baseTable.getNodeForNodeId(id);
            cacheUpdate(n, id);
            return n;
        }
    }

    // Node ==> NodeId
    private NodeId _idForNode(Node node, boolean allocate) {
        if ( node == Node.ANY )
            return NodeId.NodeIdAny;
        // Try once outside the synchronized
        // (Cache access is thread-safe.)
        NodeId nodeId = cacheLookup(node);
        if ( nodeId != null )
            return nodeId;
        synchronized (lock) {
            // Update two caches inside synchronized.
            // Check still valid.
            nodeId = cacheLookup(node);
            if ( nodeId != null )
                return nodeId;

            if ( allocate )
                nodeId = baseTable.getAllocateNodeId(node);
            else {
                if ( notPresent(node) )
                    // Known not be in the baseTable.
                    return NodeId.NodeDoesNotExist;
                else
                    nodeId = baseTable.getNodeIdForNode(node);
            }
            // Ensure caches have it. Includes recording "no such node"
            cacheUpdate(node, nodeId);
            return nodeId;
        }
    }

    // ----------------
    // ---- Only places that the caches are touched

    /**
     * Test whether in the "not present" cache.
     * True means "known to be absent from the baseTable".
     */
    private boolean notPresent(Node node) {
        if ( notPresent == null )
            return false;
        return notPresent.containsKey(node);
    }

    /**
     * Check caches to see if we can map a NodeId to a Node. Returns null on no
     * cache entry.
     */
    private Node cacheLookup(NodeId id) {
        if ( id2node_Cache == null )
            return null;
        return id2node_Cache.getIfPresent(id);
    }

    /**
     * Check caches to see if we can map a Node to a NodeId. Returns null on no
     * cache entry.
     */
    private NodeId cacheLookup(Node node) {
        // Remember things known (currently) not to exist.
        // Does not matter if notPresent is being updated elsewhere.
        return node2id_Cache.getIfPresent(node);
    }

    /** Update the Node&lt;-&gt;NodeId caches */
    private void cacheUpdate(Node node, NodeId id) {
        if ( node == null )
            return;

        // synchronized is further out.
        // The "notPresent" cache is used to note whether a node
        // is known not to exist in the baseTable..
        // This must be specially handled later if the node is added.
        // Only top-level transactions can add nodes to the "notPresent" cache.
        if ( NodeId.isDoesNotExist(id) ) {
            if ( notPresent != null && inTopLevelTxn())
                notPresent.put(node, Boolean.TRUE);
            return;
        }

        if ( id == NodeId.NodeIdAny ) {
            Log.warn(this, "Attempt to cache NodeIdAny - ignored");
            return;
        }

        if ( node2id_Cache != null )
            node2id_Cache.put(node, id);
        if ( id2node_Cache != null )
            id2node_Cache.put(id, node);
        // Remove if previously marked "not present"
        if ( notPresent != null )
            notPresent.remove(node);
    }

    // A top-level transaction can update the not-present cache.
    // It is either
    // - a write transaction or
    // - a read transaction and no active writer.
    private boolean inTopLevelTxn() {
        Thread writer = writingThread;
        return (writer == null) || (writer == Thread.currentThread());
    }

    // -- TransactionListener
    @Override
    public void notifyTxnStart(Transaction transaction) {
        if (transaction.isWriteTxn())
            updateStart();
    }

    @Override
    public void notifyPromoteFinish(Transaction transaction) {
        if(transaction.isWriteTxn())
            updateStart();
    }

    @Override
    public void notifyCompleteFinish(Transaction transaction) {
        if(transaction.isWriteTxn()) {
            updateCommit();
        }
    }

    @Override
    public void notifyAbortStart(Transaction transaction) {
        if(transaction.isWriteTxn())
            updateAbort();
    }
    // -- TransactionListener

    // The cache is "optimistic" - nodes are added during the transaction.
    // The underlying file has them "transactionally".
    //
    // On abort, it does need to be undone because the underlying NodeTable
    // being cached will not have them.
    //
    // We don't "undo" for abort because it would mean keeping an data structure that
    // is related to the size of the transaction and if in-memory, a limitation of
    // scale.
    private void updateStart() {
        node2id_Cache.enableBuffering();
        id2node_Cache.enableBuffering();
        writingThread = Thread.currentThread();
    }

    private void updateAbort() {
        writingThread = null;

        node2id_Cache.dropBuffer();
        id2node_Cache.dropBuffer();
    }

    private void updateCommit() {
        writingThread = null;
        // Write to main caches.
        node2id_Cache.flushBuffer();
        id2node_Cache.flushBuffer();
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            if ( node2id_Cache != null )
                return node2id_Cache.isEmpty();
            if ( id2node_Cache != null )
                id2node_Cache.isEmpty();
            // Write through.
            return baseTable.isEmpty();
        }
    }

    @Override
    public synchronized void close() {
        if ( baseTable == null )
            // Already closed
            return;
        baseTable.close();
        node2id_Cache = null;
        id2node_Cache = null;
        notPresent = null;
        baseTable = null;
        writingThread = null;
    }

    @Override
    public void sync() {
        baseTable.sync();
    }

    @Override
    public Iterator<Pair<NodeId, Node>> all() {
        if ( false )
            testForConsistency();
        return baseTable.all();
    }

    private void testForConsistency() {
        Iterator<Node> iter1 = Iter.toList(node2id_Cache.keys()).iterator();

        for (; iter1.hasNext() ; ) {
            Node n = iter1.next();

            NodeId nId = node2id_Cache.getIfPresent(n);
            if ( !id2node_Cache.containsKey(nId) )
                throw new TDBException("Inconsistent: " + n + " => " + nId);
            if ( notPresent.containsKey(n) )
                throw new TDBException("Inconsistent: " + n + " in notPresent cache (1)");
        }
        Iterator<NodeId> iter2 = Iter.toList(id2node_Cache.keys()).iterator();
        for (; iter2.hasNext() ; ) {
            NodeId nId = iter2.next();
            Node n = id2node_Cache.getIfPresent(nId);
            if ( !node2id_Cache.containsKey(n) )
                throw new TDBException("Inconsistent: " + nId + " => " + n);
            if ( notPresent.containsKey(n) )
                throw new TDBException("Inconsistent: " + n + " in notPresent cache (2)");
        }
    }

    @Override
    public String toString() {
        return "Cache(" + baseTable.toString() + ")";
    }
}
