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

import java.io.BufferedReader ;
import java.io.FileReader ;
import java.io.IOException ;
import java.io.Reader ;
import java.util.* ;

import junit.framework.TestCase ;
import org.junit.Assert ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl ;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl ;
import com.hp.hpl.jena.reasoner.InfGraph ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.ReasonerFactory ;
import com.hp.hpl.jena.reasoner.TriplePattern ;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.vocabulary.RDF ;

/**
 * A utility for loading a set of test reasoner problems and running defined
 * sets of listStatement operations and checking the results.
 * <p>
 * Each of the source, query and result models are specified in
 * different files. The files can be of type .rdf, .nt or .n3.</p>
 * <p>
 * A single manifest file defines the set of tests to run. Each test
 * specifies a name, source tbox file, source data file, query file and result file using 
 * the properties "name", "source", "query" and "result" in the namespace
 * "http://www.hpl.hp.com/semweb/2003/query_tester#". The file names are 
 * given as strings instead of URIs because the base directory for the test
 * files is subject to change. </p>
 * <p>
 * Within the query file each triple is treated as a triple pattern
 * to be searched for. Variables are indicated by resources in of the
 * form "var:x".</p>
 */
public class ReasonerTester {

    /** The namespace for the test specification schema */
    public static final String NS = "http://www.hpl.hp.com/semweb/2003/query_tester#";
    
    /** The base URI in which the files are purported to reside */
    public static final String BASE_URI = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    /** The rdf class to which all tests belong */
    public static final Resource testClass;
    
    /** The predicate defining the description of the test */
    public static final Property descriptionP;
    
    /** The predicate defining the source tbox file for the test */
    public static final Property tboxP;
    
    /** The predicate defining the source data file for the test */
    public static final Property dataP;
    
    /** The predicate defining the query file for the test */
    public static final Property queryP;
    
    /** The predicate defining the result file for the test */
    public static final Property resultP;
    
    /** The base directory in which the test data is stored */
    public static final String baseDir = "testing/reasoners/";
    
    // Static initializer for the predicates
    static {
        descriptionP = new PropertyImpl(NS, "description");
        tboxP = new PropertyImpl(NS, "tbox");
        dataP = new PropertyImpl(NS, "data");
        queryP = new PropertyImpl(NS, "query");
        resultP = new PropertyImpl(NS, "result");
        testClass = new ResourceImpl(NS, "Test");
    }
    
    /** The rdf defining all the tests to be run */
    protected Model testManifest;
    
    /** A cache of loaded source files, map from source name to Model */
    protected Map<String, Model> sourceCache = new HashMap<>();
    
    protected static Logger logger = LoggerFactory.getLogger(ReasonerTester.class);
    
    /**
     * Constructor.
     * @param manifest the name of the manifest file defining these
     * tests - relative to baseDir
     */
    public ReasonerTester(String manifest) throws IOException {
        testManifest = loadFile(manifest, false);
    }
    
    /**
     * Utility to load a file in rdf/nt/n3 format as a Model.
     * @param file the file name, relative to baseDir
     * @param cache set to true if the file could be usefully cached
     * @return the loaded Model
     */
    public Model loadFile(String file, boolean cache) throws IOException {
        if (cache && sourceCache.keySet().contains(file)) {
            return sourceCache.get(file);
        }
        String langType = "RDF/XML";
        if (file.endsWith(".nt")) {
            langType = "N-TRIPLE";
        } else if (file.endsWith("n3")) {
            langType = "N3";
        }
        Model result = ModelFactory.createDefaultModel();
        Reader reader = new BufferedReader(new FileReader(baseDir + file));
        result.read(reader, BASE_URI + file, langType);
        if (cache) {
            sourceCache.put(file, result);
        }
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
    public Graph loadTestFile(Resource test, Property predicate) throws IOException {
        if (test.hasProperty(predicate)) {
            String fileName = test.getRequiredProperty(predicate).getObject().toString();
            boolean cache = predicate.equals(tboxP) || predicate.equals(dataP);
            return loadFile(fileName, cache).getGraph();
        } else {
            return Factory.createGraphMem();
        }
    }
    
    /**
     * Convert a triple into a triple pattern by converting var resources into
     * wildcard variables.
     */
    public static TriplePattern tripleToPattern(Triple t) {
        return new TriplePattern(
                        nodeToPattern(t.getSubject()),
                        nodeToPattern(t.getPredicate()),
                        nodeToPattern(t.getObject()));
    }
    
    /**
     * Convert a node into a pattern node by converting var resources into wildcard
     * variables.
     */
    public static Node nodeToPattern(Node n) {
        if (n.isURI() && n.toString().startsWith("var:")) {
            return Node_RuleVariable.WILD;
//            return Node.ANY;
        } else {
            return n;
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
     * Run all the tests in the manifest
     * @param reasoner the reasoner to be tested
     * @param testcase the JUnit test case which is requesting this test
     * @return true if all the tests pass
     * @throws IOException if one of the test files can't be found
     * @throws JenaException if the test can't be found or fails internally
     */
    public boolean runTests(Reasoner reasoner, TestCase testcase) throws IOException {
        for ( String test : listTests() )
        {
            if ( !runTest( test, reasoner, testcase ) )
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
        ResIterator tests = testManifest.listResourcesWithProperty(RDF.type, testClass);
        while (tests.hasNext()) {
            testList.add(tests.next().toString());
        }
        return testList;
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
        Reasoner reasoner = reasonerF.create(configuration);
        return runTest(uri, reasoner, testcase);
    }
    
    /**
     * Run a single designated test.
     * @param uri the uri of the test, as defined in the manifest file
     * @param reasoner the reasoner to be tested
     * @param testcase the JUnit test case which is requesting this test
     * @return true if the test passes
     * @throws IOException if one of the test files can't be found
     * @throws JenaException if the test can't be found or fails internally
     */
    public boolean runTest(String uri, Reasoner reasoner, TestCase testcase) throws IOException {
        // Find the specification for the named test
        Resource test = testManifest.getResource(uri);
        if (!test.hasProperty(RDF.type, testClass)) {
            throw new JenaException("Can't find test: " + uri);
        }

        String description = test.getRequiredProperty(descriptionP).getObject().toString();
        logger.debug("Reasoner test " + test.getURI() + " - " + description);
        
        // Construct the inferred graph
        Graph tbox = loadTestFile(test, tboxP);
        Graph data = loadTestFile(test, dataP);
        InfGraph graph = reasoner.bindSchema(tbox).bind(data);
        
        // Run each query triple and accumulate the results
        Graph queryG = loadTestFile(test, queryP);
        Graph resultG = Factory.createGraphMem();

        Iterator<Triple> queries = queryG.find(null, null, null);
        while (queries.hasNext()) {
            TriplePattern query = tripleToPattern( queries.next() );
            logger.debug("Query: " + query);
            Iterator<Triple> answers = graph.find(query.asTripleMatch());
            while (answers.hasNext()) {
                Triple ans = answers.next();
                logger.debug("ans: " + TriplePattern.simplePrintString(ans));
                resultG.add(ans);
            }
        }
        
        // Check the total result set against the correct answer
        Graph correctG = loadTestFile(test, resultP);
        boolean correct = correctG.isIsomorphicWith(resultG);
        // Used in debugging the tests ...
        // Can't just leave it as a logger.debug because there are unit tests to which are supposed to given
        // a test failure which would then problem unwanted output.
        /*
        System.out.println("Reasoner test " + test.getURI() + " - " + description);
        if (!correct) {
            System.out.println("Missing triples:");
            for (Iterator i = correctG.find(null, null, null); i.hasNext(); ) {
                Triple t = (Triple) i.next();
                if (!resultG.contains(t)) {
                    System.out.println("  " + t);
                }
            }
            System.out.println("Extra triples:");
            for (Iterator i = resultG.find(null, null, null); i.hasNext(); ) {
                Triple t = (Triple) i.next();
                if (!correctG.contains(t)) {
                    System.out.println("  - " + t);
                }
            }
            
        }
        */
        // ... end of debugging hack
        if (testcase != null) {
            Assert.assertTrue(description, correct);
        }
        return correct;
    }
    
}
