/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */
 
package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

abstract class AbsChecker implements Constants {

	final boolean wantLite;
	int monotoneLevel = Levels.Lite;
	AbsChecker(boolean lite, GraphMaker gf) {
	//	super(gf.createGraph(), personality);
		hasBeenChecked = gf.getGraph();
		wantLite = lite;
	}

	Graph hasBeenChecked; // This is a subgraph of the input triples
	// it can be extended to an OWL Lite/DL graph.

	final boolean add(Triple t, boolean topLevelCall) {
		return addX(t, topLevelCall) != 0;
	}

	Map nodeInfo = new HashMap();
	CNodeI getCNode(Node n) {
		CNodeI rslt = (CNodeI)nodeInfo.get(n);
		if ( rslt == null ) {
			rslt = CNode.create(n, this );
			nodeInfo.put(n,rslt);
		}
		return rslt;
	}
	abstract boolean extraInfo();
	/**0 on failure, 1 on trivial, 2 on refinement.
		 * @param topLevelCall True if t has not already been checked, false if t is being rechecked, as a result of some other changes
		 * @param t A triple from a graph being checked.
		 * @return 0 on failure, 1 on trivial, 2 on refinement
		 */
	final int addX(Triple t, boolean topLevelCall) {
		//		System.err.println("+ " + t.toString() + (topLevelCall?" !":""));
		CNodeI s = getCNode(t.getSubject());
		CNodeI p = getCNode(t.getPredicate());
		CNodeI o = getCNode(t.getObject());
		boolean success = true;
		int s0 = -1;
		int p0 = -1;
		int o0 = -1;
		int s1 = s.getCategories();
		int p1 = p.getCategories();
		int o1 = o.getCategories();
		int sOrig = s1;
		int pOrig = p1;
		int oOrig = o1;
		int key = Failure;

		if (!topLevelCall) {
			// On recursive calls this triple is already in the hasBeenCheckedSet
			// we keep it on the call stack in order to reduce
			// the amount of checking we have to do.
			hasBeenChecked.delete(t);
		}
		while (success) {
			if (s1 == s0 && p1 == p0 && o1 == o0)
				break; // the exit point for success
			/*
			System.err.println("s:" + s0 + " -> " + s1 + "\n" +
			              " p:" + p0 + " -> " + p1 + "\n" +
			              " o:" + o0 + " -> " + o1); */
			s0 = s1; // record these values, exit when stable
			p0 = p1;
			o0 = o1;
//			key = SubCategorize.refineTriple(s0, p0, o0);
			key = LookupTable.qrefine(s0,p0,o0);
			if (key == Failure) {
				addProblem(Levels.DL, t);
				success = false;
			} else {
				if (LookupTable.dl(key)) {
					if (wantLite) {
						success = false;
						addProblem(Levels.Lite, t);
					} else {
						setMonotoneLevel(Levels.DL);
					}
				}
				o.setCategories(LookupTable.object(key), false);
				p.setCategories(LookupTable.prop(key), false);
				s.setCategories(LookupTable.subject(key), false);
				success =
					success
						&& o.update()
						&& p.update()
						&& s.update();
			}
			s1 = s.getCategories();
			p1 = p.getCategories();
			o1 = o.getCategories();
		}
		if (success) {
			if (!LookupTable.removeTriple(key)) 
			     hasBeenChecked.add(t);
			else {
//				System.err.println("D" + dCnt++);
//				dump(s0,p0,o0);
//				dump(t);
			}
			actions(key, s, o, t);
		} else {
			if (!topLevelCall)
				hasBeenChecked.add(t);
			s.setCategories(sOrig, false);
			p.setCategories(pOrig, false);
			o.setCategories(oOrig, false);
		}
		if ( success && p1 == Grammar.owldisjointWith) {
			if ( s.equals(o)) {
				addProblem(Levels.DL, t);
				success = false;
			}
			else
			   s.addDisjoint(o);
		}
		int rr;
		if (!success) {
			setMonotoneLevel(wantLite ? Levels.DL : Levels.Full);
			rr = 0;
		} else if (s1 == sOrig && p1 == pOrig && o1 == oOrig)
			rr = 1;
		else
			rr = 2;
		//	System.err.println("* " + t.toString() + "[" + rr + "]");
		return rr;
	}

//	void dump(Triple t) {
//		dump(t.getSubject());
//		dump(t.getPredicate());
//		dump(t.getObject());
//	}
	static int dCnt = 0;
	void setMonotoneLevel(int l) {
		if (monotoneLevel < l)
			monotoneLevel = l;
	}
	abstract void actions(int key, CNodeI s, CNodeI o, Triple t);
	
//	Map dumpSupport = new HashMap();
//	
//	void dump(Node n){
//		Integer ix = (Integer)dumpSupport.get(n);
//		if ( ix==null) {
//			ix = new Integer(dumpSupport.size());
//			dumpSupport.put(n,ix);
//		}
//		System.err.print(ix + ": ");
//		System.err.println(CategorySet.catString(getCNode(n).getCategories()));
//	}
//	void dump(int s,int p, int o) {
//		dump("S",s);
//		dump("P",p);
//		dump("O",o);
//		System.err.println();
//	}
//	void dump(String p,int c) {
//		System.err.println(p+CategorySet.catString(c));
//	}
//
	boolean recursivelyUpdate(Node n) {
		return rec(n, null, null) && rec(null, n, null) && rec(null, null, n);
	}
	//static int call = 0;
	private boolean rec(Node s, Node p, Node o) {
		boolean rslt = true;
		//int i =0;
		//int n = hasBeenChecked.size();
		//int c = call++;
		ClosableIterator it =
			new EarlyBindingIterator(hasBeenChecked.find(s, p, o));
		while (rslt && it.hasNext()) {
			//	System.err.println("[" + c +"]rec " + i++ + " of " + n);
			rslt = add((Triple) it.next(), false);
		}
		//System.err.println("[" + c +"]rec done " + n);

		it.close();
		return rslt;
	}
	

	void addProblem(int lvl, Triple t) {
		setMonotoneLevel(lvl + 1);
	}

	void addProblem(SyntaxProblem sp) {
		setMonotoneLevel(sp.level + 1);
	}
	/**
	 * @param node
	 * @param node2
	 */
	void addDisjoint(Node node, Node node2) {
		// do nothing
		
	}

}


/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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
