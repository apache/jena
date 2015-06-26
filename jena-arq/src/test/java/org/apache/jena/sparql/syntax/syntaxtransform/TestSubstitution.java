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

package org.apache.jena.sparql.syntax.syntaxtransform;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps ;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

/** Test of variable replaced by value */
public class TestSubstitution extends BaseTest
{
    @Test public void subst_01() { testQuery("SELECT * { }", "SELECT * {}", "o", "1") ; }
    
    @Test public void subst_02() { testQuery("SELECT ?x { }", "SELECT ?x {}", "o", "1") ; }

    @Test public void subst_03() { testQuery("SELECT ?o { }", "SELECT (1 as ?o) {}", "o", "1") ; }

    @Test public void subst_04() { testQuery("SELECT (?o AS ?z) { }", "SELECT (1 AS ?z) {}", "o", "1") ; }

    @Test public void subst_05() { testQuery("SELECT (?o+2 AS ?z) { }", "SELECT (1+2 AS ?z) {}", "o", "1") ; }

    @Test public void subst_09() { testQuery("SELECT * {?s ?p ?o}", "SELECT * {?s ?p 1}", "o", "1") ; }  
    
    @Test public void subst_10() { testQuery("SELECT * { SELECT ?o {} }", "SELECT * {{SELECT (1 as ?o) {}}}", "o", "1") ; }
    
    @Test public void subst_11() { testQuery("SELECT * { ?s ?p ?o { SELECT ?x { ?x ?p ?o } } }",
                                             "SELECT * { ?s ?p 1  { SELECT ?x { ?x ?p 1 } } }",
                                             "o", "1") ; }

    @Test public void subst_50() { testUpdate("DELETE { ?s <urn:p> ?x } WHERE {}",
                                              "DELETE { ?s <urn:p> <urn:x> } WHERE {}", "x", "<urn:x>") ; }

    //static final String PREFIX = "PREFIX : <http://example/>\n" ;
    static final String PREFIX = "" ;

    private void testQuery(String input, String output, String varStr, String valStr)
    {
        Query q1 = QueryFactory.create(PREFIX+input) ;
        Query qExpected = QueryFactory.create(PREFIX+output) ;
        
        Map<Var, Node> map = new HashMap<Var, Node>() ;
        map.put(Var.alloc(varStr), SSE.parseNode(valStr)) ;
        
        Query qTrans = QueryTransformOps.transform(q1, map) ;
        assertEquals(qExpected, qTrans) ;
    }

    private void testUpdate(String input, String output, String varStr, String valStr)
    {
        UpdateRequest req1 = UpdateFactory.create(PREFIX+input) ;
        UpdateRequest reqExpected = UpdateFactory.create(PREFIX+output) ;
        
        Map<Var, Node> map = new HashMap<Var, Node>() ;
        map.put(Var.alloc(varStr), SSE.parseNode(valStr)) ;
        
        UpdateRequest reqTrans = UpdateTransformOps.transform(req1, map) ;
        
        // Crude.
        String x1 = reqExpected.toString().replaceAll("[ \n\t]", "") ;
        String x2 = reqTrans.toString().replaceAll("[ \n\t]", "") ;
        //assertEquals(reqExpected, reqTrans) ;
        assertEquals(x1, x2) ;
    }
    
}

