/******************************************************************
 * File:        ManualExample.java
 * Created by:  Dave Reynolds
 * Created on:  26-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ManualExample.java,v 1.6 2003-08-27 13:11:15 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.ModelLoader;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

import java.io.PrintWriter;
import java.util.*;

/**
 * Some code samples from the user manual.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-08-27 13:11:15 $
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
        Model data = ModelLoader.loadModel(fname);
        InfModel infmodel = ModelFactory.createRDFSModel(data);
        ValidityReport validity = infmodel.validate();
        if (validity.isValid()) {
            System.out.println("OK");
        } else {
            System.out.println("Conflicts");
            for (Iterator i = validity.getReports(); i.hasNext(); ) {
                ValidityReport.Report report = (ValidityReport.Report)i.next();
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
            for (Iterator id = inf.getDerivation(s); id.hasNext(); ) {
                Derivation deriv = (Derivation) id.next();
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
        Iterator list = inf.listStatements(A, null, (RDFNode)null);
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



/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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