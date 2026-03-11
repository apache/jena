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

import org.apache.jena.query.text.TextIndexException;
import org.junit.Test;

/**
 * Tests for CQL2-JSON parsing.
 */
public class TestCqlParser {

    @Test
    public void testParseEqual() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"=\",\"args\":[{\"property\":\"state\"},\"WA\"]}");

        assertInstanceOf(CqlExpression.CqlComparison.class, expr);
        CqlExpression.CqlComparison cmp = (CqlExpression.CqlComparison) expr;
        assertEquals("=", cmp.op());
        assertEquals("state", cmp.property());
        assertEquals("WA", cmp.value());
    }

    @Test
    public void testParseNotEqual() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"<>\",\"args\":[{\"property\":\"status\"},\"closed\"]}");

        CqlExpression.CqlComparison cmp = (CqlExpression.CqlComparison) expr;
        assertEquals("<>", cmp.op());
        assertEquals("status", cmp.property());
    }

    @Test
    public void testParseLessThan() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"<\",\"args\":[{\"property\":\"year\"},2020]}");

        CqlExpression.CqlComparison cmp = (CqlExpression.CqlComparison) expr;
        assertEquals("<", cmp.op());
        assertEquals("year", cmp.property());
        assertEquals(2020, cmp.value());
    }

    @Test
    public void testParseGreaterThanEqual() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\">=\",\"args\":[{\"property\":\"depth\"},100.5]}");

        CqlExpression.CqlComparison cmp = (CqlExpression.CqlComparison) expr;
        assertEquals(">=", cmp.op());
        assertEquals("depth", cmp.property());
        assertEquals(100.5, cmp.value());
    }

    @Test
    public void testParseAnd() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"and\",\"args\":[" +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"state\"},\"WA\"]}," +
            "  {\"op\":\">\",\"args\":[{\"property\":\"year\"},2020]}" +
            "]}");

        assertInstanceOf(CqlExpression.CqlAnd.class, expr);
        CqlExpression.CqlAnd and = (CqlExpression.CqlAnd) expr;
        assertEquals(2, and.args().size());
    }

    @Test
    public void testParseOr() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"or\",\"args\":[" +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"state\"},\"WA\"]}," +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"state\"},\"OR\"]}" +
            "]}");

        assertInstanceOf(CqlExpression.CqlOr.class, expr);
        CqlExpression.CqlOr or = (CqlExpression.CqlOr) expr;
        assertEquals(2, or.args().size());
    }

    @Test
    public void testParseNot() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"not\",\"args\":[" +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"status\"},\"draft\"]}" +
            "]}");

        assertInstanceOf(CqlExpression.CqlNot.class, expr);
        CqlExpression.CqlNot not = (CqlExpression.CqlNot) expr;
        assertInstanceOf(CqlExpression.CqlComparison.class, not.arg());
    }

    @Test
    public void testParseIn() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"in\",\"args\":[{\"property\":\"state\"},[\"WA\",\"OR\",\"CA\"]]}");

        assertInstanceOf(CqlExpression.CqlIn.class, expr);
        CqlExpression.CqlIn in = (CqlExpression.CqlIn) expr;
        assertEquals("state", in.property());
        assertEquals(3, in.values().size());
    }

    @Test
    public void testParseBetween() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"between\",\"args\":[{\"property\":\"year\"},[2020,2025]]}");

        assertInstanceOf(CqlExpression.CqlBetween.class, expr);
        CqlExpression.CqlBetween btw = (CqlExpression.CqlBetween) expr;
        assertEquals("year", btw.property());
        assertEquals(2020, btw.lower());
        assertEquals(2025, btw.upper());
    }

    @Test
    public void testParseLike() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"like\",\"args\":[{\"property\":\"name\"},\"Gold%\"]}");

        assertInstanceOf(CqlExpression.CqlLike.class, expr);
        CqlExpression.CqlLike like = (CqlExpression.CqlLike) expr;
        assertEquals("name", like.property());
        assertEquals("Gold%", like.pattern());
    }

    @Test
    public void testParseSpatial() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},{\"type\":\"Point\",\"coordinates\":[1,2]}]}");

        assertInstanceOf(CqlExpression.CqlSpatial.class, expr);
        CqlExpression.CqlSpatial sp = (CqlExpression.CqlSpatial) expr;
        assertEquals("s_intersects", sp.op());
        assertEquals("geometry", sp.property());
    }

    @Test
    public void testParseNestedAndOr() {
        CqlExpression expr = CqlParser.parse(
            "{\"op\":\"and\",\"args\":[" +
            "  {\"op\":\"or\",\"args\":[" +
            "    {\"op\":\"=\",\"args\":[{\"property\":\"a\"},\"1\"]}," +
            "    {\"op\":\"=\",\"args\":[{\"property\":\"a\"},\"2\"]}" +
            "  ]}," +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"b\"},\"3\"]}" +
            "]}");

        CqlExpression.CqlAnd and = (CqlExpression.CqlAnd) expr;
        assertEquals(2, and.args().size());
        assertInstanceOf(CqlExpression.CqlOr.class, and.args().get(0));
    }

    @Test
    public void testCanonicalOrderIndependent() {
        CqlExpression and1 = CqlParser.parse(
            "{\"op\":\"and\",\"args\":[" +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"a\"},\"1\"]}," +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"b\"},\"2\"]}" +
            "]}");

        CqlExpression and2 = CqlParser.parse(
            "{\"op\":\"and\",\"args\":[" +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"b\"},\"2\"]}," +
            "  {\"op\":\"=\",\"args\":[{\"property\":\"a\"},\"1\"]}" +
            "]}");

        assertEquals(and1.toCanonical(), and2.toCanonical());
    }

    @Test
    public void testInValuesSorted() {
        CqlExpression in = CqlParser.parse(
            "{\"op\":\"in\",\"args\":[{\"property\":\"state\"},[\"CA\",\"WA\",\"OR\"]]}");

        CqlExpression.CqlIn inExpr = (CqlExpression.CqlIn) in;
        // Values should be sorted in the record
        assertEquals("CA", inExpr.values().get(0));
        assertEquals("OR", inExpr.values().get(1));
        assertEquals("WA", inExpr.values().get(2));
    }

    @Test(expected = TextIndexException.class)
    public void testParseMissingOp() {
        CqlParser.parse("{\"args\":[{\"property\":\"a\"},\"1\"]}");
    }

    @Test(expected = TextIndexException.class)
    public void testParseMissingArgs() {
        CqlParser.parse("{\"op\":\"=\"}");
    }

    @Test(expected = TextIndexException.class)
    public void testParseUnknownOp() {
        CqlParser.parse("{\"op\":\"unknown\",\"args\":[{\"property\":\"a\"},\"1\"]}");
    }

    private static void assertInstanceOf(Class<?> expected, Object actual) {
        assertTrue("Expected " + expected.getSimpleName() + " but got " + actual.getClass().getSimpleName(),
            expected.isInstance(actual));
    }
}
