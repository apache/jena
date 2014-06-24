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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.reasoner.Derivation;
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
    Node p = NodeFactory.createURI("p");
    Node q = NodeFactory.createURI("q");
    Node r = NodeFactory.createURI("r");
    Node s = NodeFactory.createURI("s");
    Node a = NodeFactory.createURI("a");
    Node b = NodeFactory.createURI("b");
    Node c = NodeFactory.createURI("c");
    Node d = NodeFactory.createURI("d");
    Node e = NodeFactory.createURI("e");

    /**
     * Return an inference graph working over the given rule set and raw data.
     * Can be overridden by subclasses of this test class.
     * @param rules the rule set to use
     * @param data the graph of triples to process
     * @param tabled an array of predicates that should be tabled
     */
    public static InfGraph makeInfGraph(List<Rule> rules, Graph data, Node[] tabled) {
        FBRuleReasoner reasoner = new FBRuleReasoner(rules);
        FBRuleInfGraph infgraph = (FBRuleInfGraph) reasoner.bind(data);
        for ( Node aTabled : tabled )
        {
            infgraph.setTabled( aTabled );
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
        List<Rule> rules = Rule.parseRules(ruleSrc);
        Graph data = Factory.createGraphMem();
        for ( Triple triple : triples )
        {
            data.add( triple );
        }
        InfGraph infgraph =  makeInfGraph(rules, data, tabled);
        ExtendedIterator<Triple> results = infgraph.find(query);
        assertTrue(results.hasNext());
        Triple result = results.next();
        results.close();
        Rule rule = rules.get(rulenumber);
        List<Triple> matchList = Arrays.asList(matches);
        Iterator<Derivation> derivations = infgraph.getDerivation(result);
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
