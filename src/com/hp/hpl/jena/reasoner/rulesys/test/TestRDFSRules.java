/******************************************************************
 * File:        TestRDFSRules.java
 * Created by:  Dave Reynolds
 * Created on:  08-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestRDFSRules.java,v 1.18 2003-12-08 10:48:27 andy_seaborne Exp $
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** * Test suite to test the production rule version of the RDFS implementation.
 * <p> The tests themselves have been replaced by an updated version
 * of the top level TestRDFSReasoners but this file is maintained for now since
 * the top level timing test can sometimes be useful. </p>
 *  * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a> * @version $Revision: 1.18 $ on $Date: 2003-12-08 10:48:27 $ */
public class TestRDFSRules extends TestCase {   
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    protected static Log logger = LogFactory.getLog(TestRDFSRules.class);
    
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
//        suite.addTest(new TestRDFSRules( "hiddenTestRDFSReasonerDebug" ));
//        return suite;
    }  

    /**
     * Test a single RDFS case.
     */
    public void hiddenTestRDFSReasonerDebug() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSRuleReasonerFactory.theInstance();
        
        assertTrue("RDFS hybrid-tgc reasoner test", tester.runTest("http://www.hpl.hp.com/semweb/2003/query_tester/rdfs/test11", rf, this, null));
    }

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
    public void testRDFSExptReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSRuleReasonerFactory.theInstance();
        assertTrue("RDFS experimental (hybrid+tgc) reasoner tests", tester.runTests(rf, this, null));
    }

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
            Reasoner rdfsFBRule = RDFSFBRuleReasonerFactory.theInstance().create(null);
            Reasoner rdfs1    = RDFSReasonerFactory.theInstance().create(null);
            Reasoner rdfsFinal    = RDFSRuleReasonerFactory.theInstance().create(null);
        
            doTiming(rdfs1, tbox, data, "RDFS1", 1);    
            doTiming(rdfsFBRule, tbox, data, "RDFS FB rule", 1);    
            doTiming(rdfsFinal, tbox, data, "RDFS final rule", 1);    
            doTiming(rdfs1, tbox, data, "RDFS1", 50);    
            doTiming(rdfsFBRule, tbox, data, "RDFS FB rule", 50);    
            doTiming(rdfsFinal, tbox, data, "RDFS final rule", 50);    
 
        } catch (Exception e) {
            System.out.println("Problem: " + e.toString());
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

