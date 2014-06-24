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

import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Rename ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestVarRename extends BaseTest
{
    @Test public void rename_01() { rename("(bgp (<s> <p> <o>))", "(bgp (<s> <p> <o>))", true) ; }
    @Test public void rename_02() { rename("(bgp (<s> ?p <o>))", "(bgp (<s> ?/p <o>))", true) ; }
    @Test public void rename_03() { rename("(bgp (?s ?p <o>))", "(bgp (?s ?/p <o>))", true, "s") ; }
    @Test public void rename_04() { rename("(filter (+ ?s ?x) (bgp (?s ?p <o>)))", "(filter (+ ?s ?/x) (bgp (?s ?/p <o>)))", true, "s") ; }

    @Test public void rename_05() { rename("(group ((?.1 (str ?x))) ((?.0 (count))) (bgp (triple ?x :p ?v)))",
                                           "(group ((?/.1 (str ?x))) ((?/.0 (count))) (bgp (triple ?x :p ?/v)))",
                                           true, "x" ) ; }
    
    @Test public void rename_06() { rename("(group ((?.1 (str ?x))) ((?.0 (max ?v))) (bgp (triple ?x :p ?v)))",
                                           "(group ((?/.1 (str ?x))) ((?/.0 (max ?/v))) (bgp (triple ?x :p ?/v)))",
                                           true, "x" ) ; }

    @Test public void rename_07() { rename("(assign ((?x (+ ?/a ?/b))) (table unit))", 
                                           "(assign ((?/x (+ ?//a ?//b))) (table unit))",
                                           true) ; }
    @Test public void rename_08() { rename("(assign ((?x (+ ?/a ?/b))) (table unit))", 
                                           "(assign ((?/x (+ ?/a ?//b))) (table unit))",
                                           false, "/a") ; }
    
    @Test public void rename_09() { rename("(project (?s ?p) (bgp (?s ?p ?o)))",  
                                           "(project (?s ?/p) (bgp (?s ?/p ?/o)))",
                                           true,
                                           "s") ; }
    
    @Test public void rename_10() { rename("(order (?s ?p) (bgp (?s ?p ?o)))",  
                                           "(order (?s ?/p) (bgp (?s ?/p ?/o)))",
                                           true,
                                           "s") ; }
    
    @Test public void rename_11() { rename("(project (?s) (order (?s ?p) (bgp (?s ?p ?o))))",  
                                           "(project (?s) (order (?s ?/p) (bgp (?s ?/p ?/o))))",
                                           true,
                                           "s") ; }
    
    @Test public void rename_12() { rename("(leftjoin (bgp (?s ?p ?o)) (bgp (?s ?p ?o1)) () )",
                                           "(leftjoin (bgp (?s ?/p ?o)) (bgp (?s ?/p ?/o1)) () )",
                                           true,
                                           "s", "o") ; }
    
    // JENA-502 : failure to rewrite (table) in var rename.
    @Test public void rename_13() { rename("(project (?s) (project (?s) (table (vars ?obj) (row [?obj 123])) ))",
                                           "(project (?s) (project (?s) (table (vars ?/obj) (row [?/obj 123])) ))",
                                           true,
                                           "s") ; }
    
    // JENA-494 : sub-query and service interaction
    @Test public void rename_14() { rename("(project (?z) (project (?z) (sequence (service <http://foo> (bgp (?c ?p ?z)) ) (bgp (?c ?q ?z)) ) ) )",
                                           "(project (?z) (project (?z) (sequence (service <http://foo> (bgp (?/c ?/p ?z)) ) (bgp (?/c ?/q ?z)) ) ) )",
                                           true,
                                           "z") ; }

    
    @Test public void rename_reverse_01() { reverse("(project (?s ?/p) (bgp (?s ?/p ?/o)))",
                                                    "(project (?s ?p) (bgp (?s ?p ?o)))", true ) ; }  

    @Test public void rename_reverse_02() { reverse("(assign ((?/x (+ ?//a ?///b))) (table unit))",
                                                    "(assign ((?x (+ ?a ?b))) (table unit))", 
                                                    true ) ; }  
    
    private static void reverse(String string, String string2, boolean repeatedly)
    {
        Op opOrig = SSE.parseOp(string) ;
        Op opExpected = SSE.parseOp(string2) ;
        Op opActual = Rename.reverseVarRename(opOrig, repeatedly) ;
        assertEquals(opExpected, opActual) ;
    }

    private static void rename(String string, String string2, boolean reversable, String... varNames)
    {
        Set<Var> s = new HashSet<>() ;
        for ( String vn : varNames )
            s.add(Var.alloc(vn)) ;
        rename(string, string2, reversable, s) ;
    }

    private static void rename(String string, String string2, boolean reversable,  Set<Var> constant)
    {
        Op opOrig = SSE.parseOp(string) ;
        Op opExpected = SSE.parseOp(string2) ;
        Op opActual = Rename.renameVars(opOrig, constant) ;
        assertEquals(opExpected, opActual) ;
        
        if ( reversable )
        {
            // Undo.
            Op opRebuilt = Rename.reverseVarRename(opActual, false) ;
            assertEquals(opOrig, opRebuilt) ;
        }
    }
    
    @Test public void query_rename_01()
    {
        String queryString =  
            "SELECT ?x { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }" ;
        String opExpectedString =
            "(project (?x)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (project (?v)\n" + 
            "        (join\n" + 
            "          (bgp (triple ?/x ?/y ?v))\n" + 
            "          (project (?/w)\n" + 
            "            (bgp (triple ?//a ?//y ?/w))))))))";
        checkRename(queryString, opExpectedString) ;
    }

    @Test public void query_rename_02()
    {
        String queryString = 
            "SELECT ?x { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT * { ?a ?y ?w }}} LIMIT 50 } }"  ;  
        String opExpectedString = 
            "(project (?x)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (project (?v)\n" + 
            "        (join (bgp (triple ?/x ?/y ?v)) (bgp (triple ?/a ?/y ?/w))))" +
            ")))" ; 
        checkRename(queryString, opExpectedString) ;
    }

    @Test public void query_rename_03()
    {
        String queryString = "SELECT ?x { ?s ?p ?o . { SELECT * { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }" ;  
        String opExpectedString = 
            "(project (?x)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (join\n" + 
            "        (bgp (triple ?x ?y ?v))\n" + 
            "        (project (?w)\n" + 
            "          (bgp (triple ?/a ?/y ?w)))))))" ;
        checkRename(queryString, opExpectedString) ;
    }

    @Test public void query_rename_04()
    {
        String queryString = "SELECT * { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }" ;  
        String opExpectedString = 
            "(join\n" + 
            "  (bgp (triple ?s ?p ?o))\n" + 
            "  (slice _ 50\n" + 
            "    (project (?v)\n" + 
            "      (join\n" + 
            "        (bgp (triple ?/x ?/y ?v))\n" + 
            "        (project (?/w)\n" + 
            "          (bgp (triple ?//a ?//y ?/w)))))))" ;
        checkRename(queryString, opExpectedString) ;
    }

    @Test public void query_rename_05()
    {
        String queryString = "SELECT ?v { ?s ?p ?o . { SELECT ?v { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} LIMIT 50 } }"    ;  
        String opExpectedString = 
            "(project (?v)\n" + 
            "  (join\n" + 
            "    (bgp (triple ?s ?p ?o))\n" + 
            "    (slice _ 50\n" + 
            "      (project (?v)\n" + 
            "        (join\n" + 
            "          (bgp (triple ?/x ?/y ?v))\n" + 
            "          (project (?/w)\n" + 
            "            (bgp (triple ?//a ?//y ?/w))))))))" ;
        checkRename(queryString, opExpectedString) ;
    }

    @Test public void query_rename_06()
    {
        String queryString = "SELECT ?w { ?s ?p ?o . { SELECT ?w { ?x ?y ?v {SELECT ?w { ?a ?y ?w }}} } } LIMIT 50" ;  
        String opExpectedString = 
            "(slice _ 50\n" + 
            "  (project (?w)\n" + 
            "    (join\n" + 
            "      (bgp (triple ?s ?p ?o))\n" + 
            "      (project (?w)\n" + 
            "        (join\n" + 
            "          (bgp (triple ?/x ?/y ?/v))\n" + 
            "          (project (?w)\n" + 
            "            (bgp (triple ?//a ?//y ?w))))))))\n" + 
            "" ;
        checkRename(queryString, opExpectedString) ;
    }

    @Test public void query_rename_07()
    {
        String queryString = "SELECT * { ?s ?p ?o . { SELECT ?w { ?x ?y ?v }}}"  ;  
        String opExpectedString = 
            "(join\n" + 
            "  (bgp (triple ?s ?p ?o))\n" + 
            "  (project (?w)\n" + 
            "    (bgp (triple ?/x ?/y ?/v))))" ;
        checkRename(queryString, opExpectedString) ;
    }
    
    static void checkRename(String queryString, String opExpectedString)
    {
        Op opExpected = SSE.parseOp(opExpectedString) ;
        queryString = "PREFIX : <http://example/>\n"+queryString ;
        Query query = QueryFactory.create(queryString) ;
        Op op = Algebra.compile(query) ;
        Op opRenamed = TransformScopeRename.transform(op) ;
        assertEquals(opExpected, opRenamed) ;
    }
}
