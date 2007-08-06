/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import java.util.*;

import junit.framework.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternGraph;
import com.hp.hpl.jena.sparql.engine.optimizer.core.ConnectedGraph;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.VariableCounting;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;


/**
 * This test case implements a number of BasicPattern tests, where 
 * each test consists of a check whether or not the number of components
 * of a BasicPattern is correct. A component of a BasicPattern is a 
 * BasicPattern with a subset of joined triple patterns.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class TestGraphBuild extends TestCase
{
	private BasicPattern pattern ;
	private List components ; // List<Map<String, Integer>>
	
	public TestGraphBuild(String title, BasicPattern pattern, List components)
	{		
		super(title) ;
		
		this.pattern = pattern ;
		this.components = components ; // List<Map<String, Integer>>
	}
	
	// Run the dynamically loaded test cases
	protected void runTest()
	{	
		BasicPatternGraph graph = new BasicPatternGraph(pattern, new VariableCounting()) ;
	    assertTrue(graph.numberOfConnectedComponents() == components.size()) ;
	    
	    for (int i = 0; i < components.size(); i++)
	    {	    	
	    	Integer nodes = (Integer)((Map)components.get(i)).get("nodes") ;
	    	Integer edges = (Integer)((Map)components.get(i)).get("edges") ;
	    	ConnectedGraph component = graph.getComponent(i) ;
	    	
	    	assertTrue(component.getNodes().size() == nodes.intValue()) ;
	    	assertTrue(component.getEdges().size() == edges.intValue()) ;
	    }
	}
	
	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestGraphBuild") ;
	    Model model = Util.readModel("testing/Optimizer/TestGraphBuild-manifest.n3") ;
        
        QueryExecution qe = QueryExecutionFactory.create(queryTestCases(), model);
        
		try 
		{
			ResultSet rs = qe.execSelect() ;
			
			while (rs.hasNext()) 
			{
				QuerySolution solution = rs.nextSolution() ;
				
				String title = solution.getLiteral("title").getLexicalForm() ;
				Seq patternR = model.getSeq(solution.getResource("pattern")) ;
				Seq graphR = model.getSeq(solution.getResource("graph")) ;
				// Extract the triples of the corresponding basic pattern for the test case
				BasicPattern basicPattern = getBasicPattern(patternR) ;
				// Extract the information about the number of nodes and edges of the graph for the test case
				List components = getGraphComponents(graphR) ; // List<Map<String, Integer>>
				
				// Add a new test to the test suite
				ts.addTest(new TestGraphBuild(title, basicPattern, components)) ;
			}
		} finally { 
			qe.close() ; 
		}
		
		model.close() ;

		return ts ;
    }
	
	// Given the basicPattern RDF node (blank node), create a BasicPattern object of the triples
	private static BasicPattern getBasicPattern(Seq patternR)
	{
		BasicPattern pattern = new BasicPattern() ;
		
		// Given a pattern resource, identify the triples
		for (Iterator iter = patternR.iterator(); iter.hasNext(); )
		{
			// Get the rdf:li (i.e. the triple pattern), split by space
			String[] triple = ((Literal)iter.next()).getLexicalForm().split(" ") ;
			// Create a BasicPattern (list of Triples) from the string representation of the BGP
			pattern.add(new Triple(Util.createNode(triple[0]), 
								   Util.createNode(triple[1]), 
								   Util.createNode(triple[2]))) ;
		}
		
		return pattern ;
	}
	
	// Given the graph RDF node (blank node) extract the corresponding components with nodes and edges
	private static List getGraphComponents(Seq graphR)
	{
		Property nodesP = ResourceFactory.createProperty(Util.TEST_NS + "nodes") ;
        Property edgesP = ResourceFactory.createProperty(Util.TEST_NS + "edges") ;
		List components = new ArrayList() ; // List<Map<String, Integer>>

		// Given a graph resource, we need to iterate over the components and extract the information about nodes and edges
		for (Iterator iter = graphR.iterator(); iter.hasNext(); )
		{
			Resource componentR = (Resource)iter.next() ;
			Map component = new HashMap() ; // Map<String, Integer>
			String nodes = componentR.getProperty(nodesP).getObject().asNode().getLiteralLexicalForm() ;
			String edges = componentR.getProperty(edgesP).getObject().asNode().getLiteralLexicalForm() ;
		
			// Create a component with nodes and edges information
			component.put("nodes", new Integer(nodes)) ;
			component.put("edges", new Integer(edges)) ;
			
			// Add the new component to the list of components for the graph
			components.add(component) ;
		}
		
		return components ;
	}
	
	// The query to retrieve the test cases with the basic pattern and the graph
	private static String queryTestCases()
	{
		/* The query with ARQ property function
		 * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		 * PREFIX dc: <http://purl.org/dc/elements/1.1/>
		 * PREFIX : <TEST_NS>
		 * PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>
		 * SELECT ?title ?basicPattern ?graph ?triple ?nodes ?edges
		 * WHERE {
		 * ?testCase rdf:type :TestCase .
		 * ?testCase dc:title ?title .
		 * ?testCase :basicPattern ?basicPattern .
		 * ?basicPattern apf:seq ?triple .
		 * ?testCase :graph ?graph .
		 * ?graph apf:seq ?component .
		 * ?component :nodes ?nodes .
		 * ?component :edges ?edges .
		 * }
		 * ORDER BY ASC(?title)
		 */
		
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		   	   "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
		   	   "PREFIX : <" + Util.TEST_NS + ">" +
		   	   "SELECT ?title ?pattern ?graph " +
		   	   "WHERE {" +
		   	   "?tc rdf:type :TestCase ." +
		   	   "?tc dc:title ?title ." +
		   	   "?tc :pattern ?pattern ." +
		   	   "?tc :graph ?graph ." +
		   	   "}" +
		   	   "ORDER BY ASC(?title)" ;
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