/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import static com.hp.hpl.jena.tdb.lib.NodeLib.decode;
import static com.hp.hpl.jena.tdb.lib.NodeLib.encode;
import lib.Bytes;
import lib.CacheLRU;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.base.file.ObjectFile;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.lib.NodeLib;

public abstract class NodeTableBase implements NodeTable
{
    // Assumes an ObjectFile and an Indexer, which may be an Index but allows
    // this to be overriden for a direct use of BDB.

    public ObjectFile objects ;
    private Index nodeHashToId ;        // hash -> int
    
    // Currently, these caches are updated together.
    // Good for small scale mix and match of update and query
    private CacheLRU<Node, NodeId> node2id_Cache ;
    private CacheLRU<NodeId, Node> id2node_Cache ;

    // Delayed construction - must call init.
    protected NodeTableBase() {}
    
    protected void init(Index nodeToId, ObjectFile objectFile,
                        int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        this.nodeHashToId = nodeToId ;
        this.objects = objectFile ;
        node2id_Cache = 
            (nodeToIdCacheSize <= 0) ? null : new CacheLRU<Node, NodeId>(nodeToIdCacheSize) ;
        id2node_Cache = 
            (idToNodeCacheSize <= 0) ? null : new CacheLRU<NodeId, Node>(idToNodeCacheSize) ;
    }
    
    // Combined into one constructor.
    protected NodeTableBase(Index nodeToId, ObjectFile objectFile, int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        this() ;
        init(nodeToId, objectFile, idToNodeCacheSize, idToNodeCacheSize) ;
    }
    
    @Override
    public NodeId idForNode(Node node)
    {
        if ( node2id_Cache != null )
        {
            NodeId id = node2id_Cache.get(node) ;
            if ( id != null )
                return id ; 
        }
        long hash = NodeLib.hash(node) ;
        return accessIndex(node, hash, false) ;
    }

    @Override
    public NodeId storeNode(Node node)
    {
        if ( node2id_Cache != null )
        {
            NodeId id = node2id_Cache.get(node) ;
            if ( id != null )
                return id ; 
        }
        // Not in cache.  Try the index.
        long hash = NodeLib.hash(node) ;
        return accessIndex(node, hash, true) ;    // Allow override.
    }

    @Override
    public Node retrieveNode(NodeId id)
    {
        if ( id2node_Cache != null )
        {
            Node n = id2node_Cache.get(id) ;
            if ( n != null )
                return n ; 
        }
        String s = objects.read(id) ;
        Node n = decode(s) ;
        
        // Update caches
        if ( node2id_Cache != null )
            node2id_Cache.put(n, id) ;
        if ( id2node_Cache != null )
            id2node_Cache.put(id, n) ;
        return n ;
    }

    // Access the node->NodeId index.  
    // Given a node and a hash, return NodeId
    // Assumes a cache miss on node2id_Cache
    protected NodeId accessIndex(Node node, long hash, boolean create)
    {
        byte k[] = new byte[Const.NodeKeyLength] ;
        Bytes.setLong(hash, k, 0) ;
        // Key only.
        Record r = nodeHashToId.getRecordFactory().create(k) ;

        // Key and value, or null
        Record r2 = nodeHashToId.find(r) ;
        if ( r2 == null )
        {
            // Not found.
            if ( ! create )
                return NodeId.NodeDoesNotExist ;
           
            // Write the node, which allocates an id for it.
            NodeId id = writeNode(node) ;
            
            // Update the r record with the new id.
            id.toBytes(r.getValue(), 0) ;
            
            // Put in index
            if ( ! nodeHashToId.add(r) )
                throw new PGraphException("NodeTableBase::nodeToId - record mysteriously appeared") ;

            updateCaches(node, id) ;
            return id ;
        }
        
        // Found in the nodeHashToId index.  Ensure caches have it.
        NodeId id = NodeId.create(r2.getValue(), 0) ;
        updateCaches(node, id) ;
        return id ;
    }
    
    private void updateCaches(Node node, NodeId id)
    {
        if ( node2id_Cache != null )
            node2id_Cache.put(node, id) ;
        if ( id2node_Cache != null )
            id2node_Cache.put(id, node) ;
    }
    
    protected final NodeId writeNode(Node node)
    {
        String s = encode(node) ;
        return objects.write(s) ;
    }
    

    @Override
    public void close()
    {
        if ( nodeHashToId != null )
            nodeHashToId.close() ;
        if ( objects != null )
            objects.close() ; 
    }

    @Override
    public void sync(boolean force)
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