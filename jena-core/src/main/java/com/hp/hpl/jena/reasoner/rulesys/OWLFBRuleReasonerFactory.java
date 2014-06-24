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
                reasoner.setTraceOn( doTrace );
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
