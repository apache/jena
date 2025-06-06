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

package org.apache.jena.reasoner.rulesys.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.FBRuleInfGraph;
import org.apache.jena.reasoner.rulesys.FBRuleReasoner;
import org.apache.jena.reasoner.rulesys.Functor;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.OWLFBRuleReasoner;
import org.apache.jena.reasoner.rulesys.OWLFBRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
import org.apache.jena.reasoner.rulesys.Util;
import org.apache.jena.reasoner.test.TestUtil;
import org.apache.jena.shared.ClosedException;
import org.apache.jena.shared.impl.JenaParameters;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Test suite for the hybrid forward/backward rule system.
 */
public class TestFBRules extends TestCase {

    protected static Logger logger = LoggerFactory.getLogger(TestFBRules.class);

    // Useful constants
    protected Node p = NodeFactory.createURI("p");
    protected Node q = NodeFactory.createURI("q");
    protected Node n1 = NodeFactory.createURI("n1");
    protected Node n2 = NodeFactory.createURI("n2");
    protected Node n3 = NodeFactory.createURI("n3");
    protected Node n4 = NodeFactory.createURI("n4");
    protected Node n5 = NodeFactory.createURI("n5");
    protected Node res = NodeFactory.createURI("res");
    protected Node r = NodeFactory.createURI("r");
    protected Node s = NodeFactory.createURI("s");
    protected Node t = NodeFactory.createURI("t");
    protected Node u = NodeFactory.createURI("u");
    protected Node v = NodeFactory.createURI("v");
    protected Node w = NodeFactory.createURI("w");
    protected Node x = NodeFactory.createURI("x");
    protected Node a = NodeFactory.createURI("a");
    protected Node b = NodeFactory.createURI("b");
    protected Node c = NodeFactory.createURI("c");
    protected Node d = NodeFactory.createURI("d");
    protected Node C1 = NodeFactory.createURI("C1");
    protected Node C2 = NodeFactory.createURI("C2");
    protected Node C3 = NodeFactory.createURI("C3");
    protected Node sP = RDFS.Nodes.subPropertyOf;
    protected Node sC = RDFS.Nodes.subClassOf;
    protected Node ty = RDF.Nodes.type;

    /**
     * Boilerplate for junit
     */
    public TestFBRules( String name ) {
        super( name );
    }

    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestFBRules.class );
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestFBRules( "testNumericFunctors" ));
//        return suite;
    }

    private static  Graph createGraphForTest() {
        return GraphMemFactory.createDefaultGraph();
    }

    /**
     * Override in subclasses to test other reasoners.
     */
    public Reasoner createReasoner(List<Rule> rules) {
        FBRuleReasoner reasoner = new FBRuleReasoner(rules);
        reasoner.tablePredicate(RDFS.Nodes.subClassOf);
        reasoner.tablePredicate(RDF.Nodes.type);
        reasoner.tablePredicate(p);
        return reasoner;
    }

    /**
     * Assemble a test infGraph from a rule source and starting data
     */
    public InfGraph createInfGraph(String rules, Graph data) {
        return createReasoner( Rule.parseRules(rules) ).bind(data);
    }


    /**
     * Assemble a test infGraph from a rule source and empty data
     */
    public InfGraph createInfGraph(String rules) {
        return createReasoner( Rule.parseRules(rules) ).bind(createGraphForTest());
    }

    /**
     * Check parser extension for f/b distinction.
     */
    public void testParser() {
        String rf = "(?a rdf:type ?t) -> (?t rdf:type rdfs:Class).";
        String rb = "(?t rdf:type rdfs:Class) <- (?a rdf:type ?t).";
        assertTrue( ! Rule.parseRule(rf).isBackward() );
        assertTrue(   Rule.parseRule(rb).isBackward() );
    }

    /**
     * Minimal rule tester to check basic pattern match, forward style.
     */
    public void testRuleMatcher() {
        String rules = "[r1: (?a p ?b), (?b q ?c) -> (?a, q, ?c)]" +
                       "[r2: (?a p ?b), (?b p ?c) -> (?a, p, ?c)]" +
                       "[r3: (?a p ?a), (n1 p ?c), (n1, p, ?a) -> (?a, p, ?c)]" +
                       "[r4: (n4 ?p ?a) -> (n4, ?a, ?p)]";

        InfGraph infgraph = createInfGraph(rules);
        infgraph.add(Triple.create(n1, p, n2));
        infgraph.add(Triple.create(n2, p, n3));
        infgraph.add(Triple.create(n2, q, n3));
        infgraph.add(Triple.create(n4, p, n4));

        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null),
            new Triple[] {
                Triple.create(n1, p, n2),
                Triple.create(n2, p, n3),
                Triple.create(n2, q, n3),
                Triple.create(n4, p, n4),
                Triple.create(n1, p, n3),
                Triple.create(n1, q, n3),
                Triple.create(n4, n4, p),
            });
    }

    /**
     * Test functor handling
     */
    public void testEmbeddedFunctors() {
        String rules = "(?C owl:onProperty ?P), (?C owl:allValuesFrom ?D) -> (?C rb:restriction all(?P, ?D))." +
                        "(?C rb:restriction all(eg:p, eg:D)) -> (?C rb:restriction 'allOK')." +
                       "[ -> (eg:foo eg:prop functor(eg:bar, 1)) ]" +
                       "[ (?x eg:prop functor(eg:bar, ?v)) -> (?x eg:propbar ?v) ]" +
                       "[ (?x eg:prop functor(?v, ?*)) -> (?x eg:propfunc ?v) ]" +
                       "";

        Model data = ModelFactory.createDefaultModel();
        Resource R1 = data.createResource(PrintUtil.egNS + "R1");
        Resource D = data.createResource(PrintUtil.egNS + "D");
        Property p = data.createProperty(PrintUtil.egNS, "p");
        Property propbar = data.createProperty(PrintUtil.egNS, "propbar");
        Property rbr = data.createProperty(ReasonerVocabulary.RBNamespace, "restriction");
        R1.addProperty(OWL.onProperty, p).addProperty(OWL.allValuesFrom, D);

        InfGraph infgraph = createInfGraph(rules, data.getGraph());
        Model infModel = ModelFactory.createModelForGraph(infgraph);
        Resource foo = infModel.createResource(PrintUtil.egNS + "foo");

        RDFNode fLit = infModel.getResource(R1.getURI()).getRequiredProperty(rbr).getObject();
        assertNotNull(fLit);
        assertTrue(fLit.isLiteral());
        String strflit = fLit.asLiteral().getLexicalForm();
        assertEquals("allOK", strflit);

        Literal one = (Literal)foo.getRequiredProperty(propbar).getObject();
        assertEquals(Integer.valueOf(1), one.getValue());
    }

    /**
     * The the minimal machinery for supporting builtins
     */
    public void testBuiltins() {
        String rules =  //"[testRule1: (n1 ?p ?a) -> print('rule1test', ?p, ?a)]" +
                       "[r1: (n1 p ?x), addOne(?x, ?y) -> (n1 q ?y)]" +
                       "[r2: (n1 p ?x), lessThan(?x, 3) -> (n2 q ?x)]" +
                       "[axiom1: -> (n1 p 1)]" +
                       "[axiom2: -> (n1 p 4)]" +
                       "";

        InfGraph infgraph = createInfGraph(rules);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, q, null),
            new Triple[] {
                Triple.create(n1, q, Util.makeIntNode(2)),
                Triple.create(n1, q, Util.makeIntNode(5))
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, q, null),
            new Triple[] {
                Triple.create(n2, q, Util.makeIntNode(1))
            });

    }

    /**
     * Test schmea partial binding machinery, forward subset.
     */
    public void testSchemaBinding() {
        String rules = "[testRule1: (n1 p ?a) -> (n2, p, ?a)]" +
                       "[testRule2: (n1 q ?a) -> (n2, q, ?a)]" +
                       "[testRule3: (n2 p ?a), (n2 q ?a) -> (res p ?a)]" +
                       "[testBRule4: (n3 p ?a) <- (n1, p, ?a)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        Graph schema = createGraphForTest();
        schema.add(Triple.create(n1, p, n3));
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, q, n4));
        data.add(Triple.create(n1, q, n3));

        Reasoner reasoner =  createReasoner(ruleList);
        Reasoner boundReasoner = reasoner.bindSchema(schema);
        InfGraph infgraph = boundReasoner.bind(data);

        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null),
            new Triple[] {
                Triple.create(n1, p, n3),
                Triple.create(n2, p, n3),
                Triple.create(n3, p, n3),
                Triple.create(n1, q, n4),
                Triple.create(n2, q, n4),
                Triple.create(n1, q, n3),
                Triple.create(n2, q, n3),
                Triple.create(res, p, n3)
            });
    }

    /**
     * The the "remove" builtin
     */
    public void testRemoveBuiltin() {
        String rules =
                       "[rule1: (?x p ?y), (?x q ?y) -> remove(0)]" +
                       "";

        InfGraph infgraph = createInfGraph(rules);
        infgraph.add(Triple.create(n1, p, Util.makeIntNode(1)));
        infgraph.add(Triple.create(n1, p, Util.makeIntNode(2)));
        infgraph.add(Triple.create(n1, q, Util.makeIntNode(2)));

        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                Triple.create(n1, p, Util.makeIntNode(1)),
                Triple.create(n1, q, Util.makeIntNode(2))
            });

    }

    /**
     * Test the rebind operation.
     */
    public void testRebind() {
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, n2));
        InfGraph infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                Triple.create(n1, p, n2),
                Triple.create(n1, q, n2)
            });
        Graph ndata = createGraphForTest();
        ndata.add(Triple.create(n1, p, n3));
        infgraph.rebind(ndata);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                Triple.create(n1, p, n3),
                Triple.create(n1, q, n3)
            });
    }


    /**
     * Test that reset does actually clear out all the data.
     * We use the RDFS configuration because uses both TGC, forward and backward
     * rules and so is a good check.
     */
    public void testRebind2() {
        String NS = "http://jena.hpl.hp.com/test#";
        Model base = ModelFactory.createDefaultModel();
        Resource A = base.createResource(NS + "A");
        Resource B = base.createResource(NS + "B");
        Resource I = base.createResource(NS + "i");
        A.addProperty(RDFS.subClassOf, B);
        I.addProperty(RDF.type, A);
        InfModel inf = ModelFactory.createInfModel(ReasonerRegistry.getRDFSReasoner(), base);
        assertTrue(inf.containsResource(A) && inf.containsResource(I));
        base.removeAll();
        inf.rebind();
        assertFalse(inf.containsResource(A) || inf.containsResource(I));
    }

    /**
     * Test rebindAll reconsults a changed ruleset
     */
    public void testRebindAll() {
        String NS = "http://jena.hpl.hp.com/example#";
        List<Rule> rules1 = Rule.parseRules( "(?x http://jena.hpl.hp.com/example#p ?y) -> (?x http://jena.hpl.hp.com/example#q ?y)." );
        List<Rule> rules2 = Rule.parseRules( "(?x http://jena.hpl.hp.com/example#q ?y) -> (?x http://jena.hpl.hp.com/example#r ?y)." );
        Model m = ModelFactory.createDefaultModel();
        Property p = m.createProperty(NS + "p");
        Property q = m.createProperty(NS + "q");
        Property r = m.createProperty(NS + "r");
        Resource a = m.createResource(NS + "a");
        Resource b = m.createResource(NS + "b");
        Statement s1 = m.createStatement(a, p, b);
        Statement s2 = m.createStatement(a, q, b);
        Statement s3 = m.createStatement(a, r, b);
        m.add(s1);
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules1);
        InfModel infModel = ModelFactory.createInfModel(reasoner, m);
        reasoner.addRules(rules2);
        TestUtil.assertIteratorValues(this, infModel.listStatements(a, null, (RDFNode)null),
                new Object[] {s1, s2});
        ((FBRuleInfGraph)infModel.getGraph()).rebindAll();
        TestUtil.assertIteratorValues(this, infModel.listStatements(a, null, (RDFNode)null),
                new Object[] {s1, s2, s3});
    }

    /**
     * Test the close operation.
     */
    public void testClose() {
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, n2));
        InfGraph infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                Triple.create(n1, p, n2),
                Triple.create(n1, q, n2)
            });
        infgraph.close();
        boolean foundException = false;
        try {
            infgraph.find(n1, null, null);
        } catch (ClosedException e) {
            foundException = true;
        }
        assertTrue("Close detected", foundException);
    }

    /**
     * Test example pure backchaining rules
     */
    public void testBackchain1() {
        Graph data = createGraphForTest();
        data.add(Triple.create(p, sP, q));
        data.add(Triple.create(q, sP, r));
        data.add(Triple.create(C1, sC, C2));
        data.add(Triple.create(C2, sC, C3));
        data.add(Triple.create(a, ty, C1));
        String rules =
        "[rdfs8:  (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c)]" +
        "[rdfs9:  (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y), (?a rdf:type ?x)]" +
        "[-> (rdf:type rdfs:range rdfs:Class)]" +
        "[rdfs3:  (?y rdf:type ?c) <- (?x ?p ?y), (?p rdfs:range ?c)]" +
        "[rdfs7:  (?a rdfs:subClassOf ?a) <- (?a rdf:type rdfs:Class)]";
        InfGraph infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this,
            infgraph.find(a, ty, null),
            new Object[] {
                Triple.create(a, ty, C1),
                Triple.create(a, ty, C2),
                Triple.create(a, ty, C3)
            } );
        TestUtil.assertIteratorValues(this,
            infgraph.find(C1, sC, a),
            new Object[] {
            } );
    }

    /**
     * Test complex rule head unification
     */
    public void testBackchain2() {
        Graph data = createGraphForTest();
        data.add(Triple.create(c, q, d));
        String rules =
            "[r1: (c r ?x) <- (?x p f(?x b))]" +
            "[r2: (?y p f(a ?y)) <- (c q ?y)]";
        InfGraph infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this,
              infgraph.find(c, r, null), new Object[] { } );

        data.add(Triple.create(c, q, a));
        rules =
        "[r1: (c r ?x) <- (?x p f(?x a))]" +
        "[r2: (?y p f(a ?y)) <- (c q ?y)]";
        infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this,
              infgraph.find(c, r, null),
              new Object[] {
                  Triple.create(c, r, a)
              } );

        data = createGraphForTest();
        data.add(Triple.create(a, q, a));
        data.add(Triple.create(a, q, b));
        data.add(Triple.create(a, q, c));
        data.add(Triple.create(b, q, d));
        data.add(Triple.create(b, q, b));
        rules =
          "[r1: (c r ?x) <- (?x p ?x)]" +
          "[r2: (?x p ?y) <- (a q ?x), (b q ?y)]";
        infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this,
              infgraph.find(c, r, null),
              new Object[] {
                  Triple.create(c, r, b)
              } );

        rules =
          "[r1: (c r ?x) <- (?x p ?x)]" +
          "[r2: (a p ?x) <- (a q ?x)]" ;
        infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this,
              infgraph.find(c, r, null),
              new Object[] {
                  Triple.create(c, r, a)
              } );
    }

    /**
     * Test restriction example
     */
    public void testBackchain3() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, ty, r));
        data.add(Triple.create(a, p, b));
        data.add(Triple.create(r, sC, C1));
        data.add(Triple.create(C1, ty, OWL.Restriction.asNode()));
        data.add(Triple.create(C1, OWL.onProperty.asNode(), p));
        data.add(Triple.create(C1, OWL.allValuesFrom.asNode(), c));
        String rules =
    "[rdfs9:   (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y) (?a rdf:type ?x)]" +
    "[restriction2: (?C owl:equivalentClass all(?P, ?D)) <- (?C rdf:type owl:Restriction), (?C owl:onProperty ?P), (?C owl:allValuesFrom ?D)]" +
    "[rs2: (?X rdf:type all(?P,?C)) <- (?D owl:equivalentClass all(?P,?C)), (?X rdf:type ?D)]" +
    "[rp4: (?Y rdf:type ?C) <- (?X rdf:type all(?P, ?C)), (?X ?P ?Y)]";
        InfGraph infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this,
              infgraph.find(b, ty, c), new Object[] {
                  Triple.create(b, ty, c)
              } );
    }

    /**
     * Test example hybrid rule.
     */
    public void testHybrid1() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, p, b));
        data.add(Triple.create(p, ty, s));
        String rules =
        "[r1: (?p rdf:type s) -> [r1b: (?x ?p ?y) <- (?y ?p ?x)]]";
        InfGraph infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this,
              infgraph.find(null, p, null), new Object[] {
                  Triple.create(a, p, b),
                  Triple.create(b, p, a)
              } );
    }

    /**
     * Test example hybrid rule.
     */
    public void testHybrid2() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, r, b));
        data.add(Triple.create(p, ty, s));
        String rules =
        "[a1: -> (a rdf:type t)]" +
        "[r0: (?x r ?y) -> (?x p ?y)]" +
        "[r1: (?p rdf:type s) -> [r1b: (?x ?p ?y) <- (?y ?p ?x)]]" +
        "[r2: (?p rdf:type s) -> [r2b: (?x ?p ?x) <- (?x rdf:type t)]]";
        FBRuleInfGraph infgraph = (FBRuleInfGraph) createInfGraph(rules, data);
        infgraph.setDerivationLogging(true);
        infgraph.prepare();
        assertTrue("Forward rule count", infgraph.getNRulesFired() == 3);
        TestUtil.assertIteratorValues(this,
              infgraph.find(null, p, null), new Object[] {
                  Triple.create(a, p, a),
                  Triple.create(a, p, b),
                  Triple.create(b, p, a)
              } );
        // Suppressed until LP engine implements rule counting, if ever
//        assertTrue("Backward rule count", infgraph.getNRulesFired() == 8);

        // Check derivation tracing as well
        // Suppressed until LP engine implements derivation tracing
        Iterator<Derivation> di = infgraph.getDerivation(Triple.create(b, p, a));
        assertTrue(di.hasNext());
        RuleDerivation d = (RuleDerivation)di.next();
        assertTrue(d.getRule().getName().equals("r1b"));
        TestUtil.assertIteratorValues(this, d.getMatches().iterator(), new Object[] { Triple.create(a, p, b) });
        assertTrue(! di.hasNext());
    }

    /**
     * Test example hybrid rules for rdfs.
     */
    public void testHybridRDFS() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, p, b));
        data.add(Triple.create(p, RDFS.range.asNode(), C1));
        String rules =
    "[rdfs2:  (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ]" +
    "[rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]" +
    "[rdfs5a: (?a rdfs:subPropertyOf ?b), (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c)]" +
    "[rdfs5b: (?a rdf:type rdf:Property) -> (?a rdfs:subPropertyOf ?a)]" +
    "[rdfs6:  (?p rdfs:subPropertyOf ?q) -> [ (?a ?q ?b) <- (?a ?p ?b)] ]" +
    "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]" +
    "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" +
    "[rdfs9:  (?x rdfs:subClassOf ?y) -> [ (?a rdf:type ?y) <- (?a rdf:type ?x)] ]" ;
        InfGraph infgraph = createInfGraph(rules, data);
//        ((FBRuleInfGraph)infgraph).setTraceOn(true);
        TestUtil.assertIteratorValues(this,
              infgraph.find(b, ty, null), new Object[] {
                  Triple.create(b, ty, C1)
              } );
    }

    /**
     * Test example hybrid rules for rdfs.
     */
    public void testHybridRDFS2() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, p, b));
        data.add(Triple.create(p, sP, r));
        data.add(Triple.create(r, RDFS.range.asNode(), C1));
        String rules =
    "[rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]" +
    "[rdfs6:  (?p rdfs:subPropertyOf ?q) -> [ (?a ?q ?b) <- (?a ?p ?b)] ]" ;
        InfGraph infgraph = createInfGraph(rules, data);
//        ((FBRuleInfGraph)infgraph).setTraceOn(true);
        TestUtil.assertIteratorValues(this,
              infgraph.find(b, ty, C1), new Object[] {
                  Triple.create(b, ty, C1)
              } );
    }

    /**
     * Test access to makeInstance machinery from a Brule.
     */
    public void testMakeInstance() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, ty, C1));
        String rules =
        "[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]" +
        "[r2:  (?t rdf:type C2) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]" ;
        InfGraph infgraph = createInfGraph(rules, data);

        Node valueInstance = getValue(infgraph, a, p);
        assertNotNull(valueInstance);
        Node valueInstance2 = getValue(infgraph, a, p);
        assertEquals(valueInstance, valueInstance2);
        Node valueType = getValue(infgraph, valueInstance, RDF.type.asNode());
        assertEquals(valueType, C2);
    }

    /**
     * Test access to makeInstance machinery from a Brule.
     */
    public void testMakeInstances() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, ty, C1));
        String rules =
        "[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, ?t)]" ;
        InfGraph infgraph = createInfGraph(rules, data);

        Node valueInstance = getValue(infgraph, a, p);
        assertNotNull(valueInstance);
        Node valueInstance2 = getValue(infgraph, a, p);
        assertEquals(valueInstance, valueInstance2);
    }

    /**
     * Test case for makeInstance which failed during development.
     */
    public void testMakeInstanceBug() {
        Graph data = createGraphForTest();
        data.add(Triple.create(a, ty, r));
        data.add(Triple.create(r, sC, Functor.makeFunctorNode("some", new Node[] {p, C1})));
        String rules =
        "[some1: (?C rdfs:subClassOf some(?P, ?D)) ->"
        + "[some1b: (?X ?P ?T) <- (?X rdf:type ?C), unbound(?T), noValue(?X, ?P), makeInstance(?X, ?P, ?D, ?T) ]"
        + "[some1b2: (?T rdf:type ?D) <- (?X rdf:type ?C), bound(?T), makeInstance(?X, ?P, ?D, ?T) ]"
        + "]";
        InfGraph infgraph = createInfGraph(rules, data);

        Node valueInstance = getValue(infgraph, a, p);
        assertNotNull(valueInstance);
        Node valueType = getValue(infgraph, valueInstance, ty);
        assertEquals(valueType, C1);

    }

    /**
     * Test numeric functors
     */
    public void testNumericFunctors() {
        String rules =
        "[r1: (?x p f(a, ?x)) -> (?x q f(?x)) ]" +
        "[r1: (?x p f(a, 0)) -> (?x s res) ]" +
                       "";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, Util.makeIntNode(2)) );
        data.add(Triple.create(n2, p, Functor.makeFunctorNode("f", new Node[] {
                                        a, Util.makeIntNode(0)  })));
        data.add(Triple.create(n3, p, Functor.makeFunctorNode("f", new Node[] {
               a, NodeFactory.createLiteralDT( "0", XSDDatatype.XSDnonNegativeInteger ) } )));
        InfGraph infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(null, s, null),
            new Triple[] {
                Triple.create(n2, s, res),
                Triple.create(n3, s, res),
            });
    }

    /**
     * Test the builtins themselves
     */
    public void testBuiltins2() {
        // Numeric comparisions
        Node lt = NodeFactory.createURI("lt");
        Node gt = NodeFactory.createURI("gt");
        Node le = NodeFactory.createURI("le");
        Node ge = NodeFactory.createURI("ge");
        Node eq = NodeFactory.createURI("eq");
        Node ne = NodeFactory.createURI("ne");
        String rules =
        "[r1: (?x q ?vx), (?y q ?vy), lessThan(?vx, ?vy) -> (?x lt ?y)]" +
        "[r2: (?x q ?vx), (?y q ?vy), greaterThan(?vx, ?vy) -> (?x gt ?y)]" +
        "[r3: (?x q ?vx), (?y q ?vy), le(?vx, ?vy) -> (?x le ?y)]" +
        "[r4: (?x q ?vx), (?y q ?vy), ge(?vx, ?vy) -> (?x ge ?y)]" +
        "[r5: (?x q ?vx), (?y q ?vy), notEqual(?vx, ?vy) -> (?x ne ?y)]" +
        "[r6: (?x q ?vx), (?y q ?vy), equal(?vx, ?vy) -> (?x eq ?y)]" +
                       "";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, q, Util.makeIntNode(2)) );
        data.add(Triple.create(n2, q, Util.makeIntNode(2)) );
        data.add(Triple.create(n3, q, Util.makeIntNode(3)) );
        InfGraph infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, n2),
            new Triple[] {
                Triple.create(n1, eq, n2),
                Triple.create(n1, le, n2),
                Triple.create(n1, ge, n2),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, n3),
            new Triple[] {
                Triple.create(n1, ne, n3),
                Triple.create(n1, lt, n3),
                Triple.create(n1, le, n3),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n3, null, n1),
            new Triple[] {
                Triple.create(n3, ne, n1),
                Triple.create(n3, gt, n1),
                Triple.create(n3, ge, n1),
            });

        // Floating point comparisons
        data = createGraphForTest();
        data.add(Triple.create(n1, q, Util.makeIntNode(2)) );
        data.add(Triple.create(n2, q, Util.makeDoubleNode(2.2)) );
        data.add(Triple.create(n3, q, Util.makeDoubleNode(2.3)) );
        infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, n2),
            new Triple[] {
                Triple.create(n1, ne, n2),
                Triple.create(n1, le, n2),
                Triple.create(n1, lt, n2),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, null, n3),
            new Triple[] {
                Triple.create(n2, ne, n3),
                Triple.create(n2, le, n3),
                Triple.create(n2, lt, n3),
            });

        // XSD timeDate point comparisons
        data = createGraphForTest();
        XSDDatatype dt = new XSDDatatype("dateTime");
        data.add(Triple.create(n1, q, NodeFactory.createLiteralDT("2000-03-04T20:00:00Z", XSDDatatype.XSDdateTime)));
        data.add(Triple.create(n2, q, NodeFactory.createLiteralDT("2001-03-04T20:00:00Z", XSDDatatype.XSDdateTime)));
        data.add(Triple.create(n3, q, NodeFactory.createLiteralDT("2002-03-04T20:00:00Z", XSDDatatype.XSDdateTime)));
        infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, n2),
            new Triple[] {
                Triple.create(n1, ne, n2),
                Triple.create(n1, le, n2),
                Triple.create(n1, lt, n2),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, null, n3),
            new Triple[] {
                Triple.create(n2, ne, n3),
                Triple.create(n2, le, n3),
                Triple.create(n2, lt, n3),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, null, n1),
            new Triple[] {
                Triple.create(n2, ne, n1),
                Triple.create(n2, ge, n1),
                Triple.create(n2, gt, n1),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n3, null, n2),
            new Triple[] {
                Triple.create(n3, ne, n2),
                Triple.create(n3, ge, n2),
                Triple.create(n3, gt, n2),
            });

        // Support for now(?x)
        rules = "[r1: now(?x) -> (a p ?x)]";
        infgraph = createInfGraph(rules);
        infgraph.prepare();
        Graph result = infgraph.getDeductionsGraph();
        assertEquals(1, result.size());
        Triple tr = result.find(null, null, null).next();
        Node nowN = tr.getObject();
        assertTrue(nowN.isLiteral());
        Object nowO = nowN.getLiteralValue();
        assertTrue(nowO instanceof XSDDateTime);

        // Arithmetic
        rules =
        "[r1: (?x p ?a), (?x q ?b), sum(?a, ?b, ?c) -> (?x s ?c)]" +
        "[r2: (?x p ?a), (?x q ?b), product(?a, ?b, ?c) -> (?x t ?c)]" +
        "[r3: (?x p ?a), (?x q ?b), difference(?b, ?a, ?c) -> (?x u ?c)]" +
        "[r4: (?x p ?a), (?x q ?b), quotient(?b, ?a, ?c) -> (?x v ?c)]" +
        "[r4: (?x p ?a), (?x q ?b), min(?b, ?a, ?c) -> (?x r ?c)]" +
        "[r4: (?x p ?a), (?x q ?b), max(?b, ?a, ?c) -> (?x x ?c)]" +
                       "";
        data = createGraphForTest();
        data.add(Triple.create(n1, p, Util.makeIntNode(3)) );
        data.add(Triple.create(n1, q, Util.makeIntNode(5)) );
        infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                Triple.create(n1, p, Util.makeIntNode(3)),
                Triple.create(n1, q, Util.makeIntNode(5)),
                Triple.create(n1, s, Util.makeIntNode(8)),
                Triple.create(n1, t, Util.makeIntNode(15)),
                Triple.create(n1, u, Util.makeIntNode(2)),
                Triple.create(n1, v, Util.makeIntNode(1)),
                Triple.create(n1, r, Util.makeIntNode(3)),
                Triple.create(n1, x, Util.makeIntNode(5)),
            });

        // Note type checking
        rules =
        "[r1: (?x p ?y), isLiteral(?y) -> (?x s 'literal')]" +
        "[r1: (?x p ?y), notLiteral(?y) -> (?x s 'notLiteral')]" +
        "[r1: (?x p ?y), isBNode(?y) -> (?x s 'bNode')]" +
        "[r1: (?x p ?y), notBNode(?y) -> (?x s 'notBNode')]" +
                       "";
        data = createGraphForTest();
        data.add(Triple.create(n1, p, Util.makeIntNode(3)) );
        data.add(Triple.create(n2, p, res));
        data.add(Triple.create(n3, p, NodeFactory.createBlankNode()));
        infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(n1, s, null),
            new Triple[] {
                Triple.create(n1, s, NodeFactory.createLiteralString("literal")),
                Triple.create(n1, s, NodeFactory.createLiteralString("notBNode")),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, s, null),
            new Triple[] {
                Triple.create(n2, s, NodeFactory.createLiteralString("notLiteral")),
                Triple.create(n2, s, NodeFactory.createLiteralString("notBNode")),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n3, s, null),
            new Triple[] {
                Triple.create(n3, s, NodeFactory.createLiteralString("notLiteral")),
                Triple.create(n3, s, NodeFactory.createLiteralString("bNode")),
            });

        // Data type checking
        rules =
        "[r1: (?x p ?y), isDType(?y, rdfs:Literal) -> (?x s 'isLiteral')]" +
        "[r1: (?x p ?y), isDType(?y, http://www.w3.org/2001/XMLSchema#int) -> (?x s 'isXSDInt')]" +
        "[r1: (?x p ?y), isDType(?y, http://www.w3.org/2001/XMLSchema#string) -> (?x s 'isXSDString')]" +
        "[r1: (?x p ?y), notDType(?y, rdfs:Literal) -> (?x s 'notLiteral')]" +
        "[r1: (?x p ?y), notDType(?y, http://www.w3.org/2001/XMLSchema#int) -> (?x s 'notXSDInt')]" +
        "[r1: (?x p ?y), notDType(?y, http://www.w3.org/2001/XMLSchema#string) -> (?x s 'notXSDString')]" +
                       "";
        data = createGraphForTest();
        data.add(Triple.create(n1, p, Util.makeIntNode(3)) );
        data.add(Triple.create(n2, p, NodeFactory.createLiteralString("foo")) );
        data.add(Triple.create(n3, p, NodeFactory.createLiteralDT("foo", XSDDatatype.XSDstring)) );
        data.add(Triple.create(n4, p, n4));
        data.add(Triple.create(n5, p, NodeFactory.createLiteralDT("-1", XSDDatatype.XSDnonNegativeInteger)) );
        infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(null, s, null),
            new Triple[] {
                Triple.create(n1, s, NodeFactory.createLiteralString("isLiteral")),
                Triple.create(n1, s, NodeFactory.createLiteralString("isXSDInt")),
                Triple.create(n1, s, NodeFactory.createLiteralString("notXSDString")),

                Triple.create(n2, s, NodeFactory.createLiteralString("isLiteral")),
                Triple.create(n2, s, NodeFactory.createLiteralString("notXSDInt")),
                Triple.create(n2, s, NodeFactory.createLiteralString("isXSDString")),

                Triple.create(n3, s, NodeFactory.createLiteralString("isLiteral")),
                Triple.create(n3, s, NodeFactory.createLiteralString("notXSDInt")),
                Triple.create(n3, s, NodeFactory.createLiteralString("isXSDString")),

                Triple.create(n4, s, NodeFactory.createLiteralString("notLiteral")),
                Triple.create(n4, s, NodeFactory.createLiteralString("notXSDInt")),
                Triple.create(n4, s, NodeFactory.createLiteralString("notXSDString")),

                Triple.create(n5, s, NodeFactory.createLiteralString("notLiteral")),
                Triple.create(n5, s, NodeFactory.createLiteralString("notXSDInt")),
                Triple.create(n5, s, NodeFactory.createLiteralString("notXSDString")),
            });

        // Literal counting
        rules = "[r1: (?x p ?y), countLiteralValues(?x, p, ?c) -> (?x s ?c)]";
        data = createGraphForTest();
        data.add(Triple.create(n1, p, Util.makeIntNode(2)) );
        data.add(Triple.create(n1, p, Util.makeIntNode(2)) );
        data.add(Triple.create(n1, p, Util.makeIntNode(3)) );
        data.add(Triple.create(n1, p, n2) );
        infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, s, null),
            new Triple[] {
                Triple.create(n1, s, Util.makeIntNode(2)),
            });

        // Map list operation
        rules = "[r1: (n1 p ?l) -> listMapAsSubject(?l, q, C1)]" +
                "[r2: (n1 p ?l) -> listMapAsObject ( a, q, ?l)]";
        data = createGraphForTest();
        data.add(Triple.create(n1, p, Util.makeList(new Node[]{b, c, d}, data) ));
        infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this, infgraph.find(null, q, null),
            new Triple[] {
                Triple.create(b, q, C1),
                Triple.create(c, q, C1),
                Triple.create(d, q, C1),
                Triple.create(a, q, b),
                Triple.create(a, q, c),
                Triple.create(a, q, d),
            });
    }

    /**
     * Check string manipulation builtins, new at 2.5.
     */
    public void testStringBuiltins() {
        String rules =
            "[r1: (?x p ?y) strConcat(?y, rdf:type, 'foo', ?z) -> (?x q ?z) ] \n" +
            "[r1: (?x p ?y) strConcat(?z) -> (?x q ?z) ] \n" +
            "[r2: (?x p ?y) uriConcat('http://jena.hpl.hp.com/test#', ?y, ?z) -> (?x q ?z) ]";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, NodeFactory.createLiteralString("test")) );
        InfGraph infgraph = createInfGraph(rules, data);

        TestUtil.assertIteratorValues(this, infgraph.find(null, q, null),
            new Triple[] {
            Triple.create(n1, q, NodeFactory.createLiteralString("testhttp://www.w3.org/1999/02/22-rdf-syntax-ns#typefoo")),
            Triple.create(n1, q, NodeFactory.createLiteralString("")),
            Triple.create(n1, q, NodeFactory.createURI("http://jena.hpl.hp.com/test#test")),
            });

        rules =
            "[r1: (?x p ?y) regex(?y, '(.*)\\\\s(.*) (f.*)') -> (?x q 'ok') ] \n" +
            "[r2: (?x p ?y) regex(?y, '(.*)\\\\s(.*) (f.*)', ?m1, ?m2, ?m3) -> (?x r ?m2) ] \n" +
            "";
        data = createGraphForTest();
        data.add(Triple.create(n1, p, NodeFactory.createLiteralString("foo bar foo")) );
        data.add(Triple.create(n2, p, NodeFactory.createLiteralString("foo bar baz")) );
        infgraph = createInfGraph(rules, data);
        TestUtil.assertIteratorValues(this, infgraph.find(null, q, null),
                new Triple[] {
                Triple.create(n1, q, NodeFactory.createLiteralString("ok")),
                });
        TestUtil.assertIteratorValues(this, infgraph.find(null, r, null),
                new Triple[] {
                Triple.create(n1, r, NodeFactory.createLiteralString("bar")),
                });
    }

    /**
     * Test regex handling of null groups
     */
    public void testRegexNulls() {
        String rules =
            "[r2: (?x p ?y) regex(?y, '((Boys)|(Girls))(.*)', ?m1, ?m2, ?m3, ?m4) ->  (?x q ?m2) (?x r ?m3) (?x s ?m4) ] \n" +
            "";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, NodeFactory.createLiteralString("Girls44")) );
        InfGraph infgraph = createInfGraph(rules, data);
        infgraph.prepare();
        TestUtil.assertIteratorValues(this, infgraph.getDeductionsGraph().find(null, null, null),
                new Triple[] {
            Triple.create(n1, q, NodeFactory.createLiteralString("")),
            Triple.create(n1, r, NodeFactory.createLiteralString("Girls")),
            Triple.create(n1, s, NodeFactory.createLiteralString("44")),
                });
    }

    /**
     * More extensive check of arithmetic which checks that binding to an
     * expected answer also works
     */
    public void testArithmetic() {
        doTestArithmetic("sum", 3, 5, 8);
        doTestArithmetic("difference", 5, 3, 2);
        doTestArithmetic("product", 3, 5, 15);
        doTestArithmetic("quotient", 12, 3, 4);
        doTestArithmetic("min", 3, 5, 3);
        doTestArithmetic("max", 3, 5, 5);
    }

    /**
     * Internals of testArithmetic which sets up a rule
     * and executes it with expected and illegal answers.
     */
    private void doTestArithmetic(String op, int arg1, int arg2, int expected) {
        String rules =
            "[r1: (?x p ?a), (?x q ?b), (?x r ?c) " + op + "(?a, ?b, ?c) -> (?x s ?c)]\n " +
            "[r2: (?x p ?a), (?x q ?b), (?x t ?c) " + op + "(?a, ?b, ?c) -> (?x u ?c)]";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, Util.makeIntNode(arg1)) );
        data.add(Triple.create(n1, q, Util.makeIntNode(arg2)) );
        data.add(Triple.create(n1, r, Util.makeIntNode(expected)) );
        data.add(Triple.create(n1, t, Util.makeIntNode(expected+1)) );
        InfGraph infgraph = createInfGraph(rules, data);
        assertTrue( infgraph.contains(n1, s, Util.makeIntNode(expected)));
        assertFalse( infgraph.contains(n1, u, Node.ANY) );
    }

    /**
     * Helper - returns the single object value for an s/p pair, asserts an error
     * if there is more than one.
     */
    private Node getValue(Graph g, Node s, Node p) {
        ExtendedIterator<Triple> i = g.find(s, p, null);
        assertTrue(i.hasNext());
        Node result = i.next().getObject();
        if (i.hasNext()) {
            assertTrue("multiple values not expected", false);
            i.close();
        }
        return result;
    }

    /**
     * Investigate a suspicious case in the OWL ruleset, is the backchainer
     * returning duplicate values?
     */
    public void testDuplicatesEC4() {
        boolean prior = JenaParameters.enableFilteringOfHiddenInfNodes;
        try {
            JenaParameters.enableFilteringOfHiddenInfNodes = false;
            Model premisesM = FileManager.getInternal().loadModelInternal("file:testing/wg/equivalentClass/premises004.rdf");
            Graph data = premisesM.getGraph();
            Reasoner reasoner =  new OWLFBRuleReasoner(OWLFBRuleReasonerFactory.theInstance());
            InfGraph infgraph = reasoner.bind(data);
            Node rbPrototypeProp = NodeFactory.createURI(ReasonerVocabulary.RBNamespace+"prototype");
            int count = 0;
            for (Iterator<Triple> i = infgraph.find(null, rbPrototypeProp, null); i.hasNext(); ) {
                Object t = i.next();
//                System.out.println(" - " + PrintUtil.print(t));
                count++;
            }
//            listFBGraph("direct databind case", (FBRuleInfGraph)infgraph);
            assertEquals(5, count);
            infgraph = reasoner.bindSchema(data).bind(createGraphForTest());
            count = 0;
            for (Iterator<Triple> i = infgraph.find(null, rbPrototypeProp, null); i.hasNext(); ) {
                Triple t = i.next();
//                System.out.println(" - " + PrintUtil.print(t));
                count++;
            }
//            listFBGraph("bindSchema case", (FBRuleInfGraph)infgraph);
            assertEquals(5, count);
        } finally {
            JenaParameters.enableFilteringOfHiddenInfNodes = prior;
        }
    }

    /**
     * Test skolem constant generation
     */
    public void testSkolem() {
        assertEquals( getSkolem(a, Util.makeIntNode(42)),
                      getSkolem(a, Util.makeIntNode(42)) );

        assertNotSame( getSkolem(a, Util.makeIntNode(42)),
                       getSkolem(b, Util.makeIntNode(42)) );

        assertNotSame( getSkolem(a, Util.makeIntNode(42)),
                       getSkolem(a, Util.makeIntNode(43)) );

        assertNotSame( getSkolem(a, NodeFactory.createLiteralString("foo")),
                       getSkolem(a, NodeFactory.createLiteralLang("foo", "en")) );

        assertEquals( getSkolem(NodeFactory.createLiteralString("foo")),
                getSkolem(NodeFactory.createLiteralString("foo")));

        assertNotSame( getSkolem(NodeFactory.createLiteralString("foo")),
                       getSkolem(NodeFactory.createLiteralString("bar")));
    }

    private Node getSkolem(Node x, Node y) {
        String rules =  "[r1: (?n p ?x) (?n q ?y) makeSkolem(?s ?x ?y) -> (?n s ?s)]";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, x));
        data.add(Triple.create(n1, q, y));
        InfGraph infgraph = createInfGraph(rules, data);
        return infgraph.find(n1, s, Node.ANY).next().getObject();
    }

    private Node getSkolem(Node x) {
        String rules =  "[r1: (?n p ?x)  makeSkolem(?s ?x) -> (?n s ?s)]";
        Graph data = createGraphForTest();
        data.add(Triple.create(n1, p, x));
        InfGraph infgraph = createInfGraph(rules, data);
        return infgraph.find(n1, s, Node.ANY).next().getObject();
    }

    /**
     * Check cost of creating an empty OWL closure.
     */
    public void temp() {
        Graph data = createGraphForTest();
        Graph data2 = createGraphForTest();
        Reasoner reasoner =  new OWLFBRuleReasoner(OWLFBRuleReasonerFactory.theInstance());
        FBRuleInfGraph infgraph = (FBRuleInfGraph)reasoner.bind(data);
        FBRuleInfGraph infgraph2 = (FBRuleInfGraph)reasoner.bind(data2);
        long t1 = System.currentTimeMillis();
        infgraph.prepare();
        long t2 = System.currentTimeMillis();
        System.out.println("Prepare on empty graph = " + (t2-t1) +"ms");
        t1 = System.currentTimeMillis();
        infgraph2.prepare();
        t2 = System.currentTimeMillis();
        System.out.println("Prepare on empty graph = " + (t2-t1) +"ms");
    }

    /**
     * Helper function to list a graph out to logger.info
     */
    public void listGraph(Graph g) {
        for (Iterator<Triple> i = g.find(null,null,null); i.hasNext();) {
            Triple t = i.next();
            logger.info(PrintUtil.print(t));
        }
        logger.info("  --------  ");
    }

    /**
     * Helper function to list the interesting parts of an FBInfGraph.
     */
    public void listFBGraph(String message, FBRuleInfGraph graph) {
        logger.info(message);
        logger.info("Raw graph data");
        listGraph(graph.getRawGraph());
        logger.info("Static deductions");
        listGraph(graph.getDeductionsGraph());
    }

}
