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

import java.util.Iterator ;
import java.util.Set ;

import com.hp.hpl.jena.JenaRuntime ;
import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.impl.SimpleEventManager ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.util.CollectionFactory ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NullIterator ;

/**
 * <p>
 * A graph implementation that presents the union of zero or more subgraphs,
 * one of which is distinguished as the updateable graph.
 * </p>
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
            for ( Graph m_subGraph : m_subGraphs )
            {
                if ( m_subGraph.contains( t ) )
                {
                    return true;
                }
            }
        return false;
        }

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
     * Answer the concatenation of all the iterators from a-subGraph.find( t ).
     */
    private ExtendedIterator<Triple> multiGraphFind(final TripleMatch t)
    {
        Set<Triple> seen = CollectionFactory.createHashedSet() ;
        ExtendedIterator<Triple> result = NullIterator.instance() ;
        boolean finished = false ;
        try {
            for ( Graph m_subGraph : m_subGraphs )
            {
                ExtendedIterator<Triple> newTriples = recording( rejecting( m_subGraph.find( t ), seen ), seen );
                result = result.andThen( newTriples );
            }
            finished = true ;
            return result ;
        } finally { // Throwable happened.
            if (!finished)
                result.close() ;
        }
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
