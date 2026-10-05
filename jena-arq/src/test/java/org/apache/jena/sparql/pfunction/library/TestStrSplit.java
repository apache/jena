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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.pfunction.library;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.jena.query.*;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.RegexEngine;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStrSplit {
	private final static String prologue =
			"PREFIX apf: <http://jena.apache.org/ARQ/property#>\n";

	private QueryExecution qe;

	@Test public void shouldThrowQBEIfSubjectIsList() {
		assertQueryBuildException("SELECT ?x { (?x) apf:strSplit ('foo' ';') }");
	}

	@Test public void shouldThrowQBEIfObjectIsNotList() {
		assertQueryBuildException("SELECT ?x { ?x apf:strSplit 'foo' }");
	}

	@Test public void shouldThrowQBEIfWrongNumberOfArgsInObjectList() {
		assertQueryBuildException("SELECT ?x { ?x apf:strSplit () }");
		assertQueryBuildException("SELECT ?x { ?x apf:strSplit ('foo') }");
		assertQueryBuildException("SELECT ?x { ?x apf:strSplit ('foo' ';' 'i') }");
	}

	@Test public void shouldNotErrorOnSimpleQuery() {
		query("SELECT ?x { ?x apf:strSplit ('foo' ';') }");
		qe.execSelect();
		// No exception -- pass
	}

	@Test public void literalInputNonMatchingRegex() {
		query("SELECT ?x { ?x apf:strSplit ('foo' ';') }");
		assertAllX("foo");
	}

	@Test public void emptyStringInputNonMatchingRegex() {
		query("SELECT ?x { ?x apf:strSplit ('' ';') }");
		assertAllX("");
	}

	@Test public void literalInputMatchingRegex() {
		query("SELECT ?x { ?x apf:strSplit ('foo;bar' ';') }");
		assertAllX("foo", "bar");
	}

	@Test public void boundVariableInput() {
		query("SELECT ?x { BIND ('foo;bar' AS ?input) ?x apf:strSplit (?input ';') }");
		assertAllX("foo", "bar");
	}

	@Test public void unboundVariableInput() {
		query("SELECT ?x { ?x apf:strSplit (?unbound ';') }");
		assertNoResults();
	}

	@Test public void boundVariableRegex() {
		query("SELECT ?x { BIND (';' AS ?regex) ?x apf:strSplit ('foo;bar' ?regex) }");
		assertAllX("foo", "bar");
	}

	@Test public void badInputNodeShouldHaveNoResults() {
		query("SELECT ?x { ?x apf:strSplit (<foo> ';') }");
		assertNoResults();
		query("SELECT ?x { ?x apf:strSplit (_:foo ';') }");
		assertNoResults();
	}

	@Test public void badRegexNodeShouldHaveNoResults() {
		query("SELECT ?x { ?x apf:strSplit ('foo;bar' ?unbound) }");
		assertNoResults();
		query("SELECT ?x { ?x apf:strSplit ('foo;bar' <;>) }");
		assertNoResults();
		query("SELECT ?x { ?x apf:strSplit ('foo;bar' _:foo) }");
		assertNoResults();
	}

	@Test public void literalSubjectShouldMatchIfInSplitResults() {
		query("ASK { 'foo' apf:strSplit ('foo;bar' ';') }");
		assertAsk(true);
		query("ASK { 'bar' apf:strSplit ('foo;bar' ';') }");
		assertAsk(true);
		query("ASK { 'zzz' apf:strSplit ('foo;bar' ';') }");
		assertAsk(false);
	}

    public static Stream<Arguments> slowSplits() {
        return Stream.of(Arguments.of(StringUtils.repeat("abc,", 1_000_000), ",", 1_000_000));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("slowSplits")
    public void givenSlowRegex_whenSplittingWithTimeLimit_thenFails(String data, String pattern, int expectedResultCount) {
        long existingLimit = RegexEngine.MAX_REGEX_EVALUATION_TIME;
        try {
            RegexEngine.MAX_REGEX_EVALUATION_TIME = 1;
            query("SELECT ?x WHERE { ?x apf:strSplit ('" + data + "' '" + pattern + "') }");
            long start = System.currentTimeMillis();
            ExprEvalException e = assertThrows(ExprEvalException.class, () -> qe.execSelect().hasNext());
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed <= 250, "Elapsed time was " + elapsed);
            assertTrue(Strings.CI.contains(e.getMessage(), "time limit exceeded"));
        } finally {
            RegexEngine.MAX_REGEX_EVALUATION_TIME = existingLimit;
        }
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("slowSplits")
    public void givenSlowRegex_whenSplittingWithNoTimeLimit_thenEventuallySucceeds(String data, String pattern, int expectedResultCount) {
        long existingLimit = RegexEngine.MAX_REGEX_EVALUATION_TIME;
        try {
            // Given
            query("SELECT ?x WHERE { ?x apf:strSplit ('" + data + "' '" + pattern + "') }");

            // When and Then
            RegexEngine.MAX_REGEX_EVALUATION_TIME = -1;
            assertEquals(expectedResultCount, ResultSetFormatter.consume(qe.execSelect()));
        } finally {
            RegexEngine.MAX_REGEX_EVALUATION_TIME = existingLimit;
        }
    }

	private void assertQueryBuildException(String selectQueryString) {
		try {
			query(selectQueryString);
			qe.execSelect();
			fail("Expected QueryBuildException");
		} catch (QueryBuildException ex) {
			// pass
		}
	}

	private void query(String queryString) {
		qe = QueryExecutionFactory.create(
				prologue + queryString,
				ModelFactory.createDefaultModel());
	}

	private void assertAllX(String... literalValues) {
		List<Node> expectedNodes = new ArrayList<>();
		for (String value: literalValues) {
			expectedNodes.add(NodeFactory.createLiteralString(value));
		}
		ResultSet rs = qe.execSelect();
		List<Node> actualNodes = new ArrayList<>();
		while (rs.hasNext()) {
			actualNodes.add(rs.nextBinding().get(Var.alloc("x")));
		}
		assertArrayEquals(
				expectedNodes.toArray(new Node[expectedNodes.size()]),
				actualNodes.toArray(new Node[actualNodes.size()]));
	}

	private void assertNoResults() {
		assertFalse(qe.execSelect().hasNext());
	}

	private void assertAsk(boolean expected) {
		assertEquals(expected, qe.execAsk());
	}
}
