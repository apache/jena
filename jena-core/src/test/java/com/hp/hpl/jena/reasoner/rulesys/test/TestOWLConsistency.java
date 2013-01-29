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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.FileManager;

//import java.util.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the preliminary OWL validation rules.
 */
public class TestOWLConsistency extends TestCase {
     
    /** The tbox/ontology file to test against sample data */
    public static final String testTbox = "file:testing/reasoners/owl/tbox.owl";
    
    /** A cached copy of the bound reasoner */
    public static Reasoner reasonerCache;
     
    /**
     * Boilerplate for junit
     */ 
    public TestOWLConsistency( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestOWLConsistency.class ); 
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestOWLConsistency( "testInconsistent5" ));
//        return suite;
    }  

    /**
     * Create, or retrieve from cache, an OWL reasoner already bound
     * to the test tbox.
     */
    public Reasoner makeReasoner() {
        if (reasonerCache == null) {
            Model tbox = FileManager.get().loadModel(testTbox);
            reasonerCache = ReasonerRegistry.getOWLReasoner().bindSchema(tbox.getGraph());
        }
        return reasonerCache;
    }
    
    /**
     * Should be consistent.
     */
    public void testConsistent() {
        assertTrue(doTestOn("file:testing/reasoners/owl/consistentData.rdf"));
    }
    
    /**
     * Should find problem due to overlap of disjoint classes.
     */
    public void testInconsistent1() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent1.rdf"));
    }
    
    /**
     * Should find problem due to type violations
     */
    public void testInconsistent2() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent2.rdf"));
    }
    
    /**
     * Should find problem due to count violations
     */
    public void testInconsistent3() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent3.rdf"));
    }
    
    /**
     * Should find distinct values for a functional property
     */
    public void testInconsistent4() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent4.rdf"));
    }
    
    /**
     * Should find type clash due to allValuesFrom rdfs:Literal
     */
    public void testInconsistent5() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent5.rdf"));
    }
    
    /**
     * Should find distinct literal values for a functional property
     * via an indirect sameAs
     */
    public void testInconsistent7() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent7.rdf"));
    }
    
    /**
     * Run a single consistency test on the given data file.
     */
    private boolean doTestOn(String dataFile) {
//        System.out.println("Test: " + dataFile);
        Model data = FileManager.get().loadModel(dataFile);
        InfModel infmodel = ModelFactory.createInfModel(makeReasoner(), data);
        ValidityReport reportList = infmodel.validate();
        /* Debug only
        if (reportList.isValid()) {
            System.out.println("No reported problems");
        } else {
            for (Iterator i = reportList.getReports(); i.hasNext(); ) {
                ValidityReport.Report report = (ValidityReport.Report)i.next();
                System.out.println("- "  + report);
            }
        }
        */
        return reportList.isValid();
    }
}
