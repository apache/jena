/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.bdb;

import java.io.UnsupportedEncodingException;

import lib.Bytes;
import lib.CacheLRU;

import com.sleepycat.je.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeTable;
import com.hp.hpl.jena.tdb.pgraph.PGraphException;


public class NodeTableBDBDirect implements NodeTable
{
    Database nodeToId ;     // String -> int
    Database idToNode ;     // int -> String
    
    private Transaction txn = null ;
    private Sequence idNumbering ;
    private SetupBDB config ;
    
    public NodeTableBDBDirect(SetupBDB config)
    {
        this.config = config ;
        
        try {
            nodeToId = config.dbEnv.openDatabase(txn, "node2id", config.dbConfig);
            idToNode = config.dbEnv.openDatabase(txn, "id2Node", config.dbConfig); 
            SequenceConfig sequenceConfig = new SequenceConfig() ;
            sequenceConfig.setAllowCreate(true) ;
            idNumbering = idToNode.openSequence(txn, entryNode("seq"), sequenceConfig) ;
        } catch (DatabaseException ex)
        {
            throw new PGraphException(ex) ;
        }
    }
    
    private NodeId storeNodeOrNull(Node node)
    {
        if ( node == null || node == Node.ANY )
            return NodeId.NodeIdAny ;
        return storeNode(node) ;
    }
    
    private static DatabaseEntry entryNode(String str)
    {
        try
        {
            byte[] s = str.getBytes("UTF-8") ;
            DatabaseEntry k = new DatabaseEntry(s);
            return k ;
        } catch (UnsupportedEncodingException ex)
        {
            throw new PGraphException("Should not happen") ;
        }
    }

    CacheLRU<Node, NodeId> nodeCache = null ; // new CacheLRU<Node, NodeId>(1000) ;
    
    @Override
    public NodeId storeNode(Node node)
    {
        return accessIndex(node, true) ;
    }
    
    
    private NodeId accessIndex(Node node, boolean create)
    {
        try {
            if ( nodeCache != null )
            {
                NodeId id = nodeCache.get(node) ;
                if ( id != null )
                    return id ; 
            }
            DatabaseEntry nodeEntry = entryNode(NodeLib.encode(node, null)) ;
            DatabaseEntry idEntry = new DatabaseEntry() ;
            OperationStatus status = nodeToId.get(txn, nodeEntry, idEntry, config.lockMode) ;

            if ( status == OperationStatus.SUCCESS )
            {
                long x = Bytes.getLong(idEntry.getData()) ;
                return NodeId.create(x) ;
            }
            if ( ! create )
                return NodeId.NodeDoesNotExist ;
            
            long x = idNumbering.get(txn, 1) ;
            idEntry = new DatabaseEntry(longBuff(x));

            nodeToId.put(txn, nodeEntry, idEntry) ;
            idToNode.put(txn, idEntry, nodeEntry) ;

            NodeId nodeId = NodeId.create(x) ;
            if ( nodeCache != null )
                nodeCache.put(node, nodeId) ; 
            return nodeId ;

        } catch (DatabaseException dbe) {
            throw new PGraphException("GraphBDB.storeNode", dbe) ;
        } 
    }

    @Override
    public Node retrieveNode(NodeId id)
    {
        try {
            DatabaseEntry k = new DatabaseEntry(longBuff(id.getId())) ;
            DatabaseEntry v = new DatabaseEntry() ;

            OperationStatus status = idToNode.get(txn, k, v, config.lockMode) ;

            if ( status == OperationStatus.NOTFOUND )
                return null ;
            String s = new String(v.getData(), v.getOffset(), v.getSize(), "UTF-8") ;
            Node n = NodeLib.decode(s, null) ;
            return n ;
        } catch (Exception ex)
        { throw new PGraphException("GraphBDB.retrieveNode", ex) ; }
    }


    private static byte[] longBuff(long id)
    {
        byte[] b = new byte[Const.SizeOfLong] ;
        Bytes.setLong(id, b) ;
        return b ;
    }

    @Override
    public NodeId idForNode(Node node)
    {
        return accessIndex(node, false) ;
    }

    @Override
    public void close()
    {
        try {
            nodeToId.close();
            idToNode.close();
        } catch (DatabaseException dbe) {
            throw new PGraphException(dbe) ;
        } 
    }

    @Override
    public void sync(boolean force)
    {
     // BDB sync is only for deferred write (in-memory) DBs.
//        try {
//            nodeToId.sync();
//            idToNode.sync();
//        } catch (DatabaseException dbe) {
//            throw new PGraphException(dbe) ;
//        } 
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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