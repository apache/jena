/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.bdb;

import static com.hp.hpl.jena.tdb.Const.NodeCacheSize;
import lib.Bytes;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.base.file.ObjectFile;
import com.hp.hpl.jena.tdb.pgraph.*;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/** Rather than use the Index wrapper, we directly sublcass to provide the index capability for a NodeTable */
public class NodeTableBDB extends NodeTableBase
{
    SetupBDB config ;
    Database nodeHashToId ;
    Transaction txn = null ;
    
    public NodeTableBDB(SetupBDB config, Database nodeHashToId, ObjectFile objectFile)
    {
        super(null, objectFile, NodeCacheSize, NodeCacheSize) ;
        this.config = config ;
        this.nodeHashToId = nodeHashToId ;
    }
    
    @Override
    protected NodeId accessIndex(Node node, long hash, boolean create)
    {
        try {
            byte k[] = new byte[Const.NodeKeyLength] ;
            Bytes.setLong(hash, k, 0) ;
            DatabaseEntry entry = new DatabaseEntry(k) ;
            DatabaseEntry idEntry = new DatabaseEntry() ;
            OperationStatus status = nodeHashToId.get(txn, entry, idEntry, config.lockMode) ;
            if ( status == OperationStatus.SUCCESS )
                return NodeId.create(Bytes.getLong(idEntry.getData())) ;
            NodeId x = writeNode(node) ;
            idEntry = nodeIdEntry(x);

            status = nodeHashToId.put(txn, entry, idEntry) ;
            if ( status != OperationStatus.SUCCESS )
                throw new PGraphException("NodeTableBDB.accessIndex: failed to update index: "+status.toString()) ;
            return x ;
        } catch (DatabaseException dbe) {
            throw new PGraphException("GraphBDB.storeNode", dbe) ;
        }
    }

    private static DatabaseEntry nodeIdEntry(NodeId id)
    {
        byte[] b = new byte[NodeId.SIZE] ;
        Bytes.setLong(id.getId(), b) ;
        return new DatabaseEntry(b) ;
    }
    
    @Override
    public void close()
    {
        try { nodeHashToId.close() ; }
        catch (DatabaseException ex) { throw new PGraphException(ex) ; }
        super.close() ;
    }

    @Override
    public void sync(boolean force)
    {
        // BDB sync is only for deferred write (in-memory) DBs.
//        try { nodeHashToId.sync() ; }
//        catch (DatabaseException ex) { throw new PGraphException(ex) ; }
        super.sync(force) ;
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