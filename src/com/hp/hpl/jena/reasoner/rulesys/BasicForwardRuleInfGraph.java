/******************************************************************
 * File:        BasicForwardRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  30-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BasicForwardRuleInfGraph.java,v 1.23 2003-06-19 12:58:05 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.iterator.*;

import org.apache.log4j.Logger;

/**
 * An inference graph interface that runs a set of forward chaining
 * rules to conclusion on each added triple and stores the entire
 * result set.
 * <p>
 * This implementation has a horribly inefficient rule chainer built in.
 * Once we have this working generalize this to an interface than
 * can call out to a rule engine and build a real rule engine (e.g. Rete style). </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.23 $ on $Date: 2003-06-19 12:58:05 $
 */
public class BasicForwardRuleInfGraph extends BaseInfGraph implements ForwardRuleInfGraphI {

//=======================================================================
// variables
    
    /** Table of derivation records, maps from triple to RuleDerivation */
    protected OneToManyMap derivations;
    
    /** The set of deduced triples, this is in addition to base triples in the fdata graph */
    protected FGraph fdeductions;
    
    /** Reference to any schema graph data bound into the parent reasoner */
    protected Graph schemaGraph;
    
    /** The forward rule engine being used */
    protected FRuleEngineI engine;
    
    /** The original rule set as supplied */
    protected List rules;
    
    /** Flag which, if true, enables tracing of rule actions to logger.info */
    boolean traceOn = false;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(BasicForwardRuleInfGraph.class);
    
//=======================================================================
// Core methods

   /**
    * Constructor. Creates a new inference graph to which a (compiled) rule set
    * and a data graph can be attached. This separation of binding is useful to allow
    * any configuration parameters (such as logging) to be set before the data is added.
    * Note that until the data is added using {@link #rebind rebind} then any operations
    * like add, remove, find will result in errors.
    * 
    * @param reasoner the parent reasoner 
    * @param schema the (optional) schema data which is being processed
    */
   public BasicForwardRuleInfGraph(Reasoner reasoner, Graph schema) {
       super(null, reasoner);
       instantiateRuleEngine(null);
       this.schemaGraph = schema;
   }    

   /**
    * Constructor. Creates a new inference graph based on the given rule set. 
    * No data graph is attached at this stage. This is to allow
    * any configuration parameters (such as logging) to be set before the data is added.
    * Note that until the data is added using {@link #rebind rebind} then any operations
    * like add, remove, find will result in errors.
    * 
    * @param reasoner the parent reasoner 
    * @param rules the list of rules to use this time
    * @param schema the (optional) schema or preload data which is being processed
    */
   public BasicForwardRuleInfGraph(Reasoner reasoner, List rules, Graph schema) {
       super(null, reasoner);
       instantiateRuleEngine(rules);
       this.rules = rules;
       this.schemaGraph = schema;
   }    

    /**
     * Constructor. Creates a new inference graph based on the given rule set
     * then processes the initial data graph. No precomputed deductions are loaded.
     * 
     * @param reasoner the parent reasoner 
     * @param rules the list of rules to use this time
     * @param schema the (optional) schema or preload data which is being processed
     * @param the data graph to be processed
     */
    public BasicForwardRuleInfGraph(Reasoner reasoner, List rules, Graph schema, Graph data) {
        this(reasoner, rules, schema);
        rebind(data);
    }

    /**
     * Instantiate the forward rule engine to use.
     * Subclasses can override this to switch to, say, a RETE imlementation.
     * @param rules the rule set or null if there are not rules bound in yet.
     */
    protected void instantiateRuleEngine(List rules) {
        if (rules != null) {
            engine = new FRuleEngine(this, rules);
        } else {
            engine = new FRuleEngine(this);
        }
    }
    
    /**
     * Attach a compiled rule set to this inference graph.
     * @param rulestore a compiled set of rules (i.e. the result of an FRuleEngine.compile). 
     */
    public void setRuleStore(Object ruleStore) {
        engine.setRuleStore(ruleStore);
    }
    
    /**
     * Replace the underlying data graph for this inference graph and start any
     * inferences over again. This is primarily using in setting up ontology imports
     * processing to allow an imports multiunion graph to be inserted between the
     * inference graph and the raw data, before processing.
     * @param data the new raw data graph
     */
    public void rebind(Graph data) {
        fdata = new FGraph( data );
        rebind();
    }
    
    /**
     * Cause the inference graph to reconsult the underlying graph to take
     * into account changes. Normally changes are made through the InfGraph's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfGraph's back" and this forces a full reconsult of
     * the changed data. 
     */
    public void rebind() {
        isPrepared = false;
    }
    
    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    public synchronized void prepare() {
        isPrepared = true;
        // initilize the deductions graph
        fdeductions = new FGraph( new GraphMem() );
        boolean rulesLoaded = false;
        if (schemaGraph != null) {
            rulesLoaded = preloadDeductions(schemaGraph);
        }
        if (rulesLoaded) {
            engine.fastInit(fdata); 
        } else {
            engine.init(true, fdata);
        }
    }

    /**
     * Adds a set of precomputed triples to the deductions store. These do not, themselves,
     * fire any rules but provide additional axioms that might enable future rule
     * firing when real data is added. Used to implement bindSchema processing
     * in the parent Reasoner.
     * @return return true if the rule set has also been loaded
     */
    protected boolean preloadDeductions(Graph preloadIn) {
        Graph d = fdeductions.getGraph();
        BasicForwardRuleInfGraph preload = (BasicForwardRuleInfGraph)preloadIn;
        // If the rule set is the same we can reuse those as well
        if (preload.rules == rules) {
            // Load raw deductions
            for (Iterator i = preload.find(null, null, null); i.hasNext(); ) {
                d.add((Triple)i.next());
            }
            engine.setRuleStore(preload.engine.getRuleStore());
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Add a new deduction to the deductions graph.
     */
    public void addDeduction(Triple t) {
        getDeductionsGraph().add(t);
    }
   
    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        if (!isPrepared) prepare();
        if (fdata == null) {
            return fdeductions.findWithContinuation(pattern, continuation);
        } else {
            if (continuation == null) {
                return fdata.findWithContinuation(pattern, fdeductions);
            } else {
                return fdata.findWithContinuation(pattern, FinderUtil.cascade(fdeductions, continuation) );
            }
        }
    }
   
    /** 
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     */
    public ExtendedIterator find(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), null);
    }

    /**
     * Basic pattern lookup interface.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        return findWithContinuation(pattern, null);
    }
    

    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public synchronized void add(Triple t) {
        fdata.getGraph().add(t);
        if (isPrepared) {
            engine.add(t);
        }
    }
    
    /**
     * Returns the bitwise or of ADD, DELETE, SIZE and ORDERED,
     * to show the capabilities of this implementation of Graph.
     * So a read-only graph that finds in an unordered fashion,
     * but can tell you how many triples are in the graph returns
     * SIZE.
     */
    public int capabilities() {
        return ADD | SIZE | DELETE;
    }

    /**
     * Return the number of triples in the inferred graph
     */
    public int size() {
        return fdata.getGraph().size() + fdeductions.getGraph().size();
    }
    
    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    public void delete(Triple t) {
        if (fdata != null) {
            Graph data = fdata.getGraph();
            if (data != null) {
                data.delete(t);
            }
        }
        if (isPrepared) {
            fdeductions.getGraph().delete(t);
        }
    }

//  =======================================================================
//   Implementation of ForwardRuleInfGraphI interface which is used by
//   the forward rule engine to invoke functions in this InfGraph
    
    /**
     * Adds a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void addBRule(Rule brule) {
        throw new ReasonerException("Forward reasoner does not support hybrid rules - " + brule.toShortString());
    }
        
    /**
     * Deletes a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void deleteBRule(Rule brule) {
        throw new ReasonerException("Forward reasoner does not support hybrid rules - " + brule.toShortString());
    }
    
    /**
     * Return the Graph containing all the static deductions available so far.
     */
    public Graph getDeductionsGraph() {
        return fdeductions.getGraph();
    }
    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
     */
    public ExtendedIterator findDataMatches(Node subject, Node predicate, Node object) {
        return find(subject, predicate, object);
    }
   

    /**
     * Log a dervivation record against the given triple.
     */
    public void logDerivation(Triple t, Object derivation) {
        derivations.put(t, derivation);
    }
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    public void silentAdd(Triple t) {
        fdeductions.getGraph().add(t);
    }

//=======================================================================
// support for proof traces

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
        engine.setDerivationLogging(recordDerivations);
        if (recordDerivations) {
            derivations = new OneToManyMap();
        } else {
            derivations = null;
        }
    }
    
    /**
     * Return true if derivation logging is enabled.
     */
    public boolean shouldLogDerivations() {
        return recordDerivations;
    }
    
    /**
     * Return the derivation of at triple.
     * The derivation is a List of DerivationRecords
     */
    public Iterator getDerivation(Triple t) {
        if (derivations == null) {
            return new NullIterator();
        } else {
            return derivations.getAll(t);
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
     * Return true if tracing should be acted on - i.e. if traceOn is true
     * and we are past the bootstrap phase.
     */
    public boolean shouldTrace() {
        return traceOn && engine.shouldTrace();
    }
    
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized
     */
    public long getNRulesFired() {
        return engine.getNRulesFired();
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
