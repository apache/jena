/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.framework.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;
import com.hp.hpl.jena.sparql.engine.optimizer.sampling.SamplingFactory;

/**
 * Test suite to test sampling techniques used for the 
 * index process of the optimizer
 * 
 * @author Markus Stocker
 */

public class TestSamplingDynamic extends TestCase
{
	//private String method ;
	private double factor ;
	private int size ;
	private static Model testsM, graphM ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	private static final String testCaseFileName = "testing/Optimizer/TestSampling-manifest.n3" ;
	
	public TestSamplingDynamic(String title, String method, double factor, int size)
	{		
		super(title) ;
		
		//this.method = method ;
		this.factor = factor ;
		this.size = size ;
	}
	
	// Run the dynamically loaded test cases
	public void runTest()
	{
		Graph graph = SamplingFactory.defaultSamplingMethod(graphM, factor) ;
		assertEquals(graph.size(), size) ;
	}
	
	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestSamplingDynamic") ;
        
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
				String method = solution.getLiteral("method").getString() ;
				double factor = solution.getLiteral("factor").getDouble() ;
				int size = solution.getLiteral("size").getInt() ;
				
				// Add a new test to the test suite
				ts.addTest(new TestSamplingDynamic(title, method, factor, size)) ;
			}
		} finally { 
			qe.close() ; 
		}
		
		return ts ;
    }
	
	// The query to retrieve the test cases with the basic pattern and the graph
	private static String queryTestCases()
	{		
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		   	   "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
		   	   "PREFIX : <" + Util.TEST_NS + ">" +
		   	   "SELECT ?title ?method ?factor ?size " +
		   	   "WHERE {" +
		   	   "?tc rdf:type :TestCase ." +
		   	   "?tc dc:title ?title ." +
		   	   "?tc :method ?method ." +
		   	   "?tc :factor ?factor ." +
		   	   "?tc :size ?size ." +
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