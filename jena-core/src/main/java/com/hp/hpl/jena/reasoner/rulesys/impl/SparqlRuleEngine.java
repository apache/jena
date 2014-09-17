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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.SparqlQuery;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;


public class SparqlRuleEngine {
    InfGraph infGraph;
    Rule rule;
    ArrayList<Triple> tripleResults;
    
    public SparqlRuleEngine(InfGraph infGraph, Rule rule) {
        this.infGraph = infGraph;
        this.rule = rule;
        tripleResults = new ArrayList<Triple>();

    }
    
    public SparqlRuleEngine(Rule rule) {
        this.rule = rule;
        tripleResults = new ArrayList<Triple>();

    }
    
    public void setInfGraph(InfGraph infGraph) {
        this.infGraph = infGraph;
    }
    
    public boolean run() {
        int i=0;
        int lastI=0;
        boolean cont = true;
        ArrayList<ResultRow> result = null;
        
        SparqlInRuleTabling sparqlInRuleTabling = new SparqlInRuleTabling();
        
        SRE_fireRuleTerms vSRE_fireRuleTerms = new SRE_fireRuleTerms(infGraph, rule, rule.bodyLength()-1, 0, null, sparqlInRuleTabling);
        if(vSRE_fireRuleTerms.run()) {
            result = vSRE_fireRuleTerms.getResult();
        }
        /*
        while(cont && i<rule.bodyLength()) {
            for(;!(rule.getBodyElement(i) instanceof SparqlQuery);i++);
            if(i>lastI) {
                SRE_fireRuleTerms vSRE_fireRuleTerms = new SRE_fireRuleTerms(infGraph, rule, i-1, lastI, null);
                if(vSRE_fireRuleTerms.run()) {
                    result = vSRE_fireRuleTerms.getResult();
                }
            }
            SparqlQuery sq = (SparqlQuery) rule.getBodyElement(i);
            i++;
        }    
        */
        

        assertResult(result);
        
        return result != null && result.size()>0;
    }
    
    
    private void assertResult(ArrayList<ResultRow> result){
        ClauseEntry [] vhead = rule.getHead();
        if(result != null && result.size()>0) {
            for(ResultRow r : result) {
                for(int i=0; i<vhead.length; i++) {
                    TriplePattern head = (TriplePattern) vhead[i];
                    Node subject = null;
                    if(head.getSubject().isConcrete()) {
                        subject = head.getSubject();
                    }
                    else {
                        String varName = head.getSubject().getName();
                        subject = r.getInstanceValue(varName);
                    }

                    Node predicate = null;
                    if(head.getPredicate().isConcrete()) {
                        predicate = head.getPredicate();
                    }
                    else {
                        String varName = head.getPredicate().getName();
                        predicate = r.getInstanceValue(varName);
                    }

                    Node object = null;
                    if (Functor.isFunctor(head.getObject())) {
                        Node[] args = ((Functor)head.getObject().getLiteralValue()).getArgs();
                        Node[] newArgs = new Node[args.length];
                        for(int x=0; x<args.length; x++) {
                            if(!args[x].isVariable()) {
                                newArgs[x] = args[x];
                            }
                            else {
                                String varName =  args[x].getName();
                                if(r.existField(varName)) {
                                    newArgs[x] = r.getInstanceValue(varName);
                                }
                                else {
                                    newArgs[x] = args[x];
                                }
                            }    
                        }
                        object = Functor.makeFunctorNode(((Functor)head.getObject().getLiteralValue()).getName(), newArgs);
                    } 
                    else if(head.getObject().isConcrete()) {
                        object = head.getObject();
                    }
                    else {
                        String varName = head.getObject().getName();
                        object = r.getInstanceValue(varName);
                    }

                    if(subject != null && predicate != null && object != null) {
                        Triple newT = new Triple(subject, predicate, object);
                        TriplePattern newTP = new TriplePattern(newT);
                        //if(newT.isConcrete() && newTP.compatibleWith(goal)) {
                        //if(newT.isConcrete()) {
                        if(!tripleResults.contains(newT)) {
                            tripleResults.add(newT);
                        }
                        //}
                    }
                }    
            }
        }
    }
    
    public ArrayList<Triple> getResult() {
        return tripleResults;
    }
}
