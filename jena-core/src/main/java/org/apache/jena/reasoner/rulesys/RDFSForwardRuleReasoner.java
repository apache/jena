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

package org.apache.jena.reasoner.rulesys;

import java.util.*;

import org.apache.jena.graph.Capabilities ;
import org.apache.jena.reasoner.BaseInfGraph ;
import org.apache.jena.reasoner.ReasonerFactory ;

/** 
 * A pure forward chaining implementation of the RDFS closure rules
 * based upon the basic forward rule interpreter. The normal mixed
 * forward/backward implementation is generally preferred but this has 
 * two possible uses. First, it is a test and demonstration of the forward
 * chainer. Second, if you want all the RDFS entailments for an entire 
 * dataset the forward chainer will be more efficient.
 */

public class RDFSForwardRuleReasoner extends GenericRuleReasoner {    
    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/rdfs.rules";
//    public static final String RULE_FILE = "etc/rdfs-noresource.rules";
    
    /** The parsed rules */
    protected static List<Rule> ruleSet;
    
    /**
     * Constructor
     */
    public RDFSForwardRuleReasoner(ReasonerFactory parent) {
        super(loadRules(), parent);
//        setMode(FORWARD_RETE);
        setMode(FORWARD);
    }
    
    /**
     * Return the RDFS rule set, loading it in if necessary
     */
    public static List<Rule> loadRules() {
        if (ruleSet == null) ruleSet = loadRules( RULE_FILE ); 
        return ruleSet;
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
