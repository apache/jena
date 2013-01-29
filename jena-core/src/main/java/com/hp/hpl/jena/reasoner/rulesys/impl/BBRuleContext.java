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
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * Implementation of RuleContext for use in the backward chaining
 * interpreter. The RuleContext allows builtin predicates to 
 * interpret variable bindings to access the static triple data.
 */
public class BBRuleContext implements RuleContext {
    
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingEnvironment env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected BackwardRuleInfGraphI graph;
    
    /**
     * Construct an empty context. It can't be used until
     * the rule and environment have been set.
     */
    public BBRuleContext(BackwardRuleInfGraphI graph) {
        this.graph = graph;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#contains(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
     */
    @Override
    public boolean contains(Node s, Node p, Node o) {
        ClosableIterator<Triple> i = find(s, p, o);
        boolean result = i.hasNext();
        i.close();
        return result;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#contains(com.hp.hpl.jena.graph.Triple)
     */
    @Override
    public boolean contains(Triple t) {
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#find(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
     */
    @Override
    public ClosableIterator<Triple> find(Node s, Node p, Node o) {
        return graph.findDataMatches(new TriplePattern(s, p, o));
//        return searchpath.find(new TriplePattern(s, p, o));
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#getEnv()
     */
    @Override
    public BindingEnvironment getEnv() {
        return env;
    }

    /**
     * Set the binding environment for the this context
     */
    public void setEnv(BindingEnvironment env) {
        this.env = env;
    }
    
    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#getGraph()
     */
    @Override
    public InfGraph getGraph() {
        return graph;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#getRule()
     */
    @Override
    public Rule getRule() {
        return rule;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#setRule(com.hp.hpl.jena.reasoner.rulesys.Rule)
     */
    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    @Override
    public void silentAdd(Triple t) {
        ((SilentAddI)graph).silentAdd(t);
    }

    /**
     * Assert a new triple in the deduction graph, triggering any consequent processing as appropriate.
     * In the backward case there no immediate consequences so this is equivalent to a silentAdd.
     */
    @Override
    public void add(Triple t) {
        ((SilentAddI)graph).silentAdd(t);
    }

    /**
     * Remove a triple from the deduction graph (and the original graph if relevant).
     */
    @Override
    public void remove(Triple t) {
        graph.delete(t);
    }

    /**
     * Retrieve or create a bNode representing an inferred property value.
     * This is currently only available on backward contexts and not part of the 
     * normal RuleContext interface.
     * @param instance the base instance node to which the property applies
     * @param prop the property node whose value is being inferred
     * @param pclass the (optional, can be null) class for the inferred value.
     * @return the bNode representing the property value 
     */
    public Node getTemp(Node instance, Node prop, Node pclass) {
        return graph.getTemp(instance, prop, pclass);
    }

}
