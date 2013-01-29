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

package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;

/**
 * The minimal interface to which all reasoners (or reasoner adaptors) conform. 
 * This only supports attaching the reasoner to a set of RDF graphs 
 * which represent the rules or ontologies and instance data. The actual
 * reasoner requests are made through the InfGraph which is generated once
 * the reasoner has been bound to a set of RDF data.
 */
public interface Reasoner {
    
    /**
     * This is most commonly used to attach an ontology (a set of tbox 
     * axioms in description logics jargon) to a reasoner. A certain amount
     * of precomputation may be done at this time (e.g. constructing the
     * class lattice). When the reasoner is later applied some instance data
     * these cached precomputations may be reused.
     * <p>In fact this call may be more general than the above 
     * description suggests. Firstly, a reasoner that supports arbitrary rules
     * rather than ontologies may use the same method to bind the reasoner
     * to the specific rule set (encoded in RDF). Secondly, even in the ontology
     * case a given reasoner may not require a strict separation of tbox and
     * abox - it may allow instance data in the tbox and terminology axioms in
     * the abox. </p>
     * <p>A reasoner is free to simply note this set of RDF and merge with any
     * future RDF rather than do processing at this time. </p>
     * @param tbox the ontology axioms or rule set encoded in RDF
     * @return a reasoner instace which can be used to process a data graph
     * @throws ReasonerException if the reasoner cannot be
     * bound to a rule set in this way, for example if the underlying engine
     * can only accept a single rule set in this way and one rule set has
     * already been bound in of if the ruleset is illformed.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException;
    
    /**
     * This is most commonly used to attach an ontology (a set of tbox 
     * axioms in description logics jargon) to a reasoner. A certain amount
     * of precomputation may be done at this time (e.g. constructing the
     * class lattice). When the reasoner is later applied some instance data
     * these cached precomputations may be reused.
     * <p>In fact this call may be more general than the above 
     * description suggests. Firstly, a reasoner that supports arbitrary rules
     * rather than ontologies may use the same method to bind the reasoner
     * to the specific rule set (encoded in RDF). Secondly, even in the ontology
     * case a given reasoner may not require a strict separation of tbox and
     * abox - it may allow instance data in the tbox and terminology axioms in
     * the abox. </p>
     * <p>A reasoner is free to simply note this set of RDF and merge with any
     * future RDF rather than do processing at this time. </p>
     * @param tbox the ontology axioms or rule set encoded in RDF
     * @return a reasoner instace which can be used to process a data graph
     * @throws ReasonerException if the reasoner cannot be
     * bound to a rule set in this way, for example if the underlying engine
     * can only accept a single rule set in this way and one rule set has
     * already been bound in of if the ruleset is illformed.
     */
    public Reasoner bindSchema(Model tbox) throws ReasonerException;
    
    /**
     * Attach the reasoner to a set of RDF data to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    public InfGraph bind(Graph data) throws ReasonerException;

    
    /**
     * Switch on/off drivation logging.
     * If set to true then the InfGraph created from the bind operation will start
     * life with recording of derivations switched on. This is currently only of relevance
     * to rule-based reasoners.
     * <p>
     * Default - false.
     */
    public void setDerivationLogging(boolean logOn);
    
    /**
     * Set a configuration parameter for the reasoner. Parameters can identified
     * by URI and can also be set when the Reasoner instance is created by specifying a
     * configuration in RDF.
     * 
     * @param parameterUri the property identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     */
    public void setParameter(Property parameterUri, Object value);

    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. These capabilities may be static or may depend on configuration
     * information supplied at construction time. May be null if there are
     * no useful capabilities registered.
     */
    public Model getReasonerCapabilities();
    
    /**
     * Add a configuration description for this reasoner into a partial
     * configuration specification model.
     * @param configSpec a Model into which the configuration information should be placed
     * @param base the Resource to which the configuration parameters should be added.
     */
    public void addDescription(Model configSpec, Resource base);

    /**
     * Determine whether the given property is recognized and treated specially
     * by this reasoner. This is a convenience packaging of a special case of getCapabilities.
     * @param property the property which we want to ask the reasoner about
     * @return true if the given property is handled specially by the reasoner.
     */
    public boolean supportsProperty(Property property);

    /**
     * Return the Jena Graph Capabilties that the inference graphs generated
     * by this reasoner are expected to conform to.
     */
    public Capabilities getGraphCapabilities();
}
