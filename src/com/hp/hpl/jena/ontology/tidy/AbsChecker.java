package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.enhanced.GraphPersonality;
import com.hp.hpl.jena.enhanced.Personality;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;

abstract class AbsChecker extends EnhGraph {

	final boolean wantLite;
	int monotoneLevel = Levels.Lite;
	AbsChecker(boolean lite, GraphMaker gf) {
		super(gf.getGraph(), personality);
		hasBeenChecked = gf.getGraph();
		wantLite = lite;
	}
	static Personality personality =
		new GraphPersonality()
			.add(CNodeI.class, CNode.factory)
			.add(Blank.class, CBlank.factory)
			.add(One.class, OneImpl.factory)
			.add(Two.class, TwoImpl.factory);

	Graph hasBeenChecked; // This is a subgraph of the input triples
	// it can be extended to an OWL Lite/DL graph.

	final boolean add(Triple t, boolean topLevelCall) {
		return addX(t, topLevelCall) != 0;
	}
	/**0 on failure, 1 on trivial, 2 on refinement.
		 * @param topLevelCall True if t has not already been checked, false if t is being rechecked, as a result of some other changes
		 * @param t A triple from a graph being checked.
		 * @return 0 on failure, 1 on trivial, 2 on refinement
		 */
	final int addX(Triple t, boolean topLevelCall) {
		//		System.err.println("+ " + t.toString() + (topLevelCall?" !":""));
		CNodeI s = (CNodeI) getNodeAs(t.getSubject(), CNodeI.class);
		CNodeI p = (CNodeI) getNodeAs(t.getPredicate(), CNodeI.class);
		CNodeI o = (CNodeI) getNodeAs(t.getObject(), CNodeI.class);
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
		long key = Grammar.Failure;

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
			key = SubCategorize.refineTriple(s0, p0, o0);
			if (key == Grammar.Failure) {
				addProblem(Levels.DL, t);
				success = false;
			} else {
				if (SubCategorize.dl(key)) {
					if (wantLite) {
						success = false;
						addProblem(Levels.Lite, t);
					} else {
						setMonotoneLevel(Levels.DL);
					}
				}
				success =
					success
						&& o.setCategories(SubCategorize.object(key, o0), true)
						&& p.setCategories(SubCategorize.prop(key, p0), true)
						&& s.setCategories(SubCategorize.subject(key, s0), true);
			}
			s1 = s.getCategories();
			p1 = p.getCategories();
			o1 = o.getCategories();
		}
		if (success) {
			hasBeenChecked.add(t);
			actions(key, s, o, t);
		} else {
			if (!topLevelCall)
				hasBeenChecked.add(t);
			s.setCategories(sOrig, false);
			p.setCategories(pOrig, false);
			o.setCategories(oOrig, false);
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
	void setMonotoneLevel(int l) {
		if (monotoneLevel < l)
			monotoneLevel = l;
	}
	abstract void actions(long key, CNodeI s, CNodeI o, Triple t);

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
	CNodeI getCNode(Node n) {
		return (CNodeI) getNodeAs(n, CNodeI.class);
	}

	void addProblem(int lvl, Triple t) {
		setMonotoneLevel(lvl + 1);
	}

	void addProblem(SyntaxProblem sp) {
		setMonotoneLevel(sp.level + 1);
	}

}
