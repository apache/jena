/******************************************************************
 * File:        RETEConflictSet.java
 * Created by:  Dave Reynolds
 * Created on:  04-Oct-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RETEConflictSet.java,v 1.2 2005-10-06 13:14:39 der Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $
 */

public class RETEConflictSet {
    protected static Log logger = LogFactory.getLog(FRuleEngine.class);

    /** the execution context for this conflict set */
    protected RETERuleContext gcontext;

    /** false if the overall rule system contains some non-montotonic rules */
    protected boolean isMonotonic;
    
    /** the list of rule activations left to fire */
    protected ArrayList conflictSet = new ArrayList();

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
                for (Iterator i = conflictSet.iterator(); i.hasNext(); ) {
                    CSEntry cse = (CSEntry)i.next();
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
        CSEntry cse = (CSEntry)conflictSet.remove(index);
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
    protected void execute(RETERuleContext context, boolean isAdd) {
        Rule rule = context.getRule();
        BindingEnvironment env = context.getEnv();
        ForwardRuleInfGraphI infGraph = (ForwardRuleInfGraphI)context.getGraph();
        if (infGraph.shouldTrace()) {
            logger.info("Fired rule: " + rule.toShortString());
        }
        RETEEngine engine = context.getEngine();
        engine.incRuleCount();
        List matchList = null;
        if (infGraph.shouldLogDerivations() && isAdd) {
            // Create derivation record
            matchList = new ArrayList(rule.bodyLength());
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
                if (!t.getSubject().isLiteral()) {
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
                }
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


/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
