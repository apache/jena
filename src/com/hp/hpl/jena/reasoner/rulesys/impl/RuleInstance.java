/******************************************************************
 * File:        RuleInstance.java
 * Created by:  Dave Reynolds
 * Created on:  03-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleInstance.java,v 1.1 2003-05-05 15:16:00 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import java.util.*;

/**
 *  Part of the backward chaining rule interpreter. A RuleInstance
 * represents a continuation point in processing a single rule to generate
 * goal results.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-05-05 15:16:00 $
 */
public class RuleInstance {

    /** The rule being processed */
    protected Rule rule;
    
    /** The parent goal table entry which contains this continuation point */
    protected GoalResults generator;
    
    /** The state of the clause currently being processed */
    protected RuleState state;
    
    /** The head clause whose bindings are being sought */
    protected TriplePattern head;
    
    /** A push down state of RuleStates for earlier subgoals */
    protected ArrayList stateStack = new ArrayList(); 
    
    /**
     * Constructor. Create a new continuation point for a rule in
     * the context of a specific goal represented by the table entry.
     */
    RuleInstance(GoalResults results, Rule rule) {
        this.generator = results;
        this.rule = rule;
    }
    
    /**
     * Return the next result for this rule (or FAIL or SUSPEND)
     */
    Object next() {
        if (state == null) {
            init();
            if (state == null) return StateFlag.FAIL;
        }
        BasicBackwardRuleInfGraph ruleEngine = generator.ruleEngine;
        // Process the AND until we hit a fail, a suspend or get though all the clauses
        while(true) {
            Object result = state.next();
            if ( !(result instanceof StateFlag) ) {
                // Push current state and move to the next subGoal
                int maxClause = rule.bodyLength();
                int clauseIndex = state.clauseIndex;
                BindingVector env = state.env;
                boolean finished = true;
                stateStack.add(state);
                while (clauseIndex < maxClause) {
                    Object clause = rule.getBodyElement(clauseIndex++);
                    if (clause instanceof TriplePattern) {
                        // found next subgoal to try 
                        GoalState gs = ruleEngine.findGoal((TriplePattern)clause);
                        state = new RuleState(gs, new BindingVector(state.env), clauseIndex);
                        finished = false;
                    } else {
                        if (!ruleEngine.processBuiltin(clause, rule, env)) {
                            result = StateFlag.FAIL;
                            finished = false;
                            break;      // fall through to stack pop
                        }
                    }
                }
                if (finished) break; // fall through to result return
            }
            if (result == StateFlag.SUSPEND) {
                // Record that this processing point is suspended on the current subgoal
                state.goalState.results.addDependent(generator);
                return result;
            } else if (result == StateFlag.FAIL) {
                // fail back
                int ptr = stateStack.size()-1;
                if (ptr >= 0) {
                    state = (RuleState)stateStack.get(ptr);
                    stateStack.remove(ptr);
                } else {
                    // Run out of alternatives so the whole rule fails
                    return StateFlag.FAIL;
                }
            }
        }
        // If we get to here we have run out of body clauses
        return state.env.instantiate(head);
    }

    /**
     * Intialize the RuleInstance. This needs to find the head clause
     * which is being matched and initialize the binding environment accordingly.
     * Then finds the first subgoal and sets the initial state. If a guard
     * predicate fails before then it leaves the state null as a flag that
     * the rule should fail.
     */
    private void init() {
        BindingVector env = new BindingVector();
        TriplePattern goal = generator.goal;
        
        Object headClause = rule.getHeadElement(0);
        // Find the head clause which matches the goal
        if (rule.headLength() > 1) {
            for (int i = 0; i < rule.headLength(); i++) {
                headClause = rule.getHeadElement(i);
                if ( (headClause instanceof TriplePattern) &&
                     goal.subsumes((TriplePattern)headClause) ) break;
            }
        }
        head = (TriplePattern)headClause;
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
        int clauseIndex = 0;
        BasicBackwardRuleInfGraph ruleEngine = generator.ruleEngine;
        while (state == null && clauseIndex < maxClause) {
            Object clause = rule.getBodyElement(clauseIndex++);
            if (clause instanceof TriplePattern) {
                GoalState gs = ruleEngine.findGoal((TriplePattern)clause);
                state = new RuleState(gs, env, clauseIndex);
            } else {
                if (!ruleEngine.processBuiltin(clause, rule, env)) {
                    return;
                }
            }
        }
    }
    
//  =======================================================================
//   Inner classes

    /**
     * Inner class which represents a single state of the rule.
     */
    static class RuleState {
        /** The binding environment for the rule so far */
        BindingVector env;
        
        /** The continuation point for the rule clause being processed */
        GoalState goalState;
        
        /** The clause number in the rule currently being processed.
         *  TODO this needs revising if we enable clause reordering */
        int clauseIndex;
        
        /** binding offset for subject field, -1 if none */
        int subjectBind;
        
        /** binding offset for predicate field, -1 if none */
        int predicateBind;
        
        /** binding offset for object field, -1 if none */
        int objectBind;
        
        /**
         * Constructor. Caches the map from result node to environment offset
         * to speed up result checking during next().
         */
        RuleState(GoalState goalState, BindingVector env, int clauseIndex) {
            this.goalState = goalState;
            this.env = env;
            this.clauseIndex = clauseIndex;
            TriplePattern goal = goalState.results.goal;
            Node n = goal.getSubject();
            subjectBind = (n instanceof Node_RuleVariable) ? ((Node_RuleVariable)n).getIndex() : -1 ;
            n = goal.getPredicate();
            predicateBind = (n instanceof Node_RuleVariable) ? ((Node_RuleVariable)n).getIndex() : -1 ;
            n = goal.getObject();
            objectBind = (n instanceof Node_RuleVariable) ? ((Node_RuleVariable)n).getIndex() : -1 ;
        }
        
        /**
         * Return the next match for this clause (or FAIL or SUSPEND)
         */
        Object next() {
            Object result = goalState.next();
            if (result instanceof Triple) {
                Triple t = (Triple)result;
                boolean ok = true;
                if (subjectBind != -1) ok &= env.bind(subjectBind, t.getSubject());
                if (predicateBind != -1) ok &= env.bind(predicateBind, t.getPredicate());
                if (objectBind != -1) ok &= env.bind(objectBind, t.getObject());
                if (!ok) return StateFlag.FAIL;
            }
            return result;        
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