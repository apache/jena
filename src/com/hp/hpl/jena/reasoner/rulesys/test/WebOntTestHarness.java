/******************************************************************
 * File:        WebOntTestHarness.java
 * Created by:  Dave Reynolds
 * Created on:  12-Sep-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: WebOntTestHarness.java,v 1.7 2003-09-18 08:08:51 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.test.WGReasonerTester;
import com.hp.hpl.jena.vocabulary.*;

import java.io.*;
import java.util.*;

/**
 * Test harness for running the WebOnt working group tests relevant 
 * to the OWL rule reasoner. See also TestOWLRules which runs the
 * core WG tests as part of the routine unit tests.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2003-09-18 08:08:51 $
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

    /** The reasoner being tested */
    public Reasoner reasoner;
    
    /** The total set of known tests */
    public Model testDefinitions;
    
    /** The number of tests run */
    public int testCount = 0;
    
    /** The time cost in ms of the last test to be run */
    public long lastTestDuration = 0;
    
    /** Number of tests passed */
    public int passCount = 0;
    
//  =======================================================================
//  Internal constants

    /** The base directory for the working group test files to use */
    public static final String BASE_TESTDIR = "testing/wg/";
    
    /** The base URI in which the files are purported to reside */
    public static String BASE_URI = "http://www.w3.org/2002/03owlt/";
    
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
    public static final String[] BLOCKED_TESTS = {
        // Explicitly testing non-lite features
        "http://www.w3.org/2002/03owlt/complementOf/Manifest001#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest901#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest903#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest902#test", 
        "http://www.w3.org/2002/03owlt/description-logic/Manifest904#test", 
        "http://www.w3.org/2002/03owlt/oneOf/Manifest002#test", 
        "http://www.w3.org/2002/03owlt/oneOf/Manifest003#test", 
        "http://www.w3.org/2002/03owlt/unionOf/Manifest001#test", 
        "http://www.w3.org/2002/03owlt/unionOf/Manifest002#test",
        "http://www.w3.org/2002/03owlt/equivalentClass/Manifest006#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest201#test",
        "http://www.w3.org/2002/03owlt/I5.8/Manifest004#test",
        "http://www.w3.org/2002/03owlt/I5.2/Manifest004#test",
    };
            
    /** The list of status values to include. If approvedOnly then only the first
     *  entry is allowed */
    public static final String[] STATUS_FLAGS = { "APPROVED", "PROPOSED" };
    
//  =======================================================================
//  Constructor and associated support
    
    public WebOntTestHarness() {
        testDefinitions = loadAllTestDefinitions();
        reasoner = ReasonerRegistry.getOWLReasoner();
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
        harness.runTests();
    }
    
    /**
     * Run all relevant tests.
     */
    public void runTests() {
        System.out.println("Positive entailment: ");
        runTests(findTestsOfType(OWLTest.PositiveEntailmentTest));
        System.out.println("\nNegative entailment: ");
        runTests(findTestsOfType(OWLTest.NegativeEntailmentTest));
        System.out.println("\nTrue tests: ");
        runTests(findTestsOfType(OWLTest.TrueTest));
        System.out.println("\nPassed " + passCount + " out of " + testCount);
    }
    
    /**
     * Run all tests in the given list.
     */
    public void runTests(List tests) {
        for (Iterator i = tests.iterator(); i.hasNext(); ) {
            runTest( (Resource) i.next() );
        }
    }
    
    /**
     * Run a single test of any sort, performing any appropriate logging
     * and error reporting.
     */
    public void runTest(Resource test) {
        boolean success = false;
        try {
            success = doRunTest(test);
        } catch (Exception e) {
            System.out.print("\nException: " + e);
            e.printStackTrace();
        }
        testCount++;
        if (success) {
            System.out.print( (testCount % 40 == 0) ? ".\n" : ".");
            System.out.flush();
            passCount++;
        } else {
            System.out.println("\nFAIL: " + test);
        }
        // TODO add logging to the rdf result format
    }
    
    /**
     * Run a single test of any sort, return true if the test succeeds.
     */
    public boolean doRunTest(Resource test) throws IOException {
        if (test.hasProperty(RDF.type, OWLTest.PositiveEntailmentTest) 
        ||  test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest)
        ||  test.hasProperty(RDF.type, OWLTest.TrueTest) ) {
            // Entailment tests
            Model premises = getDoc(test, RDFTest.premiseDocument);
            Model conclusions = getDoc(test, RDFTest.conclusionDocument);
            comprehensionAxioms(premises, conclusions);
            long t1 = System.currentTimeMillis();
            InfGraph graph = reasoner.bind(premises.getGraph());
            Model result = ModelFactory.createModelForGraph(graph);
            boolean correct = testEntailment(conclusions.getGraph(), result.getGraph());
            long t2 = System.currentTimeMillis();
            lastTestDuration = t2 - t1; 
            if (test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest)) {
                correct = !correct;
            }
            return correct;
        } else {
            throw new ReasonerException("Unknown test type");
        }
    }
    
    /**
     * Load the premises or conclusions for the test.
     */
    public Model getDoc(Resource test, Property docType) throws IOException {
        Model result = ModelFactory.createDefaultModel();
        StmtIterator si = test.listProperties(docType);
        while ( si.hasNext() ) {
            String fname = si.nextStatement().getObject().toString() + ".rdf";
            loadFile(fname, result);
        }
        return result;
    }

    /**
     * Utility to load a file into a model a Model. 
     * Files are assumed to be relative to the BASE_URI.
     * @param file the file name, relative to baseDir
     * @return the loaded Model
     */
    public static Model loadFile(String file, Model model) throws IOException {
        String langType = "RDF/XML";
        if (file.endsWith(".nt")) {
            langType = "N-TRIPLE";
        } else if (file.endsWith("n3")) {
            langType = "N3";
        }
        String fname = file;
        if (fname.startsWith(BASE_URI)) {
            fname = fname.substring(BASE_URI.length());
        }
        Reader reader = new BufferedReader(new FileReader(BASE_TESTDIR + fname));
        model.read(reader, BASE_URI + fname, langType);
        return model;
    }
    
    /**
     * Test a conclusions graph against a result graph. This works by
     * translating the conclusions graph into a find query which contains one
     * variable for each distinct bNode in the conclusions graph.
     */
    public boolean testEntailment(Graph conclusions, Graph result) {
        QueryHandler qh = result.queryHandler();
        Query query = WGReasonerTester.graphToQuery(conclusions);
        Iterator i = qh.prepareBindings(query, new Node[] {}).executeBindings();
        return i.hasNext();
    }
    
    /**
     * Example the conclusions graph for introduction of restrictions which
     * require a comprehension rewrite and declare new (anon) classes
     * for those restrictions.
     */
    public void comprehensionAxioms(Model premises, Model conclusions) {
        // Comprehend all restriction declarations and note them in a map
        Map comprehension = new HashMap();
        StmtIterator ri = conclusions.listStatements(null, RDF.type, OWL.Restriction);
        while (ri.hasNext()) {
            Resource restriction = ri.nextStatement().getSubject();
            StmtIterator pi = restriction.listProperties(OWL.onProperty);
            while (pi.hasNext()) {
                Resource prop = (Resource)pi.nextStatement().getObject();
                StmtIterator vi = restriction.listProperties();
                while (vi.hasNext()) {
                    Statement rs = vi.nextStatement();
                    if ( ! rs.getPredicate().equals(OWL.onProperty)) {
                        // Have a restriction on(prop) of type rs in the conclusions
                        // So assert a premise that such a restriction could exisit
                        Resource comp = premises.createResource()
                            .addProperty(RDF.type, OWL.Restriction)
                            .addProperty(OWL.onProperty, prop)
                            .addProperty(rs.getPredicate(), rs.getObject());
                        comprehension.put(restriction, comp);
                    }
                }
            }
        }
        // Comphend any intersectionOf lists
        StmtIterator ii = conclusions.listStatements(null, OWL.intersectionOf, (RDFNode)null);
        while (ii.hasNext()) {
            Statement is = ii.nextStatement();
            Resource comp = premises.createResource()
                   .addProperty(RDF.type, OWL.Class)
                   .addProperty(OWL.intersectionOf, mapList(premises, (Resource)is.getObject(), comprehension));
        }
    }

    /**
     * Helper. Adds to the target model a translation of the given RDF list
     * with each element replaced according to the map.
     */
    private Resource mapList(Model target, Resource list, Map map) {
        if (list.equals(RDF.nil)) {
            return RDF.nil;
        } else {
            Resource head = (Resource) list.getRequiredProperty(RDF.first).getObject();
            Resource rest = (Resource) list.getRequiredProperty(RDF.rest).getObject();
            Resource mapElt = target.createResource();
            mapElt.addProperty(RDF.first, map.get(head));
            mapElt.addProperty(RDF.rest, mapList(target, rest, map));
            return mapElt;
        }
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
            for (int i = 0; i < BLOCKED_TESTS.length; i++) {
                if (BLOCKED_TESTS[i].equals(test.toString())) {
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