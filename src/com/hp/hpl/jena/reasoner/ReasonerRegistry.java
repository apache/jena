/******************************************************************
 * File:        ReasonerRegistry.java
 * Created by:  Dave Reynolds
 * Created on:  16-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: ReasonerRegistry.java,v 1.14 2003-06-12 14:16:46 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.OWLFBRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RDFSFBRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;

import java.util.*;

/**
 * A global registry of known reasoner modules. Modules are identified
 * by a URI and can have associated capability and descriptive 
 * information, in RDF. Currently the schema/ontology for such 
 * descriptions is mostly open. However, we do ensure that each reasoner
 * URI at least has an associated rdf:type statement to indicate that it
 * is a reasoner.
 * <p>
 * It is up to each reasoner or some associated configuration system
 * to register it in this registry.  </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.14 $ on $Date: 2003-06-12 14:16:46 $
 */
public class ReasonerRegistry {

    /** Single glogal instance of the registry */
    protected static ReasonerRegistry theRegistry;
    
    /** Map from reasoner URI to the associated factory */
    protected Map reasonerFactories = new HashMap();
    
    /** Union of the all reasoner capability descriptions */
    protected Model allDescriptions;

    /**
     * Constructor is hidden - go via theRegistry
     */
    private ReasonerRegistry() {
        allDescriptions = new ModelMem();
        // Preload the known Jena reasoers
        register(TransitiveReasonerFactory.theInstance());
        register(RDFSReasonerFactory.theInstance());
        register(RDFSFBRuleReasonerFactory.theInstance());
        register(OWLFBRuleReasonerFactory.theInstance());
        register(GenericRuleReasonerFactory.theInstance());
        
    }
    
    /**
     * Return the single global instance of the registry
     */
    public static ReasonerRegistry theRegistry() {
        if (theRegistry == null) {
            theRegistry = new ReasonerRegistry();
        }
        return theRegistry;
    }
    
    /**
     * Register a Reasoner.
     * @param factory an factory that can be used to create instances of the reasoner
     */
    public void register(ReasonerFactory factory) {
        reasonerFactories.put(factory.getURI(), factory);
        Model description = factory.getCapabilities();
        if (description != null) {
            allDescriptions.add(description);
        }
        allDescriptions.createResource(factory.getURI())
                        .addProperty(RDF.type, ReasonerVocabulary.ReasonerClass);
    }
    
    /**
     * Register a Reasoner - simple case with no RDF description.
     * @param factory an factory that can be used to create instances of the reasoner
     * @param reasonerUri the URI used to label the reasoner, expressed as a
     * simple string
     */
    public void register(String reasonerUri, ReasonerFactory factory) {
        reasonerFactories.put(reasonerUri, factory);
        allDescriptions.createResource(reasonerUri)
                       .addProperty(RDF.type, ReasonerVocabulary.ReasonerClass);
    }
    
    /**
     * Return a composite set of RDF capability descriptions for all registered reasoners.
     * Listing all Resources of type ReasonerClass in the returned model
     * would enumerate all registered reasoners.
     */
    public Model getAllDescriptions() {
        return allDescriptions;
    }
    
    /**
     * Return information on a given Reasoner.
     * @param the URI of the reasoner
     * @param a Resource representing the reasoner whose properties (as obtainable
     * through listProperties etc) give a capability description of the reasoner; 
     * returns null if no such reasoner is registered.
     */
    public Resource getDescription(String uri) {
        Resource reasonerURI = allDescriptions.getResource(uri);
        if (allDescriptions.contains(reasonerURI, RDF.type, ReasonerVocabulary.ReasonerClass)) {
            return reasonerURI;
        } else {
            return null;
        }
    }
    
    /**
     * Return the factory for the given reasoner.
     * @param the URI of the reasoner
     * @return the ReasonerFactory instance for this reasoner
     */
    public ReasonerFactory getFactory(String uri) {
        return (ReasonerFactory)reasonerFactories.get(uri);
    }
    
    /**
     * Create and return a new instance of the reasoner identified by
     * the given uri.
     * <p>TODO: It might be useful to all pass the descriptive information to
     * the reasoner to allow multiple configurations of the same reasoner class to
     * be registered as if they were different reasoners. </p>
     * @param uri the uri of the reasoner to be created, expressed as a simple string
     * @param configuration an optional set of configuration information encoded in RDF this 
     * parameter can be null if no configuration information is required.
     * @return a reaoner instance
     * @throws ReasonerException if there is not such reasoner or if there is
     * some problem during instantiation
     */
    public Reasoner create(String uri, Model configuration) throws ReasonerException {
        ReasonerFactory factory = getFactory(uri);
        if (factory != null) {
            return factory.create(configuration);
        } else {
            throw new ReasonerException("Attempted to instantiate an unknown reasoner: " + uri);
        }
    }
    
    /**
     * Return a property Node which represents the direct version of a
     * transitively closed property.
     * 
     * <p>Not clear what the right thing to do here is. Should not just invent
     * a new local name in the same namespace because that might be a controlled
     * namespace like RDF or RDFS. Can't even just extend the namespace slightly
     * because that would be violating the web principles of namespace ownership.
     * On the other hand, this solution results in staggeringly clumsy names.</p>
     * 
     * @param property the transitive property
     */
    public static Node makeDirect(Node node) {
        String directName = "urn:x-hp-direct-predicate:" + node.getURI().replace(':','_') ;
        return Node.createURI(directName);
    }
    
    /** Prebuilt standard configuration for the default RDFS reasoner. */
    protected static Reasoner theRDFSReasoner = null;
    
    /**
     * Return a prebuilt standard configuration for the default RDFS reasoner
     */
     public static Reasoner getRDFSReasoner() {
         if (theRDFSReasoner == null) theRDFSReasoner = RDFSReasonerFactory.theInstance().create(null);
         return theRDFSReasoner;
     }
     
    /** Prebuilt standard configuration for the default subclass/subproperty transitive closure reasoner. */
    protected static Reasoner theTRANSITIVEReasoner;
    
    /**
     * Return a prebuilt standard configuration for the default subclass/subproperty transitive closure reasoner.
     */
    public static Reasoner getTransitiveReasoner() {
        if (theTRANSITIVEReasoner == null) theTRANSITIVEReasoner = TransitiveReasonerFactory.theInstance().create(null);
        return theTRANSITIVEReasoner;
    }
    
    /** Prebuilt standard configuration OWL reasoner */
    protected static Reasoner theOWLReasoner;
    
    /**
     * Prebuilt standard configuration for the default OWL reasoner. This configuration is
     * hybrid forward/backward reasoner.
     */
    public static Reasoner getOWLReasoner() {
        if (theOWLReasoner == null) theOWLReasoner = OWLFBRuleReasonerFactory.theInstance().create(null);
        return theOWLReasoner;
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

