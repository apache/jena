/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.extensions.TestSetup;
import junit.framework.*;

import java.util.* ;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternJoin;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Histogram;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.ProbabilityFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.Pattern;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndex;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndexModel;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;

/**
 * Test the probabilistic index build process based on the model provided
 * 
 * @author Markus Stocker
 */

public class TestIndex extends TestCase
{
	private static Model graphM = null ;
	private static ProbabilityIndex index = null ;
	private static ProbabilityIndexModel probability = null ;
	private static Map properties = null ;
	private static Map histograms = null ;
	private static Map patterns = null ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	
	public TestIndex(String title)
	{
		super(title) ;
	}
	
	public static void oneTimeSetUp()
	{
		graphM = Util.readModel(testDataFileName) ;
		probability = (ProbabilityIndexModel)ProbabilityFactory.createIndexModel(Util.readModel(testDataFileName)) ;
		index = probability.getIndex() ;
		properties = index.getProperties() ;
		histograms = index.getHistograms() ;
		patterns = index.getPatterns() ;
	}
	
	public static void oneTimeTearDown()
	{
		graphM.close() ;
	}
	
	public void testDataGraphSize() 
	{
		assertEquals(probability.getDataGraphSize(), 37) ;
	}
	
	public void testIndexedSize()
	{
		assertEquals(probability.getIndexedSize(), 37) ;
	}
	
	public void testMinProbability()
	{
		assertEquals(probability.getMinProbability(), 1d / 37, 0d) ;
	}
	
	public void testMinJoinedProbability()
	{
		assertEquals(probability.getMinJoinedProbability(), 1d / (37 * 37), 0d) ;
	}
	
	public void testSquaredDataGraphSize()
	{
		assertEquals(probability.getSquaredDataGraphSize(), 37 * 37, 0d) ;
	}
	
	public void testSquaredIndexedSize()
	{
		assertEquals(probability.getSquaredIndexedSize(), 37 * 37, 0d) ;
	}
	
	public void testIndexAllowsJoinedProbability()
	{
		assertTrue(index.allowsJoinedProbability() == true) ;
	}
	
	public void testIndexLevel()
	{
		assertEquals(index.getLevel(), 1) ;
	}
	
	public void testNumOfIndexedResources()
	{
		assertEquals(index.getIndexedNumRes(), 5L) ;
	}
	
	public void testIndexedSSSize()
	{
		assertEquals(index.getIndexedSSSize(), 303L) ;
	}
	
	public void testIndexedSOSize()
	{
		assertEquals(index.getIndexedSOSize(), 0L) ;
	}
	
	public void testIndexedOSSize()
	{
		assertEquals(index.getIndexedOSSize(), 0L) ;
	}
	
	public void testIndexedOOSize()
	{
		assertEquals(index.getIndexedOOSize(), 63L) ;
	}
	
	public void testNumOfIndexedProperties()
	{
		assertEquals(properties.size(), 9) ;
	}
	
	public void testPropertyLookup1()
	{
		Property property = ResourceFactory.createProperty("http://example.org#firstname") ;
		assertEquals(((Long)properties.get(property)).longValue(), 5L) ;
		assertEquals(index.lookup(property), 5L) ;
	}
	
	public void testPropertyLookup2()
	{
		Property property = ResourceFactory.createProperty("http://example.org#lastname") ;
		assertEquals(((Long)properties.get(property)).longValue(), 5L) ;
		assertEquals(index.lookup(property), 5L) ;
	}
	
	public void testPropertyLookup3()
	{
		Property property = ResourceFactory.createProperty("http://example.org#msn") ;
		assertEquals(((Long)properties.get(property)).longValue(), 3L) ;
		assertEquals(index.lookup(property), 3L) ;
	}
	
	public void testPropertyLookup4()
	{
		Property property = ResourceFactory.createProperty("http://example.org#skype") ;
		assertEquals(((Long)properties.get(property)).longValue(), 4L) ;
		assertEquals(index.lookup(property), 4L) ;
	}
	
	public void testPropertyLookup5()
	{
		Property property = ResourceFactory.createProperty("http://example.org#Workplace") ;
		//assertEquals(((Long)properties.get(property)).longValue(), 0L) ; NULL POINTER (workplace not Workplace)
		assertEquals(index.lookup(property), 0L) ;
	}
	
	public void testNumOfIndexedHistograms()
	{
		assertEquals(histograms.size(), properties.size()) ;
	}
	
	public void testHistogram1()
	{
		Property property = ResourceFactory.createProperty("http://example.org#firstname") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.size(), 5) ;
		assertEquals(histogram.getClassSize(), 0.4d, 0d) ;
		assertEquals(histogram.getLowerBound(), -9.18128756E8, 0d) ;
		assertEquals(histogram.getUpperBound(), -9.18128752E8, 0d) ;
		assertEquals(histogram.getClasses().size(), 10) ;
	}
	
	public void testHistogram2()
	{
		Property property = ResourceFactory.createProperty("http://example.org#lastname") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.size(), 5) ;
		assertEquals(histogram.getClassSize(), 0.4d, 0d) ;
		assertEquals(histogram.getLowerBound(), -1.746240346E9, 0d) ;
		assertEquals(histogram.getUpperBound(), -1.746240342E9, 0d) ;
		assertEquals(histogram.getClasses().size(), 10) ;
	}
	
	public void testHistogram3()
	{
		Property property = ResourceFactory.createProperty("http://example.org#msn") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.size(), 3) ;
		assertEquals(histogram.getClassSize(), 2.487512833E8, 0d) ;
		assertEquals(histogram.getLowerBound(), -5.9668676E8, 0d) ;
		assertEquals(histogram.getUpperBound(), 1.890826073E9, 0d) ;
		assertEquals(histogram.getClasses().size(), 10) ;
	}
	
	public void testHistogram4()
	{
		Property property = ResourceFactory.createProperty("http://example.org#skype") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.size(), 4) ;
		assertEquals(histogram.getClassSize(), 1.121603817E8, 0d) ;
		assertEquals(histogram.getLowerBound(), -6.78441091E8, 0d) ;
		assertEquals(histogram.getUpperBound(), 4.43162726E8, 0d) ;
		assertEquals(histogram.getClasses().size(), 10) ;
	}
	
	public void testHistogramClass1()
	{
		Property property = ResourceFactory.createProperty("http://example.org#firstname") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("First Name 1")), 1) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("First Name 2")), 1) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("First Name 10")), 0) ;
	}
	
	public void testHistogramClass2()
	{
		Property property = ResourceFactory.createProperty("http://example.org#lastname") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("Last Name 1")), 1) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("Last Name 2")), 1) ;
	}
	
	public void testHistogramClass3()
	{
		Property property = ResourceFactory.createProperty("http://example.org#phone") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("xxx-xxx-xxx")), 5) ;
	}
	
	public void testHistogramClass4()
	{
		Property property = ResourceFactory.createProperty("http://example.org#workplace") ;
		Histogram histogram = (Histogram)histograms.get(property) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("company1")), 2) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("company3")), 1) ;
		assertEquals(histogram.getClassFrequency(Node.createLiteral("company10")), 0) ;
	}
	
	public void testNumOfIndexedPatterns()
	{
		// The cross product of properties 4 times for SS, SO, OS, OO
		assertEquals(patterns.size(), properties.size() * properties.size() * 4) ; 
	}
	
	public void testPattern1()
	{
		Property joiningP = ResourceFactory.createProperty("http://example.org#phone") ;
		Property joinedP = ResourceFactory.createProperty("http://example.org#phone") ;
		Resource joinT = ResourceFactory.createResource(BasicPatternJoin.OO) ;
		Pattern pattern = new Pattern(joiningP, joinedP, joinT) ;
		assertEquals(((Long)patterns.get(pattern)).longValue(), 25L) ;
	}
	
	public void testPattern2()
	{
		Property joiningP = ResourceFactory.createProperty("http://example.org#email") ;
		Property joinedP = ResourceFactory.createProperty("http://example.org#skype") ;
		Resource joinT = ResourceFactory.createResource(BasicPatternJoin.SS) ;
		Pattern pattern = new Pattern(joiningP, joinedP, joinT) ;
		assertEquals(((Long)patterns.get(pattern)).longValue(), 4L) ;
	}
	
	public void testPattern3()
	{
		Property joiningP = ResourceFactory.createProperty("http://example.org#skype") ;
		Property joinedP = ResourceFactory.createProperty("http://example.org#skype") ;
		Resource joinT = ResourceFactory.createResource(BasicPatternJoin.SS) ;
		Pattern pattern = new Pattern(joiningP, joinedP, joinT) ;
		assertEquals(((Long)patterns.get(pattern)).longValue(), 6L) ;
	}
	
	public void testPattern4()
	{
		Property joiningP = ResourceFactory.createProperty("http://example.org#firstname") ;
		Property joinedP = ResourceFactory.createProperty("http://example.org#skype") ;
		Resource joinT = ResourceFactory.createResource(BasicPatternJoin.SO) ;
		Pattern pattern = new Pattern(joiningP, joinedP, joinT) ;
		assertEquals(((Long)patterns.get(pattern)).longValue(), 0L) ;
	}
	
	public void testPattern5()
	{
		Property joiningP = ResourceFactory.createProperty("http://example.org#workplace") ;
		Property joinedP = ResourceFactory.createProperty("http://example.org#workplace") ;
		Resource joinT = ResourceFactory.createResource(BasicPatternJoin.OO) ;
		Pattern pattern = new Pattern(joiningP, joinedP, joinT) ;
		assertEquals(((Long)patterns.get(pattern)).longValue(), 9L) ;
	}

	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestIndex") ;
			
		ts.addTest(new TestIndex("testDataGraphSize")) ;
		ts.addTest(new TestIndex("testIndexedSize")) ;
		ts.addTest(new TestIndex("testMinProbability")) ;
		ts.addTest(new TestIndex("testMinJoinedProbability")) ;
		ts.addTest(new TestIndex("testSquaredDataGraphSize")) ;
		ts.addTest(new TestIndex("testSquaredIndexedSize")) ;
		ts.addTest(new TestIndex("testIndexAllowsJoinedProbability")) ;
		ts.addTest(new TestIndex("testIndexLevel")) ;
		ts.addTest(new TestIndex("testNumOfIndexedResources")) ;
		ts.addTest(new TestIndex("testIndexedSSSize")) ;
		ts.addTest(new TestIndex("testIndexedSOSize")) ;
		ts.addTest(new TestIndex("testIndexedOSSize")) ;
		ts.addTest(new TestIndex("testIndexedOOSize")) ;
		ts.addTest(new TestIndex("testNumOfIndexedProperties")) ;
		ts.addTest(new TestIndex("testPropertyLookup1")) ;
		ts.addTest(new TestIndex("testPropertyLookup2")) ;
		ts.addTest(new TestIndex("testPropertyLookup3")) ;
		ts.addTest(new TestIndex("testPropertyLookup4")) ;
		ts.addTest(new TestIndex("testPropertyLookup5")) ;
		ts.addTest(new TestIndex("testNumOfIndexedHistograms")) ;
		ts.addTest(new TestIndex("testHistogram1")) ;
		ts.addTest(new TestIndex("testHistogram2")) ;
		ts.addTest(new TestIndex("testHistogram3")) ;
		ts.addTest(new TestIndex("testHistogram4")) ;
		ts.addTest(new TestIndex("testHistogramClass1")) ;
		ts.addTest(new TestIndex("testHistogramClass2")) ;
		ts.addTest(new TestIndex("testHistogramClass3")) ;
		ts.addTest(new TestIndex("testHistogramClass4")) ;
		ts.addTest(new TestIndex("testNumOfIndexedPatterns")) ;
		ts.addTest(new TestIndex("testPattern1")) ;
		ts.addTest(new TestIndex("testPattern2")) ;
		ts.addTest(new TestIndex("testPattern3")) ;
		ts.addTest(new TestIndex("testPattern4")) ;
		ts.addTest(new TestIndex("testPattern5")) ;
		
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