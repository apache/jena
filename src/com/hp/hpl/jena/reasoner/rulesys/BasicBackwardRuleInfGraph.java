/******************************************************************
 * File:        BasicBackwardRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  29-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BasicBackwardRuleInfGraph.java,v 1.1 2003-04-29 16:52:03 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.*;
import org.apache.log4j.Logger;
/**
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-04-29 16:52:03 $
 */
public class BasicBackwardRuleInfGraph extends BaseInfGraph {

    //=======================================================================
    // variables
        
    /** Set for rules being used */
    protected List rules;
    
    /** Merge of base data, schema and any additional axiomatic information */
    protected Finder assertions;
        
    /** Table of derivation records, maps from triple to RuleDerivation */
    protected OneToManyMap derivations;
    
    /** performance stats - number of rules passing initial trigger */
    int nRulesTriggered = 0;
    
    /** performance stats - number of rules fired */
    long nRulesFired = 0;
    
    /** Flag which, if true, enables tracing of rule actions to logger.info */
    boolean traceOn = false;
    
    /** threshold on the numbers of rule firings allowed in a single operation */
    long nRulesThreshold = BasicForwardRuleInfGraph.DEFAULT_RULES_THRESHOLD;

    /** log4j logger */
    protected static Logger logger = Logger.getLogger(BasicBackwardRuleInfGraph.class);
    
    //=======================================================================
    // Core methods    

    /**
     * Constructor. Creates a new inference graph using the rules and schema
     * information defined by the parent reasoner.
     * 
     * @param reasoner the reasoner configuration to be used, includes a definition
     * of the rule set and any additional schema information not present in the data graph.
     * @param data the instance data to be processed
     */
    public BasicBackwardRuleInfGraph(BasicBackwardRuleReasoner reasoner, Graph data) {
        super(data, reasoner);
        this.rules = reasoner.getRules();
        if (reasoner.schemaGraph == null) {
            assertions = fdata;
        } else {
            assertions = FinderUtil.cascade(fdata, new FGraph(reasoner.schemaGraph));
        }
    }

    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        // TODO - implement!
        return null;
    }
   
    /** 
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     */
    public ExtendedIterator find(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), null);
    }

    /**
     * Basic pattern lookup interface.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        return findWithContinuation(pattern, null);
    }
    
    
//=======================================================================
// support for proof traces

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
        if (recordDerivations) {
            derivations = new OneToManyMap();
        } else {
            derivations = null;
        }
    }
    
    /**
     * Return the derivation of at triple.
     * The derivation is a List of DerivationRecords
     */
    public Iterator getDerivation(Triple t) {
        return derivations.getAll(t);
    }
    
    /**
     * Change the threshold on the number of rule firings 
     * allowed during a single operation.
     * @param threshold the new cutoff on the number rules firings per external op
     */
    public void setRuleThreshold(long threshold) {
        nRulesThreshold = threshold;
    }
    
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Logger at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        traceOn = state;
    }
    
//=======================================================================
// Rule engine


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

