/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.extensions.TestSetup;
import junit.framework.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.ProbabilityFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndex;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndexModel;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.sparql.engine.optimizer.Optimizer;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicsRegistry;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.IndexLevel;

/**
 * Test the API, i.e. all the access methods of factories, etc.
 * This test cases mainly throw null pointer exceptions if
 * something goes wrong while calling the API with the parameters.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class TestAPI extends TestCase
{
	private static Model graphM = null, indexM = null ;
	private static ProbabilityIndex index = null ;
	private static ProbabilityIndexModel probability = null ;
	private static Query query = null ;
	private static Set exProperty = new HashSet() ;
	private static final Property firstnameP = ResourceFactory.createProperty("http://example.org#firstname") ;
	private static final Property lastnameP = ResourceFactory.createProperty("http://example.org#lastname") ;
	private static final Property workplaceP = ResourceFactory.createProperty("http://example.org#workplace") ;
	private static final Property msnP = ResourceFactory.createProperty("http://example.org#msn") ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	
	public TestAPI(String title)
	{
		super(title) ;
	}
	
	public static void oneTimeSetUp()
	{
		graphM = Util.readModel(testDataFileName) ;
		probability = (ProbabilityIndexModel)ProbabilityFactory.createIndexModel(Util.readModel(testDataFileName)) ;
		index = probability.getIndex() ;
		indexM = index.getModel() ;
		
		ElementTriplesBlock el = new ElementTriplesBlock() ;
		el.addTriple(new Triple(Var.alloc("x"), Var.alloc("y"), Var.alloc("z"))) ;
		
		query = QueryFactory.make() ;
		query.setQuerySelectType() ;
		query.setQueryResultStar(true) ;
		query.setQueryPattern(el) ;
		
		exProperty.add(firstnameP) ;
		exProperty.add(lastnameP) ;
		exProperty.add(workplaceP) ;
		exProperty.add(RDF.type) ;
		exProperty.add(RDF.first) ;
	}
	
	public static void oneTimeTearDown()
	{
		graphM.close() ;
	}
	
	public void testOptimizerEnable()
	{
		Optimizer.enable() ;
		Optimizer.enable(null) ;
		Optimizer.enable(new Config(HeuristicsRegistry.BGP_VARIABLE_COUNTING)) ;
		Optimizer.enable(ARQ.getContext(), new Config(HeuristicsRegistry.BGP_VARIABLE_COUNTING)) ;
		Optimizer.enable(graphM, indexM) ;
		Optimizer.enable(graphM, indexM, null) ;
		Optimizer.enable(graphM, indexM, new Config(HeuristicsRegistry.BGP_VARIABLE_COUNTING)) ;
		Optimizer.enable(ARQ.getContext(), graphM, indexM, null) ;
		Optimizer.enable(ARQ.getContext(), graphM, indexM, new Config(HeuristicsRegistry.BGP_VARIABLE_COUNTING)) ;
	}
	
	public void testOptimizerDisable()
	{
		Optimizer.disable() ;
		Optimizer.disable(ARQ.getContext()) ;
	}
	
	public void testExplain()
	{
		Optimizer.explain(graphM, query) ;
		Optimizer.explain(graphM, query, null) ;
		Optimizer.explain(graphM, query, new Config(HeuristicsRegistry.BGP_VARIABLE_COUNTING)) ;
		Optimizer.explain(ARQ.getContext(), graphM, query, null) ;
		Optimizer.explain(ARQ.getContext(), graphM, query, new Config(HeuristicsRegistry.BGP_VARIABLE_COUNTING)) ;
	}
	
	public void testIndex()
	{		
		Optimizer.index(graphM) ;
		Optimizer.index(graphM, new Config(IndexLevel.LIGHTWEIGHT)) ;
		Optimizer.index(graphM, new Config(IndexLevel.FULL)) ;
		Optimizer.index(graphM, new Config(exProperty)) ;
		
		// Create an index and make some tests on it, especially if the properties are excluded
		Model indexM = Optimizer.index(graphM, new Config(IndexLevel.LIGHTWEIGHT, exProperty)) ;
		ProbabilityIndexModel probability = (ProbabilityIndexModel)ProbabilityFactory.loadIndexModel(graphM, indexM, null) ;
		
		ProbabilityIndex index = probability.getIndex() ;
		
		Map properties = index.getProperties() ;
		Map histograms = index.getHistograms() ;
		
		assertTrue(index.allowsJoinedProbability() == false) ;
		assertTrue(properties.containsKey(msnP) == true) ;
		assertTrue(properties.containsKey(firstnameP) == false) ;
		assertTrue(properties.containsKey(lastnameP) == false) ;
		assertTrue(histograms.containsKey(msnP) == true) ;
		assertTrue(histograms.containsKey(firstnameP) == false) ;
		assertTrue(histograms.containsKey(lastnameP) == false) ;
	}
	
	public void testProbability1()
	{
		ProbabilityFactory.createDefaultModel(graphM, null) ;
		ProbabilityFactory.createDefaultModel(graphM.getGraph(), null) ;
		ProbabilityFactory.createIndex(graphM) ;
		ProbabilityFactory.createIndex(graphM.getGraph()) ;
		ProbabilityFactory.createIndex(graphM, new Config(exProperty)) ;
		ProbabilityFactory.createIndex(graphM, new Config(IndexLevel.FULL, exProperty)) ;
		ProbabilityFactory.createIndexModel(graphM, new Config(IndexLevel.FULL, exProperty)) ;
		ProbabilityFactory.loadDataModel(graphM, null) ;
		ProbabilityFactory.loadDataModel(graphM.getGraph(), null) ;
		ProbabilityFactory.loadDefaultModel(graphM, indexM, null) ;
		ProbabilityFactory.loadDefaultModel(graphM.getGraph(), indexM, null) ;
		ProbabilityFactory.loadIndexModel(graphM, indexM, null) ;
	}
	
	public void testProbability2()
	{
		ProbabilityFactory.createDefaultModel(graphM, new Config()) ;
		ProbabilityFactory.createDefaultModel(graphM.getGraph(), new Config()) ;
		ProbabilityFactory.createIndex(graphM) ;
		ProbabilityFactory.createIndex(graphM.getGraph()) ;
		ProbabilityFactory.createIndex(graphM, new Config(exProperty)) ;
		ProbabilityFactory.createIndex(graphM, new Config(IndexLevel.FULL, exProperty)) ;
		ProbabilityFactory.createIndexModel(graphM, new Config(IndexLevel.FULL, exProperty)) ;
		ProbabilityFactory.loadDataModel(graphM, new Config()) ;
		ProbabilityFactory.loadDataModel(graphM.getGraph(), new Config()) ;
		ProbabilityFactory.loadDefaultModel(graphM, indexM, new Config()) ;
		ProbabilityFactory.loadDefaultModel(graphM.getGraph(), indexM, new Config()) ;
		ProbabilityFactory.loadIndexModel(graphM, indexM, new Config()) ;
	}
	

	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestAPI") ;
			
        // This test should run first, in order to enable the optimizer for subsequent tests
		ts.addTest(new TestAPI("testOptimizerDisable")) ;
		ts.addTest(new TestAPI("testOptimizerEnable")) ;
		ts.addTest(new TestAPI("testExplain")) ;
		ts.addTest(new TestAPI("testIndex")) ;
		ts.addTest(new TestAPI("testProbability1")) ;
		ts.addTest(new TestAPI("testProbability2")) ;
		
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