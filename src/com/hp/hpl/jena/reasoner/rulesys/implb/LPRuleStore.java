/******************************************************************
 * File:        LPRuleStore.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPRuleStore.java,v 1.2 2003-07-20 19:06:07 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.RuleStore;
import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 * Holds the set of backward rules used by an LPEngine. Is responsible
 * for compile the rules into internal byte codes before use.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-07-20 19:06:07 $
 */
public class LPRuleStore extends RuleStore {
    
    /** Flag to indicate whether the rules have been compiled into code objects */
    protected boolean isCompiled = false;
    
    /** A map from predicate to a list of RuleClauseCode objects for that predicate.
     *  Uses Node_RuleVariable.ANY for wildcard predicates.
     *  TODO; Check ordering.
     */ 
    protected Map predicateToCodeMap;
    
    /** The list of all RuleClauseCode objects, used to implement wildcard queries */
    protected ArrayList allRuleClauseCodes;
    
    /**
     * Return an ordered list of RuleClauseCode objects to implement the given 
     * predicate.
     * @param predicate the predicate node or Node_RuleVariable.ANY for wildcards.
     */
    public List codeFor(Node predicate) {
        if (!isCompiled) {
            compileAll();
        }
        if (predicate.isVariable()) {
            return allRuleClauseCodes;
        } else {
            return (List) predicateToCodeMap.get(predicate);
        }
    }
    
    /**
     * Return an ordered list of RuleClauseCode objects to implement the given 
     * query pattern. This may use indexing to narrow the rule set more that the predicate-only case. 
     * @param goal the triple pattern that makes up the query
     */
    public List codeFor(TriplePattern goal) {
        // TODO: add indexing
        // Is use of TriplePattern ok here - check it doesn't lead to store turn over
        return codeFor(goal.getPredicate());
    }
    
    /**
     * Compile all the rules in a table. initially just indexed on predicate but want to 
     * add better indexing for the particular cases of wildcard rules and type rules. 
     */
    protected void compileAll() {
        // TODO: add support for wildcard rules
        predicateToCodeMap = new HashMap();
        allRuleClauseCodes = new ArrayList();
        for (Iterator ri = getAllRules().iterator(); ri.hasNext(); ) {
            Rule r = (Rule)ri.next();
            ClauseEntry term = r.getHeadElement(0);
            if (term instanceof TriplePattern) {
                Node predicate = ((TriplePattern)term).getPredicate();
                if (predicate.isVariable()) {
                    throw new ReasonerException("Wildcard predicates not yet supported");
                } else {
                    RuleClauseCode code = new RuleClauseCode(r);
                    allRuleClauseCodes.add(code);
                    List predicateCode = (List)predicateToCodeMap.get(predicate);
                    if (predicateCode == null) {
                        predicateCode = new ArrayList();
                        predicateToCodeMap.put(predicate, predicateCode);
                    }
                    predicateCode.add(code);
                }
            }
        }
        predicateToCodeMap.put(Node_RuleVariable.ANY, allRuleClauseCodes);
        
        
        // Now compile all the clauses
        for (Iterator i = allRuleClauseCodes.iterator(); i.hasNext(); ) {
            RuleClauseCode code = (RuleClauseCode)i.next();
            code.compile(this);
        }
        isCompiled = true;
    }
    
    /**
     * Add/remove a single rule from the store. 
     * Overridden in order to reset the "isCompiled" flag.
     * 
     * @param rule the rule, single headed only
     * @param isAdd true to add, false to remove 
     */
    protected void doAddRemoveRule(Rule rule, boolean isAdd) {
        isCompiled = false;
        super.doAddRemoveRule(rule, isAdd);
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