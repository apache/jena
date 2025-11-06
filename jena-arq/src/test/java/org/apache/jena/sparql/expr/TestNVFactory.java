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

import static java.lang.String.format;
import static org.apache.jena.datatypes.xsd.XSDDatatype.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.nodevalue.*;
import org.apache.jena.vocabulary.RDF;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestNVFactory {

    // basic testing
    private <T extends NodeValue> NodeValue test(String lex, RDFDatatype dt, Class<T> expectedClass) {
        Node n = NodeFactory.createLiteralDT(lex, dt);
        NodeValue nv = NVFactory.create(n);
        assertTrue(expectedClass.isInstance(nv),
                  format("Expected instance of %s but got %s", expectedClass.getSimpleName(), nv.getClass().getSimpleName()));
        assertEquals(lex, nv.asNode().getLiteralLexicalForm());
        return nv;
    }

    @Test
    public void testDecimal_1() {
        NodeValue nv = test("12.34", XSDdecimal, NodeValueDecimal.class);
        assertTrue(nv.isDecimal());
        assertNotNull(nv.getDecimal());
        assertTrue(nv.isFloat());
        assertTrue(nv.isDouble());
        assertFalse(nv.isInteger());
        assertTrue(nv.isDecimal());
    }

    @Test
    public void testDecimal_2() {
        NodeValue nv = test("12", XSDdecimal, NodeValueDecimal.class);
        assertTrue(nv.isDecimal());
        assertNotNull(nv.getDecimal());
    }

    @Test
    public void testDecimal_3() {
        NodeValue nv = test("-12", XSDdecimal, NodeValueDecimal.class);
        assertTrue(nv.isDecimal());
        assertNotNull(nv.getDecimal());
    }

    @Test
    public void testFloat() {
        NodeValue nv = test("12.34", XSDfloat, NodeValueFloat.class);
        assertTrue(nv.isFloat());
        assertTrue(nv.isDouble());
        assertFalse(nv.isInteger());
        assertFalse(nv.isDecimal());
    }

    @Test
    public void testDouble() {
        NodeValue nv = test("12.34", XSDdouble, NodeValueDouble.class);
        assertFalse(nv.isFloat());
        assertTrue(nv.isDouble());
        assertFalse(nv.isInteger());
        assertFalse(nv.isDecimal());
    }

    @Test
    public void testInteger() {
        NodeValue nv = test("123", XSDinteger, NodeValueInteger.class);
        assertTrue(nv.isFloat());
        assertTrue(nv.isDouble());
        assertTrue(nv.isInteger());
        assertTrue(nv.isDecimal());
    }

    @Test
    public void testDerivedInt() {
        NodeValue nv = test("123", XSDint, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedLong() {
        NodeValue nv = test("123", XSDlong, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedShort() {
        NodeValue nv = test("123", XSDshort, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedByte() {
        NodeValue nv = test("123", XSDbyte, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedUnsignedByte() {
        NodeValue nv = test("123", XSDunsignedByte, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedUnsignedShort() {
        NodeValue nv = test("123", XSDunsignedShort, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedUnsignedInt() {
        NodeValue nv = test("123", XSDunsignedInt, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedUnsignedLong() {
        NodeValue nv = test("123", XSDunsignedLong, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedNonPositiveInteger() {
        NodeValue nv = test("-123", XSDnonPositiveInteger, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedNonNegativeInteger() {
        NodeValue nv = test("123", XSDnonNegativeInteger, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedPositiveInteger() {
        NodeValue nv = test("123", XSDpositiveInteger, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testDerivedNegativeInteger() {
        NodeValue nv = test("-123", XSDnegativeInteger, NodeValueInteger.class);
        assertTrue(nv.isInteger());
    }

    @Test
    public void testInvalidIntegerLiteral() {
        NodeValue nv = test("not-an-integer", XSDinteger, NodeValueNode.class);
        assertNotNull(nv);
        assertFalse(nv.isNumber());
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidDecimalFormat() {
        // Multiple decimal points
        NodeValue nv = test("12.34.56", XSDdecimal, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidFloatFormat() {
        // Incomplete exponent
        NodeValue nv = test("12.34e", XSDfloat, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidIntegerFormat() {
        // Decimal not allowed for integer
        NodeValue nv = test("123.45", XSDinteger, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidPositiveIntegerNegativeValue() {
        // Negative value not allowed for positive integer
        NodeValue nv = test("-123", XSDpositiveInteger, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidUnsignedIntValue() {
        // Negative value not allowed for unsigned
        NodeValue nv = test("-1", XSDunsignedInt, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidByteRange() {
        // Outside byte range (-128 to 127)
        NodeValue nv = test("128", XSDbyte, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidUnsignedByteRange() {
        // Outside unsigned byte range (0 to 255)
        NodeValue nv = test("256", XSDunsignedByte, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidNonNegativeInteger() {
        // Negative value not allowed
        NodeValue nv = test("-1", XSDnonNegativeInteger, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testInvalidNonPositiveInteger() {
        // Positive value not allowed
        NodeValue nv = test("1", XSDnonPositiveInteger, NodeValueNode.class);
        assertFalse(nv.isInteger());
    }

    @Test
    public void testBoolean() {
        NodeValue nv1 = test("true", XSDboolean, NodeValueBoolean.class);
        NodeValue nv2 = test("false", XSDboolean, NodeValueBoolean.class);
    }

    @Test
    public void testString() {
        NodeValue nv = test("test string", XSDstring, NodeValueString.class);
    }

    @Test
    public void testLangString() {
        String lex = "hello";
        String lang = "en";
        Node n = NodeFactory.createLiteralLang(lex, lang);
        NodeValue nv = NVFactory.create(n);
        assertTrue(NodeValueLang.class.isInstance(nv));
        assertEquals(lex, nv.getString());
        assertEquals(lang, nv.getLang());
    }

    @Test
    public void testDirLangString() {
        String lex = "hello";
        String lang = "en-GB";
        String ltr = "ltr";
        Node n = NodeFactory.createLiteralDirLang(lex, lang, ltr);
        NodeValue nv = NVFactory.create(RDF.dtDirLangString, n);
        assertTrue(NodeValueLangDir.class.isInstance(nv));
        assertEquals(lex, nv.getString());
        assertEquals(lang, nv.getLang());
    }

    @Test
    public void testDateTimeType() {
        NodeValue nv = test("2025-11-03T12:00:00", XSDdateTime, NodeValueDateTime.class);
        assertTrue(nv.isDateTime());
        assertFalse(nv.isDate());
        assertFalse(nv.isTime());
    }

    @Test
    public void testDate() {
        NodeValue nv = test("2025-11-03", XSDdate, NodeValueDateTime.class);
        assertFalse(nv.isDateTime());
        assertTrue(nv.isDate());
        assertFalse(nv.isTime());
    }

    @Test
    public void testTime() {
        NodeValue nv = test("12:00:00", XSDtime, NodeValueDateTime.class);
        assertFalse(nv.isDateTime());
        assertFalse(nv.isDate());
        assertTrue(nv.isTime());
    }

    @Test
    public void testDateTimeStamp() {
        NodeValue nv = test("2025-11-03T12:00:00Z", XSDdateTimeStamp, NodeValueDateTime.class);
        assertTrue(nv.isDateTime());
    }

    @Test
    public void testGDay() {
        NodeValue nv = test("---03", XSDgDay, NodeValueDateTime.class);
        assertFalse(nv.isDateTime());
        assertTrue(nv.isGDay());
        assertFalse(nv.isGMonthDay());
    }

    @Test
    public void testGMonth() {
        NodeValue nv = test("--11", XSDgMonth, NodeValueDateTime.class);
        assertFalse(nv.isDateTime());
        assertTrue(nv.isGMonth());
        assertFalse(nv.isGMonthDay());
    }

    @Test
    public void testGMonthDay() {
        NodeValue nv = test("--11-03", XSDgMonthDay, NodeValueDateTime.class);
        assertFalse(nv.isDateTime());
        assertFalse(nv.isGMonth());
        assertTrue(nv.isGMonthDay());
    }

    @Test
    public void testGYear() {
        NodeValue nv = test("2025", XSDgYear, NodeValueDateTime.class);
        assertFalse(nv.isDateTime());
        assertTrue(nv.isGYear());
        assertFalse(nv.isGYearMonth());
    }

    @Test
    public void testGYearMonth() {
        NodeValue nv = test("2025-11", XSDgYearMonth, NodeValueDateTime.class);
        assertFalse(nv.isDateTime());
        assertFalse(nv.isGYear());
        assertTrue(nv.isGYearMonth());
    }

    // Keep original duration types test as it has special assertions
    @Test
    public void testDuration() {
        NodeValue nv = test("P1Y2M3DT4H5M6S", XSDduration, NodeValueDuration.class);
        assertTrue(nv.isDuration());
        assertFalse(nv.isDayTimeDuration());
        assertFalse(nv.isYearMonthDuration());
    }

    @Test
    public void testDuration_2() {
        NodeValue nv = test("P3DT4H5M6S", XSDduration, NodeValueDuration.class);
        assertTrue(nv.isDuration());
        assertTrue(nv.isDayTimeDuration());
        assertFalse(nv.isYearMonthDuration());
    }

    @Test
    public void testDuration_3() {
        NodeValue nv = test("P1Y2M", XSDduration, NodeValueDuration.class);
        assertTrue(nv.isDuration());
        assertFalse(nv.isDayTimeDuration());
        assertTrue(nv.isYearMonthDuration());
    }

    @Test
    public void testDayTimeDuration() {
        NodeValue nv = test("P3DT4H5M6S", XSDdayTimeDuration, NodeValueDuration.class);
        assertTrue(nv.isDuration());
        assertTrue(nv.isDayTimeDuration());
        assertFalse(nv.isYearMonthDuration());
    }

    @Test
    public void testYearMonthDuration() {
        NodeValue nv = test("P1Y2M", XSDyearMonthDuration, NodeValueDuration.class);
        assertTrue(nv.isDuration());
        assertFalse(nv.isDayTimeDuration());
        assertTrue(nv.isYearMonthDuration());
    }

    @Test
    public void testInvalidDayTimeDuration() {
        NodeValue nv = test("P1Y", XSDdayTimeDuration, NodeValueNode.class);
        assertFalse(nv.isDuration());
        assertFalse(nv.isDayTimeDuration());
        assertFalse(nv.isYearMonthDuration());
    }

    @Test
    public void testInvalidYearMonthDuration() {
        NodeValue nv = test("PT3H", XSDyearMonthDuration, NodeValueNode.class);
        assertFalse(nv.isDuration());
        assertFalse(nv.isDayTimeDuration());
        assertFalse(nv.isYearMonthDuration());
    }

    @Test
    public void testInvalidDateFormat_1() {
        test("not-a-date", XSDdate, NodeValueNode.class);
    }

    @Test
    public void testInvalidDateFormat_2() {
        // Invalid for xsd:date
        test("2025-11-03T15:04:30", XSDdate, NodeValueNode.class);
    }

    @Test
    public void testInvalidTimeFormat() {
        // Invalid hours
        test("25:00:00", XSDtime, NodeValueNode.class);
    }

    @Test
    public void testInvalidDateTimeFormat_1() {
        // Invalid valid for month, day, and hour
        NodeValue nv = test("2025-13-45T25:00:00", XSDdateTime, NodeValueNode.class);
    }

    @Test
    public void testInvalidDateTimeFormat_2() {
        // Invalid for xsd:dateTime
        NodeValue nv = test("2025-12-25", XSDdateTime, NodeValueNode.class);
    }

    @Test
    public void testInvalidDurationFormat() {
        // Invalid duration designator
        test("P1X", XSDduration, NodeValueNode.class);
    }

    @Test
    public void testInvalidGMonthFormat() {
        // Invalid month value
        test("--13", XSDgMonth, NodeValueNode.class);
    }

    @Test
    public void testInvalidGDayFormat() {
        // Invalid day value
        test("---32", XSDgDay, NodeValueNode.class);
    }
}