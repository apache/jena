/******************************************************************
 * File:        TestGenericRules.java
 * Created by:  Dave Reynolds
 * Created on:  08-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestGenericRules.java,v 1.12 2004-02-20 11:22:10 der Exp $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test the packaging of all the reasoners into the GenericRuleReasoner.
 * The other tests check out this engine. These tests just need to touch 
 * enough to validate the packaging.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.12 $ on $Date: 2004-02-20 11:22:10 $
 */
public class TestGenericRules extends TestCase {
    
    protected static Log logger = LogFactory.getLog(TestFBRules.class);

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
                                    "[r2: (?a q ?b) -> (?a p ?c)]" +
                                    "-> table(p). -> table(q).");
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
//        suite.addTest(new TestGenericRules( "testAddRemove2" ));
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
        "[r2: (?p rdf:type s) -> [r2b: (?x ?p ?x) <- (?x rdf:type t)]]" +
        "-> tableAll()."
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
//        java.io.PrintWriter out = new java.io.PrintWriter(System.out); 
//        d.printTrace(out, true);
//        out.close();
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
        configuration.addProperty(ReasonerVocabulary.PROPruleSet, "testing/reasoners/genericRuleTest.rules");
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
        
        // Check retrieval of configuration
        Model m2 = ModelFactory.createDefaultModel();
        Resource newConfig = m2.createResource();
        reasoner.addDescription(m2, newConfig);
        TestUtil.assertIteratorValues(this, newConfig.listProperties(), new Statement[] {
            m2.createStatement(newConfig, ReasonerVocabulary.PROPderivationLogging, "true"),
            m2.createStatement(newConfig, ReasonerVocabulary.PROPruleMode, "hybrid"),
            m2.createStatement(newConfig, ReasonerVocabulary.PROPruleSet, "testing/reasoners/genericRuleTest.rules")
            } );
       
        // Manual reconfig and check retrieval of changes
        reasoner.setParameter(ReasonerVocabulary.PROPderivationLogging, "false");
        newConfig = m2.createResource();
        reasoner.addDescription(m2, newConfig);
        TestUtil.assertIteratorValues(this, newConfig.listProperties(), new Statement[] {
            m2.createStatement(newConfig, ReasonerVocabulary.PROPderivationLogging, "false"),
            m2.createStatement(newConfig, ReasonerVocabulary.PROPruleMode, "hybrid"),
            m2.createStatement(newConfig, ReasonerVocabulary.PROPruleSet, "testing/reasoners/genericRuleTest.rules")
            } );
    }
    
    /**
     * Test control of functor filtering
     */
    public void testHybridFunctorFilter() {
        Graph data = new GraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(a, p, s));
        List rules = Rule.parseRules( "[r0: (?x r ?y) (?x p ?z) -> (?x q func(?y, ?z)) ]" );        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(rules);
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, q, null), new Object[] {
              } );
              
        reasoner.setFunctorFiltering(false);
        infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, q, null), new Object[] {
                  new Triple(a, q, Functor.makeFunctorNode("func", new Node[]{b, s}))
              } );
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
        "-> table(rdf:type)." +
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
    
    /**
     * Resolve a bug using remove in rules themselves.
     */
    public void testAddRemove2() {
        Graph data = new GraphMem();
        data.add(new Triple(a, p, Util.makeIntNode(0)));
        List rules = Rule.parseRules(
        "(?x p ?v) noValue(a r 1) -> (?x p inc(1, a)) (?x r 1).\n" +
        "(?x p ?v) noValue(a r 2) -> (?x p inc(1, b)) (?x r 2).\n" +
        "(?x p ?v) (?x p inc(?i, ?t)) sum(?v, ?i, ?s) -> remove(0,1), (?x p ?s).\n");
        
        // This version doesn't work but its not clear if it should
//        List rules = Rule.parseRules(
//        "(?x p ?v) noValue(a r 1) addOne(?v, ?v2) -> remove(0) (?x p ?v2) (?x r 1).\n" +
//        "(?x p ?v) noValue(a r 2) addOne(?v, ?v2) -> remove(0) (?x p ?v2) (?x r 2).\n");
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(rules);
        reasoner.setMode(GenericRuleReasoner.FORWARD_RETE);
        
        InfGraph infgraph = reasoner.bind(data);
        TestUtil.assertIteratorValues(this, 
              infgraph.find(a, p, null), new Object[] {
                  new Triple(a, p, Util.makeIntNode(2))
              } );
    }
}



/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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