/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Checker.java,v 1.41 2003-11-30 21:13:19 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.graph.compose.MultiUnion;

/**
 * 
 * This class implements the OWL Syntax Checker.
 * The basic mode of use, is to create a Checker
 * and to add one or more Models, OntModels or Graphs.
 * It will generally do the right thing, vis-a-vis
 * imports, and not duplicating imports processing 
 * already performed by an OntModel.
 * The three methods {@link getProblems()}
 * {@link getErrors()} and {@link getSubLanguage()}
 * can all be used repeatedly and at any point. They
 * report on what has been added so far.
 * When constructing a checker, you must choose whether
 * to record errors and problems concerning non-OWL Lite
 * constructs, or only concerning non-OWL DL constructs.
 * For either choice {@link #getSubLanguage()} functions
 * correctly (i.e. the grammar used is identical). However,
 * if the Checker has been constructed with the liteflag as false,
 * it is not possible to access a rationale for an ontology
 * being in OWL DL rather than OWL Lite.
 * 
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class Checker extends com.hp.hpl.jena.ontology.tidy.impl.CheckerImpl {
	/**
	 * A command-line syntax checker.
	 * First argument, URL of document to check,
	 * Optional second argument is "Lite", "DL" or "Full"
	 * and error messages will be generated if needed.
	 * @param argv
	 */
	static public void main(String argv[]) {
		
		Checker chk =
			new Checker(argv.length == 2 && argv[1].equalsIgnoreCase("Lite")
			
	);
		chk.load(argv[0]);
		//  System.err.println("g added.");
		String subLang = chk.getSubLanguage();
		System.out.println(subLang);

		if (argv.length > 1) {
			if (argv[1].equals(subLang))
				return;
			if (argv[1].equalsIgnoreCase("Full") || subLang.equals("Lite")) {
				System.err.println(
					"All constructs were in OWL " + subLang + ".");
				return;
			}
		}

		Iterator it = chk.getProblems();
		while (it.hasNext()) {
			SyntaxProblem sp = (SyntaxProblem) it.next();
			System.err.println(sp.longDescription());
		}

	}

/**
 * Create a new checker - indicate whether errors
 * are wanted for non-OWL Lite cnstructions
 * or only non-OWL DL constructions.
 * as the core grammar.
 * @param liteFlag If true 
 *     {@ link #getErrors()} and 
 *     {@link #getProblems()} will indicate any OWL DL or OWL Full construction.
 */
	public Checker(boolean liteFlag) {
		super(liteFlag);
	}
	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors found by the syntax checker.
	 * If the liteFlag was set in the constructor then
	 * all OWL DL and OWL Full constructs are errors.
	 * If the liteFlag was not set, then only OWL Full constructs
	 * are errors.
	 */
	public Iterator getErrors() {
		return super.getErrors();
	}
	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors or warnings found by the syntax checker.
	 */
	public Iterator getProblems() {
		return super.getProblems();
	}
	/**
	 * Include this graph in the check.
	 * Many graphs can be checked together.
	 * Does not process imports,
	 * and does not attempt to be clever at all (e.g. 
	no special treatment for inferred graphs, 
	it just processes the inferred triples as normal).
	 * @param g A graph to include in the check.
	 */
	public void rawAdd(Graph g) {
		super.rawAdd(g);
	}
	/**
	 * Include an ontology and its imports
	 * in the check.
	 * @param url Load the ontology from this URL.
	 */
	public void load(String url) {
		super.load(url);
	}
	/**
	 * Which subLanguage is this document in.
	 * @return "Lite", "DL" or "Full".
	 */
	public String getSubLanguage() {
		return super.getSubLanguage();
	}

	/**
	 * Adds the graph to the syntax check.
	 * Only considers the base triples of an inferred graph
	 * (if recognised as such), processes imports (guessing
	 * that any {@link MultiUnion} has in fact been created by 
	 * {@link OntModel}
	 * and contains the imports closure).
	 * @param m
	 */
	public void add(Graph g) {
		while (g instanceof InfGraph)
			g = ((InfGraph) g).getRawGraph();
		if (g instanceof MultiUnion) {
			// We guess that this is imports closed already.	
			rawAdd(g);
		} else {
			addGraphAndImports(g);
		}
	}

/**
 * Forces adding the imports closure of a graph.
 * @param g
 */
  public void addGraphAndImports(Graph g) {
  	rawAdd(importsClosure(g));
  }
	/**
	 * Adds the model to the syntax check.
	 * Only considers the base triples of an inferred model
	 * (if recognised as such), processes imports.
	 * @param m
	 */
	public void add(Model m) {
		add(m.getGraph());
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