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

// Package
///////////////
package com.hp.hpl.jena.graph.compose;


// Imports
///////////////
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.BulkUpdateHandler ;
import com.hp.hpl.jena.graph.Capabilities ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.TransactionHandler ;
import com.hp.hpl.jena.graph.impl.WrappedBulkUpdateHandler ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;

/**
 * <p>
 * A base class for composition graphs that are composed from zero or more
 * sub-graphs (thus providing a basis for polyadic composition operators).
 * A distinguished graph is the designated graph for additions to the union.
 * By default, this is the first sub-graph of the composition, however any
 * of the graphs in the composition can be nominated to be the distinguished
 * graph.
 * </p>
 */
public abstract class Polyadic extends CompositionBase
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** A list of the sub-graphs that this composition contains */
    protected List<Graph> m_subGraphs = new ArrayList<>();

    /** The distinguished graph for adding to. If null, use the 0'th graph in the list. */
    protected Graph m_baseGraph = null;


    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a composition of exactly no sub graphs.
     * </p>
     */
    public Polyadic() {
    }


    /**
     * <p>
     * Construct a composition of all of the given graphs
     * </p>
     *
     * @param graphs An array of the sub-graphs of this composition
     */
    public Polyadic( Graph[] graphs) {
        for ( Graph graph : graphs )
        {
            m_subGraphs.add( graph );
        }
    }

    @Override
    protected PrefixMapping createPrefixMapping()
    { 
        return new PolyadicPrefixMappingImpl( this );
    }
    
    /**
     * <p>
     * Construct a composition of all of the given graphs.
     * </p>
     *
     * @param graphs An iterator of the sub-graphs of this composition. If graphs is
     *               a closable iterator, it will be automatically closed.
     */
    public Polyadic( Iterator<Graph> graphs ) {
        while (graphs.hasNext()) {
            m_subGraphs.add( graphs.next() );
        }

        if (graphs instanceof ClosableIterator<?>) {
            ((ClosableIterator<Graph>) graphs).close();
        }
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Close the graph by closing all of the sub-graphs.
     * </p>
     *
     * @see com.hp.hpl.jena.graph.Graph#close()
     */
    @Override
    public void close() {
        for ( Graph m_subGraph : m_subGraphs )
        {
            m_subGraph.close();
        }
        super.close();
    }


    /**
     * <p>
     * Answer true if this graph contains the given graph as a sub-component.
     * </p>
     *
     * @param graph A graph to test
     * @return True if the graph is this graph, or is a sub-graph of this one.
     * @see com.hp.hpl.jena.graph.Graph#dependsOn(Graph)
     */
    @Override
    public boolean dependsOn( Graph graph ) {
        return (graph == this) || m_subGraphs.contains( graph );
    }


    /**
     * <p>
     * Add the given graph to this composition.
     * </p>
     *
     * @param graph A sub-graph to add to this composition
     */
    public void addGraph( Graph graph ) {
        m_subGraphs.add( graph );
    }


    /**
     * <p>
     * Remove the given graph from this composition.  If the removed graph is the
     * designated updateable graph, the updatable graph goes back to the default
     * for this composition.
     * </p>
     *
     * @param graph A sub-graph to remove from this composition
     */
    public void removeGraph( Graph graph ) {
        m_subGraphs.remove( graph );

        if (m_baseGraph == graph) {
            m_baseGraph = null;
        }
    }


    /**
     * <p>
     * Answer the distinguished graph for the composition, which will be the graph
     * that receives triple adds and deletes. If no base graph is defined,
     * return null.
     * </p>
     *
     * @return The distinguished updateable graph, or null if there are no graphs
     *         in this composition
     */
    public Graph getBaseGraph() {
        if (m_baseGraph == null) {
            // no designated graph, so default to the first graph on the list
            return (m_subGraphs.size() == 0) ? null : m_subGraphs.get( 0 );
        }
        else {
            return m_baseGraph;
        }
    }


    /**
     * <p>
     * Answer the distinguished graph for the composition, which will be the graph
     * that receives triple adds and deletes. If no base graph is defined, throw
     * a {@link JenaException}.
     * </p>
     *
     * @return The distinguished updateable graph, or null if there are no graphs
     *         in this composition
     */
    public Graph getRequiredBaseGraph() {
        Graph base = getBaseGraph();
        if (base == null) {
            throw new JenaException( "This polyadic graph should have a base graph, but none is defined" );
        }
        else {
            return base;
        }
    }


    /**
     * <p>
     * Set the designated updateable graph for this composition.
     * </p>
     *
     * @param graph One of the graphs currently in this composition to be the
     *              designated graph to receive udpates
     * @exception IllegalArgumentException if graph is not one of the members of
     *             the composition
     */
    public void setBaseGraph( Graph graph ) {
        if (m_subGraphs.contains( graph )) {
            m_baseGraph = graph;
            bulkHandler = null;
        }
        else {
            throw new IllegalArgumentException( "The updateable graph must be one of the graphs from the composition" );
        }
    }


    /**
     * <p>
     * Answer a list of the graphs other than the updateable (base) graph
     * </p>
     *
     * @return A list of all of the sub-graphs, excluding the base graph.
     */
    public List<Graph> getSubGraphs() {
        List<Graph> sg = new ArrayList<>( m_subGraphs );

        if (getBaseGraph() != null) {
            sg.remove( getBaseGraph() );
        }

        return sg;
    }

    @Override
    @Deprecated
    public BulkUpdateHandler getBulkUpdateHandler() {
        if (bulkHandler == null)
            bulkHandler = new WrappedBulkUpdateHandler( this, getRequiredBaseGraph().getBulkUpdateHandler() );
        return bulkHandler;
    }

    // the following methods all delegate handling capabilities to the base graph
    // TODO: this needs to be integrated with WrappedGraph, but we don't have time to do so before Jena 2.0 release

    @Override
    public TransactionHandler getTransactionHandler() {
        return (getBaseGraph() == null) ? super.getTransactionHandler() : getBaseGraph().getTransactionHandler();
        }

    @Override
    public Capabilities getCapabilities() {
        return (getBaseGraph() == null) ? super.getCapabilities() : getBaseGraph().getCapabilities();
    }

}
