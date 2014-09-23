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

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.CacheSet ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Cache wrapper around a NodeTable.  
 * Assumes all access goes through this wrapper.
 * Read-cache - write caching is done via the object file used by the base NodeTable. 
 */ 
public class NodeTableCache implements NodeTable
{
    // These caches are updated together.
    // See synchronization in _retrieveNodeByNodeId and _idForNode
    private Cache<Node, NodeId> node2id_Cache = null ;
    private Cache<NodeId, Node> id2node_Cache = null ;
    
    // A small cache of "known unknowns" to speed up searching for impossible things.   
    // Cache update needed on NodeTable changes because a node may become "known"
    private CacheSet<Node> notPresent = null ;
    private NodeTable baseTable ;
    private Object lock = new Object() ;

    public static NodeTable create(NodeTable nodeTable, StoreParams params) {
        int nodeToIdCacheSize = params.getNode2NodeIdCacheSize() ;
        int idToNodeCacheSize = params.getNodeId2NodeCacheSize() ;
        if ( nodeToIdCacheSize <= 0 && idToNodeCacheSize <= 0 )
            return nodeTable ;
        return new NodeTableCache(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, params.getNodeMissCacheSize()) ;
    }

    public static NodeTable create(NodeTable nodeTable, int nodeToIdCacheSize, int idToNodeCacheSize, int nodeMissesCacheSize)
    {
        if ( nodeToIdCacheSize <= 0 && idToNodeCacheSize <= 0 )
            return nodeTable ;
        return new NodeTableCache(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, nodeMissesCacheSize) ;
    }

    private NodeTableCache(NodeTable baseTable, int nodeToIdCacheSize, int idToNodeCacheSize, int nodeMissesCacheSize)
    {
        this.baseTable = baseTable ;
        if ( nodeToIdCacheSize > 0) 
            node2id_Cache = CacheFactory.createCache(nodeToIdCacheSize) ;
        if ( idToNodeCacheSize > 0)
            id2node_Cache = CacheFactory.createCache(idToNodeCacheSize) ;
        if ( nodeMissesCacheSize > 0 )
            notPresent = CacheFactory.createCacheSet(nodeMissesCacheSize) ;
    }

    public final NodeTable getBaseTable() { return baseTable ; } 
    
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
    // NodeId ==> Node
    private Node _retrieveNodeByNodeId(NodeId id)
    {
        if ( NodeId.isDoesNotExist(id) )
            return null ;
        if ( NodeId.isAny(id) )
            return null ;

        synchronized (lock)
        {
            Node n = cacheLookup(id) ;
            if ( n != null )
                return n ; 

            if ( baseTable == null )
                System.err.println(""+this) ;
            
            n = baseTable.getNodeForNodeId(id) ;
            cacheUpdate(n, id) ;
            return n ;
        }
    }

    // Node ==> NodeId
    private NodeId _idForNode(Node node, boolean allocate)
    {
        if ( node == Node.ANY )
            return NodeId.NodeIdAny ;
        
        synchronized (lock)
        {
            // Check caches.
            NodeId nodeId = cacheLookup(node) ;
            if ( nodeId != null )
                return nodeId ; 

            if ( allocate )
                nodeId = baseTable.getAllocateNodeId(node) ;
            else
                nodeId = baseTable.getNodeIdForNode(node) ;

            // Ensure caches have it.  Includes recording "no such node"
            cacheUpdate(node, nodeId) ;
            return nodeId ;
        }
    }

    // ----------------
    // ---- Only places that the caches are touched
    
    /** Check caches to see if we can map a NodeId to a Node. Returns null on no cache entry. */ 
    private Node cacheLookup(NodeId id)
    {
        if ( id2node_Cache == null ) return null ;
        return id2node_Cache.get(id) ;
    }
    
    /** Check caches to see if we can map a Node to a NodeId. Returns null on no cache entry. */ 
    private NodeId cacheLookup(Node node)
    {
        // Remember things known (currently) not to exist
        if ( notPresent != null && notPresent.contains(node) ) 
            return null ;
        if ( node2id_Cache == null )
            return null ;
        return node2id_Cache.get(node) ; 
    }

    /** Update the Node->NodeId caches */
    private void cacheUpdate(Node node, NodeId id)
    {
        // synchronized is further out.
        // The "notPresent" cache is used to note whether a node
        // is known not to exist.
        // This must be specially handled later if the node is added. 
        if ( NodeId.isDoesNotExist(id) )
        {
            if ( notPresent != null ) 
                notPresent.add(node) ;
            return ;
        }
        
        if ( id == NodeId.NodeIdAny )
        {
            Log.warn(this, "Attempt to cache NodeIdAny - ignored") ;
            return ;
        }
        
        if ( node2id_Cache != null )
            node2id_Cache.put(node, id) ;
        if ( id2node_Cache != null )
            id2node_Cache.put(id, node) ;
        // Remove if previously marked "not present"
        if ( notPresent != null && notPresent.contains(node) )
            notPresent.remove(node) ;
    }
    // ----

    @Override
    public NodeId allocOffset()
    {
        return baseTable.allocOffset() ;
    }
    
    @Override
    public boolean isEmpty()
    {
        synchronized (lock)
        {
            if ( node2id_Cache != null )
                return node2id_Cache.isEmpty() ;
            if ( id2node_Cache != null )
                id2node_Cache.isEmpty() ;
            // Write through.
            return baseTable.isEmpty() ;
        }
    }

    @Override
    public synchronized void close()
    {
        if ( baseTable == null )
            // Already closed (NodeTables can be shared so .close via two routes).
            return ;

        baseTable.close() ;
        node2id_Cache = null ;
        id2node_Cache = null ;
        notPresent = null ;
        baseTable = null ;
    }

    @Override
    public void sync() { baseTable.sync() ; }
    
    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        if ( false )
            testForConsistency() ;
        return baseTable.all() ;
    }
    
    private void testForConsistency()
    {
        Iterator<Node> iter1 = Iter.toList(node2id_Cache.keys()).iterator() ;
        
        for ( ; iter1.hasNext() ; )
        {
            Node n = iter1.next() ;
            
            NodeId nId = node2id_Cache.get(n) ; 
            if ( !id2node_Cache.containsKey(nId) )
                throw new TDBException("Inconsistent: "+n+" => "+nId) ;
            if ( notPresent.contains(n) )
                throw new TDBException("Inconsistent: "+n+" in notPresent cache (1)") ;
        }
        Iterator<NodeId> iter2 = Iter.toList(id2node_Cache.keys()).iterator() ;
        for ( ; iter2.hasNext() ; )
        {
            NodeId nId = iter2.next() ;
            Node n =  id2node_Cache.get(nId) ; 
            if ( !node2id_Cache.containsKey(n) )
                throw new TDBException("Inconsistent: "+nId+" => "+n) ;
            if ( notPresent.contains(n) )
                throw new TDBException("Inconsistent: "+n+" in notPresent cache (2)") ;
        }
        
        
    }
    
    @Override
    public String toString() { return "Cache("+baseTable.toString()+")" ; }
}
