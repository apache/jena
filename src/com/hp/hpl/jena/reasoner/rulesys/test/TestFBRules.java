/******************************************************************
 * File:        TestFBRules.java
 * Created by:  Dave Reynolds
 * Created on:  29-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestFBRules.java,v 1.27 2003-09-12 15:15:39 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ClosedException;
import com.hp.hpl.jena.util.ModelLoader;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Test suite for the hybrid forward/backward rule system.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.27 $ on $Date: 2003-09-12 15:15:39 $
 */
public class TestFBRules extends TestCase {
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(TestFBRules.class);
    
    // Useful constants
    protected Node p = Node.createURI("p");
    protected Node q = Node.createURI("q");
    protected Node n1 = Node.createURI("n1");
    protected Node n2 = Node.createURI("n2");
    protected Node n3 = Node.createURI("n3");
    protected Node n4 = Node.createURI("n4");
    protected Node res = Node.createURI("res");
    protected Node r = Node.createURI("r");
    protected Node s = Node.createURI("s");
    protected Node t = Node.createURI("t");
    protected Node a = Node.createURI("a");
    protected Node b = Node.createURI("b");
    protected Node c = Node.createURI("c");
    protected Node d = Node.createURI("d");
    protected Node C1 = Node.createURI("C1");
    protected Node C2 = Node.createURI("C2");
    protected Node C3 = Node.createURI("C3");
    protected Node sP = RDFS.subPropertyOf.getNode();
    protected Node sC = RDFS.subClassOf.getNode();
    protected Node ty = RDF.type.getNode();
     
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

    /**
     * Override in subclasses to test other reasoners.
     */
    public Reasoner createReasoner(List rules) {
        FBRuleReasoner reasoner = new FBRuleReasoner(rules); 
        reasoner.tablePredicate(RDFS.Nodes.subClassOf);
        reasoner.tablePredicate(RDF.Nodes.type);
        reasoner.tablePredicate(p);
        return reasoner;
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
        List ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = createReasoner(ruleList).bind(new GraphMem());
        infgraph.add(new Triple(n1, p, n2));
        infgraph.add(new Triple(n2, p, n3));
        infgraph.add(new Triple(n2, q, n3));
        infgraph.add(new Triple(n4, p, n4));
        
        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null),
            new Triple[] {
                new Triple(n1, p, n2),
                new Triple(n2, p, n3),
                new Triple(n2, q, n3),
                new Triple(n4, p, n4),
                new Triple(n1, p, n3),
                new Triple(n1, q, n3),
                new Triple(n4, n4, p),
            });
    }
    
    /**
     * Test functor handling
     */
    public void testEmbeddedFunctors() {
        String rules = "(?C rdf:type owl:Restriction), (?C owl:onProperty ?P), (?C owl:allValuesFrom ?D) -> (?C rb:restriction all(?P, ?D))." +
                        "(?C rb:restriction all(eg:p, eg:D)) -> (?C rb:restriction 'allOK')." +
                       "[ -> (eg:foo eg:prop functor(eg:bar, 1)) ]" +
                       "[ (?x eg:prop functor(eg:bar, ?v)) -> (?x eg:propbar ?v) ]" +
                       "[ (?x eg:prop functor(?v, ?*)) -> (?x eg:propfunc ?v) ]" +
                       "";
        List ruleList = Rule.parseRules(rules);
        
        Model data = ModelFactory.createDefaultModel();
        Resource R1 = data.createResource(PrintUtil.egNS + "R1");
        Resource D = data.createResource(PrintUtil.egNS + "D");
        Property p = data.createProperty(PrintUtil.egNS, "p");
        Property prop = data.createProperty(PrintUtil.egNS, "prop");
        Property propbar = data.createProperty(PrintUtil.egNS, "propbar");
        Property propfunc = data.createProperty(PrintUtil.egNS, "propfunc");
        Property rbr = data.createProperty(ReasonerVocabulary.RBNamespace, "restriction");
        R1.addProperty(RDF.type, OWL.Restriction)
          .addProperty(OWL.onProperty, p)
          .addProperty(OWL.allValuesFrom, D);
        
        Reasoner reasoner =  createReasoner(ruleList);
        InfGraph infgraph = reasoner.bind(data.getGraph());
        Model infModel = ModelFactory.createModelForGraph(infgraph);
        Resource foo = infModel.createResource(PrintUtil.egNS + "foo");
        Resource bar = infModel.createResource(PrintUtil.egNS + "bar");
        
        RDFNode flit = infModel.getResource(R1.getURI()).getRequiredProperty(rbr).getObject();
        assertNotNull(flit);
        assertEquals(flit.toString(), "allOK");
//        assertTrue(flit instanceof Literal);
//        Functor func = (Functor)((Literal)flit).getValue();
//        assertEquals("all", func.getName());
//        assertEquals(p.getNode(), func.getArgs()[0]);
//        assertEquals(D.getNode(), func.getArgs()[1]);
        
        Literal one = (Literal)foo.getRequiredProperty(propbar).getObject();
        assertEquals(new Integer(1), one.getValue());
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
        List ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = createReasoner(ruleList).bind(new GraphMem());
        TestUtil.assertIteratorValues(this, infgraph.find(n1, q, null),
            new Triple[] {
                new Triple(n1, q, Util.makeIntNode(2)),
                new Triple(n1, q, Util.makeIntNode(5))
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, q, null),
            new Triple[] {
                new Triple(n2, q, Util.makeIntNode(1))
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
        List ruleList = Rule.parseRules(rules);
        Graph schema = new GraphMem();
        schema.add(new Triple(n1, p, n3));
        Graph data = new GraphMem();
        data.add(new Triple(n1, q, n4));
        data.add(new Triple(n1, q, n3));
        
        Reasoner reasoner =  createReasoner(ruleList);
        Reasoner boundReasoner = reasoner.bindSchema(schema);
        InfGraph infgraph = boundReasoner.bind(data);

        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null),
            new Triple[] {
                new Triple(n1, p, n3),
                new Triple(n2, p, n3),
                new Triple(n3, p, n3),
                new Triple(n1, q, n4),
                new Triple(n2, q, n4),
                new Triple(n1, q, n3),
                new Triple(n2, q, n3),
                new Triple(res, p, n3)
            });
    }
    
    /**
     * The the "remove" builtin
     */
    public void testRemoveBuiltin() {
        String rules =  
                       "[rule1: (?x p ?y), (?x q ?y) -> remove(0)]" +
                       "";
        List ruleList = Rule.parseRules(rules);

        InfGraph infgraph = createReasoner(ruleList).bind(new GraphMem());
        infgraph.add(new Triple(n1, p, Util.makeIntNode(1)));
        infgraph.add(new Triple(n1, p, Util.makeIntNode(2)));
        infgraph.add(new Triple(n1, q, Util.makeIntNode(2)));
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, Util.makeIntNode(1)),
                new Triple(n1, q, Util.makeIntNode(2))
            });
        
    }
    
    /**
     * Test the rebind operation.
     */
    public void testRebind() {
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        List ruleList = Rule.parseRules(rules);
        Graph data = new GraphMem();
        data.add(new Triple(n1, p, n2));
        InfGraph infgraph = createReasoner(ruleList).bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, n2),
                new Triple(n1, q, n2)
            });
        Graph ndata = new GraphMem();
        ndata.add(new Triple(n1, p, n3));
        infgraph.rebind(ndata);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, n3),
                new Triple(n1, q, n3)
            });
    }
    
   
    /**
     * Test the close operation.
     */
    public void testClose() {
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        List ruleList = Rule.parseRules(rules);
        Graph data = new GraphMem();
        data.add(new Triple(n1, p, n2));
        InfGraph infgraph = createReasoner(ruleList).bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, n2),
                new Triple(n1, q, n2)
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
        Graph data = new GraphMem();
        data.add(new Triple(p, sP, q));
        data.add(new Triple(q, sP, r));
        data.add(new Triple(C1, sC, C2));
        data.add(new Triple(C2, sC, C3));
        data.add(new Triple(a, ty, C1));
        List rules = Rule.parseRules(
        "[rdfs8:  (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c)]" + 
        "[rdfs9:  (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y), (?a rdf:type ?x)]" +
        "[-> (rdf:type rdfs:range rdfs:Class)]" +
        "[rdfs3:  (?y rdf:type ?c) <- (?x ?p ?y), (?p rdfs:range ?c)]" +
        "[rdfs7:  (?a rdfs:subClassOf ?a) <- (?a rdf:type rdfs:Class)]"
                        );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, ty, null), 
            new Object[] {
                new Triple(a, ty, C1),
                new Triple(a, ty, C2),
                new Triple(a, ty, C3)
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
        Graph data = new GraphMem();
        data.add(new Triple(c, q, d));
        List rules = Rule.parseRules(
            "[r1: (c r ?x) <- (?x p f(?x b))]" +
            "[r2: (?y p f(a ?y)) <- (c q ?y)]"
                          );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), new Object[] { } );
              
        data.add(new Triple(c, q, a));
        rules = Rule.parseRules(
        "[r1: (c r ?x) <- (?x p f(?x a))]" +
        "[r2: (?y p f(a ?y)) <- (c q ?y)]"
                          );        
        reasoner =  createReasoner(rules);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), 
              new Object[] {
                  new Triple(c, r, a)
              } );
            
        data = new GraphMem();
        data.add(new Triple(a, q, a));
        data.add(new Triple(a, q, b));
        data.add(new Triple(a, q, c));
        data.add(new Triple(b, q, d));
        data.add(new Triple(b, q, b));
        rules = Rule.parseRules(
          "[r1: (c r ?x) <- (?x p ?x)]" +
          "[r2: (?x p ?y) <- (a q ?x), (b q ?y)]"
                          );        
        reasoner =  createReasoner(rules);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), 
              new Object[] {
                  new Triple(c, r, b)
              } );
              
        rules = Rule.parseRules(
          "[r1: (c r ?x) <- (?x p ?x)]" +
          "[r2: (a p ?x) <- (a q ?x)]"
                          );        
        reasoner =  createReasoner(rules);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), 
              new Object[] {
                  new Triple(c, r, a)
              } );
    }

    /**
     * Test restriction example
     */
    public void testBackchain3() {    
        Graph data = new GraphMem();
        data.add(new Triple(a, ty, r));
        data.add(new Triple(a, p, b));
        data.add(new Triple(r, sC, C1));
        data.add(new Triple(C1, ty, OWL.Restriction.asNode()));
        data.add(new Triple(C1, OWL.onProperty.asNode(), p));
        data.add(new Triple(C1, OWL.allValuesFrom.asNode(), c));
        List rules = Rule.parseRules(
    "[rdfs9:   (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y) (?a rdf:type ?x)]" +
    "[restriction2: (?C owl:equivalentClass all(?P, ?D)) <- (?C rdf:type owl:Restriction), (?C owl:onProperty ?P), (?C owl:allValuesFrom ?D)]" +
    "[rs2: (?X rdf:type all(?P,?C)) <- (?D owl:equivalentClass all(?P,?C)), (?X rdf:type ?D)]" +
    "[rp4: (?Y rdf:type ?C) <- (?X rdf:type all(?P, ?C)), (?X ?P ?Y)]"
                          );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, c), new Object[] {
                  new Triple(b, ty, c)
              } );
    }
    
    /**
     * Test example hybrid rule.
     */
    public void testHybrid1() {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(p, ty, s));
        List rules = Rule.parseRules(
        "[r1: (?p rdf:type s) -> [r1b: (?x ?p ?y) <- (?y ?p ?x)]]"
                          );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, p, null), new Object[] {
                  new Triple(a, p, b),
                  new Triple(b, p, a)
              } );
    }
    
    /**
     * Test example hybrid rule.
     */
    public void testHybrid2() {
        Graph data = new GraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(p, ty, s));
        List rules = Rule.parseRules(
        "[a1: -> (a rdf:type t)]" +
        "[r0: (?x r ?y) -> (?x p ?y)]" +
        "[r1: (?p rdf:type s) -> [r1b: (?x ?p ?y) <- (?y ?p ?x)]]" +
        "[r2: (?p rdf:type s) -> [r2b: (?x ?p ?x) <- (?x rdf:type t)]]"
                          );        
        Reasoner reasoner =  createReasoner(rules);
        FBRuleInfGraph infgraph = (FBRuleInfGraph) reasoner.bind(data);
        infgraph.setDerivationLogging(true);
        infgraph.prepare();
        assertTrue("Forward rule count", infgraph.getNRulesFired() == 3);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, p, null), new Object[] {
                  new Triple(a, p, a),
                  new Triple(a, p, b),
                  new Triple(b, p, a)
              } );
        // Suppressed until LP engine implements rule counting, if ever
//        assertTrue("Backward rule count", infgraph.getNRulesFired() == 8);
              
        // Check derivation tracing as well
        // Suppressed until LP engine implements derivation tracing
        Iterator di = infgraph.getDerivation(new Triple(b, p, a));
        assertTrue(di.hasNext());
        RuleDerivation d = (RuleDerivation)di.next();
        assertTrue(d.getRule().getName().equals("r1b"));
        TestUtil.assertIteratorValues(this, d.getMatches().iterator(), new Object[] { new Triple(a, p, b) });
        assertTrue(! di.hasNext());
    }
    
    /**
     * Test example hybrid rules for rdfs.
     */
    public void testHybridRDFS() {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(p, RDFS.range.asNode(), C1));
        List rules = Rule.parseRules(
    "[rdfs2:  (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ]" +
    "[rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]" + 
    "[rdfs5a: (?a rdfs:subPropertyOf ?b), (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c)]" + 
    "[rdfs5b: (?a rdf:type rdf:Property) -> (?a rdfs:subPropertyOf ?a)]" + 
    "[rdfs6:  (?p rdfs:subPropertyOf ?q) -> [ (?a ?q ?b) <- (?a ?p ?b)] ]" + 
    "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]" +
    "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
    "[rdfs9:  (?x rdfs:subClassOf ?y) -> [ (?a rdf:type ?y) <- (?a rdf:type ?x)] ]" +
                          "" );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
//        ((FBRuleInfGraph)infgraph).setTraceOn(true);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, null), new Object[] {
                  new Triple(b, ty, C1)
              } );
    }
    
    /**
     * Test example hybrid rules for rdfs.
     */
    public void testHybridRDFS2() {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(p, sP, r));
        data.add(new Triple(r, RDFS.range.asNode(), C1));
        List rules = Rule.parseRules(
    "[rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]" + 
    "[rdfs6:  (?p rdfs:subPropertyOf ?q) -> [ (?a ?q ?b) <- (?a ?p ?b)] ]" + 
                          "" );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
//        ((FBRuleInfGraph)infgraph).setTraceOn(true);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, C1), new Object[] {
                  new Triple(b, ty, C1)
              } );
    }

    /**
     * Test access to makeInstance machinery from a Brule.
     */
    public void testMakeInstance() {
        Graph data = new GraphMem();
        data.add(new Triple(a, ty, C1));
        List rules = Rule.parseRules(
        "[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]" +
        "[r2:  (?t rdf:type C2) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]" +
                          "" );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        
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
        Graph data = new GraphMem();
        data.add(new Triple(a, ty, C1));
        List rules = Rule.parseRules(
        "[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, ?t)]" +
                          "" );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        
        Node valueInstance = getValue(infgraph, a, p);
        assertNotNull(valueInstance);
        Node valueInstance2 = getValue(infgraph, a, p);
        assertEquals(valueInstance, valueInstance2);
    }
    
    /**
     * Test case for makeInstance which failed during development.
     */
    public void testMakeInstanceBug() {
        Graph data = new GraphMem();
        data.add(new Triple(a, ty, r));
        data.add(new Triple(r, sC, Functor.makeFunctorNode("some", new Node[] {p, C1})));
        List rules = Rule.parseRules(
        "[some1: (?C rdfs:subClassOf some(?P, ?D)) ->"
        + "[some1b: (?X ?P ?T) <- (?X rdf:type ?C), unbound(?T), noValue(?X, ?P), makeInstance(?X, ?P, ?D, ?T) ]" 
        + "[some1b2: (?T rdf:type ?D) <- (?X rdf:type ?C), bound(?T), makeInstance(?X, ?P, ?D, ?T) ]"
        + "]");
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        
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
        List ruleList = Rule.parseRules(rules);
        Graph data = new GraphMem();
        data.add(new Triple(n1, p, Util.makeIntNode(2)) );
        data.add(new Triple(n2, p, Functor.makeFunctorNode("f", new Node[] {
                                        a, Util.makeIntNode(0)  })));
        data.add(new Triple(n3, p, Functor.makeFunctorNode("f", new Node[] {
               a, Node.createLiteral(new LiteralLabel("0", "", XSDDatatype.XSDnonNegativeInteger)) } )));
        InfGraph infgraph = createReasoner(ruleList).bind(data);
        
        TestUtil.assertIteratorValues(this, infgraph.find(null, s, null),
            new Triple[] {
                new Triple(n2, s, res),
                new Triple(n3, s, res),
            });
    }
    
    /**
     * Test the builtins themselves
     */
    public void testBuiltins2() {
        // Numeric comparisions
        Node lt = Node.createURI("lt");
        Node gt = Node.createURI("gt");
        Node le = Node.createURI("le");
        Node ge = Node.createURI("ge");
        Node eq = Node.createURI("eq");
        Node ne = Node.createURI("ne");
        String rules =  
        "[r1: (?x q ?vx), (?y q ?vy), lessThan(?vx, ?vy) -> (?x lt ?y)]" +
        "[r2: (?x q ?vx), (?y q ?vy), greaterThan(?vx, ?vy) -> (?x gt ?y)]" +
        "[r3: (?x q ?vx), (?y q ?vy), le(?vx, ?vy) -> (?x le ?y)]" +
        "[r4: (?x q ?vx), (?y q ?vy), ge(?vx, ?vy) -> (?x ge ?y)]" +
        "[r5: (?x q ?vx), (?y q ?vy), notEqual(?vx, ?vy) -> (?x ne ?y)]" +
        "[r6: (?x q ?vx), (?y q ?vy), equal(?vx, ?vy) -> (?x eq ?y)]" +
                       "";
        List ruleList = Rule.parseRules(rules);
        Graph data = new GraphMem();
        data.add(new Triple(n1, q, Util.makeIntNode(2)) );
        data.add(new Triple(n2, q, Util.makeIntNode(2)) );
        data.add(new Triple(n3, q, Util.makeIntNode(3)) );
        InfGraph infgraph = createReasoner(ruleList).bind(data);
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, n2),
            new Triple[] {
                new Triple(n1, eq, n2),
                new Triple(n1, le, n2),
                new Triple(n1, ge, n2),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, n3),
            new Triple[] {
                new Triple(n1, ne, n3),
                new Triple(n1, lt, n3),
                new Triple(n1, le, n3),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n3, null, n1),
            new Triple[] {
                new Triple(n3, ne, n1),
                new Triple(n3, gt, n1),
                new Triple(n3, ge, n1),
            });
        
        // Floating point comparisons
        data = new GraphMem();
        data.add(new Triple(n1, q, Util.makeIntNode(2)) );
        data.add(new Triple(n2, q, Util.makeDoubleNode(2.2)) );
        data.add(new Triple(n3, q, Util.makeDoubleNode(2.3)) );
        infgraph = createReasoner(ruleList).bind(data);
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, n2),
            new Triple[] {
                new Triple(n1, ne, n2),
                new Triple(n1, le, n2),
                new Triple(n1, lt, n2),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, null, n3),
            new Triple[] {
                new Triple(n2, ne, n3),
                new Triple(n2, le, n3),
                new Triple(n2, lt, n3),
            });
            
        // Arithmetic            
        rules =  
        "[r1: (?x p ?a), (?x q ?b), sum(?a, ?b, ?c) -> (?x s ?c)]" +
        "[r2: (?x p ?a), (?x q ?b), product(?a, ?b, ?c) -> (?x t ?c)]" +
                       "";
        ruleList = Rule.parseRules(rules);
        data = new GraphMem();
        data.add(new Triple(n1, p, Util.makeIntNode(3)) );
        data.add(new Triple(n1, q, Util.makeIntNode(5)) );
        infgraph = createReasoner(ruleList).bind(data);
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, Util.makeIntNode(3)),
                new Triple(n1, q, Util.makeIntNode(5)),
                new Triple(n1, s, Util.makeIntNode(8)),
                new Triple(n1, t, Util.makeIntNode(15)),
            });
         
        // Note type checking   
        rules =  
        "[r1: (?x p ?y), isLiteral(?y) -> (?x s 'literal')]" +
        "[r1: (?x p ?y), notLiteral(?y) -> (?x s 'notLiteral')]" +
        "[r1: (?x p ?y), isBNode(?y) -> (?x s 'bNode')]" +
        "[r1: (?x p ?y), notBNode(?y) -> (?x s 'notBNode')]" +
                       "";
        ruleList = Rule.parseRules(rules);
        data = new GraphMem();
        data.add(new Triple(n1, p, Util.makeIntNode(3)) );
        data.add(new Triple(n2, p, res));
        data.add(new Triple(n3, p, Node.createAnon()));
        infgraph = createReasoner(ruleList).bind(data);
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, s, null),
            new Triple[] {
                new Triple(n1, s, Node.createLiteral("literal", "", null)),
                new Triple(n1, s, Node.createLiteral("notBNode", "", null)),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, s, null),
            new Triple[] {
                new Triple(n2, s, Node.createLiteral("notLiteral", "", null)),
                new Triple(n2, s, Node.createLiteral("notBNode", "", null)),
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n3, s, null),
            new Triple[] {
                new Triple(n3, s, Node.createLiteral("notLiteral", "", null)),
                new Triple(n3, s, Node.createLiteral("bNode", "", null)),
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
        ruleList = Rule.parseRules(rules);
        data = new GraphMem();
        data.add(new Triple(n1, p, Util.makeIntNode(3)) );
        data.add(new Triple(n2, p, Node.createLiteral("foo", "", null)) );
        data.add(new Triple(n3, p, Node.createLiteral("foo", "", XSDDatatype.XSDstring)) );
        data.add(new Triple(n4, p, n4));
        infgraph = createReasoner(ruleList).bind(data);
        
        TestUtil.assertIteratorValues(this, infgraph.find(null, s, null),
            new Triple[] {
                new Triple(n1, s, Node.createLiteral("isLiteral", "", null)),
                new Triple(n1, s, Node.createLiteral("isXSDInt", "", null)),
                new Triple(n1, s, Node.createLiteral("notXSDString", "", null)),

                new Triple(n2, s, Node.createLiteral("isLiteral", "", null)),
                new Triple(n2, s, Node.createLiteral("notXSDInt", "", null)),
                new Triple(n2, s, Node.createLiteral("notXSDString", "", null)),

                new Triple(n3, s, Node.createLiteral("isLiteral", "", null)),
                new Triple(n3, s, Node.createLiteral("notXSDInt", "", null)),
                new Triple(n3, s, Node.createLiteral("isXSDString", "", null)),

                new Triple(n4, s, Node.createLiteral("notLiteral", "", null)),
                new Triple(n4, s, Node.createLiteral("notXSDInt", "", null)),
                new Triple(n4, s, Node.createLiteral("notXSDString", "", null)),
            });
            
        // Literal counting
        rules = "[r1: (?x p ?y), countLiteralValues(?x, p, ?c) -> (?x s ?c)]";
        ruleList = Rule.parseRules(rules);
        data = new GraphMem();
        data.add(new Triple(n1, p, Util.makeIntNode(2)) );
        data.add(new Triple(n1, p, Util.makeIntNode(2)) );
        data.add(new Triple(n1, p, Util.makeIntNode(3)) );
        data.add(new Triple(n1, p, n2) );
        infgraph = createReasoner(ruleList).bind(data);
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, s, null),
            new Triple[] {
                new Triple(n1, s, Util.makeIntNode(2)),
            });
            
    }
         
    
    /**
     * Helper - returns the single object value for an s/p pair, asserts an error
     * if there is more than one.
     */
    private Node getValue(Graph g, Node s, Node p) {
        ExtendedIterator i = g.find(s, p, null);
        assertTrue(i.hasNext());
        Node result = ((Triple)i.next()).getObject();
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
    public void testDuplicatesEC4() throws IOException {
        Model premisesM = ModelLoader.loadModel("file:testing/wg/equivalentClass/premises004.rdf");
        Graph data = premisesM.getGraph();
        Reasoner reasoner =  new OWLFBRuleReasoner(OWLFBRuleReasonerFactory.theInstance());
        InfGraph infgraph = reasoner.bind(data);
        Node rbPrototypeProp = Node.createURI(ReasonerVocabulary.RBNamespace+"prototype");
        int count = 0;
        for (Iterator i = infgraph.find(null, rbPrototypeProp, null); i.hasNext(); ) {
            Object t = i.next();
//            System.out.println(" - " + PrintUtil.print(t));
            count++;
        }
//        listFBGraph("direct databind case", (FBRuleInfGraph)infgraph);
        assertTrue(count == 5);
        
        infgraph = reasoner.bindSchema(data).bind(new GraphMem());
        count = 0;
        for (Iterator i = infgraph.find(null, rbPrototypeProp, null); i.hasNext(); ) {
            Object t = i.next();
//            System.out.println(" - " + PrintUtil.print(t));
            count++;
        }
//        listFBGraph("bindSchema case", (FBRuleInfGraph)infgraph);
        assertTrue(count == 5);
    }
    
    /**
     * Check cost of creating an empty OWL closure.
     */
    public void temp() {
        Graph data = new GraphMem();
        Graph data2 = new GraphMem();
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
        for (Iterator i = g.find(null,null,null); i.hasNext();) {
            Triple t = (Triple)i.next();
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