/******************************************************************
 * File:        TestReasoners.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestReasoners.java,v 1.22 2003-08-04 14:01:32 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.reasoner.transitiveReasoner.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.*;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.*;

/**
 * Unit tests for initial experimental reasoners
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.22 $ on $Date: 2003-08-04 14:01:32 $
 */
public class TestReasoners extends TestCase {
    
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
     * Test delete operation for Transtive reasoner.
     */
    public void testTransitiveRemove() {
        Graph data = new GraphMem();
        Node a = Node.createURI("a");
        Node b = Node.createURI("b");
        Node c = Node.createURI("c");
        Node d = Node.createURI("d");
        Node e = Node.createURI("e");
        Node closedP = RDFS.subClassOf.asNode();
        data.add( new Triple(a, RDFS.subClassOf.asNode(), b) );
        data.add( new Triple(a, RDFS.subClassOf.asNode(), c) );
        data.add( new Triple(b, RDFS.subClassOf.asNode(), d) );
        data.add( new Triple(c, RDFS.subClassOf.asNode(), d) );
        data.add( new Triple(d, RDFS.subClassOf.asNode(), e) );
        Reasoner reasoner = TransitiveReasonerFactory.theInstance().create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(a, RDFS.subClassOf.asNode(), null),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
                new Triple(a, closedP, d),
                new Triple(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(b, RDFS.subClassOf.asNode(), null),
            new Object[] {
                new Triple(b, closedP, b),
                new Triple(b, closedP, d),
                new Triple(b, closedP, e)
            });
        infgraph.delete(new Triple(b, closedP, d));
        TestUtil.assertIteratorValues(this, infgraph.find(a, RDFS.subClassOf.asNode(), null),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
                new Triple(a, closedP, d),
                new Triple(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(b, RDFS.subClassOf.asNode(), null),
            new Object[] {
                new Triple(b, closedP, b),
            });
        infgraph.delete(new Triple(a, closedP, c));
        TestUtil.assertIteratorValues(this, infgraph.find(a, RDFS.subClassOf.asNode(), null),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(b, RDFS.subClassOf.asNode(), null),
            new Object[] {
                new Triple(b, closedP, b)
            });
        TestUtil.assertIteratorValues(this, data.find(null, RDFS.subClassOf.asNode(), null),
            new Object[] {
                new Triple(a, closedP, b),
                new Triple(c, closedP, d),
                new Triple(d, closedP, e)
            });
    }
  
    /**
     * Test  metalevel add/remove subproperty operations for transitive reasoner.
     */
    public void testTransitiveMetaLevel() {
        doTestMetaLevel(TransitiveReasonerFactory.theInstance());
    }
  
    /**
     * Test  metalevel add/remove subproperty operations for rdsf reasoner.
     */
    public void testRDFSMetaLevel() {
        doTestMetaLevel(RDFSRuleReasonerFactory.theInstance());
    }
    
    /**
     * Test metalevel add/remove subproperty operations for a reasoner.
     */
    public void doTestMetaLevel(ReasonerFactory rf) {
        Graph data = new GraphMem();
        Node c1 = Node.createURI("C1");
        Node c2 = Node.createURI("C2");
        Node c3 = Node.createURI("C3");
        Node p = Node.createURI("p");
        Node q = Node.createURI("q");
        Node sC = RDFS.subClassOf.asNode();
        Node sP = RDFS.subPropertyOf.asNode();
        Node ty = RDF.type.asNode();
        data.add( new Triple(c2, sC, c3));
        data.add( new Triple(c1, p, c2));
        Reasoner reasoner = rf.create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        infgraph.add(new Triple(p, q, sC));
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        infgraph.add(new Triple(q, sP, sP));
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
                new Triple(c1, sC, c1),
                new Triple(c1, sC, c2),
                new Triple(c1, sC, c3)
            });
        infgraph.delete(new Triple(p, q, sC));
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
    }
 
    /**
     * Test rebind operation for the RDFS1 reasoner
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
     * Test the ModelFactory interface
     */
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

    /**
     * Run test on findWithPremies for Transitive reasoner.
     */
    public void testTransitiveFindWithPremises() {
        doTestFindWithPremises(TransitiveReasonerFactory.theInstance());
    }

    /**
     * Run test on findWithPremies for RDFS reasoner.
     */
    public void testRDFSFindWithPremises() {
        doTestFindWithPremises(RDFSRuleReasonerFactory.theInstance());
    }
    
    /**
     * Test a reasoner's ability to implement find with premises.
     * Assumes the reasoner can at least implement RDFS subClassOf.
     */
    public void doTestFindWithPremises(ReasonerFactory rf) {
        Node c1 = Node.createURI("C1");
        Node c2 = Node.createURI("C2");
        Node c3 = Node.createURI("C3");
        Node sC = RDFS.subClassOf.asNode();
        Graph data = new GraphMem();
        data.add( new Triple(c2, sC, c3));
        Graph premise = new GraphMem();
        premise.add( new Triple(c1, sC, c2));
        Reasoner reasoner = rf.create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null, premise),
            new Object[] {
                new Triple(c1, sC, c2),
                new Triple(c1, sC, c3),
                new Triple(c1, sC, c1)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        
    }
        
    /**
     * Test for duplicate statements in a constructed ontology.
     */
    public void testDuplicateStatements() {
        String NS = "http://swt/test#"; 
         OntModelSpec s = new OntModelSpec(ModelFactory.createMemModelMaker(), 
                                     null, null, ProfileRegistry.DAML_LANG); 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, null); 
 
         OntClass documentC = model.createClass(NS + "DOCUMENT"); 
         OntClass topicC = model.createClass(NS + "TOPIC"); 
 
         ObjectProperty hasTopicP = model.createObjectProperty(NS + "hasTopic"); 
         hasTopicP.addDomain(documentC); 
         hasTopicP.addRange(topicC); 
         ObjectProperty hasDocP = model.createObjectProperty(NS + "hasDocument"); 
         hasDocP.addDomain(topicC); 
         hasDocP.addRange(documentC); 
         hasDocP.setInverseOf(hasTopicP); 
 
         Individual fooTopic = model.createIndividual(NS + "fooTopic", topicC); 
         Individual fooDoc = model.createIndividual(NS + "fooDoc", documentC); 
 
         fooDoc.addProperty(hasTopicP, fooTopic); 
 
         TestUtil.assertIteratorLength(fooDoc.listProperties(hasTopicP), 1);
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

