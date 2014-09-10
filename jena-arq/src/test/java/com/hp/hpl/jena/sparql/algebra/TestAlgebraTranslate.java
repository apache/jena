/**
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

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.sse.SSE ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

/** Test translation of syntax to algebra - no otpimization */ 
public class TestAlgebraTranslate extends BaseTest 
{    
    @Test public void translate_01() { test("?s ?p ?o", "(bgp (triple ?s ?p ?o))") ; }
    
    @Test public void translate_02() { test("?s ?p ?o2 . BIND(?v+1 AS ?v1)", 
                                            "(extend [(?v1 (+ ?v 1))]",
                                            "   (bgp [triple ?s ?p ?o2]))"
                                            ) ; }
    
    @Test public void translate_03() { test("?s ?p ?o2 . LET(?v1 := ?v+1) LET(?v2 := ?v+2)",
                                            "(assign ((?v1 (+ ?v 1)) (?v2 (+ ?v 2)))", 
                                            "  (bgp (triple ?s ?p ?o2)))"
                                            ) ; }

    @Test public void translate_04() { test("?s ?p ?o2 . BIND(?v+1 AS ?v1) BIND(?v + 2 AS ?v2)",
                                            // If combining (extend) during generation.
                                            //"(extend ((?v1 (+ ?v 1)) (?v2 (+ ?v 2)))", 
                                            "  (extend ((?v2 (+ ?v 2)))",
                                            "    (extend ((?v1 (+ ?v 1)))",
                                            "      (bgp (triple ?s ?p ?o2))))"
                                            ) ; }
    
    
    @Test public void translate_05() { test("?s ?p ?o2 BIND(?v+1 AS ?v1) LET(?v2 := ?v+2)",
                                            "(assign ((?v2 (+ ?v 2)))", 
                                            "  (extend ((?v1 (+ ?v 1)))", 
                                            "    (bgp (triple ?s ?p ?o2))))"
                                            ) ; }

    @Test public void translate_06() { test("?s ?p ?o2 LET(?v2 := ?v+2) BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))", 
                                            "  (assign ((?v2 (+ ?v 2)))", 
                                            "    (bgp (triple ?s ?p ?o2))))"
                                            ) ; }

    @Test public void translate_07() { test("{ ?s ?p ?o1 . } BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))", 
                                            "  (bgp (triple ?s ?p ?o1)) )" 
                                            ) ; }

    @Test public void translate_08() { test("BIND(5 AS ?v1)", "(extend ((?v1 5)) [table unit])") ; } 

    @Test public void translate_09() { test("{ ?s ?p ?o1 . } ?s ?p ?o2 . BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))",
                                            "  (join",
                                            "    (bgp (triple ?s ?p ?o1))", 
                                            "    (bgp (triple ?s ?p ?o2))))"
                                            ) ; }
    
    @Test public void translate_10() { test("?s ?p ?o2 . ?s ?p ?o3 . BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))",
                                            "   [bgp (triple ?s ?p ?o2) (triple ?s ?p ?o3)])"
                                            ) ; } 
    
    @Test public void translate_11() { test("{ SELECT * {?s ?p ?o2}} BIND(?o+1 AS ?v1)",
                                            "(extend [(?v1 (+ ?o 1))]",
                                            "   (bgp (triple ?s ?p ?o2)))"
                                            ) ; } 
    

    @Test public void translate_20() { test("?s1 ?p ?o . ?s2 ?p ?o OPTIONAL { ?s ?p3 ?o3 . ?s ?p4 ?o4 }",
                                            "(leftjoin",
                                            "  [bgp (?s1 ?p ?o) (?s2 ?p ?o)]",
                                            "  [bgp (?s ?p3 ?o3) (?s ?p4 ?o4)] )") ; }
                                            
    @Test public void translate_21() { test("?s1 ?p ?o . ?s2 ?p ?o BIND (99 AS ?z) OPTIONAL { ?s ?p3 ?o3 . ?s ?p4 ?o4 }",
                                            "(leftjoin",
                                            "  (extend ((?z 99))[bgp (?s1 ?p ?o) (?s2 ?p ?o)])",
                                            "  [bgp (?s ?p3 ?o3) (?s ?p4 ?o4)] )") ; }
                                            
    @Test public void translate_22() { test("BIND (99 AS ?z) OPTIONAL { ?s ?p3 ?o3 . ?s ?p4 ?o4 }",
                                            "(leftjoin",
                                            "  (extend ((?z 99))[table unit])",
                                            "  [bgp (?s ?p3 ?o3) (?s ?p4 ?o4)] )") ; }

    @Test public void translate_23() { test("OPTIONAL { BIND (99 AS ?z)}",
                                            "(leftjoin",
                                            "  [table unit]",
                                            "  [extend ((?z 99)) (table unit)] )" ) ; }
    
    protected AlgebraGenerator getGenerator() {
        return new AlgebraGenerator();
    }
    
    // Helper.  Prints the test result (check it!)
    protected void test(String qs)
    {
        qs = "SELECT * {\n"+qs+"\n}" ;
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op opActual = this.getGenerator().compile(query) ;
        String x = opActual.toString() ;
        x = x.replaceAll("\n$", "") ;
        x = x.replace("\n", "\", \n\"") ;
        System.out.print('"') ;
        System.out.print(x) ;
        System.out.println('"') ;
        System.out.println() ;
    }
    
    
    protected void test(String qs, String... y)
    {
        qs = "SELECT * {\n"+qs+"\n}" ;
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;

        String opStr = StrUtils.strjoinNL(y) ;
        Op opExpected = SSE.parseOp(opStr) ;
        Op opActual = this.getGenerator().compile(query) ;
        assertEquals(opExpected, opActual) ;
    }

}

