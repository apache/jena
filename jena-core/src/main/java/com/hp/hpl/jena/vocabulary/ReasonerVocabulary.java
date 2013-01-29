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

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasoner;

/**
 * A collection of RDF terms used in driving or configuring some of the
 * builtin reasoners.
 */
public class ReasonerVocabulary {
    
    /** The namespace used for system level descriptive properties of any reasoner */
    public static final String JenaReasonerNS = "http://jena.hpl.hp.com/2003/JenaReasoner#";
    
    /** The RDF class to which all Reasoners belong */
    public static final Resource ReasonerClass = ResourceFactory.createResource(JenaReasonerNS + "ReasonerClass");
    
    /** Reasoner description property: name of the reasoner */
    public static final Property nameP = jenaReasonerProperty( "name" );
    
    /** Reasoner description property: text description of the reasoner */
    public static final Property descriptionP = jenaReasonerProperty( "description" );
    
    /** Reasoner description property: version of the reasoner */
    public static final Property versionP = jenaReasonerProperty( "version" );
    
    /** Reasoner description property: a schema property supported by the reasoner */
    public static final Property supportsP = jenaReasonerProperty( "supports" );
    
    /** Reasoner description property: a configuration property supported by the reasoner */
    public static final Property configurationP = jenaReasonerProperty( "configurationProperty" );

    /** A faux property used in reasoner capabilty models to denote reasoners that infer that individuals have rdf:type owl:Thing (or daml:Thing) */
    public static final Property individualAsThingP = jenaReasonerProperty( "individualAsThing" ); 

    /** The property that represents the direct/minimal version of the subClassOf relationship */
    public static final Property directSubClassOf = makeDirect( RDFS.subClassOf ); 

    /** The property that represents the direct/minimal version of the subPropertyOf relationship */
    public static final Property directSubPropertyOf = makeDirect( RDFS.subPropertyOf ); 

    /** The property that represents the direct/minimal version of the rdf:type relationship */
    public static final Property directRDFType = makeDirect( RDF.type ); 
    
    /** Base URI used for configuration properties for rule reasoners */
    public static final String PropURI = "http://jena.hpl.hp.com/2003/RuleReasoner";

    /** Property used to configure the derivation logging behaviour of a reasoner.
     *  Set to "true" to enable logging of derivations. */
    public static final Property PROPderivationLogging = ruleReasonerProperty( "derivationLogging" );

    /** Property used to configure the tracing behaviour of a reasoner.
     *  Set to "true" to enable internal trace message to be sent to Logger.info . */
    public static final Property PROPtraceOn = ruleReasonerProperty( "traceOn" );

    /** Property used to set the mode of a generic rule reasoner.
     *  Valid values are the strings "forward", "backward" or "hybrid" */
    public static final Property PROPruleMode = ruleReasonerProperty( "ruleMode" );
    
    /** Property used to attach a file a rules to a generic rule reasoner.
     *  Value should a URI giving the rule set to use. */
    public static final Property PROPruleSet = ruleReasonerProperty( "ruleSet" );
    
    /** Property used to switch on/off OWL schema translation on a generic rule reasoner.
     *  Value should be "true" to enable OWL translation */
    public static final Property PROPenableOWLTranslation = ruleReasonerProperty( "enableOWLTranslation" );
    
    /** Property used to switch on/off use of the dedicated subclass/subproperty
     *  caching in a generic rule reasoner. Set to "true" to enable caching. */
    public static final Property PROPenableTGCCaching = ruleReasonerProperty( "enableTGCCaching" );
    
    /** Property used to switch on/off scanning of data for container membership
     * properties in RDFS preprocessing. */
    public static final Property PROPenableCMPScan = ruleReasonerProperty( "enableCMPScan" );
    
    /** Property used to switch to different RDFS processing levles. The
     * legal levels are "default", "simple", and "full". */
    public static final Property PROPsetRDFSLevel = ruleReasonerProperty( "setRDFSLevel" );
    
    /** Property used to switch on/off filtering of functors from returned results
     *  in the generic rule engine. Default is filtering on. */
    public static final Property PROPenableFunctorFiltering = ruleReasonerProperty( "enableFunctorFiltering" );
    
    /** Constant for PROPsetRDFSLevel - default behaviour */
    public static final String RDFS_DEFAULT = RDFSRuleReasoner.DEFAULT_RULES;
    
    /** Constant for PROPsetRDFSLevel - fullest implementation supported. */
    public static final String RDFS_FULL = RDFSRuleReasoner.FULL_RULES;
    
    /** Constant for PROPsetRDFSLevel - simplified, higher performance rules. */
    public static final String RDFS_SIMPLE = RDFSRuleReasoner.SIMPLE_RULES;    

    /** A namespace used for Rubrik specific properties */
    public static final String RBNamespace = "urn:x-hp-jena:rubrik/";

    /** Property used to switch on validation in owl ruleset */
    public static final Property RB_VALIDATION = ResourceFactory.createProperty(RBNamespace, "validation");

    /** Property used for validation reports in owl ruleset */
    public static final Property RB_VALIDATION_REPORT = ResourceFactory.createProperty(RBNamespace, "violation");
    
                
    /** Property to denote the URL of an external reasoner. Default is http://localhost:8081 */
    public static final Property EXT_REASONER_URL = ResourceFactory.createProperty( JenaReasonerNS, "extReasonerURL" );
    
    /** Property to denote the ontology language (OWL, DAML, RDFS) an external reasoner will process. 
     *  Values are URI's, see {@link com.hp.hpl.jena.ontology.ProfileRegistry}. Default is OWL. */
    public static final Property EXT_REASONER_ONT_LANG = ResourceFactory.createProperty( JenaReasonerNS, "extReasonerOntologyLang" );
    
    /** Property to denote the axioms file that the reasoner will use for background knowledge. 
     *  Values are URL's. Default is no axioms. */
    public static final Property EXT_REASONER_AXIOMS = ResourceFactory.createProperty( JenaReasonerNS, "extReasonerAxioms" );

    /**
        Property of a GRR config with object a node with rule set properties.
    */
    public static final Property ruleSet = jenaReasonerProperty( "ruleSet" );

    /**
        Property of a GRR config with object a resource who's URI is the URL
        of a Jena rules text.
    */
    public static final Property ruleSetURL = jenaReasonerProperty( "ruleSetURL" );

    /**
        Property of a GRR rule-set config that specifies a rule as a string in the
        Jena rules language.
    */
    public static final Property hasRule = jenaReasonerProperty( "hasRule" );

    /**
        Property of a reasoner that specifies the URL of a schema to load.
    */
    public static final Property schemaURL = jenaReasonerProperty( "schemaURL" );
    
    protected static final Property jenaReasonerProperty( String localName )
        { return ResourceFactory.createProperty( JenaReasonerNS, localName ); }

    protected static final Property ruleReasonerProperty( String localName )
        { return ResourceFactory.createProperty( PropURI + "#" + localName ); }

    /**
        compact call to ReasonerRegistry.makeDirect
    */
    private static final Property makeDirect( Property type )
        { return ResourceFactory.createProperty( ReasonerRegistry.makeDirect( type.asNode().getURI() ) ); }

    //  --------------------------------------------------------------------
// Method versions of key namespaces which are more initializer friendly


    /** Return namespace used for Rubric specific properties */
    public static final String getRBNamespace() {
        return RBNamespace;
    }
    
    /** Return namespace used for system level descriptive properties of any reasoner */
    public static final String getJenaReasonerNS() {
        return JenaReasonerNS;
    }
}
