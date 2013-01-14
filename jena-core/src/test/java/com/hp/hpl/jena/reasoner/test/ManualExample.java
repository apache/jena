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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

import java.io.PrintWriter;
import java.util.*;

/**
 * Some code samples from the user manual.
 */
public class ManualExample {

    /** Illustrate different ways of finding a reasoner */
    public void test1() {
        String NS = "urn:x-hp-jena:eg/";
        
        // Build a trivial example data set
        Model rdfsExample = ModelFactory.createDefaultModel();
        Property p = rdfsExample.createProperty(NS, "p");
        Property q = rdfsExample.createProperty(NS, "q");
        rdfsExample.add(p, RDFS.subPropertyOf, q);
        rdfsExample.createResource(NS+"a")
                   .addProperty(p, "foo");
        
        // Create an RDFS inference model the easy way
//        InfModel inf = ModelFactory.createRDFSModel(rdfsExample);
        // Create an RDFS inference model the hard way
        Resource config = ModelFactory.createDefaultModel()
                          .createResource()
                          .addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "simple");
        Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(config);
        // Set the parameter the easier way
//        reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel, 
//                              ReasonerVocabulary.RDFS_SIMPLE);
        InfModel inf = ModelFactory.createInfModel(reasoner, rdfsExample);
        Resource a = inf.getResource(NS+"a");
        Statement s = a.getProperty(q);
        System.out.println("Statement: " + s);
    }
    
    /** illustrate validation */
    public void test2(String fname) {
        System.out.println("Testing " + fname);
        Model data = FileManager.get().loadModel(fname);
        InfModel infmodel = ModelFactory.createRDFSModel(data);
        ValidityReport validity = infmodel.validate();
        if (validity.isValid()) {
            System.out.println("OK");
        } else {
            System.out.println("Conflicts");
            for (Iterator<Report> i = validity.getReports(); i.hasNext(); ) {
                ValidityReport.Report report = i.next();
                System.out.println(" - " + report);
//                System.out.println(" - " + i.next());
            }
        }
    }
    
    /** illustrate generic rules and derivation tracing */
    public void test3() {
        // Test data
        String egNS = PrintUtil.egNS;   // Namespace for examples
        Model rawData = ModelFactory.createDefaultModel();
        Property p = rawData.createProperty(egNS, "p");
        Resource A = rawData.createResource(egNS + "A");
        Resource B = rawData.createResource(egNS + "B");
        Resource C = rawData.createResource(egNS + "C");
        Resource D = rawData.createResource(egNS + "D");
        A.addProperty(p, B);
        B.addProperty(p, C);
        C.addProperty(p, D);
        
        // Rule example
        String rules = "[rule1: (?a eg:p ?b) (?b eg:p ?c) -> (?a eg:p ?c)]";
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        InfModel inf = ModelFactory.createInfModel(reasoner, rawData);
        
        PrintWriter out = new PrintWriter(System.out);
        for (StmtIterator i = inf.listStatements(A, p, D); i.hasNext(); ) {
            Statement s = i.nextStatement(); 
            System.out.println("Statement is " + s);
            for (Iterator<Derivation> id = inf.getDerivation(s); id.hasNext(); ) {
                Derivation deriv = id.next();
                deriv.printTrace(out, true);
            }
        }
        out.flush();
    }
    
    /** Another generic rules illustration */
    public void test4() {
        // Test data
        String egNS = PrintUtil.egNS;   // Namespace for examples
        Model rawData = ModelFactory.createDefaultModel();
        Property first = rawData.createProperty(egNS, "concatFirst");
        Property second = rawData.createProperty(egNS, "concatSecond");
        Property p = rawData.createProperty(egNS, "p");
        Property q = rawData.createProperty(egNS, "q");
        Property r = rawData.createProperty(egNS, "r");
        Resource A = rawData.createResource(egNS + "A");
        Resource B = rawData.createResource(egNS + "B");
        Resource C = rawData.createResource(egNS + "C");
        A.addProperty(p, B);
        B.addProperty(q, C);
        r.addProperty(first, p);
        r.addProperty(second, q);
        
        // Rule example for
        String rules = 
            "[r1: (?c eg:concatFirst ?p), (?c eg:concatSecond ?q) -> " +            "     [r1b: (?x ?c ?y) <- (?x ?p ?z) (?z ?q ?y)] ]";        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
//        reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);
        InfModel inf = ModelFactory.createInfModel(reasoner, rawData);
//        System.out.println("OK = " + inf.contains(A, r, C));
        Iterator<Statement> list = inf.listStatements(A, null, (RDFNode)null);
        System.out.println("A * * =>");
        while (list.hasNext()) {
            System.out.println(" - " + list.next());
        }
    }
    
    public static void main(String[] args) {
        try {
//            new ManualExample().test1();
//            new ManualExample().test2("file:testing/reasoners/rdfs/dttest2.nt");
//            new ManualExample().test2("file:testing/reasoners/rdfs/dttest3.nt");
//            new ManualExample().test3();
            new ManualExample().test4();
        } catch (Exception e) {
            System.out.println("Problem: " + e);
            e.printStackTrace();
        }
    }
}
