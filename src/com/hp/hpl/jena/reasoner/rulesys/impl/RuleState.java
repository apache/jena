/******************************************************************
 * File:        RuleState.java
 * Created by:  Dave Reynolds
 * Created on:  11-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleState.java,v 1.4 2003-05-15 08:38:24 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;

/**
 * Part of the backward chaining rule interpreter. A RuleState represents
 * the state of a partially expanded search tree for a single Rule.
 * The RuleStates are linked back in an OR tree to a root goal which is
 * being satisfied. Each RuleState shares a pointer to a RuleInstance which
 * holds references for the rule being processed and the goal which the rule is
 * satisfying.
 * <p>
 * Encapuslation warning: this object is used in the tight inner loop of the engine so we access its
 * field pointers directly rather than through accessor methods.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-05-15 08:38:24 $
 */
public class RuleState {

    /** Reference to a package of information on the rule being processed */
    protected RuleInstance ruleInstance;
    
    /**  the parent RuleState for backtracking */
    protected RuleState prev;
    
    /** The binding environment for the rule so far */
    protected BindingVector env;
    
    /** The continuation point for the rule clause being processed */
    protected GoalState goalState;
    
    /** Flag to indicate that rule state is scheduled on the agenda */
    protected boolean isScheduled;
    
    /** The clause number in the rule to be processed next.
     *  TODO this needs revising if we enable clause reordering */
    int clauseIndex;
    
    /** binding offset for subject field, -1 if none */
    int subjectBind;
    
    /** binding offset for predicate field, -1 if none */
    int predicateBind;
    
    /** binding offset for object field, -1 if none */
    int objectBind;
    
    /** functor node for object binding */
    protected Functor functorMatch;
    
    /**
     * Normal constructor. Creates a new RuleState as an extension to an existing one.
     * @param parent the parent RuleState being expanded, can't be null
     * @param clause the TriplePattern which forms to goal for this state
     * @param index the index of the clause in the parent rule
     * @param env the prebound enviornment to use
     */
    public RuleState(RuleState parent, TriplePattern clause, int index, BindingVector env) {
        prev = parent;
        ruleInstance = parent.ruleInstance;
        clauseIndex = index;
        this.env = env;
        TriplePattern subgoal = env.bind((TriplePattern)clause);
        goalState = ruleInstance.engine.findGoal(subgoal);
        initMapping(subgoal);
    }
    
    /**
     * Constructor used when creating the first RuleState for a rule.
     * The caller is responsible for initializing the mapping.
     */
    private RuleState(RuleInstance ruleInstance, BindingVector env, GoalState goalState, int index) {
        prev = null;
        this.ruleInstance = ruleInstance; 
        this.env = env;
        this.goalState = goalState;
        this.clauseIndex = index;
    }
    
    /**
     * Return the next match for this clause (or FAIL or SUSPEND)
     */
    Object next() {
        if (goalState == null) {
            return StateFlag.SATISFIED;
        } else {
            return goalState.next();
        }
    }
    
    /**
     * Return a new binding environment based on this one but extended
     * by the matches resulting from the given triple result for this state.
     */
    public BindingVector newEnvironment(Triple result) {
        BindingVector newenv = new BindingVector(env);
        Node[] rawenv = newenv.getEnvironment();
        if (subjectBind != -1) rawenv[subjectBind] = result.getSubject();
        if (predicateBind != -1) rawenv[predicateBind] = result.getPredicate();
        if (objectBind != -1) rawenv[objectBind] = result.getObject();
        // Functor matches are not precompiled but intepreted
        if (functorMatch != null) {
            Node obj = result.getObject();
            if (Functor.isFunctor(obj)) {
                Functor objValue = (Functor)obj.getLiteral().getValue();
                if (objValue.getName().equals(functorMatch.getName())) {
                    Node[] margs = functorMatch.getArgs();
                    Node[] args = objValue.getArgs();
                    if (margs.length != args.length) return null;
                    for (int i = 0; i < margs.length; i++) {
                        Node match = margs[i];
                        if (match instanceof Node_RuleVariable) {
                            rawenv[((Node_RuleVariable)match).getIndex()] = args[i]; 
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return newenv;
    }
    
    /**
     * Return the final goal result, based on the given binding environment
     */
    public Triple getResult(BindingVector newenv) {
        return newenv.instantiate(ruleInstance.head);
    }
    
    /**
     * Initialize the mapping pointers that map result values to environment bindings
     */
    private void initMapping(TriplePattern goal) {
        Node n = goal.getSubject();
        subjectBind = (n instanceof Node_RuleVariable) ? ((Node_RuleVariable)n).getIndex() : -1 ;
        n = goal.getPredicate();
        predicateBind = (n instanceof Node_RuleVariable) ? ((Node_RuleVariable)n).getIndex() : -1 ;
        n = goal.getObject();
        objectBind = (n instanceof Node_RuleVariable) ? ((Node_RuleVariable)n).getIndex() : -1 ;
        if (Functor.isFunctor(n)) functorMatch = (Functor)n.getLiteral().getValue();
    }
    
    /**
     * Create the first RuleState for using a given rule to satisfy a goal.
     * @param rule the rule being instantiated
     * @param generator the GoalTable entry that this rule should generate results for
     * @param engine the parent rule engine
     * @return the instantiated initial RuleState or null if a guard predicate
     * fails so the rule is not applicable.
     */
    public static RuleState createInitialState(Rule rule, GoalResults generator) {
        BindingVector env = new BindingVector();
        TriplePattern goal = generator.goal;
        
        Object headClause = rule.getHeadElement(0);
        TriplePattern head = (TriplePattern)headClause;
        Node n = head.getSubject();
        if (n instanceof Node_RuleVariable) {
            Node g = goal.getSubject();
            if (!g.isVariable()) env.bind(n, g);
        }
        n = head.getPredicate();
        if (n instanceof Node_RuleVariable) {
            Node g = goal.getPredicate();
            if (!g.isVariable()) env.bind(n, g);
        }
        n = head.getObject();
        if (n instanceof Node_RuleVariable) {
            Node g = goal.getObject();
            if (!g.isVariable()) env.bind(n, g);
        }
        int maxClause = rule.bodyLength();
        
        // Find the first goal clause
        RuleInstance ri = new RuleInstance(generator, rule, head);
        int clauseIndex = 0;
        while (clauseIndex < maxClause) {
            Object clause = rule.getBodyElement(clauseIndex++);
            if (clause instanceof TriplePattern) {
                TriplePattern subgoal = env.bind((TriplePattern)clause);
                GoalState gs = generator.getEngine().findGoal(subgoal);
                RuleState rs = new RuleState(ri, env, gs, clauseIndex);
                rs.initMapping(subgoal);
//                BRuleEngine.logger.debug("Created " + rs + ", for goal(" + goal +")");
                return rs;
            } else {
                if (!generator.getEngine().processBuiltin(clause, rule, env)) {
                    return null;
                }
            }
        }
        // If we get to here there are no rule body clause to process
        return new RuleState(ri, env, null, 0);
    }
    
    /**
     * Unify a goal with the head of a rule. This is a poor-man's unification,
     * we should try swtiching to a more conventional global-variables-with-trail
     * implementation in the future.
     * @return An initialized binding environment for the rule variables
     * or null if the unificatin fails. If a variable in the environment becomes
     * aliased to another variable through the unification this is represented
     * by having its value in the environment be the variable to which it is aliased.
     */ 
    public static BindingVector unify(TriplePattern goal, TriplePattern head) {
        Node[] gEnv = new Node[BindingStack.MAX_VAR];
        Node[] hEnv = new Node[BindingStack.MAX_VAR];
        
        if (!unify(goal.getSubject(), head.getSubject(), gEnv, hEnv)) {
            return null;
        } 
        if (!unify(goal.getPredicate(), head.getPredicate(), gEnv, hEnv)) {
            return null; 
        } 
        
        Node gObj = goal.getObject();
        Node hObj = goal.getObject();
        if (Functor.isFunctor(hObj)) {
            if (Functor.isFunctor(hObj)) {
                Functor gFunctor = (Functor)gObj.getLiteral().getValue();
                Functor hFunctor = (Functor)hObj.getLiteral().getValue();
                if ( ! gFunctor.getName().equals(hFunctor.getName()) ) {
                    return null;
                }
                Node[] gArgs = gFunctor.getArgs();
                Node[] hArgs = hFunctor.getArgs();
                if ( gArgs.length != hArgs.length ) return null;
                for (int i = 0; i < gArgs.length; i++) {
                    if (! unify(gArgs[i], hArgs[i], gEnv, hEnv) ) {
                        return null;
                    }
                }
            } else if (hObj instanceof Node_RuleVariable) {
                // No extra biding to do, success
            } else {
                // unifying simple ground object with functor, failure
                return null;
            }
        } else if (hObj instanceof Node_RuleVariable) {
            if (!unify(gObj, hObj, gEnv, hEnv)) return null;
        } else {
            if ( ! hObj.sameValueAs(gObj) ) return null;
        }
        // Successful bind if we get here
        return new BindingVector(hEnv);
    }
    
    /**
     * Unify a single pair of goal/head nodes. Unification of a head var to
     * a goal var is recorded using an Integer in the head env to point to a
     * goal env and storing the head var in the goal env slot.
     * @return true if they are unifiable, side effects the environments
     */
    private static boolean unify(Node gNode, Node hNode, Node[] gEnv, Node[] hEnv) {
        if (hNode instanceof Node_RuleVariable) {
            int hIndex = ((Node_RuleVariable)hNode).getIndex();
            if (gNode instanceof Node_RuleVariable) {
                // Record variable bind between head and goal to detect aliases
                int gIndex = ((Node_RuleVariable)gNode).getIndex();
                if (gEnv[gIndex] == null) {
                    // First time bind so record link 
                    gEnv[gIndex] = hNode;
                } else {
                    // aliased var so follow trail to alias
                    hEnv[hIndex] = gEnv[gIndex];
                }
            } else {
                hEnv[hIndex] = gNode;
            }
            return true;
        } else {
            if (gNode instanceof Node_RuleVariable) {
                int gIndex = ((Node_RuleVariable)gNode).getIndex();
                Node gVal = gEnv[gIndex]; 
                if (gVal == null) {
                    //. No variable alias so just record binding
                    gEnv[gIndex] = hNode;
                } else if (gVal instanceof Node_RuleVariable) {
                    // Already an alias
                    hEnv[((Node_RuleVariable)gVal).getIndex()] = hNode;
                    gEnv[gIndex] = hNode;
                } else {
                    return gVal.sameValueAs(hNode);
                }
                return true;
            } else {
                return hNode.sameValueAs(gNode); 
            }
        }
    }
    
    /**
     * Printable string
     */
    public String toString() {
        return "RuleState " 
                + ruleInstance.rule.toShortString()
                + "("+ (clauseIndex-1) +")"
                + ", env=" + env 
                + ", gs=" + goalState;
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