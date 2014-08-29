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

package com.hp.hpl.jena.reasoner.test;

import java.io.BufferedInputStream ;
import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.net.URL ;
import java.util.ArrayList ;
import java.util.List ;

import junit.framework.TestCase ;
import org.junit.Assert ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl ;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl ;
import com.hp.hpl.jena.rdfxml.xmlinput.ARPTests ;
import com.hp.hpl.jena.reasoner.InfGraph ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.ReasonerFactory ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.vocabulary.RDF ;

/**
 * A utility to support execution of the RDFCode working group entailment
 * tests as specified in <a href="http://www.w3.org/TR/2003/WD-rdf-testcases-20030123/">
 * http://www.w3.org/TR/2003/WD-rdf-testcases-20030123/</a>.
 * 
 * <p>The manifest file defines a set of tests. Only the positive and negative
 * entailment tests are handled by this utility. Each test defines a set
 * of data files to load. For normal positive entailment tests we check each
 * triple in the conclusions file to ensure it is included in the inferred
 * graph. For postive entailment tests which are supposed to entail the 
 * false document we run an additional validation check. For
 * negative entailment tests which tests all triples in the non-conclusions file 
 * and check that at least one trile is missing. </p>
 */
public class WGReasonerTester {

    /** The namespace for the test specification schema */
    public static final String NS = "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#";
    
    /** The base URI in which the files are purported to reside */
    public static final String BASE_URI = "http://www.w3.org/2000/10/rdf-tests/rdfcore/";
    
    /** Default location for the test data */
    public static final String DEFAULT_BASE_DIR = "testing/wg/";
    
    /** The base directory in which the test data is actually stored */
    final protected String baseDir;
    
    /** The rdf class for positive tests */
    public static final Resource PositiveEntailmentTest;
    
    /** The rdf class for positive tests */
    public static final Resource NegativeEntailmentTest;
    
    /** The constant used to indicate an invalid document */
    public static final Resource FalseDocument;
    
    /** The predicate defining the description of the test */
    public static final Property descriptionP;
    
    /** The predicate defining the status of the test */
    public static final Property statusP;
    
    /** The predicate defining the rule sets used */
    public static final Property entailmentRulesP;
    
    /** The predicate defining a premise for the test */
    public static final Property premiseDocumentP;
    
    /** The predicate defining the conclusion from the test */
    public static final Property conclusionDocumentP;
    
    /** The type of the current test */
    Resource testType;
    
    /** List of tests block because they are only intended for non-dt aware processors */
    public static final String[] blockedTests = {
        BASE_URI + "datatypes/Manifest.rdf#language-important-for-non-dt-entailment-1",
        BASE_URI + "datatypes/Manifest.rdf#language-important-for-non-dt-entailment-2",
    // Additional blocked tests, because we do not implement them ... jjc
        BASE_URI + "pfps-10/Manifest.rdf#non-well-formed-literal-1",
        BASE_URI + "xmlsch-02/Manifest.rdf#whitespace-facet-3",
//	BASE_URI + "xmlsch-02/Manifest.rdf#whitespace-facet-2",
//	BASE_URI + "xmlsch-02/Manifest.rdf#whitespace-facet-1",
//	BASE_URI + "datatypes-intensional/Manifest.rdf#xsd-integer-string-incompatible",
    };
    
    // Static initializer for the predicates
    static {
        PositiveEntailmentTest = new ResourceImpl(NS, "PositiveEntailmentTest");
        NegativeEntailmentTest = new ResourceImpl(NS, "NegativeEntailmentTest");
        FalseDocument = new ResourceImpl(NS, "False-Document");
        descriptionP = new PropertyImpl(NS, "description");
        statusP = new PropertyImpl(NS, "status");
        entailmentRulesP = new PropertyImpl(NS, "entailmentRules");
        premiseDocumentP = new PropertyImpl(NS, "premiseDocument");
        conclusionDocumentP = new PropertyImpl(NS, "conclusionDocument");
    }
    
    /** The rdf defining all the tests to be run */
    protected Model testManifest;
    
    protected static Logger logger = LoggerFactory.getLogger(WGReasonerTester.class);
    
    /**
     * Constructor.
     * @param manifest the name of the manifest file defining these
     * tests - relative to baseDir
     * @param baseDir override default base directory for the tests and manifest
     */
    public WGReasonerTester(String manifest, String baseDir) throws IOException {
        this.baseDir = baseDir;
        testManifest = loadFile(manifest);
    }
    
    /**
     * Constructor.
     * @param manifest the name of the manifest file defining these
     * tests - relative to baseDir
     */
    public WGReasonerTester(String manifest) throws IOException {
        this(manifest, DEFAULT_BASE_DIR);
    }
    
    /**
     * Utility to load a file in rdf/nt/n3 format as a Model.
     * Files are assumed to be relative to the BASE_URI.
     * @param file the file name, relative to baseDir
     * @return the loaded Model
     */
    public Model loadFile(String file) throws IOException {
        String langType = "RDF/XML";
        if (file.endsWith(".nt")) {
            langType = "N-TRIPLE";
        } else if (file.endsWith("n3")) {
            langType = "N3";
        }
        Model result = ModelFactory.createDefaultModel();
        String fname = file;
        if (fname.startsWith(BASE_URI)) {
            fname = fname.substring(BASE_URI.length());
        }
        
        /* Change note - jjc
         * Now use InputStream instead of Reader (general hygine).
         * Also treat http:.... as URL not local file.
         */
        InputStream in;
        if ( baseDir.startsWith("http:")) {
        	in = new URL(baseDir+fname).openStream();
        } else {
        	in = new FileInputStream(baseDir + fname);
        }
        in = new BufferedInputStream(in);
        
        
        result.read(in, BASE_URI + fname, langType);
        return result;
    }
    
    /**
     * Load the datafile given by the property name.
     * @param test the test being processed
     * @param predicate the property of the test giving the file name to load
     * @return a graph containing the file contents or an empty graph if the property
     * is not present
     * @throws IOException if the property is present but the file can't be found
     */
    private Graph loadTestFile(Resource test, Property predicate) throws IOException {
        if (test.hasProperty(predicate)) {
            String fileName = test.getRequiredProperty(predicate).getObject().toString();
            return loadFile(fileName).getGraph();
        } else {
            return Factory.createGraphMem();
        }
    }
    
    /**
     * Run all the tests in the manifest
     * @param reasonerF the factory for the reasoner to be tested
     * @param testcase the JUnit test case which is requesting this test
     * @param configuration optional configuration information
     * @return true if all the tests pass
     * @throws IOException if one of the test files can't be found
     * @throws JenaException if the test can't be found or fails internally
     */
    public boolean runTests(ReasonerFactory reasonerF, TestCase testcase, Resource configuration) throws IOException {
        for ( String test : listTests() )
        {
            if ( !runTest( test, reasonerF, testcase, configuration ) )
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Return a list of all test names defined in the manifest for this test harness.
     */
    public List<String> listTests() {
        List<String> testList = new ArrayList<>();
        ResIterator tests = testManifest.listResourcesWithProperty(RDF.type, PositiveEntailmentTest);
        while (tests.hasNext()) {
            testList.add(tests.next().toString());
        }
        tests = testManifest.listResourcesWithProperty(RDF.type, NegativeEntailmentTest);
        while (tests.hasNext()) {
            testList.add(tests.next().toString());
        }
        return testList;
    }
    
    /**
     * Return the type of the last test run. Nasty hack to enable calling test harness
     * to interpret the success/fail boolen differently according to test type.
     */
    public Resource getTypeOfLastTest() {
        return testType;
    }
    
    /**
     * Run a single designated test.
     * @param uri the uri of the test, as defined in the manifest file
     * @param reasonerF the factory for the reasoner to be tested
     * @param testcase the JUnit test case which is requesting this test
     * @param configuration optional configuration information
     * @return true if the test passes
     * @throws IOException if one of the test files can't be found
     * @throws JenaException if the test can't be found or fails internally
     */
    public boolean runTest(String uri, ReasonerFactory reasonerF, TestCase testcase, Resource configuration) throws IOException {
        return runTestDetailedResponse(uri,reasonerF,testcase,configuration) != FAIL;
    }
    static final public int FAIL = -1;
    static final public int NOT_APPLICABLE = 0;
    static final public int INCOMPLETE = 1;
    static final public int PASS  = 2;
    
	/**
		 * Run a single designated test.
		 * @param uri the uri of the test, as defined in the manifest file
		 * @param reasonerF the factory for the reasoner to be tested
		 * @param testcase the JUnit test case which is requesting this test
		 * @param configuration optional configuration information
		 * @return true if the test passes
		 * @throws IOException if one of the test files can't be found
		 * @throws JenaException if the test can't be found or fails internally
		 */
    
	
	   public int runTestDetailedResponse(String uri, ReasonerFactory reasonerF, TestCase testcase, Resource configuration) throws IOException {
    
        // Find the specification for the named test
        Resource test = testManifest.getResource(uri);
        testType = (Resource)test.getRequiredProperty(RDF.type).getObject();
        if (!(testType.equals(NegativeEntailmentTest) ||
               testType.equals(PositiveEntailmentTest) ) ) {
            throw new JenaException("Can't find test: " + uri);
        }

        Statement descriptionS = test.getProperty(descriptionP);
        String description = (descriptionS == null) ? "no description" : descriptionS.getObject().toString();
        String status = test.getRequiredProperty(statusP).getObject().toString();
        logger.debug("WG test " + test.getURI() + " - " + status);
        if (! status.equals("APPROVED")) {
            return NOT_APPLICABLE;
        }
        
        // Skip the test designed for only non-datatype aware processors
           for ( String blockedTest : blockedTests )
           {
               if ( test.getURI().equals( blockedTest ) )
               {
                   return NOT_APPLICABLE;
               }
           }
                
        // Load up the premise documents
        Model premises = ModelFactory.createDefaultModel();
        for (StmtIterator premisesI = test.listProperties(premiseDocumentP); premisesI.hasNext(); ) {
            premises.add(loadFile(premisesI.nextStatement().getObject().toString()));
        }

        // Load up the conclusions document
        Model conclusions = null;
        Resource conclusionsRes = (Resource) test.getRequiredProperty(conclusionDocumentP).getObject();
        Resource conclusionsType = (Resource) conclusionsRes.getRequiredProperty(RDF.type).getObject();
        if (!conclusionsType.equals(FalseDocument)) {
            conclusions = loadFile(conclusionsRes.toString());
        }
        
        // Construct the inferred graph
        Reasoner reasoner = reasonerF.create(configuration);
        InfGraph graph = reasoner.bind(premises.getGraph());
        Model result = ModelFactory.createModelForGraph(graph);
        
        // Check the results against the official conclusions
        boolean correct = true;
        int goodResult = PASS;
        boolean noisy = !(baseDir.equals(DEFAULT_BASE_DIR)
               || ARPTests.internet );
        if (testType.equals(PositiveEntailmentTest)) {
            if (conclusions == null) {
                // Check that the result is flagged as semantically invalid
                correct = ! graph.validate().isValid();
                if (noisy) {
                    System.out.println("PositiveEntailmentTest of FalseDoc " + test.getURI() + (correct ? " - OK" : " - FAIL"));
                }
            } else {
                correct = testConclusions(conclusions.getGraph(), result.getGraph());
                if (!graph.validate().isValid()) {
                    correct = false;
                }
                if (noisy) {
                    System.out.println("PositiveEntailmentTest " + test.getURI() + (correct ? " - OK" : " - FAIL"));
                }
            }
        } else {
        	  goodResult = INCOMPLETE;
            // A negative entailment check
            if (conclusions == null) {
                // Check the result is not flagged as invalid
                correct = graph.validate().isValid();
                if (noisy) {
                    System.out.println("NegativentailmentTest of FalseDoc " + test.getURI() + (correct ? " - OK" : " - FAIL"));
                }
            } else {
                correct = !testConclusions(conclusions.getGraph(), result.getGraph());
                if (noisy) {
                    System.out.println("NegativeEntailmentTest " + test.getURI() + (correct ? " - OK" : " - FAIL"));
                }
            }
        }

        // Debug output on failure
        if (!correct) {
            logger.debug("Premises: " );
            for (StmtIterator i = premises.listStatements(); i.hasNext(); ) {
                logger.debug("  - " + i.nextStatement());
            }
            logger.debug("Conclusions: " );
            if (conclusions != null) {
                for (StmtIterator i = conclusions.listStatements(); i.hasNext(); ) {
                    logger.debug("  - " + i.nextStatement());
                }
            }
        }
        
        // Signal the results        
        if (testcase != null) {
//            if ( !correct )
//            {
//                boolean b = testConclusions(conclusions.getGraph(), result.getGraph());
//                System.out.println("**** actual") ;
//                result.write(System.out, "TTL") ; 
//                System.out.println("**** expected") ;
//                conclusions.write(System.out, "TTL") ;
//            }
            Assert.assertTrue("Test: " + test + "\n" +  description, correct);
        }
        return correct?goodResult:FAIL;
    }
    
    /**
     * Test a conclusions graph against a result graph.
    * This works by
     * translating the conclusions graph into a find query which contains one
     * variable for each distinct bNode in the conclusions graph.
     */
    public static boolean testConclusions(Graph conclusions, Graph result) {
        return Matcher.subgraphInferred(conclusions, result) ;
    }

}
