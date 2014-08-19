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

package com.hp.hpl.jena.reasoner.sparqlinrules.test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.Rule.SparqlRuleParserException;
import java.io.ByteArrayInputStream;
import java.util.List;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Test;



public class SparqlinRulesTest extends TestCase{
    Model m;
    
    public SparqlinRulesTest( String name ) {
        super( name ); 
        inic_data();
    }
    
    public SparqlinRulesTest(  ) {
        inic_data();
    }
    
    private void inic_data() {

        String myData = 
            "<eg:A> <eg:p> <eg:B> .\n" +
            "<eg:B> <eg:p> <eg:C> .\n" +
            "<eg:D> <eg:p> <eg:E> .\n" +
            "<eg:C> <eg:p> <eg:D> .";
       
        m = ModelFactory.createDefaultModel();
         
        m.read( new ByteArrayInputStream( myData.getBytes() ), null, "TTL" );
        
    }
    
    public static TestSuite suite() {
        return new TestSuite(SparqlinRulesTest1.class);
    }  

    
    public void run2() {
        System.out.println("test 1/5");
        sparqlInRule_01();
        System.out.println("test 2/5");
        sparqlInRule_02();
        System.out.println("test 3/5");
        sparqlInRule_03();
        System.out.println("test 4/5");
        sparqlInRule_04();
        System.out.println("test 5/5");
        sparqlInRule_05();

    }
    

  /**
     * Test of parseRule method, of class Rule.
     * 
     * @Test
    public void testParseRule() {
        testSparqlInRule();
    }
     */
    
    public void test_sparqlInRule() {
        sparqlInRule_01();
        sparqlInRule_02();
        sparqlInRule_03();
        sparqlInRule_04();
        sparqlInRule_05();
        
    }
    
    //@Test
    public void sparqlInRule_01() {
        //Sparql command without prefix. SPARQL command in the beginning of the body in a Forward rule
        String rule = "[rule1: (\\\\\\sparql Select ?a ?c where {?a <eg:p> ?c} \\\\\\sparql) -> (?a <eg:p> ?c)]";
        int ruleBodyLength = 1;
        parseRule_createReasoner(rule, ruleBodyLength);
    }
    
    //@Test
    public void sparqlInRule_02() {
        //Sparql command with prefix. SPARQL command in the beginning of the body in a Forward rule
        String rule = "[rule1: (\\\\\\sparql prefix : <eg:> Select ?a ?b where {?a :p ?b} \\\\\\sparql)  -> (?a <eg:p> ?c)]";
        int ruleBodyLength = 1;
        parseRule_createReasoner(rule, ruleBodyLength);       
    }
    
    //@Test 
    public void sparqlInRule_03() {
       //SPARQL command in the the body in a Backward rule
        String rule = "[rule1: (?a <eg:p> ?c) <- (\\\\\\sparql Select ?c ?d where {?c <eg:p> ?d} \\\\\\sparql) ]";
        int ruleBodyLength = 1;
        parseRule_createReasoner(rule, ruleBodyLength);       
    }
    
    //@Test 
    public void sparqlInRule_04() {
        //SPARQL command in the head in a Backward rule. Isn't allowed, returns an error. 
        String rule = "[rule1: (?a <eg:p> ?b) (?b <eg:p> ?c) -> (?a <eg:p> ?c) (\\\\\\sparql Select ?c ?d where {?c <eg:p> ?d} \\\\\\sparql) ]";
        int ruleBodyLength = 1;
        boolean giveError = false;
        try{
            parseRuleString(rule, ruleBodyLength);   
        }
        catch(SparqlRuleParserException err) {
            giveError = true;
        }
        assertTrue(giveError);      
    }
    
    //@Test
    public void sparqlInRule_05() {
        //SPARQL command in the head in a Forward rule. Isn't allowed, returns an error. 
        String rule = "[rule1: (?a <eg:p> ?b) (\\\\\\sparql Select ?c ?d where {?c <eg:p> ?d} \\\\\\sparql) <- (?a <eg:p> ?c) (?b <eg:p> ?c) ]";
        int ruleBodyLength = 1;
        boolean giveError = false;
        try{
            parseRuleString(rule, ruleBodyLength);   
        }
        catch(SparqlRuleParserException err) {
            giveError = true;
        }
        assertTrue(giveError);
    }
    
     private void parseRuleString(String ruleString, int expectedBodyLength) {
        // Exception will neatly fail the test in JUnit.
        List<Rule> rules = Rule.parseRules(ruleString);
        assertTrue(rules.get(0).bodyLength()==expectedBodyLength) ;
    }
    
    
    private void parseRule_createReasoner(String ruleString, int expectedBodyLength) {
        List<Rule> rules = Rule.parseRules(ruleString);
        assertTrue(rules.get(0).bodyLength()==expectedBodyLength);
        Reasoner reasoner = new GenericRuleReasoner(rules);
        InfModel inf = ModelFactory.createInfModel(reasoner, m);
    }
}
