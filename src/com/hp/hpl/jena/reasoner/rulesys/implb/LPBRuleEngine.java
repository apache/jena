/******************************************************************
 * File:        LPBRuleEngine.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPBRuleEngine.java,v 1.4 2003-08-08 16:12:53 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

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
 * @version $Revision: 1.4 $ on $Date: 2003-08-08 16:12:53 $
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
    protected LinkedList agenda = new LinkedList();
    
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
//        System.out.println("Adding rule: " + rule);
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
     * Check that there are no currently processing queries, if there are
     * throw a ConcurrentModification exception. Should be called from with
     * a synchronized block.
     */
    public void checkSafeToUpdate() {
        if (!activeInterpreters.isEmpty()) {
            throw new ConcurrentModificationException("Backward query engine is active during attempt to update graph data");
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
    public synchronized Generator generatorFor(TriplePattern goal, Collection clauses) {
        Generator generator = (Generator) tabledGoals.get(goal);
        if (generator == null) {
            LPInterpreter interpreter = new LPInterpreter(this, goal, clauses);
            activeInterpreters.add(interpreter);
            generator = new Generator(interpreter);
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
            LPInterpreter interpreter = new LPInterpreter(this, goal);
            activeInterpreters.add(interpreter);
            generator = new Generator(interpreter);
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
     * Run the scheduled generators until the given generator is ready to run
     * then run that generator until it generates some results or closes.
     */
    public synchronized void pump(LPAgendaEntry gen) {
        while(!gen.isReady()) {
            if (agenda.isEmpty()) return;
            // TODO: Consider scanning agenda for entries with max # dependents
            LPAgendaEntry next = (LPAgendaEntry) agenda.removeFirst();
            next.pump();
        }
        gen.pump();
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