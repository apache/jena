/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.rulesys.test;


import junit.framework.TestSuite ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Aggregate tester that runs all the test associated with the rulesys package.
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
        addTest( "FRuleEngineIFactoryTest", FRuleEngineIFactoryTest.suite() );
        //addTest ("TestRuleLoader", TestRuleLoader.suite() );
        
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
