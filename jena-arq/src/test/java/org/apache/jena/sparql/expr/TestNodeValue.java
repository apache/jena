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

import java.math.BigDecimal;
import java.util.*;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Marker;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/** Break expression testing suite into parts
 * @see TestExpressions
 * @see TestExprLib
 * @see TestNodeValue
 */
public class TestNodeValue
{
    static final double doubleAccuracy = 0.00000001d;
    static boolean warningSetting;

    @BeforeAll public static void beforeClass() {
        warningSetting = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false;
    }

    @AfterAll public static void afterClass() {
        NodeValue.VerboseWarnings = warningSetting;
    }

    @Test
    public void testInt1() {
        NodeValue v = NodeValue.makeInteger(5);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertFalse(v.hasNode(), ()->"Is a node: " + v);
    }

    @Test
    public void testInt2() {
        NodeValue v = NodeValue.makeNodeInteger(5);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testInt3() {
        NodeValue v1 = NodeValue.makeNodeInteger(5);
        NodeValue v2 = NodeValue.makeInteger(5);
        assertTrue(v1.getInteger().equals(v2.getInteger()), ()->"Not same integer: " + v1 + " & " + v2);
    }

    @Test
    public void testFloat1() {
        NodeValue v = NodeValue.makeFloat(5);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isFloat(), ()->"Not a float: " + v);
        assertTrue(v.isDouble(), ()->"Float not a double: " + v);
        assertFalse(v.hasNode(), ()->"No node: " + v);
    }

    @Test
    public void testFloat2() {
        NodeValue v = NodeValue.makeNodeFloat(5);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a float: " + v);
        assertTrue(v.isDouble(), ()->"Float not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testFloat3() {
        NodeValue v1 = NodeValue.makeNodeFloat(5.7f);
        NodeValue v2 = NodeValue.makeFloat(5.7f);
        assertTrue(v1.getFloat() == v2.getFloat(), ()->"Not same float: " + v1 + " & " + v2);
        assertTrue(v1.getDouble() == v2.getDouble(), ()->"Not same float as double: " + v1 + " & " + v2);
    }

    @Test
    public void testFloat4() {
        NodeValue v1 = NodeValue.makeNodeFloat("5.7");
        NodeValue v2 = NodeValue.makeFloat(5.7f);
        assertTrue(v1.getFloat() == v2.getFloat(), ()->"Not same float: " + v1 + " & " + v2);
        assertTrue(v1.getDouble() == v2.getDouble(), ()->"Not same float as double: " + v1 + " & " + v2);
    }

    @Test
    public void testDouble1() {
        NodeValue v = NodeValue.makeDouble(5);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a double: " + v);
        assertFalse(v.hasNode(), ()->"No node: " + v);
    }

    @Test
    public void testDouble2() {
        NodeValue v = NodeValue.makeNodeDouble(5);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testDouble3() {
        NodeValue v1 = NodeValue.makeNodeDouble(5.7);
        NodeValue v2 = NodeValue.makeDouble(5.7);
        assertTrue(v1.getDouble() == v2.getDouble(), ()->"Not same double: " + v1 + " & " + v2);
    }

    @Test
    public void testDecimal1() {
        NodeValue v = NodeValue.makeDecimal(new BigDecimal("1.3"));
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDecimal(), ()->"Not a double: " + v);
        assertFalse(v.hasNode(), ()->"Is a node: " + v);
    }

    @Test
    public void testDecimal2() {
        NodeValue v = NodeValue.makeNodeDecimal("1.3");
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDecimal(), ()->"Not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testDecimal3() {
        NodeValue v1 = NodeValue.makeDecimal(new BigDecimal("1.3"));
        NodeValue v2 = NodeValue.makeNodeDecimal("1.3");
        assertTrue(v1.getDecimal().compareTo(v2.getDecimal()) == 0, ()->"Not same decimal: " + v1 + " & " + v2);
        assertEquals(v1, v2, ()->"Not same decimal by equals: " + v1 + " & " + v2);
    }

    @Test
    public void testDateTime1x() {
        // Legacy
        // Better to use XMLGregorianCalendar
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Clear/ set all fields (milliseconds included).
        cal.setTimeInMillis(0);
        cal.set(2005, 01, 18, 20, 39, 10); // NB Months from 0, not 1

        @SuppressWarnings("removal")
        NodeValue v = NodeValue.makeDateTime(cal);
        assertTrue(v.isDateTime(), ()->"Not a dateTime: " + v);
        assertFalse(v.isDate(), ()->"A date: " + v);
        // DateTimes always have nodes because we used that to parse the thing.
    }

    @Test
    public void testDateTime1() {
        // Better to use XMLGregorianCalendar
        XMLGregorianCalendar cal = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("2025-11-05T12:08:30.5Z");
        NodeValue v = NodeValue.makeDateTime(cal);
        assertTrue(v.isDateTime(), ()->"Not a dateTime: " + v);
        assertFalse(v.isDate(), ()->"A date: " + v);
    }


    @Test
    public void testDateTime2() {
        NodeValue v = NodeValue.makeNodeDateTime("2005-02-18T20:39:10Z");
        assertTrue(v.isDateTime(), ()->"Not a dateTime: " + v);
        assertFalse(v.isDate(), ()->"A date: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testDateTime3() {
        NodeValue v1 = NodeValue.makeDateTime("2005-02-18T20:39:10Z");
        NodeValue v2 = NodeValue.makeNodeDateTime("2005-02-18T20:39:10Z");
        assertEquals(v1.getDateTime(), v2.getDateTime(), ()->"Not Calendar.equals: ");
    }

    @Test
    public void testDateTime4() {
        Calendar cal1 = new GregorianCalendar();
        cal1.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Clear/ set all fields (milliseconds included).
        cal1.setTimeInMillis(0);
        cal1.set(2005, 01, 18, 20, 39, 10); // NB Months from 0, not 1

        NodeValue v = NodeValue.makeNode("2005-02-18T20:39:10Z", XSDDatatype.XSDdateTime);
        assertTrue(v.isDateTime(), ()->"Not a dateTime: " + v);
        assertFalse(v.isDate(), ()->"A date: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        Calendar cal2 = v.getDateTime().toGregorianCalendar();
        assertEquals(0, cal1.compareTo(cal2), ()->"Not equal: " + v);
    }

    @Test
    public void testDateTime5() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            // Illegal lexical for a dateTime.
            NodeValue v = NodeValue.makeNode("2005-02-18", XSDDatatype.XSDdateTime);
            assertFalse(v.isDate(), ()->"Date!: " + v);
            assertFalse(v.isDateTime(), ()->"Datetime!: " + v);
        }
        finally {
            NodeValue.VerboseWarnings = b;
        }
    }

    @Test
    public void testDateTime6() {
        NodeValue v0 = NodeValue.makeDateTime("2005-02-18T20:39:10Z");
        NodeValue v1 = NodeValue.makeDateTime("2005-02-18T20:39:10.0Z");
        NodeValue v2 = NodeValue.makeDateTime("2005-02-18T20:39:10.00Z");
        NodeValue v3 = NodeValue.makeDateTime("2005-02-18T20:39:10.000Z");
        assertEquals(v0.getDateTime(), v1.getDateTime(), ()->"Not Calendar.equals: ");
        assertEquals(v0.getDateTime(), v2.getDateTime(), ()->"Not Calendar.equals: ");
        assertEquals(v0.getDateTime(), v3.getDateTime(), ()->"Not Calendar.equals: ");
    }

    @Test
    public void testDateTime7() {
        NodeValue v0 = NodeValue.makeDateTime("2005-02-18T20:39:10Z");
        NodeValue v1 = NodeValue.makeDateTime("2005-02-18T20:39:10.001Z");
        assertNotSame(v0.getDateTime(), v1.getDateTime(), ()->"Calendar.equals: ");
    }

    @Test
    public void testDateTime8() {
        NodeValue v0 = NodeValue.makeDateTime("2005-02-18T20:39:10-05:00");
        NodeValue v1 = NodeValue.makeDateTime("2005-02-18T17:39:10.000-08:00");
        assertEquals(v0.getDateTime(), v1.getDateTime(), ()->"Not Calendar.equals: ");
    }

    private static NodeValue parse(String nodeString) {
        Node n = SSE.parseNode(nodeString);
        NodeValue nv = NodeValue.makeNode(n);
        return nv;
    }

    @Test
    public void testDateTimeStamp1() {
        // xsd:dateTimeStamp is a derived datatype of xsd:dateTime.
        NodeValue nv = parse("'2000-01-01T00:00:00+00:00'^^xsd:dateTimeStamp");
        assertTrue(nv.isDateTime());
        assertFalse(nv.isDate());
    }

    @Test
    public void testDate1x() {
        // Legacy
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Clear/ set all fields (milliseconds included).
        cal.setTimeInMillis(0);
        cal.set(2005, 01, 18, 0, 0, 0);
        @SuppressWarnings("removal")
        NodeValue v = NodeValue.makeDate(cal);
        assertTrue(v.isDate(), ()->"Not a date: " + v);
        assertFalse(v.isDateTime(), ()->"A dateTime: " + v);
    }

    @Test
    public void testDate1() {
        // Better to use XMLGregorianCalendar
        XMLGregorianCalendar cal = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("2025-11-05");
        NodeValue v = NodeValue.makeDate(cal);
        assertTrue(v.isDate(), ()->"Not a date: " + v);
        assertFalse(v.isDateTime(), ()->"A dateTime: " + v);
    }

    @Test
    public void testDate2() {
        NodeValue v = NodeValue.makeNodeDate("2005-02-18");
        assertTrue(v.isDate(), ()->"Not a date: " + v);
        assertFalse(v.isDateTime(), ()->"A dateTime: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testDate3() {
        NodeValue v1 = NodeValue.makeDate("2005-02-18+01:00");
        NodeValue v2 = NodeValue.makeNodeDate("2005-02-18+01:00");
        assertEquals(v1.getDateTime(), v2.getDateTime(), ()->"Not Calendar.equals: ");
    }

    @Test
    public void testDate4() {
        Calendar cal1 = new GregorianCalendar();
        cal1.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Clear/ set all fields (milliseconds included).
        cal1.setTimeInMillis(0);
        // Must be ",0,0,0"
        cal1.set(2005, 01, 18, 0, 0, 0); // NB Months from 0, not 1

        NodeValue v = NodeValue.makeNode("2005-02-18Z", XSDDatatype.XSDdate);
        assertTrue(v.isDate(), ()->"Not a date: " + v);
        assertFalse(v.isDateTime(), ()->"A dateTime: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        Calendar cal2 = v.getDateTime().toGregorianCalendar();
        assertEquals(0, cal1.compareTo(cal2), ()->"Not equal: " + v);
    }

    @Test
    public void testDate5() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            // Illegal lexical for a date.
            NodeValue v = NodeValue.makeNode("2005-02-18T20:39:10Z", XSDDatatype.XSDdate);
            assertFalse(v.isDateTime(), ()->"Datetime!: " + v);
            assertFalse(v.isDate(), ()->"Date!: " + v);
        }
        finally {
            NodeValue.VerboseWarnings = b;
        }
    }

    @Test
    public void testNodeInt1() {
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDinteger);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testNodeInt2() {
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDdouble);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testNodeInt3() {
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDinteger);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();
        assertEquals("57", actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeInt4() {
        NodeValue v = NodeValue.makeNodeInteger(18);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();
        assertEquals("18", actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeInt5() {
        // Legal as a bare integer but not canonical form
        NodeValue v = NodeValue.makeNodeInteger("018");
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();
        assertEquals("018", actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeInt6() {
        // Leading/trail whitespace.
        NodeValue v = NodeValue.makeNodeInteger(" 18");
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testNodeInt7() {
        // Leading/trail whitespace.
        NodeValue v = NodeValue.makeNodeInteger(" 18 ");
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isInteger(), ()->"Not an integer: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testNodeInt8() {
        // Internal whitespace. Not a number.
        NodeValue v = NodeValue.makeNodeInteger("1 8");
        assertFalse(v.isNumber(), ()->"A number!: " + v);
        assertFalse(v.isInteger(), ()->"An integer!: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }


    @Test
    public void testNodeFloat1() {
        // There is no SPARQL representation in short form of a float.
        NodeValue v = NodeValue.makeNode("57.0", XSDDatatype.XSDfloat);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isFloat(), ()->"Not a float: " + v);
        assertTrue(v.isDouble(), ()->"Not a double(float): " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();

        assertEquals("\"57.0\"^^<" + XSDDatatype.XSDfloat.getURI() + ">", actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeFloat2() {
        // WhiteSpace facet
        NodeValue v = NodeValue.makeNode(" 57.0 ", XSDDatatype.XSDfloat);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isFloat(), ()->"Not a float: " + v);
        assertTrue(v.isDouble(), ()->"Not a double(float): " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();

        assertEquals("\" 57.0 \"^^<" + XSDDatatype.XSDfloat.getURI() + ">", actualStr, ()->"Print form mismatch");
    }


    @Test
    public void testNodeDouble1() {
        // Note input form is legal and canonical as a lexical form double
        NodeValue v = NodeValue.makeNode("57.0e0", XSDDatatype.XSDdouble);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();

        assertEquals("57.0e0", actualStr, ()->"Print form mismatch");
//                     "\"57\"^^<"+XSDDatatype.XSDdouble.getURI()+">",
//                     actualStr);
    }

    @Test
    public void testNodeDouble2() {
        // Note input form is not legal as a lexical form double
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDdouble);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();

        assertEquals("\"57\"^^<" + XSDDatatype.XSDdouble.getURI() + ">", actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeDouble3() {
        // Note input form is legal but not canonical as a bare FP
        NodeValue v = NodeValue.makeNode("057.0e0", XSDDatatype.XSDdouble);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();

        assertEquals("057.0e0", actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeDouble4() {
        // Leading/trail whitespace.
        NodeValue v = NodeValue.makeNode(" 057.0e0 ", XSDDatatype.XSDdouble);
        assertTrue(v.isNumber(), ()->"Not a number: " + v);
        assertTrue(v.isDouble(), ()->"Not a double: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testNodeBool1() {
        NodeValue v = NodeValue.makeNode("true", XSDDatatype.XSDboolean);
        assertTrue(v.isBoolean(), ()->"Not a boolean: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        assertTrue(v.getBoolean(), ()->"Not satisfied: " + v);
    }

    @Test
    public void testNodeBool2() {
        NodeValue v = NodeValue.makeNode("false", XSDDatatype.XSDboolean);
        assertTrue(v.isBoolean(), ()->"Not a boolean: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        assertFalse(v.getBoolean(), ()->"Satisfied: " + v);
    }

    @Test
    public void testNodeBool3() {
        NodeValue v = NodeValue.makeBoolean(true);
        assertTrue(v.isBoolean(), ()->"Not a boolean: " + v);
        // assertTrue("Not a node: "+v, v.hasNode());
        assertTrue(v.getBoolean(), ()->"Not true: " + v);
        assertTrue(XSDFuncOp.effectiveBooleanValue(v), ()->"Not true: " + v);
    }

    @Test
    public void testNodeBool4() {
        NodeValue v = NodeValue.makeBoolean(false);
        assertTrue(v.isBoolean(), ()->"Not a boolean: " + v);
        // assertTrue("Not a node: "+v, v.hasNode());
        assertFalse(v.getBoolean(), ()->"Not false: " + v);
        assertFalse(XSDFuncOp.effectiveBooleanValue(v), ()->"Not false: " + v);
    }

    @Test
    public void testNodeDateTime1() {
        NodeValue v = NodeValue.makeNode("2021-11-08T20:37:25+01:00", XSDDatatype.XSDdateTime);
        assertTrue(v.isDateTime(), ()->"Not a dateTime: " + v);
    }

    @Test
    public void testNodeDateTime2() {
        NodeValue v = NodeValue.makeNode("\t2021-11-08T20:37:26+01:00\t", XSDDatatype.XSDdateTime);
        assertTrue(v.isDateTime(), ()->"Not a dateTime: " + v);
    }

    @Test
    public void testNodeGYear1() {
        NodeValue v = NodeValue.makeNode("2021", XSDDatatype.XSDgYear);
        assertTrue(v.isGYear(), ()->"Not a gYear: " + v);
    }

    @Test
    public void testNodeGYear2() {
        NodeValue v = NodeValue.makeNode("\t2021\t", XSDDatatype.XSDgYear);
        assertTrue(v.isGYear(), ()->"Not a gYear: " + v);
    }

    @Test
    public void testNodeDuration1() {
        NodeValue v = NodeValue.makeNode("P1Y", XSDDatatype.XSDyearMonthDuration);
        assertTrue(v.isYearMonthDuration(), ()->"Not a yearMonthDuration: " + v);
        assertTrue(v.isDuration(), ()->"Not a duration: " + v);
    }

    @Test
    public void testNodeDuration2() {
        NodeValue v = NodeValue.makeNode("P1Y  ", XSDDatatype.XSDduration);
        assertTrue(v.isDuration(), ()->"Not a yearMonthDuration: " + v);
    }

    @Test
    public void testNodeDuration3() {
        // Internal whiespace -> invalide.
        NodeValue v = NodeValue.makeNode("P1Y  10S", XSDDatatype.XSDduration);
        assertFalse(v.isDuration(), ()->"Is a valid duration: " + v);
    }

    static NodeValue make(String str) {
        Node n = NodeFactoryExtra.parseNode(str);
        NodeValue nv = NodeValue.makeNode(n);
        return nv;
    }

    @Test public void gregorian_01() {
        NodeValue nv = make("'1999'^^xsd:gYear");

        assertTrue(nv.isGYear());
        assertFalse(nv.isGYearMonth());
        assertFalse(nv.isGMonth());
        assertFalse(nv.isGMonthDay());
        assertFalse(nv.isGDay());
    }

    @Test public void gregorian_02() {
        NodeValue nv = make("'1999-01'^^xsd:gYearMonth");

        assertFalse(nv.isGYear());
        assertTrue(nv.isGYearMonth());
        assertFalse(nv.isGMonth());
        assertFalse(nv.isGMonthDay());
        assertFalse(nv.isGDay());
    }

    @Test public void gregorian_03() {
        NodeValue nv = make("'--01'^^xsd:gMonth");

        assertFalse(nv.isGYear());
        assertFalse(nv.isGYearMonth());
        assertTrue(nv.isGMonth());
        assertFalse(nv.isGMonthDay());
        assertFalse(nv.isGDay());
    }

    @Test public void gregorian_04() {
        NodeValue nv = make("'--01-30'^^xsd:gMonthDay");

        assertFalse(nv.isGYear());
        assertFalse(nv.isGYearMonth());
        assertFalse(nv.isGMonth());
        assertTrue(nv.isGMonthDay());
        assertFalse(nv.isGDay());
    }

    @Test public void gregorian_05() {
        NodeValue nv = make("'---30'^^xsd:gDay");

        assertFalse(nv.isGYear());
        assertFalse(nv.isGYearMonth());
        assertFalse(nv.isGMonth());
        assertFalse(nv.isGMonthDay());
        assertTrue(nv.isGDay());
    }

    @Test public void langString_01() {

        NodeValue nv = make("''@en");
        assertFalse(nv.isString());
        assertTrue(nv.isLangString());
        assertEquals("en", nv.getLang());
        assertEquals("", nv.getString());
        assertEquals("", nv.asString());
    }

    @Test public void langString_02() {
        NodeValue nv = make("'not empty'@cy");
        assertFalse(nv.isString());
        assertTrue(nv.isLangString());
        assertEquals("cy", nv.getLang());
        assertEquals("not empty", nv.getString());
        assertEquals("not empty", nv.asString());
    }

    @Test
    public void testBadLexcial1() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            NodeValue v = NodeValue.makeNodeInteger("abc");
            assertFalse(v.isInteger(), ()->"Good integer: " + v);
            assertFalse(v.isNumber(), ()->"Good number: " + v);
        }
        finally {
            NodeValue.VerboseWarnings = b;
        }
    }

    @Test
    public void testBadLexcial2() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            NodeValue v = NodeValue.makeNodeInteger("1.8");
            assertFalse(v.isInteger(), ()->"Good integer: " + v);
            assertFalse(v.isNumber(), ()->"Good number: " + v);
        }
        finally {
            NodeValue.VerboseWarnings = b;
        }
    }

    @Test
    public void testBadLexcial3() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            NodeValue v = NodeValue.makeDateTime("2005-10-34T00:00:01Z");
            assertFalse(v.isDateTime(), ()->"Good date: " + v);
        }
        finally {
            NodeValue.VerboseWarnings = b;
        }
    }

    @Test
    public void testBadLexcial4() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            // Has a space
            String s = "2005-10-14T 09:30:23+01:00";
            NodeValue.VerboseWarnings = false;
            NodeValue v1 = NodeValue.makeDateTime(s);
            assertFalse(v1.isDateTime(), ()->"Good date: " + v1);
            s = s.replaceAll(" ", "");
            NodeValue v2 = NodeValue.makeDateTime(s);
            assertTrue(v2.isDateTime(), ()->"Bad date: " + v2);
        }
        finally {
            NodeValue.VerboseWarnings = b;
        }
    }

    // Effective boolean value rules.
    // boolean: value of the boolean
    // string: length(string) > 0 is true
    // numeric: number != Nan && number != 0 is true
    // http://www.w3.org/TR/xquery/#dt-ebv

    @Test
    public void testEBV1() {
        assertTrue(NodeValue.TRUE.isBoolean(), ()->"Not a boolean");
        assertTrue(NodeValue.TRUE.getBoolean(), ()->"Not true");
        assertTrue(XSDFuncOp.effectiveBooleanValue(NodeValue.TRUE), ()->"Not true");
    }

    @Test
    public void testEBV2() {
        assertTrue(NodeValue.FALSE.isBoolean(), ()->"Not a boolean");
        assertFalse(NodeValue.FALSE.getBoolean(), ()->"Not false");
        assertFalse(XSDFuncOp.effectiveBooleanValue(NodeValue.FALSE), ()->"Not false");
    }

    @Test
    public void testEBV3() {
        NodeValue v = NodeValue.makeInteger(1);
        assertFalse(v.isBoolean(), ()->"It's a boolean: " + v);
        assertTrue(XSDFuncOp.effectiveBooleanValue(v), ()->"Not EBV true: " + v);
    }

    @Test
    public void testEBV4() {
        NodeValue v = NodeValue.makeInteger(0);
        assertFalse(v.isBoolean(), ()->"It's a boolean: " + v);
        try {
            v.getBoolean();
            fail("getBoolean should fail");
        }
        catch (ExprEvalException e) {}
        assertFalse(XSDFuncOp.effectiveBooleanValue(v), ()->"Not EBV false: " + v);
    }

    @Test
    public void testEBV5() {
        NodeValue v = NodeValue.makeString("xyz");
        assertFalse(v.isBoolean(), ()->"It's a boolean: " + v);
        // assertTrue("Not a node: "+v, v.hasNode());
        try {
            v.getBoolean();
            fail("getBoolean should fail");
        }
        catch (ExprEvalException e) {}
        assertTrue(XSDFuncOp.effectiveBooleanValue(v), ()->"Not EBV true: " + v);
    }

    @Test
    public void testEBV6() {
        NodeValue v = NodeValue.makeString("");
        assertFalse(v.isBoolean(), ()->"It's a boolean: " + v);
        try {
            v.getBoolean();
            fail("getBoolean should fail");
        }
        catch (ExprEvalException e) {}
        assertFalse(XSDFuncOp.effectiveBooleanValue(v), ()->"Not EBV false: " + v);
    }

    // EBV includes plain literals which includes language tagged literals.
    @Test
    public void testEBV7() {
        Node x = NodeFactory.createLiteralLang("", "en");
        NodeValue v = NodeValue.makeNode(x);
        assertFalse(XSDFuncOp.effectiveBooleanValue(v), ()->"Not EBV false: " + v);
    }

    @Test
    public void testEBV8() {
        Node x = NodeFactory.createLiteralLang("not empty", "en");
        NodeValue v = NodeValue.makeNode(x);
        assertTrue(XSDFuncOp.effectiveBooleanValue(v), ()->"Not EBV true: " + v);
    }

    static boolean ebvDouble(double d) {
        return XSDFuncOp.effectiveBooleanValue(NodeValue.makeDouble(d));
    }

    @Test
    public void testEBV9() {
        assertTrue ( ebvDouble(0.01d) );
        assertFalse( ebvDouble(0.0d) );
        assertFalse( ebvDouble(-0.0d) );

        assertFalse( ebvDouble(Double.NaN) );

        assertTrue ( ebvDouble(Double.MIN_NORMAL) );
        assertTrue ( ebvDouble(Double.MIN_VALUE) );
        assertTrue ( ebvDouble(Double.MAX_VALUE) );

        assertTrue ( ebvDouble(Double.POSITIVE_INFINITY) );
        assertTrue ( ebvDouble(Double.NEGATIVE_INFINITY) );

        Node x = NodeFactory.createLiteralDT("NaN", XSDDatatype.XSDdouble);
        NodeValue v = NodeValue.makeNode(x);
        assertFalse(XSDFuncOp.effectiveBooleanValue(v));
    }

    static boolean ebvFloat(float f) {
        return XSDFuncOp.effectiveBooleanValue(NodeValue.makeFloat(f));
    }

    @Test
    public void testEBV10() {
        assertTrue ( ebvFloat(0.01f) );
        assertFalse( ebvFloat(0.0f) );
        assertFalse( ebvFloat(-0.0f) );

        assertFalse( ebvFloat(Float.NaN) );

        assertTrue ( ebvFloat(Float.MIN_NORMAL) );
        assertTrue ( ebvFloat(Float.MIN_VALUE) );
        assertTrue ( ebvFloat(Float.MAX_VALUE) );

        assertTrue ( ebvFloat(Float.POSITIVE_INFINITY) );
        assertTrue ( ebvFloat(Float.NEGATIVE_INFINITY) );

        Node x = NodeFactory.createLiteralDT("NaN", XSDDatatype.XSDfloat);
        NodeValue v = NodeValue.makeNode(x);
        assertFalse(XSDFuncOp.effectiveBooleanValue(v));
    }

    private static boolean filterEBV(NodeValue nv) {
        try {
            return XSDFuncOp.effectiveBooleanValue(nv);
        }
        catch (ExprEvalException ex) {
            return false;
        }
    }

    @Test
    public void testFloatDouble1() {
        NodeValue v1 = NodeValue.makeNodeDouble("1.5");
        NodeValue v2 = NodeValue.makeNode("1.5", XSDDatatype.XSDfloat);
        assertTrue(NodeValue.sameValueAs(v1, v2), ()->"Should be equal: 1.5 float and 1.5 double");
    }

    @Test
    public void testFloatDouble5() {
        NodeValue v1 = NodeValue.makeNodeDouble("1.3");
        NodeValue v2 = NodeValue.makeNode("1.3", XSDDatatype.XSDfloat);
        assertFalse(NodeValue.sameValueAs(v1, v2), ()->"Should not be equal: 1.3 float and 1.3 double");
    }

    // More effective boolean values - see TestExpressionARQ

    @Test
    public void testString1() {
        NodeValue v = NodeValue.makeString("string");
        assertTrue(v.isString(), ()->"Not a string: " + v);
        assertFalse(v.hasNode(), ()->"Is a node: " + v);

    }

    @Test
    public void testNodeString1() {
        NodeValue v = NodeValue.makeNode("string", null, (String)null); // Plain
                                                                        // literal
        assertTrue(v.isString(), ()->"Not a string: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
    }

    @Test
    public void testNodeString2() {
        NodeValue v = NodeValue.makeNode("string", null, (String)null); // Plain
                                                                        // literal
        assertTrue(v.isString(), ()->"Not a string: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();
        assertEquals("\"string\"", actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeString3() {
        NodeValue v = NodeValue.makeNode("string", XSDDatatype.XSDstring);
        assertTrue(v.isString(), ()->"Not a string: " + v);
        assertTrue(v.hasNode(), ()->"Not a node: " + v);
        String actualStr = v.asQuotedString();
        // RDF 1.1 -- appearance is a without ^^
        String rightAnswer = "\"string\"";
        assertEquals(rightAnswer, actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeSortKey1() {
        NodeValue nv = NodeValue.makeSortKey("Wagen","de");
        assertTrue(nv.isSortKey(), ()->"Not a sort key: " + nv);
        String actualStr = nv.asQuotedString();
        String rightAnswer = "\"Wagen\"";
        assertEquals(rightAnswer, actualStr, ()->"Print form mismatch");
    }

    @Test
    public void testNodeSortKey2() {
        final String[] unordered =
                {"Broager", "Åkirkeby", "Børkop", "Ærøskøbing", "Brædstrup", "Wandsbek"};
        final String[] ordered =
                {"Broager", "Brædstrup", "Børkop", "Wandsbek", "Ærøskøbing", "Åkirkeby"};
        // tests collation sort order for Danish
        final String collation = "da";
        List<NodeValue> nodeValues = new LinkedList<>();
        for (String string : unordered) {
            nodeValues.add(NodeValue.makeSortKey(string, collation));
        }
        nodeValues.sort(new Comparator<NodeValue>() {
            @Override
            public int compare(NodeValue o1, NodeValue o2) {
                return NodeValue.compare(o1, o2);
            }
        });
        List<String> result = new LinkedList<>();
        for (NodeValue nv : nodeValues) {
            String s = nv.asNode().getLiteralLexicalForm();
            result.add(s);
        }
        assertArrayEquals(ordered, result.toArray(new String[0]));
    }

    @Test
    public void testNodeSortKey3() {
        final String[] unordered = new String[]
                {"Broager", "Åkirkeby", "Børkop", "Ærøskøbing", "Brædstrup", "Wandsbek"};
        final String[] ordered = new String[]
                {"Ærøskøbing", "Åkirkeby", "Brædstrup", "Broager", "Børkop", "Wandsbek"};
        // tests collation sort order with Danish words, but New Zealand English collation rules
        final String collation = "en-NZ";
        List<NodeValue> nodeValues = new LinkedList<>();
        for (String string : unordered) {
            nodeValues.add(NodeValue.makeSortKey(string, collation));
        }
        nodeValues.sort(new Comparator<NodeValue>() {
            @Override
            public int compare(NodeValue o1, NodeValue o2) {
                return NodeValue.compare(o1, o2);
            }
        });
        List<String> result = new LinkedList<>();
        for (NodeValue nv : nodeValues) {
            String s = nv.asNode().getLiteralLexicalForm();
            result.add(s);
        }
        assertArrayEquals(ordered, result.toArray(new String[0]));
    }

    @Test
    public void testNodeSortKey4() {
        // Collation sort order for Finnish
        final String collation = "fi";
        String[] ordered = new String[]
                {"tsahurin kieli", "tšekin kieli", "tulun kieli", "töyhtöhyyppä"};
        // Query String
        final String queryString = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX arq: <http://jena.apache.org/ARQ/function#>\n" +
                "SELECT ?label WHERE {\n" +
                "   VALUES ?label { \"tulun kieli\"@es \"tšekin kieli\" \"tsahurin kieli\"@en \"töyhtöhyyppä\"@fi }\n" +
                "}\n" +
                "ORDER BY arq:collation(\"" + collation + "\", ?label)";
        Model model = ModelFactory.createDefaultModel();
        Query query = QueryFactory.create(queryString);
        List<String> result = new LinkedList<>();
        try (QueryExecution qExec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qExec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                result.add(solution.getLiteral(solution.varNames().next()).getLexicalForm());
            }
        }
        assertArrayEquals(ordered, result.toArray(new String[0]));
    }

    @Test
    public void testNodeSortKey5() {
     // Collation sort order for English from Belize
        final String collation = "en-BZ";
        String[] ordered = new String[]
                {"töyhtöhyyppä", "tsahurin kieli", "tšekin kieli", "tulun kieli"};
        // Query String
        final String queryString = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX arq: <http://jena.apache.org/ARQ/function#>\n" +
                "SELECT ?label WHERE {\n" +
                "   VALUES ?label { \"tulun kieli\"@es \"tšekin kieli\" \"tsahurin kieli\"@en \"töyhtöhyyppä\"@fi }\n" +
                "}\n" +
                "ORDER BY arq:collation(\"" + collation + "\", ?label)";
        Model model = ModelFactory.createDefaultModel();
        Query query = QueryFactory.create(queryString);
        List<String> result = new LinkedList<>();
        try (QueryExecution qExec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qExec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                result.add(solution.getLiteral(solution.varNames().next()).getLexicalForm());
            }
        }
        assertArrayEquals(ordered, result.toArray(new String[0]));
    }

    @Test
    public void testSameValue1() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeInteger(7);
        assertTrue(NodeValue.notSameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");
        assertFalse(NodeValue.sameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");

        NodeValue nv3 = NodeValue.makeInteger(5);
        assertTrue(NodeValue.sameValueAs(nv1, nv3), ()->"Different values (" + nv1 + "," + nv3 + ")");
        assertFalse(NodeValue.notSameValueAs(nv1, nv3), ()->"Different values - notNotSame (" + nv1 + "," + nv3 + ")");
    }

    @Test
    public void testSameValue2() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeNodeInteger(7);
        assertTrue(NodeValue.notSameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");
        assertFalse(NodeValue.sameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");

        NodeValue nv3 = NodeValue.makeNodeInteger(5);
        assertTrue(NodeValue.sameValueAs(nv1, nv3), ()->"Different values (" + nv1 + "," + nv3 + ")");
        assertFalse(NodeValue.notSameValueAs(nv1, nv3), ()->"Different values - notNotSame (" + nv1 + "," + nv3 + ")");
    }

    @Test
    public void testSameValue3() {
        NodeValue nv1 = NodeValue.makeDecimal("1.5");
        NodeValue nv2 = NodeValue.makeDecimal("1.6");
        assertTrue(NodeValue.notSameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");
        assertFalse(NodeValue.sameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");

        NodeValue nv3 = NodeValue.makeDecimal("1.50");
        assertTrue(NodeValue.sameValueAs(nv1, nv3), ()->"Different values (" + nv1 + "," + nv3 + ")");
        assertFalse(NodeValue.notSameValueAs(nv1, nv3), ()->"Different values - notNotSame (" + nv1 + "," + nv3 + ")");
    }

    @Test
    public void testSameValue4() {
        NodeValue nv1 = NodeValue.makeDecimal("3");
        NodeValue nv2 = NodeValue.makeInteger(4);
        assertTrue(NodeValue.notSameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");
        assertFalse(NodeValue.sameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");

        NodeValue nv3 = NodeValue.makeInteger(3);
        assertTrue(NodeValue.sameValueAs(nv1, nv3), ()->"Different values (" + nv1 + "," + nv3 + ")");
        assertFalse(NodeValue.notSameValueAs(nv1, nv3), ()->"Different values - notNotSame (" + nv1 + "," + nv3 + ")");
    }

    @Test
    public void testSameValue5() {
        NodeValue nv1 = NodeValue.makeDecimal("-1.5"); // Must be exact for
                                                       // double and decimal
        NodeValue nv2 = NodeValue.makeDouble(1.5);
        assertTrue(NodeValue.notSameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");
        assertFalse(NodeValue.sameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");

        NodeValue nv3 = NodeValue.makeDouble(-1.5);
        assertTrue(NodeValue.sameValueAs(nv1, nv3), ()->"Different values (" + nv1 + "," + nv3 + ")");
        assertFalse(NodeValue.notSameValueAs(nv1, nv3), ()->"Different values - notNotSame (" + nv1 + "," + nv3 + ")");
    }

    @Test
    public void testSameValue6() {
        NodeValue nv1 = NodeValue.makeNodeInteger(17);
        NodeValue nv2 = NodeValue.makeDouble(34);
        assertTrue(NodeValue.notSameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");
        assertFalse(NodeValue.sameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");

        NodeValue nv3 = NodeValue.makeDouble(17);
        assertTrue(NodeValue.sameValueAs(nv1, nv3), ()->"Different values (" + nv1 + "," + nv3 + ")");
        assertFalse(NodeValue.notSameValueAs(nv1, nv3), ()->"Different values - notNotSame (" + nv1 + "," + nv3 + ")");
    }

    @Test
    public void testSameValue7() {
        NodeValue nv1 = NodeValue.makeBoolean(true);
        NodeValue nv2 = NodeValue.makeString("a");
        assertTrue(NodeValue.notSameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");
        assertFalse(NodeValue.sameValueAs(nv1, nv2), ()->"Same values (" + nv1 + "," + nv2 + ")");

        NodeValue nv3 = NodeValue.makeNodeBoolean(true);
        assertTrue(NodeValue.sameValueAs(nv1, nv3), ()->"Different values (" + nv1 + "," + nv3 + ")");
        assertFalse(NodeValue.notSameValueAs(nv1, nv3), "Different values - notNotSame (" + nv1 + "," + nv3 + ")");
    }

    @Test
    public void testSameValueNaN_double_1() {
        NodeValue nv1 = NodeValue.makeNode("NaN", XSDDatatype.XSDdouble);
        NodeValue nv2 = NodeValue.makeNode("NaN", XSDDatatype.XSDdouble);
        assertEquals(nv1, nv2);
        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test
    public void testSameValueNaN_float_1() {
        NodeValue nv1 = NodeValue.makeNode("NaN", XSDDatatype.XSDfloat);
        NodeValue nv2 = NodeValue.makeNode("NaN", XSDDatatype.XSDfloat);
        assertEquals(nv1, nv2);
        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));
        // NaN is weird.
        assertFalse(NodeValue.sameValueAs(nv1, nv1));
        assertTrue(NodeValue.notSameValueAs(nv1, nv1));
    }

    @Test
    public void testSameValueNaN_double_2() {
        NodeValue nv1 = NodeValue.makeDouble(Double.NaN);
        NodeValue nv2 = NodeValue.makeDouble(Double.NaN);
        assertEquals(nv1, nv2);
        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));

        assertFalse(NodeValue.sameValueAs(nv1, nv1));
        assertTrue(NodeValue.notSameValueAs(nv1, nv1));
    }

    @Test
    public void testSameValueNaN_float_2() {
        NodeValue nv1 = NodeValue.makeFloat(Float.NaN);
        NodeValue nv2 = NodeValue.makeFloat(Float.NaN);
        assertEquals(nv1, nv2);
        assertFalse(NodeValue.sameValueAs(nv1, nv2));
        assertTrue(NodeValue.notSameValueAs(nv1, nv2));

        assertFalse(NodeValue.sameValueAs(nv1, nv1));
        assertTrue(NodeValue.notSameValueAs(nv1, nv1));
    }

    @Test
    public void testLang1() {
        Node n1 = org.apache.jena.graph.NodeFactory.createLiteralLang("xyz", "en");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = org.apache.jena.graph.NodeFactory.createLiteralLang("xyz", "en");
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertTrue(NodeValue.sameValueAs(nv1, nv2));
    }

    @Test
    public void testLang2() {
        Node n1 = org.apache.jena.graph.NodeFactory.createLiteralLang("xyz", "en");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = org.apache.jena.graph.NodeFactory.createLiteralLang("xyz", "EN");
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertTrue(NodeValue.sameValueAs(nv1, nv2));
        // Jena5 - langtags are formatted on creation so node are unique upto case.
        assertTrue(nv1.equals(nv2));
    }

    @Test
    public void testLang3() {
        Node n1 = org.apache.jena.graph.NodeFactory.createLiteralLang("xyz", "en");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = org.apache.jena.graph.NodeFactory.createLiteralLang("xyz", "en");
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertFalse(NodeValue.notSameValueAs(nv1, nv2));
    }

    @Test
    public void testLang4() {
        NodeValue nv1 = parse("'xyz'@en");
        NodeValue nv2 = parse("'xyz'@EN");
        assertFalse(NodeValue.notSameValueAs(nv1, nv2));
        // Jena5 - langtags are formatted on creation so node are unique upto case.
        assertTrue(nv1.equals(nv2));
    }

    //Compare value first and then language tag
    @Test
    public void testLangCompareAlways1() {
        // @de before @en => "'abc'@en" > "'bcd'@de" => +1
        NodeValue nv1 = parse("'abc'@en");
        NodeValue nv2 = parse("'bcd'@de");
        assertEquals(Expr.CMP_GREATER, NodeCmp.compareRDFTerms(nv1.getNode(), nv2.getNode()));
        assertEquals(Expr.CMP_GREATER, NodeValue.compareAlways(nv1, nv2));
    }

    //Language tag comparison ignore case first, then case sensitive
    @Test
    public void testLangCompareAlways2() {
        // @FR before @it. => "'abc'@it" > "'abc'@FR" => +1
        NodeValue nv1 = parse("'abc'@it");
        NodeValue nv2 = parse("'abc'@FR");
        assertEquals(Expr.CMP_GREATER, NodeCmp.compareRDFTerms(nv1.getNode(), nv2.getNode()));
        assertEquals(Expr.CMP_GREATER, NodeValue.compareAlways(nv1, nv2));
    }

    public void testLangCompareAlways3() {
        // @FR after @en. => "'abc'@en" < "'abc'@FR" => -1
        NodeValue nv1 = parse("'abc'@en");
        NodeValue nv2 = parse("'abc'@FR");
        assertEquals(Expr.CMP_LESS, NodeCmp.compareRDFTerms(nv1.getNode(), nv2.getNode()));
        assertEquals(Expr.CMP_LESS, NodeValue.compareAlways(nv1, nv2));
    }


    @Test
    public void testEquals1() {
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeInteger(1);
        assertEquals(nv1, nv2, "Not NodeValue.equals()");
    }

    @Test
    public void testEquals2() {
        NodeValue nv1 = NodeValue.makeNodeInteger(1);
        NodeValue nv2 = NodeValue.makeInteger(1);
        assertEquals(nv1, nv2, "Not NodeValue.equals()");
    }

    @Test
    public void testEquals3() { // Make different ways but equals
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeNodeInteger(1);
        assertEquals(nv1, nv2, "Not NodeValue.equals()");
    }

    @Test
    public void testEquals4() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createURI("http://example"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("http://example"));
        assertEquals(nv1, nv2, "Not NodeValue.equals()");
    }

    @Test
    public void testNotEquals1() {
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeInteger(2);
        assertFalse(nv1.equals(nv2), "NodeValue.equals()");
    }

    @Test
    public void testNotEquals2() {
        NodeValue nv1 = NodeValue.makeNodeInteger(1);
        NodeValue nv2 = NodeValue.makeNodeString("1");
        assertFalse(nv1.equals(nv2), "NodeValue.equals()");
    }

    @Test
    public void testNotEquals3() { // Literals and URIs are different.
        NodeValue nv1 = NodeValue.makeNode(org.apache.jena.graph.NodeFactory.createURI("http://example"));
        NodeValue nv2 = NodeValue.makeNode(org.apache.jena.graph.NodeFactory.createLiteralString("http://example"));
        assertFalse(nv1.equals(nv2), "NodeValue.equals()");
    }

    @Test
    public void testTripleTerms1() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p 456)>>");
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        int xa = NodeValue.compare(nv1, nv2);
        assertEquals(Expr.CMP_LESS, xa);
        int xb = NodeValue.compare(nv2, nv1);
        assertEquals(Expr.CMP_GREATER, xb);
    }

    @Test
    public void testTripleTerms2() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p 'abc')>>");
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertThrows(ExprNotComparableException.class, ()->NodeValue.compare(nv1, nv2));
    }

    @Test
    public void testTripleTerms3() {
        // General (sort) comparison. 'abc' < before 123
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p 'abc')>>");
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        int x = NodeValue.compareAlways(nv1, nv2);
        assertEquals(Expr.CMP_GREATER, x);
    }

    @Test
    public void testBadNodeValue1() {
        assertThrows(ExprException.class, ()-> {
            Node n = SSE.parseNode("?variable");
            NodeValue.makeNode(n);
        });
    }

    @Test
    public void testBadNodeValue2() {
        assertThrows(ExprException.class, ()-> {
            Node n = SSE.parseNode("<<(:s :p ?variable)>>");
            NodeValue.makeNode(n);
        });
    }

    @Test
    public void testBadNodeValue3() {
        assertThrows(ExprException.class, ()-> {
            Node n = SSE.parseNode("<<( :s :p <<( :x :y ?variable )>> )>>");
            NodeValue.makeNode(n);
        });
    }

    @Test
    public void testCustomNode() {
        Node expectedNode = Node_Marker.marker("test");
        Expr expr = ExprLib.nodeToExpr(expectedNode);
        NodeValue nv = expr.getConstant();
        Node actualNode = nv.asNode();
        assertEquals(expectedNode, actualNode);
    }
}
