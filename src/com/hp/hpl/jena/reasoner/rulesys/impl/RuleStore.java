/******************************************************************
 * File:        RuleStore.java
 * Created by:  Dave Reynolds
 * Created on:  04-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RuleStore.java,v 1.13 2003-08-27 13:11:15 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.OneToManyMap;

/**
 * Indexes a collection of rule. The currently implementation is
 * a crude first version aimed at supporting the backchaining
 * interpreter. It only indexes on predicate. 
 * <p>
 * The rules are normalized to only contain a single head element
 * by duplicating any multi headed rules.
 * </p> 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.13 $ on $Date: 2003-08-27 13:11:15 $
 */
public class RuleStore {

    /** The set of rules indexed by head predicate */
    protected OneToManyMap goalMap = new OneToManyMap();
    
    /** The list of all rules in the store */
    protected List allRules;
    
    /** Index of the rules, used to block multiple entries */
    protected Set ruleIndex = new HashSet();
    
    /**
     * Constructor. Create an empty rule store.
     */
    public RuleStore() {
        allRules = new ArrayList();
    };
    
    /**
     * Constructor. Stores and indexes a list of rules.
     */
    public RuleStore(List rules) {
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            addRule( (Rule)i.next() );
        }
        allRules = rules;
    }
    
    /**
     * Add all the rules and  from an existing rulestore into this one.
     */
    public void addAll(RuleStore store) {
        for (Iterator i = store.getAllRules().iterator(); i.hasNext(); ) {
            addRule( (Rule)i.next() );
        }
    }
    
    /**
     * Add a single rule to the store. 
     */
    public void addRule(Rule rule) {
        addRemoveRule(rule, true);
    }
    
    /**
     * Remove a single rule from the store
     */
    public void deleteRule(Rule rule) {
        addRemoveRule(rule, false);
    }
    
    /**
     * Add a single/remove a compound rule from the store.
     * @param rule the rule, may have multiple heads
     * @param isAdd true to add, false to remove 
     */
    private void addRemoveRule(Rule rule, boolean isAdd) {
        if (rule.headLength() != 1) {
            for (int j = 0; j < rule.headLength(); j++) {
                Rule newRule = new Rule(rule.getName(), 
                                    new ClauseEntry[] {rule.getHeadElement(j)}, 
                                    rule.getBody() );
                newRule.setNumVars(rule.getNumVars());
                doAddRemoveRule(newRule, isAdd);
            }
                
        } else {
            doAddRemoveRule(rule, isAdd);
        }
    }
    
    /**
     * Add/remove a single rule from the store. 
     * @param rule the rule, single headed only
     * @param isAdd true to add, false to remove 
     */
    protected void doAddRemoveRule(Rule rule, boolean isAdd) {
        if (isAdd && ruleIndex.contains(rule)) return;
        if (isAdd) {
            ruleIndex.add(rule);
            if (allRules != null) allRules.add(rule);
        } else {
            ruleIndex.remove(rule);
            if (allRules != null) allRules.remove(rule);
        }
        Object headClause = rule.getHeadElement(0);
        if (headClause instanceof TriplePattern) {
            TriplePattern headpattern = (TriplePattern)headClause;
            Node predicate = headpattern.getPredicate();
            if (predicate.isVariable()) {
                if (isAdd) {
                    goalMap.put(Node.ANY, rule);
                } else {
                    goalMap.remove(Node.ANY, rule);
                }
            } else {
                if (isAdd) {
                    goalMap.put(predicate, rule);
                } else {
                    goalMap.remove(predicate, rule);
                }
            }
        }
    }
    
    /**
     * Return a list of rules that match the given goal pattern
     * @param goal the goal being matched
     */
    public List rulesFor(TriplePattern goal) {
        List rules = new ArrayList();
        if (goal.getPredicate().isVariable()) {
            checkAll(goalMap.values().iterator(), goal, rules);
        } else {
            checkAll(goalMap.getAll(goal.getPredicate()), goal, rules);
            checkAll(goalMap.getAll(Node.ANY), goal, rules);
        }
        return rules;
    }
    
    /**
     * Return an ordered list of all registered rules.
     */
    public List getAllRules() {
        return allRules;
    }
    
    /**
     * Delete all the rules.
     */
    public void deleteAllRules() {
        allRules.clear();
        goalMap.clear();
        ruleIndex.clear();
    }
    
    /**
     * Helper method to extract all matching clauses from an
     * iterator over rules
     */
    private void checkAll(Iterator candidates, TriplePattern goal, List matchingRules) {
        while (candidates.hasNext()) {
            Rule r = (Rule)candidates.next();
            if ( ((TriplePattern)r.getHeadElement(0)).compatibleWith(goal) ) {
                matchingRules.add(r);
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