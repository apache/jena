/******************************************************************
 * File:        RuleState.java
 * Created by:  Dave Reynolds
 * Created on:  11-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleState.java,v 1.14 2003-05-21 11:13:49 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.PrintUtil;
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
 * @version $Revision: 1.14 $ on $Date: 2003-05-21 11:13:49 $
 */
public class RuleState {
    
//  =======================================================================
//   variables

    /** Reference to a package of information on the rule being processed */
    protected RuleInstance ruleInstance;
    
    /**  the parent RuleState for backtracking */
    protected RuleState prev;
    
    /** A trail of variable bindings made during the processing of this state */
    protected Trail trail;
        
    /** The continuation point for the rule clause being processed */
    protected GoalState goalState;
    
    /** Flag to indicate that rule state is scheduled on the agenda */
    protected boolean isScheduled = false;
    
    /** The clause number in the rule to be processed next. */
    int clauseIndex;
    
//  =======================================================================
//   constructors
    
    /**
     * Normal constructor. Creates a new RuleState as an extension to an existing one.
     * @param parent the parent RuleState being expanded, can't be null
     * @param trail the trail extension containing trail bindings for the match that forked this state
     * @param clause the TriplePattern which forms the goal for this state
     * @param index the index of the clause in the parent rule
     */
    public RuleState(RuleState parent, Trail trail, TriplePattern clause, int index) {
        prev = parent;
        this.trail = trail;
        ruleInstance = parent.ruleInstance;
        clauseIndex = index;
        TriplePattern subgoal = parent.trail.partInstantiate((TriplePattern)clause);
        goalState = ruleInstance.engine.findGoal(subgoal);
        ruleInstance.generator.incRefCount();
    }
    
    /**
     * Constructor used when creating the first RuleState for a rule.
     */
    private RuleState(RuleInstance ruleInstance, Trail trail, GoalState goalState, int index) {
        prev = null;
        this.trail = trail;
        this.ruleInstance = ruleInstance; 
        this.goalState = goalState;
        this.clauseIndex = index;
        ruleInstance.generator.incRefCount();
    }
    
//  =======================================================================
//  main operations
    
    /**
     * Return the next match for this clause (or FAIL or SUSPEND)
     */
    Object next() {
        if (goalState == null) {
            return StateFlag.SATISFIED;
        } else {
            Object result = goalState.next();
            return result;
        }
    }
        
    /**
     * Return the final goal result. Returns null if this is not a legal result shape.
     */
    public Triple getResult() {
        Triple t = trail.instantiate(ruleInstance.head);
        if (t.getSubject().isLiteral() || t.getPredicate().isLiteral()) return null;
        return t;
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
        unwindBindings();
        if (goalState != null) goalState.close();
        ruleInstance.generator.decRefCount();
    }
    
    /**
     * Unwind all of the bindings associated with this rule state. 
     */
    public void unwindBindings() {
        trail.unwindBindings();
    }
    
    
    /**
     * Unwind all of the bindings associated with this rule state and all its parents.
     * Used when context switching to a completely different goal tree. 
     */
    public void unwindAllBindings() {
        if (prev != null) prev.unwindAllBindings();
        trail.unwindBindings();
    }
    
    /**
     * Restore all the bindings associated with this rule state.
     */
    public void restoreBindings() {
        if (prev != null) prev.restoreBindings();
        trail.activate();
    }
    
    /**
     * Return the clause currently being processed.
     */
    public TriplePattern getCurrentClause() {
        return (TriplePattern)ruleInstance.rule.getBodyElement(clauseIndex-1);
    }
    
    /**
     * Printable string
     */
    public String toString() {
        Rule rule = ruleInstance.rule;
        // Convert the rule to an instantiated rule    
            StringBuffer buff = new StringBuffer();
            buff.append("[ ");
            if (rule.getName() != null) {
                buff.append(rule.getName());
                buff.append(": ");
            }
            for (int i = 0; i < rule.bodyLength(); i++) {
                if (i == (clauseIndex-1) ) {
                    buff.append(" ^^ ");
                }
                Object clause = rule.getBodyElement(i);
                if (clause instanceof TriplePattern) {
                    buff.append(PrintUtil.print(trail.partInstantiate((TriplePattern)clause)));
                } else {
                    buff.append(trail.getMostGroundVersion((Functor)clause).toString());
                }
                buff.append(" ");
            }
            buff.append("-> ");
            for (int i = 0; i < rule.headLength(); i++) {
                Object clause = rule.getHeadElement(i);
                if (clause instanceof TriplePattern) {
                    buff.append(PrintUtil.print(trail.partInstantiate((TriplePattern)clause)));
                } else {
                    buff.append(trail.getMostGroundVersion((Functor)clause).toString());
                }
                buff.append(" ");
            }
            buff.append("]");
            return "RuleState: " + buff;
//        return "RuleState " 
//                + ruleInstance.rule.toShortString()
//                + "("+ (clauseIndex-1) +")"
//                + ", gs=" + goalState;
    }
    
//  =======================================================================
//  Support for creating a first rule state in a search tree
        
    /**
     * Create the first RuleState for using a given rule to satisfy a goal.
     * @param rule the rule being instantiated
     * @param generator the GoalTable entry that this rule should generate results for
     * @param engine the parent rule engine
     * @return the instantiated initial RuleState or null if a guard predicate
     * fails so the rule is not applicable.
     */
    public static RuleState createInitialState(Rule orule, GoalResults generator) {
        Rule rule = orule.cloneRule();
        TriplePattern goal = generator.goal;
        TriplePattern head = (TriplePattern) rule.getHeadElement(0);
        Trail trail = new Trail();
        if ( ! trail.unify(goal, head)) return null;
        
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
                    int score1 = scoreClauseBoundness((TriplePattern)clause, head);
                    int score2 = scoreClauseBoundness((TriplePattern)secondClause, head);
                    if (score2 > score1) {
                        ri.clausesReordered = true;
                        ri.secondClause = clauseIndex;
                        clause = secondClause;
                        clauseIndex++;
                    }
                }
                // ... end of clause reorder
                TriplePattern subgoal = trail.partInstantiate((TriplePattern)clause);
                if (!subgoal.isLegal()) {
                    trail.unwindBindings();
                    return null;
                }
                GoalState gs = generator.getEngine().findGoal(subgoal);
                RuleState rs = new RuleState(ri, trail, gs, clauseIndex);
                trail.unwindBindings();
                return rs;
            } else {
                if (!generator.getEngine().processBuiltin(clause, rule, trail)) {
                    trail.unwindBindings();
                    return null;
                }
            }
        }
        // If we get to here there are no rule body clause to process
        trail.unwindBindings();
        return new RuleState(ri, trail, null, 0);
    }
    
    
    /**
     * Score a clause in terms of groundedness using simple heurisitcs.
     * For this case we are only considering head variables which occur
     * in the clause and score on boundedness of these.
     */
    private static int scoreClauseBoundness(TriplePattern clause, TriplePattern head) {
        return 
                scoreNodeBoundness(clause.getSubject(), head) +
                scoreNodeBoundness(clause.getPredicate(), head)  +
                scoreNodeBoundness(clause.getObject(), head);

    }
    
    /**
     * Score a node from a pattern as part of scoreClauseBoundedness.
     */
    private static int scoreNodeBoundness(Node n, TriplePattern head) {
        if (n.isVariable()) {
            if (n == head.getSubject() || n == head.getPredicate() || n == head.getObject() ) {
                Node val = ((Node_RuleVariable)n).deref();
                if (n == null || n.isVariable()) return -5;
                return 5;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
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