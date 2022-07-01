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

package org.apache.jena.sparql.algebra.optimize;

import static org.junit.Assert.assertEquals;

import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.Rename ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test ;
import org.junit.rules.TestName;

public class TestVarRename
{
    @Rule public TestName name = new TestName();
    
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


    // JENA-2132 : Renaming must descend into RDFStar TripleNodes
    @Test public void query_rename_08()
    {
        String queryString
                = "SELECT COUNT(*) {\n"
                + "  SELECT ?src {\n"
                + "    ?src  <urn:connectedTo>  ?tgt .\n"
                + "    << ?src <urn:connectedTo> ?tgt >>\n"
                + "                  <urn:hasValue>  ?v\n"
                + "  }\n"
                + "}";

        String opExpectedString
                = "(project (?.1)\n"
                + "  (extend ((?.1 ?.0))\n"
                + "    (group () ((?.0 (count)))\n"
                + "      (project (?src)\n"
                + "        (bgp\n"
                + "          (triple ?src <urn:connectedTo> ?/tgt)\n"
                + "          (triple << ?src <urn:connectedTo> ?/tgt >> <urn:hasValue> ?/v)\n"
                + "        )))))";

        checkRename(queryString, opExpectedString) ;
    }
    
    // JENA-1275
    @Test
    public void filter_not_exists_scoping_03() {
        //@formatter:off
        Op orig = SSE.parseOp(StrUtils.strjoinNL("(project (?triangles ?openTriplets)",
                                       "  (project (?openTriplets)",
                                       "    (extend ((?openTriplets ?.0))",
                                       "      (group () ((?.0 (count ?x)))",
                                       "        (filter (notexists",
                                       "                   (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?z ?c ?x)))",
                                       "          (quadpattern",
                                       "            (quad <urn:x-arq:DefaultGraphNode> ?x ?a ?y)",
                                       "            (quad <urn:x-arq:DefaultGraphNode> ?y ?b ?z)",
                                       "          ))))))"));
        Op expected = SSE.parseOp(StrUtils.strjoinNL("(project (?triangles ?openTriplets)",
                "  (project (?openTriplets)",
                "    (extend ((?openTriplets ?/.0))",
                "      (group () ((?/.0 (count ?/x)))",
                "        (filter (notexists",
                "                   (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?/z ?/c ?/x)))",
                "          (quadpattern",
                "            (quad <urn:x-arq:DefaultGraphNode> ?/x ?/a ?/y)",
                "            (quad <urn:x-arq:DefaultGraphNode> ?/y ?/b ?/z)",
                "          ))))))"));
        //@formatter:on
        
        Op transformed = TransformScopeRename.transform(orig);
        
        Assert.assertEquals(transformed, expected);
    }
    
    // JENA-1275
    @Test
    public void filter_not_exists_scoping_04() {
        //@formatter:off
        Op orig = SSE.parseOp(StrUtils.strjoinNL(
                                       "  (project (?openTriplets)",
                                       "    (extend ((?openTriplets ?.0))",
                                       "      (group () ((?.0 (count ?x)))",
                                       "        (filter (notexists",
                                       "                   (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?z ?c ?x)))",
                                       "          (quadpattern",
                                       "            (quad <urn:x-arq:DefaultGraphNode> ?x ?a ?y)",
                                       "            (quad <urn:x-arq:DefaultGraphNode> ?y ?b ?z)",
                                       "          )))))"));
        Op expected = SSE.parseOp(StrUtils.strjoinNL(
                "  (project (?openTriplets)",
                "    (extend ((?openTriplets ?.0))",
                "      (group () ((?.0 (count ?x)))",
                "        (filter (notexists",
                "                   (quadpattern (quad <urn:x-arq:DefaultGraphNode> ?z ?c ?x)))",
                "          (quadpattern",
                "            (quad <urn:x-arq:DefaultGraphNode> ?x ?a ?y)",
                "            (quad <urn:x-arq:DefaultGraphNode> ?y ?b ?z)",
                "          )))))"));
        //@formatter:on
        
        Op transformed = TransformScopeRename.transform(orig);
        
        Assert.assertEquals(transformed, expected);
    }
    
    @Test public void renameExpr_01() {
        renameExpr("(exists (triple ?z ?p ?x))",
                   "(exists (triple ?/z ?/p ?x))",
                   "x");
    }
    
    @Test public void renameExpr_02() {
        renameExpr("(exists (filter (= ?a ?x) (triple ?z ?p ?x)) )", 
                   "(exists (filter (= ?/a ?x) (triple ?/z ?/p ?x)))", 
                   "x");
    }

    // JENA-1275
    @Test public void rename_X_01() {
        rename("(filter (exprlist (= ?x ?z) (exists (triple ?z ?p ?x)) ) (triple ?x ?a ?z) )",
               "(filter (exprlist (= ?x ?/z) (exists (triple ?/z ?/p ?x)) ) (triple ?x ?/a ?/z) )",
               "x");
    }

    // FAILURE
    @Test public void rename_X_02() {
        String str1 = "(project (?C ?x) (extend ((?C ?.0))  (group (?x) ((?.0 (count)))  (bgp (triple ?s ?p ?x)))))" ;
        String str2 = "(project (?/C ?x) (extend ((?/C ?/.0))  (group (?x) ((?/.0 (count)))  (bgp (triple ?/s ?/p ?x)))))" ;
        rename(str1, str2, "x");
    }
    
    @Test public void rename_X_02a() {
        String str1 = "(project (?C ?s) (extend ((?C ?.0))  (group (?s) ((?.0 (count)))  (bgp (triple ?s ?p ?x)))))" ;
        String str2 = "(project (?/C ?/s) (extend ((?/C ?/.0))  (group (?/s) ((?/.0 (count)))  (bgp (triple ?/s ?/p ?x)))))" ;
        rename(str1, str2, "x");
    }

    @Test public void rename_X_03() {
        String str1 = "(leftjoin (triple ?s ?p ?x) (triple ?a ?b ?x) (= ?a ?x) )";
        String str2 = "(leftjoin (triple ?/s ?/p ?x) (triple ?/a ?/b ?x) (= ?/a ?x) )";
        rename(str1, str2, "x");
    }

    @Test public void rename_X_04() {
        String str1 = "(order ((+ ?a ?x)) (triple ?a ?p ?x))";      /// order to take one expr?
        String str2 = "(order ((+ ?/a ?x)) (triple ?/a ?/p ?x))";
        rename(str1, str2, "x");
    }

    @Test public void rename_X_05() {
        String str1 = "(extend (?x (+ ?a ?x)) (triple ?a ?p ?x))";
        String str2 = "(extend (?x (+ ?/a ?x)) (triple ?/a ?/p ?x))";
        rename(str1, str2, "x");
    }

    @Test public void rename_X_06() {
        String str1 = "(extend (?a (+ ?a ?x)) (triple ?a ?p ?x))";
        String str2 = "(extend (?/a (+ ?/a ?x)) (triple ?/a ?/p ?x))";
        rename(str1, str2, "x");
    }

    @Test public void rename_X_07() {
        String str1 = "(assign (?x (+ ?a ?x)) (triple ?a ?p ?x))";
        String str2 = "(assign (?x (+ ?/a ?x)) (triple ?/a ?/p ?x))";
        rename(str1, str2, "x");
    }

    @Test public void rename_X_08() {
        String str1 = "(assign (?a (+ ?a ?x)) (triple ?a ?p ?x))";
        String str2 = "(assign (?/a (+ ?/a ?x)) (triple ?/a ?/p ?x))";
        rename(str1, str2, "x");
    }

    
    private void checkRename(String queryString, String opExpectedString)
    {
        Op opExpected = SSE.parseOp(opExpectedString) ;
        queryString = "PREFIX : <http://example/>\n"+queryString ;
        Query query = QueryFactory.create(queryString) ;
        Op op = Algebra.compile(query) ;
        Op opRenamed = TransformScopeRename.transform(op) ;
        assertEquals(opExpected, opRenamed) ;
    }
    
    private void reverse(String string, String string2, boolean repeatedly) {
        Op opOrig = SSE.parseOp(string);
        Op opExpected = SSE.parseOp(string2);
        Op opActual = Rename.reverseVarRename(opOrig, repeatedly);
        assertEquals(opExpected, opActual);
    }
    private void rename(String string, String string2, boolean reversable, String... varNames) {
        Set<Var> s = new HashSet<>();
        for ( String vn : varNames )
            s.add(Var.alloc(vn));
        rename(string, string2, reversable, s);
    }
    private void rename(String inputStr, String expectedStr, boolean reversable, Set<Var> constant) {
        Op opOrig = SSE.parseOp(inputStr);
        Op opExpected = SSE.parseOp(expectedStr);
        Op opActual = Rename.renameVars(opOrig, constant);
    
        if ( DEV && !opExpected.equals(opActual) ) {
            System.err.println("**** Test: " + name.getMethodName());
            System.err.println("::Expected::");
            System.err.print(opExpected);
            System.err.println("::Got::");
            System.err.print(opActual);
        }
    
        assertEquals(opExpected, opActual);
    
        if ( reversable ) {
            // Undo.
            Op opRebuilt = Rename.reverseVarRename(opActual, false);
            assertEquals(opOrig, opRebuilt);
        }
    }

    // Print failures more clearly.
    private static boolean DEV = true;

    private void renameExpr(String inputStr, String expectedStr, String ... varNames) {
        Set<Var> s = set(varNames);
        Expr exOrig = SSE.parseExpr(inputStr) ;
        Expr exExpected = SSE.parseExpr(expectedStr) ;
        Expr exprActual = Rename.renameVars(exOrig, s);
        
        if ( DEV && ! exExpected.equals(exprActual) ) {
            System.err.println("**** Test: "+name.getMethodName());
            System.err.println("::Expected::");
            System.err.println(exExpected);
            System.err.println("::Got::");
            System.err.println(exprActual);
        }
        assertEquals(exExpected, exprActual) ;
    }
    private static Set<Var> set(String[] varNames) {
        Set<Var> s = new HashSet<>() ;
        for ( String vn : varNames )
            s.add(Var.alloc(vn)) ;
        return s ;
    }
    private void rename(String inputStr, String expectedStr, String... varNames) {
        rename(inputStr, expectedStr, true, varNames);
    }
}
