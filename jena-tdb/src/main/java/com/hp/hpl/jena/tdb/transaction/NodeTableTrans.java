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

package com.hp.hpl.jena.tdb.transaction;

import static org.apache.jena.atlas.logging.FmtLog.warn ;
import static org.apache.jena.atlas.logging.Log.warn ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.ByteBufferLib ;
import org.apache.jena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTableCache ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTableInline ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTableNative ;

public class NodeTableTrans implements NodeTable, TransactionLifecycle
{
    private static Logger log = LoggerFactory.getLogger(NodeTableTrans.class) ;
    // TODO flag to note is any work is needed on commit.
    private final NodeTable base ;
    private long allocOffset ;
    
    private NodeTable nodeTableJournal = null ;
    private static int CacheSize = 10000 ;      // [TxTDB:TODO] Make configurable 
    private boolean passthrough = false ;
    
    private Index nodeIndex ;
    private ObjectFile journalObjFile ;
    // Start of the journal file for this transaction.
    // Always zero currently but allows for future  
    private long journalObjFileStartOffset ; 
    private final String label ;
    private final Transaction txn ;     // Can be null (during recovery).
    
    public NodeTableTrans(Transaction txn, String label, NodeTable sub, Index nodeIndex, ObjectFile objFile)
    {
        this.txn = txn ;
        this.base = sub ;
        this.nodeIndex = nodeIndex ;
        this.journalObjFile = objFile ;
        // Clear bytes from an old run
        // (a crash while writing means the old transaction did not commit
        //  any bytes in the file are junk)
        // This is coupled to the fact the prepare phase does the actually data writing. 
        journalObjFile.truncate(0) ;
        this.label = label ; 
    }

    public void setPassthrough(boolean v)   { passthrough = v ; }
    public NodeTable getBaseNodeTable()     { return base ; }
    public NodeTable getJournalTable()      { return nodeTableJournal ; }
    public Transaction getTransaction()     { return txn ; }
    
    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        if ( passthrough ) return base.getAllocateNodeId(node) ;
        NodeId nodeId = getNodeIdForNode(node) ;
        if ( ! NodeId.isDoesNotExist(nodeId) )
            return nodeId ;
        // add to journal
        nodeId = allocate(node) ;
        return nodeId ;
    }
    
    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        if ( node == Node.ANY )
            return NodeId.NodeIdAny ;
        if ( passthrough ) return base.getNodeIdForNode(node) ;
        NodeId nodeId = nodeTableJournal.getNodeIdForNode(node) ;
        if ( ! NodeId.isDoesNotExist(nodeId) )
            return mapFromJournal(nodeId) ;
        nodeId = base.getNodeIdForNode(node) ;
        return nodeId ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        if ( NodeId.isAny(id) ) 
            return Node.ANY ;
        if ( passthrough ) return base.getNodeForNodeId(id) ;
        long x = id.getId() ;
        if ( x < allocOffset )
            return base.getNodeForNodeId(id) ;
        id = mapToJournal(id) ;
        Node node = nodeTableJournal.getNodeForNodeId(id) ;
        return node ;
    }

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



    /** Convert from a id to the id in the "journal" file */ 
    private NodeId mapToJournal(NodeId id)
    { 
        if ( passthrough )
           throw new TDBTransactionException("Not in an active transaction") ;
        if ( NodeId.isInline(id) )
            return id ; 
        return NodeId.create(id.getId()-allocOffset) ;
    }
    
    /** Convert from a id in other to an external id  */ 
    private NodeId mapFromJournal(NodeId id)
    { 
        if ( passthrough )
            throw new TDBTransactionException("Not in an active transaction") ;
        if ( NodeId.isInline(id) )
            return id ; 
        return NodeId.create(id.getId()+allocOffset) ; 
    }
    
    private NodeId allocate(Node node)
    {
        NodeId nodeId = nodeTableJournal.getAllocateNodeId(node) ;
        nodeId = mapFromJournal(nodeId) ;
        return nodeId ;
    }
    
    @Override
    public NodeId allocOffset()
    {
        if ( passthrough ) return base.allocOffset() ;
        // If we have done the append stage, this is invalid as the base may change under our feet
        // Would need to track base operations.
        NodeId x1 = nodeTableJournal.allocOffset() ;
        NodeId x2 = mapFromJournal(x1) ;
        return x2 ;
    }

    @Override
    public void begin(Transaction txn)
    {
        //debug("%s begin", txn.getLabel()) ;
        
        if ( this.txn.getTxnId() != txn.getTxnId() )
            throw new TDBException(String.format("Different transactions: %s %s", this.txn.getLabel(), txn.getLabel())) ;
        if ( passthrough )
            throw new TDBException("Already active") ;
        passthrough = false ;
        
        allocOffset = base.allocOffset().getId() ;
        // base node table empty e.g. first use.
        journalObjFileStartOffset = journalObjFile.length() ;
        // Because the data is written in prepare, the journal of object data is
        // always empty at the start of a transaction.
        if ( journalObjFileStartOffset != 0 )
            warn(log, "%s journalStartOffset not zero: %d/0x%02X",txn.getLabel(), journalObjFileStartOffset, journalObjFileStartOffset) ;
        allocOffset += journalObjFileStartOffset ;
        
        this.nodeTableJournal = new NodeTableNative(nodeIndex, journalObjFile) ;
        this.nodeTableJournal = NodeTableCache.create(nodeTableJournal, CacheSize, CacheSize, 100) ;
        // This class knows about non-mappable inline values.   mapToJournal(NodeId)/mapFromJournal. 
        this.nodeTableJournal = NodeTableInline.create(nodeTableJournal) ;
    }
    
    static public boolean APPEND_LOG = false ; 
    
    /** Copy from the journal file to the real file */
    /*package*/ void append()
    {
        Iterator<Pair<NodeId, Node>> iter = nodeTableJournal.all() ;
        Pair<NodeId, Node> firstPair = null ;
        Pair<NodeId, Node> lastPair = null ;
        
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> x = iter.next() ;
            
            if ( firstPair == null )
                firstPair = x ;
            lastPair = x ;
            
            NodeId nodeId = x.getLeft() ;
            Node node = x.getRight() ;
            debug("  append: %s -> %s", x, mapFromJournal(nodeId)) ;
            // This does the write.
            NodeId nodeId2 = base.getAllocateNodeId(node) ;
            if ( ! nodeId2.equals(mapFromJournal(nodeId)) )
                inconsistent(node, nodeId, nodeId2) ;
        }
    }
    
    private void inconsistent(Node node , NodeId nodeId , NodeId nodeId2 )
    {
        String msg = String.format("Different ids for %s: allocated: expected %s, got %s", node, mapFromJournal(nodeId), nodeId2) ;
        System.err.println() ;
        System.err.println() ;
        System.err.println(msg) ;
        dump() ;   
        System.err.println() ;
        throw new TDBException(msg) ;
    }
    
    // Debugging only
    private void dump()
    {
        System.err.println(">>>>>>>>>>") ;
        System.err.println("label = "+label) ;
        System.err.println("txn = "+txn) ;
        System.err.println("offset = "+allocOffset) ;
        System.err.println("journalStartOffset = "+journalObjFileStartOffset) ;
        System.err.println("journal = "+journalObjFile.getLabel()) ;
        if ( true )
            return ;
        
        System.err.println("nodeTableJournal >>>") ;
        Iterator<Pair<NodeId, Node>> iter = nodeTableJournal.all() ;
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> x = iter.next() ;
            NodeId nodeId = x.getLeft() ;
            Node node = x.getRight() ;
            NodeId mapped = mapFromJournal(nodeId) ;
            //debug("append: %s -> %s", x, mapFromJournal(nodeId)) ;
            // This does the write.
            NodeId nodeId2 = base.getAllocateNodeId(node) ;
            System.err.println(x + "  mapped=" + mapped + " getAlloc="+nodeId2) ;
        }
        
        System.err.println("journal >>>") ;
        Iterator<Pair<Long, ByteBuffer>> iter1 = this.journalObjFile.all() ;
        for ( ; iter1.hasNext() ; )
        {
            Pair<Long, ByteBuffer> p = iter1.next() ;
            System.err.println(p.getLeft()+" : "+p.getRight()) ;
            ByteBufferLib.print(System.err, p.getRight()) ;
        }
        
        System.err.println("nodeIndex >>>") ;
        Iterator<Record> iter2 = this.nodeIndex.iterator() ;
        for ( ; iter2.hasNext() ; )
        {
            Record r = iter2.next() ;
            System.err.println(r) ;
        }
        System.err.println("<<<<<<<<<<") ;
    }
    
    @Override
    public void commitPrepare(Transaction txn)
    {
        debug("commitPrepare") ;
        
        // The node table is append-only so it can be written during prepare.
        // The index isn't written (via the transaction journal) until enact.
        if ( nodeTableJournal == null )
            throw new TDBTransactionException(txn.getLabel()+": Not in a transaction for a commit to happen") ;
        writeNodeJournal() ;
        
        if ( journalObjFile != null && journalObjFile.length() != 0 )
        {
            long x = journalObjFile.length() ;
            throw new TDBTransactionException(txn.getLabel()+": journalObjFile not cleared ("+x+")") ;
        }
    }
    
    @Override
    public void commitEnact(Transaction txn)
    {
        debug("commitEnact") ;
        // The work was done in commitPrepare, using the fact that node data file
        // is append only.  Until here, pointers to the extra data aren't available
        // until the index is written.
        // The index is written via the transaction journal.
        //writeJournal() ;
    }

    private void writeNodeJournal()
    {
        long expected = base.allocOffset().getId() ;
        long len = journalObjFile.length() ;
        if ( expected != allocOffset )
            warn(log, "Inconsistency: base.allocOffset() = "+expected+" : allocOffset = "+allocOffset) ;
        
        long newbase = -1 ; 
        append() ;      // Calls all() which does a buffer flish.
        // Reset (in case we use this again)
        nodeIndex.clear() ;
        journalObjFile.truncate(journalObjFileStartOffset) ;    // Side effect is a buffer flush.
        //journalObjFile.sync() ;
        journalObjFile.close() ;                                // Side effect is a buffer flush.
        journalObjFile = null ;
        base.sync() ;
        allocOffset = -99 ; // base.allocOffset().getId() ; // Will be invalid as we may write through to the base table later.
        passthrough = true ;
    }

    @Override
    public void commitClearup(Transaction txn)
    {
        debug("commitClearup") ;
        finish() ;
    }

    @Override
    public void abort(Transaction txn)
    {
        debug("abort") ;
        if ( nodeTableJournal == null )
            throw new TDBTransactionException(txn.getLabel()+": Not in a transaction for a commit to happen") ;
        // Ensure the cache does not flush.
        nodeTableJournal = null ;
        // then make sure the journal file is empty.
        if ( journalObjFile != null )
        {
            journalObjFile.truncate(journalObjFileStartOffset) ;
            journalObjFile.sync() ;
        }
        finish() ;
    }
    
    private void finish()
    {
        close() ;
        passthrough = true ;
        nodeTableJournal = null ;
        journalObjFile = null ;
   }

    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        // Better would be to convert the spill file format.
        return Iter.concat(base.all(), nodeTableJournal.all()) ;
    }

    @Override
    public boolean isEmpty()
    {
        return nodeTableJournal.isEmpty() && base.isEmpty() ;
    }

    @Override
    public void sync()
    {
        if ( passthrough )
            base.sync() ;
    }

    @Override
    public void close()
    {
        if ( nodeIndex != null )
            nodeIndex.close() ;
        nodeIndex = null ;
        // Closing the journal flushes it; i.e. disk IO. 
        if ( journalObjFile != null )
            journalObjFile.close() ;
        journalObjFile = null ;
    }

    @Override
    public String toString() { return "NodeTableTrans:"+label+"(#"+Integer.toHexString(super.hashCode())+")" ; }
    
    private void debug(String fmt, Object... args)
    {
        if ( log.isDebugEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.debug(label+": "+x) ;
        }
    }
}
