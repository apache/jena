/******************************************************************
 * File:        TestReasoners.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestReasoners.java,v 1.17 2003-06-16 17:01:57 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.reasoner.transitiveReasoner.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Unit tests for initial experimental reasoners
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.17 $ on $Date: 2003-06-16 17:01:57 $
 */
public class TestReasoners extends TestCase {
    
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(TestReasoners.class);
    
    /**
     * Boilerplate for junit
     */ 
    public TestReasoners( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestReasoners.class);
    }  

    /**
     * Test the basic functioning of a Transitive closure cache 
     */
    public void testTransitiveReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("transitive/manifest.rdf");
        ReasonerFactory rf = TransitiveReasonerFactory.theInstance();
        assertTrue("transitive reasoner tests", tester.runTests(rf, this, null));
    }

    /**
     * Test rebind operation for the transitive reasoner
     */
    public void testTransitiveRebind() {
        Graph data = new GraphMem();
        Node C1 = Node.createURI("C1");
        Node C2 = Node.createURI("C2");
        Node C3 = Node.createURI("C3");
        Node C4 = Node.createURI("C4");
        data.add( new Triple(C1, RDFS.subClassOf.asNode(), C2) );
        data.add( new Triple(C2, RDFS.subClassOf.asNode(), C3) );
        Reasoner reasoner = TransitiveReasonerFactory.theInstance().create(null);
        assertTrue(reasoner.supportsProperty(RDFS.subClassOf));
        assertTrue(! reasoner.supportsProperty(RDFS.domain));
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(C1, null, null), 
            new Object[] {
                new Triple(C1, RDFS.subClassOf.asNode(), C1),
                new Triple(C1, RDFS.subClassOf.asNode(), C2),
                new Triple(C1, RDFS.subClassOf.asNode(), C3)
            } );
        Graph data2 = new GraphMem();
        data2.add( new Triple(C1, RDFS.subClassOf.asNode(), C2) );
        data2.add( new Triple(C2, RDFS.subClassOf.asNode(), C4) );
        infgraph.rebind(data2);
            
        // Incremental additions
        Node a = Node.createURI("a");
        Node b = Node.createURI("b");
        Node c = Node.createURI("c");
        infgraph.add(new Triple(a, RDFS.subClassOf.asNode(), b));
        infgraph.add(new Triple(b, RDFS.subClassOf.asNode(), c));
        TestUtil.assertIteratorValues(this, 
            infgraph.find(b, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                new Triple(b, RDFS.subClassOf.asNode(), c),
                new Triple(b, RDFS.subClassOf.asNode(), b)
            } );
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                new Triple(a, RDFS.subClassOf.asNode(), a),
                new Triple(a, RDFS.subClassOf.asNode(), b),
                new Triple(a, RDFS.subClassOf.asNode(), c)
            } );
        Node p = Node.createURI("p");
        Node q = Node.createURI("q");
        Node r = Node.createURI("r");
        infgraph.add(new Triple(p, RDFS.subPropertyOf.asNode(), q));
        infgraph.add(new Triple(q, RDFS.subPropertyOf.asNode(), r));
        TestUtil.assertIteratorValues(this, 
            infgraph.find(q, RDFS.subPropertyOf.asNode(), null), 
            new Object[] {
                new Triple(q, RDFS.subPropertyOf.asNode(), q),
                new Triple(q, RDFS.subPropertyOf.asNode(), r)
            } );
        TestUtil.assertIteratorValues(this, 
            infgraph.find(p, RDFS.subPropertyOf.asNode(), null), 
            new Object[] {
                new Triple(p, RDFS.subPropertyOf.asNode(), p),
                new Triple(p, RDFS.subPropertyOf.asNode(), q),
                new Triple(p, RDFS.subPropertyOf.asNode(), r)
            } );
    }
    
    /**
     * Test the basic functioning of an RDFS reasoner
     */
    // /*
    public void testRDFSReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest.rdf");
        ReasonerFactory rf = RDFSReasonerFactory.theInstance();
        assertTrue("RDFS reasoner tests", tester.runTests(rf, this, null));
        // Test effect of switching off property scan - should break container property test case
        Model configuration = new ModelMem();
        configuration.createResource(RDFSReasonerFactory.URI)
                     .addProperty(RDFSReasonerFactory.scanProperties, "false");
        assertTrue("RDFS reasoner tests", 
                    !tester.runTest(NAMESPACE + "rdfs/test17", rf, null, configuration));
        
        // Check capabilities description
        Reasoner r = rf.create(null);
        assertTrue(r.supportsProperty(RDFS.subClassOf));
        assertTrue(r.supportsProperty(RDFS.domain));
        assertTrue(r.supportsProperty(RDFS.range));
    }
    // */

    /**
     * Test the simple datatype range validation code.
     */
    public void testRDFSDTRange() throws IOException {
        assertTrue( ! doTestRDFSDTRange("dttest1.nt"));
        assertTrue( ! doTestRDFSDTRange("dttest2.nt"));
        assertTrue( doTestRDFSDTRange("dttest3.nt"));
    }

    /**
     * Test rebind operation for the transitive reasoner
     */
    public void testRDFSRebind() {
        Graph data = new GraphMem();
        Node C1 = Node.createURI("C1");
        Node C2 = Node.createURI("C2");
        Node C3 = Node.createURI("C3");
        Node C4 = Node.createURI("C4");
        data.add( new Triple(C1, RDFS.subClassOf.asNode(), C2) );
        data.add( new Triple(C2, RDFS.subClassOf.asNode(), C3) );
        Reasoner reasoner = RDFSReasonerFactory.theInstance().create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(C1, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                new Triple(C1, RDFS.subClassOf.asNode(), C1),
                new Triple(C1, RDFS.subClassOf.asNode(), C2),
                new Triple(C1, RDFS.subClassOf.asNode(), C3)
            } );
        Graph data2 = new GraphMem();
        data2.add( new Triple(C1, RDFS.subClassOf.asNode(), C2) );
        data2.add( new Triple(C2, RDFS.subClassOf.asNode(), C4) );
        infgraph.rebind(data2);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(C1, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                new Triple(C1, RDFS.subClassOf.asNode(), C1),
                new Triple(C1, RDFS.subClassOf.asNode(), C2),
                new Triple(C1, RDFS.subClassOf.asNode(), C4)
            } );
    }

    /**
     * Helper for dt range testing - loads a file, validates it using RDFS/DT
     * and returns error status of the result
     */
    private boolean doTestRDFSDTRange(String file) throws IOException {
        Model m = WGReasonerTester.loadFile("../reasoners/rdfs/" + file);
        ReasonerFactory rf = RDFSReasonerFactory.theInstance();
        InfGraph g = rf.create(null).bind(m.getGraph());
        ValidityReport report = g.validate();
        if (!report.isValid()) {
            logger.debug("Validation error report:");
            for (Iterator i = report.getReports(); i.hasNext(); ) {
                logger.debug(i.next().toString());
            }
        }
        return report.isValid();
    }
        
    /**
     * Run the relevant working group tests
     */
    // /*
    public void testWGRDFStests() throws IOException {
        WGReasonerTester tester = new WGReasonerTester("Manifest.rdf");
        ReasonerFactory rf = RDFSReasonerFactory.theInstance();

        /*
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#semantic-equivalence-within-type-2", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#semantic-equivalence-between-datatypes", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#semantic-equivalence-within-type-1", rf, this, null);
        tester.runTest(tester.BASE_URI + "rdfs-domain-and-range/Manifest.rdf#conjunction-test", rf, this, null);
        tester.runTest(tester.BASE_URI + "rdfs-subPropertyOf-semantics/Manifest.rdf#test001", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#non-well-formed-literal-1", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#test010", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#test008", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#language-ignored-for-numeric-types-3", rf, this, null);
        tester.runTest(tester.BASE_URI + "rdfs-no-cycles-in-subPropertyOf/Manifest.rdf#test001", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#language-ignored-for-numeric-types-2", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#language-ignored-for-numeric-types-1", rf, this, null);
        tester.runTest(tester.BASE_URI + "rdfs-no-cycles-in-subClassOf/Manifest.rdf#test001", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#range-clash", rf, this, null);
        tester.runTest(tester.BASE_URI + "rdfs-domain-and-range/Manifest.rdf#intensionality-range", rf, this, null);
        tester.runTest(tester.BASE_URI + "statement-entailment/Manifest.rdf#test004", rf, this, null);
        tester.runTest(tester.BASE_URI + "rdfs-domain-and-range/Manifest.rdf#intensionality-domain", rf, this, null);
        tester.runTest(tester.BASE_URI + "statement-entailment/Manifest.rdf#test003", rf, this, null);
        tester.runTest(tester.BASE_URI + "statement-entailment/Manifest.rdf#test002", rf, this, null);
        tester.runTest(tester.BASE_URI + "statement-entailment/Manifest.rdf#test001", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#non-well-formed-literal-2", rf, this, null);
        tester.runTest(tester.BASE_URI + "rdfs-container-membership-superProperty/Manifest.rdf#test001", rf, this, null);
        tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#test009", rf, this, null);
        */

        // Suppressed until we figure how to turn off datatype entailments and why we should want to
        //tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#language-important-for-non-dt-entailment-2", rf, this, null);
        //tester.runTest(tester.BASE_URI + "datatypes/Manifest.rdf#language-important-for-non-dt-entailment-1", rf, this, null);
        
        // Run all test found
        tester.runTests(rf, this, null);
    }
    // */
    
    /**
     * Current sub-cases under debug
     */
    /*
    public void testRDFSCase() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest.rdf");
        ReasonerFactory rf = RDFSReasonerFactory.theInstance();
        assertTrue("RDFS reasoner tests", 
                    tester.runTest(NAMESPACE + "rdfs/test17", rf, this, null));
    }
    */

    /**
     * Test the ModelFactory interface
     */
    // /*
    public void testModelFactoryRDFS() {
        Model data = ModelFactory.createDefaultModel();
        Property p = data.createProperty("urn:x-hp:ex/p");
        Resource a = data.createResource("urn:x-hp:ex/a");
        Resource b = data.createResource("urn:x-hp:ex/b");
        Resource C = data.createResource("urn:x-hp:ex/c");
        data.add(p, RDFS.range, C)
            .add(a, p, b);
        Model result = ModelFactory.createRDFSModel(data);
        StmtIterator i = result.listStatements( b, RDF.type, (RDFNode)null );
        TestUtil.assertIteratorValues(this, i, new Object[] {
            new StatementImpl(b, RDF.type, RDFS.Resource),
            new StatementImpl(b, RDF.type, C)
        });
        
    }
    // */
        
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

