/******************************************************************
 * File:        GenericRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  08-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: GenericRuleReasoner.java,v 1.6 2003-06-17 15:51:16 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.RuleStore;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

import java.io.IOException;
import java.util.*;

/**
 * A reasoner interface that is able to invoke any of the useful
 * rule engine combinations. The rule set can be set after the reasoner
 * instance is created. The mode can be set to forward, backward or hybrid.
 * The OWL-specific rule augmentation can be included. Each of these settings
 * can be controlled using the configuration graph, specific methods calls or
 * generic setParameter calls.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-06-17 15:51:16 $
 */
public class GenericRuleReasoner extends FBRuleReasoner {

    /** Prepared set of rules used for Backward-only mode */
    protected RuleStore bRuleStore;
    
    /** the current rule mode */
    protected RuleMode mode = HYBRID;
    
    /** Flag, if true we cache the closure of the pure rule set with its axioms */
    protected static final boolean cachePreload = true;
    
    /** Flag, if true then subClass and subProperty lattices will be optimized using TGCs, only applicable to HYBRID reasoners */
    protected boolean enableTGCCaching = false;
    
    /** Flag, if true then rules will be augmented by OWL translations of the schema */
    protected boolean enableOWLTranslation = false;
    
    /** Constant - the mode description for pure forward chaining */
    public static final RuleMode FORWARD = new RuleMode("forward");
    
    /** Constant - the mode description for pure forward chaining, using RETE engine */
    public static final RuleMode FORWARD_RETE = new RuleMode("forwardRETE");
    
    /** Constant - the mode description for pure backward chaining */
    public static final RuleMode BACKWARD = new RuleMode("backward");
    
    /** Constant - the mode description for mixed forward/backward */
    public static final RuleMode HYBRID = new RuleMode("hybrid");
    
//  =======================================================================
//  Constructors
     
    /**
     * Constructor. This is the raw version that does not reference a ReasonerFactory
     * and so has no capabilities description. 
     * @param rules a list of Rule instances which defines the ruleset to process
     */
    public GenericRuleReasoner(List rules) {
        super(rules);
    }
    
    /**
     * Constructor
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     * @param configuration RDF model to configure the rule set and mode, can be null
     */
    public GenericRuleReasoner(ReasonerFactory factory, Model configuration) {
        super(factory);
        if (configuration != null) {
            Resource base = configuration.getResource(GenericRuleReasonerFactory.URI);
            StmtIterator i = base.listProperties();
            while (i.hasNext()) {
                Statement st = i.nextStatement();
                doSetParameter(st.getPredicate().getURI(), st.getObject().toString());
            }
        }
    }
    
    /**
     * Constructor
     * @param rules a list of Rule instances which defines the ruleset to process
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     */
    public GenericRuleReasoner(List rules, ReasonerFactory factory) {
        super(rules, factory);
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    protected GenericRuleReasoner(List rules, Graph schemaGraph, ReasonerFactory factory, RuleMode mode) {
        this(rules, factory);
        this.schemaGraph = schemaGraph;
        this.mode = mode;
    }
    
//  =======================================================================
//  Parameter control

    /**
     * Set the direction of rule inference desired.
     * If set to a pure mode (FORWARD, BACKWARD) then the rules will be
     * interpreted as operating in that direction which ever direction
     * they were written in. In HYBRID mode then the direction of the rule
     * itself which control whether it is used in forward or backward mode. 
     * In addition, HYBRID mode allows forward rules to generate addition
     * backward rules.
     */
    public void setMode(RuleMode mode) {
        if (schemaGraph != null) {
            throw new ReasonerException("Can't change mode of a reasoner bound to a schema");
        }
        this.mode = mode;
        preload = null;
        bRuleStore = null;
    }
    
    /**
     * Set (or change) the rule set that this reasoner should execute.
     * @param rules a list of Rule objects
     */
    public void setRules(List rules) {
        // Currently redunant but it will differ from the inherited
        // version in the future
        super.setRules(rules);
    }
    
    /**
     * Set to true to enable translation of selected parts of an OWL schema
     * to additional rules. At present only intersction statements are handled this way.
     * The translation is only applicable in HYBRID mode.
     */
    public void setOWLTranslation(boolean enableOWLTranslation) {
        if (enableOWLTranslation && (mode != HYBRID)) {
            throw new ReasonerException("Can only enable OWL rule translation in HYBRID mode");
        }
        this.enableOWLTranslation = enableOWLTranslation;        
    }
    
    /**
     * Set to true to enable caching of subclass/subproperty lattices in a
     * specialized cache rather than using the rule systems. This has substantially
     * higher performance but it is done as a separate initialization pass and so
     * can only work correct with some rule sets. This is only guaranteed to be implemented
     * for the HYBRID mode.
     */
    public void setTransitiveClosureCaching(boolean enableTGCCaching) {
        this.enableTGCCaching = enableTGCCaching;
    }
    
    /**
     * Set a configuration paramter for the reasoner. The supported parameters
     * are:
     * <ul>
     * <li>PROPderivationLogging - set to true to enable recording all rule derivations</li>
     * <li>PROPtraceOn - set to true to enable verbose trace information to be sent to the logger INFO channel</li>
     * <li>PROPruleMode - set to "forward", "backward" or "hybrid" to control the rule processing direction</li>
     * <li>PROPruleSet - argument is a string giving the URI for a rule set to load</li>
     * <li>PROPenableOWLTranslation - set to true to enable translation of OWL schema elements to rules</li>
     * </ul> 
     * 
     * @param parameterUri the uri identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     */
    public void setParameter(String parameterUri, Object value) {
        if (!doSetParameter(parameterUri, value)) {
            throw new IllegalParameterException("GenericRuleReasoner doesn't recognize configuration parameter " + parameterUri);
        }
    }
    
    /**
     * Internal version of setParameter that does not directly raise an
     * exception on parameters it does not reconize.
     * @return false if the parameter was not recognized
     */
    private boolean doSetParameter(String parameterUri, Object value) {
        if (parameterUri.equals(ReasonerVocabulary.PROPderivationLogging.getURI())) {
            recordDerivations = Util.convertBooleanPredicateArg(parameterUri, value);
            
        } else if (parameterUri.equals(ReasonerVocabulary.PROPtraceOn.getURI())) {
            traceOn =  Util.convertBooleanPredicateArg(parameterUri, value);
            
        } else if (parameterUri.equals(ReasonerVocabulary.PROPenableOWLTranslation.getURI())) {
            enableOWLTranslation =  Util.convertBooleanPredicateArg(parameterUri, value);
            
        } else if (parameterUri.equals(ReasonerVocabulary.PROPruleMode.getURI())) {
            if (value.equals(FORWARD.name)) {
                mode = FORWARD;
            } else if (value.equals(FORWARD_RETE.name)) {
                mode = FORWARD_RETE;
            } else if (value.equals(BACKWARD.name)) {
                mode = BACKWARD;
            } else if (value.equals(HYBRID.name)) {
                mode = HYBRID;
            } else {
                throw new IllegalParameterException("PROPruleMode can only be 'forward'm 'forwardRETE', 'backward', 'hybrid', not " + value);
            }
            
        } else if (parameterUri.equals(ReasonerVocabulary.PROPruleSet.getURI())) {
            if (value instanceof String) {
                try {
                    String ruleString = Util.loadURLFile((String)value);
                    setRules(Rule.parseRules(ruleString));
                } catch (IOException e) {
                    throw new ReasonerException("Failed to open rule file: " + value, e);
                }
            } else {
                throw new IllegalParameterException("PROPruleSet value should be a URI string. Was a " + value.getClass());
            }
        } else {
            return false;
        }
        return true;
    }
    
//  =======================================================================
//  Implementation methods

    /**
     * Precompute the implications of a schema graph. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        if (schemaGraph != null) {
            throw new ReasonerException("Can only bind one schema at a time to a GenericRuleReasoner");
        }
        Graph graph = null;
        if (mode == FORWARD) {
            graph = new BasicForwardRuleInfGraph(this, rules, null, tbox);
            ((InfGraph)graph).prepare();
        } else if (mode == FORWARD_RETE) {
                graph = new RETERuleInfGraph(this, rules, null, tbox);
                ((InfGraph)graph).prepare();
        } else if (mode == BACKWARD) {
            graph = tbox;
        } else {
            List ruleSet = rules;
            if (enableOWLTranslation) {
                ruleSet = OWLFBRuleReasoner.augmentRules(ruleSet, tbox);
            }
            graph = new FBRuleInfGraph(this, ruleSet, getPreload(), tbox);
            if (enableTGCCaching) ((FBRuleInfGraph)graph).setUseTGCCache();
            ((FBRuleInfGraph)graph).prepare();
        }
        GenericRuleReasoner grr = new GenericRuleReasoner(rules, graph, factory, mode);
        grr.setDerivationLogging(recordDerivations);
        grr.setTraceOn(traceOn);
        grr.setTransitiveClosureCaching(enableTGCCaching);
        return grr;
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
        Graph schemaArg = schemaGraph == null ? getPreload() : schemaGraph;
        InfGraph graph = null; 
        if (mode == FORWARD) {
            graph = new BasicForwardRuleInfGraph(this, rules, schemaArg);
            ((BasicForwardRuleInfGraph)graph).setTraceOn(traceOn);
        } else if (mode == FORWARD_RETE) {
                graph = new RETERuleInfGraph(this, rules, schemaArg);
                ((BasicForwardRuleInfGraph)graph).setTraceOn(traceOn);
        } else if (mode == BACKWARD) {
            graph = new BasicBackwardRuleInfGraph(this, getBruleStore(), data, schemaArg);
            ((BasicBackwardRuleInfGraph)graph).setTraceOn(traceOn);
        } else {
            List ruleSet = ((FBRuleInfGraph)schemaArg).getRules();
            if (enableOWLTranslation) {
                ruleSet = OWLFBRuleReasoner.augmentRules(ruleSet, data);
            }
            graph = new FBRuleInfGraph(this, ruleSet, schemaArg);
            if (enableTGCCaching) ((FBRuleInfGraph)graph).setUseTGCCache();
            ((FBRuleInfGraph)graph).setTraceOn(traceOn);
        }
        graph.setDerivationLogging(recordDerivations);
        graph.rebind(data);
        return graph;
    }
    
    /**
     * Get the single static precomputed rule closure.
     */
    protected synchronized InfGraph getPreload() {
        if (cachePreload && preload == null && mode == HYBRID) {
            if (mode == HYBRID) {
                preload = new FBRuleInfGraph(this, rules, null);
                if (enableTGCCaching) ((FBRuleInfGraph)preload).setUseTGCCache();
            } else if (mode == FORWARD) {
                preload = new BasicForwardRuleInfGraph(this, rules, null);
            } else if (mode == FORWARD_RETE) {
                preload = new RETERuleInfGraph(this, rules, null);
            }
            preload.prepare();
        }
        return preload;
    }
    
    /**
     * Return the prepared backward only rules.
     */
    protected RuleStore getBruleStore() {
        if (bRuleStore == null) {
            bRuleStore = new RuleStore(rules);
        }
        return bRuleStore;
    }
    
//  =======================================================================
//  Inner classes

    /** 
     * Class used as an enum for describing rule modes.
     */
    public static class RuleMode {
        /** Name for the mode */
        String name;
        
        /** Constructor */
        protected RuleMode(String name) {
            this.name = name;
        }
        
        public String toString() {
            return name;
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