/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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
 * $Id: TestSuiteRegression.java,v 1.1.1.1 2002-12-19 19:14:18 bwm Exp $
 */

package com.hp.hpl.jena.mem;

import junit.framework.TestSuite;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1.1.1 $ $Date: 2002-12-19 19:14:18 $
 */
public class TestSuiteRegression extends Object {

    public static TestSuite suite() {
        return suite(new TestSuite());
    }

    public static TestSuite suite(TestSuite suite) {
        suite.addTest(new TestCaseBasic("test1"));
        suite.addTest(new TestCaseBasic("test2"));
        suite.addTest(new TestCaseBasic("test3"));
        suite.addTest(new TestCaseBasic("test4"));
        suite.addTest(new TestCaseBasic("test5"));
        suite.addTest(new TestCaseBasic("test6"));
        suite.addTest(new TestCaseBasic("test7"));
        suite.addTest(new TestCaseBasic("test8"));
        suite.addTest(new TestCaseBasic("test9"));
        suite.addTest(new TestCaseBasic("test10"));
        suite.addTest(new TestCaseBasic("test11"));
        suite.addTest(new TestCaseBasic("test12"));
        suite.addTest(new TestCaseBasic("test13"));
        suite.addTest(new TestCaseBasic("test14"));
        suite.addTest(new TestCaseBasic("test15"));
        suite.addTest(new TestCaseBasic("test16"));
        suite.addTest(new TestCaseBasic("test17"));
        suite.addTest(new TestCaseBasic("test18"));
        suite.addTest(new TestCaseBasic("test19"));
        //      suite.addTest(new TestCaseBasic("test20"));
        suite.addTest(new TestCaseBasic("test97"));

        suite.addTest(new TestCaseBasic("testModelEquals"));
        suite.addTest(new TestCaseBasic("testMatch"));
        //    suite.addTest(new TestCaseBasic("testWriterAndReader"));
        suite.addTest(new TestCaseBasic("testNTripleReader"));
        //    suite.addTest(new TestCaseBasic("testWriterInterface"));
        suite.addTest(new TestCaseBasic("testReaderInterface"));

        suite.addTest(new TestCaseBugs("bug36"));

        return suite;
    }
}
