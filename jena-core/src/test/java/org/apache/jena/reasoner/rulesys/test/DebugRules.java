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

package org.apache.jena.reasoner.rulesys.test;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.graph.GraphMemFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.reasoner.InfGraph ;
import org.apache.jena.reasoner.rulesys.BasicForwardRuleReasoner ;
import org.apache.jena.reasoner.rulesys.Rule ;
import org.apache.jena.reasoner.rulesys.Util ;
import org.apache.jena.util.PrintUtil ;

/** * Using during debuging of the rule systems.
 * Runs a named set of rules (can contain axioms and rules) and
 * lists all the resulting entailments.
 */
public class DebugRules {

    /** The name of the rule set to load */
    public static final String ruleFile = "etc/temp.rules";
    
    /** The parsed set of rules */
    public List<Rule> ruleset;
    
    /** Constructor - loads the rules */
    public DebugRules(String rulefileName) {
        ruleset = Rule.parseRules(Util.loadRuleParserFromResourceFile(rulefileName));
    }
    
    /** Run a single test */
    public void run() {
        
        BasicForwardRuleReasoner reasoner = new BasicForwardRuleReasoner(ruleset);
        InfGraph result = reasoner.bind(GraphMemFactory.createGraphMem());
        System.out.println("Final graph state");
        for (Iterator<Triple> i = result.find(null, null, null); i.hasNext(); ) {
            System.out.println(PrintUtil.print(i.next()));
        }
        
    }
    
    public static void main(String[] args) {
        try {
            DebugRules tester = new DebugRules(ruleFile);
            tester.run();
        } catch (Exception e) {
            System.out.println("Problem: " + e);
        }
    }
    
}
