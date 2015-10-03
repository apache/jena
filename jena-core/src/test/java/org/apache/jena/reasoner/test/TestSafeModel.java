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

package org.apache.jena.reasoner.test;

import java.util.List;

import static org.apache.jena.util.PrintUtil.egNS ;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner ;
import org.apache.jena.reasoner.rulesys.Rule ;
import org.apache.jena.reasoner.rulesys.impl.SafeGraph ;

/**
 * Some Jena reasoners support extended graphs which relax the RDF syntactic constraints
 * against literals in the subject position. By default getDeductionsModel in those
 * cases will return a SafeModel 
 */
public class TestSafeModel  extends TestCase {
    
    /**
     * Boilerplate for junit
     */ 
    public TestSafeModel( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestSafeModel.class);
    }  

    /**
     * Create a generalized model via inference and check it is
     * safe but unwrappable
     */
    public void testBasics() {
        Model base = ModelFactory.createDefaultModel();
        Resource r = base.createResource(egNS + "r");
        Property p = base.createProperty(egNS + "p");
        Property q = base.createProperty(egNS + "q");
        Literal l = base.createLiteral("foo");
        Statement asserted = base.createStatement(r, p, l);
        r.addProperty(p, l);
        
        List<Rule> rules = Rule.parseRules("(?r eg:p ?v) -> (?v eg:q ?r).");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel inf = ModelFactory.createInfModel(reasoner, base);
        TestUtil.assertIteratorValues(this, inf.listStatements(), new Statement[]{asserted});
        
        Model deductions = inf.getDeductionsModel();
        TestUtil.assertIteratorValues(this, deductions.listStatements(), new Statement[]{});
        
        Graph safeGraph = deductions.getGraph();
        assertTrue(safeGraph instanceof SafeGraph);
        
        Graph rawGraph = ((SafeGraph)safeGraph).getRawGraph();
        Triple deduction = new Triple(l.asNode(), q.asNode(), r.asNode());
        TestUtil.assertIteratorValues(this, 
                rawGraph.find(Node.ANY, Node.ANY, Node.ANY), 
                new Triple[]{deduction});
    }

}
