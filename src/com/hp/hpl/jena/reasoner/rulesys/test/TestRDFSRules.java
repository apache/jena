/******************************************************************
 * File:        TestRDFSRules.java
 * Created by:  Dave Reynolds
 * Created on:  08-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestRDFSRules.java,v 1.13 2003-06-16 17:01:57 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.test.*;
import com.hp.hpl.jena.util.ModelLoader;
// import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;

/** * Test suite to test the production rule version of the RDFS implementation.
 *  * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a> * @version $Revision: 1.13 $ on $Date: 2003-06-16 17:01:57 $ */
public class TestRDFSRules extends TestCase {   
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(TestRDFSRules.class);
    
    /**
     * Boilerplate for junit
     */ 
    public TestRDFSRules( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestRDFSRules.class);
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestRDFSRules( "testRDFSExptReasoner" ));
//        return suite;
    }  

    /**
     * Test the basic functioning of the forward RDFS rule reasoner
     */
    public void testRDFSReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect.rdf");
        ReasonerFactory rf = RDFSRuleReasonerFactory.theInstance();
        assertTrue("RDFS forward reasoner tests", tester.runTests(rf, this, null));
    }

//    /**
//     * Test a single RDFS case.
//     */
//    public void testRDFSReasonerDebug() throws IOException {
//        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
//        ReasonerFactory rf = RDFSFBRuleReasonerFactory.theInstance();
//        
//        assertTrue("RDFS forward reasoner test", tester.runTest("http://www.hpl.hp.com/semweb/2003/query_tester/rdfs/test6", rf, this, null));
//    }

    /**
     * Test the basic functioning of the hybrid RDFS rule reasoner
     */
    public void testRDFSFBReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSFBRuleReasonerFactory.theInstance();
        assertTrue("RDFS hybrid reasoner tests", tester.runTests(rf, this, null));
    }

    /**
     * Test the basic functioning of the hybrid RDFS rule reasoner with TGC cache
     */
//    public void testRDFSExptReasoner() throws IOException {
//        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
//        ReasonerFactory rf = RDFSExptRuleReasonerFactory.theInstance();
//        assertTrue("RDFS experimental (hybrid+tgc) reasoner tests", tester.runTests(rf, this, null));
//    }

    /**
     * Test the capabilities description.
     */
    public void testRDFSDescription() {
        ReasonerFactory rf = RDFSFBRuleReasonerFactory.theInstance();
        Reasoner r = rf.create(null);
        assertTrue(r.supportsProperty(RDFS.subClassOf));        
        assertTrue(r.supportsProperty(RDFS.domain));        
        assertTrue( ! r.supportsProperty(OWL.allValuesFrom));        
    }
    
    /**
     * Time a trial list of results from an inf graph.
     */
    private static void doTiming(Reasoner r, Model tbox, Model data, String name, int loop) {
        Resource C1 = ResourceFactory.createResource("http://www.hpl.hp.com/semweb/2003/eg#C1");
        Resource C2 = ResourceFactory.createResource("http://www.hpl.hp.com/semweb/2003/eg#C2");
        
        long t1 = System.currentTimeMillis();
        int count = 0;
        for (int lp = 0; lp < loop; lp++) {
            Model m = ModelFactory.createModelForGraph(r.bindSchema(tbox.getGraph()).bind(data.getGraph()));
            count = 0;
            for (Iterator i = m.listStatements(null, RDF.type, C1); i.hasNext(); i.next()) count++;
        }
        long t2 = System.currentTimeMillis();
        long time10 = (t2-t1)*10/loop;
        long time = time10/10;
        long timeFraction = time10 - (time*10);
        System.out.println(name + ": " + count +" results in " + time + "." + timeFraction +"ms");
//        t1 = System.currentTimeMillis();
//        for (int j = 0; j < 10; j++) {
//            count = 0;
//            for (Iterator i = m.listStatements(null, RDF.type, C1); i.hasNext(); i.next()) count++;
//        }
//        t2 = System.currentTimeMillis();
//        System.out.println(name + ": " + count + " results in " + (t2-t1)/10 +"ms");
    }
    
    /**
     * Simple timing test used to just a broad feel for how performance of the
     * pure FPS rules compares with the hand-crafted version.
     * The test ontology and data is very small. The test query is designed to
     * require an interesting fraction of the inferences to be made but not all of them.
     * The bigger the query the more advantage the FPS (which eagerly computes everything)
     * would have over the normal approach.
     */
    public static void main(String[] args) {
        try {
            Model tbox = ModelLoader.loadModel("testing/reasoners/rdfs/timing-tbox.rdf");
            Model data = ModelLoader.loadModel("testing/reasoners/rdfs/timing-data.rdf");
            Reasoner rdfsRule = RDFSRuleReasonerFactory.theInstance().create(null);
            Reasoner rdfsFBRule = RDFSFBRuleReasonerFactory.theInstance().create(null);
            Reasoner rdfs1    = RDFSReasonerFactory.theInstance().create(null);
        
            doTiming(rdfs1, tbox, data, "RDFS1", 1);    
            doTiming(rdfsRule, tbox, data, "RDFS F rule", 1);    
            doTiming(rdfsFBRule, tbox, data, "RDFS FB rule", 1);    
            doTiming(rdfs1, tbox, data, "RDFS1", 50);    
            doTiming(rdfsRule, tbox, data, "RDFS F rule", 10);    
            doTiming(rdfsFBRule, tbox, data, "RDFS FB rule", 50);    
 
        } catch (Exception e) {
            System.out.println("Problem: " + e.toString());
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

