/******************************************************************
 * File:        OWLExptRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  10-Jul-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: OWLExptRuleReasoner.java,v 1.6 2004-12-07 09:56:31 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl.oldCode;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.reasoner.rulesys.impl.OWLRuleTranslationHook;
import com.hp.hpl.jena.shared.WrappedIOException;
import com.hp.hpl.jena.graph.*;

/**
 * A hybrid forward/backward implementation of the OWL closure rules - experimental variant.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2004-12-07 09:56:31 $
 */
public class OWLExptRuleReasoner extends FBRuleReasoner  {
    
    /** The location of the OWL rule definitions on the class path */
    protected static final String RULE_FILE = "etc/owl-fb.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /** The precomputed axiom closure and compiled rule set */
    protected static FBRuleInfGraph preload; 
    
    /** Flag, set to true to use the LP engine */
    private static final boolean USE_LP = true;
    
    protected static Log logger = LogFactory.getLog(OWLExptRuleReasoner.class);
    
    /**
     * Constructor
     */
    public OWLExptRuleReasoner(ReasonerFactory factory) {
        super(loadRules(), factory);
        
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    private OWLExptRuleReasoner(OWLExptRuleReasoner parent, InfGraph schemaGraph) {
        super(parent.rules, schemaGraph, parent.factory);
    }
    
    /**
     * Return the rule set, loading it in if necessary
     */
    public static List loadRules() {
        if (ruleSet == null) {
            try {
                ruleSet = Rule.parseRules(Util.loadResourceFile(RULE_FILE));
            } catch (WrappedIOException e) {
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
        if (schemaGraph != null) {
            throw new ReasonerException("Can only bind one schema at a time to an OWLRuleReasoner");
        }
        FBRuleInfGraph graph = makeInfGraph(rules, tbox, true);
        graph.addPreprocessingHook(new OWLRuleTranslationHook());
        graph.prepare();
        return new OWLExptRuleReasoner(this, graph);
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
        FBRuleInfGraph graph =  null;
        InfGraph schemaArg = schemaGraph == null ? getPreload() : (FBRuleInfGraph)schemaGraph; 
        List baseRules = ((FBRuleInfGraph)schemaArg).getRules();
        graph = makeInfGraph(baseRules, schemaArg, false);
        graph.addPreprocessingHook(new OWLRuleTranslationHook());
        graph.setDerivationLogging(recordDerivations);
        graph.setTraceOn(isTraceOn());
        graph.rebind(data);
                
        return graph;
    }
    
    /**
     * Get the single static precomputed rule closure.
     */
    public InfGraph getPreload() {
        synchronized (OWLExptRuleReasoner.class) {
            if (preload == null) {
                preload = makeInfGraph(rules, null, false);
                preload.prepare();
            }
            return preload;
        }
    }
    
    /**
     * Construct an FB rule infgraph variant. Allows switching between the normal
     * and LP implementations during development.
     */
    private FBRuleInfGraph makeInfGraph(List rules, Graph schema, boolean doPreload) {
        if (USE_LP) {
            if (doPreload) {
                return new FBRuleInfGraph(this, rules, getPreload(), schema);
            } else {
                return new FBRuleInfGraph(this, rules, schema);
            }
        } else {
            if (doPreload) {
                return new FBRuleInfGraph(this, rules, getPreload(), schema);
            } else {
                return new FBRuleInfGraph(this, rules, schema);
            }
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