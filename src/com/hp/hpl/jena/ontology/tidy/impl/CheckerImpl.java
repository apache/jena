/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.tidy.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/**
 * This class implements the OWL Syntax Checker from the 
 * <a href="http://www.w3.org/TR/2003/CR-owl-test-20030818/#dfn-OWL-syntax-checker">OWL Test Cases</a> Candidate Recommendation.
 * <h4>Implementation issues concerning
 * <a href="http://www.w3.org/TR/2003/CR-owl-semantics-20030818/">OWL Semantics
 * &amp; Abstract Syntax</a></h4>
 * <ul>
 * <li>S&AS does not seem to say that the object of an annotation triple
 * must be one of the IDs, it seems to allow an arbitrary URIref.
 * We require it to have some type, reflecting the <a href=
 *  "http://lists.w3.org/Archives/Public/www-webont-wg/2003Mar/0066.html" 
 * >decision on OWL DL
 * Syntax</a>.</li>
 * <pre>owl:imports rdf:type owl:OntologyProperty .
 * </pre> is permitted only if there is a triple with 
 * owl:imports as predicate.
 * This is implemented</li>
 * 
 * 
 * 
 * @author Jeremy J. Carroll
 *
 */
public class CheckerImpl extends AbsChecker {
	//	private GraphMaker gf;
	private Vector monotoneProblems = new Vector();
	private Vector warnings = new Vector();

	private Vector nonMonotoneProblems = null;
	private int nonMonotoneLevel;

	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors found by the syntax checker.
	 */
	public Iterator getErrors() {
		nonMonotoneLevel = Levels.Lite;
		if (monotoneProblems.size() > 0)
			return monotoneProblems.iterator();
		snapCheck();
		return nonMonotoneProblems.iterator();
	}

	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors or warnings found by the syntax checker.
	 */
	public Iterator getProblems() {
		return new ConcatenatedIterator(getErrors(), warnings.iterator());
	}
	protected void endBNode(Node n) {
    if (!useRemove)
      return;
		CNodeI info = getCNode(n);
		int c = info.getCategories();
		int f = CategorySet.flags[c];

		if (f != 0) {
			if ((f & CategorySet.STRUCT1) != 0) {
				if (getCNode(n).asOne().incompleteOne())
					bnProblem(
						"Incomplete blank owl:Class or owl:AllDifferent",
						n);
			}
			if ((f & CategorySet.STRUCT2) != 0) {
				if (getCNode(n).asTwo().incompleteTwo())
					bnProblem("Incomplete rdf:List or owl:Restriction", n);
			}
			if ((f & ~(CategorySet.STRUCT1 | CategorySet.STRUCT2)) != 0) {
				if ((f & CategorySet.UNTYPED) != 0) {
					bnProblem("Untyped node", n);
				}
				if ((f & CategorySet.ORPHAN) != 0) {
					bnProblem("Orphaned rdf:List node", n);
				}
				if ((f & CategorySet.DLORPHAN) != 0) {
					bnProblem(
						"Orphaned blank owl:Class or owl:Restriction is in OWL DL",
						n,
						Levels.Lite);
				}
			}
			info.asBlank().strip(
				Q.member(Grammar.unnamedIndividual, CategorySet.getSet(c)));
			// TODO more of this optimization			
		}
	}
	//static public int cyCnt = 0;
	synchronized private void snapCheck() {
		if (nonMonotoneProblems == null) {
			nonMonotoneProblems = new Vector();
			nonMonotoneLevel = Levels.Lite;
			//Model m = ModelFactory.createModelForGraph(asGraph());
			CheckerImpl m = this;
			cyclicTouched = new HashSet();
			Iterator it = nodeInfo.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry ent = (Map.Entry) it.next();
				Node n = (Node) ent.getKey();
				CNodeI info = (CNodeI) ent.getValue();
				int c = info.getCategories();
				int f = CategorySet.flags[c];
				boolean stripped = false;

				if (f == 0)
					continue;

				if (n.isBlank())
					stripped = info.asBlank().stripped();

				if (!stripped) {

					if ((f & CategorySet.STRUCT1) != 0) {
						if (getCNode(n).asOne().incompleteOne())
							nonMonProblem(
								"Incomplete blank owl:Class or owl:AllDifferent",
								n);
					}
					if ((f & CategorySet.STRUCT2) != 0) {
						if (getCNode(n).asTwo().incompleteTwo())
							nonMonProblem(
								"Incomplete rdf:List or owl:Restriction",
								n);
					}
					if ((f & ~(CategorySet.STRUCT1 | CategorySet.STRUCT2))
						== 0)
						continue;
					if ((f & CategorySet.UNTYPED) != 0) {
						nonMonProblem("Untyped node", n);
					}
					if ((f & CategorySet.ORPHAN) != 0) {
						nonMonProblem("Orphaned rdf:List node", n);
					}
					if ((f & CategorySet.DLORPHAN) != 0) {
						nonMonProblem(
							"Orphaned blank owl:Class or owl:Restriction is in OWL DL",
							n,
							Levels.Lite);
					}
				}
				if ((f & CategorySet.CYCLIC) != 0) {
					if (Q
						.intersect(Grammar.descriptionsX, CategorySet.getSet(c))
						|| Q.intersect(
							Grammar.restrictionsX,
							CategorySet.getSet(c))) {

						nonMonProblem(
							"Cyclic blank owl:Class or owl:Restriction",
							n);
					}

					if (Q.intersect(Grammar.listsX, CategorySet.getSet(c))) {
						//    System.err.println(CategorySet.catString(cn.getCategories()));
						//		dump(n);
						nonMonProblem("Cyclic rdf:List", n);

					}

					// If this is an individual then we have to check it.

					if (Q
						.member(
							Grammar.unnamedIndividual,
							CategorySet.getSet(c))) {
						isCyclic((OneTwoImpl) info, n);
					}
				}

			}

			clearCyclicState();

			checkDisjoint();

		}
	}

	private void checkDisjoint() {
		Iterator i = disjoints.keySet().iterator();
		while (i.hasNext()) {
			Node b = (Node) i.next();
			if (!b.isBlank())
				continue;

			Iterator j = ((Set) disjoints.get(b)).iterator();
			while (j.hasNext()) {
				Node a = (Node) j.next();
				Iterator k = ((Set) disjoints.get(b)).iterator();
				while (k.hasNext()) {
					Node c = (Node) k.next();
					// TODO improve owl:disjointWith error msg
					if (!(a.equals(c)
						|| ((Set) disjoints.get(a)).contains(c))) {
						nonMonProblem("Ill-formed owl:disjointWith", b);
					}
				}
			}
		}
	}
	private boolean isCyclic(OneTwoImpl blk, Node n) {
		int st = blk.getCyclicState();
		switch (st) {
			case OneTwoImpl.Checking :
				blk.setCyclicState(OneTwoImpl.IsCyclic);
				nonMonProblem("Cyclic unnamed individual", n);
			case OneTwoImpl.IsCyclic :
				return true;
			case OneTwoImpl.Undefined :
				blk.setCyclicState(OneTwoImpl.Checking);
				Triple t = blk.get(2);
				boolean rslt;
				if (t == null)
					rslt = false;
				else
					rslt = isCyclic((OneTwoImpl) getCNode(t.getSubject()), n);
				blk.setCyclicState(
					rslt ? OneTwoImpl.IsCyclic : OneTwoImpl.NonCyclic);
				return rslt;
			case OneTwoImpl.NonCyclic :
				return false;
			default :
				throw new BrokenException("Impossible case in switch.");
		}

	}

	private void clearCyclicState() {
		Iterator it = cyclicTouched.iterator();
		while (it.hasNext()) {
			((OneTwoImpl) it.next()).setCyclicState(OneTwoImpl.Undefined);
		}
	}
	private void nonMonProblem(String shortD, Node n) {
		nonMonProblem(shortD, n, Levels.DL);
	}
	private void nonMonProblem(String shortD, Node n, int lvl) {
		Model m = ModelFactory.createDefaultModel();
		Graph mg = m.getGraph();
		if (nonMonotoneLevel <= lvl)
			nonMonotoneLevel = lvl + 1;

		if (lvl == Levels.Lite && !wantLite)
			return;

		EnhNode enh = ((EnhGraph) m).getNodeAs(n, RDFNode.class);
		Iterator it = this.hasBeenChecked.find(n, null, null);
		while (it.hasNext())
			mg.add((Triple) it.next());

		this.nonMonotoneProblems.add(new SyntaxProblemImpl(shortD, enh, lvl));
	}
	private void bnProblem(String shortD, Node n) {
		bnProblem(shortD, n, Levels.DL);
	}
	private void bnProblem(String shortD, Node n, int lvl) {
		Model m = ModelFactory.createDefaultModel();
		Graph mg = m.getGraph();
		setMonotoneLevel(lvl + 1);

		if (lvl == Levels.Lite && !wantLite)
			return;

		EnhNode enh = ((EnhGraph) m).getNodeAs(n, RDFNode.class);
		Iterator it = this.hasBeenChecked.find(n, null, null);
		while (it.hasNext())
			mg.add((Triple) it.next());
		addProblem(new SyntaxProblemImpl(shortD, enh, lvl));
	}

	/**
	 * Construct a syntax checker.
	 * Will not explain why something is in DL rather than Lite.	 
	 * */
	public CheckerImpl() {
		this(false);
	}
	/**
	 * Construct a syntax checker.
	 * If Lite is true, {@link Checker#getProblems} will
	 * explain why the graph is in DL rather than Lite.
	 * @param lite
	 */
	public CheckerImpl(boolean lite) {
		super(lite);
	}
	/**
	 * Include this graph in the check.
	 * Many graphs can be checked together.
	 * Does not process imports.
	 * @param g A graph to include in the check.
	 */
	public void addRaw(Graph g) {
		// Add every triple
		ClosableIterator it = null;
		try {
			it = g.find(null, null, null);
			while (it.hasNext()) {
				add((Triple) it.next(), true);
			}
		} finally {
			if (it != null)
				it.close();
		}
	}
	/**
	 * Include an ontology and its imports
	 * in the check.
	 * @param url Load the ontology from this URL.
	 */
	public void load(String url) {
		// create an ontology model with no reasoner and the default doc manager
		OntModel m =
			ModelFactory.createOntologyModel(
				new OntModelSpec(null, null, null, ProfileRegistry.OWL_LANG),
				null);
		//OntModel m = ModelFactory.createOntologyModel();
		m.getDocumentManager().setProcessImports(true);

		m.read(url);

		// since we specified the null reasoner, the graph of the model is the union graph
		addRaw(m.getGraph());
	}
	//private boolean wantLite = true;

	void addProblem(int lvl, Triple t, String msg) {
		super.addProblem(lvl, t, msg);
		if (lvl == Levels.Lite && !wantLite)
			return;

		Graph min;

		if (justForErrorMessages == null) {
			min = Factory.createDefaultGraph(ReificationStyle.Minimal);
			min.add(t);
		} else {
			min =
				new MinimalSubGraph(lvl == Levels.Lite, t, this)
					.getContradiction();
		}
		addProblem(
			new SyntaxProblemImpl(
				msg + " Not a " + Levels.toString(lvl) + " subgraph",
				min,
				lvl));

	}
	void addProblem(SyntaxProblemImpl sp) {
		super.addProblem(sp);
		switch (sp.getLevel()) {
			case Levels.Warning :
				warnings.add(sp);
			case Levels.Lite :
				if (!wantLite)
					return;
			default :
				monotoneProblems.add(sp);

		}
	}
	/**
	 * Which subLanguage is this document in.
	 * @return "Lite", "DL" or "Full".
	 */
	public String getSubLanguage() {
		if (monotoneLevel < Levels.Full && monotoneProblems.size() == 0)
			snapCheck();
		int m =
			monotoneLevel < nonMonotoneLevel ? nonMonotoneLevel : monotoneLevel;
		if (wantLite && m == Levels.DL)
			return "DL or Full";
		return Levels.toString(m);
	}
	void actions(int key, CNodeI s, CNodeI o, Triple t) {
		if (look.tripleForObject(key))
			o.asBlank().addObjectTriple(t);
		if (look.tripleForSubject(key))
			s.asBlank().addObjectTriple(t);

		switch (look.action(key)) {
			case FirstOfOne :
				s.asOne().first(t);
				break;
			case FirstOfTwo :
				s.asTwo().first(t);
				break;
			case SecondOfTwo :
				s.asTwo().second(t);
				break;
		}

	}
	Map disjoints = new HashMap();

	void addDisjoint(Node a, Node b) {
		Set sa = (Set) disjoints.get(a);
		if (sa == null) {
			sa = new HashSet();
			disjoints.put(a, sa);
		}
		sa.add(b);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.AbsChecker#extraInfo()
	 */
	boolean extraInfo() {
		return false;
	}

	protected Graph importsClosure(Graph g) {
		// create an ontology model with no reasoner and the default doc manager
		OntModelSpec dullOWL =
			new OntModelSpec(null, null, null, ProfileRegistry.OWL_LANG);
		dullOWL.getDocumentManager().setProcessImports(true);
		OntModel m =
			ModelFactory.createOntologyModel(
				dullOWL,
				ModelFactory.createModelForGraph(g));

		// the ont model graph must be the union graph, since we specified the null reasoner (hence no inf graph)
		return m.getGraph();
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
