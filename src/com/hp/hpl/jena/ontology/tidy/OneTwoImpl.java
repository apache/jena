/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: OneTwoImpl.java,v 1.6 2003-04-18 10:45:28 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class OneTwoImpl extends CGeneral {
	// local *cache*, on cache miss must always go to graph.

	private Triple seen[] = new Triple[3];

	OneTwoImpl(Node n, EnhGraph g) {
		super(n, g);
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
			getChecker().addProblem(new SyntaxProblem(shortMsg[i], problem, Levels.DL));

		} else {

			Graph G = getGraph().asGraph();
			Reifier R = G.getReifier();
			Node n = Node.createAnon();
			R.reifyAs(n, t);
			getGraph().asGraph().add(
				new Triple(asNode(), gProp[i], n));
			seen[i] = t;

		}
	}
    static private Node gProp[] = {
    	Vocab.firstPart,
    	Vocab.secondPart,
    	Vocab.objectOfTriple
    };
	private Triple get(int i) {
		if (seen[i] == null) {
			Graph G = getGraph().asGraph();
			ClosableIterator it = G.find(asNode(), gProp[i], null);
			if (it.hasNext())
				seen[i] =
					G.getReifier().getTriple(((Triple) it.next()).getObject());
			it.close();
		}
		return seen[i];
	}

	boolean incomplete(int i) {
		for (int j = 0; j < i; j++)
			if (get(j) == null)
				return true;
		return false;
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