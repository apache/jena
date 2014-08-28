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
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.InfModelImpl;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.SparqlQuery;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ExecSparqlCommand {
    public static ArrayList<Triple>  executeSparqlQuery_bak(SparqlQuery sparqlQuery, TriplePattern head, InfGraph infGraph){
        
        Model m = new InfModelImpl(infGraph);
        Query qf = QueryFactory.create(sparqlQuery.getSparqlCmd());
        Map<String, String> rulesPrefixes = sparqlQuery.getRulesPrefixes();
        Set keys = rulesPrefixes.keySet();
        
        for (Iterator i = keys.iterator(); i.hasNext();) 
        {
            String prefix = (String) i.next();
            String localname = (String) rulesPrefixes.get(prefix);
            qf.setPrefix(prefix, localname);
        }
 
        QueryExecution qe = QueryExecutionFactory.create(qf, m);
        
        ResultSet results = qe.execSelect(); 
        List<String> resultVars = results.getResultVars();
        ArrayList<Triple> tripleResults = new ArrayList<Triple>();
        while(results.hasNext()) {
            QuerySolution qs = results.nextSolution();
            Node subject = null;
            if(head.getSubject().isConcrete()) {
                subject = head.getSubject();
            }
            else {
                String varName = head.getSubject().getName().replace("?", "");
                if(resultVars.contains(varName)) {
                    subject = qs.get(varName).asNode();
                }
            }
            
            Node predicate = null;
            if(head.getPredicate().isConcrete()) {
                predicate = head.getPredicate();
            }
            else {
                String varName = head.getPredicate().getName().replace("?", "");
                if(resultVars.contains(varName)) {
                    predicate = qs.get(varName).asNode();
                }
            }
            
            Node object = null;
            if(head.getObject().isConcrete()) {
                object = head.getObject();
            }
            else {
                String varName = head.getObject().getName().replace("?", "");
                if(resultVars.contains(varName)) {
                    object = qs.get(varName).asNode();
                }
            }
 
            if(subject != null && predicate != null && object != null) {
                Triple newT = new Triple(subject, predicate, object);
                if(newT.isConcrete()) {
                    tripleResults.add(newT);
                }
            }
            
 
        }
        if(tripleResults.size()>0) {
            /*
            BackwardRuleInfGraphI infGraph = engine.getInfGraph();
            RuleDerivation d = new RuleDerivation(envFrame.getRule(), head, tripleResults, infGraph);
            infGraph.logDerivation(head, d);
            */
        }
        return tripleResults;
    }

    public static ArrayList<Triple>  executeSparqlQuery(SparqlQuery sparqlQuery, TriplePattern head, TriplePattern goal, InfGraph infGraph){
        String lSparql = sparqlQuery.getSparqlCmd();
   
        ArrayList<Triple> tripleResults = new ArrayList<Triple>();
        Model m = new InfModelImpl(infGraph);
        QueryExecution qe = QueryExecutionFactory.create(sparqlQuery.getQuery(), m);

            
        if(sparqlQuery.getQuery().isSelectType()) {
            //System.out.println("head -> "+head.toString()+";   goal -> "+goal.toString());

            if(head.getSubject().isVariable() && !goal.getSubject().isVariable()) {
                lSparql = lSparql.replace(head.getSubject().getName(), ((Node_URI) goal.getSubject()).toString());
            }

            if(head.getPredicate().isVariable() && !goal.getPredicate().isVariable()) {
                lSparql = lSparql.replace(head.getPredicate().getName(), goal.getPredicate().getLiteralValue().toString());
            }

           if(head.getObject().isVariable() && !goal.getObject().isVariable()) {
                lSparql = lSparql.replace(head.getObject().getName().toString(), goal.getObject().getLiteralValue().toString());
            }


            //Model m = new InfModelImpl(infGraph);
            //Query qf = QueryFactory.create(sparqlQuery.getSparqlCmd());
            /*
            Map<String, String> rulesPrefixes = sparqlQuery.getRulesPrefixes();
            Set keys = rulesPrefixes.keySet();

            for (Iterator i = keys.iterator(); i.hasNext();) 
            {
                String prefix = (String) i.next();
                String localname = (String) rulesPrefixes.get(prefix);
                qf.setPrefix(prefix, localname);
            }
            */
            //QueryExecution qe = QueryExecutionFactory.create(sparqlQuery.getQuery(), m);

            ResultSet results = qe.execSelect(); 
            List<String> resultVars = results.getResultVars();
            while(results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Node subject = null;
                if(head.getSubject().isConcrete()) {
                    subject = head.getSubject();
                }
                else {
                    String varName = head.getSubject().getName().replace("?", "");
                    if(resultVars.contains(varName)) {
                        subject = qs.get(varName).asNode();
                    }
                }

                Node predicate = null;
                if(head.getPredicate().isConcrete()) {
                    predicate = head.getPredicate();
                }
                else {
                    String varName = head.getPredicate().getName().replace("?", "");
                    if(resultVars.contains(varName)) {
                        predicate = qs.get(varName).asNode();
                    }
                }

                Node object = null;
                if(head.getObject().isConcrete()) {
                    object = head.getObject();
                }
                else {
                    String varName = head.getObject().getName().replace("?", "");
                    if(resultVars.contains(varName)) {
                        object = qs.get(varName).asNode();
                    }
                }

                if(subject != null && predicate != null && object != null) {
                    Triple newT = new Triple(subject, predicate, object);
                    TriplePattern newTP = new TriplePattern(newT);
                    //if(newT.isConcrete() && newTP.compatibleWith(goal)) {
                    if(newT.isConcrete()) {
                        tripleResults.add(newT);
                    }
                }


            }
        }
        else if(sparqlQuery.getQuery().isAskType()) {
            if(qe.execAsk() && head.isGround()) {
                Triple newT = new Triple(head.getSubject(), head.getPredicate(), head.getObject());
                tripleResults.add(newT);
            }    
        }
        if(tripleResults.size()>0) {
            /*
            BackwardRuleInfGraphI infGraph = engine.getInfGraph();
            RuleDerivation d = new RuleDerivation(envFrame.getRule(), head, tripleResults, infGraph);
            infGraph.logDerivation(head, d);
            */
        }
        return tripleResults;
    }
    
    public static ArrayList<Triple>  executeSparqlQuery(SparqlQuery sparqlQuery, InfGraph infGraph){
        //String lSparql = sparqlQuery.getSparqlCmd();
   
        TriplePattern [] vhead = sparqlQuery.getHead();
  
        
        Model m = new InfModelImpl(infGraph);

        QueryExecution qe = QueryExecutionFactory.create(sparqlQuery.getQuery(), m);
        
        ArrayList<Triple> tripleResults = new ArrayList<Triple>();
        
        if(qe.getQuery().isSelectType()) {
        
            ResultSet results = qe.execSelect(); 
            List<String> resultVars = results.getResultVars();
            while(results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                for(int i=0; i<vhead.length; i++) {
                    TriplePattern head = vhead[i];
                    Node subject = null;
                    if(head.getSubject().isConcrete()) {
                        subject = head.getSubject();
                    }
                    else {
                        String varName = head.getSubject().getName().replace("?", "");
                        if(resultVars.contains(varName)) {
                            subject = qs.get(varName).asNode();
                        }
                    }

                    Node predicate = null;
                    if(head.getPredicate().isConcrete()) {
                        predicate = head.getPredicate();
                    }
                    else {
                        String varName = head.getPredicate().getName().replace("?", "");
                        if(resultVars.contains(varName)) {
                            predicate = qs.get(varName).asNode();
                        }
                    }

                    Node object = null;
                    if(head.getObject().isConcrete()) {
                        object = head.getObject();
                    }
                    else {
                        String varName = head.getObject().getName().replace("?", "");
                        if(resultVars.contains(varName)) {
                            object = qs.get(varName).asNode();
                        }
                    }

                    if(subject != null && predicate != null && object != null) {
                        Triple newT = new Triple(subject, predicate, object);
                        TriplePattern newTP = new TriplePattern(newT);
                        //if(newT.isConcrete() && newTP.compatibleWith(goal)) {
                        if(newT.isConcrete()) {
                            tripleResults.add(newT);
                        }
                    }
                }

            }
        }
        else if(qe.getQuery().isAskType()) {
            if(qe.execAsk()) {
                for(int i=0; i<vhead.length; i++) {
                    TriplePattern head = vhead[i];
                    if(head.isGround()) {
                        Triple newT = new Triple(head.getSubject(), head.getPredicate(), head.getObject());
                        tripleResults.add(newT);
                    }
                }
            }    
        }
        
        if(tripleResults.size()>0) {
            /*
            BackwardRuleInfGraphI infGraph = engine.getInfGraph();
            RuleDerivation d = new RuleDerivation(envFrame.getRule(), head, tripleResults, infGraph);
            infGraph.logDerivation(head, d);
            */
        }
        return tripleResults;
    }
        
    
    public static ArrayList<Triple>  executeSparqlQuery(SparqlQuery sparqlQuery, TriplePattern goal, RuleClauseCode clause, InfGraph infGraph){
        
        String v1 = goal.getSubject().getName();
        String v2 = goal.getPredicate().getName();
        String v3 = goal.getObject().getName();
        clause.toString();
        TriplePattern head = goal;
        String lSparql = sparqlQuery.getSparqlCmd();

        if(head.getSubject().isVariable() && !goal.getSubject().isVariable()) {
            lSparql = lSparql.replace(head.getSubject().getName(), ((Node_URI) goal.getSubject()).toString());
        }
        
        if(head.getPredicate().isVariable() && !goal.getPredicate().isVariable()) {
            lSparql = lSparql.replace(head.getPredicate().getName(), goal.getPredicate().getLiteralValue().toString());
        }

       if(head.getObject().isVariable() && !goal.getObject().isVariable()) {
            lSparql = lSparql.replace(head.getObject().getName().toString(), goal.getObject().getLiteralValue().toString());
        }
        
  
        Model m = new InfModelImpl(infGraph);
        Query qf = QueryFactory.create(sparqlQuery.getSparqlCmd());
        Map<String, String> rulesPrefixes = sparqlQuery.getRulesPrefixes();
        Set keys = rulesPrefixes.keySet();
        
        for (Iterator i = keys.iterator(); i.hasNext();) 
        {
            String prefix = (String) i.next();
            String localname = (String) rulesPrefixes.get(prefix);
            qf.setPrefix(prefix, localname);
        }
 
        QueryExecution qe = QueryExecutionFactory.create(qf, m);
        
        ResultSet results = qe.execSelect(); 
        List<String> resultVars = results.getResultVars();
        ArrayList<Triple> tripleResults = new ArrayList<Triple>();
        while(results.hasNext()) {
            QuerySolution qs = results.nextSolution();
            Node subject = null;
            if(head.getSubject().isConcrete()) {
                subject = head.getSubject();
            }
            else {
                String varName = head.getSubject().getName().replace("?", "");
                if(resultVars.contains(varName)) {
                    subject = qs.get(varName).asNode();
                }
            }
            
            Node predicate = null;
            if(head.getPredicate().isConcrete()) {
                predicate = head.getPredicate();
            }
            else {
                String varName = head.getPredicate().getName().replace("?", "");
                if(resultVars.contains(varName)) {
                    predicate = qs.get(varName).asNode();
                }
            }
            
            Node object = null;
            if(head.getObject().isConcrete()) {
                object = head.getObject();
            }
            else {
                String varName = head.getObject().getName().replace("?", "");
                if(resultVars.contains(varName)) {
                    object = qs.get(varName).asNode();
                }
            }
 
            if(subject != null && predicate != null && object != null) {
                Triple newT = new Triple(subject, predicate, object);
                TriplePattern newTP = new TriplePattern(newT);
                if(newT.isConcrete() && newTP.compatibleWith(goal)) {
                    tripleResults.add(newT);
                }
            }
            
 
        }
        if(tripleResults.size()>0) {
            /*
            BackwardRuleInfGraphI infGraph = engine.getInfGraph();
            RuleDerivation d = new RuleDerivation(envFrame.getRule(), head, tripleResults, infGraph);
            infGraph.logDerivation(head, d);
            */
        }
    
        return tripleResults;
    }
      
}
