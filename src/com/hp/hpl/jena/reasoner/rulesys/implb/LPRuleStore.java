/******************************************************************
 * File:        LPRuleStore.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPRuleStore.java,v 1.12 2003-08-17 20:09:18 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

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
 * @version $Revision: 1.12 $ on $Date: 2003-08-17 20:09:18 $
 */
public class LPRuleStore extends RuleStore {
    
    /** Flag to indicate whether the rules have been compiled into code objects */
    protected boolean isCompiled = false;
    
    /** A map from predicate to a list of RuleClauseCode objects for that predicate.
     *  Uses Node_RuleVariable.WILD for wildcard predicates.
     */ 
    protected Map predicateToCodeMap;
    
    /** The list of all RuleClauseCode objects, used to implement wildcard queries */
    protected ArrayList allRuleClauseCodes;

    /** Two level index map - index on predicate then on object */
    protected Map indexPredicateToCodeMap;
        
    /** Set of predicates which should be tabled */
    protected HashSet tabledPredicates = new HashSet();
        
    /** Threshold for number of rule entries in a predicate bucket before second level indexing kicks in */
    private static final int INDEX_THRESHOLD = 20;
    
    /**
     * Construct a rule store containing the given rules.
     * @param rules the rules to initialize the store with.
     */
    public LPRuleStore(List rules) {
        super(rules);
    }
    
    /**
     * Construct an empty rule store
     */
    public LPRuleStore() {
        super();
    }
    
    /**
     * Add all the rules and tabling instructions from an existing rulestore into this one.
     */
    public void addAll(LPRuleStore store) {
        super.addAll(store);
        tabledPredicates.addAll(store.tabledPredicates);
    }
    
    /**
     * Register an RDF predicate as one whose presence in a goal should force
     * the goal to be tabled.
     */
    public synchronized void tablePredicate(Node predicate) {
        tabledPredicates.add(predicate);
    }    
    
    /**
     * Return an ordered list of RuleClauseCode objects to implement the given 
     * predicate.
     * @param predicate the predicate node or Node_RuleVariable.WILD for wildcards.
     */
    public List codeFor(Node predicate) {
        if (!isCompiled) {
            compileAll();
        }
        if (predicate.isVariable()) {
            return allRuleClauseCodes;
        } else {
            List codeList = (List) predicateToCodeMap.get(predicate);
            if (codeList == null) {
                // Uknown predicate, so only the wildcard rules apply
                codeList = (List) predicateToCodeMap.get(Node_RuleVariable.WILD);
            }
            return codeList;
        }
    }
    
    /**
     * Return an ordered list of RuleClauseCode objects to implement the given 
     * query pattern. This may use indexing to narrow the rule set more that the predicate-only case. 
     * @param goal the triple pattern that makes up the query
     */
    public List codeFor(TriplePattern goal) {
        List allRules = codeFor(goal.getPredicate());
        if (allRules == null) {
            return allRules;
        }
        Map indexedCodeTable = (Map) indexPredicateToCodeMap.get(goal.getPredicate());
        if (indexedCodeTable != null) {
            List indexedCode = (List) indexedCodeTable.get(goal.getObject());
            if (indexedCode != null) {
                return indexedCode;
            }
        }
        return allRules;
    }
    
    /**
     * Return true if the given predicate is indexed.
     */
    public boolean isIndexedPredicate(Node predicate) {
        return (indexPredicateToCodeMap.get(predicate) != null);
    }
    
    /**
     * Return true if the given goal is tabled, currently this is true if the
     * predicate is a tabled predicate or the predicate is a wildcard and some
     * tabled predictes exist.
     */
    public boolean isTabled(TriplePattern goal) {
        // This version forces tabling of all goals anyway
        return true;
        // Original used selective tabling, small perf disadvantage on early tests
//        return isTabled(goal.getPredicate());
    }
    
    /**
     * Return true if the given predicated is tabled, currently this is true if the
     * predicate is a tabled predicate or the predicate is a wildcard and some
     * tabled predictes exist.
     */
    public boolean isTabled(Node predicate) {
        if (predicate.isVariable() && !tabledPredicates.isEmpty()) {
            return true;
        } else {
            return tabledPredicates.contains(predicate);
        }
    }
    
    /**
     * Compile all the rules in a table. initially just indexed on predicate but want to 
     * add better indexing for the particular cases of wildcard rules and type rules. 
     */
    protected void compileAll() {
        isCompiled = true;
        
        predicateToCodeMap = new HashMap();
        allRuleClauseCodes = new ArrayList();
        indexPredicateToCodeMap = new HashMap();
        for (Iterator ri = getAllRules().iterator(); ri.hasNext(); ) {
            Rule r = (Rule)ri.next();
            ClauseEntry term = r.getHeadElement(0);
            if (term instanceof TriplePattern) {
                RuleClauseCode code = new RuleClauseCode(r);
                allRuleClauseCodes.add(code);
                Node predicate = ((TriplePattern)term).getPredicate();
                if (predicate.isVariable()) {
                    predicate = Node_RuleVariable.WILD;
                }
                List predicateCode = (List)predicateToCodeMap.get(predicate);
                if (predicateCode == null) {
                    predicateCode = new ArrayList();
                    predicateToCodeMap.put(predicate, predicateCode);
                }
                predicateCode.add(code);
                if (predicateCode.size() > INDEX_THRESHOLD) {
                    indexPredicateToCodeMap.put(predicate, new HashMap());
                }
            }
        }

        // Now add the wild card rules into the list for each non-wild predicate)
        List wildRules = (List) predicateToCodeMap.get(Node_RuleVariable.WILD);
        if (wildRules != null) {
            for (Iterator i = predicateToCodeMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry)i.next();
                Node predicate = (Node)entry.getKey();
                List predicateCode = (List)entry.getValue();
                if (predicate != Node_RuleVariable.WILD) {
                    predicateCode.addAll(wildRules);
                }
            }
        }
        indexPredicateToCodeMap.put(Node_RuleVariable.WILD, new HashMap());
                
        // Now built any required two level indices
        for (Iterator i = indexPredicateToCodeMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
            Node predicate = (Node) entry.getKey();
            HashMap predicateMap = (HashMap) entry.getValue();
            List wildRulesForPredicate = new ArrayList();
            List allRulesForPredicate =  predicate.isVariable() ? allRuleClauseCodes : (List) predicateToCodeMap.get(predicate);
            for (Iterator j = allRulesForPredicate.iterator(); j.hasNext(); ) {
                RuleClauseCode code = (RuleClauseCode)j.next();
                ClauseEntry head = code.getRule().getHeadElement(0);
                boolean indexed = false;
                if (head instanceof TriplePattern) {
                    Node objectPattern = ((TriplePattern)head).getObject();
                    if (!objectPattern.isVariable() && !Functor.isFunctor(objectPattern)) {
                        // Index against object
                        List indexedCode = (List) predicateMap.get(objectPattern);
                        if (indexedCode == null) {
                            indexedCode = new ArrayList();
                            predicateMap.put(objectPattern, indexedCode);
                        }
                        indexedCode.add(code);
                        indexed = true;
                    }
                }
                if (!indexed) {
                    wildRulesForPredicate.add(code);
                }
            }
            // Now fold the rules that apply to any index entry into all the indexed entries
            for (Iterator k = predicateMap.entrySet().iterator(); k.hasNext(); ) {
                Map.Entry ent = (Map.Entry)k.next();
                Node pred = (Node)ent.getKey();
                List predicateCode = (List)ent.getValue();
                predicateCode.addAll(wildRulesForPredicate);
            }
        }
        
        // Now compile all the clauses
        for (Iterator i = allRuleClauseCodes.iterator(); i.hasNext(); ) {
            RuleClauseCode code = (RuleClauseCode)i.next();
            code.compile(this);
        }
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