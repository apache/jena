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

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/**
    A  wrapper class which simply defers all operations to its base.
 */
public class WrappedGraph implements GraphWithPerform
{
    protected Graph base;
    protected BulkUpdateHandler bud;
    protected GraphEventManager gem;
    
    public WrappedGraph( Graph base )
    { this.base = base; } 

    @Override
    public boolean dependsOn( Graph other )
    { return base.dependsOn( other ); }

    @Override
    public TransactionHandler getTransactionHandler()
    { return base.getTransactionHandler(); }

    @Override
    @Deprecated
    public BulkUpdateHandler getBulkUpdateHandler()
    {
        if (bud == null)  bud = new WrappedBulkUpdateHandler( this, base.getBulkUpdateHandler() );
        return bud;
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler()
    { return base.getStatisticsHandler(); }

    @Override
    public Capabilities getCapabilities()
    { return base.getCapabilities(); }

    @Override
    public GraphEventManager getEventManager()
    {
        if (gem == null) gem = new SimpleEventManager( this ); 
        return gem;
    }

    @Override
    public PrefixMapping getPrefixMapping()
    { return base.getPrefixMapping(); }

    @Override
    public void add(Triple t)
    {
        base.add(t) ;
        getEventManager().notifyAddTriple(this, t) ;
    }

    @Override
    public void delete(Triple t)
    {
        base.delete(t) ;
        getEventManager().notifyDeleteTriple(this, t) ;
    }

    @Override
    public void clear()
    {
        base.clear() ;
        getEventManager().notifyEvent(this, GraphEvents.removeAll ) ;   
    }

    @Override
    public void remove(Node s, Node p, Node o)
    {
        base.remove(s,p,o) ;
        getEventManager().notifyEvent(this, GraphEvents.remove(s, p, o) ) ;
    }

    @Override
    public ExtendedIterator<Triple> find( TripleMatch m )
    { return SimpleEventManager.notifyingRemove( this, base.find( m ) ); }

    @Override
    public ExtendedIterator<Triple> find( Node s, Node p, Node o )
    { return SimpleEventManager.notifyingRemove( this, base.find( s, p, o ) ); }

    @Override
    public boolean isIsomorphicWith( Graph g )
    { return base.isIsomorphicWith( g ); }

    @Override
    public boolean contains( Node s, Node p, Node o )
    { return base.contains( s, p, o ); }

    @Override
    public boolean contains( Triple t )
    { return base.contains( t ); }

    @Override
    public void close()
    { base.close(); }

    @Override
    public boolean isClosed()
    { return base.isClosed(); }

    @Override
    public boolean isEmpty()
    { return base.isEmpty(); }

    @Override
    public int size()
    { return base.size(); }

    @Override
    public void performAdd(Triple t)
    { base.add( t ); }

    @Override
    public void performDelete(Triple t)
    { base.delete( t ); }
 }
