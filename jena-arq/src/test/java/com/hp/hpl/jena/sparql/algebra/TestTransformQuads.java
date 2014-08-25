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

package com.hp.hpl.jena.sparql.algebra;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

//Tests for conversion of algebra forms to quad form. 
public class TestTransformQuads extends BaseTest
{
    // Simple
    @Test public void quads01() { test ("{ GRAPH ?g { ?s ?p ?o } }", 
                                        "(quadpattern (quad ?g ?s ?p ?o))" 
                                        ) ; }
    // Not nested
    @Test public void quads02() { test ("{ GRAPH ?g { ?s ?p ?o } GRAPH ?g1 { ?s1 ?p1 ?o1 }  }", 
                                        "(sequence" +
                                        "    (quadpattern (quad ?g ?s ?p ?o))",
                                        "    (quadpattern (quad ?g1 ?s1 ?p1 ?o1)))"
                                       ) ; }
    
    @Test public void quads03() { test ("{ GRAPH ?g { ?s ?p ?o } GRAPH ?g { ?s1 ?p1 ?o1 }  }",
                                        "(sequence" +
                                        "   (quadpattern (quad ?g ?s ?p ?o))" +
                                        "   (quadpattern (quad ?g ?s1 ?p1 ?o1)))"
                                        ) ; }
    // Nested
    @Test public void quads04() { test ("{ GRAPH ?g { ?s ?p ?o GRAPH ?g1 { ?s1 ?p1 ?o1 }  } }",
                                        "(sequence" +
                                        "   (quadpattern (quad ?g ?s ?p ?o))" +
                                        "   (quadpattern (quad ?g1 ?s1 ?p1 ?o1)))"
                                        ) ; }
    
    @Test public void quads05() { test ("{ GRAPH ?g { ?s ?p ?o GRAPH ?g { ?s1 ?p1 ?o1 }  } }", 
                                        "(assign ((?g ?*g0))" +
                                        "   (sequence" +
                                        "     (quadpattern (quad ?*g0 ?s ?p ?o))" +
                                        "     (quadpattern (quad ?g ?s1 ?p1 ?o1))))" 
                                        ) ; }
    // Filters
    @Test public void quads10() { test ("{ GRAPH ?g { ?s ?p ?o FILTER (str(?g) = 'graphURI') } }", 
                                        "(assign ((?g ?*g0))" +
                                        "   (filter (= (str ?g) 'graphURI')" +
                                        "     (quadpattern (quad ?*g0 ?s ?p ?o))))" 
                                        ) ; }
    
    @Test public void quads11() { test ("{ GRAPH ?g { ?s ?p ?o } FILTER (str(?g) = 'graphURI') }",
                                        "(filter (= (str ?g) 'graphURI')" +
                                        "   (quadpattern (quad ?g ?s ?p ?o)))"
                                        ) ; }
    
    // Nested and filter
    @Test public void quads20() { test ("{ GRAPH ?g { ?s ?p ?o GRAPH ?g1 { ?s1 ?p1 ?o1 FILTER (str(?g) = 'graphURI') } } }",
                                        "(assign ((?g ?*g0))" +
                                        "   (sequence" +
                                        "     (quadpattern (quad ?*g0 ?s ?p ?o))" +
                                        "     (filter (= (str ?g) 'graphURI')" +
                                        "       (quadpattern (quad ?g1 ?s1 ?p1 ?o1)))))"
                                        ) ; }
    
    @Test public void quads21() { test ("{ GRAPH ?g { ?s ?p ?o GRAPH ?g1 { ?s1 ?p1 ?o1 FILTER (str(?g1) = 'graphURI') } } }",
                                        "(sequence" +
                                        "   (quadpattern (quad ?g ?s ?p ?o))" +
                                        "   (assign ((?g1 ?*g0))" +
                                        "     (filter (= (str ?g1) 'graphURI')" +
                                        "       (quadpattern (quad ?*g0 ?s1 ?p1 ?o1)))))"
                                       ) ; }
    
    // Tricky pattern ... twice.
    @Test public void quads30() { test ( "{ GRAPH ?g { ?s ?p ?o FILTER (str(?g) = 'graphURI') } " +
                                         "  GRAPH ?g { ?s ?p ?o FILTER (str(?g) = 'graphURI') } }",
                                         "(sequence" +
                                         "   (assign ((?g ?*g0))" +
                                         "     (filter (= (str ?g) 'graphURI')" +
                                         "       (quadpattern (quad ?*g0 ?s ?p ?o))))" +
                                         "   (assign ((?g ?*g1))" +
                                         "     (filter (= (str ?g) 'graphURI')" +
                                         "       (quadpattern (quad ?*g1 ?s ?p ?o)))))"
                                         ) ; }

    // NOT EXISTS
    @Test public void quads31() { test ( "{ GRAPH ?g { ?s ?p ?o FILTER NOT EXISTS { GRAPH ?g1 { ?s1 ?p ?o1 } } } }",
                                         "(filter (notexists",
                                         "   (quadpattern (quad ?g1 ?s1 ?p ?o1)))",
                                         "  (quadpattern (quad ?g ?s ?p ?o)))"
                                         ) ; }

    // NOT EXISTS
    @Test public void quads32() { test ( "{ ?s ?p ?o FILTER NOT EXISTS { GRAPH ?g1 { ?s1 ?p ?o1 } } }",
                                         "(filter (notexists",
                                         "   (quadpattern (quad ?g1 ?s1 ?p ?o1)))",
                                         "  (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?s ?p ?o)))"
                                         ) ; }
    
    // Regression in toQuadForm() - currently disable as the test fails with an ARQInternalErrorException
    @Test public void quads33() { test ( "{ GRAPH ?g { { SELECT ?x WHERE { ?x ?p ?g } } } }",
                                          "(project (?x)",
                                          "  (quadpattern (quad ?g ?x ?/p ?/g)))") ; }

    // JENA-535
    @Test public void quads34() { test ( "{ ?s ?p ?o OPTIONAL { FILTER NOT EXISTS { ?x ?y ?z } } }",
                                         "(conditional",
                                         "  (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?s ?p ?o))",
                                         "  (filter (notexists",
                                         "             (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?x ?y ?z)))",
                                         "    (table unit)))") ; }
    
    // NOT EXISTS in left join expression. 
    @Test public void quads35() { test ( "{ ?s ?p ?o OPTIONAL { FILTER NOT EXISTS { ?x ?y ?z } } }",
                                         false,
                                         "(leftjoin",
                                         "   (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?s ?p ?o))",
                                         "   (table unit)",
                                         "   (notexists",
                                         "     (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?x ?y ?z))))") ; }
        
    // NOT EXISTS in left join expression. 
    @Test public void quads36() { test ( "{ ?s ?p ?o OPTIONAL { FILTER NOT EXISTS { GRAPH ?g { ?x ?y ?z } } } }",
                                         false,
                                         "(leftjoin",
                                         "   (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?s ?p ?o))",
                                         "   (table unit)",
                                         "   (notexists",
                                         "     (quadpattern (?g ?x ?y ?z))))") ; }
    
    // NOT EXISTS in BIND 
    @Test public void quads37() { test ( "{ BIND ( true && NOT EXISTS { GRAPH ?g { ?x ?y ?z } } AS ?X ) }",
                                         "(extend ((?X (&& true (notexists",
                                         "                         (quadpattern (quad ?g ?x ?y ?z))))))",
                                         "    (table unit))") ; }

        

    private static void test(String patternString, String... strExpected) {
        test(patternString, true, strExpected) ;
    }
    
    
    private static void test(String patternString, boolean optimize, String... strExpected)
    {
        Query q = QueryFactory.create("SELECT * WHERE "+patternString) ;
        Op op = Algebra.compile(q) ;
        if ( optimize )
            op = Algebra.optimize(op) ;
        op = Algebra.toQuadForm(op) ;
        
        Op op2 = SSE.parseOp(StrUtils.strjoinNL(strExpected)) ;
        assertEquals(op2, op) ;
    }
}

