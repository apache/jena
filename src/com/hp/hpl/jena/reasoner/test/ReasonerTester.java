/******************************************************************
 * File:        QueryTester.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: ReasonerTester.java,v 1.9 2003-05-05 21:52:43 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.util.*;
import java.io.*;

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
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.9 $ on $Date: 2003-05-05 21:52:43 $
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
    protected Map sourceCache = new HashMap();
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(ReasonerTester.class);
    
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
            return (Model)sourceCache.get(file);
        }
        String langType = "RDF/XML";
        if (file.endsWith(".nt")) {
            langType = "N-TRIPLE";
        } else if (file.endsWith("n3")) {
            langType = "N3";
        }
        Model result = new ModelMem();
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
            String fileName = test.getProperty(predicate).getObject().toString();
            boolean cache = predicate.equals(tboxP) || predicate.equals(dataP);
            return loadFile(fileName, cache).getGraph();
        } else {
            return new GraphMem();
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
     * @throws RDFException if the test can't be found or fails internally
     */
    public boolean runTests(ReasonerFactory reasonerF, TestCase testcase, Model configuration) throws IOException {
        ResIterator tests = testManifest.listSubjectsWithProperty(RDF.type, testClass);
        while (tests.hasNext()) {
            String test = tests.next().toString();
            if (!runTest(test, reasonerF, testcase, configuration)) return false;
        }
        return true;
    }
    
    /**
     * Run a single designated test.
     * @param uri the uri of the test, as defined in the manifest file
     * @param reasonerF the factory for the reasoner to be tested
     * @param testcase the JUnit test case which is requesting this test
     * @param configuration optional configuration information
     * @return true if the test passes
     * @throws IOException if one of the test files can't be found
     * @throws RDFException if the test can't be found or fails internally
     */
    public boolean runTest(String uri, ReasonerFactory reasonerF, TestCase testcase, Model configuration) throws IOException {
        // Find the specification for the named test
        Resource test = testManifest.getResource(uri);
        if (!test.hasProperty(RDF.type, testClass)) {
            throw new RDFException("Can't find test: " + uri);
        }

        String description = test.getProperty(descriptionP).getObject().toString();
        logger.debug("Reasoner test " + test.getURI() + " - " + description);
        
        // Construct the inferred graph
        Graph tbox = loadTestFile(test, tboxP);
        Graph data = loadTestFile(test, dataP);
        Reasoner reasoner = reasonerF.create(configuration);
        InfGraph graph = reasoner.bindSchema(tbox).bind(data);
        
        // Run each query triple and accumulate the results
        Graph queryG = loadTestFile(test, queryP);
        Graph resultG = new GraphMem();

        Iterator queries = queryG.find(null, null, null);
        while (queries.hasNext()) {
            TriplePattern query = tripleToPattern((Triple)queries.next());
            logger.debug("Query: " + query);
            Iterator answers = graph.find(query.asTripleMatch());
            while (answers.hasNext()) {
                Triple ans = (Triple)answers.next();
                logger.debug("ans: " + TriplePattern.simplePrintString(ans));
                resultG.add(ans);
            }
        }
        
        // Check the total result set against the correct answer
        Graph correctG = loadTestFile(test, resultP);
        boolean correct = correctG.isIsomorphicWith(resultG);
        // Used in debugging the tests ...
        /*
        if (!correct) {
            System.out.println("Missing triples:");
            for (Iterator i = correctG.find(null, null, null); i.hasNext(); ) {
                Triple t = (Triple) i.next();
                if (!resultG.contains(t)) {
                    System.out.println("  - " + t);
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
            TestCase.assertTrue(description, correct);
        }
        return correct;
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

