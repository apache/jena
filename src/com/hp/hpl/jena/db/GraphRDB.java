/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db;

import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;



/**
 *
 * GraphRDB implementation
 * 
 * This graph stores data persistently in a relational database.
 * Most application developers should not need to interact directly
 * with GraphRDB, instead use ModelRDB.
 * 
 * Each GraphRDB keeps a list of specialized graphs.
 * 
 * For each operation, it works through the list of graphs
 * attempting to perform the operation on each one.
 * 
 * The intention is that each type of specialized graph is
 * optimized for a different type of triple.  For example, one
 * type of specialied graph might be optimized for storing
 * triples in a particular ontology.  The last specialized
 * graph in the list is always a generic one that can handle any
 * valid RDF triple.
 * 
 * The order of the specialied graphs is consistent and
 * immutable after the graph is constructed.  This aids 
 * optimization.  For example, if a specialied graph
 * is asked to perform an operatin on a triple, and it knows 
 * that it would have added it if asked, then it can advise the 
 * calling GraphRDB that the operaton is complete even though
 * it doesn't know anything about other specialized graphs later
 * in the list.
 * 
 * @since Jena 2.0
 * 
 * @author csayers (based in part on GraphMem by bwm).
 * @version $Revision: 1.19 $
 */
public class GraphRDB extends GraphBase implements Graph {

    static public final String DEFAULT = "DEFAULT";

	protected IRDBDriver m_driver = null;
	protected DBPropGraph m_properties = null; 
	protected DBPrefixMappingImpl m_prefixMapping = null;
	protected List m_specializedGraphs = null;
	protected List m_specializedGraphReifiers = null;
	protected Reifier m_reifier = null;

	protected int m_reificationBehaviour = 0;
	
	/**
	 * Optimize all triples representing part or all of a reified statement.
	 * 
	 * <p>
	 * For common cases, where Graphs either contain mostly reified triples,
	 * or mostly non-reified triples, this is the best choice.  It optimizes
	 * all reified triples regardless of how they are added to the graph,
	 * provides a simple interface, and is quite efficient.
	 * </p>
	 * 
	 * <p> 
	 * With this choice, if you do <code>add(A)</code> then 
	 * <code>contains(A)</code> will return true for all A.
	 * </p>
	 * 
	 */
	public static final int OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING = 1;

	/**
	 * Optimize and hide any triples representing part or all of a reified statement.
	 * (regardless of whether added by <code>add(Triple)</code> or using the Reifier interface).
	 * 
	 * <p>
	 * If you store a mix of reified and non-reified statments within a single graph
	 * and you wish to query only non-reified statements, then this is more efficient than
	 * the other options.  However, it can cause unexpected behaviour, for example, if you do:
	 *   <code>
	 * 		add(new Triple( s, RDF.predicate, o))
	 *   </code>
	 * then that triple will be hidden and a subsequent call to <code>contains</code>, 
	 * <code>find</code>, or <code>size</code> will not show it's presence.  The only 
	 * way to see that statement is to use <code>Reifier.getHiddenTriples</code>.
	 * </p>
	 * 
	 */
	public static final int OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS = 2;

	/**
	 * Optimize and hide only fully reified statements added via the Reifier interface.
	 * 
	 * <p>
	 * This treats triples added through the Reifier interface as distinct from those
	 * added using the normal Graph.add function.  Those added via the reifier interface
	 * will be optimized and hidden from view.  Those added via Graph.add will not
	 * be optimized and will be visible.
	 * </p>
	 * 
	 * <p>
	 * Since many of the techniques for adding triple to Graphs use Graph.add, and
	 * that is never optimized, this is not usually a good choice.  It is included 
	 * for backward compability with Jena 1.	
	 * </p>
	 */
	public static final int OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS = 3;
	
	
	/**
	 * Construct a new GraphRDB
	 * @param con an open connection to the database
	 * @param graphID is the name of a graph or GraphRDB.DEFAULT
	 * @param requestedProperties a set of default properties. 
	 * (May be null, if non-null should be a superset of the properties 
	 * obtained by calling ModelRDB.getDefaultModelProperties ).
	 * @param isNew is true if the graph doesn't already exist and 
	 * false otherwise.  (If unsure, test for existance by using 
	 * IDBConnection.containsGraph ).
	 * @deprecated Please use the alernate constructor and explicely choose the desired 
	 * reification behaviour.
	 */
	public GraphRDB( IDBConnection con, String graphID, Graph requestedProperties, boolean isNew) {
		this(con, graphID, requestedProperties, OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS, isNew);
	}
		
	/**
	 * Construct a new GraphRDB
	 * @param con an open connection to the database
	 * @param graphID is the name of a graph or GraphRDB.DEFAULT
	 * @param requestedProperties a set of default properties. 
	 * (May be null, if non-null should be a superset of the properties 
	 * obtained by calling ModelRDB.getDefaultModelProperties ).
	 * @param reificationBehaviour specifies how this graph should handle reified triples.
	 * The options are 	OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS, 
	 * OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS or OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING.
	 *
	 * @param isNew is true if the graph doesn't already exist and 
	 * false otherwise.  (If unsure, test for existance by using
	 * IDBConnection.containsGraph ).
	 */
	public GraphRDB( IDBConnection con, String graphID, Graph requestedProperties, int reificationBehaviour, boolean isNew) {
	
		m_reificationBehaviour = reificationBehaviour;
		
		if(graphID == null)
			graphID = DEFAULT;
			
		// Find the driver
		m_driver = con.getDriver();
		
		// Look for properties for this graphID
		m_properties = DBPropGraph.findPropGraphByName( m_driver.getSystemSpecializedGraph(), graphID );
		
		if( m_properties != null) {
			if( isNew )
				throw new AlreadyExistsException( graphID );
			if( requestedProperties != null )
				throw new RDFRDBException("Error,attempt to change a graph's properties after it has been used.");			
			m_specializedGraphs = m_driver.recreateSpecializedGraphs( m_properties );	
		}
		else {	
			if( !isNew )
				throw new DoesNotExistException( graphID );
			
			if( requestedProperties == null )
				throw new RDFRDBException("Error requested properties is null");
			
			m_properties = new DBPropGraph( m_driver.getSystemSpecializedGraph(), graphID, requestedProperties);
			DBPropDatabase dbprop = new DBPropDatabase( m_driver.getSystemSpecializedGraph());
			dbprop.addGraph(m_properties);
			m_specializedGraphs = m_driver.createSpecializedGraphs( m_properties );
		}
		
		// Keep a list of the specialized graphs that handle reification
		// (we'll need this later to support getReifier)
		
		m_specializedGraphReifiers = new ArrayList();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			Object o = it.next();
			if( o instanceof SpecializedGraphReifier )
				m_specializedGraphReifiers.add(o);
		}
	}
	
	/** 
	 * Returns the Node for this model in the system properties graph.
	 * 
	 * The properties of each GraphRDB (things like how it is stored in
	 * the database) are themelves stored in a system Graph.  This function
	 * returns the Node which represents this GraphRDB in the system Graph.
	 * 
	 * @since Jena 2.0
	 */	
	public Node getNode() { 
		if(m_properties == null)
			throw new RDFRDBException("Error - attempt to call getNode() on a GraphRDB that has already been removed");
		return m_properties.getNode(); 
	}

	/** 
	 * Returns triples that describe this graph in the system properties graph.
	 * 
	 * The properties of each GraphRDB (things like how it is stored in
	 * the database) are stored as triples in a system Graph.  This function
	 * returns those triples.
	 * 
	 * @since Jena 2.0
	 */	
	public ExtendedIterator getPropertyTriples() {
		if(m_properties == null)
			throw new RDFRDBException("Error - attempt to call getPropertyTriples on a GraphRDB that has been removed.");
		return m_properties.listTriples();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#add(com.hp.hpl.jena.graph.Triple)
	 */
	public void add(Triple t) {
		if(m_specializedGraphs == null)
			throw new RDFRDBException("Error - attempt to call add on a GraphRDB that has already been closed");

		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			if( sg instanceof SpecializedGraphReifier && m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS)
				continue; // don't let the reifier graphs see partial reifications
			sg.add( t, complete);
			if( complete.isDone())
				return;
		}
		
		throw new JenaException("Error - GraphRDB.add(Triple) failed to find a suitable store for the triple:"+t.toString());
		
	}

	/** Add a list of triples.
	 * 
	 * @param triples List to be added. This is unchanged by the call
	 */
	public void add(List triples) {
		if(m_specializedGraphs == null)
			throw new RDFRDBException("Error - attempt to call add on a GraphRDB that has already been closed");

		ArrayList localTriples = new ArrayList( triples );
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			if( sg instanceof SpecializedGraphReifier && m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS)
				continue; // don't let the reifier graphs see partial reifications
			sg.add( localTriples, complete);
			if( complete.isDone())
				return;
		}
		
		throw new JenaException("Error - GraphRDB.add(List) failed to find a suitable store for at least one triple:"+triples.get(0).toString());
		
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#delete(com.hp.hpl.jena.graph.Triple)
	 */
	public void delete(Triple t) {
		if(m_specializedGraphs == null)
			throw new RDFRDBException("Error - attempt to call delete on a GraphRDB that has already been closed");
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			if( sg instanceof SpecializedGraphReifier && m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS)
				continue; // don't let the reifier graphs see partial reifications
			sg.delete( t, complete);
			if( complete.isDone())
				return;
		}
		
		throw new JenaException("Error - GraphRDB.delete(Triple) failed to find a suitable store for the triple:"+t.toString());

	}

	/** Delete a list of triples.
	 * 
	 * @param triples List to be deleted. This is unchanged by the call.
	 */
	public void delete(List triples) {
		if(m_specializedGraphs == null)
			throw new RDFRDBException("Error - attempt to call delete on a GraphRDB that has already been closed");
		ArrayList localTriples = new ArrayList( triples );
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			if( sg instanceof SpecializedGraphReifier && m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS)
				continue; // don't let the reifier graphs see partial reifications
			sg.delete( localTriples, complete);
			if( complete.isDone())
				return;
		}
		
		throw new JenaException("Error - GraphRDB.delete(Triple) failed to find a suitable store for at least one triple:"+triples.get(0).toString());

	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#size()
	 */
	public int size() {
		if(m_specializedGraphs == null)
			throw new RDFRDBException("Error - attempt to call size on a GraphRDB that has already been closed");
		int result =0;		
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			if( sg instanceof SpecializedGraphReifier && 
				(m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ||
				m_reificationBehaviour == OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS))
				continue; // don't let the reifier graphs see partial reifications
			result += sg.tripleCount();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#contains(com.hp.hpl.jena.graph.Triple)
	 */
	public boolean contains(Triple t) {
		if(m_specializedGraphs == null)
			throw new RDFRDBException("Error - attempt to call contains on a GraphRDB that has already been closed");
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			if( sg instanceof SpecializedGraphReifier && 
				(m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ||
				m_reificationBehaviour == OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS))
				continue; // don't let the reifier graphs see partial reifications
			boolean result = sg.contains( t, complete);
			if( result == true || complete.isDone() == true )
				return result;
		}
		return false;
	}

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
		if(m_specializedGraphs == null)
			throw new RDFRDBException("Error - attempt to call find on a GraphRDB that has already been closed");
		ExtendedIterator result = new NiceIterator();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = (SpecializedGraph) it.next();
			if( sg instanceof SpecializedGraphReifier && 
				(m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ||
				m_reificationBehaviour == OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS))
				continue; // don't let the reifier graphs see partial reifications
			ExtendedIterator partialResult = sg.find( m, complete);
			result = result.andThen(partialResult);
			if( complete.isDone())
				break;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getBulkUpdateHandler()
	 */
	 public BulkUpdateHandler getBulkUpdateHandler()
		{ return new DBBulkUpdateHandler( this ); }

	/* 
	 * (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getReifier()
	 */
	public Reifier getReifier() {
		if (m_reifier == null) {
			if (m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS
				|| m_reificationBehaviour == OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS) {
				m_reifier = new DBReifier(this, m_specializedGraphReifiers, m_specializedGraphReifiers);
			} else {
				m_reifier = new DBReifier(this, m_specializedGraphReifiers, new ArrayList());
			}
		}
		return m_reifier;
	}
	 
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getPrefixMapping()
	 */
	public PrefixMapping getPrefixMapping() { 
		if( m_prefixMapping == null)
			m_prefixMapping = new DBPrefixMappingImpl( m_properties );
		return m_prefixMapping; 
	}


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getTransactionHandler()
	 */
	public TransactionHandler getTransactionHandler() {
		return new DBTransactionHandler(m_driver, this);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#close()
	 */
	public synchronized void close() {
		if( m_specializedGraphs != null) {
			Iterator it = m_specializedGraphs.iterator();
			while( it.hasNext() ) {
				SpecializedGraph sg = (SpecializedGraph) it.next();
				sg.close();
			}
			m_specializedGraphs = null;
		}
	}
	
	/**
     * Remove this Graph entirely from the database.
     * 
     * This operation is unique to GraphRDB - it removes all
     * mention of this graph from the database - after removing
     * a graph it is recommended to immediately call close()
     * (there is no other useful operation that may be 
     * performed, and so no reason to keep the Graph around).
     */
    public synchronized void remove() {
    	
    	if(m_specializedGraphs == null)
    		throw new RDFRDBException("Error - attempt to call remove on a Graph that has already been closed");
    	// First we ask the driver to remove the specialized graphs
    	m_driver.removeSpecializedGraphs( m_properties, m_specializedGraphs );
    	m_properties = null;
    	m_specializedGraphs = null;
    }

	/**
	 * Return the connection
	 * 
	 * @return IDBConnection for the database on which this graph is stored.  
	 * Returns null if the connection has not yet been estabilished.
	 */
	public IDBConnection getConnection() {
		if( m_driver == null )
			return null;
		return m_driver.getConnection();
	}
}

/*
 *  (c) Copyright Hewlett-Packard Company 2003.
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