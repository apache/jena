/******************************************************************
 * File:        DAMLMicroReasonerFactory.java
 * Created by:  Dave Reynolds
 * Created on:  23-Jul-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: DAMLMicroReasonerFactory.java,v 1.6 2004-12-07 09:56:28 andy_seaborne Exp $
 *****************************************************************/
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
 * @version $Revision: 1.6 $ on $Date: 2004-12-07 09:56:28 $
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
    public Reasoner create(Resource configuration) {
        return new DAMLMicroReasoner(this);
    }
   
    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. This method is normally called by the ReasonerRegistry which caches
     * the resulting information so dynamically creating here is not really an overhead.
     */
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
    public String getURI() {
        return URI;
    }

}


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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