/******************************************************************
 * File:        GoalTable.java
 * Created by:  Dave Reynolds
 * Created on:  03-May-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: GoalTable.java,v 1.5 2005-02-21 12:18:05 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl.oldCode;

import com.hp.hpl.jena.reasoner.*;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Part of the backwared chaining rule interpreter. The goal table
 *  is a table of partially evaluated goals. This could be done by
 *  variant-based or sumsumption-based tabling. We currently use variant-based.
 *  TODO Investigate performance impact of switching to subsumption-based.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2005-02-21 12:18:05 $
 */
public class GoalTable {

    /** The set of goal entries indexed by goal */
    protected Map table = new HashMap();
    
    /** The parent inference engine for the goal table */
    protected BRuleEngine ruleEngine;
    
    static Log logger = LogFactory.getLog(GoalTable.class);
        
    /**
     * Constructor. Creates a new, empty GoalTable. Any goal search on
     * this table will include the results from searching the given set of
     * raw data graphs.
     * @param ruleEngine the parent inference engine instance for this table
     */
    public GoalTable(BRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    /**
     * Find the set of memoized solutions for the given goal
     * and return a GoalState that can traverse all the solutions.
     * 
     * @param goal the goal to be solved
     * @return a GoalState which can iterate over all of the goal solutions
     */
    public GoalState findGoal(TriplePattern goal) {
//        if (ruleEngine.getInfGraph().isTraceOn()) {
//            logger.debug("findGoal on " + goal.toString());
//        }
        GoalResults results = (GoalResults) table.get(goal);
        if (results == null || !goal.variantOf(results.goal)) {
            results = new GoalResults(goal, ruleEngine);
            table.put(goal, results);
        }
        return new GoalState(ruleEngine.getInfGraph().findDataMatches(goal), results);
    }
        
    /**
     * Clear all tabled results. 
     */
    public void reset() {
        table = new HashMap();
    }
    
    /**
     * Clear all partial results, closing any pending RuleStates but leaving
     * completed goals intact.
     */
    public void removePartialGoals() {
        for (Iterator i= table.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
            TriplePattern goal = (TriplePattern)entry.getKey();
            GoalResults result = (GoalResults)entry.getValue();
            if ( ! result.isComplete()) {
                // Close any iterators in the dependent goal states.
                for (Iterator d = result.dependents.iterator(); d.hasNext(); ) {
                    RuleState rs = (RuleState)d.next();
                    if (rs.goalState != null) rs.goalState.close();
                }
                i.remove();
            }
        }
    }
    
    /**
     * Set all the goals in the table to "complete".
     */
    public void setAllComplete() {
        for (Iterator i = table.values().iterator(); i.hasNext(); ) {
            ((GoalResults)i.next()).setAllComplete();
        }
    }
    
    /**
     * Dump an a summary of the goal table state to stdout.
     * Just debugging, do not use for real.
     */
    public void dump() {
        System.out.println("Final goal table");
        for (Iterator i = table.values().iterator(); i.hasNext(); ) {
            GoalResults gr = (GoalResults)i.next();
            System.out.println(gr.toString() );
            for (int j = 0; j < gr.numResults(); j++) {
                System.out.println(" - " + gr.getResult(j));
            }
        }
    }
    
}



/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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