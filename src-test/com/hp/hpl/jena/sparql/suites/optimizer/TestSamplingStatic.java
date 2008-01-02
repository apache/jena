/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.framework.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;
import com.hp.hpl.jena.sparql.engine.optimizer.sampling.SamplingFactory;

/**
 * Some static tests for sampling techniques (e.g. isomorph test for
 * sampling of 100%)
 * 
 * @author Markus Stocker
 */

public class TestSamplingStatic extends TestCase
{
	private static Graph graph ;
	private static Model model ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	
	public TestSamplingStatic(String title)
	{
		super(title) ;
	}
	
	public void testIsomorphism()
	{
		Graph g = SamplingFactory.defaultSamplingMethod(model, 1.0) ;
		assertTrue(g.isIsomorphicWith(graph) == true) ;
	}
	
	public void testEmpty()
	{
		Graph g = SamplingFactory.defaultSamplingMethod(model, 0.0) ;
		assertTrue(g.isEmpty() == true) ;
	}
	
	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestSamplingStatic") ;
		
	    model = Util.readModel(testDataFileName) ;
	    graph = model.getGraph() ;
	    
        // Some static tests
        ts.addTest(new TestSamplingStatic("testIsomorphism")) ;
        ts.addTest(new TestSamplingStatic("testEmpty")) ;
        
		return ts ;
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