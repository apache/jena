/*
 *  (c) Copyright Hewlett-Packard Company 2003 
 *  All rights reserved.
 *
 */

package com.hp.hpl.jena.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.MapFiller;
import com.hp.hpl.jena.util.iterator.MapMany;

/**
 * @author hkuno
 * @version $Version$
 *
 * TripleStoreGraph is an abstract superclass for TripleStoreGraph
 * implementations.  By "triple store," we mean that the subjects, predicate
 * and object URI's are stored in a single collection (denormalized).
 *  
 */

public class SpecializedGraphReifier_RDB implements SpecializedGraphReifier {

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
	public IDBID my_GID = new DBIDInt(0);

	// lset name
	private String m_lsetName;

	// lset classname
	private String m_className;

	// cache of reified statement status
	private ReifCacheMap m_reifCache;

	public PSet_ReifStore_RDB m_reif;

	// constructors

	/** 
	 * Constructor
	 * Create a new instance of a TripleStore graph.
	 */
	SpecializedGraphReifier_RDB(DBPropLSet lProp, IPSet pSet) {
		m_pset = pSet;
		m_dbPropLSet = lProp;
		m_reifCache = new ReifCacheMap(1);
		m_reif = (PSet_ReifStore_RDB) m_pset;
	}

	/** 
	 *  Constructor
	 * 
	 *  Create a new instance of a TripleStore graph, taking
	 *  DBPropLSet and a PSet as arguments
	 */
	public SpecializedGraphReifier_RDB(IPSet pSet) {
		m_pset = pSet;
		m_reifCache = new ReifCacheMap(1);
		m_reif = (PSet_ReifStore_RDB) m_pset;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#add(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(Node n, Triple t, CompletionFlag complete) throws Reifier.AlreadyReifiedException {
		ReifCache rs = m_reifCache.load((Node_URI) t.getSubject());
		if (rs != null)
			throw new Reifier.AlreadyReifiedException(n);
		m_reif.storeReifStmt((Node_URI)n, t, my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#delete(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void delete(Node n, Triple t, CompletionFlag complete) {
		m_reifCache.flushAll();
		m_reif.deleteReifStmt( (Node_URI) n, t, my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#contains(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public boolean contains(Node n, Triple t, CompletionFlag complete) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#findReifiedNodes(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public ExtendedIterator findReifiedNodes(TripleMatch t, CompletionFlag complete) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#findReifiedTriple(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public Triple findReifiedTriple(Node n, CompletionFlag complete) {
		ExtendedIterator it = m_reif.findReifNodes(n, true, my_GID);
		Triple res = null;
		if ( it.hasNext() ) {
				List l = (List) it.next();
				if ( !it.hasNext() )
					res = new Triple((Node)l.get(1), (Node)l.get(2), (Node)l.get(3));
		}
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
	public ExtendedIterator findReifiedTriples(Node n, CompletionFlag complete) {
		return m_reif.findReifNodes(n, false, my_GID);
	}

	/** Find if a node has been marked as being of type Statement.
	 * 
	 * @param n is the Node for which we are querying.
	 * @return ExtendedIterator.
	 */
	public boolean findIfStatement(Node n) {
		ExtendedIterator it = m_reif.findReifNodes(n, true, my_GID);
		return it.hasNext();
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
		throw new RuntimeException("sorry, not implemented");
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(Triple frag, CompletionFlag complete) throws Reifier.AlreadyReifiedException {
		StmtMask m = new StmtMask(frag);
		if (m.hasNada())
			return;
/*
		boolean fht = m.hasType();
		Triple t = fragToTriple(frag, m);
		Node_URI stmtURI = (Node_URI) frag.getSubject();
		ReifCache rs = m_reifCache.load(stmtURI);
		if (rs == null) {
			// not in database
			m_reif.storeReifStmt(stmtURI, t, fht, my_GID);
			complete.setDone();

		} else {
			Node_URI stmtURI = (Node_URI) frag.getSubject();
			StmtMask rsm = rs.getStmtMask();
			if (rsm.hasIntersect(m)) {
				// see if this is a duplicate fragment
				boolean dup = fht && rsm.hasType();
				if (dup == false) {
					// not a type fragement; have to search db
					TripleMatch tm =
						new StandardTripleMatch(
							t.getSubject(),
							m.hasProp() ? t.getPredicate() : null,
							m.hasObj() ? t.getObject() : null);
					ResultSetReifIterator it = m_reif.findReif(stmtURI, t, tm, my_GID);
					dup = it.hasNext();
				}
				if (!dup && rsm.isStmt()) {
					throw new Reifier.AlreadyReifiedException(n);
				}
				// cannot perform a reificiation
				m_reif.storeTriple(t, fht, my_GID);
				m_reifCache.flush(rs);
			} else {
				// reification may be possible
				if (rs.getCnt() > 1) {
					// reification not possible
					m_reif.storeTriple(t, fht, my_GID);
				} else {
					// reification may be possible
					rsm.setUnion(m);
					m_reif.updateTriple(t, fht, my_GID);
				}
			}
		}
		complete.setDone();
*/
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void delete(Triple frag, CompletionFlag complete) {
		StmtMask m = new StmtMask(frag);
		if (m.hasNada())
			return;
/*
		Triple t = fragToTriple(frag, m);
		// first need to see if database contains fragment
		TripleMatch tm =
			new StandardTripleMatch(
				t.getSubject(),
				m.hasProp() ? t.getPredicate() : null,
				m.hasObj() ? t.getObject() : null);
		ResultSetReifIterator it = m_reif.find(tm, my_GID);
		if (it.hasNext()) {
			// db has fragment. now we have something to do
			// should be only one instance of this pair
			m_reif.updateNull(t, m, my_GID);
			// now see if this enables a reified statement
			ReifCache rs = m_reifCache.load((Node_URI) t.getSubject());
			if (rs.getStmtMask().hasNada()) {
				// delete last fragment
				m_reif.deleteTriple(t, my_GID);
				// need to do compaction of fragments
			}
		}
		complete.setDone();
*/
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(List triples, CompletionFlag complete) {
		ArrayList remainingTriples = new ArrayList();
		for( int i=0; i< triples.size(); i++) {
			CompletionFlag partialResult = new CompletionFlag();
			add( (Triple)triples.get(i), partialResult);
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
	public void delete(List triples, CompletionFlag complete) {
		boolean result = true;
		Iterator it = triples.iterator();
		while(it.hasNext()) {
			CompletionFlag partialResult = new CompletionFlag();
			delete( (Triple)it.next(), partialResult);
			result = result && partialResult.isDone();
		}
		if( result )
			complete.setDone();		
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#tripleCount()
	 */
	public int tripleCount() {
		// A very inefficient, but simple implementation
		ExtendedIterator it = find(new StandardTripleMatch(null, null, null), new CompletionFlag());
		int count = 0;
		while (it.hasNext()) {
			count++;
		}
		it.close();
		return count;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public ExtendedIterator find(TripleMatch t, CompletionFlag complete) {

		ExtendedIterator nodes = findReifiedNodes(new StandardTripleMatch(null, null, null), new CompletionFlag());
		ExtendedIterator allTriples = new MapMany(nodes, new ExpandReifiedTriples(this));

		return allTriples.filterKeep(new TripleMatchFilter(t));
	}

	public class ExpandReifiedTriples implements MapFiller {

		SpecializedGraphReifier_RDB m_sgr;
		
		ExpandReifiedTriples( SpecializedGraphReifier_RDB sgr) { 
			m_sgr = sgr; 
		}
		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.util.iterator.MapFiller#refill(java.lang.Object, java.util.ArrayList)
		 */
		public boolean refill(Object x, ArrayList pending) {
			Node node = (Node) x;
			boolean addedToPending = false;

			if( m_sgr.findIfStatement( node)) {
				pending.add( new Triple( node, Reifier.type, Reifier.Statement ) );
				addedToPending = true;
			}
			
			Iterator it = m_sgr.findReifiedTriples(node, new CompletionFlag());
			while( it.hasNext()) {
				Triple t = (Triple)it.next();
				if( !t.getSubject().equals(Node.ANY)) {
					pending.add( new Triple( node, Reifier.subject, t.getSubject() ));
					addedToPending = true;
				}
				if( !t.getPredicate().equals(Node.ANY)) {
					pending.add( new Triple( node, Reifier.predicate, t.getPredicate() ));
					addedToPending = true;
				}
				if( !t.getObject().equals(Node.ANY)) {
					pending.add( new Triple( node, Reifier.object, t.getObject() ));
					addedToPending = true;
				}
					
			}
			return addedToPending;
		}

	}
	/**
	 * Tests if a triple is contained in the specialized graph.
	 * @param t is the triple to be tested
	 * @param complete is true if the graph can guarantee that 
	 *  no other specialized graph 
	 * could hold any matching triples.
	 * @return boolean result to indicte if the tripple was contained
	 */
	public boolean contains(Triple t, CompletionFlag complete) {
		// A very inefficient, but simple implementation
		TripleMatch m = new StandardTripleMatch(t.getSubject(), t.getPredicate(), t.getObject());
		ExtendedIterator it = find(m, complete);
		boolean result = it.hasNext();
		it.close();
		return result;
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#getLSetName()
	 */
	public String getLSetName() {
		return m_lsetName;
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#getClassName()
	 */
	public String getClassName() {
		return m_className;
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#close()
	 */
	public void close() {
		m_reif.close();
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#clear()
	 */
	public void clear() {
		m_reif.removeStatementsFromDB(my_GID);
	}

	public class ReifCacheMap {
		protected int cacheSize = 1;
		protected ReifCache[] cache;
		protected boolean[] inUse;

		ReifCacheMap(int size) {
			int i;
			inUse = new boolean[size];
			for (i = 0; i < size; i++)
				inUse[i] = false;
		}

		ReifCache lookup(Node_URI stmtURI) {
			int i;
			for (i = 0; i < cacheSize; i++) {
				if (inUse[i] && (cache[i].getStmtURI().equals(stmtURI)))
					return cache[i];
			}
			return null;
		}

		public void flushAll() {
			int i;
			for (i = 0; i < cacheSize; i++)
				inUse[i] = false;
		}

		public void flush(ReifCache entry) {
			flushAll(); // optimize later
		}

		// TODO - The stmtURI could be a bNode - so this should be Node, not Node_URI.
		public ReifCache load(Node_URI stmtURI) {
			return null;
/*
			ReifCache entry = lookup(stmtURI);
			if (entry != null)
				return entry;
			flushAll();
			StmtMask m = new StmtMask();
			Triple t;
			int cnt = 0;
			TripleMatch tm = new StandardTripleMatch(stmtURI, null, null);
			ResultSetReifIterator it = m_reif.find(tm, my_GID);
			// ? ? ? ? how to get results;
			need hasType flag while (it.hasNext()) {
				cnt++;
				t = (Triple) it.next();
				m.union(new StmtMask(t));
			}
			if (m.hasSPOT() && (cnt == 1))
				m.setIsStmt();

			inUse[0] = true;
			cache[0] = new ReifCache(stmtURI, m, cnt);
			return cache[0];
*/
		}

		protected Triple fragToTriple(Triple t, StmtMask s) {
			Triple res;
			Node_URI n = (Node_URI) t.getSubject();
			if (s.hasProp())
				return new Triple(n, t.getPredicate(), Node.ANY);
			else if (s.hasObj())
				return new Triple(n, Node.ANY, t.getObject());
			else
				return new Triple(n, Node.ANY, Node.ANY);
		}

	}

	class ReifCache {
	
			protected Node_URI stmtURI;
			protected StmtMask mask;
			protected int tripleCnt;
		
			ReifCache( Node_URI s, StmtMask m, int cnt )
				{ stmtURI = s; mask = m; tripleCnt = cnt; }
		
			public StmtMask getStmtMask() { return mask; }
			public int getCnt() { return tripleCnt; }
			public Node_URI getStmtURI() { return stmtURI; }
			public void setMask ( StmtMask m ) { mask = m; }
			public void setCnt ( int cnt ) { tripleCnt = cnt; }
			public void incCnt ( int cnt ) { tripleCnt++; }
			public void decCnt ( int cnt ) { tripleCnt--; }
			

	}

	static boolean isReifProp ( Node_URI p ) {
		return p.equals(Reifier.subject) ||
			p.equals(Reifier.predicate)||
			p.equals(Reifier.object) || 
			p.equals(Reifier.type);			
	}
				
	class StmtMask {
		
			protected int mask;
				
			public static final int HasSubj = 1;
			public static final int HasProp = 2;
			public static final int HasObj = 4;
			public static final int HasType = 8;
			public static final int HasSPOT = 15;
			public static final int IsStmt = 16;
			public static final int HasNada = 0;
		
			public boolean hasSubj () { return (mask ^ HasSubj) == HasSubj; };
			public boolean hasProp () { return (mask ^ HasProp) == HasProp; };
			public boolean hasObj () { return (mask ^ HasObj) == HasObj; };
			public boolean hasType () { return (mask ^ HasType) == HasType; };
			public boolean hasSPOT () { return (mask ^ HasSPOT) == HasSPOT; };
			public boolean isStmt () { return (mask ^ IsStmt) == IsStmt; };
			public boolean hasNada () { return mask == HasNada; };
				
			// note: have SPOT does not imply a reification since
			// 1) there may be multiple fragments for prop, obj
			// 2) the fragments may be in multiple tuples
		
			StmtMask ( Triple t ) {
				mask = HasNada;
				Node_URI p = (Node_URI) t.getPredicate();
				if ( p != null ) {
					if ( p.equals(Reifier.subject) ) mask = HasSubj;
					else if ( p.equals(Reifier.predicate) ) mask = HasProp; 
					else if ( p.equals(Reifier.object) ) mask = HasObj; 
					else if ( p.equals(Reifier.type) ) {
							Node_URI o = (Node_URI) t.getObject();
							if ( o.equals(Reifier.Statement) ) mask = HasType;
					}
				}			
			}
		
			StmtMask () { mask = HasNada; };
		
			public void setUnion ( StmtMask m ) {
				mask |= m.mask;	
			}
				
			public void setIsStmt ( StmtMask m ) {
				mask |= IsStmt;	
			}
		
			public boolean hasIntersect ( StmtMask m ) {
				return (mask & m.mask) != 0;	
			}
		
			public boolean hasDifference ( StmtMask m ) {
				return (mask ^ m.mask) != 0;	
			}

	}

}

	/*
	 *  (c) Copyright Hewlett-Packard Company 2003
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
