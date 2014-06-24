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

package com.hp.hpl.jena.reasoner.rulesys;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.BaseInfGraph;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.FGraph;
import com.hp.hpl.jena.reasoner.Finder;
import com.hp.hpl.jena.reasoner.FinderUtil;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.impl.FRuleEngineI;
import com.hp.hpl.jena.reasoner.rulesys.impl.FRuleEngineIFactory;
import com.hp.hpl.jena.reasoner.rulesys.impl.SafeGraph;
import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;

/**
 * An inference graph interface that runs a set of forward chaining
 * rules to conclusion on each added triple and stores the entire
 * result set.
 * <p>
 * This implementation has a horribly inefficient rule chainer built in.
 * Once we have this working generalize this to an interface than
 * can call out to a rule engine and build a real rule engine (e.g. Rete style). </p>
 */

public class BasicForwardRuleInfGraph extends BaseInfGraph implements ForwardRuleInfGraphI {
    
    /** Table of derivation records, maps from triple to RuleDerivation */
    protected OneToManyMap<Triple, Derivation> derivations;
    
    /** The set of deduced triples, this is in addition to base triples in the fdata graph */
    protected FGraph fdeductions;
    
    /** A safe wrapped version of the deductions graph used for reporting getDeductions */
    protected Graph safeDeductions;
    
    /** Reference to any schema graph data bound into the parent reasoner */
    protected Graph schemaGraph;
    
    /** The forward rule engine being used */
    protected FRuleEngineI engine;
    
    /** The original rule set as supplied */
    private List<Rule> rules;
    
    /** Flag, if true then find results will be filtered to remove functors and illegal RDF */
    public boolean filterFunctors = true;
    
    /** Flag which, if true, enables tracing of rule actions to logger.info */
    protected boolean traceOn = false;
    
//    private static Logger logger = LoggerFactory.getLogger(BasicForwardRuleInfGraph.class);
    
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
   public BasicForwardRuleInfGraph(Reasoner reasoner, List<Rule> rules, Graph schema)
   {       
       super( null, reasoner );
       instantiateRuleEngine( rules );
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
     * @param data the data graph to be processed
     */
    public BasicForwardRuleInfGraph(Reasoner reasoner, List<Rule> rules, Graph schema, Graph data) {
        this(reasoner, rules, schema);
        rebind(data);
    }

    /**
     * Instantiate the forward rule engine to use.
     * Subclasses can override this to switch to, say, a RETE imlementation.
     * @param rules the rule set or null if there are not rules bound in yet.
     */
    protected void instantiateRuleEngine(List<Rule> rules) {
        engine = FRuleEngineIFactory.getInstance().createFRuleEngineI(this, rules, false);
    }
    
    /**
     * Attach a compiled rule set to this inference graph.
     * @param ruleStore a compiled set of rules (i.e. the result of an FRuleEngine.compile). 
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
    @Override
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
    @Override
    public void rebind() {
        version++;
        this.setPreparedState(false);
    }

    /**
     * Return the schema graph, if any, bound into this inference graph.
     */
    @Override
    public Graph getSchemaGraph() {
        return schemaGraph;
    }
        
    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    @Override
    public synchronized void prepare() {
        if (this.isPrepared()) return;
        this.setPreparedState(true);
        
        // initilize the deductions graph
        fdeductions = new FGraph( createDeductionsGraph() );
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
        BasicForwardRuleInfGraph preload = (BasicForwardRuleInfGraph) preloadIn;
        // If the rule set is the same we can reuse those as well
        if (preload.rules == rules) {
            // Load raw deductions
            for (Iterator<Triple> i = preload.find(null, null, null); i.hasNext(); ) {
                d.add(i.next());
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
    @Override
    public void addDeduction(Triple t) {
        getDeductionsGraph().add(t);
    }
    
    /**
     * Set to true to cause functor-valued literals to be dropped from rule output.
     * Default is true.
     */
    @Override
    public void setFunctorFiltering(boolean param) {
        filterFunctors = param;
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
    @Override
    public ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation) {
        return findWithContinuation(pattern, continuation, true);
    }
    
    /**
     * Internals of findWithContinuation implementation which allows control
     * over functor filtering.
     */
    private ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation, boolean filter) {
        checkOpen();
        this.requirePrepared();
        ExtendedIterator<Triple> result = null;
        if (fdata == null) {
            result = fdeductions.findWithContinuation(pattern, continuation);
        } else {
            if (continuation == null) {
                result = fdata.findWithContinuation(pattern, fdeductions);
            } else {
                result = fdata.findWithContinuation(pattern, FinderUtil.cascade(fdeductions, continuation) );
            }
        }
        if (filter && filterFunctors) {
          return result.filterDrop(Functor.acceptFilter);
        } else {
            return result;
        }
    }
   
    /** 
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     */
    @Override
    public ExtendedIterator<Triple> graphBaseFind(Node subject, Node property, Node object) {
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
    @Override
    public ExtendedIterator<Triple> find(TriplePattern pattern) {
        return findWithContinuation(pattern, null);
    }
    

    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    @Override
    public synchronized void performAdd(Triple t) {
        version++;
        fdata.getGraph().add(t);
        if (this.isPrepared()) {
            engine.add(t);
        }
    }

    /**
     * Return the number of triples in the inferred graph
     */
    @Override
    public int graphBaseSize() {
        checkOpen();
        this.requirePrepared();
        int baseSize = fdata.getGraph().size();
        int dedSize = fdeductions.getGraph().size();
        // System.err.println( ">> BasicForwardRuleInfGraph::size = " + baseSize + "(base) + " + dedSize + "(deductions)" );
        return baseSize + dedSize;
    }
    
    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    @Override
    public void performDelete(Triple t) {
        version++;
        if (fdata != null) {
            Graph data = fdata.getGraph();
            if (data != null) {
                data.delete(t);
            }
        }
        if (!this.isPrepared()) {
            fdeductions.getGraph().delete(t);
        }
    }
   
    /** 
     * Free all resources, any further use of this Graph is an error.
     */
    @Override
    public void close() {
        if (!closed) {
            engine = null;
            fdeductions = null;
            rules = null;
            schemaGraph = null;
            super.close();
        }
    }

//  =======================================================================
//   Implementation of ForwardRuleInfGraphI interface which is used by
//   the forward rule engine to invoke functions in this InfGraph
    
    /**
     * Adds a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    @Override
    public void addBRule(Rule brule) {
        throw new ReasonerException("Forward reasoner does not support hybrid rules - " + brule.toShortString());
    }
        
    /**
     * Deletes a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    @Override
    public void deleteBRule(Rule brule) {
        throw new ReasonerException("Forward reasoner does not support hybrid rules - " + brule.toShortString());
    }
    
    /**
     * Return the Graph containing all the static deductions available so far.
     * Will force a prepare.
     */
    @Override
    public Graph getDeductionsGraph() {
        prepare();
        return safeDeductions; 
    }
   
    /** 
     * Create the graph used to hold the deductions. Can be overridden
     * by subclasses that need special purpose graph implementations here.
     * Assumes the graph underlying fdeductions and associated SafeGraph
     * wrapper can be reused if present thus enabling preservation of
     * listeners. 
     */
    protected Graph createDeductionsGraph() {
        if (fdeductions != null) {
            Graph dg = fdeductions.getGraph();
            if (dg != null) {
                // Reuse the old graph in order to preserve any listeners
                safeDeductions.clear();
                return dg;
            }
        }
        Graph dg = Factory.createGraphMem( ); 
        safeDeductions = new SafeGraph( dg );
        return dg;
    }
    
    /**
     * Return the Graph containing all the static deductions available so far.
     * Does not trigger a prepare action. Returns a SafeWrapper and so
     * can be used for update (thus triggering listeners) but not
     * for access to generalized triples
     */
    @Override
    public Graph getCurrentDeductionsGraph() {
        return safeDeductions;
//        return fdeductions.getGraph();
    }
    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
     */
    @Override
    public ExtendedIterator<Triple> findDataMatches(Node subject, Node predicate, Node object) {
        return findWithContinuation(new TriplePattern(subject, predicate, object), null, false);
    }
   

    /**
     * Log a dervivation record against the given triple.
     */
    @Override
    public void logDerivation(Triple t, Derivation derivation) {
        derivations.put(t, derivation);
    }
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    @Override
    public void silentAdd(Triple t) {
        fdeductions.getGraph().add(t);
    }

//=======================================================================
// support for proof traces

    /**
     * Set to true to enable derivation caching
     */
    @Override
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
        engine.setDerivationLogging(recordDerivations);
        if (recordDerivations) {
            derivations = new OneToManyMap<>();
        } else {
            derivations = null;
        }
    }
    
    /**
     * Return true if derivation logging is enabled.
     */
    @Override
    public boolean shouldLogDerivations() {
        return recordDerivations;
    }
    
    /**
     * Return the derivation of at triple.
     * The derivation is a List of DerivationRecords
     */
    @Override
    public Iterator<Derivation> getDerivation(Triple t) {
        if (derivations == null) {
            return new NullIterator<>();
        } else {
            return derivations.getAll(t);
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
     * Return true if tracing should be acted on - i.e. if traceOn is true
     * and we are past the bootstrap phase.
     */
    @Override
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
