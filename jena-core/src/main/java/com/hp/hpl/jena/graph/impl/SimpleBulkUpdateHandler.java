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
    @Deprecated
    public void add( Triple [] triples )
        {
            for ( Triple triple : triples )
            {
                graph.performAdd( triple );
            }
        manager.notifyAddArray( graph, triples );
        }
        
    @Override
    @Deprecated
    public void add( List<Triple> triples )
        { add( triples, true ); }
        
    protected void add( List<Triple> triples, boolean notify )
        {
            for ( Triple triple : triples )
            {
                graph.performAdd( triple );
            }
        if (notify) manager.notifyAddList( graph, triples );
        }

    @Override
    @Deprecated
    public void add( Iterator<Triple> it )
        { addIterator( it, true ); }

    public void addIterator( Iterator<Triple> it, boolean notify )
        { 
        List<Triple> s = IteratorCollection.iteratorToList( it );
        add( s, false );
        if (notify) manager.notifyAddIterator( graph, s );
        }
        
    @Override
    @Deprecated
    public void add(Graph g)
    {
        addIterator(GraphUtil.findAll(g), false) ;
        manager.notifyAddGraph(graph, g) ;
    }
    

    @Override
    @Deprecated
    public void add(Graph g, boolean withReifications)
    {
        // Now Standard reification is the only mode, just add into the graph.   
        add(g) ;
    }

   
    @Override
    @Deprecated
    public void delete( Triple [] triples )
        {
            for ( Triple triple : triples )
            {
                graph.performDelete( triple );
            }
        manager.notifyDeleteArray( graph, triples );
        }
    
    @Override
    @Deprecated
    public void delete( List<Triple> triples )
        { delete( triples, true ); }
        
    protected void delete( List<Triple> triples, boolean notify )
        {
            for ( Triple triple : triples )
            {
                graph.performDelete( triple );
            }
        if (notify) manager.notifyDeleteList( graph, triples );
        }
    
    @Override
    @Deprecated
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
        ArrayList<Triple> L = new ArrayList<>();
        Iterator<Triple> it = g.find( Triple.ANY );
        while (it.hasNext()) L.add( it.next() );
        return L;
        }
            
    @Override
    @Deprecated
    public void delete( Graph g )
        { delete( g, false ); }
        
    @Override
    @Deprecated
    public void delete( Graph g, boolean withReifications )
        { 
        if (g.dependsOn( graph ))
            delete( triplesOf( g ) );
        else
            deleteIterator( GraphUtil.findAll( g ), false );
        manager.notifyDeleteGraph( graph, g );
        }
    
    @Override
    @Deprecated
    public void removeAll()
        { removeAll( graph ); 
        notifyRemoveAll(); }
    
    protected void notifyRemoveAll()
        { manager.notifyEvent( graph, GraphEvents.removeAll ); }

    @Override
    @Deprecated
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
