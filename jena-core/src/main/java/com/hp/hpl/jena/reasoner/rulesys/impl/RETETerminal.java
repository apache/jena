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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The final node in a RETE graph. It runs the builtin guard clauses
 * and then, if the token passes, executes the head operations.
 */
public class RETETerminal implements RETESinkNode {

    /** Context containing the specific rule and parent graph */
    protected RETERuleContext context;
    
    protected static Logger logger = LoggerFactory.getLogger(FRuleEngine.class);
    
    /**
     * Constructor.
     * @param rule the rule which this terminal should fire.
     * @param engine the parent rule engine through which the deductions and recursive network can be reached.
     * @param graph the wider encompasing infGraph needed to for the RuleContext
     */
    public RETETerminal(Rule rule, RETEEngine engine, ForwardRuleInfGraphI graph) {
        context = new RETERuleContext(graph, engine);
        context.rule = rule;
    }
    
    /**
     * Constructor. Used internally for cloning.
     * @param rule the rule which this terminal should fire.
     * @param engine the parent rule engine through which the deductions and recursive network can be reached.
     * @param graph the wider encompasing infGraph needed to for the RuleContext
     */
    protected RETETerminal(RETERuleContext context) {
        this.context = context;
    }
    
    /**
     * Change the engine/graph to which this terminal should deliver its results.
     */
    public void setContext(RETEEngine engine, ForwardRuleInfGraphI graph) {
        Rule rule = context.getRule();
        context = new RETERuleContext(graph, engine);
        context.setRule(rule);
    }
    
    /** 
     * Propagate a token to this node.
     * @param env a set of variable bindings for the rule being processed. 
     * @param isAdd distinguishes between add and remove operations.
     */
    @Override
    public void fire(BindingVector env, boolean isAdd) {
        Rule rule = context.getRule();
        context.setEnv(env);
        
        if (! context.shouldFire(isAdd)) return;

        // Now fire the rule
        context.getEngine().requestRuleFiring(rule, env, isAdd);
    }
    
    /**
     * Clone this node in the network.
     * @param netCopy a map from RETENode to cloned instance
     * @param contextIn the new context to which the network is being ported
     */
    
    @Override
    public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext contextIn) {
        RETETerminal clone = (RETETerminal)netCopy.get(this);
        if (clone == null) {
            RETERuleContext newContext = new RETERuleContext((ForwardRuleInfGraphI)contextIn.getGraph(), contextIn.getEngine());
            newContext.setRule(context.getRule());
            clone = new RETETerminal(newContext);
            netCopy.put(this, clone);
        }
        return clone;
    }
    
}
