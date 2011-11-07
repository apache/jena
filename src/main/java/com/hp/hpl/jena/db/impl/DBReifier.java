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

import com.hp.hpl.jena.db.*;

/**
 *  Implementation of Reifier for graphs stored in a database.
 * 
 * @author csayers based in part on SimpleReifier by kers.
*/

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.shared.*;

public class DBReifier implements Reifier
    {
    protected GraphRDB m_parent = null;
    protected Graph m_hiddenTriples = null;    
	protected List<SpecializedGraphReifier> m_reifiers = null;
	protected List<SpecializedGraphReifier> m_hidden_reifiers = null;

	// For now, we just deal with a single specializedGraphReifier,
	// but in the future we could replace this with a list of
	// those and operate much as the GraphRDB implementation
	// does with it's list of SpecializedGraphs.
	protected SpecializedGraphReifier m_reifier = null;
    
    protected ReificationStyle m_style;
    
	/** 
	 *  Construct a reifier for GraphRDB's.
	 *  
	 *  @param parent the Graph for which we will expose reified triples.
	 *  @param allReifiers a List of SpecializedGraphReifiers which reifiy triples in that graph.
	 *  @param hiddenReifiers the subset of allReifiers whose triples are hidden when querying the parent graph.
	 */
	public DBReifier(GraphRDB parent, ReificationStyle style, 
	                 List<SpecializedGraphReifier> allReifiers, 
	                 List<SpecializedGraphReifier> hiddenReifiers ) {
		m_parent = parent;
		m_reifiers = allReifiers;
		m_hidden_reifiers = hiddenReifiers;
        m_style = style;
		
		// For now, just take the first specializedGraphReifier
		if (m_reifiers.size() != 1)
			throw new BrokenException("Internal error - DBReifier requires exactly one SpecializedGraphReifier");
		m_reifier = m_reifiers.get(0);
	}
            
    /* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#getParentGraph()
	 */
	@Override
    public Graph getParentGraph() { 
    	return m_parent; }
        
    @Override
    public ReificationStyle getStyle()
        { return m_style; }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#getHiddenTriples()
	 */
	private Graph getReificationTriples() {
		if( m_hiddenTriples == null) 
            m_hiddenTriples = new DBReifierGraph(m_parent, m_hidden_reifiers);
		return m_hiddenTriples;
	}
    
    @Override
    public ExtendedIterator<Triple> find( TripleMatch m )
        { return getReificationTriples().find( m ); }
    
    @Override
    public ExtendedIterator<Triple> findExposed( TripleMatch m )
        { return getReificationTriples().find( m ); }
    
    @Override
    public ExtendedIterator<Triple> findEither( TripleMatch m, boolean showHidden )
        { return showHidden == m_style.conceals() ? getReificationTriples().find( m ) : Triple.None; }

    @Override
    public int size() 
        { return m_style.conceals() ? 0 : getReificationTriples().size(); }

    /**
        Utility method useful for its short name: answer a new CompletionFlag
        initialised to false.
    */
    protected static SpecializedGraph.CompletionFlag newComplete()  
        { return new SpecializedGraph.CompletionFlag(); }
        
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#reifyAs(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public Node reifyAs( Node n, Triple t ) {
		m_reifier.add( n, t, newComplete() );
		return n;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Node)
	 */
	@Override
    public boolean hasTriple(Node n) {
		return m_reifier.findReifiedTriple( n, newComplete() ) != null;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public boolean hasTriple( Triple t ) {
		return m_reifier.findReifiedNodes(t, newComplete() ).hasNext();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#allNodes()
	 */
	@Override
    public ExtendedIterator<Node> allNodes() {
		return m_reifier.findReifiedNodes( null, newComplete() );
	}
    
    /**
        All the nodes reifying triple <code>t</code>, using the matching code
        from SimpleReifier.
    */
    @Override
    public ExtendedIterator<Node> allNodes( Triple t )
        { return m_reifier.findReifiedNodes( t, newComplete() ); }
        
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public void remove( Node n, Triple t ) {
		m_reifier.delete( n, t, newComplete() );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public void remove( Triple t ) {
		m_reifier.delete(null,t, newComplete() );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#handledAdd(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public boolean handledAdd(Triple t) {
		SpecializedGraph.CompletionFlag complete = newComplete();
		m_reifier.add(t, complete);
		return complete.isDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#handledRemove(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public boolean handledRemove(Triple t) {
		SpecializedGraph.CompletionFlag complete = newComplete();
		m_reifier.delete(t, complete);
		return complete.isDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.GetTriple#getTriple(com.hp.hpl.jena.graph.Node)
	 */
	@Override
    public Triple getTriple(Node n) {
		return m_reifier.findReifiedTriple(n, newComplete() );
	}
    
    @Override
    public void close() {
        // TODO anything useful for a close operation
    }
        
}
