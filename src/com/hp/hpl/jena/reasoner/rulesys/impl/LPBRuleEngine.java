/******************************************************************
 * File:        LPBRuleEngine.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPBRuleEngine.java,v 1.1 2003-08-21 12:04:45 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.iterator.*;

import org.apache.log4j.Logger;
import java.util.*;

/**
 * LP version of the core backward chaining engine. For each parent inference
 * graph (whether pure backward or hybrid) there should be one LPBRuleEngine
 * instance. The shared instance holds any common result caching, rule store
 * and global state data. However, all the processing is done by instances
 * of the LPInterpreter - one per query.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-08-21 12:04:45 $
 */
public class LPBRuleEngine {
    
//  =======================================================================
//   variables

    /** Store which holds the raw and compiled rules */
    protected LPRuleStore ruleStore;
    
    /** The parent inference graph to which this engine is attached */
    protected BackwardRuleInfGraphI infGraph;
    
    /** True if debug information should be written out */
    protected boolean traceOn = false;
    
    /** Set to true to flag that derivations should be logged */
    protected boolean recordDerivations;
        
    /** List of engine instances which are still processing queries */
    protected List activeInterpreters = new ArrayList();
    
    /** Table mapping tabled goals to generators for those goals.
     *  This is here so that partial goal state can be shared across multiple queries. */
    protected HashMap tabledGoals = new HashMap();
    
    /** Set of generators waiting to be run */
//    protected LinkedList agenda = new LinkedList();
//    protected List agenda = new ArrayList();
    protected Collection agenda = new HashSet();
    
    /** Optional profile of number of time each rule is entered, set to non-null to profile */
    protected HashMap profile;
    
    /** The number of generator cycles to wait before running a completion check.
     *  If set to 0 then checks will be done in the generator each time. */
    public static final int CYCLES_BETWEEN_COMPLETION_CHECK = 3;
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(LPBRuleEngine.class);
    
//  =======================================================================
//  Constructors
    
    /**
     * Constructor.
     * @param infGraph the parent inference graph which is using this engine
     * @param rules the indexed set of rules to process
     */
    public LPBRuleEngine(BackwardRuleInfGraphI infGraph, LPRuleStore rules) {
        this.infGraph = infGraph;
        ruleStore = rules;
    }
    
    /**
     * Constructor. Creates an empty engine to which rules must be added.
     * @param infGraph the parent inference graph which is using this engine
     */
    public LPBRuleEngine(BackwardRuleInfGraphI infGraph) {
        this.infGraph = infGraph;
        ruleStore = new LPRuleStore();
    }
    
//  =======================================================================
//  Control methods
    
    /**
     * Start a new interpreter running to answer a query.
     * @param goal the query to be processed
     * @return a closable iterator over the query results
     */
    public synchronized ExtendedIterator find(TriplePattern goal) {
        LPInterpreter interpreter = new LPInterpreter(this, goal);
        activeInterpreters.add(interpreter);
        return WrappedIterator.create( new LPTopGoalIterator(interpreter));
    }
    
    /**
     * Clear all tabled results.
     */
    public synchronized void reset() {
        checkSafeToUpdate();
        tabledGoals = new HashMap();
        agenda.clear();
    }
    
    /**
     * Add a single rule to the store.
     * N.B. This will invalidate current partial results and the engine
     * should be reset() before future queries. 
     */
    public synchronized void addRule(Rule rule) {
        checkSafeToUpdate();
        ruleStore.addRule(rule);
    }
    
    /**
     * Remove a single rule from the store.
     * N.B. This will invalidate current partial results and the engine
     * should be reset() before future queries. 
     */
    public synchronized void deleteRule(Rule rule) {
        checkSafeToUpdate();
        ruleStore.deleteRule(rule);
    }
    
    /**
     * Return an ordered list of all registered rules.
     */
    public synchronized List getAllRules() {
        checkSafeToUpdate();
        return ruleStore.getAllRules();
    }
    
    /**
     * Delete all the rules.
     */
    public synchronized void deleteAllRules() {
        checkSafeToUpdate();
        ruleStore.deleteAllRules();     
    }
    
    /**
     * Stop the current work. Forcibly stop all current query instances over this engine.
     */
    public synchronized void halt() {
        for (Iterator i = activeInterpreters.iterator(); i.hasNext(); ) {
            ((LPInterpreter)i.next()).close();
        }
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
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
    }
    
    /**
     * Return true in derivations should be logged.
     */
    public boolean getDerivationLogging() {
        return recordDerivations;
    }

    /** Return the rule store associated with the inference graph */
    public LPRuleStore getRuleStore() {
        return ruleStore;
    }
    
    /** Return the parent infernce graph associated with this engine */
    public BackwardRuleInfGraphI getInfGraph() {
        return infGraph;
    }
    
    /** Detatch the given engine from the list of active engines for this inf graph */
    public synchronized void detach(LPInterpreter engine) {
        activeInterpreters.remove(engine);
    }
    
    /**
     * Check that there are no currently processing queries.
     * Could throw an exception here but often this can be caused by simply leaving
     * an unclosed iterator. So instead we try to close the iterators and assume the
     * rest of the context will be reset by the add call.
     * 
     * <p>Should be called from with a synchronized block.
     */
    public void checkSafeToUpdate() {
        if (!activeInterpreters.isEmpty()) {
            ArrayList toClose = new ArrayList();
            for (Iterator i = activeInterpreters.iterator(); i.hasNext(); ) {
                LPInterpreter interpreter = (LPInterpreter)i.next();
                if (interpreter.iContext instanceof LPTopGoalIterator) {
                    toClose.add(interpreter.iContext);
                }
            }
            for (Iterator i = toClose.iterator(); i.hasNext(); ) {
                ((LPTopGoalIterator)i.next()).close();
            }
        }
    }
    
    
//  =======================================================================
//  Interface for tabled operations

    /**
     * Register an RDF predicate as one whose presence in a goal should force
     * the goal to be tabled.
     */
    public synchronized void tablePredicate(Node predicate) {
        ruleStore.tablePredicate(predicate);
    }
    
    /**
     * Return a generator for the given goal (assumes that the caller knows that
     * the goal should be tabled).
     * @param goal the goal whose results are to be generated
     * @param clauses the precomputed set of code blocks used to implement the goal
     */
    public synchronized Generator generatorFor(TriplePattern goal, List clauses) {
        Generator generator = (Generator) tabledGoals.get(goal);
        if (generator == null) {
            LPInterpreter interpreter = new LPInterpreter(this, goal, clauses, false);
            activeInterpreters.add(interpreter);
            generator = new Generator(interpreter, goal);
            schedule(generator);
            tabledGoals.put(goal, generator);
        }
        return generator;
    }
        
    /**
     * Return a generator for the given goal (assumes that the caller knows that
     * the goal should be tabled).
     * @param goal the goal whose results are to be generated
     */
    public synchronized Generator generatorFor(TriplePattern goal) {
        Generator generator = (Generator) tabledGoals.get(goal);
        if (generator == null) {
            LPInterpreter interpreter = new LPInterpreter(this, goal, false);
            activeInterpreters.add(interpreter);
            generator = new Generator(interpreter, goal);
            schedule(generator);
            tabledGoals.put(goal, generator);
        }
        return generator;
    }
    
    /**
     * Register that a generator or specific generator state (Consumer choice point)
     * is now ready to run.
     */
    public void schedule(LPAgendaEntry state) {
        agenda.add(state);
    }
    
    /**
     * Run the scheduled generators until the given generator is ready to run.
     */
    public synchronized void pump(LPInterpreterContext gen) {
//        System.out.println("Pump agenda on engine " + this + ", size = " + agenda.size());
        Collection batch = null;
        if (CYCLES_BETWEEN_COMPLETION_CHECK > 0) {
            batch = new ArrayList(CYCLES_BETWEEN_COMPLETION_CHECK);
        }
        int count = 0; 
        while(!gen.isReady()) {
            if (agenda.isEmpty()) {
//                System.out.println("Cycled " + this + ", " + count);
                return;
            } 
            Iterator ai = agenda.iterator();
            LPAgendaEntry next = (LPAgendaEntry) ai.next();
            ai.remove();
//            int chosen = agenda.size() - 1;
//            LPAgendaEntry next = (LPAgendaEntry) agenda.get(chosen);
//            agenda.remove(chosen);
//            System.out.println("  pumping entry " + next);
            next.pump();
            count ++;
            if (CYCLES_BETWEEN_COMPLETION_CHECK > 0) {
                batch.add(next.getGenerator());
                if (count % CYCLES_BETWEEN_COMPLETION_CHECK == 0) {
                    Generator.checkForCompletions(batch);
                    batch.clear();
                }
            }
        }
        if (CYCLES_BETWEEN_COMPLETION_CHECK > 0 && !batch.isEmpty()) {
            Generator.checkForCompletions(batch);
        }
        
//        System.out.println("Cycled " + this + ", " + count);
    }
     
    /**
     * Check all known interpeter contexts to see if any are complete.
     */
    public void checkForCompletions() {
        ArrayList contexts = new ArrayList(activeInterpreters.size());
        for (Iterator i = activeInterpreters.iterator(); i.hasNext(); ) {
            LPInterpreter interpreter = (LPInterpreter)i.next();
            if (interpreter.iContext instanceof Generator) {
                contexts.add(interpreter.iContext);     
            }
        }
        Generator.checkForCompletions(contexts);
    }
    
//  =======================================================================
//  Profiling support
   
    /**
     * Record a rule invocation in the profile count.
     */
    public void incrementProfile(RuleClauseCode clause) {
        if (profile != null) {
            String index = clause.toString();
            Count count = (Count)profile.get(index);
            if (count == null) {
                profile.put(index, new Count(clause).inc());
            } else {
                count.inc();
            }
        }
    }
    
    /**
     * Reset the profile.
     * @param enable it true then profiling will continue with a new empty profile table,
     * if false profiling will stop all current data lost.
     */
    public void resetProfile(boolean enable) {
        profile = enable ? new HashMap() : null;
    }
    
    /**
     * Print a profile of rules used since the last reset.
     */
    public void printProfile() {
        if (profile == null) {
            System.out.println("No profile collected");
        } else {
            ArrayList counts = new ArrayList();
            counts.addAll(profile.values());
            Collections.sort(counts);
            System.out.println("LP engine rule profile");
            for (Iterator i = counts.iterator(); i.hasNext(); ) {
                System.out.println(i.next());
            }
        }
    }
    
    /**
     * Record count of number of rule invocations, used in profile structure only.
     */
    static class Count implements Comparable {
        protected int count = 0;
        protected RuleClauseCode clause;

        /** Constructor */
        public Count(RuleClauseCode clause) {
            this.clause = clause;
        }
        
        /** return the count value */
        public int getCount() {
            return count;        
        }
        
        /** increment the count value, return the count object */
        public Count inc() {
            count++;
            return this;
        }
        
        /** Ordering */
        public int compareTo(Object other) {
            Count otherCount = (Count) other;
            return (count < otherCount.count) ? -1 : ( (count == otherCount.count) ? 0 : +1);
        }
        
        /** Printable form */
        public String toString() {
            return " " + count + "\t - " + clause;
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