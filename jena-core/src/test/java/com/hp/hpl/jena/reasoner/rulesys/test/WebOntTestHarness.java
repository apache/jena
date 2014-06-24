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

import java.io.* ;
import java.util.* ;

import com.hp.hpl.jena.ontology.OntModelSpec ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.reasoner.InfGraph ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.ReasonerException ;
import com.hp.hpl.jena.reasoner.ReasonerRegistry ;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph ;
import com.hp.hpl.jena.reasoner.test.WGReasonerTester ;
import com.hp.hpl.jena.vocabulary.* ;

/**
 * Test harness for running the WebOnt working group tests relevant 
 * to the OWL rule reasoner. See also TestOWLRules which runs the
 * core WG tests as part of the routine unit tests.
 */
public class WebOntTestHarness {

//  =======================================================================
//  Variables to control the test operations

    /** Set to true to include modified test versions */
    public static boolean includeModified = false;
    
    /** Set to true to use approved tests only */
    public static boolean approvedOnly = true;
    
    /** Set to true to print LP engine profile information */
    public static boolean printProfile = false;
    
//  =======================================================================
//  Internal state

    /** The reasoner being tested */
    Reasoner reasoner;
    
    /** The total set of known tests */
    Model testDefinitions;
    
    /** The number of tests run */
    int testCount = 0;
    
    /** The time cost in ms of the last test to be run */
    long lastTestDuration = 0;
    
    /** Number of tests passed */
    int passCount = 0;
    
    /** The model describing the results of the run */
    Model testResults;
    
    /** The resource which acts as a description for the Jena2 instance being tested */
    Resource jena2;
    
//  =======================================================================
//  Internal constants

    /** The base directory for the working group test files to use */
    public static final String BASE_TESTDIR = "testing/wg/";
    
    /** The base URI in which the files are purported to reside */
    public static String BASE_URI = "http://www.w3.org/2002/03owlt/";
    
    /** The base URI for the results file */
    public static String BASE_RESULTS_URI = "http://jena.sourceforge.net/data/owl-results.rdf";
    
    /** The list of subdirectories to process (omits the rdf/rdfs dirs) */
    public static final String[] TEST_DIRS= {"AllDifferent", "AllDistinct", 
            "AnnotationProperty", "DatatypeProperty", "FunctionalProperty",
            "I3.2", "I3.4", "I4.1", "I4.5", "I4.6", "I5.1", "I5.2", "I5.21", "I5.24",
            "I5.26", "I5.3", "I5.5", "I5.8", "InverseFunctionalProperty", "Nothing", 
            "Restriction", "SymmetricProperty", "Thing", "TransitiveProperty", 
            "allValuesFrom", "amp-in-url", "cardinality", "complementOf", "datatypes", 
            "differentFrom", "disjointWith", "distinctMembers", 
            "equivalentClass", "equivalentProperty", "imports", 
            "intersectionOf", "inverseOf", "localtests", "maxCardinality", "miscellaneous",
            "oneOf", "oneOfDistinct", "sameAs", "sameClassAs", "sameIndividualAs", 
            "samePropertyAs", "someValuesFrom", "statement-entailment", "unionOf", 
            "xmlbase",
            "description-logic", 
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
//        "http://www.w3.org/2002/03owlt/oneOf/Manifest002#test", 
//        "http://www.w3.org/2002/03owlt/oneOf/Manifest003#test", 
        "http://www.w3.org/2002/03owlt/oneOf/Manifest004#test", 
        "http://www.w3.org/2002/03owlt/unionOf/Manifest001#test", 
        "http://www.w3.org/2002/03owlt/unionOf/Manifest002#test",
        "http://www.w3.org/2002/03owlt/unionOf/Manifest003#test",
        "http://www.w3.org/2002/03owlt/unionOf/Manifest004#test",
        "http://www.w3.org/2002/03owlt/equivalentClass/Manifest006#test",
        "http://www.w3.org/2002/03owlt/equivalentClass/Manifest007#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest201#test",
        "http://www.w3.org/2002/03owlt/I5.8/Manifest004#test",
        "http://www.w3.org/2002/03owlt/I5.2/Manifest004#test",
        
        "http://www.w3.org/2002/03owlt/description-logic/Manifest008#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest011#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest015#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest019#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest023#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest026#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest027#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest029#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest030#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest032#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest033#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest035#test",

        "http://www.w3.org/2002/03owlt/description-logic/Manifest101#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest102#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest103#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest104#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest105#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest106#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest107#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest108#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest109#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest110#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest111#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest502#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest504#test",
        
        "http://www.w3.org/2002/03owlt/description-logic/Manifest202#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest203#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest204#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest205#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest206#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest207#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest208#test",

        "http://www.w3.org/2002/03owlt/description-logic/Manifest209#test",
        
        "http://www.w3.org/2002/03owlt/miscellaneous/Manifest010#test",
        "http://www.w3.org/2002/03owlt/miscellaneous/Manifest011#test",
        "http://www.w3.org/2002/03owlt/SymmetricProperty/Manifest002#test",
        
        "http://www.w3.org/2002/03owlt/Thing/Manifest005#test",
        
        // Temporary block - incomplete (OOM eventually in some cases)
        "http://www.w3.org/2002/03owlt/TransitiveProperty/Manifest002#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest661#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest662#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest663#test",
        
        "http://www.w3.org/2002/03owlt/description-logic/Manifest608#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest611#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest615#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest623#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest626#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest627#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest630#test",        
        "http://www.w3.org/2002/03owlt/description-logic/Manifest668#test",
        "http://www.w3.org/2002/03owlt/description-logic/Manifest668#test",

    };
            
    /** The list of status values to include. If approvedOnly then only the first
     *  entry is allowed */
    public static final String[] STATUS_FLAGS = { "APPROVED", "PROPOSED" };
    
//  =======================================================================
//  Constructor and associated support
    
    public WebOntTestHarness() {
        testDefinitions = loadAllTestDefinitions();
        reasoner = ReasonerRegistry.getOWLReasoner();
        initResults();
    }

    /** Load all of the known manifest files into a single model */
    public static Model loadAllTestDefinitions() {
        System.out.print("Loading manifests "); System.out.flush();
        Model testDefs = ModelFactory.createDefaultModel();
        int count = 0;
        for ( String TEST_DIR : TEST_DIRS )
        {
            File dir = new File( BASE_TESTDIR + TEST_DIR );
            String[] manifests = dir.list( new FilenameFilter()
            {
                @Override
                public boolean accept( File df, String name )
                {
                    if ( name.startsWith( "Manifest" ) && name.endsWith( ".rdf" ) )
                    {
                        return includeModified || !name.endsWith( "-mod.rdf" );
                    }
                    else
                    {
                        return false;
                    }
                }
            } );
            for ( String manifest : manifests )
            {
                File mf = new File( dir, manifest );
                try
                {
                    testDefs.read( new FileInputStream( mf ), "file:" + mf );
                    count++;
                    if ( count % 8 == 0 )
                    {
                        System.out.print( "." );
                        System.out.flush();
                    }
                }
                catch ( FileNotFoundException e )
                {
                    System.out.println( "File not readable - " + e );
                }
            }
        }
        System.out.println("loaded");
        return testDefs;
    }
    
    /** 
     * Initialize the result model.
     */
    public void initResults() {
        testResults = ModelFactory.createDefaultModel();
        jena2 = testResults.createResource(BASE_RESULTS_URI + "#jena2");
        jena2.addProperty(RDFS.comment, 
            testResults.createLiteral(
                "<a xmlns=\"http://www.w3.org/1999/xhtml\" href=\"http://jena.sourceforce.net/\">Jena2</a> includes a rule-based inference engine for RDF processing, " +
                "supporting both forward and backward chaining rules. Its OWL rule set is designed to provide sound " +
                "but not complete instance resasoning for that fragment of OWL/Full limited to the OWL/lite vocabulary. In" +
                "particular it does not support unionOf/complementOf.",
                true)
        );
        jena2.addProperty(RDFS.label, "Jena2");
        testResults.setNsPrefix("results", OWLResults.NS);
    }
    
//  =======================================================================
//  Main control methods
    
    public static void main(String[] args) throws IOException {
        String resultFile = "owl-results.rdf";
        if (args.length >= 1) {
            resultFile = args[0];
        }
        WebOntTestHarness harness = new WebOntTestHarness();
        harness.runTests();
//        harness.runTest("http://www.w3.org/2002/03owlt/AnnotationProperty/Manifest004#test");
//        harness.runTest("http://www.w3.org/2002/03owlt/AnnotationProperty/Manifest003#test");
//        harness.runTest("http://www.w3.org/2002/03owlt/Thing/Manifest001#test");
//        harness.runTest("http://www.w3.org/2002/03owlt/Thing/Manifest002#test");
//        harness.runTest("http://www.w3.org/2002/03owlt/Thing/Manifest003#test");
//        harness.runTest("http://www.w3.org/2002/03owlt/Thing/Manifest004#test");
//        harness.runTest("http://www.w3.org/2002/03owlt/Thing/Manifest005#test");
        RDFWriter writer = harness.testResults.getWriter("RDF/XML-ABBREV");
        OutputStream stream = new FileOutputStream(resultFile);
        writer.setProperty("showXmlDeclaration", "true");
        harness.testResults.setNsPrefix("", "http://www.w3.org/1999/xhtml");
        writer.write(harness.testResults, stream, BASE_RESULTS_URI);
    }
    
    /**
     * Run all relevant tests.
     */
    public void runTests() {
        System.out.println("Testing " + (approvedOnly ? "only APPROVED" : "APPROVED and PROPOSED") );
        System.out.println("Positive entailment: ");
        runTests(findTestsOfType(OWLTest.PositiveEntailmentTest));
//        System.out.println("\nNegative entailment: ");
//        runTests(findTestsOfType(OWLTest.NegativeEntailmentTest));
        System.out.println("\nTrue tests: ");
        runTests(findTestsOfType(OWLTest.TrueTest));
        System.out.println("\nOWL for OWL tests: ");
        runTests(findTestsOfType(OWLTest.OWLforOWLTest));
        System.out.println("\nImport entailment tests: ");
        runTests(findTestsOfType(OWLTest.ImportEntailmentTest));
        System.out.println("\nInconsistency tests: ");
        runTests(findTestsOfType(OWLTest.InconsistencyTest));
        System.out.println("\nPassed " + passCount + " out of " + testCount);
    }
    
    /**
     * Run all tests in the given list.
     */
    public void runTests(List<Resource> tests) {
        for ( Resource test : tests )
        {
            runTest( test );
        }
    }
    
    /**
     * Run a single test of any sort, performing any appropriate logging
     * and error reporting.
     */
    public void runTest(String test) {
        runTest(testDefinitions.getResource(test));
    }
     
    /**
     * Run a single test of any sort, performing any appropriate logging
     * and error reporting.
     */
    public void runTest(Resource test) {
        System.out.println("Running " + test);
        boolean success = false;
        boolean fail = false;
        try {
            success = doRunTest(test);
        } catch (Exception e) {
            fail = true;
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
        Resource resultType = null;
        if (fail) {
            resultType = OWLResults.FailingRun;
        } else {
            if (test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest) 
            ||  test.hasProperty(RDF.type, OWLTest.ConsistencyTest)) {
                resultType = success ? OWLResults.PassingRun : OWLResults.FailingRun;
            } else {
                resultType = success ? OWLResults.PassingRun : OWLResults.IncompleteRun;
            }
        }
        // log to the rdf result format
        Resource result = testResults.createResource()
            .addProperty(RDF.type, OWLResults.TestRun)
            .addProperty(RDF.type, resultType)
            .addProperty(OWLResults.test, test)
            .addProperty(OWLResults.system, jena2);
    }
    
    /**
     * Run a single test of any sort, return true if the test succeeds.
     */
    public boolean doRunTest(Resource test) throws IOException {
        if (test.hasProperty(RDF.type, OWLTest.PositiveEntailmentTest) 
        ||  test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest)
        ||  test.hasProperty(RDF.type, OWLTest.OWLforOWLTest)
        ||  test.hasProperty(RDF.type, OWLTest.ImportEntailmentTest)
        ||  test.hasProperty(RDF.type, OWLTest.TrueTest) ) {
            // Entailment tests
            boolean processImports = test.hasProperty(RDF.type, OWLTest.ImportEntailmentTest);
            Model premises = getDoc(test, RDFTest.premiseDocument, processImports);
            Model conclusions = getDoc(test, RDFTest.conclusionDocument);
            comprehensionAxioms(premises, conclusions);
            long t1 = System.currentTimeMillis();
            InfGraph graph = reasoner.bind(premises.getGraph());
            if (printProfile) {
                ((FBRuleInfGraph)graph).resetLPProfile(true);
            }
            Model result = ModelFactory.createModelForGraph(graph);
            boolean correct = WGReasonerTester.testConclusions(conclusions.getGraph(), result.getGraph());
            long t2 = System.currentTimeMillis();
            lastTestDuration = t2 - t1; 
            if (printProfile) {
                ((FBRuleInfGraph)graph).printLPProfile();
            }
            if (test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest)) {
                correct = !correct;
            }
            return correct;
        } else if (test.hasProperty(RDF.type, OWLTest.InconsistencyTest)) {
//            System.out.println("Starting: " + test);
            Model input = getDoc(test, RDFTest.inputDocument);
            long t1 = System.currentTimeMillis();
            InfGraph graph = reasoner.bind(input.getGraph());
            boolean correct = ! graph.validate().isValid();
            long t2 = System.currentTimeMillis();
            lastTestDuration = t2 - t1; 
            return correct;
        } else if (test.hasProperty(RDF.type, OWLTest.ConsistencyTest)) {
            // Not used normally becase we are not complete enough to prove consistency
//            System.out.println("Starting: " + test);
            Model input = getDoc(test, RDFTest.inputDocument);
            long t1 = System.currentTimeMillis();
            InfGraph graph = reasoner.bind(input.getGraph());
            boolean correct = graph.validate().isValid();
            long t2 = System.currentTimeMillis();
            lastTestDuration = t2 - t1; 
            return correct;
        } else {
            for (StmtIterator i = test.listProperties(RDF.type); i.hasNext(); ) {
                System.out.println("Test type = " + i.nextStatement().getObject());
            }
            throw new ReasonerException("Unknown test type");
        }
    }
   
    /**
     * Load the premises or conclusions for the test, optional performing
     * import processing.
     */
    public Model getDoc(Resource test, Property docType, boolean processImports) throws IOException {
        if (processImports) {
            Model result = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
            StmtIterator si = test.listProperties(docType);
            while ( si.hasNext() ) {
                String fname = si.nextStatement().getObject().toString() + ".rdf";
                loadFile(fname, result);
            }
            return result;
        } else {
            return getDoc(test, docType);
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
     * Example the conclusions graph for introduction of restrictions which
     * require a comprehension rewrite and declare new (anon) classes
     * for those restrictions.
     */
    public void comprehensionAxioms(Model premises, Model conclusions) {
        // Comprehend all restriction declarations and note them in a map
        Map<Resource, Resource> comprehension = new HashMap<>();
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
        // Comprehend any intersectionOf lists. Introduce anon class which has the form
        // of the intersection expression.
        // Rewrite queries of the form (X intersectionOf Y) to the form
        //   (X equivalentClass ?CC) (?CC intersectionOf Y)
        StmtIterator ii = conclusions.listStatements(null, OWL.intersectionOf, (RDFNode)null);
        List<Statement> intersections = new ArrayList<>();
        while (ii.hasNext()) { 
            intersections.add(ii.nextStatement());
        }
        for ( Statement is : intersections )
        {
            // Declare in the premises that such an intersection exists
            Resource comp =
                premises.createResource().addProperty( RDF.type, OWL.Class ).addProperty( OWL.intersectionOf,
                                                                                          mapList( premises,
                                                                                                   (Resource) is.getObject(),
                                                                                                   comprehension ) );
            // Rewrite the conclusions to be a test for equivalence between the class being
            // queried and the comprehended interesection
            conclusions.remove( is );
            conclusions.add( is.getSubject(), OWL.equivalentClass, comp );
        }
        // Comprehend any oneOf lists
        StmtIterator io = conclusions.listStatements(null, OWL.oneOf, (RDFNode)null);
        while (io.hasNext()) {
            Statement s = io.nextStatement();
            Resource comp = premises.createResource()
                        .addProperty(OWL.oneOf, s.getObject());
        }
    }

    /**
     * Helper. Adds to the target model a translation of the given RDF list
     * with each element replaced according to the map.
     */
    private Resource mapList(Model target, Resource list, Map<Resource, Resource> map) {
        if (list.equals(RDF.nil)) {
            return RDF.nil;
        } else {
            Resource head = (Resource) list.getRequiredProperty(RDF.first).getObject();
            Resource rest = (Resource) list.getRequiredProperty(RDF.rest).getObject();
            Resource mapElt = target.createResource();
            Resource mapHead = map.get(head);
            if (mapHead == null) mapHead = head;
            mapElt.addProperty(RDF.first, mapHead);
            mapElt.addProperty(RDF.rest, mapList(target, rest, map));
            return mapElt;
        }
    }
    
//  =======================================================================
//  Internal helper functions
    
    /** Return a list of all tests of the given type, according to the current filters */
    public List<Resource> findTestsOfType(Resource testType) {
        ArrayList<Resource> result = new ArrayList<>();
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
                for ( String STATUS_FLAG : STATUS_FLAGS )
                {
                    if ( status.getString().equals( STATUS_FLAG ) )
                    {
                        accept = true;
                        break;
                    }
                }
            }
            // Check for blocked tests
            for ( String BLOCKED_TEST : BLOCKED_TESTS )
            {
                if ( BLOCKED_TEST.equals( test.toString() ) )
                {
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
