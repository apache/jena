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

package com.hp.hpl.jena.db;

import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
 *
 * A persistent Graph implementation using a relational database for storage.
 * 
 * <p>This graph stores data persistently in a relational database.
 * It supports the full Graph interface and should operate just like
 * a GraphMem.</p>
 * 
 * <p>
 * Internally, each GraphRDB keeps a list of specialized graphs.
 * For each operation, it works through the list of graphs
 * attempting to perform the operation on each one.</p>
 * 
 * <p>
 * The intention is that each type of specialized graph is
 * optimized for a different type of triple.  For example, one
 * type of specialied graph might be optimized for storing
 * triples in a particular ontology.  The last specialized
 * graph in the list is always a generic one that can handle any
 * valid RDF triple.</p>
 * 
 * <p>
 * The order of the specialized graphs is consistent and
 * immutable after the graph is constructed.  This aids 
 * optimization.  For example, if a specialized graph
 * is asked to perform an operatin on a triple, and it knows 
 * that it would have added it if asked, then it can advise the 
 * calling GraphRDB that the operaton is complete even though
 * it doesn't know anything about other specialized graphs later
 * in the list.</p>
 * 
 * @since Jena 2.0
 * 
 * @author csayers (based in part on GraphMem by bwm).
 * @version $Revision: 1.1 $
 */
public class GraphRDB extends GraphBase implements Graph {

	/** The name used for the default graph.
	 */
    static public final String DEFAULT = "DEFAULT";

	protected IRDBDriver m_driver = null;
	protected DBPropGraph m_properties = null; 
	protected DBPrefixMappingImpl m_prefixMapping = null;
	protected List<SpecializedGraph> m_specializedGraphs = null;
	protected List<SpecializedGraphReifier> m_specializedGraphReifiers = null;
	protected List<SpecializedGraph> m_specializedGraphAsserted = null;
	protected List<SpecializedGraph> m_specializedGraphsAll = null;
	protected Reifier m_reifier = null;

	protected int m_reificationBehaviour = 0;
	
	/**
	 * Optimize all triples representing part or all of a reified statement; 
	 * this is the recommended option.
	 * 
	 * <p>
	 * This is the best choice in almost every case.  It optimizes
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
	 * 
	 * <p>
	 * This optimizes all triples but hides them so they are visible only via the reifier interface.
	 * There is no significant performance advantage in using this option and it is not recommended.
	 * It is included only for compatability with in-memory graphs.
	 * </p>
	 * 
	 * <p>
	 * Note that it will also cause unexpected behaviour, for example, if you do:
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
	 * Optimize and hide only fully reified statements added via the Reifier interface, use only for backward-compatability with Jena1.
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
	 * only for backward compability with Jena 1.	There is no performance advantage
	 * in using this option.
	 * </p>
	 */
	public static final int OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS = 3;
	
    /**
        Answer the integer representing the RDB reifier style given the Reifier style.
    */
	public static int styleRDB( ReificationStyle style )
        {
        if (style == ReificationStyle.Standard) 
            return GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING;
        if (style == ReificationStyle.Convenient)
            return GraphRDB.OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS;
        if (style == ReificationStyle.Minimal)
            return GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS;
        throw new JenaException( "unsupported reification style" );
        }
        
    /**
        Answer the reification style corresponding to the DB behaviour integer.
    */
    public static ReificationStyle styleRDB( int behaviour )
        {
        if (behaviour == OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING)
            return ReificationStyle.Standard;
        if (behaviour == OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS)
            return ReificationStyle.Convenient;
        if (behaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS)
            return ReificationStyle.Minimal;
        throw new JenaException( "unsupported reification behaviour" );
        }
		
	/**
	 * Construct a new GraphRDB
	 * @param con an open connection to the database
	 * @param graphID is the name of a graph or GraphRDB.DEFAULT
	 * @param requestedProperties a set of default properties. 
	 * (May be null, if non-null should be a superset of the properties 
	 * obtained by calling ModelRDB.getDefaultModelProperties ).
	 * @param reificationBehaviour specifies how this graph should handle reified triples.
	 * The options are OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING 
     * (strongly recommended), OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS 
     * (included only for full compatability with all the options for in-memory Graphs), 
	 * OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS (included only for compatability with older jena1-style usage).
	 *
	 * @param isNew is true if the graph doesn't already exist and 
	 * false otherwise.  (If unsure, test for existance by using
	 * IDBConnection.containsGraph ).
	 */
	public GraphRDB( IDBConnection con, String graphID, Graph requestedProperties, int reificationBehaviour, boolean isNew) {
	   super( styleRDB( reificationBehaviour ) );
		m_reificationBehaviour = reificationBehaviour;
		
		if(graphID == null)
			graphID = DEFAULT;
		else if ( graphID.equals(DEFAULT) )
			throw new JenaException("The model name \"" + DEFAULT + "\" is reserved.");
			
		// Find the driver
		m_driver = con.getDriver();
		SpecializedGraph sysGraph = m_driver.getSystemSpecializedGraph(true);
		
		// Look for properties for this graphID
		m_properties = DBPropGraph.findPropGraphByName( sysGraph, graphID );
		
		if( m_properties != null) {
			if( isNew )
				throw new AlreadyExistsException( graphID );
			if( requestedProperties != null )
				throw new RDFRDBException("Error: attempt to change a graph's properties after it has been used.");			
			m_specializedGraphsAll = m_driver.recreateSpecializedGraphs( m_properties );	
		}
		else {	
			if( !isNew )
				throw new DoesNotExistException( graphID );			
			if( requestedProperties == null )
				throw new RDFRDBException("Error: requested properties is null");
			
			/*
			m_properties = new DBPropGraph( m_driver.getSystemSpecializedGraph(true), graphID, requestedProperties);
			DBPropDatabase dbprop = new DBPropDatabase( m_driver.getSystemSpecializedGraph(true));
			dbprop.addGraph(m_properties);
			*/
			m_specializedGraphsAll = m_driver.createSpecializedGraphs( graphID, requestedProperties );
			m_properties = DBPropGraph.findPropGraphByName( sysGraph, graphID );
			if ( m_properties == null )
				throw new RDFRDBException("Graph properties not found after creating graph.");
		}
		
		// Keep a list of the specialized graphs that handle reification
		// (we'll need this later to support getReifier)
		
		m_specializedGraphReifiers = new ArrayList<SpecializedGraphReifier>();
		m_specializedGraphAsserted = new ArrayList<SpecializedGraph>();
		Iterator<SpecializedGraph> it = m_specializedGraphsAll.iterator();
		while( it.hasNext() ) {
		    SpecializedGraph o = it.next();
			if( o instanceof SpecializedGraphReifier )
				m_specializedGraphReifiers.add((SpecializedGraphReifier)o);
			else 
				m_specializedGraphAsserted.add(o);
		}
		m_specializedGraphs = m_specializedGraphsAll;
	}
    
    /**
        Database graphs do not presently support the equivalence of plain
        string literals and xsd:string literals (and other typed literals
        in general).
         
        @see com.hp.hpl.jena.graph.Graph#getCapabilities()
    */
    @Override
    public Capabilities getCapabilities()
        { 
        if (capabilities == null) capabilities = new AllCapabilities()
            { @Override
            public boolean handlesLiteralTyping() { return false; } };
        return capabilities;
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
	public ExtendedIterator<Triple> getPropertyTriples() {
		if(m_properties == null)
			throw new RDFRDBException("Error - attempt to call getPropertyTriples on a GraphRDB that has been removed.");
		return m_properties.listTriples();
	}

	protected boolean isOpen()
		{ return !isClosed(); }
    
    @Override
    public boolean isClosed()
        { return m_specializedGraphs == null; }

    @Override
    protected void checkOpen()
        { if (isClosed()) throw new ClosedException( "GraphRDB", this ); }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#add(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
    public void performAdd(Triple t) {
		checkOpen();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
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
	public void add(List<Triple> triples) {
		checkOpen();
		List<Triple> localTriples = new ArrayList<Triple>( triples );
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
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
	@Override
    public void performDelete(Triple t) {
        checkOpen();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
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
	public void delete( List<Triple> triples ) {
		checkOpen();
		List<Triple> localTriples = new ArrayList<Triple>( triples );
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
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
	@Override
    public int graphBaseSize() {
		checkOpen();
        int result = 0;		
		Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
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
	@Override
    public boolean graphBaseContains(Triple t) {
		checkOpen();
        SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
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
	 * @see com.hp.hpl.jena.graph.Graph#find(com.hp.hpl.jena.graph.TripleMatch)
	 */
	@Override
    public ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
		checkOpen();
        ExtendedIterator<Triple> result = NullIterator.instance();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
		while( it.hasNext() ) {
			SpecializedGraph sg = it.next();
			if( sg instanceof SpecializedGraphReifier && 
				(m_reificationBehaviour == OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ||
				m_reificationBehaviour == OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS))
				continue; // don't let the reifier graphs see partial reifications
			ExtendedIterator<Triple> partialResult = sg.find( m, complete);
			result = result.andThen(partialResult);
			if( complete.isDone())
				break;
		}
		return SimpleEventManager.notifyingRemove( this, result );
	}

    @Override
    public ExtendedIterator<Triple> reifierTriples( TripleMatch m )
        { return NullIterator.instance(); }
    
    @Override
    public int reifierSize()
        { return 0; }
    
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getBulkUpdateHandler()
	 */
	 @Override
    public BulkUpdateHandler getBulkUpdateHandler()
		{ return new DBBulkUpdateHandler( this ); }

	/* 
	 * (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getReifier()
	 */
	@Override
    public Reifier getReifier() {
		if (m_reifier == null) 
				m_reifier = new DBReifier( this, style, m_specializedGraphReifiers, m_specializedGraphReifiers );
		return m_reifier;
	}
	 
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getPrefixMapping()
	 */
	@Override
    public PrefixMapping getPrefixMapping() { 
		if( m_prefixMapping == null)
			m_prefixMapping = new DBPrefixMappingImpl( m_properties );
		return m_prefixMapping; 
	}


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#getTransactionHandler()
	 */
	@Override
    public TransactionHandler getTransactionHandler() {
		return new DBTransactionHandler(m_driver, this);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#close()
	 */
	@Override
    public synchronized void close() {
		if( m_specializedGraphs != null) {
			Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
			while( it.hasNext() ) {
				SpecializedGraph sg = it.next();
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
    	checkOpen();
    	// First we ask the driver to remove the specialized graphs
    	m_driver.removeSpecializedGraphs( m_properties, m_specializedGraphs );
    	m_properties = null;
    	m_specializedGraphs = null;
    }

    /**
     * Remove all statements from this graph. 
     */
    public synchronized void clear() {
        if( m_specializedGraphs != null) {
            Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
            while( it.hasNext() ) {
                SpecializedGraph sg = it.next();
                sg.clear();
            }
        }
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
	
	/**
	 * Return the reification behavior (GraphRDB) for this graph
	 * 
	 * @return integer that defines the reification behavior for this graphRDB.
	 */
	public int reificationBehavior( )
		{
		return m_reificationBehaviour;
		}

	

	/**
	 * Return an iterator over the specialized graphs for this graph
	 * 
	 * @return Iterator over the list of specialized graphs.
	 */
	public Iterator<SpecializedGraph> getSpecializedGraphs() {
		return m_specializedGraphs.iterator();
	}

	private DBQueryHandler q = null;
   
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Graph#queryHandler()
	 */
	@Override
    public QueryHandler queryHandler()
		{
		if (q == null) q = new DBQueryHandler( this);
		return q;
		}

	public DBQueryHandler DBqueryHandler()
	{
	if (q == null) queryHandler();
	return q;
	}

	/**
	* Get the value of DoDuplicateCheck
	* @return bool boolean
	*/
	public boolean getDoDuplicateCheck() {
		return m_driver.getDoDuplicateCheck();
	}

	/**
	* Set the value of DoDuplicateCheck.
	* @param bool boolean
	*/
	public void setDoDuplicateCheck(boolean bool) {
		m_driver.setDoDuplicateCheck(bool);
		boolean nb = !bool;
		if (isOpen()) {
			Iterator<SpecializedGraph> it = m_specializedGraphs.iterator();
			while (it.hasNext()) {
				SpecializedGraph sg = it.next();
				sg.getPSet().setSkipDuplicateCheck(nb);
			}
		}
	}

	/**
	 * Set the value of DoFastpath.
	 * @param val boolean
	 */
	public void setDoFastpath ( boolean val ) {
		DBqueryHandler().setDoFastpath(val);
	}
	
	/**
	 * Get the value of DoFastpath.
	 * @return boolean
	 */
	public boolean getDoFastpath () {
		return DBqueryHandler().getDoFastpath();
	}

	/**
	 * Set the value of QueryOnlyAsserted.
	 * @param opt boolean
	 */
	public void setQueryOnlyAsserted ( boolean opt ) {
		if ( opt ) {
			m_specializedGraphs = m_specializedGraphAsserted;
			DBqueryHandler().setQueryOnlyReified(false);
		} else
			m_specializedGraphs = m_specializedGraphsAll;
		DBqueryHandler().setQueryOnlyAsserted(opt);
	}

	/**
	 * Get the value of QueryOnlyAsserted.
	 * @return boolean
	 */
	public boolean getQueryOnlyAsserted() {
		return DBqueryHandler().getQueryOnlyAsserted();
	}

	/**
	 * Set the value of QueryOnlyReified.
	 * @param opt boolean
	 */
	public void setQueryOnlyReified ( boolean opt ) {
		if ( opt ) {
			
		    m_specializedGraphs = new ArrayList<SpecializedGraph>(m_specializedGraphReifiers) ;
			DBqueryHandler().setQueryOnlyAsserted(false);
		} else {
			m_specializedGraphs = m_specializedGraphsAll;
		}
		DBqueryHandler().setQueryOnlyReified(opt);
	}

	/**
	 * Get the value of QueryOnlyReified.
	 * @return boolean
	 */
	public boolean getQueryOnlyReified() {
		return DBqueryHandler().getQueryOnlyReified();
	}

	/**
	 * Set the value of QueryFullReified.
	 * @param opt boolean
	 */
	public void setQueryFullReified ( boolean opt ) {
		DBqueryHandler().setQueryFullReified(opt);
	}

	/**
	 * Get the value of QueryFullReified.
	 * @return boolean
	 */
	public boolean getQueryFullReified() {
		return DBqueryHandler().getQueryFullReified();
	}
	
	/**
	 * Set the value of DoImplicitJoin.
	 * @param val boolean
	 */
	public void setDoImplicitJoin ( boolean val ) {
		DBqueryHandler().setDoImplicitJoin(val);
	}


}
