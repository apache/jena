/******************************************************************
 * File:        WebOntTestHarness.java
 * Created by:  Dave Reynolds
 * Created on:  12-Sep-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: WebOntTestHarness.java,v 1.4 2003-09-16 16:04:56 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.io.*;
import java.util.*;

/**
 * Test harness for running the WebOnt working group tests relevant 
 * to the OWL rule reasoner. See also TestOWLRules which runs the
 * core WG tests as part of the routine unit tests.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-09-16 16:04:56 $
 */
public class WebOntTestHarness {

//  =======================================================================
//  Variables to control the test operations

    /** Set to true to include modified test versions */
    public static boolean includeModified = false;
    
    /** Set to true to use approved tests only */
    public static boolean approvedOnly = true;
    
//  =======================================================================
//  Internal state

    /** The total set of known tests */
    public Model testDefinitions;
    
//  =======================================================================
//  Internal constants

    /** The base directory for the working group test files to use */
    public static final String BASE_TESTDIR = "testing/wg/";
    
    /** The list of subdirectories to process (omits the rdf/rdfs dirs) */
    public static final String[] TEST_DIRS= {"AllDifferent", "AllDistinct", 
            "AnnotationProperty", "DatatypeProperty", "FunctionalProperty",
            "I3.2", "I3.4", "I4.1", "I4.5", "I4.6", "I5.1", "I5.2", "I5.21", "I5.24",
            "I5.26", "I5.3", "I5.5", "I5.8", "InverseFunctionalProperty", "Nothing", 
            "Restriction", "SymmetricProperty", "Thing", "TransitiveProperty", 
            "allValuesFrom", "amp-in-url", "cardinality", "complementOf", "datatypes", 
            "description-logic", "differentFrom", "disjointWith", "distinctMembers", 
            "equivalentClass", "equivalentProperty", "imports", 
            "intersectionOf", "inverseOf", "localtests", "maxCardinality", "miscellaneous",
            "oneOf", "oneOfDistinct", "sameAs", "sameClassAs", "sameIndividualAs", 
            "samePropertyAs", "someValuesFrom", "statement-entailment", "unionOf", 
            "xmlbase",
//            "extra-credit", 
        };
    
    /** List of tests that are blocked because they test language features beyond Lite */
    public static final String[] BLOCK_TESTS = {
        "http://www.w3.org/2002/03owlt/complementOf/Manifest001#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest901#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest903#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest902#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest904#test", 
        "http://www.w3.org/2002/03owlt/oneOf/Manifest002#test", 
        "http://www.w3.org/2002/03owlt/oneOf/Manifest003#test", 
        "http://www.w3.org/2002/03owlt/unionOf/Manifest001#test", 
        "http://www.w3.org/2002/03owlt/unionOf/Manifest002#test", 
    };
            
    /** The list of status values to include. If approvedOnly then only the first
     *  entry is allowed */
    public static final String[] STATUS_FLAGS = { "APPROVED", "PROPOSED" };
    
//  =======================================================================
//  Constructor and associated support
    
    public WebOntTestHarness() {
        testDefinitions = loadAllTestDefinitions();
    }

    /** Load all of the known manifest files into a single model */
    public static Model loadAllTestDefinitions() {
        System.out.print("Loading "); System.out.flush();
        Model testDefs = ModelFactory.createDefaultModel();
        int count = 0;
        for (int idir = 0; idir < TEST_DIRS.length; idir++) {
            File dir = new File(BASE_TESTDIR + TEST_DIRS[idir]);
            String[] manifests = dir.list(new FilenameFilter() {
                    public boolean accept(File df, String name) {
                        if (name.startsWith("Manifest") && name.endsWith(".rdf")) {
                            return includeModified || ! name.endsWith("-mod.rdf");
                        } else {
                            return false;
                        }
                    }
                });
            for (int im = 0; im < manifests.length; im++) {
                String manifest = manifests[im];
                File mf = new File(dir, manifest);
                try {
                    testDefs.read(new FileInputStream(mf), "file:" + mf);
                    count ++;
                    if (count % 8 == 0) {
                        System.out.print("."); System.out.flush();
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File not readable - " + e);
                }
            }
        }
        System.out.println("loaded");
        return testDefs;
    }
    
//  =======================================================================
//  Main control methods
    
    public static void main(String[] args) {
        WebOntTestHarness harness = new WebOntTestHarness();
        List l = harness.findTestsOfType(OWLTest.PositiveEntailmentTest);
        harness.findTestsOfType(OWLTest.NegativeEntailmentTest);
    }
    
//  =======================================================================
//  Internal helper functions
    
    /** Return a list of all tests of the given type, according to the current filters */
    public List findTestsOfType(Resource testType) {
        ArrayList result = new ArrayList();
        StmtIterator si = testDefinitions.listStatements(null, RDF.type, testType);
        while (si.hasNext()) {
            Resource test = si.nextStatement().getSubject();
            boolean accept = true;
            // Check test status
            Literal status = (Literal) test.getProperty(RDFTest.status).getObject();
            if (approvedOnly) {
                accept = status.getString().equals(STATUS_FLAGS[0]);
            } else {
                accept = false;
                for (int i = 0; i < STATUS_FLAGS.length; i++) {
                    if (status.getString().equals(STATUS_FLAGS[i])) {
                        accept = true;
                        break;
                    }
                }
            }
            // Check for blocked tests
            for (int i = 0; i < BLOCK_TESTS.length; i++) {
                if (BLOCK_TESTS[i].equals(test.toString())) {
                    accept = false; 
                }
            }
            // End of filter tests
            if (accept) {
                result.add(test);
            }
        }
        return result;
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