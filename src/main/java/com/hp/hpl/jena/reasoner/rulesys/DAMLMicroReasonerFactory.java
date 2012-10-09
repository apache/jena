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
 * We do not support DAML inference. This factory creates a reasoner which is
 * a slightly extended variant
 * of the RDFS reasoner to support some interesting subsets of DAML
 * that correspond roughly to what was there in Jena1. We hope.
 *
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:38 $
 * @deprecated This DAML reasoner will be removed from future versions of Jena because it is obsolete.
 */
public class DAMLMicroReasonerFactory implements ReasonerFactory {

    /** Single global instance of this factory */
    private static ReasonerFactory theInstance = new DAMLMicroReasonerFactory();

    /** Static URI for this reasoner type */
    public static final String URI = "http://jena.hpl.hp.com/2003/DAMLMicroReasonerFactory";

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
     * @param configuration a set of arbitrary configuration information for the reasoner, this will be
     * ignored in this case because the micro reasoner is not configurable
     */
    @Override
    public Reasoner create(Resource configuration) {
        return new DAMLMicroReasoner(this);
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
            base.addProperty(ReasonerVocabulary.nameP, "DAML micro Rule Reasoner")
                .addProperty(ReasonerVocabulary.descriptionP, "RDFS rule set with small extensions to support DAML")
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subClassOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subPropertyOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.member)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.range)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.domain)
                .addProperty(ReasonerVocabulary.supportsP, DAML_OIL.subClassOf)
                .addProperty(ReasonerVocabulary.supportsP, DAML_OIL.subPropertyOf)
                .addProperty(ReasonerVocabulary.supportsP, DAML_OIL.range)
                .addProperty(ReasonerVocabulary.supportsP, DAML_OIL.domain)
                .addProperty(ReasonerVocabulary.supportsP, ReasonerVocabulary.individualAsThingP )
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
