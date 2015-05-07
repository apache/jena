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

package org.apache.jena.reasoner.rulesys.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import org.apache.jena.reasoner.* ;
import org.apache.jena.reasoner.rulesys.* ;
import org.apache.jena.reasoner.test.* ;
import org.apache.jena.vocabulary.OWL ;
import org.apache.jena.vocabulary.RDFS ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Test suite to test the production rule version of the RDFS implementation.
 * <p> The tests themselves have been replaced by an updated version
 * of the top level TestRDFSReasoners but this file is maintained for now since
 * the top level timing test can sometimes be useful. </p>
 */
public class TestRDFSRules extends TestCase {   
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    protected static Logger logger = LoggerFactory.getLogger(TestRDFSRules.class);
    
    /**
     * Boilerplate for junit
     */ 
    public TestRDFSRules( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestRDFSRules.class);
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestRDFSRules( "hiddenTestRDFSReasonerDebug" ));
//        return suite;
    }  

    /**
     * Test a single RDFS case.
     */
    public void hiddenTestRDFSReasonerDebug() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSRuleReasonerFactory.theInstance();
        
        assertTrue("RDFS hybrid-tgc reasoner test", tester.runTest("http://www.hpl.hp.com/semweb/2003/query_tester/rdfs/test11", rf, this, null));
    }

    /**
     * Test the basic functioning of the hybrid RDFS rule reasoner
     */
    public void testRDFSFBReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSFBRuleReasonerFactory.theInstance();
        assertTrue("RDFS hybrid reasoner tests", tester.runTests(rf, this, null));
    }

    /**
     * Test the basic functioning of the hybrid RDFS rule reasoner with TGC cache
     */
    public void testRDFSExptReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSRuleReasonerFactory.theInstance();
        assertTrue("RDFS experimental (hybrid+tgc) reasoner tests", tester.runTests(rf, this, null));
    }

    /**
     * Test the capabilities description.
     */
    public void testRDFSDescription() {
        ReasonerFactory rf = RDFSFBRuleReasonerFactory.theInstance();
        Reasoner r = rf.create(null);
        assertTrue(r.supportsProperty(RDFS.subClassOf));        
        assertTrue(r.supportsProperty(RDFS.domain));        
        assertTrue( ! r.supportsProperty(OWL.allValuesFrom));        
    }    
}
