/*
   (c) Copyright 2003,2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Checker.java,v 1.49 2004-12-14 13:30:38 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

//import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.tidy.impl.CheckerImpl;

import com.hp.hpl.jena.ontology.*;


/**
 * 
 * This class implements the OWL Syntax Checker,
 * and is integrated with Jena Models, OntModels, and Graphs.
 * The basic mode of use, is to create a Checker
 * and to add one or more Models, OntModels or Graphs.
 * It tries to do the right thing, vis-a-vis
 * imports, without duplicating imports processing 
 * already performed by an {@link OntModel};
 * in particular, to use a specific 
 * {@link OntDocumentManager}
 * create an {@link OntModel} using that document
 * manager. Any imports that are processed by
 * this class, a private document manager is used,
 * constructed from the default profile
 * (with imports processing explicitly on).
 * The three methods {@link #getProblems()}
 * {@link #getErrors()} and {@link #getSubLanguage()}
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
public class Checker extends CheckerImpl implements CheckerResults {


/**
 * Create a new checker - indicate whether error reports
 * are wanted for non-OWL Lite constructions
 * or only non-OWL DL constructions.
 * @param liteFlag If true 
 *     {@link #getErrors()} and 
 *     {@link #getProblems()} will indicate any OWL DL or OWL Full construction.
 */
	public Checker(boolean liteFlag) {
		super(liteFlag);
	}


	/**
* Adds the graph, and definitely not its imports, to the syntax check.
	 * Many graphs can be checked together.
	 * Does not process imports,
	 * and does not attempt to be clever at all (e.g. 
	no special treatment for inferred graphs, 
	it just processes the inferred triples as normal).
	 * @param g The graph to be added.
	 */
	public void addRaw(Graph g) {
		super.addRaw(g);
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
	 * Include an ontology and its imports
	 * in the check.
	 * @param url Load the ontology from this URL.
	 * @param lang The language (RDF/XML, N3 or N-TRIPLE) in which the ontology is written.
	 */
	public void load(String url,String lang) {
		super.load(url,lang);
	}


	/**
	 * Adds the graph to the syntax check.
	 * Only considers the base triples of an inferred graph
	 * (if recognised as such), processes imports (guessing
	 * that any {@link MultiUnion} has in fact been created by 
	 * {@link com.hp.hpl.jena.ontology.OntModel}
	 * and contains the imports closure).
	 * @param g The Graph to be added.
	 */
	public void add(Graph g) {
		while (g instanceof InfGraph)
			g = ((InfGraph) g).getRawGraph();
		if (g instanceof MultiUnion) {
			// We guess that this is imports closed already.	
			addRaw(g);
		} else {
			addGraphAndImports(g);
		}
	}

/**
* Adds the graph, and definitely its imports, to the syntax check.
* If g is an inferred graph, the inferred triples are added (which is
* probably not what was desired).
 * @param g The Graph to be added.
 */
  public void addGraphAndImports(Graph g) {
  	addRaw(importsClosure(g));
  }
	/**
	 * Adds the model to the syntax check.
	 * Only considers the base triples of an inferred model
	 * (if recognised as such), along with any imports.
	 * <p>
	 * If the <code>Model</code> is an 
	 * {@link com.hp.hpl.jena.ontology.OntModel} created with the {@link ModelFactory}
 then the base graph with its imports (which have already
 been collected) are added. Import processing is not redone
 at this stage.
 <p>
 The behaviour is identical to that of {@link #add(Graph)};
 better control,if needed, is achieved through the use of 
 {@link #addGraphAndImports} and {@link #addRaw}.
	 * @param m The Model to be added.
	 */
	public void add(Model m) {
		add(m.getGraph());
	}


}
/*
   (c) Copyright 2003,2004 Hewlett-Packard Development Company, LP
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