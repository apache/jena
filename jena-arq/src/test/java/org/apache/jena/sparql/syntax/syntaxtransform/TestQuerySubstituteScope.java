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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;

/**
 * Variable usage for {@link QueryTransformOps}.
 */
public class TestQuerySubstituteScope {

    private Var varX = Var.alloc("X");
    private Var varY = Var.alloc("Y");
    private Var varZ = Var.alloc("Z");

    @Test public void valid_scope_01() {
        testScopeRestriction("SELECT * {}");
    }

    @Test public void valid_scope_02() {
        testScopeRestriction("SELECT * {}", varX);
    }

    @Test public void valid_scope_03() {
        testScopeRestriction("SELECT * { ?s ?p ?X}", varX);
    }

    @Test public void valid_scope_04() {
        testScopeRestriction("SELECT * { ?s ?p ?o BIND(123 AS ?n)}", varZ);
    }

    @Test public void valid_scope_05() {
        testScopeRestriction("SELECT * { VALUES ?z { 123 } }", varZ);
    }

    @Test public void valid_scope_06() {
        testScopeRestriction("SELECT * { VALUES ?z { 123 } } GROUP BY (str(?z) AS ?str)", varZ);
    }

    @Test public void valid_scope_07() {
        testScopeRestriction("SELECT (?z + 1 AS ?z2) { VALUES ?z { 123 } }", varX);
    }

    @Test public void valid_scope_08() {
        testScopeRestriction("SELECT ?X  { ?s ?p ?o }", varX);
    }

    @Test public void valid_scope_10() {
        testScopeRestriction("SELECT * { ?s ?p ?o { SELECT ?X { ?a ?b ?c }}}", varX, varY, varZ);
    }

    // subquery

    @Test public void invalid_scope_01() {
        testScopeRestrictionBad("SELECT * { BIND(123 AS ?X) }", varX);
    }

    @Test public void invalid_scope_02() {
        testScopeRestrictionBad("SELECT (123 AS ?X) { ?s ?p ?o }", varX);
    }

    @Test public void invalid_scope_03() {
        testScopeRestrictionBad("SELECT * { VALUES ?X { 123 } ?s ?p ?o }", varX);
    }

    @Test public void invalid_scope_04() {
        testScopeRestrictionBad("SELECT * { ?s ?p ?o } GROUP BY (str(?p) AS ?X)", varX);
    }

    @Test public void invalid_scope_05() {
        testScopeRestrictionBad("SELECT (count(*) as ?Z) { ?s ?p ?o } GROUP BY ?s", varX, varY, varZ);
    }

    @Test public void invalid_scope_06() {
        testScopeRestriction("SELECT * { ?s ?p ?o { SELECT (?c +1 AS ?X) { ?a ?b ?c }}}", varX, varY, varZ);
    }

    private void testScopeRestriction(String queryString, Var...vars ) {
        var query = QueryFactory.create(queryString);
        var varsList = Arrays.asList(vars);
        QuerySyntaxSubstituteScope.scopeCheck(query, varsList);
    }

    private void testScopeRestrictionBad(String queryString, Var...vars ) {
        assertThrows(QueryScopeException.class, ()->testScopeRestriction(queryString, vars));
    }

}
