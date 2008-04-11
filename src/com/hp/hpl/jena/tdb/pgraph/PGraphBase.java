/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import static com.hp.hpl.jena.tdb.pgraph.PGraphFactory.indexRecordFactory;
import static com.hp.hpl.jena.tdb.pgraph.PGraphFactory.nodeRecordFactory;
import iterator.Filter;
import iterator.Iter;

import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.graph.GraphSyncListener;
import com.hp.hpl.jena.tdb.index.IndexFactory;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.lib.TupleLib;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/** Machinary to implement a "nodes and triples" style graph, based on 3 indexes
 * (SPO, POS, OSP) and a node table form can map to and from ints. 
 */

public abstract class PGraphBase extends GraphBase implements Sync
{
    // Better to have an array?
    private IndexFactory indexFactory = null ;
    private TripleIndex indexSPO = null ;
    private TripleIndex indexPOS = null ;
    private TripleIndex indexOSP = null ;
    private NodeTable nodeTable = null ;
    
    private final PGraphQueryHandler queryHandler = new PGraphQueryHandler(this) ;

    protected PGraphBase() {}
    
    // Two ways to initialize the indexes: via anm indexfcatory or directly with TripleIndexes.
    
    protected void init(IndexFactory factory, NodeTable nodeTable)
    {
        this.indexFactory = factory ;

        RangeIndex idxSPO = factory.createRangeIndex(indexRecordFactory, "SPO") ;
        TripleIndex triplesSPO = new TripleIndex("SPO", idxSPO) ;

        RangeIndex idxPOS = factory.createRangeIndex(indexRecordFactory, "POS") ;
        TripleIndex triplesPOS = new TripleIndex("POS", idxPOS) ;

        RangeIndex idxOSP = factory.createRangeIndex(indexRecordFactory, "OSP") ;
        TripleIndex triplesOSP = new TripleIndex("OSP", idxOSP) ;
        
        init(triplesSPO, triplesPOS, triplesOSP, nodeTable) ;
    }
    
    
    protected void init(TripleIndex spo, TripleIndex pos, TripleIndex osp, NodeTable nodeTable)
    {
        if ( spo == null )
            throw new TDBException("SPO index is required") ;
        
        this.indexSPO = spo ;
        this.indexPOS = pos ;
        this.indexOSP = osp ;
        this.nodeTable = nodeTable ;
        
        int syncPoint = Const.SyncTick ;
        if ( syncPoint > 0 )
            this.getEventManager().register(new GraphSyncListener(this, syncPoint)) ;
    }

    @Override
    public QueryHandler queryHandler()
    { 
        return queryHandler ;
    }
    
    
    public NodeTable getNodeTable() {  return nodeTable ; } 
    
  
    @Override
    public void performAdd( Triple t ) 
    { 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        NodeId sId = storeNode(s) ;
        NodeId pId = storeNode(p) ;
        NodeId oId = storeNode(o) ;
        
        if ( ! indexSPO.add(sId, pId, oId) )
            return ;

        if ( indexPOS != null )
        {
            if ( ! indexPOS.add(sId, pId, oId) )
                throw new PGraphException("POS duplicate: "+t) ;
        }

        if ( indexOSP != null )
        {
            if ( ! indexOSP.add(sId, pId, oId) )
                throw new PGraphException("OSP duplicate: "+t) ;
        }
    }

    @Override
    public void performDelete( Triple t ) 
    { 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;

        NodeId sId = storeNode(s) ;
        NodeId pId = storeNode(p) ;
        NodeId oId = storeNode(o) ;

        if ( ! indexSPO.delete(sId, pId, oId) )
            return ;
        if ( indexPOS != null )
        {
            if ( ! indexPOS.delete(sId, pId, oId) )
                throw new PGraphException("No POS entry") ;
        }
        
        if ( indexOSP != null )
        {
            if ( ! indexOSP.delete(sId, pId, oId) )
                throw new PGraphException("No OSP entry") ;
        }
    }
    
//    // Make sure that the stage generator isn't installed.
//    @Override
//    public QueryHandler queryHandler()
//    { return new GraphQueryHandlerTDB(this) ; }
    
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
        return nodeTable.idForNode(node) ;
    }
    
    // Store node, return id.  Node may already be stored.
    //protected abstract int storeNode(Node node) ;
    
    protected final NodeId storeNode(Node node)
    {
        return nodeTable.storeNode(node) ;
    }
    
    protected final Node retrieveNode(NodeId id)
    {
        return nodeTable.retrieveNode(id) ;
    }

    // ==== Triple indexes
    
    @Override
    protected ExtendedIterator graphBaseFind(TripleMatch m)
    {
        Node s = m.getMatchSubject() ;
        Node p = m.getMatchPredicate() ;
        Node o = m.getMatchObject() ;
        
        NodeId subj = idForNode(s) ;
        if ( subj == NodeId.NodeDoesNotExist )
            return new com.hp.hpl.jena.util.iterator.NullIterator() ;
        
        NodeId pred = idForNode(p) ;
        if ( pred == NodeId.NodeDoesNotExist )
            return new com.hp.hpl.jena.util.iterator.NullIterator() ;
        
        NodeId obj = idForNode(o) ;
        if ( obj == NodeId.NodeDoesNotExist )
            return new com.hp.hpl.jena.util.iterator.NullIterator() ;
        
//        if ( subj < 0 && subj != NodeTable.NodeNotConcrete )
//            throw new JenaException("Subject error") ;
//        if ( pred < 0 && pred != NodeTable.NodeNotConcrete )
//            throw new JenaException("Predicate error") ;
//        if ( obj < 0 && obj != NodeTable.NodeNotConcrete )
//            throw new JenaException("Object error") ;

        boolean s_set = ( subj != NodeId.NodeIdAny ) ;
        boolean p_set = ( pred != NodeId.NodeIdAny ) ;
        boolean o_set = ( obj  != NodeId.NodeIdAny ) ;

        if ( s_set && p_set && o_set )
        {
            // s p o
            if( indexSPO.find(subj, pred, obj).hasNext() )
                return new com.hp.hpl.jena.util.iterator.SingletonIterator(new Triple(s,p,o)) ;
            else
                return new com.hp.hpl.jena.util.iterator.NullIterator() ;
        }
        
        Iterator<Tuple<NodeId>> _iter = find(subj, pred, obj) ;
        Iterator<Triple> iter = TupleLib.convertToTriples(nodeTable, _iter) ;
        return new MapperIterator(iter) ;
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
            if ( w > 0 )
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
            // No index for a 
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
    


    static class MapperIterator extends NiceIterator
    {
        Iterator<Triple> iter ;
        MapperIterator(Iterator<Triple> iter)
        {
            this.iter = iter ;
        }
        
        @Override
        public boolean hasNext() { return iter.hasNext() ; } 
        
        @Override
        public Triple next() { return iter.next(); }
    }
    
    final
    protected Iterator<Triple> all(TripleIndex index)
    {
        return TupleLib.convertToTriples(nodeTable, index.all()) ;
    }
    
    // Include more here!
    
    @Override
    public Capabilities getCapabilities()
    {
        if ( capabilities == null )
            capabilities = new Capabilities(){
                public boolean sizeAccurate() { return true; }
                public boolean addAllowed() { return true ; }
                public boolean addAllowed( boolean every ) { return true; } 
                public boolean deleteAllowed() { return true ; }
                public boolean deleteAllowed( boolean every ) { return true; } 
                public boolean canBeEmpty() { return true; }
                public boolean iteratorRemoveAllowed() { return false; } /* ** */
                public boolean findContractSafe() { return false; }
                public boolean handlesLiteralTyping() { return false; } /* ** */
            } ; 
        
        return super.getCapabilities() ;
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
        super.close() ;
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

    protected final Triple triple(NodeId s, NodeId p, NodeId o) 
    {
        Node sNode = retrieveNode(s) ;
        Node pNode = retrieveNode(p) ;
        Node oNode = retrieveNode(o) ;
        return new Triple(sNode, pNode, oNode) ;
    }
    
    protected final Triple triple(Tuple<NodeId> tuple) 
    {
        return triple(tuple.get(0), tuple.get(1), tuple.get(2)) ;
    }

    // Getters and setters for most things - USE WITH CARE
    // PLaced here so detailed manipulatation code can be extenral to this class. 
    
    public IndexFactory getIndexFactory()
    {
        return indexFactory ;
    }

    public void setIndexFactory(IndexFactory indexFactory)
    {
        this.indexFactory = indexFactory ;
    }

    public TripleIndex getIndexSPO()
    {
        return indexSPO ;
    }

//    public void setIndexSPO(TripleIndex indexSPO)
//    {
//        this.indexSPO = indexSPO ;
//    }

    public TripleIndex getIndexPOS()
    {
        return indexPOS ;
    }

    public void setIndexPOS(TripleIndex indexPOS)
    {
        this.indexPOS = indexPOS ;
    }

    public TripleIndex getIndexOSP()
    {
        return indexOSP ;
    }

    public void setIndexOSP(TripleIndex indexOSP)
    {
        this.indexOSP = indexOSP ;
    }

    public RecordFactory getIndexRecordFactory()
    {
        return indexRecordFactory ;
    }

//    public static void setIndexRecordFactory(RecordFactory indexRecordFactory)
//    {
//        PGraphBase.indexRecordFactory = indexRecordFactory ;
//    }

    public RecordFactory getNodeRecordFactory()
    {
        return nodeRecordFactory ;
    }

//    public static void setNodeRecordFactory(RecordFactory nodeRecordFactory)
//    {
//        PGraphBase.nodeRecordFactory = nodeRecordFactory ;
//    }

    public NodeTable getNodeTable(NodeTable nodeTable)
    {
        return nodeTable ;
    }

//    public void setNodeTable(NodeTable nodeTable)
//    {
//        this.nodeTable = nodeTable ;
//    }
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