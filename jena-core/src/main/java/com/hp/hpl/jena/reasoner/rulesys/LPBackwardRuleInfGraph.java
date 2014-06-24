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

import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;

import java.util.*;

import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.iterator.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inference graph for accessing the LP version of the backward chaining
 * rule engine.
 */
public class LPBackwardRuleInfGraph extends BaseInfGraph implements BackwardRuleInfGraphI {

//  =======================================================================
//   variables

    /** The LP rule engine controller which handles the processing for this graph */
    protected LPBRuleEngine engine;
    
    /** Table of derivation records, maps from triple to RuleDerivation */
    protected OneToManyMap<Triple, Derivation> derivations;
    
    /** An optional graph of separate schema assertions that should also be processed */
    protected FGraph fschema;
    
    /** Cache of deductions made from the rules */
    protected FGraph fdeductions;
     
    /** A finder that searches across the data, schema and axioms */
    protected Finder dataFind;
    
    /** Cache of temporary property values inferred through getTemp calls */
    protected TempNodeCache tempNodecache;
        
    static Logger logger = LoggerFactory.getLogger(LPBackwardRuleInfGraph.class);
    
//  =======================================================================
//   Core methods

    /**
     * Constructor. Create a new backward inference graph to process
     * the given data. The parent reasoner supplies the ruleset and
     * any additional schema graph.
     * 
     * @param reasoner the parent reasoner 
     * @param ruleStore the indexed set of rules to use
     * @param data the data graph to be processed
     * @param schema optional precached schema (use null if not required)
     */
    public LPBackwardRuleInfGraph(Reasoner reasoner, LPRuleStore ruleStore, Graph data, Graph schema) {
        super(data, reasoner);
        if (schema != null) {
            fschema = new FGraph(schema);
        }
        engine = new LPBRuleEngine(this, ruleStore);
        tempNodecache = new TempNodeCache(this);
    }    

    /**
     * Return the schema graph, if any, bound into this inference graph.
     */
    @Override
    public Graph getSchemaGraph() {
        return fschema.getGraph();
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
        
        fdeductions = new FGraph( Factory.createGraphMem() );
        extractAxioms();
        dataFind = fdata;
        if (fdeductions != null) {
            dataFind = FinderUtil.cascade(dataFind, fdeductions);
        }
        if (fschema != null) {
            dataFind = FinderUtil.cascade(dataFind, fschema);
        }
        
        this.setPreparedState(true);
    }

    /**
     * Replace the underlying data graph for this inference graph and start any
     * inferences over again. This is primarily using in setting up ontology imports
     * processing to allow an imports multiunion graph to be inserted between the
     * inference graph and the raw data, before processing.
     * @param data the new raw data graph
     */
    @Override
    public synchronized void rebind(Graph data) {
        engine.checkSafeToUpdate();
        fdata = new FGraph(data);
        this.setPreparedState(false);
    }
    
    /**
     * Cause the inference graph to reconsult the underlying graph to take
     * into account changes. Normally changes are made through the InfGraph's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfGraph's back" and this forces a full reconsult of
     * the changed data. 
     */
    @Override
    public synchronized void rebind() {
        version++;
        engine.checkSafeToUpdate();
        this.setPreparedState(false);
    }

    /**
     * Flush out all cached results. Future queries have to start from scratch.
     */
    @Override
    public synchronized void reset() {
        version++;
        engine.checkSafeToUpdate();
        engine.reset();
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
    public synchronized ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation) {
        checkOpen();
        this.requirePrepared();
        ExtendedIterator<Triple> result = engine.find(pattern).filterKeep( new UniqueFilter<Triple>());
        if (continuation != null) {
            result = result.andThen(continuation.find(pattern));
        }
        return result.filterDrop(Functor.acceptFilter);
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
        engine.checkSafeToUpdate();
        fdata.getGraph().add(t);
        this.setPreparedState(false);
    }
     
    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    @Override
    public synchronized void performDelete(Triple t) {
        version++;
        engine.checkSafeToUpdate();
        fdata.getGraph().delete(t);
        this.setPreparedState(false);
    }
       
    /**
     * Set a predicate to be tabled/memoized by the LP engine. 
     */
    public void setTabled(Node predicate) {
        engine.tablePredicate(predicate);
        if (isTraceOn()) {
            logger.info("LP TABLE " + predicate);
        }
    }
    
//  =======================================================================
//   support for proof traces

    /**
     * Set to true to enable derivation caching
     */
    @Override
    public void setDerivationLogging(boolean recordDerivations) {
        engine.setDerivationLogging(recordDerivations);
        if (recordDerivations) {
            derivations = new OneToManyMap<>();
        } else {
            derivations = null;
        }
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
        engine.setTraceOn(state);
    }
    
    /**
     * Return true if tracing is switched on
     */
    public boolean isTraceOn() {
        return engine.isTraceOn();
    }
        
//    =======================================================================
//     Interface between infGraph and the goal processing machinery

    /**
     * Log a dervivation record against the given triple.
     */
    @Override
    public void logDerivation(Triple t, Derivation derivation) {
        derivations.put(t, derivation);
    }

    /**
     * Match a pattern just against the stored data (raw data, schema,
     * axioms) but no derivation.
     */
    @Override
    public ExtendedIterator<Triple> findDataMatches(TriplePattern pattern) {
        return dataFind.find(pattern);
    }
            
    /**
     * Process a call to a builtin predicate
     * @param clause the Functor representing the call
     * @param env the BindingEnvironment for this call
     * @param rule the rule which is invoking this call
     * @return true if the predicate succeeds
     */
    @Override
    public boolean processBuiltin(ClauseEntry clause, Rule rule, BindingEnvironment env) {
        throw new ReasonerException("Internal error in FBLP rule engine, incorrect invocation of building in rule " + rule); 
    }
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    @Override
    public void silentAdd(Triple t) {
        fdeductions.getGraph().add(t);
    }

    /**
     * Retrieve or create a bNode representing an inferred property value.
     * @param instance the base instance node to which the property applies
     * @param prop the property node whose value is being inferred
     * @param pclass the (optional, can be null) class for the inferred value.
     * @return the bNode representing the property value 
     */
    @Override
    public Node getTemp(Node instance, Node prop, Node pclass) {
        return tempNodecache.getTemp(instance, prop, pclass);
    }
    
//    =======================================================================
//     Rule engine extras

    /**
     * Find any axioms (rules with no body) in the rule set and
     * add those to the auxilliary graph to be included in searches.
     */
    protected void extractAxioms() {
        Graph axioms = fdeductions.getGraph();
        BBRuleContext contextForBuiltins = null;
        for ( Rule rule : engine.getRuleStore().getAllRules() )
        {
            if ( rule.bodyLength() == 0 )
            {
                // An axiom
                for ( int j = 0; j < rule.headLength(); j++ )
                {
                    ClauseEntry axiom = rule.getHeadElement( j );
                    if ( axiom instanceof TriplePattern )
                    {
                        axioms.add( ( (TriplePattern) axiom ).asTriple() );
                    }
                    else if ( axiom instanceof Functor )
                    {
                        if ( contextForBuiltins == null )
                        {
                            contextForBuiltins = new BBRuleContext( this );
                        }
                        Functor f = (Functor) axiom;
                        Builtin implementation = f.getImplementor();
                        if ( implementation == null )
                        {
                            throw new ReasonerException( "Attempted to invoke undefined functor: " + f );
                        }
                        Node[] args = f.getArgs();
                        contextForBuiltins.setEnv( new BindingVector( args ) );
                        contextForBuiltins.setRule( rule );
                        implementation.headAction( args, args.length, contextForBuiltins );
                    }
                }
            }
        }
    }

}
