/******************************************************************
 * File:        ReasonerVocabulary.java
 * Created by:  Dave Reynolds
 * Created on:  04-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ReasonerVocabulary.java,v 1.20 2005-04-08 08:12:56 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasoner;

/**
 * A collection of RDF terms used in driving or configuring some of the
 * builtin reasoners.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.20 $ on $Date: 2005-04-08 08:12:56 $
 */
public class ReasonerVocabulary {
    
    /** The namespace used for system level descriptive properties of any reasoner */
    public static String JenaReasonerNS = "http://jena.hpl.hp.com/2003/JenaReasoner#";
    
    /** The RDF class to which all Reasoners belong */
    public static Resource ReasonerClass = ResourceFactory.createResource(JenaReasonerNS + "ReasonerClass");
    
    /** Reasoner description property: name of the reasoner */
    public static Property nameP;
    
    /** Reasoner description property: text description of the reasoner */
    public static Property descriptionP;
    
    /** Reasoner description property: version of the reasoner */
    public static Property versionP;
    
    /** Reasoner description property: a schema property supported by the reasoner */
    public static Property supportsP;
    
    /** Reasoner description property: a configuration property supported by the reasoner */
    public static Property configurationP;

    /** The property that represents the direct/minimal version of the subClassOf relationship */
    public static Property directSubClassOf; 

    /** The property that represents the direct/minimal version of the subPropertyOf relationship */
    public static Property directSubPropertyOf; 

    /** The property that represents the direct/minimal version of the rdf:type relationship */
    public static Property directRDFType; 

    /** A faux property used in reasoner capabilty models to denote reasoners that infer that individuals have rdf:type owl:Thing (or daml:Thing) */
    public static Property individualAsThingP;
    
    /** Base URI used for configuration properties for rule reasoners */
    public static final String PropURI = "http://jena.hpl.hp.com/2003/RuleReasoner";

    /** Property used to configure the derivation logging behaviour of a reasoner.
     *  Set to "true" to enable logging of derivations. */
    public static Property PROPderivationLogging;

    /** Property used to configure the tracing behaviour of a reasoner.
     *  Set to "true" to enable internal trace message to be sent to Logger.info . */
    public static Property PROPtraceOn;

    /** Property used to set the mode of a generic rule reasoner.
     *  Valid values are the strings "forward", "backward" or "hybrid" */
    public static Property PROPruleMode;
    
    /** Property used to attach a file a rules to a generic rule reasoner.
     *  Value should a URI giving the rule set to use. */
    public static Property PROPruleSet;
    
    /** Property used to switch on/off OWL schema translation on a generic rule reasoner.
     *  Value should be "true" to enable OWL translation */
    public static Property PROPenableOWLTranslation;
    
    /** Property used to switch on/off use of the dedicated subclass/subproperty
     *  caching in a generic rule reasoner. Set to "true" to enable caching. */
    public static Property PROPenableTGCCaching;
    
    /** Property used to switch on/off scanning of data for container membership
     * properties in RDFS preprocessing. */
    public static Property PROPenableCMPScan;
    
    /** Property used to switch to different RDFS processing levles. The
     * legal levels are "default", "simple", and "full". */
    public static Property PROPsetRDFSLevel;
    
    /** Constant for PROPsetRDFSLevel - default behaviour */
    public static String RDFS_DEFAULT = RDFSRuleReasoner.DEFAULT_RULES;
    
    /** Constant for PROPsetRDFSLevel - fullest implementation supported. */
    public static String RDFS_FULL = RDFSRuleReasoner.FULL_RULES;
    
    /** Constant for PROPsetRDFSLevel - simplified, higher performance rules. */
    public static String RDFS_SIMPLE = RDFSRuleReasoner.SIMPLE_RULES;    
    
    /** Property used to switch on/off filtering of functors from returned results
     *  in the generic rule engine. Default is filtering on. */
    public static Property PROPenableFunctorFiltering;

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
           
//  --------------------------------------------------------------------
//  Initializers

    static {
        try {
            nameP = ResourceFactory.createProperty(JenaReasonerNS, "name");
            descriptionP = ResourceFactory.createProperty(JenaReasonerNS, "description");
            versionP = ResourceFactory.createProperty(JenaReasonerNS, "version");
            supportsP = ResourceFactory.createProperty(JenaReasonerNS, "supports");
            configurationP = ResourceFactory.createProperty(JenaReasonerNS, "configurationProperty");
            directSubClassOf = ResourceFactory.createProperty(ReasonerRegistry.makeDirect(RDFS.subClassOf.getNode()).getURI());
            directSubPropertyOf = ResourceFactory.createProperty(ReasonerRegistry.makeDirect(RDFS.subPropertyOf.getNode()).getURI());
            directRDFType = ResourceFactory.createProperty(ReasonerRegistry.makeDirect(RDF.type.getNode()).getURI());
            individualAsThingP = ResourceFactory.createProperty(JenaReasonerNS, "individualAsThing");
            PROPderivationLogging  = ResourceFactory.createProperty(PropURI+"#", "derivationLogging");
            PROPtraceOn = ResourceFactory.createProperty(PropURI+"#", "traceOn");
            PROPruleMode = ResourceFactory.createProperty(PropURI+"#", "ruleMode");
            PROPruleSet = ResourceFactory.createProperty(PropURI+"#", "ruleSet");
            PROPenableOWLTranslation = ResourceFactory.createProperty(PropURI+"#", "enableOWLTranslation");
            PROPenableTGCCaching = ResourceFactory.createProperty(PropURI+"#", "enableTGCCaching");
            PROPenableCMPScan = ResourceFactory.createProperty(PropURI+"#", "enableCMPScan");
            PROPsetRDFSLevel = ResourceFactory.createProperty(PropURI+"#", "setRDFSLevel");
            PROPenableFunctorFiltering= ResourceFactory.createProperty(PropURI+"#", "enableFunctorFiltering");
        } catch (Exception e) {
            System.err.println("Initialization error: " + e);
            e.printStackTrace(System.err);
        }
    }
}


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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