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
 * Reasoner configuration for the OWL mini reasoner.
 * Key limitations over the normal OWL configuration are:
 * <UL>
 * <li>omits the someValuesFrom => bNode entailments</li>
 * <li>avoids any guard clauses which would break the find() contract</li>
 * <li>omits inheritance of range implications for XSD datatype ranges</li>
 * </UL>
 */
public class OWLMiniReasoner extends GenericRuleReasoner implements Reasoner {

    /** The location of the OWL rule definitions on the class path */
    protected static final String MINI_RULE_FILE = "etc/owl-fb-mini.rules";
    
    /** The parsed rules */
    protected static List<Rule> miniRuleSet;
    
    /**
     * Return the rule set, loading it in if necessary
     */
    public static List<Rule> loadRules() {
        if (miniRuleSet == null) miniRuleSet = loadRules( MINI_RULE_FILE );
        return miniRuleSet;
    }
    
    
    /**
     * Constructor
     */
    public OWLMiniReasoner(ReasonerFactory factory) {
        super(loadRules(), factory);
        setOWLTranslation(true);
        setMode(HYBRID);
//        setTransitiveClosureCaching(true);
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

}
