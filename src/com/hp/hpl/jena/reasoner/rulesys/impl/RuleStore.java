/******************************************************************
 * File:        RuleStore.java
 * Created by:  Dave Reynolds
 * Created on:  04-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleStore.java,v 1.3 2003-05-12 07:58:25 der Exp $
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
 * @version $Revision: 1.3 $ on $Date: 2003-05-12 07:58:25 $
 */
public class RuleStore {

    /** The set of rules indexed by head predicate */
    protected OneToManyMap goalMap;
    
    /** The list of all rules in the store */
    protected List allRules;
    
    /**
     * Constructor. Stores and indexes a list of rules.
     */
    public RuleStore(List rules) {
        allRules = rules;
        goalMap = new OneToManyMap();
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule rule = (Rule)i.next();
            if (rule.headLength() != 1) {
                for (int j = 0; j < rule.headLength(); j++) {
                    addRule ( new Rule(rule.getName(), 
                                        new Object[] {rule.getHeadElement(j)}, 
                                        rule.getBody()
                                        ) );
                }
                
            } else {
                addRule(rule);
            }
        }
    }
    
    /**
     * Add a single rule to the store. It assumes the rule
     * has a single head element.
     */
    public void addRule(Rule rule) {
        Object headClause = rule.getHeadElement(0);
        if (headClause instanceof TriplePattern) {
            TriplePattern headpattern = (TriplePattern)headClause;
            Node predicate = headpattern.getPredicate();
            if (predicate.isVariable()) {
                goalMap.put(Node.ANY, rule);
            } else {
                goalMap.put(predicate, rule);
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