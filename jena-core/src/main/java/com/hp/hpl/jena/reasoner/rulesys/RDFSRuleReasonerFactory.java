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
 * Factory class for creating blank instances of the hybrid rule RDFS reasoner 
 * with TGC support.
 */
public class RDFSRuleReasonerFactory implements ReasonerFactory {
    
    /** Single global instance of this factory */
    private static ReasonerFactory theInstance = new RDFSRuleReasonerFactory();
    
    /** Static URI for this reasoner type */
    public static final String URI = "http://jena.hpl.hp.com/2003/RDFSExptRuleReasoner";
    
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
     * @param configuration a set of arbitrary configuration information for the reasoner
     */
    @Override
    public Reasoner create(Resource configuration) {
        return new RDFSRuleReasoner (this, configuration );
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
            base.addProperty(ReasonerVocabulary.nameP, "RDFS FB-TGC Rule Reasoner")
                .addProperty(ReasonerVocabulary.descriptionP, "Complete RDFS implementation supporting metalevel statements.\n"
                                            + "Can separate tbox and abox data if desired to reuse tbox caching or mix them.")
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subClassOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subPropertyOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.member)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.range)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.domain)
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
