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

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

/**
 * Customization of the generic rule inference graph for RDFS inference.
 * In fact all the rule processing is unchanged, the only extenstion is
 * the validation support.
 */
public class RDFSRuleInfGraph extends FBRuleInfGraph {

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     */
    public RDFSRuleInfGraph(Reasoner reasoner, List<Rule> rules, Graph schema) {
        super(reasoner, rules, schema);
    }

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     * @param data the data graph to be processed
     */
    public RDFSRuleInfGraph(Reasoner reasoner, List<Rule> rules, Graph schema, Graph data) {
        super(reasoner, rules, schema, data);
    }
    
    /**
     * Test the consistency of the bound data. For RDFS this checks that all
     * instances of datatype-ranged properties have correct data values.
     * 
     * @return a ValidityReport structure
     */
    @Override
    public ValidityReport validate() {
        // The full configuration uses validation rules so check for these
        StandardValidityReport report = (StandardValidityReport)super.validate();
        // Also do a hardwired check to handle the simpler configurations
        performDatatypeRangeValidation(report);
        return report;
    }

}
