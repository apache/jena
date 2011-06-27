/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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
        if ( NodeId.doesNotExist(id) )
            return null ;
        if ( NodeId.isAny(id) )
            return null ;

        synchronized (this)
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
        
        synchronized (this)
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
        if ( NodeId.doesNotExist(id) )
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
}
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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