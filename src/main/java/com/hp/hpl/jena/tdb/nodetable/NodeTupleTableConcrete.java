/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;
import static java.lang.String.format ;

import java.util.Iterator ;

import org.openjena.atlas.iterator.NullIterator ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleTable ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.lib.TupleLib ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Group a tuple table and node table together to provide a real NodeTupleTable */ 
public class NodeTupleTableConcrete implements NodeTupleTable
{
    protected final NodeTable nodeTable ;
    protected final TupleTable tupleTable ;
    
    public NodeTupleTableConcrete(int N, TupleIndex[] indexes, NodeTable nodeTable)
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
            n[i] = nodeTable.getAllocateNodeId(nodes[i]) ;

        Tuple<NodeId> t = Tuple.create(n) ;
        try {
            return tupleTable.add(t) ;
        } catch (TDBException ex)
        {
            String x = NodeLib.format(" ", nodes) ; 
            System.err.println("Bad add for tuple: "+x) ;
            throw ex ;
        }
    }

    public boolean deleteRow(Node...nodes)
    {
        NodeId n[] = new NodeId[nodes.length] ;
        for ( int i = 0 ; i < nodes.length ; i++ )
        {
            NodeId id = idForNode(nodes[i]) ;
            if ( NodeId.doesNotExist(id) )
                return false ;
            n[i] = id ;
        }
        
        Tuple<NodeId> t = Tuple.create(n) ;
        return tupleTable.delete(t) ;
    }
    
    /** Find by node. */
    public Iterator<Tuple<Node>> find(Node...nodes)
    {
        Iterator<Tuple<NodeId>> iter1 = findAsNodeIds(nodes) ;
        if ( iter1 == null )
            return new NullIterator<Tuple<Node>>() ;
        Iterator<Tuple<Node>> iter2 = TupleLib.convertToNodes(nodeTable, iter1) ;
        return iter2 ;
    }
    
    /** Find by node - return an iterator of NodeIds. 
     * Can return "null" (when a node is known to be unknown)
     * for not found as well as NullIterator (when no tuples are found
     * (unknown unknown).
     */ 
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node...nodes)
    {
        NodeId n[] = new NodeId[nodes.length] ;
        
        for ( int i = 0 ; i < nodes.length ; i++ )
        {
            NodeId id = idForNode(nodes[i]) ;
            if ( NodeId.doesNotExist(id) )
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
        if ( node.isVariable() )
            throw new TDBException("Can't pass variables to NodeTupleTable.find*") ; 
        return nodeTable.getNodeIdForNode(node) ;
    }
    
    // ==== Accessors
    
    /** Return the undelying tuple table - used with great care by tools
     * that directly manipulate internal structures. 
     */
    public final TupleTable getTupleTable() { return tupleTable ; }
    
    /** Return the node table */
    public final NodeTable getNodeTable()   { return nodeTable ; }
    
    public boolean isEmpty()        { return tupleTable.isEmpty() ; }
    
    public void clear()             { tupleTable.clear(); }
    
    public long size()              { return tupleTable.size() ; }
    
    //@Override
    public final void close()
    {
        tupleTable.close() ;
        nodeTable.close() ;
    }
    
    //@Override
    public final void sync() { sync(true) ; }
    
    //@Override
    public final void sync(boolean force)
    {
        tupleTable.sync(force) ;
        nodeTable.sync(force) ;
    }
   
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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