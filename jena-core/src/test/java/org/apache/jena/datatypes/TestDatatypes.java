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

package org.apache.jena.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDouble;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;

public class TestDatatypes {

    public XSDDatatype xsdDateTime = XSDDatatype.XSDdateTime;
    public XSDDatatype xsdDateTimeStamp = XSDDatatype.XSDdateTimeStamp;
    public XSDDatatype xsdDuration = XSDDatatype.XSDduration;
    public XSDDatatype xsdYearMonthDuration = XSDDatatype.XSDyearMonthDuration;
    public XSDDatatype xsdDayTimeDuration = XSDDatatype.XSDdayTimeDuration;
    public XSDDatatype xsdLanguage = XSDDatatype.XSDlanguage;

    @Test
    public void registration_01() {
        checkRegistration1("dateTime", XSD.dateTime);
    }

    @Test
    public void registration_02() {
        checkRegistration1("dateTimeStamp", XSD.dateTimeStamp);
    }

    @Test
    public void registration_03() {
        checkRegistration1("duration", XSD.duration);
    }

    @Test
    public void registration_04() {
        checkRegistration1("yearMonthDuration", XSD.yearMonthDuration);
    }

    @Test
    public void registration_05() {
        checkRegistration1("dayTimeDuration", XSD.dayTimeDuration);
    }

    // xsd:dateTimeStamp

    @Test
    public void dateTimeStamp_01() {
        valid(xsdDateTime, "2015-02-23T15:21:18Z");
        valid(xsdDateTimeStamp, "2015-02-23T15:21:18Z");
    }

    @Test
    public void dateTimeStamp_02() {
        valid(xsdDateTime, "2015-02-23T15:21:18");
        invalid(xsdDateTimeStamp, "2015-02-23T15:21:18");
    }

    @Test
    public void dateTimeStamp_03() {
        invalid(xsdDateTime, "2015-02-23Z");
        invalid(xsdDateTimeStamp, "2015-02-23Z");
    }

    @Test
    public void dateTimeStamp_04() {
        valid(xsdDateTime, "2015-02-23T15:21:18.665Z");
        valid(xsdDateTimeStamp, "2015-02-23T15:21:18.665Z");
    }

    @Test
    public void dateTimeStamp_05() {
        valid(xsdDateTime, "2015-02-23T15:21:18.665+00:00");
        valid(xsdDateTimeStamp, "2015-02-23T15:21:18.665+00:00");
    }

    @Test
    public void dateTimeStamp_06() {
        invalid(xsdDateTime, "2015-02-23T15:21:18.665+15:00");
        invalid(xsdDateTimeStamp, "2015-02-23T15:21:18.665+15:00");
    }

    // xsd:yearMonthDuration
    @Test
    public void yearMonthDuration_01() {
        valid(xsdDuration, "P1Y");
        valid(xsdYearMonthDuration, "P1Y");
    }

    @Test
    public void yearMonthDuration_02() {
        valid(xsdDuration, "-P1M");
        valid(xsdYearMonthDuration, "-P1M");
    }

    @Test
    public void yearMonthDuration_03() {
        valid(xsdYearMonthDuration, "P9Y10M");
        valid(xsdYearMonthDuration, "P9Y10M");
    }

    @Test
    public void yearMonthDuration_04() {
        valid(xsdDuration, "P1Y1D");
        invalid(xsdYearMonthDuration, "P1Y1D");
    }

    @Test
    public void yearMonthDuration_05() {
        valid(xsdDuration, "P1YT1M");
        invalid(xsdYearMonthDuration, "P1YT1M");
    }

    @Test
    public void yearMonthDuration_06() {
        valid(xsdDuration, "P1D");
        invalid(xsdYearMonthDuration, "P1D");
    }

    // xsd:dayTimeDuration
    @Test
    public void dayTimeDuration_01() {
        valid(xsdDuration, "PT0S");
        valid(xsdDayTimeDuration, "PT0S");
    }

    @Test
    public void dayTimeDuration_02() {
        invalid(xsdDuration, "PT");
        invalid(xsdDayTimeDuration, "PT");
    }

    @Test
    public void dayTimeDuration_03() {
        valid(xsdDuration, "P1D");
        valid(xsdDayTimeDuration, "P1D");
    }

    @Test
    public void dayTimeDuration_04() {
        valid(xsdDuration, "PT1M");
        valid(xsdDayTimeDuration, "PT1M");
    }

    @Test
    public void dayTimeDuration_05() {
        valid(xsdDuration, "PT1S");
        valid(xsdDayTimeDuration, "PT1S");
    }

    @Test
    public void dayTimeDuration_06() {
        valid(xsdDuration, "PT1M");
        invalid(xsdDayTimeDuration, "P1M");
    }

    @Test
    public void dayTimeDuration_07() {
        invalid(xsdDuration, "P1DT");
        invalid(xsdDayTimeDuration, "P1DT");
    }

    @Test
    public void language_01() {
        valid(xsdLanguage, "en");
        valid(xsdLanguage, "en-UK");
        valid(xsdLanguage, "es-419");
    }

    @Test
    public void language_02() {
        invalid(xsdLanguage, "-");
        invalid(xsdLanguage, "en-");
        invalid(xsdLanguage, "-en-UK");
        invalid(xsdLanguage, "-es-419-");
    }

    @Test
    public void language_03() {
        valid(xsdLanguage, "zh-Hans");
        valid(xsdLanguage, "zh-Hant-HK");
        valid(xsdLanguage, "de-CH-1901");
        valid(xsdLanguage, "de-DE-u-co-phonebk");
    }

    @Test
    public void language_04() {
        // non-ASCII characters are not allowed
        // (here: "goose" in Polish)
        invalid(xsdLanguage, "gęś");
    }

    @Test
    public void valueToLex_bigdecimal_01() {
        testValueToLex(new BigDecimal("0.004"), XSDDatatype.XSDdecimal);
    }

    @Test
    public void valueToLex_bigdecimal_02() {
        testValueToLex(new BigDecimal("1E21"), XSDDatatype.XSDdecimal);
    }

    @Test
    public void valueToLex_double_01() {
        testValueToLex(Double.valueOf("1E21"), XSDDatatype.XSDdouble);
    }

    @Test
    public void valueToLex_double_02() {
        testValueToLex(Double.POSITIVE_INFINITY, XSDDatatype.XSDdouble);
    }

    @Test
    public void valueToLex_double_03() {
        testValueToLex(Double.NEGATIVE_INFINITY, XSDDatatype.XSDdouble);
    }

    @Test
    public void valueToLex_float_01() {
        testValueToLex(Float.valueOf("1E21"), XSDDatatype.XSDfloat);
    }

    @Test
    public void valueToLex_float_02() {
        testValueToLex(Float.POSITIVE_INFINITY, XSDDatatype.XSDfloat);
    }

    @Test
    public void valueToLex_float_03() {
        testValueToLex(Float.NEGATIVE_INFINITY, XSDDatatype.XSDfloat);
    }

    @Test
    public void passAsString_UUID() {
        testLiteralIsCorrectType(UUID.randomUUID(), XSDDatatype.XSDstring);
    }

    @Test
    public void passAsString_Integer() {
        testLiteralIsCorrectType(5, XSDDatatype.XSDstring);
    }

    @Test
    public void passAsString_Float() {
        testLiteralIsCorrectType(9.99f, XSDDatatype.XSDstring);
    }

    @Test
    public void passAsInteger_String() {
        testLiteralIsCorrectType("5", XSDDatatype.XSDint);
    }

    @Test
    public void passAsFloat_String() {
        testLiteralIsCorrectType("5.55", XSDDatatype.XSDfloat);
    }

    @Test
    public void baseDataTypeEquality() {
        var rdfDataType1 = new BaseDatatype("urn:x-hp-dt:unknown");
        assertEquals(rdfDataType1, rdfDataType1);

        var rdfDataType2 = new BaseDatatype("urn:x-hp-dt:unknown");
        assertEquals(rdfDataType1, rdfDataType2);
        assertEquals(rdfDataType2, rdfDataType1);

        assertEquals(rdfDataType1.hashCode(), rdfDataType2.hashCode());
    }

    @Test
    public void baseDataTypeEmptyEquality() {
        var rdfDataType1 = new BaseDatatype("");
        assertEquals(rdfDataType1, rdfDataType1);

        var rdfDataType2 = new BaseDatatype("");
        assertEquals(rdfDataType1, rdfDataType2);
        assertEquals(rdfDataType2, rdfDataType1);

        assertEquals(rdfDataType1.hashCode(), rdfDataType2.hashCode());
    }

    @Test
    public void baseDataTypeNullEquality() {
        var rdfDataType1 = new BaseDatatype(null);
        assertEquals(rdfDataType1, rdfDataType1);

        var rdfDataType2 = new BaseDatatype(null);
        assertEquals(rdfDataType1, rdfDataType2);
        assertEquals(rdfDataType2, rdfDataType1);

        assertEquals(rdfDataType1.hashCode(), rdfDataType2.hashCode());
    }

    @Test
    public void xsdDoubleEquality() {
        var rdfDataType1 = new XSDDouble("double", Double.class);
        assertEquals(rdfDataType1, rdfDataType1);

        var rdfDataType2 = XSDDatatype.XSDdouble;
        assertEquals(rdfDataType1, rdfDataType2);
        assertEquals(rdfDataType2, rdfDataType1);

        assertEquals(rdfDataType1.hashCode(), rdfDataType2.hashCode());
    }

    @Test
    public void baseDataTypeNotEquals() {
        var rdfDataType1 = new BaseDatatype("urn:x-hp-dt:unknownA");
        var rdfDataType2 = new BaseDatatype("urn:x-hp-dt:unknownB");

        assertNotEquals(rdfDataType1, rdfDataType2);
        assertNotEquals(rdfDataType2, rdfDataType1);
    }

    @Test
    public void baseDataTypeNullNotEquals() {
        var rdfDataType1 = new BaseDatatype("urn:x-hp-dt:unknownA");
        var rdfDataType2 = new BaseDatatype(null);

        assertNotEquals(rdfDataType1, rdfDataType2);
        assertNotEquals(rdfDataType2, rdfDataType1);
    }

    @Test
    public void hashCodeEqualsUriHashCode() {
        var rdfDataType = new BaseDatatype("urn:x-hp-dt:unknown");
        assertEquals(rdfDataType.getURI().hashCode(), rdfDataType.hashCode());
    }

    private void testValueToLex(Object value, XSDDatatype datatype) {
        Node node = NodeFactory.createLiteralByValue(value, datatype);
        assertTrue("Not valid lexical form " + value + " -> " + node, datatype.isValid(node.getLiteralLexicalForm()));
    }

    private void testLiteralIsCorrectType(Object value, XSDDatatype datatype) {
        Node node = NodeFactory.createLiteralByValue(value, datatype);
        assertEquals("If passing object of type " + value.getClass().getSimpleName() + " as " + datatype.toString()
                     + " it needs to be treated as " + datatype.getJavaClass().getSimpleName(), node.getLiteralValue().getClass(),
                     datatype.getJavaClass());
    }

    private void valid(XSDDatatype xsddatatype, String string) {
        assertTrue("Expected valid: " + string, xsddatatype.isValid(string));
    }

    private void invalid(XSDDatatype xsddatatype, String string) {
        assertFalse("Expected invalid: " + string, xsddatatype.isValid(string));
    }

    private void checkRegistration1(String localName, Resource r) {
        XSDDatatype _xsd = (XSDDatatype)NodeFactory.getType(XSD.getURI() + localName);
        assertNotNull(_xsd);
        assertEquals(r.getURI(), _xsd.getURI());
    }

}
