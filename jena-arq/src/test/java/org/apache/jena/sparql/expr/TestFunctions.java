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

package org.apache.jena.sparql.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.util.ExprUtils;

public class TestFunctions
{
    // Test of fn;* are in org.apache.jena.sparql.function.library.*
    // TestFunctions2 has the SPARQl keyword functions.

    private static final NodeValue TRUE     = NodeValue.TRUE;
    private static final NodeValue FALSE    = NodeValue.FALSE;

    @Test public void expr1() { test("1", NodeValue.makeInteger(1)); }

    @Test public void exprJavaSubstring1() { test("afn:substr('abc',0,0)", NodeValue.makeString("")); }
    @Test public void exprJavaSubstring2() { test("afn:substr('abc',0,1)", NodeValue.makeString("a")); }
    @Test public void exprJavaSubstring3() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,0)", NodeValue.makeString("")); }
    @Test public void exprJavaSubstring4() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,1)", NodeValue.makeString("a")); }
    // Test from JENA-785
    @Test public void exprJavaSubstring5() { test("afn:substr('𐐈𐑌𐐻𐐪𐑉𐐿𐐻𐐮𐐿𐐲', 0, 1)", NodeValue.makeString("𐐈")); }

    // SPRINTF
    @Test public void exprSprintf_01()      { test("afn:sprintf('%06d', 11)",NodeValue.makeString("000011")); }
    @Test public void exprSprintf_02()      { test("afn:sprintf('%s', 'abcdefghi')",NodeValue.makeString("abcdefghi")); }
    @Test public void exprSprintf_03()      { test("afn:sprintf('sometext %s', 'abcdefghi')",NodeValue.makeString("sometext abcdefghi")); }
    @Test public void exprSprintf_04()      { test("afn:sprintf('%1$tm %1$te,%1$tY', '2016-03-17'^^xsd:date)",NodeValue.makeString("03 17,2016")); }

    @Test public void exprSprintf_06()      { test("afn:sprintf('this is %s', 'false'^^xsd:boolean)",NodeValue.makeString("this is false")); }
    @Test public void exprSprintf_07()      { test("afn:sprintf('this number is equal to %.2f', '11.22'^^xsd:decimal)",NodeValue.makeString("this number is equal to "+String.format("%.2f",11.22))); }
    @Test public void exprSprintf_08()      { test("afn:sprintf('%.3f', '1.23456789'^^xsd:float)",NodeValue.makeString(String.format("%.3f",1.23456789))); }
    @Test public void exprSprintf_09()      { test("afn:sprintf('this number is equal to %o in the octal system', '11'^^xsd:integer)",NodeValue.makeString("this number is equal to 13 in the octal system")); }
    @Test public void exprSprintf_10()      { test("afn:sprintf('this number is equal to %.5f', '1.23456789'^^xsd:double)",NodeValue.makeString("this number is equal to "+String.format("%.5f",1.23456789))); }
    @Test public void exprSprintf_11()      { test("afn:sprintf('%.0f != %s', '12.23456789'^^xsd:double,'15')",NodeValue.makeString("12 != 15")); }
    @Test public void exprSprintf_12()      { test("afn:sprintf('(%.0f,%s,%d) %4$tm %4$te,%4$tY', '12.23456789'^^xsd:double,'12',11,'2016-03-17'^^xsd:date)",NodeValue.makeString("(12,12,11) 03 17,2016")); }

    // Timezone tests

    // Timezone -11:00 to any timezone can be a day ahead
    @Test public void exprSprintf_20() { test_exprSprintf_tz_exact("2005-10-14T14:09:43-11:00"); }
    // Timezone Z to any timezone can be a day behind or a day ahead
    @Test public void exprSprintf_21() { test_exprSprintf_tz_exact("2005-10-14T12:09:43+00:00"); }
    // Timezone +11:00 can be a day behind
    @Test public void exprSprintf_22() { test_exprSprintf_tz_exact("2005-10-14T10:09:43+11:00"); }

    private static void test_exprSprintf_tz_exact(String nodeStr) {
        // JVM default timezone.
        String exprStr = "afn:sprintf('%1$tm %1$te,%1$tY', "+NodeValue.makeDateTime(nodeStr).toString()+")";
        Expr expr = ExprUtils.parse(exprStr);
        NodeValue r = expr.eval(null, LibTestExpr.createTest());
        assertTrue(r.isString());
        String s = r.getString();
        // Parse the date
        String dtFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";
        SimpleDateFormat sdtFormat = new SimpleDateFormat(dtFormat);
        Date dtDate = null;
        try {
            dtDate = sdtFormat.parse(nodeStr);
        } catch (ParseException ex) {
            fail("Parse failure "+ex.getMessage(), ex);
        }
        // print the date based on the JVM timezone.
        SimpleDateFormat stdFormatOut = new SimpleDateFormat("MM dd,yyyy");
        stdFormatOut.setTimeZone(TimeZone.getDefault());
        String outDate = stdFormatOut.format(dtDate);
        assertEquals(s,outDate);
    }

    private static void test_exprSprintf_tz_possibilites(String nodeStr, String... possible) {
        String exprStr = "afn:sprintf('%1$tm %1$te,%1$tY', "+NodeValue.makeDateTime(nodeStr).toString()+")";
        Expr expr = ExprUtils.parse(exprStr);
        NodeValue r = expr.eval(null, LibTestExpr.createTest());
        assertTrue(r.isString());
        String s = r.getString();
        // Timezones! The locale data can be -1, 0, +1 from the Z day.
        boolean b = false;
        for (String poss : possible ) {
            if ( poss.equals(s) )
                b = true;
        }
        assertTrue(b);
    }

    // Timezone -11:00 to any timezone can be a day ahead
    @Test public void exprSprintf_23() { test_exprSprintf_tz_possibilites("2005-10-14T14:09:43-11:00",  "10 14,2005", "10 15,2005"); }
    // Timezone Z to any timezone can be a day behind or a day ahead
    @Test public void exprSprintf_24() { test_exprSprintf_tz_possibilites("2005-10-14T12:09:43Z",       "10 13,2005", "10 14,2005", "10 15,2005"); }
    // Timezone +11:00 can be a day behind
    @Test public void exprSprintf_25() { test_exprSprintf_tz_possibilites("2005-10-14T10:09:43+11:00",  "10 13,2005", "10 14,2005"); }

    // Better name!
    @Test public void localTimezone_2() { test("afn:timezone()", nv->nv.isDayTimeDuration()); }
    @Test public void localDateTime_1() { test("afn:nowtz()", nv-> nv.isDateTime()); }
    // Test field defined.
    @Test public void localDateTime_2() { test("afn:nowtz()", nv-> nv.getDateTime().getTimezone() >= -14 * 60 ); }

    @Test public void localDateTime_3() { test("afn:nowtz() = NOW()", NodeValue.TRUE); }

    private void test(String exprStr, NodeValue result) {
        Expr expr = ExprUtils.parse(exprStr);
        NodeValue r = expr.eval(null, LibTestExpr.createTest());
        assertEquals(result, r);
    }

    private void test(String exprStr, Predicate<NodeValue> test) {
        Expr expr = ExprUtils.parse(exprStr);
        NodeValue r = expr.eval(null, LibTestExpr.createTest());
        assertTrue(test.test(r), "Input="+exprStr);
    }

    private void testEqual(String exprStr, String exprStrExpected) {
        Expr expr = ExprUtils.parse(exprStrExpected);
        NodeValue rExpected = expr.eval(null, LibTestExpr.createTest());
        test(exprStr, rExpected);
    }

    private void testEvalException(String exprStr) {
        Expr expr = ExprUtils.parse(exprStr);
        try {
            NodeValue r = expr.eval(null, LibTestExpr.createTest());
            fail("No exception raised");
        }
        catch (ExprEvalException ex) {}
    }
}
