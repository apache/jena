/******************************************************************
 * File:        TestLPDerivation.java
 * Created by:  Dave Reynolds
 * Created on:  07-Oct-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestLPDerivation.java,v 1.4 2008-01-02 12:08:20 andy_seaborne Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the derivation tracing of the LP system.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $
 */

public class TestLPDerivation extends TestCase {
    
    /**
     * Boilerplate for junit
     */ 
    public TestLPDerivation( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestLPDerivation.class );
    }
    
    // Useful constants
    Node p = Node.createURI("p");
    Node q = Node.createURI("q");
    Node r = Node.createURI("r");
    Node s = Node.createURI("s");
    Node a = Node.createURI("a");
    Node b = Node.createURI("b");
    Node c = Node.createURI("c");
    Node d = Node.createURI("d");
    Node e = Node.createURI("e");

    /**
     * Return an inference graph working over the given rule set and raw data.
     * Can be overridden by subclasses of this test class.
     * @param rules the rule set to use
     * @param data the graph of triples to process
     * @param tabled an array of predicates that should be tabled
     */
    public static InfGraph makeInfGraph(List rules, Graph data, Node[] tabled) {
        FBRuleReasoner reasoner = new FBRuleReasoner(rules);
        FBRuleInfGraph infgraph = (FBRuleInfGraph) reasoner.bind(data);
        for (int i = 0; i < tabled.length; i++) {
            infgraph.setTabled(tabled[i]);
        }
//        infgraph.setTraceOn(true);
        infgraph.setDerivationLogging(true);
        return infgraph;
    }

    /**
     * Run a single derivation test. Only tests the first derivation found.
     * @param ruleSrc source for a set of rules
     * @param tabled  array of predicate nodes which should be tabled by the rules
     * @param triples inital array of triple data
     * @param query   the query to be tested the first result will be checked
     * @param matches the set of triple matches which should occur in the derivation
     * @param rulenumber the index of the rule in the rule list which should occur in the derivation
     */
    private void doTest(String ruleSrc, Node[] tabled, Triple[] triples, TripleMatch query, Triple[] matches, int rulenumber) {
        List rules = Rule.parseRules(ruleSrc);
        Graph data = Factory.createGraphMem();
        for (int i = 0; i < triples.length; i++) {
            data.add(triples[i]);
        }
        InfGraph infgraph =  makeInfGraph(rules, data, tabled);
        ExtendedIterator results = infgraph.find(query);
        assertTrue(results.hasNext());
        Triple result = (Triple) results.next();
        results.close();
        Rule rule = (Rule)rules.get(rulenumber);
        List matchList = Arrays.asList(matches);
        Iterator derivations = infgraph.getDerivation(result);
        assertTrue(derivations.hasNext());
        RuleDerivation derivation = (RuleDerivation) derivations.next();
//        PrintWriter pw = new PrintWriter(System.out);
//        derivation.printTrace(pw, true);
//        pw.close();
        assertEquals(result, derivation.getConclusion());
        assertEquals(matchList, derivation.getMatches());
        assertEquals(rule, derivation.getRule());
    }

    /**
     * Test simple rule derivation.
     */
    public void testBasic() {
        doTest(
                "(?x p ?y) <- (?x q ?y).", new Node[]{},    // Rules + tabling
                new Triple[] {                              // Data
                        new Triple(a, q, b),  
                },
                new Triple(a, p, b),                        // query
                new Triple[] {                              // Expected match list in derivation
                        new Triple(a, q, b)
                },
                0                                           // Expected rule in derivation
                );
    }
    
    /**
     * Test simple rule derivation from pair
     */
    public void testBasic2() {
        doTest(
                "(?x p ?y) <- (?x q ?y). (?x p ?y) <- (?x r ?y).", 
                new Node[]{},    // Rules + tabling
                new Triple[] {                              // Data
                        new Triple(a, r, b),  
                },
                new Triple(a, p, b),                        // query
                new Triple[] {                              // Expected match list in derivation
                        new Triple(a, r, b)
                },
                1                                           // Expected rule in derivation
                );
    }
    
    /**
     * Test composite derivation.
     */
    public void testComposite() {
        doTest(
                "(?x p ?y) <- (?x q ?y) (?x r ?y).",  new Node[]{},    // Rules + tabling
                new Triple[] {                              // Data
                        new Triple(a, q, b),  
                        new Triple(a, r, b),  
                },
                new Triple(a, p, b),                        // query
                new Triple[] {                              // Expected match list in derivation
                        new Triple(a, q, b),  
                        new Triple(a, r, b)
                },
                0                                           // Expected rule in derivation
                );
    }
    
    /**
     * Test Chain derivation.
     */
    public void testChain() {
        doTest(
                "(?x s ?y) <- (?x r ?y). (?x p ?y) <- (?x q ?y) (?x s ?y). ",  
                new Node[]{},    // Rules + tabling
                new Triple[] {                              // Data
                        new Triple(a, q, b),  
                        new Triple(a, r, b),  
                },
                new Triple(a, p, b),                        // query
                new Triple[] {                              // Expected match list in derivation
                        new Triple(a, q, b),  
                        new Triple(a, s, b)
                },
                1                                           // Expected rule in derivation
                );
    }
    
    /**
     * Test tabled chaining
     */
    public void testTabled() {
        doTest(
                "(?x p ?z) <- (?x p ?y) (?y p ?z).",
                new Node[]{ p },    // Rules + tabling
                new Triple[] {                              // Data
                        new Triple(a, p, b),  
                        new Triple(a, p, c),  
                        new Triple(b, p, d),  
                },
                new Triple(a, p, d),                        // query
                new Triple[] {                              // Expected match list in derivation
                        new Triple(a, p, b),  
                        new Triple(b, p, d)
                },
                0                                           // Expected rule in derivation
                );
    }
    
}


/*
    (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
