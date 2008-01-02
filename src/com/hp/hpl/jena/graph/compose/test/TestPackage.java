/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPackage.java,v 1.13 2008-01-02 12:07:21 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

/**
	@author kers
*/

import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.mem.test.TestSuiteRegression;

import junit.framework.*;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.13 $ $Date: 2008-01-02 12:07:21 $
 */
public class TestPackage extends TestCase {
    
    public static TestSuite suite() {
    	TestSuite result = new TestSuite();
        suite( result, Intersection.class );
        suite( result, Union.class );
        suite( result, Difference.class );
    /* */
        result.addTest( TestDelta.suite() );
        result.addTest( TestUnion.suite() );
        result.addTest( TestDisjointUnion.suite() );
        result.addTest( TestDifference.suite() );
        result.addTest( TestIntersection.suite() );
        result.addTestSuite( TestUnionStatistics.class );
        result.addTest( TestMultiUnion.suite() );
    /* */
        result.addTest( TestPolyadicPrefixMapping.suite() );
        return  result;
    }

    public static TestSuite suite(TestSuite suite,Class c) {

    	for (int i=0;i<TestSuiteRegression.testNames.length;i++)
           suite.addTest(new TestCaseBasic(TestSuiteRegression.testNames[i],c));
    	
        return suite;
    }
}


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
