/******************************************************************
 * File:        OWLBRuleReasonerFactory.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: OWLBRuleReasonerFactory.java,v 1.1 2003-05-13 08:18:12 der Exp $
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
 * <li><b>ruleThreshold</b> - which limits the number of rules that can be fired on a single 
 * data processing stage to the given number (useful to limit infinite runaways). </li>
 * </ul>
 *
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-05-13 08:18:12 $
 */
public class OWLBRuleReasonerFactory implements ReasonerFactory {
    
    /** Single global instance of this factory */
    private static ReasonerFactory theInstance = new OWLBRuleReasonerFactory();
    
    /** Static URI for this reasoner type */
    public static final String URI = "http://www.hpl.hp.com/semweb/2003/OWLBRuleReasoner";
    
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
    public Reasoner create(Model configuration) {
        OWLBRuleReasoner reasoner = new OWLBRuleReasoner();
        if (configuration != null) {
            Boolean doLog = Util.checkBinaryPredicate(URI, BasicForwardRuleReasoner.PROPderivationLogging, configuration);
            if (doLog != null) {
                reasoner.setDerivationLogging(doLog.booleanValue());
            }
            Boolean doTrace = Util.checkBinaryPredicate(URI, BasicForwardRuleReasoner.PROPtraceOn, configuration);
            if (doTrace != null) {
                reasoner.setTraceOn(doTrace.booleanValue());
            }
            Integer threshold = Util.getIntegerPredicate(URI, BasicForwardRuleReasoner.PROPrulesThreshold, configuration);
            if (threshold != null) {
                reasoner.setRulesThreshold(threshold.intValue());
            }
        }
        return reasoner;
    }
   
    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. This method is normally called by the ReasonerRegistry which caches
     * the resulting information so dynamically creating here is not really an overhead.
     */
    public Model getCapabilities() {
        Model capabilities = ModelFactory.createDefaultModel();
        Resource base = capabilities.createResource(getURI());
        base.addProperty(ReasonerRegistry.nameP, "OWL BRule Reasoner")
            .addProperty(ReasonerRegistry.descriptionP, "Experimental OWL reasoner.\n"
                                        + "Can separate tbox and abox data if desired to reuse tbox caching or mix them.")
            .addProperty(ReasonerRegistry.supportsP, RDFS.subClassOf)
            .addProperty(ReasonerRegistry.supportsP, RDFS.subPropertyOf)
            .addProperty(ReasonerRegistry.supportsP, RDFS.member)
            .addProperty(ReasonerRegistry.supportsP, RDFS.range)
            .addProperty(ReasonerRegistry.supportsP, RDFS.domain)
            // TODO - add OWL elements supported
            .addProperty(ReasonerRegistry.versionP, "0.1");
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