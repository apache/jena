/******************************************************************
 * File:        RuleState.java
 * Created by:  Dave Reynolds
 * Created on:  11-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleState.java,v 1.11 2003-05-20 10:21:55 der Exp $
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
 * @version $Revision: 1.11 $ on $Date: 2003-05-20 10:21:55 $
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
    protected boolean isScheduled = false;
    
    /** The clause number in the rule to be processed next. */
    int clauseIndex;
    
    /** binding offset for subject field, -1 if none */
    int subjectBind;
    
    /** binding offset for predicate field, -1 if none */
    int predicateBind;
    
    /** binding offset for object field, -1 if none */
    int objectBind;
    
    /** functor node for object binding */
    protected Functor functorMatch = null;
    
    /**
     * Normal constructor. Creates a new RuleState as an extension to an existing one.
     * @param parent the parent RuleState being expanded, can't be null
     * @param clause the TriplePattern which forms the goal for this state
     * @param index the index of the clause in the parent rule
     * @param env the prebound enviornment to use
     */
    public RuleState(RuleState parent, TriplePattern clause, int index, BindingVector env) {
        prev = parent;
        ruleInstance = parent.ruleInstance;
        clauseIndex = index;
        this.env = env;
        TriplePattern subgoal = env.partInstantiate((TriplePattern)clause);
        goalState = ruleInstance.engine.findGoal(subgoal);
        initMapping(subgoal);
        ruleInstance.generator.incRefCount();
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
        ruleInstance.generator.incRefCount();
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
        if (subjectBind != -1)   newenv.bind(subjectBind, result.getSubject());
        if (predicateBind != -1) newenv.bind(predicateBind, result.getPredicate());
        if (objectBind != -1)    newenv.bind(objectBind, result.getObject());
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
                            Node val = args[i];
                            if (Functor.isFunctor(val)) return null;
                            if (!newenv.bind(match, val)) return null;
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
     * Return the index of the next body clause to try.
     * Takes clause reordering into account.
     */ 
    protected int nextClauseIndex() {
        if (ruleInstance.clausesReordered) {
            if (clauseIndex == (ruleInstance.secondClause + 1) ) {
                // go back to do first clause
                return ruleInstance.secondClause - 1;
            } else if (clauseIndex == ruleInstance.secondClause) {
                return clauseIndex + 1;
            }
        }
        return clauseIndex;
    }
    
    /**
     * Close a non-longer needed rule state. This will decrement
     * the reference count of the goal table entry (this might have been
     * the last RuleState working on that entry) and will close any
     * iterators in the goal state.
     */
    public void close() {
        if (goalState != null) goalState.close();
        ruleInstance.generator.decRefCount();
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
        TriplePattern goal = generator.goal;
        TriplePattern head = (TriplePattern) rule.getHeadElement(0);
        BindingVector env = BindingVector.unify(goal, head, rule.getNumVars());
        if (env == null) return null;
        
        // Find the first goal clause
        RuleInstance ri = new RuleInstance(generator, rule, head);
        int maxClause = rule.bodyLength();
        int clauseIndex = 0;
        while (clauseIndex < maxClause) {
            Object clause = rule.getBodyElement(clauseIndex++);
            if (clause instanceof TriplePattern) {
                // Check for possible clause reorder ...
                Object secondClause = null;
                boolean foundSecondClause = false;
                if (clauseIndex < maxClause) {
                    secondClause = rule.getBodyElement(clauseIndex);
                    if (secondClause instanceof TriplePattern) {
                        foundSecondClause = true;
                    }
                }
                if (foundSecondClause) {
                    int score1 = scoreClauseBoundness((TriplePattern)clause, head, env);
                    int score2 = scoreClauseBoundness((TriplePattern)secondClause, head, env);
                    if (score2 > score1) {
                        ri.clausesReordered = true;
                        ri.secondClause = clauseIndex;
                        clause = secondClause;
                        clauseIndex++;
                    }
                }
                // ... end of clause reorder
                TriplePattern subgoal = env.partInstantiate((TriplePattern)clause);
                if (!subgoal.isLegal()) return null;
                GoalState gs = generator.getEngine().findGoal(subgoal);
                RuleState rs = new RuleState(ri, env, gs, clauseIndex);
                rs.initMapping(subgoal);
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
     * Score a clause in terms of groundedness using simple heurisitcs.
     * For this case we are only considering head variables which occur
     * in the clause and score on boundedness of these.
     */
    private static int scoreClauseBoundness(TriplePattern clause,
                                               TriplePattern head, 
                                               BindingVector env) {
        return 
                scoreNodeBoundness(clause.getSubject(), head, env) +
                scoreNodeBoundness(clause.getPredicate(), head, env)  +
                scoreNodeBoundness(clause.getObject(), head, env);

    }
    
    /**
     * Score a node from a pattern as part of scoreClauseBoundedness.
     */
    private static int scoreNodeBoundness(Node n, TriplePattern head, BindingVector env) {
        if (n.isVariable()) {
            if (n == head.getSubject() || n == head.getPredicate() || n == head.getObject() ) {
                Node val = env.getBinding(n);
                if (n == null || n.isVariable()) return -5;
                return 5;
            } else {
                return 0;
            }
        } else {
            return 1;
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