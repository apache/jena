/******************************************************************
 * File:        OWLBRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: OWLFBRuleReasoner.java,v 1.13 2004-03-19 15:43:16 chris-dollin Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.util.*;
import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.OWLRuleTranslationHook;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.graph.*;

/**
 * A hybrid forward/backward implementation of the OWL closure rules.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.13 $ on $Date: 2004-03-19 15:43:16 $
 */
public class OWLFBRuleReasoner extends FBRuleReasoner {
    
    /** The location of the OWL rule definitions on the class path */
    protected static final String RULE_FILE = "etc/owl-fb.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /** The precomputed axiom closure and compiled rule set */
    protected static FBRuleInfGraph staticPreload; 
    
    protected static Log logger = LogFactory.getLog(OWLFBRuleReasoner.class);
    
    /**
     * Constructor
     */
    public OWLFBRuleReasoner(ReasonerFactory factory) {
        super(loadRules(), factory);
        
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    private OWLFBRuleReasoner(OWLFBRuleReasoner parent, InfGraph schemaGraph) {
        super(parent.rules, schemaGraph, parent.factory);
    }
    
    /**
     * Return the rule set, loading it in if necessary
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
    
    
    /**
     * Precompute the implications of a schema graph. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        checkArgGraph(tbox);
        if (schemaGraph != null) {
            throw new ReasonerException("Can only bind one schema at a time to an OWLRuleReasoner");
        }
        FBRuleInfGraph graph = new FBRuleInfGraph(this, rules, getPreload(), tbox);
        graph.addPreprocessingHook(new OWLRuleTranslationHook());
        graph.prepare();
        return new OWLFBRuleReasoner(this, graph);
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
        checkArgGraph(data);
        FBRuleInfGraph graph =  null;
        InfGraph schemaArg = schemaGraph == null ? getPreload() : (FBRuleInfGraph)schemaGraph; 
        List baseRules = ((FBRuleInfGraph)schemaArg).getRules();
        graph = new FBRuleInfGraph(this, baseRules, schemaArg);
        graph.addPreprocessingHook(new OWLRuleTranslationHook());
        graph.setDerivationLogging(recordDerivations);
        graph.setTraceOn(traceOn);
        graph.rebind(data);
                
        return graph;
    }
    
    /**
     * Get the single static precomputed rule closure.
     */
    public InfGraph getPreload() {
        synchronized (OWLFBRuleReasoner.class) {
            if (staticPreload == null) {
                boolean prior = JenaParameters.enableFilteringOfHiddenInfNodes;
                try {
                    JenaParameters.enableFilteringOfHiddenInfNodes = true;
                    staticPreload = new FBRuleInfGraph(this, rules, null);
                    staticPreload.prepare();
                } finally {
                    JenaParameters.enableFilteringOfHiddenInfNodes = prior;
                }
            }
            return staticPreload;
        }
    }
    
    /**
     * Check an argument graph to make sure it is not an OWL rule graph
     * already and if so log a warning message.
     */
    private void checkArgGraph(Graph g) {
        if (JenaParameters.enableOWLRuleOverOWLRuleWarnings) {
            if (g instanceof InfGraph) {
                if (((InfGraph)g).getReasoner() instanceof OWLFBRuleReasoner) {
                    logger.warn("Creating OWL rule reasoner working over another OWL rule reasoner");
                }
            }
        }
    }
}


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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