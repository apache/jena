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

import java.io.IOException;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.ReasonerTester;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 *  Test an FB hyrid using the emerging LP engine on the basic RDFS tests.
 */
public class TestLPRDFS extends TestCase {
    
    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/rdfs-fb-lp-expt.rules";
    
    /** The parsed rules */
    protected static List<Rule> ruleSet;
        
    /** The tester utility */
    protected ReasonerTester tester;
     
    static Logger logger = LoggerFactory.getLogger(TestLPRDFS.class);
    
    /**
     * Boilerplate for junit
     */ 
    public TestLPRDFS( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestLPRDFS.class);
//        TestSuite suite = new TestSuite();
//        try {
//            TestRDFSReasoners.constructQuerytests(
//                        suite,
//                        "rdfs/manifest-nodirect-noresource.rdf", 
//                        makeReasoner());            
//       } catch (IOException e) {
//           // failed to even built the test harness
//           logger.error("Failed to construct RDFS test harness", e);
//       }
//       return suite;
   }  
   
    public void test1()  throws IOException {
        doTest("test1");
    }
   
    public void test2()  throws IOException {
        doTest("test2");
    }
   
    public void test3()  throws IOException {
        doTest("test3");
    }
   
    public void test4()  throws IOException {
        doTest("test4");
    }
   
    public void test5()  throws IOException {
        doTest("test5");
    }
   
    public void test6()  throws IOException {
        doTest("test6");
    }
   
    public void test7()  throws IOException {
        doTest("test7");
    }
   
    public void test8()  throws IOException {
        doTest("test8");
    }
   
    public void test9()  throws IOException {
        doTest("test9");
    }
   
    public void test10()  throws IOException {
        doTest("test10");
    }
   
    public void test11()  throws IOException {
        doTest("test11");
    }
   
    public void test12()  throws IOException {
        doTest("test12");
    }
   
    public void test13()  throws IOException {
        doTest("test13");
    }
   
    public void test14()  throws IOException {
        doTest("test14");
    }
   
    public void test15()  throws IOException {
        doTest("test15");
    }
   
    public void test16()  throws IOException {
        doTest("test16");
    }
   
    public void test18()  throws IOException {
        doTest("test18");
    }
   
    public void test20()  throws IOException {
        doTest("test20");
    }
   
    /**
     * Run a named test.
     */
    public void doTest(String name) throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        tester.runTest(ReasonerTester.BASE_URI + "rdfs/" + name, makeReasoner(), this);
    }
    
    /**
     * Return the reasoner to test
     */
    public static Reasoner makeReasoner() {
        FBRuleReasoner reasoner = new FBRuleReasoner(loadRules());
        // Don't have TGC enable yet.
        return reasoner;
    }
    
    /**
     * Return the RDFS rule set, loading it in if necessary
     */
    public static List<Rule> loadRules() {
        if (ruleSet == null) ruleSet = FBRuleReasoner.loadRules( RULE_FILE );
        return ruleSet;
    }

}
