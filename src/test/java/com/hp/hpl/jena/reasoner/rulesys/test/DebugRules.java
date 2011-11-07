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

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.PrintUtil;

import java.util.*;
import java.io.*;

/** * Using during debuging of the rule systems.
 * Runs a named set of rules (can contain axioms and rules) and
 * lists all the resulting entailments.
 *  * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a> * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:42 $ */
public class DebugRules {

    /** The name of the rule set to load */
    public static final String ruleFile = "etc/temp.rules";
    
    /** The parsed set of rules */
    public List<Rule> ruleset;
    
    /** Constructor - loads the rules */
    public DebugRules(String rulefileName) throws IOException {
        ruleset = Rule.parseRules(Util.loadRuleParserFromResourceFile(rulefileName));
    }
    
    /** Run a single test */
    public void run() {
        
        BasicForwardRuleReasoner reasoner = new BasicForwardRuleReasoner(ruleset);
        InfGraph result = reasoner.bind(Factory.createGraphMem());
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
