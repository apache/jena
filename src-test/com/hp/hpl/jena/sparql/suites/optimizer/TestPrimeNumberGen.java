/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import junit.framework.*;
import com.hp.hpl.jena.sparql.engine.optimizer.core.PrimeNumberGen;

/**
 * This is a test for the prime number generator. The class tests
 * the first 58 prime numbers.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class TestPrimeNumberGen extends TestCase
{
	PrimeNumberGen prime = new PrimeNumberGen() ;
	// http://www.research.att.com/~njas/sequences/A000040
	int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 
					31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 
					73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 
					127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 
					179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 
					233, 239, 241, 251, 257, 263, 269, 271} ;
	
	public TestPrimeNumberGen(String title)
	{		
		super(title) ;
	}
	
	protected void runTest()
	{	
		assertTrue(prime.first() == 2) ;
		
	    for (int i = 0; i < primes.length; i++)
	    {	    	
	    	assertEquals(prime.next(), primes[i]) ;
	    }
	}
	
	// Build the test suite
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestPrimeNumberGen") ;

        // Add a new test to the test suite
		ts.addTest(new TestPrimeNumberGen("TestPrimeNumberGen")) ;

		return ts ;
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