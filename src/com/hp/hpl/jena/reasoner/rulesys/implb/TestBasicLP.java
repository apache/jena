/******************************************************************
 * File:        TestBasicLP.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestBasicLP.java,v 1.6 2003-07-25 16:34:34 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Early test cases for the LP version of the backward chaining system.
 * <p>
 * To be moved to a test directory once the code is working.
 * </p>
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-07-25 16:34:34 $
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
    Node D1 = Node.createURI("D1");
    Node D2 = Node.createURI("D2");
    Node D3 = Node.createURI("D3");

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
//        suite.addTest(new TestBasicLP( "testBaseRules5" ));
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
     * Test backtracking - simple triple query.
     */
    public void testBacktrack1() {
        doTest("[r1: (?x r ?y) <- (?x p ?y)]",
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(a, p, c),
                    new Triple(a, p, d)
                },
                new Triple(a, p, Node.ANY),
                new Object[] {
                    new Triple(a, p, b),
                    new Triple(a, p, c),
                    new Triple(a, p, d)
                } );
    }
    
    /**
     * Test backtracking - chain to simple triple query.
     */
    public void testBacktrack2() {
        doTest("[r1: (?x r ?y) <- (?x p ?y)]",
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(a, p, c),
                    new Triple(a, p, d)
                },
                new Triple(a, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, b),
                    new Triple(a, r, c),
                    new Triple(a, r, d)
                } );
    }
    
    /**
     * Test backtracking - simple choice point
     */
    public void testBacktrack3() {
        doTest("[r1: (?x r C1) <- (?x p b)]" +
               "[r2: (?x r C2) <- (?x p b)]" +
               "[r3: (?x r C3) <- (?x p b)]",
                new Triple[] {
                    new Triple(a, p, b)
                },
                new Triple(a, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, C1),
                    new Triple(a, r, C2),
                    new Triple(a, r, C3)
                } );
    }
    
    /**
     * Test backtracking - nested choice point
     */
    public void testBacktrack4() {
        doTest("[r1: (?x r C1) <- (?x p b)]" +
               "[r2: (?x r C2) <- (?x p b)]" +
               "[r3: (?x r C3) <- (?x p b)]" +
               "[r4: (?x s ?z) <- (?x p ?w), (?x r ?y) (?y p ?z)]",
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(C1, p, D1),
                    new Triple(C2, p, D2),
                    new Triple(C3, p, D3)
                },
                new Triple(a, s, Node.ANY),
                new Object[] {
                    new Triple(a, s, D1),
                    new Triple(a, s, D2),
                    new Triple(a, s, D3)
                } );
    }
    
    /**
     * Test backtracking - nested choice point with multiple triple matches
     */
    public void testBacktrack5() {
        doTest("[r1: (?x r C3) <- (C1 p ?x)]" +
               "[r2: (?x r C2) <- (C2 p ?x)]" +
               "[r4: (?x s ?y) <- (?x r ?y)]",
                new Triple[] {
                    new Triple(C1, p, D1),
                    new Triple(C1, p, a),
                    new Triple(C2, p, D2),
                    new Triple(C2, p, b)
                },
                new Triple(Node.ANY, s, Node.ANY),
                new Object[] {
                    new Triple(D1, s, C3),
                    new Triple(a, s, C3),
                    new Triple(D2, s, C2),
                    new Triple(b, s, C2)
                } );
    }
    
    /**
     * Test backtracking - nested choice point with multiple triple matches, and
     * checking temp v. permanent variable usage
     */
    public void testBacktrack6() {
        doTest("[r1: (?x r C1) <- (?x p a)]" +
               "[r2: (?x r C2) <- (?x p b)]" +
               "[r3: (?x q C1) <- (?x p b)]" +
               "[r4: (?x q C2) <- (?x p a)]" +
               "[r5: (?x s ?y) <- (?x r ?y) (?x q ?y)]",
                new Triple[] {
                    new Triple(D1, p, a),
                    new Triple(D2, p, a),
                    new Triple(D2, p, b),
                    new Triple(D3, p, b)
                },
                new Triple(Node.ANY, s, Node.ANY),
                new Object[] {
                    new Triple(D2, s, C1),
                    new Triple(D2, s, C2),
                } );
    }
    
    /**
     * Test backtracking - nested choice point with simple triple matches
     */
    public void testBacktrack7() {
        doTest( "[r1: (?x r C1) <- (?x p b)]" +
                "[r2: (?x r C2) <- (?x p b)]" +
                "[r3: (?x r C3) <- (?x p b)]" +
                "[r3: (?x r D1) <- (?x p b)]" +
                "[r4: (?x q C2) <- (?x p b)]" +
                "[r5: (?x q C3) <- (?x p b)]" +
                "[r5: (?x q D1) <- (?x p b)]" +
                "[r6: (?x t C1) <- (?x p b)]" +
                "[r7: (?x t C2) <- (?x p b)]" +
                "[r8: (?x t C3) <- (?x p b)]" +
                "[r9: (?x s ?y) <- (?x r ?y) (?x q ?y) (?x t ?y)]",
                new Triple[] {
                    new Triple(a, p, b),
                },
                new Triple(Node.ANY, s, Node.ANY),
                new Object[] {
                    new Triple(a, s, C2),
                    new Triple(a, s, C3),
                } );
    }
    
    /**
     * Test backtracking - nested choice point with simple triple matches,
     * permanent vars but used just once in body
     */
    public void testBacktrack8() {
        doTest( "[r1: (?x r C1) <- (?x p b)]" +
                "[r2: (?x r C2) <- (?x p b)]" +
                "[r3: (?x r C3) <- (?x p b)]" +
                "[r3: (?x r D1) <- (?x p b)]" +
                "[r4: (?x q C2) <- (?x p b)]" +
                "[r5: (?x q C3) <- (?x p b)]" +
                "[r5: (?x q D1) <- (?x p b)]" +
                "[r6: (?x t C1) <- (?x p b)]" +
                "[r7: (?x t C2) <- (?x p b)]" +
                "[r8: (?x t C3) <- (?x p b)]" +
                "[r9: (?x s ?y) <- (?w r C1) (?x q ?y) (?w t C1)]",
                new Triple[] {
                    new Triple(a, p, b),
                },
                new Triple(Node.ANY, s, Node.ANY),
                new Object[] {
                    new Triple(a, s, D1),
                    new Triple(a, s, C2),
                    new Triple(a, s, C3),
                } );
    }
   
    /**
     * Test backtracking - multiple triple matches
     */
    public void testBacktrack9() {
        doTest("[r1: (?x s ?y) <- (?x r ?y) (?x q ?y)]",
                new Triple[] {
                    new Triple(a, r, D1),
                    new Triple(a, r, D2),
                    new Triple(a, r, D3),
                    new Triple(b, r, D2),
                    new Triple(a, q, D2),
                    new Triple(b, q, D2),
                    new Triple(b, q, D3),
                },
                new Triple(Node.ANY, s, Node.ANY),
                new Object[] {
                    new Triple(a, s, D2),
                    new Triple(b, s, D2),
                } );
    }
    
    /**
     * Test clause order is right
     */
    public void testClauseOrder() {
        LPRuleStore store = new LPRuleStore();
        List rules = Rule.parseRules(
            "[r1: (?x r C1) <- (?x p b)]" +
            "[r1: (?x r C2) <- (?x p b)]" +
            "[r2: (?x r C3) <- (?x r C3) (?x p b)]");
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            store.addRule((Rule)i.next());
        }
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        InfGraph infgraph =  new LPBackwardRuleInfGraph(null, store, data, null);
        ExtendedIterator i = infgraph.find(Node.ANY, r, Node.ANY);
        assertTrue(i.hasNext());
        assertEquals(i.next(), new Triple(a, r, C1));
        i.close();
    }
    
    /**
     * Test axioms work.
     */
    public void testAxioms() {
        doTest("[a1: -> (a r C1) ]" +
               "[a2: -> (a r C2) ]" +
               "[a3: (b r C1) <- ]" +
               "[r1: (?x s ?y) <- (?x r ?y)]",
                new Triple[] {
                },
                new Triple(Node.ANY, s, Node.ANY),
                new Object[] {
                    new Triple(a, s, C1),
                    new Triple(a, s, C2),
                    new Triple(b, s, C1),
                } );
    }

    /**
     * Test nested invocate of rules with permananet vars
     */
    public void testNestedPvars() {
        doTest("[r1: (?x r ?y) <- (?x p ?z) (?z q ?y)]" +
               "[r1: (?y t ?x) <- (?x p ?z) (?z q ?y)]" +
               "[r3: (?x s ?y) <- (?x r ?y) (?y t ?x)]",
                new Triple[] {
                    new Triple(a, p, C1),
                    new Triple(a, p, C2),
                    new Triple(a, p, C3),
                    new Triple(C2, q, b),
                    new Triple(C3, q, c),
                    new Triple(D1, q, D2),
                },
                new Triple(Node.ANY, s, Node.ANY),
                new Object[] {
                    new Triple(a, s, b),
                    new Triple(a, s, c),
                } );
    }
    
    /**
     * Test simple invocation of a builtin
     */
    public void testBuiltin1() {
        doTest("[r1: (?x r ?y) <- (?x p ?v), sum(?v 2 ?y)]",
                new Triple[] {
                    new Triple(a, p, Util.makeIntNode(3)),
                    new Triple(b, p, Util.makeIntNode(4))
                },
                new Triple(Node.ANY, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, Util.makeIntNode(5)),
                    new Triple(b, r, Util.makeIntNode(6)),
                } );
    }
    
    /** 
     * Generic test operation.
     * @param ruleSrc the source of the rules
     * @param triples a set of triples to insert in the graph before the query
     * @param query the TripleMatch to search for
     * @param results the array of expected results
     */
    private void doTest(String ruleSrc, Triple[] triples, TripleMatch query, Object[] results) {
        LPRuleStore store = new LPRuleStore();
        List rules = Rule.parseRules(ruleSrc);
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            store.addRule((Rule)i.next());
        }
        Graph data = new GraphMem();
        for (int i = 0; i < triples.length; i++) {
            data.add(triples[i]);
        }
        InfGraph infgraph =  new LPBackwardRuleInfGraph(null, store, data, null);
        TestUtil.assertIteratorValues(this, infgraph.find(query), results); 

    }
    
    /** 
     * Generic base test operation on a graph with the single triple (a, p, b)
     * @param ruleSrc the source of the rules
     * @param query the TripleMatch to search for
     * @param results the array of expected results
     */
    private void doBasicTest(String ruleSrc, TripleMatch query, Object[] results) {
        doTest(ruleSrc, new Triple[]{new Triple(a,p,b)}, query, results);
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