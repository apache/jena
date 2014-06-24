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
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;
import java.io.*;

/**
 * Unit tests for simple infrastructure pieces of the rule systems.
 */
public class TestBasics extends TestCase  {
    
    // Maximum size of binding environment needed in the tests
    private static final int MAX_VARS = 10;
    
    // Useful constants
    Node p = NodeFactory.createURI("p");
    Node q = NodeFactory.createURI("q");
    Node r = NodeFactory.createURI("r");
    Node s = NodeFactory.createURI("s");
    Node n1 = NodeFactory.createURI("n1");
    Node n2 = NodeFactory.createURI("n2");
    Node n3 = NodeFactory.createURI("n3");
    Node n4 = NodeFactory.createURI("n4");
    Node n5 = NodeFactory.createURI("n5");
    Node res = NodeFactory.createURI("res");
        
     
    /**
     * Boilerplate for junit
     */ 
    public TestBasics( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestBasics.class ); 
    }  

    /**
     * Test the internal rule parser
     */
    public void testRuleParser() {
       String[] testRules = new String[] {
            "(?a rdf:type ?_) -> (?a rdf:type ?b).",
            "(?a rdf:type ?_), (?a rdf:type ?_) -> (?a rdf:type ?b).",
            "(?a rdf:type max(?a,1)) -> (?a rdf:type 'foo').",
            "(?a rdf:type ?_) -> addOne(?a).",
            "(?a rdf:type ?_) -> [(?a rdf:type ?_) -> addOne(?a)].",
           "(?a rdf:type ?_) -> (?a rdf:type '42').",
           "(?a rdf:type ?_) -> (?a rdf:type 4.2).",
           "(?a rdf:type ?_) -> (?a rdf:type ' fool that,I(am)').",
            "[rule1: (?a rdf:type ?_) -> (?a rdf:type a)]",
            "-> print(' ').",
            "-> print(' literal with embedded \\' characters ').",
            "-> print(\" literal characters \").",
            "-> print(42). ",
            "-> print('42'^^xsd:byte). ",
            "-> print('42'^^http://www.w3.org/2001/XMLSchema#int). ",
            "-> print('42'^^foobar:byte). ",
            "-> print(<foo://a/file>). "
        };
        String[] testResults = new String[] {
            "[ (?a rdf:type ?_) -> (?a rdf:type ?b) ]",
            "[ (?a rdf:type ?_) (?a rdf:type ?_) -> (?a rdf:type ?b) ]",
            "[ (?a rdf:type 'max(?a '1'^^http://www.w3.org/2001/XMLSchema#int)'^^urn:x-hp-jena:Functor) -> (?a rdf:type 'foo') ]",
            "[ (?a rdf:type ?_) -> addOne(?a) ]",
            "[ (?a rdf:type ?_) -> [ (?a rdf:type ?_) -> addOne(?a) ] ]",
            "[ (?a rdf:type ?_) -> (?a rdf:type '42') ]",
            "[ (?a rdf:type ?_) -> (?a rdf:type '4.2'^^http://www.w3.org/2001/XMLSchema#float) ]",
            "[ (?a rdf:type ?_) -> (?a rdf:type ' fool that,I(am)') ]",
            "[ rule1: (?a rdf:type ?_) -> (?a rdf:type a) ]",
            "[ -> print(' ') ]",
            "[ -> print(' literal with embedded ' characters ') ]",
            "[ -> print(' literal characters ') ]",
            "[ -> print('42'^^http://www.w3.org/2001/XMLSchema#int) ]",
            "[ -> print('42'^^http://www.w3.org/2001/XMLSchema#byte) ]",
            "[ -> print('42'^^http://www.w3.org/2001/XMLSchema#int) ]",
            "[ -> print('42'^^http://foobar#byte) ]",
            "[ -> print(foo://a/file) ]",
        };
        
        PrintUtil.registerPrefix("foobar", "http://foobar#");
        for (int i = 0; i < testRules.length; i++) {
            Rule r = Rule.parseRule(testRules[i]);
            assertEquals(testResults[i], r.toString());
        }
        
        // Test for an illegal rule format
        String[] testBadRules = new String[] {
                "(foo(?A) eg:p ?B) <- (?a, eg:p, ?B)." ,
                "(foo(?A) eg:p ?B) -> (?a, eg:p, ?B)." 
        };
        for ( String testBadRule : testBadRules )
        {
            boolean foundError = false;
            try
            {
                Rule r = Rule.parseRule( testBadRule );
            }
            catch ( Rule.ParserException e )
            {
                foundError = true;
            }
            assertTrue( "Failed to find illegal rule", foundError );
        }
    }

    /**
     * Test rule equality operations.
     */
    public void testRuleEquality() {
        Rule r1 = Rule.parseRule("(?a p ?b) -> (?a q ?b).");
        Rule r2 = Rule.parseRule("(?a p ?b) -> (?b q ?a).");
        Rule r1b = Rule.parseRule("(?x p ?y) -> (?x q ?y).");
        Rule r3 = Rule.parseRule("(?a p ?b), addOne(?a) -> (?a q ?b).");
        Rule r3b = Rule.parseRule("(?c p ?d), addOne(?c) -> (?c q ?d).");
        Rule r4 = Rule.parseRule("(?a p ?b), makeTemp(?a) -> (?a q ?b).");
        Rule r5 = Rule.parseRule("(?a p ?b), addOne(?b) -> (?a q ?b).");
        Rule r6 = Rule.parseRule("(?a p ?b), addOne(p) -> (?a q ?b).");
        assertTrue(! r1.equals(r2));
        assertTrue(  r1.equals(r1b));
        assertTrue(! r1.equals(r3));
        assertTrue(  r3.equals(r3b));
        assertTrue(! r3.equals(r4));
        assertTrue(! r3.equals(r5));
        assertTrue(! r3.equals(r6));
    }
    
    /**
     * Test the BindingEnvironment machinery
     */
    public void testBindingEnvironment() {
        BindingStack env = new BindingStack();
        env.reset(MAX_VARS);
        
        env.bind(3, n1);
        assertEquals(n1, env.getEnvironment()[3]);
        env.push();
        env.bind(2, n2);
        assertEquals(n2, env.getEnvironment()[2]);
        env.unwind();
        assertEquals(null, env.getEnvironment()[2]);
        assertEquals(n1, env.getEnvironment()[3]);
        env.push();
        env.bind(1, n3);
        assertEquals(null, env.getEnvironment()[2]);
        assertEquals(n1, env.getEnvironment()[3]);
        assertEquals(n3, env.getEnvironment()[1]);
        env.unwind();
        assertEquals(null, env.getEnvironment()[2]);
        assertEquals(n1, env.getEnvironment()[3]);
        assertEquals(null, env.getEnvironment()[1]);
        env.push();
        env.bind(1, n3);
        assertEquals(null, env.getEnvironment()[2]);
        assertEquals(n1, env.getEnvironment()[3]);
        assertEquals(n3, env.getEnvironment()[1]);
        env.commit();
        assertEquals(null, env.getEnvironment()[2]);
        assertEquals(n1, env.getEnvironment()[3]);
        assertEquals(n3, env.getEnvironment()[1]);
        try {
            env.unwind();
            assertTrue("Failed to catch end of stack", false);
        } catch (IndexOutOfBoundsException e) {
        }
    }  
    
    /**
     * Test simple single clause binding
     */
    public void testClauseMaching() {
        BindingStack env = new BindingStack();
        env.reset(MAX_VARS);
        List<Rule> rules = new ArrayList<>();
        BasicForwardRuleInfGraph inf = new BasicForwardRuleInfGraph(
                                            new BasicForwardRuleReasoner(rules), rules, null);
        TriplePattern p1 = new TriplePattern(
                                    new Node_RuleVariable("?a", 0),
                                    n1,
                                    new Node_RuleVariable("?b", 1));
        TriplePattern p2 = new TriplePattern(
                                    new Node_RuleVariable("?b", 1),
                                    new Node_RuleVariable("?c", 2),
                                    n2);

        // Should fail with no bindings
        boolean match = FRuleEngine.match(p1, new Triple(n1, n2, n3), env);
        assertTrue(!match);
        assertEquals(null, env.getEnvironment()[0]);
        assertEquals(null, env.getEnvironment()[1]);
        assertEquals(null, env.getEnvironment()[2]);
        
        // Should succeed with two bindings
        match = FRuleEngine.match(p1, new Triple(n2, n1, n3), env);
        assertTrue(match);
        assertEquals(n2, env.getEnvironment()[0]);
        assertEquals(n3, env.getEnvironment()[1]);
        assertEquals(null, env.getEnvironment()[2]);
        
        // should fail but leave prior bindings intact
        match = FRuleEngine.match(p2, new Triple(n1, n2, n2), env);
        assertTrue(!match);
        assertEquals(n2, env.getEnvironment()[0]);
        assertEquals(n3, env.getEnvironment()[1]);
        assertEquals(null, env.getEnvironment()[2]);
        
        // should succeed with full binding set
        match = FRuleEngine.match(p2, new Triple(n3, n1, n2), env);
        assertTrue(match);
        assertEquals(n2, env.getEnvironment()[0]);
        assertEquals(n3, env.getEnvironment()[1]);
        assertEquals(n1, env.getEnvironment()[2]);
    }
      
    /**
     * Minimal rule tester to check basic pattern match
     */
    public void testRuleMatcher() {
        String rules = "[r1: (?a p ?b), (?b q ?c) -> (?a, q, ?c)]" +
                       "[r2: (?a p ?b), (?b p ?c) -> (?a, p, ?c)]" +
                       "[r3: (?a p ?a), (n1 p ?c), (n1, p, ?a) -> (?a, p, ?c)]" +
                       "[r4: (n4 ?p ?a) -> (n4, ?a, ?p)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(Factory.createGraphMem());
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
     * Test derivation machinery
     */
    public void testRuleDerivations() {
        String rules = "[testRule1: (n1 p ?a) -> (n2, p, ?a)]" +
                       "[testRule2: (n1 q ?a) -> (n2, q, ?a)]" +
                       "[testRule3: (n2 p ?a), (n2 q ?a) -> (res p ?a)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(Factory.createGraphMem());
        infgraph.setDerivationLogging(true);
        infgraph.add(new Triple(n1, p, n3));
        infgraph.add(new Triple(n1, q, n4));
        infgraph.add(new Triple(n1, q, n3));

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
        
        Iterator<Derivation> derivs = infgraph.getDerivation(new Triple(res, p, n3));
        StringWriter outString = new StringWriter(250);
        PrintWriter out = new PrintWriter(outString);
        while (derivs.hasNext()) {
            Derivation d = derivs.next();
            d.printTrace(out, true);
        }
        out.flush();
        
        String testString = TestUtil.normalizeWhiteSpace("Rule testRule3 concluded (res p n3) <-\n" +
                "    Rule testRule1 concluded (n2 p n3) <-\n" +
                "        Fact (n1 p n3)\r\n" +
                "    Rule testRule2 concluded (n2 q n3) <-\n" +
                "        Fact (n1 q n3)\r\n");
        assertEquals(testString, TestUtil.normalizeWhiteSpace(outString.getBuffer().toString()));
    }
    
          
    /**
     * Test axiom handling machinery
     */
    public void testAxiomHandling() {
        String rules = "[testRule1: (n1 p ?a) -> (n2, p, ?a)]" +
                       "[testRule2: (n1 q ?a) -> (n2, q, ?a)]" +
                       "[testRule3: (n2 p ?a), (n2 q ?a) -> (res p ?a)]" +
                       "[axiom1: -> (n1 p n3)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(Factory.createGraphMem());
        TestUtil.assertIteratorValues(this, infgraph.find(null, null, null),
            new Triple[] {
                new Triple(n1, p, n3),
                new Triple(n2, p, n3),
            });
        
        infgraph.add(new Triple(n1, q, n4));
        infgraph.add(new Triple(n1, q, n3));

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
     * Test schema partial binding machinery
     */
    public void testSchemaBinding() {
        String rules = "[testRule1: (n1 p ?a) -> (n2, p, ?a)]" +
                       "[testRule2: (n1 q ?a) -> (n2, q, ?a)]" +
                       "[testRule3: (n2 p ?a), (n2 q ?a) -> (res p ?a)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        Graph schema = Factory.createGraphMem();
        schema.add(new Triple(n1, p, n3));
        Graph data = Factory.createGraphMem();
        data.add(new Triple(n1, q, n4));
        data.add(new Triple(n1, q, n3));
        
        Reasoner reasoner =  new BasicForwardRuleReasoner(ruleList);
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
     * Test functor handling
     */
    public void testEmbeddedFunctors() {
        String rules = "(?C owl:onProperty ?P), (?C owl:allValuesFrom ?D) -> (?C rb:restriction all(?P, ?D))." +
                       "(?C rb:restriction all(eg:p, eg:D)) -> (?C rb:restriction 'allOK')." +
                       "[ -> (eg:foo eg:prop functor(eg:bar, 1)) ]" +
                       "[ (?x eg:prop functor(eg:bar, ?v)) -> (?x eg:propbar ?v) ]" +
                       "[ (?x eg:prop functor(?v, ?*)) -> (?x eg:propfunc ?v) ]" +
                       "";
        List<Rule> ruleList = Rule.parseRules(rules);
        
        Model data = ModelFactory.createDefaultModel();
        Resource R1 = data.createResource(PrintUtil.egNS + "R1");
        Resource D = data.createResource(PrintUtil.egNS + "D");
        Property p = data.createProperty(PrintUtil.egNS, "p");
        Property prop = data.createProperty(PrintUtil.egNS, "prop");
        Property propbar = data.createProperty(PrintUtil.egNS, "propbar");
        Property propfunc = data.createProperty(PrintUtil.egNS, "propfunc");
        Property rbr = data.createProperty(ReasonerVocabulary.RBNamespace, "restriction");
        R1.addProperty(OWL.onProperty, p).addProperty(OWL.allValuesFrom, D);
        
        Reasoner reasoner =  new BasicForwardRuleReasoner(ruleList);
        InfGraph infgraph = reasoner.bind(data.getGraph());
        Model infModel = ModelFactory.createModelForGraph(infgraph);
        Resource foo = infModel.createResource(PrintUtil.egNS + "foo");
        Resource bar = infModel.createResource(PrintUtil.egNS + "bar");
        
        RDFNode flit = infModel.getResource(R1.getURI()).getRequiredProperty(rbr).getObject();
        assertNotNull(flit);
        assertEquals(flit.toString(), "allOK");
//        assertTrue(flit instanceof Literal);
//        Functor func = (Functor)((Literal)flit).getValue();
//        assertEquals("all", func.getName());
//        assertEquals(p.getNode(), func.getArgs()[0]);
//        assertEquals(D.getNode(), func.getArgs()[1]);
        
        Literal one = (Literal)foo.getRequiredProperty(propbar).getObject();
        assertEquals(new Integer(1), one.getValue());
    }
    
    /**
     * The the minimal machinery for supporting builtins
     */
    public void testBuiltins() {
        String rules =  //"[testRule1: (n1 ?p ?a) -> print('rule1test', ?p, ?a)]" +
                       "[r1: (n1 p ?x), addOne(?x, ?y) -> (n1 q ?y)]" +
                       "[r2: (n1 p ?x), lessThan(?x, 3) -> (n2 q ?x)]" +
                       "[axiom1: -> (n1 p 1)]" +
                       "[axiom2: -> (n1 p 4)]" +
                       "";
        List<Rule> ruleList = Rule.parseRules(rules);
        
        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(Factory.createGraphMem());
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
     * The the "remove" builtin
     */
    public void testRemoveBuiltin() {
        String rules =  
                       "[rule1: (?x p ?y), (?x q ?y) -> remove(0)]" +
                       "";
        List<Rule> ruleList = Rule.parseRules(rules);

        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(Factory.createGraphMem());
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
     * The the "drop" builtin
     */
    public void testDropBuiltin() {
        String rules =  
                       "[rule1: (?x p ?y) -> drop(0)]" +
                       "";
        List<Rule> ruleList = Rule.parseRules(rules);

        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(Factory.createGraphMem());
        infgraph.add(new Triple(n1, p, Util.makeIntNode(1)));
        infgraph.add(new Triple(n1, p, Util.makeIntNode(2)));
        infgraph.add(new Triple(n1, q, Util.makeIntNode(2)));
        
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, q, Util.makeIntNode(2))
            });
        
    }
    
    /**
     * Test the rebind operation.
     */
    public void testRebind() {
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        Graph data = Factory.createGraphMem();
        data.add(new Triple(n1, p, n2));
        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(data);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, n2),
                new Triple(n1, q, n2)
            });
        Graph ndata = Factory.createGraphMem();
        ndata.add(new Triple(n1, p, n3));
        infgraph.rebind(ndata);
        TestUtil.assertIteratorValues(this, infgraph.find(n1, null, null),
            new Triple[] {
                new Triple(n1, p, n3),
                new Triple(n1, q, n3)
            });
    }
     
    /**
     * Test size bug, used to blow up if size was called before any queries.
     */
    public void testSize() {
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        Graph data = Factory.createGraphMem();
        data.add(new Triple(n1, p, n2));
        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(data);
        assertEquals(infgraph.size(), 2);
    }
    
    /**
     * Check validity report implementation, there had been a stupid bug here.
     */
    public void testValidityReport() {
        StandardValidityReport report = new StandardValidityReport();
        report.add(false, "dummy", "dummy1");
        report.add(false, "dummy", "dummy3");
        assertTrue(report.isValid());
        report.add(true,  "dummy", "dummy2");
        assertTrue( ! report.isValid());
        
        report = new StandardValidityReport();
        report.add(false, "dummy", "dummy1");
        report.add(true,  "dummy", "dummy2");
        report.add(false, "dummy", "dummy3");
        assertTrue( ! report.isValid());

        report = new StandardValidityReport();
        report.add(new ValidityReport.Report(false, "dummy", "dummy1"));
        report.add(new ValidityReport.Report(true, "dummy", "dummy2"));
        report.add(new ValidityReport.Report(false, "dummy", "dummy3"));
        assertTrue( ! report.isValid());
    }
       
    /**
     * Test the list conversion utility that is used in some of the builtins.
     */
    public void testConvertList() {
        Graph data = Factory.createGraphMem();
        Node first = RDF.Nodes.first;
        Node rest  = RDF.Nodes.rest;
        Node nil = RDF.Nodes.nil;
        data.add(new Triple(n1, first, p));
        data.add(new Triple(n1, rest, n2));
        data.add(new Triple(n2, first, q));
        data.add(new Triple(n2, rest, nil));

        data.add(new Triple(n3, first, p));
        data.add(new Triple(n3, rest, n4));
        data.add(new Triple(n4, rest, n5));
        data.add(new Triple(n5, first, q));
        data.add(new Triple(n5, rest, nil));
        
        String rules = "[rule1: (?x p ?y) -> (?x q ?y)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        InfGraph infgraph = new BasicForwardRuleReasoner(ruleList).bind(data);
        
        RuleContext context = new BFRuleContext( (ForwardRuleInfGraphI) infgraph);
        List<Node> result = Util.convertList(n1, context);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), p);
        assertEquals(result.get(1), q);

        List<Node> result2 = Util.convertList(n3, context);
        assertEquals(result2.size(), 1);
        assertEquals(result2.get(0), p);
    }
     
}
