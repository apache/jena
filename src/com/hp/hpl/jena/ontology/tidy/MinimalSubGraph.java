/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: MinimalSubGraph.java,v 1.3 2003-04-19 03:37:11 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.graph.Triple;
import java.util.*;

/**
 * This class breaks the invariant of its superclass.
 * The only things that are safe to call are:
 * the constructor and {@link #getContradiction()}.
 * @author jjc
 *
 */
class MinimalSubGraph extends AbsChecker {
	private final Checker parent;
	private final Set todo = new HashSet();
	private final Set done = new HashSet();
	/**
	 * @param triple  triple U context is not lite/DL valid
	 * @param context A lite/DL valid graph
	 * @param lite
	 */
	MinimalSubGraph(boolean lite, Triple problem, Checker parent) {
		super(lite, new DefaultGraphFactory());
		this.parent = parent;
		if (!add(problem, false)) {
			// Break superclass invariant - only method that can be called is
			// getContradicition()
			hasBeenChecked.add(problem);
		} else {
			todo(problem);
			extend();
		}
	}

	Graph getContradiction() {
		return hasBeenChecked;
	}

	private void todo(Triple t) {
		todo(t.getSubject());
		todo(t.getObject());
		todo(t.getPredicate());
	}
	private void todo(Node n) {
		if (!done.contains(n))
			todo.add(n);
	}
	// pre-condition hasBeenChecked union ctxt is a contradicition
	// pre-condition ctxt is reachable from problem
	// post-condition hasBeenChecked is a contradiction
	private void extend() {
		while (true) {
			// todo cannot be empty, because if it were
			// we would have found a contradiction.
			Node n = (Node) todo.iterator().next();
			todo.remove(n);
			done.add(n);
			if (unfinished(n)) {
				if (extend(n))
					return;
			}
		}
	}

	// return true if hasBeenChecked is a contradiction
	private boolean extend(Node n) {
		return extend(n, null, null, n)
			|| extend(null, n, null, n)
			|| extend(null, null, n, n);
	}
	private boolean unfinished(Node n) {
		CNodeI inp = parent.getCNode(n);
		CNodeI here = getCNode(n);
		return inp.getCategories() != here.getCategories();
	}
	private boolean nontrivialReally(Node qu, Node rslt) {
		return qu != null && unfinished(rslt);
	}
	// consider adding matching triples, return true on a contradiction
	private boolean extend(Node s, Node p, Node o, Node k) {
		Iterator it = parent.hasBeenChecked.find(s, p, o);
		while (it.hasNext()) {
			Triple t = (Triple) it.next();
			int r = addX(t, true);
			switch (r) {
				case 0 :
				    hasBeenChecked.add(t);
					return true;
				case 1 :
					if (// if the only unfinished node is
					// the one we started with then this is not interesting
					 (!(nontrivialReally(s,t.getSubject())
						|| nontrivialReally(p, t.getPredicate())
						|| nontrivialReally(o, t.getObject())))
						|| // Moreover, if the one we started with is now finished, it wasn't
					// this time, and so we can wait for the other nodes to get back
					// to us.
					 (
							!unfinished(k))) {
						hasBeenChecked.delete(t);
						break;
					}
					// fall through
				case 2 :
					todo(t);
					break;
			}

		}
		return false;

	}

	// The remaining methods are no-ops.
	// These allow the sharing of AbsChecker between this class
	// and the Checker class.
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.AbsChecker#actions(long, com.hp.hpl.jena.ontology.tidy.CNodeI, com.hp.hpl.jena.ontology.tidy.CNodeI, com.hp.hpl.jena.graph.Triple)
	 */
	void actions(long key, CNodeI s, CNodeI o, Triple t) {
		// nothing
	}



}

/*
	(c) Copyright Hewlett-Packard Company 2003
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
