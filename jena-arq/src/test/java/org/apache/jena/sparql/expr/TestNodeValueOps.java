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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.nodevalue.NodeValueOps;
import org.apache.jena.vocabulary.RDF;

public class TestNodeValueOps {

    @Test public void testCheckAndGetStringLiteral1() {
        NodeValue nv = NodeValue.makeNode("abc", XSDDatatype.XSDstring);
        Node n = NodeValueOps.checkAndGetStringLiteral("Test", nv);
        assertEquals( "abc", n.getLiteralLexicalForm());
    }

    @Test public void testCheckAndGetStringLiteral2() {
        NodeValue nv = NodeValue.makeNode("abc", XSDDatatype.XSDnormalizedString);
        Node n = NodeValueOps.checkAndGetStringLiteral("Test", nv);
        assertEquals( "abc", n.getLiteralLexicalForm());
    }

    @Test public void testCheckAndGetStringLiteral3() {
        NodeValue nv = NodeValue.makeString("abc");
        Node n = NodeValueOps.checkAndGetStringLiteral("Test", nv);
        assertEquals( "abc", n.getLiteralLexicalForm());
    }

    @Test
    public void testCheckAndGetStringLiteral4() {
        // The form "abc"^^rdf:langString (no lang tag) is not derived from xsd:string.
        NodeValue nv = NodeValue.makeNode("abc", RDF.dtLangString);
        assertThrows(ExprEvalException.class, ()->NodeValueOps.checkAndGetStringLiteral("Test", nv));
    }

    // ** Addition
    // Numerics
    @Test
    public void nv_add_1() {
        testAdd("12", "13", "'25'^^xsd:integer");
    }

    @Test
    public void nv_add_2() {
        testAdd("'12'^^xsd:decimal", "13", "'25.0'^^xsd:decimal");
    }

    @Test
    public void nv_add_3() {
        testAdd("'12.0'^^xsd:decimal", "13", "'25.0'^^xsd:decimal");
    }

    @Test
    public void nv_add_4() {
        testAdd("12e0", "13", "25.0e0");
    }

    // Strings
    @Test
    public void nv_add_10() {
        testAdd("'12'", "'13'", "'1213'");
    }

    // Durations (need to test the wiring, not whether the calculation is right)
    @Test
    public void nv_add_20() {
        testAdd("'PT1H'^^xsd:duration", "'PT1H'^^xsd:duration", "'PT2H'^^xsd:duration");
    }

    @Test
    public void nv_add_21() {
        testAdd("'PT1H'^^xsd:dayTimeDuration", "'PT1H'^^xsd:dayTimeDuration", "'PT2H'^^xsd:dayTimeDuration");
    }

    // Outside the XSD spec.
    @Test
    public void nv_add_22() {
        testAdd("'P1Y'^^xsd:yearMonthDuration", "'PT4H'^^xsd:dayTimeDuration", "'P1YT4H'^^xsd:duration");
    }

    // Date/time + duration
    @Test
    public void nv_add_23() {
        testAdd("'2000-01-01'^^xsd:date", "'P1Y'^^xsd:duration", "'2001-01-01'^^xsd:date");
    }

    @Test
    public void nv_add_24() {
        testAdd("'2000-01-01T00:00:00Z'^^xsd:dateTime", "'P1Y1M'^^xsd:yearMonthDuration", "'2001-02-01T00:00:00Z'^^xsd:dateTime");
    }

    @Test
    public void nv_add_25() {
        testAdd("'2000-01-01T00:00:00Z'^^xsd:dateTime", "'P1Y1M1DT1H1M1.1S'^^xsd:duration", "'2001-02-02T01:01:01.1Z'^^xsd:dateTime");
    }

    @Test
    public void nv_add_26() {
        testAdd("'00:00:00'^^xsd:time", "'PT1H2M3.4S'^^xsd:duration", "'01:02:03.4'^^xsd:time");
    }

    // Bad mixes
    @Test
    public void nv_add_50() {
		assertThrows(ExprEvalException.class, ()-> testAdd("'12'", "13"));
    }

    @Test
    public void nv_add_51() {
        assertThrows(ExprEvalException.class, ()-> testAdd("'12'", "'PT1H'^^xsd:duration") );
    }

    @Test
    public void nv_add_52() {
        assertThrows(ExprEvalException.class, ()-> testAdd("'2012-04-05'^^xsd:date", "'2012-04-05'^^xsd:date"));
    }

    // ** Subtraction
    // Numerics
    @Test
    public void nv_sub_1() {
        testSub("12", "13", "-1");
    }

    @Test
    public void nv_sub_2() {
        testSub("12", "13.0", "-1.0");
    }

    @Test
    public void nv_sub_3() {
        testSub("12e0", "13.0", "-1.0e0");
    }

    // Durations
    @Test
    public void nv_sub_20() {
        testSub("'PT2H'^^xsd:duration", "'PT1H'^^xsd:duration", "'PT1H'^^xsd:duration");
    }

    @Test
    public void nv_sub_21() {
        testSub("'PT2H'^^xsd:dayTimeDuration", "'PT1H'^^xsd:dayTimeDuration", "'PT1H'^^xsd:dayTimeDuration");
    }

    @Test
    public void nv_sub_22() {
        testSub("'P2Y'^^xsd:yearMonthDuration", "'P1Y'^^xsd:yearMonthDuration", "'P1Y'^^xsd:yearMonthDuration");
    }

    @Test
    public void nv_sub_23() {
        testSub("'P3D'^^xsd:dayTimeDuration", "'PT4H'^^xsd:dayTimeDuration", "'P2DT20H'^^xsd:dayTimeDuration");
    }

    // Date/time - duration
    @Test
    public void nv_sub_30() {
        testSub("'2000-01-01'^^xsd:date", "'P1Y'^^xsd:duration", "'1999-01-01'^^xsd:date");
    }

    @Test
    public void nv_sub_31() {
        testSub("'2000-01-01T00:00:00Z'^^xsd:dateTime", "'P1Y1M'^^xsd:yearMonthDuration", "'1998-12-01T00:00:00Z'^^xsd:dateTime");
    }

    @Test
    public void nv_sub_32() {
        testSub("'2000-01-01T00:00:00Z'^^xsd:dateTime", "'P1Y1M1DT1H1M1.1S'^^xsd:duration", "'1998-11-29T22:58:58.9Z'^^xsd:dateTime");
    }

    @Test
    public void nv_sub_33() {
        testSub("'10:11:12'^^xsd:time", "'PT1H2M3.4S'^^xsd:duration", "'09:09:08.6'^^xsd:time");
    }
    // Date/time - date/time

    @Test
    public void nv_sub_50() {
		assertThrows(ExprEvalException.class, ()-> testSub("'12'", "'13'") );
    }

    // ** Multiplication

    @Test
    public void nv_mult_1() {
        testMult("12", "13", "156");
    }

    @Test
    public void nv_mult_2() {
        testMult("-12", "13.0", "-156.0");
    }

    @Test
    public void nv_mult_3() {
        testMult("'PT1H2M'^^xsd:duration", "2", "'PT2H4M'^^xsd:duration");
    }

    // ** Division
    @Test
    public void nv_div_num_1() {
        testDiv("12", "2", "6.0");
    }

    @Test
    public void nv_div_num_2() {
        testDiv("12", "2e0", "6.0e0");
    }

    @Test
    public void nv_div_dur_3() {
        testDiv("'PT24H20M'^^xsd:duration", "2", "'PT12H10M'^^xsd:duration");
    }

    @Test
    public void nv_div_dur_4() {
        testDiv("'PT24H20M'^^xsd:dayTimeDuration", "2", "'PT12H10M'^^xsd:duration");
    }

    // Note - not normalized.
    @Test
    public void nv_div_dur_5() {
        testDiv("'PT24H20M'^^xsd:dayTimeDuration", "0.1", "'PT240H200M'^^xsd:duration");
    }

    @Test
    public void nv_div_dur_6() {
        testDiv("'P2Y8M'^^xsd:duration", "2", "'P1Y4M'^^xsd:duration");
    }

    @Test
    public void nv_div_dur_7() {
        testDiv("'P2Y8M'^^xsd:yearMonthDuration", "2", "'P1Y4M'^^xsd:duration");
    }

    // Note - not normalized.
    @Test
    public void nv_div_dur_8() {
        testDiv("'P2Y8M'^^xsd:yearMonthDuration", "0.1", "'P20Y80M'^^xsd:duration");
    }

    @Test
    public void nv_div_dur_bad_1() {
        // Year-month divided by day-time
        assertThrows(ExprEvalException.class, ()->
        testDiv("'P2Y8M'^^xsd:yearMonthDuration", "'PT12H10M'^^xsd:duration"));
    }

    @Test
    public void nv_div_dur_bad_2() {
     // day-time divided by year-month
        assertThrows(ExprEvalException.class, ()->
        testDiv("'PT12H10M'^^xsd:duration", "'P2Y8M'^^xsd:duration"));
    }

    @Test
    public void nv_div_dur_bad_3() {
     // day-time divided by year-month
        assertThrows(ExprEvalException.class, ()-> testDiv("'PT12H10M'^^xsd:duration", "0"));
    }

    @Test
    public void nv_div_dur_bad_4() {
     // day-time divided by year-month
        assertThrows(ExprEvalException.class, ()->testDiv("'P1Y'^^xsd:duration", "0"));
    }

    @Test
    public void nv_div_dur_bad_5() {
        // day-time divided by zero interval
        assertThrows(ExprEvalException.class, ()-> testDiv("'PT1H'^^xsd:duration", "'PT0S'^^xsd:duration"));
    }

    @Test
    public void nv_div_dur_bad_6() {
        // year-month divided by zero interval
        assertThrows(ExprEvalException.class, ()-> testDiv("'P1Y'^^xsd:duration", "'P0Y'^^xsd:duration"));
    }

    // == Workers

    static void testAdd(String s1, String s2, String s3) {
        NodeValue nv3 = NodeValue.parse(s3);
        NodeValue nv = testAdd(s1, s2);
        assertEquals(nv3, nv);
    }

    static NodeValue testAdd(String s1, String s2) {
        NodeValue nv1 = NodeValue.parse(s1);
        NodeValue nv2 = NodeValue.parse(s2);
        return NodeValueOps.additionNV(nv1, nv2);
    }

    static void testSub(String s1, String s2, String s3) {
        NodeValue nv3 = NodeValue.parse(s3);
        NodeValue nv = testSub(s1, s2);
        assertEquals(nv3, nv);
    }

    static NodeValue testSub(String s1, String s2) {
        NodeValue nv1 = NodeValue.parse(s1);
        NodeValue nv2 = NodeValue.parse(s2);
        return NodeValueOps.subtractionNV(nv1, nv2);
    }

    static void testMult(String s1, String s2, String s3) {
        NodeValue nv3 = NodeValue.parse(s3);
        NodeValue nv = testMult(s1, s2);
        assertEquals(nv3, nv);
    }

    static NodeValue testMult(String s1, String s2) {
        NodeValue nv1 = NodeValue.parse(s1);
        NodeValue nv2 = NodeValue.parse(s2);
        return NodeValueOps.multiplicationNV(nv1, nv2);
    }

    static void testDiv(String s1, String s2, String s3) {
        NodeValue nv3 = NodeValue.parse(s3);
        NodeValue nv = testDiv(s1, s2);
        assertEquals(nv3, nv);
    }

    static NodeValue testDiv(String s1, String s2) {
        NodeValue nv1 = NodeValue.parse(s1);
        NodeValue nv2 = NodeValue.parse(s2);
        return NodeValueOps.divisionNV(nv1, nv2);
    }
}
