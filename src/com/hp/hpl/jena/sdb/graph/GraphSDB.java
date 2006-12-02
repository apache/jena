/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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
import com.hp.hpl.jena.mem.TrackingTripleIterator;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.*;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.compiler.BlockBGP;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreLoader;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


public class GraphSDB extends GraphBase implements Graph
{
    private static Log log = LogFactory.getLog(GraphSDB.class) ;

    protected PrefixMapping pmap = null ;
    protected Store store = null ;
    
    // ARP buffers, which results in nested updates from our prespective
    protected int inBulkUpdate = 0 ;
    
    public GraphSDB(Store store)
    { 
        this(store, false) ;
    }

    public GraphSDB(Store store, boolean reset)
    {
        this.store = store ;
        
        if ( reset )
            store.getTableFormatter().format() ;
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
                pmap = new PrefixMappingSDB(store.getConnection()) ;
            } catch (Exception ex)
            { log.warn("Failed to get prefixes: "+ex.getMessage()) ; }
        return pmap ;
    }
    @Override
    protected ExtendedIterator graphBaseFind(TripleMatch m)
    {
        // Fake a query.
        CompileContext cxt = new CompileContext(getStore(), new Query().getPrefixMapping()) ;
        List<Node>vars = new ArrayList<Node>() ;
        
        Node s = m.getMatchSubject() ;
        if ( s == null ) s = Node.createVariable("s") ;
        
        Node p = m.getMatchPredicate() ;
        if ( p == null ) p = Node.createVariable("p") ; 
        
        Node o = m.getMatchObject() ;
        if ( o == null ) o = Node.createVariable("o") ;
        
        Triple triple = new Triple(s, p ,o) ;
        
        if ( s.isVariable() ) vars.add(s) ;
        if ( p.isVariable() ) vars.add(p) ;
        if ( o.isVariable() ) vars.add(o) ;
        
        BlockBGP block = new BlockBGP() ;
        block.add(triple) ;

        DataSourceGraph dsg = new DataSourceGraphImpl() ;
        dsg.setDefaultGraph(this) ;
        ExecutionContext execCxt = new ExecutionContext(new Context(),  new Query(), this, dsg) ;
        QueryIterator qIter = store.getQueryCompiler().exec(getStore(), block, new BindingRoot(), execCxt) ;
        List<Triple> triples = new ArrayList<Triple>() ;
        
        for (; qIter.hasNext() ; )
        {
            Binding b = qIter.nextBinding() ;
            Node sResult = s ;
            Node pResult = p ;
            Node oResult = o ;
            if ( sResult.isVariable() )
                sResult = b.get("s") ;
            if ( pResult.isVariable() )
                pResult = b.get("p") ;
            if ( oResult.isVariable() )
                oResult = b.get("o") ;
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
        GraphIterator(Iterator iter) { super(iter) ; }
        
        @Override
        public void remove()
        { delete((Triple)current) ; }
    }
    
    public StoreLoader getBulkLoader() { return store.getLoader() ; }
    
    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
    {
    	if (bulkHandler == null) bulkHandler = new UpdateHandlerSDB(this);
    	return bulkHandler;
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
        store.getLoader().addTriple(triple) ;
        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
    }
    
    @Override
    public void performDelete( Triple triple ) 
    {
    	if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
        store.getLoader().deleteTriple(triple) ;
        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
    }
    
    public void startBulkUpdate()  { inBulkUpdate += 1 ; if (inBulkUpdate == 1) store.getLoader().startBulkUpdate();}
    public void finishBulkUpdate() { inBulkUpdate -= 1 ; if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();}
    
    @Override
    public TransactionHandler getTransactionHandler() { return store.getConnection().getTransactionHandler() ; }
    
    @Override
    public int graphBaseSize() { return (int)store.getSize(); }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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