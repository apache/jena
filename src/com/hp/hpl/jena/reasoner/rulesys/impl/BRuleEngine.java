/******************************************************************
 * File:        Agenda.java
 * Created by:  Dave Reynolds
 * Created on:  11-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BRuleEngine.java,v 1.10 2003-05-20 17:31:37 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Part of the backward chaining rule interpreter. Maintains an agenda containing
 * an ordered list of RuleStates awaiting processing (each RuleState
 * represents the tip of a partially expanded search tree).   
 * <p>
 * This object does the top level scheduling of rule processing. By default
 * it batches up several results from the top rule in the agenda before
 * switching to expand other trees. The size of this batch is adjustable.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.10 $ on $Date: 2003-05-20 17:31:37 $
 */
public class BRuleEngine {

    /** a list of active RuleStates to be processed */
    protected LinkedList agenda = new LinkedList();
    
    /** The table of all goals */
    protected GoalTable goalTable;
    
    /** The inference graph which is using this engine */
    protected BasicBackwardRuleInfGraph infGraph;
    
    /** Indexed version of the rule set */
    protected RuleStore ruleStore;
    
    /** True if debug information should be written out */
    protected boolean traceOn = false;
    
    /** The size of the result batch permitted before a rule should 
     * reschedule itself lower on the agenda */
    protected int batchSize = 10;
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(BRuleEngine.class);
    
    /**
     * Constructor.
     * @param infGraph the parent inference graph which is using this engine
     * @param rules the indexed set of rules to process
     */
    public BRuleEngine(BasicBackwardRuleInfGraph infGraph, RuleStore rules) {
        this.infGraph = infGraph;
        goalTable = new GoalTable(this);
        ruleStore = rules;
    }
    
    /**
     * Clear the tabled results
     */
    public void reset() {
        goalTable.reset();
    }
    
    /**
     * Find the set of memoized solutions for the given goal
     * and return a GoalState that can traverse all the solutions.
     * 
     * @param goal the goal to be solved
     * @return a GoalState which can iterate over all of the goal solutions
     */
    public synchronized GoalState findGoal(TriplePattern goal) {
        return goalTable.findGoal(goal);
    }
   
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Logger at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        traceOn = state;
    }
    
    /**
     * Return true if traces of rule firings should be logged.
     */
    public boolean isTraceOn() {
        return traceOn;
    }

    /**
     * Dump an a summary of the goal table state to stdout.
     * Just debugging, do not use for real.
     */
    public void dump() {
        goalTable.dump();        
    }
    
    /**
     * Append a new rule node to the end of the agenda.
     */
    public synchronized void appendToAgenda(RuleState rs) {
        if (!rs.isScheduled) {
            if (traceOn) {
//                logger.debug("append to agenda: " + rs);
            }
            agenda.add(rs);
            rs.isScheduled = true;
        }
    }
    
    /**
     * Prepend a new rule node to the head of the agenda.
     */
    public synchronized void prependToAgenda(RuleState rs) {
        if (!rs.isScheduled) {
            if (traceOn) {
//                logger.debug("prepend to agenda: " + rs);
            }
            agenda.add(0, rs);
            rs.isScheduled = true;
        }
    }
    
    /**
     * Get next agenda item. May do heuristic selection of next item to process.
     */
    public RuleState nextAgendaItem() {
        RuleState next = (RuleState)agenda.removeFirst();
        next.isScheduled = false;
        return next;
        
        // The reordering attempts had no positive effect
//        int maxPending = 0;
//        RuleState best = null;
//        int bestIndex = 0;
//        int limit = Math.min(10, agenda.size());
//        for (int i = 0; i < limit; i++) {
//            RuleState rs = (RuleState)agenda.get(i);
//            GoalState gs = rs.goalState;
//            if (gs != null && gs.results.started) {
//                int pending = gs.results.numResults() - gs.solutionPointer;
//                if (pending > maxPending) {
//                    maxPending = pending;
//                    best = rs;
//                    bestIndex = i;
//                }
//            }
//        }
//        if (best == null) return (RuleState)agenda.removeFirst();
//        agenda.remove(bestIndex);
//        return best;
    }
    
    /**
     * Return a list of rules that match the given goal entry
     */
    public List rulesFor(TriplePattern goal) {
        return ruleStore.rulesFor(goal);
    }  
    
    /**
     * Return the rule infernce graph that owns this engine.
     */
    public BasicBackwardRuleInfGraph getInfGraph() {
        return infGraph;  
    }
    
    /**
     * Process a call to a builtin predicate
     * @param clause the Functor representing the call
     * @param env the BindingEnvironment for this call
     * @param rule the rule which is invoking this call
     * @return true if the predicate succeeds
     */
    public boolean processBuiltin(Object clause, Rule rule, BindingEnvironment env) {
        return infGraph.processBuiltin(clause, rule, env);
    }
    
    /**
     * The main processing loop. Continues processing agenda items until either
     * a new solution to the top goal has been found or the agenda is empty and
     * so no more solutions are available.
     * 
     * @param topGoalState the top level GoalState whose values are being sought
     * @return null if all processing is complete and no more solutions are
     * available, otherwise returns the next result available for the topGoal.
     */
    public synchronized Triple next(GoalState topGoalState) {
        GoalResults topGoal = topGoalState.getGoalResultsEntry();
        int numResults = 0;
        RuleState current = null;
        RuleState continuation = null;
        try {
            while(true) {
                boolean foundResult = false;
                RuleState delayedRSClose = null;
                Trail nexttrail = null;
                if (current == null) {
                    // Move to the next agenda item
                    // (if empty then an exception is thrown and caught later)
                    current = nextAgendaItem();
                    current.restoreBindings();
                    numResults = 0;
                }
                if (traceOn) {
                    logger.debug("Processing " + current);
                }
                Object result = current.next();
                if (result == StateFlag.FAIL) {
                    // backtrack, if we fall off the root of this tree then
                    // current will end up as null in which case the loop with
                    // shift to the next agenda item
                    if (traceOn) {
                        logger.debug("Failed");
                    }
                    delayedRSClose = current;
                    current = current.prev;
                } else if (result == StateFlag.SUSPEND) {
                    // Can do no more with this goal
                    if (traceOn) {
                        logger.debug("Suspend " + current);
                    }
                    current.goalState.results.addDependent(current);
                    current.unwindBindings();
                    current = current.prev;
                } else if (result == StateFlag.SATISFIED) {
                    // The rule had no clauses left to check, so return answers
                    foundResult = true;
                    delayedRSClose = current;
                    continuation = current.prev;
                } else {                    
                    // We have a result so continue extending this search tree depth first
                    nexttrail = new Trail();
                    if (! nexttrail.unify((Triple)result, current.getCurrentClause()) ) {
                        // failed a functor match - so loop back to look for more results
                        // Might be better to reschedule onto the end of the agenda?
                        continue;
                    }
                    Rule rule = current.ruleInstance.rule;
                    boolean foundGoal = false;
                    int maxClause = rule.bodyLength();
                    int clauseIndex = current.nextClauseIndex();
                    while (clauseIndex < maxClause && !foundGoal) {
                        Object clause = rule.getBodyElement(clauseIndex++);
                        if (clause instanceof TriplePattern) {
                            // found next subgoal to try
                            // Push current state onto stack 
                            TriplePattern subgoal = nexttrail.partInstantiate((TriplePattern)clause);
                            if (!subgoal.isLegal()) {
                                // branch has failed
                                delayedRSClose = current;
                                current = current.prev;
                            } else {                                
                                current = new RuleState(current, nexttrail, subgoal, clauseIndex);
                            }
                            foundGoal = true;
                        } else {
                            if (!infGraph.processBuiltin(clause, rule, nexttrail)) {
                                // This branch has failed
                                delayedRSClose = current;
                                current = current.prev;
                                foundGoal = true;
                            }
                        }
                    }
                    if (!foundGoal) {
                        // If we get to here then this branch has completed and we have a result
                        foundResult = true;
                        continuation = current;
                    }
                }
                if (foundResult) {
                    // If we get to here then this branch has completed and we have a result
                    GoalResults resultDest = current.ruleInstance.generator;
                    Triple finalResult = current.getResult();
                    if (traceOn) {
                        logger.debug("Result:" + finalResult + " <- " + current);
                    }
                    boolean newresult = resultDest.addResult(finalResult);
                    if (nexttrail != null) nexttrail.unwindBindings();
                    if (delayedRSClose != null) {
                        delayedRSClose.close();
                    }
                    numResults++;
                    current = continuation;
                    if (newresult && resultDest == topGoal) {
                        // Found a top level goal result so return it now
                        if (current != null) prependToAgenda(current);
                        return finalResult;
                    } else if (numResults > batchSize) {
                        // push the current state lower down agenda and try another
                        if (current != null) appendToAgenda(current);
                        current = null;
                    }
                } else {
                    if (delayedRSClose != null) {
                        delayedRSClose.close();
                    }
                }
            }
        } catch (NoSuchElementException e) {
            // No more agenda items can be processed, so the topGoal is as satisfied as it can be
            if (traceOn) {
                logger.debug("Completed all");
            }
            goalTable.setAllComplete();
            return null;
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