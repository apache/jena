/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 *
 * $Id: RandomizedTestSuiteRegression.java,v 1.2 2005-02-21 12:18:50 andy_seaborne Exp $
 */

package com.hp.hpl.jena.shared.test;

import junit.framework.*;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.2 $ $Date: 2005-02-21 12:18:50 $
 */
public class RandomizedTestSuiteRegression extends Object {

    public static TestSuite suite() {
    	TestSuite s = new TestSuite();
    	s.setName("Random order models");
        return suite(s);
    }

    public static TestSuite suite(TestSuite suite) {
        suite.addTest(new RandomizedTestCaseBasic("test1"));
        suite.addTest(new RandomizedTestCaseBasic("test2"));
        suite.addTest(new RandomizedTestCaseBasic("test3"));
        suite.addTest(new RandomizedTestCaseBasic("test4"));
        suite.addTest(new RandomizedTestCaseBasic("test5"));
        suite.addTest(new RandomizedTestCaseBasic("test6"));
        suite.addTest(new RandomizedTestCaseBasic("test7"));
        suite.addTest(new RandomizedTestCaseBasic("test8"));
        suite.addTest(new RandomizedTestCaseBasic("test9"));
        suite.addTest(new RandomizedTestCaseBasic("test10"));
        suite.addTest(new RandomizedTestCaseBasic("test11"));
        suite.addTest(new RandomizedTestCaseBasic("test12"));
        suite.addTest(new RandomizedTestCaseBasic("test13"));
        suite.addTest(new RandomizedTestCaseBasic("test14"));
        suite.addTest(new RandomizedTestCaseBasic("test15"));
        suite.addTest(new RandomizedTestCaseBasic("test16"));
        suite.addTest(new RandomizedTestCaseBasic("test17"));
        suite.addTest(new RandomizedTestCaseBasic("test18"));
        suite.addTest(new RandomizedTestCaseBasic("test19"));
        //      suite.addTest(new TestCaseBasic("test20"));
        suite.addTest(new RandomizedTestCaseBasic("test97"));

        suite.addTest(new RandomizedTestCaseBasic("testModelEquals"));
        
   
        suite.addTest(new RandomizedTestCaseBasic("testMatch"));
        //    suite.addTest(new TestCaseBasic("testWriterAndReader"));
        suite.addTest(new RandomizedTestCaseBasic("testNTripleReader"));
        //    suite.addTest(new TestCaseBasic("testWriterInterface"));
        suite.addTest(new RandomizedTestCaseBasic("testReaderInterface"));

      
        return suite;
    }
}
