/******************************************************************
 * File:        TestTrialOWLRules.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestTrialOWLRules.java,v 1.7 2003-07-18 12:50:50 chris-dollin Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.ModelLoader;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.*;

/**
 * Test suite to test experimental versions of the OWL reasoner, not 
 * included in the master regression test suite.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2003-07-18 12:50:50 $
 */
public class TestTrialOWLRules extends TestCase {

    /** The name of the manifest file to test */
    protected String manifest;
       
    /** Flag to control whether tracing and logging enabled */
    protected static boolean enableTracing = false;
    
    /** Flag to control whether to print performance stats as we go */
    protected static boolean printStats = true;
    
    /** Configuration spec for the reasoner under test */
    protected static Resource configuration;
    
    static {
        Model m = ModelFactory.createDefaultModel();
        configuration = m.createResource(GenericRuleReasonerFactory.URI);
        configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
        configuration.addProperty(ReasonerVocabulary.PROPruleSet, "etc/owl-fb-test.rules");
        configuration.addProperty(ReasonerVocabulary.PROPenableOWLTranslation, true);
    }
    
    /**
     * Boilerplate for junit
     */ 
    public TestTrialOWLRules( String manifest ) {
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
        suite.addTest(new TestTrialOWLRules("SymmetricProperty/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("FunctionalProperty/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("FunctionalProperty/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("FunctionalProperty/Manifest003.rdf"));
        suite.addTest(new TestTrialOWLRules("InverseFunctionalProperty/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("InverseFunctionalProperty/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("InverseFunctionalProperty/Manifest003.rdf"));
        suite.addTest(new TestTrialOWLRules("rdf-charmod-uris/Manifest.rdf"));
        suite.addTest(new TestTrialOWLRules("I5.5/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("I5.5/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("I5.5/Manifest003.rdf"));
        suite.addTest(new TestTrialOWLRules("I5.5/Manifest004.rdf"));
        suite.addTest(new TestTrialOWLRules("inverseOf/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("TransitiveProperty/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("equivalentClass/Manifest001.rdf"));   // bx - long
        suite.addTest(new TestTrialOWLRules("equivalentClass/Manifest002.rdf"));    // bx - long but terminates
        suite.addTest(new TestTrialOWLRules("equivalentClass/Manifest003.rdf"));    // bx - long but terminates
        suite.addTest(new TestTrialOWLRules("equivalentClass/Manifest005.rdf"));  // bx - timeout
        suite.addTest(new TestTrialOWLRules("equivalentProperty/Manifest001.rdf"));    // bx - long but terminates
        suite.addTest(new TestTrialOWLRules("equivalentProperty/Manifest002.rdf"));    // bx - long but terminates
        suite.addTest(new TestTrialOWLRules("equivalentProperty/Manifest003.rdf"));
        suite.addTest(new TestTrialOWLRules("I4.6/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("I4.6/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("I5.1/Manifest001.rdf"));   // bx - v. long but terminates
        suite.addTest(new TestTrialOWLRules("I5.24/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("I5.24/Manifest002-mod.rdf"));
        suite.addTest(new TestTrialOWLRules("equivalentProperty/Manifest006.rdf"));
        suite.addTest(new TestTrialOWLRules("intersectionOf/Manifest001.rdf")); // bx - takes a long time

        // Disjointness tests
        suite.addTest(new TestTrialOWLRules("differentFrom/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("disjointWith/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("disjointWith/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("AllDifferent/Manifest001.rdf")); // bx gets lost

        // Restriction tests
        suite.addTest(new TestTrialOWLRules("allValuesFrom/Manifest001.rdf"));    // bx - long but terminates
        suite.addTest(new TestTrialOWLRules("allValuesFrom/Manifest002.rdf"));   // bx - slow
        suite.addTest(new TestTrialOWLRules("someValuesFrom/Manifest002.rdf"));   // bx - slow
        suite.addTest(new TestTrialOWLRules("maxCardinality/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("maxCardinality/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("FunctionalProperty/Manifest005-mod.rdf"));
        suite.addTest(new TestTrialOWLRules("I5.24/Manifest004-mod.rdf"));  // bx - long
        suite.addTest(new TestTrialOWLRules("localtests/Manifest001.rdf"));      // bx - long but terminates
        suite.addTest(new TestTrialOWLRules("localtests/Manifest002.rdf"));   // bx - long but terminates
        suite.addTest(new TestTrialOWLRules("cardinality/Manifest001-mod.rdf")); // bx gets lost
        suite.addTest(new TestTrialOWLRules("cardinality/Manifest002-mod.rdf")); // bx gets lost
        suite.addTest(new TestTrialOWLRules("cardinality/Manifest003-mod.rdf")); // bx gets lost
        suite.addTest(new TestTrialOWLRules("cardinality/Manifest004-mod.rdf")); // bx gets lost
        suite.addTest(new TestTrialOWLRules("I5.24/Manifest003-mod.rdf"));
        suite.addTest(new TestTrialOWLRules("cardinality/Manifest005-mod.rdf")); // bx gets lost
        suite.addTest(new TestTrialOWLRules("cardinality/Manifest006-mod.rdf")); // bx gets lost
        suite.addTest(new TestTrialOWLRules("equivalentClass/Manifest004.rdf"));  // bx - timeout
        
        // Needs prototype creation rule
//        suite.addTest(new TestTrialOWLRules("someValuesFrom/Manifest001.rdf")); // bx needs creation rule
        
        // Duplications of tests included earlier
//        suite.addTest(new TestTrialOWLRules("differentFrom/Manifest002.rdf"));  // Duplication of AllDifferent#1
//        suite.addTest(new TestTrialOWLRules("distinctMembers/Manifest001.rdf"));  // Duplication of AllDifferent#1
        
        // Consistency tests - not yet implemented by tester
//      suite.addTest(new TestTrialOWLRules("I5.3/Manifest005.rdf"));
//      suite.addTest(new TestTrialOWLRules("I5.3/Manifest006.rdf"));
//      suite.addTest(new TestTrialOWLRules("I5.3/Manifest007.rdf"));
//      suite.addTest(new TestTrialOWLRules("I5.3/Manifest008.rdf"));
//      suite.addTest(new TestTrialOWLRules("I5.3/Manifest009.rdf"));
//      suite.addTest(new TestTrialOWLRules("Nothing/Manifest001.rdf"));
//      suite.addTest(new TestTrialOWLRules("miscellaneous/Manifest001.rdf"));
//      suite.addTest(new TestTrialOWLRules("miscellaneous/Manifest002.rdf"));
        
        // Non-feature tests
//      suite.addTest(new TestTrialOWLRules("I3.2/Manifest001.rdf"));
//      suite.addTest(new TestTrialOWLRules("I3.2/Manifest002.rdf"));
//      suite.addTest(new TestTrialOWLRules("I3.2/Manifest003.rdf"));
//      suite.addTest(new TestTrialOWLRules("I3.4/Manifest001.rdf"));
//      suite.addTest(new TestTrialOWLRules("I4.1/Manifest001.rdf"));

        // Outside (f)lite set - hasValue, oneOf, complementOf, unionOf
        /*
        suite.addTest(new TestTrialOWLRules("unionOf/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("unionOf/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("oneOf/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("oneOf/Manifest002.rdf"));
        suite.addTest(new TestTrialOWLRules("oneOf/Manifest003.rdf"));
        suite.addTest(new TestTrialOWLRules("oneOf/Manifest004.rdf"));
        suite.addTest(new TestTrialOWLRules("complementOf/Manifest001.rdf"));
        suite.addTest(new TestTrialOWLRules("FunctionalProperty/Manifest004.rdf"));
        suite.addTest(new TestTrialOWLRules("InverseFunctionalProperty/Manifest004.rdf"));
        suite.addTest(new TestTrialOWLRules("equivalentClass/Manifest007.rdf"));
        suite.addTest(new TestTrialOWLRules("equivalentClass/Manifest006.rdf"));
        suite.addTest(new TestTrialOWLRules("equivalentProperty/Manifest004.rdf"));
        suite.addTest(new TestTrialOWLRules("equivalentProperty/Manifest005.rdf"));
        suite.addTest(new TestTrialOWLRules("Nothing/Manifest002.rdf"));
        */
        
        return suite;
    }  
   
    /**
     * The test runner
     */
    protected void runTest() throws IOException {
        OWLWGTester tester = new OWLWGTester(GenericRuleReasonerFactory.theInstance(), this, configuration);
//        OWLWGTester tester = new OWLWGTester(OWLExptRuleReasonerFactory.theInstance(), this, null);
        tester.runTests(manifest, enableTracing, printStats);
    }

    /**
     * Boiler plate code for loading up and exploring a specific test case
     * for use during debugging.
     */
    public static void main(String[] args) {
        Model premises = ModelLoader.loadModel("file:testing/wg/someValuesFrom/premises001.rdf");
        Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
        InfModel conclusions = ModelFactory.createInfModel(reasoner, premises);
        
        System.out.println("Premises = ");
        for (Iterator i = premises.listStatements(); i.hasNext(); ) {
            System.out.println(" - " + i.next());
        }
        
        Resource i = conclusions.getResource("http://www.w3.org/2002/03owlt/someValuesFrom/premises001#i");
        Property p = conclusions.getProperty("http://www.w3.org/2002/03owlt/someValuesFrom/premises001#p");
        Resource c = conclusions.getResource("http://www.w3.org/2002/03owlt/someValuesFrom/premises001#c");
        Resource r = conclusions.getResource("http://www.w3.org/2002/03owlt/someValuesFrom/premises001#r");
        Resource v = (Resource)i.getRequiredProperty(p).getObject();
        System.out.println("Value of i.p = " + v);
        System.out.println("Types of v are: ");
        for (Iterator it2 = conclusions.listStatements(v, RDF.type, (RDFNode)null); it2.hasNext(); ) {
            System.out.println(" - " + it2.next());
        }
//        System.out.println("Things of type r are: ");
//        for (Iterator it = conclusions.listStatements(null, RDF.type, r); it.hasNext(); ) {
//            System.out.println(" - " + it.next());
//        }
//        System.out.println("Types of i are: ");
//        for (Iterator it = conclusions.listStatements(i, RDF.type, (RDFNode)null); it.hasNext(); ) {
//            System.out.println(" - " + it.next());
//        }
//        System.out.println("Things of type r are: ");
//        for (Iterator it = conclusions.listStatements(null, RDF.type, r); it.hasNext(); ) {
//            System.out.println(" - " + it.next());
//        }
        
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