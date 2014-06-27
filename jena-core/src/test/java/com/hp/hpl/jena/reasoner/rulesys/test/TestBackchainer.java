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

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.ArrayList ;
import java.util.List ;

import junit.framework.TestCase ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.reasoner.InfGraph ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.TriplePattern ;
import com.hp.hpl.jena.reasoner.rulesys.* ;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector ;
import com.hp.hpl.jena.reasoner.test.TestUtil ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

/**
 * Test harness for the backward chainer. 
 * Parameterizable in subclasses by overriding createReasoner.
 * The original version was developed for the original backchaining interpeter. 
 * That has now been obsoleted at this is now used to double check the
 * LP engine, though the bulk of such tests are really done by TestBasicLP.
 */
public class TestBackchainer extends TestCase {
    
    // Maximum size of binding environment needed in the tests
    private static final int MAX_VARS = 10;

    // Useful constants
    protected Node p = NodeFactory.createURI("p");
    protected Node q = NodeFactory.createURI("q");
    protected Node r = NodeFactory.createURI("r");
    protected Node s = NodeFactory.createURI("s");
    protected Node t = NodeFactory.createURI("t");
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
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestBackchainer( "testRDFSProblemsb" ));
//        return suite;
    }  

    /**
     * Override in subclasses to test other reasoners.
     */
    public Reasoner createReasoner(List<Rule> rules) {
        LPBackwardRuleReasoner reasoner = new LPBackwardRuleReasoner(rules);
        reasoner.tablePredicate(sP);
        reasoner.tablePredicate(sC);
        reasoner.tablePredicate(ty);
        reasoner.tablePredicate(p);
        reasoner.tablePredicate(a);
        reasoner.tablePredicate(b);
        return reasoner;
    }
    
    /**
     * Test parser modes to support backarrow notation are working
     */
    public void testParse() {
        List<Rule> rules = Rule.parseRules(testRules1);
        assertEquals("BRule parsing", 
                        "[ (?x ?q ?y) <- (?p rdfs:subPropertyOf ?q) (?x ?p ?y) ]", 
                        rules.get(0).toString());
        assertEquals("BRule parsing", 
                        "[ (?a rdfs:subPropertyOf ?c) <- (?a rdfs:subPropertyOf ?b) (?b rdfs:subPropertyOf ?c) ]", 
                        rules.get(1).toString());
    }
    
    /**
     * Test goal/head unify operation.
     */
    public void testUnify() {
        Node_RuleVariable xg = new Node_RuleVariable("?x", 0);
        Node_RuleVariable yg = new Node_RuleVariable("?y", 1);
        Node_RuleVariable zg = new Node_RuleVariable("?z", 2);
        
        Node_RuleVariable xh = new Node_RuleVariable("?x", 0);
        Node_RuleVariable yh = new Node_RuleVariable("?y", 1);
        Node_RuleVariable zh = new Node_RuleVariable("?z", 2);
        
        TriplePattern g1 = new TriplePattern(xg, p, yg);
        TriplePattern g2 = new TriplePattern(xg, p, xg);
        TriplePattern g3 = new TriplePattern( a, p, xg);
        TriplePattern g4 = new TriplePattern( a, p,  b);
        
        TriplePattern h1 = new TriplePattern(xh, p, yh);
        TriplePattern h2 = new TriplePattern(xh, p, xh);
        TriplePattern h3 = new TriplePattern( a, p, xh);
        TriplePattern h4 = new TriplePattern( a, p,  b);
        TriplePattern h5 = new TriplePattern(xh, p,  a);
        
        doTestUnify(g1, h1, true, new Node[] {null, null});
        doTestUnify(g1, h2, true, new Node[] {null, null});
        doTestUnify(g1, h3, true, new Node[] {null, null});
        doTestUnify(g1, h4, true, new Node[] {null, null});
        doTestUnify(g1, h5, true, new Node[] {null, null});
        
        doTestUnify(g2, h1, true, new Node[] {null, xh});
        doTestUnify(g2, h2, true, new Node[] {null, null});
        doTestUnify(g2, h3, true, new Node[] {a, null});
        doTestUnify(g2, h4, false, null);
        doTestUnify(g2, h5, true, new Node[] {a, null});
        
        doTestUnify(g3, h1, true, new Node[] {a, null});
        doTestUnify(g3, h2, true, new Node[] {a, null});
        doTestUnify(g3, h3, true, new Node[] {null, null});
        doTestUnify(g3, h4, true, new Node[] {null, null});
        doTestUnify(g3, h5, true, new Node[] {a, null});
        
        doTestUnify(g4, h1, true, new Node[] {a, b});
        doTestUnify(g4, h2, false, null);
        doTestUnify(g4, h3, true, new Node[] {b});
        doTestUnify(g4, h4, true, null);
        doTestUnify(g4, h5, false, null);
        
        // Recursive case
        doTestUnify(h1, h1, true, new Node[] {null, null});
        
        // Wildcard case
        doTestUnify(new TriplePattern(null, null, null), h2, true, new Node[] {null, null});

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
        
        // Check binding environment use
        BindingVector env = BindingVector.unify(g2, h1, MAX_VARS);
        env.bind(xh, c);
        assertEquals(env.getBinding(yh), c);
        env = BindingVector.unify(g2, h1, MAX_VARS);
        env.bind(yh, c);
        assertEquals(env.getBinding(xh), c);
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
        BindingVector result = BindingVector.unify(goal, head, MAX_VARS);
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
    
    /**
     * Check that a reasoner over an empty rule set accesses
     * the raw data successfully.
     */
    public void testListData() {
        Graph data = Factory.createGraphMem();
        for ( Triple dataElt : dataElts )
        {
            data.add( dataElt );
        }
        Graph schema = Factory.createGraphMem();
        schema.add(new Triple(c, p, c));
        
        // Case of schema and data but no rule axioms
        Reasoner reasoner =  createReasoner(new ArrayList<Rule>());
        InfGraph infgraph = reasoner.bindSchema(schema).bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, null, null), 
            new Object[] {
                new Triple(p, sP, q),
                new Triple(q, sP, r),
                new Triple(a,  p, b), 
                new Triple(c, p, c)});
                
        // Case of data and rule axioms but no schema
        List<Rule> rules = Rule.parseRules("-> (d p d).");
        reasoner =  createReasoner(rules);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, null, null), 
            new Object[] {
                new Triple(p, sP, q),
                new Triple(q, sP, r),
                new Triple(a,  p, b), 
                new Triple(d, p, d)});
                
        // Case of data and rule axioms and schema
        infgraph = reasoner.bindSchema(schema).bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, null, null), 
            new Object[] {
                new Triple(p, sP, q),
                new Triple(q, sP, r),
                new Triple(a,  p, b), 
                new Triple(c, p, c),
                new Triple(d, p, d)});
                
    }
   
    /**
     * Test basic rule operations - simple AND rule 
     */
    public void testBaseRules1() {    
        List<Rule> rules = Rule.parseRules("[r1: (?a r ?c) <- (?a p ?b),(?b p ?c)]");        
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(b, p, c));
        data.add(new Triple(b, p, d));
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, r, null), 
            new Object[] {
                new Triple(a, r, c),
                new Triple(a, r, d)
            } );
    }
   
    /**
     * Test basic rule operations - simple OR rule 
     */
    public void testBaseRules2() {    
        List<Rule> rules = Rule.parseRules(
                "[r1: (?a r ?b) <- (?a p ?b)]" +
                "[r2: (?a r ?b) <- (?a q ?b)]" +
                "[r3: (?a r ?b) <- (?a s ?c), (?c s ?b)]"
        );        
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(b, q, c));
        data.add(new Triple(a, s, b));
        data.add(new Triple(b, s, d));
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, r, null), 
            new Object[] {
                new Triple(a, r, b),
                new Triple(b, r, c),
                new Triple(a, r, d)
            } );
    }
   
    /**
     * Test basic rule operations - simple OR rule with chaining 
     */
    public void testBaseRules2b() {    
        List<Rule> rules = Rule.parseRules(
                "[r1: (?a r ?b) <- (?a p ?b)]" +
                "[r2: (?a r ?b) <- (?a q ?b)]" +
                "[r3: (?a r ?b) <- (?a t ?c), (?c t ?b)]" +
                "[r4: (?a t ?b) <- (?a s ?b)]"
        );        
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(b, q, c));
        data.add(new Triple(a, s, b));
        data.add(new Triple(b, s, d));
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, r, null), 
            new Object[] {
                new Triple(a, r, b),
                new Triple(b, r, c),
                new Triple(a, r, d)
            } );
    }
    
    /**
     * Test basic rule operations - simple AND rule check with tabling.
     */
    public void testBaseRules3() {    
        List<Rule> rules = Rule.parseRules("[rule: (?a rdfs:subPropertyOf ?c) <- (?a rdfs:subPropertyOf ?b),(?b rdfs:subPropertyOf ?c)]");        
        Reasoner reasoner =  createReasoner(rules);
        Graph data = Factory.createGraphMem();
        data.add(new Triple(p, sP, q) );
        data.add(new Triple(q, sP, r) );
        data.add(new Triple(p, sP, s) );
        data.add(new Triple(s, sP, t) );
        data.add(new Triple(a,  p, b) );
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, RDFS.subPropertyOf.asNode(), null), 
            new Object[] {
                new Triple(p, sP, q),
                new Triple(q, sP, r),
                new Triple(p, sP, s),
                new Triple(s, sP, t),
                new Triple(p, sP, t),
                new Triple(p, sP, r)
            } );
    }
    
    /**
     * Test basic rule operations - simple AND rule check with tabling.
     */
    public void testBaseRules3b() {    
        List<Rule> rules = Rule.parseRules("[rule: (?a rdfs:subPropertyOf ?c) <- (?a rdfs:subPropertyOf ?b),(?b rdfs:subPropertyOf ?c)]");        
        Reasoner reasoner =  createReasoner(rules);
        Graph data = Factory.createGraphMem();
        data.add(new Triple(p, sP, q) );
        data.add(new Triple(q, sP, r) );
        data.add(new Triple(r, sP, t) );
        data.add(new Triple(q, sP, s) );
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, RDFS.subPropertyOf.asNode(), null), 
            new Object[] {
                new Triple(p, sP, q),
                new Triple(q, sP, r),
                new Triple(r, sP, t),
                new Triple(q, sP, s),
                new Triple(p, sP, s),
                new Triple(p, sP, r),
                new Triple(p, sP, t),
                new Triple(q, sP, t),
                new Triple(p, sP, r)
            } );
    }

    /**
     * Test basic rule operations - simple AND/OR with tabling.
     */
    public void testBaseRules4() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(b, r, c));
        data.add(new Triple(b, r, b));
        data.add(new Triple(b, r, d));
        List<Rule> rules = Rule.parseRules(
                        "[r1: (?x p ?y) <- (?x r ?y)]" +
                        "[r2: (?x p ?z) <- (?x p ?y), (?y r ?z)]" 
                        );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, p, null), 
            new Object[] {
                new Triple(a, p, b),
                new Triple(a, p, d),
                new Triple(a, p, c)
            } );
    }

    /**
     * Test basic rule operations - simple AND/OR with tabling.
     */
    public void testBaseRulesXSB1() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(p, c, q));
        data.add(new Triple(q, c, r));
        data.add(new Triple(p, d, q));
        data.add(new Triple(q, d, r));
        List<Rule> rules = Rule.parseRules(
            "[r1: (?x a ?y) <- (?x c ?y)]" +
            "[r2: (?x a ?y) <- (?x b ?z), (?z c ?y)]" +
            "[r3: (?x b ?y) <- (?x d ?y)]" +
            "[r4: (?x b ?y) <- (?x a ?z), (?z d ?y)]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(p, a, null), 
            new Object[] {
                new Triple(p, a, q),
                new Triple(p, a, r)
            } );
    }
    
    /**
     * Test basic functor usage.
     */
    public void testFunctors1() {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(a, q, c));
        List<Rule> rules = Rule.parseRules(
            "[r1: (?x r f(?y,?z)) <- (?x p ?y), (?x q ?z)]" +
            "[r2: (?x s ?y) <- (?x r f(?y, ?z))]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, s, null), 
            new Object[] {
                new Triple(a, s, b)
            } );
    }
    
    /**
     * Test basic functor usage.
     */
    public void testFunctors2() {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(a, q, c));
        data.add(new Triple(a, t, d));
        List<Rule> rules = Rule.parseRules(
            "[r1: (?x r f(?y,?z)) <- (?x p ?y), (?x q ?z)]" +
            "[r2: (?x s ?y) <- (?x r f(?y, ?z))]" +
            "[r3: (?x r g(?y,?z)) <- (?x p ?y), (?x t ?z)]" +
            "[r4: (?x s ?z) <- (?x r g(?y, ?z))]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, s, null), 
            new Object[] {
                new Triple(a, s, b),
                new Triple(a, s, d)
            } );
    }
    
    /**
     * Test basic functor usage.
     */
    public void testFunctors3() {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, s, b));
        data.add(new Triple(a, t, c));
        List<Rule> rules = Rule.parseRules(
            "[r1: (a q f(?x,?y)) <- (a s ?x), (a t ?y)]" +
            "[r2: (a p ?x) <- (a q ?x)]" +
            "[r3: (a r ?y) <- (a p f(?x, ?y))]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, r, null), 
            new Object[] {
                new Triple(a, r, c)
            } );
    }

    /**
     * Test basic builtin usage.
     */
    public void testBuiltin1() {
        Graph data = Factory.createGraphMem();
        List<Rule> rules = Rule.parseRules(
            "[a1: -> (a p 2) ]" +
            "[a2: -> (a q 3) ]" +
            "[r1: (?x r ?s) <- (?x p ?y), (?x q ?z), sum(?y, ?z, ?s)]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, r, null), 
            new Object[] {
                new Triple(a, r, Util.makeIntNode(5))
            } );
    }
   
    /**
     * Test basic builtin usage.
     */
    public void testBuiltin2() {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(a, q, c));
        List<Rule> rules = Rule.parseRules(
            "[r1: (?x r ?y ) <- bound(?x), (?x p ?y) ]" +
            "[r2: (?x r ?y) <- unbound(?x), (?x q ?y)]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, r, null), 
            new Object[] {
                new Triple(a, r, b)
            } );
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, r, null), 
            new Object[] {
                new Triple(a, r, c)
            } );
    }
   
    /**
     * Test basic builtin usage.
     */
    public void testBuiltin3() {
        Graph data = Factory.createGraphMem();
        List<Rule> rules = Rule.parseRules(
            "[r1: (a p b ) <- unbound(?x) ]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, null, null), 
            new Object[] {
                new Triple(a, p, b)
            } );
    }
  
    /**
     * Test basic ground head patterns.
     */
    public void testGroundHead() {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, r, b));
        List<Rule> rules = Rule.parseRules(
            "[r1: (a p b ) <- (a r b) ]"
        );
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, null, null), 
            new Object[] {
                new Triple(a, p, b),
                new Triple(a, r, b)
            } );
    }
  
//    /**
//     * Test multiheaded rule.
//     */
//    public void testMutliHead() {
//        Graph data = new GraphMem();
//        data.add(new Triple(a, p, b));
//        data.add(new Triple(b, r, c));
//        List<Rule> rules = Rule.parseRules(
//            "[r1: (?x s ?z), (?z s ?x) <- (?x p ?y) (?y r ?z) ]"
//        );
//        Reasoner reasoner =  createReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(null, s, null), 
//            new Object[] {
//                new Triple(a, s, c),
//                new Triple(c, s, a)
//            } );
//    }

    /**
     * Test rebind operation
     */
    public void testRebind() {
        List<Rule> rules = Rule.parseRules("[r1: (?a r ?c) <- (?a p ?b),(?b p ?c)]");        
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(b, p, c));
        data.add(new Triple(b, p, d));
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, r, null), 
            new Object[] {
                new Triple(a, r, c),
                new Triple(a, r, d)
            } );
        Graph ndata = Factory.createGraphMem();
        ndata.add(new Triple(a, p, d));
        ndata.add(new Triple(d, p, b));
        infgraph.rebind(ndata);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, r, null), 
            new Object[] {
                new Triple(a, r, b)
            } );

    }

    /**
     * Test troublesome rdfs rules
     */
    public void testRDFSProblemsb() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(C1, sC, C2));
        data.add(new Triple(C2, sC, C3));
        data.add(new Triple(C1, ty, RDFS.Class.asNode()));
        data.add(new Triple(C2, ty, RDFS.Class.asNode()));
        data.add(new Triple(C3, ty, RDFS.Class.asNode()));
        List<Rule> rules = Rule.parseRules(
        "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
        "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]"
                        );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, sC, null), 
            new Object[] {
                new Triple(C1, sC, C2),
                new Triple(C1, sC, C3),
                new Triple(C1, sC, C1),
                new Triple(C2, sC, C3),
                new Triple(C2, sC, C2),
                new Triple(C3, sC, C3),
            } );
    }

    /**
     * Test troublesome rdfs rules
     */
    public void testRDFSProblems() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(p, sP, q));
        data.add(new Triple(q, sP, r));
        data.add(new Triple(C1, sC, C2));
        data.add(new Triple(C2, sC, C3));
        data.add(new Triple(a, ty, C1));
        List<Rule> rules = Rule.parseRules(
        "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
        "[rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]" +
//        "[-> (rdf:type rdfs:range rdfs:Class)]" +
        "[rdfs3:  (?x ?p ?y), (?p rdfs:range ?c) -> (?y rdf:type ?c)]" +
        "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]"
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
    public void testHeadUnify() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(c, q, d));
        List<Rule> rules = Rule.parseRules(
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
            
        data = Factory.createGraphMem();
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
    public void testRestriction1() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, ty, r));
        data.add(new Triple(a, p, b));
        data.add(new Triple(r, sC, C1));
        data.add(new Triple(C1, OWL.onProperty.asNode(), p));
        data.add(new Triple(C1, OWL.allValuesFrom.asNode(), c));
        List<Rule> rules = Rule.parseRules(
    "[rdfs9:  (?x rdfs:subClassOf ?y) (?a rdf:type ?x) -> (?a rdf:type ?y)]" +
    "[restriction2: (?C owl:onProperty ?P), (?C owl:allValuesFrom ?D) -> (?C owl:equivalentClass all(?P, ?D))]" +
    "[rs2: (?D owl:equivalentClass all(?P,?C)), (?X rdf:type ?D) -> (?X rdf:type all(?P,?C))]" +
    "[rp4: (?X rdf:type all(?P, ?C)), (?X ?P ?Y) -> (?Y rdf:type ?C)]"
                          );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, c), new Object[] {
                  new Triple(b, ty, c)
              } );
    }
    

    /**
     * Test restriction example. The rules are more than the minimum required
     * to solve the query and they interact to given run away seaches if there
     * is a problem. 
     */
    public void testRestriction2() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, ty, OWL.Thing.asNode()));
        data.add(new Triple(p, ty, OWL.FunctionalProperty.asNode()));
        data.add(new Triple(c, OWL.equivalentClass.asNode(), C1));
        data.add(new Triple(C1, ty, OWL.Restriction.asNode()));
        data.add(new Triple(C1, OWL.onProperty.asNode(), p));
        data.add(new Triple(C1, OWL.maxCardinality.asNode(), Util.makeIntNode(1)));
        List<Rule> rules = Rule.parseRules(
        // these ones are required for the inference.
        "[rdfs9:  bound(?y)   (?x rdfs:subClassOf ?y) (?a rdf:type ?x) -> (?a rdf:type ?y)]" + 
        "[restriction4: (?C rdf:type owl:Restriction), (?C owl:onProperty ?P), (?C owl:maxCardinality ?X) -> (?C owl:equivalentClass max(?P, ?X))]" +
        "[restrictionProc11: (?P rdf:type owl:FunctionalProperty), (?X rdf:type owl:Thing) -> (?X rdf:type max(?P, 1))]" +
//        "[equivalentClass1: (?P owl:equivalentClass ?Q) -> (?P rdfs:subClassOf ?Q), (?Q rdfs:subClassOf ?P) ]" +
        "[equivalentClass1: (?P owl:equivalentClass ?Q) -> (?P rdfs:subClassOf ?Q) ]" +
        "[equivalentClass1: (?P owl:equivalentClass ?Q) -> (?Q rdfs:subClassOf ?P) ]" +
        "[restrictionSubclass1: bound(?D) (?D owl:equivalentClass ?R), isFunctor(?R) (?X rdf:type ?R)-> (?X rdf:type ?D)]" +
         // these ones are noise which can cause run aways or failures if there are bugs        
        "[rdfs8:  unbound(?c) (?a rdfs:subClassOf ?b) (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
        "[rdfs8:  bound(?c)   (?b rdfs:subClassOf ?c) (?a rdfs:subClassOf ?b) -> (?a rdfs:subClassOf ?c)]" + 
        "[rdfs9:  unbound(?y) (?a rdf:type ?x) (?x rdfs:subClassOf ?y) -> (?a rdf:type ?y)]" + 
        "[-> (rdf:type      rdfs:range rdfs:Class)]" +
        "[rdfs3:  bound(?c)   (?p rdfs:range ?c) (?x ?p ?y) -> (?y rdf:type ?c)]" + 
        "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]" +
        "[restrictionProc13: (owl:Thing rdfs:subClassOf all(?P, ?C)) -> (?P rdfs:range ?C)]" +
        "[restrictionSubclass1: unbound(?D) (?X rdf:type ?R), isFunctor(?R) (?D owl:equivalentClass ?R) -> (?X rdf:type ?D)]" +
        "[restrictionSubclass2: bound(?R), isFunctor(?R), (?D owl:equivalentClass ?R),(?X rdf:type ?D) -> (?X rdf:type ?R)]" +
        "[restrictionSubclass2: unbound(?R), (?X rdf:type ?D), (?D owl:equivalentClass ?R) isFunctor(?R) -> (?X rdf:type ?R)]" +
                       ""  );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(a, ty, C1), new Object[] {
                  new Triple(a, ty, C1)
              } );
        TestUtil.assertIteratorValues(this, 
              infgraph.find(a, ty, c), new Object[] {
                  new Triple(a, ty, c)
              } );
    }

    /**
     * Test restriction example
     */
    public void testRestriction3() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, ty, r));
        data.add(new Triple(r, sC, C1));
        data.add(new Triple(C1, ty, OWL.Restriction.asNode()));
        data.add(new Triple(C1, OWL.onProperty.asNode(), p));
        data.add(new Triple(C1, OWL.allValuesFrom.asNode(), c));
        List<Rule> rules = Rule.parseRules(
        "[-> (rdfs:subClassOf rdfs:range rdfs:Class)]" +
//        "[-> (owl:Class rdfs:subClassOf rdfs:Class)]" +
        "[rdfs3:  bound(?c)   (?p rdfs:range ?c) (?x ?p ?y) -> (?y rdf:type ?c)]" + 
        "[rdfs3:  unbound(?c) (?x ?p ?y), (?p rdfs:range ?c) -> (?y rdf:type ?c)]" +    
        "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]" +
        "[rdfs8:  (?a rdfs:subClassOf ?b) (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
        "[restrictionProc4b: bound(?Y) (?X ?P ?Y), notEqual(?P, rdf:type), (?X rdf:type all(?P, ?C)),-> (?Y rdf:type ?C)]" +
        "[restrictionProc4b: unbound(?Y), (?X rdf:type all(?P, ?C)), (?X ?P ?Y), notEqual(?P, rdf:type),-> (?Y rdf:type ?C)]" +
                       ""  );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, ty, c), new Object[] {
              } );
    }

    /**
     * Test close and halt operation.
     */
    public void testClose() {    
        Graph data = Factory.createGraphMem();
        data.add(new Triple(p, sP, q));
        data.add(new Triple(q, sP, r));
        data.add(new Triple(C1, sC, C2));
        data.add(new Triple(C2, sC, C3));
        data.add(new Triple(a, ty, C1));
        data.add(new Triple(ty, RDFS.range.asNode(), RDFS.Class.asNode()));
        List<Rule> rules = Rule.parseRules(
        "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
        "[rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]" +
//        "[-> (rdf:type rdfs:range rdfs:Class)]" +
        "[rdfs3:  (?x ?p ?y), (?p rdfs:range ?c) -> (?y rdf:type ?c)]" +
        "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]"
                        );        
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        // Get just one result
        ExtendedIterator<Triple> it = infgraph.find(a, ty, null);
        Triple result = it.next();
        assertEquals(result.getSubject(), a);
        assertEquals(result.getPredicate(), ty);
        it.close();
        // Make sure if we start again we get the full listing.
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, ty, null), 
            new Object[] {
                new Triple(a, ty, C1),
                new Triple(a, ty, C2),
                new Triple(a, ty, C3)
            } );
    }

    /**
     * Test problematic rdfs case
     */
    public void testBug1() {
        Graph data = Factory.createGraphMem();
        Node p = NodeFactory.createURI("http://www.hpl.hp.com/semweb/2003/eg#p");
        Node r = NodeFactory.createURI("http://www.hpl.hp.com/semweb/2003/eg#r");
        Node C1 = NodeFactory.createURI("http://www.hpl.hp.com/semweb/2003/eg#C1");
        data.add(new Triple(a, p, b));
        List<Rule> rules = Rule.parseRules(Util.loadRuleParserFromResourceFile("testing/reasoners/bugs/rdfs-error1.brules"));
        Reasoner reasoner =  createReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(b, ty, C1), 
            new Object[] {
                new Triple(b, ty, C1)
            } );
        
    }
    
}
