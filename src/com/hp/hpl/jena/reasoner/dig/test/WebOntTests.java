/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            20-Apr-2004
 * Filename           $RCSfile: WebOntTests.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-04-23 22:38:07 $
 *               by   $Author: ian_dickinson $
 *
 * @copyright@
 *****************************************************************************/

// Package
///////////////

package com.hp.hpl.jena.reasoner.dig.test;


// Imports
///////////////
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.dig.DIGReasoner;
import com.hp.hpl.jena.reasoner.dig.DIGReasonerFactory;
import com.hp.hpl.jena.reasoner.test.WGReasonerTester;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.LogFactory;



/**
 * <p>
 * Test harness for running the WebOnt working group tests on the DIG reasoner
 * interface. This class is derived from Dave's
 * {@link com.hp.hpl.jena.reasoner.rulesys.test.WebOntTestHarness WebOntTestHarness}.
 * </p>
 * 
 * @author Ian Dickinson, HP Labs ( <a href="mailto:Ian.Dickinson@hp.com">email
 *         </a>)
 * @version Release @release@ ($Id: eclipse-template.txt,v 1.2 2003/10/20
 *          22:03:02 ian_dickinson Exp $)
 */
public class WebOntTests
{
    // Constants
    //////////////////////////////////

    /** The base directory for the working group test files to use */
    public static final String BASE_TESTDIR = "testing/wg/";

    /** The base URI in which the files are purported to reside */
    public static String BASE_URI = "http://www.w3.org/2002/03owlt/";

    /** The base URI for the results file */
    public static String BASE_RESULTS_URI = "http://jena.sourceforge.net/data/owl-results.rdf";

    /** The list of subdirectories to process (omits the rdf/rdfs dirs) */
    public static final String[] TEST_DIRS = {"AllDifferent", "AllDistinct", "AnnotationProperty", "DatatypeProperty",
            "FunctionalProperty", "I3.2", "I3.4", "I4.1", "I4.5", "I4.6", "I5.1", "I5.2", "I5.21", "I5.24", "I5.26",
            "I5.3", "I5.5", "I5.8", "InverseFunctionalProperty", "Nothing", "Restriction", "SymmetricProperty",
            "Thing", "TransitiveProperty", "Class", "allValuesFrom", "amp-in-url", "cardinality", "complementOf", "datatypes",
            "differentFrom", "disjointWith", "distinctMembers", "equivalentClass", "equivalentProperty", "imports",
            "intersectionOf", "inverseOf", "localtests", "maxCardinality", "miscellaneous", "oneOf", "oneOfDistinct",
            "sameAs", "sameClassAs", "sameIndividualAs", "samePropertyAs", "someValuesFrom", "statement-entailment",
            "unionOf", "xmlbase", "description-logic",
    //            "extra-credit",
    };

    /**
     * List of tests that are blocked because they test language features
     * beyond OWL DL
     */
    public static final String[] BLOCKED_TESTS = {};

    /**
     * The list of status values to include. If approvedOnly then only the
     * first entry is allowed
     */
    public static final String[] STATUS_FLAGS = {"APPROVED", "PROPOSED"};

    // Static variables
    //////////////////////////////////

    /** Set to true to include modified test versions */
    protected static boolean s_includeModified = false;

    /** Set to true to use approved tests only */
    protected static boolean s_approvedOnly = true;


    // Instance variables
    //////////////////////////////////

    /** The reasoner being tested */
    private DIGReasoner m_reasoner;

    /** The total set of known tests */
    private Model m_testDefinitions;

    /** The number of tests run */
    private int m_testCount = 0;

    /** The time cost in ms of the last test to be run */
    private long m_lastTestDuration = 0;

    /** Number of tests passed */
    private int m_passCount = 0;

    /** The model describing the results of the run */
    private Model m_testResults;

    /**
     * The resource which acts as a description for the Jena2 instance being
     * tested
     */
    private Resource m_jena2;


    // Constructors
    //////////////////////////////////

    public WebOntTests() {
        m_testDefinitions = loadAllTestDefinitions();
        m_reasoner = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        initResults();
    }

    
    // External signature methods
    //////////////////////////////////

    public static void main( String[] args ) throws IOException {
        String resultFile = "owl-results.rdf";
        String testName = null;
        
        if (args.length >= 1) {
            testName = args[0];
        }

        WebOntTests harness = new WebOntTests();
        if (testName == null) {
            harness.runTests();
        }
        else {
            harness.runTest( testName );
        }

        RDFWriter writer = harness.m_testResults.getWriter("RDF/XML-ABBREV");
        OutputStream stream = new FileOutputStream(resultFile);
        writer.setProperty("showXmlDeclaration", "true");
        harness.m_testResults.setNsPrefix("", "http://www.w3.org/1999/xhtml");
        writer.write(harness.m_testResults, stream, BASE_RESULTS_URI);
    }

    /**
     * Run all relevant tests.
     */
    public void runTests() {
        System.out.println("Testing " + (s_approvedOnly ? "only APPROVED" : "APPROVED and PROPOSED"));
        System.out.println("Positive entailment: ");
        runTests(findTestsOfType(OWLTest.PositiveEntailmentTest));
        System.out.println("\nNegative entailment: ");
        runTests(findTestsOfType(OWLTest.NegativeEntailmentTest));
        System.out.println("\nTrue tests: ");
        runTests(findTestsOfType(OWLTest.TrueTest));
        System.out.println("\nOWL for OWL tests: ");
        runTests(findTestsOfType(OWLTest.OWLforOWLTest));
        System.out.println("\nImport entailment tests: ");
        runTests(findTestsOfType(OWLTest.ImportEntailmentTest));
        System.out.println("\nInconsistency tests: ");
        runTests(findTestsOfType(OWLTest.InconsistencyTest));
        System.out.println("\nPassed " + m_passCount + " out of " + m_testCount);
    }

    /**
     * Run all tests in the given list.
     */
    public void runTests( List tests ) {
        for (Iterator i = tests.iterator(); i.hasNext();) {
            runTest((Resource) i.next());
        }
    }

    /**
     * Run a single test of any sort, performing any appropriate logging and
     * error reporting.
     */
    public void runTest( String test ) {
        runTest(m_testDefinitions.getResource(test));
    }

    /**
     * Run a single test of any sort, performing any appropriate logging and
     * error reporting.
     */
    public void runTest( Resource test ) {
        System.out.println("Running " + test);
        boolean success = false;
        boolean fail = false;
        try {
            success = doRunTest(test);
        }
        catch (Exception e) {
            fail = true;
            System.err.print("\nException: " + e);
            e.printStackTrace();
        }
        m_testCount++;
        
        if (success) {
            System.out.print((m_testCount % 40 == 0) ? ".\n" : ".");
            System.out.flush();
            m_passCount++;
        }
        else {
            System.out.println("\nFAIL: " + test);
        }
        Resource resultType = null;
        
        if (fail) {
            resultType = OWLResults.FailingRun;
        }
        else {
            if (test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest)
                    || test.hasProperty(RDF.type, OWLTest.ConsistencyTest)) {
                resultType = success ? OWLResults.PassingRun : OWLResults.FailingRun;
            }
            else {
                resultType = success ? OWLResults.PassingRun : OWLResults.IncompleteRun;
            }
        }
        
        // log to the rdf result format
        Resource result = m_testResults.createResource()
                                       .addProperty(RDF.type, OWLResults.TestRun)
                                       .addProperty(RDF.type, resultType)
                                       .addProperty(OWLResults.test, test)
                                       .addProperty(OWLResults.system, m_jena2);
    }

    /**
     * Run a single test of any sort, return true if the test succeeds.
     */
    public boolean doRunTest( Resource test ) 
        throws IOException 
    {
        if (test.hasProperty(RDF.type, OWLTest.PositiveEntailmentTest)
                || test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest)
                || test.hasProperty(RDF.type, OWLTest.OWLforOWLTest)
                || test.hasProperty(RDF.type, OWLTest.ImportEntailmentTest)
                || test.hasProperty(RDF.type, OWLTest.TrueTest)) {
            // Entailment tests
            boolean processImports = test.hasProperty( RDF.type, OWLTest.ImportEntailmentTest );
            Model premises = getDoc( test, RDFTest.premiseDocument, processImports );
            Model conclusions = getDoc( test, RDFTest.conclusionDocument );
            
            long t1 = System.currentTimeMillis();
            boolean correct = testEntailment( conclusions, m_reasoner.bind( premises.getGraph() ) );
            m_lastTestDuration = System.currentTimeMillis() - t1;

            if (test.hasProperty(RDF.type, OWLTest.NegativeEntailmentTest)) {
                correct = !correct;
            }
            return correct;
        }
        else if (false && test.hasProperty(RDF.type, OWLTest.InconsistencyTest)) {
            //            System.out.println("Starting: " + test);
            Model input = getDoc(test, RDFTest.inputDocument);
            long t1 = System.currentTimeMillis();
            InfGraph graph = m_reasoner.bind(input.getGraph());
            boolean correct = !graph.validate().isValid();
            m_lastTestDuration = System.currentTimeMillis() - t1;
            return correct;
        }
        else if (false && test.hasProperty(RDF.type, OWLTest.ConsistencyTest)) {
            // Not used normally becase we are not complete enough to prove
            // consistency
            //            System.out.println("Starting: " + test);
            Model input = getDoc(test, RDFTest.inputDocument);
            long t1 = System.currentTimeMillis();
            InfGraph graph = m_reasoner.bind(input.getGraph());
            boolean correct = graph.validate().isValid();
            long t2 = System.currentTimeMillis();
            m_lastTestDuration = t2 - t1;
            return correct;
        }
        else {
            for (StmtIterator i = test.listProperties(RDF.type); i.hasNext();) {
                System.out.println("Test type = " + i.nextStatement().getObject());
            }
            throw new ReasonerException("Unknown test type");
        }
    }

    /**
     * Load the premises or conclusions for the test, optional performing
     * import processing.
     */
    public Model getDoc( Resource test, Property docType, boolean processImports ) throws IOException {
        if (processImports) {
            Model result = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
            StmtIterator si = test.listProperties(docType);
            while (si.hasNext()) {
                String fname = si.nextStatement().getObject().toString() + ".rdf";
                loadFile(fname, result);
            }
            return result;
        }
        else {
            return getDoc(test, docType);
        }
    }

    /**
     * Load the premises or conclusions for the test.
     */
    public Model getDoc( Resource test, Property docType ) throws IOException {
        Model result = ModelFactory.createDefaultModel();
        StmtIterator si = test.listProperties(docType);
        while (si.hasNext()) {
            String fname = si.nextStatement().getObject().toString() + ".rdf";
            loadFile(fname, result);
        }
        return result;
    }

    /**
     * Utility to load a file into a model a Model. Files are assumed to be
     * relative to the BASE_URI.
     * 
     * @param file the file name, relative to baseDir
     * @return the loaded Model
     */
    public static Model loadFile( String file, Model model ) throws IOException {
        String langType = "RDF/XML";
        if (file.endsWith(".nt")) {
            langType = "N-TRIPLE";
        }
        else if (file.endsWith("n3")) {
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
    public boolean testEntailment( Model conclusions, InfGraph inf ) {
        List queryRoots = listQueryRoots( conclusions );
        Model result = ModelFactory.createDefaultModel();
        boolean correct = true;
        
        for (Iterator i = queryRoots.iterator(); i.hasNext(); ) {
            Resource root = (Resource) i.next();
            
            for (StmtIterator j = root.listProperties();  j.hasNext(); ) {
                Statement rootQuery = j.nextStatement();
                
                // add the resulting triples to the graph
                ExtendedIterator k =inf.find( rootQuery.getSubject().asNode(),
                                              rootQuery.getPredicate().asNode(),
                                              rootQuery.getObject().asNode(),
                                              conclusions.getGraph() ); 
                while (k.hasNext()) {
                    Triple t = (Triple) k.next();
                    LogFactory.getLog( getClass() ).debug( "testEntailment got triple " + t );
                    result.getGraph().add( t );
                }
            }
        }
        
        // now check that the conclusions, framed as a query, holds
        QueryHandler qh = result.queryHandler();
        Query query = WGReasonerTester.graphToQuery(conclusions.getGraph());
        Iterator i = qh.prepareBindings(query, new Node[] {}).executeBindings();
        return i.hasNext();
    }



    // Internal implementation methods
    //////////////////////////////////

    /** Load all of the known manifest files into a single model */
    protected Model loadAllTestDefinitions() {
        System.out.print("Loading manifests ");
        System.out.flush();
        Model testDefs = ModelFactory.createDefaultModel();
        int count = 0;
        for (int idir = 0; idir < TEST_DIRS.length; idir++) {
            File dir = new File(BASE_TESTDIR + TEST_DIRS[idir]);
            String[] manifests = dir.list(new FilenameFilter() {
                public boolean accept( File df, String name ) {
                    return name.startsWith("Manifest") && name.endsWith(".rdf") &&
                            (s_includeModified || !name.endsWith("-mod.rdf"));
                }
            });
            if (manifests == null) {
                System.err.println( "No manifests for " + BASE_TESTDIR + TEST_DIRS[idir] );
            }
            else {
                for (int im = 0; im < manifests.length; im++) {
                    String manifest = manifests[im];
                    File mf = new File(dir, manifest);
                    try {
                        testDefs.read(new FileInputStream(mf), "file:" + mf);
                        count++;
                        if (count % 8 == 0) {
                            System.out.print(".");
                            System.out.flush();
                        }
                    }
                    catch (FileNotFoundException e) {
                        System.out.println("File not readable - " + e);
                    }
                }
            }
        }
        System.out.println("loaded");
        return testDefs;
    }

    /**
     * Initialize the result model.
     */
    protected void initResults() {
        m_testResults = ModelFactory.createDefaultModel();
        m_jena2 = m_testResults.createResource(BASE_RESULTS_URI + "#jena2");
        m_jena2
                .addProperty(
                        RDFS.comment,
                        m_testResults
                                .createLiteral(
                                        "<a xmlns=\"http://www.w3.org/1999/xhtml\" href=\"http://jena.sourceforce.net/\">Jena2</a> includes a rule-based inference engine for RDF processing, "
                                                + "supporting both forward and backward chaining rules. Its OWL rule set is designed to provide sound "
                                                + "but not complete instance resasoning for that fragment of OWL/Full limited to the OWL/lite vocabulary. In"
                                                + "particular it does not support unionOf/complementOf.", true));
        m_jena2.addProperty(RDFS.label, "Jena2");
        m_testResults.setNsPrefix("results", OWLResults.NS);
    }


    /**
     * Return a list of all tests of the given type, according to the current
     * filters
     */
    public List findTestsOfType( Resource testType ) {
        ArrayList result = new ArrayList();
        StmtIterator si = m_testDefinitions.listStatements(null, RDF.type, testType);
        while (si.hasNext()) {
            Resource test = si.nextStatement().getSubject();
            boolean accept = true;
            // Check test status
            Literal status = (Literal) test.getProperty(RDFTest.status).getObject();
            if (s_approvedOnly) {
                accept = status.getString().equals(STATUS_FLAGS[0]);
            }
            else {
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

    /**
     * The query roots of a model are the subject resources that are not
     * the objects of any other statements.
     */
    protected List listQueryRoots( Model m ) {
        List l = new ArrayList();
        
        for (ResIterator i = m.listSubjects(); i.hasNext(); ) {
            Resource subj = i.nextResource();
            
            if (!m.contains( null, null, subj )) {
                // subj is not the object of any other statement
                l.add( subj );
            }
        }
        
        return l;
    }
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 * (c) Copyright Hewlett-Packard Development Company, LP 2003,2004 All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
