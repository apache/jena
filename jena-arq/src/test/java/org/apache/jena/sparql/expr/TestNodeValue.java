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

import static org.junit.Assert.*;

import java.math.BigDecimal ;
import java.util.*;

import org.apache.jena.JenaRuntime ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/** Break expression testing suite into parts
 * @see TestExpressions
 * @see TestExprLib
 * @see TestNodeValue
 */
public class TestNodeValue
{
    static final double doubleAccuracy = 0.00000001d ;
    static boolean warningSetting ;

    @BeforeClass public static void beforeClass() {
        warningSetting = NodeValue.VerboseWarnings ;
        NodeValue.VerboseWarnings = false ;
    }

    @AfterClass public static void afterClass() {
        NodeValue.VerboseWarnings = warningSetting ;
    }

    @Test
    public void testInt1() {
        NodeValue v = NodeValue.makeInteger(5);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertFalse("Is a node: " + v, v.hasNode());
    }

    @Test
    public void testInt2() {
        NodeValue v = NodeValue.makeNodeInteger(5);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testInt3() {
        NodeValue v1 = NodeValue.makeNodeInteger(5);
        NodeValue v2 = NodeValue.makeInteger(5);
        assertTrue("Not same integer: " + v1 + " & " + v2, v1.getInteger().equals(v2.getInteger()));
    }

    @Test
    public void testFloat1() {
        NodeValue v = NodeValue.makeFloat(5);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a float: " + v, v.isFloat());
        assertTrue("Float not a double: " + v, v.isDouble());
        assertFalse("No node: " + v, v.hasNode());
    }

    @Test
    public void testFloat2() {
        NodeValue v = NodeValue.makeNodeFloat(5);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a float: " + v, v.isDouble());
        assertTrue("Float not a double: " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testFloat3() {
        NodeValue v1 = NodeValue.makeNodeFloat(5.7f);
        NodeValue v2 = NodeValue.makeFloat(5.7f);
        assertTrue("Not same float: " + v1 + " & " + v2, v1.getFloat() == v2.getFloat());
        assertTrue("Not same float as double: " + v1 + " & " + v2, v1.getDouble() == v2.getDouble());
    }

    @Test
    public void testDouble1() {
        NodeValue v = NodeValue.makeDouble(5);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDouble());
        assertFalse("No node: " + v, v.hasNode());
    }

    @Test
    public void testDouble2() {
        NodeValue v = NodeValue.makeNodeDouble(5);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testDouble3() {
        NodeValue v1 = NodeValue.makeNodeDouble(5.7);
        NodeValue v2 = NodeValue.makeDouble(5.7);
        assertTrue("Not same double: " + v1 + " & " + v2, v1.getDouble() == v2.getDouble());
    }

    @Test
    public void testDecimal1() {
        NodeValue v = NodeValue.makeDecimal(new BigDecimal("1.3"));
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDecimal());
        assertFalse("Is a node: " + v, v.hasNode());
    }

    @Test
    public void testDecimal2() {
        NodeValue v = NodeValue.makeNodeDecimal("1.3");
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDecimal());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testDecimal3() {
        NodeValue v1 = NodeValue.makeDecimal(new BigDecimal("1.3"));
        NodeValue v2 = NodeValue.makeNodeDecimal("1.3");
        assertTrue("Not same decimal: " + v1 + " & " + v2, v1.getDecimal().compareTo(v2.getDecimal()) == 0);
        assertEquals("Not same decimal by equals: " + v1 + " & " + v2, v1, v2);
    }

    @Test
    public void testDateTime1() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Clear/ set all fields (milliseconds included).
        cal.setTimeInMillis(0);
        cal.set(2005, 01, 18, 20, 39, 10); // NB Months from 0, not 1

        NodeValue v = NodeValue.makeDateTime(cal);
        assertTrue("Not a dateTime: " + v, v.isDateTime());
        assertFalse("A date: " + v, v.isDate());
        // DateTimes always have nodes because we used that to parse the thing.
    }

    @Test
    public void testDateTime2() {
        NodeValue v = NodeValue.makeNodeDateTime("2005-02-18T20:39:10Z");
        assertTrue("Not a dateTime: " + v, v.isDateTime());
        assertFalse("A date: " + v, v.isDate());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testDateTime3() {
        NodeValue v1 = NodeValue.makeDateTime("2005-02-18T20:39:10Z");
        NodeValue v2 = NodeValue.makeNodeDateTime("2005-02-18T20:39:10Z");
        assertEquals("Not Calendar.equals: ", v1.getDateTime(), v2.getDateTime());
    }

    @Test
    public void testDateTime4() {
        Calendar cal1 = new GregorianCalendar();
        cal1.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Clear/ set all fields (milliseconds included).
        cal1.setTimeInMillis(0);
        cal1.set(2005, 01, 18, 20, 39, 10); // NB Months from 0, not 1

        NodeValue v = NodeValue.makeNode("2005-02-18T20:39:10Z", XSDDatatype.XSDdateTime);
        assertTrue("Not a dateTime: " + v, v.isDateTime());
        assertFalse("A date: " + v, v.isDate());
        assertTrue("Not a node: " + v, v.hasNode());
        Calendar cal2 = v.getDateTime().toGregorianCalendar();
        assertEquals("Not equal: " + v, 0, cal1.compareTo(cal2));
    }

    @Test
    public void testDateTime5() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            // Illegal lexical for a dateTime.
            NodeValue v = NodeValue.makeNode("2005-02-18", XSDDatatype.XSDdateTime);
            assertFalse("Date!: " + v, v.isDate());
            assertFalse("Datetime!: " + v, v.isDateTime());
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
        assertEquals("Not Calendar.equals: ", v0.getDateTime(), v1.getDateTime());
        assertEquals("Not Calendar.equals: ", v0.getDateTime(), v2.getDateTime());
        assertEquals("Not Calendar.equals: ", v0.getDateTime(), v3.getDateTime());
    }

    @Test
    public void testDateTime7() {
        NodeValue v0 = NodeValue.makeDateTime("2005-02-18T20:39:10Z");
        NodeValue v1 = NodeValue.makeDateTime("2005-02-18T20:39:10.001Z");
        assertNotSame("Calendar.equals: ", v0.getDateTime(), v1.getDateTime());
    }

    @Test
    public void testDateTime8() {
        NodeValue v0 = NodeValue.makeDateTime("2005-02-18T20:39:10-05:00");
        NodeValue v1 = NodeValue.makeDateTime("2005-02-18T17:39:10.000-08:00");
        assertEquals("Not Calendar.equals: ", v0.getDateTime(), v1.getDateTime());
    }

    @Test
    public void testDateTimeStamp1() {
        // xsd:dateTimeStamp is a derived datatype of xsd:dateTime.
        Node n = SSE.parseNode("'2000-01-01T00:00:00+00:00'^^xsd:dateTimeStamp");
        NodeValue nv = NodeValue.makeNode(n);
        assertTrue(nv.isDateTime());
        assertFalse(nv.isDate());
    }

    @Test
    public void testDate1() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Clear/ set all fields (milliseconds included).
        cal.setTimeInMillis(0);
        // NB Months from 0, not 1
        // For a date, must be time = 00:00:00
        cal.set(2005, 01, 18, 0, 0, 0);

        NodeValue v = NodeValue.makeDate(cal);
        assertTrue("Not a date: " + v, v.isDate());
        assertFalse("A dateTime: " + v, v.isDateTime());
        // DateTimes always have nodes because we used that to parse the thing.
    }

    @Test
    public void testDate2() {
        NodeValue v = NodeValue.makeNodeDate("2005-02-18");
        assertTrue("Not a date: " + v, v.isDate());
        assertFalse("A dateTime: " + v, v.isDateTime());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testDate3() {
        NodeValue v1 = NodeValue.makeDate("2005-02-18+01:00");
        NodeValue v2 = NodeValue.makeNodeDate("2005-02-18+01:00");
        assertEquals("Not Calendar.equals: ", v1.getDateTime(), v2.getDateTime());
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
        assertTrue("Not a date: " + v, v.isDate());
        assertFalse("A dateTime: " + v, v.isDateTime());
        assertTrue("Not a node: " + v, v.hasNode());
        Calendar cal2 = v.getDateTime().toGregorianCalendar();
        assertEquals("Not equal: " + v, 0, cal1.compareTo(cal2));
    }

    @Test
    public void testDate5() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            // Illegal lexical for a date.
            NodeValue v = NodeValue.makeNode("2005-02-18T20:39:10Z", XSDDatatype.XSDdate);
            assertFalse("Datetime!: " + v, v.isDateTime());
            assertFalse("Date!: " + v, v.isDate());
        }
        finally {
            NodeValue.VerboseWarnings = b;
        }
    }

    @Test
    public void testNodeInt1() {
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDinteger);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testNodeInt2() {
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDdouble);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testNodeInt3() {
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDinteger);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();
        assertEquals("Print form mismatch", "57", actualStr);
    }

    @Test
    public void testNodeInt4() {
        NodeValue v = NodeValue.makeNodeInteger(18);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();
        assertEquals("Print form mismatch", "18", actualStr);
    }

    @Test
    public void testNodeInt5() {
        // Legal as a bare integer but not canonical form
        NodeValue v = NodeValue.makeNodeInteger("018");
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();
        assertEquals("Print form mismatch", "018", actualStr);
    }

    @Test
    public void testNodeInt6() {
        // Leading/trail whitespace.
        NodeValue v = NodeValue.makeNodeInteger(" 18");
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testNodeInt7() {
        // Leading/trail whitespace.
        NodeValue v = NodeValue.makeNodeInteger(" 18 ");
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not an integer: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testNodeInt8() {
        // Internal whitespace. Not a number.
        NodeValue v = NodeValue.makeNodeInteger("1 8");
        assertFalse("A number!: " + v, v.isNumber());
        assertFalse("An integer!: " + v, v.isInteger());
        assertTrue("Not a node: " + v, v.hasNode());
    }


    @Test
    public void testNodeFloat1() {
        // Theer is no SPARQL representation in short form of a float.
        NodeValue v = NodeValue.makeNode("57.0", XSDDatatype.XSDfloat);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a float: " + v, v.isFloat());
        assertTrue("Not a double(float): " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();

        assertEquals("Print form mismatch", "\"57.0\"^^<" + XSDDatatype.XSDfloat.getURI() + ">", actualStr);
    }

    @Test
    public void testNodeDouble1() {
        // Note input form is legal and canomical as a lexical form double
        NodeValue v = NodeValue.makeNode("57.0e0", XSDDatatype.XSDdouble);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();

        assertEquals("Print form mismatch", "57.0e0", actualStr) ;
//                     "\"57\"^^<"+XSDDatatype.XSDdouble.getURI()+">",
//                     actualStr);
    }

    @Test
    public void testNodeDouble2() {
        // Note input form is not legal as a lexical form double
        NodeValue v = NodeValue.makeNode("57", XSDDatatype.XSDdouble);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();

        assertEquals("Print form mismatch", "\"57\"^^<" + XSDDatatype.XSDdouble.getURI() + ">", actualStr);
    }

    @Test
    public void testNodeDouble3() {
        // Note input form is legal but not canonical as a bare FP
        NodeValue v = NodeValue.makeNode("057.0e0", XSDDatatype.XSDdouble);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();

        assertEquals("Print form mismatch", "057.0e0", actualStr);
    }

    @Test
    public void testNodeDouble4() {
        // Leading/trail whitespace.
        NodeValue v = NodeValue.makeNode(" 057.0e0 ", XSDDatatype.XSDdouble);
        assertTrue("Not a number: " + v, v.isNumber());
        assertTrue("Not a double: " + v, v.isDouble());
        assertTrue("Not a node: " + v, v.hasNode());
    }


    @Test
    public void testNodeBool1() {
        NodeValue v = NodeValue.makeNode("true", XSDDatatype.XSDboolean);
        assertTrue("Not a boolean: " + v, v.isBoolean());
        assertTrue("Not a node: " + v, v.hasNode());
        assertTrue("Not satisfied: " + v, v.getBoolean());
    }

    @Test
    public void testNodeBool2() {
        NodeValue v = NodeValue.makeNode("false", XSDDatatype.XSDboolean);
        assertTrue("Not a boolean: " + v, v.isBoolean());
        assertTrue("Not a node: " + v, v.hasNode());
        assertFalse("Satisfied: " + v, v.getBoolean());
    }

    @Test
    public void testNodeBool3() {
        NodeValue v = NodeValue.makeBoolean(true);
        assertTrue("Not a boolean: " + v, v.isBoolean());
        // assertTrue("Not a node: "+v, v.hasNode()) ;
        assertTrue("Not true: " + v, v.getBoolean());
        assertTrue("Not true: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    @Test
    public void testNodeBool4() {
        NodeValue v = NodeValue.makeBoolean(false);
        assertTrue("Not a boolean: " + v, v.isBoolean());
        // assertTrue("Not a node: "+v, v.hasNode()) ;
        assertFalse("Not false: " + v, v.getBoolean());
        assertFalse("Not false: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    static NodeValue make(String str) {
        Node n = NodeFactoryExtra.parseNode(str);
        NodeValue nv = NodeValue.makeNode(n);
        return nv;
    }

    @Test public void gregorian_01() {
        NodeValue nv = make("'1999'^^xsd:gYear") ;

        assertTrue(nv.isGYear()) ;
        assertFalse(nv.isGYearMonth()) ;
        assertFalse(nv.isGMonth()) ;
        assertFalse(nv.isGMonthDay()) ;
        assertFalse(nv.isGDay()) ;
    }

    @Test public void gregorian_02() {
        NodeValue nv = make("'1999-01'^^xsd:gYearMonth") ;

        assertFalse(nv.isGYear()) ;
        assertTrue(nv.isGYearMonth()) ;
        assertFalse(nv.isGMonth()) ;
        assertFalse(nv.isGMonthDay()) ;
        assertFalse(nv.isGDay()) ;
    }

    @Test public void gregorian_03() {
        NodeValue nv = make("'--01'^^xsd:gMonth") ;

        assertFalse(nv.isGYear()) ;
        assertFalse(nv.isGYearMonth()) ;
        assertTrue(nv.isGMonth()) ;
        assertFalse(nv.isGMonthDay()) ;
        assertFalse(nv.isGDay()) ;
    }

    @Test public void gregorian_04() {
        NodeValue nv = make("'--01-30'^^xsd:gMonthDay") ;

        assertFalse(nv.isGYear()) ;
        assertFalse(nv.isGYearMonth()) ;
        assertFalse(nv.isGMonth()) ;
        assertTrue(nv.isGMonthDay()) ;
        assertFalse(nv.isGDay()) ;
    }

    @Test public void gregorian_05() {
        NodeValue nv = make("'---30'^^xsd:gDay") ;

        assertFalse(nv.isGYear()) ;
        assertFalse(nv.isGYearMonth()) ;
        assertFalse(nv.isGMonth()) ;
        assertFalse(nv.isGMonthDay()) ;
        assertTrue(nv.isGDay()) ;
    }

    @Test public void langString_01() {

        NodeValue nv = make("''@en") ;
        assertFalse(nv.isString()) ;
        assertTrue(nv.isLangString()) ;
        assertEquals("en", nv.getLang()) ;
        assertEquals("", nv.getString()) ;
        assertEquals("", nv.asString()) ;
    }

    @Test public void langString_02() {
        NodeValue nv = make("'not empty'@cy") ;
        assertFalse(nv.isString()) ;
        assertTrue(nv.isLangString()) ;
        assertEquals("cy", nv.getLang()) ;
        assertEquals("not empty", nv.getString()) ;
        assertEquals("not empty", nv.asString()) ;
    }

    @Test
    public void testBadLexcial1() {
        boolean b = NodeValue.VerboseWarnings;
        try {
            NodeValue.VerboseWarnings = false;
            NodeValue v = NodeValue.makeNodeInteger("abc");
            assertFalse("Good integer: " + v, v.isInteger());
            assertFalse("Good number: " + v, v.isNumber());
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
            assertFalse("Good integer: " + v, v.isInteger());
            assertFalse("Good number: " + v, v.isNumber());
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
            assertFalse("Good date: " + v, v.isDateTime());
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
            assertFalse("Good date: " + v1, v1.isDateTime());
            s = s.replaceAll(" ", "");
            NodeValue v2 = NodeValue.makeDateTime(s);
            assertTrue("Bad date: " + v2, v2.isDateTime());
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
        assertTrue("Not a boolean", NodeValue.TRUE.isBoolean());
        assertTrue("Not true", NodeValue.TRUE.getBoolean());
        assertTrue("Not true", XSDFuncOp.booleanEffectiveValue(NodeValue.TRUE));
    }

    @Test
    public void testEBV2() {
        assertTrue("Not a boolean", NodeValue.FALSE.isBoolean());
        assertFalse("Not false", NodeValue.FALSE.getBoolean());
        assertFalse("Not false", XSDFuncOp.booleanEffectiveValue(NodeValue.FALSE));
    }

    @Test
    public void testEBV3() {
        NodeValue v = NodeValue.makeInteger(1);
        assertFalse("It's a boolean: " + v, v.isBoolean());
        assertTrue("Not EBV true: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    @Test
    public void testEBV4() {
        NodeValue v = NodeValue.makeInteger(0);
        assertFalse("It's a boolean: " + v, v.isBoolean());
        try {
            v.getBoolean();
            fail("getBoolean should fail");
        }
        catch (ExprEvalException e) {}
        assertFalse("Not EBV false: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    @Test
    public void testEBV5() {
        NodeValue v = NodeValue.makeString("xyz");
        assertFalse("It's a boolean: " + v, v.isBoolean());
        // assertTrue("Not a node: "+v, v.hasNode()) ;
        try {
            v.getBoolean();
            fail("getBoolean should fail");
        }
        catch (ExprEvalException e) {}
        assertTrue("Not EBV true: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    @Test
    public void testEBV6() {
        NodeValue v = NodeValue.makeString("");
        assertFalse("It's a boolean: " + v, v.isBoolean());
        try {
            v.getBoolean();
            fail("getBoolean should fail");
        }
        catch (ExprEvalException e) {}
        assertFalse("Not EBV false: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    // EBV includes plain literals which includes language tagged literals.
    @Test
    public void testEBV7() {
        Node x = NodeFactory.createLiteral("", "en");
        NodeValue v = NodeValue.makeNode(x);
        assertFalse("Not EBV false: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    @Test
    public void testEBV8() {
        Node x = NodeFactory.createLiteral("not empty", "en");
        NodeValue v = NodeValue.makeNode(x);
        assertTrue("Not EBV true: " + v, XSDFuncOp.booleanEffectiveValue(v));
    }

    static boolean ebvDouble(double d) {
        return XSDFuncOp.booleanEffectiveValue(NodeValue.makeDouble(d));
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

        Node x = NodeFactory.createLiteral("NaN", XSDDatatype.XSDdouble);
        NodeValue v = NodeValue.makeNode(x);
        assertFalse(XSDFuncOp.booleanEffectiveValue(v));
    }

    static boolean ebvFloat(float f) {
        return XSDFuncOp.booleanEffectiveValue(NodeValue.makeFloat(f));
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

        Node x = NodeFactory.createLiteral("NaN", XSDDatatype.XSDfloat);
        NodeValue v = NodeValue.makeNode(x);
        assertFalse(XSDFuncOp.booleanEffectiveValue(v));
    }

    private static boolean filterEBV(NodeValue nv) {
        try {
            return XSDFuncOp.booleanEffectiveValue(nv);
        }
        catch (ExprEvalException ex) {
            return false;
        }
    }

    @Test
    public void testFloatDouble1() {
        NodeValue v1 = NodeValue.makeNodeDouble("1.5");
        NodeValue v2 = NodeValue.makeNode("1.5", XSDDatatype.XSDfloat);
        assertTrue("Should be equal: 1.5 float and 1.5 double", NodeValue.sameAs(v1, v2));
    }

    @Test
    public void testFloatDouble5() {
        NodeValue v1 = NodeValue.makeNodeDouble("1.3");
        NodeValue v2 = NodeValue.makeNode("1.3", XSDDatatype.XSDfloat);
        assertFalse("Should not be equal: 1.3 float and 1.3 double", NodeValue.sameAs(v1, v2));
    }

    // More effective boolean values - see TestExpressionARQ

    @Test
    public void testString1() {
        NodeValue v = NodeValue.makeString("string");
        assertTrue("Not a string: " + v, v.isString());
        assertFalse("Is a node: " + v, v.hasNode());

    }

    @Test
    public void testNodeString1() {
        NodeValue v = NodeValue.makeNode("string", null, (String)null); // Plain
                                                                        // literal
        assertTrue("Not a string: " + v, v.isString());
        assertTrue("Not a node: " + v, v.hasNode());
    }

    @Test
    public void testNodeString2() {
        NodeValue v = NodeValue.makeNode("string", null, (String)null); // Plain
                                                                        // literal
        assertTrue("Not a string: " + v, v.isString());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();
        assertEquals("Print form mismatch", "\"string\"", actualStr);
    }

    @Test
    public void testNodeString3() {
        NodeValue v = NodeValue.makeNode("string", XSDDatatype.XSDstring); // XSD
                                                                           // String
                                                                           // literal
        assertTrue("Not a string: " + v, v.isString());
        assertTrue("Not a node: " + v, v.hasNode());
        String actualStr = v.asQuotedString();
        String rightAnswer = JenaRuntime.isRDF11
            // RDF 1.1 -- appearance is a without ^^
            ? "\"string\"" : "\"string\"^^<" + XSDDatatype.XSDstring.getURI() + ">";

        assertEquals("Print form mismatch", rightAnswer, actualStr);
    }

    @Test
    public void testNodeSortKey1() {
        NodeValue nv = NodeValue.makeSortKey("Wagen", "de");
        assertTrue("Not a sort key: " + nv, nv.isSortKey());
        String actualStr = nv.asQuotedString();
        String rightAnswer = "\"Wagen\"";
        assertEquals("Print form mismatch", rightAnswer, actualStr);
    }

    @Test
    public void testNodeSortKey2() {
        final String[] unordered =
                {"Broager", "Åkirkeby", "Børkop", "Ærøskøbing", "Brædstrup", "Wandsbek"};
        final String[] ordered =
                {"'Broager'", "'Brædstrup'", "'Børkop'", "'Wandsbek'", "'Ærøskøbing'", "'Åkirkeby'"};
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
            String s = nv.toString();
            result.add(s);
        }
        assertArrayEquals(ordered, result.toArray(new String[0]));
    }

    @Test
    public void testNodeSortKey3() {
        final String[] unordered = new String[]
                {"Broager", "Åkirkeby", "Børkop", "Ærøskøbing", "Brædstrup", "Wandsbek"};
        final String[] ordered = new String[]
                {"'Ærøskøbing'", "'Åkirkeby'", "'Brædstrup'", "'Broager'", "'Børkop'", "'Wandsbek'"};
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
            String s = nv.toString();
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

    // TODO testSameValueDecimal tests
    // TODO sameValueAs mixed tests

    @Test
    public void testSameValue1() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeInteger(7);
        assertTrue("Same values (" + nv1 + "," + nv2 + ")", NodeValue.notSameAs(nv1, nv2));
        assertFalse("Same values (" + nv1 + "," + nv2 + ")", NodeValue.sameAs(nv1, nv2));

        NodeValue nv3 = NodeValue.makeInteger(5);
        assertTrue("Different values (" + nv1 + "," + nv3 + ")", NodeValue.sameAs(nv1, nv3));
        assertFalse("Different values - notNotSame (" + nv1 + "," + nv3 + ")", NodeValue.notSameAs(nv1, nv3));
    }

    @Test
    public void testSameValue2() {
        NodeValue nv1 = NodeValue.makeInteger(5);
        NodeValue nv2 = NodeValue.makeNodeInteger(7);
        assertTrue("Same values (" + nv1 + "," + nv2 + ")", NodeValue.notSameAs(nv1, nv2));
        assertFalse("Same values (" + nv1 + "," + nv2 + ")", NodeValue.sameAs(nv1, nv2));

        NodeValue nv3 = NodeValue.makeNodeInteger(5);
        assertTrue("Different values (" + nv1 + "," + nv3 + ")", NodeValue.sameAs(nv1, nv3));
        assertFalse("Different values - notNotSame (" + nv1 + "," + nv3 + ")", NodeValue.notSameAs(nv1, nv3));
    }

    @Test
    public void testSameValue3() {
        NodeValue nv1 = NodeValue.makeDecimal("1.5");
        NodeValue nv2 = NodeValue.makeDecimal("1.6");
        assertTrue("Same values (" + nv1 + "," + nv2 + ")", NodeValue.notSameAs(nv1, nv2));
        assertFalse("Same values (" + nv1 + "," + nv2 + ")", NodeValue.sameAs(nv1, nv2));

        NodeValue nv3 = NodeValue.makeDecimal("1.50");
        assertTrue("Different values (" + nv1 + "," + nv3 + ")", NodeValue.sameAs(nv1, nv3));
        assertFalse("Different values - notNotSame (" + nv1 + "," + nv3 + ")", NodeValue.notSameAs(nv1, nv3));
    }

    @Test
    public void testSameValue4() {
        NodeValue nv1 = NodeValue.makeDecimal("3");
        NodeValue nv2 = NodeValue.makeInteger(4);
        assertTrue("Same values (" + nv1 + "," + nv2 + ")", NodeValue.notSameAs(nv1, nv2));
        assertFalse("Same values (" + nv1 + "," + nv2 + ")", NodeValue.sameAs(nv1, nv2));

        NodeValue nv3 = NodeValue.makeInteger(3);
        assertTrue("Different values (" + nv1 + "," + nv3 + ")", NodeValue.sameAs(nv1, nv3));
        assertFalse("Different values - notNotSame (" + nv1 + "," + nv3 + ")", NodeValue.notSameAs(nv1, nv3));
    }

    @Test
    public void testSameValue5() {
        NodeValue nv1 = NodeValue.makeDecimal("-1.5"); // Must be exact for
                                                       // double and decimal
        NodeValue nv2 = NodeValue.makeDouble(1.5);
        assertTrue("Same values (" + nv1 + "," + nv2 + ")", NodeValue.notSameAs(nv1, nv2));
        assertFalse("Same values (" + nv1 + "," + nv2 + ")", NodeValue.sameAs(nv1, nv2));

        NodeValue nv3 = NodeValue.makeDouble(-1.5);
        assertTrue("Different values (" + nv1 + "," + nv3 + ")", NodeValue.sameAs(nv1, nv3));
        assertFalse("Different values - notNotSame (" + nv1 + "," + nv3 + ")", NodeValue.notSameAs(nv1, nv3));
    }

    @Test
    public void testSameValue6() {
        NodeValue nv1 = NodeValue.makeNodeInteger(17);
        NodeValue nv2 = NodeValue.makeDouble(34);
        assertTrue("Same values (" + nv1 + "," + nv2 + ")", NodeValue.notSameAs(nv1, nv2));
        assertFalse("Same values (" + nv1 + "," + nv2 + ")", NodeValue.sameAs(nv1, nv2));

        NodeValue nv3 = NodeValue.makeDouble(17);
        assertTrue("Different values (" + nv1 + "," + nv3 + ")", NodeValue.sameAs(nv1, nv3));
        assertFalse("Different values - notNotSame (" + nv1 + "," + nv3 + ")", NodeValue.notSameAs(nv1, nv3));
    }

    @Test
    public void testSameValue7() {
        NodeValue nv1 = NodeValue.makeBoolean(true);
        NodeValue nv2 = NodeValue.makeString("a");
        assertTrue("Same values (" + nv1 + "," + nv2 + ")", NodeValue.notSameAs(nv1, nv2));
        assertFalse("Same values (" + nv1 + "," + nv2 + ")", NodeValue.sameAs(nv1, nv2));

        NodeValue nv3 = NodeValue.makeNodeBoolean(true);
        assertTrue("Different values (" + nv1 + "," + nv3 + ")", NodeValue.sameAs(nv1, nv3));
        assertFalse("Different values - notNotSame (" + nv1 + "," + nv3 + ")", NodeValue.notSameAs(nv1, nv3));
    }

    @Test
    public void testLang1() {
        Node n1 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "en");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "en");
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertTrue(NodeValue.sameAs(nv1, nv2));
    }

    @Test
    public void testLang2() {
        Node n1 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "en");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "EN");
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertTrue(NodeValue.sameAs(nv1, nv2));
        assertFalse(nv1.equals(nv2));
    }

    @Test
    public void testLang3() {
        Node n1 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "en");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "en");
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertFalse(NodeValue.notSameAs(nv1, nv2));
    }

    @Test
    public void testLang4() {
        Node n1 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "en");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "EN");
        NodeValue nv2 = NodeValue.makeNode(n2);
        assertFalse(NodeValue.notSameAs(nv1, nv2));
        assertFalse(nv1.equals(nv2));
    }

    @Test
    public void testEquals1() {
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeInteger(1);
        assertEquals("Not NodeValue.equals()", nv1, nv2);
    }

    @Test
    public void testEquals2() {
        NodeValue nv1 = NodeValue.makeNodeInteger(1);
        NodeValue nv2 = NodeValue.makeInteger(1);
        assertEquals("Not NodeValue.equals()", nv1, nv2);
    }

    @Test
    public void testEquals3() { // Make different ways but equals
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeNodeInteger(1);
        assertEquals("Not NodeValue.equals()", nv1, nv2);
    }

    @Test
    public void testEquals4() {
        NodeValue nv1 = NodeValue.makeNode(org.apache.jena.graph.NodeFactory.createURI("http://example"));
        NodeValue nv2 = NodeValue.makeNode(org.apache.jena.graph.NodeFactory.createURI("http://example"));
        assertEquals("Not NodeValue.equals()", nv1, nv2);
    }

    @Test
    public void testNotEquals1() {
        NodeValue nv1 = NodeValue.makeInteger(1);
        NodeValue nv2 = NodeValue.makeInteger(2);
        assertFalse("NodeValue.equals()", nv1.equals(nv2));
    }

    @Test
    public void testNotEquals2() {
        NodeValue nv1 = NodeValue.makeNodeInteger(1);
        NodeValue nv2 = NodeValue.makeNodeString("1");
        assertFalse("NodeValue.equals()", nv1.equals(nv2));
    }

    @Test
    public void testNotEquals3() { // Literals and URIs are different.
        NodeValue nv1 = NodeValue.makeNode(org.apache.jena.graph.NodeFactory.createURI("http://example"));
        NodeValue nv2 = NodeValue.makeNode(org.apache.jena.graph.NodeFactory.createLiteral("http://example"));
        assertFalse("NodeValue.equals()", nv1.equals(nv2));
    }
}
