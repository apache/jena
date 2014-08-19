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

package com.hp.hpl.jena.reasoner.sparqlinrules.test;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TestsGenericClass {
    
    public static void tmpPrintResult(ArrayList<Binding>  list_res, String msg){
        System.out.println("Results of "+msg);
        
        for(Binding q : list_res){
            System.out.println(q.toString());
        }
    }
    
    public static void tmpPrintResult2(ArrayList<ResultRow> list_res, String msg){
        System.out.println("Results of "+msg);
        
        for(ResultRow q : list_res){
            System.out.println(q.toString());
        }
    }
    
    public static List<Rule> createListRules (String myRules) {
        BufferedReader br = new BufferedReader(new StringReader(myRules));
            
        return Rule.parseRules( Rule.rulesParserFromReader(br) );
    }
    
    public static ResultSet executeSparql(String lSparql, String myData, String myRules, String engineMode){
        Model m = fillModel(myData);
        InfModel inf = createInfReasoner(myRules, m, engineMode);
        
        return executeSparql(lSparql, inf); 
    }
    
    public static ResultSet executeSparql(String lSparql, InfModel inf){
        QueryExecution qe = QueryExecutionFactory.create(lSparql,
        inf);
        
        return qe.execSelect(); 
    }
    
    public static Model fillModel(String myData) {
        Model m = ModelFactory.createDefaultModel();
        
        m.read( new ByteArrayInputStream( myData.getBytes() ), null, "TTL" );   
        
        return m;
    }
    
    public static InfModel createInfReasoner(String myRules, Model m, String engineMode) {
        GenericRuleReasoner reasoner = new GenericRuleReasoner(createListRules(myRules));
        //System.out.println(m.getGraph().toString());
        
        reasoner.setParameter(ReasonerVocabulary.PROPruleMode, engineMode);


        return ModelFactory.createInfModel(reasoner, m);
    }   
  
 
}
