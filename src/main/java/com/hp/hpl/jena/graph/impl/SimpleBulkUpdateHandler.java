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

    @Override
    public void add( Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) graph.performAdd( triples[i] ); 
        manager.notifyAddArray( graph, triples );
        }
        
    @Override
    public void add( List<Triple> triples )
        { add( triples, true ); }
        
    protected void add( List<Triple> triples, boolean notify )
        {
        for (int i = 0; i < triples.size(); i += 1) graph.performAdd( triples.get(i) ); 
        if (notify) manager.notifyAddList( graph, triples );
        }

    @Override
    public void add( Iterator<Triple> it )
        { addIterator( it, true ); }

    public void addIterator( Iterator<Triple> it, boolean notify )
        { 
        List<Triple> s = IteratorCollection.iteratorToList( it );
        add( s, false );
        if (notify) manager.notifyAddIterator( graph, s );
        }
        
    @Override
    public void add( Graph g )
        { add( g, false ); }
        
    @Override
    public void add( Graph g, boolean withReifications )
        { 
        addIterator( GraphUtil.findAll( g ), false );  
        if (withReifications) addReifications( graph, g );
        manager.notifyAddGraph( graph, g );
        }
        
    public static void addReifications( Graph ours, Graph g )
        {
        Reifier r = g.getReifier();
        Iterator<Node> it = r.allNodes();
        while (it.hasNext())
            {
            Node node = it.next();
            ours.getReifier().reifyAs( node, r.getTriple( node ) );
            }
        }
        
    public static void deleteReifications( Graph ours, Graph g )
        {
        Reifier r = g.getReifier();
        Iterator<Node> it = r.allNodes();
        while (it.hasNext())
            {
            Node node = it.next();
            ours.getReifier().remove( node, r.getTriple( node ) );
            }
        }

    @Override
    public void delete( Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) graph.performDelete( triples[i] ); 
        manager.notifyDeleteArray( graph, triples );
        }
    
    @Override
    public void delete( List<Triple> triples )
        { delete( triples, true ); }
        
    protected void delete( List<Triple> triples, boolean notify )
        { 
        for (int i = 0; i < triples.size(); i += 1) graph.performDelete( triples.get(i) );
        if (notify) manager.notifyDeleteList( graph, triples );
        }
    
    @Override
    public void delete( Iterator<Triple> it )
        { deleteIterator( it, true ); }
        
    public void deleteIterator( Iterator<Triple> it, boolean notify )
        {  
        List<Triple> L = IteratorCollection.iteratorToList( it );
        delete( L, false );
        if (notify) manager.notifyDeleteIterator( graph, L );
         }
         
    private List<Triple> triplesOf( Graph g )
        {
        ArrayList<Triple> L = new ArrayList<Triple>();
        Iterator<Triple> it = g.find( Triple.ANY );
        while (it.hasNext()) L.add( it.next() );
        return L;
        }
            
    @Override
    public void delete( Graph g )
        { delete( g, false ); }
        
    @Override
    public void delete( Graph g, boolean withReifications )
        { 
        if (g.dependsOn( graph ))
            delete( triplesOf( g ) );
        else
            deleteIterator( GraphUtil.findAll( g ), false );
        if (withReifications) deleteReifications( graph, g );
        manager.notifyDeleteGraph( graph, g );
        }
    
    @Override
    public void removeAll()
        { removeAll( graph ); 
        notifyRemoveAll(); }
    
    protected void notifyRemoveAll()
        { manager.notifyEvent( graph, GraphEvents.removeAll ); }

    @Override
    public void remove( Node s, Node p, Node o )
        { removeAll( graph, s, p, o ); 
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) ); }
    
    public static void removeAll( Graph g, Node s, Node p, Node o )
        {
        ExtendedIterator<Triple> it = g.find( s, p, o );
        try { while (it.hasNext()) { it.next(); it.remove(); } }
        finally { it.close(); }
        }
    
    public static void removeAll( Graph g )
        {
        ExtendedIterator<Triple> it = GraphUtil.findAll( g );
        try { while (it.hasNext()) { it.next(); it.remove(); } }
        finally { it.close(); }
        }
    }
