/******************************************************************
 * File:        GoalResults.java
 * Created by:  Dave Reynolds
 * Created on:  03-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: GoalResults.java,v 1.3 2003-05-08 15:09:24 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.BasicBackwardRuleInfGraph;

import java.util.*;
import org.apache.log4j.Logger;

/**
 * Part of the backward chaining rule interpreter. The goal table
 * is a table of partially evaluated goals. Each entry is an instance
 * of GoalResults which contains the goal (a generalized triple pattern
 * which supports structured literals), a set triple values, a completion
 * flag and a generator (which represents a continuation point for
 * finding further goal values). This is essentially an encapsulation of
 * the OR graph of the evaluation trace.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-05-08 15:09:24 $
 */
public class GoalResults {

//  =======================================================================
//   variables

    /** The goal who values are being memoised by this entry */
    TriplePattern goal;
    
    /** The sequence of answers available so far */
    ArrayList resultSet;
     
    /** True if all the values for this goal are known */
    boolean isComplete;
     
    /** The set of other GoalTableEntries which are currently blocked
     *  waiting for this one to return more results */
    Set dependents;
    
    /** The parent inference engine for the goal table containing this entry */
    BasicBackwardRuleInfGraph ruleEngine;
    
    /** The set of remaining RuleInstances that can generate results for this entry */
    List ruleInstances;
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(GoalResults.class);
    
//  =======================================================================
//   methods

    /**
     * Contructor.
     * 
     * @param goal the goal whose matches are to be memoised.
     * @param ruleEngine the parent inference engine for the goal table containing this entry
     */
    public GoalResults(TriplePattern goal, BasicBackwardRuleInfGraph ruleEngine) {
        this.goal = goal;
        this.ruleEngine = ruleEngine;
        resultSet = new ArrayList();
        isComplete = false;
        dependents = new HashSet();
        ruleInstances = ruleEngine.rulesFor(this);
    }
    
    /**
     * Return true of this goal is known to have been completely
     * evaluated.
     */
    public boolean isComplete() {
        return isComplete;
    }
    
    /**
     * Return the number of available memoized results for this goal.
     */
    public int numResults() {
        return resultSet.size();
    }
    
    /**
     * Return the n'th memoized result for this goal.
     */
    public Triple getResult(int n) {
        return (Triple)resultSet.get(n);
    }
    
    /**
     * Record that a goal processor has suspened waiting for more
     * results from this subgoal
     */
    public void addDependent(GoalResults dependent) {
        dependents.add(dependent);
    }
    
    /**
     * Move all the blocked dependents to the agenda for further processing.
     */
    public void flushDependents() {
        for (Iterator i = dependents.iterator(); i.hasNext(); ) {
            GoalResults dep = (GoalResults)i.next();
            ruleEngine.addAgendaItem(dep);
        }
        dependents.clear();
    }
    
    /**
     * Indicate that the goal has completed.
     */
    public void setComplete() {
        isComplete = true;
        flushDependents();
    }
    
    /**
     * Attempt to generate additional results for this goal.
     * If a new result is found it is both added to the result set and
     * returned to the caller. If that happens then any dependents are
     * added to the agenda and discarded.
     * @return the newly generated Triple matching this goal, or SUSPEND no
     * more results can be generated yet or FAIL if this goal is now complete.
     */
    public Object crank() {
        if (isComplete) return StateFlag.FAIL;
        ListIterator riPtr = ruleInstances.listIterator();
        while (riPtr.hasNext()) {
            RuleInstance ri = (RuleInstance)riPtr.next();
            Object result = ri.next();
            if (result == StateFlag.FAIL) {
                riPtr.remove();
            } else if (result instanceof Triple) {
                resultSet.add(result);
                flushDependents();
                if (ruleEngine.isTraceOn()) {
                    logger.debug("Cranking GoalResult on goal (" + goal + ") generated " + result);
                }
                return result;
            }
        }
        // If we get here there are no results available at present
        if (ruleInstances.size() == 0) setComplete();
        return isComplete ? StateFlag.FAIL : StateFlag.SUSPEND;
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