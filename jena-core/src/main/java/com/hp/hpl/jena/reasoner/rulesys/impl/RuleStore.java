/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 */
public class RuleStore {

    /** The set of rules indexed by head predicate */
    protected OneToManyMap<Node, Rule> goalMap = new OneToManyMap<>();
    
    /** The list of all rules in the store */
    protected List<Rule> allRules;
    
    /** Index of the rules, used to block multiple entries */
    protected Set<Rule> ruleIndex = new HashSet<>();
    
    /**
     * Constructor. Create an empty rule store.
     */
    public RuleStore() {
        allRules = new ArrayList<>();
    }
    
    /**
     * Constructor. Stores and indexes a list of rules.
     */
    public RuleStore(List<Rule> rules) {
        for ( Rule rule : rules )
        {
            addRule( rule );
        }
        allRules = rules;
    }
    
    /**
     * Add all the rules and  from an existing rulestore into this one.
     */
    public void addAll(RuleStore store) {
        for ( Rule rule : store.getAllRules() )
        {
            addRule( rule );
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
    public List<Rule> rulesFor(TriplePattern goal) {
        List<Rule> rules = new ArrayList<>();
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
    public List<Rule> getAllRules() {
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
    private void checkAll(Iterator<Rule> candidates, TriplePattern goal, List<Rule> matchingRules) {
        while (candidates.hasNext()) {
            Rule r = candidates.next();
            if ( ((TriplePattern)r.getHeadElement(0)).compatibleWith(goal) ) {
                matchingRules.add(r);
            }
        }
    }
 
}
