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

package com.hp.hpl.jena.sparql.algebra.optimize ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.optimize.AbstractTestTransform ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformTopN ;

public class TestTransformTopN  extends AbstractTestTransform {
    public TestTransformTopN() {}

    @Test public void topN_01() {
        String input = StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (order (?z)"
             ,"    (bgp (triple ?s ?p ?z)) ))"
                );
        String output = StrUtils.strjoinNL
            ("(top (5 ?z)"
             ,"  (bgp (triple ?s ?p ?z)))"
                );
        test(input, output) ;
    }
    @Test public void topN_02() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (project (?z)"
             ,"    (order (?z)"
             ,"      (bgp (triple ?s ?p ?z)) )))"
                );
        String output = StrUtils.strjoinNL
            ("(project (?z)"
            ,"  (top (5 ?z)"
            ,"    (bgp (triple ?s ?p ?z)) ))"
                );
        test(input, output) ;
    }

    @Test public void topN_03() {
        String input = StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (project (?s ?z)"
             ,"    (order (?z ?s)"
             ,"      (bgp (triple ?s ?p ?z)) )))"
                );
        String output = StrUtils.strjoinNL
            ("(project (?s ?z)"
            ,"  (top (5 ?z ?s)"
            ,"    (bgp (triple ?s ?p ?z)) ))"
                );
        test(input, output) ;
    }
    
    @Test public void topN_04() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (project (?s ?z)"
             ,"    (order (?z)"
             ,"      (bgp (triple ?s ?p ?z)) )))"

                );

        String output = StrUtils.strjoinNL
            ("(project (?s ?z)"
            ,"  (top (5 ?z)"
            ,"    (bgp (triple ?s ?p ?z))))"

                );
        test(input, output) ;
    }
    
    @Test public void topN_05() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ," (project (?z)"
             ,"   (order (?z ?s)"
             ,"     (bgp (triple ?s ?p ?z)) )))"
                );

        String output = StrUtils.strjoinNL
            ("(project (?z)"
            ,"  (top (5 ?z ?s)"
            ,"    (bgp (triple ?s ?p ?z)) ))"
                );

        // Order being wider than project blocks the optimization.  
        test(input, output) ;
    }
    
    @Test public void topN_06() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (project (?s ?p)"
             ,"    (order (?z ?s)"
             ,"      (bgp (triple ?s ?p ?z)) )))"
                );
        String output = StrUtils.strjoinNL
            ("(project (?s ?p)"
            ,"  (top (5 ?z ?s)"
            ,"    (bgp (triple ?s ?p ?z)) ))"
                );

        test(input, output) ;
    }
    
    @Test public void topN_07() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
            ,"  (project (?s ?p ?z)"
            ,"    (order (?z ?s)"
            ,"      (bgp (triple ?s ?p ?z)) )))"
                );
        String output = StrUtils.strjoinNL
            ("(project (?s ?p ?z)"
            ,"  (top (5 ?z ?s)"
            ,"   (bgp (triple ?s ?p ?z)) ))"
                );
        test(input, output) ;
    }
    
    // ---- The same but with distinct
    
    @Test public void topN_11() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (distinct"
             ,"    (order (?z)"
             ,"      (bgp (triple ?s ?p ?z)) )))"
                );

        String output = StrUtils.strjoinNL
            ("(top (5 ?z)"
             ,"  (distinct"
             ,"    (bgp (triple ?s ?p ?z)) ))"

                );
        test(input, output) ;
    }

    @Test public void topN_12() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (distinct"
             ,"    (project (?s ?z)"
             ,"      (order (?z ?s)"
             ,"        (bgp (triple ?s ?p ?z)) ))))"
                );

        String output = StrUtils.strjoinNL
            ("(top (5 ?z ?s)"
             ,"  (distinct"
             ,"    (project (?s ?z)"
             ,"      (bgp (triple ?s ?p ?z)) )))"
                );
        test(input, output) ;
    }
    
    @Test public void topN_13() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (distinct"
             ,"    (project (?z)"
             ,"      (order (?z)"
             ,"        (bgp (triple ?s ?p ?z)) ))))"
                );

        String output = StrUtils.strjoinNL
            ("(top (5 ?z)"
             ,"  (distinct"
             ,"    (project (?z)"
             ,"      (bgp (triple ?s ?p ?z)) )))"
                );
        test(input, output) ;
    }
    
    @Test public void topN_14() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (distinct"
             ,"    (project (?s ?z)"
             ,"      (order (?z)"
             ,"        (bgp (triple ?s ?p ?z)) ))))"
                );

        String output = StrUtils.strjoinNL
            ("(top (5 ?z)"
             ,"  (distinct"
             ,"    (project (?s ?z)"
             ,"      (bgp (triple ?s ?p ?z)) )))"

                );
        test(input, output) ;
    }
    
    @Test public void topN_15() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (distinct"
             ,"    (project (?z)"
             ,"      (order (?z ?s)"
             ,"        (bgp (triple ?s ?p ?z)) ))))"

                );
        // Order being wider than project blocks the optimization.  
        String output = input ;
        test(input, output) ;
    }
    
    @Test public void topN_16() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (distinct"
             ,"    (project (?s ?p)"
             ,"      (order (?z ?s)"
             ,"        (bgp (triple ?s ?p ?z)) ))))"
                );
        // The mismatch of project and order blocks the optimization.  
        String output = input ;
        test(input, output) ;
    }
    
    @Test public void topN_17() {
        String input = 
            StrUtils.strjoinNL
            ("(slice _ 5"
             ,"  (distinct"
             ,"    (project (?s ?p ?z)"
             ,"      (order (?z ?s)"
             ,"        (bgp (triple ?s ?p ?z)) ))))"
                );
        String output = StrUtils.strjoinNL
            ("(top (5 ?z ?s)"
             ,"  (distinct"
             ,"    (project (?s ?p ?z)"
             ,"      (bgp (triple ?s ?p ?z)) )))"
                );
        test(input, output) ;
    }
    
    // ---- From query to transfomed algebra.
    
    @Test public void topN_query_01() {
        String output = StrUtils.strjoinNL
            ("    (project (?s)"
            ," (order (?p ?o)"
            ,"   (bgp (triple ?s ?p ?o))))"
            );
        testQuery("SELECT ?s { ?s ?p ?o } ORDER BY ?p ?o", output);
    }
    
    @Test public void topN_query_02() {
        String output = StrUtils.strjoinNL
            ("  (slice 1 _"
            ,"  (project (?s)"
            ,"    (top (6 ?p ?o)"
            ,"      (bgp (triple ?s ?p ?o)))))"
            ) ;
        testQuery("SELECT ?s { ?s ?p ?o } ORDER BY ?p ?o OFFSET 1 LIMIT 5", output);
    }
    
    @Test public void topN_query_03() {
        String output = StrUtils.strjoinNL
            ("(slice 1 _"
            ,"  (project (?s)"
            ,"    (top (6 ?p ?o)"
            ,"      (bgp (triple ?s ?p ?o)))))"
                );
        testQuery("SELECT ?s { ?s ?p ?o } ORDER BY ?p ?o OFFSET 1 LIMIT 5", output);
    }

    @Test public void topN_query_04() {
        String output = StrUtils.strjoinNL
            ("(slice 1 _"
            ,"  (project (?s)"
            ,"    (top (6 ?p ?o)"
            ,"      (bgp (triple ?s ?p ?o)))))"
            );        testQuery("SELECT ?s { ?s ?p ?o } ORDER BY ?p ?o OFFSET 1 LIMIT 5", output);
    }

    private void test(String input, String output) {
        Transform transform = new TransformTopN() ;
        testOp(input, transform, output) ;
    }

    // ---- From query to transfomed algebra.
    
    private void testQuery(String input, String output) {
        Transform transform = new TransformTopN() ;
        testQuery(input, transform, output) ;
    }
}

