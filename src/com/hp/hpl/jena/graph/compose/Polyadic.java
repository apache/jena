/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            4 Mar 2003
 * Filename           $RCSfile: Polyadic.java,v $
 * Revision           $Revision: 1.13 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:16 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.graph.compose;


// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.WrappedBulkUpdateHandler;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;


/**
 * <p>
 * A base class for composition graphs that are composed from zero or more
 * sub-graphs (thus providing a basis for polyadic composition operators).
 * A distinguished graph is the designated graph for additions to the union.
 * By default, this is the first sub-graph of the composition, however any
 * of the graphs in the composition can be nominated to be the distinguished
 * graph.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Polyadic.java,v 1.13 2004-12-06 13:50:16 andy_seaborne Exp $
 */
public abstract class Polyadic
    extends CompositionBase
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** A list of the sub-graphs that this composition contains */
    protected List m_subGraphs = new ArrayList();
    
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
        for (int i = 0;  i < graphs.length;  i++) {
            m_subGraphs.add( graphs[i] );
        }
    }
    
    private PrefixMapping pm;
    
    public PrefixMapping getPrefixMapping()
        { 
        if (pm == null) pm = new PolyadicPrefixMappingImpl( this );
        return pm;
        }
    
    /**
     * <p>
     * Construct a composition of all of the given graphs.
     * </p>
     * 
     * @param graphs An iterator of the sub-graphs of this composition. If graphs is
     *               a closable iterator, it will be automatically closed.
     */
    public Polyadic( Iterator graphs ) {
        while (graphs.hasNext()) {
            m_subGraphs.add( graphs.next() );
        }
        
        if (graphs instanceof ClosableIterator) {
            ((ClosableIterator) graphs).close();
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
    public void close() {
        for (Iterator i = m_subGraphs.iterator();  i.hasNext();  ) {
            ((Graph) i.next()).close();
        }
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
     * that receives triple adds and deletes.
     * </p>
     * 
     * @return The distinguished updateable graph, or null if there are no graphs 
     *         in this composition
     */
    public Graph getBaseGraph() {
        if (m_baseGraph == null) {
            // no designated graph, so default to the first graph on the list
            return (m_subGraphs.size() == 0) ? null : ((Graph) m_subGraphs.get( 0 ));
        }
        else {
            return m_baseGraph;
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
    public List getSubGraphs() {
        List sg = new ArrayList( m_subGraphs );
        
        if (getBaseGraph() != null) {
            sg.remove( getBaseGraph() );
        }
       
        return sg;
    }

    public BulkUpdateHandler getBulkUpdateHandler() {
        if (getBaseGraph() == null)
            throw new RuntimeException(); // return super.getBulkUpdateHandler();
        if (bulkHandler == null)  
            bulkHandler = new WrappedBulkUpdateHandler( this, getBaseGraph().getBulkUpdateHandler() );
        return bulkHandler;
    }

    // the following methods all delegate handling capabilities to the base graph
    // TODO: this needs to be integrated with WrappedGraph, but we don't have time to do so before Jena 2.0 release
    
    public TransactionHandler getTransactionHandler() { 
        return (getBaseGraph() == null) ? super.getTransactionHandler() : getBaseGraph().getTransactionHandler(); 
        }

    public Capabilities getCapabilities() { 
        return (getBaseGraph() == null) ? super.getCapabilities() : getBaseGraph().getCapabilities(); 
    }

}


/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

