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

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.rulesys.impl.ResultList;
import com.hp.hpl.jena.reasoner.rulesys.impl.ResultRow;
import java.util.ArrayList;
import static junit.framework.TestCase.assertTrue;




public class SparqlinRulesTest2 {
    Model m;
    InfModel inf;
    
    String engineMode;
    
    final int nTests = 15;
    
    static final String myData1 = 
            "<eg:A> <eg:p> <eg:B> .\n" +
            "<eg:B> <eg:p> <eg:C> .\n" +
            "<eg:D> <eg:p> <eg:E> .\n" +
            "<eg:C> <eg:p> <eg:D> .";
        
        
    static final String myData2 = 
            "<eg:A> <eg:p> <eg:B> .\n" +
            "<eg:B> <eg:p> <eg:C> .\n" +
            "<eg:C> <eg:p> <eg:D> . \n" + 
            "<eg:C> <eg:p> <eg:C1> . \n" + 
            "<eg:D> <eg:p> <eg:D1> . "; 

    static final String myData3 = 
                "<http://www.example.com/A> <http://www.example.com/p> <http://www.example.com/B> .\n" +
                "<http://www.example.com/B> <http://www.example.com/p> <http://www.example.com/C> .\n" +
                "<http://www.example.com/D> <http://www.example.com/p> <http://www.example.com/E> .\n" +
                "<http://www.example.com/C> <http://www.example.com/p> <http://www.example.com/D> .";

        
    static final String myRule11 = 
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. \n " +
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). \n";

    static final String myRule12 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). \n";

    static final String myRule13 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n";
        
    static final String myRule21 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p1> ?z) -> (?x <eg:p2> ?z). ";
        
        
    static final String myRule22 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p1> ?y) (?y <eg:p> ?z)  -> (?x <eg:p2> ?z). ";
       
    static final String myRule31 = 
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. \n " +
                "(\\\\\\sparql Select ?x ?y where {?x <eg:p2> ?y .} \\\\\\sparql) -> (?x <eg:p1> ?y). \n " + 
                "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). \n";

    static final String myRule32 = 
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p1> ?y) -> (?x <eg:p2> ?y). \n";
        
        
    static final String myRule41 = 
                " -> table(<eg:p2>). \n " +
                "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p1> ?y) -> (?x <eg:p2> ?y). \n";

    static final String myRule42 = 
                " -> table(<eg:p2>). \n " +
                " -> table(<eg:p1>). \n " +
                  "(\\\\\\sparql Select ?x ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p1> ?z). \n " + 
                "(?x <eg:p1> ?y) -> (?x <eg:p2> ?y). \n";
       
       
    static final String myRule51 = 
                "@prefix p: <http://www.example.com/>.  \n " +
                "(\\\\\\sparql Select ?x ?z where {?x p:p ?y . ?y p:p ?z .} \\\\\\sparql) -> (?x p:p1 ?z). \n " + 
                "(?x p:p ?y) (?y p:p ?z) -> (?x p:p2 ?z). \n";

    static final String myRule52 = 
                "@prefix p: <http://www.example.com/>.  \n " +
                "(\\\\\\sparql prefix p: <http://www.example.com/>  Select ?x ?z where {?x p:p ?y . ?y p:p ?z .} \\\\\\sparql) -> (?x p:p1 ?z). \n " + 
                "(?x p:p ?y) (?y p:p ?z) -> (?x p:p2 ?z). \n";

    static final String myRule53 = 
                "(\\\\\\sparql prefix p: <http://www.example.com/> Select ?x ?z where {?x p:p ?y . ?y p:p ?z .} \\\\\\sparql) -> (?x <http://www.example.com/p1> ?z). \n " + 
                "(?x <http://www.example.com/p> ?y) (?y <http://www.example.com/p> ?z) -> (?x <http://www.example.com/p2> ?z). \n";

        
    static final String myRule55 = 
                "(\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <http://www.example.com/p1> ?z). \n " + 
                "(?x <http://www.example.com/p> ?y) (?y <http://www.example.com/p> ?z) -> (?x <http://www.example.com/p2> ?z). \n";
        
    static final String myRule54 = 
                "@prefix p: <http://www.example.com/>.  \n " +
                "(\\\\\\sparql prefix p: <http://www.example.com/>  Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <http://www.example.com/p1> ?z). \n " + 
                "(?x p:p ?y) (?y p:p ?z) -> (?x p:p2 ?z). \n";
        
    static final String myRule61 = 
                "(\\\\\\sparql ask {<eg:A> <eg:p> ?y . ?y <eg:p> <eg:C> .} \\\\\\sparql) -> (<eg:A> <eg:p1> <eg:C>). \n " + 
                "(<eg:B> <eg:p2> <eg:D>) <- (\\\\\\sparql ask {<eg:B> <eg:p> ?y . ?y <eg:p> <eg:D> .} \\\\\\sparql) . \n" +
                "(\\\\\\sparql ask {<eg:A> <eg:p> ?y . ?y <eg:p> <eg:B> .} \\\\\\sparql) -> (<eg:A> <eg:p1> <eg:B>). \n " + 
                "(<eg:B> <eg:p2> <eg:C>) <- (\\\\\\sparql ask {<eg:B> <eg:p> ?y . ?y <eg:p> <eg:C> .} \\\\\\sparql) . \n";


    static final String [] testData = {
        "<eg:A> <eg:p> <eg:B> .\n" + //data1
        "<eg:B> <eg:p> <eg:C> .\n" +
        "<eg:D> <eg:p> <eg:E> .\n" +
        "<eg:C> <eg:p> <eg:D> ."   +
        "<eg:p1> <eg:type> <eg:gen> .\n" +
        "<eg:p2> <eg:type> <eg:gen> .\n" +
        "<eg:p3> <eg:type> <eg:gen> .\n" +
        "<eg:p4> <eg:type> <eg:gen> .\n",             
        "<eg:A> <eg:p> <eg:B> .\n" + //data2
        "<eg:A> <eg:p> <eg:C> .\n" +
        "<eg:A> <eg:p> <eg:D> .\n" +
        "<eg:B> <eg:p> <eg:E> .\n" +
        "<eg:B> <eg:p> <eg:F> .\n" +
        "<eg:C> <eg:p> <eg:G> .\n" +
        "<eg:C> <eg:p> <eg:H> .\n" +
        "<eg:D> <eg:p> <eg:I> .\n" +
        "<eg:D> <eg:p> <eg:J> .\n",
        "<eg:A> <eg:p> <eg:B> .\n" + //data3
        "<eg:A> <eg:p> <eg:C> .\n" +
        "<eg:A> <eg:p> <eg:D> .\n" +
        "<eg:B> <eg:p> <eg:E> .\n" +
        "<eg:B> <eg:p> <eg:F> .\n" +
        "<eg:C> <eg:p> <eg:G> .\n" +
        "<eg:C> <eg:p> <eg:H> .\n" +
        "<eg:D> <eg:p> <eg:I> .\n" +
        "<eg:D> <eg:p> <eg:J> .\n" +
        "<eg:E> <eg:p> <eg:D> .\n" +
        "<eg:G> <eg:p> <eg:D> .\n" +
        "<eg:A> <eg:p5> <eg:E> .\n" +
        "<eg:C> <eg:p5> <eg:D> .\n" +            
        "<eg:A> <eg:v> 1 .\n" +
        "<eg:B> <eg:v> 0 .\n" +
        "<eg:C> <eg:v> 1 .\n" +
        "<eg:D> <eg:v> 1 .\n" +
        "<eg:E> <eg:v> 0 .\n" +
        "<eg:F> <eg:v> 1 .\n" +
        "<eg:G> <eg:v> 0 .\n" +
        "<eg:H> <eg:v> 1 .\n" +
        "<eg:I> <eg:v> 1 .\n" +
        "<eg:J> <eg:v> 0 .\n" +
        "<eg:p1> <eg:type> <eg:gen> .\n" +
        "<eg:p2> <eg:type> <eg:gen> .\n" +
        "<eg:p3> <eg:type> <eg:gen> .\n" +
        "<eg:p4> <eg:type> <eg:gen> .\n" +
        "<eg:p2> <eg:type> <eg:gen1> .\n" +
        "<eg:p4> <eg:type> <eg:gen1> .\n" +    
        "<eg:p4> <eg:type> <eg:gen2> .\n",         
        "<eg:A> <eg:p> <eg:B> .\n" + //data4
        "<eg:A> <eg:p> <eg:B> .\n" +    
        "<eg:B> <eg:p> <eg:C> .\n" +
        "<eg:D> <eg:p> <eg:E> .\n",
        "<eg:A> <eg:p> <eg:B> .\n" + //data5
        "<eg:A> <eg:p> <eg:C> .\n" +    
        "<eg:A> <eg:p> <eg:D> .\n" +
        "<eg:B> <eg:p> <eg:E> .\n" +
        "<eg:C> <eg:p> <eg:E> .\n" +
        "<eg:D> <eg:p> <eg:E> .\n",   
        "<eg:Miguel> <eg:age> 19 .\n" + //data6
        "<eg:Ricardo> <eg:age> 17 .",
        "<http://www.example.com/A> <http://www.example.com/p> <http://www.example.com/B> .\n" + //data7
        "<http://www.example.com/A> <http://www.example.com/p> <http://www.example.com/C> .\n" +
        "<http://www.example.com/A> <http://www.example.com/p> <http://www.example.com/D> .\n" +
        "<http://www.example.com/B> <http://www.example.com/p> <http://www.example.com/E> .\n" +
        "<http://www.example.com/B> <http://www.example.com/p> <http://www.example.com/F> .\n" +
        "<http://www.example.com/C> <http://www.example.com/p> <http://www.example.com/G> .\n" +
        "<http://www.example.com/C> <http://www.example.com/p> <http://www.example.com/H> .\n" +
        "<http://www.example.com/D> <http://www.example.com/p> <http://www.example.com/I> .\n" +
        "<http://www.example.com/D> <http://www.example.com/p> <http://www.example.com/J> .\n" +
        "<http://www.example.com/E> <http://www.example.com/p> <http://www.example.com/D> .\n" +
        "<http://www.example.com/G> <http://www.example.com/p> <http://www.example.com/D> .\n" +
        "<http://www.example.com/A> <http://www.example.com/v> 1 .\n" +
        "<http://www.example.com/B> <http://www.example.com/v> 0 .\n" +
        "<http://www.example.com/C> <http://www.example.com/v> 1 .\n" +
        "<http://www.example.com/D> <http://www.example.com/v> 1 .\n" +
        "<http://www.example.com/E> <http://www.example.com/v> 0 .\n" +
        "<http://www.example.com/F> <http://www.example.com/v> 1 .\n" +
        "<http://www.example.com/G> <http://www.example.com/v> 0 .\n" +
        "<http://www.example.com/H> <http://www.example.com/v> 1 .\n" +
        "<http://www.example.com/I> <http://www.example.com/v> 1 .\n" +
        "<http://www.example.com/J> <http://www.example.com/v> 0 .\n" +
        "<http://www.example.com/p1> <http://www.example.com/type> <http://www.example.com/gen> .\n" +
        "<http://www.example.com/p2> <http://www.example.com/type> <http://www.example.com/gen> .\n" +
        "<http://www.example.com/p3> <http://www.example.com/type> <http://www.example.com/gen> .\n" +
        "<http://www.example.com/p4> <http://www.example.com/type> <http://www.example.com/gen> .\n", 
    };
    
    static final String [] myRules1 = {
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <eg:p2> ?z). ",
        "(?a <eg:p> ?b) (?c <eg:p> ?d) (?a <eg:p> ?d) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?a <eg:p2> ?d). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?x) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <eg:p2> ?y). ",
        "(<eg:A> <eg:p> <eg:B>) (<eg:B> <eg:p> <eg:C>) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (<eg:A> <eg:p2> <eg:C>). ",
        "(<eg:A> <eg:p> <eg:B>) (<eg:B> <eg:p> <eg:A>) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (<eg:A> <eg:p2> <eg:C>). ",
        "(?x <eg:age> ?a) ge(?a, 18) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <eg:is> <eg:Adult>). ",
        "(?x <eg:age> ?a) sum(?a, 3, ?b) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql)  -> (?x <eg:agePlus3> ?b). ",  
        "(?x <eg:age> ?a) sum(?a, 2, ?b) (?y <eg:age> ?b) (\\\\\\sparql Select ?x ?z where {?x <http://www.example.com/p> ?y . ?y <http://www.example.com/p> ?z .} \\\\\\sparql) -> (?x <eg:agePlus2> ?y). ",
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?z where {&y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p2> ?z). ",
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?y ?z where {?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p2> ?z). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (\\\\\\sparql Select ?z ?w where {?z <eg:p> ?w . } \\\\\\sparql) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?y ?z where {?y <eg:p> ?z . } \\\\\\sparql) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ",        
        "(\\\\\\sparql Select ?x ?y where {?x <eg:p> ?y . } \\\\\\sparql) (?y <eg:p> ?z) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (\\\\\\sparql Select ?w where {&z <eg:p> ?w . } \\\\\\sparql) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?z where {&y <eg:p> ?z . } \\\\\\sparql) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ",
        "(\\\\\\sparql Select ?x ?y where {?x <eg:p> ?y . } \\\\\\sparql) (\\\\\\sparql Select ?y ?z where {?y <eg:p> ?z . } \\\\\\sparql) (\\\\\\sparql Select ?z ?w where {?z <eg:p> ?w . } \\\\\\sparql) -> (?x <eg:p2> ?w). ",
        "(\\\\\\sparql Select ?x ?y where {?x <eg:p> ?y . } \\\\\\sparql) (\\\\\\sparql Select ?z where {&y <eg:p> ?z . } \\\\\\sparql) (\\\\\\sparql Select ?w where {&z <eg:p> ?w . } \\\\\\sparql) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:v> ?v) greaterThan(?v, 0) (\\\\\\sparql Select ?y ?z where {?y <eg:p> ?z . } \\\\\\sparql) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:v> ?v) greaterThan(?v, 0) (\\\\\\sparql Select ?z where {&y <eg:p> ?z .} \\\\\\sparql) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ",       
        "(?x <eg:p> ?y) (?y <eg:v> ?v) greaterThan(?v, 0) (?y <eg:p> ?z) (\\\\\\sparql Select ?z ?w where {?z <eg:p> ?w . } \\\\\\sparql) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:v> ?v) greaterThan(?v, 0) (?y <eg:p> ?z) (\\\\\\sparql Select ?w where {&z <eg:p> ?w . } \\\\\\sparql)  -> (?x <eg:p2> ?w). ",        
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?y ?z where {?y <eg:p> ?z .} \\\\\\sparql) (?z <eg:v> ?v) greaterThan(?v, 0) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ", 
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?z where {&y <eg:p> ?z .} \\\\\\sparql) (?z <eg:v> ?v) greaterThan(?v, 0) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ", 
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (?z <eg:v> ?v) greaterThan(?v, 0) (\\\\\\sparql Select ?z ?w where {?z <eg:p> ?w . } \\\\\\sparql) -> (?x <eg:p2> ?w). ", 
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (?z <eg:v> ?v) greaterThan(?v, 0) (\\\\\\sparql Select ?w where {&z <eg:p> ?w . } \\\\\\sparql) -> (?x <eg:p2> ?w). ", 
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (\\\\\\sparql Select ?z ?w where {?z <eg:p> ?w . } \\\\\\sparql) (?w <eg:v> ?v) greaterThan(?v, 0) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (\\\\\\sparql Select ?w where {&z <eg:p> ?w . } \\\\\\sparql) (?w <eg:v> ?v) greaterThan(?v, 0) -> (?x <eg:p2> ?w). ",
        "@prefix eg: <http://www.example.com/> . \n (?x eg:p ?y) (?y eg:p ?z) (\\\\\\sparql Select ?z ?w where {?z eg:p ?w . } \\\\\\sparql) (?w eg:v ?v) greaterThan(?v, 0) -> (?x eg:p2 ?w). ",
        "@prefix eg: <http://www.example.com/> . \n (?x eg:p ?y) (?y eg:p ?z) (\\\\\\sparql Select ?w where {&z eg:p ?w . } \\\\\\sparql) (?w eg:v ?v) greaterThan(?v, 0) -> (?x eg:p2 ?w). ",
        "(?x <http://www.example.com/p> ?y) (?y <http://www.example.com/p> ?z) (\\\\\\sparql prefix eg:<http://www.example.com/> Select ?z ?w where {?z eg:p ?w . } \\\\\\sparql) (?w <http://www.example.com/v> ?v) greaterThan(?v, 0) -> (?x <http://www.example.com/p2> ?w). ",
        "(?x <http://www.example.com/p> ?y) (?y <http://www.example.com/p> ?z) (\\\\\\sparql prefix eg:<http://www.example.com/> Select ?w where {&z eg:p ?w . } \\\\\\sparql) (?w <http://www.example.com/v> ?v) greaterThan(?v, 0) -> (?x <http://www.example.com/p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?w) (\\\\\\sparql Select ?w ?z where {?w <eg:p> ?z . } \\\\\\sparql) -> (?x <eg:p2> ?z). \n" +
        "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p1> ?z). ",
    };

    
    static final String [] myRules2 = {
        "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). ",
        "(?a <eg:p> ?b) (?c <eg:p> ?d) (?a <eg:p> ?d) -> (?a <eg:p2> ?d). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?x) -> (?x <eg:p2> ?y). ",
        "(<eg:A> <eg:p> <eg:B>) (<eg:B> <eg:p> <eg:C>) -> (<eg:A> <eg:p2> <eg:C>). ",
        "(<eg:A> <eg:p> <eg:B>) (<eg:B> <eg:p> <eg:A>) -> (<eg:A> <eg:p2> <eg:C>). ",
        "(?x <eg:age> ?a) ge(?a, 18) -> (?x <eg:is> <eg:Adult>). ",
        "(?x <eg:age> ?a) sum(?a, 3, ?b) -> (?x <eg:agePlus3> ?b). ", 
        "(?x <eg:age> ?a) sum(?a, 2, ?b) (?y <eg:age> ?b) -> (?x <eg:agePlus2> ?y). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:v> ?v) greaterThan(?v, 0) (?y <eg:p> ?z) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (?z <eg:v> ?v) greaterThan(?v, 0) (?z <eg:p> ?w) -> (?x <eg:p2> ?w). ", 
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (?z <eg:p> ?w) (?w <eg:v> ?v) greaterThan(?v, 0) -> (?x <eg:p2> ?w). ", 
        //"(?x <http://www.example.com/p> ?y) (?y <http://www.example.com/p> ?z) (?z <http://www.example.com/p> ?w) (?w <http://www.example.com/v> ?v) greaterThan(?v, 0) -> (?x <http://www.example.com/p2> ?w). "
        "@prefix eg: <http://www.example.com/> . \n (?x eg:p ?y) (?y eg:p ?z) (?z eg:p ?w) (?w eg:v ?v) greaterThan(?v, 0) -> (?x eg:p2 ?w). ",
	"(?x <eg:p1> ?w) (\\\\\\sparql Select ?w ?z where {?w <eg:p> ?z . } \\\\\\sparql) -> (?x <eg:p2> ?z). \n" +
        "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p1> ?z). \n "+
        "(?x <eg:p> ?y) (?y <eg:p> ?w) (?w <eg:p> ?z) -> (?x <eg:p4> ?z). ",
	"(?x <eg:p> ?w) (\\\\\\sparql Select ?w ?z where {?w <eg:p> ?z . } \\\\\\sparql) -> (?x <eg:p1> ?z). \n" +
        "(?x <eg:p1> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> ?z). \n "+
        "(?x <eg:p> ?y) (?y <eg:p> ?w) (?w <eg:p> ?z) -> (?x <eg:p4> ?z). ",
	"(?x <eg:p> ?w) (\\\\\\sparql Select ?w ?z where {?w <eg:p1> ?z . } \\\\\\sparql) -> (?x <eg:p2> ?z). \n" +
        "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p1> ?z). \n "+
        "(?x <eg:p> ?y) (?y <eg:p> ?w) (?w <eg:p> ?z) -> (?x <eg:p4> ?z). ", 
	"(?x <eg:p> ?w) (\\\\\\sparql Select ?w ?z where {?w <eg:p1> ?z . } \\\\\\sparql) -> (?x <eg:p2> ?z). \n" +
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?y ?z where {?y <eg:p> ?z . } \\\\\\sparql) -> (?x <eg:p1> ?z). \n "+
        "(?x <eg:p> ?y) (?y <eg:p> ?w) (?w <eg:p> ?z) -> (?x <eg:p4> ?z). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) -> (?x <eg:p2> pair(?y, ?z)) . \n"+
        "(?x <eg:p2> pair(?y, ?z)) (?z <eg:p> ?w) -> (?x <eg:p4> ?w). ",
        "(\\\\\\sparql Select ?x ?y ?z where {?x <eg:p> ?y . ?y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p2> pair(?y, ?z)) . \n"+
        "(?x <eg:p2> pair(?y, ?z)) (?z <eg:p> ?w) -> (?x <eg:p4> ?w). ",
        "(?x <eg:p> ?y) (\\\\\\sparql Select ?z where {&y <eg:p> ?z .} \\\\\\sparql) -> (?x <eg:p2> pair(?y, ?z)) . \n"+
        "(?x <eg:p2> pair(?y, ?z)) (?z <eg:p> ?w) -> (?x <eg:p4> ?w). ",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (\\\\\\sparql ask {&x <eg:p5> &z .} \\\\\\sparql) -> (?x <eg:p4> ?z) . \n",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (?x <eg:p5> ?z) -> (?x <eg:p4> ?z) . \n",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (\\\\\\sparql ask {&x <eg:p5> &z .} \\\\\\sparql) (?z <eg:p> ?w) -> (?x <eg:p4> ?w) . \n",
        "(?x <eg:p> ?y) (?y <eg:p> ?z) (?x <eg:p5> ?z) (?z <eg:p> ?w) -> (?x <eg:p4> ?w) . \n"
    };
    
    static final String [] sparqlCmd = {
        "Select ?x ?z where {?x <eg:p2> ?z} ",
        "Select ?a ?d where {?a <eg:p2> ?d} ",
        "Select ?x where {?x <eg:is> <eg:Adult>} ",
        "Select ?x ?z where {?x <eg:agePlus3> ?z} ",
        "Select ?x ?z where {?x <eg:agePlus2> ?z} ",
        "Select ?x ?z where {?x <http://www.example.com/p2> ?z} ",
        "Select ?x ?p ?z where {?x ?p ?z . ?p <eg:type> <eg:gen1> .} ",
        "Select ?x ?z where {?x ?p ?z . ?p <eg:type> <eg:gen1> .} ", 
        "Select ?x ?z where {?x ?p ?z . ?p <eg:type> <eg:gen2> .} "
    };
    
 
    static final int [] [] testPairs = {
        {0, 0, 8, 0, 0, 0},
        {0, 0, 9, 0, 0, 0},
        {0, 1, 8, 0, 1, 0},
        {0, 2, 8, 0, 2, 0},
        {0, 3, 8, 0, 3, 0},
        {0, 4, 8, 0, 4, 0},
        {0, 2, 10, 0, 2, 8},
        {0, 2, 11, 0, 2, 8},
        {0, 2, 12, 0, 2, 8},
        {0, 2, 13, 0, 2, 8},
        {0, 2, 14, 0, 2, 8},
        {0, 2, 15, 0, 2, 8},
        {0, 2, 16, 0, 2, 8},
        {0, 2, 17, 0, 2, 9},
        {0, 2, 18, 0, 2, 9},
        {0, 2, 19, 0, 2, 9},
        {0, 2, 20, 0, 2, 9},
        {0, 2, 21, 0, 2, 10},
        {0, 2, 22, 0, 2, 10},
        {0, 2, 23, 0, 2, 10},
        {0, 2, 24, 0, 2, 10},
        {0, 2, 25, 0, 2, 11},
        {0, 2, 26, 0, 2, 11},
        {5, 6, 27, 5, 6, 12},
        {5, 6, 28, 5, 6, 12},
        {5, 6, 29, 5, 6, 12},
        {5, 6, 30, 5, 6, 12},           
        {7, 2, 13, 8, 2, 13},
        {7, 2, 14, 8, 2, 14},
        {7, 2, 13, 8, 2, 15},
        {7, 2, 14, 8, 2, 16}
    };    
 
    static final int [] [] testPairs2 = {
        {6, 2, 18, 6, 2, 17, 0},
        {6, 2, 19, 6, 2, 17, 0},
        {6, 2, 20, 6, 2, 21, 1},
        {6, 2, 22, 6, 2, 23, 1}
    };

    
    
    public SparqlinRulesTest2(String engineMode) {
        
        this.engineMode = engineMode;

    }
    
    
    
    public void run(String msg) {
        int nTests1 = nTests + testPairs.length + testPairs2.length;
        System.out.println(msg + " - test 1/" + nTests1);
        sparqlInRule_01();
        System.out.println(msg + " - test 2/" + nTests1);
        sparqlInRule_02();
        System.out.println(msg + " - test 3/" + nTests1);
        sparqlInRule_03();
        System.out.println(msg + " - test 4/" + nTests1);
        sparqlInRule_04();
        System.out.println(msg + " - test 5/" + nTests1);
        sparqlInRule_05();
        System.out.println(msg + " - test 6/" + nTests1);
        sparqlInRule_06();
        System.out.println(msg + " - test 7/" + nTests1);
        sparqlInRule_07();
        System.out.println(msg + " - test 8/" + nTests1);
        sparqlInRule_08();
        System.out.println(msg + " - test 9/" + nTests1);
        sparqlInRule_09();
        System.out.println(msg + " - test 10/" + nTests1);
        sparqlInRule_10();
        System.out.println(msg + " - test 11/" + nTests1);
        sparqlInRule_11();
        System.out.println(msg + " - test 12/" + nTests1);
        sparqlInRule_12();
        System.out.println(msg + " - test 13/" + nTests1);
        sparqlInRule_13();
        System.out.println(msg + " - test 14/" + nTests1);
        sparqlInRule_14();
        System.out.println(msg + " - test 15/" + nTests1);
        sparqlInRule_15();
        sparqlInRule_16(msg);
        sparqlInRule_17(msg);
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
        sparqlInRule_16(null);
        sparqlInRule_17(null);
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
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
    }
    
    public void sparqlInRule_02() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule11, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule11, engineMode);
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
    }
    
    public void sparqlInRule_03() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule12, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule12, engineMode);
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
    }
    
    public void sparqlInRule_04() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule13, engineMode);

        
        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addResult("a", NodeFactory.createURI("eg:A"));
        row1.addResult("b", NodeFactory.createURI("eg:C"));
        resultrows.add(row1);
                
        ResultRow row2 = new ResultRow();
        row2.addResult("a", NodeFactory.createURI("eg:B"));
        row2.addResult("b", NodeFactory.createURI("eg:D"));
        resultrows.add(row2);

        ResultRow row3 = new ResultRow();
        row3.addResult("a", NodeFactory.createURI("eg:C"));
        row3.addResult("b", NodeFactory.createURI("eg:E"));
        resultrows.add(row3);
        
        ResultList result2 = new ResultList(resultrows);

        
        assertTrue(result2.sameResult_notEmpty(result1));
    }
    
    
    public void sparqlInRule_05() {
        String lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule21, engineMode);

        
        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addResult("a", NodeFactory.createURI("eg:A"));
        row1.addResult("b", NodeFactory.createURI("eg:D"));
        resultrows.add(row1);
                
        ResultRow row2 = new ResultRow();
        row2.addResult("a", NodeFactory.createURI("eg:B"));
        row2.addResult("b", NodeFactory.createURI("eg:D1"));
        resultrows.add(row2);

        ResultRow row3 = new ResultRow();
        row3.addResult("a", NodeFactory.createURI("eg:A"));
        row3.addResult("b", NodeFactory.createURI("eg:C1"));
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
        
        assertTrue(result2.sameResult_notEmpty(result1));
        //assertTrue2(result1, result2);
    }
    

    public void sparqlInRule_06() {
        String lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule22, engineMode);
        

        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addResult("a", NodeFactory.createURI("eg:A"));
        row1.addResult("b", NodeFactory.createURI("eg:D"));
        resultrows.add(row1);
                
        ResultRow row2 = new ResultRow();
        row2.addResult("a", NodeFactory.createURI("eg:A"));
        row2.addResult("b", NodeFactory.createURI("eg:C1"));
        resultrows.add(row2);
        
        ResultRow row3 = new ResultRow();
        row3.addResult("a", NodeFactory.createURI("eg:B"));
        row3.addResult("b", NodeFactory.createURI("eg:D1"));
        resultrows.add(row3);

        ResultList result2 = new ResultList(resultrows);
        
        /*
        System.out.println("Result 1");
        result1.print();
        
        System.out.println("Result 2");
        result2.print();
        */
        
        assertTrue(result2.sameResult_notEmpty(result1));
        //assertTrue2(result1, result2);
    }

    public void sparqlInRule_07() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule32, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData2, myRule32, engineMode);
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
    }
    
    public void sparqlInRule_08() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData2, myRule31, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData2, myRule32, engineMode);
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
    }

    public void sparqlInRule_09() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule41, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule41, engineMode);
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
    }
         
    public void sparqlInRule_10() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule42, engineMode);
        
        lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r2 = TestsGenericClass.executeSparql(lSparql, myData1, myRule42, engineMode);
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
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
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
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
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
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
        
        
        assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
    }
     
  
    public void sparqlInRule_14() {
        String lSparql = "Select ?a ?b where {?a <eg:p1> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule61, engineMode);
        

        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        ResultRow row1 = new ResultRow();
        row1.addResult("a", NodeFactory.createURI("eg:A"));
        row1.addResult("b", NodeFactory.createURI("eg:C"));
        resultrows.add(row1);
   
        ResultList result2 = new ResultList(resultrows);
        
        assertTrue(result2.sameResult_notEmpty(result1));
        //assertTrue2(result1, result2);
    }
   
    public void sparqlInRule_15() {
        String lSparql = "Select ?a ?b where {?a <eg:p2> ?b}";
        ResultSet r1 = TestsGenericClass.executeSparql(lSparql, myData1, myRule61, engineMode);
        

        ResultList result1 = (new ConvertResult()).getresult(r1);
       
        ArrayList<ResultRow> resultrows = new ArrayList<>();
        
        if(engineMode.compareTo("backward")==0 || engineMode.compareTo("hybrid")==0) {
            ResultRow row1 = new ResultRow();
            row1.addResult("a", NodeFactory.createURI("eg:B"));
            row1.addResult("b", NodeFactory.createURI("eg:D"));
            resultrows.add(row1);
        }
        ResultList result2 = new ResultList(resultrows);
        /*
        System.out.println("Result");
        result1.print();
        */
        //assertTrue(result2.sameResult_notEmpty(result1));
        assertTrue(result2.sameResult(result1));
    }

    public void sparqlInRule_16(String msg) {
        for(int i=0; i<testPairs.length; i++) {
            ResultSet r1 = TestsGenericClass.executeSparql(sparqlCmd[testPairs[i][0]], testData[testPairs[i][1]], myRules1[testPairs[i][2]], engineMode);
            ResultSet r2 = TestsGenericClass.executeSparql(sparqlCmd[testPairs[i][3]], testData[testPairs[i][4]], myRules2[testPairs[i][5]], engineMode);
            if(msg!=null) {
                System.out.println(msg + " - test  "+(i+nTests+1)+"/"+ (nTests+testPairs.length+testPairs2.length));
            }
            assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
        }
        
    
    }    
    
    public void sparqlInRule_17(String msg) {
        for(int i=0; i<testPairs2.length; i++) {
            if(testPairs2[i][6] == 1 || this.engineMode!="backward"){

                ResultSet r1 = TestsGenericClass.executeSparql(sparqlCmd[testPairs2[i][0]], testData[testPairs2[i][1]], myRules2[testPairs2[i][2]], engineMode);
                ResultSet r2 = TestsGenericClass.executeSparql(sparqlCmd[testPairs2[i][3]], testData[testPairs2[i][4]], myRules2[testPairs2[i][5]], engineMode);
                
                if(msg!=null) {
                    System.out.println(msg + " - test  "+(i+testPairs.length+nTests+1)+"/"+ (nTests+testPairs.length+testPairs2.length));
                }    
                assertTrue(CompareResults.sameResult_notEmpty(r1, r2));
            } 
            else {
                if(msg!=null) {
                    System.out.println(msg + " - test - this test cannot be executed in backward mode -  "+(i+nTests+1)+"/"+ (nTests+testPairs.length+testPairs2.length));
                }
            }
        }
        
    
    }
    
    /*
    private void assertTrue2(ResultList result1, ResultList result2) {
        if(!result2.sameResult_notEmpty(result1)) {
            System.out.println("Result 1");
            result1.print();
            System.out.println("Result 2");
            result2.print();
        }
        assertTrue(result2.sameResult_notEmpty(result1));
    }
    */
}
