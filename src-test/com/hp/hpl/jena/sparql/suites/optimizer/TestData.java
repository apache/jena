/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.extensions.TestSetup;
import junit.framework.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.ProbabilityFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityDataModel;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;

/**
 * Test the probabilistic data build process based on the model provided
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class TestData extends TestCase
{
	private static Model graphM = null ;
	private static ProbabilityDataModel probability = null ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	
	public TestData(String title)
	{
		super(title) ;
	}
	
	public static void oneTimeSetUp()
	{
		graphM = Util.readModel(testDataFileName) ;
		probability = (ProbabilityDataModel)ProbabilityFactory.loadDataModel(Util.readModel(testDataFileName), null) ;
	}
	
	public static void oneTimeTearDown()
	{
		graphM.close() ;
	}
	
	public void testDataGraphSize()
	{
		assertEquals(probability.getDataGraphSize(), 37) ;
	}
	
	public void testMinProbability()
	{
		assertEquals(probability.getMinProbability(), 1d / 37, 0d) ;
	}
	
	public void testSquaredDataGraphSize()
	{
		assertEquals(probability.getSquaredDataGraphSize(), 37 * 37, 0d) ;
	}

	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestData") ;
			
		ts.addTest(new TestData("testDataGraphSize")) ;
		ts.addTest(new TestData("testMinProbability")) ;
		ts.addTest(new TestData("testSquaredDataGraphSize")) ;
		
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