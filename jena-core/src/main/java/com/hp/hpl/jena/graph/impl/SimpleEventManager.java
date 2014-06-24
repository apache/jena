/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
*/

public class SimpleEventManager implements GraphEventManager
    {
    protected Graph graph;
    protected List<GraphListener>  listeners;
    
    public SimpleEventManager( Graph graph ) 
        { 
        this.graph = graph;
        this.listeners = new CopyOnWriteArrayList<>();
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
