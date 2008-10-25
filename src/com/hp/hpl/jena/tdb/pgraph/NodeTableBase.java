/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import static com.hp.hpl.jena.tdb.lib.NodeLib.decode;
import static com.hp.hpl.jena.tdb.lib.NodeLib.encode;
import static com.hp.hpl.jena.tdb.lib.NodeLib.setHash;
import lib.Cache;
import lib.CacheFactory;
import lib.CacheSetLRU;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.util.ALog;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.index.Index;

public abstract class NodeTableBase implements NodeTable
{
    // Assumes an ObjectFile and an Indexer, which may be an Index but allows
    // this to be overriden for a direct use of BDB.

    public ObjectFile objects ;
    protected Index nodeHashToId ;        // hash -> int
    
    // Currently, these caches are updated together.
    protected Cache<Node, NodeId> node2id_Cache = null ;
    protected Cache<NodeId, Node> id2node_Cache = null ;
    
    // A small cache of "known unknowns" to speed up searching for impossible things.   
    // Cache update needed on NodeTable changes because a node may become "known"
    protected CacheSetLRU<Node> notPresent ;

    // Delayed construction - must call init explicitly.
    protected NodeTableBase() {}
    
    // Combined into one constructor.
    protected NodeTableBase(Index nodeToId, ObjectFile objectFile, int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        this() ;
        init(nodeToId, objectFile, idToNodeCacheSize, idToNodeCacheSize) ;
    }
    
    protected void init(Index nodeToId, ObjectFile objectFile,
                        int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        this.nodeHashToId = nodeToId ;
        this.objects = objectFile ;
        if ( nodeToIdCacheSize > 0) 
            node2id_Cache = CacheFactory.createCache(nodeToIdCacheSize) ;
        if ( idToNodeCacheSize > 0)
            id2node_Cache = CacheFactory.createCache(idToNodeCacheSize) ;
        notPresent = new CacheSetLRU<Node>(100) ;
    }

    // ---- Public interface for Node <==> NodeId

    /** Get the Node for this NodeId, or null if none */
    @Override
    public Node retrieveNodeByNodeId(NodeId id)
    {
        return _retrieveNodeByNodeId(id) ;
    }

    /** Find the NodeId for a node, or return NodeId.NodeDoesNotExist */ 
    @Override
    public NodeId nodeIdForNode(Node node)  { return _idForNode(node, false) ; }

    /** Find the NodeId for a node, allocating a new NodeId if th eNode does not yet have a NodeId */ 
    @Override
    public NodeId storeNode(Node node)  { return _idForNode(node, true) ; }

    // ---- The worker functions
    
    private Node _retrieveNodeByNodeId(NodeId id)
    {
        if ( id == NodeId.NodeDoesNotExist )
            return null ;
        if ( id == NodeId.NodeIdAny )
            return null ;
        
        // Inline?
        Node n = NodeId.extract(id) ;
        if ( n != null )
            return n ; 
        
        synchronized (this)
        {
            n = cacheLookup(id) ;   // Includes known to not exist
            if ( n != null )
                return n ; 

            n = readNodeByNodeId(id) ;
            cacheUpdate(n, id) ;
            return n ;
            
        }
    }

    // ----------------
    // Node to Node Id worker
    // Find a node, possibly placing it in the node file as well
    private NodeId _idForNode(Node node, boolean allocate)
    {
        if ( node == Node.ANY )
            return NodeId.NodeIdAny ;
        
        // Inline?
        NodeId nodeId = NodeId.inline(node) ;
        if ( nodeId != null )
            return nodeId ;

        synchronized (this)
        {
            // Check caches.
            nodeId = cacheLookup(node) ;
            if ( nodeId != null )
                return nodeId ; 

            nodeId = accessIndex(node, allocate) ;

            // Ensure caches have it.  Includes recording "no such node"
            cacheUpdate(node, nodeId) ;
            return nodeId ;
        }
    }
    
    // Access the node->NodeId index.
    // Synchronized.
    // Given a node and a hash, return NodeId
    // Assumes a cache miss on node2id_Cache
    protected NodeId accessIndex(Node node, boolean create)
    {
        Hash hash = new Hash(nodeHashToId.getRecordFactory().keyLength()) ;
        setHash(hash, node) ;
        byte k[] = hash.getBytes() ;        
        // Key only.
        Record r = nodeHashToId.getRecordFactory().create(k) ;

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
        // r.valkue := id bytes ; 
        id.toBytes(r.getValue(), 0) ;

        // Put in index - may appear because of concurrency
        if ( ! nodeHashToId.add(r) )
            throw new TDBException("NodeTableBase::nodeToId - record mysteriously appeared") ;

        return id ;
    }
    
    // ---- Only places that the caches are touched
    
    /** Check caches to see if we can map a NodeId to a Node. Returns null on no cache entry. */ 
    protected Node cacheLookup(NodeId id)
    {
        if ( id2node_Cache == null ) return null ;
        return id2node_Cache.getObject(id) ;
    }
    
    /** Check caches to see if we can map a Node to a NodeId. Returns null on no cache entry. */ 
    protected NodeId cacheLookup(Node node)
    {
        // Remember things known (currently) not to exist 
        if ( notPresent.contains(node) ) return null ;
        if ( node2id_Cache == null ) return null ;
        return node2id_Cache.getObject(node) ; 
    }

    /** Update the Node->NodeId caches */
    protected void cacheUpdate(Node node, NodeId id)
    {
        // synchronized is further out.
        // The "notPresent" cache is used to note whether a node
        // is known not to exist.
        // This must be specially handled later if the node is added. 
        if ( id == NodeId.NodeDoesNotExist )
        {
            notPresent.add(node) ;
            return ;
        }
        
        if ( id == NodeId.NodeIdAny )
        {
            ALog.warn(this, "Attempt to cache NodeIdAny - ignored") ;
            return ;
        }
        
        if ( node2id_Cache != null )
            node2id_Cache.putObject(node, id) ;
        if ( id2node_Cache != null )
            id2node_Cache.putObject(id, node) ;
        if ( notPresent.contains(node) )
            notPresent.remove(node) ;
    }
    // ----
    
    // -------- NodeId<->Node
    // Assumes NodeId inlining and caching has been handled.
    // Assumes synchronized (the caches wil be updated consistently)
    // Only places for accessing the ObjectFile.
    
    protected final NodeId writeNodeToTable(Node node)
    {
        String s = encode(node) ;
        return objects.write(s) ;
    }
    

    protected final Node readNodeByNodeId(NodeId id)
    {
        String s = objects.read(id) ;
        Node n = decode(s) ;
        return n ;
    }
    // -------- NodeId<->Node

    @Override
    public synchronized void close()
    {
        if ( nodeHashToId != null )
            nodeHashToId.close() ;
        if ( objects != null )
            objects.close() ; 
    }

    @Override
    public synchronized void sync(boolean force)
    {
        if ( nodeHashToId != null )
            nodeHashToId.sync(force) ;
        if ( objects != null )
            objects.sync(force) ;
    }
}
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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