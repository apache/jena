/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import java.util.*;

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
import com.hp.hpl.jena.sparql.suites.optimizer.Util;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.Heuristic;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.query.ARQ;

/**
 * This test case allows testing of multiple heuristics 
 * to check whether the returned optimized BasicPattern 
 * is the same as defined by the test case.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class TestGraphOptimize extends TestCase
{
	private Heuristic heuristic ;
	private BasicPattern patternIn ;
	private BasicPattern patternOut ;

	
	public TestGraphOptimize(String title, Heuristic heuristic, BasicPattern patternIn, BasicPattern patternOut)
	{		
		super(title) ;
		
		this.heuristic = heuristic ;
		this.patternIn = patternIn ;
		this.patternOut = patternOut ;
	}
	
	// Run the dynamically loaded test cases
	public void runTest()
	{	
		BasicPatternGraph graph = new BasicPatternGraph(patternIn, heuristic) ;
		BasicPattern patternOpt = graph.optimize() ;
	    
		// Test if the optimized pattern equals to the out pattern specified by the test
		assertTrue(patternOpt.equals(patternOut)) ;
	}
	
	// Build the test suite
	public static Test suite()
    {
		Context cxt = ARQ.getContext() ;
        TestSuite ts = new TestSuite("TestGraphOptimize") ;
	    Model testsM = Util.readModel("testing/Optimizer/TestGraphOptimize-manifest.n3") ;
	    Model graphM = Util.readModel("testing/Optimizer/Test-data.n3") ;
        
        QueryExecution qe = QueryExecutionFactory.create(queryTestCases(), testsM);
        
		try 
		{
			ResultSet rs = qe.execSelect() ;
			
			while (rs.hasNext()) 
			{
				QuerySolution solution = rs.nextSolution() ;
				
				String title = solution.getLiteral("title").getLexicalForm() ;
				Heuristic heuristic = Util.getHeuristic(solution.getLiteral("heuristic").getLexicalForm(), cxt, graphM.getGraph()) ;
				Seq patternInR = testsM.getSeq(solution.getResource("patternIn")) ;
				Seq patternOutR = testsM.getSeq(solution.getResource("patternOut")) ;
				BasicPattern patternIn = getBasicPattern(patternInR) ;
				BasicPattern patternOut = getBasicPattern(patternOutR) ;
				
				// Add a new test to the test suite
				ts.addTest(new TestGraphOptimize(title, heuristic, patternIn, patternOut)) ;
			}
		} finally { 
			qe.close() ; 
		}
		
		testsM.close() ;

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
	
	// The query to retrieve the test cases with the basic pattern and the graph
	private static String queryTestCases()
	{		
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		   	   "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
		   	   "PREFIX : <" + Util.TEST_NS + ">" +
		   	   "SELECT ?title ?heuristic ?patternIn ?patternOut ?qpi ?sei " +
		   	   "WHERE {" +
		   	   "?tc rdf:type :TestCase ." +
		   	   "?tc dc:title ?title ." +
		   	   "?tc :heuristic ?heuristic ." +
		   	   "?tc :patternIn ?patternIn ." +
		   	   "?tc :patternOut ?patternOut ." +
		   	   "OPTIONAL { ?tc :qpi ?qpi } " +
		   	   "OPTIONAL { ?tc :sei ?sei } " +
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