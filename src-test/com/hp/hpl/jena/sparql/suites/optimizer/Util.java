/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import java.io.InputStream;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.Heuristic;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicsRegistry;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;

/**
 * The class provides some common functionality required 
 * for the test classes, e.g. model loading.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class Util 
{
	public static final String TEST_NS = "http://www.w3.org/2006/03/test-description#" ;
	
	// Read the manifest file model with the test cases
	public static Model readModel(String file) 
	{
		Model model = ModelFactory.createDefaultModel() ;
		InputStream in = FileManager.get().open(file) ;
		if (in == null)
			throw new IllegalArgumentException("File: " + file + " not found") ;
		model.read(in, null, "N3") ;
		
		return model ;
	}
	
	/*
	 * Create a node from a string representation of a S/P/O 
	 * (e.g. ?s1 => Node.createVariable("s1"))
	 */
	public static Node createNode(String element)
	{
		if (StringUtils.contains(element, "?"))
			return Node.createVariable(element.substring(1, element.length())) ;
		else if (element.startsWith(":"))
			return Node.createURI(Constants.localhostNS + element.substring(1, element.length())) ;
		else if (element.startsWith("<"))
			return Node.createURI(element.substring(1, element.length() - 1)) ;
		else if (element.startsWith("http:"))
			return Node.createURI(element) ;
		else if (element.equals("ANY"))
			return Node.ANY ;
		else
			return Node.createLiteral(element) ;
	}
	
	// Return the heuristic instance corresponding to the heuristic defined by the test case
	public static Heuristic getHeuristic(String heuristic, Context context, Graph graph)
	{
		HeuristicsRegistry registry = new HeuristicsRegistry(context, graph) ;
		return registry.get(heuristic) ;
	}
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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