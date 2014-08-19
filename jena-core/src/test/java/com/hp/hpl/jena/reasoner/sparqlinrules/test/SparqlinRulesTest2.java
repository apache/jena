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

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import java.util.ArrayList;
import static junit.framework.TestCase.assertTrue;


public class SparqlinRulesTest2 {
    Model m;
    InfModel inf;
    
    String engineMode;
    
    String myData1;
    String myData2;
    String myData3;
    
    String myRule11;
    String myRule12;
    String myRule13;
    String myRule21;
    String myRule22;
    String myRule31;
    String myRule32;
    String myRule41;
    String myRule42;
    String myRule51;
    String myRule52;
    String myRule53;
    String myRule54;
    String myRule61;

    public SparqlinRulesTest2(String engineMode) {
        
        this.engineMode = engineMode;
        myData1 = 
            "<eg:A> <eg:p> <eg:B> .\n" +
            "<eg:B> <eg:p> <eg:C> .\n" +
            "<eg:D> <eg:p> <eg:E> .\n" +
            "<eg:C> <eg:p> <eg:D> .";
        
        
        myData2 = 
            "<eg:A> <eg:p> <eg:B> .\n" +
            "<eg:B> <eg:p> <eg:C> .\n" +
            "<eg:C> <eg:p> <eg:D> . \n" + 
            "<eg:C> <eg:p> <eg:C1> . \n" + 
            "<eg:D> <eg:p> <eg:D1> . "; 

        myData3 = 
                "<http://www.example.com/A> <http://www.example.com/p> <http://www.example.com/B> .\n" +
                "<http://www.example.com/B> <http://www.example.com/p> <http://www.example.com/C> .\n" +
                "<http://www.example.com/D> <http://www.example.com/p> <http://www.example.com/E> .\n" +
                "<http://www.example.com/C> <http://www.example.com/p> <http://www.example.com/D> .";

        
        myRule11 = 
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. \n " +
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). \n";

        myRule12 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). \n";

        myRule13 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n";
        
        myRule21 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p1> ?z) -> (?x <eg:p2> ?z). ";
        
        
        myRule22 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p1> ?y) (?y <eg:p> ?z)  -> (?x <eg:p2> ?z). ";
       
        myRule31 = 
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. \n " +
                "(\\\\\\sparql Select ?x ?y where {?x <eg:p2> ?y .} \\\\\\sparql) -> (?x <eg:p1> ?y). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). \n";

        myRule32 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p1> ?y) -> (?x <eg:p2> ?y). \n";
        
        
        myRule41 = 
                " -> table(<eg:p2>). \n " +
                "(\\\\\\sparql Select ?x ?y where {?x <eg:p2> ?y .} \\\\\\sparql) -> (?x <eg:p1> ?y). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p2> ?z) -> (?x <eg:p2> ?z). \n";

       myRule42 = 
                " -> table(<eg:p2>). \n " +
                " -> table(<eg:p1>). \n " +
                "(\\\\\\sparql Select ?x ?y where {?x <eg:p2> ?y .} \\\\\\sparql) -> (?x <eg:p1> ?y). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p2> ?z) -> (?x <eg:p2> ?z). \n";
        
       
        myRule51 = 
                "@prefix p: <http://www.example.com/>.  \n " +
                "(\\\\\\sparql Select ?x ?z where {?x p:p ?y . ?y p:p ?z .} \\\\\\sparql) -> (?x p:p1 ?z). \n " + 
                "(?x p:p ?y) (?y p:p ?z) -> (?x p:p2 ?z). \n";

        myRule52 = 
                "@prefix p: <http://www.example.com/>.  \n " +
                "(\\\\\\sparql prefix p: <http://www.example.com/>  Select ?x ?z where {?x p:p ?y . ?y p:p ?z .} \\\\\\sparql) -> (?x p:p1 ?z). \n " + 
                "(?x p:p ?y) (?y p:p ?z) -> (?x p:p2 ?z). \n";

        myRule53 = 
                "(\\\\\\sparql prefix p: <http://www.example.com/> Select ?x ?z where {?x p:p ?y . ?y p:p ?z .} \\\\\\sparql) -> (?x <http://www.example.com/p1> ?z). \n " + 
                "(?x <http://www.example.com/p> ?y) (?y <http://www.example.com/p> ?z) -> (?x <http://www.example.com/p2> ?z). \n";

        
        myRule54 = 
                "(\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <http://www.example.com/p1> ?z). \n " + 
                "(?x <http://www.example.com/p> ?y) (?y <http://www.example.com/p> ?z) -> (?x <http://www.example.com/p2> ?z). \n";
        
        myRule54 = 
                "@prefix p: <http://www.example.com/>.  \n " +
                "(\\\\\\sparql prefix p: <http://www.example.com/>  Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <http://www.example.com/p1> ?z). \n " + 
                "(?x p:p ?y) (?y p:p ?z) -> (?x p:p2 ?z). \n";
        
        myRule61 = 
                "(\\\\\\sparql ask {<eg:A> <eg:p> ?y . ?y <eg:p> <eg:C> .} \\\\\\sparql) -> (<eg:A> <eg:p1> <eg:C>). \n " + 
                "(<eg:B> <eg:p2> <eg:D>) <- (\\\\\\sparql ask {<eg:B> <eg:p> ?y . ?y <eg:p> <eg:D> .} \\\\\\sparql) . \n" +
                "(\\\\\\sparql ask {<eg:A> <eg:p> ?y . ?y <eg:p> <eg:B> .} \\\\\\sparql) -> (<eg:A> <eg:p1> <eg:B>). \n " + 
                "(<eg:B> <eg:p2> <eg:C>) <- (\\\\\\sparql ask {<eg:B> <eg:p> ?y . ?y <eg:p> <eg:C> .} \\\\\\sparql) . \n";

    }
    
    public void run(String msg) {
        final int nTests = 15;
        System.out.println(msg + " - test 1/" + nTests);
        sparqlInRule_01();
        System.out.println(msg + " - test 2/" + nTests);
        sparqlInRule_02();
        System.out.println(msg + " - test 3/" + nTests);
        sparqlInRule_03();
        System.out.println(msg + " - test 4/" + nTests);
        sparqlInRule_04();
        System.out.println(msg + " - test 5/" + nTests);
        sparqlInRule_05();
        System.out.println(msg + " - test 6/" + nTests);
        sparqlInRule_06();
        System.out.println(msg + " - test 7/" + nTests);
        sparqlInRule_07();
        System.out.println(msg + " - test 8/" + nTests);
        sparqlInRule_08();
        System.out.println(msg + " - test 9/" + nTests);
        sparqlInRule_09();
        System.out.println(msg + " - test 10/" + nTests);
        sparqlInRule_10();
        System.out.println(msg + " - test 11/" + nTests);
        sparqlInRule_11();
        System.out.println(msg + " - test 12/" + nTests);
        sparqlInRule_12();
        System.out.println(msg + " - test 13/" + nTests);
        sparqlInRule_13();
        System.out.println(msg + " - test 14/" + nTests);
        sparqlInRule_14();
        System.out.println(msg + " - test 15/" + nTests);
        sparqlInRule_15();
    }
    
    
    public void run() {
        sparqlInRule_01();
        sparqlInRule_02();
        sparqlInRule_03();
        sparqlInRule_04();
        sparqlInRule_05();
        sparqlInRule_06();
        sparqlInRule_07();
        sparqlInRule_08();
        sparqlInRule_09();
        sparqlInRule_10();
        sparqlInRule_11();
        sparqlInRule_12();
        sparqlInRule_13();
        sparqlInRule_14();
        sparqlInRule_15();
    }
    
    /*
    public void runTests() {

        if(sparqlInRule_01()) {
            System.out.println("test01 - Succeeded");
        }
        else {
            System.out.println("test01 - Fail");
        }
        
        if(sparqlInRule_02()) {
            System.out.println("test02 - Succeeded");
        }
        else {
            System.out.println("test02 - Fail");
        }
        
        if(sparqlInRule_03()) {
            System.out.println("test03 - Succeeded");
        }
        else {
            System.out.println("test03 - Fail");
        }
      
        if(sparqlInRule_04()) {
            System.out.println("test04 - Succeeded");
        }
        else {
            System.out.println("test04 - Fail");
        }

        if(sparqlInRule_05()) {
            System.out.println("test05 - Succeeded");
        }
        else {
            System.out.println("test05 - Fail");
        } 
        
        if(sparqlInRule_06()) {
            System.out.println("test06 - Succeeded");
        }
        else {
            System.out.println("test06 - Fail");
        }   
        
        if(sparqlInRule_07()) {
            System.out.println("test07 - Succeeded");
        }
        else {
            System.out.println("test07 - Fail");
        }
        
        if(sparqlInRule_08()) {
            System.out.println("test08 - Succeeded");
        }
        else {
            System.out.println("test08 - Fail");
        }      
 

        if(sparqlInRule_09()) {
            System.out.println("test09 - Succeeded");
        }
        else {
            System.out.println("test09 - Fail");
        }  

        if(sparqlInRule_10()) {
            System.out.println("test10 - Succeeded");
        }
        else {
            System.out.println("test10 - Fail");
        }  
        
        
        if(sparqlInRule_11()) {
            System.out.println("test11 - Succeeded");
        }
        else {
            System.out.println("test11 - Fail");
        }          
    }
    */
    

    public void sparqlInRule_01() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule12, engineMode);

        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule12, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
    
    public void sparqlInRule_02() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule11, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule11, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
    
    public void sparqlInRule_03() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule12, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule12, engineMode);
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
    
    public void sparqlInRule_04() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule13, engineMode);

        
        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addField(new ResultField("a", "eg:A"));
        row1.addField(new ResultField("b", "eg:C"));
        resultrows.add(row1);
                
        ResultRow row2 = new ResultRow();
        row2.addField(new ResultField("a", "eg:B"));
        row2.addField(new ResultField("b", "eg:D"));
        resultrows.add(row2);

        ResultRow row3 = new ResultRow();
        row3.addField(new ResultField("a", "eg:C"));
        row3.addField(new ResultField("b", "eg:E"));
        resultrows.add(row3);
        
        ResultList result2 = new ResultList(resultrows);

        
        assertTrue(result2.sameResult(result1));
    }
    
    
    public void sparqlInRule_05() {
        String lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule21, engineMode);

        
        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addField(new ResultField("a", "eg:A"));
        row1.addField(new ResultField("b", "eg:D"));
        resultrows.add(row1);
                
        ResultRow row2 = new ResultRow();
        row2.addField(new ResultField("a", "eg:B"));
        row2.addField(new ResultField("b", "eg:D1"));
        resultrows.add(row2);

        ResultRow row3 = new ResultRow();
        row3.addField(new ResultField("a", "eg:A"));
        row3.addField(new ResultField("b", "eg:C1"));
        resultrows.add(row3);
        
        ResultList result2 = new ResultList(resultrows);
        
        /*
        lSparql = "Select ?a ?b where {?a <eg:p> ?b}";
        ResultSet r3 = TestsGenericClass.executeSparql(lSparql, myData2, myRule21);
        ResultList result3 = convertResult(r3);
        lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r4 = TestsGenericClass.executeSparql(lSparql, myData2, myRule21);
        ResultList result4 = convertResult(r4);
        
        System.out.println("Result clause 1 {?a <eg:p> ?b}");
        result3.print();
        
        System.out.println("Result clause 1 {?a <eg:p1> ?b}");
        result4.print();
        

        System.out.println("Result 1");
        result1.print();
        
        System.out.println("Result 2");
        result2.print();
        */
        
        assertTrue(result2.sameResult(result1));
    }
    

    public void sparqlInRule_06() {
        String lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule22, engineMode);
        

        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addField(new ResultField("a", "eg:A"));
        row1.addField(new ResultField("b", "eg:D"));
        resultrows.add(row1);
                
        ResultRow row2 = new ResultRow();
        row2.addField(new ResultField("a", "eg:A"));
        row2.addField(new ResultField("b", "eg:C1"));
        resultrows.add(row2);
        
        ResultRow row3 = new ResultRow();
        row3.addField(new ResultField("a", "eg:B"));
        row3.addField(new ResultField("b", "eg:D1"));
        resultrows.add(row3);

        ResultList result2 = new ResultList(resultrows);
        
        /*
        System.out.println("Result 1");
        result1.print();
        
        System.out.println("Result 2");
        result2.print();
        */
        
        assertTrue(result2.sameResult(result1));
    }

    public void sparqlInRule_07() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule32, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData2, myRule32, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
    
    public void sparqlInRule_08() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule31, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData2, myRule32, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }

    public void sparqlInRule_09() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule41, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule41, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
         
    public void sparqlInRule_10() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule42, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule42, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }            
    
    public void sparqlInRule_11() {
        String lSparql = 
            "PREFIX p:<http://www.example.com/> \n" +            
            "Select ?a ?b where {?a p:p1 ?b}";
        
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData3, myRule51, engineMode);
        
        lSparql = 
            "PREFIX p:<http://www.example.com/> \n" +            
            "Select ?a ?b where {?a p:p2 ?b}";
        
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData3, myRule51, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
    
    public void sparqlInRule_12() {
        String lSparql = 
            "PREFIX p:<http://www.example.com/> \n" +            
            "Select ?a ?b where {?a p:p1 ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData3, myRule52, engineMode);
        
        lSparql = 
            "PREFIX p:<http://www.example.com/> \n" +            
            "Select ?a ?b where {?a p:p2 ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData3, myRule52, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
     
    public void sparqlInRule_13() {
        String lSparql = 
            "PREFIX p:<http://www.example.com/> \n" +            
            "Select ?a ?b where {?a p:p1 ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData3, myRule53, engineMode);
        
        lSparql = 
            "PREFIX p:<http://www.example.com/> \n" +            
            "Select ?a ?b where {?a p:p2 ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData3, myRule53, engineMode);
        
        
        assertTrue(CompareResults.sameResult(r1, r2));
    }
     
  
    public void sparqlInRule_14() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule61, engineMode);
        

        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addField(new ResultField("a", "eg:A"));
        row1.addField(new ResultField("b", "eg:C"));
        resultrows.add(row1);
   
        ResultList result2 = new ResultList(resultrows);
        
        assertTrue(result2.sameResult(result1));
    }
   
    public void sparqlInRule_15() {
        String lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule61, engineMode);
        

        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        if(engineMode.compareTo("backward")==0 || engineMode.compareTo("hybrid")==0) {
            ResultRow row1 = new ResultRow();
            row1.addField(new ResultField("a", "eg:B"));
            row1.addField(new ResultField("b", "eg:D"));
            resultrows.add(row1);
        }
        ResultList result2 = new ResultList(resultrows);
        /*
        System.out.println("Result");
        result1.print();
        */
        assertTrue(result2.sameResult(result1));
    }
}
