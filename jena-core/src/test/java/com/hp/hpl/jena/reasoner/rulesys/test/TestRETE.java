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

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestRETE  extends TestCase {
     
    // Useful constants
    Node_RuleVariable x = new Node_RuleVariable("x", 0);
    Node_RuleVariable y = new Node_RuleVariable("y", 1);
    Node_RuleVariable z = new Node_RuleVariable("z", 2);
    Node p = NodeFactory.createURI("p");
    Node q = NodeFactory.createURI("q");
    Node a = NodeFactory.createURI("a");
    Node b = NodeFactory.createURI("b");
    Node c = NodeFactory.createURI("c");
    Node d = NodeFactory.createURI("d");
    Node e = NodeFactory.createURI("e");
    Node r = NodeFactory.createURI("r");
    Node s = NodeFactory.createURI("s");
    Node n1 = NodeFactory.createURI("n1");
    Node n2 = NodeFactory.createURI("n2");
    Node n3 = NodeFactory.createURI("n3");
    Node n4 = NodeFactory.createURI("n4");
    Node res = NodeFactory.createURI("res");
         
    /**
     * Boilerplate for junit
     */ 
    public TestRETE( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestRETE.class ); 
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestRETE( "foo" ));
//        return suite;
    }  

    /**
     * Test clause compiler and clause filter implementation.
     */
    public void testClauseFilter() {
        doTestClauseFilter( new TriplePattern(a, p, x), 
                            new Triple(a, p, b), new Node[]{b, null, null});
        doTestClauseFilter( new TriplePattern(x, p, b), 
                            new Triple(a, p, b), new Node[]{a, null, null});
        doTestClauseFilter( new TriplePattern(a, p, x), new Triple(b, p, a), null);
        doTestClauseFilter( new TriplePattern(a, p, x), new Triple(a, q, a), null);
        doTestClauseFilter( new TriplePattern(x, p, x), 
                            new Triple(a, p, a), new Node[]{a, null, null});
        doTestClauseFilter( new TriplePattern(x, p, x), new Triple(a, p, b), null);
        doTestClauseFilter( 
            new TriplePattern(a, p, Functor.makeFunctorNode("f", new Node[]{x, c})), 
            new Triple(a, p, a), 
            null);
        doTestClauseFilter( 
            new TriplePattern(a, p, x), 
            new Triple(a, p, Functor.makeFunctorNode("f", new Node[]{b, c})), 
            new Node[]{Functor.makeFunctorNode("f", new Node[]{b, c}), null, null});
        doTestClauseFilter( 
            new TriplePattern(a, p, Functor.makeFunctorNode("g", new Node[]{x, c})), 
            new Triple(a, p, Functor.makeFunctorNode("f", new Node[]{b, c})), 
            null);
        doTestClauseFilter( 
            new TriplePattern(a, p, Functor.makeFunctorNode("f", new Node[]{x, c})), 
            new Triple(a, p, Functor.makeFunctorNode("f", new Node[]{b, c})), 
            new Node[] {b, null, null});
        doTestClauseFilter( 
            new TriplePattern(x, p, Functor.makeFunctorNode("f", new Node[]{x, c})), 
            new Triple(a, p, Functor.makeFunctorNode("f", new Node[]{a, c})), 
            new Node[] {a, null, null});
        doTestClauseFilter( 
            new TriplePattern(x, p, Functor.makeFunctorNode("f", new Node[]{x, c})), 
            new Triple(a, p, Functor.makeFunctorNode("f", new Node[]{b, c})), 
            null);
    }

    /**
     * Helper for testing clause filters.
     */
    private void doTestClauseFilter(TriplePattern pattern, Triple test, Node[] expected) {
        RETETestNode tnode = new RETETestNode();
        RETEClauseFilter cf = RETEClauseFilter.compile(pattern, 3, new LinkedList<Node>());
        cf.setContinuation(tnode);
        cf.fire(test, true);
        if (expected == null) {
            assertTrue(tnode.firings == 0);
        } else {
            assertTrue(tnode.firings == 1);
            assertTrue(tnode.isAdd);
            assertEquals(new BindingVector(expected), tnode.env);
        }
    }
    
    /**
     * Inner class usable as a dummy RETENode end point for testing.
     */
    protected static class RETETestNode implements RETESinkNode {
        /** The environment passed in */
        BindingVector env;
        
        /** The mode flag */
        boolean isAdd;
        
        /** True if the fire has been called */
        int firings = 0;

        /** 
         * Propagate a token to this node.
         * @param env a set of variable bindings for the rule being processed. 
         * @param isAdd distinguishes between add and remove operations.
         */
        @Override
        public void fire(BindingVector env, boolean isAdd) {
            firings++;
            this.env = env;
            this.isAdd = isAdd;
        }
        
        /**
         * Clone this node in the network across to a different context.
         * @param netCopy a map from RETENodes to cloned instance so far.
         * @param context the new context to which the network is being ported
         */
        @Override
        public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
            // Dummy, not used in testing
            return this;
        }
        
    }
      
    /**
     * Minimal rule tester to check basic pattern match.
     */
    public void testRuleMatcher() {
        doRuleTest( "[r1: (?a p ?b), (?b q ?c) -> (?a, q, ?c)]" +
                       "[r2: (?a p ?b), (?b p ?c) -> (?a, p, ?c)]" +
                       "[r3: (?a p ?a), (n1 p ?c), (n1, p, ?a) -> (?a, p, ?c)]" +
                       "[r4: (n4 ?p ?a) -> (n4, ?a, ?p)]",
                    new Triple[] {
                        new Triple(n1, p, n2),
                        new Triple(n2, p, n3),
                        new Triple(n2, q, n3),
                        new Triple(n4, p, n4) },
                    new Triple[] {
                        new Triple(n1, p, n2),
                        new Triple(n2, p, n3),
                        new Triple(n2, q, n3),
                        new Triple(n4, p, n4),
                        new Triple(n1, p, n3),
                        new Triple(n1, q, n3),
                        new Triple(n4, n4, p),
                    });
                    
        doRuleTest( "[testRule1: (n1 p ?a) -> (n2, p, ?a)]" +
                        "[testRule2: (n1 q ?a) -> (n2, q, ?a)]" +
                        "[testRule3: (n2 p ?a), (n2 q ?a) -> (res p ?a)]" +
                        "[axiom1: -> (n1 p n3)]",
                     new Triple[] {},
                     new Triple[] {
                         new Triple(n1, p, n3),
                         new Triple(n2, p, n3)
                     });
        
        doRuleTest( "[testRule1: (n1 p ?a) -> (n2, p, ?a)]" +
                        "[testRule2: (n1 q ?a) -> (n2, q, ?a)]" +
                        "[testRule3: (n2 p ?a), (n2 q ?a) -> (res p ?a)]" +
                        "[axiom1: -> (n1 p n3)]",
                     new Triple[] {
                         new Triple(n1, q, n4),
                         new Triple(n1, q, n3)
                     },
                     new Triple[] {
                         new Triple(n1, p, n3),
                         new Triple(n2, p, n3),
                         new Triple(n1, q, n4),
                         new Triple(n2, q, n4),
                         new Triple(n1, q, n3),
                         new Triple(n2, q, n3),
                         new Triple(res, p, n3)
                     });
        doRuleTest( "[rule1: (?x p ?y), (?x q ?y) -> remove(0)]",
                     new Triple[] {
                         new Triple(n1, p, Util.makeIntNode(1)),
                         new Triple(n1, p, Util.makeIntNode(2)),
                         new Triple(n1, q, Util.makeIntNode(2))
                     },
                     new Triple[] {
                         new Triple(n1, p, Util.makeIntNode(1)),
                         new Triple(n1, q, Util.makeIntNode(2))
                     });
    }

    /**
     * Perform a rule test on the raw RETE engine. This requires some fiddling
     * with dummy parent graphs.
     */
    private void doRuleTest(String rules, Triple[] adds, Triple[] expected) {
        List<Rule> ruleList = Rule.parseRules(rules);
        BasicForwardRuleInfGraph infgraph = new BasicForwardRuleInfGraph(null, new ArrayList<Rule>(), null, Factory.createGraphMem());
//        infgraph.setTraceOn(true);
        RETEEngine engine = new RETEEngine(infgraph, ruleList);
        infgraph.prepare();
        engine.init(true, new FGraph(Factory.createGraphMem()));
        for ( Triple add : adds )
        {
            engine.addTriple( add, true );
        }
        engine.runAll();
        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null), expected);
    }
    
    /**
     * Check that the rulestate cloning keeps two descendent graphs independent.
     * 
     */
    public void testRuleClone() {
        String rules = "[testRule1: (a p ?x) (b p ?x) -> (n1 p ?x) ]" +
                       "[testRule2: (?x q ?y) -> (?x p ?y)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        Graph schema = Factory.createGraphMem();
        schema.add(new Triple(a, q, c));
        schema.add(new Triple(a, q, d));

        Graph data1 = Factory.createGraphMem();
        data1.add(new Triple(b, q, c));
        
        Graph data2 = Factory.createGraphMem();
        data2.add(new Triple(b, q, d));
        
        GenericRuleReasoner reasoner =  new GenericRuleReasoner(ruleList);
        reasoner.setMode(GenericRuleReasoner.FORWARD_RETE);
        Reasoner boundReasoner = reasoner.bindSchema(schema);
        InfGraph infgraph1 = boundReasoner.bind(data1);
        InfGraph infgraph2 = boundReasoner.bind(data2);

        TestUtil.assertIteratorValues(this, infgraph1.find(null, p, null),
            new Triple[] {
                new Triple(a, p, c),
                new Triple(a, p, d),
                new Triple(b, p, c),
                new Triple(n1, p, c)
            });

        TestUtil.assertIteratorValues(this, infgraph2.find(null, p, null),
            new Triple[] {
                new Triple(a, p, c),
                new Triple(a, p, d),
                new Triple(b, p, d),
                new Triple(n1, p, d)
            });
    }
}
