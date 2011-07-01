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
    private final NodeTable base ;
    private long offset ;
    
    private NodeTable nodeTableJournal ;
    private static int CacheSize = 10000 ;
    private boolean passthrough = false ;
    private boolean inTransaction = false ;
    
    private final Index nodeIndex ;
    private final ObjectFile journal ;
    
    public NodeTableTrans(NodeTable sub, Index nodeIndex, ObjectFile journal)
    {
        this.base = sub ;

        this.nodeIndex = nodeIndex ;
        this.journal = journal ;
        this.nodeTableJournal = null ;
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
        inTransaction = true ;
        offset = (int)base.allocOffset().getId() ;
        // Fast-ish clearing of the file.
        nodeIndex.clear() ;
        journal.reposition(0) ;
        
        this.nodeTableJournal = new NodeTableNative(nodeIndex, journal) ;
        this.nodeTableJournal = NodeTableCache.create(nodeTableJournal, CacheSize, CacheSize) ;
        // Do not add the inline NodeTable here - don't convert it's values by the offset!  

        
        // Setup.
//        journal.position(0) ;
//        this.otherAllocOffset = journal.length() ;
    }
    
    /** Copy from the journal file to the real file */
    public /*temporary*/ void append()
    {
        // Asummes all() is in order from low to high.
        Iterator<Pair<NodeId, Node>> iter = nodeTableJournal.all() ;
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> x = iter.next() ;
            NodeId nodeId = x.getLeft() ;
            Node node = x.getRight() ;
            NodeId nodeId2 = base.getAllocateNodeId(node) ;
            if ( ! nodeId2.equals(mapFromJournal(nodeId)) )
                throw new TDBException(String.format("Different ids allocated: expected %s, got %s\n", nodeId, nodeId2)) ; 
        }
    }
    
    @Override
    public void commit(Transaction txn)
    {
        if ( ! inTransaction )
            throw new TDBTransactionException("Not in a transaction for a commit to happen") ; 
        append() ;
        base.sync() ;
        //other.reposition(0) ;
        passthrough = true ;
    }

    @Override
    public void abort(Transaction txn)
    {
//        other.reposition(0) ;
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
    {}

}

