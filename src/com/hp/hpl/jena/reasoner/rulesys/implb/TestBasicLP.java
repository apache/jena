/******************************************************************
 * File:        TestBasicLP.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestBasicLP.java,v 1.1 2003-07-22 16:41:42 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Early test cases for the LP version of the backward chaining system.
 * <p>
 * To be moved to a test directory once the code is working.
 * </p>
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-07-22 16:41:42 $
 */
public class TestBasicLP  extends TestCase {
    
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

    /**
     * Boilerplate for junit
     */ 
    public TestBasicLP( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestBasicLP.class ); 
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestBasicLP( "testNamed" ));
//        return suite;
    }  
   
    /**
     * Test basic rule operations - lookup, no matching rules
     */
    public void testBaseRules1() {    
        doBasicTest("[r1: (?x r c) <- (?x p b)]", 
                     new Triple(Node.ANY, p, b),
                     new Object[] {
                        new Triple(a, p, b)
                     } );
    }
   
    /**
     * Test basic rule operations - simple chain rule
     */
    public void testBaseRules2() {    
        doBasicTest("[r1: (?x r c) <- (?x p b)]", 
                     new Triple(Node.ANY, r, c),
                     new Object[] {
                        new Triple(a, r, c)
                     } );
    }
   
    /**
     * Test basic rule operations - chain rule with head unification
     */
    public void testBaseRules3() {    
        doBasicTest("[r1: (?x r ?x) <- (?x p b)]", 
                     new Triple(Node.ANY, r, a),
                     new Object[] {
                        new Triple(a, r, a)
                     } );
    }
    
    /**
     * Test basic rule operations - rule with head unification, non-temp var
     */
    public void testBaseRules4() {    
        doBasicTest("[r1: (?x r ?x) <- (?y p b), (?x p b)]", 
                     new Triple(Node.ANY, r, a),
                     new Object[] {
                        new Triple(a, r, a)
                     } );
    }
    
    /**
     * Test basic rule operations - simple cascade
     */
    public void testBaseRules5() {    
        doBasicTest("[r1: (?x q ?y) <- (?x r ?y)(?y s ?x)]" +
                    "[r2: (?x r ?y) <- (?x p ?y)]" + 
                    "[r3: (?x s ?y) <- (?y p ?x)]", 
                     new Triple(Node.ANY, q, Node.ANY),
                     new Object[] {
                        new Triple(a, q, b)
                     } );
    }
   
    /**
     * Test basic rule operations - chain rule which will fail at head time
     */
    public void testBaseRules6() {    
        doBasicTest("[r1: (?x r ?x) <- (?x p b)]", 
                     new Triple(a, r, b),
                     new Object[] {
                     } );
    }
   
    /**
     * Test basic rule operations - chain rule which will fail in search
     */
    public void testBaseRules7() {    
        doBasicTest("[r1: (?x r ?y) <- (?x p c)]", 
                     new Triple(a, r, b),
                     new Object[] {
                     } );
    }
    
    /**
     * Test basic rule operations - simple chain
     */
    public void testBaseRules8() {    
        doBasicTest("[r1: (?x q ?y) <- (?x r ?y)]" +
                    "[r2: (?x r ?y) <- (?x p ?y)]", 
                     new Triple(Node.ANY, q, Node.ANY),
                     new Object[] {
                        new Triple(a, q, b)
                     } );
    }
    
    /**
     * Test basic rule operations - simple chain
     */
    public void testBaseRules9() {    
        doBasicTest("[r1: (?x q ?y) <- (?x r ?y)]" +
                    "[r2: (?x r ?y) <- (?y p ?x)]", 
                     new Triple(Node.ANY, q, Node.ANY),
                     new Object[] {
                        new Triple(b, q, a)
                     } );
    }
    
    /** 
     * Generic base test operation on a graph with the single triple (a, p, b)
     * @param ruleSrc the source of the rules
     * @param query the TripleMatch to search for
     * @param results the array of expected results
     */
    private void doBasicTest(String ruleSrc, TripleMatch query, Object[] results) {
        LPRuleStore store = new LPRuleStore();
        List rules = Rule.parseRules(ruleSrc);
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            store.addRule((Rule)i.next());
        }
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        InfGraph infgraph =  new LPBackwardRuleInfGraph(null, store, data, null);
        TestUtil.assertIteratorValues(this, infgraph.find(query), results); 

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