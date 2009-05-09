/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Iterator;

import atlas.lib.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.graph.GraphSyncListener;
import com.hp.hpl.jena.tdb.graph.UpdateListener;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** A graph implementation that projects a graph from a quad table */
public class GraphNamedTDB extends GraphTDBBase
{
    private static Logger log = LoggerFactory.getLogger(GraphNamedTDB.class) ;
    
    private final QuadTable quadTable ; 
    private NodeId graphNodeId = null ;

    private final ReorderTransformation transform ;

    public GraphNamedTDB(DatasetGraphTDB dataset, Node graphName, ReorderTransformation transform) 
    {
        super(dataset, graphName, transform, dataset.getLocation()) ;
        this.quadTable = dataset.getQuadTable() ;
        this.transform = transform ;
        
        if ( graphName == null )
            throw new TDBException("GraphNamedTDB: Null graph name") ; 
        if ( ! graphName.isURI() )
            throw new TDBException("GraphNamedTDB: Graph name not a URI") ; 
        
        int syncPoint = SystemTDB.SyncTick ;
        if ( syncPoint > 0 )
            this.getEventManager().register(new GraphSyncListener(this, syncPoint)) ;
        this.getEventManager().register(new UpdateListener(this)) ;
    }

//    @Override
//    public QueryHandler queryHandler()
//    { return queryHandler ; }
//    
//    @Override
//    public TransactionHandler getTransactionHandler()
//    { return transactionHandler ; }
    
    @Override
    protected PrefixMapping createPrefixMapping()
    {
        return dataset.getPrefixes().getPrefixMapping(graphNode.getURI()) ;
    }

    @Override
    public void performAdd( Triple t ) 
    { 
        boolean changed = dataset.getQuadTable().add(graphNode, t) ;
        if ( ! changed )
            duplicate(t) ;
    }

    private void duplicate(Triple t)
    {
        if ( TDB.getContext().isTrue(SystemTDB.symLogDuplicates) && log.isInfoEnabled() )
        {
            String $ = FmtUtils.stringForTriple(t, this.getPrefixMapping()) ;
            log.info("Duplicate: ("+$+")") ;
        }
    }

    @Override
    public void performDelete( Triple t ) 
    { 
        boolean changed = dataset.getQuadTable().delete(graphNode, t) ;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        Iterator<Quad> iter = dataset.getQuadTable().find(graphNode, m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iter == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;
        
        return new MapperIteratorQuads(graphNode, iter) ;
    }

    @Override
    protected Iterator<Tuple<NodeId>> countThis()
    {
        NodeId gn = getGraphNodeId() ;
        Tuple<NodeId> t = Tuple.create(gn, null, null, null) ;
        
        Iterator<Tuple<NodeId>> iter = dataset.getQuadTable().getNodeTupleTable().getTupleTable().find(t) ;
        return iter ;
    }
    
    /** Graph node as NodeId */
    public final NodeId getGraphNodeId()
    {
        if ( graphNodeId == null )
            graphNodeId = dataset.getQuadTable().getNodeTupleTable().getNodeTable().getNodeIdForNode(graphNode) ;
        return graphNodeId ;
    }

    @Override
    public Tuple<Node> asTuple(Triple triple)
    {
//        if ( getGraphNode() == null )
//            return Tuple.create(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
//        else
            return Tuple.create(getGraphNode(), triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    @Override
    public NodeTupleTable getNodeTupleTable()
    {
        // Concrete default graph.
        if ( graphNode == null || Quad.isDefaultGraph(graphNode) )
            return dataset.getDefaultTripleTableTable().getNodeTupleTable() ;
        return dataset.getQuadTable().getNodeTupleTable() ;
    }

    @Override
    final public void close()
    {
        dataset.close();
        super.close() ;
    }
    
    @Override
    public void sync(boolean force)
    {
        dataset.getPrefixes().sync(force) ;
        dataset.sync(force);
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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