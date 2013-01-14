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

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.test.*;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.Iterator;
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
    
    /**
     * Time a trial list of results from an inf graph.
     */
    private static void doTiming(Reasoner r, Model tbox, Model data, String name, int loop) {
        Resource C1 = ResourceFactory.createResource("http://www.hpl.hp.com/semweb/2003/eg#C1");
        Resource C2 = ResourceFactory.createResource("http://www.hpl.hp.com/semweb/2003/eg#C2");
        
        long t1 = System.currentTimeMillis();
        int count = 0;
        for (int lp = 0; lp < loop; lp++) {
            Model m = ModelFactory.createModelForGraph(r.bindSchema(tbox.getGraph()).bind(data.getGraph()));
            count = 0;
            for (Iterator<Statement> i = m.listStatements(null, RDF.type, C1); i.hasNext(); i.next()) count++;
        }
        long t2 = System.currentTimeMillis();
        long time10 = (t2-t1)*10/loop;
        long time = time10/10;
        long timeFraction = time10 - (time*10);
        System.out.println(name + ": " + count +" results in " + time + "." + timeFraction +"ms");
//        t1 = System.currentTimeMillis();
//        for (int j = 0; j < 10; j++) {
//            count = 0;
//            for (Iterator i = m.listStatements(null, RDF.type, C1); i.hasNext(); i.next()) count++;
//        }
//        t2 = System.currentTimeMillis();
//        System.out.println(name + ": " + count + " results in " + (t2-t1)/10 +"ms");
    }    
}
