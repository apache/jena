/******************************************************************
 * File:        RDFSRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  16-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RDFSRuleReasoner.java,v 1.7 2003-06-22 16:10:31 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.RDFSCMPPreprocessHook;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

/**
 * A full implemention of RDFS reasoning using a hybrid rule system, together
 * with optimized subclass/subproperty closure using the transitive graph caches.
 * Implements the container membership property rules using an optional
 * data scanning hook. Implements datatype range validation.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2003-06-22 16:10:31 $
 */
public class RDFSRuleReasoner extends GenericRuleReasoner {
    
    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/rdfs-fb-tgc.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /** The (stateless) preprocessor for container membership properties */
    protected static RulePreprocessHook cmpProcessor = new RDFSCMPPreprocessHook();
    
    /**
     * Constructor
     */
    public RDFSRuleReasoner(ReasonerFactory parent) {
        super(loadRules(), parent);
        setMode(HYBRID);
        setTransitiveClosureCaching(true);
        //addPreprocessingHook(new RDFSCMPPreprocessHook());
    }
    
    /**
     * Constructor
     * @param factory the parent reasoner factory which is consulted to answer capability questions
     * @param configuration RDF information to configure the rule set and mode, can be null
     */
    public RDFSRuleReasoner(ReasonerFactory factory, Resource configuration) {
        this(factory);
        if (configuration != null) {
            StmtIterator i = configuration.listProperties();
            while (i.hasNext()) {
                Statement st = i.nextStatement();
                doSetParameter(st.getPredicate().getURI(), st.getObject().toString());
            }
        }
    }
   
    /**
     * Internal version of setParameter that does not directly raise an
     * exception on parameters it does not reconize.
     * @return false if the parameter was not recognized
     */
    protected boolean doSetParameter(String parameterUri, Object value) {
        if (parameterUri.equals(ReasonerVocabulary.PROPenableCMPScan.getURI())) {
            boolean scanProperties = Util.convertBooleanPredicateArg(parameterUri, value);
            if (scanProperties) {
                addPreprocessingHook(cmpProcessor);
            } else {
                removePreprocessingHook(cmpProcessor);
            }
            return true;
        } else {
            return super.doSetParameter(parameterUri, value);
        }
    }
    
    /**
     * Attach the reasoner to a set of RDF data to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * 
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    public InfGraph bind(Graph data) throws ReasonerException {
        Graph schemaArg = schemaGraph == null ? getPreload() : schemaGraph;
        InfGraph graph = null; 
        List ruleSet = ((FBRuleInfGraph)schemaArg).getRules();
        FBRuleInfGraph fbgraph = new RDFSRuleInfGraph(this, ruleSet, schemaArg);
        graph = fbgraph; 
        if (enableTGCCaching) fbgraph.setUseTGCCache();
        fbgraph.setTraceOn(traceOn);
        if (preprocessorHooks!= null) {
            for (Iterator i = preprocessorHooks.iterator(); i.hasNext(); ) {
                fbgraph.addPreprocessingHook((RulePreprocessHook)i.next());
            }
        }
        graph.setDerivationLogging(recordDerivations);
        graph.rebind(data);
        return graph;
    }
    
    /**
     * Return the RDFS rule set, loading it in if necessary
     */
    public static List loadRules() {
        if (ruleSet == null) {
            try {
                ruleSet = Rule.parseRules(Util.loadResourceFile(RULE_FILE));
            } catch (IOException e) {
                throw new ReasonerException("Can't load rules file: " + RULE_FILE, e);
            }
        }
        return ruleSet;
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