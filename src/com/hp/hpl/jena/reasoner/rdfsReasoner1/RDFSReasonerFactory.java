/******************************************************************
 * File:        RDFSReasonerFactory.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jan-03
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RDFSReasonerFactory.java,v 1.15 2004-12-07 09:56:19 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.*;

/**
 * Factory class for creating blank instances of the RDFS reasoner.
 *
 * @deprecated Obsoleted at jena2p4, replaced by 
 * {@link com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory}.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.15 $ on $Date: 2004-12-07 09:56:19 $
 */
public class RDFSReasonerFactory implements ReasonerFactory {
    
    /** Single global instance of this factory */
    private static ReasonerFactory theInstance = new RDFSReasonerFactory();
    
    /** Static URI for this reasoner type */
    public static final String URI = "http://jena.hpl.hp.com/2003/RDFSReasoner1";
    
    /** Cache of the capabilities description */
    protected Model capabilities;
    
    /** Property used to configure the scan behaviour of the reasoner.
     *  Set to "true" to enable scanning of triples looking for rdf:_1 assertions. */
    public static final Property scanProperties = new PropertyImpl(URI+"#", "scanProperties");
    
    /**
     * Return the single global instance of this factory
     */
    public static ReasonerFactory theInstance() {
        return theInstance;
    }
    
    /**
     * Constructor method that builds an instance of the associated Reasoner
     * @param configuration a set of arbitrary configuration information to be 
     * passed the reasoner, encoded as RDF properties of a base configuration resource,
     * can be null in no custom configuration is required.
     */
    public Reasoner create(Resource configuration) {
        return new RDFSReasoner(configuration);
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
            base.addProperty(ReasonerVocabulary.nameP, "RDFS Reasoner 1")
                .addProperty(ReasonerVocabulary.descriptionP, "Complete RDFS implementation supporting metalevel statements.\n"
                                            + "Eager caching of schema information, back chaining for most entailments\n"
                                            + "Can separate tbox and abox data if desired to reuse tbox caching or mix them.")
                .addProperty(ReasonerVocabulary.configurationP, scanProperties)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subClassOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subPropertyOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.member)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.range)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.domain)
                .addProperty(ReasonerVocabulary.supportsP, TransitiveReasoner.directSubClassOf)
                .addProperty(ReasonerVocabulary.supportsP, TransitiveReasoner.directSubPropertyOf)
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
    
    /**
     * Temporary testing hack
     */
    public static void main(String[] args) {
        Resource rdfsDescr = ReasonerRegistry.theRegistry().getDescription(URI);
        System.out.println("Reasoner: " + rdfsDescr);
        for (StmtIterator i = rdfsDescr.listProperties(); i.hasNext(); ) {
            Statement s = i.nextStatement();
            System.out.println(s.getPredicate().getLocalName() + " = " + s.getObject());
        }
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

