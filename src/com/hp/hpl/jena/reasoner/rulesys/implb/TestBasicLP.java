/******************************************************************
 * File:        TestBasicLP.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestBasicLP.java,v 1.17 2003-08-12 17:01:27 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Early test cases for the LP version of the backward chaining system.
 * <p>
 * To be moved to a test directory once the code is working.
 * </p>
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.17 $ on $Date: 2003-08-12 17:01:27 $
 */
public class TestBasicLP  extends TestCase {
    
    // Useful constants
    Node p = Node.createURI("p");
    Node q = Node.createURI("q");
    Node r = Node.createURI("r");
    Node s = Node.createURI("s");
    Node t = Node.createURI("t");
    Node u = Node.createURI("u");
    Node a = Node.createURI("a");
    Node b = Node.createURI("b");
    Node c = Node.createURI("c");
    Node d = Node.createURI("d");
    Node e = Node.createURI("e");
    Node C1 = Node.createURI("C1");
    Node C2 = Node.createURI("C2");
    Node C3 = Node.createURI("C3");
    Node C4 = Node.createURI("C4");
    Node D1 = Node.createURI("D1");
    Node D2 = Node.createURI("D2");
    Node D3 = Node.createURI("D3");
    Node sP = RDFS.subPropertyOf.getNode();
    Node sC = RDFS.subClassOf.getNode();
    Node ty = RDF.type.getNode();

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
//        return new TestSuite( TestBasicLP.class );
        
        TestSuite suite = new TestSuite();
        suite.addTest(new TestBasicLP( "testProblem7" ));
        return suite;
    }  
   
    /**
     * Return an inference graph working over the given rule set and raw data.
     * Can be overridden by subclasses of this test class.
     * @param rules the rule set to use
     * @param data the graph of triples to process
     */
    public InfGraph makeInfGraph(List rules, Graph data) {
        FBLPRuleReasoner reasoner = new FBLPRuleReasoner(rules);
        FBLPRuleInfGraph infgraph = (FBLPRuleInfGraph) reasoner.bind(data);
        return infgraph;
    }
   
    /**
     * Return an inference graph working over the given rule set and raw data.
     * Can be overridden by subclasses of this test class.
     * @param rules the rule set to use
     * @param data the graph of triples to process
     * @param tabled an array of predicates that should be tabled
     */
    public InfGraph makeInfGraph(List rules, Graph data, Node[] tabled) {
        FBLPRuleReasoner reasoner = new FBLPRuleReasoner(rules);
        FBLPRuleInfGraph infgraph = (FBLPRuleInfGraph) reasoner.bind(data);
        for (int i = 0; i < tabled.length; i++) {
            infgraph.setTabled(tabled[i]);
        }
        return infgraph;
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
        List rules = Rule.parseRules(
            "[r1: (?x r C1) <- (?x p b)]" +
            "[r1: (?x r C2) <- (?x p b)]" +
            "[r2: (?x r C3) <- (?x r C3) (?x p b)]");
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        InfGraph infgraph =  makeInfGraph(rules, data);
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
     * Test nested invocation of rules with permanent vars
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
     * Test simple invocation of a builtin
     */
    public void testBuiltin2() {
        doTest("[r1: (?x r C1) <- (?x p ?v), lessThan(?v 3)]",
                new Triple[] {
                    new Triple(a, p, Util.makeIntNode(1)),
                    new Triple(b, p, Util.makeIntNode(2)),
                    new Triple(c, p, Util.makeIntNode(3))
                },
                new Triple(Node.ANY, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, C1),
                    new Triple(b, r, C1),
                } );
    }
    
    /**
     * Test wildcard predicate usage - simple triple search.
     * Rules look odd because we have to hack around the recursive loops.
     */
    public void testWildPredicate1() {
        doTest("[r1: (b r ?y) <- (a ?y ?v)]",
                new Triple[] {
                    new Triple(a, p, C1),
                    new Triple(a, q, C2),
                    new Triple(a, q, C3),
                },
                new Triple(b, r, Node.ANY),
                new Object[] {
                    new Triple(b, r, p),
                    new Triple(b, r, q)
                } );
    }
    
    /**
     * Test wildcard predicate usage - combind triple search and multiclause matching.
     * Rules look odd because we have to hack around the recursive loops.
     */
    public void testWildPredicate2() {
        doTest("[r1: (a r ?y) <- (b ?y ?v)]" +
                "[r2: (?x q ?y) <- (?x p ?y)]" +
                "[r3: (?x s C1) <- (?x p C1)]" +
                "[r4: (?x t C2) <- (?x p C2)]",
                new Triple[] {
                    new Triple(b, p, C1),
                    new Triple(b, q, C2),
                    new Triple(b, q, C3),
                    new Triple(a, p, C1),
                    new Triple(a, p, C2),
                    new Triple(c, p, C1),
                },
                new Triple(a, Node.ANY, Node.ANY),
                new Object[] {
                    new Triple(a, r, p),
                    new Triple(a, r, q),
                    new Triple(a, q, C1),
                    new Triple(a, q, C2),
                    new Triple(a, s, C1),
                    new Triple(a, t, C2),
                    new Triple(a, p, C1),
                    new Triple(a, p, C2),
                    new Triple(a, r, s),
                } );
    }
    
    /**
     * Test wildcard predicate usage - combined triple search and multiclause matching.
     * Rules look odd because we have to hack around the recursive loops.
     */
    public void testWildPredicate3() {
        String rules = "[r1: (a r ?y) <- (b ?y ?v)]" +
                "[r2: (?x q ?y) <- (?x p ?y)]" +
                "[r3: (?x s C1) <- (?x p C1)]" +
                "[r4: (?x t ?y) <- (?x ?y C1)]";
        Triple[] data =
                new Triple[] {
                    new Triple(b, p, C1),
                    new Triple(b, q, C2),
                    new Triple(b, q, C3),
                    new Triple(a, p, C1),
                    new Triple(a, p, C2),
                    new Triple(c, p, C1),
                };
        doTest(rules, data,
                new Triple(a, Node.ANY, C1),
                new Object[] {
                    new Triple(a, q, C1),
                    new Triple(a, s, C1),
                    new Triple(a, p, C1),
                } );
        doTest(rules, data,
                new Triple(a, t, Node.ANY),
                new Object[] {
                    new Triple(a, t, q),
                    new Triple(a, t, s),
                    new Triple(a, t, p),
                } );
        doTest(rules, data,
                new Triple(Node.ANY, t, q),
                new Object[] {
                    new Triple(a, t, q),
                    new Triple(b, t, q),
                    new Triple(c, t, q)
                } );
    }
    
    /**
     * Test wildcard predicate usage - wildcard in head as well
     */
    public void testWildPredicate4() {
        doTest("[r1: (a ?p ?x) <- (b ?p ?x)]",
                new Triple[] {
                    new Triple(b, p, C1),
                    new Triple(b, q, C2),
                    new Triple(b, q, C3),
                    new Triple(c, q, d),
                },
                new Triple(a, Node.ANY, Node.ANY),
                new Object[] {
                    new Triple(a, p, C1),
                    new Triple(a, q, C2),
                    new Triple(a, q, C3),
                } );
    }

    /**
     * Test functor usage.
     */
    public void testFunctors1() {
        String ruleSrc = "[r1: (?x s ?y) <- (?x p foo(?z, ?y))] ";
        Triple[] triples =
            new Triple[] {
                new Triple(a, p, Functor.makeFunctorNode("foo", new Node[] {C1, C2})),
                new Triple(a, p, Functor.makeFunctorNode("bar", new Node[] {C1, D1})),
                new Triple(b, p, Functor.makeFunctorNode("foo", new Node[] {C1, C2})),
                new Triple(a, p, Functor.makeFunctorNode("foo", new Node[] {C1, C3})),
                new Triple(a, p, D1),
            };
        doTest(ruleSrc, triples, new Triple(Node.ANY, s, Node.ANY),
            new Object[] {
                new Triple(a, s, C2),
                new Triple(b, s, C2),
                new Triple(a, s, C3)
            } );
    }

    /**
     * Test functor usage.
     */
    public void testFunctors2() {
        String ruleSrc = "[r1: (?x r foo(?y,?z)) <- (?x p ?y), (?x q ?z)]" +
               "[r2: (?x s ?y) <- (?x r foo(?z, ?y))] ";
        Triple[] triples =
            new Triple[] {
                new Triple(a, p, C1),
                new Triple(a, p, C3),
                new Triple(a, q, C2),
                new Triple(b, p, D1),
                new Triple(b, q, D2),
                new Triple(b, q, D3),
            };
        doTest(ruleSrc, triples, new Triple(Node.ANY, s, Node.ANY),
            new Object[] {
                new Triple(a, s, C2),
                new Triple(b, s, D2),
                new Triple(b, s, D3)
            } );
    }

    /**
     * Test functor usage.
     */
    public void testFunctors3() {
        String ruleSrc = "[r1: (?x r foo(p,?y)) <- (?x p ?y)]" +
                         "[r2: (?x r foo(q,?y)) <- (?x q ?y)]" +
                        "[r3: (?x r ?y) <- (?x t ?y)] " +
                        "[r4: (?x s ?y) <- (?x r ?y), notFunctor(?y)] " +
                        "[r5: (?x s ?y) <- (?x r foo(?y, ?z))] ";
        Triple[] triples =
            new Triple[] {
                new Triple(a, p, C1),
                new Triple(b, q, D1),
                new Triple(b, p, D2),
                new Triple(c, t, d)
            };
        doTest(ruleSrc, triples, new Triple(Node.ANY, s, Node.ANY),
            new Object[] {
                new Triple(a, s, p),
                new Triple(b, s, p),
                new Triple(b, s, q),
                new Triple(c, s, d)
            } );
    }
    
    /**
     * Test tabled predicates. Simple chain call case.
     */
    public void testTabled1() {
        doTest("[r1: (?a q ?b) <- (?a p ?b)]" +
               "[r2: (?x r ?y) <- (?x q ?y)]",
                new Node[] { q },
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(b, p, c),
                },
                new Triple(Node.ANY, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, b),
                    new Triple(b, r, c)
                } );
    }
    
    /**
     * Test tabled predicates. Simple transitive closure case.
     */
    public void testTabled2() {
        doTest("[r1: (?a p ?c) <- (?a p ?b)(?b p ?c)]",
                new Node[] { p },
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(b, p, c),
                    new Triple(b, p, d),
                },
                new Triple(Node.ANY, p, Node.ANY),
                new Object[] {
                    new Triple(a, p, b),
                    new Triple(b, p, c),
                    new Triple(a, p, c),
                    new Triple(b, p, d),
                    new Triple(a, p, d),
                } );
    }
    
    /**
     * Test tabled predicates. Simple transitive closure over normal predicates
     */
    public void testTabled3() {
        doTest("[r1: (?x p ?z) <- (?x p ?y), (?y p ?z)]" +
               "[r2: (?x p ?z) <- (?x e ?z), (?z q ?z)]",
                new Node[] { p },
                new Triple[] {
                    new Triple(a, e, b),
                    new Triple(a, e, d),
                    new Triple(b, e, c),
                    new Triple(a, q, a),
                    new Triple(b, q, b),
                    new Triple(c, q, c),
                },
                new Triple(a, p, Node.ANY),
                new Object[] {
                    new Triple(a, p, b),
//                    new Triple(b, p, c),
                    new Triple(a, p, c)
                } );
    }
    
    /**
     * Test tabled predicates. Co-routining example.
     */
    public void testTabled4() {
        doTest("[r1: (?x a ?y) <- (?x c ?y)]" +
               "[r2: (?x a ?y) <- (?x b ?z), (?z c ?y)]" +
               "[r3: (?x b ?y) <- (?x d ?y)]" +
               "[r4: (?x b ?y) <- (?x a ?z) (?z c ?y)]",
                new Node[] { a, b },
                new Triple[] {
                    new Triple(p, c, q),
                    new Triple(q, c, r),
                    new Triple(p, d, q),
                    new Triple(q, d, r),
                },
                new Triple(p, a, Node.ANY),
                new Object[] {
                    new Triple(p, a, q),
                    new Triple(p, a, r)
                } );
    }
    
    /**
     * Test tabled predicates. Simple transitive closure case.
     */
    public void testTabled5() {
        doTest("[r1: (?a p ?c) <- (?a p ?b)(?b p ?c)]" +
               "[r2: (?a r ?b) <- (?a q ?b)]",
                new Node[] { p },
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(b, p, c),
                    new Triple(a, q, d),
                    new Triple(c, q, d),
                },
                new Triple(a, Node.ANY, Node.ANY),
                new Object[] {
                    new Triple(a, p, b),
                    new Triple(a, p, c),
                    new Triple(a, q, d),
                    new Triple(a, r, d),
                } );
    }
   
    /**
     * Test tabled predicates. Simple transitive closure case, tabling set
     * by rule base.
     */
    public void testTabled6() {
        doTest("[-> table(p)] [r1: (?a p ?c) <- (?a p ?b)(?b p ?c)]",
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(b, p, c),
                    new Triple(b, p, d),
                },
                new Triple(Node.ANY, p, Node.ANY),
                new Object[] {
                    new Triple(a, p, b),
                    new Triple(b, p, c),
                    new Triple(a, p, c),
                    new Triple(b, p, d),
                    new Triple(a, p, d),
                } );
    }
   
    /**
     * Test RDFS example.
     */
    public void testRDFS1() {
        doTest(
    "[ (?a rdf:type C1) <- (?a rdf:type C2) ]" +
    "[ (?a rdf:type C2) <- (?a rdf:type C3) ]" +
    "[ (?a rdf:type C3) <- (?a rdf:type C4) ]",
                new Node[] { ty },
                new Triple[] {
                    new Triple(a, ty, C1),
                    new Triple(b, ty, C2),
                    new Triple(c, ty, C3),
                    new Triple(d, ty, C4),
                },
                new Triple(Node.ANY, ty, C1),
                new Object[] {
                    new Triple(a, ty, C1),
                    new Triple(b, ty, C1),
                    new Triple(c, ty, C1),
                    new Triple(d, ty, C1),
                } );
    }
   
    /**
     * Test RDFS example - branched version
     */
    public void testRDFS2() {
        doTest(
    "[ (?a rdf:type C1) <- (?a rdf:type C2) ]" +
    "[ (?a rdf:type C1) <- (?a rdf:type C3) ]" +
    "[ (?a rdf:type C1) <- (?a rdf:type C4) ]",
                new Node[] { ty },
                new Triple[] {
                    new Triple(a, ty, C1),
                    new Triple(b, ty, C2),
                    new Triple(c, ty, C3),
                    new Triple(d, ty, C4),
                },
                new Triple(Node.ANY, ty, C1),
                new Object[] {
                    new Triple(a, ty, C1),
                    new Triple(b, ty, C1),
                    new Triple(c, ty, C1),
                    new Triple(d, ty, C1),
                } );
    }

    /**
     * A problem from the original backchainer tests - interaction
     * of tabling and functor expansion.
     */
    public void testProblem1() {
        doTest(
               "[r1: (a q f(?x,?y)) <- (a s ?x), (a t ?y)]" +
               "[r2: (a p ?x) <- (a q ?x)]" +
               "[r3: (a r ?y) <- (a p f(?x, ?y))]",
                new Node[] { p },
                new Triple[] {
                    new Triple(a, s, b),
                    new Triple(a, t, c)
                },
                new Triple(a, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, c)
                } );

    }

    /**
     * A problem from the original backchainer tests - tabled closure operation.
     */
    public void testProblem2() {
        String ruleSrc = 
        "[rdfs8:  (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c)]" + 
        "[rdfs7:  (?a rdfs:subClassOf ?a) <- (?a rdf:type rdfs:Class)]";
        doTest( ruleSrc,
                new Node[] { ty, sC },
                new Triple[] {
                    new Triple(C1, sC, C2),
                    new Triple(C2, sC, C3),
                    new Triple(C1, ty, RDFS.Class.asNode()),
                    new Triple(C2, ty, RDFS.Class.asNode()),
                    new Triple(C3, ty, RDFS.Class.asNode())
                },
                new Triple(Node.ANY, sC, Node.ANY),
                new Object[] {
                    new Triple(C1, sC, C2),
                    new Triple(C1, sC, C3),
                    new Triple(C1, sC, C1),
                    new Triple(C2, sC, C3),
                    new Triple(C2, sC, C2),
                    new Triple(C3, sC, C3)
                } );
    }

    /**
     * A problem from the original backchainer tests - bound/unbound primitives
     */
    public void testProblem3() {
        String rules =         "[r1: (?x r ?y ) <- bound(?x), (?x p ?y) ]" +
        "[r2: (?x r ?y) <- unbound(?x), (?x q ?y)]";
        doTest(rules,
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(a, q, c)
                },
                new Triple(a, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, b)
                } );
        doTest(rules,
                new Triple[] {
                    new Triple(a, p, b),
                    new Triple(a, q, c)
                },
                new Triple(Node.ANY, r, Node.ANY),
                new Object[] {
                    new Triple(a, r, c)
                } );
    }

    /**
     * A problem from the original backchainer tests - head unification test
     */
    public void testProblem4() {
        String rules =   "[r1: (c r ?x) <- (?x p ?x)]" +
        "[r2: (?x p ?y) <- (a q ?x), (b q ?y)]";
        doTest(rules,
                new Node[] { r, p },
                new Triple[] {
                    new Triple(a, q, a),
                    new Triple(a, q, b),
                    new Triple(a, q, c),
                    new Triple(b, q, b),
                    new Triple(b, q, d),
                },
                new Triple(c, r, Node.ANY),
                new Object[] {
                    new Triple(c, r, b)
                } );
    }

    /**
     * A problem from the original backchainer tests - RDFS example which threw an NPE 
     */
    public void testProblem5() {
        String ruleSrc = 
        "[rdfs8:  (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c)]" + 
        "[rdfs9:   (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y), (?a rdf:type ?x)]" +
        "[(rdf:type rdfs:range rdfs:Class) <-]" +
        "[rdfs3:  (?y rdf:type ?c) <- (?x ?p ?y), (?p rdfs:range ?c)]" +
        "[rdfs7:  (?a rdfs:subClassOf ?a) <- (?a rdf:type rdfs:Class)]";
        doTest( ruleSrc,
                new Node[] { ty, sC },
                new Triple[] {
                    new Triple(p, sP, q),
                    new Triple(q, sP, r),
                    new Triple(C1, sC, C2),
                    new Triple(C2, sC, C3),
                    new Triple(a, ty, C1)
                },
                new Triple(a, ty, Node.ANY),
                new Object[] {
                    new Triple(a, ty, C1),
                    new Triple(a, ty, C2),
                    new Triple(a, ty, C3)
                } );
    }

    /**
     * A problem from the original backchainer tests - RDFS example which threw an NPE 
     */
    public void testProblem6() {
        String ruleSrc = 
        "[rdfs9:   (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y), (?a rdf:type ?x)]" +
        "[restriction2: (?C owl:equivalentClass all(?P, ?D)) <- (?C rdf:type owl:Restriction), (?C owl:onProperty ?P), (?C owl:allValuesFrom ?D)]" +
        "[rs2: (?X rdf:type all(?P,?C)) <- (?D owl:equivalentClass all(?P,?C)), (?X rdf:type ?D)]" +
        "[rp4: (?Y rdf:type ?C) <- (?X rdf:type all(?P, ?C)), (?X ?P ?Y)]";
        doTest( ruleSrc,
                new Node[] { ty, sC, OWL.equivalentClass.asNode() },
                new Triple[] {
                    new Triple(a, ty, r),
                    new Triple(a, p, b),
                    new Triple(r, sC, C1),
                    new Triple(C1, ty, OWL.Restriction.asNode()),
                    new Triple(C1, OWL.onProperty.asNode(), p),
                    new Triple(C1, OWL.allValuesFrom.asNode(), c)
                },
                new Triple(b, ty, c),
                new Object[] {
                    new Triple(b, ty, c)
                } );
    }

    /**
     * A problem from the original backchainer tests - incorrect additional deduction
     */
    public void testProblem7() {
        String ruleSrc = 
        "[rdfs8:  (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c)]" + 
        "[rdfs9:   (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y), (?a rdf:type ?x)]" +
//        "[(rdf:type rdfs:range rdfs:Class) <-]" +
//        "[rdfs3:  (?y rdf:type ?c) <- (?x ?p ?y), (?p rdfs:range ?c)]" +
        "[rdfs3:  (?y rdf:type rdfs:Class) <- (?x rdf:type ?y)]" +
        "[rdfs7:  (?a rdfs:subClassOf ?a) <- (?a rdf:type rdfs:Class)]";
        List rules = Rule.parseRules(ruleSrc);
        Node[] tabled = new Node[] { ty, sC }; 
        Triple[] triples = new Triple[] {
                    new Triple(C1, sC, C2),
                    new Triple(C2, sC, C3),
                    new Triple(a, ty, C1)
                };
        Graph data = new GraphMem();
        for (int i = 0; i < triples.length; i++) {
            data.add(triples[i]);
        }
        InfGraph infgraph =  makeInfGraph(rules, data, tabled);
        ExtendedIterator it = infgraph.find(a, ty, null);
        Triple result = (Triple)it.next();
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
     * Generic test operation.
     * @param ruleSrc the source of the rules
     * @param triples a set of triples to insert in the graph before the query
     * @param query the TripleMatch to search for
     * @param results the array of expected results
     */
    private void doTest(String ruleSrc, Triple[] triples, TripleMatch query, Object[] results) {
        List rules = Rule.parseRules(ruleSrc);
        Graph data = new GraphMem();
        for (int i = 0; i < triples.length; i++) {
            data.add(triples[i]);
        }
        InfGraph infgraph =  makeInfGraph(rules, data);
        TestUtil.assertIteratorValues(this, infgraph.find(query), results); 

    }

    /** 
     * Generic test operation.
     * @param ruleSrc the source of the rules
     * @param tabled the predicates that should be tabled
     * @param triples a set of triples to insert in the graph before the query
     * @param query the TripleMatch to search for
     * @param results the array of expected results
     */
    private void doTest(String ruleSrc, Node[] tabled, Triple[] triples, TripleMatch query, Object[] results) {
        List rules = Rule.parseRules(ruleSrc);
        Graph data = new GraphMem();
        for (int i = 0; i < triples.length; i++) {
            data.add(triples[i]);
        }
        InfGraph infgraph =  makeInfGraph(rules, data, tabled);
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