/******************************************************************
 * File:        ReasonerVocabulary.java
 * Created by:  Dave Reynolds
 * Created on:  04-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ReasonerVocabulary.java,v 1.26 2008-01-21 15:23:36 chris-dollin Exp $
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
 * @version $Revision: 1.26 $ on $Date: 2008-01-21 15:23:36 $
 */
public class ReasonerVocabulary {
    
    /** The namespace used for system level descriptive properties of any reasoner */
    public static final String JenaReasonerNS = "http://jena.hpl.hp.com/2003/JenaReasoner#";
    
    /** The RDF class to which all Reasoners belong */
    public static final Resource ReasonerClass = ResourceFactory.createResource(JenaReasonerNS + "ReasonerClass");
    
    /** Reasoner description property: name of the reasoner */
    public static final Property nameP = property( "name" );
    
    /** Reasoner description property: text description of the reasoner */
    public static final Property descriptionP = property( "description" );
    
    /** Reasoner description property: version of the reasoner */
    public static final Property versionP = property( "version" );
    
    /** Reasoner description property: a schema property supported by the reasoner */
    public static final Property supportsP = property( "supports" );
    
    /** Reasoner description property: a configuration property supported by the reasoner */
    public static final Property configurationP = property( "configurationProperty" );

    /** A faux property used in reasoner capabilty models to denote reasoners that infer that individuals have rdf:type owl:Thing (or daml:Thing) */
    public static final Property individualAsThingP = property( "individualAsThing" ); 

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
    public static final Property PROPderivationLogging = PROP( "derivationLogging" );

    /** Property used to configure the tracing behaviour of a reasoner.
     *  Set to "true" to enable internal trace message to be sent to Logger.info . */
    public static final Property PROPtraceOn = PROP( "traceOn" );

    /** Property used to set the mode of a generic rule reasoner.
     *  Valid values are the strings "forward", "backward" or "hybrid" */
    public static final Property PROPruleMode = PROP( "ruleMode" );
    
    /** Property used to attach a file a rules to a generic rule reasoner.
     *  Value should a URI giving the rule set to use. */
    public static final Property PROPruleSet = PROP( "ruleSet" );
    
    /** Property used to switch on/off OWL schema translation on a generic rule reasoner.
     *  Value should be "true" to enable OWL translation */
    public static final Property PROPenableOWLTranslation = PROP( "enableOWLTranslation" );
    
    /** Property used to switch on/off use of the dedicated subclass/subproperty
     *  caching in a generic rule reasoner. Set to "true" to enable caching. */
    public static final Property PROPenableTGCCaching = PROP( "enableTGCCaching" );
    
    /** Property used to switch on/off scanning of data for container membership
     * properties in RDFS preprocessing. */
    public static final Property PROPenableCMPScan = PROP( "enableCMPScan" );
    
    /** Property used to switch to different RDFS processing levles. The
     * legal levels are "default", "simple", and "full". */
    public static final Property PROPsetRDFSLevel = PROP( "setRDFSLevel" );
    
    /** Property used to switch on/off filtering of functors from returned results
     *  in the generic rule engine. Default is filtering on. */
    public static final Property PROPenableFunctorFiltering = PROP( "enableFunctorFiltering" );
    
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

    public static final Property ruleSet = property( "ruleSet" );

    public static final Property ruleSetURL = property( "ruleSetURL" );

    public static final Property hasRule = property( "hasRule" );

    public static final Property schemaURL = property( "schemaURL" );
    
    protected static final Property property( String localName )
        { return ResourceFactory.createProperty( JenaReasonerNS, localName ); }

    private static final Property PROP( String localName )
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


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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