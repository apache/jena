/******************************************************************
 * File:        TestPackage.java
 * Created by:  Dave Reynolds
 * Created on:  30-Mar-03
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestPackage.java,v 1.6 2010-05-15 20:12:17 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;


import junit.framework.TestSuite ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Aggregate tester that runs all the test associated with the rulesys package.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2010-05-15 20:12:17 $
 */

public class TestPackage extends TestSuite {

    protected static Logger logger = LoggerFactory.getLogger(TestPackage.class);
    
    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("RuleSys");
        
        addTestSuite( TestConfigVocabulary.class );
        addTestSuite( TestGenericRuleReasonerConfig.class );
        addTest( "TestBasics", TestBasics.suite() );
        addTest( "TestBackchainer", TestBackchainer.suite() );
        addTest( "TestLPBasics", TestBasicLP.suite() );
        addTest( "TestLPDerivation", TestLPDerivation.suite() );
        addTest( "TestFBRules", TestFBRules.suite() );
        addTest( "TestGenericRules", TestGenericRules.suite() );
        addTest( "TestRETE", TestRETE.suite() );
        addTest( TestSetRules.suite() );
        addTest( "OWLRuleUnitTests", OWLUnitTest.suite() );
        addTest( "TestBugs", TestBugs.suite() );
        addTest( "TestOWLMisc", TestOWLMisc.suite() );
        addTest( "TestCapabilities", TestCapabilities.suite() );
        addTest( "TestComparatorBuiltins", TestComparatorBuiltins.suite() );
        
        try {
            /* uncomment the following block when we switch to java 1.6 and update ConcurrentTest to do deadlock detection */
//            // Check the JVM supports the management interfaces needed for
//            // running the concurrency test
//            ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
//            long[] ids = tmx.findDeadlockedThreads();
            addTest( "ConcurrentyTest", ConcurrencyTest.suite() );
        } catch (Throwable t) {
            logger.warn("Skipping concurrency test, JVM doesn't seem to support fileDeadlockedThreads");
        }
        addTestSuite( TestInferenceReification.class );
        addTestSuite( TestRestrictionsDontNeedTyping.class );
        
        // No longer needed because the tests are now subsumed in OWLUnitTest
        // addTest( "TestOWLConsistency", TestOWLRules.suite() );
    }

    // helper method
    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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