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
import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * Reasoner factory for the OWL mini configuration. Key limitations over
 * the normal OWL configuration are:
 * <UL>
 * <li>omits the someValuesFrom => bNode entailments</li>
 * <li>avoids any guard clauses which would break the find() contract</li>
 * <li>omits inheritance of range implications for XSD datatype ranges</li>
 * </UL>
 */
public class OWLMiniReasonerFactory implements ReasonerFactory {
    
    /** Single global instance of this factory */
    private static ReasonerFactory theInstance = new OWLMiniReasonerFactory();
    
    /** Static URI for this reasoner type */
    public static final String URI = "http://jena.hpl.hp.com/2003/OWLMiniFBRuleReasoner";
    
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
        OWLMiniReasoner reasoner = new OWLMiniReasoner(this);
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
            base.addProperty(ReasonerVocabulary.nameP, "OWL Mini Reasoner")
                .addProperty(ReasonerVocabulary.descriptionP, "Experimental mini OWL reasoner.\n"
                                            + "Can separate tbox and abox data if desired to reuse tbox caching or mix them.")
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subClassOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subPropertyOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.member)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.range)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.domain)
                .addProperty(ReasonerVocabulary.supportsP, TransitiveReasoner.directSubClassOf.toString() ) // TODO -- typing
                .addProperty(ReasonerVocabulary.supportsP, TransitiveReasoner.directSubPropertyOf.toString() ) // TODO -- typing
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
                .addProperty(ReasonerVocabulary.supportsP, OWL.maxCardinality )        // Only partial
                .addProperty(ReasonerVocabulary.supportsP, OWL.cardinality )           // Only partial
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
