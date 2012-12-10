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

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.util.PrintUtil;

import junit.framework.TestSuite;

public class TestInferenceReification extends AbstractTestReifier 
    {
    /**
     * Boilerplate for junit
     */ 
    public TestInferenceReification( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestInferenceReification.class ); 
    }  
    
    @Override
    public Graph getGraph()
        { return makeInfGraph( "", "" ); }

    /**
     * Case 1: Rules construct a reified statement, is that
     * visible as reified statement in the InfGraph?
     */
    public void testSimpleReification() {
        String rules =  
            "[r1: (?x eh:p ?o) -> (?o rdf:type rdf:Statement) (?o rdf:subject ?x)" +
            "                         (?o rdf:predicate eh:q) (?o rdf:object 42)]";
        Model m = makeInfModel( rules, "r1 p r" );
        TestUtil.assertIteratorLength( m.listReifiedStatements(), 1 );
    }
    
    private Reasoner ruleBaseReasoner()
        { return new FBRuleReasoner( Rule.parseRules( "" ) ); }
    
    public void testConstructingModelDoesntForcePreparation()
        {
        Model m = makeInfModel( "", "" );
        if (((BaseInfGraph) m.getGraph()).isPrepared()) fail();
        }
    
    /**
     * Case 1: Rules complete an exisiting partially reified statement.
     */
    public void SUPPRESStestReificationCompletion() {
        String rules =  
            "[r1: (?x rdf:subject ?s) (?x rdf:predicate ?p) -> (?x rdf:object eh:bar)]";
        Model m = makeInfModel(rules, "r1 rdf:type rdf:Statement; r1 rdf:subject foo; r1 rdf:predicate p" );
        RSIterator i = m.listReifiedStatements();
        assertTrue(i.hasNext());
        assertEquals( triple("foo p bar"), i.nextRS().getStatement().asTriple());
        assertFalse(i.hasNext());
    }
    
    /**
     * Internal helper: create an InfGraph with given rule set and base data.
     * The base data is encoded in kers-special RDF syntax.
     */    
    private InfGraph makeInfGraph(String rules, String data) {
        PrintUtil.registerPrefix("eh", "eh:/");
        Graph base = graphWith( data );
        List<Rule> ruleList = Rule.parseRules(rules);
        return new FBRuleReasoner(ruleList).bind( base );
    }
    
    /**
     * Internal helper: create a Model which wraps an InfGraph with given rule set and base data.
     * The base data is encoded in kers-special RDF syntax.
     */
        
    private Model makeInfModel( String rules, String data ) {
        return ModelFactory.createModelForGraph( makeInfGraph(rules, data ) );
    }
    
}
