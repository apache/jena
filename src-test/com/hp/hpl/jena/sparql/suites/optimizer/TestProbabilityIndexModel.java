/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.framework.*;
import junit.extensions.TestSetup;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.ProbabilityFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndexModel;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;

/**
 * Probability test case. The test class is based on the TestProbability-manifest.
 * 
 * @author Markus Stocker
 */

public class TestProbabilityIndexModel extends TestCase
{
	private Triple triple1 ;
	private Triple triple2 ;
	private double probability ;
	private double selectivity ;
	private static double minProbability, minJoinedProbability ;
	private static long dataGraphSize, indexedSize, squaredDataGraphSize, squaredIndexedSize ;
	private static ProbabilityIndexModel statistics ;
	private static Model graphM, testsM, indexM, dataM ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	private static final String testCaseFileName = "testing/Optimizer/TestProbabilityIndexModel-manifest.n3" ;
	
	public TestProbabilityIndexModel(String title, Triple triple1, Triple triple2, double probability, double selectivity)
	{		
		super(title) ;

		this.triple1 = triple1 ;
		this.triple2 = triple2 ;
		this.probability = probability ;
		this.selectivity = selectivity ;
	}
	
	// Run the dynamically loaded test cases
	protected void runTest()
	{
		double p = statistics.getProbability(triple1, triple2) ;
		double s = statistics.getSelectivity(triple1, triple2) ;
		
		assertTrue(statistics.getMinProbability() == minProbability) ;
		assertTrue(statistics.getMinJoinedProbability() == minJoinedProbability) ;
		assertTrue(statistics.getDataGraphSize() == dataGraphSize) ;
		assertTrue(statistics.getIndexedSize() == indexedSize) ;
		assertTrue(statistics.getSquaredDataGraphSize() == squaredDataGraphSize) ;
		assertTrue(statistics.getSquaredIndexedSize() == squaredIndexedSize) ;
		assertTrue("Actual: " + p + " Target: " + probability, p == probability) ;
		assertTrue("Actual: " + s + " Target: " + selectivity, s == selectivity) ;
	}
	
	public static void oneTimeSetUp()
	{		
		QueryExecution qe = QueryExecutionFactory.create(queryGenericTestCase(), testsM);

		try 
		{
			ResultSet rs = qe.execSelect() ;
			QuerySolution solution = rs.nextSolution() ;
			minProbability = solution.getLiteral("minProbability").getDouble() ;
			minJoinedProbability = solution.getLiteral("minJoinedProbability").getDouble() ;
			dataGraphSize = solution.getLiteral("dataGraphSize").getLong() ;
			indexedSize = solution.getLiteral("indexedSize").getLong() ;
			squaredDataGraphSize = solution.getLiteral("squaredDataGraphSize").getLong() ;
			squaredIndexedSize = solution.getLiteral("squaredIndexedSize").getLong() ;
		} finally { 
			qe.close() ; 
		}
		
		dataM = Util.readModel(testDataFileName) ;
		indexM = ProbabilityFactory.createIndex(dataM) ;
		statistics = (ProbabilityIndexModel)ProbabilityFactory.loadIndexModel(dataM, indexM, null) ;
	}
	
	public static void oneTimeTearDown()
	{
		graphM.close() ;
		testsM.close() ;
		dataM.close() ;
		indexM.close() ;
	}
	
	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestProbabilityIndexModel") ;
        graphM = Util.readModel(testDataFileName) ;
    	testsM = Util.readModel(testCaseFileName) ;
        QueryExecution qe = QueryExecutionFactory.create(queryTestCases(), testsM);

		try 
		{
			ResultSet rs = qe.execSelect() ;
			while (rs.hasNext()) 
			{
				Triple triple1 = null, triple2 = null ;
				QuerySolution solution = rs.nextSolution() ;
				
				String title = solution.getLiteral("title").getLexicalForm() ;
				double probability = solution.getLiteral("probability").getDouble() ;
				double selectivity = solution.getLiteral("selectivity").getDouble() ;
				
				triple1 = getTriple(solution.getResource("triple1")) ;
				
				if (solution.getResource("triple2") != null)
					triple2 = getTriple(solution.getResource("triple2")) ;
				
				ts.addTest(new TestProbabilityIndexModel(title, triple1, triple2, probability, selectivity)) ;
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
	
	private static Triple getTriple(Resource resource)
	{
		Node s = null, p = null, o = null ;
		StmtIterator stmtIter = testsM.listStatements(resource, (Property)null, (RDFNode)null) ;
		
		while (stmtIter.hasNext())
		{
			Statement stmt = stmtIter.nextStatement() ;
			Property predicate = stmt.getPredicate() ;
			String predicateURI = predicate.getURI() ;
			
			if (predicateURI.equals(Util.TEST_NS + "subject"))
				s = Util.createNode(stmt.getObject().asNode().getLiteralLexicalForm()) ;
			else if (predicateURI.equals(Util.TEST_NS + "predicate"))
				p = Util.createNode(stmt.getObject().asNode().getLiteralLexicalForm()) ;
			else if (predicateURI.equals(Util.TEST_NS + "object"))
				o = Util.createNode(stmt.getObject().asNode().getLiteralLexicalForm()) ;
		}
		
		stmtIter.close() ;
			
		return new Triple(s, p, o) ;
	}
	
	// The query to retrieve the test cases with the basic pattern and the graph
	private static String queryTestCases()
	{		
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		   	   "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
		   	   "PREFIX : <" + Util.TEST_NS + ">" +
		   	   "SELECT ?title ?triple1 ?triple2 ?probability ?selectivity " +
		   	   "WHERE {" +
		   	   "?tc rdf:type :TestCase ." +
		   	   "?tc dc:title ?title ." +
		   	   "?tc :triple1 ?triple1 ." +
		   	   "OPTIONAL { ?tc :triple2 ?triple2 } " + 
		   	   "?tc :probability ?probability ." +
		   	   "?tc :selectivity ?selectivity ." + 
		   	   "}" +
		   	   "ORDER BY ASC(?title)" ;
	}
	
	private static String queryGenericTestCase()
	{
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	   	   	   "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
	   	   	   "PREFIX : <" + Util.TEST_NS + ">" +
	   	   	   "SELECT ?minProbability ?minJoinedProbability ?dataGraphSize ?indexedSize ?squaredDataGraphSize ?squaredIndexedSize " +
	   	   	   "WHERE {" +
	   	   	   "?tc rdf:type :TestCase ." +
	   	   	   "?tc :minProbability ?minProbability ." +
	   	   	   "?tc :minJoinedProbability ?minJoinedProbability ." +
	   	   	   "?tc :dataGraphSize ?dataGraphSize ." +
	   	   	   "?tc :indexedSize ?indexedSize ." +
	   	   	   "?tc :squaredDataGraphSize ?squaredDataGraphSize ." +
	   	   	   "?tc :squaredIndexedSize ?squaredIndexedSize ." +
	   	   	   "}" ;
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