/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleBulkUpdateHandler.java,v 1.9 2003-07-15 11:00:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;

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
    private GraphBase graph;
    private GraphEventManager manager;
    
    public SimpleBulkUpdateHandler( GraphBase graph )
        { 
        this.graph = graph; 
        this.manager = graph.getEventManager();
        }

    public void add( Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) graph.performAdd( triples[i] ); 
        manager.notifyAddArray( triples );
        }
        
    public void add( List triples )
        { add( triples, true ); }
        
    protected void add( List triples, boolean notify )
        {
        for (int i = 0; i < triples.size(); i += 1) graph.performAdd( (Triple) triples.get(i) ); 
        if (notify) manager.notifyAddList( triples );
        }

    public void add( Iterator it )
        { addIterator( it, true ); }
        
    /**
        Add all the elements of the iterator to the graph. Trickery to arrange that if
        the graph has no listeners, the iterator is not duplicated. Not sure how to
        test this yet.
    */    
    public void addIterator( Iterator it, boolean notify )
        { 
        if (notify && manager.listening())
            {
            List s = GraphUtil.iteratorToList( it );
            add( s, false );
            manager.notifyAddIterator( s );
            }
        else
            while (it.hasNext()) graph.performAdd( (Triple) it.next() ); 
        }
        
    public void add( Graph g )
        { 
        addIterator( GraphUtil.findAll( g ), false );  
        addReifications( graph, g );
        manager.notifyAddGraph( g );
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
        manager.notifyDeleteArray( triples );
        }
    
    public void delete( List triples )
        { delete( triples, true ); }
        
    protected void delete( List triples, boolean notify )
        { 
        for (int i = 0; i < triples.size(); i += 1) graph.performDelete( (Triple) triples.get(i) );
        if (notify) manager.notifyDeleteList( triples );
        }
    
    public void delete( Iterator it )
        { deleteIterator( it, true ); }
        
    public void deleteIterator( Iterator it, boolean notify )
        {  
        if (notify && manager.listening())
            {
            List L = GraphUtil.iteratorToList( it );
            delete( L, false );
            manager.notifyDeleteIterator( L );
            }
        else
            while (it.hasNext()) graph.performDelete( (Triple) it.next() );    
         }
         
    private List triplesOf( Graph g )
        {
        ArrayList L = new ArrayList();
        Iterator it = g.find( null, null, null );
        while (it.hasNext()) L.add( it.next() );
        return L;
        }
            
    public void delete( Graph g )
        { 
        if (g.dependsOn( graph ))
            delete( triplesOf( g ) );
        else
            deleteIterator( GraphUtil.findAll( g ), false );
        deleteReifications( graph, g );
        manager.notifyDeleteGraph( g );
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