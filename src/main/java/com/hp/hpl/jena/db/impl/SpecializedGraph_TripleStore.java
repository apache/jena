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

import java.util.List;

import com.hp.hpl.jena.graph.*;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author hkuno
 * @version $Version$
 *
 * TripleStoreGraph is an abstract superclass for TripleStoreGraph
 * implementations.  By "triple store," we mean that the subjects, predicate
 * and object URI's are stored in a single collection (denormalized).
 * 
 */
public abstract class SpecializedGraph_TripleStore extends SpecializedGraphBase {

	/**
	 * holds PSet
	 */
	public IPSet m_pset;
	
	/**
	 * caches a copy of LSet properties
	 */
	public DBPropLSet m_dbPropLSet;
	
	/**
	 * holds ID of graph in database (defaults to "0")
	 */
	public IDBID my_GID = null;
	
	// constructors
	
	/** 
	 * Constructor
	 * Create a new instance of a TripleStore graph.
	 */
	SpecializedGraph_TripleStore(DBPropLSet lProp, IPSet pSet, Integer dbGraphID) {
		m_pset = pSet;
		m_dbPropLSet = lProp;
		my_GID = new DBIDInt(dbGraphID);
	}
	
	/** 
	 *  Constructor
	 * 
	 *  Create a new instance of a TripleStore graph, taking
	 *  DBPropLSet and a PSet as arguments
	 */
	public SpecializedGraph_TripleStore(IPSet pSet, Integer dbGraphID) {
		m_pset = pSet;
		my_GID = new DBIDInt(dbGraphID);
	}
	
	/** 
     * Attempt to add all the triples from a graph to the specialized graph
     * 
     * Caution - this call changes the graph passed in, deleting from 
     * it each triple that is successfully added.
     * 
     * Node that when calling add, if complete is true, then the entire
     * graph was added successfully and the graph g will be empty upon
     * return.  If complete is false, then some triples in the graph could 
     * not be added.  Those triples remain in g after the call returns.
     * 
     * If the triple can't be stored for any reason other than incompatability
     * (for example, a lack of disk space) then the implemenation should throw
     * a runtime exception.
     * 
	 * @param g is a graph containing triples to be added
	 * @param complete is true if a subsequent call to contains(triple) will return true for any triple in g.
     */
    @Override
    public void add(Graph g, CompletionFlag complete) {
    	ExtendedIterator<Triple> it = GraphUtil.findAll( g );
    	while (it.hasNext()) add( it.next(), complete );
		complete.setDone();
    }
    
    
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void add(Triple t, CompletionFlag complete) {
        m_pset.storeTriple(t, my_GID);
		complete.setDone();
	}


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void add(List<Triple> triples, CompletionFlag complete) {
		m_pset.storeTripleList(triples,my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void delete(Triple t, CompletionFlag complete) {
		m_pset.deleteTriple(t, my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void delete(List<Triple> triples, CompletionFlag complete) {
		m_pset.deleteTripleList(triples,my_GID);
		complete.setDone();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#tripleCount()
	 */
	@Override
    public int tripleCount() {
		return(m_pset.tripleCount(my_GID));
	}
	
	/* (non-Javadoc)
 	* @see com.hp.hpl.jena.db.impl.SpecializedGraph#contains(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
 	*/
	@Override
    public boolean contains(Triple t, CompletionFlag complete) {
		complete.setDone(); 
		return (m_pset.statementTableContains(my_GID, t));
	}
 	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public ExtendedIterator<Triple> find(TripleMatch t, CompletionFlag complete) {
		complete.setDone();
		return m_pset.find(t, my_GID);
		}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#close()
	 */
	@Override
    public void close() {
		m_pset.close();
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#clear()
	 */
	@Override
    public void clear() {
		m_pset.removeStatementsFromDB(my_GID);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#graphIdGet()
	 */
	@Override
    public int getGraphId() {
		return ((DBIDInt)my_GID).getIntID();
	}
    
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#PSetGet()
	 */
	@Override
    public IPSet getPSet() {
		return m_pset;
	}
    	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#DBPropLSetGet()
	 */
	@Override
    public DBPropLSet getDBPropLSet() {
		return m_dbPropLSet;
	}

}
