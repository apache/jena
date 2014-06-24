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

import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.test.OWLWGTester;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Version of the OWL unit tests used during development of the mini ruleset.
 */
public class OWLUnitTest extends TestCase {
    
//  --------------  statics defining the whole test suite ---------------------
    
    /** The set of reasoner(factories) to test */
    public static ReasonerFactory[] reasonerFactories = {
        OWLFBRuleReasonerFactory.theInstance(),
        OWLMicroReasonerFactory.theInstance(),
        OWLMiniReasonerFactory.theInstance()
    };
    
    /** The names of the reasoner(factories) to report in the test suite */
    public static final String[] reasonerNames = { "full", "Micro", "Mini" };
    
    /** bit flag to indicate the test should be passed by the default reasoner */
    public static final int FB = 1;

    /** bit flag to indicate the test should be passed by the mini reasoner */
    public static final int MINI = 2;

    /** bit flag to indicate the test should be passed by the micro reasoner */
    public static final int MICRO = 4;
    
    // Flags from OWLConsistencyTest, copied here for brevity
    public static final int INCONSISTENT = OWLConsistencyTest.INCONSISTENT;
    public static final int WARNINGS = OWLConsistencyTest.WARNINGS;
    public static final int CLEAN = OWLConsistencyTest.CLEAN;

    /** The set of test cases to be used */
    public static TestDef[] testDefs = {
        //  /*
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
        new TestDef("unionOf/Manifest001.rdf", FB | MICRO | MINI ),
        
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
        
        new TestDef("localtests/ManifestHv1.rdf", FB | MINI | MICRO),
        new TestDef("localtests/ManifestHv2.rdf", FB | MINI | MICRO),
        
        // Needs bNode creation rule
        new TestDef("someValuesFrom/Manifest001.rdf", FB ),
    
         // New local tests
        new TestDef("localtests/Manifest003.rdf", FB | MICRO | MINI),
        new TestDef("localtests/Manifest004.rdf", FB | MINI), // Requires equality
        new TestDef("localtests/Manifest006.rdf", FB ), // a oneOF case
        
        // Inheritance of domain/range by subProperties, inverseof
        new TestDef("localtests/Manifest007.rdf", FB | MICRO | MINI),
        new TestDef("localtests/Manifest008.rdf", FB | MICRO | MINI),

        // Consistency tests
        // clean case
        new TestDef(new OWLConsistencyTest("tbox.owl", "consistentData.rdf", CLEAN, null), FB | MICRO | MINI),
        // Instance of disjoint classes
        new TestDef(new OWLConsistencyTest("tbox.owl", "inconsistent1.rdf", INCONSISTENT, 
                ResourceFactory.createResource("http://jena.hpl.hp.com/testing/reasoners/owl#ia")), FB | MICRO |  MINI),
        // Type violation
        new TestDef(new OWLConsistencyTest("tbox.owl", "inconsistent2.rdf", INCONSISTENT, null), FB | MICRO | MINI),
        // Count violation
        new TestDef(new OWLConsistencyTest("tbox.owl", "inconsistent3.rdf", INCONSISTENT, null), FB | MINI),
        // Distinct values for functional property
        new TestDef(new OWLConsistencyTest("tbox.owl", "inconsistent4.rdf", INCONSISTENT, null), FB |  MINI),
        // Distinct literal values for functional property
        new TestDef(new OWLConsistencyTest("tbox.owl", "inconsistent6.rdf", INCONSISTENT, null), FB |  MINI),
        // Type clash - allValuesFrom rdfs:Literal
        new TestDef(new OWLConsistencyTest("tbox.owl", "inconsistent5.rdf", INCONSISTENT, null), FB | MICRO | MINI),
        // Intersection of disjoint classes                                     
        new TestDef(new OWLConsistencyTest("tbox.owl", "emptyClass1.rdf", WARNINGS, null), FB | MICRO | MINI),
        // Equivalent to Nothing
        new TestDef(new OWLConsistencyTest("tbox.owl", "emptyClass2.rdf", WARNINGS, null), FB | MICRO | MINI),
        // disjoint with Thing
        new TestDef(new OWLConsistencyTest("tbox.owl", "emptyClass3.rdf", WARNINGS, null), FB | MICRO | MINI),
    };

//  --------------  instance variables for a single test ----------------------    

    /** The name of the manifest file to test */
    protected String manifest;
    
    /** The reasoner factory under test */
    protected ReasonerFactory reasonerFactory;
    
    /**
     * Boilerplate for junit
     */ 
    public OWLUnitTest( String manifest, String rName, ReasonerFactory rf) {
        super( rName + ":" + manifest ); 
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
            String rName = reasonerNames[i];
            ReasonerFactory rf = reasonerFactories[i];
            for ( TestDef test : testDefs )
            {
                if ( test.applicableTo( rf ) )
                {
                    if ( test.spec instanceof String )
                    {
                        suite.addTest( new OWLUnitTest( (String) test.spec, rName, rf ) );
                    }
                    else if ( test.spec instanceof OWLConsistencyTest )
                    {
                        OWLConsistencyTest oct = (OWLConsistencyTest) test.spec;
                        suite.addTest( new OWLConsistencyTest( oct, rName, rf ) );
                    }
                }
            }
        }
        return suite;
    }
    
    /**
     * The test runner
     */
    @Override
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
        /** Test spec, could be a the relative URI for a manifest, or a consistecy test object */
        public Object spec;  
        
        /** Bitmap of the reasoners this test is relevant to */
        public int validFor;
         
        /** Constructor */
        public TestDef(Object spec, int validFor) {
            this.spec = spec;
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
    }
    
}
