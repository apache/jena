/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: WrappedBulkUpdateHandler.java,v 1.1 2004-06-28 14:43:17 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;

/**
 	WrappedBulkUpdateHandler - a base class for wrapped bulk update handlers
 	(needed so WrappedGraph works properly with events)
 	 
 	@author kers
*/
public class WrappedBulkUpdateHandler 
	implements BulkUpdateHandler
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

    public void add( Triple [] triples )
        {
        base.add( triples );
        manager.notifyAddArray( triples );
        }
    
    public void add( List triples )
        {
        base.add( triples );
        manager.notifyAddList( triples );
        }

    public void add( Iterator it )
        {
        List s = GraphUtil.iteratorToList( it );
        base.add( s );
        manager.notifyAddIterator( s );
        }

    public void add( Graph g, boolean withReifications )
        {
        base.add( g, withReifications );
        manager.notifyAddGraph( g );
        }
    
    public void add( Graph g )
        {
	    base.add( g );
	    manager.notifyAddGraph( g );
        }

    public void delete( Triple[] triples )
        {
        base.delete( triples );
        manager.notifyDeleteArray( triples );
        }

    public void delete( List triples )
        {
        base.delete( triples );
        manager.notifyDeleteList( triples );
        }

    public void delete( Iterator it )
        {
        List s = GraphUtil.iteratorToList( it );
        base.delete( s );
        manager.notifyDeleteIterator( s );
        }

    public void delete( Graph g )
        {
        base.delete( g );
        manager.notifyDeleteGraph( g );
        }

    public void delete( Graph g, boolean withReifications )
        {
        base.delete( g, withReifications );
        manager.notifyDeleteGraph( g );
        }

    public void removeAll()
        {
        base.removeAll();
        manager.notifyEvent( graph, GraphEvents.removeAll );
        }

    public void remove( Node s, Node p, Node o )
        {
        base.remove( s, p, o );
        }

    }


/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
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