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
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.SparqlQuery;
import static com.hp.hpl.jena.reasoner.rulesys.impl.ExecSparqlCommand.executeSparqlQuery2;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.util.ArrayList;
import java.util.HashMap;


public class SRE_fireRuleTerms {
    InfGraph infGraph;
    Rule rule;
    int endTerm;
    int beginTerm;
    ArrayList<ResultRow> result;
    ResultRow instNodes;
    SparqlInRuleTabling sparqlInRuleTabling;
    
    public SRE_fireRuleTerms(InfGraph infGraph, Rule rule, int endTerm, int beginTerm, ResultRow instNodes, SparqlInRuleTabling sparqlInRuleTabling) {
        this.infGraph = infGraph;
        this.rule = rule;
        this.endTerm = endTerm;
        this.beginTerm = beginTerm;
        this.instNodes = instNodes;
        result = new ArrayList<ResultRow>();
        this.sparqlInRuleTabling = sparqlInRuleTabling;
    }
    
    public boolean run() {
        boolean retV = true;
        ClauseEntry ce = rule.getBodyElement(beginTerm);
        
        if(ce instanceof TriplePattern) {
            TriplePattern cetp = (TriplePattern) ce;
            boolean [] nodeConcrete = {false, false, false};
            String [] nodeVar = {"", "", ""};

            Node subject = null;
            if(cetp.getSubject().isConcrete()) {
                subject = cetp.getSubject();
                nodeConcrete[0] = true;
            }
            else {
                nodeVar[0] = cetp.getSubject().getName();
                if(instNodes != null) {                
                    subject = instNodes.getInstanceValue(nodeVar[0]);
                    nodeConcrete[0] = (subject != null);
                }
            }    
            
            Node predicate = null;
            if(cetp.getPredicate().isConcrete()) {
                predicate = cetp.getPredicate();
                nodeConcrete[1] = true;
            }
            else {
                nodeVar[1] = cetp.getPredicate().getName();
                if(instNodes != null) {                
                    predicate = instNodes.getInstanceValue(nodeVar[1]);
                    nodeConcrete[1] = (predicate != null);
                }
            }  
            
            Node object = null;
            if(cetp.getObject().isConcrete()) {
                object = cetp.getObject();
                nodeConcrete[2] = true;
            }
            else {
                nodeVar[2] = cetp.getObject().getName();
                if(instNodes != null) {                
                    object = instNodes.getInstanceValue(nodeVar[2]);
                    nodeConcrete[2] = (object != null);
                }
            } 
            
            ExtendedIterator<Triple> triplesResult = infGraph.find(subject, predicate, object);
                       
            ResultList rl_result = new ResultList();
            while(triplesResult.hasNext()) {
                Triple t = triplesResult.next();
                ResultRow row = new ResultRow();
                if(instNodes != null) {
                    row.addResult(instNodes);
                }
                if(!nodeConcrete[0]){
                    row.addResult(nodeVar[0], t.getSubject());
                }
                if(!nodeConcrete[1]){
                    row.addResult(nodeVar[1], t.getPredicate());
                }
                if(!nodeConcrete[2]){
                    row.addResult(nodeVar[2], t.getObject());
                }
                rl_result.addResultRow(row);
            }
            
            boolean sameResult = false;
            ResultList rl_tabled = this.sparqlInRuleTabling.getValue0(subject, predicate, object);
            if(rl_tabled != null) {
                sameResult = rl_tabled.sameResult(rl_result);
            }
            
            if(beginTerm == endTerm) {
                result.addAll(rl_result.getResult());
            }
            else {
                if(sameResult) {
                    result.addAll(this.sparqlInRuleTabling.getValue1(subject, predicate, object).getResult());           
                }
                else {
                    for(ResultRow row : rl_result.getResult()) {
                        SRE_fireRuleTerms vSRE_fireRuleTerms = new SRE_fireRuleTerms(infGraph, rule, endTerm, beginTerm+1, row, this.sparqlInRuleTabling);
                            if(vSRE_fireRuleTerms.run()) {
                                ArrayList<ResultRow> rr_result = vSRE_fireRuleTerms.getResult();
                                result.addAll(rr_result);
                            }     
                    }
                    this.sparqlInRuleTabling.insertValue(subject, predicate, object, rl_result, (new ResultList(result)));
 
                }
            }
            

            retV = result.size()>0;
        }    
        else if(ce instanceof SparqlQuery) {
            SparqlQuery sq = new SparqlQuery((SparqlQuery) ce);
            sq.setSparqlQuery(replaceVars(sq));
            Answer_SparqlInRules answer = executeSparqlQuery2(sq, infGraph);
            retV = answer.getAnswer();
            if(retV && sq.getQuery().isSelectType()) {
                ResultList rl_result = new ResultList(); 
                for(ResultRow rr : answer.getResultList().getResult()) {
                    putQuestionMark(rr);
                    ResultRow joinRes = joinResult(rr, instNodes);
                    if(joinRes != null) {
                        rl_result.addResultRow(joinRes);
                    }    
                }
                boolean sameResult = false;
                ResultList rl_tabled = this.sparqlInRuleTabling.getValue0(sq.getSparqlCmd());
                if(rl_tabled != null) {
                    sameResult = rl_tabled.sameResult(rl_result);
                }

                if(beginTerm == endTerm) {
                    result.addAll(rl_result.getResult());
                }
                else {
                    if(sameResult) {
                        result.addAll(this.sparqlInRuleTabling.getValue1(sq.getSparqlCmd()).getResult());           
                    }
                    else {
                        for(ResultRow row : rl_result.getResult()) {
                            SRE_fireRuleTerms vSRE_fireRuleTerms = new SRE_fireRuleTerms(infGraph, rule, endTerm, beginTerm+1, row, this.sparqlInRuleTabling);
                            if(vSRE_fireRuleTerms.run()) {
                                ArrayList<ResultRow> rr_result = vSRE_fireRuleTerms.getResult();
                                result.addAll(rr_result);
                            }     
                        }
                        this.sparqlInRuleTabling.insertValue(sq.getSparqlCmd(), rl_result, (new ResultList(result)));
                    }
                }
            }
            else if(retV && sq.getQuery().isAskType()) {
                if(beginTerm == endTerm) {
                    result.add(instNodes);
                }
                else {
                    SRE_fireRuleTerms vSRE_fireRuleTerms = new SRE_fireRuleTerms(infGraph, rule, endTerm, beginTerm+1, instNodes, this.sparqlInRuleTabling);
                    if(vSRE_fireRuleTerms.run()) {
                        ArrayList<ResultRow> rr_result = vSRE_fireRuleTerms.getResult();
                        result.addAll(rr_result);
                    }
                }
            }
        }
        else if(ce instanceof Functor) {
            Functor f = (Functor) ce;
            Node [] args = f.getArgs();
            Node [] newArgs = new Node[args.length];
            BindingVector bindVector = new BindingVector( args.length );
            for(int i=0; i<args.length; i++) {
                Node n = args[i];
                if(args[i].isVariable()) {
                    boolean isInstatiate = false;
                    int a=0;
                    while(!isInstatiate && a<instNodes.size()) {
                        Node n1 = instNodes.getInstanceValue(args[i].getName());
                        if(n1!=null) {
                            n = n1;
                            bindVector.bind(i, n);
                            isInstatiate = true;
                        } else {                           
                            a++;
                        }
                    }
                }
                newArgs[i] = n;
                //bindVector.bind(i, n);
            }
            SparqlInRulesRuleContext tempContext = new SparqlInRulesRuleContext(infGraph);
            tempContext.setRule( rule );
            tempContext.setEnv( bindVector );
            //retV = f.evalAsBodyClause(tempContext);
            retV = f.getImplementor().bodyCall(newArgs, newArgs.length, tempContext);
            if(retV) {
                ResultRow row = new ResultRow();
                if(instNodes != null) {
                    row.addResult(instNodes);
                }
                Node [] envNodes = bindVector.getEnvironment();
                for(int i=0; i<args.length; i++) {
                    if(newArgs[i].isVariable() && envNodes[i] != null && envNodes[i].isConcrete()) {
                        row.addResult(newArgs[i].getName(), envNodes[i]);
                    }
                }
                if(beginTerm == endTerm) {
                    result.add(row);
                }
                else {
                    SRE_fireRuleTerms vSRE_fireRuleTerms = new SRE_fireRuleTerms(infGraph, rule, endTerm, beginTerm+1, row, this.sparqlInRuleTabling);
                    if(vSRE_fireRuleTerms.run()) {
                        result.addAll(vSRE_fireRuleTerms.getResult());
                    }        
                }
            }
        }
        return retV;
    }

    public ArrayList<ResultRow> getResult() {
        return result;
    }
    
    
    private void putQuestionMark(ResultRow rr){
        HashMap<String, Node> new_rr = new HashMap<String, Node>();
        
        for(String field : rr.getAllFieldsName()){
            String newField = field;
            if(!field.startsWith("?")) {
                newField = "?" + field;
            }
            new_rr.put(newField, rr.getInstanceValue(field));
        }
        rr.setResult(new_rr);
        
    }
    
    private ResultRow joinResult(ResultRow rr1, ResultRow rr2){
        ResultRow rrRet = new ResultRow();
        if(rr2 != null) {
            rrRet.addResult(rr2);
        }
        boolean allMatch = true;
        if(rr1 != null) {
            for(String field : rr1.getAllFieldsName()) {
                if(rr2 != null && rr2.existField(field)) {
                    allMatch = rr2.getInstanceValue(field).sameValueAs(rr1.getInstanceValue(field));
                    if(!allMatch) break;
                }
                else {
                    rrRet.addResult(field, rr1.getInstanceValue(field));
                }
            }
        }
        if(allMatch && rrRet.size()>0) {
            return rrRet;
        }
        else {
            return null;
        }
    }
    
    private String replaceVars(SparqlQuery sq) {
        String retCmd = sq.getSparqlCmd();
        
        if(instNodes!=null) {
            for(String field : instNodes.getAllFieldsName()) {
                String fieldSparql = field.replace("?", "");

                if(instNodes.getInstanceValue(field).isURI() && sq.existOuterVariable(fieldSparql)) {
                    retCmd = retCmd.replace("?"+sq.getOuterVariable(fieldSparql), "<" + instNodes.getInstanceValue(field).getURI() + ">");
                }    
            }
        }
        return retCmd;
    }
}
