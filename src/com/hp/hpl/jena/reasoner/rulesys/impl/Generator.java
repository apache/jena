/******************************************************************
 * File:        Generator.java
 * Created by:  Dave Reynolds
 * Created on:  06-Aug-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: Generator.java,v 1.6 2005-03-23 14:04:24 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.*;

import com.hp.hpl.jena.reasoner.TriplePattern;

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
 * @version $Revision: 1.6 $ on $Date: 2005-03-23 14:04:24 $
 */
public class Generator implements LPAgendaEntry, LPInterpreterContext {

    /** The intepreter instance which generates the results for this goal, 
     *  null if the generator is complete */
    protected LPInterpreter interpreter;
        
    /** The ordered set of results available for the goal */
    protected ArrayList results = new ArrayList();
    
    /** A indexed version of the result set, used while the generator is live 
     *  to detect duplicate results */
    protected Set resultSet;
    
    /** set to true if the dependent generator has new results ready for us */
    protected boolean isReady = true;
    
    /** set to true if at least one branch has block so an active readiness check is required */
    protected boolean checkReadyNeeded = false;
    
    /** The set of choice points producing results for us to use */
    protected Set generatingCPs = new HashSet();
    
    /** The list of active consumer choice points consuming results from this generator */
    protected Set consumingCPs = new HashSet();
    
    /** Flags whether the generator is live/dead/unknown during completion checking */
    protected LFlag completionState;
    
    /** The goal the generator is satisfying - just used in debugging */
    protected TriplePattern goal;
    
    /** True if this generator can produce at most one answer */
    protected boolean isSingleton;
    
//    /** Distance of generator from top level goal, used in scheduling */
//    protected int depth = DEFAULT_DEPTH;
//    
//    /** Default depth if not known */
//    public static final int DEFAULT_DEPTH = 100;
    
    /**
     * Constructor.
     * 
     * @param interpreter an initialized interpreter instance that will answer 
     * results for this generator.
     */
    public Generator(LPInterpreter interpreter, TriplePattern goal) {
        this.interpreter = interpreter;
        this.goal = goal;       // Just used for debugging
        isSingleton = goal.isGround();
        if (!isSingleton) resultSet = new HashSet();
    }
    
    /**
     * Return the number of results available from this context.
     */
    public int numResults() {
        return results.size();
    }
    
    /**
     * Return true if the generator is ready to be scheduled (i.e. it is not
     * known to be complete and not known to be waiting for a dependent generator).
     */
    public boolean isReady() {
        if (isComplete()) return false;
        if (checkReadyNeeded) {
            isReady = false;
            for (Iterator i = generatingCPs.iterator(); i.hasNext(); ) {
                if ( ((ConsumerChoicePointFrame)i.next()).isReady() ) {
                    isReady = true;
                    break;
                }
            }
            checkReadyNeeded = false;
            return isReady;
        } else {
            return isReady;
        }
    }
    
    /**
     * Directly set that this generator is ready (because the generator
     * for one of its generatingCPs has produced new results).
     */
    public void setReady(ConsumerChoicePointFrame ccp) {
        if (!isComplete()) {
            interpreter.engine.schedule(ccp);
            isReady = true;
            checkReadyNeeded = false;
        }
    }
    
    /**
     * Return true if the generator is complete.
     */
    public boolean isComplete() {
        return interpreter == null;
    }
    
//    /**
//     * Return the estimated number of generators between the top level goal and this one.
//     */
//    public int getDepth() {
//        return depth;
//    }
    
//    /**
//     * Set the depth of this generator, it will not be propagated until
//     * a further depednents are found.
//     */
//    public void setDepth(int d) {
//        depth = d;
//    }
    
    /**
     * Signal that this generator is complete, no more results can be created.
     */
    public void setComplete() {
        if (!isComplete()) {
            interpreter.close();
            interpreter = null;
            resultSet = null;
            isReady = false;
            completionState = LFlag.DEAD;
            // Anyone we were generating results for is now finished
            for (Iterator i = consumingCPs.iterator(); i.hasNext(); ) {
                ConsumerChoicePointFrame ccp = (ConsumerChoicePointFrame)i.next();
                if ( ! ccp.isReady()) {
                    ccp.setFinished();
                }
            }
            generatingCPs = null;
            consumingCPs.clear();
        }
    }
    
    /**
     * Add a new client choince point to consume results from this generator.
     */
    public void addConsumer(ConsumerChoicePointFrame ccp) {
        consumingCPs.add(ccp);
//        // Update distance from top goal
//        int newDepth = ccp.context == null ? 1 : ccp.context.getDepth() + 1;
//        if (newDepth < depth) depth = newDepth;
    }
    
    /**
     * Remove a terminated consuming choice point from the state set.
     */
    public void removeConsumer(ConsumerChoicePointFrame ccp) {
        consumingCPs.remove(ccp);
        // We used to set it complete if there were no consumers left.
        // However, a generator might be part of one query, incompletely consumed
        // and then opened again on a different query,
        // it seems better to omit this. TODO review
//        if (!isComplete() &&consumingCPs.isEmpty()) {
//            setComplete();
//        }
    }
        
    /**
     * Signal dependents that we have new results.
     */
    public void notifyResults() {
        LPBRuleEngine engine = interpreter.getEngine();
        for (Iterator i = consumingCPs.iterator(); i.hasNext(); ) {
            ConsumerChoicePointFrame cons = (ConsumerChoicePointFrame)i.next();
            cons.setReady();
        }
    }

    /**
     * Notify that the interpreter has now blocked on the given choice point.
     */
    public void notifyBlockedOn(ConsumerChoicePointFrame ccp) {
        generatingCPs.add(ccp);
        checkReadyNeeded = true; 
    }
    
    /** 
     * Notify this context that the given choice point has terminated
     * and can be remove from the wait list. 
     */
    public void notifyFinished(ConsumerChoicePointFrame ccp) {
        if (generatingCPs != null) {
            generatingCPs.remove(ccp); 
        }
        checkReadyNeeded = true;
    }

    /**
     * Start this generator running for the first time.
     * Should be called from within an appropriately synchronized block.
     */
    public void pump() {
        pump(this);
    }
    
    /**
     * Start this generator running from the given previous blocked generating
     * choice point.
     * Should be called from within an appropriately synchronized block.
     */
    public void pump(LPInterpreterState context) {
        if (isComplete()) return;
        interpreter.setState(context);
        int priorNresults = results.size();
        while (true) {
            Object result = interpreter.next();
            if (result == StateFlag.FAIL) {
                checkReadyNeeded = true;
                break;
            } else {
                // Simple triple result
                if (isSingleton) {
                    results.add(result);
                    isReady = false;
                    break;
                } else if (resultSet.add(result)) {
                    results.add(result);
                }
            }
        }
        if (results.size() > priorNresults) {
            notifyResults();
        }
        // Early termination check, close a singleton as soon as we have the ans
        if (isSingleton && results.size() == 1) {
            setComplete();
        }
        if (LPBRuleEngine.CYCLES_BETWEEN_COMPLETION_CHECK == 0) {
            checkForCompletions();
        }
    }
    
    /**
     * Return the generator associated with this entry (might be the entry itself)
     */
    public Generator getGenerator() {
        return this;
    }
    
    /**
     * Check for deadlocked states where none of the generators we are (indirectly)
     * dependent on can run.
     */
    public void checkForCompletions() {
        HashSet visited = new HashSet();
        if (runCompletionCheck(visited) != LFlag.LIVE) {
            postCompletionCheckScan(visited);
        }
    }
    
    /**
     * Check for deadlocked states across a collection of generators which have
     * been run.
     */
    public static void checkForCompletions(Collection completions) {
        HashSet visited = new HashSet();
        boolean atLeastOneZombie = false;
        for (Iterator i = completions.iterator(); i.hasNext(); ) {
            Generator g = (Generator)i.next();
            if (g.runCompletionCheck(visited) != LFlag.LIVE) {
                atLeastOneZombie = true;
            }
        }
        if (atLeastOneZombie) {
            postCompletionCheckScan(visited);
        }
    }
    
    /**
     * Check whether this generator is live (indirectly dependent on a ready
     * generator), dead (complete) or in a deadlock loop which might or
     * might not be live (unknown).
     */
    protected LFlag runCompletionCheck(Set visited) {
        if (isComplete()) return LFlag.DEAD;
        if (! visited.add(this)) return this.completionState;
        completionState = LFlag.UNKNOWN;
        if (isReady()) {
            completionState = LFlag.LIVE;
        } else {
            for (Iterator i = generatingCPs.iterator(); i.hasNext(); ) {
                ConsumerChoicePointFrame ccp = (ConsumerChoicePointFrame)i.next();
                if (ccp.isReady()) {
                    completionState = LFlag.LIVE;
                    break;
                } else if ( ccp.generator.runCompletionCheck(visited) == LFlag.LIVE) {
                    completionState = LFlag.LIVE;
                    break;
                }
            }
        }
        return completionState;
    }
    
    /**
     * Scan the result of a (set of) completion check(s) to detect which of the
     * unknowns are actually live and set the remaining (deadlocked) states
     * to complete.
     */
    protected static void postCompletionCheckScan(Set visited ) {
        for (Iterator iv = visited.iterator(); iv.hasNext(); ) {
            Generator g = (Generator)iv.next();
            if (g.completionState == LFlag.LIVE) {
                for (Iterator i = g.consumingCPs.iterator(); i.hasNext(); ) {
                    LPInterpreterContext link = ((ConsumerChoicePointFrame)i.next()).getConsumingContext();
                    if (link instanceof Generator) {
                        ((Generator)link).propagateLive(visited); 
                    }
                }
            }
        }
        
        for (Iterator iv = visited.iterator(); iv.hasNext(); ) {
            Generator g = (Generator)iv.next();
            if (g.completionState != LFlag.LIVE) {
                g.setComplete();
            }
        }
        return;
     }
    
    /**
     * Propagate liveness state forward to consuming generators, but only those 
     * within the filter set.
     */
    protected void propagateLive(Set filter) {
        if (completionState != LFlag.LIVE) {
            completionState = LFlag.LIVE;
            for (Iterator i = consumingCPs.iterator(); i.hasNext(); ) {
                LPInterpreterContext link = ((ConsumerChoicePointFrame)i.next()).getConsumingContext();
                if (link instanceof Generator) {
                    ((Generator)link).propagateLive(filter); 
                }
            }
        }
    }
    
    /**
     * Inner class used to flag generator states during completeness check.
     */
    private static class LFlag {
    
        /** Label for printing */
        private String label;

        public static final LFlag LIVE = new LFlag("Live");
        public static final LFlag DEAD = new LFlag("Dead");
        public static final LFlag UNKNOWN = new LFlag("Unknown");
    
        /** Constructor */
        private LFlag(String label) {
            this.label = label;
        }
    
        /** Print string */
        public String toString() {
            return label;
        }
    }

    
}


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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