/******************************************************************
 * File:        DebugMakeInstance.java
 * Created by:  Dave Reynolds
 * Created on:  13-Aug-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: DebugMakeInstance.java,v 1.1 2003-08-14 07:51:09 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.OWLExptRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.test.OWLWGTester;
import com.hp.hpl.jena.util.ModelLoader;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.*;

/**
 * A debug framework used to isolate the essentials of the "someValueFrom"
 * test which gives random failures under the normal OWL test harness.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-08-14 07:51:09 $
 */
public class DebugMakeInstance extends TestCase {

    /** The name of the manifest file to test */
    protected String manifest;
    
    /**
     * Boilerplate for junit
     */ 
    public DebugMakeInstance( String manifest ) {
        super( manifest ); 
        this.manifest = manifest;
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new DebugMakeInstance("someValuesFrom/Manifest001.rdf"));
        return suite;
    }
    
    /**
     * Construct the reasoner under test
     */
    public Reasoner makeReasoner() {
        return OWLExptRuleReasonerFactory.theInstance().create(null);
    }
    
    /**
     * Test the results match the expected conclusions.
     * In this case it is overridden to perform manual tests to isolate the problem.
     */
    protected boolean testConclusions(Graph expected, Graph resultGraph) {
        Node i = Node.createURI("http://www.w3.org/2002/03owlt/someValuesFrom/premises001#i");
        Node p = Node.createURI("http://www.w3.org/2002/03owlt/someValuesFrom/premises001#p");
        Node c = Node.createURI("http://www.w3.org/2002/03owlt/someValuesFrom/premises001#c");
        
        boolean ok = false;
        for (Iterator it = resultGraph.find(i, p, null); it.hasNext(); ) {
            Triple t = (Triple)it.next();
            Node val = t.getObject();
            System.out.println("Found i p " + val);
            if (resultGraph.contains(val, RDF.Nodes.type, c)) {
                System.out.println("Which is type c");
                ok = true;
            } else {
                System.out.println("Not type c");
            }
        }
        return ok;
        
//        // Alternative sequence 
//        boolean ok = false;
//        for (Iterator it = resultGraph.find(null, RDF.Nodes.type, c); it.hasNext(); ) {
//            Triple t = (Triple)it.next();
//            Node val = t.getSubject();
//            System.out.println("Found something of type c " + val);
//            if (resultGraph.contains(i, p, val)) {
//                System.out.println("Found - i p " + val);
//                ok = true;
//            }
//        }
//        return ok;
        
    }
    
    /**
     * Load up a premise/conclusions document.
     */
    protected Model loadTestModel(String uri) {
        String fname = uri;
        if (fname.startsWith(OWLWGTester.BASE_URI)) {
            fname = fname.substring(OWLWGTester.BASE_URI.length());
        }
        return ModelLoader.loadModel(OWLWGTester.baseDir + fname + ".rdf");
    }
    
    /**
     * The test runner
     */
    protected void runTest() throws IOException {
        // Load up the manifest
        Model spec = ModelLoader.loadModel(OWLWGTester.baseDir + manifest);
        ResIterator tests = spec.listSubjectsWithProperty(RDF.type, OWLWGTester.PositiveEntailmentTest);
        while (tests.hasNext()) {
            Resource test = tests.nextResource();
            // Find the specification for the named test
            String description = test.getRequiredProperty(OWLWGTester.descriptionP).getObject().toString();
            String status = test.getRequiredProperty(OWLWGTester.statusP).getObject().toString();
        
            // Load up the premise documents
            Model premises = ModelFactory.createNonreifyingModel();
            for (StmtIterator premisesI = test.listProperties(OWLWGTester.premiseDocumentP); premisesI.hasNext(); ) {
                premises.add(loadTestModel(premisesI.nextStatement().getObject().toString()));
            }

            // Load up the conclusions document
            Resource conclusionsRes = (Resource) test.getRequiredProperty(OWLWGTester.conclusionDocumentP).getObject();
            Model conclusions = loadTestModel(conclusionsRes.toString());
        
            // Construct the inferred graph
            Reasoner reasoner = makeReasoner();
            InfGraph resultGraph = reasoner.bind(premises.getGraph());
        
            // Check the results against the official conclusions
            boolean correct = testConclusions(conclusions.getGraph(), resultGraph);
        
            assertTrue("Test: " + test + "\n" +  description, correct);
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