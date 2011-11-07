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

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
 *
 * Implementation of a "hidden triples" graph for reified statements in GraphRDB.
 * 
 * This makes the list of specializedGraphReifers in the GraphRDB into a read-only
 * Graph - suitable to be returned by Reifier.getHiddenTriples() )
 * 
 * @since Jena 2.0
 * 
 * @author csayers 
 * @version $Revision: 1.1 $
 */
public class DBReifierGraph implements Graph {

	protected List<SpecializedGraphReifier> m_specializedGraphs = null;
	protected GraphRDB m_parent = null;        // parent graph;
	
	/**
	 * Construct a new DBReifierGraph
	 * 
	 * @param reifiers List of SpecializedGraphReifers which holds the reified triples.  This
	 * list may be empty and, in that case, the DBReifierGraph just appears to hold no triples.
	 */
	public DBReifierGraph( GraphRDB parent, List<SpecializedGraphReifier> reifiers) {
	
		m_parent = parent;
		m_specializedGraphs = reifiers;
		
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#add(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public void add( Triple t ) {
		throw new AddDeniedException( "cannot add to DB reifier", t );		
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#delete(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public void delete(Triple t) {
		throw new DeleteDeniedException( "cannot delete from a DB reifier", t );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#size()
	 */
	@Override
    public int size() {
        checkUnclosed();
		int result =0;		
		Iterator<SpecializedGraphReifier> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
			result += sg.tripleCount();
		}
		return result;
	}

    @Override
    public boolean isEmpty()
        { return size() == 0; }
        
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#contains(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public boolean contains(Triple t) {
        checkUnclosed();
		SpecializedGraph.CompletionFlag complete = newComplete();
		Iterator<SpecializedGraphReifier> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
			boolean result = sg.contains( t, newComplete() );
			if (result || complete.isDone()) return result;
		}
		return false;
	}
    
    protected SpecializedGraph.CompletionFlag newComplete()
        { return new SpecializedGraph.CompletionFlag(); } 

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#contains(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
	 */
	@Override
    public boolean contains(Node s, Node p, Node o) {
		return contains( Triple.create( s, p, o ) );
	} 

    @Override
    public GraphStatisticsHandler getStatisticsHandler()
        { return null; }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#find(com.hp.hpl.jena.graph.TripleMatch)
	 */
	@Override
    public ExtendedIterator<Triple> find(TripleMatch m) {
        checkUnclosed();
		ExtendedIterator<Triple> result = NullIterator.instance() ;
		SpecializedGraph.CompletionFlag complete = newComplete();
		Iterator<SpecializedGraphReifier> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
			ExtendedIterator<Triple> partialResult = sg.find( m, complete);
			result = result.andThen(partialResult);
			if( complete.isDone())
				break;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getPrefixMapping()
	 */
	@Override
    public PrefixMapping getPrefixMapping() { 
		return m_parent.getPrefixMapping();
	}


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getTransactionHandler()
	 */
	@Override
    public TransactionHandler getTransactionHandler() {
		return m_parent.getTransactionHandler();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#close()
	 */
	@Override
    public void close() {
		m_specializedGraphs = null;
		m_parent = null;
	}

    @Override
    public boolean isClosed()
        { return m_specializedGraphs == null; }
    
    private void checkUnclosed()
        {
        if (isClosed())
            throw new ClosedException( "this DB Reifier has been closed", this );
        }
    
    @Override
    public GraphEventManager getEventManager()
        { throw new BrokenException( "DB reifiers do not yet implement getEventManager" ); }
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#dependsOn(com.hp.hpl.jena.graph.Graph)
	 */
	@Override
    public boolean dependsOn(Graph other) {
		return m_parent.dependsOn(other);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#queryHandler()
	 */
	@Override
    public QueryHandler queryHandler() {
		return new SimpleQueryHandler(this);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getBulkUpdateHandler()
	 */
	@Override
    public BulkUpdateHandler getBulkUpdateHandler() {
		return m_parent.getBulkUpdateHandler();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getCapabilities()
	 */
	@Override
    public Capabilities getCapabilities() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getReifier()
	 */
	@Override
    public Reifier getReifier() {
		throw new JenaException( "DB Reifier graphs have no reifiers" );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#find(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
	 */
	@Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
		return find( Triple.createMatch( s, p, o ) );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#isIsomorphicWith(com.hp.hpl.jena.graph.Graph)
	 */
	@Override
    public boolean isIsomorphicWith(Graph g) {
		return g != null && GraphMatcher.equals( this, g );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#capabilities()
	 */
	public int capabilities() {
		return 0;
	}
    
    @Override
    public String toString()
        { return GraphBase.toString( "DBReifier ", this ); }
}
