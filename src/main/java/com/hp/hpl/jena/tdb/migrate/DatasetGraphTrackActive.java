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

package com.hp.hpl.jena.tdb.migrate;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Transactional ;
import com.hp.hpl.jena.sparql.util.Context ;

public abstract class DatasetGraphTrackActive implements DatasetGraph, Transactional
{
    // Layer - make this extend DatasetGraphWrapper - i.e. a switching wrapper.
    // Can use switcher for switching between locations?
    
    protected abstract DatasetGraph get() ;
    private boolean inTransaction = false ; 

    protected DatasetGraphTrackActive()
    {
        inTransaction = false ;
    }

    protected abstract void checkActive() ;
    protected abstract void checkNotActive() ;
    
    @Override
    public final void begin(ReadWrite readWrite)
    {
        checkNotActive() ;
        _begin(readWrite) ;
        inTransaction = true ;
    }
    
    @Override
    public void commit()
    {
        checkActive() ;
        _commit() ;
        inTransaction = false ;
    }

    @Override
    public void abort()
    {
        checkActive() ;
        _abort() ;
        inTransaction = false ;
    }
    
    @Override
    public void close()
    {
        _close() ;
        inTransaction = false ;
        // Can call close repeatedly 
        if (get() != null)
            get().close() ;
    }

    @Override
    public boolean isInTransaction()    { return inTransaction ; }
    
    protected abstract void _begin(ReadWrite readWrite) ;
    protected abstract void  _commit() ;
    protected abstract void  _abort() ;
    protected abstract void  _close() ;
    
    @Override
    public boolean containsGraph(Node graphNode)
    { 
        checkActive() ;
        return get().containsGraph(graphNode) ;
    }

    @Override
    public Graph getDefaultGraph()
    { return get().getDefaultGraph(); }

    @Override
    public Graph getGraph(Node graphNode)
    { return get().getGraph(graphNode) ; }

    @Override
    public void addGraph(Node graphName, Graph graph)
    { get().addGraph(graphName, graph) ; }

    @Override
    public void removeGraph(Node graphName)
    { get().removeGraph(graphName) ; }

    @Override
    public void setDefaultGraph(Graph g)
    { get().setDefaultGraph(g) ; }

    @Override
    public Lock getLock()
    { return get().getLock() ; }

    @Override
    public Iterator<Node> listGraphNodes()
    { return get().listGraphNodes() ; }

    @Override
    public void add(Quad quad)
    { get().add(quad) ; }

    @Override
    public void delete(Quad quad)
    { get().delete(quad) ; }

    @Override
    public void add(Node g, Node s, Node p, Node o)
    { get().add(g, s, p, o) ; }

    @Override
    public void delete(Node g, Node s, Node p, Node o)
    { get().delete(g, s, p, o) ; }
    
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o)
    { get().deleteAny(g, s, p, o) ; }

    @Override
    public boolean isEmpty()
    { return get().isEmpty() ; }
    
    @Override
    public Iterator<Quad> find()
    { return get().find() ; }

    @Override
    public Iterator<Quad> find(Quad quad)
    { return get().find(quad) ; }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    { return get().find(g, s, p, o) ; }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o)
    { return get().findNG(g, s, p, o) ; }

    @Override
    public boolean contains(Quad quad)
    { return get().contains(quad) ; }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o)
    { return get().contains(g, s, p, o) ; }

    @Override
    public Context getContext()
    { return get().getContext() ; }

    @Override
    public long size()
    { return get().size() ; }
}
