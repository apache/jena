/******************************************************************
 * File:        TestOWLRules.java
 * Created by:  Dave Reynolds
 * Created on:  11-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestOWLRules.java,v 1.14 2003-08-14 17:48:25 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;

/**
 * Test suite to test the production rule version of the OWL reasoner
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.14 $ on $Date: 2003-08-14 17:48:25 $
 */
public class TestOWLRules extends TestCase {

    /** The name of the manifest file to test */
    protected String manifest;
       
    /** Set to true to test the pure forward instead of a hybrid reasoner */
    protected static boolean testForward = false;
    
    /** Set to true to test the experimental hybrid instead of the released one */
    protected static boolean testExpt = false;
    
    /** Flag to control whether tracing and logging enabled */
    protected static boolean enableTracing = false;
    
    /** Flag to control whether to print performance stats as we go */
    protected static boolean printStats = false;
    
    /**
     * Boilerplate for junit
     */ 
    public TestOWLRules( String manifest ) {
        super( manifest ); 
        this.manifest = manifest;
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        
        // Basic property and equivalence tests
//        suite.addTest(new TestOWLRules("SymmetricProperty/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("FunctionalProperty/Manifest001.rdf"));
        suite.addTest(new TestOWLRules("FunctionalProperty/Manifest002.rdf"));
//        suite.addTest(new TestOWLRules("FunctionalProperty/Manifest003.rdf"));
//        suite.addTest(new TestOWLRules("InverseFunctionalProperty/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("InverseFunctionalProperty/Manifest002.rdf"));
//        suite.addTest(new TestOWLRules("InverseFunctionalProperty/Manifest003.rdf"));
//        suite.addTest(new TestOWLRules("rdf-charmod-uris/Manifest.rdf"));
//        suite.addTest(new TestOWLRules("I5.5/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("I5.5/Manifest002.rdf"));
//        suite.addTest(new TestOWLRules("I5.5/Manifest003.rdf"));
//        suite.addTest(new TestOWLRules("I5.5/Manifest004.rdf"));
//        suite.addTest(new TestOWLRules("inverseOf/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("TransitiveProperty/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("equivalentClass/Manifest001.rdf"));   // bx - long
//        suite.addTest(new TestOWLRules("equivalentClass/Manifest002.rdf"));    // bx - long but terminates
//        suite.addTest(new TestOWLRules("equivalentClass/Manifest003.rdf"));    // bx - long but terminates
//        suite.addTest(new TestOWLRules("equivalentClass/Manifest005.rdf"));  // bx - timeout
//        suite.addTest(new TestOWLRules("equivalentProperty/Manifest001.rdf"));    // bx - long but terminates
//        suite.addTest(new TestOWLRules("equivalentProperty/Manifest002.rdf"));    // bx - long but terminates
//        suite.addTest(new TestOWLRules("equivalentProperty/Manifest003.rdf"));
//        suite.addTest(new TestOWLRules("I4.6/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("I4.6/Manifest002.rdf"));
//        suite.addTest(new TestOWLRules("I5.1/Manifest001.rdf"));   // bx - v. long but terminates
//        suite.addTest(new TestOWLRules("I5.24/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("I5.24/Manifest002-mod.rdf"));
//        suite.addTest(new TestOWLRules("equivalentProperty/Manifest006.rdf"));
//        suite.addTest(new TestOWLRules("intersectionOf/Manifest001.rdf")); // bx - takes a long time
//
//        // Disjointness tests
//        suite.addTest(new TestOWLRules("differentFrom/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("disjointWith/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("disjointWith/Manifest002.rdf"));
//        suite.addTest(new TestOWLRules("AllDifferent/Manifest001.rdf")); // bx gets lost
//
//        // Restriction tests
//        suite.addTest(new TestOWLRules("allValuesFrom/Manifest001.rdf"));    // bx - long but terminates
//        suite.addTest(new TestOWLRules("allValuesFrom/Manifest002.rdf"));   // bx - slow
//        suite.addTest(new TestOWLRules("someValuesFrom/Manifest002.rdf"));   // bx - slow
//        suite.addTest(new TestOWLRules("maxCardinality/Manifest001.rdf"));
//        suite.addTest(new TestOWLRules("maxCardinality/Manifest002.rdf"));
//        suite.addTest(new TestOWLRules("FunctionalProperty/Manifest005-mod.rdf"));
//        suite.addTest(new TestOWLRules("I5.24/Manifest004-mod.rdf"));  // bx - long
//        suite.addTest(new TestOWLRules("localtests/Manifest001.rdf"));      // bx - long but terminates
//        suite.addTest(new TestOWLRules("localtests/Manifest002.rdf"));   // bx - long but terminates
//        suite.addTest(new TestOWLRules("cardinality/Manifest001-mod.rdf")); // bx gets lost
//        suite.addTest(new TestOWLRules("cardinality/Manifest002-mod.rdf")); // bx gets lost
//        suite.addTest(new TestOWLRules("cardinality/Manifest003-mod.rdf")); // bx gets lost
//        suite.addTest(new TestOWLRules("cardinality/Manifest004-mod.rdf")); // bx gets lost
//        suite.addTest(new TestOWLRules("I5.24/Manifest003-mod.rdf"));
//        suite.addTest(new TestOWLRules("cardinality/Manifest005-mod.rdf")); // bx gets lost
//        suite.addTest(new TestOWLRules("cardinality/Manifest006-mod.rdf")); // bx gets lost
//        suite.addTest(new TestOWLRules("equivalentClass/Manifest004.rdf"));  // bx - timeout
//        
//        // Needs prototype creation rule
//        suite.addTest(new TestOWLRules("someValuesFrom/Manifest001.rdf")); // bx needs creation rule
        
        // Duplications of tests included earlier
//        suite.addTest(new TestOWLRules("differentFrom/Manifest002.rdf"));  // Duplication of AllDifferent#1
//        suite.addTest(new TestOWLRules("distinctMembers/Manifest001.rdf"));  // Duplication of AllDifferent#1
        
        // Consistency tests - not yet implemented by tester
//      suite.addTest(new TestOWLRules("I5.3/Manifest005.rdf"));
//      suite.addTest(new TestOWLRules("I5.3/Manifest006.rdf"));
//      suite.addTest(new TestOWLRules("I5.3/Manifest007.rdf"));
//      suite.addTest(new TestOWLRules("I5.3/Manifest008.rdf"));
//      suite.addTest(new TestOWLRules("I5.3/Manifest009.rdf"));
//      suite.addTest(new TestOWLRules("Nothing/Manifest001.rdf"));
//      suite.addTest(new TestOWLRules("miscellaneous/Manifest001.rdf"));
//      suite.addTest(new TestOWLRules("miscellaneous/Manifest002.rdf"));
        
        // Non-feature tests
//      suite.addTest(new TestOWLRules("I3.2/Manifest001.rdf"));
//      suite.addTest(new TestOWLRules("I3.2/Manifest002.rdf"));
//      suite.addTest(new TestOWLRules("I3.2/Manifest003.rdf"));
//      suite.addTest(new TestOWLRules("I3.4/Manifest001.rdf"));
//      suite.addTest(new TestOWLRules("I4.1/Manifest001.rdf"));

        // Outside (f)lite set - hasValue, oneOf, complementOf, unionOf
        /*
        suite.addTest(new TestOWLRules("unionOf/Manifest001.rdf"));
        suite.addTest(new TestOWLRules("unionOf/Manifest002.rdf"));
        suite.addTest(new TestOWLRules("oneOf/Manifest001.rdf"));
        suite.addTest(new TestOWLRules("oneOf/Manifest002.rdf"));
        suite.addTest(new TestOWLRules("oneOf/Manifest003.rdf"));
        suite.addTest(new TestOWLRules("oneOf/Manifest004.rdf"));
        suite.addTest(new TestOWLRules("complementOf/Manifest001.rdf"));
        suite.addTest(new TestOWLRules("FunctionalProperty/Manifest004.rdf"));
        suite.addTest(new TestOWLRules("InverseFunctionalProperty/Manifest004.rdf"));
        suite.addTest(new TestOWLRules("equivalentClass/Manifest007.rdf"));
        suite.addTest(new TestOWLRules("equivalentClass/Manifest006.rdf"));
        suite.addTest(new TestOWLRules("equivalentProperty/Manifest004.rdf"));
        suite.addTest(new TestOWLRules("equivalentProperty/Manifest005.rdf"));
        suite.addTest(new TestOWLRules("Nothing/Manifest002.rdf"));
        */
        
        return suite;
    }  

    /**
     * Test the basic functioning of an RDFS reasoner
     */
    /*
    public void testOWLReasoner() throws IOException {
        OWLWGTester tester = new OWLWGTester(OWLRuleReasonerFactory.theInstance(), this, null);
        tester.runTests("SymmetricProperty/Manifest001.rdf");
    }
    */
    
    /**
     * The test runner
     */
    protected void runTest() throws IOException {
        if (testForward) {
            OWLWGTester tester = new OWLWGTester(OWLRuleReasonerFactory.theInstance(), this, null);
            tester.runTests(manifest, enableTracing, printStats);
//            OWLRuleReasoner.printStats();
        } else {
            ReasonerFactory rf = testExpt ? OWLExptRuleReasonerFactory.theInstance() 
                                          : OWLFBRuleReasonerFactory.theInstance();
            OWLWGTester tester = new OWLWGTester(rf, this, null);
            tester.runTests(manifest, enableTracing, printStats);
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

