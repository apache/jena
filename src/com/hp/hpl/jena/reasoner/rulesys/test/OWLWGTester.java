/******************************************************************
 * File:        OWLWGTester.java
 * Created by:  Dave Reynolds
 * Created on:  11-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: OWLWGTester.java,v 1.15 2003-08-17 20:09:57 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.implb.FBLPRuleInfGraph;
import com.hp.hpl.jena.reasoner.test.WGReasonerTester;
import com.hp.hpl.jena.util.ModelLoader;
//import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

import com.hp.hpl.jena.shared.*;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;


/**
 * A test harness for running the OWL working group tests. This
 * differs from the RDF one in several ways (separate manifest files,
 * different namespaces, document references lack suffix ...).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.15 $ on $Date: 2003-08-17 20:09:57 $
 */
public class OWLWGTester {
    /** The base URI in which the files are purported to reside */
    public static String BASE_URI = "http://www.w3.org/2002/03owlt/";
    
    /** The base directory in which the test data is actually stored */
    public static String baseDir = "testing/wg/";
    
    /** The namespace for the test specification schema */
    public static final String NS_OTEST = "http://www.w3.org/2002/03owlt/testOntology#";
    
    /** The namespace for the test specification schema */
    public static final String NS_RTEST = "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#";
    
    /** The rdf class for positive tests */
    public static final Resource PositiveEntailmentTest;
    
    /** The rdf class for positive tests */
    public static final Resource NegativeEntailmentTest;
    
    /** The predicate defining the description of the test */
    public static final Property descriptionP;
    
    /** The predicate defining a premise for the test */
    public static final Property premiseDocumentP;
    
    /** The predicate defining the conclusion from the test */
    public static final Property conclusionDocumentP;
    
    /** The predicate defining the status of the test */
    public static final Property statusP;
    
    /** The reasoner factory being tested */
    protected ReasonerFactory reasonerF;
    
    /** The configuration information for the reasoner */
    protected Resource configuration;
    
    /** The test case which has invoke this test */
    protected TestCase testcase;
    
    /** The processing time used since testcase creation */
    protected static long timeCost = 0;
    
    /** The total number of tests run */
    protected static int numTests = 0;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(OWLWGTester.class);
    
    // Static initializer for the predicates
    static {
        PositiveEntailmentTest = ResourceFactory.createProperty(NS_OTEST, "PositiveEntailmentTest");
        NegativeEntailmentTest = ResourceFactory.createProperty(NS_OTEST, "NegativeEntailmentTest");
        descriptionP = ResourceFactory.createProperty(NS_RTEST, "description");
        premiseDocumentP = ResourceFactory.createProperty(NS_RTEST, "premiseDocument");
        conclusionDocumentP = ResourceFactory.createProperty(NS_RTEST, "conclusionDocument");
        statusP = ResourceFactory.createProperty(NS_RTEST, "status");
    }
    
    /**
     * Constructor
     * @param reasonerF the factory for the reasoner to be tested
     * @param testcase the JUnit test case which is requesting this test
     * @param configuration optional configuration information
     */
    public OWLWGTester(ReasonerFactory reasonerF, TestCase testcase, Resource configuration) {
        this.reasonerF = reasonerF;
        this.testcase = testcase;
        this.configuration = configuration;
    }
    
    /**
     * Run all the tests in the manifest
     * @param manifestFile the name of the manifest file relative to baseDir
     * @param log set to true to enable derivation logging
     * @param stats set to true to log performance statistics
     * @return true if all the tests pass
     * @throws IOException if one of the test files can't be found
     */
    public boolean runTests(String manifestFile, boolean log, boolean stats) throws IOException {
        // Load up the manifest
        Model manifest = ModelLoader.loadModel(baseDir + manifestFile);
        ResIterator tests = manifest.listSubjectsWithProperty(RDF.type, PositiveEntailmentTest);
        while (tests.hasNext()) {
            Resource test = tests.nextResource();
            if (!runTest(test, log, stats)) return false;
        }
        tests = manifest.listSubjectsWithProperty(RDF.type, NegativeEntailmentTest);
        while (tests.hasNext()) {
            Resource test = tests.nextResource();
            if (!runTest(test, log, stats)) return false;
        }
        return true;
    }

     /**
     * Run a single designated test.
     * @param test the root node descibing the test
     * @param log set to true to enable derivation logging
     * @param stats set to true to log performance statistics
     * @return true if the test passes
     * @throws IOException if one of the test files can't be found
     */
    public boolean runTest(Resource test, boolean log, boolean stats) throws IOException {
        // Find the specification for the named test
        RDFNode testType = test.getRequiredProperty(RDF.type).getObject();
        if (!(testType.equals(NegativeEntailmentTest) ||
               testType.equals(PositiveEntailmentTest) ) ) {
            throw new JenaException("Can't find test: " + test);
        }

        String description = test.getRequiredProperty(descriptionP).getObject().toString();
        String status = test.getRequiredProperty(statusP).getObject().toString();
        logger.debug("WG test " + test.getURI() + " - " + status);
        
        // Load up the premise documents
        Model premises = ModelFactory.createNonreifyingModel();
        for (StmtIterator premisesI = test.listProperties(premiseDocumentP); premisesI.hasNext(); ) {
            premises.add(loadFile(premisesI.nextStatement().getObject().toString() + ".rdf"));
        }

        // Load up the conclusions document
        Resource conclusionsRes = (Resource) test.getRequiredProperty(conclusionDocumentP).getObject();
        Model conclusions = loadFile(conclusionsRes.toString() + ".rdf");
        
        // Construct the inferred graph
        // Optional logging
        if (log) {
            if (configuration == null) {
                Model m = ModelFactory.createDefaultModel();
                configuration = m.createResource();
            }
            configuration.addProperty(ReasonerVocabulary.PROPtraceOn, "true")
                         .addProperty(ReasonerVocabulary.PROPderivationLogging, "true");
        }
        Reasoner reasoner = reasonerF.create(configuration);
        long t1 = System.currentTimeMillis();
        InfGraph graph = reasoner.bind(premises.getGraph());
        Model result = ModelFactory.createModelForGraph(graph);
        
        if (stats && graph instanceof FBLPRuleInfGraph) {
//            ((FBLPRuleInfGraph)graph).resetLPProfile(true);
        }
        
        // Check the results against the official conclusions
        boolean correct = true;
        if (testType.equals(PositiveEntailmentTest)) {
            correct = testConclusions(conclusions.getGraph(), result.getGraph());
        } else {
            // A negative entailment check
            correct = !testConclusions(conclusions.getGraph(), result.getGraph());
        }
        long t2 = System.currentTimeMillis();
        timeCost += (t2-t1);
        numTests++;
        if (stats) {
            logger.info("Time=" + (t2-t1) + "ms for " + test.getURI());
            printStats();
            
            if (graph instanceof FBLPRuleInfGraph) {
                ((FBLPRuleInfGraph)graph).printLPProfile();
            }
        }
        
        if (!correct) {
            // List all the forward deductions for debugging
//            if (graph instanceof FBRuleInfGraph) {
//                System.out.println("Error: deductions graph was ...");
//                FBRuleInfGraph fbGraph = (FBRuleInfGraph)graph;
//                Graph deductions = fbGraph.getDeductionsGraph();
//                for (Iterator i = deductions.find(null,null,null); i.hasNext();) {
//                    logger.info(" - " + PrintUtil.print(i.next()));
//                }
//            }
        }
        
        // Signal the results        
        if (testcase != null) {
            TestCase.assertTrue("Test: " + test + "\n" +  description, correct);
        }
        return correct;
    }

    /**
     * Utility to load a file as a Model. 
     * Files are assumed to be relative to the BASE_URI.
     * @param file the file name, relative to baseDir
     * @return the loaded Model
     */
    public static Model loadFile(String file) throws IOException {
        String langType = "RDF/XML";
        if (file.endsWith(".nt")) {
            langType = "N-TRIPLE";
        } else if (file.endsWith("n3")) {
            langType = "N3";
        }
        Model result = ModelFactory.createNonreifyingModel();
        String fname = file;
        if (fname.startsWith(BASE_URI)) {
            fname = fname.substring(BASE_URI.length());
        }
        Reader reader = new BufferedReader(new FileReader(baseDir + fname));
        result.read(reader, BASE_URI + fname, langType);
        return result;
    }
    
    /**
     * Test a conclusions graph against a result graph. This works by
     * translating the conclusions graph into a find query which contains one
     * variable for each distinct bNode in the conclusions graph.
     */
    private boolean testConclusions(Graph conclusions, Graph result) {
        QueryHandler qh = result.queryHandler();
        Query query = WGReasonerTester.graphToQuery(conclusions);
        Iterator i = qh.prepareBindings(query, new Node[] {}).executeBindings();
        return i.hasNext();
    }
    
    /**
     * Log (info level) some summary information on the timecost of the tests.
     */
    public void printStats() {
        logger.info("Ran " + numTests +" in " + timeCost +"ms = " + (timeCost/numTests) + "ms/test");
    }

}
