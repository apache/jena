/******************************************************************
 * File:        TestCurrentRDFWG.java
 * Created by:  Dave Reynolds
 * Created on:  25-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestCurrentRDFWG.java,v 1.5 2003-11-07 17:43:17 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

//import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasoner;
//import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.OWLFBRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

/**
 * Test the default RDFS reasoner against the current set of working group tests
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2003-11-07 17:43:17 $
 */
public class TestCurrentRDFWG extends ReasonerTestBase {
    
    /** Location of the test file directory */
    public static final String TEST_DIR = "testing/wg20031010/";    
//    public static final String TEST_DIR = "testing/wg/";    
    
    /** The base URI for the results file */
    public static String BASE_RESULTS_URI = "http://jena.sourceforge.net/data/rdf-results.rdf";
   
    /** The model describing the results of the run */
    Model testResults;
    
    /** The resource which acts as a description for the Jena2 instance being tested */
    Resource jena2;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(TestCurrentRDFWG.class);

    /**
     * Boilerplate for junit
     */ 
    public TestCurrentRDFWG( String name ) {
        super( name ); 
    }
   
    /** 
     * Initialize the result model.
     */
    public void initResults() {
        testResults = ModelFactory.createDefaultModel();
        jena2 = testResults.createResource(BASE_RESULTS_URI + "#jena2");
        jena2.addProperty(RDFS.comment, 
            testResults.createLiteral(
                "<a xmlns=\"http://www.w3.org/1999/xhtml\" href=\"http://jena.sourceforce.net/\">Jena2</a> includes a rule-based inference engine for RDF processing, " +
                "supporting both forward and backward chaining rules. Its OWL rule set is designed to provide sound " +
                "but not complete instance resasoning for that fragment of OWL/Full limited to the OWL/lite vocabulary. In" +
                "particular it does not support unionOf/complementOf.",
                true)
        );
        jena2.addProperty(RDFS.label, "Jena2");
        testResults.setNsPrefix("results", OWLResults.NS);
    }
        
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        try {
            Resource config = newResource()
            .addProperty(ReasonerVocabulary.PROPenableCMPScan, true)
            .addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "full");
//            config.addProperty(ReasonerVocabulary.PROPtraceOn, true);
            constructRDFWGtests(suite, RDFSRuleReasonerFactory.theInstance(), config);
//            constructRDFWGtests(suite, RDFSReasonerFactory.theInstance(), config);
                        
        } catch (IOException e) {
            // failed to even built the test harness
            logger.error("Failed to construct RDF WG test harness", e);
        }
        return suite;
    }  
        
    /**
     * Build the working group tests for the given reasoner.
     */
    private static void constructRDFWGtests(TestSuite suite, ReasonerFactory rf, Resource config) throws IOException {
        WGReasonerTester tester = new WGReasonerTester("Manifest.rdf", TEST_DIR);
        for (Iterator i = tester.listTests().iterator(); i.hasNext(); ) {
            String test = (String)i.next();
            suite.addTest(new TestReasonerWG(tester, test, rf, config));
        }
    }        

    /**
     * Inner class defining a test framework for invoking a single 
     * RDFCore working group test.
     */
    static class TestReasonerWG extends TestCase {
        
        /** The tester which already has the test manifest loaded */
        WGReasonerTester tester;
        
        /** The name of the specific test to run */
        String test;
        
        /** The factory for the reasoner type under test */
        ReasonerFactory reasonerFactory;
        
        /** An optional configuration model */
        Resource config;
        
        /** Constructor */
        TestReasonerWG(WGReasonerTester tester, String test, 
                                 ReasonerFactory reasonerFactory, Resource config) {
            super(test);
            this.tester = tester;
            this.test = test;
            this.reasonerFactory = reasonerFactory;
            this.config = config;
        }
        
        /**
         * The test runner
         */
        public void runTest() throws IOException {
            boolean success = tester.runTest(test, reasonerFactory, this, config);
//            Resource resultType = null;
//             if (test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest) 
//             ||  test.hasProperty(RDF.type, OWLTest.ConsistencyTest)) {
//                 resultType = success ? OWLResults.PassingRun : OWLResults.FailingRun;
//             } else {
//                 resultType = success ? OWLResults.PassingRun : OWLResults.IncompleteRun;
//             }
//             // log to the rdf result format
//             Resource result = testResults.createResource()
//                 .addProperty(RDF.type, OWLResults.TestRun)
//                 .addProperty(RDF.type, resultType)
//                 .addProperty(OWLResults.test, test)
//                 .addProperty(OWLResults.system, jena2);

        }

    }
    
}

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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