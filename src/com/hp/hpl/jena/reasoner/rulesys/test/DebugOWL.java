/******************************************************************
 * File:        DebugOWL.java
 * Created by:  Dave Reynolds
 * Created on:  12-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: DebugOWL.java,v 1.3 2003-06-13 16:31:15 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
//import com.hp.hpl.jena.graph.compose.Union;
//import com.hp.hpl.jena.mem.GraphMem;
//import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.ModelLoader;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

/**
 * Test harnness for investigating OWL reasoner correctness and performance
 * on specific local test files. Unit testing is done using OWLWGTester or simplar,
 * this code is a debugging tools rather than a tester.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-06-13 16:31:15 $
 */
public class DebugOWL {

    /** The base reasoner being tested */
    Reasoner reasoner = OWLFBRuleReasonerFactory.theInstance().create(null);
    
    /** The raw tests data as a Graph */
    Graph testdata;
    
    /** The inference graph under test */
    InfGraph infgraph;
    
    /**
     * Construct a test harness on a given source file.
     */
    public DebugOWL(String testFile) {
        testdata = ModelLoader.loadModel(testFile).getGraph();
//        testdata = new GraphMem();
//        ((OWLFBRuleReasoner)reasoner).setTraceOn(true);
        infgraph = reasoner.bind(testdata);
    }
    
    /**
     * Construct a test harness on a schema + data file
     */
    public DebugOWL(String schemaFile, String testFile) {
        testdata = ModelLoader.loadModel(testFile).getGraph();
        Graph schema = ModelLoader.loadModel(schemaFile).getGraph();
        infgraph = reasoner.bindSchema(schema).bind(testdata);
//        infgraph = reasoner.bind(new Union(schema, testdata));
    }
    
    /**
     * Test and time an access operation.
     */
    long list(Node s, Node p, Node o, boolean print) {
        long t1 = System.currentTimeMillis();
        int count = 0;
        for (Iterator i = infgraph.find(s,p,o); i.hasNext(); ) {
            Triple t = (Triple)i.next();
            count++;
            if (print) {
                System.out.println(" - " + PrintUtil.print(t));
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Found " + count + " results");
        return (t2 - t1);
    }
    
    public static void main(String[] args) {
        try {
            String dataFile = "file:testing/ontology/owl/list-syntax/test-with-import.rdf";
            String schemaFile = "file:vocabularies/owl.owl";
            
            DebugOWL tester = new DebugOWL(dataFile);
            System.out.println("Test data only started ...");
            long t = tester.list(null, RDF.type.asNode(), RDFS.Class.asNode(), true);
            System.out.println("Took " + t + "ms");
            
//            tester = new DebugOWL(schemaFile);
//            System.out.println("Test schema only started ...");
//            t = tester.list(null, RDF.type.asNode(), RDFS.Class.asNode(), false);
//            System.out.println("Took " + t + "ms");
//            
//            tester = new DebugOWL(schemaFile, dataFile);
//            System.out.println("Test schema + data  started ...");
//            t = tester.list(null, RDF.type.asNode(), RDFS.Class.asNode(), false);
//            System.out.println("Took " + t + "ms");
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