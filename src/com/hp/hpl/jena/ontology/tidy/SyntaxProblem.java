/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SyntaxProblem.java,v 1.11 2004-12-06 13:50:15 andy_seaborne Exp $
*/
package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import java.util.*;
import java.io.*;

/**
 * This class encapsulates some problem found during syntax checking.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class SyntaxProblem {
	static Model emptyModel = ModelFactory.createDefaultModel();
	private Graph pgraph;
	private EnhNode pnode;
	protected final int level;
	private String shortDescription;

	static EnhNode inEmptyModel(Node n) {
		return ((EnhGraph)emptyModel).getNodeAs(n,RDFNode.class);
	}
	/**
	 * A syntax problem with only a problemNode().
	 * @param shortD Short description
	 * @param en The problem node (or null)
	 * @param g The problem graph (or null)
	 * @param lvl The highest level at which this is illegal.
	 */
	private SyntaxProblem( String shortDesc,
	               EnhNode en,
	               Graph g,
	               int lvl
	             ) {
	     if ( en == null && g == null )
	        throw new BrokenException("Logic error in OWL Syntax Checker");
	     shortDescription = shortDesc;
	     pnode = en;
	     pgraph = g;
	     level = lvl;
	}
	/**
	 * A syntax problem with only a problemNode().
	 * @param shortD Short description
	 * @param en The problem node
	 * @param lvl The highest level at which this is illegal.
	 */
	protected SyntaxProblem(String shortD, EnhNode en, int lvl) {
		this(shortD, en, null, lvl );
	}	
	/**
	* A syntax problem with only a problemNode().
	* @param shortD Short description
	* @param n The problem node
	* @param lvl The highest level at which this is illegal.
	*/
   protected SyntaxProblem(String shortD, Node n, int lvl) {
	   this(shortD, inEmptyModel(n), null, lvl );
   }
	/**
	 * A SyntaxProblem with only a problemSubGraph().
	 * @param shortD Short description
	 * @param g The problem graph
	 * @param lvl The highest level at which this is illegal.
	 */
	protected SyntaxProblem(String shortD, Graph g, int lvl) {
		this(shortD, null, g, lvl);
	}

	/**
	 * A syntax problem with only a problemNode().
	 * @param shortD Short description
	 * @param n The problem node
	 * @param g Use me to form the subgraph of all triples involving n
	 * @param lvl The highest level at which this is illegal.
	 */
	SyntaxProblem nodeInGraph(String shortD,Node n,Graph g, int lvl) {
		Model m = ModelFactory.createDefaultModel();
		Graph ng = m.getGraph();
		addAll(ng,g,n,null,null);
		addAll(ng,g,null,n,null);
		addAll(ng,g,null,null,n);
		return new SyntaxProblem(shortD,((EnhGraph)m).getNodeAs(n,RDFNode.class),lvl);
	}
	
	static private void addAll(Graph addToMe, Graph fromHere,
	   Node s, Node p, Node o) {
	   	Iterator it = fromHere.find(s,p,o);
	   	while (it.hasNext()) {
	   		addToMe.add((Triple)it.next());
	   	}
	   }
	static private RDFWriter defaultWriter = new NTripleWriter();
	private RDFWriter wtr = defaultWriter;
	/**
	 * Sets the writer used for creation of the 
	 * {@link #longDescription()}.
	 * @param w
	 * @return the old writer
	 */
	public RDFWriter setWriter(RDFWriter w){
		RDFWriter old = wtr;
		wtr = w;
		return old;
	}
	/**
	 * Everything you ever wanted to know about this 
	 * problem.
	 * @return An exhaustive description of the problem.
	 */
	public String longDescription() {
		StringWriter ww = new StringWriter();
		ww.write("Not in OWL " + Levels.toString(level) + ": "
		  + shortDescription + "\n");
		Model m;
		if ( pnode != null ) {
			Node n = pnode.asNode();
			ww.write("Concerning ");
			if ( n.isBlank() )
			  ww.write("blank node");
			else if ( n.isURI() )
			  ww.write( "<" + n.getURI() + ">");
			else {
				LiteralLabel ll = n.getLiteral();
				String lang = ll.language();
				String dt = ll.getDatatypeURI();
				ww.write("\"" + ll.toString() + "\"" +
				  ( ( lang != null && lang.length() >0 ) 
				     ?( "@" + lang ) 
				     : "" ) +
				   ( dt == null ? "" :
				     ("^^<"+dt +">")));
				   
			}
			m = (Model)pnode.getGraph();
			if ( m == emptyModel )
			  ww.write("\n");
			else
 			   ww.write(" in:\n");
		} else {
			ww.write("Concerning sub-graph:\n");
			m = ModelFactory.createModelForGraph(pgraph);
		}
		wtr.write(m,ww,"never:never.never");
		return ww.toString();
	}
	/**
	 * An orphan or some other node that is at
	 * the centre of this problem.
	 * The associated {@link EnhGraph} is always a 
	 * {@link Model}, and is intended to be small
	 * but complete (e.g. all triples involving an
	 * orphan node).
	 * @return A problem node or null
	 */
	public EnhNode problemNode() {
		return pnode;
	}
	/**
	 * A line or two characterising this 
	 * problem.
	 * @return A short description of the problem.
	 */
	public String shortDescription() {
		return shortDescription;
	}
	/**
	 * A (hopefully small) subgraph exhibiting the problem.
	 * At least one of problemSubGraph and 
	 * {@link #problemNode}
	 * is non-null. If both are non-null then the problemSubGraph()
	 * is the graph underlying the problemNode().
	 * @return the problem sub-graph
	 */
	public Graph problemSubGraph() {
		return pgraph;
	}
	/** The level associated with this problem.
	* @return One of the values in {@link Levels}.
	*/
	public int getLevel() {
		return level;
	}
	

}

/*
	(c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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