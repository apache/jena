/******************************************************************
 * File:        OWLBRuleReasonerFactory.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: OWLFBRuleReasonerFactory.java,v 1.2 2010-04-26 19:33:14 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * Factory class for creating blank instances of the OWL Reasoner.
 * <p>
 * The reasoner can be configured using three properties (set as
 * properties of the base reasonder URI in a configuration model). These are:
 * <ul>
 * <li><b>derivationLogging</b> - if set to true this causes all derivations to
 * be recorded in an internal data structure for replay through the {@link com.hp.hpl.jena.reasoner.InfGraph#getDerivation getDerivation}
 * method. </li>
 * <li><b>traceOn</b> - if set to true this causes all rule firings and deduced triples to be
 * written out to the Logger at INFO level.</li>
 * </ul>
 *
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2010-04-26 19:33:14 $
 */
public class OWLFBRuleReasonerFactory implements ReasonerFactory {
    
    /** Single global instance of this factory */
    private static ReasonerFactory theInstance = new OWLFBRuleReasonerFactory();
    
    /** Static URI for this reasoner type */
    public static final String URI = "http://jena.hpl.hp.com/2003/OWLFBRuleReasoner";
    
    /** Cache of the capabilities description */
    protected Model capabilities;
    
    /**
     * Return the single global instance of this factory
     */
    public static ReasonerFactory theInstance() {
        return theInstance;
    }
    
    /**
     * Constructor method that builds an instance of the associated Reasoner
     * @param configuration a set of arbitrary configuration information to be 
     * passed the reasoner encoded within an RDF graph
     */
    @Override
    public Reasoner create(Resource configuration) {
        OWLFBRuleReasoner reasoner = new OWLFBRuleReasoner(this);
        if (configuration != null) {
            Boolean doLog = Util.checkBinaryPredicate(ReasonerVocabulary.PROPderivationLogging, configuration);
            if (doLog != null) {
                reasoner.setDerivationLogging(doLog.booleanValue());
            }
            Boolean doTrace = Util.checkBinaryPredicate(ReasonerVocabulary.PROPtraceOn, configuration);
            if (doTrace != null) {
                reasoner.setTraceOn(doTrace.booleanValue());
            }
        }
        return reasoner;
    }
   
    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. This method is normally called by the ReasonerRegistry which caches
     * the resulting information so dynamically creating here is not really an overhead.
     */
    @Override
    public Model getCapabilities() {
        if (capabilities == null) {
            capabilities = ModelFactory.createDefaultModel();
            Resource base = capabilities.createResource(getURI());
            base.addProperty(ReasonerVocabulary.nameP, "OWL BRule Reasoner")
                .addProperty(ReasonerVocabulary.descriptionP, "Experimental OWL reasoner.\n"
                                            + "Can separate tbox and abox data if desired to reuse tbox caching or mix them.")
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subClassOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subPropertyOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.member)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.range)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.domain)
                // TODO - add OWL elements supported
                .addProperty(ReasonerVocabulary.supportsP, ReasonerVocabulary.individualAsThingP )
                .addProperty(ReasonerVocabulary.supportsP, OWL.ObjectProperty )
                .addProperty(ReasonerVocabulary.supportsP, OWL.DatatypeProperty)
                .addProperty(ReasonerVocabulary.supportsP, OWL.FunctionalProperty )
                .addProperty(ReasonerVocabulary.supportsP, OWL.SymmetricProperty )
                .addProperty(ReasonerVocabulary.supportsP, OWL.TransitiveProperty )
                .addProperty(ReasonerVocabulary.supportsP, OWL.InverseFunctionalProperty )

                .addProperty(ReasonerVocabulary.supportsP, OWL.hasValue )
                .addProperty(ReasonerVocabulary.supportsP, OWL.intersectionOf )
                .addProperty(ReasonerVocabulary.supportsP, OWL.unionOf )        // Only partial
                .addProperty(ReasonerVocabulary.supportsP, OWL.minCardinality )        // Only partial
                .addProperty(ReasonerVocabulary.supportsP, OWL.maxCardinality )        // Only partial
                .addProperty(ReasonerVocabulary.supportsP, OWL.cardinality )           // Only partial
                .addProperty(ReasonerVocabulary.supportsP, OWL.someValuesFrom)         // Only partial
                .addProperty(ReasonerVocabulary.supportsP, OWL.allValuesFrom )         // Only partial
                .addProperty(ReasonerVocabulary.supportsP, OWL.sameAs )
                .addProperty(ReasonerVocabulary.supportsP, OWL.differentFrom )
                .addProperty(ReasonerVocabulary.supportsP, OWL.disjointWith )
                
                .addProperty(ReasonerVocabulary.versionP, "0.1");
        }
        return capabilities;
    }
    
    /**
     * Return the URI labelling this type of reasoner
     */
    @Override
    public String getURI() {
        return URI;
    }
    
}

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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