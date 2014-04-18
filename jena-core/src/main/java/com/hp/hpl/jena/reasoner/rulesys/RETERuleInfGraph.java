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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;

import java.util.*;

/**
 * RETE implementation of the forward rule infernce graph.
 */
public class RETERuleInfGraph extends BasicForwardRuleInfGraph {

    /**
     * Constructor. Creates a new inference graph to which a (compiled) rule set
     * and a data graph can be attached. This separation of binding is useful to allow
     * any configuration parameters (such as logging) to be set before the data is added.
     * Note that until the data is added using {@link #rebind rebind} then any operations
     * like add, remove, find will result in errors.
     * 
     * @param reasoner the parent reasoner 
     * @param schema the (optional) schema data which is being processed
     */
    public RETERuleInfGraph(Reasoner reasoner, Graph schema) {
        super(reasoner, schema);
    }    

    /**
     * Constructor. Creates a new inference graph based on the given rule set. 
     * No data graph is attached at this stage. This is to allow
     * any configuration parameters (such as logging) to be set before the data is added.
     * Note that until the data is added using {@link #rebind rebind} then any operations
     * like add, remove, find will result in errors.
     * 
     * @param reasoner the parent reasoner 
     * @param rules the list of rules to use this time
     * @param schema the (optional) schema or preload data which is being processed
     */
    public RETERuleInfGraph(Reasoner reasoner, List<Rule> rules, Graph schema) {
        super(reasoner, rules, schema);
    }    

     /**
      * Constructor. Creates a new inference graph based on the given rule set
      * then processes the initial data graph. No precomputed deductions are loaded.
      * 
      * @param reasoner the parent reasoner 
      * @param rules the list of rules to use this time
      * @param schema the (optional) schema or preload data which is being processed
      * @param data the data graph to be processed
      */
     public RETERuleInfGraph(Reasoner reasoner, List<Rule> rules, Graph schema, Graph data) {
         super(reasoner, rules, schema, data);
     }

    /**
     * Instantiate the forward rule engine to use.
     * Subclasses can override this to switch to, say, a RETE imlementation.
     * @param rules the rule set or null if there are not rules bound in yet.
     */
    @Override
    protected void instantiateRuleEngine(List<Rule> rules) {
        engine = FRuleEngineIFactory.getInstance().createFRuleEngineI(this, rules, true);
    }

    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    @Override
    public synchronized void performAdd(Triple t) {
        this.requirePrepared();
        fdata.getGraph().add(t);
        engine.add(t);
    }
    
    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    @Override
    public void performDelete(Triple t) {
        this.requirePrepared();
        if (fdata != null) {
            Graph data = fdata.getGraph();
            if (data != null) {
                data.delete(t);
            }
        }
        engine.delete(t);
        fdeductions.getGraph().delete(t);
    }

}
