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

/**
 	WrappedBulkUpdateHandler - a base class for wrapped bulk update handlers
 	(needed so WrappedGraph works properly with events). Each operation is
 	passed on to the base handler, and then this graph's event manager is
 	notified.
 	 
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
    @Deprecated
    public void add( Triple [] triples )
        {
        base.add( triples );
        manager.notifyAddArray( graph, triples );
        }
    
    @Override
    @Deprecated
    public void add( List<Triple> triples )
        {
        base.add( triples );
        manager.notifyAddList( graph, triples );
        }

    @Override
    @Deprecated
    public void add( Iterator<Triple> it )
        {
        List<Triple> s = IteratorCollection.iteratorToList( it );
        base.add( s );
        manager.notifyAddIterator( graph, s );
        }

    @Override
    @Deprecated
    public void add( Graph g, boolean withReifications )
        {
        base.add( g, withReifications );
        manager.notifyAddGraph( graph, g );
        }
    
    @Override
    @Deprecated
    public void add( Graph g )
        {
	    base.add( g );
	    manager.notifyAddGraph( graph, g );
        }

    @Override
    @Deprecated
    public void delete( Triple[] triples )
        {
        base.delete( triples );
        manager.notifyDeleteArray( graph, triples );
        }

    @Override
    @Deprecated
    public void delete( List<Triple> triples )
        {
        base.delete( triples );
        manager.notifyDeleteList( graph, triples );
        }

    @Override
    @Deprecated
    public void delete( Iterator<Triple> it )
        {
        List<Triple> s = IteratorCollection.iteratorToList( it );
        base.delete( s );
        manager.notifyDeleteIterator( graph, s );
        }

    @Override
    @Deprecated
    public void delete( Graph g )
        {
        base.delete( g );
        manager.notifyDeleteGraph( graph, g );
        }

    @Override
    @Deprecated
    public void delete( Graph g, boolean withReifications )
        {
        base.delete( g, withReifications );
        manager.notifyDeleteGraph( graph, g );
        }

    @Override
    @Deprecated
    public void removeAll()
        {
        base.removeAll();
        manager.notifyEvent( graph, GraphEvents.removeAll );
        }

    @Override
    @Deprecated
    public void remove( Node s, Node p, Node o )
        {
        base.remove( s, p, o );
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) );
        }

    }
