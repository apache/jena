/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.framework.*;

/**
 * All the ARQo tests 
 * @author Markus Stocker
 */

public class OptimizerTestSuite extends TestSuite
{    
    static public TestSuite suite()
    {
        TestSuite ts = new OptimizerTestSuite() ;
 
        // This test has to be executed first, or the test suite has to assure that the optimizer is enabled per default first
        ts.addTest(TestEnabled.suite()) ;
        ts.addTest(TestConfig.suite()) ;
        ts.addTest(TestAPI.suite()) ;
        ts.addTest(TestData.suite()) ;
        ts.addTest(TestIndex.suite()) ;
        ts.addTest(TestPrimeNumberGen.suite()) ;
        ts.addTest(TestSuiteGraph.suite()) ;
        ts.addTest(TestSuiteHeuristic.suite()) ;
        ts.addTest(TestSuiteProbability.suite()) ;
        ts.addTest(TestSuiteSampling.suite()) ;
        
        return ts ;
    }

	private OptimizerTestSuite()
	{
        super("Optimizer");
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