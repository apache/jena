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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.ForwardRuleInfGraphI;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;

/**
 * Manages a set of ready-to-fire rules. For monotonic rule sets
 * we simply fire the rules as soon as they are triggered. For non-monotonic
 * rule sets we stack them up in a conflict set and fire them one-at-a-time,
 * propagating all changes between times.
 * <p>
 * Note, implementation is not thread-safe. Would be easy to make it so but 
 * concurrent adds to InfModel are not supported anyway.
 */

public class RETEConflictSet {
    protected static Logger logger = LoggerFactory.getLogger(FRuleEngine.class);

    /** the execution context for this conflict set */
    protected RETERuleContext gcontext;

    /** false if the overall rule system contains some non-montotonic rules */
    protected boolean isMonotonic;
    
    /** the list of rule activations left to fire */
    protected ArrayList<CSEntry> conflictSet = new ArrayList<>();

    /** count the number of positive entries - optimization hack */
    protected int nPos = 0;
    
    /** count the number of negative entries - optimization hack */
    protected int nNeg = 0;
    
    /** Construct an empty conflict set, noting whether the overall rule system is monotonic or not */
    public RETEConflictSet(RETERuleContext context, boolean isMonotonic) {
        this.gcontext = context;
        this.isMonotonic = isMonotonic;
    }
    
    /**
     * Record a request for a rule firing. For monotonic rulesets it may be
     * actioned immediately, otherwise it will be stacked up.
     */
    public void add(Rule rule, BindingEnvironment env, boolean isAdd) {
        if (isMonotonic) {
            RETERuleContext context = new RETERuleContext((ForwardRuleInfGraphI)gcontext.getGraph(), gcontext.getEngine());
            context.setEnv(env);
            context.setRule(rule);
            execute(context, isAdd);
        } else {
            // Add to the conflict set, compressing +/- pairs
            boolean done = false;
            if ( (isAdd && nNeg > 0) || (!isAdd && nPos > 0) ) {
                for (Iterator<CSEntry> i = conflictSet.iterator(); i.hasNext(); ) {
                    CSEntry cse = i.next();
                    if (cse.rule != rule) continue;
                    if (cse.env.equals(env)) {
                        if (isAdd != cse.isAdd) {
                            i.remove();
                            if (cse.isAdd) nPos--; else nNeg --;
                            done = true;
                        } else {
                            // Redundant insert? Probably leave in for side-effect cases like print
                        }
                    }
                }
            }
            if (!done) {
                conflictSet.add(new CSEntry(rule, env, isAdd));
                if (isAdd) nPos++; else nNeg++;
            }
        }
    }

    /**
     * Return true if there are no more rules awaiting firing.
     */
    public boolean isEmpty() {
        return conflictSet.isEmpty();
    }
    
    /**
     * Pick on pending rule from the conflict set and fire it.
     * Return true if there was a rule to fire.
     */
    public boolean fireOne() {
        if (isEmpty()) return false;
        int index = conflictSet.size() - 1;
        CSEntry cse = conflictSet.remove(index);
        if (cse.isAdd) nPos--; else nNeg --;
        RETERuleContext context = new RETERuleContext((ForwardRuleInfGraphI)gcontext.getGraph(), gcontext.getEngine());
        context.setEnv(cse.env);
        context.setRule(cse.rule);
        if (context.shouldStillFire()) {
            execute(context, cse.isAdd);
        }
        return true;
        
    }
    
    /**
     * Execute a single rule firing. 
     */
    public static void execute(RETERuleContext context, boolean isAdd) {
        Rule rule = context.getRule();
        BindingEnvironment env = context.getEnv();
        ForwardRuleInfGraphI infGraph = (ForwardRuleInfGraphI)context.getGraph();
        if (infGraph.shouldTrace()) {
            logger.info("Fired rule: " + rule.toShortString());
        }
        RETEEngine engine = context.getEngine();
        engine.incRuleCount();
        List<Triple> matchList = null;
        if (infGraph.shouldLogDerivations() && isAdd) {
            // Create derivation record
            matchList = new ArrayList<>(rule.bodyLength());
            for (int i = 0; i < rule.bodyLength(); i++) {
                Object clause = rule.getBodyElement(i);
                if (clause instanceof TriplePattern) {
                    matchList.add(env.instantiate((TriplePattern)clause));
                } 
            }
        }
        for (int i = 0; i < rule.headLength(); i++) {
            Object hClause = rule.getHeadElement(i);
            if (hClause instanceof TriplePattern) {
                Triple t = env.instantiate((TriplePattern) hClause);
                // Used to filter out triples with literal subjects
                // but this is not necessary
                // if (!t.getSubject().isLiteral()) {
                    // Only add the result if it is legal at the RDF level.
                    // E.g. RDFS rules can create assertions about literals
                    // that we can't record in RDF
                    if (isAdd) {
                        if ( ! context.contains(t) ) {
                            engine.addTriple(t, true);
                            if (infGraph.shouldLogDerivations()) {
                                infGraph.logDerivation(t, new RuleDerivation(rule, t, matchList, infGraph));
                            }
                        }
                    } else {
                        if ( context.contains(t)) {
                            // Remove the generated triple
                            engine.deleteTriple(t, true);
                        }
                    }
              // }
            } else if (hClause instanceof Functor && isAdd) {
                Functor f = (Functor)hClause;
                Builtin imp = f.getImplementor();
                if (imp != null) {
                    imp.headAction(f.getBoundArgs(env), f.getArgLength(), context);
                } else {
                    throw new ReasonerException("Invoking undefined Functor " + f.getName() +" in " + rule.toShortString());
                }
            } else if (hClause instanceof Rule) {
                Rule r = (Rule)hClause;
                if (r.isBackward()) {
                    if (isAdd) {
                        infGraph.addBRule(r.instantiate(env));
                    } else {
                        infGraph.deleteBRule(r.instantiate(env));
                    }
                } else {
                    throw new ReasonerException("Found non-backward subrule : " + r); 
                }
            }
        }        
    }
        
    // Inner class representing a conflict set entry 
    private static class CSEntry {
        protected Rule rule;
        protected BindingEnvironment env;
        protected boolean isAdd;
        
        CSEntry(Rule rule, BindingEnvironment env, boolean isAdd) {
            this.rule = rule;
            this.env = env;
            this.isAdd = isAdd;
        }
    }
}
