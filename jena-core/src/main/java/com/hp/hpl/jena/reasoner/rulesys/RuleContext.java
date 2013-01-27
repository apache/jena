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

import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.graph.*;

/**
 * Interface used to convey context information from a rule engine
 * to the stack of procedural builtins. This gives access
 * to the triggering rule, the variable bindings and the set of
 * currently known triples. 
 */
public interface RuleContext {
    /**
     * Returns the current variable binding environment for the current rule.
     * @return BindingEnvironment
     */
    public BindingEnvironment getEnv();

    /**
     * Returns the parent inference graph.
     * @return InfGraph
     */
    public InfGraph getGraph();
    
    /**
     * Returns the rule.
     * @return Rule
     */
    public Rule getRule();

    /**
     * Sets the rule.
     * @param rule The rule to set
     */
    public void setRule(Rule rule);
    
    /**
     * Return true if the triple is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    public boolean contains(Triple t);
    
    /**
     * Return true if the triple pattern is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    public boolean contains(Node s, Node p, Node o);
    
    /**
     * In some formulations the context includes deductions that are not yet
     * visible to the underlying graph but need to be checked for.
     */
    public ClosableIterator<Triple> find(Node s, Node p, Node o);
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    public void silentAdd(Triple t);

    /**
     * Assert a new triple in the deduction graph, triggering any consequent processing as appropriate.
     */
    public void add(Triple t);
    
    /**
     * Remove a triple from the deduction graph (and the original graph if relevant).
     */
    public void remove(Triple t);
}
