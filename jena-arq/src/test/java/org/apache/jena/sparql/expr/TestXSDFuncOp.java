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

package org.apache.jena.sparql.expr;

import static org.junit.jupiter.api.Assertions.*;

//import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.nodevalue.*;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.XSDNumUtils;

public class TestXSDFuncOp {
    private static final double accuracyExact_D = 0.0d;
    private static final float  accuracyExact_F = 0.0f;
    private static final double accuracyClose_D = 0.000001d;
    private static final float  accuracyClose_F = 0.000001f;

    @Test public void lex_decimal_1() {
        lex_decimal_value(BigDecimal.valueOf(0), "0.0");
    }

    @Test public void lex_decimal_2() {
        lex_decimal_value(BigDecimal.valueOf(1), "1.0");
    }

    @Test public void lex_decimal_3() {
        lex_decimal_value(BigDecimal.valueOf(0.5), "0.5");
    }

    @Test public void lex_decimal_4() {
        lex_decimal_value(BigDecimal.valueOf(-0.5), "-0.5");
    }

    @Test public void lex_decimal_5() {
        lex_decimal_value(BigDecimal.valueOf(1_000_000_000_000_000L), "1000000000000000.0");
    }

    @Test public void lex_decimal_6() {
        lex_decimal_value(BigDecimal.valueOf(-1_000_000_000_000_000L), "-1000000000000000.0");
    }

    @Test public void lex_decimal_canonical_1() {
        lex_decimal_canonical("+.0", "0.0");
    }

    @Test public void lex_decimal_canonical_2() {
        lex_decimal_canonical("-.0", "0.0");
    }

    @Test public void lex_decimal_canonical_3() {
        lex_decimal_canonical("0010", "10.0");
    }

    @Test public void lex_decimal_canonical_4() {
        lex_decimal_canonical("0012.0000", "12.0");
    }

    @Test public void lex_decimal_canonical_5() {
        lex_decimal_canonical("-0012.0000", "-12.0");
    }

    // Exact given lexical form preserved.
    @Test public void lex_decimal_nodevalue_1() {
        lex_decimal_nodevalue("0.0", "0.0");
    }

    @Test public void lex_decimal_nodevalue_2() {
        // As input.
        lex_decimal_nodevalue("0.", "0.");
    }

    @Test public void lex_decimal_nodevalue3() {
        // As input.
        lex_decimal_nodevalue("+.0", "+.0");
    }

    private static void lex_decimal_value(BigDecimal decimal, String expected) {
        String lex = XSDNumUtils.stringFormatARQ(decimal);
        assertEquals(expected, lex);
    }

    private static void lex_decimal_nodevalue(String input, String expected) {
        NodeValue nv = NodeValue.makeDecimal(input);
        String lex = nv.asString();
        assertEquals(expected, lex);
    }

    private static void lex_decimal_canonical(String input, String expected) {
        BigDecimal decimal = new BigDecimal(input);
        String lex = XSDNumUtils.stringFormatARQ(decimal);
        assertEquals(expected, lex);
    }

    // These add tests also test that the right kind of operation was done.

    @Test public void testAddIntegerInteger() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeInteger(7);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isInteger(), "Not an integer: " + r);
        assertTrue(r instanceof NodeValueInteger, "Not a NodeValueInteger: " + r);
        assertEquals(12, r.getInteger().longValue(), "Wrong result");
    }

    @Test public void testAddDecimalDecimal() {
        NodeValue nv1 = NodeValue.makeDecimal(4.3);
        NodeValue nv2 = NodeValue.makeDecimal(3.7);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDecimal(), "Not a decimal: " + r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: " + r);
        assertEquals(8d, r.getDecimal().doubleValue(), accuracyExact_D, "Wrong result");
    }

    @Test public void testAddFloatFloat() {
        NodeValue nv1 = NodeValue.makeFloat(7.5f);
        NodeValue nv2 = NodeValue.makeFloat(2.5f);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a float: " + r);
        assertTrue(r instanceof NodeValueFloat, "Not a NodeValueFloat: " + r);
        assertEquals(10f, r.getFloat(), accuracyExact_F, "Wrong result");
        assertEquals(10d, r.getDouble(), accuracyExact_D, "Wrong result (as doubles)");
    }

    @Test public void testAddDoubleDouble() {
        NodeValue nv1 = NodeValue.makeDouble(7.5);
        NodeValue nv2 = NodeValue.makeDouble(2.5);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(10d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testAddIntegerDecimal() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeDecimal(7);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDecimal(), "Not a decimal: " + r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: " + r);
        assertEquals(12, r.getDecimal().longValue(), "Wrong result");
    }

    @Test public void testAddDecimalInteger() {
        NodeValue nv1 = NodeValue.makeDecimal(7);
        NodeValue nv2 = NodeValue.makeInteger(5);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDecimal(), "Not a decimal: " + r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: " + r);
        assertEquals(12, r.getDecimal().longValue(), "Wrong result");
    }

    @Test public void testAddIntegerFloat() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeFloat(7);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isFloat(), "Not a float: " + r);
        assertTrue(r instanceof NodeValueFloat, "Not a NodeValueFloat: " + r);
        assertEquals(12d, r.getDouble(), accuracyExact_F, "Wrong result");
    }

    @Test public void testAddFloatInteger() {
        NodeValue nv1 = NodeValue.makeFloat(7);
        NodeValue nv2 = NodeValue.makeInteger(5);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isFloat(), "Not a float: " + r);
        assertTrue(r instanceof NodeValueFloat, "Not a NodeValueFloat: " + r);
        assertEquals(12d, r.getDouble(), accuracyExact_F, "Wrong result");
    }

    @Test public void testAddIntegerDouble() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeDouble(7);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(12d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testAddDoubleInteger() {
        NodeValue nv1 = NodeValue.makeDouble(7);
        NodeValue nv2 = NodeValue.makeInteger(5);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(12d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testAddDecimalFloat() {
        NodeValue nv1 = NodeValue.makeDecimal(3.5);
        NodeValue nv2 = NodeValue.makeFloat(4.5f);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isFloat(), "Not a Float: " + r);
        assertTrue(r instanceof NodeValueFloat, "Not a NodeValueFloat: " + r);
        assertEquals(8f, r.getFloat(), accuracyExact_F, "Wrong result");
    }

    @Test public void testAddFloatDecimal() {
        NodeValue nv1 = NodeValue.makeFloat(4.5f);
        NodeValue nv2 = NodeValue.makeDecimal(3.5);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isFloat(), "Not a Float: " + r);
        assertTrue(r instanceof NodeValueFloat, "Not a NodeValueFloat: " + r);
        assertEquals(8f, r.getFloat(), accuracyExact_F, "Wrong result");
    }

    @Test public void testAddDecimalDouble() {
        NodeValue nv1 = NodeValue.makeDecimal(3.5);
        NodeValue nv2 = NodeValue.makeDouble(4.5);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(8d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testAddDoubleDecimal() {
        NodeValue nv1 = NodeValue.makeDouble(4.5);
        NodeValue nv2 = NodeValue.makeDecimal(3.5);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(8d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testAddDoubleFloat() {
        NodeValue nv1 = NodeValue.makeDouble(4.5);
        NodeValue nv2 = NodeValue.makeFloat(3.5f);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(8d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testAddFloatDouble() {
        NodeValue nv1 = NodeValue.makeFloat(4.5f);
        NodeValue nv2 = NodeValue.makeDouble(3.5d);
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(8d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    // divide(integer, integer) => decimal
    @Test public void testDivideIntegerInteger() {
        NodeValue nv1 = NodeValue.makeInteger(25);
        NodeValue nv2 = NodeValue.makeInteger(2);
        NodeValue r = XSDFuncOp.numDivide(nv1, nv2);
        assertTrue(r.isDecimal(), "Not a decimal: " + r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: " + r);
        assertEquals(12.5d, r.getDecimal().doubleValue(), accuracyExact_D, "Wrong result");
    }

    private static String divideDecimal(String v1, String v2, String v3) {
        NodeValue nv1 = NodeValue.makeDecimal(v1);
        NodeValue nv2 = NodeValue.makeDecimal(v2);
        NodeValue nv3 = NodeValue.makeDecimal(v3);
        NodeValue r = XSDFuncOp.numDivide(nv1, nv2);
        assertTrue(r.isDecimal(), "Not a decimal: " + r);
        // sameAv (value) test : does not test lexical form or datatype.
        assertTrue(NodeValue.sameValueAs(r, nv3), "Wrong result : expected=" + r + " : got=" + nv3);
        return r.asNode().getLiteralLexicalForm();
    }

    @Test public void testDivideDecimal1() {
        divideDecimal("1", "10", "0.1");
    }

    @Test public void testDivideDecimal2() {
        divideDecimal("1", "2", "0.5");
    }

    @Test public void testDivideDecimal3() {
        // Depends on XSDFuncOp.DIVIDE_PRECISION = 24
        String x = divideDecimal("1", "3", "0.333333333333333333333333");
        assertEquals(26, x.length(), "Wrong lexical form length");
    }

    @Test public void testDivideDecimal4() {
        String x = divideDecimal("0", "3", "0");
        assertEquals("0.0", x, "Wrong lexical form");
    }

    // JENA-1943 : If exact, return more than DIVIDE_PRECISION
    @Test public void testDivideDecimal5() {
        String x = divideDecimal("1", "10000000000000000000000000", "0.0000000000000000000000001");
        // More than 2 (for the "0.") plus XSDFuncOp.DIVIDE_PRECISION = 24
        assertEquals(27, x.length(), "Wrong length lexical form");
    }

    // JENA-1943
    @Test public void testDivideDecimal6() {
        String x = divideDecimal("1", "10000000000000000000000000000", "0.0000000000000000000000000001");
        // Exact
        assertEquals(30, x.length(), "Wrong length lexical form");
    }

    // divide errors
    @Test
    public void testDivideByZero1() {
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeInteger(0);
        assertThrows(ExprEvalException.class, ()->XSDFuncOp.numDivide(nv1, nv2));
    }

    @Test public void testDivideByZero2() {
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeDouble(0);
        NodeValue r = XSDFuncOp.numDivide(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r.getDouble() == Double.POSITIVE_INFINITY, "Not a +INF: " + r);
    }

    @Test public void testDivideByZero4() {
        NodeValue nv1 = NodeValue.makeInteger(-1);
        NodeValue nv2 = NodeValue.makeDouble(-0);
        NodeValue r = XSDFuncOp.numDivide(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r.getDouble() == Double.NEGATIVE_INFINITY, "Not a -INF: " + r);
    }

    @Test
    public void testDivideByZero5() {
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeDecimal(0);
        assertThrows(ExprEvalException.class, ()->XSDFuncOp.numDivide(nv1, nv2));
    }

    @Test
    public void testDivideByZero6() {
        NodeValue nv1 = NodeValue.makeDecimal(1);
        NodeValue nv2 = NodeValue.makeDecimal(0);
        assertThrows(ExprEvalException.class, ()->XSDFuncOp.numDivide(nv1, nv2));
    }

    @Test public void testSubtractDoubleDecimal() {
        NodeValue nv1 = NodeValue.makeDouble(4.5);
        NodeValue nv2 = NodeValue.makeDecimal(3.5);
        NodeValue r = XSDFuncOp.numSubtract(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(1d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testSubtractDecimalInteger() {
        NodeValue nv1 = NodeValue.makeDecimal(3.5);
        NodeValue nv2 = NodeValue.makeInteger(2);
        NodeValue r = XSDFuncOp.numSubtract(nv1, nv2);
        assertTrue(r.isDecimal(), "Not a decimal: " + r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: " + r);
        assertTrue(NodeValue.sameValueAs(NodeValue.makeDecimal(1.5), r), "Wrong result");
    }

    @Test public void testMultiplyDoubleDecimal() {
        NodeValue nv1 = NodeValue.makeDouble(4.5);
        NodeValue nv2 = NodeValue.makeDecimal(3.5);
        NodeValue r = XSDFuncOp.numMultiply(nv1, nv2);
        assertTrue(r.isDouble(), "Not a double: " + r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: " + r);
        assertEquals(4.5d * 3.5d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testMultiplyDecimalInteger() {
        NodeValue nv1 = NodeValue.makeDecimal(3.5);
        NodeValue nv2 = NodeValue.makeInteger(2);
        NodeValue r = XSDFuncOp.numMultiply(nv1, nv2);
        assertTrue(r.isDecimal(), "Not a decimal: " + r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: " + r);
        assertEquals(7L, r.getDecimal().longValue(), "Wrong result");
    }

    @Test public void testCompare1() {
        NodeValue nv5 = NodeValue.makeInteger(5);
        NodeValue nv7 = NodeValue.makeInteger(7);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7), "Does not compare " + nv5 + " & " + nv7);

        NodeValue nv5b = NodeValue.makeInteger(5);
        assertEquals(NodeValue.CMP_EQUAL, NodeValue.compare(nv5, nv5b), "Does not compare " + nv5 + " & " + nv5b);
    }

    @Test public void testCompare2() {
        NodeValue nv5 = NodeValue.makeInteger(5);
        NodeValue nv7 = NodeValue.makeNodeInteger(7);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7), "Does not compare " + nv5 + " & " + nv7);

        NodeValue nv5b = NodeValue.makeNodeInteger(5);
        assertEquals(NodeValue.CMP_EQUAL, NodeValue.compare(nv5, nv5b), "Does not compare " + nv5 + " & " + nv5b);
    }

    @Test public void testCompare3() {
        NodeValue nv5 = NodeValue.makeInteger(5);
        NodeValue nv7 = NodeValue.makeDouble(7);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7), "Does not compare " + nv5 + " & " + nv7);
    }

    @Test public void testCompare4() {
        NodeValue nv5 = NodeValue.makeInteger(5);
        NodeValue nv7 = NodeValue.makeFloat(7);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7), "Does not compare " + nv5 + " & " + nv7);
    }

    @Test public void testCompare5() {
        NodeValue nv5 = NodeValue.makeInteger(5);
        NodeValue nv7 = NodeValue.makeDecimal(7);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7), "Does not compare " + nv5 + " & " + nv7);
    }

    @Test public void testCompare10() {
        NodeValue nv1 = NodeValue.makeDateTime("2005-10-14T13:09:43Z");
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T14:09:43Z");
        assertEquals(NodeValue.CMP_LESS, NodeValue.compare(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
    }

    @Test public void testCompare11() {
        NodeValue nv1 = NodeValue.makeDateTime("2005-10-14T13:09:43-08:00"); // Different
                                                                             // timezones
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T13:09:43+01:00");
        assertEquals(NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
    }

    @Test public void testCompare12() {
        if ( !ARQ.isTrue(ARQ.strictSPARQL) ) {
            NodeValue nv1 = NodeValue.makeDate("2006-07-21-08:00"); // Different
                                                                    // timezones
            NodeValue nv2 = NodeValue.makeNodeDate("2006-07-21+01:00");
            assertEquals(NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
        }
    }

    @Test
    public void testCompare15() {
        NodeValue nv1 = NodeValue.makeDate("2005-10-14Z");
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T14:09:43Z");
        assertThrows(ExprNotComparableException.class, ()->NodeValue.compare(nv1, nv2));
    }

    @Test
    public void testCompare16() {
        // One in a timezone, one not. Within +/- 14 hours. Can't compare.
        NodeValue nv1 = NodeValue.makeDateTime("2007-08-31T16:20:03");
        NodeValue nv2 = NodeValue.makeDateTime("2007-08-31T16:20:03Z");
        assertThrows(ExprNotComparableException.class, ()->NodeValue.compare(nv1, nv2));
    }

    @Test public void testCompare17() {
        // One in a timezone, one not. Within +/- 14 hours. Can't compare.
        NodeValue nv1 = NodeValue.makeDate("2007-08-31");
        NodeValue nv2 = NodeValue.makeDate("2007-08-31Z");
        assertThrows(ExprNotComparableException.class, ()->NodeValue.compare(nv1, nv2));
    }

    @Test public void testCompare18() {
        // One in a timezone, one not. More than +/- 14 hours. Can compare.
        NodeValue nv1 = NodeValue.makeDateTime("2007-08-31T16:20:03");
        NodeValue nv2 = NodeValue.makeDateTime("2007-08-31T01:20:03Z");
        assertEquals(Expr.CMP_GREATER, NodeValue.compare(nv1, nv2));
    }

    @Test public void testCompare20() {
        NodeValue nv1 = NodeValue.makeString("abcd");
        NodeValue nv2 = NodeValue.makeNodeString("abc");
        assertEquals(NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
    }

    @Test public void testCompare21() {
        NodeValue nv5 = NodeValue.makeInteger(5);
        NodeValue nv7 = NodeValue.makeString("5");

        try {
            NodeValue.compare(nv5, nv7);
            fail("Should not compare (but did) " + nv5 + " & " + nv7);
        } catch (ExprEvalException ex) { /* expected */}

        int x = NodeValue.compareAlways(nv5, nv7);
        assertEquals(NodeValue.CMP_GREATER, NodeValue.compareAlways(nv5, nv7), "Does not compare " + nv5 + " & " + nv7);
    }

    @Test public void testCompare22() {
        NodeValue nv1 = NodeValue.makeNodeString("aaa");
        NodeValue nv2 = NodeValue.makeString("aaabbb");

        int x = NodeValue.compare(nv1, nv2);
        assertEquals(x, Expr.CMP_LESS, "Not CMP_LESS");
        assertTrue(x != Expr.CMP_GREATER, "It's CMP_GREATER");
        assertTrue(x != Expr.CMP_EQUAL, "It's CMP_EQUAL");
    }

    @Test
    public void testCompare23() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createBlankNode());
        NodeValue nv2 = NodeValue.makeString("5");
        assertThrows(ExprNotComparableException.class, ()->NodeValue.compare(nv1, nv2));
    }

    @Test public void testSameUnknown_1() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createURI("test:abc"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc"));

        assertTrue(NodeValue.sameValueAs(nv1, nv2));
        assertFalse(NodeValue.notSameValueAs(nv1, nv2));
        int x = NodeValue.compare(nv1, nv2);
        assertEquals(Expr.CMP_EQUAL, x);
    }

    @Test
    public void testSameUnknown_2() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createURI("test:xyz"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc"));

        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));
        // Not value-comparable.
        assertThrows(ExprNotComparableException.class, ()->NodeValue.compare(nv1, nv2));
        //assertEquals(Expr.CMP_UNEQUAL, x);
    }

    @Test
    public void testSameUnknown_3() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createBlankNode());
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc"));

        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));

        assertThrows(ExprNotComparableException.class, ()->NodeValue.compare(nv1, nv2));
    }

    // ---- sameValueAs -- xsd:dateTime

    // SameValue and compare of date and dateTimes
    // Timezone trickynesses - if one has a TZ and the other has not, then a difference of 14 hours
    // is needed for a comparison.

    @Test public void testSameDateTime_1() {
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T09:22:03");
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T09:22:03");

        assertTrue(NodeValue.sameValueAs(nv1, nv2));
        assertFalse(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test public void testSameDateTime_2() {
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T09:22:03");
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T19:00:00");

        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test public void testSameDateTime_3() {
        // These are the same.
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T10:22:03+01:00");
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T09:22:03Z");

        assertTrue(NodeValue.sameValueAs(nv1, nv2));
        assertFalse(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test public void testSameDateTime_4() {
        // These are not the same.
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T10:22:03+01:00");
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T10:22:03Z");

        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test public void testSameDateTime_5() {
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T10:22:03+01:00");
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T09:22:03");     // No
                                                                           // timezone

        try {
            NodeValue.sameValueAs(nv1, nv2);
            fail("Should not sameValueAs (but did) " + nv1 + " & " + nv2);
        } catch (ExprEvalException ex) {}

        try {
            NodeValue.notSameValueAs(nv1, nv2);
            fail("Should not notSameValueAs (but did) " + nv1 + " & " + nv2);
        } catch (ExprEvalException ex) {}
    }

    // ---- sameValueAs -- xsd:date

    @Test public void testSameDate_1() {
        NodeValue nv1 = NodeValue.makeDate("2007-09-04");
        NodeValue nv2 = NodeValue.makeDate("2007-09-04");

        assertTrue(NodeValue.sameValueAs(nv1, nv2));
        assertFalse(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test public void testSameDate_2() {
        NodeValue nv1 = NodeValue.makeDate("2007-09-04Z");
        NodeValue nv2 = NodeValue.makeDate("2007-09-04+00:00");

        assertTrue(NodeValue.sameValueAs(nv1, nv2));
        assertFalse(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test public void testSameDate_3() {
        NodeValue nv1 = NodeValue.makeDate("2007-09-04Z");
        NodeValue nv2 = NodeValue.makeDate("2007-09-04");     // No timezone

        try {
            NodeValue.sameValueAs(nv1, nv2);
            fail("Should not sameValueAs (but did) " + nv1 + " & " + nv2);
        } catch (ExprEvalException ex) {}

        try {
            NodeValue.notSameValueAs(nv1, nv2);
            fail("Should not notSameValueAs (but did) " + nv1 + " & " + nv2);
        } catch (ExprEvalException ex) {}
    }

    // General comparisons for sorting.

    // bnodes < URIs < literals

    @Test public void testCompareGeneral1() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createBlankNode());
        NodeValue nv2 = NodeValue.makeString("5");

        // bNodes before strings
        int x = NodeValue.compareAlways(nv1, nv2);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
    }

    @Test public void testCompareGeneral2() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createBlankNode());
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc"));

        // bNodes before URIs
        int x = NodeValue.compareAlways(nv1, nv2);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
    }

    @Test public void testCompareGeneral3() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralString("test:abc"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc"));

        // URIs before literals
        int x = NodeValue.compareAlways(nv1, nv2);
        assertEquals(NodeValue.CMP_GREATER, NodeValue.compareAlways(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
    }

    @Test public void testCompareGeneral4() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createURI("test:abc"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:xyz"));

        int x = NodeValue.compareAlways(nv1, nv2);
        assertEquals(NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2), "Does not compare " + nv1 + " & " + nv2);
    }

    @Test public void testCompareDuration_01() {
        testCompare("'P365D'^^xsd:duration", "'P300D'^^xsd:duration", Expr.CMP_GREATER);
    }

    // JENA-814
    @Test
    public void testCompareDuration_02() {
        assertThrows(ExprNotComparableException.class,
                     () -> testCompare("'P365D'^^xsd:duration", "'P1Y'^^xsd:duration", Expr.CMP_INDETERMINATE));
    }

    // JENA-814
    @Test
    public void testCompareDuration_03() {
        assertThrows(ExprNotComparableException.class,
                     () -> testCompare("'P365D'^^xsd:dayTimeDuration", "'P1Y'^^xsd:yearMonthDuration", Expr.CMP_INDETERMINATE));
    }

    // JENA-814
    @Test
    public void testCompareDuration_04() {
        assertThrows(ExprNotComparableException.class,
                     () -> testCompare("'P1M'^^xsd:duration", "'P28D'^^xsd:duration", Expr.CMP_INDETERMINATE));
    }

    // JENA-814
    @Test
    public void testCompareDuration_05() {
        assertThrows(ExprNotComparableException.class,
                     () -> testCompare("'P1M'^^xsd:yearMonthDuration", "'P28D'^^xsd:dayTimeDuration", Expr.CMP_INDETERMINATE));
    }

    @Test public void testCompareDuration_06() {
        testCompare("'P13M'^^xsd:yearMonthDuration", "'P1Y'^^xsd:yearMonthDuration", Expr.CMP_GREATER);
    }

    // -------

    private static void testCompare(String s1, String s2, int correct) {
        NodeValue nv1 = parse(s1);
        NodeValue nv2 = parse(s2);
        int x = NodeValue.compare(nv1, nv2);
        assertEquals(correct, x, "("+s1+", "+s2+") -> "+name(x)+" ["+name(correct)+"]");
        int y = x;
        if ( x == Expr.CMP_LESS || x == Expr.CMP_GREATER )
            y = -x;
        assertEquals(NodeValue.compare(nv2, nv1), y, "Not symmetric: ("+s1+", "+s2+")");
    }

    private static String name(int cmp) {
        switch(cmp) {
            case Expr.CMP_EQUAL :       return "EQ";
            case Expr.CMP_GREATER :     return "GT";
            case Expr.CMP_LESS :        return "LT";
            case Expr.CMP_UNEQUAL :     return "NE";
            case Expr.CMP_INDETERMINATE : return "INDET";
            default:return "Unknown";

        }
    }

    private static NodeValue parse(String str) {
        Node n = SSE.parseNode(str);
        return NodeValue.makeNode(n);
    }
    // abs is a test of Function.unaryOp machinery
    @Test public void testAbs1() {
        NodeValue nv = NodeValue.makeInteger(2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isInteger(), "Not an integer: "+r);
        assertTrue(r instanceof NodeValueInteger, "Not a NodeValueInteger: "+r);
        assertEquals(2, r.getInteger().longValue(), "Wrong result" );
    }

    @Test public void testAbs2() {
        NodeValue nv = NodeValue.makeInteger(-2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isInteger(), "Not an integer: "+r);
        assertTrue(r instanceof NodeValueInteger, "Not a NodeValueInteger: "+r);
        assertEquals(2, r.getInteger().longValue(), "Wrong result" );
    }

    @Test public void testAbs3() {
        NodeValue nv = NodeValue.makeDecimal(2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isDecimal(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: "+r);
        assertEquals(2d, r.getDecimal().doubleValue(), accuracyExact_D, "Wrong result" );
    }

    @Test public void testAbs4() {
        NodeValue nv = NodeValue.makeDecimal(-2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isDecimal(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: "+r);
        assertEquals(2d, r.getDecimal().doubleValue(), accuracyExact_D, "Wrong result" );
    }

    @Test public void testAbs5() {
        NodeValue nv = NodeValue.makeFloat(2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isFloat(), "Not an float: "+r);
        assertTrue(r instanceof NodeValueFloat, "Not a NodeValueFloat: "+r);
        assertEquals(2f, r.getFloat(), accuracyExact_F, "Wrong result" );
    }

    @Test public void testAbs6() {
        NodeValue nv = NodeValue.makeFloat(-2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isFloat(), "Not an float: "+r);
        assertTrue(r instanceof NodeValueFloat, "Not a NodeValueFloat: "+r);
        assertEquals(2d, r.getFloat(), accuracyExact_F, "Wrong result" );
    }

    @Test public void testAbs7() {
        NodeValue nv = NodeValue.makeDouble(2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isDouble(), "Not an double: "+r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: "+r);
        assertEquals(2d, r.getDouble(), accuracyExact_D, "Wrong result" );
    }

    @Test public void testAbs8() {
        NodeValue nv = NodeValue.makeDouble(-2);
        NodeValue r = XSDFuncOp.abs(nv);
        assertTrue(r.isDouble(), "Not an double: "+r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: "+r);
        assertEquals(2d, r.getDouble(), accuracyExact_D, "Wrong result");
    }

    @Test public void testCeiling1() {
        NodeValue nv = NodeValue.makeDecimal(2.6);
        NodeValue r = XSDFuncOp.ceiling(nv);
        assertTrue(r.isDecimal(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: "+r);
        assertEquals(3, r.getDecimal().longValue(), "Wrong result");
    }

    @Test public void testCeiling2() {
        NodeValue nv = NodeValue.makeDecimal(-3.6);
        NodeValue r = XSDFuncOp.ceiling(nv);
        assertTrue(r.isDecimal(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: "+r);
        assertEquals(-3, r.getDecimal().longValue(), "Wrong result" );
    }

    @Test public void testCeiling3() {
        NodeValue nv = NodeValue.makeDouble(2.6);
        NodeValue r = XSDFuncOp.ceiling(nv);
        assertTrue(r.isDouble(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: "+r);
        assertEquals(3d, r.getDouble(), accuracyExact_D, "Wrong result" );
    }

    @Test public void testCeiling4() {
        NodeValue nv = NodeValue.makeDouble(-3.6);
        NodeValue r = XSDFuncOp.ceiling(nv);
        assertTrue(r.isDouble(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: "+r);
        assertEquals(-3d, r.getDouble(), accuracyExact_D, "Wrong result" );
    }

    @Test public void testCeiling5() {
        NodeValue nv = NodeValue.makeInteger(3);
        NodeValue r = XSDFuncOp.ceiling(nv);
        assertTrue(r.isInteger(), "Not an integer: "+r);
        assertTrue(r instanceof NodeValueInteger, "Not a NodeValueInteger: "+r);
        assertEquals(3, r.getInteger().longValue(), "Wrong result" );
    }

    @Test public void testFloor1() {
        NodeValue nv = NodeValue.makeDecimal(2.6);
        NodeValue r = XSDFuncOp.floor(nv);
        assertTrue(r.isDecimal(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: "+r);
        assertEquals(2, r.getDecimal().longValue(), "Wrong result");
    }

    @Test public void testFloor2() {
        NodeValue nv = NodeValue.makeDecimal(-3.6);
        NodeValue r = XSDFuncOp.floor(nv);
        assertTrue(r.isDecimal(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDecimal, "Not a NodeValueDecimal: "+r);
        assertEquals(-4, r.getDecimal().longValue(), "Wrong result" );
    }

    @Test public void testFloor3() {
        NodeValue nv = NodeValue.makeDouble(2.6);
        NodeValue r = XSDFuncOp.floor(nv);
        assertTrue(r.isDouble(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: "+r);
        assertEquals(2d, r.getDouble(), accuracyExact_D, "Wrong result" );
    }

    @Test public void testFloor4() {
        NodeValue nv = NodeValue.makeDouble(-3.6);
        NodeValue r = XSDFuncOp.floor(nv);
        assertTrue(r.isDouble(), "Not a decimal: "+r);
        assertTrue(r instanceof NodeValueDouble, "Not a NodeValueDouble: "+r);
        assertEquals(-4d, r.getDouble(), accuracyExact_D, "Wrong result" );
    }

    @Test public void testFloor5() {
        NodeValue nv = NodeValue.makeInteger(3);
        NodeValue r = XSDFuncOp.floor(nv);
        assertTrue(r.isInteger(), "Not an integer: "+r);
        assertTrue(r instanceof NodeValueInteger, "Not a NodeValueInteger: "+r);
        assertEquals(3, r.getInteger().longValue(), "Wrong result" );
    }

    @Test public void testSqrt1() {
        NodeValue four = NodeValue.makeInteger(4);
        NodeValue two = NodeValue.makeDouble(2);
        NodeValue result = XSDFuncOp.sqrt( four );

        assertTrue(result.isDouble());
        assertFalse(result.isDecimal());
        assertTrue( NodeValue.sameValueAs( two, result));
        assertTrue( two.asNode().sameValueAs(result.asNode()) );
    }

    @Test public void testSqrt2() {
        NodeValue four = NodeValue.makeDouble(4);
        NodeValue two = NodeValue.makeInteger(2);
        NodeValue result = XSDFuncOp.sqrt( four );

        assertTrue(result.isDouble());
        assertTrue( NodeValue.sameValueAs( two, result));

        assertNotNull(result.asNode());
    }

    @Test
    public void testStrReplace() {
        //test invalid pattern
        NodeValue wrong = NodeValue.makeString("^(?:-*[^-]){-9}");
        NodeValue nvStr= NodeValue.makeString("AGIKLAKLMTUARAR");
        NodeValue empty= NodeValue.makeString("");
        assertThrows(ExprEvalException.class, ()->XSDFuncOp.strReplace(nvStr, wrong, empty) );
    }

    // All compatible - no timezone.
    private static NodeValue nv_dt = NodeValue.makeNode("2010-03-22T20:31:54.5", XSDDatatype.XSDdateTime);
    private static NodeValue nv_d = NodeValue.makeNode("2010-03-22", XSDDatatype.XSDdate);
    private static NodeValue nv_gy = NodeValue.makeNode("2010", XSDDatatype.XSDgYear);
    private static NodeValue nv_gym = NodeValue.makeNode("2010-03", XSDDatatype.XSDgYearMonth);

    private static NodeValue nv_gmd = NodeValue.makeNode("--03-22", XSDDatatype.XSDgMonthDay);
    private static NodeValue nv_gm = NodeValue.makeNode("--03", XSDDatatype.XSDgMonth);
    private static NodeValue nv_gd = NodeValue.makeNode("---22", XSDDatatype.XSDgDay);
    private static NodeValue nv_t = NodeValue.makeNode("20:31:54.5", XSDDatatype.XSDtime);

    private static void testDateTimeCast(NodeValue nv, XSDDatatype xsd, NodeValue nvResult ) {
        NodeValue nv2 = XSDFuncOp.dateTimeCast(nv, xsd);
        assertEquals(nvResult, nv2);
    }

    // datetime to other
    @Test public void cast_gregorian_01() { testDateTimeCast(nv_dt, XSDDatatype.XSDdateTime, nv_dt); }
    @Test public void cast_gregorian_02() { testDateTimeCast(nv_dt, XSDDatatype.XSDdate, nv_d); }
    @Test public void cast_gregorian_03() { testDateTimeCast(nv_dt, XSDDatatype.XSDgYear, nv_gy); }
    @Test public void cast_gregorian_04() { testDateTimeCast(nv_dt, XSDDatatype.XSDgYearMonth, nv_gym); }
    @Test public void cast_gregorian_05() { testDateTimeCast(nv_dt, XSDDatatype.XSDgMonthDay, nv_gmd); }
    @Test public void cast_gregorian_06() { testDateTimeCast(nv_dt, XSDDatatype.XSDgMonth, nv_gm); }
    @Test public void cast_gregorian_07() { testDateTimeCast(nv_dt, XSDDatatype.XSDgDay, nv_gd); }

    @Test public void cast_gregorian_08() { testDateTimeCast(nv_dt, XSDDatatype.XSDtime, nv_t); }

    // date to other
    @Test public void cast_gregorian_10() { testDateTimeCast(nv_d, XSDDatatype.XSDdateTime, NodeValue.makeNode("2010-03-22T00:00:00", XSDDatatype.XSDdateTime)); }
    @Test public void cast_gregorian_11() { testDateTimeCast(nv_d, XSDDatatype.XSDdate, nv_d); }
    @Test public void cast_gregorian_12() { testDateTimeCast(nv_d, XSDDatatype.XSDgYear, nv_gy); }
    @Test public void cast_gregorian_13() { testDateTimeCast(nv_d, XSDDatatype.XSDgYearMonth, nv_gym); }
    @Test public void cast_gregorian_14() { testDateTimeCast(nv_d, XSDDatatype.XSDgMonthDay, nv_gmd); }
    @Test public void cast_gregorian_15() { testDateTimeCast(nv_d, XSDDatatype.XSDgMonth, nv_gm); }
    @Test public void cast_gregorian_16() { testDateTimeCast(nv_d, XSDDatatype.XSDgDay, nv_gd); }

    // G* to self
    @Test public void cast_gregorian_21() { testDateTimeCast(nv_gym, XSDDatatype.XSDgYearMonth, nv_gym); }
    @Test public void cast_gregorian_22() { testDateTimeCast(nv_gy, XSDDatatype.XSDgYear, nv_gy); }
    @Test public void cast_gregorian_23() { testDateTimeCast(nv_gmd, XSDDatatype.XSDgMonthDay, nv_gmd); }
    @Test public void cast_gregorian_24() { testDateTimeCast(nv_gm, XSDDatatype.XSDgMonth, nv_gm); }
    @Test public void cast_gregorian_25() { testDateTimeCast(nv_gd, XSDDatatype.XSDgDay, nv_gd); }

    // G* to date
    @Test public void cast_gregorian_31() { assertThrows(ExprEvalTypeException.class, ()-> testDateTimeCast(nv_gym, XSDDatatype.XSDdate, nv_d) ); }
    @Test public void cast_gregorian_32() { assertThrows(ExprEvalTypeException.class, ()-> testDateTimeCast(nv_gy, XSDDatatype.XSDdate, NodeValue.makeDate("2010-01-01")) ); }
    @Test public void cast_gregorian_33() { assertThrows(ExprEvalTypeException.class, ()-> testDateTimeCast(nv_gmd, XSDDatatype.XSDdate, nv_d) ); }
    @Test public void cast_gregorian_34() { assertThrows(ExprEvalTypeException.class, ()-> testDateTimeCast(nv_gm, XSDDatatype.XSDdate, nv_d) ); }
    @Test public void cast_gregorian_35() { assertThrows(ExprEvalTypeException.class, ()-> testDateTimeCast(nv_gd, XSDDatatype.XSDdate, nv_d) ); }

    // Junk to date/time thing.
    @Test
    public void cast_err_gregorian_01()     { assertThrows(ExprEvalException.class, ()->testDateTimeCast(NodeValue.FALSE, XSDDatatype.XSDgDay, nv_gd) ); }

    private static NodeValue nv_dt_tz1 = NodeValue.makeNode("2010-03-22T20:31:54.5+01:00", XSDDatatype.XSDdateTime);
    private static NodeValue nv_dt_tz2 = NodeValue.makeNode("2010-03-22T20:31:54.5-05:00", XSDDatatype.XSDdateTime);
    private static NodeValue nv_dt_tz3 = NodeValue.makeNode("2010-03-22T20:31:54.5Z", XSDDatatype.XSDdateTime);

    private static NodeValue nv_d_tz1 = NodeValue.makeNode("2010-03-22+01:00", XSDDatatype.XSDdate);
    private static NodeValue nv_d_tz2 = NodeValue.makeNode("2010-03-22-05:00", XSDDatatype.XSDdate);
    private static NodeValue nv_d_tz3 = NodeValue.makeNode("2010-03-22Z", XSDDatatype.XSDdate);

    private static NodeValue nv_t_tz1 = NodeValue.makeNode("20:31:54.5+01:00", XSDDatatype.XSDtime);
    private static NodeValue nv_t_tz2 = NodeValue.makeNode("20:31:54.5-05:00", XSDDatatype.XSDtime);
    private static NodeValue nv_t_tz3 = NodeValue.makeNode("20:31:54.5Z", XSDDatatype.XSDtime);

    @Test public void cast_date_tz_01() { testDateTimeCast(nv_dt_tz1, XSDDatatype.XSDdate, nv_d_tz1); }
    @Test public void cast_date_tz_02() { testDateTimeCast(nv_dt_tz2, XSDDatatype.XSDdate, nv_d_tz2); }
    @Test public void cast_date_tz_03() { testDateTimeCast(nv_dt_tz3, XSDDatatype.XSDdate, nv_d_tz3); }

    @Test public void cast_time_tz_01() { testDateTimeCast(nv_dt_tz1, XSDDatatype.XSDtime, nv_t_tz1); }
    @Test public void cast_time_tz_02() { testDateTimeCast(nv_dt_tz2, XSDDatatype.XSDtime, nv_t_tz2); }
    @Test public void cast_time_tz_03() { testDateTimeCast(nv_dt_tz3, XSDDatatype.XSDtime, nv_t_tz3); }

    @Test public void fn_error_01() {
        try {
            LibTestExpr.eval("fn:error()");
            fail("No exception");
        }
        catch (ExprEvalException ex) {
            assertNull(ex.getMessage());
        }
    }

    @Test public void fn_error_02() {
        try {
            LibTestExpr.eval("fn:error('MESSAGE')");
            fail("No exception");
        }
        catch (ExprEvalException ex) {
            assertEquals("MESSAGE", ex.getMessage());
        }
    }
}
