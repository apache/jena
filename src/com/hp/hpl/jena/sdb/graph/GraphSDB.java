/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.AllCapabilities;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.mem.TrackingTripleIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import com.hp.hpl.jena.query.Query;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;
import com.hp.hpl.jena.sdb.store.StoreLoader;
import com.hp.hpl.jena.sdb.store.StoreLoaderPlus;



public class GraphSDB extends GraphBase implements Graph
{
    private static Log log = LogFactory.getLog(GraphSDB.class) ;

    protected PrefixMapping pmap = null ;
    protected Store store = null ;
    
    // ARP buffers, which results in nested updates from our prespective
    protected int inBulkUpdate = 0 ;
    
    protected Node graphNode = Quad.defaultGraphNode ;
    protected DatasetStoreGraph datasetStore = null ;

	protected TransactionHandler transactionHandler;
    
    public GraphSDB(Store store, String uri)
    { 
        this(store, Node.createURI(uri)) ;
        
    }
    
    public GraphSDB(Store store)
    { 
        this(store, (Node)null) ;
    }

    public GraphSDB(Store store, Node graphNode)
    {
        if ( graphNode == null )
            graphNode = Quad.defaultGraphNode ;

        this.store = store ;
        this.graphNode = graphNode ;
        this.transactionHandler = new TransactionHandlerSDB(this);
        // Avoid looping here : DatasetStoreGraph can make GraphSDB's
        datasetStore = new DatasetStoreGraph(store, this) ;
        
        //readPrefixMapping() ;
    }
    
    /* We don't support value tests, hence handlesLiteralTyping is false */
    @Override
    public Capabilities getCapabilities()
    { 
    	if (capabilities == null)
            capabilities = new AllCapabilities()
        	  { @Override public boolean handlesLiteralTyping() { return false; } };
        return capabilities;
    }
    
    public Store getStore() { return store ; } 

    public SDBConnection getConnection() { return store.getConnection() ; }
    
    @Override
    public PrefixMapping getPrefixMapping()
    { 
        if ( pmap == null )
            try {
                String graphURI = null ;
                if ( graphNode.equals(Quad.defaultGraphNode) )
                    graphURI = "" ;
                else if ( graphNode.isURI() )
                    graphURI = graphNode.getURI() ; 
                else
                {
                    log.warn("Not a URI for graph name") ;
                    graphURI = graphNode.toString() ;
                }
                
                pmap = new PrefixMappingSDB(graphURI, store.getConnection()) ;
            } catch (Exception ex)
            { log.warn("Failed to get prefixes: "+ex.getMessage()) ; }
        return pmap ;
    }
    
    private Quad quad(TripleMatch m)
    {
        Node s = m.getMatchSubject() ;
        Var sVar = null ;
        if ( s == null )
        {
            sVar = Var.alloc("s") ;
            s = sVar ;
        }
        
        Node p = m.getMatchPredicate() ;
        Var pVar = null ;
        if ( p == null )
        {
            pVar = Var.alloc("p") ;
            p = pVar ;
        }
        
        Node o = m.getMatchObject() ;
        Var oVar = null ;
        if ( o == null )
        {
            oVar = Var.alloc("o") ;
            o = oVar ;
        }
        
        return new Quad(graphNode, s, p ,o) ;
    }

    @Override
    protected ExtendedIterator graphBaseFind(TripleMatch m)
    {
        // Fake a query.
        SDBRequest cxt = new SDBRequest(getStore(), new Query()) ;
        
        
        Node s = m.getMatchSubject() ;
        Var sVar = null ;
        if ( s == null )
        {
            sVar = Var.alloc("s") ;
            s = sVar ;
        }
        
        Node p = m.getMatchPredicate() ;
        Var pVar = null ;
        if ( p == null )
        {
            pVar = Var.alloc("p") ;
            p = pVar ;
        }
        
        Node o = m.getMatchObject() ;
        Var oVar = null ;
        if ( o == null )
        {
            oVar = Var.alloc("o") ;
            o = oVar ;
        }
        
        Triple triple = new Triple(s, p ,o) ;
        
        // Evaluate as an algebra expression
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(triple) ;
        Op op = new OpQuadPattern(graphNode, pattern) ;
        Plan plan = QueryEngineSDB.getFactory().create(op, datasetStore, null, null) ;
        
        QueryIterator qIter = plan.iterator() ;
        List<Triple> triples = new ArrayList<Triple>() ;
        
        for (; qIter.hasNext() ; )
        {
            Binding b = qIter.nextBinding() ;
            Node sResult = s ;
            Node pResult = p ;
            Node oResult = o ;
            if ( sVar != null )
                sResult = b.get(sVar) ;
            if ( pVar != null )
                pResult = b.get(pVar) ;
            if ( oVar != null )
                oResult = b.get(oVar) ;
            Triple resultTriple = new Triple(sResult, pResult, oResult) ;
            if ( log.isDebugEnabled() )
                log.debug("  "+resultTriple) ;
            triples.add(resultTriple) ;
        }
        qIter.close() ;
        return new GraphIterator(triples.iterator()) ;
    }

    class GraphIterator extends TrackingTripleIterator
    {
        GraphIterator(Iterator<Triple> iter) { super(iter) ; }
        
        @Override
        public void remove()
        { delete(current) ; }
    }
    
    public StoreLoader getBulkLoader() { return store.getLoader() ; }
    
    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
    {
    	if (bulkHandler == null) bulkHandler = new UpdateHandlerSDB(this);
    	return bulkHandler;
    }
    
    @Override
    public QueryHandler queryHandler()
    {
        return new GraphQueryHandlerSDB(this, graphNode, datasetStore) ;
    }
    
    @Override
    public GraphEventManager getEventManager()
    {
    	if (gem == null) gem = new EventManagerSDB( this );
        return gem;
    }
    
    @Override
    public void performAdd( Triple triple )
    {
    	if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
        
        if ( graphNode == Quad.defaultGraphNode )
            store.getLoader().addTriple(triple) ;
        else
        {
            // XXX
            StoreLoaderPlus x = (StoreLoaderPlus)store.getLoader() ;
            x.addQuad(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
        }
        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
    }
    
    @Override
    public void performDelete( Triple triple ) 
    {
    	if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
        if ( graphNode == Quad.defaultGraphNode )
            store.getLoader().deleteTriple(triple) ;
        else
        {
            // XXX
            StoreLoaderPlus x = (StoreLoaderPlus)store.getLoader() ;
            x.deleteQuad(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
        }
        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
    }
    
    public void startBulkUpdate()  { inBulkUpdate += 1 ; if (inBulkUpdate == 1) store.getLoader().startBulkUpdate();}
    public void finishBulkUpdate() { inBulkUpdate -= 1 ; if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();}
    
    @Override
    public TransactionHandler getTransactionHandler() { return transactionHandler; }
    
    @Override
    public int graphBaseSize() { return (int) ((graphNode == Quad.defaultGraphNode) ? store.getSize() : store.getSize(graphNode)); }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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