/******************************************************************
 * File:        TestGenericRules.java
 * Created by:  Dave Reynolds
 * Created on:  08-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestGenericRules.java,v 1.3 2003-06-22 16:10:50 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import com.hp.hpl.jena.graph.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

import org.apache.log4j.Logger;


/**
 * Test the packaging of all the reasoners into the GenericRuleReasoner.
 * The other tests check out this engine. These tests just need to touch 
 * enough to validate the packaging.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-06-22 16:10:50 $
 */
public class TestGenericRules extends TestCase {
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(TestFBRules.class);

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
    Node ty = RDF.type.getNode();
    Node sC = RDFS.subClassOf.getNode();

    List ruleList = Rule.parseRules("[r1: (?a p ?b), (?b p ?c) -> (?a p ?c)]" +
                                    "[r2: (?a q ?b) -> (?a p ?c)]");
    Triple[] ans = new Triple[] { new Triple(a, p, b),
                                   new Triple(b, p, c),
                                   new Triple(a, p, c) };
                                 
    /**
     * Boilerplate for junit
     */ 
    public TestGenericRules( String name ) {
        super( name ); 
    }

    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestGenericRules.class ); 
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestGenericRules( "testAddRemove" ));
//        return suite;
    }  
    
     
    /**
     * Minimal rule tester to check basic pattern match, forward style.
     */
    public void testForward() {
        Graph test = new GraphMem();
        test.add(new Triple(a, p, b));
        test.add(new Triple(b, p, c));
        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(ruleList);
        reasoner.setMode(GenericRuleReasoner.FORWARD);
        
        // Check data bind version
        InfGraph infgraph = reasoner.bind(test);
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
        
        // Check schema bind version
        infgraph = reasoner.bindSchema(test).bind(new GraphMem());
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
    }
     
    /**
     * Minimal rule tester to check basic pattern match, backward style.
     */
    public void testBackward() {
        Graph test = new GraphMem();
        test.add(new Triple(a, p, b));
        test.add(new Triple(b, p, c));
        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(ruleList);
        reasoner.setMode(GenericRuleReasoner.BACKWARD);
        
        // Check data bind version
        InfGraph infgraph = reasoner.bind(test);
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
        
        // Check schema bind version
        infgraph = reasoner.bindSchema(test).bind(new GraphMem());
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
    }
    
    /**
     * Test example hybrid rule.
     */
    public void testHybrid() {
        Graph data = new GraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(p, ty, s));
        List rules = Rule.parseRules(
        "[a1: -> (a rdf:type t)]" +
        "[r0: (?x r ?y) -> (?x p ?y)]" +
        "[r1: (?p rdf:type s) -> [r1b: (?x ?p ?y) <- (?y ?p ?x)]]" +
        "[r2: (?p rdf:type s) -> [r2b: (?x ?p ?x) <- (?x rdf:type t)]]"
                          );        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(rules);
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        
        InfGraph infgraph = reasoner.bind(data);
        infgraph.setDerivationLogging(true);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, p, null), new Object[] {
                  new Triple(a, p, a),
                  new Triple(a, p, b),
                  new Triple(b, p, a)
              } );
              
        // Check derivation tracing as well
        Iterator di = infgraph.getDerivation(new Triple(b, p, a));
        assertTrue(di.hasNext());
        RuleDerivation d = (RuleDerivation)di.next();
        assertTrue(d.getRule().getName().equals("r1b"));
        TestUtil.assertIteratorValues(this, d.getMatches().iterator(), new Object[] { new Triple(a, p, b) });
        assertTrue(! di.hasNext());
    }
    
    /**
     * Test example parameter setting
     */
    public void testParameters() {
        Graph data = new GraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(p, ty, s));

        Model m = ModelFactory.createDefaultModel();
        Resource configuration= m.createResource(GenericRuleReasonerFactory.URI);
        configuration.addProperty(ReasonerVocabulary.PROPderivationLogging, "true");
        configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
        configuration.addProperty(ReasonerVocabulary.PROPruleSet, "file:testing/reasoners/genericRuleTest.rules");
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(configuration);
        
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, p, null), new Object[] {
                  new Triple(a, p, a),
                  new Triple(a, p, b),
                  new Triple(b, p, a)
              } );
              
        // Check derivation tracing as well
        Iterator di = infgraph.getDerivation(new Triple(b, p, a));
        assertTrue(di.hasNext());
        RuleDerivation d = (RuleDerivation)di.next();
        assertTrue(d.getRule().getName().equals("r1b"));
        TestUtil.assertIteratorValues(this, d.getMatches().iterator(), new Object[] { new Triple(a, p, b) });
        assertTrue(! di.hasNext());
    }

    /**
     * Test add/remove support
     */
    public void testAddRemove() {
        doTestAddRemove(false);
        doTestAddRemove(true);
    }
    
    /**
     * Internals of add/remove test.
     * @param useTGC set to true to use transitive caching
     */
    public void doTestAddRemove(boolean useTGC) {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, C1));
        data.add(new Triple(C1, sC, C2));
        data.add(new Triple(C2, sC, C3));
        List rules = Rule.parseRules(
        "[r1: (?x p ?c) -> (?x rdf:type ?c)] " +
        "[rdfs9:  (?x rdfs:subClassOf ?y) -> [ (?a rdf:type ?y) <- (?a rdf:type ?x)] ]"
                          );
        if (!useTGC) {
            rules.add(Rule.parseRule("[rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)] "));        
        }
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(rules);
//        reasoner.setTraceOn(true);
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        reasoner.setTransitiveClosureCaching(useTGC);
        
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(a, ty, null), new Object[] {
                  new Triple(a, ty, C1),
                  new Triple(a, ty, C2),
                  new Triple(a, ty, C3)
              } );
              
        logger.debug("Checkpoint 1");
        infgraph.delete(new Triple(C1, sC, C2));
        TestUtil.assertIteratorValues(this, 
              infgraph.find(a, ty, null), new Object[] {
                  new Triple(a, ty, C1)
              } );
         
        logger.debug("Checkpoint 2");
        infgraph.add(new Triple(C1, sC, C3));
        infgraph.add(new Triple(b, p, C2));
        TestUtil.assertIteratorValues(this, 
              infgraph.find(a, ty, null), new Object[] {
                  new Triple(a, ty, C1),
                  new Triple(a, ty, C3)
              } );
        TestUtil.assertIteratorValues(this, 
              infgraph.find(b, ty, null), new Object[] {
                  new Triple(b, ty, C2),
                  new Triple(b, ty, C3)
              } );
         
        TestUtil.assertIteratorValues(this, 
              data.find(null, null, null), new Object[] {
                  new Triple(a, p, C1),
                  new Triple(b, p, C2),
                  new Triple(C2, sC, C3),
                  new Triple(C1, sC, C3)
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