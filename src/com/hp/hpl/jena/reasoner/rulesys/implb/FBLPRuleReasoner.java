/******************************************************************
 * File:        FBLPRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  26-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: FBLPRuleReasoner.java,v 1.2 2003-08-13 10:45:55 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;


import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import com.hp.hpl.jena.graph.*;
import java.util.*;

/**
 * Rule-based reasoner interface. This is the default rule reasoner to use.
 * It supports both forward reasoning and backward reasoning, including use
 * of forward rules to generate and instantiate backward rules.
 * 
 * This version is purely temporary during development of the LP engine and will get
 * replaced by the upgraded FBRuleReasoner when the LP section is released.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-08-13 10:45:55 $
 */
public class FBLPRuleReasoner implements Reasoner {
    
    /** The parent reasoner factory which is consulted to answer capability questions */
    protected ReasonerFactory factory;

    /** The rules to be used by this instance of the forward engine */
    protected List rules;
    
    /** A precomputed set of schema deductions */
    protected Graph schemaGraph;
    
    /** Flag to set whether the inference class should record derivations */
    protected boolean recordDerivations = false;

    /** Flag which, if true, enables tracing of rule actions to logger.info */
    boolean traceOn = false;
//    boolean traceOn = true;

    /** Flag, if true we cache the closure of the pure rule set with its axioms */
    protected static final boolean cachePreload = true;
    
    /** The cached empty closure, if wanted */
    protected InfGraph preload;  
     
    /**
     * Constructor. This is the raw version that does not reference a ReasonerFactory
     * and so has no capabilities description. 
     * @param rules a list of Rule instances which defines the ruleset to process
     */
    public FBLPRuleReasoner(List rules) {
        this.rules = rules;
    }
    
    /**
     * Constructor
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     */
    public FBLPRuleReasoner(ReasonerFactory factory) {
        this(null, factory);
    }
    
    /**
     * Constructor
     * @param rules a list of Rule instances which defines the ruleset to process
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     */
    public FBLPRuleReasoner(List rules, ReasonerFactory factory) {
        this(rules);
        this.factory = factory;
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    protected FBLPRuleReasoner(List rules, Graph schemaGraph, ReasonerFactory factory) {
        this(rules, factory);
        this.schemaGraph = schemaGraph;
    }

    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. These capabilities may be static or may depend on configuration
     * information supplied at construction time. May be null if there are
     * no useful capabilities registered.
     */
    public Model getCapabilities() {
        if (factory != null) {
            return factory.getCapabilities();
        } else {
            return null;
        }
    }

    /**
     * Determine whether the given property is recognized and treated specially
     * by this reasoner. This is a convenience packaging of a special case of getCapabilities.
     * @param property the property which we want to ask the reasoner about, given as a Node since
     * this is part of the SPI rather than API
     * @return true if the given property is handled specially by the reasoner.
     */
    public boolean supportsProperty(Property property) {
        if (factory == null) return false;
        Model caps = factory.getCapabilities();
        Resource root = caps.getResource(factory.getURI());
        return caps.contains(root, ReasonerVocabulary.supportsP, property);
    }
    
    /**
     * Precompute the implications of a schema graph. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        if (schemaGraph != null) {
            throw new ReasonerException("Can only bind one schema at a time to an OWLRuleReasoner");
        }
        FBLPRuleInfGraph graph = new FBLPRuleInfGraph(this, rules, getPreload(), tbox);
        graph.prepare();
        FBLPRuleReasoner fbr  = new FBLPRuleReasoner(rules, graph, factory);
        fbr.setDerivationLogging(recordDerivations);
        fbr.setTraceOn(traceOn);
        return fbr;
    }
    
    /**
     * Precompute the implications of a schema Model. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Model tbox) throws ReasonerException {
        return bindSchema(tbox.getGraph());
    }
    
    /**
     * Attach the reasoner to a set of RDF data to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * 
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    public InfGraph bind(Graph data) throws ReasonerException {
        Graph schemaArg = schemaGraph == null ? getPreload() : (FBLPRuleInfGraph)schemaGraph; 
        FBLPRuleInfGraph graph = new FBLPRuleInfGraph(this, rules, schemaArg);
        graph.setDerivationLogging(recordDerivations);
        graph.setTraceOn(traceOn);
        graph.rebind(data);
        return graph;
    }
    
    /**
     * Set (or change) the rule set that this reasoner should execute.
     * @param rules a list of Rule objects
     */
    public void setRules(List rules) {
        this.rules = rules;
        preload = null;
    }
    
    /**
     * Return the this of Rules used by this reasoner
     * @return a List of Rule objects
     */
    public List getRules() {
        return rules;
    } 
    
    /**
     * Register an RDF predicate as one whose presence in a goal should force
     * the goal to be tabled. This is better done directly in the rule set.
     */
    public synchronized void tablePredicate(Node predicate) {
        // Create a dummy rule which tables the predicate ...
        Rule tablePredicateRule = new Rule("", 
                new ClauseEntry[]{
                    new Functor("table", new Node[] { predicate })
                }, 
                new ClauseEntry[]{});
        // ... end append the rule to the ruleset
        rules.add(tablePredicateRule);
    }
    
    /**
     * Get the single static precomputed rule closure.
     */
    protected synchronized InfGraph getPreload() {
        if (cachePreload && preload == null) {
            preload = (new FBLPRuleInfGraph(this, rules, null));
            preload.prepare();
        }
        return preload;
    }
       
    /**
     * Switch on/off drivation logging.
     * If set to true then the InfGraph created from the bind operation will start
     * life with recording of derivations switched on. This is currently only of relevance
     * to rule-based reasoners.
     * <p>
     * Default - false.
     */
    public void setDerivationLogging(boolean logOn) {
        recordDerivations = logOn;
    }
    
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Logger at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        traceOn = state;
    }
    
    /**
     * Set a configuration paramter for the reasoner. The supported parameters
     * are:
     * <ul>
     * <li>PROPderivationLogging - set to true to enable recording all rule derivations</li>
     * <li>PROPtraceOn - set to true to enable verbose trace information to be sent to the logger INFO channel</li>
     * </ul> 
     * 
     * @param parameterUri the uri identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     */
    public void setParameter(String parameterUri, Object value) {
        if (parameterUri.equals(ReasonerVocabulary.PROPderivationLogging.getURI())) {
            recordDerivations = Util.convertBooleanPredicateArg(parameterUri, value);
        } else if (parameterUri.equals(ReasonerVocabulary.PROPtraceOn.getURI())) {
            traceOn =  Util.convertBooleanPredicateArg(parameterUri, value);
        } else {
            throw new IllegalParameterException("Don't recognize configuration parameter " + parameterUri + " for rule-based reasoner");
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