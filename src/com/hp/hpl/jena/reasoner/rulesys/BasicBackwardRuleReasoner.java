/******************************************************************
 * File:        BasicBackwardRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  29-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BasicBackwardRuleReasoner.java,v 1.1 2003-05-05 15:16:00 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

/**
 * Reasoner implementation which augments or transforms an RDF graph
 * according to a set of rules. The rules are processed using a
 * tabled backchaining interpreter which is implemented by the
 * relvant InfGraph class. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-05-05 15:16:00 $
 */
public class BasicBackwardRuleReasoner implements Reasoner {

    /** The rules to be used by this instance of the forward engine */
    protected List rules;
    
    /** A cache set of schema data used in partial binding chains */
    protected Graph schemaGraph;
    
    /** Flag to set whether the inference class should record derivations */
    protected boolean recordDerivations = false;
    
    /** threshold on the numbers of rule firings allowed in a single operation */
    protected long nRulesThreshold = BasicForwardRuleInfGraph.DEFAULT_RULES_THRESHOLD;

    /**
     * Constructor
     * @param rules a list of Rule instances which defines the ruleset to process
     */
    public BasicBackwardRuleReasoner(List rules) {
        this.rules = rules;
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    private BasicBackwardRuleReasoner(List rules, Graph schemaGraph) {
        this.rules = rules;
        this.schemaGraph = schemaGraph;
    }
    
    /**
     * Precompute the implications of a schema graph.
     * The practicality benefit of this has not yet been fully checked out.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        return new BasicBackwardRuleReasoner(rules, tbox);
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
        BasicBackwardRuleInfGraph graph = new BasicBackwardRuleInfGraph(this, data);
        graph.setDerivationLogging(recordDerivations);
        graph.setRuleThreshold(nRulesThreshold);
        return graph;
    }
    
    /**
     * Return the this of Rules used by this reasoner
     * @return a List of Rule objects
     */
    public List getRules() {
        return rules;
    } 
   
    /**
     * Switch on/off drivation logging.
     * If set to true then the InfGraph created from the bind operation will start
     * life with recording of derivations switched on. This is currently only of relevance
     * to rule-based reasoners.
     * <p>
     * Default - false.
     */
    public void setDerivationLogging(boolean logOn) {
        recordDerivations = logOn;
    }
    
    /**
     * Set the threshold on the numbers of rule firings allowed in a single operation.
     */
    public void setRulesThreshold(long threshold) {
        nRulesThreshold = threshold;
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

