/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleEventManager.java,v 1.4 2003-07-11 10:16:10 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;

/**
    Simple implementation of GraphEventManager for GraphBase to use.
    The listeners are held as an [Array]List.
    
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
        
    public void notifyAdd( Triple t ) 
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAdd( t ); 
        }
    
    public void notifyAdd( Triple [] ts )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAdd( ts ); 
        }
        
    public void notifyAdd( List L )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAdd( L);      
        }
        
    public void notifyAddIterator( List it )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyAdd( it.iterator() ); 
        }
        
    public void notifyDelete( Triple t ) 
        { 
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDelete( t ); 
        }
        
    public void notifyDelete( Triple [] ts )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDelete( ts ); 
        }
        
    public void notifyDelete( List L )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDelete( L);      
        }
        
    public void notifyDeleteIterator( List L )
        {
        for (int i = 0; i < listeners.size(); i += 1) 
            ((GraphListener) listeners.get(i)).notifyDelete( L.iterator() ); 
        }
    
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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