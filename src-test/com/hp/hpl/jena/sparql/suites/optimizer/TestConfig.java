/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.extensions.TestSetup;
import junit.framework.*;

import java.util.Set;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.ProbabilityFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityDataModel;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndex;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndexModel;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.IndexLevel;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternOptimizer;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.GraphStatisticsHeuristic;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicBasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicsRegistry;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.ProbabilisticFramework;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.VariableCounting;
import com.hp.hpl.jena.query.ARQ;

/**
 * Some tests to test if the configuration settings behave as expected
 * 
 * @author Markus Stocker
 */

public class TestConfig extends TestCase
{
	private static long size ;
	private static Model graphM = null ;
	private static Set exProperty = new HashSet() ;
	private static final Property firstnameP = ResourceFactory.createProperty("http://example.org#firstname") ;
	private static final Property lastnameP = ResourceFactory.createProperty("http://example.org#lastname") ;
	private static final Property workplaceP = ResourceFactory.createProperty("http://example.org#workplace") ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	
	public TestConfig(String title)
	{
		super(title) ;
	}
	
	public static void oneTimeSetUp()
	{
		graphM = Util.readModel(testDataFileName) ;
		size = graphM.size() ;
		
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
	
	public void testIndexLevel()
	{
		ProbabilityIndexModel probability = null ;
		ProbabilityIndex index = null ;
		Config config = new Config() ;
		config.setIndexLevel(IndexLevel.FULL) ;
		
		probability = (ProbabilityIndexModel)ProbabilityFactory.createIndexModel(graphM, config) ;
		index = probability.getIndex() ;
		
		assertTrue(index.getLevel() == IndexLevel.FULL) ;
		assertTrue(index.allowsJoinedProbability() == true) ;
		
		Model indexM = ProbabilityFactory.createIndex(graphM, new Config(IndexLevel.LIGHTWEIGHT)) ;
		probability = (ProbabilityIndexModel)ProbabilityFactory.loadIndexModel(graphM, indexM, config) ;
		index = probability.getIndex() ;
		
		assertTrue(index.getLevel() == IndexLevel.LIGHTWEIGHT) ;
		assertTrue(index.allowsJoinedProbability() == false) ;
 	}
	
	public void testBasicPatternHeuristic()
	{
		BasicPatternOptimizer optimizer = null ;
		HeuristicBasicPattern heuristic = null ;
		Config config = new Config() ;
		config.setBasicPatternHeuristic(HeuristicsRegistry.BGP_GRAPH_STATISTICS_HANDLER) ;

		optimizer = new BasicPatternOptimizer(ARQ.getContext(), graphM.getGraph(), null, config) ;
		heuristic = optimizer.getHeuristicBasicPattern() ;
		
		assertTrue(heuristic instanceof GraphStatisticsHeuristic) ;

		optimizer = new BasicPatternOptimizer(ARQ.getContext(), graphM.getGraph(), null, new Config(HeuristicsRegistry.BGP_PROBABILISTIC_FRAMEWORK)) ;
		heuristic = optimizer.getHeuristicBasicPattern() ;
		
		assertTrue(heuristic instanceof ProbabilisticFramework) ;
		
		optimizer = new BasicPatternOptimizer(ARQ.getContext(), graphM.getGraph(), null, new Config(HeuristicsRegistry.BGP_VARIABLE_COUNTING)) ;
		heuristic = optimizer.getHeuristicBasicPattern() ;
		
		assertTrue(heuristic instanceof VariableCounting) ;
	}
	
	public void testExProperty()
	{
		ProbabilityIndexModel probability = null ;
		ProbabilityIndex index = null ;
		Set exclude = null ;
		Config config = new Config() ;
		config.setExProperty(exProperty) ;
		
		probability = (ProbabilityIndexModel)ProbabilityFactory.createIndexModel(graphM, config) ;
		index = probability.getIndex() ;
		exclude = index.getExProperty() ;
		
		assertTrue(exclude.containsAll(exProperty)) ;
		assertFalse(exclude.contains(RDF.Bag)) ;
		
		Model indexM = ProbabilityFactory.createIndex(graphM, new Config(exProperty)) ;
		probability = (ProbabilityIndexModel)ProbabilityFactory.loadIndexModel(graphM, indexM, config) ;
		index = probability.getIndex() ;
		exclude = index.getExProperty() ;
		
		assertTrue(exclude.containsAll(exProperty)) ;
		assertFalse(exclude.contains(RDF.Bag)) ;
 	}
	
	public void testLimitMinProbability1()
	{
		ProbabilityIndexModel probability = null ;
		Config config = new Config() ;
		config.setLimitMinProbability(false) ;
		
		probability = (ProbabilityIndexModel)ProbabilityFactory.createIndexModel(graphM, config) ;
		assertTrue(probability.getMinProbability() == Double.MIN_VALUE) ;
		assertTrue(probability.getMinJoinedProbability() == Double.MIN_VALUE) ;
		
		Model indexM = ProbabilityFactory.createIndex(graphM) ;
		probability = (ProbabilityIndexModel)ProbabilityFactory.loadIndexModel(graphM, indexM, new Config(true)) ;
		assertTrue(probability.getMinProbability() == 1d / size) ;
		assertTrue(probability.getMinJoinedProbability() == 1d / (size * size)) ;
 	}
	
	public void testLimitMinProbability2()
	{
		ProbabilityDataModel probability = null ;
		
		probability = (ProbabilityDataModel)ProbabilityFactory.loadDataModel(graphM, new Config(false)) ;
		assertTrue(probability.getMinProbability() == Double.MIN_VALUE) ;
		
		probability = (ProbabilityDataModel)ProbabilityFactory.loadDataModel(graphM, new Config(true)) ;
		assertTrue(probability.getMinProbability() == 1d / size) ;
 	}

	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestConfig") ;
			
		ts.addTest(new TestConfig("testIndexLevel")) ;
		ts.addTest(new TestConfig("testBasicPatternHeuristic")) ;
		ts.addTest(new TestConfig("testExProperty")) ;
		ts.addTest(new TestConfig("testLimitMinProbability1")) ;
		ts.addTest(new TestConfig("testLimitMinProbability2")) ;
		
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
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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