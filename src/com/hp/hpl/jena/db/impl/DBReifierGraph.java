/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
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
 * @version $Revision: 1.15 $
 */
public class DBReifierGraph implements Graph {

	protected List m_specializedGraphs = null; // list of SpecializedGraphReifiers
	protected GraphRDB m_parent = null;        // parent graph;
	
	/**
	 * Construct a new DBReifierGraph
	 * 
	 * @param reifiers List of SpecializedGraphReifers which holds the reified triples.  This
	 * list may be empty and, in that case, the DBReifierGraph just appears to hold no triples.
	 */
	public DBReifierGraph( GraphRDB parent, List reifiers) {
	
		m_parent = parent;
		m_specializedGraphs = reifiers;
		
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#add(com.hp.hpl.jena.graph.Triple)
	 */
	public void add( Triple t ) {
		throw new AddDeniedException( "cannot add to DB reifier", t );		
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#delete(com.hp.hpl.jena.graph.Triple)
	 */
	public void delete(Triple t) {
		throw new DeleteDeniedException( "cannot delete from a DB reifier", t );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#size()
	 */
	public int size() {
        checkUnclosed();
		int result =0;		
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			result += sg.tripleCount();
		}
		return result;
	}

    public boolean isEmpty()
        { return size() == 0; }
        
    private void checkUnclosed()
        {
        if (m_specializedGraphs == null)
            throw new ClosedException( "this DB Reifier has been closed", this );
        }
        
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#contains(com.hp.hpl.jena.graph.Triple)
	 */
	public boolean contains(Triple t) {
        checkUnclosed();
		SpecializedGraph.CompletionFlag complete = newComplete();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
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
	public boolean contains(Node s, Node p, Node o) {
		return contains(new Triple(s, p, o));
	} 
			

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#find(com.hp.hpl.jena.graph.TripleMatch)
	 */
	public ExtendedIterator find(TripleMatch m) {
        checkUnclosed();
		ExtendedIterator result = new NiceIterator();
		SpecializedGraph.CompletionFlag complete = newComplete();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			ExtendedIterator partialResult = sg.find( m, complete);
			result = result.andThen(partialResult);
			if( complete.isDone())
				break;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getPrefixMapping()
	 */
	public PrefixMapping getPrefixMapping() { 
		return m_parent.getPrefixMapping();
	}


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getTransactionHandler()
	 */
	public TransactionHandler getTransactionHandler() {
		return m_parent.getTransactionHandler();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#close()
	 */
	public void close() {
		m_specializedGraphs = null;
		m_parent = null;
	}
    
    public GraphEventManager getEventManager()
        { throw new BrokenException( "DB reifiers do not yet implement getEventManager" ); }
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#dependsOn(com.hp.hpl.jena.graph.Graph)
	 */
	public boolean dependsOn(Graph other) {
		return m_parent.dependsOn(other);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#queryHandler()
	 */
	public QueryHandler queryHandler() {
		return new SimpleQueryHandler(this);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getBulkUpdateHandler()
	 */
	public BulkUpdateHandler getBulkUpdateHandler() {
		return m_parent.getBulkUpdateHandler();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getCapabilities()
	 */
	public Capabilities getCapabilities() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getReifier()
	 */
	public Reifier getReifier() {
		throw new JenaException( "DB Reifier graphs have no reifiers" );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#find(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
	 */
	public ExtendedIterator find(Node s, Node p, Node o) {
		return find( Triple.createMatch( s, p, o ) );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#isIsomorphicWith(com.hp.hpl.jena.graph.Graph)
	 */
	public boolean isIsomorphicWith(Graph g) {
		return g != null && GraphMatcher.equals( this, g );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#capabilities()
	 */
	public int capabilities() {
		return 0;
	}
    
    public String toString()
        { return GraphBase.toString( "DBReifier ", this ); }
}

/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */