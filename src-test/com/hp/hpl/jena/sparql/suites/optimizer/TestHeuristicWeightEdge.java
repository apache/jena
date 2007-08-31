/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternGraph;
import com.hp.hpl.jena.sparql.engine.optimizer.core.ConnectedGraph;
import com.hp.hpl.jena.sparql.engine.optimizer.core.GraphEdge;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.ProbabilityFactory;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.query.ARQ;


/**
 * Test if the estimated weight for an edge given a 
 * heuristic corresponds to the expected value specified
 * within the test case. Note that for the test cases
 * the property :edge is allowed to contain just two
 * triple pattern which have to be joined by at least
 * one common variable in order to define an edge.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class TestHeuristicWeightEdge extends TestCase
{
	private String heuristic ;
	private BasicPattern pattern ;
	private double weight ;
	private static Model testsM, graphM ;
	private static Context context = ARQ.getContext() ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	private static final String testCaseFileName = "testing/Optimizer/TestHeuristicWeightEdge-manifest.n3" ;

	public TestHeuristicWeightEdge(String title, String heuristic, BasicPattern pattern, double weight)
	{		
		super(title) ;
		
		this.heuristic = heuristic ;
		this.pattern = pattern ;
		this.weight = weight ;
	}
	
	public static void oneTimeSetUp()
	{
		context.set(Constants.PF, 
					ProbabilityFactory.createDefaultModel(Util.readModel(testDataFileName), null)) ;
	}
	
	public static void oneTimeTearDown()
	{
		graphM.close() ;
		testsM.close() ;
	}
	
	// Run the dynamically loaded test cases
	public void runTest()
	{	
		BasicPatternGraph graph = new BasicPatternGraph(pattern, Util.getHeuristic(heuristic, context, graphM.getGraph())) ;
		ConnectedGraph component = graph.getComponent(0) ;
		
		// The BGP has no connected graph defined
		if (component == null)
			return ;
		
		List edges = component.getEdges() ;
		
		// There are no edges defined
		if (edges.size() == 0)
			return ;
		
		// Extract the edge from the graph (there is only one edge specified for each test case)
		GraphEdge edge = (GraphEdge)edges.iterator().next() ;
	    
		// Test if the node weight equals to the expected weight specified within the test
		assertTrue(edge.weight() == weight) ;
	}
	
	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestHeuristicWeightEdge") ;
	    testsM = Util.readModel(testCaseFileName) ;
	    graphM = Util.readModel(testDataFileName) ;
        
        QueryExecution qe = QueryExecutionFactory.create(queryTestCases(), testsM);
        
		try 
		{
			ResultSet rs = qe.execSelect() ;
			
			while (rs.hasNext()) 
			{
				QuerySolution solution = rs.nextSolution() ;
				
				String title = solution.getLiteral("title").getLexicalForm() ;
				String heuristic = solution.getLiteral("heuristic").getLexicalForm() ;
				Seq edgeR = testsM.getSeq(solution.getResource("edge")) ;
				double weight = solution.getLiteral("weight").getDouble() ;
				BasicPattern pattern = getBasicPattern(edgeR) ;
				
				// Add a new test to the test suite
				ts.addTest(new TestHeuristicWeightEdge(title, heuristic, pattern, weight)) ;
			}
		} finally { 
			qe.close() ; 
		}
		
		 // Wrapper for the test suite including the test cases which executes the setup only once
		TestSetup wrapper = new TestSetup(ts) 
		{
			protected void setUp() 
			{
				oneTimeSetUp();
			}

			protected void tearDown() 
			{
				oneTimeTearDown();
			}
		};
		
		return wrapper ;
    }
	
	// Given the basicPattern RDF node (blank node), create a BasicPattern object of the triples
	private static BasicPattern getBasicPattern(Seq edgeR)
	{
		BasicPattern pattern = new BasicPattern() ;
		
		for (Iterator iter = edgeR.iterator(); iter.hasNext(); )
		{
			String[] triple = ((Literal)iter.next()).getLexicalForm().split(" ") ;
			// Create a BasicPattern from the string representation of the node
			pattern.add(new Triple(Util.createNode(triple[0]), 
								   Util.createNode(triple[1]), 
								   Util.createNode(triple[2]))) ;
		}
		
		return pattern ;
	}
	
	// The query to retrieve the test cases
	private static String queryTestCases()
	{		
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		   	   "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
		   	   "PREFIX : <" + Util.TEST_NS + ">" +
		   	   "SELECT ?title ?heuristic ?edge ?weight " +
		   	   "WHERE {" +
		   	   "?tc rdf:type :TestCase ." +
		   	   "?tc dc:title ?title ." +
		   	   "?tc :heuristic ?heuristic ." +
		   	   "?tc :edge ?edge ." +
		   	   "?tc :weight ?weight ." +
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