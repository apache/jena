/******************************************************************
 * File:        TestTrail.java
 * Created by:  Dave Reynolds
 * Created on:  20-May-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestTrail.java,v 1.4 2005-02-21 12:18:06 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl.oldCode;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *  Test harness for the prototype binding trail implementation.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2005-02-21 12:18:06 $
 */
public class TestTrail extends TestCase {

    Node a = Node.createURI("a");
    Node b = Node.createURI("b");
    Node c = Node.createURI("c");
    Node p = Node.createURI("p");
    Node q = Node.createURI("q");
    
    /**
     * Boilerplate for junit
     */ 
    public TestTrail( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestTrail.class ); 
    }  

    /**
     * Test unification support.
     */
    public void testUnify() {
        Node_RuleVariable X = new Node_RuleVariable("x", 0);
        Node_RuleVariable Y = new Node_RuleVariable("y", 1);
        Node_RuleVariable Z = new Node_RuleVariable("z", 2);
 
        Trail trail = new Trail();
        assertTrue(trail.unify(new TriplePattern(X, p, Y), new TriplePattern(a, p, b)));
        assertEquals(X.deref(), a);
        assertEquals(Y.deref(), b);
        assertTrue(Z.isUnbound());
        trail.unwindAndClear();
        assertTrue(X.isUnbound());
        assertTrue(Y.isUnbound());
        
        assertTrue(trail.unify(new TriplePattern(X, p, X), new TriplePattern(Z, p, a)));
        assertEquals(X.deref(), a);
        assertEquals(Z.deref(), a);
        trail.unwindAndClear();
        
        TriplePattern gf = new TriplePattern(X, p, 
                                Functor.makeFunctorNode("f", new Node[]{X, b}));
        TriplePattern hf1 = new TriplePattern(Y, p, 
                                Functor.makeFunctorNode("f", new Node[]{Z, b}));
        TriplePattern hf2 = new TriplePattern(Y, p, 
                                Functor.makeFunctorNode("f", new Node[]{a, Y}));
        TriplePattern hf3 = new TriplePattern(Y, p, 
                                Functor.makeFunctorNode("f", new Node[]{b, Y}));
        assertTrue(trail.unify(gf, hf1));
        assertEquals(X.deref(), Y.deref());
        assertEquals(X.deref(), Z.deref());
        trail.unwindAndClear();

        assertTrue(! trail.unify(gf, hf2));
        assertTrue(X.isUnbound());
        assertTrue(Y.isUnbound());
        assertTrue(Z.isUnbound());
        trail.unwindAndClear();

        assertTrue(trail.unify(gf, hf3));
        assertEquals(X.deref(), b);
        assertEquals(Y.deref(), b);
        trail.unwindAndClear();
        
    }
    
    /**
     * Check a few triple pattern invariants. These are not directly
     * part of the trail system but the trail machinery depends on them.
     */
    public void testMatching() {
        Node_RuleVariable X = new Node_RuleVariable("x", 0);
        Node_RuleVariable Y = new Node_RuleVariable("y", 1);
        Node_RuleVariable Z = new Node_RuleVariable("z", 2);
        Node_RuleVariable X1 = new Node_RuleVariable("x1", 0);
        Node_RuleVariable Y1 = new Node_RuleVariable("y1", 1);
        Node_RuleVariable Z1 = new Node_RuleVariable("z1", 2);
 
        assertTrue(X.sameValueAs(Y));
        TriplePattern f1 = new TriplePattern(X, p, 
                                Functor.makeFunctorNode("f", new Node[]{X, b}));
        TriplePattern f2 = new TriplePattern(Y, p, 
                                Functor.makeFunctorNode("f", new Node[]{Z, b}));
        TriplePattern f3 = new TriplePattern(Y1, p, 
                                Functor.makeFunctorNode("f", new Node[]{Y1, b}));
        TriplePattern f4 = new TriplePattern(X1, p, 
                                Functor.makeFunctorNode("f", new Node[]{Z1, b}));
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertTrue(f1.variantOf(f3));
        assertTrue(f2.variantOf(f4));
        assertTrue( ! f1.variantOf(f2));
        assertTrue( ! f3.variantOf(f4));
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