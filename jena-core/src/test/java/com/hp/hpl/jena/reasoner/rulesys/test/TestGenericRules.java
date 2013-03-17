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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner.RuleMode ;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import com.hp.hpl.jena.graph.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the packaging of all the reasoners into the GenericRuleReasoner.
 * The other tests check out this engine. These tests just need to touch 
 * enough to validate the packaging.
 */
public class TestGenericRules extends TestCase {
    
    protected static Logger logger = LoggerFactory.getLogger(TestFBRules.class);

    // Useful constants
    Node p = NodeFactory.createURI("p");
    Node q = NodeFactory.createURI("q");
    Node r = NodeFactory.createURI("r");
    Node s = NodeFactory.createURI("s");
    Node t = NodeFactory.createURI("t");
    Node a = NodeFactory.createURI("a");
    Node b = NodeFactory.createURI("b");
    Node c = NodeFactory.createURI("c");
    Node d = NodeFactory.createURI("d");
    Node C1 = NodeFactory.createURI("C1");
    Node C2 = NodeFactory.createURI("C2");
    Node C3 = NodeFactory.createURI("C3");
    Node ty = RDF.Nodes.type;
    Node sC = RDFS.Nodes.subClassOf;

    List<Rule> ruleList = Rule.parseRules("[r1: (?a p ?b), (?b p ?c) -> (?a p ?c)]" +
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
//        suite.addTest(new TestGenericRules( "testFunctorLooping" ));
//        return suite;
    }  
    
     
    /**
     * Minimal rule tester to check basic pattern match, forward style.
     */
    public void testForward() {
        Graph test = Factory.createGraphMem();
        test.add(new Triple(a, p, b));
        test.add(new Triple(b, p, c));
        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(ruleList);
        reasoner.setMode(GenericRuleReasoner.FORWARD);
        
        // Check data bind version
        InfGraph infgraph = reasoner.bind(test);
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
        
        // Check schema bind version
        infgraph = reasoner.bindSchema(test).bind(Factory.createGraphMem());
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
    }
     
    /**
     * Minimal rule tester to check basic pattern match, backward style.
     */
    public void testBackward() {
        Graph test = Factory.createGraphMem();
        test.add(new Triple(a, p, b));
        test.add(new Triple(b, p, c));
        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(ruleList);
        reasoner.setMode(GenericRuleReasoner.BACKWARD);
        
        // Check data bind version
        InfGraph infgraph = reasoner.bind(test);
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
        
        // Check schema bind version
        infgraph = reasoner.bindSchema(test).bind(Factory.createGraphMem());
        TestUtil.assertIteratorValues(this, infgraph.find(null, p, null), ans);
    }
    
    /**
     * Test example hybrid rule.
     */
    public void testHybrid() {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(p, ty, s));
        List<Rule> rules = Rule.parseRules(
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
        Iterator<Derivation> di = infgraph.getDerivation(new Triple(b, p, a));
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
     * Test early detection of illegal backward rules.
     */
    public void testBRuleErrorHandling() {
        Graph data = Factory.createGraphMem();
        List<Rule> rules = Rule.parseRules(
                    "[a1: -> [(?x eg:p ?y) (?x eg:q ?y) <- (?x eg:r ?y)]]"
                );
        boolean foundException = false;
        try {
            GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
            reasoner.setRules(rules);
            reasoner.setMode(GenericRuleReasoner.HYBRID);
            InfGraph infgraph = reasoner.bind(data);
            infgraph.prepare();
        } catch (ReasonerException e) {
            foundException = true;
        }
        assertTrue("Catching use of multi-headed brules", foundException);
    }
    
    /**
     * Test example parameter setting
     */
    public void testParameters() {
        Graph data = Factory.createGraphMem();
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
        Iterator<Derivation> di = infgraph.getDerivation(new Triple(b, p, a));
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
        
        // Mutiple rule file loading
        m = ModelFactory.createDefaultModel();
        configuration= m.createResource(GenericRuleReasonerFactory.URI);
        configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
        configuration.addProperty(ReasonerVocabulary.PROPruleSet, "testing/reasoners/ruleset1.rules");
        configuration.addProperty(ReasonerVocabulary.PROPruleSet, "testing/reasoners/ruleset2.rules");
        reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(configuration);
        
        infgraph = reasoner.bind(Factory.createGraphMem());
        Node an = NodeFactory.createURI(PrintUtil.egNS + "a");
        Node C = NodeFactory.createURI(PrintUtil.egNS + "C");
        Node D = NodeFactory.createURI(PrintUtil.egNS + "D");
        TestUtil.assertIteratorValues(this, 
              infgraph.find(null, null, null), new Object[] {
                new Triple(an, RDF.Nodes.type, C),
                new Triple(an, RDF.Nodes.type, D),
              } );
        
        // Test that the parameter initialization is not be overridden by subclasses
        m = ModelFactory.createDefaultModel();
        configuration = m.createResource(GenericRuleReasonerFactory.URI);
        configuration.addProperty( ReasonerVocabulary.PROPenableTGCCaching, m.createLiteral("true") );
        reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(configuration);
        InfModel im = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        Resource Ac = im.createResource(PrintUtil.egNS + "A");
        Resource Bc = im.createResource(PrintUtil.egNS + "B");
        Resource Cc = im.createResource(PrintUtil.egNS + "C");
        im.add(Ac, RDFS.subClassOf, Bc);
        im.add(Bc, RDFS.subClassOf, Cc);
        assertTrue("TGC enabled correctly", im.contains(Ac, RDFS.subClassOf, Cc));
        
     }
    
    /**
     * Check that the use of typed literals in the configuration also works
     */
    public void testTypedConfigParameters() {
        Model m = ModelFactory.createDefaultModel();
        Resource configuration= m.createResource(GenericRuleReasonerFactory.URI);
        configuration.addProperty(ReasonerVocabulary.PROPenableTGCCaching, m.createTypedLiteral(Boolean.TRUE));
        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(configuration);
        InfModel im = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        Resource Ac = im.createResource(PrintUtil.egNS + "A");
        Resource Bc = im.createResource(PrintUtil.egNS + "B");
        Resource Cc = im.createResource(PrintUtil.egNS + "C");
        im.add(Ac, RDFS.subClassOf, Bc);
        im.add(Bc, RDFS.subClassOf, Cc);
        assertTrue("TGC enabled correctly", im.contains(Ac, RDFS.subClassOf, Cc));
    }
    
    /**
     * Test control of functor filtering
     */
    public void testHybridFunctorFilter() {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, r, b));
        data.add(new Triple(a, p, s));
        List<Rule> rules = Rule.parseRules( "[r0: (?x r ?y) (?x p ?z) -> (?x q func(?y, ?z)) ]" );        
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
     * Test recursive rules involving functors
     * May lock up in there is a bug.
     */
    public void testFunctorLooping() {
        doTestFunctorLooping(GenericRuleReasoner.FORWARD_RETE);
        doTestFunctorLooping(GenericRuleReasoner.HYBRID);
    }
    
    /**
     * Test recursive rules involving functors.
     * May lock up in there is a bug.
     * TODO: arrange test to run in a separate thread with a timeout
     */
    public void doTestFunctorLooping(RuleMode mode) {
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, r, b));
        List<Rule> rules = Rule.parseRules( "(?x r ?y) -> (?x p func(?x)). (?x p ?y) -> (?x p func(?x))." );        
        GenericRuleReasoner reasoner = (GenericRuleReasoner)GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setRules(rules);
        reasoner.setMode(mode);
        
        InfGraph infgraph = reasoner.bind(data);
        // The p should have been asserted but is invisible
        assertFalse( infgraph.contains(Node.ANY, p, Node.ANY) );
    }
    
    /**
     * Test the @prefix and @include extensions to the rule parser
     */
    public void testExtendedRuleParser() {
        List<Rule> rules = Rule.rulesFromURL("file:testing/reasoners/ruleParserTest1.rules");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setTransitiveClosureCaching(true);
        Model base = ModelFactory.createDefaultModel();
        InfModel m = ModelFactory.createInfModel(reasoner, base);
        
        // Check prefix case
        String NS1 = "http://jena.hpl.hp.com/newprefix#";
        String NS2 = "http://jena.hpl.hp.com/newprefix2#";
        String NS3 = "http://jena.hpl.hp.com/newprefix3#";
        Resource A = m.getResource(NS1 + "A"); 
        Resource C = m.getResource(NS1 + "C"); 
        Property p = m.getProperty(NS2 + "p");
        Property a = m.getProperty(NS3 + "a");
        Resource foo = m.getResource(NS1 + "foo");
        assertTrue("@prefix test", m.contains(A, p, foo));
        
        // Check RDFS rule inclusion
        assertTrue("@include RDFS test", m.contains(A, RDFS.subClassOf, C));
        assertTrue("@include test", m.contains(a,a,a));
    }

    /**
     * Test that @include supports fileManger redirections
     */
    public void testIncludeRedirect() {
        assertFalse( checkIncludeFound("file:testing/reasoners/importTest.rules") );
        LocationMapper lm = FileManager.get().getLocationMapper();
        lm.addAltEntry("file:testing/reasoners/includeAlt.rules", 
                     "file:testing/reasoners/include.rules");
        assertTrue( checkIncludeFound("file:testing/reasoners/importTest.rules") );
        lm.removeAltEntry("file:testing/reasoners/includeAlt.rules"); 
    }
    
    /**
     * Check whether the test included file has been found 
     */
    private boolean checkIncludeFound(String ruleSrc) {
        try {
            List<Rule> rules = Rule.rulesFromURL(ruleSrc);
            GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
            Model base = ModelFactory.createDefaultModel();
            InfModel m = ModelFactory.createInfModel(reasoner, base);
            
            // Check prefix case
            String NS3 = "http://jena.hpl.hp.com/newprefix3#";
            Property a = m.getProperty(NS3 + "a");
            return  m.contains(a,a,a);
        } catch (Exception e) {
            return false;
        }
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
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, C1));
        data.add(new Triple(C1, sC, C2));
        data.add(new Triple(C2, sC, C3));
        List<Rule> rules = Rule.parseRules(
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
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, Util.makeIntNode(0)));
        List<Rule> rules = Rule.parseRules(
                "(?x p ?v)-> (?x q inc(1, a)).\n" +
                "(?x p ?v)-> (?x q inc(1, b)).\n" +
                "(?x p ?v) (?x q inc(?i, ?t)) noValue(?x r ?t) sum(?v, ?i, ?s) -> remove(0,1), (?x p ?s) (?x r ?t).\n");
        
        // Older version, relied on implicit rule ordering in Jena2.2 not value in 2.3
//        "(?x p ?v) noValue(a r 1) -> (?x q inc(1, a)) (?x r 1).\n" +
//        "(?x p ?v) noValue(a r 2) -> (?x q inc(1, b)) (?x r 2).\n" +
//        "(?x p ?v) (?x q inc(?i, ?t)) sum(?v, ?i, ?s) -> remove(0,1), (?x p ?s).\n");
        
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
