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

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.reasoner.*;

import java.util.*;

/**
 * Reasoner configuration for the OWL micro reasoner.
 * This only supports:
 * <ul>
 * <li>RDFS entailments</li>
 * <li>basic OWL axioms like ObjectProperty subClassOf Property</li>
 * <li>intersectionOf, equivalentClass and forward implication of unionOf sufficient for traversal
 * of explicit class hierachies<.li>
 * <li>Property axioms (inversOf, SymmetricProperty, TransitiveProperty, equivalentProperty)</li>
 * </ul>
 * There is some experimental support for the cheaper class restriction handlingly which
 * should not be relied on at this point.
 */
public class OWLMicroReasoner extends GenericRuleReasoner implements Reasoner {

    /** The location of the OWL rule definitions on the class path */
    protected static final String MICRO_RULE_FILE = "etc/owl-fb-micro.rules";
    
    /** The parsed rules */
    protected static List<Rule> microRuleSet;
    
    /**
     * Return the rule set, loading it in if necessary
     */
    public static List<Rule> loadRules() {
        if (microRuleSet == null) microRuleSet = loadRules( MICRO_RULE_FILE );
        return microRuleSet;
    }
    
    
    /**
     * Constructor
     */
    public OWLMicroReasoner(ReasonerFactory factory) {
        super(loadRules(), factory);
        setOWLTranslation(true);
        setMode(HYBRID);
        setTransitiveClosureCaching(true);
    }
    

    /**
     * Return the Jena Graph Capabilties that the inference graphs generated
     * by this reasoner are expected to conform to.
     */
    @Override
    public Capabilities getGraphCapabilities() {
        if (capabilities == null) {
            capabilities = new BaseInfGraph.InfFindSafeCapabilities();
        }
        return capabilities;
    }
    
    /**
     * Attach the reasoner to a set of RDF data to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * 
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    @Override
    public InfGraph bind(Graph data) throws ReasonerException {
        InfGraph graph = super.bind(data);
        ((FBRuleInfGraph)graph).setDatatypeRangeValidation(true);
        return graph;
    }

}
