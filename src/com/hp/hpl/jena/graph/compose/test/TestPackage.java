/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPackage.java,v 1.6 2004-09-06 15:19:25 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

/**
	@author kers
*/

import com.hp.hpl.jena.graph.compose.*;

import junit.framework.TestSuite;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.6 $ $Date: 2004-09-06 15:19:25 $
 */
public class TestPackage extends Object {
    
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
        result.addTest( TestMultiUnion.suite() );
    /* */
        result.addTest( TestPolyadicPrefixMapping.suite() );
        return  result;
    }

    public static TestSuite suite(TestSuite suite,Class c) {
        suite.addTest(new TestCaseBasic("test1",c));
        suite.addTest(new TestCaseBasic("test2",c));
        suite.addTest(new TestCaseBasic("test3",c));
        suite.addTest(new TestCaseBasic("test4",c));
        suite.addTest(new TestCaseBasic("test5",c));
        suite.addTest(new TestCaseBasic("test6",c));
        suite.addTest(new TestCaseBasic("test7",c));
        suite.addTest(new TestCaseBasic("test8",c));
        suite.addTest(new TestCaseBasic("test9",c));
        suite.addTest(new TestCaseBasic("test10",c));
        suite.addTest(new TestCaseBasic("test11",c));
        suite.addTest(new TestCaseBasic("test12",c));
        suite.addTest(new TestCaseBasic("test13",c));
        suite.addTest(new TestCaseBasic("test14",c));
        suite.addTest(new TestCaseBasic("test15",c));
        suite.addTest(new TestCaseBasic("test16",c));
        suite.addTest(new TestCaseBasic("test17",c));
        suite.addTest(new TestCaseBasic("test18",c));
        suite.addTest(new TestCaseBasic("test19",c));
//        suite.addTest(new TestCaseBasic("test20"));
        suite.addTest(new TestCaseBasic("test97",c));
        
        suite.addTest(new TestCaseBasic("testModelEquals",c));
        suite.addTest(new TestCaseBasic("testMatch",c));
        // suite.addTest(new TestCaseBasic("testWriterAndReader",c));
        // suite.addTest(new TestCaseBasic("testNTripleReader",c));
        // suite.addTest(new TestCaseBasic("testWriterInterface",c));
        // suite.addTest(new TestCaseBasic("testReaderInterface",c));
        
        //suite.addTest(new TestCaseBugs("bug36"));
        
        return suite;
    }
}


/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
