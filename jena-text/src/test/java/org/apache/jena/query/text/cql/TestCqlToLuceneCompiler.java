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

package org.apache.jena.query.text.cql;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.text.ShaclIndexMapping;
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclIndexMapping.FieldType;
import org.apache.jena.query.text.ShaclIndexMapping.IndexProfile;
import org.apache.lucene.search.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CQL-to-Lucene compiler: pushdown/residual split, field type mappings.
 */
public class TestCqlToLuceneCompiler {

    private CqlToLuceneCompiler compiler;

    @Before
    public void setUp() {
        FieldDef stateField = new FieldDef("state", FieldType.KEYWORD, null,
            true, true, true, false, false, false, Collections.emptySet());
        FieldDef yearField = new FieldDef("year", FieldType.INT, null,
            true, true, false, true, false, false, Collections.emptySet());
        FieldDef depthField = new FieldDef("depth", FieldType.DOUBLE, null,
            true, true, false, true, false, false, Collections.emptySet());
        FieldDef nameField = new FieldDef("name", FieldType.KEYWORD, null,
            true, true, false, false, false, false, Collections.emptySet());
        FieldDef notIndexedField = new FieldDef("notes", FieldType.TEXT, null,
            true, false, false, false, false, false, Collections.emptySet());

        IndexProfile profile = new IndexProfile(
            NodeFactory.createURI("http://example.org/Shape"),
            Collections.singleton(NodeFactory.createURI("http://example.org/Thing")),
            "uri", "docType",
            Arrays.asList(stateField, yearField, depthField, nameField, notIndexedField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(profile));
        compiler = new CqlToLuceneCompiler(mapping);
    }

    @Test
    public void testEqualKeyword() {
        CqlExpression expr = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlToLuceneCompiler.CompileResult r = compiler.compile(expr);

        assertNotNull("Should push down keyword equal", r.pushed());
        assertNull("No residual for indexed keyword", r.residual());
        assertTrue(r.pushed() instanceof TermQuery);
    }

    @Test
    public void testNotEqualKeyword() {
        CqlExpression expr = new CqlExpression.CqlComparison("<>", "state", "WA");
        CqlToLuceneCompiler.CompileResult r = compiler.compile(expr);

        assertNotNull(r.pushed());
        assertNull(r.residual());
        assertTrue(r.pushed() instanceof BooleanQuery);
    }

    @Test
    public void testEqualInt() {
        CqlExpression expr = new CqlExpression.CqlComparison("=", "year", 2020);
        CqlToLuceneCompiler.CompileResult r = compiler.compile(expr);

        assertNotNull(r.pushed());
        assertNull(r.residual());
    }

    @Test
    public void testGreaterThanInt() {
        CqlExpression expr = new CqlExpression.CqlComparison(">", "year", 2020);
        CqlToLuceneCompiler.CompileResult r = compiler.compile(expr);

        assertNotNull(r.pushed());
        assertNull(r.residual());
    }

    @Test
    public void testRangeDouble() {
        CqlExpression expr = new CqlExpression.CqlComparison(">=", "depth", 100.0);
        CqlToLuceneCompiler.CompileResult r = compiler.compile(expr);

        assertNotNull(r.pushed());
        assertNull(r.residual());
    }

    @Test
    public void testNonIndexedFieldIsResidual() {
        CqlExpression expr = new CqlExpression.CqlComparison("=", "notes", "important");
        CqlToLuceneCompiler.CompileResult r = compiler.compile(expr);

        assertNull("Non-indexed field should not push", r.pushed());
        assertNotNull("Should be residual", r.residual());
    }

    @Test
    public void testUnknownFieldIsResidual() {
        CqlExpression expr = new CqlExpression.CqlComparison("=", "nonexistent", "val");
        CqlToLuceneCompiler.CompileResult r = compiler.compile(expr);

        assertNull(r.pushed());
        assertNotNull(r.residual());
    }

    @Test
    public void testAndPartialPush() {
        // state=WA is pushable, notes=x is not (not indexed)
        CqlExpression pushable = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlExpression residual = new CqlExpression.CqlComparison("=", "notes", "important");
        CqlExpression and = new CqlExpression.CqlAnd(List.of(pushable, residual));

        CqlToLuceneCompiler.CompileResult r = compiler.compile(and);

        assertNotNull("Should push part of AND", r.pushed());
        assertNotNull("Should have residual for non-indexed field", r.residual());
    }

    @Test
    public void testAndFullPush() {
        CqlExpression a = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlExpression b = new CqlExpression.CqlComparison(">", "year", 2020);
        CqlExpression and = new CqlExpression.CqlAnd(List.of(a, b));

        CqlToLuceneCompiler.CompileResult r = compiler.compile(and);

        assertNotNull(r.pushed());
        assertNull("All pushable, no residual", r.residual());
    }

    @Test
    public void testOrAllPushable() {
        CqlExpression a = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlExpression b = new CqlExpression.CqlComparison("=", "state", "OR");
        CqlExpression or = new CqlExpression.CqlOr(List.of(a, b));

        CqlToLuceneCompiler.CompileResult r = compiler.compile(or);

        assertNotNull("Should push OR when all pushable", r.pushed());
        assertNull(r.residual());
    }

    @Test
    public void testOrPartiallyPushableBecomesResidual() {
        CqlExpression pushable = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlExpression residual = new CqlExpression.CqlComparison("=", "notes", "x");
        CqlExpression or = new CqlExpression.CqlOr(List.of(pushable, residual));

        CqlToLuceneCompiler.CompileResult r = compiler.compile(or);

        assertNull("OR with non-pushable child cannot push", r.pushed());
        assertNotNull(r.residual());
    }

    @Test
    public void testNotPushable() {
        CqlExpression inner = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlExpression not = new CqlExpression.CqlNot(inner);

        CqlToLuceneCompiler.CompileResult r = compiler.compile(not);

        assertNotNull(r.pushed());
        assertNull(r.residual());
        assertTrue(r.pushed() instanceof BooleanQuery);
    }

    @Test
    public void testNotResidual() {
        CqlExpression inner = new CqlExpression.CqlComparison("=", "notes", "x");
        CqlExpression not = new CqlExpression.CqlNot(inner);

        CqlToLuceneCompiler.CompileResult r = compiler.compile(not);

        assertNull(r.pushed());
        assertNotNull(r.residual());
    }

    @Test
    public void testInKeyword() {
        CqlExpression in = new CqlExpression.CqlIn("state", List.of("WA", "OR", "CA"));
        CqlToLuceneCompiler.CompileResult r = compiler.compile(in);

        assertNotNull(r.pushed());
        assertNull(r.residual());
        assertTrue(r.pushed() instanceof TermInSetQuery);
    }

    @Test
    public void testInNumeric() {
        CqlExpression in = new CqlExpression.CqlIn("year", List.of(2020, 2021, 2022));
        CqlToLuceneCompiler.CompileResult r = compiler.compile(in);

        assertNotNull(r.pushed());
        assertNull(r.residual());
    }

    @Test
    public void testBetweenInt() {
        CqlExpression btw = new CqlExpression.CqlBetween("year", 2020, 2025);
        CqlToLuceneCompiler.CompileResult r = compiler.compile(btw);

        assertNotNull(r.pushed());
        assertNull(r.residual());
    }

    @Test
    public void testBetweenDouble() {
        CqlExpression btw = new CqlExpression.CqlBetween("depth", 10.0, 100.0);
        CqlToLuceneCompiler.CompileResult r = compiler.compile(btw);

        assertNotNull(r.pushed());
        assertNull(r.residual());
    }

    @Test
    public void testLikeKeyword() {
        CqlExpression like = new CqlExpression.CqlLike("name", "Gold%");
        CqlToLuceneCompiler.CompileResult r = compiler.compile(like);

        assertNotNull(r.pushed());
        assertNull(r.residual());
        assertTrue(r.pushed() instanceof WildcardQuery);
    }

    @Test
    public void testSpatialAlwaysResidual() {
        CqlExpression spatial = new CqlExpression.CqlSpatial("s_intersects", "geometry", "{}");
        CqlToLuceneCompiler.CompileResult r = compiler.compile(spatial);

        assertNull(r.pushed());
        assertNotNull(r.residual());
    }
}
