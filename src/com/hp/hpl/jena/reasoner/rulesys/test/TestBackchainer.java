/******************************************************************
 * File:        TestBackchainer.java
 * Created by:  Dave Reynolds
 * Created on:  04-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestBackchainer.java,v 1.9 2003-05-15 08:37:35 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *  
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.9 $ on $Date: 2003-05-15 08:37:35 $
 */
public class TestBackchainer extends TestCase {

    // Useful constants
    Node p = Node.createURI("p");
    Node q = Node.createURI("q");
    Node r = Node.createURI("r");
    Node s = Node.createURI("s");
    Node t = Node.createURI("t");
    Node a = Node.createURI("a");
    Node b = Node.createURI("b");
    Node c = Node.createURI("c");
    Node d = Node.createURI("d");
    Node C1 = Node.createURI("C1");
    Node C2 = Node.createURI("C2");
    Node C3 = Node.createURI("C3");
    Node sP = RDFS.subPropertyOf.getNode();
    Node sC = RDFS.subClassOf.getNode();
    Node ty = RDF.type.getNode();
    
    String testRules1 = 
        "(?x ?q ?y) <- (?p rdfs:subPropertyOf ?q)(?x ?p ?y). " + 
        "(?a rdfs:subPropertyOf ?c) <- (?a rdfs:subPropertyOf ?b)(?b rdfs:subPropertyOf ?c). ";
        
    String testRuleAxioms = "[ -> (p rdfs:subPropertyOf q)]" +
                            "[ -> (q rdfs:subPropertyOf r) ]" +
                            "[ -> (a p b) ]";
                            
    Triple[] dataElts = new Triple[] {
                            new Triple(p, sP, q),
                            new Triple(q, sP, r),
                            new Triple(a,  p, b) 
                            };
     
    /**
     * Boilerplate for junit
     */ 
    public TestBackchainer( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestBackchainer.class ); 
    }  

//    /**
//     * Test parser modes to support backarrow notation are working
//     */
//    public void testParse() {
//        List rules = Rule.parseRules(testRules1);
//        assertEquals("BRule parsing", 
//                        "[ (?p rdfs:subPropertyOf ?q) (?x ?p ?y) -> (?x ?q ?y) ]", 
//                        rules.get(0).toString());
//        assertEquals("BRule parsing", 
//                        "[ (?a rdfs:subPropertyOf ?b) (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c) ]", 
//                        rules.get(1).toString());
//    }
//    
    /**
     * Test goal/head unify operation.
     */
    public void testUnify() {
        Node_RuleVariable xg = new Node_RuleVariable("x", 0);
        Node_RuleVariable yg = new Node_RuleVariable("y", 1);
        Node_RuleVariable zg = new Node_RuleVariable("z", 2);
        
        Node_RuleVariable xh = new Node_RuleVariable("x", 0);
        Node_RuleVariable yh = new Node_RuleVariable("y", 1);
        Node_RuleVariable zh = new Node_RuleVariable("z", 2);
        
        TriplePattern g1 = new TriplePattern(xg, p, yg);
        TriplePattern g2 = new TriplePattern(xg, p, xg);
        TriplePattern g3 = new TriplePattern( a, p, xg);
        TriplePattern g4 = new TriplePattern( a, p,  b);
        
        TriplePattern h1 = new TriplePattern(xh, p, yh);
        TriplePattern h2 = new TriplePattern(xh, p, xh);
        TriplePattern h3 = new TriplePattern( a, p, xh);
        TriplePattern h4 = new TriplePattern( a, p,  b);
        
//        doTestUnify(g1, h1, true, new Node[] {null, null});
//        doTestUnify(g1, h2, true, new Node[] {null, null});
//        doTestUnify(g1, h3, true, new Node[] {null, null});
//        doTestUnify(g1, h4, true, new Node[] {null, null});
        
        doTestUnify(g2, h1, true, new Node[] {null, xh});
        doTestUnify(g2, h2, true, new Node[] {null, null});
        doTestUnify(g2, h3, true, new Node[] {a, null});
        doTestUnify(g2, h4, false, null);
        
        doTestUnify(g3, h1, true, new Node[] {a, null});
        doTestUnify(g3, h2, true, new Node[] {a, null});
        doTestUnify(g3, h3, true, new Node[] {null, null});
        doTestUnify(g3, h4, true, new Node[] {null, null});
        
        doTestUnify(g4, h1, true, new Node[] {a, b});
        doTestUnify(g4, h2, false, null);
        doTestUnify(g4, h3, true, new Node[] {b});
        doTestUnify(g4, h4, true, null);

        // Test functor cases as well!
        TriplePattern gf = new TriplePattern(xg, p, 
                                Functor.makeFunctorNode("f", new Node[]{xg, b}));
        TriplePattern hf1 = new TriplePattern(yh, p, 
                                Functor.makeFunctorNode("f", new Node[]{zh, b}));
        TriplePattern hf2 = new TriplePattern(yh, p, 
                                Functor.makeFunctorNode("f", new Node[]{a, yh}));
        TriplePattern hf3 = new TriplePattern(yh, p, 
                                Functor.makeFunctorNode("f", new Node[]{b, yh}));
        doTestUnify(gf, hf1, true, new Node[] {null, null, yh});
        doTestUnify(gf, hf2, false, null);
        doTestUnify(gf, hf3, true, new Node[] {null, b});
    }
    
    /**
     * Helper for testUnify.
     * @param goal goal triple pattern
     * @param head head triple pattern
     * @param succeed whether match should succeeed or fail
     * @param env list list of expected environment bindings
     * 
     */
    private void doTestUnify(TriplePattern goal, TriplePattern head, boolean succeed, Node[] env) {
        BindingVector result = RuleState.unify(goal, head);
        if (succeed) {
            assertNotNull(result);
            if (env != null) {
                for (int i = 0; i < env.length; i++) {
                    Node n = result.getEnvironment()[i];
                    if (env[i] != null) {
                        assertEquals(env[i], n);
                    } else {
                        assertNull(n);
                    }
                }
            }
        } else {
            assertNull(result);
        }
    }
    
//    /**
//     * Check that a reasoner over an empty rule set accesses
//     * the raw data successfully.
//     */
//    public void testListData() {
//        Graph data = new GraphMem();
//        for (int i = 0; i < dataElts.length; i++) {
//            data.add(dataElts[i]);
//        }
//        Graph schema = new GraphMem();
//        schema.add(new Triple(c, p, c));
//        
//        // Case of schema and data but no rule axioms
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(new ArrayList());
//        InfGraph infgraph = reasoner.bindSchema(schema).bind(data);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, null, null), 
//            new Object[] {
//                new Triple(p, sP, q),
//                new Triple(q, sP, r),
//                new Triple(a,  p, b), 
//                new Triple(c, p, c)});
//                
//        // Case of data and rule axioms but no schema
//        List rules = Rule.parseRules("-> (d p d).");
//        reasoner =  new BasicBackwardRuleReasoner(rules);
//        infgraph = reasoner.bind(data);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, null, null), 
//            new Object[] {
//                new Triple(p, sP, q),
//                new Triple(q, sP, r),
//                new Triple(a,  p, b), 
//                new Triple(d, p, d)});
//                
//        // Case of data and rule axioms and schema
//        infgraph = reasoner.bindSchema(schema).bind(data);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, null, null), 
//            new Object[] {
//                new Triple(p, sP, q),
//                new Triple(q, sP, r),
//                new Triple(a,  p, b), 
//                new Triple(c, p, c),
//                new Triple(d, p, d)});
//                
//    }
//   
//    /**
//     * Test basic rule operations - simple AND rule 
//     */
//    public void testBaseRules1() {    
//        List rules = Rule.parseRules("[r1: (?a r ?c) <- (?a p ?b),(?b p ?c)]");        
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(b, p, c));
//        data.add(new Triple(b, p, d));
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, r, null), 
//            new Object[] {
//                new Triple(a, r, c),
//                new Triple(a, r, d)
//            } );
//    }
//   
//    /**
//     * Test basic rule operations - simple OR rule 
//     */
//    public void testBaseRules2() {    
//        List rules = Rule.parseRules(
//                "[r1: (?a r ?b) <- (?a p ?b)]" +
//                "[r2: (?a r ?b) <- (?a q ?b)]" +
//                "[r3: (?a r ?b) <- (?a s ?c), (?c s ?b)]"
//        );        
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(b, q, c));
//        data.add(new Triple(a, s, b));
//        data.add(new Triple(b, s, d));
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, r, null), 
//            new Object[] {
//                new Triple(a, r, b),
//                new Triple(b, r, c),
//                new Triple(a, r, d)
//            } );
//    }
//   
//    /**
//     * Test basic rule operations - simple OR rule with chaining 
//     */
//    public void testBaseRules2b() {    
//        List rules = Rule.parseRules(
//                "[r1: (?a r ?b) <- (?a p ?b)]" +
//                "[r2: (?a r ?b) <- (?a q ?b)]" +
//                "[r3: (?a r ?b) <- (?a t ?c), (?c t ?b)]" +
//                "[r4: (?a t ?b) <- (?a s ?b)]"
//        );        
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(b, q, c));
//        data.add(new Triple(a, s, b));
//        data.add(new Triple(b, s, d));
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, r, null), 
//            new Object[] {
//                new Triple(a, r, b),
//                new Triple(b, r, c),
//                new Triple(a, r, d)
//            } );
//    }
//    
//    /**
//     * Test basic rule operations - simple AND rule check with tabling.
//     */
//    public void testBaseRules3() {    
//        List rules = Rule.parseRules("[rule: (?a rdfs:subPropertyOf ?c) <- (?a rdfs:subPropertyOf ?b),(?b rdfs:subPropertyOf ?c)]");        
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        Graph data = new GraphMem();
//        data.add(new Triple(p, sP, q) );
//        data.add(new Triple(q, sP, r) );
//        data.add(new Triple(p, sP, s) );
//        data.add(new Triple(s, sP, t) );
//        data.add(new Triple(a,  p, b) );
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, RDFS.subPropertyOf.asNode(), null), 
//            new Object[] {
//                new Triple(p, sP, q),
//                new Triple(q, sP, r),
//                new Triple(p, sP, s),
//                new Triple(s, sP, t),
//                new Triple(p, sP, t),
//                new Triple(p, sP, r)
//            } );
//    }
//    
//    /**
//     * Test basic rule operations - simple AND rule check with tabling.
//     */
//    public void testBaseRules3b() {    
//        List rules = Rule.parseRules("[rule: (?a rdfs:subPropertyOf ?c) <- (?a rdfs:subPropertyOf ?b),(?b rdfs:subPropertyOf ?c)]");        
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        Graph data = new GraphMem();
//        data.add(new Triple(p, sP, q) );
//        data.add(new Triple(q, sP, r) );
//        data.add(new Triple(r, sP, t) );
//        data.add(new Triple(q, sP, s) );
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, RDFS.subPropertyOf.asNode(), null), 
//            new Object[] {
//                new Triple(p, sP, q),
//                new Triple(q, sP, r),
//                new Triple(r, sP, t),
//                new Triple(q, sP, s),
//                new Triple(p, sP, s),
//                new Triple(p, sP, r),
//                new Triple(p, sP, t),
//                new Triple(q, sP, t),
//                new Triple(p, sP, r)
//            } );
//    }
//
//    /**
//     * Test basic rule operations - simple AND/OR with tabling.
//     */
//    public void testBaseRules4() {    
//        Graph data = new GraphMem();
//        data.add(new Triple(a, r, b));
//        data.add(new Triple(b, r, c));
//        data.add(new Triple(b, r, b));
//        data.add(new Triple(b, r, d));
//        List rules = Rule.parseRules(
//                        "[r1: (?x p ?y) <- (?x r ?y)]" +
//                        "[r2: (?x p ?z) <- (?x p ?y), (?y r ?z)]" 
//                        );        
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, p, null), 
//            new Object[] {
//                new Triple(a, p, b),
//                new Triple(a, p, d),
//                new Triple(a, p, c)
//            } );
//    }
//
//    /**
//     * Test basic rule operations - simple AND/OR with tabling.
//     */
//    public void testBaseRulesXSB1() {    
//        Graph data = new GraphMem();
//        data.add(new Triple(p, c, q));
//        data.add(new Triple(q, c, r));
//        data.add(new Triple(p, d, q));
//        data.add(new Triple(q, d, r));
//        List rules = Rule.parseRules(
//            "[r1: (?x a ?y) <- (?x c ?y)]" +
//            "[r2: (?x a ?y) <- (?x b ?z), (?z c ?y)]" +
//            "[r3: (?x b ?y) <- (?x d ?y)]" +
//            "[r4: (?x b ?y) <- (?x a ?z), (?z d ?y)]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(p, a, null), 
//            new Object[] {
//                new Triple(p, a, q),
//                new Triple(p, a, r)
//            } );
//    }
//    
//    /**
//     * Test basic functor usage.
//     */
//    public void testFunctors1() {
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(a, q, c));
//        List rules = Rule.parseRules(
//            "[r1: (?x r f(?y,?z)) <- (?x p ?y), (?x q ?z)]" +
//            "[r2: (?x s ?y) <- (?x r f(?y, ?z))]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, s, null), 
//            new Object[] {
//                new Triple(a, s, b)
//            } );
//    }
//    
//    /**
//     * Test basic functor usage.
//     */
//    public void testFunctors2() {
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(a, q, c));
//        data.add(new Triple(a, t, d));
//        List rules = Rule.parseRules(
//            "[r1: (?x r f(?y,?z)) <- (?x p ?y), (?x q ?z)]" +
//            "[r2: (?x s ?y) <- (?x r f(?y, ?z))]" +
//            "[r3: (?x r g(?y,?z)) <- (?x p ?y), (?x t ?z)]" +
//            "[r4: (?x s ?z) <- (?x r g(?y, ?z))]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, s, null), 
//            new Object[] {
//                new Triple(a, s, b),
//                new Triple(a, s, d)
//            } );
//    }
//    
//    /**
//     * Test basic builtin usage.
//     */
//    public void testBuiltin1() {
//        Graph data = new GraphMem();
//        List rules = Rule.parseRules(
//            "[a1: -> (a p '2') ]" +
//            "[a2: -> (a q '3') ]" +
//            "[r1: (?x r ?s) <- (?x p ?y), (?x q ?z), sum(?y, ?z, ?s)]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, r, null), 
//            new Object[] {
//                new Triple(a, r, Util.makeIntNode(5))
//            } );
//    }
//   
//    /**
//     * Test basic builtin usage.
//     */
//    public void testBuiltin2() {
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(a, q, c));
//        List rules = Rule.parseRules(
//            "[r1: (?x r ?y ) <- bound(?x), (?x p ?y) ]" +
//            "[r2: (?x r ?y) <- unbound(?x), (?x q ?y)]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, r, null), 
//            new Object[] {
//                new Triple(a, r, b)
//            } );
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, r, null), 
//            new Object[] {
//                new Triple(a, r, c)
//            } );
//    }
//   
//    /**
//     * Test basic builtin usage.
//     */
//    public void testBuiltin3() {
//        Graph data = new GraphMem();
//        List rules = Rule.parseRules(
//            "[r1: (a p b ) <- unbound(?x) ]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, null, null), 
//            new Object[] {
//                new Triple(a, p, b)
//            } );
//    }
//  
//    /**
//     * Test basic ground head patterns.
//     */
//    public void testGroundHead() {
//        Graph data = new GraphMem();
//        data.add(new Triple(a, r, b));
//        List rules = Rule.parseRules(
//            "[r1: (a p b ) <- (a r b) ]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, null, null), 
//            new Object[] {
//                new Triple(a, p, b),
//                new Triple(a, r, b)
//            } );
//    }
//  
//    /**
//     * Test multiheaded rule.
//     */
//    public void testMutliHead() {
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(b, r, c));
//        List rules = Rule.parseRules(
//            "[r1: (?x s ?z), (?z s ?x) <- (?x p ?y) (?y r ?z) ]"
//        );
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, s, null), 
//            new Object[] {
//                new Triple(a, s, c),
//                new Triple(c, s, a)
//            } );
//    }
//
//    /**
//     * Test rebind operation
//     */
//    public void testRebind() {
//        List rules = Rule.parseRules("[r1: (?a r ?c) <- (?a p ?b),(?b p ?c)]");        
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(b, p, c));
//        data.add(new Triple(b, p, d));
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, r, null), 
//            new Object[] {
//                new Triple(a, r, c),
//                new Triple(a, r, d)
//            } );
//        Graph ndata = new GraphMem();
//        ndata.add(new Triple(a, p, d));
//        ndata.add(new Triple(d, p, b));
//        infgraph.rebind(ndata);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, r, null), 
//            new Object[] {
//                new Triple(a, r, b)
//            } );
//
//    }
//
//    /**
//     * Test troublesome rdfs rules
//     */
//    public void testRDFSProblemsb() {    
//        Graph data = new GraphMem();
//        data.add(new Triple(C1, sC, C2));
//        data.add(new Triple(C2, sC, C3));
//        data.add(new Triple(C1, ty, RDFS.Class.asNode()));
//        data.add(new Triple(C2, ty, RDFS.Class.asNode()));
//        data.add(new Triple(C3, ty, RDFS.Class.asNode()));
//        List rules = Rule.parseRules(
//        "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
//        "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]"
//                        );        
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, sC, null), 
//            new Object[] {
//                new Triple(C1, sC, C2),
//                new Triple(C1, sC, C3),
//                new Triple(C1, sC, C1),
//                new Triple(C2, sC, C3),
//                new Triple(C2, sC, C2),
//                new Triple(C3, sC, C3),
//            } );
//    }

//    /**
//     * Test troublesome rdfs rules
//     */
//    public void testRDFSProblems() {    
//        Graph data = new GraphMem();
//        data.add(new Triple(p, sP, q));
//        data.add(new Triple(q, sP, r));
//        data.add(new Triple(C1, sC, C2));
//        data.add(new Triple(C2, sC, C3));
//        data.add(new Triple(a, ty, C1));
//        List rules = Rule.parseRules(
//        "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
//        "[rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]" +
//        "[-> (rdf:type rdfs:range rdfs:Class)]" +
//        "[rdfs3:  (?x ?p ?y), (?p rdfs:range ?c) -> (?y rdf:type ?c)]" +
//        "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]"
//                        );        
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
////        TestUtil.assertIteratorValues(this, 
////            infgraph.find(a, ty, null), 
////            new Object[] {
////                new Triple(a, ty, C1),
////                new Triple(a, ty, C2),
////                new Triple(a, ty, C3)
////            } );
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(C1, sC, a), 
//            new Object[] {
//            } );
//    }

//  /**
//   * Test complex rule head unification
//   */
//  public void testHeadUnitfy() {    
//      Graph data = new GraphMem();
//      data.add(new Triple(c, q, d));
//      List rules = Rule.parseRules(
//      "[r1: (c r ?x) <- (?x p f(?x b))]" +
//      "[r2: (?y p f(a ?y)) <- (c q ?y)]"
//                      );        
//      Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//      InfGraph infgraph = reasoner.bind(data);
//      ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//      TestUtil.assertIteratorValues(this, 
//          infgraph.find(c, r, null), 
//          new Object[] {
//              new Triple(c, r, d)
//          } );
//          
//      data = new GraphMem();
//      data.add(new Triple(a, q, a));
//      data.add(new Triple(a, q, b));
//      data.add(new Triple(a, q, c));
//      data.add(new Triple(b, q, d));
//      data.add(new Triple(b, q, b));
//      rules = Rule.parseRules(
//      "[r1: (c r ?x) <- (?x p ?x)]" +
//      "[r2: (?x p ?y) <- (a q ?x), (b q ?y)]"
//                      );        
//      reasoner =  new BasicBackwardRuleReasoner(rules);
//      infgraph = reasoner.bind(data);
//      ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//      TestUtil.assertIteratorValues(this, 
//          infgraph.find(c, r, null), 
//          new Object[] {
//              new Triple(c, r, b)
//          } );
//          
//      rules = Rule.parseRules(
//      "[r1: (c r ?x) <- (?x p ?x)]" +
//      "[r2: (a p ?x) <- (a q ?x)]"
//                      );        
//      reasoner =  new BasicBackwardRuleReasoner(rules);
//      infgraph = reasoner.bind(data);
//      ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//      TestUtil.assertIteratorValues(this, 
//          infgraph.find(c, r, null), 
//          new Object[] {
//              new Triple(c, r, a)
//          } );
//  }

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