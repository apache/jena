/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: WrappedGraph.java,v 1.1 2009-06-29 08:55:43 castagna Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A  wrapper class which simply defers all operations to its base.
 	@author kers
*/
public class WrappedGraph implements GraphWithPerform
    {
    protected Graph base;
    protected Reifier reifier;
    protected BulkUpdateHandler bud;
    protected GraphEventManager gem;
    
    public WrappedGraph( Graph base )
        { this.base = base; 
        this.reifier = new WrappedReifier( base.getReifier(), this ); }

    @Override
    public boolean dependsOn( Graph other )
        { return base.dependsOn( other ); }

    @Override
    public QueryHandler queryHandler()
        { return base.queryHandler(); }

    @Override
    public TransactionHandler getTransactionHandler()
        { return base.getTransactionHandler(); }

    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bud == null)  bud = new WrappedBulkUpdateHandler( this, base.getBulkUpdateHandler() );
        return bud;
        }

    @Override
    public GraphStatisticsHandler getStatisticsHandler()
        { return base.getStatisticsHandler(); }
    
    @Override
    public Capabilities getCapabilities()
        { return base.getCapabilities(); }

    @Override
    public GraphEventManager getEventManager()
        {
        if (gem == null) gem = new SimpleEventManager( this ); 
        return gem;
        }

    @Override
    public Reifier getReifier()
        {return reifier; }

    @Override
    public PrefixMapping getPrefixMapping()
        { return base.getPrefixMapping(); }

    @Override
    public void add( Triple t ) 
        { base.add( t );
        getEventManager().notifyAddTriple( this, t ); }

    @Override
    public void delete( Triple t ) 
        { base.delete( t ); 
        getEventManager().notifyDeleteTriple( this, t ); }

    @Override
    public ExtendedIterator<Triple> find( TripleMatch m )
        { return SimpleEventManager.notifyingRemove( this, base.find( m ) ); }

    @Override
    public ExtendedIterator<Triple> find( Node s, Node p, Node o )
        { return SimpleEventManager.notifyingRemove( this, base.find( s, p, o ) ); }

    @Override
    public boolean isIsomorphicWith( Graph g )
        { return base.isIsomorphicWith( g ); }

    @Override
    public boolean contains( Node s, Node p, Node o )
        { return base.contains( s, p, o ); }

    @Override
    public boolean contains( Triple t )
        { return base.contains( t ); }

    @Override
    public void close()
        { base.close(); }
    
    @Override
    public boolean isClosed()
        { return base.isClosed(); }

    @Override
    public boolean isEmpty()
        { return base.isEmpty(); }

    @Override
    public int size()
        { return base.size(); }
    
    @Override
    public void performAdd(Triple t)
        { base.add( t ); }

    @Override
    public void performDelete(Triple t)
        { base.delete( t ); }
    }


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/