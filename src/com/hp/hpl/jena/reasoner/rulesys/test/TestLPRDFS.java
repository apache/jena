/******************************************************************
 * File:        TestLPRDFS.java
 * Created by:  Dave Reynolds
 * Created on:  26-Jul-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestLPRDFS.java,v 1.5 2004-12-07 09:56:34 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.io.IOException;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.ReasonerTester;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.*;

/**
 *  Test an FB hyrid using the emerging LP engine on the basic RDFS tests.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2004-12-07 09:56:34 $
 */
public class TestLPRDFS extends TestCase {
    
    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/rdfs-fb-lp-expt.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
        
    /** The tester utility */
    protected ReasonerTester tester;
     
    static Log logger = LogFactory.getLog(TestLPRDFS.class);
    
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
    public static List loadRules() {
        if (ruleSet == null) ruleSet = FBRuleReasoner.loadRules( RULE_FILE );
        return ruleSet;
    }

}



/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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