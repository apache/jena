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

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.RDFSCMPPreprocessHook;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

/**
 * A full implemention of RDFS reasoning using a hybrid rule system, together
 * with optimized subclass/subproperty closure using the transitive graph caches.
 * Implements the container membership property rules using an optional
 * data scanning hook. Implements datatype range validation.
 */
public class RDFSRuleReasoner extends GenericRuleReasoner {
    
    /** Constant: used to indicate default RDFS processing level */
    public static final String DEFAULT_RULES = "default";
    
    /** Constant: used to indicate full RDFS processing level */
    public static final String FULL_RULES = "full";
    
    /** Constant: used to indicate minimal RDFS processing level */
    public static final String SIMPLE_RULES = "simple";
    
    /** The location of the default RDFS rule definitions on the class path */
    protected static final String RULE_FILE = "etc/rdfs-fb-tgc-noresource.rules";
    
    /** The location of the full RDFS rule definitions on the class path */
    protected static final String FULL_RULE_FILE = "etc/rdfs-fb-tgc.rules";
    
    /** The location of the simple RDFS rule definitions on the class path */
    protected static final String SIMPLE_RULE_FILE = "etc/rdfs-fb-tgc-simple.rules";
    
    /** The cached rule sets, indexed by processing level */
    protected static Map<String, List<Rule>> ruleSets = new HashMap<>();
    
    /** The rule file names, indexed by processing level */
    protected static Map<String, String> ruleFiles;
    
    /** The (stateless) preprocessor for container membership properties */
    protected static RulePreprocessHook cmpProcessor = new RDFSCMPPreprocessHook();
    
    static {
        ruleFiles = new HashMap<>();
        ruleFiles.put(DEFAULT_RULES, RULE_FILE);
        ruleFiles.put(FULL_RULES, FULL_RULE_FILE);
        ruleFiles.put(SIMPLE_RULES, SIMPLE_RULE_FILE);
    }
    
    /**
     * Constructor
     */
    public RDFSRuleReasoner( ReasonerFactory parent ) {
        super(loadRulesLevel(DEFAULT_RULES), parent);
        setMode(HYBRID);
        setTransitiveClosureCaching(true);
        //addPreprocessingHook(new RDFSCMPPreprocessHook());
    }
    
    /**
     * Constructor
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     * @param configuration RDF information to configure the rule set and mode, can be null
     */
    public RDFSRuleReasoner(ReasonerFactory factory, Resource configuration) {
        this(factory);
        if (configuration != null) {
            StmtIterator i = configuration.listProperties();
            while (i.hasNext()) {
                Statement st = i.nextStatement();
                doSetParameter(st.getPredicate(), st.getObject().toString());
            }
        }
    }
   
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    protected RDFSRuleReasoner(FBRuleInfGraph schemaGraph, ReasonerFactory factory) {
        super(schemaGraph.getRules(), factory);
        this.schemaGraph = schemaGraph;
    }
    
    /**
     * Internal version of setParameter that does not directly raise an
     * exception on parameters it does not reconize.
     * @return false if the parameter was not recognized
     */
    @Override
    protected boolean doSetParameter(Property parameter, Object value) {
        if (parameter.equals(ReasonerVocabulary.PROPenableCMPScan)) {
            boolean scanProperties = Util.convertBooleanPredicateArg(parameter, value);
            if (scanProperties) {
                addPreprocessingHook(cmpProcessor);
            } else {
                removePreprocessingHook(cmpProcessor);
            }
            return true;
        } else if (parameter.equals(ReasonerVocabulary.PROPsetRDFSLevel)) {
            String level = ((String)value).toLowerCase();
            setRules(loadRulesLevel(level));
            if (level.equals(FULL_RULES)) {
                addPreprocessingHook(cmpProcessor);
            } else {
                removePreprocessingHook(cmpProcessor);
            }
            return true;
        } else {
            return super.doSetParameter(parameter, value);
        }
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
        Graph schemaArg = schemaGraph == null ? getPreload() : schemaGraph;
        InfGraph graph = null; 
        List<Rule> ruleSet = ((FBRuleInfGraph)schemaArg).getRules();
        FBRuleInfGraph fbgraph = new RDFSRuleInfGraph(this, ruleSet, schemaArg);
        graph = fbgraph; 
        if (enableTGCCaching) fbgraph.setUseTGCCache();
        fbgraph.setTraceOn(traceOn);
        if (preprocessorHooks!= null) {
            for (RulePreprocessHook rulePreprocessHook : preprocessorHooks)
            {
                fbgraph.addPreprocessingHook(rulePreprocessHook);
            }
        }
        graph.setDerivationLogging(recordDerivations);
        graph.rebind(data);
        return graph;
    }
    
    /**
     * Precompute the implications of a schema graph. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    @Override
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        if (schemaGraph != null) {
            throw new ReasonerException("Can only bind one schema at a time to an RDFSRuleReasoner");
        }
        FBRuleInfGraph graph = new FBRuleInfGraph(this, rules, getPreload(), tbox);
        if (enableTGCCaching) (graph).setUseTGCCache();
        graph.prepare();
        RDFSRuleReasoner grr = new RDFSRuleReasoner(graph, factory);
        grr.setDerivationLogging(recordDerivations);
        grr.setTraceOn(traceOn);
        grr.setTransitiveClosureCaching(enableTGCCaching);
        grr.setFunctorFiltering(filterFunctors);
        if (preprocessorHooks != null) {
            for (RulePreprocessHook rulePreprocessHook : preprocessorHooks)
            {
                grr.addPreprocessingHook(rulePreprocessHook);
            }
        }
        return grr;
    }
    
    /**
     * Return the RDFS rule set, loading it in if necessary.
     * @param level a string defining the processing level required
     */
    public static List<Rule> loadRulesLevel(String level) {
        List<Rule> ruleSet = ruleSets.get(level);
        if (ruleSet == null) {
            String file = ruleFiles.get(level);
            if (file == null) {
                throw new ReasonerException("Illegal RDFS conformance level: " + level);
            }
            ruleSet = loadRules( file );
            ruleSets.put(level, ruleSet);
        }
        return ruleSet;
    }

    /**
     * Return the Jena Graph Capabilties that the inference graphs generated
     * by this reasoner are expected to conform to.
     */
    @Override
    public Capabilities getGraphCapabilities() {
        if (capabilities == null) {
            capabilities = new BaseInfGraph.InfFindSafeCapabilities();
        }
        return capabilities;
    }
        
}
