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

package org.apache.jena.reasoner.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory ;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;
import org.apache.jena.util.FileManager ;
import org.apache.jena.util.PrintUtil ;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for transitive reasoner (includes some early RDFS reasoner checks)
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
        Graph data = Factory.createGraphMem();
        Node C1 = NodeFactory.createURI("C1");
        Node C2 = NodeFactory.createURI("C2");
        Node C3 = NodeFactory.createURI("C3");
        Node C4 = NodeFactory.createURI("C4");
        data.add( Triple.create(C1, RDFS.subClassOf.asNode(), C2) );
        data.add( Triple.create(C2, RDFS.subClassOf.asNode(), C3) );
        Reasoner reasoner = TransitiveReasonerFactory.theInstance().create(null);
        assertTrue(reasoner.supportsProperty(RDFS.subClassOf));
        assertTrue(! reasoner.supportsProperty(RDFS.domain));
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(C1, null, null), 
            new Object[] {
                Triple.create(C1, RDFS.subClassOf.asNode(), C1),
                Triple.create(C1, RDFS.subClassOf.asNode(), C2),
                Triple.create(C1, RDFS.subClassOf.asNode(), C3)
            } );
        Graph data2 = Factory.createGraphMem();
        data2.add( Triple.create(C1, RDFS.subClassOf.asNode(), C2) );
        data2.add( Triple.create(C2, RDFS.subClassOf.asNode(), C4) );
        infgraph.rebind(data2);
            
        // Incremental additions
        Node a = NodeFactory.createURI("a");
        Node b = NodeFactory.createURI("b");
        Node c = NodeFactory.createURI("c");
        infgraph.add(Triple.create(a, RDFS.subClassOf.asNode(), b));
        infgraph.add(Triple.create(b, RDFS.subClassOf.asNode(), c));
        TestUtil.assertIteratorValues(this, 
            infgraph.find(b, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                Triple.create(b, RDFS.subClassOf.asNode(), c),
                Triple.create(b, RDFS.subClassOf.asNode(), b)
            } );
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                Triple.create(a, RDFS.subClassOf.asNode(), a),
                Triple.create(a, RDFS.subClassOf.asNode(), b),
                Triple.create(a, RDFS.subClassOf.asNode(), c)
            } );
        Node p = NodeFactory.createURI("p");
        Node q = NodeFactory.createURI("q");
        Node r = NodeFactory.createURI("r");
        infgraph.add(Triple.create(p, RDFS.subPropertyOf.asNode(), q));
        infgraph.add(Triple.create(q, RDFS.subPropertyOf.asNode(), r));
        TestUtil.assertIteratorValues(this, 
            infgraph.find(q, RDFS.subPropertyOf.asNode(), null), 
            new Object[] {
                Triple.create(q, RDFS.subPropertyOf.asNode(), q),
                Triple.create(q, RDFS.subPropertyOf.asNode(), r)
            } );
        TestUtil.assertIteratorValues(this, 
            infgraph.find(p, RDFS.subPropertyOf.asNode(), null), 
            new Object[] {
                Triple.create(p, RDFS.subPropertyOf.asNode(), p),
                Triple.create(p, RDFS.subPropertyOf.asNode(), q),
                Triple.create(p, RDFS.subPropertyOf.asNode(), r)
            } );
    }
    
    /**
     * Test delete operation for Transtive reasoner.
     */
    public void testTransitiveRemove() {
        Graph data = Factory.createGraphMem();
        Node a = NodeFactory.createURI("a");
        Node b = NodeFactory.createURI("b");
        Node c = NodeFactory.createURI("c");
        Node d = NodeFactory.createURI("d");
        Node e = NodeFactory.createURI("e");
        Node closedP = RDFS.subClassOf.asNode();
        data.add( Triple.create(a, RDFS.subClassOf.asNode(), b) );
        data.add( Triple.create(a, RDFS.subClassOf.asNode(), c) );
        data.add( Triple.create(b, RDFS.subClassOf.asNode(), d) );
        data.add( Triple.create(c, RDFS.subClassOf.asNode(), d) );
        data.add( Triple.create(d, RDFS.subClassOf.asNode(), e) );
        Reasoner reasoner = TransitiveReasonerFactory.theInstance().create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(a, RDFS.subClassOf.asNode(), null),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
                Triple.create(a, closedP, d),
                Triple.create(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(b, RDFS.subClassOf.asNode(), null),
            new Object[] {
                Triple.create(b, closedP, b),
                Triple.create(b, closedP, d),
                Triple.create(b, closedP, e)
            });
        infgraph.delete(Triple.create(b, closedP, d));
        TestUtil.assertIteratorValues(this, infgraph.find(a, RDFS.subClassOf.asNode(), null),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
                Triple.create(a, closedP, d),
                Triple.create(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(b, RDFS.subClassOf.asNode(), null),
            new Object[] {
                Triple.create(b, closedP, b),
            });
        infgraph.delete(Triple.create(a, closedP, c));
        TestUtil.assertIteratorValues(this, infgraph.find(a, RDFS.subClassOf.asNode(), null),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(b, RDFS.subClassOf.asNode(), null),
            new Object[] {
                Triple.create(b, closedP, b)
            });
        TestUtil.assertIteratorValues(this, data.find(null, RDFS.subClassOf.asNode(), null),
            new Object[] {
                Triple.create(a, closedP, b),
                Triple.create(c, closedP, d),
                Triple.create(d, closedP, e)
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
        Graph data = Factory.createGraphMem();
        Node c1 = NodeFactory.createURI("C1");
        Node c2 = NodeFactory.createURI("C2");
        Node c3 = NodeFactory.createURI("C3");
        Node p = NodeFactory.createURI("p");
        Node q = NodeFactory.createURI("q");
        Node sC = RDFS.subClassOf.asNode();
        Node sP = RDFS.subPropertyOf.asNode();
        data.add( Triple.create(c2, sC, c3));
        data.add( Triple.create(c1, p, c2));
        Reasoner reasoner = rf.create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        infgraph.add(Triple.create(p, q, sC));
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        infgraph.add(Triple.create(q, sP, sP));
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
                Triple.create(c1, sC, c1),
                Triple.create(c1, sC, c2),
                Triple.create(c1, sC, c3)
            });
        infgraph.delete(Triple.create(p, q, sC));
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
    }
    
    /**
     * Check a complex graph's transitive reduction. 
     */
    public void testTransitiveReduction() {
        Model test = FileManager.getInternal().loadModelInternal("testing/reasoners/bugs/subpropertyModel.n3");
        Property dp = test.getProperty(TransitiveReasoner.directSubPropertyOf.getURI());
        doTestTransitiveReduction(test, dp);
    }
    
    /**
     * Test that a transitive reduction is complete.
     * Assumes test graph has no cycles (other than the trivial
     * identity ones). 
     */
    public void doTestTransitiveReduction(Model model, Property dp) {
        InfModel im = ModelFactory.createInfModel(ReasonerRegistry.getTransitiveReasoner(), model);
        
        for (ResIterator i = im.listSubjects(); i.hasNext();) {
            Resource base = i.nextResource();
            
            List<RDFNode> directLinks = new ArrayList<>();
            for (NodeIterator j = im.listObjectsOfProperty(base, dp); j.hasNext(); ) {
                directLinks.add(j.next());
            }

            for (int n = 0; n < directLinks.size(); n++) {
                Resource d1 = (Resource)directLinks.get(n);
                for (int m = n+1; m < directLinks.size(); m++) {
                    Resource d2 = (Resource)directLinks.get(m);
                    
                    if (im.contains(d1, dp, d2) && ! base.equals(d1) && !base.equals(d2)) {
                        assertTrue("Triangle discovered in transitive reduction", false);
                    }
                }
            }
        }
    }
    
    /**
     * The reasoner contract for bind(data) is not quite precise. It allows for
     * reasoners which have state so that reusing the same reasoner on a second data
     * model might lead to interference. This in fact used to happen with the transitive
     * reasoner. This is a test to check the top level symptoms of this which can be
     * solved just be not reusing reasoners.
     * @todo this test might be better moved to OntModel tests somewhere
     */
    public void testTransitiveSpecReuse() {
        OntModel om1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF);
        Resource c1 = om1.createResource(PrintUtil.egNS + "Class1");
        Resource c2 = om1.createResource(PrintUtil.egNS + "Class2");
        Resource c3 = om1.createResource(PrintUtil.egNS + "Class3");
        om1.add(c1, RDFS.subClassOf, c2);
        om1.add(c2, RDFS.subClassOf, c3);
        om1.prepare();
        assertFalse(om1.isEmpty());
        OntModel om2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF);
        StmtIterator si = om2.listStatements();
        boolean ok = ! si.hasNext();
        si.close();
        assertTrue("Transitive reasoner state leak", ok);
    }
    
    /**
     * The reasoner contract for bind(data) is not quite precise. It allows for
     * reasoners which have state so that reusing the same reasoner on a second data
     * model might lead to interference. This in fact used to happen with the transitive
     * reasoner. This is a test to check that the transitive reasoner state reuse has been fixed at source.
     */
    public void testTransitiveBindReuse() {
        Reasoner  r = ReasonerRegistry.getTransitiveReasoner();
        InfModel om1 = ModelFactory.createInfModel(r, ModelFactory.createDefaultModel());
        Resource c1 = om1.createResource(PrintUtil.egNS + "Class1");
        Resource c2 = om1.createResource(PrintUtil.egNS + "Class2");
        Resource c3 = om1.createResource(PrintUtil.egNS + "Class3");
        om1.add(c1, RDFS.subClassOf, c2);
        om1.add(c2, RDFS.subClassOf, c3);
        om1.prepare();
        assertFalse(om1.isEmpty());
        InfModel om2 = ModelFactory.createInfModel(r, ModelFactory.createDefaultModel());
        StmtIterator si = om2.listStatements();
        boolean ok = ! si.hasNext();
        si.close();
        assertTrue("Transitive reasoner state leak", ok);
    }
    
    /**
     * Test that two transitive engines are independent.
     * See JENA-1260
     */
    public void testTransitiveEngineSeparation() throws InterruptedException {
        String NS = "http://example.com/test#";

        Property sp = ResourceFactory.createProperty(NS, "sp");
        Property  p = ResourceFactory.createProperty(NS, "p");
        Property  s = ResourceFactory.createProperty(NS, "s");
        Resource  q = ResourceFactory.createProperty(NS, "q");
        Reasoner reasoner = ReasonerRegistry.getTransitiveReasoner();
        
        InfModel simple = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        simple.add(s, sp, p);
        assertFalse( simple.contains(s, RDFS.subPropertyOf, p) );
        
        InfModel withSP = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        withSP.add(sp, RDFS.subPropertyOf, RDFS.subPropertyOf);
        withSP.add(s, sp, p);
        assertTrue( withSP.contains(s, RDFS.subPropertyOf, p) );

        simple.add(q, sp, p);
        assertFalse( simple.contains(q, RDFS.subPropertyOf, p) );
    }
        
    /**
     * Test rebind operation for the RDFS reasoner
     */
    public void testRDFSRebind() {
        Graph data = Factory.createGraphMem();
        Node C1 = NodeFactory.createURI("C1");
        Node C2 = NodeFactory.createURI("C2");
        Node C3 = NodeFactory.createURI("C3");
        Node C4 = NodeFactory.createURI("C4");
        data.add( Triple.create(C1, RDFS.subClassOf.asNode(), C2) );
        data.add( Triple.create(C2, RDFS.subClassOf.asNode(), C3) );
        Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(C1, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                Triple.create(C1, RDFS.subClassOf.asNode(), C1),
                Triple.create(C1, RDFS.subClassOf.asNode(), C2),
                Triple.create(C1, RDFS.subClassOf.asNode(), C3)
            } );
        Graph data2 = Factory.createGraphMem();
        data2.add( Triple.create(C1, RDFS.subClassOf.asNode(), C2) );
        data2.add( Triple.create(C2, RDFS.subClassOf.asNode(), C4) );
        infgraph.rebind(data2);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(C1, RDFS.subClassOf.asNode(), null), 
            new Object[] {
                Triple.create(C1, RDFS.subClassOf.asNode(), C1),
                Triple.create(C1, RDFS.subClassOf.asNode(), C2),
                Triple.create(C1, RDFS.subClassOf.asNode(), C4)
            } );
    }
 
    /**
     * Test remove operations on an RDFS reasoner instance.
     * This is an example to test that rebing is invoked correctly rather
     * than an RDFS-specific test.
     */
    public void testRDFSRemove() {
        InfModel m = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());
        String NS = PrintUtil.egNS;
        Property p = m.createProperty(NS, "p");
        Resource D = m.createResource(NS + "D");
        Resource i = m.createResource(NS + "i");
        Resource c = m.createResource(NS + "c");
        Resource d = m.createResource(NS + "d");
        p.addProperty(RDFS.domain, D);
        i.addProperty(p, c);
        i.addProperty(p, d);
        TestUtil.assertIteratorValues(this, i.listProperties(), new Object[] {
                m.createStatement(i, p, c),
                m.createStatement(i, p, d),
                m.createStatement(i, RDF.type, D),
                m.createStatement(i, RDF.type, RDFS.Resource),
        });
        i.removeAll(p);
        TestUtil.assertIteratorValues(this, i.listProperties(), new Object[] {
        });
    }
    
    /**
     * Cycle bug in transitive reasoner
     */
    public void testTransitiveCycleBug() {
        Model m = FileManager.getInternal().loadModelInternal( "file:testing/reasoners/bugs/unbroken.n3" );
        OntModel om = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM_TRANS_INF, m );
        OntClass rootClass = om.getOntClass( RDFS.Resource.getURI() );
        Resource c = m.getResource("c");
        Set<OntClass> direct = rootClass.listSubClasses( true ).toSet();
        assertFalse( direct.contains( c ) );
        
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
            data.createStatement(b, RDF.type, RDFS.Resource ),
            data.createStatement(b, RDF.type, C )
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
        Node c1 = NodeFactory.createURI("C1");
        Node c2 = NodeFactory.createURI("C2");
        Node c3 = NodeFactory.createURI("C3");
        Node sC = RDFS.subClassOf.asNode();
        Graph data = Factory.createGraphMem();
        data.add( Triple.create(c2, sC, c3));
        Graph premise = Factory.createGraphMem();
        premise.add( Triple.create(c1, sC, c2));
        Reasoner reasoner = rf.create(null);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null, premise),
            new Object[] {
                Triple.create(c1, sC, c2),
                Triple.create(c1, sC, c3),
                Triple.create(c1, sC, c1)
            });
        TestUtil.assertIteratorValues(this, infgraph.find(c1, sC, null),
            new Object[] {
            });
        
    }
}
