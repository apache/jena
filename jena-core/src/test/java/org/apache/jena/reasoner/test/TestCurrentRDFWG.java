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

package org.apache.jena.reasoner.test;

import java.io.IOException ;

import junit.framework.TestCase ;
import junit.framework.TestSuite ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.reasoner.ReasonerFactory ;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory ;
import org.apache.jena.shared.impl.JenaParameters ;
import org.apache.jena.vocabulary.OWLResults ;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.jena.vocabulary.ReasonerVocabulary ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Test the default RDFS reasoner against the current set of working group tests
 */
public class TestCurrentRDFWG extends ReasonerTestBase {

    /** Location of the test file directory */
    public static final String TEST_DIR = "testing/wg20031010/";
//    public static final String TEST_DIR = "testing/wg/";

    /** The base URI for the results file */
    public static String BASE_RESULTS_URI = "http://jena.apache.org/data/rdf-results.rdf";

    /** The model describing the results of the run */
    Model testResults;

    /** The resource which acts as a description for the Jena instance being tested */
    Resource jena;

    protected static Logger logger = LoggerFactory.getLogger(TestCurrentRDFWG.class);

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
        jena = testResults.createResource(BASE_RESULTS_URI + "#jena2");
        jena.addProperty(RDFS.label, "Jena2");
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
            .addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "full");
            constructRDFWGtests(suite, RDFSRuleReasonerFactory.theInstance(), config);

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
        JenaParameters.enableWhitespaceCheckingOfTypedLiterals = true;
        WGReasonerTester tester = new WGReasonerTester("Manifest.rdf", TEST_DIR);
        for ( String test : tester.listTests() )
        {
            suite.addTest( new TestReasonerWG( tester, test, rf, config ) );
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
        @Override
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
