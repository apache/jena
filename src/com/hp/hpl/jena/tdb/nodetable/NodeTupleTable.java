/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;
import static java.lang.String.format;
import iterator.NullIterator;

import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.Closeable;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.index.TupleTable;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.lib.TupleLib;
import com.hp.hpl.jena.tdb.store.NodeId;


/** Support code to group tuple table and node table */ 
public class NodeTupleTable implements Sync, Closeable
{
    protected final NodeTable nodeTable ;
    protected final TupleTable tupleTable ;
    
    public NodeTupleTable(int N, TupleIndex[] indexes, NodeTable nodeTable)
    {
        if ( indexes.length == 0 || indexes[0] == null )
            throw new TDBException("A primary index is required") ;
        for ( TupleIndex index : indexes )
        {
            if ( N != index.getTupleLength() )
                throw new TDBException(format("Inconsistent: TupleTable width is %d but index %s is %d",
                                              N, index.getLabel(), index.getTupleLength() )) ;   
        }
        
        this.tupleTable = new TupleTable(N, indexes) ;
        this.nodeTable = nodeTable ;
    }
    
    public boolean addRow(Node...nodes)
    {
        NodeId n[] = new NodeId[nodes.length] ;
        for ( int i = 0 ; i < nodes.length ; i++ )
            n[i] = storeNode(nodes[i]) ;
        
        Tuple<NodeId> t = Tuple.create(n) ;
        return tupleTable.add(t) ;
    }
    
    public boolean deleteRow(Node...nodes)
    {
        NodeId n[] = new NodeId[nodes.length] ;
        for ( int i = 0 ; i < nodes.length ; i++ )
        {
            NodeId id = idForNode(nodes[i]) ;
            if ( id == NodeId.NodeDoesNotExist )
                return false ;
            n[i] = id ;
        }
        
        Tuple<NodeId> t = Tuple.create(n) ;
        return tupleTable.delete(t) ;
    }
    
    /** Find by node. */
    public Iterator<Tuple<Node>> find(Node...nodes)
    {
        Iterator<Tuple<NodeId>> _iter = findAsNodeIds(nodes) ;
        if ( _iter == null )
            return new NullIterator<Tuple<Node>>() ;
        Iterator<Tuple<Node>> iter = TupleLib.convertToNodes(nodeTable, _iter) ;
        return iter ;
    }
    
    /** Find by node - returnan iterator of NodeIds. Can return "null" for not found as well as NullIterator" */
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node...nodes)
    {
        NodeId n[] = new NodeId[nodes.length] ;
        
        for ( int i = 0 ; i < nodes.length ; i++ )
        {
            NodeId id = idForNode(nodes[i]) ;
            if ( id == NodeId.NodeDoesNotExist )
                return null ;
            n[i] = id ;
        }

        return find(n) ;
    }
    
    
    /** Find by NodeId. */
    public Iterator<Tuple<NodeId>> find(NodeId...ids)
    {
        Tuple<NodeId> tuple = Tuple.create(ids) ;
        Iterator<Tuple<NodeId>> iter = tupleTable.find(tuple) ;
        return iter ;
    }

    
    // ==== Node

    protected final NodeId idForNode(Node node)
    {
        if ( node == null || node == Node.ANY )
            return NodeId.NodeIdAny ;
        return nodeTable.nodeIdForNode(node) ;
    }
    
    // Store node, return id.  Node may already be stored.
    //protected abstract int storeNode(Node node) ;
    
    protected final NodeId storeNode(Node node)
    {
        return nodeTable.storeNode(node) ;
    }
    
    protected final Node retrieveNode(NodeId id)
    {
        return nodeTable.retrieveNodeByNodeId(id) ;
    }
    
    // ==== Accessors
    
    /** Return the undelying tuple table - used with great care by tools
     * that directly manipulate internal structures. 
     */
    public final TupleTable getTupleTable() { return tupleTable ; }
    
    /** Return the node table */
    public final NodeTable getNodeTable()   { return nodeTable ; }
    
    public boolean isEmpty()        { return tupleTable.isEmpty() ; }
    
    public long size()              { return tupleTable.size() ; }
    
    @Override
    public final void close()
    {
        tupleTable.close() ;
        nodeTable.close() ;
    }
    
    @Override
    public final void sync(boolean force)
    {
        tupleTable.sync(force) ;
        nodeTable.sync(force) ;
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