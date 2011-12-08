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

package com.hp.hpl.jena.tdb.nodetable;

import java.util.Iterator ;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;
import org.openjena.atlas.lib.CacheSet ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.graph.Node ;
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
    private CacheSet<Node> notPresent ;
    private NodeTable baseTable ;
    private Object lock = new Object() ;

    public static NodeTable create(NodeTable nodeTable, int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        if ( nodeToIdCacheSize <= 0 && idToNodeCacheSize <= 0 )
            return nodeTable ;
        return new NodeTableCache(nodeTable, nodeToIdCacheSize, idToNodeCacheSize) ;
    }

    private NodeTableCache(NodeTable baseTable, int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        this.baseTable = baseTable ;
        if ( nodeToIdCacheSize > 0) 
            node2id_Cache = CacheFactory.createCache(nodeToIdCacheSize) ;
        if ( idToNodeCacheSize > 0)
            id2node_Cache = CacheFactory.createCache(idToNodeCacheSize) ;
        notPresent = CacheFactory.createCacheSet(100) ;
    }

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
    // NodeId ==> Node
    private Node _retrieveNodeByNodeId(NodeId id)
    {
        if ( NodeId.isDoesNotExist(id) )
            return null ;
        if ( NodeId.isAny(id) )
            return null ;

        synchronized (lock)
        {
            Node n = cacheLookup(id) ;   // Includes known to not exist
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
        if ( notPresent.contains(node) ) return null ;
        if ( node2id_Cache == null ) return null ;
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
        if ( notPresent.contains(node) )
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
        return baseTable.all() ;
    }
    
    @Override
    public String toString() { return "Cache("+baseTable.toString()+")" ; }
}
