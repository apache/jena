/******************************************************************
 * File:        RDFSReasonerFactory.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RDFSReasonerFactory.java,v 1.6 2003-04-15 21:24:09 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.*;

/**
 * Factory class for creating blank instances of the RDFS reasoner.
 *
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-04-15 21:24:09 $
 */
public class RDFSReasonerFactory implements ReasonerFactory {
    
    /** Single global instance of this factory */
    private static ReasonerFactory theInstance = new RDFSReasonerFactory();
    
    /** Static URI for this reasoner type */
    public static final String URI = "http://www.hpl.hp.com/semweb/2003/RDFSReasoner1";
    
    /** Property used to configure the scan behaviour of the reasoner.
     *  Set to "true" to enable scanning of triples looking for rdf:_1 assertions. */
    public static final Property scanProperties = new PropertyImpl(URI+"#", "scanProperties");
    
    /** Property used to configure the checking of ranges of datatype properties
     *  Set to "true" to enable eager range checking on add. */
    public static final Property checkDTRange = new PropertyImpl(URI+"#", "checkDTRange");
    
    /**
     * Return the single global instance of this factory
     */
    public static ReasonerFactory theInstance() {
        return theInstance;
    }
    
    /**
     * Constructor method that builds an instance of the associated Reasoner
     * @param configuration a set of arbitrary configuration information to be 
     * passed the reasoner encoded within an RDF graph.
     */
    public Reasoner create(Model configuration) {
        return new RDFSReasoner(configuration);
    }
   
    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. This method is normally called by the ReasonerRegistry which caches
     * the resulting information so dynamically creating here is not really an overhead.
     */
    public Model getCapabilities() {
        Model capabilities = new ModelMem();
        Resource base = capabilities.createResource(getURI());
        base.addProperty(ReasonerRegistry.nameP, "RDFS Reasoner 1")
            .addProperty(ReasonerRegistry.descriptionP, "Complete RDFS implementation supporting metalevel statements.\n"
                                        + "Eager caching of schema information, back chaining for most entailments\n"
                                        + "Can separate tbox and abox data if desired to reuse tbox caching or mix them.")
            .addProperty(ReasonerRegistry.configurationP, scanProperties)
            .addProperty(ReasonerRegistry.supportsP, RDFS.subClassOf)
            .addProperty(ReasonerRegistry.supportsP, RDFS.subPropertyOf)
            .addProperty(ReasonerRegistry.supportsP, RDFS.member)
            .addProperty(ReasonerRegistry.supportsP, RDFS.range)
            .addProperty(ReasonerRegistry.supportsP, RDFS.domain)
            .addProperty(ReasonerRegistry.supportsP, TransitiveReasoner.directSubClassOf)
            .addProperty(ReasonerRegistry.supportsP, TransitiveReasoner.directSubPropertyOf)
            .addProperty(ReasonerRegistry.versionP, "0.1");
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
    (c) Copyright Hewlett-Packard Company 2003
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

