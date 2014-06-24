/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.iterator.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * LP version of the core backward chaining engine. For each parent inference
 * graph (whether pure backward or hybrid) there should be one LPBRuleEngine
 * instance. The shared instance holds any common result caching, rule store
 * and global state data. However, all the processing is done by instances
 * of the LPInterpreter - one per query.
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
    protected List<LPInterpreter> activeInterpreters = new ArrayList<>();
    
    /** Table mapping tabled goals to generators for those goals.
     *  This is here so that partial goal state can be shared across multiple queries. */
    protected HashMap<TriplePattern, Generator> tabledGoals = new HashMap<>();
    
    /** Set of generators waiting to be run */
    protected LinkedList<LPAgendaEntry> agenda = new LinkedList<>();
//    protected List agenda = new ArrayList();
//    protected Collection agenda = new HashSet();
    
    /** Optional profile of number of time each rule is entered, set to non-null to profile */
    protected HashMap<String, Count> profile;
    
    /** The number of generator cycles to wait before running a completion check.
     *  If set to 0 then checks will be done in the generator each time. */
    public static final int CYCLES_BETWEEN_COMPLETION_CHECK = 3;
    
    static Logger logger = LoggerFactory.getLogger(LPBRuleEngine.class);
    
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
    public synchronized ExtendedIterator<Triple> find(TriplePattern goal) {
        LPInterpreter interpreter = new LPInterpreter(this, goal);
        activeInterpreters.add(interpreter);
        return WrappedIterator.create( new LPTopGoalIterator(interpreter));
    }
    
    /**
     * Clear all tabled results.
     */
    public synchronized void reset() {
        checkSafeToUpdate();
        tabledGoals = new HashMap<>();
        agenda.clear();
    }
    
    /**
     * Add a single rule to the store.
     * N.B. This will invalidate current partial results and the engine
     * should be reset() before future queries. 
     */
    public synchronized void addRule(Rule rule) {
        checkSafeToUpdate();
        if (rule.headLength() > 1) {
            throw new ReasonerException("Backward rules only allowed one head clause");
        }
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
    public synchronized List<Rule> getAllRules() {
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
        ArrayList<LPInterpreter> copy = new ArrayList<>(activeInterpreters);
        // Copy because closing the LPInterpreter will detach it from this engine which affects activeInterpreters
        for ( LPInterpreter aCopy : copy )
        {
            aCopy.close();
        }
    }
       
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Log at "INFO" level.
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
     * <p>Should be called from within a synchronized block.
     */
    public void checkSafeToUpdate() {
        if (!activeInterpreters.isEmpty()) {
            ArrayList<LPInterpreterContext> toClose = new ArrayList<>();
            for ( LPInterpreter interpreter : activeInterpreters )
            {
                if ( interpreter.getContext() instanceof LPTopGoalIterator )
                {
                    toClose.add( interpreter.getContext() );
                }
            }
            for ( LPInterpreterContext aToClose : toClose )
            {
                ( (LPTopGoalIterator) aToClose ).close();
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
    public synchronized Generator generatorFor(TriplePattern goal, List<RuleClauseCode> clauses) {
        Generator generator = tabledGoals.get(goal);
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
        Generator generator = tabledGoals.get(goal);
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
    public void pump(LPInterpreterContext gen) {
        Collection<Generator> batch = null;
        if (CYCLES_BETWEEN_COMPLETION_CHECK > 0) {
            batch = new ArrayList<>(CYCLES_BETWEEN_COMPLETION_CHECK);
        }
        int count = 0; 
        while(!gen.isReady()) {
            if (agenda.isEmpty()) {
//                System.out.println("Cycled " + this + ", " + count);
                return;
            }
            
            LPAgendaEntry next = getNextAgendaEntry(); 
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
     * Pick and agenda entry to progress and remove it from the queue
     */
    private LPAgendaEntry getNextAgendaEntry() {
        synchronized (agenda) {
            int chosen = agenda.size() - 1;
            LPAgendaEntry next = agenda.get(chosen);
            agenda.remove(chosen);
            return next;
        }
    }
    
    /**
     * Check all known interpeter contexts to see if any are complete.
     */
    public void checkForCompletions() {
        List<Generator> contexts = null;
        synchronized (activeInterpreters) {
            contexts = new ArrayList<>(activeInterpreters.size());
            for ( LPInterpreter activeInterpreter : activeInterpreters )
            {
                LPInterpreterContext context = activeInterpreter.getContext();
                if ( context instanceof Generator )
                {
                    contexts.add( (Generator) context );
                }
            }

        }
        Generator.checkForCompletions( contexts );
    }
    
//  =======================================================================
//  Profiling support
   
    /**
     * Record a rule invocation in the profile count.
     */
    public void incrementProfile(RuleClauseCode clause) {
        if (profile != null) {
            String index = clause.toString();
            Count count = profile.get(index);
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
        profile = enable ? new HashMap<String, Count>() : null;
    }
    
    /**
     * Print a profile of rules used since the last reset.
     */
    public void printProfile() {
        if (profile == null) {
            System.out.println("No profile collected");
        } else {
            ArrayList<Count> counts = new ArrayList<>();
            counts.addAll(profile.values());
            Collections.sort(counts);
            System.out.println("LP engine rule profile");
            for ( Count count : counts )
            {
                System.out.println( count );
            }
        }
    }
    
    /**
     * Record count of number of rule invocations, used in profile structure only.
     */
    static class Count implements Comparable<Count> {
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
        @Override
        public int compareTo(Count other) {
            return (count < other.count) ? -1 : ( (count == other.count) ? 0 : +1);
        }
        
        /** Printable form */
        @Override
        public String toString() {
            return " " + count + "\t - " + clause;
        }
        
    }
}
