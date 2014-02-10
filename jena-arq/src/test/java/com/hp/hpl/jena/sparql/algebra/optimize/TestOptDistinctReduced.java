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

package com.hp.hpl.jena.sparql.algebra.optimize;

import org.junit.Test ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.algebra.Transform ;

public class TestOptDistinctReduced extends AbstractTestTransform
{
    Transform tDistinctToReduced            = new TransformDistinctToReduced() ;
    Transform tOrderByDistinctApplication  = new TransformOrderByDistinctApplication() ;
    
    @Test public void distinct_to_reduced_01()
    {
        // Not safe to transform because ORDER BY does not cover * which is ?s ?p ?o
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p ?o"  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (order (?p ?o)\n" +
            "    (bgp (triple ?s ?p ?o))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }

    @Test public void distinct_to_reduced_02()
    {
        // Safe to transform because ORDER BY does cover * which is ?s ?p
        String queryString = "SELECT DISTINCT * { ?s ?p 123 } ORDER BY ?s ?p"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (order (?s ?p)\n" +
            "    (bgp (triple ?s ?p 123))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }

    @Test public void distinct_to_reduced_03()
    {
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?s ?p ?o"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (order (?s ?p ?o)\n" +
            "    (bgp (triple ?s ?p ?o))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_04()
    {
        // This is safe to transform since all project variables 
        // appear in the ORDER BY.
        // Ordering of variables in the ORDER BY is irrelevant as long as they appear
        // before any non-projected variables
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY ?p ?o"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (project (?p)\n" +
            "    (order (?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_05()
    {
        // Unsafe : ORDER BY has ?o before ?p
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY ?o ?p"  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (project (?p)\n" +
            "    (order (?o ?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    

    @Test public void distinct_to_reduced_06()
    {
            // Per JENA-587 this is safe to transform since all project variables 
            // appear in the ORDER BY
            // Ordering of variables in the ORDER BY is irrelevant as long as they appear
            // before any non-projected variables
            assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
            String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?p ?o"  ;  
            String opExpectedString = 
                "(reduced\n" + 
                "  (project (?p ?o)\n" +
                "    (order (?p ?o)\n" +
                "      (bgp (triple ?s ?p ?o)))))" ; 
            testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_07()
    {
        // Per JENA-587 this is safe to transform since all project variables 
        // appear in the ORDER BY
        // Ordering of variables in the ORDER BY is irrelevant as long as they appear
        // before any non-projected variables
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?o ?p"  ;  
        String opExpectedString = 
            "(reduced\n" + 
                "  (project (?p ?o)\n" +
                "    (order (?o ?p)\n" +
                "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_08()
    {
        // Per JENA-587 this is safe to transform since all project variables 
        // appear in the ORDER BY
        // Ordering of variables in the ORDER BY is irrelevant as long as they appear
        // before any non-projected variables
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?o ?p ?s"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?o ?p ?s)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_09()
    {
        // Per JENA-587 this is safe to transform since all project variables 
        // appear in the ORDER BY
        // Ordering of variables in the ORDER BY is irrelevant as long as they appear
        // before any non-projected variables
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?p ?o ?s"  ;  
        String opExpectedString = 
            "(reduced\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?p ?o ?s)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_10()
    {
        // Per JENA-587 this is unsafe to transform since a non-project variable 
        // appears before all the projected variables are seen in the ORDER BY
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?s ?p ?o"  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?s ?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_11()
    {
        // Per JENA-587 this is unsafe to transform since a non-project variable 
        // appears before all the projected variables are seen in the ORDER BY
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?p ?s ?o"  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?p ?s ?o)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_12()
    {
        // Per JENA-587 this is unsafe to transform since a non-project variable 
        // appears before all the projected variables are seen in the ORDER BY
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } ORDER BY ?s"  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (project (?p ?o)\n" +
            "    (order (?s)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_to_reduced_13()
    {
        // Per JENA-587 this is unsafe to transform since there is no ORDER BY
        assertTrue(ARQ.isTrueOrUndef(ARQ.optDistinctToReduced)) ;
        String queryString = "SELECT DISTINCT ?p ?o { ?s ?p ?o } "  ;  
        String opExpectedString = 
            "(distinct\n" + 
            "  (project (?p ?o)\n" +
            "      (bgp (triple ?s ?p ?o))))" ; 
        testQuery(queryString, tDistinctToReduced, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_01()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY ?p";
        String opExpectedString =
            "(order (?p)\n" +
            "  (distinct\n" +
            "    (project (?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        testQuery(queryString, tOrderByDistinctApplication, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_03()
    {
        // Evaluation reordering optimization doesn't apply if it's a SELECT *
        // Also per JENA-587 DISTINCT -> REDUCED transformation cannot apply either
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT * { ?s ?p ?o } ORDER BY ?p";
        String opExpectedString =
            "  (distinct\n" +
            "    (order (?p)\n" +
            "      (bgp (triple ?s ?p ?o))))" ;
        testQuery(queryString, tOrderByDistinctApplication, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_04()
    {
        // The optimization still applies when order conditions are not simple variables
        // provided every variable used in an expression appears in the project list
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY LCASE(STR(?p))";
        String opExpectedString =
            "(order ((lcase (str (?p))))\n" +
            "  (distinct\n" +
            "    (project (?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        testQuery(queryString, tOrderByDistinctApplication, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_05()
    {
        // The optimization still applies when order conditions are not simple variables
        // provided every variable used in an expression appears in the project list
        String queryString = "SELECT DISTINCT ?s ?p { ?s ?p ?o } ORDER BY LCASE(CONCAT(?s, ?p))";
        String opExpectedString =
            "(order ((lcase (concat ?s ?p)))\n" +
            "  (distinct\n" +
            "    (project (?s ?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        testQuery(queryString, tOrderByDistinctApplication, opExpectedString) ;
    }
    
    @Test public void distinct_order_by_application_06()
    {
        // The optimization can apply when order conditions are not simple variables
        // provided every variable used in an expression appears in the project list
        // In this case it should not apply because the condition used a variable that
        // does not appear in the project list
        // Per JENA-587 the DISTINCT to REDUCED optimization also does not apply
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT DISTINCT ?p { ?s ?p ?o } ORDER BY LCASE(CONCAT(?s, ?p))";
        String opExpectedString =
            "  (distinct\n" +
            "    (project (?p)\n" +
            "      (order ((lcase (concat ?s ?p)))\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        testQuery(queryString, tOrderByDistinctApplication, opExpectedString) ;
    }
    
    @Test public void reduced_order_by_application_01()
    {
        assertTrue(ARQ.isTrueOrUndef(ARQ.optOrderByDistinctApplication)) ;
        String queryString = "SELECT REDUCED ?p { ?s ?p ?o } ORDER BY ?p";
        String opExpectedString =
            "(order (?p)\n" +
            "  (reduced\n" +
            "    (project (?p)\n" +
            "      (bgp (triple ?s ?p ?o)))))" ;
        testQuery(queryString, tOrderByDistinctApplication, opExpectedString) ;
    }
    
    @Test public void reduced_order_by_application_02() 
    {
        try {
            ARQ.setFalse(ARQ.optOrderByDistinctApplication) ;
            assertTrue(ARQ.isFalse(ARQ.optOrderByDistinctApplication)) ;
            String queryString = "SELECT REDUCED ?p { ?s ?p ?o } ORDER BY ?p" ;
            String opExpectedString =
                "(reduced\n" +
                "  (project (?p)\n" +
                "    (order (?p)\n" +
                "      (bgp (triple ?s ?p ?o)))))" ;
            testOptimize(queryString, opExpectedString) ;
        } finally {
            ARQ.unset(ARQ.optOrderByDistinctApplication) ;
        }
    }
}
