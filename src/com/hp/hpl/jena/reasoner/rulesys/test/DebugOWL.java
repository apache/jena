/******************************************************************
 * File:        DebugOWL.java
 * Created by:  Dave Reynolds
 * Created on:  12-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: DebugOWL.java,v 1.13 2003-07-17 11:01:19 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.io.IOException;
import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.mem.GraphMem;
//import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.ModelLoader;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
//import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;

import org.apache.log4j.Logger;

/**
 * Test harnness for investigating OWL reasoner correctness and performance
 * on specific local test files. Unit testing is done using OWLWGTester or simplar,
 * this code is a debugging tools rather than a tester.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.13 $ on $Date: 2003-07-17 11:01:19 $
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
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(DebugOWL.class);
    
    /** reasoner config: experimental ruleset and config */
    public static final int EXPT = 1;
    
    /** reasoner config: normal OWL-FB */
    public static final int OWLFB = 2;
    
    /** reasoner config: normal OWL forward */
    public static final int OWL = 3;
    
    /** reasoner config: normal RDFS */
    public static final int RDFSFB = 4;
    
    /** reasoner config: experimental RDFS - hybrid + TGC */
    public static final int RDFSExpt = 5;
    
    /** reasoner config: experimental OWL */
    public static final int OWLExpt = 6;
    
    
    /**
     * Construct an empty test harness.
     */
    public DebugOWL(int config) {
        testdata = new GraphMem();
        schema = null;
        
        switch(config) {
            
        case EXPT:
            reasoner = GenericRuleReasonerFactory.theInstance().create(null);
            GenericRuleReasoner grr = (GenericRuleReasoner)reasoner;
            grr.setMode(GenericRuleReasoner.HYBRID);
            try {
                grr.setRules(Rule.parseRules(Util.loadResourceFile("etc/expt.rules")));
            } catch (IOException e) {
                System.out.println("Failed to open rules file: " + e);
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
            
            case OWL:
                reasoner = OWLRuleReasonerFactory.theInstance().create(null);
//                ((OWLRuleReasoner)reasoner).setTraceOn(true);
                break;
            
            case RDFSFB:
                reasoner = RDFSFBRuleReasonerFactory.theInstance().create(null);
                break;
            
            case RDFSExpt:
                reasoner = RDFSRuleReasonerFactory.theInstance().create(null);
                break;
            
            case OWLExpt:
                reasoner = OWLExptRuleReasonerFactory.theInstance().create(null);
//                ((OWLExptRuleReasoner)reasoner).setTraceOn(true);
                break;
            
        } 
        
    }
    
    /**
     * Load a test data set from file.
     */
    public void load(String testFile) {
        testdata = ModelLoader.loadModel(testFile).getGraph();
        schema = null;
    }
    
    /**
     * Load both a schema and an instance data file.
     */
    public void load(String schemaFile, String testFile) {
        testdata = ModelLoader.loadModel(testFile).getGraph();
        schema = ModelLoader.loadModel(schemaFile).getGraph();
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
        testdata = new GraphMem();
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
                    Node concept = Node.createURI("concept" + conceptPtr);
                    if (withProps) { 
                        property = Node.createURI("prop" + conceptPtr);
                        properties[conceptPtr] = property;
                    }
                    concepts[conceptPtr++] = concept;
                }
            } else {
                for (int j = levelStart; j < levelEnd; j++) {
                    Node superConcept = concepts[j];
                    for (int k = 0; k < NS; k++) {
                        Node concept = Node.createURI("concept" + conceptPtr);
                        if (withProps) { 
                            property = Node.createURI("prop" + conceptPtr);
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
                    Node instance = Node.createURI("instance"+instancePtr);
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
        for (Iterator i = infgraph.find(s,p,o); i.hasNext(); ) {
            Triple t = (Triple)i.next();
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
    }
    
    /**
     * Run a standard test squence based on Volz et al sets
     */
    public void runVolz() {
        runVolz(3,5,10, false);
        runVolz(3,5,10, false);
        runVolz(4,5,10, false);
        runVolz(5,5,10, false);
        runVolz(3,5,30, false);
        runVolz(4,5,30, false);
        runVolz(5,5,30, false);
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
            new DebugOWL(OWLExpt).listClassesOn(dataFile2);
            
            // owl.owl goes into meltdown with even the forward rules
//            new DebugOWL(OWLFB).run(schemaFile);
//            new DebugOWL(OWL).run("file:temp/owl-subset.owl");
            
            // Test volz examples on OWL config
//            new DebugOWL(OWLFB).runVolz();
//            new DebugOWL(OWLExpt).runVolz();
            
            // Test volz examples on RDFS config
//            System.out.println("Volz tests on RDFSRule");
//            new DebugOWL(RDFSExpt).runVolz();
//            System.out.println("Volz tests on expt, not tgc just type rules");
//            new DebugOWL(EXPT).runVolz();
                        
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