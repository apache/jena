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

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 * Holds the set of backward rules used by an LPEngine. Is responsible
 * for compile the rules into internal byte codes before use.
 */
public class LPRuleStore extends RuleStore {
    
    /** Flag to indicate whether the rules have been compiled into code objects */
    protected boolean isCompiled = false;
    
    /** A map from predicate to a list of RuleClauseCode objects for that predicate.
     *  Uses Node_RuleVariable.WILD for wildcard predicates.
     */ 
    protected Map<Node, List<RuleClauseCode>> predicateToCodeMap;
    
    /** The list of all RuleClauseCode objects, used to implement wildcard queries */
    protected ArrayList<RuleClauseCode> allRuleClauseCodes;

    /** Two level index map - index on predicate then on object */
    protected Map<Node, Map<Node, List<RuleClauseCode>>> indexPredicateToCodeMap;
        
    /** Set of predicates which should be tabled */
    protected HashSet<Node> tabledPredicates = new HashSet<>();
        
    /** Threshold for number of rule entries in a predicate bucket before second level indexing kicks in */
    private static final int INDEX_THRESHOLD = 20;
    
    /** True if all goals should be treated as tabled */
    protected boolean allTabled = false;
    
    /**
     * Construct a rule store containing the given rules.
     * @param rules the rules to initialize the store with.
     */
    public LPRuleStore(List<Rule> rules) {
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
        allTabled = tabledPredicates.contains(Node.ANY);
    }
    
    /**
     * Register an RDF predicate as one whose presence in a goal should force
     * the goal to be tabled.
     */
    public synchronized void tablePredicate(Node predicate) {
        tabledPredicates.add(predicate);
        if (predicate == Node.ANY) allTabled = true;
    }    
    
    /**
     * Return an ordered list of RuleClauseCode objects to implement the given 
     * predicate.
     * @param predicate the predicate node or Node_RuleVariable.WILD for wildcards.
     */
    public List<RuleClauseCode> codeFor(Node predicate) {
        if (!isCompiled) {
            compileAll();
        }
        if (predicate.isVariable()) {
            return allRuleClauseCodes;
        } else {
            List<RuleClauseCode> codeList = predicateToCodeMap.get(predicate);
            if (codeList == null) {
                // Uknown predicate, so only the wildcard rules apply
                codeList = predicateToCodeMap.get(Node_RuleVariable.WILD);
            }
            return codeList;
        }
    }
    
    /**
     * Return an ordered list of RuleClauseCode objects to implement the given 
     * query pattern. This may use indexing to narrow the rule set more that the predicate-only case. 
     * @param goal the triple pattern that makes up the query
     */
    public List<RuleClauseCode> codeFor(TriplePattern goal) {
        List<RuleClauseCode> allRules = codeFor(goal.getPredicate());
        if (allRules == null) {
            return allRules;
        }
        Map<Node, List<RuleClauseCode>> indexedCodeTable = indexPredicateToCodeMap.get(goal.getPredicate());
        if (indexedCodeTable != null) {
            List<RuleClauseCode> indexedCode = indexedCodeTable.get(goal.getObject());
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
        return isTabled(goal.getPredicate());
    }
    
    /**
     * Return true if the given predicated is tabled, currently this is true if the
     * predicate is a tabled predicate or the predicate is a wildcard and some
     * tabled predictes exist.
     */
    public boolean isTabled(Node predicate) {
        if (allTabled) return true;
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
        
        predicateToCodeMap = new HashMap<>();
        allRuleClauseCodes = new ArrayList<>();
        indexPredicateToCodeMap = new HashMap<>();
        for ( Rule r : getAllRules() )
        {
            ClauseEntry term = r.getHeadElement( 0 );
            if ( term instanceof TriplePattern )
            {
                RuleClauseCode code = new RuleClauseCode( r );
                allRuleClauseCodes.add( code );
                Node predicate = ( (TriplePattern) term ).getPredicate();
                if ( predicate.isVariable() )
                {
                    predicate = Node_RuleVariable.WILD;
                }
                List<RuleClauseCode> predicateCode = predicateToCodeMap.get( predicate );
                if ( predicateCode == null )
                {
                    predicateCode = new ArrayList<>();
                    predicateToCodeMap.put( predicate, predicateCode );
                }
                predicateCode.add( code );
                if ( predicateCode.size() > INDEX_THRESHOLD )
                {
                    indexPredicateToCodeMap.put( predicate, new HashMap<Node, List<RuleClauseCode>>() );
                }
            }
        }

        // Now add the wild card rules into the list for each non-wild predicate)
        List<RuleClauseCode> wildRules = predicateToCodeMap.get(Node_RuleVariable.WILD);
        if (wildRules != null) {
            for ( Map.Entry<Node, List<RuleClauseCode>> entry : predicateToCodeMap.entrySet() )
            {
                Node predicate = entry.getKey();
                List<RuleClauseCode> predicateCode = entry.getValue();
                if ( predicate != Node_RuleVariable.WILD )
                {
                    predicateCode.addAll( wildRules );
                }
            }
        }
        indexPredicateToCodeMap.put(Node_RuleVariable.WILD, new HashMap<Node, List<RuleClauseCode>>());
                
        // Now built any required two level indices
        for ( Map.Entry<Node, Map<Node, List<RuleClauseCode>>> entry : indexPredicateToCodeMap.entrySet() )
        {
            Node predicate = entry.getKey();
            Map<Node, List<RuleClauseCode>> predicateMap = entry.getValue();
            List<RuleClauseCode> wildRulesForPredicate = new ArrayList<>();
            List<RuleClauseCode> allRulesForPredicate =
                predicate.isVariable() ? allRuleClauseCodes : predicateToCodeMap.get( predicate );
            for ( Iterator<RuleClauseCode> j = allRulesForPredicate.iterator(); j.hasNext(); )
            {
                RuleClauseCode code = j.next();
                ClauseEntry head = code.getRule().getHeadElement( 0 );
                boolean indexed = false;
                if ( head instanceof TriplePattern )
                {
                    Node objectPattern = ( (TriplePattern) head ).getObject();
                    if ( !objectPattern.isVariable() && !Functor.isFunctor( objectPattern ) )
                    {
                        // Index against object
                        List<RuleClauseCode> indexedCode = predicateMap.get( objectPattern );
                        if ( indexedCode == null )
                        {
                            indexedCode = new ArrayList<>();
                            predicateMap.put( objectPattern, indexedCode );
                        }
                        indexedCode.add( code );
                        indexed = true;
                    }
                }
                if ( !indexed )
                {
                    wildRulesForPredicate.add( code );
                }
            }
            // Now fold the rules that apply to any index entry into all the indexed entries
            for ( Iterator<Map.Entry<Node, List<RuleClauseCode>>> k = predicateMap.entrySet().iterator(); k.hasNext(); )
            {
                Map.Entry<Node, List<RuleClauseCode>> ent = k.next();
                Node pred = ent.getKey();
                List<RuleClauseCode> predicateCode = ent.getValue();
                predicateCode.addAll( wildRulesForPredicate );
            }
        }
        
        // Now compile all the clauses
        for ( RuleClauseCode code : allRuleClauseCodes )
        {
            code.compile( this );
        }
    }
    
    /**
     * Add/remove a single rule from the store. 
     * Overridden in order to reset the "isCompiled" flag.
     * 
     * @param rule the rule, single headed only
     * @param isAdd true to add, false to remove 
     */
    @Override
    protected void doAddRemoveRule(Rule rule, boolean isAdd) {
        isCompiled = false;
        super.doAddRemoveRule(rule, isAdd);
    }

}
