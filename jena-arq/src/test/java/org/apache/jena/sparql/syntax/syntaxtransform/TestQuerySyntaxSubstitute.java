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

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;

public class TestQuerySyntaxSubstitute {

    private static Map<Var, Node> substitutions1 = Map.of(Var.alloc("x"), NodeFactory.createURI("http://example/xxx"));
    private static Map<Var, Node> substitutions2 = orderedMapOf(Var.alloc("x"), NodeFactory.createURI("http://example/xxx"),
                                                                Var.alloc("y"), NodeFactory.createURI("http://example/yyy"));
    private static Map<Var, Var> varRenames = Map.of(Var.alloc("x"), Var.alloc("x1"),
                                                     Var.alloc("y"), Var.alloc("y1"));

    private static <K, V> Map<K, V> orderedMapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1,v1);
        map.put(k2, v2);
        return map;
    }

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
    // Sub-query with a visible variable and a hidden variable
    @Test public void syntaxSubstitute_12() {
        testSubstitute("SELECT * { ?s ?p ?o { SELECT ?x { ?x :p ?y } } }", substitutions2,
                       "SELECT ?s ?p ?o ?x (:yyy AS ?y) { ?s ?p ?o { SELECT (:xxx AS ?x) { :xxx :p :yyy } }}"
                );
    }

    @Test public void syntaxSubstitute_13() {
        testSubstitute("SELECT * { ?s ?p ?o { SELECT * { ?s ?p ?o . ?x :p ?y } } }", substitutions2,
                       "SELECT ?s ?p ?o (:xxx AS ?x) (:yyy AS ?y) { ?s ?p ?o { SELECT * { ?s ?p ?o . :xxx :p :yyy } }}"
                );
    }

    // Multi-level variable.
    @Test public void syntaxSubstitute_14() {
        testSubstitute("SELECT * { ?x ?p ?o { SELECT * { ?x :p ?z } } }", substitutions2,
                       "SELECT ?p ?o ?z (:xxx AS ?x) (:yyy AS ?y) { :xxx ?p ?o { SELECT * { :xxx :p ?z } }}"
                      );
    }

    // ==== Other query forms

    @Test public void syntaxSubstitute_describe_1() {
        testSubstitute("DESCRIBE ?x", substitutions1, "DESCRIBE :xxx");
    }

    @Test public void syntaxSubstitute_describe_2() {
        testSubstitute("DESCRIBE ?x {}", substitutions1, "DESCRIBE :xxx {}");
    }

    @Test public void syntaxSubstitute_describe_3() {
        testSubstitute("DESCRIBE ?x ?y { ?x :p ?y }", substitutions1, "DESCRIBE :xxx ?y { :xxx :p ?y }");
    }

    @Test public void syntaxSubstitute_construct_1() {
        testSubstitute("CONSTRUCT {} WHERE {}", substitutions1, "CONSTRUCT {} WHERE {}");
    }

    @Test public void syntaxSubstitute_construct_2() {
        testSubstitute("CONSTRUCT { ?x :p ?y } WHERE {?x :p ?y }", substitutions1, "CONSTRUCT { :xxx :p ?y } WHERE { :xxx :p ?y }");
    }

    @Test public void syntaxSubstitute_ask_1() {
        testSubstitute("ASK { ?x :p ?y}" , substitutions1, "ASK { :xxx :p ?y }");
    }

    // ==== Variable-variable renaming.
    // This is always possible so no scoping checks are done.

    @Test public void syntaxSubstituteVarToVar_01() {
        testSubstituteVars("SELECT ?x { ?x :p ?z }", varRenames, "SELECT ?x1 { ?x1 :p ?z }");
    }

    @Test public void syntaxSubstituteVarToVar_02() {
        testSubstituteVars("SELECT ?x { ?x :p ?y }", varRenames, "SELECT ?x1 { ?x1 :p ?y1 }");
    }

    @Test public void syntaxSubstituteVarToVar_03() {
        testSubstituteVars("SELECT ?z ?y { ?a ?b ?z { SELECT ?y { :s :p ?y } GROUP BY ?y} }", varRenames,
                           "SELECT ?z ?y1 { ?a ?b ?z { SELECT ?y1 { :s :p ?y1 } GROUP BY ?y1} }" );
    }

    // Var to var where var to constant is not allowed.
    @Test public void syntaxSubstituteVarToVar_10() {
        testSubstituteVars("SELECT ?x { ?x ?c ?d FILTER( ?x != ?b ) }", varRenames,
                           "SELECT ?x1 { ?x1 ?c ?d FILTER( ?x1 != ?b ) }");
    }

    @Test public void syntaxSubstituteVarToVar_11() {
        // Still hitting the scope check.
        testSubstituteVars("SELECT * { BIND (:q AS ?x) BIND (?x + 99 AS ?A) }", varRenames,
                           "SELECT * { BIND (:q AS ?x1) BIND (?x1 + 99 AS ?A) }");
    }

    @Test public void syntaxSubstituteVarToVar_12() {
        // Still hitting the scope check.
        testSubstituteVars("SELECT * { VALUES ?x { :q } }", varRenames,
                           "SELECT * { VALUES ?x1 { :q } }");
    }

    // ==== Scope failures.

    @Test public void syntaxSubstituteScopeEx_01() {
        assertThrows(QueryScopeException.class, ()->
            testSubstitute("SELECT (456 AS ?x) { ?y :p ?z }",  substitutions1,
                          ""
                          ));
    }

    @Test public void syntaxSubstituteScopeEx_02() {
        assertThrows(QueryScopeException.class, ()->
            testSubstitute("SELECT * { ?y :p ?z BIND(789 AS ?x)}", substitutions1,
                          ""
                          ));
    }

    private void testSubstitute(String qs, Map<Var, ? extends Node> substitutions, String outcome) {
        String prologue = "PREFIX : <http://example/> ";
        String queryString = prologue+qs;
        Query query = QueryFactory.create(queryString);
        Query query2 = QueryTransformOps.syntaxSubstitute(query, substitutions);    // syntaxSubstitute, including modifying the SELECT clause
        String queryOutcomeString = prologue+outcome;
        Query queryOutcome = QueryFactory.create(queryOutcomeString);
        assertEquals(queryOutcome,  query2);
    }

    private void testSubstituteVars(String qs, Map<Var, ? extends Node> substitutions, String outcome) {
        String prologue = "PREFIX : <http://example/> ";
        String queryString = prologue+qs;
        Query query = QueryFactory.create(queryString);
        Query query2 = QueryTransformOps.replaceVars(query, substitutions);     // replaceVars
        Query queryOutcome = QueryFactory.create(prologue+outcome);
        assertEquals(queryOutcome,  query2);
    }
}
