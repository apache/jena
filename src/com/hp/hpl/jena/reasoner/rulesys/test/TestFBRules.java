/******************************************************************
 * File:        TestFBRules.java
 * Created by:  Dave Reynolds
 * Created on:  29-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestFBRules.java,v 1.3 2003-06-02 09:04:31 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;

/**
 * Test suite for the hybrid forward/backward rule system.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-06-02 09:04:31 $
 */
public class TestFBRules extends TestCase {
    // Useful constants
    Node p = Node.createURI("p");
    Node q = Node.createURI("q");
    Node n1 = Node.createURI("n1");
    Node n2 = Node.createURI("n2");
    Node n3 = Node.createURI("n3");
    Node n4 = Node.createURI("n4");
    Node res = Node.createURI("res");
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
    Node sP = RDFS.subPropertyOf.getNode();
    Node sC = RDFS.subClassOf.getNode();
    Node ty = RDF.type.getNode();
     
    /**
     * Boilerplate for junit
     */ 
    public TestFBRules( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestFBRules.class ); 
    }  

    /**
     * Check parser extension for f/b distinction.
     */
    public void testParser() {
        String rf = "(?a rdf:type ?t) -> (?t rdf:type rdfs:Class).";
        String rb = "(?t rdf:type rdfs:Class) <- (?a rdf:type ?t).";
        assertTrue( ! Rule.parseRule(rf).isBackward() );
        assertTrue(   Rule.parseRule(rb).isBackward() );
    }
     
    /**
     * Minimal rule tester to check basic pattern match, forward style.
     */
    public void testRuleMatcher() {
        String rules = "[r1: (?a p ?b), (?b q ?c) -> (?a, q, ?c)]" +
                       "[r2: (?a p ?b), (?b p ?c) -> (?a, p, ?c)]" +
                       "[r3: (?a p ?a), (n1 p ?c), (n1, p, ?a) -> (?a, p, ?c)]" +
                       "[r4: (n4 ?p ?a) -> (n4, ?a, ?p)]";
        List ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = new FBRuleReasoner(ruleList).bind(new GraphMem());
        infgraph.add(new Triple(n1, p, n2));
        infgraph.add(new Triple(n2, p, n3));
        infgraph.add(new Triple(n2, q, n3));
        infgraph.add(new Triple(n4, p, n4));
        
        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null),
            new Triple[] {
                new Triple(n1, p, n2),
                new Triple(n2, p, n3),
                new Triple(n2, q, n3),
                new Triple(n4, p, n4),
                new Triple(n1, p, n3),
                new Triple(n1, q, n3),
                new Triple(n4, n4, p),
            });
    }
    
    /**
     * Test functor handling
     */
    public void testEmbeddedFunctors() {
        String rules = "(?C rdf:type owl:Restriction), (?C owl:onProperty ?P), (?C owl:allValuesFrom ?D) -> (?C rb:restriction all(?P, ?D))." +
                       "[ -> (eg:foo eg:prop functor(eg:bar, '1')) ]" +
                       "[ (?x eg:prop functor(eg:bar, ?v)) -> (?x eg:propbar ?v) ]" +
                       "[ (?x eg:prop functor(?v, *)) -> (?x eg:propfunc ?v) ]" +
                       "";
        List ruleList = Rule.parseRules(rules);
        
        Model data = ModelFactory.createDefaultModel();
        Resource R1 = data.createResource(PrintUtil.egNS + "R1");
        Resource D = data.createResource(PrintUtil.egNS + "D");
        Property p = data.createProperty(PrintUtil.egNS, "p");
        Property prop = data.createProperty(PrintUtil.egNS, "prop");
        Property propbar = data.createProperty(PrintUtil.egNS, "propbar");
        Property propfunc = data.createProperty(PrintUtil.egNS, "propfunc");
        Property rbr = data.createProperty(Rule.RBNamespace, "restriction");
        R1.addProperty(RDF.type, OWL.Restriction)
          .addProperty(OWL.onProperty, p)
          .addProperty(OWL.allValuesFrom, D);
        
        Reasoner reasoner =  new FBRuleReasoner(ruleList);
        InfGraph infgraph = reasoner.bind(data.getGraph());
        Model infModel = ModelFactory.createModelForGraph(infgraph);
        Resource foo = infModel.createResource(PrintUtil.egNS + "foo");
        Resource bar = infModel.createResource(PrintUtil.egNS + "bar");
        
        RDFNode flit = infModel.getResource(R1.getURI()).getProperty(rbr).getObject();
        assertNotNull(flit);
        assertTrue(flit instanceof Literal);
        Functor func = (Functor)((Literal)flit).getValue();
        assertEquals("all", func.getName());
        assertEquals(p.getNode(), func.getArgs()[0]);
        assertEquals(D.getNode(), func.getArgs()[1]);
        
        Literal one = (Literal)foo.getProperty(propbar).getObject();
        assertEquals(new Integer(1), one.getValue());
    }
    
    /**
     * The the minimal machinery for supporting builtins
     */
    public void testBuiltins() {
        String rules =  //"[testRule1: (n1 ?p ?a) -> print('rule1test', ?p, ?a)]" +
                       "[r1: (n1 p ?x), addOne(?x, ?y) -> (n1 q ?y)]" +
                       "[r2: (n1 p ?x), lessThan(?x, '3') -> (n2 q ?x)]" +
                       "[axiom1: -> (n1 p '1')]" +
                       "[axiom2: -> (n1 p '4')]" +
                       "";
        List ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = new FBRuleReasoner(ruleList).bind(new GraphMem());
        TestUtil.assertIteratorValues(this, infgraph.find(n1, q, null),
            new Triple[] {
                new Triple(n1, q, Util.makeIntNode(2)),
                new Triple(n1, q, Util.makeIntNode(5))
            });
        TestUtil.assertIteratorValues(this, infgraph.find(n2, q, null),
            new Triple[] {
                new Triple(n2, q, Util.makeIntNode(1))
            });
        
    }
         
    /**
     * Test schmea partial binding machinery, forward subset.
     */
    public void testSchemaBinding() {
        String rules = "[testRule1: (n1 p ?a) -> (n2, p, ?a)]" +
                       "[testRule2: (n1 q ?a) -> (n2, q, ?a)]" +
                       "[testRule3: (n2 p ?a), (n2 q ?a) -> (res p ?a)]";
        List ruleList = Rule.parseRules(rules);
        Graph schema = new GraphMem();
        schema.add(new Triple(n1, p, n3));
        Graph data = new GraphMem();
        data.add(new Triple(n1, q, n4));
        data.add(new Triple(n1, q, n3));
        
        Reasoner reasoner =  new FBRuleReasoner(ruleList);
        Reasoner boundReasoner = reasoner.bindSchema(schema);
        InfGraph infgraph = boundReasoner.bind(data);

        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null),
            new Triple[] {
                new Triple(n1, p, n3),
                new Triple(n2, p, n3),
                new Triple(n1, q, n4),
                new Triple(n2, q, n4),
                new Triple(n1, q, n3),
                new Triple(n2, q, n3),
                new Triple(res, p, n3)
            });
    }
    
    /**
     * The the "remove" builtin
     */
    public void testRemoveBuiltin() {
        String rules =  
                       "[rule1: (?x p ?y), (?x q ?y) -> remove('0')]" +
                       "";
        List ruleList = Rule.parseRules(rules);

        InfGraph infgraph = new FBRuleReasoner(ruleList).bind(new GraphMem());
        infgraph.add(new Triple(n1, p, Util.makeIntNode(1)));
        infgraph.add(new Triple(n1, p, Util.makeIntNode(2)));
        infgraph.add(new Triple(n1, q, Util.makeIntNode(2)));
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, Util.makeIntNode(1)),
                new Triple(n1, q, Util.makeIntNode(2))
            });
        
    }
    
    /**
     * Test the rebind operation.
     */
    public void testRebind() {
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        List ruleList = Rule.parseRules(rules);
        Graph data = new GraphMem();
        data.add(new Triple(n1, p, n2));
        InfGraph infgraph = new FBRuleReasoner(ruleList).bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, n2),
                new Triple(n1, q, n2)
            });
        Graph ndata = new GraphMem();
        ndata.add(new Triple(n1, p, n3));
        infgraph.rebind(ndata);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, n3),
                new Triple(n1, q, n3)
            });
    }
    

    /**
     * Test example pure backchaining rules
     */
    public void testBackchain1() {    
        Graph data = new GraphMem();
        data.add(new Triple(p, sP, q));
        data.add(new Triple(q, sP, r));
        data.add(new Triple(C1, sC, C2));
        data.add(new Triple(C2, sC, C3));
        data.add(new Triple(a, ty, C1));
        List rules = Rule.parseRules(
        "[rdfs8:  (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c)]" + 
        "[rdfs9:  (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y), (?a rdf:type ?x)]" +
        "[-> (rdf:type rdfs:range rdfs:Class)]" +
        "[rdfs3:  (?y rdf:type ?c) <- (?x ?p ?y), (?p rdfs:range ?c)]" +
        "[rdfs7:  (?a rdfs:subClassOf ?a) <- (?a rdf:type rdfs:Class)]"
                        );        
        Reasoner reasoner =  new FBRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
            infgraph.find(a, ty, null), 
            new Object[] {
                new Triple(a, ty, C1),
                new Triple(a, ty, C2),
                new Triple(a, ty, C3)
            } );
        TestUtil.assertIteratorValues(this, 
            infgraph.find(C1, sC, a), 
            new Object[] {
            } );
    }

    /**
     * Test complex rule head unification
     */
    public void testBackchain2() {    
        Graph data = new GraphMem();
        data.add(new Triple(c, q, d));
        List rules = Rule.parseRules(
            "[r1: (c r ?x) <- (?x p f(?x b))]" +
            "[r2: (?y p f(a ?y)) <- (c q ?y)]"
                          );        
        Reasoner reasoner =  new FBRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), new Object[] { } );
              
        data.add(new Triple(c, q, a));
        rules = Rule.parseRules(
        "[r1: (c r ?x) <- (?x p f(?x a))]" +
        "[r2: (?y p f(a ?y)) <- (c q ?y)]"
                          );        
        reasoner =  new FBRuleReasoner(rules);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), 
              new Object[] {
                  new Triple(c, r, a)
              } );
            
        data = new GraphMem();
        data.add(new Triple(a, q, a));
        data.add(new Triple(a, q, b));
        data.add(new Triple(a, q, c));
        data.add(new Triple(b, q, d));
        data.add(new Triple(b, q, b));
        rules = Rule.parseRules(
          "[r1: (c r ?x) <- (?x p ?x)]" +
          "[r2: (?x p ?y) <- (a q ?x), (b q ?y)]"
                          );        
        reasoner =  new FBRuleReasoner(rules);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), 
              new Object[] {
                  new Triple(c, r, b)
              } );
              
        rules = Rule.parseRules(
          "[r1: (c r ?x) <- (?x p ?x)]" +
          "[r2: (a p ?x) <- (a q ?x)]"
                          );        
        reasoner =  new FBRuleReasoner(rules);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(c, r, null), 
              new Object[] {
                  new Triple(c, r, a)
              } );
    }

    /**
     * Test restriction example
     */
    public void testBackchain3() {    
        Graph data = new GraphMem();
        data.add(new Triple(a, ty, r));
        data.add(new Triple(a, p, b));
        data.add(new Triple(r, sC, C1));
        data.add(new Triple(C1, ty, OWL.Restriction.asNode()));
        data.add(new Triple(C1, OWL.onProperty.asNode(), p));
        data.add(new Triple(C1, OWL.allValuesFrom.asNode(), c));
        List rules = Rule.parseRules(
    "[rdfs9:   (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y) (?a rdf:type ?x)]" +
    "[restriction2: (?C owl:equivalentClass all(?P, ?D)) <- (?C rdf:type owl:Restriction), (?C owl:onProperty ?P), (?C owl:allValuesFrom ?D)]" +
    "[rs2: (?X rdf:type all(?P,?C)) <- (?D owl:equivalentClass all(?P,?C)), (?X rdf:type ?D)]" +
    "[rp4: (?Y rdf:type ?C) <- (?X rdf:type all(?P, ?C)), (?X ?P ?Y)]"
                          );        
        Reasoner reasoner =  new FBRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, c), new Object[] {
                  new Triple(b, ty, c)
              } );
    }
    
    /**
     * Test example hybrid rule.
     */
    public void testHybrid1() {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(p, ty, s));
        List rules = Rule.parseRules(
        "[r1: (?p rdf:type s) -> [r1b: (?x ?p ?y) <- (?y ?p ?x)]]"
                          );        
        Reasoner reasoner =  new FBRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, p, null), new Object[] {
                  new Triple(a, p, b),
                  new Triple(b, p, a)
              } );
    }
    
    /**
     * Test example hybrid rule.
     */
    public void testHybrid2() {
        Graph data = new GraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(p, ty, s));
        List rules = Rule.parseRules(
        "[a1: -> (a rdf:type t)]" +
        "[r0: (?x r ?y) -> (?x p ?y)]" +
        "[r1: (?p rdf:type s) -> [r1b: (?x ?p ?y) <- (?y ?p ?x)]]" +
        "[r2: (?p rdf:type s) -> [r2b: (?x ?p ?x) <- (?x rdf:type t)]]"
                          );        
        Reasoner reasoner =  new FBRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, p, null), new Object[] {
                  new Triple(a, p, a),
                  new Triple(a, p, b),
                  new Triple(b, p, a)
              } );
    }
    
    /**
     * Test example hybrid rules for rdfs.
     */
    public void testHybridRDFS() {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(p, RDFS.range.asNode(), C1));
        List rules = Rule.parseRules(
    "[rdfs2:  (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ]" +
    "[rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]" + 
    "[rdfs5a: (?a rdfs:subPropertyOf ?b), (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c)]" + 
    "[rdfs5b: (?a rdf:type rdf:Property) -> (?a rdfs:subPropertyOf ?a)]" + 
    "[rdfs6:  (?p rdfs:subPropertyOf ?q) -> [ (?a ?q ?b) <- (?a ?p ?b)] ]" + 
    "[rdfs7:  (?a rdf:type rdfs:Class) -> (?a rdfs:subClassOf ?a)]" +
    "[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]" + 
    "[rdfs9:  (?x rdfs:subClassOf ?y) -> [ (?a rdf:type ?y) <- (?a rdf:type ?x)] ]" +
                          "" );        
        Reasoner reasoner =  new FBRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
//        ((FBRuleInfGraph)infgraph).setTraceOn(true);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, null), new Object[] {
                  new Triple(b, ty, C1)
              } );
    }
    
    /**
     * Test example hybrid rules for rdfs.
     */
    public void testHybridRDFS2() {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, b));
        data.add(new Triple(p, sP, r));
        data.add(new Triple(r, RDFS.range.asNode(), C1));
        List rules = Rule.parseRules(
    "[rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]" + 
    "[rdfs6:  (?p rdfs:subPropertyOf ?q) -> [ (?a ?q ?b) <- (?a ?p ?b)] ]" + 
                          "" );        
        Reasoner reasoner =  new FBRuleReasoner(rules);
        InfGraph infgraph = reasoner.bind(data);
//        ((FBRuleInfGraph)infgraph).setTraceOn(true);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, C1), new Object[] {
                  new Triple(b, ty, C1)
              } );
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