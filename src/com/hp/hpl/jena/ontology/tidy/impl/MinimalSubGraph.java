/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: MinimalSubGraph.java,v 1.1 2003-11-28 07:46:59 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import java.util.*;
import com.hp.hpl.jena.shared.*;

/**
 * This class breaks the invariant of its superclass.
 * The only things that are safe to call are:
 * the constructor and {@link #getContradiction()}.
 * @author jjc
 *
 */
class MinimalSubGraph extends AbsChecker {
	static final int MAXDIST = 10000;
	private final Checker parent;
	private final Set todo = new HashSet();
	//private final Set done = new HashSet();
	private final Map allMinInfos = new HashMap();
	private final Set activeMinInfos = new HashSet();
	
	private int distance;
	void add(MinimalityInfo x) {
		allMinInfos.put(x.cn.asNode(),x);
	}
	/**
	 * @param triple  triple U context is not lite/DL valid
	 * @param context A lite/DL valid graph
	 * @param lite
	 */
	MinimalSubGraph(boolean lite, Triple problem, Checker parent) {
		super(lite, new SimpleGraphMaker());
		this.parent = parent;
		if (!add(problem, false)) {
			// Break superclass invariant - only method that can be called is
			// getContradicition()
			hasBeenChecked.add(problem);
		} else {
			if ( true ) 
			  return;
			hasBeenChecked.delete(problem);
			setDistance(problem.getSubject(),0);
			setDistance(problem.getPredicate(),0);
			setDistance(problem.getObject(),0);
			todo(problem);
			hasBeenChecked.add(problem);
			extend();
		}
	}

	Graph getContradiction() {
		return hasBeenChecked;
	}

	private void todo(Triple t) {
		if (!hasBeenChecked.contains(t))
			todo.add(t);
	}
	private void todo(Node s, Node p, Node o) {
		Iterator it = parent.hasBeenChecked.find(s,p,o);
		while ( it.hasNext() ) {
			Triple t = (Triple)it.next();
			todo(t);
		}
	}
	private void todo(Node n) {
		todo(n,Node.ANY,Node.ANY);
		todo(Node.ANY,n,Node.ANY);
		todo(Node.ANY,Node.ANY,n);
	}
	// pre-condition hasBeenChecked union ctxt is a contradicition
	// pre-condition ctxt is reachable from problem
	// post-condition hasBeenChecked is a contradiction
	private void extend() {
		while (true) {
			Triple bestTriple = null;
			boolean bestIsTrivial = true;
			Triple tryMe = null;
			int bestScore = -1;
			

			Iterator it2;
			MinimalityInfo mi;
			setMinInfos();
			// Foreach node in todo try extending it
			Iterator it = todo.iterator();
			while (it.hasNext()) {
				tryMe = (Triple)it.next();
				// Reset minInfos
				
				switch ( addX(tryMe, true) ) {
					case 0:
					  hasBeenChecked.add(tryMe);
					  return;
					case 1:
					 if (!bestIsTrivial) break;
					int sc = score();
					if ( sc > bestScore ) {
					  bestScore = sc;
					  bestTriple = tryMe;
					}
				  	break;
					case 2:
					  if (bestIsTrivial) {
					  	bestIsTrivial = false;
					  	bestScore = -1;
					  }
					  sc = score();
					  if ( sc > bestScore ) {
					  	bestScore = sc;
					  	bestTriple = tryMe;
					  }
					  break;
					default:
					  throw new BrokenException("Can't happen");
				}
				hasBeenChecked.delete(tryMe);
				reset(allMinInfos);
				
			}
			

			if ( bestTriple == null)
			 throw new BrokenException("no bestTriple");
			// Choose best and extend
			addX( bestTriple, true );
			todo.remove(bestTriple);

			distance = MAXDIST;
			todoAndDistance(bestTriple.getSubject());
			todoAndDistance(bestTriple.getPredicate());
			todoAndDistance(bestTriple.getObject());

			setDistance(bestTriple.getSubject(),distance);
			setDistance(bestTriple.getPredicate(),distance);
			setDistance(bestTriple.getObject(),distance);
			
			/*
			// todo cannot be empty, because if it were
			// we would have found a contradiction.
			Node n = (Node) todo.iterator().next();
		//	todo.remove(n);
		//	done.add(n);
			if (unfinished(n)) {
				if (extend(n))
					return;
			}
			*/
		}
	}
	private void setMinInfos() {
		Iterator it2 = allMinInfos.values().iterator();
		while (it2.hasNext()) {
			MinimalityInfo mi = (MinimalityInfo)it2.next();
			mi.oldCategory = mi.cn.getCategories();
			mi.cn.getSeen(mi.oldSeen);
		}
	}
	private void todoAndDistance(Node n) {
		activeMinInfos.add(allMinInfos.get(n));
		todo(n);
		int d = getDistance(n);
		if ( d>= 0 && d<distance) {
			distance = d;
		}
	}
	private void reset(Map m) {
		Iterator it2;
			it2 = m.values().iterator();
			while (it2.hasNext()) {
				MinimalityInfo mi = (MinimalityInfo)it2.next();
				mi.cn.setCategories(mi.oldCategory,false);
				mi.cn.setSeen(mi.oldSeen);
			}
	}

  int getDistance(Node n) {
  	MinimalityInfo mi = (MinimalityInfo)allMinInfos.get(n);
  	return mi.distance;
  }
  void setDistance(Node n, int d) {
	((MinimalityInfo)allMinInfos.get(n)).distance = d;
  }
  int score() {
  	int bestSc = 0;
  	Iterator it = activeMinInfos.iterator();
  	while (it.hasNext() ) {
  		MinimalityInfo mi = (MinimalityInfo)it.next();
  		int distScore = (MAXDIST - mi.distance)*MAXDIST;
  		if ( distScore + MAXDIST < bestSc)
  		  continue;
  		int newCatLength = CategorySet.getSet(mi.cn.getCategories()).length;
  		int oldCatLength = CategorySet.getSet(mi.oldCategory).length;
  		
  		//if ( newCatLength == oldCatLength )
  		//  continue;
  		if ( newCatLength > oldCatLength)
  		  throw new BrokenException("cat length problem");
  		distScore += oldCatLength - newCatLength;
  		
  		if ( distScore > bestSc)
  		  bestSc = distScore;
  		
  	}
  	return bestSc;
  	
  }

	// The remaining methods are no-ops.
	// These allow the sharing of AbsChecker between this class
	// and the Checker class.
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.AbsChecker#actions(long, com.hp.hpl.jena.ontology.tidy.CNodeI, com.hp.hpl.jena.ontology.tidy.CNodeI, com.hp.hpl.jena.graph.Triple)
	 */
	void actions(int key, CNodeI s, CNodeI o, Triple t) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.AbsChecker#extraInfo()
	 */
	boolean extraInfo() {
		return true;
	}



}

/*
	(c) Copyright 2003 Hewlett-Packard Development Company, LP
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
