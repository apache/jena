/**
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

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableCache ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableNative ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class NodeTableTrans implements NodeTable, Transactional
{
    private static Logger log = LoggerFactory.getLogger(NodeTableTrans.class) ;
    // TODO flag to note is any work is needed on commit.
    private final NodeTable base ;
    private long offset ;
    
    private NodeTable nodeTableJournal = null ;
    private static int CacheSize = 10000 ;      // [TxTDB:TODO] Make configurable 
    private boolean passthrough = false ;
    
    private final Index nodeIndex ;
    private ObjectFile journal ;
    // Start of the journal file for this transaction.
    // Always zero currently but allows for future  
    private long journalStartOffset ; 
    private final String label ;
    
    public NodeTableTrans(String label, NodeTable sub, Index nodeIndex, ObjectFile journal)
    {
        this.base = sub ;
        this.nodeIndex = nodeIndex ;
        this.journal = journal ;
        this.label = label ; 
    }

    public void setPassthrough(boolean v)   { passthrough = v ; }
    public NodeTable getBaseNodeTable()     { return base ; }
    public NodeTable getJournalTable()      { return nodeTableJournal ; }
    
    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        if ( passthrough ) return base.getAllocateNodeId(node) ;
        NodeId nodeId = getNodeIdForNode(node) ;
        if ( ! NodeId.doesNotExist(nodeId) )
            return nodeId ;
        // add to journal
        nodeId = allocate(node) ;
        return nodeId ;
    }
    
    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        if ( passthrough ) return base.getNodeIdForNode(node) ;
        NodeId nodeId = nodeTableJournal.getNodeIdForNode(node) ;
        if ( ! NodeId.doesNotExist(nodeId) )
            return mapFromJournal(nodeId) ;
        nodeId = base.getNodeIdForNode(node) ;
        return nodeId ;
    }

    /** Convert from a id to the id in the "journal" file */ 
    private NodeId mapToJournal(NodeId id) { return NodeId.create(id.getId()-offset) ; }
    
    /** Convert from a id in other to an external id  */ 
    private NodeId mapFromJournal(NodeId id) { return NodeId.create(id.getId()+offset) ; }
    
    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        if ( passthrough ) return base.getNodeForNodeId(id) ;
        long x = id.getId() ;
        if ( x < offset )
            return base.getNodeForNodeId(id) ;
        id = mapToJournal(id) ;
        Node node = nodeTableJournal.getNodeForNodeId(id) ;
        return node ;
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
        NodeId x = nodeTableJournal.allocOffset() ;
        return mapFromJournal(x) ;
    }

    @Override
    public void begin(Transaction txn)
    {
        passthrough = false ;
        
        offset = base.allocOffset().getId() ;
        // Any outstanding transactions
        long journalOffset = journal.length() ;
        debug("begin: %s", label) ;
        offset += journalOffset ;
        this.nodeTableJournal = new NodeTableNative(nodeIndex, journal) ;
        this.nodeTableJournal = NodeTableCache.create(nodeTableJournal, CacheSize, CacheSize) ;
        // Do not add the inline NodeTable here - don't convert it's values by the offset!  
    }
    
    /** Copy from the journal file to the real file */
    public /*temporary*/ void append()
    {
        // Assumes all() is in order from low to high.
        Iterator<Pair<NodeId, Node>> iter = nodeTableJournal.all() ;
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> x = iter.next() ;
            NodeId nodeId = x.getLeft() ;
            Node node = x.getRight() ;
            NodeId nodeId2 = base.getAllocateNodeId(node) ;
            if ( ! nodeId2.equals(mapFromJournal(nodeId)) )
                throw new TDBException(String.format("Different ids allocated: expected %s, got %s\n", mapFromJournal(nodeId), nodeId2)) ; 
        }
    }
    
    @Override
    public void commitPrepare(Transaction txn)
    {
        debug("commitPrepare: %s", label) ;
        // The node table is append-only so it can be written during prepare.
        // It does not need to wait for "enact".
        if ( nodeTableJournal == null )
            throw new TDBTransactionException("Not in a transaction for a commit to happen") ;
        writeNodeJournal() ;
    }
    
    @Override
    public void commitEnact(Transaction txn)
    {
        // The work was done in commitPrepare, using the fact that node data file
        // is append only.  Until pointers to the extra data aren't available
        // until the index is written.
        debug("commitEnact: %s", label) ;
        //writeJournal() ;
    }

    private void writeNodeJournal()
    {
        append() ;
        nodeIndex.clear() ;
        journal.truncate(journalStartOffset) ;
        journal.sync() ;
        base.sync() ;
        offset = base.allocOffset().getId() ;
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
        if ( nodeTableJournal == null )
            throw new TDBTransactionException("Not in a transaction for a commit to happen") ;
        // Ensure the cache does not flush.
        nodeTableJournal = null ;
        // then make sure the journal file is empty.
        journal.truncate(journalStartOffset) ;
        finish() ;
    }
    
    private void finish()
    {
        passthrough = true ;
        nodeTableJournal = null ;
        close() ;
   }

    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        // Better would be to convert the spill file format.
        return Iter.concat(base.all(), nodeTableJournal.all()) ;
    }

    @Override
    public void sync()
    {}

    @Override
    public void close()
    {
        // Closing the journal flushes it; i.e. disk IO. 
        if ( journal != null )
            journal.close() ;
        journal = null ;
    }

    @Override
    public String toString() { return "NodeTableTrans:"+label ; }
    
    private void debug(String fmt, Object... args)
    {
        if ( log.isDebugEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.debug(x) ;
        }
    }
}

