/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SimpleEventManager.java,v 1.9 2004-03-23 13:47:40 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;

/**
    Simple implementation of GraphEventManager for GraphBase to use.
    The listeners are held as an [Array]List.
<p>
    The code duplication is a right pain. The most natural removal tactic, a
    meta-method that took the notification method as an argument, is not
    available in Java, and I can't off-hand think of a clean alternative.
    
    @author hedgehog
*/

public class SimpleEventManager implements GraphEventManager
    {
    protected Graph graph;
    protected List  listeners;
    
    SimpleEventManager( Graph graph ) 
        { 
        this.graph = graph;
        this.listeners = new ArrayList(); 
        }
    
    public GraphEventManager register( GraphListener listener ) 
        { 
        listeners.add( listener );
        return this; 
        }
        
    public GraphEventManager unregister( GraphListener listener ) 
        { 
        listeners.remove( listener ); 
        return this;
        }
    
    public boolean listening()
        { return listeners.size() > 0; }
        
    public void notifyAddTriple( Triple t ) 
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAddTriple( t ); 
        }
    
    public void notifyAddArray( Triple [] ts )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAddArray( ts ); 
        }
        
    public void notifyAddList( List L )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAddList( L);      
        }
        
    public void notifyAddIterator( List it )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAddIterator( it.iterator() ); 
        }
        
    public void notifyAddIterator( Iterator it )
        { notifyAddIterator( GraphUtil.iteratorToList( it ) ); }
        
    public void notifyAddGraph( Graph g )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAddGraph( g ); 
        }
        
    public void notifyDeleteTriple( Triple t ) 
        { 
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDeleteTriple( t ); 
        }
        
    public void notifyDeleteArray( Triple [] ts )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDeleteArray( ts ); 
        }
        
    public void notifyDeleteList( List L )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDeleteList( L);      
        }
        
    public void notifyDeleteIterator( List L )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDeleteIterator( L.iterator() ); 
        }
        
    public void notifyDeleteIterator( Iterator it )
        { notifyDeleteIterator( GraphUtil.iteratorToList( it ) ); }    
            
    public void notifyDeleteGraph( Graph g )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDeleteGraph( g ); 
        }
    
    public void notifyEvent( Graph source, Object event )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyEvent( source, event ); }
    
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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