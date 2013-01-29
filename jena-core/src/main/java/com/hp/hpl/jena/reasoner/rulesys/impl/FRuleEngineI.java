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

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.Finder;
//import java.util.List;

/**
 * Rule engines implement the internals of forward rule inference
 * graphs and the forward part of hybrid graphs. This interface
 * abstracts the interface onto such engines to allow a graph to
 * switch between direct and RETE style implementations.
 */
public interface FRuleEngineI {
    
    /**
     * Process all available data. This should be called once a deductions graph
     * has be prepared and loaded with any precomputed deductions. It will process
     * the rule axioms and all relevant existing exiting data entries.
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     * @param inserts the set of triples to be processed, normally this is the
     * raw data graph but may include additional deductions made by preprocessing hooks
     */
    public void init(boolean ignoreBrules, Finder inserts);
    
    /**
     * Process all available data. This version expects that all the axioms 
     * have already be preprocessed and the rules have been compiled
     * @param inserts the set of triples to be processed, normally this is the
     * raw data graph but may include additional deductions made by preprocessing hooks
     */
    public void fastInit(Finder inserts);
    
    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public void add(Triple t);
    
    /**
     * Remove one triple to the data graph.
     * @return true if the effects could be correctly propagated or
     * false if not (in which case the entire engine should be restarted).
     */
    public boolean delete(Triple t);
    
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized
     */
    public long getNRulesFired();
    
    /**
     * Return true if the internal engine state means that tracing is worthwhile.
     * It will return false during the axiom bootstrap phase.
     */
    public boolean shouldTrace();

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations);
    
    /**
     * Access the precomputed internal rule form. Used when precomputing the
     * internal axiom closures.
     */
    public Object getRuleStore();
    
    /**
     * Set the internal rule from from a precomputed state.
     */
    public void setRuleStore(Object ruleStore);
    
    /**
     * Compile a list of rules into the internal rule store representation.
     * @param rules the list of Rule objects
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     * @return an object that can be installed into the engine using setRuleStore.
     */
//    public void compile(List rules, boolean ignoreBrules);

}
