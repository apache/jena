/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import iterator.Filter;
import iterator.Iter;
import iterator.NullIterator;
import iterator.SingletonIterator;

import java.util.Iterator;

import lib.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.lib.TupleLib;

/** A Triple table.  There is no persistent table, it is all held in the
 *  indexes and the node table. 
 * 
 *  Machinary to implement a "nodes and triples" style graph,
 *  based on 3 indexes (SPO, POS, OSP)
 *  and a node table form can map to and from integers.
 */

public class TripleTable implements Sync, Closeable
{
    private static Logger log = LoggerFactory.getLogger(TripleTable.class) ;
    
    private TripleIndex indexSPO = null ;
    private TripleIndex indexPOS = null ;
    private TripleIndex indexOSP = null ;
    private NodeTable nodeTable = null ;

    private Location location ;
    
    protected TripleTable() {}
    
    public TripleTable(TripleIndex spo, TripleIndex pos, TripleIndex osp, 
                       NodeTable nodeTable, Location location)
    {
        if ( spo == null )
            throw new TDBException("SPO index is required") ;
        
        this.indexSPO = spo ;
        this.indexPOS = pos ;
        this.indexOSP = osp ;
        this.nodeTable = nodeTable ;
        this.location = location ;
    }
    
    /** Insert a triple - return true if it's a new triple, false if a duplicate */
    public boolean add( Triple t ) 
    { 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        NodeId sId = storeNode(s) ;
        NodeId pId = storeNode(p) ;
        NodeId oId = storeNode(o) ;
        
        if ( indexSPO != null )
        {
            if ( ! indexSPO.add(sId, pId, oId) )
            {
                duplicate(t, sId, pId, oId) ;
                return false ;
            }
        }
        
        if ( indexPOS != null )
        {
            if ( ! indexPOS.add(sId, pId, oId) )
                throw new TDBException("POS duplicate: "+t) ;
        }

        if ( indexOSP != null )
        {
            if ( ! indexOSP.add(sId, pId, oId) )
                throw new TDBException("OSP duplicate: "+t) ;
        }
        return true ;
    }

    private void duplicate(Triple t, NodeId id, NodeId id2, NodeId id3)
    { }

    /** Delete a triple  - return true if it was deleted, false if it didn't exist */
    public boolean delete( Triple t ) 
    { 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;

        NodeId sId = storeNode(s) ;
        NodeId pId = storeNode(p) ;
        NodeId oId = storeNode(o) ;
        
        if ( ! indexSPO.delete(sId, pId, oId) )
            return false ;
        if ( indexPOS != null )
        {
            if ( ! indexPOS.delete(sId, pId, oId) )
                throw new TDBException("No POS entry") ;
        }
        
        if ( indexOSP != null )
        {
            if ( ! indexOSP.delete(sId, pId, oId) )
                throw new TDBException("No OSP entry") ;
        }
        return true ;
    }

    // ==== Node
    
    protected String encode(Node node)
    {
        if ( node.isBlank() )
            return "_:"+node.getBlankNodeLabel() ;
        return FmtUtils.stringForNode(node) ;
    }

    protected Node decode(String s)
    {
        if ( s.startsWith("_:") )   
        {
            s = s.substring(2) ;
            return Node.createAnon(new AnonId(s)) ;
        }
        
        return SSE.parseNode(s) ;
    }

    private final NodeId idForNode(Node node)
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

    /** Find by node. */
    public Iterator<Triple> find(Node s, Node p, Node o)
    {
        NodeId subj = idForNode(s) ;
        if ( subj == NodeId.NodeDoesNotExist )
            return new NullIterator<Triple>() ;
        
        NodeId pred = idForNode(p) ;
        if ( pred == NodeId.NodeDoesNotExist )
            return new NullIterator<Triple>() ;
        
        NodeId obj = idForNode(o) ;
        if ( obj == NodeId.NodeDoesNotExist )
            return new NullIterator<Triple>() ;
        
        boolean s_set = ( subj != NodeId.NodeIdAny ) ;
        boolean p_set = ( pred != NodeId.NodeIdAny ) ;
        boolean o_set = ( obj  != NodeId.NodeIdAny ) ;

        if ( s_set && p_set && o_set )
        {
            // s p o
            if( indexSPO.find(subj, pred, obj).hasNext() )
                return new SingletonIterator<Triple>(new Triple(s,p,o)) ;
            else
                return new NullIterator<Triple>() ;
        }
        
        Iterator<Tuple<NodeId>> _iter = find(subj, pred, obj) ;
        Iterator<Triple> iter = TupleLib.convertToTriples(nodeTable, _iter) ;
        return iter ;
    }
    
    public Iterator<Tuple<NodeId>> find(NodeId subj, NodeId pred, NodeId obj)
    {
        if ( subj == NodeId.NodeIdAny ) subj = null ;
        if ( pred == NodeId.NodeIdAny ) pred = null ;
        if ( obj == NodeId.NodeIdAny ) obj = null ;
     
        int numSlots = 0 ;
        if ( subj != null ) numSlots++ ;
        if ( pred != null ) numSlots++ ;
        if ( obj  != null ) numSlots++ ;
        
        TripleIndex index = null ;
        int indexNumSlots = 0 ;
        
        if ( indexSPO != null )
        {
            int w = indexSPO.weight(subj, pred, obj) ;
            if ( w > indexNumSlots )
            {
                indexNumSlots = w ;
                index = indexSPO ; 
            }
        }
        
        if ( indexPOS != null )
        {
            int w = indexPOS.weight(subj, pred, obj) ;
            if ( w > indexNumSlots )
            {
                indexNumSlots = w ;
                index = indexPOS ;
            }
        }
            
        if ( indexOSP != null )
        {
            int w = indexOSP.weight(subj, pred, obj) ;
            if ( w > indexNumSlots )
            {
                indexNumSlots = w ;
                index = indexOSP ;
            }
        }
        
        Iterator<Tuple<NodeId>> iter = null ;
        
        if ( index == null )
            // No index at all.  Scan.
            iter = indexSPO.all() ;
        else 
            iter = index.find(subj, pred, obj) ;
        
        if ( indexNumSlots < numSlots )
            // Didn't match all defined slots in request.  
            // Partial or full scan needed.
            iter = scan(iter, subj, pred, obj) ;
        
        return iter ;
    }
    
    private Iterator<Tuple<NodeId>> scan(Iterator<Tuple<NodeId>> iter,
                                         final NodeId subj, final NodeId pred, final NodeId obj)
    {
        Filter<Tuple<NodeId>> filter = new Filter<Tuple<NodeId>>()
        {
            @Override
            public boolean accept(Tuple<NodeId> item)
            {
                if ( subj != null && ! (item.get(0).equals(subj)) )
                    return false ;
                if ( pred != null && ! (item.get(1).equals(pred)) )
                    return false ;
                if ( obj != null && ! (item.get(2).equals(obj)) )
                    return false ;
                return true ;
            }
        } ;
        
        return Iter.filter(iter, filter) ;
            
    }
    
    @Override
    final public void close()
    {
        if ( indexSPO != null )
            indexSPO.close();
        if ( indexPOS != null )
            indexPOS.close();
        if ( indexOSP != null )
            indexOSP.close();
        if ( nodeTable != null )
            nodeTable.close() ;
    }
    
    public void dumpIndexes()
    {
        if ( indexSPO == null && indexPOS == null && indexOSP == null )
        {
            System.out.println("No indexes") ;
            return ;
        }
        
        if ( indexSPO != null ) indexSPO.dump();
        if ( indexPOS != null ) indexPOS.dump();
        if ( indexOSP != null ) indexOSP.dump();
    }
    
    @Override
    public void sync(boolean force)
    {
        if ( indexSPO != null )
            indexSPO.sync(force);
        if ( indexPOS != null )
            indexPOS.sync(force);
        if ( indexOSP != null )
            indexOSP.sync(force);
        if ( nodeTable != null )
            nodeTable.sync(force) ;
    }

    /// Getters and setters - use with care,
    public Location getLocation()                   { return location ; }

    public TripleIndex getIndexSPO()                { return indexSPO ; }
    public TripleIndex getIndexPOS()                { return indexPOS ; }
    public TripleIndex getIndexOSP()                { return indexOSP ; }

    public void setIndexSPO(TripleIndex indexSPO)   { this.indexSPO = indexSPO ; }
    public void setIndexPOS(TripleIndex indexPOS)   { this.indexPOS = indexPOS ; }
    public void setIndexOSP(TripleIndex indexOSP)   { this.indexOSP = indexOSP ; }

    public NodeTable getNodeTable()                 { return nodeTable ; }
    public void setNodeTable(NodeTable nodeTable)   { this.nodeTable = nodeTable ; }
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