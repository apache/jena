/******************************************************************
 * File:        TestRDFSReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestRDFSReasoners.java,v 1.2 2003-06-19 20:46:56 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.*;
import com.hp.hpl.jena.reasoner.rulesys.RDFSExptRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RDFSFBRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

/**
 * Test the set of admissable RDFS reasoners.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-06-19 20:46:56 $
 */
public class TestRDFSReasoners extends TestCase {
    
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(TestReasoners.class);

    /**
     * Boilerplate for junit
     */ 
    public TestRDFSReasoners( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        try {
            
            constructRDFWGtests(suite, RDFSReasonerFactory.theInstance(), null);
            constructQuerytests(suite, "rdfs/manifest.rdf", RDFSReasonerFactory.theInstance(), null);

            // FB reasoner doesn't support validation so the full set of wg tests are
            // comment out            
//            constructRDFWGtests(suite, RDFSFBRuleReasonerFactory.theInstance(), null);
            constructQuerytests(suite, "rdfs/manifest-nodirect-noresource.rdf", RDFSFBRuleReasonerFactory.theInstance(), null);
            
//            constructRDFWGtests(suite, RDFSExptRuleReasonerFactory.theInstance(), null);
            constructQuerytests(suite, "rdfs/manifest-nodirect-noresource.rdf", RDFSExptRuleReasonerFactory.theInstance(), null);
            
            suite.addTest(new TestRDFSMisc(RDFSReasonerFactory.theInstance(), null));
            
        } catch (IOException e) {
            // failed to even built the test harness
            logger.error("Failed to construct RDFS test harness", e);
        }
        return suite;
    }  
    
    /**
     * Building the query tests for the given reasoner.
     */
    private static void constructQuerytests(TestSuite suite, String manifest, ReasonerFactory rf, Model config) throws IOException {
        ReasonerTester tester = new ReasonerTester(manifest);
        for (Iterator i = tester.listTests().iterator(); i.hasNext(); ) {
            String test = (String)i.next();
            suite.addTest(new TestReasonerFromManifest(tester, test, rf, config));
        }
    }
    
    /**
     * Building the working group tests for the given reasoner.
     */
    private static void constructRDFWGtests(TestSuite suite, ReasonerFactory rf, Model config) throws IOException {
        WGReasonerTester tester = new WGReasonerTester("Manifest.rdf");
        for (Iterator i = tester.listTests().iterator(); i.hasNext(); ) {
            String test = (String)i.next();
            suite.addTest(new TestReasonerWG(tester, test, rf, config));
        }
    }
        
    /**
     * Inner class defining a test framework for invoking a single locally
     * defined query-over-inference test.
     */
    static class TestReasonerFromManifest extends TestCase {
        
        /** The tester which already has the test manifest loaded */
        ReasonerTester tester;
        
        /** The name of the specific test to run */
        String test;
        
        /** The factory for the reasoner type under test */
        ReasonerFactory reasonerFactory;
        
        /** An optional configuration model */
        Model config;
        
        /** Constructor */
        TestReasonerFromManifest(ReasonerTester tester, String test, 
                                 ReasonerFactory reasonerFactory, Model config) {
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
            tester.runTest(test, reasonerFactory, this, config);
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
        Model config;
        
        /** Constructor */
        TestReasonerWG(WGReasonerTester tester, String test, 
                                 ReasonerFactory reasonerFactory, Model config) {
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
            tester.runTest(test, reasonerFactory, this, config);
        }

    }
    
    /**
     * Inner class defining the misc extra tests needed to check out a
     * candidate RDFS reasoner.
     */
    static class TestRDFSMisc extends TestCase {
        
        /** The factory for the reasoner type under test */
        ReasonerFactory reasonerFactory;
        
        /** An optional configuration model */
        Model config;
        
        /** Constructor */
        TestRDFSMisc(ReasonerFactory reasonerFactory, Model config) {
            super("TestRDFSMisc");
            this.reasonerFactory = reasonerFactory;
            this.config = config;
        }

        /**
         * The test runner
         */
        public void runTest() throws IOException {
            ReasonerTester tester = new ReasonerTester("rdfs/manifest.rdf");
            // Test effect of switching off property scan - should break container property test case
            Model configuration = new ModelMem();
            if (config != null) configuration.add(config);
            configuration.createResource(RDFSReasonerFactory.URI)
                         .addProperty(RDFSReasonerFactory.scanProperties, "false");
            assertTrue("RDFS reasoner tests", 
                        !tester.runTest(NAMESPACE + "rdfs/test17", reasonerFactory, null, configuration));
        
            // Check capabilities description
            Reasoner r = reasonerFactory.create(null);
            assertTrue(r.supportsProperty(RDFS.subClassOf));
            assertTrue(r.supportsProperty(RDFS.domain));
            assertTrue(r.supportsProperty(RDFS.range));

            // Datatype tests
            assertTrue( ! doTestRDFSDTRange("dttest1.nt", reasonerFactory));
            assertTrue( ! doTestRDFSDTRange("dttest2.nt", reasonerFactory));
            assertTrue( doTestRDFSDTRange("dttest3.nt", reasonerFactory));
        }

        /**
         * Helper for dt range testing - loads a file, validates it using RDFS/DT
         * and returns error status of the result
         */
        private boolean doTestRDFSDTRange(String file, ReasonerFactory rf) throws IOException {
            Model m = WGReasonerTester.loadFile("../reasoners/rdfs/" + file);
            InfGraph g = rf.create(null).bind(m.getGraph());
            ValidityReport report = g.validate();
            if (!report.isValid()) {
                logger.debug("Validation error report:");
                for (Iterator i = report.getReports(); i.hasNext(); ) {
                    logger.debug(i.next().toString());
                }
            }
            return report.isValid();
        }
          
    }
    
}



/*
    (c) Copyright Hewlett-Packard Company 2003
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