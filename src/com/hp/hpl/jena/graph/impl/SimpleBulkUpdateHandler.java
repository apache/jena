/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SimpleBulkUpdateHandler.java,v 1.25 2005-02-21 11:52:10 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    A simple-minded implementation of the bulk update interface. This only
    operates on (subclasses of) GraphBase, since it needs access to the
    performAdd/performDelete operations.
<p>
    It handles update events, with a special eye to not copying iterators unless
    there is at least one listener registered with the graph's event manager.
    
 	@author kers
*/

public class SimpleBulkUpdateHandler implements BulkUpdateHandler
    {
    protected GraphWithPerform graph;
    protected GraphEventManager manager;
    
    public SimpleBulkUpdateHandler( GraphWithPerform graph )
        { 
        this.graph = graph; 
        this.manager = graph.getEventManager();
        }

    public void add( Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) graph.performAdd( triples[i] ); 
        manager.notifyAddArray( graph, triples );
        }
        
    public void add( List triples )
        { add( triples, true ); }
        
    protected void add( List triples, boolean notify )
        {
        for (int i = 0; i < triples.size(); i += 1) graph.performAdd( (Triple) triples.get(i) ); 
        if (notify) manager.notifyAddList( graph, triples );
        }

    public void add( Iterator it )
        { addIterator( it, true ); }

    public void addIterator( Iterator it, boolean notify )
        { 
        List s = IteratorCollection.iteratorToList( it );
        add( s, false );
        if (notify) manager.notifyAddIterator( graph, s );
        }
        
    public void add( Graph g )
        { add( g, false ); }
        
    public void add( Graph g, boolean withReifications )
        { 
        addIterator( GraphUtil.findAll( g ), false );  
        if (withReifications) addReifications( graph, g );
        manager.notifyAddGraph( graph, g );
        }
        
    public static void addReifications( Graph ours, Graph g )
        {
        Reifier r = g.getReifier();
        Iterator it = r.allNodes();
        while (it.hasNext())
            {
            Node node = (Node) it.next();
            ours.getReifier().reifyAs( node, r.getTriple( node ) );
            }
        }
        
    public static void deleteReifications( Graph ours, Graph g )
        {
        Reifier r = g.getReifier();
        Iterator it = r.allNodes();
        while (it.hasNext())
            {
            Node node = (Node) it.next();
            ours.getReifier().remove( node, r.getTriple( node ) );
            }
        }

    public void delete( Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) graph.performDelete( triples[i] ); 
        manager.notifyDeleteArray( graph, triples );
        }
    
    public void delete( List triples )
        { delete( triples, true ); }
        
    protected void delete( List triples, boolean notify )
        { 
        for (int i = 0; i < triples.size(); i += 1) graph.performDelete( (Triple) triples.get(i) );
        if (notify) manager.notifyDeleteList( graph, triples );
        }
    
    public void delete( Iterator it )
        { deleteIterator( it, true ); }
        
    public void deleteIterator( Iterator it, boolean notify )
        {  
        List L = IteratorCollection.iteratorToList( it );
        delete( L, false );
        if (notify) manager.notifyDeleteIterator( graph, L );
         }
         
    private List triplesOf( Graph g )
        {
        ArrayList L = new ArrayList();
        Iterator it = g.find( Triple.ANY );
        while (it.hasNext()) L.add( it.next() );
        return L;
        }
            
    public void delete( Graph g )
        { delete( g, false ); }
        
    public void delete( Graph g, boolean withReifications )
        { 
        if (g.dependsOn( graph ))
            delete( triplesOf( g ) );
        else
            deleteIterator( GraphUtil.findAll( g ), false );
        if (withReifications) deleteReifications( graph, g );
        manager.notifyDeleteGraph( graph, g );
        }
    
    public void removeAll()
        { removeAll( graph ); 
        notifyRemoveAll(); }
    
    protected void notifyRemoveAll()
        { manager.notifyEvent( graph, GraphEvents.removeAll ); }

    public void remove( Node s, Node p, Node o )
        { removeAll( graph, s, p, o ); 
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) ); }
    
    public static void removeAll( Graph g, Node s, Node p, Node o )
        {
        ExtendedIterator it = g.find( s, p, o );
        try { while (it.hasNext()) { it.next(); it.remove(); } }
        finally { it.close(); }
        }
    
    public static void removeAll( Graph g )
        {
        ExtendedIterator it = GraphUtil.findAll( g );
        try { while (it.hasNext()) { it.next(); it.remove(); } }
        finally { it.close(); }
        }
    }


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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