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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.ForwardRuleInfGraphI;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.SilentAddI;
import com.hp.hpl.jena.util.iterator.ClosableIterator;


public class SparqlInRulesRuleContext implements RuleContext{
   
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingEnvironment env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected InfGraph graph;
    
    
    /**
     * Constructor.
     * @param graph the inference graph which owns this context.
     */
    public SparqlInRulesRuleContext(InfGraph graph) {
        this.graph = graph;
    }
    
    /**
     * Returns the current variable binding environment for the current rule.
     * @return BindingEnvironment
     */
    @Override
    public BindingEnvironment getEnv() {
        return env;
    }

    /**
     * Returns the graph.
     * @return InfGraph
     */
    @Override
    public InfGraph getGraph() {
        return graph;
    }
    
    /**
     * Returns the rule.
     * @return Rule
     */
    @Override
    public Rule getRule() {
        return rule;
    }

    /**
     * Sets the rule.
     * @param rule The rule to set
     */
    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * Sets the current binding environment for this context.
     * @param env the binding environment so far
     */
    public void setEnv(BindingEnvironment env) {
        this.env = env;
    }
    
    /**
     * Return true if the triple is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    @Override
    public boolean contains(Triple t) {
        // Can't use stackCache.contains because that does not do semantic equality
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }
    
    /**
     * Return true if the triple pattern is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    @Override
    public boolean contains(Node s, Node p, Node o) {
        ClosableIterator<Triple> it = find(s, p, o);
        boolean result = it.hasNext();
        it.close();
        return result;
    }
    
    /**
     * In some formulations the context includes deductions that are not yet
     * visible to the underlying graph but need to be checked for.
     * However, currently this calls the graph find directly.
     */
    @Override
    public ClosableIterator<Triple> find(Node s, Node p, Node o) {
        //return graph.find(s, p, o).andThen(pendingCache.find(s, p, o));
        return graph.find(s, p, o);
    }
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    @Override
    public void silentAdd(Triple t) {
        ((SilentAddI)graph).silentAdd(t);
    }

    /**
     * Remove a triple from the deduction graph (and the original graph if relevant).
     */
    @Override
    public void remove(Triple t) {
        graph.getRawGraph().delete(t);
    }

    /**
     * Assert a new triple in the deduction graph, triggering any consequent processing as appropriate.
     */
    @Override
    public void add(Triple t) {
        silentAdd(t);
    }

    /**
     * Check whether the rule should fire in this context.
     */
    public boolean shouldFire(boolean allowUnsafe) {
        // Check any non-pattern clauses 
        for (int i = 0; i < rule.bodyLength(); i++) {
            Object clause = rule.getBodyElement(i);
            if (clause instanceof Functor) {
                // Fire a built in
                if (allowUnsafe) {
                    if (!((Functor)clause).evalAsBodyClause(this)) {
                        // Failed guard so just discard and return
                        return false;
                    }
                } else {
                    // Don't re-run side-effectful clause on a re-run
                    if (!((Functor)clause).safeEvalAsBodyClause(this)) {
                        // Failed guard so just discard and return
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Check if a rule from the conflict set is still OK to fire.
     * Just checks the non-monotonic guards such as noValue.
     */
    public boolean shouldStillFire() {
        // Check any non-pattern clauses 
        for (int i = 0; i < rule.bodyLength(); i++) {
            Object clause = rule.getBodyElement(i);
            if (clause instanceof Functor) {
                Builtin builtin = ((Functor)clause).getImplementor();
                if (builtin != null && !builtin.isMonotonic()) {
                    if (!((Functor)clause).evalAsBodyClause(this)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
        
}
