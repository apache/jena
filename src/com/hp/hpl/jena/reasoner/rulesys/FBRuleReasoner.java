/******************************************************************
 * File:        FBRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  29-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: FBRuleReasoner.java,v 1.14 2004-08-04 11:31:06 chris-dollin Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.shared.WrappedIOException;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import com.hp.hpl.jena.graph.*;
import java.util.*;

/**
 * Rule-based reasoner interface. This is the default rule reasoner to use.
 * It supports both forward reasoning and backward reasoning, including use
 * of forward rules to generate and instantiate backward rules.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.14 $ on $Date: 2004-08-04 11:31:06 $
 */
public class FBRuleReasoner implements RuleReasoner {
    
    /** The parent reasoner factory which is consulted to answer capability questions */
    protected ReasonerFactory factory;

    /** The rules to be used by this instance of the forward engine */
    protected List rules = new ArrayList();
    
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
    
    /** The original configuration properties, if any */
    protected Resource configuration;
     
    /**
     * Constructor. This is the raw version that does not reference a ReasonerFactory
     * and so has no capabilities description. 
     * @param rules a list of Rule instances which defines the ruleset to process
     */
    public FBRuleReasoner(List rules) {
        if (rules == null) throw new NullPointerException( "null rules" );
        this.rules = rules;
    }
    
    /**
     * Constructor
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     */
    public FBRuleReasoner(ReasonerFactory factory) {
        this( new ArrayList(), factory);
    }
   
    /**
     * Constructor
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     * @param configuration RDF node to configure the rule set and mode, can be null
     */
    public FBRuleReasoner(ReasonerFactory factory, Resource configuration) {
        this( new ArrayList(), factory);
        this.configuration = configuration;
        if (configuration != null) {
            StmtIterator i = configuration.listProperties();
            while (i.hasNext()) {
                Statement st = i.nextStatement();
                doSetParameter(st.getPredicate(), st.getObject().toString());
            }
        }
    }
   
    /**
     * Constructor
     * @param rules a list of Rule instances which defines the ruleset to process
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     */
    public FBRuleReasoner(List rules, ReasonerFactory factory) {
        this(rules);
        this.factory = factory;
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    protected FBRuleReasoner(List rules, Graph schemaGraph, ReasonerFactory factory) {
        this(rules, factory);
        this.schemaGraph = schemaGraph;
    }

    /**
         Add the given rules to the current set and answer this Reasoner. Provided 
         so that the Factory can deal out reasoners with specified rulesets. 
         There may well be a better way to arrange this.
         TODO review & revise
    */
    public FBRuleReasoner addRules(List rules) {
        List combined = new ArrayList( this.rules );
        combined.addAll( rules );
        setRules( combined );
        return this;
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
     * Add a configuration description for this reasoner into a partial
     * configuration specification model.
     * @param configSpec a Model into which the configuration information should be placed
     * @param base the Resource to which the configuration parameters should be added.
     */
    public void addDescription(Model configSpec, Resource base) {
        if (configuration != null) {
            StmtIterator i = configuration.listProperties();
            while (i.hasNext()) {
                Statement st = i.nextStatement();
                configSpec.add(base, st.getPredicate(), st.getObject());
            }
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
        FBRuleInfGraph graph = new FBRuleInfGraph(this, rules, getPreload(), tbox);
        graph.prepare();
        FBRuleReasoner fbr  = new FBRuleReasoner(rules, graph, factory);
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
        Graph schemaArg = schemaGraph == null ? getPreload() : (FBRuleInfGraph)schemaGraph; 
        FBRuleInfGraph graph = new FBRuleInfGraph(this, rules, schemaArg);
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
     * Return the list of Rules used by this reasoner
     * @return a List of Rule objects
     */
    public List getRules() {
        return rules;
    } 
    
    /**
         Answer the list of rules loaded from the given filename. May throw a
         ReasonerException wrapping an IOException.
    */
    public static List loadRules( String fileName ) {
        try 
            { return Rule.parseRules(Util.loadResourceFile( fileName ) ); }
        catch (WrappedIOException e) 
            { throw new ReasonerException("Can't load rules file: " + fileName, e.getCause() ); }
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
            preload = (new FBRuleInfGraph(this, rules, null));
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
     * are logged out to the Log at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        traceOn = state;
    }
    
    /**
     * Return the state of the trace flag.If set to true then rule firings
     * are logged out to the Log at "INFO" level.
     */
    public boolean isTraceOn() {
        return traceOn;
    } 

    /**
     * Set a configuration parameter for the reasoner. The supported parameters
     * are:
     * <ul>
     * <li>PROPderivationLogging - set to true to enable recording all rule derivations</li>
     * <li>PROPtraceOn - set to true to enable verbose trace information to be sent to the logger INFO channel</li>
     * </ul> 
     * 
     * @param parameter the property identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     * @throws IllegalParameterException if the parameter is unknown 
     */
    public void setParameter(Property parameter, Object value) {
        if (!doSetParameter(parameter, value)) {
            throw new IllegalParameterException("RuleReasoner does not recognize configuration parameter " + parameter);
        } else {
            // Record the configuration change
            if (configuration == null) {
                Model configModel = ModelFactory.createDefaultModel();
                configuration = configModel.createResource();
            } 
            Util.updateParameter(configuration, parameter, value);
        }
    }

    /**
     * Set a configuration parameter for the reasoner. The supported parameters
     * are:
     * <ul>
     * <li>PROPderivationLogging - set to true to enable recording all rule derivations</li>
     * <li>PROPtraceOn - set to true to enable verbose trace information to be sent to the logger INFO channel</li>
     * </ul> 
     * @param parameter the property identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     * @return false if the parameter was not known
     */
    protected boolean doSetParameter(Property parameter, Object value) {
        if (parameter.equals(ReasonerVocabulary.PROPderivationLogging)) {
            recordDerivations = Util.convertBooleanPredicateArg(parameter, value);
            return true;
        } else if (parameter.equals(ReasonerVocabulary.PROPtraceOn)) {
            traceOn =  Util.convertBooleanPredicateArg(parameter, value);
            return true;
        } else {
            return false;
        }
    }

}


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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