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

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.graph.*;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the generic RuleContext interface used by
 * the basic forward (BF) rule engine. This provides additional
 * methods specific to the functioning of that engine.
 */
public class BFRuleContext implements RuleContext {
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingStack env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected ForwardRuleInfGraphI graph;
    
    /** A stack of triples which have been added to the graph but haven't yet been processed. */
    protected List<Triple> stack;
    
    /** A temporary list of Triples which will be added to the stack and triples at the end of a rule scan */
    protected List<Triple> pending;

    /** A temporary list of Triples which will be removed from the graph at the end of a rule scan */
    protected List<Triple> deletesPending = new ArrayList<>();

    /** A searchable index into the pending triples */
    protected Graph pendingCache;
    
    protected static Logger logger = LoggerFactory.getLogger(BFRuleContext.class);
    
    /**
     * Constructor.
     * @param graph the inference graph which owns this context.
     */
    public BFRuleContext(ForwardRuleInfGraphI graph) {
        this.graph = graph;
        env = new BindingStack();
        stack = new ArrayList<>();
        pending = new ArrayList<>();
        pendingCache = Factory.createGraphMem();
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
     * Variant of the generic getEnv interface specific to the basic
     * forward rule system.
     * Returns the current variable binding environment for the current rule.
     * @return BindingStack
     */
    public BindingStack getEnvStack() {
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
     * Add a triple to the stack of triples to waiting to be processed by the rule engine.
     */
    public void addTriple(Triple t) {
        if (graph.shouldTrace()) {
            if (rule != null) {
                logger.debug("Adding to stack (" + rule.toShortString() + "): " + PrintUtil.print(t));
            } else {
                logger.debug("Adding to stack : " + PrintUtil.print(t));
            }
        }
        stack.add(t);
    }
    
    /**
     * Add a triple to a temporary "pending" store, ready to be added to added to the
     * deductions graph and the processing stack later.
     * <p>This is needed to prevent concurrrent modification exceptions which searching
     * the deductions for matches to a given rule.
     */
    @Override
    public void add(Triple t) {
        if (graph.shouldTrace()) {
            if (rule != null) {
                logger.debug("Adding to pending (" + rule.toShortString() + "): " + PrintUtil.print(t));
            } else {
                logger.debug("Adding to pending : " + PrintUtil.print(t));
            }
        }
        pending.add(t);
        //pendingCache.add(t);
    }
            
    /**
     * Take all the pending triples and add them to both the given graph and
     * to the processing stack.
     */
    public void flushPending() {
        for (Iterator<Triple> i = pending.iterator(); i.hasNext(); ) {
            Triple t = i.next();
            stack.add(t);
            graph.addDeduction(t);
            i.remove();
            // pendingCache.delete(t);
        }
        pending.clear();
        // Flush out pending removes as well
        for ( Triple t : deletesPending )
        {
            graph.delete( t );
        }
        deletesPending.clear();
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
        // Can't use stackCache.contains because that does not do semantic equality
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
        return graph.findDataMatches(s, p, o);
    }
    
    /**
     * Return the next triple to be added to the graph, removing it from
     * the stack.
     * @return the Triple or null if there are no more
     */
    public Triple getNextTriple() {
        if (stack.size() > 0) {
            Triple t = stack.remove(stack.size() - 1);
            return t;
        } else {
            return null;
        } 
    }
    
    /**
     * Reset the binding environemnt back to empty.
     * @param newSize the number of variables needed for processing the new rule
     */
    public void resetEnv(int newSize) {
        env.reset(newSize);
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
        deletesPending.add(t);
//        graph.delete(t);
    }

}
