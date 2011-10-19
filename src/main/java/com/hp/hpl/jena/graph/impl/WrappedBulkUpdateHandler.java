/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: WrappedBulkUpdateHandler.java,v 1.1 2009-06-29 08:55:43 castagna Exp $
*/
package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.IteratorCollection;

/**
 	WrappedBulkUpdateHandler - a base class for wrapped bulk update handlers
 	(needed so WrappedGraph works properly with events). Each operation is
 	passed on to the base handler, and then this graph's event manager is
 	notified.
 	 
 	@author kers
*/
public class WrappedBulkUpdateHandler implements BulkUpdateHandler
    {
    protected BulkUpdateHandler base;
    protected GraphEventManager manager;
    protected GraphWithPerform graph;
    
    public WrappedBulkUpdateHandler( GraphWithPerform graph, BulkUpdateHandler base )
        {
        this.graph = graph;
        this.base = base;
        this.manager = graph.getEventManager();
        }

    @Override
    public void add( Triple [] triples )
        {
        base.add( triples );
        manager.notifyAddArray( graph, triples );
        }
    
    @Override
    public void add( List<Triple> triples )
        {
        base.add( triples );
        manager.notifyAddList( graph, triples );
        }

    @Override
    public void add( Iterator<Triple> it )
        {
        List<Triple> s = IteratorCollection.iteratorToList( it );
        base.add( s );
        manager.notifyAddIterator( graph, s );
        }

    @Override
    public void add( Graph g, boolean withReifications )
        {
        base.add( g, withReifications );
        manager.notifyAddGraph( graph, g );
        }
    
    @Override
    public void add( Graph g )
        {
	    base.add( g );
	    manager.notifyAddGraph( graph, g );
        }

    @Override
    public void delete( Triple[] triples )
        {
        base.delete( triples );
        manager.notifyDeleteArray( graph, triples );
        }

    @Override
    public void delete( List<Triple> triples )
        {
        base.delete( triples );
        manager.notifyDeleteList( graph, triples );
        }

    @Override
    public void delete( Iterator<Triple> it )
        {
        List<Triple> s = IteratorCollection.iteratorToList( it );
        base.delete( s );
        manager.notifyDeleteIterator( graph, s );
        }

    @Override
    public void delete( Graph g )
        {
        base.delete( g );
        manager.notifyDeleteGraph( graph, g );
        }

    @Override
    public void delete( Graph g, boolean withReifications )
        {
        base.delete( g, withReifications );
        manager.notifyDeleteGraph( graph, g );
        }

    @Override
    public void removeAll()
        {
        base.removeAll();
        manager.notifyEvent( graph, GraphEvents.removeAll );
        }

    @Override
    public void remove( Node s, Node p, Node o )
        {
        base.remove( s, p, o );
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) );
        }

    }


/*
(c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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