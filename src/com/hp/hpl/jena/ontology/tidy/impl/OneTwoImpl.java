/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: OneTwoImpl.java,v 1.6 2005-02-21 12:08:33 andy_seaborne Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.tidy.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class OneTwoImpl extends CGeneral implements One, Two, Blank {
	// local *cache*, on cache miss must always go to graph.

	private Triple seen[] = new Triple[3];
  private boolean stripped = false;
	void getSeen(Triple a[]) {

			a[0] = seen[0];
			a[1] = seen[1];
			a[2] = seen[2];
	}
	void setSeen(Triple a[]) {
		seen[0] = a[0];
		seen[1] = a[1];
		seen[2] = a[2];

	}
	OneTwoImpl(Node n, AbsChecker g) {
		super(n, g);
		if (getCategories() == -1)
			setCategories(Grammar.blank, false);
	}

	public void first(Triple t) {
		check(0, t);
	}
	public void second(Triple t) {
		check(1, t);
	}
	static private String shortMsg[] =
		{
			"Illegal description/restriction/list/alldifferent structure",
			"Illegal restriction/list structure",
			"Node may not be object of multiple triples" };
	void check(int i, Triple t) {
		Triple old = get(i);
		if (old != null) {
			if (old.equals(t))
				return;
			Graph problem = ModelFactory.createDefaultModel().getGraph();
			problem.add(old);
			problem.add(t);
			//     getChecker().setMonotoneLevel(Levels.Full);
			getChecker().addProblem(
				new SyntaxProblemImpl(shortMsg[i], problem, Levels.DL));

		} else {

			seen[i] = t;

		}
	}
	Triple get(int i) {
		return seen[i];
	}

	boolean incomplete(int i) {
		for (int j = 0; j < i; j++)
			if (get(j) == null)
				return true;
		return false;
	}

	protected int cyclicState = Undefined;

	public void addObjectTriple(Triple t) {
		check(2, t);
	}

	protected int getCyclicState() {
		return this.cyclicState;
	}

	protected static final int Checking = 1;

	protected static final int Undefined = 0;

	protected static final int IsCyclic = 2;

	protected static final int NonCyclic = 3;

	protected void setCyclicState(int st) {
		cyclicState = st;
		checker.cyclicTouched.add(this);
	}

	public boolean incompleteOne() {
		return incomplete(1);
	}

	public boolean incompleteTwo() {
		return incomplete(2);
	}
	
	public void strip(boolean indiv){

		seen[0] = null; seen[1] = null;
	  if (!indiv) seen[2] = null;
	  stripped = true;
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Blank#stripped()
	 */
	public boolean stripped() {
		return stripped;
	}

}

/*
	(c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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