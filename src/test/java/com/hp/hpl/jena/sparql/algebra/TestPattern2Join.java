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
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformPattern2Join ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestPattern2Join extends BaseTest
{
    
    @Test public void bgp2join_01() { test3("{}", 
                                           "(table unit)") ; }
    
    @Test public void bgp2join_02() { test3("{?s ?p ?o}", 
                                           "(triple ?s ?p ?o)") ; }
    
    @Test public void bgp2join_03() { test3("{?s1 ?p1 ?o1 . ?s2 ?p2 ?o2 . }", 
                                           "(join ",
                                           "  (triple ?s1 ?p1 ?o1)",
                                           "  (triple ?s2 ?p2 ?o2) )") ; }
    
    @Test public void bgp2join_04() { test3("{?s1 ?p1 ?o1 . ?s2 ?p2 ?o2 . ?s3 ?p3 ?o3 . }", 
                                           "(join ",
                                           "   (join (triple ?s1 ?p1 ?o1) (triple ?s2 ?p2 ?o2))",
                                           "   (triple ?s3 ?p3 ?o3) )") ; }
    
    @Test public void qp2join_01() { test4("{GRAPH ?g { }}", "(datasetnames ?g)") ; }

    @Test public void qp2join_02() { test4("{GRAPH ?g { ?s ?p ?o  }}", "(quad ?g ?s ?p ?o)") ; }

    @Test public void qp2join_03() { test4("{GRAPH ?g { ?s1 ?p1 ?o1 . ?s2 ?p2 ?o2 .  }}",
                                             "(join ",
                                             "  (quad ?g ?s1 ?p1 ?o1)",
                                             "  (quad ?g ?s2 ?p2 ?o2) )") ; }
    
    @Test public void qp2join_04() { test4("{GRAPH ?g { ?s1 ?p1 ?o1 . ?s2 ?p2 ?o2 . ?s3 ?p3 ?o3 .}}", 
                                           "(join ",
                                           "   (join (quad ?g ?s1 ?p1 ?o1) (quad ?g ?s2 ?p2 ?o2))",
                                           "   (quad ?g ?s3 ?p3 ?o3) )") ; }
    
    @Test public void qp2join_05() { test4("{GRAPH ?g { ?s1 ?p1 ?o1 . ?s2 ?p2 ?o2 . ?s3 ?p3 ?o3 .}}", 
                                           "(join ",
                                           "   (join (quad ?g ?s1 ?p1 ?o1) (quad ?g ?s2 ?p2 ?o2))",
                                           "   (quad ?g ?s3 ?p3 ?o3) )") ; }

    @Test public void qp2join_06() { test4("{GRAPH ?g1 { ?s1 ?p1 ?o1 } GRAPH ?g2 { ?s2 ?p2 ?o2 } }", 
                                           "(join (quad ?g1 ?s1 ?p1 ?o1) (quad ?g2 ?s2 ?p2 ?o2) )") ; }

    
    @Test public void qp2join_07() { test4("{GRAPH ?g1 { ?s1 ?p1 ?o1 . ?s9 ?p9 ?o9} GRAPH ?g2 { ?s2 ?p2 ?o2 . ?s8 ?p8 ?o8}}",     
                                           "(join ",
                                           "   (join (quad ?g1 ?s1 ?p1 ?o1) (quad ?g1 ?s9 ?p9 ?o9))",
                                           "   (join (quad ?g2 ?s2 ?p2 ?o2) (quad ?g2 ?s8 ?p8 ?o8))",
                                           ")") ; }


    private static void test3(String pattern, String... joinForm)
    {
        Query q = QueryFactory.create("PREFIX : <http://example/> SELECT * "+pattern) ;
        Op op = Algebra.compile(q.getQueryPattern()) ;
        test(op, joinForm) ;
    }
    
    private static void test4(String pattern, String... joinForm)
    {
        Query q = QueryFactory.create("PREFIX : <http://example/> SELECT * "+pattern) ;
        Op op = Algebra.compile(q.getQueryPattern()) ;
        op = Algebra.toQuadForm(op) ;
        test(op, joinForm) ;
    }
    
    private static void test(Op input, String... joinForm)
    {
        Op op2 = Transformer.transform(new TransformPattern2Join() , input) ;
        String x = StrUtils.strjoinNL(joinForm) ;
        Op opExpected = SSE.parseOp("(prefix ((: <http://example/>)) "+x+")") ;
        assertEquals(opExpected, op2) ; 
    }
}

