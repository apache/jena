/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            4 Mar 2003
 * Filename           $RCSfile: MultiUnion.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2009-10-06 13:04:43 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.graph.compose;


// Imports
///////////////
import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.SimpleEventManager;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;


/**
 * <p>
 * A graph implementation that presents the union of zero or more subgraphs,
 * one of which is distinguished as the updateable graph.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: MultiUnion.java,v 1.2 2009-10-06 13:04:43 ian_dickinson Exp $
 */
public class MultiUnion extends Polyadic
{
    /**
     * <p>
     * Construct a union of exactly no sub graphs.
     * </p>
     */
    public MultiUnion() {
        super();
    }


    /**
     * <p>
     * Construct a union of all of the given graphs
     * </p>
     *
     * @param graphs An array of the sub-graphs of this union
     */
    public MultiUnion( Graph[] graphs) {
        super( graphs );
    }


    /**
     * <p>
     * Construct a union of all of the given graphs.
     * </p>
     *
     * @param graphs An iterator of the sub-graphs of this union. If graphs is
     *               a closable iterator, it will be automatically closed.
     */
    public MultiUnion( Iterator<Graph> graphs ) {
        super( graphs );
    }

    /**
        Answer true iff we're optimising find and query over unions with a
        single element.
    */
    private boolean optimiseOne()
        { return optimising && m_subGraphs.size() == 1; }
    
    private boolean optimising = JenaRuntime.getSystemProperty( "jena.union.optimise", "yes" ).equals( "yes" );
    
    // External signature methods
    //////////////////////////////////

    /**
        Unions share the reifiers of their base graphs. THIS WILL CHANGE.
    */
    @Override  public Reifier getReifier()
        { Graph base = getBaseGraph();
        return base == null ? super.getReifier() : base.getReifier(); }

    @Override  protected GraphStatisticsHandler createStatisticsHandler()
        { return new MultiUnionStatisticsHandler( this ); }
    
    /**
     * <p>
     * Add the given triple to the union model; the actual component model to
     * be updated will be the designated (or default) {@linkplain #getBaseGraph updateable} graph.
     * </p>
     *
     * @param t A triple to add to the union graph
     * @exception JenaException if the union does not contain any sub-graphs yet
     */
    @Override  public void performAdd( Triple t ) {
        getRequiredBaseGraph().add( t );
    }

    /**
     * <p>
     * Delete the given triple from the union model; the actual component model to
     * be updated will be the designated (or default) {@linkplain #getBaseGraph updateable} graph.
     * </p>
     *
     * @param t A triple to from the union graph
     * @exception JenaException if the union does not contain any sub-graphs yet
     */
    @Override  public void performDelete( Triple t ) {
        getRequiredBaseGraph().delete( t );
    }


    /**
     * <p>
     * Answer true if at least one of the graphs in this union contain the given triple.
     * </p>
     *
     * @param t A triple
     * @return True if any of the graphs in the union contain t
     */
    @Override  public boolean graphBaseContains( Triple t ) 
        {
        for (Iterator<Graph> i = m_subGraphs.iterator();  i.hasNext(); ) 
            if (i.next().contains( t )) return true;
        return false;
        }

    @Override public QueryHandler queryHandler()
        { return optimiseOne() ? singleGraphQueryHandler() : super.queryHandler(); }
    
    private QueryHandler singleGraphQueryHandler()
        { return (m_subGraphs.get( 0 )).queryHandler(); }

    /**
     * <p>
     * Answer an iterator over the triples in the union of the graphs in this composition. <b>Note</b>
     * that the requirement to remove duplicates from the union means that this will be an
     * expensive operation for large (and especially for persistent) graphs.
     * </p>
     *
     * @param t The matcher to match against
     * @return An iterator of all triples matching t in the union of the graphs.
     */
    @Override public ExtendedIterator<Triple> graphBaseFind( final TripleMatch t ) 
        { // optimise the case where there's only one component graph.
        ExtendedIterator<Triple> found = optimiseOne() ? singleGraphFind( t ) : multiGraphFind( t ); 
        return SimpleEventManager.notifyingRemove( MultiUnion.this, found );
        }
    
    /**
         Answer the result of <code>find( t )</code> on the single graph in
         this union.
    */
    private ExtendedIterator<Triple> singleGraphFind( final TripleMatch t )
        { return (m_subGraphs.get( 0 )).find(  t  ); }


    /**
     	Answer the concatenation of all the iterators from a-subGraph.find( t ).
    */
    private ExtendedIterator<Triple> multiGraphFind( final TripleMatch t )
        {
        Set<Triple> seen = CollectionFactory.createHashedSet();
        ExtendedIterator<Triple> result = NullIterator.instance();
        for (Iterator<Graph> graphs = m_subGraphs.iterator(); graphs.hasNext(); ) 
            {
            ExtendedIterator<Triple> newTriples = recording( rejecting( graphs.next().find( t ), seen ), seen );
            result = result.andThen( newTriples );
            }
        return result;
        }

    /**
     * <p>
     * Add the given graph to this union.  If it is already a member of the union, don't
     * add it a second time.
     * </p>
     *
     * @param graph A sub-graph to add to this union
     */
    @Override public void addGraph( Graph graph ) {
        if (!m_subGraphs.contains( graph )) {
            m_subGraphs.add( graph );
        }
    }
    
    public static class MultiUnionStatisticsHandler implements GraphStatisticsHandler
        {
        protected final MultiUnion mu;
        
        public MultiUnionStatisticsHandler( MultiUnion mu )
            { this.mu = mu; }
    
        @Override
        public long getStatistic( Node S, Node P, Node O )
            {
            long result = 0;
            for (int i = 0; i < mu.m_subGraphs.size(); i += 1)
                {
                Graph g = mu.m_subGraphs.get( i );
                GraphStatisticsHandler s = g.getStatisticsHandler();
                long n = s.getStatistic( S, P, O );
                if (n < 0) return n;
                result += n;
                }
            return result;
            }

        public MultiUnion getUnion()
            { return mu; }
        }

}


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

