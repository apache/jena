/******************************************************************
 * File:        TestReasoners.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestReasoners.java,v 1.19 2003-06-19 12:58:00 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.reasoner.transitiveReasoner.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;

/**
 * Unit tests for initial experimental reasoners
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.19 $ on $Date: 2003-06-19 12:58:00 $
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

