/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  (c) Copyright 2009 TopQuadrant, Inc.
  [See end of file]
  $Id: SimpleEventManager.java,v 1.3 2009-12-13 05:19:06 jeremy_carroll Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.TrackingTripleIterator;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    Simple implementation of GraphEventManager for GraphBase to use.
    The listeners are held as an [Array]List.
<p>
    The code duplication is a right pain. The most natural removal tactic, a
    meta-method that took the notification method as an argument, is not
    available in Java, and I can't off-hand think of a clean alternative.
<p>
    This class also holds the utility method notifyingRemove, which wraps 
    iterators so that their .remove() operation notifies the specified graph of
    the removal.    
    
    @author hedgehog, Jeremy Carroll
*/

public class SimpleEventManager implements GraphEventManager
    {
    protected Graph graph;
    protected List<GraphListener>  listeners;
    
    public SimpleEventManager( Graph graph ) 
        { 
        this.graph = graph;
        this.listeners = new CopyOnWriteArrayList<GraphListener>(); 
/* Implementation note: Jeremy Carroll
 * 
 * Use of CopyOnWriteArray is unnecessarily inefficient, in that
 * a copy is only needed when the register or unregister
 * is concurrent with an iteration over the list.
 * Since this list is not public we can either make it private
 * or provide methods for iterating, so that we know when
 * it is necessary to copy the array of listeners and when it 
 * isn't.
 * This is a fair bit of code, and would need either a lock or
 * an atomic integer or something from the concurrent package.
 * Until and unless the high cost of registering and unregistering
 * is an issue I think the current code is elegant and clean.
 * In practice, most graphs have no more than 10 listeners
 * so the 10 registrations take 55 word copy operations - nothing
 * to get upset about.
 */
        }
    
    @Override
    public GraphEventManager register( GraphListener listener ) 
        { 
        listeners.add( listener );
        return this; 
        }
        
    @Override
    public GraphEventManager unregister( GraphListener listener ) 
        { 
        listeners.remove( listener ); 
        return this;
        }
    
    @Override
    public boolean listening()
        { return listeners.size() > 0; }
        
    @Override
    public void notifyAddTriple( Graph g, Triple t ) 
        {
        for (GraphListener l:listeners) 
            l.notifyAddTriple( g, t ); 
        }
    
    @Override
    public void notifyAddArray( Graph g, Triple [] ts )
        {
        for (GraphListener l:listeners) 
            l.notifyAddArray( g, ts ); 
        }
        
    @Override
    public void notifyAddList( Graph g, List<Triple> L )
        {
        for (GraphListener l:listeners) 
            l.notifyAddList( g, L);      
        }
        
    @Override
    public void notifyAddIterator( Graph g, List<Triple> it )
        {
        for (GraphListener l:listeners) 
            l.notifyAddIterator( g, it.iterator() ); 
        }
        
    @Override
    public void notifyAddIterator( Graph g, Iterator<Triple> it )
        { notifyAddIterator( g, IteratorCollection.iteratorToList( it ) ); }
        
    @Override
    public void notifyAddGraph( Graph g, Graph added )
        {
        for (GraphListener l:listeners) 
            l.notifyAddGraph( g, added ); 
        }
        
    @Override
    public void notifyDeleteTriple( Graph g, Triple t ) 
        { 
        for (GraphListener l:listeners) 
            l.notifyDeleteTriple( g, t ); 
        }
        
    @Override
    public void notifyDeleteArray( Graph g, Triple [] ts )
        {
        for (GraphListener l:listeners) 
            l.notifyDeleteArray( g, ts ); 
        }
        
    @Override
    public void notifyDeleteList( Graph g, List<Triple> L )
        {
        for (GraphListener l:listeners) 
            l.notifyDeleteList( g, L );      
        }
        
    @Override
    public void notifyDeleteIterator( Graph g, List<Triple> L )
        {
        for (GraphListener l:listeners) 
            l.notifyDeleteIterator( g, L.iterator() ); 
        }
        
    @Override
    public void notifyDeleteIterator( Graph g, Iterator<Triple> it )
        { notifyDeleteIterator( g, IteratorCollection.iteratorToList( it ) ); }    
            
    @Override
    public void notifyDeleteGraph( Graph g, Graph removed )
        {
        for (GraphListener l:listeners) 
            l.notifyDeleteGraph( g, removed ); 
        }
    
    @Override
    public void notifyEvent( Graph source, Object event )
        {
        for (GraphListener l:listeners) 
            l.notifyEvent( source, event ); }

    /**
     * Answer an iterator which wraps <code>i</code> to ensure that if a .remove()
     * is executed on it, the graph <code>g</code> will be notified.
    */
    public static ExtendedIterator<Triple> notifyingRemove( final Graph g, Iterator<Triple> i )
        {
        return new TrackingTripleIterator( i )
            {            
            protected final GraphEventManager gem = g.getEventManager();
            @Override
            public void remove()
                {
                super.remove();
                gem.notifyDeleteTriple( g, current );
                }
            };
        }
    
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