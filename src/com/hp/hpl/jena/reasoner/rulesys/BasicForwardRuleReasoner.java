/******************************************************************
 * File:        BasicForwardRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  30-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BasicForwardRuleReasoner.java,v 1.2 2003-05-08 15:08:53 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

/**
 * Reasoner implementation which augments or transforms an RDF graph
 * according to a set of rules. This trivial version does not support
 * separate schema processing. The actual work is done in the inference
 * graph implementation.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-05-08 15:08:53 $
 */
public class BasicForwardRuleReasoner implements Reasoner {

    /** The rules to be used by this instance of the forward engine */
    protected List rules;
    
    /** A precomputed set of schema deductions */
    protected InfGraph schemaGraph;
    
    /** Flag to set whether the inference class should record derivations */
    protected boolean recordDerivations = false;
    
    /** threshold on the numbers of rule firings allowed in a single operation */
    protected long nRulesThreshold = BasicForwardRuleInfGraph.DEFAULT_RULES_THRESHOLD;

    /** Flag which, if true, enables tracing of rule actions to logger.info */
    boolean traceOn = false;
    
    /** Base URI used for configuration properties for rule reasoners */
    static final String URI = "http://www.hpl.hp.com/semweb/2003/BasicRuleReasoner";
     
    /** Property used to configure the derivation logging behaviour of the reasoner.
     *  Set to "true" to enable logging of derivations. */
    public static final Property PROPderivationLogging = ResourceFactory.createProperty(URI+"#", "derivationLogging");
    
    /** Property used to configure the tracing behaviour of the reasoner.
     *  Set to "true" to enable internal trace message to be sent to Logger.info . */
    public static final Property PROPtraceOn = ResourceFactory.createProperty(URI+"#", "traceOn");
    
    /** Property used to configure the maximum number of rule firings allowed in 
     * a single operation. Should be an xsd:int. */
    public static final Property PROPrulesThreshold = ResourceFactory.createProperty(URI+"#", "rulesThreshold");
    
    /**
     * Constructor
     * @param rules a list of Rule instances which defines the ruleset to process
     */
    public BasicForwardRuleReasoner(List rules) {
        this.rules = rules;
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    private BasicForwardRuleReasoner(List rules, InfGraph schemaGraph) {
        this.rules = rules;
        this.schemaGraph = schemaGraph;
    }
    
    /**
     * Precompute the implications of a schema graph. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        InfGraph graph = new BasicForwardRuleInfGraph(this, rules, tbox);
        return new BasicForwardRuleReasoner(rules, graph);
    }
    
    /**
     * Precompute the implications of a schema Model. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Model tbox) throws ReasonerException {
        InfGraph graph = new BasicForwardRuleInfGraph(this, rules, tbox.getGraph());
        return new BasicForwardRuleReasoner(rules, graph);
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
        BasicForwardRuleInfGraph graph = new BasicForwardRuleInfGraph(this, rules);
        graph.setDerivationLogging(recordDerivations);
        graph.setRuleThreshold(nRulesThreshold);
        graph.setTraceOn(traceOn);
        if (schemaGraph != null) {
            graph.preloadDeductions(schemaGraph);
        }
        graph.bindData(data);
        return graph;
    }
    
    /**
     * Return the this of Rules used by this reasoner
     * @return a List of Rule objects
     */
    public List getRules() {
        return rules;
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
     * Set the threshold on the numbers of rule firings allowed in a single operation.
     */
    public void setRulesThreshold(long threshold) {
        nRulesThreshold = threshold;
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
     * <li>BasicForwaredRuleReasoner.PROPderivationLogging - set to true to enable recording all rule derivations</li>
     * <li>BasicForwaredRuleReasoner.PROPtraceOn - set to true to enable verbose trace information to be sent to the logger INFO channel</li>
     * <li>BasicForwaredRuleReasoner.PROPrulesThreshold - set a limit on the number of rule firings allowed in a derivation to prevent infinite loops</li>
     * </ul> 
     * 
     * @param parameterUri the uri identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     */
    public void setParameter(String parameterUri, Object value) {
        if (parameterUri.equals(PROPderivationLogging.getURI())) {
            recordDerivations = Util.convertBooleanPredicateArg(parameterUri, value);
        } else if (parameterUri.equals(PROPtraceOn.getURI())) {
            traceOn =  Util.convertBooleanPredicateArg(parameterUri, value);
        } else if (parameterUri.equals(PROPrulesThreshold.getURI())) {
            nRulesThreshold =  Util.convertIntegerPredicateArg(parameterUri, value);
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
