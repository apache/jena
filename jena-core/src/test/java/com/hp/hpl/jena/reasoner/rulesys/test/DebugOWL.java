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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Union;
//import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.WrappedIOException;
//import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Test harnness for investigating OWL reasoner correctness and performance
 * on specific local test files. Unit testing is done using OWLWGTester or simplar,
 * this code is a debugging tools rather than a tester.
 */
public class DebugOWL {

    /** The base reasoner being tested */
    Reasoner reasoner;
    
    /** The raw tests data as a Graph */
    Graph testdata;
    
    /** The (optional) schema graph used in interpreting the test data */
    Graph schema;
    
    /** The inference graph under test */
    InfGraph infgraph;
    
    /** Concepts created by testGenerator, [layer, index] */
    Node[] concepts;
    
    /** Instances of each concept */
    Node[] instances;
    
    /** Instance properties */
    Node[] properties;
        
    static Logger logger = LoggerFactory.getLogger(DebugOWL.class);
    
    /** reasoner config: experimental ruleset and config */
    public static final int EXPT = 1;
    
    /** reasoner config: normal OWL-FB */
    public static final int OWLFB = 2;
    
    /** reasoner config: normal OWL forward */
    public static final int OWL = 3;
    
    /** reasoner config: normal RDFS */
    public static final int RDFSFB = 4;
    
    /** reasoner config: final RDFS - hybrid + TGC */
    public static final int RDFSFinal = 5;
    
    /** reasoner config: experimental OWL */
    public static final int OWLExpt = 6;
    
    /** reasoner config: LP RDFS exp */
    public static final int RDFSLPExpt = 7;
    
    
    /**
     * Construct an empty test harness.
     */
    public DebugOWL(int config) {
        testdata = Factory.createGraphMem();
        schema = null;
        
        switch(config) {
            
        case EXPT:
            reasoner = GenericRuleReasonerFactory.theInstance().create(null);
            GenericRuleReasoner grr = (GenericRuleReasoner)reasoner;
            grr.setMode(GenericRuleReasoner.HYBRID);
            try {
                grr.setRules(Rule.parseRules(Util.loadRuleParserFromResourceFile("etc/expt.rules")));
            } catch (WrappedIOException e) {
                System.out.println("Failed to open rules file: " + e.getCause() );
                System.exit(1);
            }
//            grr.setTransitiveClosureCaching(true);
//            grr.setOWLTranslation(true);
//            grr.setTraceOn(true);
            break;
            
            case OWLFB:
                reasoner = OWLFBRuleReasonerFactory.theInstance().create(null);
//                ((OWLFBRuleReasoner)reasoner).setTraceOn(true);
                break;
                        
            case RDFSFB:
                reasoner = RDFSFBRuleReasonerFactory.theInstance().create(null);
                break;
            
            case RDFSFinal:
                reasoner = RDFSRuleReasonerFactory.theInstance().create(null);
                break;
                        
            case RDFSLPExpt:
                try {
                    List<Rule> rules = Rule.parseRules(Util.loadRuleParserFromResourceFile("etc/expt.rules"));
                    reasoner = new FBRuleReasoner(rules);
                } catch (WrappedIOException e) {
                    System.out.println("Failed to open rules file: " + e.getCause());
                    System.exit(1);
                }
                break;
            
        } 
        
    }
    
    /**
     * Load a test data set from file.
     */
    public void load(String testFile) {
        testdata = FileManager.get().loadModel(testFile).getGraph();
        schema = null;
    }
    
    /**
     * Load both a schema and an instance data file.
     */
    public void load(String schemaFile, String testFile) {
        testdata = FileManager.get().loadModel(testFile).getGraph();
        schema = FileManager.get().loadModel(schemaFile).getGraph();
    }
    
    /**
     * Create an artificial data set. This variant puts schema and
     * instance data into the same testdata graph.
     * @param depth the depth of the concept tree
     * @param NS the number of subclasses at each tree level
     * @param NI the number of instances of each concept
     * @param withProps if true then properties are created for each concept and instiated for every third instance
     */
    public void createTest(int depth, int NS, int NI, boolean withProps) {
        // Calculate total store sizes and allocate
        int numClasses = 0;
        int levelSize = 1;
        for (int i = 0; i < depth; i++) {
            levelSize *= NS; 
            numClasses += levelSize;
        }
        concepts = new Node[numClasses];
        properties = new Node[numClasses];
        instances = new Node[numClasses * NI];
        logger.info("Classes: " + numClasses +" Instances: " + (numClasses * NI)
                        + (withProps ? " with properties" : ""));
        
        // Create the tree
        testdata = Factory.createGraphMem();
        // First level
        int conceptPtr = 0;
        int levelStart = 0;
        int levelEnd =  0;
        int instancePtr = 0;
        for (int i = 0; i < depth; i++) {
            // Class tree
            Node property = null;
            if (i == 0) {
                for (int j = 0; j < NS; j++) {
                    Node concept = NodeFactory.createURI("concept" + conceptPtr);
                    if (withProps) { 
                        property = NodeFactory.createURI("prop" + conceptPtr);
                        properties[conceptPtr] = property;
                    }
                    concepts[conceptPtr++] = concept;
                }
            } else {
                for (int j = levelStart; j < levelEnd; j++) {
                    Node superConcept = concepts[j];
                    for (int k = 0; k < NS; k++) {
                        Node concept = NodeFactory.createURI("concept" + conceptPtr);
                        if (withProps) { 
                            property = NodeFactory.createURI("prop" + conceptPtr);
                            properties[conceptPtr] = property;
                        }
                        concepts[conceptPtr++] = concept;
                        testdata.add(new Triple(concept, RDFS.subClassOf.asNode(), superConcept));
                    }
                }
            }
            levelStart = levelEnd;
            levelEnd = conceptPtr;
            // Instance data
            for (int j = levelStart; j < levelEnd; j++) {
                Node concept = concepts[j];
                for (int k = 0; k < NI; k++) {
                    Node instance = NodeFactory.createURI("instance"+instancePtr);
                    testdata.add(new Triple(instance, RDF.type.asNode(), concept));
                    if (withProps && (k-1)%3 == 0) {
                        testdata.add(new Triple(instances[instancePtr-1], property, instance));
                    }
                    instances[instancePtr++] = instance;
                }
            }
        }
    }
    
    /**
     * Configure the inference graph ready for testing.
     */
    public void init() {
        if (schema == null) {
            infgraph = reasoner.bind(testdata);
        } else {
//            infgraph = reasoner.bindSchema(schema).bind(testdata);
            infgraph = reasoner.bind(new Union(schema, testdata));
        }
//        if (infgraph instanceof FBRuleInfGraph) {
//            ((FBRuleInfGraph)infgraph).resetLPProfile(true);
//        }
        if (infgraph instanceof FBRuleInfGraph) {
            System.out.println("Starting prepare");
            ((FBRuleInfGraph)infgraph).prepare();
            System.out.println(".. finished");
        }
    }
    
    /**
     * Test and time an predefined class extension listing
     */
    long listC0(boolean print) {
        return list(null, RDF.type.asNode(), concepts[0], print);
    }
    
    /**
     * Test and time an general access operation.
     */
    long list(Node s, Node p, Node o, boolean print) {
        long t1 = System.currentTimeMillis();
        init();
        int count = 0;
        for (Iterator<Triple> i = infgraph.find(s,p,o); i.hasNext(); ) {
            Triple t = i.next();
            count++;
            if (print) {
                logger.info(PrintUtil.print(t));
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Found " + count + " results");
        return (t2 - t1);
    }
   
    /**
     * Create and run a list classes test.
     */
    public void runListClassesTest(int depth, int NS, int NI, boolean withProps) {
        createTest(depth, NS, NI, withProps);
        long t = list(null, RDF.type.asNode(), RDFS.Class.asNode(), false);
        System.out.println("Took " + t + "ms");
    }
   
    /**
     * Create and run a volz test.
     */
    public void runVolz(int depth, int NS, int NI, boolean withProps) {
        createTest(depth, NS, NI, withProps);
        long t = listC0(false);
        System.out.println("Took " + t + "ms");
        if (infgraph instanceof FBRuleInfGraph) {
            ((FBRuleInfGraph)infgraph).printLPProfile();
        }
    }
    
    /**
     * Run a standard test squence based on Volz et al sets
     */
    public void runVolz() {
        runVolz(3,5,10, false);
        runVolz(3,5,10, false);
        runVolz(4,5,10, false);
        runVolz(5,5,10, false);
        
//        runVolz(3,5,30, false);
//        runVolz(4,5,30, false);
//        runVolz(5,5,30, false);
//        run(3,5,10, true);
//        run(4,5,10, true);
//        run(5,5,10, true);
    }
    
    /**
     * Run default test on a named file.
     */
    public void listClassesOn(String filename) {
        load(filename);
        System.out.println("Testing: " + filename);
        long t = list(null, RDF.type.asNode(), RDFS.Class.asNode(), false);
        System.out.println("Took " + t + "ms");
    }
    
    public static void main(String[] args) {
        try {
            String dataFile = "file:testing/ontology/owl/list-syntax/test-with-import.rdf";
            String schemaFile = "file:vocabularies/owl.owl";
            String schemaFile2 = "file:testing/reasoners/bugs/owl-partial.owl";
            String dataFile2 = "file:testing/reasoners/bugs/test.owl";
            String food = "file:testing/reasoners/bugs/food.owl";

            // Example from ontology development which takes s rather than ms            
//            new DebugOWL(OWLExpt).listClassesOn(dataFile2);
            
            // owl.owl goes into meltdown with even the forward rules
//            new DebugOWL(OWLFB).run(schemaFile);
//            new DebugOWL(OWL).run("file:temp/owl-subset.owl");
            
            // Test volz examples on OWL config
//            new DebugOWL(OWLFB).runVolz();
//            new DebugOWL(OWLExpt).runVolz();
            
            // Test volz examples on RDFS config
            System.out.println("Volz tests on normal RDFS, tgc + type rules");
            new DebugOWL(RDFSFinal).runVolz();
//            System.out.println("Volz tests on lp + expt RDFS rules");
//            new DebugOWL(RDFSLPExpt).runVolz();
            
//            System.out.println("Volz tests on normal RDFS fb rules");
//            new DebugOWL(RDFSFB).runVolz();
//            System.out.println("Volz tests on lp + expt owl rules");
//            new DebugOWL(OWLExpt).runVolz();
//            System.out.println("Volz tests on normal OWL-FB");
//            new DebugOWL(OWLFB).runVolz();
                        
//            DebugOWL tester = new DebugOWL(OWLFB);
//            tester.load(dataFile2);
//            System.out.println("Test schema + data  started ...");
//            long t = tester.list(null, RDF.type.asNode(), RDFS.Class.asNode(), false);
//            System.out.println("Took " + t + "ms");

//            DebugOWL tester = new DebugOWL(EXPT);
//            tester.runListClassesTest(1,4,10,false);
//            tester.runListClassesTest(1,4,10,false);
//            tester.runListClassesTest(2,4,10,false);
//            tester.runListClassesTest(3,4,10,false);
//            tester.runListClassesTest(3,5,10,false);
//            tester.runListClassesTest(3,6,10,false);

        } catch (Exception e) {
            System.out.println("Problem: " + e);
            e.printStackTrace();
        }
    }
    
}
