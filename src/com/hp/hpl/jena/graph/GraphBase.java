/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphBase.java,v 1.7 2003-04-15 11:47:10 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    GraphBase is an implementation of Graph that provides some convenient
    base functionality for Graph implementations.
<p>
    Subtypes of GraphBase must provide add(Triple), delete(Triple), 
    find(TripleMatch,TripleAction), and size(). GraphBase provides
    default implementations of the other methods, including the other
    finds (on top of that one), a simple-minded prepare, and contains.
<p>    
	@author kers
*/

public abstract class GraphBase implements Graph {

	public boolean dependsOn(Graph other) {
		return this == other;
	}

	/**
		@see com.hp.hpl.jena.graph.Graph#queryHandler
	*/

	public QueryHandler queryHandler() {
		return new SimpleQueryHandler(this);
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#add(Triple)
	 */
	public void add(Triple t) {
		throw new UnsupportedOperationException("GraphBase::add");
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#delete(Triple)
	 */
	public void delete(Triple t) {
		throw new UnsupportedOperationException("GraphBase::delete");
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#find(TripleMatch)
	 */
	public abstract ExtendedIterator find(TripleMatch m);

	/**
		contains( t ) - return true iff the triple t is in this graph
	*/
	public boolean contains(Triple t) {
		return contains( t.getSubject(), t.getPredicate(), t.getObject() );
	}

	/**
		contains( s, p, o ) - returns true iff the triple (s, p, o) is in this graph. 
	    [currently any of them may be null as a wildcard]. Boring implementation
	    in terms of `find`, which subclasses may [should] optimise.
	*/
	public boolean contains(Node s, Node p, Node o) {
		ClosableIterator it = find(s, p, o);
		try { return it.hasNext(); } finally { it.close(); }
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#find(Node, Node, Node)
	 */
	public ExtendedIterator find(Node s, Node p, Node o) {
		return find(new StandardTripleMatch(s, p, o));
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#close()
	 */
	public void close() {
	}
	
	protected Reifier reifier = null;
	
	public Reifier getReifier() {
		if (reifier == null) reifier = new SimpleReifier( this, false );
		return reifier;
	}
    
	/**
	 * @see com.hp.hpl.jena.graph.Graph#size()
	 */
	public int size() {
		throw new UnsupportedOperationException("GraphBase::size");
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#capabilities()
	 */
	public int capabilities() {
		return 0;
	}

	public boolean isIsomorphicWith(Graph g) {
		return g != null && GraphMatcher.equals(this, g);
	}

	/** for little graphs only ... */

	public String toString() {
		StringBuffer b = new StringBuffer("{");
		String gap = "";
		ClosableIterator it = find(null, null, null);
		while (it.hasNext()) {
			b.append(gap);
			gap = "; ";
			b.append(it.next());
		}
		b.append("}");
		return b.toString();
	}
    
    /**
        return a dynamic copy of G with full reification (ie captures
        inbound reification triples)
    */
    public static Graph withReification( Graph g )
        { return new ReifyingCaptureGraph( g ); }
        
}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
