/*
 *  (c) Copyright 2003  Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 */

package com.hp.hpl.jena.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.*;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.RDF;
/**
 * @author hkuno
 * @version $Version$
 *
 * TripleStoreGraph is an abstract superclass for TripleStoreGraph
 * implementations.  By "triple store," we mean that the subjects, predicate
 * and object URI's are stored in a single collection (denormalized).
 *  
 */

public class SpecializedGraphReifier_RDB 
    extends SpecializedGraphBase 
    implements SpecializedGraphReifier {

	/**
	 * holds PSet
	 */
	public PSet_ReifStore_RDB m_pset;

	/**
	 * caches a copy of LSet properties
	 */
	public DBPropLSet m_dbPropLSet;

	/**
	 * holds ID of graph in database (defaults to "0")
	 */
	public IDBID my_GID = null;

	// cache of reified statement status
	private ReificationCacheMap m_reifCache;

	public PSet_ReifStore_RDB m_reif;

	// constructors

	/** 
	 * Constructor
	 * Create a new instance of a TripleStore graph.
	 */
	SpecializedGraphReifier_RDB(DBPropLSet lProp, IPSet pSet, Integer dbGraphID) {
		m_pset = (PSet_ReifStore_RDB) pSet;
		m_dbPropLSet = lProp;
		my_GID = new DBIDInt(dbGraphID);
		m_reifCache = new ReificationCacheMap(this, 1);
		m_reif = m_pset;
	}

	/** 
	 *  Constructor
	 * 
	 *  Create a new instance of a TripleStore graph, taking
	 *  DBPropLSet and a PSet as arguments
	 */
	public SpecializedGraphReifier_RDB(IPSet pSet, Integer dbGraphID) {
		m_pset = (PSet_ReifStore_RDB) pSet;
		my_GID = new DBIDInt(dbGraphID);
		m_reifCache = new ReificationCacheMap(this, 1);
		m_reif = m_pset;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#add(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void add(Node n, Triple t, CompletionFlag complete) throws CannotReifyException {
		ReificationStatementMask same = new ReificationStatementMask();
		ReificationStatementMask diff = new ReificationStatementMask();
		ReificationCache rs = m_reifCache.load(n, t, same, diff);
		if (rs == null) {
			m_reif.storeReifStmt(n, t, my_GID);
		} else {
			/* node already reifies something. is that a subset of triple t? */
			if ( diff.hasNada() ) {
				boolean didUpdate = false;
				/* add whatever is missing to reify t */
				if ( !same.hasSubj() ) {
					Triple st = Triple.create(n,RDF.Nodes.subject,t.getSubject());
					m_reif.updateFrag(n, st, new ReificationStatementMask(st), my_GID);
					didUpdate = true;
				}
				if ( !same.hasPred() ) {
					Triple pt = Triple.create(n,RDF.Nodes.predicate,t.getPredicate());
					m_reif.updateFrag(n, pt, new ReificationStatementMask(pt), my_GID);
					didUpdate = true;
				}
				if ( !same.hasObj() ) {
					Triple ot = Triple.create(n,RDF.Nodes.object,t.getObject());
					m_reif.updateFrag(n, ot, new ReificationStatementMask(ot), my_GID);
					didUpdate = true;
				}
				if ( !rs.mask.hasType() ) {
					Triple tt = Triple.create(n,RDF.Nodes.type,RDF.Nodes.Statement);
					m_reif.updateFrag(n, tt, new ReificationStatementMask(tt), my_GID);
					didUpdate = true;
				}
				if ( didUpdate )
					fragCompact(n);
				m_reifCache.flushAll();			
			} else {
				/* node reifies something that is not a subset of triple t */
				if ( rs.mask.isStmt() )
					throw new AlreadyReifiedException(n);
				else
					throw new CannotReifyException(n);
			}
		}
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#delete(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void delete(Node n, Triple t, CompletionFlag complete) {
		m_reifCache.flushAll();
		m_reif.deleteReifStmt( n, t, my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#contains(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public boolean contains(Node n, Triple t, CompletionFlag complete) {
		if (true)
			throw new JenaException("SpecializedGraphReifier.contains called");
		return false;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#findReifiedNodes(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public ExtendedIterator<Node> findReifiedNodes(Triple t, CompletionFlag complete) {
		complete.setDone();
		return m_reif.findReifStmtURIByTriple(t, my_GID);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#findReifiedTriple(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public Triple findReifiedTriple(Node n, CompletionFlag complete) {
		ResultSetReifIterator it = m_reif.findReifStmt(n, true, my_GID, false);
		Triple res = null;
		if ( it.hasNext() ) {
				res = it.next();
		}
		complete.setDone();
		return res;
	}

	/** Find all the triples corresponding to a given reified node.
	 * In a perfect world, there would only ever be one, but when a user calls
	 * add(Triple) there is nothing in RDF that prevents them from adding several
	 * subjects,predicates or objects for the same statement.
	 * 
	 * The resulting Triples may be incomplete, in which case some of the 
	 * nodes may be Node_ANY.
	 * 
	 * For example, if an application had previously done:
	 * add( new Triple( a, rdf.subject A )) and
	 * add( new Triple( a, rdf.object B )) and
	 * add( new Triple( a, rdf.object B2 ))
	 * 
	 * Then the result of findReifiedTriple(a, flag) will be an iterator containing
	 * Triple(A, ANY, B) and Triple(ANY, ANY, B2).
	 * 
	 * @param n is the Node for which we are querying.
	 * @param complete is true if we know we've returned all the triples which may exist.
	 * @return ExtendedIterator.
	 */
	public ExtendedIterator<Triple> findReifiedTriples(Node n, CompletionFlag complete) {
		complete.setDone();
		return m_reif.findReifStmt(n, false, my_GID, true);
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
    public void add( Graph g, CompletionFlag complete ) {
		throw new AddDeniedException( "sorry, not implemented" );
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void add(Triple frag, CompletionFlag complete) throws AlreadyReifiedException {
		ReificationStatementMask fragMask = new ReificationStatementMask(frag);
		if (fragMask.hasNada())
			return;
			
		boolean fragHasType = fragMask.hasType();
		Node stmtURI = frag.getSubject();
		ReificationCache cachedFrag = m_reifCache.load(stmtURI);
		if (cachedFrag == null) {
			// not in database
			m_reif.storeFrag(stmtURI, frag, fragMask, my_GID);
			complete.setDone();

		} else {
			ReificationStatementMask cachedMask = cachedFrag.getStmtMask();
			if (cachedMask.hasIntersect(fragMask)) {
				// see if this is a duplicate fragment
				boolean dup = fragHasType && cachedMask.hasType();
				if (dup == false) {
					// not a type fragement; have to search db to check for dup
					ExtendedIterator<Triple> it = m_reif.findFrag (stmtURI, frag, fragMask, my_GID);
					dup = it.hasNext();
					if ( dup == false ) {
						if ( cachedMask.isStmt())
							throw new AlreadyReifiedException(frag.getSubject());
						// cannot perform a reificiation; store fragment
						m_reif.storeFrag(stmtURI, frag, fragMask, my_GID);
						m_reifCache.flush(cachedFrag);
					}
				}
			} else {
				// reification may be possible; update if possible, else compact
				if (cachedFrag.canMerge(fragMask)) {
					if ( cachedFrag.canUpdate(fragMask) ) {
						m_reif.updateFrag(stmtURI, frag, fragMask, my_GID);
						cachedFrag.update(fragMask);
					} else
						fragCompact(stmtURI);					
				} else {
					// reification not possible
					m_reif.storeFrag(stmtURI, frag, fragMask, my_GID);
				}
			}
		}
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void delete(Triple frag, CompletionFlag complete) {
		ReificationStatementMask fragMask = new ReificationStatementMask(frag);
		if (fragMask.hasNada())
			return;
			
		Node stmtURI = frag.getSubject();
		
		ResultSetReifIterator it = m_reif.findFrag(stmtURI, frag, fragMask, my_GID);
		if ( it.hasNext() ) {
			if ( it.getFragCount() == 1 ) {
				/* last fragment in this tuple; can just delete it */
				m_reif.deleteFrag(frag, fragMask, my_GID);
				it.close();
			} else {
				/* remove fragment from row */
				m_reif.nullifyFrag(stmtURI, fragMask, my_GID);
				
				/* compact remaining fragments, if possible */
				it.close();
				fragCompact(stmtURI);
			}			
			// remove cache entry, if any
			ReificationCache cachedFrag = m_reifCache.lookup(stmtURI);
			if ( cachedFrag != null ) m_reifCache.flush(cachedFrag);		
		}
		complete.setDone();
	}
	
	
	/* fragCompact
	 * 
	 * Compact fragments for a given statement URI.
	 * 
	 * first, find the unique row for stmtURI that with the HasType Statement fragment.
	 * if no such row exists, we are done. then, get all fragments for stmtURI and
	 * try to merge them with the hasType fragment, deleting each as they are merged.
	 */
	protected void fragCompact ( Node stmtURI ) {
		ResultSetReifIterator itHasType;
		Triple t;
		
		itHasType = m_reif.findReifStmt(stmtURI,true,my_GID, false);
		if ( itHasType.hasNext() ) {
			/* something to do */
			t = itHasType.next();
			if ( itHasType.hasNext() ) 
                throw new JenaException("Multiple HasType fragments for URI");			
			ReificationStatementMask htMask = new ReificationStatementMask(t);
			itHasType.close();
					
			// now, look at fragments and try to merge them with the hasType fragement 
			ResultSetReifIterator itFrag = m_reif.findReifStmt(stmtURI,false,my_GID, false);
			ReificationStatementMask upMask = new ReificationStatementMask();
			while ( itFrag.hasNext() ) {
				t = itFrag.next();
				if ( itFrag.getHasType() ) continue;
				ReificationStatementMask fm = new ReificationStatementMask(rowToFrag(stmtURI, t));
				if ( htMask.hasIntersect(fm) )
					break; // can't merge all fragments
				// at this point, we can merge in the current fragment
				m_reif.updateFrag(stmtURI, t, fm, my_GID);
				htMask.setMerge(fm);
				m_reif.deleteFrag(t, fm, my_GID);
			}
		}
	}
	
	protected Triple rowToFrag ( Node stmtURI, Triple row )
	{
		Node	pred = null;
		Node	obj = null;		
		int valCnt = 0;

		if ( row.getSubject() != null ) {
			obj = row.getSubject();
			pred = RDF.Nodes.subject;
			valCnt++;	
		}
		if ( row.getPredicate() != null ) {
			obj = row.getPredicate();
			pred = RDF.Nodes.predicate;
			valCnt++;	
		}
		if ( row.getObject() != null ) {
			obj = row.getObject();
			pred = RDF.Nodes.object;
			valCnt++;	
		}
		if ( valCnt != 1 )
			throw new JenaException("Partially reified row must have exactly one value");
		
		return Triple.create(stmtURI, pred, obj);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void add(List<Triple> triples, CompletionFlag complete) {
		List<Triple> remainingTriples = new ArrayList<Triple>();
		for( int i=0; i< triples.size(); i++) {
			CompletionFlag partialResult = newComplete();
			add( triples.get(i), partialResult);
			if( !partialResult.isDone())
				remainingTriples.add(triples.get(i));
		}
		triples.clear();
		if( remainingTriples.isEmpty())
			complete.setDone();		
		else
			triples.addAll(remainingTriples);			
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public void delete(List<Triple> triples, CompletionFlag complete) {
		boolean result = true;
		Iterator<Triple> it = triples.iterator();
		while(it.hasNext()) {
			CompletionFlag partialResult = newComplete();
			delete( it.next(), partialResult);
			result = result && partialResult.isDone();
		}
		if( result )
			complete.setDone();		
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#tripleCount()
	 */
	@Override
    public int tripleCount() {
		// A very inefficient, but simple implementation
		ExtendedIterator<Triple> it = find( null, null, null, newComplete() );
		int count = 0;
		while (it.hasNext()) {
			it.next(); count++;
		}
		it.close();
		return count;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	@Override
    public ExtendedIterator<Triple> find(TripleMatch t, CompletionFlag complete) {
		
//		Node stmtURI = t.getMatchSubject();	// note: can be null
//		ResultSetReifIterator it = m_reif.findReifStmt(stmtURI, false, my_GID, true);
//		return it.filterKeep( new TripleMatchFilter( t.asTriple() ) );		
		ResultSetReifIterator it = m_reif.findReifTripleMatch(t, my_GID);
		return it;
	}

	/**
	 * Tests if a triple is contained in the specialized graph.
	 * @param t is the triple to be tested
	 * @param complete is true if the graph can guarantee that 
	 *  no other specialized graph  could hold any matching triples.
	 * @return boolean result to indicate if the triple was contained
	 */
	@Override
    public boolean contains(Triple t, CompletionFlag complete) {
		// A very inefficient, but simple implementation
		ExtendedIterator<Triple> it = find( t, complete );
		try { return it.hasNext(); } finally { it.close(); }
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#close()
	 */
	@Override
    public void close() {
		m_reif.close();
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#clear()
	 */
	@Override
    public void clear() {
		m_reif.removeStatementsFromDB(my_GID);
	}

	static boolean isReifProp ( Node_URI p ) {
		return p.equals(RDF.Nodes.subject) ||
			p.equals(RDF.Nodes.predicate)||
			p.equals(RDF.Nodes.object) || 
			p.equals(RDF.Nodes.type);			
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
	
	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#subsumes(com.hp.hpl.jena.graph.Triple, int)
	 *
	 * determine if the reifier graph has any triples of the given pattern.
	 * the table below indicates the return value for each reif style for
	 * the various types of patterns. 
	 * note: "conc" means the node in the pattern is not a concrete node.
	 * 
	 * Pattern                Minimal   Conv     Standard
	 * ANY rdf:subj ANY       none      none     all
	 * ANY rdf:pred ANY       none      none     all
	 * ANY rdf:obj  ANY       none      none     all
	 * ANY rdf:type rdf:stmt  none      none     all
	 * ANY rdf:type conc      none      none     none
	 * ANY rdf:type !conc     none      none     some
	 * ANY !conc    ANY       none      none     some
	 * else                   none      none     none
	 */
	 @Override
    public char subsumes ( Triple pattern, int reifBehavior ) {
		char res = noTriplesForPattern;
		if ( reifBehavior != GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING )
			return res;
		Node pred = pattern.getPredicate();
		if ( pred.isConcrete() ) {
			if ( pred.equals(RDF.Nodes.subject) ||
				pred.equals(RDF.Nodes.predicate) ||
				pred.equals(RDF.Nodes.object) ) 
				res = allTriplesForPattern;
			else if ( pred.equals(RDF.Nodes.type) ) {
				Node obj = pattern.getObject();
				if ( obj.equals(RDF.Nodes.Statement) )
					res = allTriplesForPattern;
				else if ( !obj.isConcrete() )
					res = someTriplesForPattern;
			}
		} else if ( (pred.isVariable()) || pred.equals(Node.ANY) ) {
			res = someTriplesForPattern;
		} else
			throw new JenaException("Unexpected predicate: " + pred.toString());
		return res;
	}
}

	/*
	 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
