/******************************************************************
 * File:        TestBackchainer.java
 * Created by:  Dave Reynolds
 * Created on:  04-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestBackchainer.java,v 1.3 2003-05-07 06:57:30 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *  
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-05-07 06:57:30 $
 */
public class TestBackchainer extends TestCase {

    // Useful constants
    Node p = Node.createURI("p");
    Node q = Node.createURI("q");
    Node r = Node.createURI("r");
    Node s = Node.createURI("s");
    Node a = Node.createURI("a");
    Node b = Node.createURI("b");
    Node c = Node.createURI("c");
    Node d = Node.createURI("d");
    Node sP = RDFS.subPropertyOf.getNode();
    
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
    }  

    /**
     * Test parser modes to support backarrow notation are working
     */
    public void testParse() {
        List rules = Rule.parseRules(testRules1);
        assertEquals("BRule parsing", 
                        "[ (?p rdfs:subPropertyOf ?q) (?x ?p ?y) -> (?x ?q ?y) ]", 
                        rules.get(0).toString());
        assertEquals("BRule parsing", 
                        "[ (?a rdfs:subPropertyOf ?b) (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c) ]", 
                        rules.get(1).toString());
    }
    
    /**
     * Check that a reasoner over an empty rule set accesses
     * the raw data successfully.
     */
    public void testListData() {
        Graph data = new GraphMem();
        for (int i = 0; i < dataElts.length; i++) {
            data.add(dataElts[i]);
        }
        Graph schema = new GraphMem();
        schema.add(new Triple(c, p, c));
        
        // Case of schema and data but no rule axioms
        Reasoner reasoner =  new BasicBackwardRuleReasoner(new ArrayList());
        InfGraph infgraph = reasoner.bindSchema(schema).bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, null, null), 
            new Object[] {
                new Triple(p, sP, q),
                new Triple(q, sP, r),
                new Triple(a,  p, b), 
                new Triple(c, p, c)});
                
        // Case of data and rule axioms but no schema
        List rules = Rule.parseRules("-> (d p d).");
        reasoner =  new BasicBackwardRuleReasoner(rules);
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
        List rules = Rule.parseRules("[r1: (?a r ?c) <- (?a p ?b),(?b p ?c)]");        
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(b, p, c));
        data.add(new Triple(b, p, d));
        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
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
        List rules = Rule.parseRules(
                "[r1: (?a r ?b) <- (?a p ?b)]" +
                "[r2: (?a r ?b) <- (?a q ?b)]" +
                "[r3: (?a r ?b) <- (?a s ?c), (?c s ?b)]"
        );        
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(b, q, c));
        data.add(new Triple(a, s, b));
        data.add(new Triple(b, s, d));
        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
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
        List rules = Rule.parseRules("[rule: (?a rdfs:subPropertyOf ?c) <- (?a rdfs:subPropertyOf ?b),(?b rdfs:subPropertyOf ?c)]");        
        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
        Graph data = new GraphMem();
        data.add(new Triple(p, sP, q) );
        data.add(new Triple(q, sP, r) );
        data.add(new Triple(a,  p, b) );
        InfGraph infgraph = reasoner.bind(data);
        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(null, RDFS.subPropertyOf.asNode(), null), 
            new Object[] {
                new Triple(p, sP, q),
                new Triple(q, sP, r),
                new Triple(p, sP, r)
            } );
    }

    /**
     * Test basic rule operations - simple AND/OR with tabling.
     */
//    public void testBaseRules4() {    
//        Graph data = new GraphMem();
//        data.add(new Triple(a, r, b));
//        data.add(new Triple(b, r, c));
//        data.add(new Triple(b, r, b));
//        data.add(new Triple(b, r, d));
//        List rules = Rule.parseRules(
//                        "[r1: (?x p ?y) <- (?x r ?y)]" +
//                        "[r2: (?x p ?z) <- (?x p ?y), (?y r ?z)]" 
//                        );        
//        Reasoner reasoner =  new BasicBackwardRuleReasoner(rules);
//        InfGraph infgraph = reasoner.bind(data);
//        ((BasicBackwardRuleInfGraph)infgraph).setTraceOn(true);
//        TestUtil.assertIteratorValues(this, 
//            infgraph.find(a, p, null), 
//            new Object[] {
//                new Triple(a, p, b),
//                new Triple(a, p, d),
//                new Triple(a, p, c)
//            } );
//    }
    
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