/******************************************************************
 * File:        Generator.java
 * Created by:  Dave Reynolds
 * Created on:  06-Aug-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Generator.java,v 1.4 2003-08-08 16:12:53 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import java.util.*;

import com.hp.hpl.jena.reasoner.ReasonerException;
//import com.hp.hpl.jena.reasoner.rulesys.impl.StateFlag;

/**
 * A generator represents a set of memoized results for a single 
 * tabled subgoal. The generator may be complete (in which case it just
 * contains the complete cached set of results for a goal), ready (not complete
 * but likely to product more results if called) or blocked (not complete and
 * awaiting results from a dependent generator).
 * <p>
 * Each generator may have multiple associated consumer choice points 
 * representing different choices in satisfying the generator's goal.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-08-08 16:12:53 $
 */
public class Generator implements LPAgendaEntry {

    /** The intepreter instance which generates the results for this goal, 
     *  null if the generator is complete */
    protected LPInterpreter interpreter;
        
    /** The ordered set of results available for the goal */
    protected ArrayList results = new ArrayList();
    
    /** A indexed version of the result set, used while the generator is live 
     *  to detect duplicate results */
    protected Set resultSet = new HashSet();
    
    /** set to true if the dependent generator has new results ready for us */
    protected boolean isReady = true;
    
    /** set to true once the interpter has first been run (and so has at least one blocked CCP by now) */
    protected boolean hasStarted = false;
    
    /** The list of consumer choice points which are awaiting results from us */
    protected List dependents = new ArrayList();
    
    /** The list of active consumer choice points for this generator */
    protected List choicePoints = new ArrayList();
    
    /**
     * Constructor.
     * 
     * @param interpreter an initialized interpreter instance that will answer 
     * results for this generator.
     */
    public Generator(LPInterpreter interpreter) {
        this.interpreter = interpreter;
    }
    
    /**
     * Signal that this generator is complete, no more results can be created.
     */
    public void setComplete() {
        if (!isComplete()) {
            interpreter.close();
            interpreter = null;
            resultSet = null;
            isReady = false;
            if ( ! choicePoints.isEmpty()) {
                throw new ReasonerException("Internal error in LP implementation: closing generator with dangling ccps");
            }
            choicePoints = null;
            for (Iterator i = dependents.iterator(); i.hasNext(); ) {
                ConsumerChoicePointFrame ccp = (ConsumerChoicePointFrame) i.next();
                ccp.generator.choicePoints.remove(ccp);
            }
            dependents = null;
        }
    }
        
    /**
     * Signal dependents that we have new results.
     */
    public void notifyResults() {
        // TODO: Fix
//        LPBRuleEngine engine = interpreter.getEngine();
//        for (Iterator i = dependents.iterator(); i.hasNext(); ) {
//            ConsumerChoicePointFrame dep = (ConsumerChoicePointFrame)i.next();
//            engine.schedule(dep);
//            dep.blockedOn = null;
//            dep.generator.isReady = true;
//        }
//        dependents.clear();
    }

    /**
     * Notify that the interpreter has now blocked, awaiting more data
     * for a generator via the given choice point.
     */
    public void notifyBlockedOn(ConsumerChoicePointFrame ccp) {
        choicePoints.add(ccp);
    }
    
    /**
     * Return true if the generator is ready to be scheduled (i.e. it is not
     * known to be complete and not known to be waiting for a dependent generator).
     */
    public boolean isReady() {
        return isReady;
    }
    
    /**
     * Return true if the generator is complete.
     */
    public boolean isComplete() {
        return interpreter == null;
    }
    
    /**
     * Remove the given generator from the list of dependents of this one.
     */
    protected void removeDependent(ConsumerChoicePointFrame dep) {
        dependents.remove(dep);
    }
    
    /**
     * Add the given generator to the list of dependents of this one.
     */
    protected void addDependent(ConsumerChoicePointFrame dep) {
        dependents.add(dep);
    }
    
    /**
     * Cycle this generator until it either completes or blocks.
     */
    public synchronized void pump() {
        // TODO: Replace completely, it is currently in a partially rewritten state
//        if (hasStarted) return;     // Once started we schedule individual CCPs
//        hasStarted = true;
//        int priorNresults = results.size();
//        while (true) {
//            Object result = interpreter.next(this);
//            if (result == StateFlag.FAIL) {
//                if (results.size() > priorNresults) notifyResults();
//                setComplete();
//                return;
//            } else if (result == StateFlag.SUSPEND) {
//                
//                blockon(interpreter.getBlockingGenerator());
//                if (isIndirectlyComplete()) {
//                    setComplete();
//                }
//                finished = true;
//            } else {
//                // Simple triple result
//                if (resultSet.add(result)) {
//                    results.add(result);
//                }
//            }
//        }
//        if (results.size() > priorNresults) {
//            propagateResultState(notifyList);
//        }
    }
    
    /**
     * Check for deadlocked states where none of the generators we are (indirectly)
     * dependent on can run.
     */
    protected boolean checkForCompletions() {
        HashSet visited = new HashSet();
        visited.add(this);
        return runCompletionCheck(visited);
    }
    
    /**
     * Check for deadlocked states where none of the generators we are (indirectly)
     * dependent on can run.
     */
    protected boolean runCompletionCheck(Set visited) {
        // TODO: rewrite
        return false;
//        if (isReady()) {
//            return false;
//        } else if (visited.add(this)) {
//            for (Iterator i = choicePoints.iterator(); i.hasNext(); ) {
//                ConsumerChoicePointFrame ccp = (ConsumerChoicePointFrame)i.next();
//                if (ccp.blockedOn == null) {
//                    return false;
//                } else if ( ! ccp.blockedOn.runCompletionCheck(visited)) {
//                    return false;
//                }
//            }
//            // Gets here if all descendents are mutually blocked
//            // Mark as complete now, though this might be moved to a second pass over the visited set 
//            setComplete();
//            return true;
//        } else {
//            return true;
//        }
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