/******************************************************************
 * File:        FRuleEngineI.java
 * Created by:  Dave Reynolds
 * Created on:  11-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: FRuleEngineI.java,v 1.6 2005-02-21 12:17:40 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.Finder;
//import java.util.List;

/**
 * Rule engines implement the internals of forward rule inference
 * graphs and the forward part of hybrid graphs. This interface
 * abstracts the interface onto such engines to allow a graph to
 * switch between direct and RETE style implementations.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2005-02-21 12:17:40 $
 */
public interface FRuleEngineI {
    
    /**
     * Process all available data. This should be called once a deductions graph
     * has be prepared and loaded with any precomputed deductions. It will process
     * the rule axioms and all relevant existing exiting data entries.
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     * @param inserts the set of triples to be processed, normally this is the
     * raw data graph but may include additional deductions made by preprocessing hooks
     */
    public void init(boolean ignoreBrules, Finder inserts);
    
    /**
     * Process all available data. This version expects that all the axioms 
     * have already be preprocessed and the rules have been compiled
     * @param inserts the set of triples to be processed, normally this is the
     * raw data graph but may include additional deductions made by preprocessing hooks
     */
    public void fastInit(Finder inserts);
    
    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public void add(Triple t);
    
    /**
     * Remove one triple to the data graph.
     * @return true if the effects could be correctly propagated or
     * false if not (in which case the entire engine should be restarted).
     */
    public boolean delete(Triple t);
    
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized
     */
    public long getNRulesFired();
    
    /**
     * Return true if the internal engine state means that tracing is worthwhile.
     * It will return false during the axiom bootstrap phase.
     */
    public boolean shouldTrace();

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations);
    
    /**
     * Access the precomputed internal rule form. Used when precomputing the
     * internal axiom closures.
     */
    public Object getRuleStore();
    
    /**
     * Set the internal rule from from a precomputed state.
     */
    public void setRuleStore(Object ruleStore);
    
    /**
     * Compile a list of rules into the internal rule store representation.
     * @param rules the list of Rule objects
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     * @return an object that can be installed into the engine using setRuleStore.
     */
//    public void compile(List rules, boolean ignoreBrules);

}



/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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