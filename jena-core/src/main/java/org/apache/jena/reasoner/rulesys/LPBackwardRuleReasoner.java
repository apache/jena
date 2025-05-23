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

package org.apache.jena.reasoner.rulesys;


import java.util.*;

import org.apache.jena.graph.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.reasoner.* ;
import org.apache.jena.reasoner.rulesys.impl.* ;
import org.apache.jena.vocabulary.ReasonerVocabulary ;

/**
 * Reasoner implementation which augments or transforms an RDF graph
 * according to a set of rules. The rules are processed using a
 * tabled LP backchaining interpreter which is implemented by the
 * relevant InfGraph class.
 */
public class LPBackwardRuleReasoner implements Reasoner {

    /** The parent reasoner factory which is consulted to answer capability questions */
    protected ReasonerFactory factory;

    /** The rules to be used by this instance of the backward engine */
    protected List<Rule> rules;

    /** Indexed, normalized copy of the rule list */
    protected LPRuleStore ruleStore;

    /** A cache set of schema data used in partial binding chains */
    protected Graph schemaGraph;

    /** Flag to set whether the inference class should record derivations */
    protected boolean recordDerivations = false;

    /** Flag which, if true, enables tracing of rule actions to logger.info */
    boolean traceOn = false;

    /**
     * Constructor. This is the raw version that does not reference a ReasonerFactory
     * and so has no capabilities description.
     * @param rules a list of Rule instances which defines the ruleset to process
     */
    public LPBackwardRuleReasoner(List<Rule> rules) {
        this.rules = rules;
        ruleStore = new LPRuleStore(rules);
    }

    /**
     * Constructor
     * @param rules a list of Rule instances which defines the ruleset to process
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     */
    public LPBackwardRuleReasoner(List<Rule> rules, ReasonerFactory factory) {
        this.rules = rules;
        this.factory = factory;
        ruleStore = new LPRuleStore(rules);
    }

    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    protected LPBackwardRuleReasoner(LPBackwardRuleReasoner parent, Graph schemaGraph) {
        rules = parent.rules;
        ruleStore = parent.ruleStore;
        this.schemaGraph = schemaGraph;
        this.factory = parent.factory;
    }

    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. These capabilities may be static or may depend on configuration
     * information supplied at construction time. May be null if there are
     * no useful capabilities registered.
     */
    @Override
    public Model getReasonerCapabilities() {
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
    @Override
    public void addDescription(Model configSpec, Resource base) {
        // No configuration
    }

    /**
     * Register an RDF predicate as one whose presence in a goal should force
     * the goal to be tabled.
     */
    public synchronized void tablePredicate(Node predicate) {
        ruleStore.tablePredicate(predicate);
    }


    /**
     * Determine whether the given property is recognized and treated specially
     * by this reasoner. This is a convenience packaging of a special case of getCapabilities.
     * @param property the property which we want to ask the reasoner about, given as a Node since
     * this is part of the SPI rather than API
     * @return true if the given property is handled specially by the reasoner.
     */
    @Override
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
    @Override
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        return new LPBackwardRuleReasoner(this, tbox);
    }

    /**
     * Precompute the implications of a schema Model. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    @Override
    public Reasoner bindSchema(Model tbox) throws ReasonerException {
        return new LPBackwardRuleReasoner(this, tbox.getGraph());
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
    @Override
    public InfGraph bind(Graph data) throws ReasonerException {
        LPBackwardRuleInfGraph graph = new LPBackwardRuleInfGraph(this, ruleStore, data, schemaGraph);
        graph.setDerivationLogging(recordDerivations);
        return graph;
    }

    /**
     * Return the this of Rules used by this reasoner
     * @return a List of Rule objects
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Switch on/off derivation logging.
     * If set to true then the InfGraph created from the bind operation will start
     * life with recording of derivations switched on. This is currently only of relevance
     * to rule-based reasoners.
     * <p>
     * Default - false.
     */
    @Override
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
     * Set a configuration paramter for the reasoner. In the case of the this
     * reasoner there are no configuration parameters and this method is simply
     * here to meet the interfaces specification
     *
     * @param parameter the property identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     */
    @Override
    public void setParameter(Property parameter, Object value) {
        throw new IllegalParameterException(parameter.toString());
    }

    /**
     * Return the Jena Graph Capabilties that the inference graphs generated
     * by this reasoner are expected to conform to.
     */
    @Deprecated
    @Override
    public Capabilities getGraphCapabilities() {
        return BaseInfGraph.reasonerInfCapabilities;
    }
}
