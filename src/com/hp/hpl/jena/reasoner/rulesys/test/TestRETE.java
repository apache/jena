/******************************************************************
 * File:        TestRETE.java
 * Created by:  Dave Reynolds
 * Created on:  10-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestRETE.java,v 1.2 2003-06-10 17:14:54 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-06-10 17:14:54 $
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
    }

    /**
     * Test the join nodes.
     */
    public void testJoins() {
        doTestJoin( "(a p ?x), (?x p c) -> (?x q ?x).", 
                     new Triple[] {
                         new Triple(a, p, b), 
                         new Triple(b, p, c)},
                     new Node[] {b} );
        doTestJoin( "(a p ?x), (?x p c) -> (?x q ?x).", 
                     new Triple[] {
                         new Triple(a, p, b), 
                         new Triple(a, p, c), 
                         new Triple(a, p, d), 
                         new Triple(b, p, c)},
                     new Node[] {b} );
        doTestJoin( "(a p ?x) -> (?x q ?x).", 
                     new Triple[] {
                         new Triple(a, p, b), 
                         new Triple(b, p, c)},
                     new Node[] {b} );
        doTestJoin( "(a p ?x), (?x p ?y), (?x q ?y) -> (?x r ?y).", 
                     new Triple[] {
                         new Triple(a, p, b), 
                         new Triple(b, p, c), 
                         new Triple(b, q, c)}, 
                     new Node[] {b, c} );
    }

    /**
     * Helper for testing clause joins.
     */
    private void doTestJoin(String ruleSource, Triple[] test, Node[] expected) {
        Rule rule = Rule.parseRule(ruleSource);
        RETETestNode tnode = new RETETestNode();
        RETEEngine engine = new RETEEngine(null);
        engine.compileRule(rule).setContinuation(tnode);
        for (int i = 0; i < test.length; i++) {
            engine.testTripleInsert(test[i]);
        }
        if (expected == null) {
            assertTrue(tnode.firings == 0);
        } else {
            assertTrue(tnode.firings > 0);
            assertTrue(tnode.isAdd);
            assertEquals(new BindingVector(expected), tnode.env);
        }
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