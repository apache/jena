/******************************************************************
 * File:        OWLUnitTest.java
 * Created by:  Dave Reynolds
 * Created on:  19-Mar-2004
 * 
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
 * [See end of file]
 * $Id: OWLUnitTest.java,v 1.2 2004-11-29 14:35:50 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.test.OWLWGTester;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Version of the OWL unit tests used during development of the mini ruleset.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2004-11-29 14:35:50 $
 */
public class OWLUnitTest extends TestCase {
    
//  --------------  statics defining the whole test suite ---------------------
    
    /** The set of reasoner(factories) to test */
    public static ReasonerFactory[] reasonerFactories = {
        OWLFBRuleReasonerFactory.theInstance(),
        OWLMicroReasonerFactory.theInstance(),
        OWLMiniReasonerFactory.theInstance()
    };
    
    /** bit flag to indicate the test should be passed by the default reasoner */
    public static final int FB = 1;

    /** bit flag to indicate the test should be passed by the mini reasoner */
    public static final int MINI = 2;

    /** bit flag to indicate the test should be passed by the micro reasoner */
    public static final int MICRO = 4;
    
    /** The set of test cases to be used */
    public static TestDef[] testDefs = {
        // subClass
        new TestDef("localtests/ManifestSubclass001.rdf", FB | MICRO | MINI),
        
        // equivalentClass
        new TestDef("equivalentClass/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("equivalentClass/Manifest002.rdf", FB | MICRO | MINI),
        new TestDef("equivalentClass/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("equivalentClass/Manifest004.rdf", FB | MINI),        // Requires prototypes
        new TestDef("equivalentClass/Manifest005.rdf", FB | MICRO | MINI),
        new TestDef("localtests/ManifestRestriction001.rdf", FB | MICRO | MINI),

        // intersectionOf
        new TestDef("intersectionOf/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("localtests/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("localtests/Manifest002.rdf", FB | MICRO | MINI),

        // unionOf
        new TestDef("unionOf/Manifest001.rdf", MICRO | MINI ),
        
        // This could be supported but isn't at the moment
        // new TestDef("unionOf/Manifest002.rdf", FB | MICRO | MINI),
        
        // Property axioms
        new TestDef("SymmetricProperty/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("SymmetricProperty/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("inverseOf/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("TransitiveProperty/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("localtests/Manifest005.rdf", FB | MICRO | MINI),
        
        // Equality related
        new TestDef("FunctionalProperty/Manifest001.rdf", FB | MINI),
        new TestDef("FunctionalProperty/Manifest002.rdf", FB | MINI),
        new TestDef("FunctionalProperty/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("InverseFunctionalProperty/Manifest001.rdf", FB | MINI),
        new TestDef("InverseFunctionalProperty/Manifest002.rdf", FB | MINI),
        new TestDef("InverseFunctionalProperty/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("I5.1/Manifest001.rdf", FB | MINI),
        
        new TestDef("rdf-charmod-uris/Manifest.rdf", FB | MICRO | MINI),
        new TestDef("I4.6/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("I5.5/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("I5.5/Manifest002.rdf", FB | MICRO | MINI),
        new TestDef("I5.5/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("I5.5/Manifest004.rdf", FB | MICRO | MINI),
        new TestDef("inverseOf/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("TransitiveProperty/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("equivalentProperty/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("equivalentProperty/Manifest002.rdf", FB | MICRO | MINI),
        new TestDef("equivalentProperty/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("I5.24/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("I5.24/Manifest002-mod.rdf", FB | MICRO | MINI),
        new TestDef("equivalentProperty/Manifest006.rdf", FB | MICRO | MINI),

        // owl:Nothing
        new TestDef("I5.2/Manifest002.rdf", FB | MINI),
        
        // Disjointness tests
        new TestDef("differentFrom/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("disjointWith/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("disjointWith/Manifest002.rdf", FB | MICRO | MINI),
        new TestDef("AllDifferent/Manifest001.rdf", FB | MICRO | MINI),

        // Restriction tests
        new TestDef("allValuesFrom/Manifest001.rdf", FB | MINI),        // Want to move this into MICRO
        new TestDef("allValuesFrom/Manifest002.rdf", FB | MICRO | MINI),
        new TestDef("someValuesFrom/Manifest002.rdf", FB | MICRO | MINI),
        new TestDef("maxCardinality/Manifest001.rdf", FB | MICRO | MINI),
        new TestDef("maxCardinality/Manifest002.rdf", FB | MICRO | MINI),
        new TestDef("FunctionalProperty/Manifest005-mod.rdf", FB | MICRO | MINI),
        new TestDef("I5.24/Manifest004-mod.rdf", FB | MINI),
        new TestDef("cardinality/Manifest001-mod.rdf", FB | MINI),
        new TestDef("cardinality/Manifest002-mod.rdf", FB | MINI),
        new TestDef("cardinality/Manifest003-mod.rdf", FB | MINI),
        new TestDef("cardinality/Manifest004-mod.rdf", FB | MINI),
        new TestDef("I5.24/Manifest003-mod.rdf", FB | MICRO | MINI),
        new TestDef("cardinality/Manifest005-mod.rdf", FB | MINI),
        new TestDef("cardinality/Manifest006-mod.rdf", FB | MINI),
        
        // Needs bNode creation rule
        new TestDef("someValuesFrom/Manifest001.rdf", FB ),
    
         // New local tests
        new TestDef("localtests/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("localtests/Manifest004.rdf", FB | MINI), // Requires equality
        new TestDef("localtests/Manifest006.rdf", FB ), // a oneOF case
    };

//  --------------  instance variables for a single test ----------------------    

    /** The name of the manifest file to test */
    protected String manifest;
    
    /** The reasoner factory under test */
    protected ReasonerFactory reasonerFactory;
    
    /**
     * Boilerplate for junit
     */ 
    public OWLUnitTest( String manifest, ReasonerFactory rf) {
        super( manifest ); 
        this.manifest = manifest;
        this.reasonerFactory = rf;
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        for (int i = 0; i < reasonerFactories.length; i++) {
            mergeSuite(suite, suite(reasonerFactories[i]));
        }
        return suite;
    }
    
    /**
     * Merge two TestSuites, why isn't this in junit?
     */
    public static TestSuite mergeSuite(TestSuite accumulator, TestSuite additions) {
        int limit = additions.testCount();
        for (int i = 0; i < limit; i++) {
            accumulator.addTest(additions.testAt(i));
        }
        return accumulator;
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite(ReasonerFactory rf) {
        TestSuite suite = new TestSuite();
        for (int i = 0; i < testDefs.length; i++) {
            TestDef test = testDefs[i];
            if (test.applicableTo(rf)) {
                suite.addTest(test.getTestCase(rf));
            }
        }
        return suite;
    }
    
    /**
     * The test runner
     */
    protected void runTest() throws IOException {
//        System.out.println(" - " + manifest + " using " + reasonerFactory.getURI());
        OWLWGTester tester = new OWLWGTester(reasonerFactory, this, null);
        tester.runTests(manifest, false, false);
    }
    
    /**
     * Inner class - use to represent a single test case and which reasoners
     * it is relevant to.
     */
    static class TestDef {
        /** The manifest file, relative to global test dir */
        public String manifest;  
        
        /** Bitmap of the reasoners this test is relevant to */
        public int validFor;
         
        /** Constructor */
        public TestDef(String manifest, int validFor) {
            this.manifest = manifest;
            this.validFor = validFor;
        }
        
        /** Return the bitflag corresponding to the given reasoner factory */
        public int flagFor(ReasonerFactory rf) {
            if (rf.equals(OWLFBRuleReasonerFactory.theInstance())) {
                return FB;
            } else if (rf.equals(OWLMiniReasonerFactory.theInstance())) {
                return MINI;
            } else if (rf.equals(OWLMicroReasonerFactory.theInstance())) {
                return MICRO;
            } else {
                throw new ReasonerException("Unrecognized OWL reasoner config in unit test");
            }
        }
        
        /** Return true if the test is relevant to this reasoner factory */
        public boolean applicableTo(ReasonerFactory rf) {
            return (validFor & flagFor(rf)) != 0;
        }
        
        /** Return a TestCase for applying this reasoner to this defined test */
        public TestCase getTestCase(ReasonerFactory rf) {
            return new OWLUnitTest(manifest, rf);
        }
    }
    
}


/*
    (c) Copyright Hewlett-Packard Development Company, LP 2004
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