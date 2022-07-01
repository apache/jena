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

package org.apache.jena.reasoner.rulesys.impl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestLPBRuleCloseBug extends TestCase {
    public static TestSuite suite() {
        return new TestSuite(TestLPBRuleCloseBug.class);
    }
    
    /**
     * Test case for JENA-2184.
     */
    @Test
    public void testCloseOfTabledIterator() {
        Model m = ModelFactory.createDefaultModel();

        String data = StrUtils.strjoinNL("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> ."
                                        ,"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
                                        ,""
                                        ,"<urn:ic:x1> rdf:type <urn:ic:SUB> ."
                                        ,"<urn:ic:SUB> rdfs:subClassOf <urn:ic:CLASS> ."
                );
        m.read(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), "", "Turtle");

        String rules = "-> table(rdf:type). (?a rdf:type <urn:ic:CLASS>) <- (?a rdf:type <urn:ic:SUB>) .";
        Reasoner reasoner = new GenericRuleReasoner( Rule.parseRules(rules) );
        InfModel infModel = ModelFactory.createInfModel(reasoner.bind(m.getGraph()));
        Graph infGraph = infModel.getGraph();

        Node x1 = NodeFactory.createURI("urn:ic:x1");
        Node clsSUB = NodeFactory.createURI("urn:ic:SUB");
        Node clsCLASS = NodeFactory.createURI("urn:ic:CLASS");

        ExtendedIterator<Triple> sInfIter = infGraph.find(x1, RDF.Nodes.type, clsSUB);
        assertTrue( sInfIter.hasNext() );
        
        // Closing without having read from the iterator
        // Forces a close of LPInterpreter instances including on behind the tabled goal for the find 
        sInfIter.close();
        
        // This query depends on the above tabled goal which was not complete before the close()
        ExtendedIterator<Triple> cInfIter = infGraph.find(x1, RDF.Nodes.type, clsCLASS);
        boolean foundClass = cInfIter.hasNext();
        assertTrue( foundClass );
    }

}
