/******************************************************************
 * File:        GoalResults.java
 * Created by:  Dave Reynolds
 * Created on:  03-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: GoalResults.java,v 1.6 2003-05-19 08:25:46 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

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
 * @version $Revision: 1.6 $ on $Date: 2003-05-19 08:25:46 $
 */
public class GoalResults {

//  =======================================================================
//   variables

    /** The goal who values are being memoised by this entry */
    protected TriplePattern goal;
    
    /** The sequence of answers available so far */
    protected ArrayList resultSet;
    
    /** A searchable version of the resultSet */
    protected HashSet resultSetIndex;
     
    /** True if all the values for this goal are known */
    protected boolean isComplete;
    
    /** True if this goal generator has started running */
    protected boolean started = false;
    
    /** The set of RuleStates which are currently blocked
     *  waiting for this table entry to have more results */
    protected Set dependents = new HashSet();
    
    /** The rule engine which this table entry is part of */
    protected BRuleEngine engine; 
    
    /** Reference count of the number of rulestates working on values for this entry */
    protected int refCount = 0;
    
    /** Flag to indicate that the goal is a singleton and so should close once one result is in */
    protected boolean isSingleton = false;
        
    /** log4j logger*/
    static Logger logger = Logger.getLogger(GoalResults.class);
    
//  =======================================================================
//   methods

    /**
     * Contructor.
     * 
     * @param goal the goal whose matches are to be memoised.
     * @param ruleEngine the parent rule engine for the goal table containing this entry
     */
    public GoalResults(TriplePattern goal, BRuleEngine ruleEngine) {
        this.goal = goal;
        resultSet = new ArrayList();
        resultSetIndex = new HashSet();
        isComplete = false;
        engine = ruleEngine;
        isSingleton = !(goal.getSubject().isVariable() || goal.getPredicate().isVariable() || goal.getObject().isVariable());
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
        if (!started) start();
        return resultSet.size();
    }
    
    /**
     * Return the n'th memoized result for this goal.
     */
    public Triple getResult(int n) {
        return (Triple)resultSet.get(n);
    }
    
    /**
     * Record that a rule node has suspened waiting for more
     * results from this subgoal
     */
    public void addDependent(RuleState dependent) {
        if (!isComplete) dependents.add(dependent);
    }
    
    /**
     * Move all the blocked dependents to the agenda for further processing.
     */
    public void flushDependents() {
        for (Iterator i = dependents.iterator(); i.hasNext(); ) {
            RuleState dep = (RuleState)i.next();
            engine.prependToAgenda(dep);
        }
//        dependents.clear();
    }
    
    /**
     * Return the rule engine processing this goal 
     */
    public BRuleEngine getEngine() {
        return engine;
    }
    
    /**
     * Indicate that the goal has completed.
     */
    public void setComplete() {
        if (!isComplete) {
            if (engine.isTraceOn()) {
                logger.debug("Completed " + this);
            }
            isComplete = true;
            resultSetIndex = null;
            flushDependents();
            dependents.clear();
        }
    }
    
    /**
     * Indicate that all goals have been completed, sets this to complete
     * but does not bother to add the dependents to the agenda.
     */
    public void setAllComplete() {
        isComplete = true;
        dependents.clear();
    }
    
    /**
     * Start up a GoalResults stream. This finds all the relevant rules
     * and adds initial states for them to the agenda.
     */
    public void start() {
        List rules = engine.rulesFor(goal);
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule rule = (Rule)i.next();
            RuleState rs = RuleState.createInitialState(rule, this);
            if (rs != null) {
                engine.appendToAgenda(rs);
            }
        }
        if (refCount <= 0) setComplete();
        started = true;
    }
    
    /**
     * Add a new result to the result set for this goal.
     * @return ture if this is a new result for this goal
     */
    public boolean addResult(Triple result) {
        if (!isComplete && !resultSetIndex.contains(result)) {
            resultSet.add(result);
            resultSetIndex.add(result);
            if (isSingleton) {
                setComplete();
            } else {
                flushDependents();
            }
            return true;
        }
        return false;
    }
    
    /**
     * Increment the reference count, called when a new RuleState refering to this result set
     * is created.
     */
    public void incRefCount() {
        refCount++;
    }
    
    /**
     * Decrement the reference count, called when a RuleState for this result set either
     * fails or completes.
     */
    public void decRefCount() {
        refCount--;
        if (refCount <= 0) {
            setComplete();
        }
    }
    
    /**
     * Printable form
     */
    public String toString() {
        return "GoalResult for: " + goal;
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