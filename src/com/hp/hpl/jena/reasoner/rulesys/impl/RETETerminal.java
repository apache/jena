/******************************************************************
 * File:        RETETerminal.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RETETerminal.java,v 1.4 2003-06-10 22:26:36 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * The final node in a RETE graph. It runs the builtin guard clauses
 * and then, if the token passes, executes the head operations.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-06-10 22:26:36 $
 */
public class RETETerminal implements RETESinkNode {

    /** Context containing the specific rule and parent graph */
    protected RETERuleContext context;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(FRuleEngine.class);
    
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
    public void fire(BindingVector env, boolean isAdd) {
        Rule rule = context.getRule();
        context.setEnv(env);
        
        // Check any non-pattern clauses 
        for (int i = 0; i < rule.bodyLength(); i++) {
            Object clause = rule.getBodyElement(i);
            if (clause instanceof Functor) {
                // Fire a built in
                if (isAdd) {
                    if (!((Functor)clause).evalAsBodyClause(context)) {
                        // Failed guard so just discard and return
                        return;
                    }
                } else {
                    // Don't re-run side-effectful clause on a re-run
                    if (!((Functor)clause).safeEvalAsBodyClause(context)) {
                        // Failed guard so just discard and return
                        return;
                    }
                }
            }
        }
        
        // Now fire the rule
        ForwardRuleInfGraphI infGraph = (ForwardRuleInfGraphI)context.getGraph();
        if (infGraph.shouldTrace()) {
            logger.info("Fired rule: " + rule.toShortString());
        }
        RETEEngine engine = context.getEngine();
        engine.incRuleCount();
        List matchList = null;
        if (infGraph.shouldLogDerivations()) {
            // Create derivation record
            matchList = new ArrayList(rule.bodyLength());
            for (int i = 0; i < rule.bodyLength(); i++) {
                Object clause = rule.getBodyElement(i);
                if (clause instanceof TriplePattern) {
                    matchList.add(instantiate((TriplePattern)clause, env));
                } 
            }
        }
        for (int i = 0; i < rule.headLength(); i++) {
            Object hClause = rule.getHeadElement(i);
            if (hClause instanceof TriplePattern) {
                Triple t = instantiate((TriplePattern) hClause, env);
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
                        // Remove the generated triple
                        engine.deleteTriple(t, true);
                    }
                }
            } else if (hClause instanceof Functor && isAdd) {
                Functor f = (Functor)hClause;
                Builtin imp = f.getImplementor();
                if (imp != null) {
                    imp.headAction(f.getBoundArgs(env), context);
                } else {
                    throw new ReasonerException("Invoking undefined Functor " + f.getName() +" in " + rule.toShortString());
                }
            } else if (hClause instanceof Rule && isAdd) {
                Rule r = (Rule)hClause;
                if (r.isBackward()) {
                    infGraph.addBRule(r.instantiate(env));
                } else {
                    throw new ReasonerException("Found non-backward subrule : " + r); 
                }
            }
        }
    }
    
    /**
     * Instantiate a triple pattern against the current environment.
     * This version handles unbound varibles by turning them into bNodes.
     * @param clause the triple pattern to match
     * @param env the current binding environment
     * @return a new, instantiated triple
     */
    public static Triple instantiate(TriplePattern pattern, BindingEnvironment env) {
        Node s = env.getGroundVersion(pattern.getSubject());
        if (s == null) s = Node.createAnon();
        Node p = env.getGroundVersion(pattern.getPredicate());
        if (p == null) p = Node.createAnon();
        Node o = env.getGroundVersion(pattern.getObject());
        if (o == null) o = Node.createAnon();
        return new Triple(s, p, o);
    }
     
}


/*
    (c) Copyright Hewlett-Packard Company 2003
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