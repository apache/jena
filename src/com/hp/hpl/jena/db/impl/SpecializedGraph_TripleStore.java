/*
 *  (c) Copyright 2003  Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
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
    public void add(Graph g, CompletionFlag complete) {
    	ExtendedIterator it = GraphUtil.findAll( g );
    	while (it.hasNext()) add( (Triple)it.next(), complete );
		complete.setDone();
    }
    
    
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(Triple t, CompletionFlag complete) {
        m_pset.storeTriple(t, my_GID);
		complete.setDone();
	}


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(List triples, CompletionFlag complete) {
		m_pset.storeTripleList(triples,my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void delete(Triple t, CompletionFlag complete) {
		m_pset.deleteTriple(t, my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void delete(List triples, CompletionFlag complete) {
		m_pset.deleteTripleList(triples,my_GID);
		complete.setDone();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#tripleCount()
	 */
	public int tripleCount() {
		return(m_pset.tripleCount());
	}
	
	/* (non-Javadoc)
 	* @see com.hp.hpl.jena.db.impl.SpecializedGraph#contains(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
 	*/
	public boolean contains(Triple t, CompletionFlag complete) {
		complete.setDone(); 
		return (m_pset.statementTableContains(my_GID, t));
	}
 	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public ExtendedIterator find(TripleMatch t, CompletionFlag complete) {
		complete.setDone();
		return (ExtendedIterator)m_pset.find(t, my_GID);
		}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#close()
	 */
	public void close() {
		m_pset.close();
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#clear()
	 */
	public void clear() {
		m_pset.removeStatementsFromDB(my_GID);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#graphIdGet()
	 */
	public int getGraphId() {
		return ((DBIDInt)my_GID).getIntID();
	}
    
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#PSetGet()
	 */
	public IPSet getPSet() {
		return m_pset;
	}
    	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#DBPropLSetGet()
	 */
	public DBPropLSet getDBPropLSet() {
		return m_dbPropLSet;
	}

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
