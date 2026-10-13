/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;

public class TestGH4300LangString {
    private static final String BMP = Character.toString(0xFFFD);
    private static final String ASTRAL = Character.toString(0x1D11E);

    private static String literal(String value, String tag) {
        return "'" + value + "'@" + tag;
    }

    private static boolean expression(String expression) {
        String query = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> SELECT (" + expression + " AS ?result) WHERE {}";
        try (QueryExecution exec = QueryExecutionFactory.create(query, DatasetFactory.empty())) {
            return exec.execSelect().next().getLiteral("result").getBoolean();
        }
    }

    @Test public void sameLanguageLessThan() {
        assertTrue(expression(literal(BMP, "en") + " < " + literal(ASTRAL, "en")));
    }

    @Test public void sameLanguageGreaterThan() {
        assertTrue(expression(literal(ASTRAL, "en") + " > " + literal(BMP, "en")));
    }

    @Test public void sameLanguageOrderBy() {
        String query = "SELECT ?v WHERE { VALUES ?v { " + literal(ASTRAL, "en") + " " + literal(BMP, "en") + " } } ORDER BY ?v";
        List<String> actual = new ArrayList<>();
        try (QueryExecution exec = QueryExecutionFactory.create(query, DatasetFactory.empty())) {
            ResultSet results = exec.execSelect();
            while (results.hasNext())
                actual.add(results.next().getLiteral("v").getLexicalForm());
        }
        assertEquals(List.of(BMP, ASTRAL), actual);
    }

    @Test public void equalityAndMembership() {
        assertTrue(expression(literal(BMP, "en") + " = " + literal(BMP, "EN")));
        assertTrue(expression(literal(BMP, "en") + " != " + literal(ASTRAL, "en")));
        assertTrue(expression(literal(BMP, "en") + " IN (" + literal(ASTRAL, "en") + ", " + literal(BMP, "en") + ")"));
        assertTrue(expression("'" + BMP + "'^^xsd:string = '" + BMP + "'"));
        assertTrue(expression("'" + BMP + "' != '" + ASTRAL + "'"));
        assertTrue(expression("'" + BMP + "' IN ('" + ASTRAL + "', '" + BMP + "'^^xsd:string)"));
    }

    @Test public void differentLanguagesKeepComparisonContract() {
        assertThrows(ExprEvalException.class, () -> TestExpressions2.eval(literal(BMP, "en") + " < " + literal(ASTRAL, "fr")));
        assertTrue(expression(literal(BMP, "en") + " != " + literal(BMP, "fr")));
    }

    @Test public void orderByDifferentLanguagesStillUsesLanguageFirst() {
        String query = "SELECT ?v WHERE { VALUES ?v { " + literal(BMP, "fr") + " " + literal(ASTRAL, "en") + " } } ORDER BY ?v";
        List<String> tags = new ArrayList<>();
        try (QueryExecution exec = QueryExecutionFactory.create(query, DatasetFactory.empty())) {
            ResultSet results = exec.execSelect();
            while (results.hasNext())
                tags.add(results.next().getLiteral("v").getLanguage());
        }
        assertEquals(List.of("en", "fr"), tags);
    }
}
