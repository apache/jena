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

package com.hp.hpl.jena.sparql.lang;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryFactory ;

public class TestVarScope extends BaseTest
{
    private static void scope(String queryStr)
    {
        Query query = QueryFactory.create(queryStr) ;
    }
    
    @Test public void scope_01() { scope("SELECT ?x { ?s ?p ?o }") ; }
    @Test public void scope_02() { scope("SELECT ?s { ?s ?p ?o }") ; }
    
    @Test public void scope_03() { scope("SELECT (?o+1 AS ?x) { ?s ?p ?o }") ; }
    
    @Test(expected=QueryException.class)
    public void scope_04() { scope("SELECT (?o+1 AS ?o) { ?s ?p ?o }") ; }
    
    @Test(expected=QueryException.class)
    public void scope_05() { scope("SELECT (?o+1 AS ?x) (?o+1 AS ?x) { ?s ?p ?o }") ; }

    @Test public void scope_06() { scope("SELECT (?z+1 AS ?x) { ?s ?p ?o } GROUP BY (?o+5 AS ?z)") ; }
    
    @Test(expected=QueryException.class)
    public void scope_07() { scope("SELECT (?o+1 AS ?x) { ?s ?p ?o } GROUP BY (?o+5 AS ?x)") ; }

    @Test public void scope_08() { scope("SELECT (count(*) AS ?X) (?X+1 AS ?Z) { ?s ?p ?o }") ; }

    @Test public void scope_09() { scope("SELECT (count(*) AS ?X) (?X+?o AS ?Z) { ?s ?p ?o } GROUP BY ?o") ; }
    
    @Test public void scope_10() { scope("SELECT (?o+1 AS ?x) { ?s ?p ?o MINUS { ?s ?p ?x} } ") ; }
    
    @Test(expected=QueryException.class)
    public void scope_15() { scope("SELECT (?o+1 AS ?x) { { SELECT (123 AS ?x) {?s ?p ?o } } } ") ; }
    
    @Test public void scope_16() { scope("SELECT (?o+1 AS ?o) { { SELECT (123 AS ?x) {?s ?p ?o } } } ") ; }

    @Test public void scope_17() { scope("SELECT (?o+1 AS ?o) { { SELECT (123 AS ?x) {?s ?p ?o FILTER(?x > 57)} } } ") ; }
    

    @Test public void scope_20() { scope("SELECT ?x { ?x ?p ?o } GROUP BY ?x") ; }
    
    @Test (expected=QueryException.class)
    public void scope_21() { scope("SELECT ?o { ?x ?p ?o } GROUP BY ?x") ; }

    @Test(expected=QueryException.class)
    public void scope_22() { scope("SELECT * { ?s ?p ?o BIND(5 AS ?o) }") ; }
    
    @Test public void scope_23() { scope("SELECT * { ?s ?p ?o { BIND(5 AS ?o) } }") ; }
 
    @Test(expected=QueryException.class)
    public void scope_24() { scope("SELECT * { { ?s ?p ?o } BIND(5 AS ?o) }") ; }
    
    @Test public void scope_25() { scope("SELECT * { { ?s ?p ?o } { BIND(5 AS ?o) } }") ; }
    
    @Test public void scope_26() { scope("SELECT * { ?s ?p ?o OPTIONAL{?s ?p2 ?o2} BIND(?o2+5 AS ?z) }") ; }

    @Test(expected=QueryException.class)
    public void scope_27() { scope("SELECT * { ?s ?p ?o OPTIONAL{?s ?p2 ?o2} BIND(5 AS ?o2) }") ; }

    @Test(expected=QueryException.class)
    public void scope_28() { scope("SELECT * { { ?s ?p ?o OPTIONAL{?s ?p2 ?o2} } BIND(?o+5 AS ?o2) }") ; }

    @Test(expected=QueryException.class)
    public void scope_29() { scope("SELECT * { ?s ?p ?o OPTIONAL{?s ?p2 ?o2} BIND(5 AS ?o) }") ; }

    @Test(expected=QueryException.class)
    public void scope_30() { scope("SELECT * { { ?s ?p ?o } OPTIONAL{?s ?p2 ?o2} BIND(5 AS ?o) }") ; }
    
    @Test(expected=QueryException.class)
    public void scope_34() { scope("SELECT * { { ?s ?p ?o } UNION {?s ?p2 ?o2} BIND(5 AS ?o) }") ; }
    
    @Test
    public void scope_35() { scope("SELECT * { ?s1 ?p1 ?z { ?s ?p ?z } UNION { BIND(5 AS ?z) } }") ; }
    
    // Subqueries
    
    @Test(expected=QueryException.class)
    public void scope_50() { scope("SELECT * { SELECT (?o+1 AS ?o) { ?s ?p ?o }}") ; }
    
    @Test
    public void scope_51()
    {
        scope("SELECT ?y { " +
        		"{ { SELECT (?x AS ?y) { ?s ?p ?x } } } UNION { { SELECT (?x AS ?y) { ?s ?p ?x } } }" +
        		"}") ;
    }
    
    @Test(expected=QueryException.class)
    public void scope_52()
    {
        scope("SELECT ?y { " +
                "{ { SELECT (?o+1 AS ?x) (?o+1 AS ?x) { ?s ?p ?o } } UNION { ?s ?p ?x } }" +
                "}") ;
    }
    
    @Test(expected=QueryException.class)
    public void scope_63()
    {
        // Check nested things get checked.
        scope("SELECT * { { ?s ?p ?o } UNION { ?s ?p ?o1 BIND(5 AS ?o1) } }") ;
    }
    
    @Test(expected=QueryException.class)
    public void scope_64()
    {
        // Check nested things get checked.
        scope("SELECT * { { { ?s ?p ?o1 BIND(5 AS ?o1) } } }") ;
    }
        

}
