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

import java.util.*;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.reasoner.BaseInfGraph;
import com.hp.hpl.jena.reasoner.ReasonerFactory;

/**
 * We do not support DAML inference. This is a slightly extended version
 * of the RDFS reasoner to support some interesting subsets of DAML
 * that correspond roughly to what was there in Jena1. We hope.
 *
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:38 $
 * @deprecated This DAML reasoner will be removed from future versions of Jena because it is obsolete.
 */
public class DAMLMicroReasoner  extends GenericRuleReasoner {

    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/daml-micro.rules";

    /** The parsed rules */
    protected static List<Rule> ruleSet;

    /**
     * Constructor
     */
    public DAMLMicroReasoner(ReasonerFactory parent) {
        super(loadRules(), parent);
        setMode(HYBRID);
        setTransitiveClosureCaching(true);
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
