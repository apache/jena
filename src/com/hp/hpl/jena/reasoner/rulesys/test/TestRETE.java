/******************************************************************
 * File:        TestRETE.java
 * Created by:  Dave Reynolds
 * Created on:  10-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestRETE.java,v 1.10 2005-02-21 12:18:14 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.10 $ on $Date: 2005-02-21 12:18:14 $
 */
public class TestRETE  extends TestCase {
     
    // Useful constants
    Node_RuleVariable x = new Node_RuleVariable("x", 0);
    Node_RuleVariable y = new Node_RuleVariable("y", 1);
    Node_RuleVariable z = new Node_RuleVariable("z", 2);
    Node p = Node.createURI("p");
    Node q = Node.createURI("q");
    Node a = Node.createURI("a");
    Node b = Node.createURI("b");
    Node c = Node.createURI("c");
    Node d = Node.createURI("d");
    Node e = Node.createURI("e");
    Node r = Node.createURI("r");
    Node s = Node.createURI("s");
    Node n1 = Node.createURI("n1");
    Node n2 = Node.createURI("n2");
    Node n3 = Node.createURI("n3");
    Node n4 = Node.createURI("n4");
    Node res = Node.createURI("res");
         
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
        RETEClauseFilter cf = RETEClauseFilter.compile(pattern, 3, new LinkedList());
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
        public RETENode clone(Map netCopy, RETERuleContext context) {
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
        List ruleList = Rule.parseRules(rules);
        BasicForwardRuleInfGraph infgraph = new BasicForwardRuleInfGraph(null, new ArrayList(), null, new GraphMem());
//        infgraph.setTraceOn(true);
        RETEEngine engine = new RETEEngine(infgraph, ruleList);
        infgraph.prepare();
        engine.init(true, new FGraph(new GraphMem()));
        for (int i = 0; i < adds.length; i++) {
            engine.addTriple(adds[i], true);
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
        List ruleList = Rule.parseRules(rules);
        Graph schema = new GraphMem();
        schema.add(new Triple(a, q, c));
        schema.add(new Triple(a, q, d));

        Graph data1 = new GraphMem();
        data1.add(new Triple(b, q, c));
        
        Graph data2 = new GraphMem();
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


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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