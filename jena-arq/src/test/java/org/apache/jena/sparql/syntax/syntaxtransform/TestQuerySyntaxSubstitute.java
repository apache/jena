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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;

public class TestQuerySyntaxSubstitute {

    private static Map<Var, Node> substitutions1 = Map.of(Var.alloc("x"), NodeFactory.createURI("http://example/xxx"));
    private static Map<Var, Node> substitutions2 = Map.of(Var.alloc("x"), NodeFactory.createURI("http://example/xxx"),
                                                          Var.alloc("y"), NodeFactory.createURI("http://example/yyy"));

    @Test public void syntaxSubstitute_01() {
        testSubstitute("SELECT * { ?x :p ?z }", substitutions1,
                      "SELECT ?z (:xxx AS ?x) { :xxx :p ?z }"
                      );
    }

    @Test public void syntaxSubstitute_02() {
        testSubstitute("SELECT ?x { ?x :p ?z }", substitutions1,
                      "SELECT (:xxx AS ?x) { :xxx :p ?z }"
                      );
    }

    @Test public void syntaxSubstitute_03() {
        testSubstitute("SELECT ?z { :a :p ?z }", substitutions1,
                      "SELECT ?z (:xxx AS ?x) { :a :p ?z }"
                      );
    }

    @Test public void syntaxSubstitute_04() {
        testSubstitute("SELECT ?x ?z { ?x :p ?z }", substitutions1,
                      "SELECT (:xxx AS ?x) ?z { :xxx :p ?z }"
                      );
    }

    @Test public void syntaxSubstitute_10() {
        testSubstitute("SELECT ?y ?x { ?x :p ?y }", substitutions2,
                      "SELECT (:yyy AS ?y) (:xxx AS ?x) { :xxx :p :yyy }"
                      );
    }

    @Test public void syntaxSubstitute_11() {
        testSubstitute("SELECT ?y ?p ?x { ?x ?p ?y }", substitutions2,
                      "SELECT (:yyy AS ?y) ?p (:xxx AS ?x) { :xxx ?p :yyy }"
                      );
    }

    // GH-2799: Sub-queries not yet ready.
//    // Sub-query visible variable.
//    @Test public void syntaxSubstitute_12() {
//        testSubstitute("SELECT * { ?s ?p ?o { SELECT ?x { ?x :p ?y } } }", substitutions1,
//                      "SELECT (:yyy AS ?y) ?p (:xxx AS ?x) { ?s ?p ?o { SELECT * { :xxx :p ?y } }}"
//                      );
//    }
//
//    // Sub-query hidden variable.
//    @Test public void syntaxSubstitute_13() {
//        testSubstitute("SELECT * { ?s ?p ?o { SELECT ?y { ?x :p ?y } } }", substitutions1,
//                      "SELECT ?s ?p ?o (:xxx AS ?x) { ?s ?p ?o { SELECT * { :xxx :p ?y } }}"
//                      );
//    }
//
//    // Multi-level variable.
//    @Test public void syntaxSubstitute_14() {
//        testSubstitute("SELECT * { ?x ?p ?o { SELECT * { ?x :p ?y } } }", substitutions2,
//                      "" //"SELECT (:yyy AS ?y) ?p (:xxx AS ?x) { ?s ?p ?o { SELECT * { :xxx :p ?y } }}"
//                      );
//    }

    @Test public void syntaxSubstitute_50() {
        assertThrows(QueryScopeException.class, ()->
            testSubstitute("SELECT (456 AS ?x) { ?y :p ?z }",  substitutions1,
                          ""
                          ));
    }

    @Test public void syntaxSubstitute_51() {
        assertThrows(QueryScopeException.class, ()->
            testSubstitute("SELECT * { ?y :p ?z BIND(789 AS ?x)}", substitutions1,
                          ""
                          ));
    }

    private void testSubstitute(String qs, Map<Var, Node> substitutions, String outcome) {
        String prologue = "PREFIX : <http://example/> ";
        String queryString = prologue+qs;
        Query query = QueryFactory.create(queryString);
        Query query2 = QueryTransformOps.syntaxSubstitute(query, substitutions);
        Query queryOutcome = QueryFactory.create(prologue+outcome);
        assertEquals(queryOutcome,  query2);
    }
}
