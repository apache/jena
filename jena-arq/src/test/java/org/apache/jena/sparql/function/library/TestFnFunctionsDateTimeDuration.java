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

package org.apache.jena.sparql.function.library;
import static org.apache.jena.sparql.expr.LibTestExpr.test;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.TimeZone;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.LibTestExpr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sys.JenaSystem ;
import org.junit.Test ;

public class TestFnFunctionsDateTimeDuration {
    static { JenaSystem.init(); }

    @Test
    public void datetime_1() {
        test("fn:dateTime('2017-09-14'^^xsd:date, '10:11:23'^^xsd:time) = '2017-09-14T10:11:23'^^xsd:dateTime");
    }

    @Test
    public void datetime_2() {
        test("fn:dateTime('2017-09-14+01:00'^^xsd:date, '10:11:23'^^xsd:time) = '2017-09-14T10:11:23+01:00'^^xsd:dateTime");
    }

    @Test
    public void datetime_3() {
        test("fn:dateTime('2017-09-14'^^xsd:date, '10:11:23+01:00'^^xsd:time) = '2017-09-14T10:11:23+01:00'^^xsd:dateTime");
    }

    @Test
    public void datetime_4() {
        test("fn:dateTime('2017-09-14+01:00'^^xsd:date, '10:11:23+01:00'^^xsd:time) = '2017-09-14T10:11:23+01:00'^^xsd:dateTime");
    }

    @Test(expected=ExprEvalException.class)
    public void datetime_5() {
        // Incompatible timezones.
        test("fn:dateTime('2017-09-14+09:00'^^xsd:date, '10:11:23+01:00'^^xsd:time) = '2017-09-14T10:11:23+01:00'^^xsd:dateTime");
    }

    @Test(expected=ExprEvalException.class)
    public void datetime_6() {
        // Bad date
        test("fn:dateTime('xyz', '10:11:23+01:00'^^xsd:time) = '2017-09-14T10:11:23+01:00'^^xsd:dateTime");
    }

    @Test(expected=ExprEvalException.class)
    public void datetime_7() {
        // Bad time
        test("fn:dateTime('2017-09-14+09:00', 'now'^^xsd:time) = '2017-09-14T10:11:23+01:00'^^xsd:dateTime");
    }

    @Test public void fromDateTime() {
        test("fn:years-from-dateTime('2017-09-14T17:01:02'^^xsd:dateTime)",   "2017");
        test("fn:months-from-dateTime('2017-09-14T17:01:02'^^xsd:dateTime)",  "09");
        test("fn:days-from-dateTime('2017-09-14T17:01:02'^^xsd:dateTime)",    "14");
        test("fn:hours-from-dateTime('2017-09-14T17:01:02'^^xsd:dateTime)",   "17");
        test("fn:minutes-from-dateTime('2017-09-14T17:01:02'^^xsd:dateTime)", "01");
        test("fn:seconds-from-dateTime('2017-09-14T17:01:02.5'^^xsd:dateTime)", "02.5");
        test("fn:timezone-from-dateTime('2017-09-14T17:01:02+01:00'^^xsd:dateTime)", "'PT1H'^^xsd:dayTimeDuration");
    }

    @Test public void fromDate() {
        test("fn:years-from-date('2017-09-14'^^xsd:date)",   "2017");
        test("fn:months-from-date('2017-09-14'^^xsd:date)",  "09");
        test("fn:days-from-date('2017-09-14'^^xsd:date)",    "14");
        test("fn:timezone-from-date('2017-09-14+01:00'^^xsd:date)", "'PT1H'^^xsd:dayTimeDuration");
    }

    @Test public void fromTime() {
        test("fn:hours-from-time('17:01:02'^^xsd:time)",   "17");
        test("fn:minutes-from-time('17:01:02'^^xsd:time)", "01");
        test("fn:seconds-from-time('17:01:02.5'^^xsd:time)", "02.5");
        test("fn:timezone-from-time('17:01:02+01:00'^^xsd:time)", "'PT1H'^^xsd:dayTimeDuration");
    }

    @Test public void fromDuration() {
        String s = "'P1Y2M3DT4H5M6.7S'^^xsd:duration";
        test("fn:years-from-duration("+s+")",    "1");
        test("fn:months-from-duration("+s+")",   "2");
        test("fn:days-from-duration("+s+")",     "3");
        test("fn:hours-from-duration("+s+")",    "4");
        test("fn:minutes-from-duration("+s+")",  "5");
        test("fn:seconds-from-duration("+s+")",  "6.7");
    }

    @Test public void fromMisc() {
        test("fn:hours-from-time('17:01:02'^^xsd:time)",   "17");
        test("fn:minutes-from-time('17:01:02'^^xsd:time)", "01");
        test("fn:seconds-from-time('17:01:02.5'^^xsd:time)", "02.5");
        test("fn:timezone-from-time('17:01:02+01:00'^^xsd:time)", "'PT1H'^^xsd:dayTimeDuration");
    }

    @Test public void from_strict() {
        // ARQ is lax about exact datatypes types unless in strict mode.
        // If the datatytpe has "days" (xsd:date, xsd;dateTime, xsd:duration and derived types)
        // the normal operating mode for "days-from-*" is to return the days
        // regardless of the specific datatype.
        ARQ.getContext().set(ARQ.strictSPARQL, true);
        try {
            testException("fn:years-from-dateTime('2017-09-14'^^xsd:date)");
            testException("fn:months-from-date('P1Y2M3DT4H5M6.7S'^^xsd:duration)");
            testException("fn:days-from-time('2017-09-14T17:01:02'^^xsd:dateTime)");
        } finally {
            ARQ.getContext().set(ARQ.strictSPARQL, false);
        }
    }

    private static void testException(String exprStr) {

        Expr expr = ExprUtils.parse(exprStr) ;
        try {
            NodeValue rExpected = expr.eval(null, LibTestExpr.createTest()) ;
            fail("Expected exception: "+exprStr);
        } catch ( ExprEvalException ex) {}
    }

    @Test public void exprAdjustDatetimeToTz_01(){
        test(
            "fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00'^^xsd:dateTime)",
            "fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00'^^xsd:dateTime,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustDatetimeToTz_02(){
        test(
            "fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00-07:00'^^xsd:dateTime)",
            "fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00-07:00'^^xsd:dateTime,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustDatetimeToTz_03() { test("fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00'^^xsd:dateTime,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeDateTime("2002-03-07T10:00:00-10:00"));}

    @Test public void exprAdjustDatetimeToTz_04() { test("fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00-07:00'^^xsd:dateTime,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeDateTime("2002-03-07T07:00:00-10:00"));}

    @Test public void exprAdjustDatetimeToTz_05() { test("fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00-07:00'^^xsd:dateTime,'PT10H'^^xsd:dayTimeDuration)",NodeValue.makeDateTime("2002-03-08T03:00:00+10:00"));}

    @Test public void exprAdjustDatetimeToTz_06() { test("fn:adjust-dateTime-to-timezone('2002-03-07T00:00:00+01:00'^^xsd:dateTime,'-PT8H'^^xsd:dayTimeDuration)",NodeValue.makeDateTime("2002-03-06T15:00:00-08:00"));}

    @Test public void exprAdjustDatetimeToTz_07() { test("fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00'^^xsd:dateTime,'')",NodeValue.makeDateTime("2002-03-07T10:00:00"));}

    @Test public void exprAdjustDatetimeToTz_08() { test("fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00-07:00'^^xsd:dateTime,'')",NodeValue.makeDateTime("2002-03-07T10:00:00"));}

    @Test public void exprAdjustDateToTz_01(){
        test(
            "fn:adjust-date-to-timezone('2002-03-07'^^xsd:date)",
            "fn:adjust-date-to-timezone('2002-03-07'^^xsd:date,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustDateToTz_02(){
        test(
            "fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date)",
            "fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustDateToTz_03() { test("fn:adjust-date-to-timezone('2002-03-07'^^xsd:date,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeDate("2002-03-07-10:00"));}

    @Test public void exprAdjustDateToTz_04() { test("fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeDate("2002-03-06-10:00"));}

    @Test public void exprAdjustDateToTz_05() { test("fn:adjust-date-to-timezone('2002-03-07'^^xsd:date,'')",NodeValue.makeDate("2002-03-07"));}

    @Test public void exprAdjustDateToTz_06() { test("fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date,'')",NodeValue.makeDate("2002-03-07"));}

    @Test public void exprAdjustTimeToTz_01(){
        test(
            "fn:adjust-time-to-timezone('10:00:00'^^xsd:time)",
            "fn:adjust-time-to-timezone('10:00:00'^^xsd:time,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustTimeToTz_02(){
        test(
            "fn:adjust-time-to-timezone('10:00:00-07:00'^^xsd:time)",
            "fn:adjust-time-to-timezone('10:00:00-07:00'^^xsd:time,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustTimeToTz_03() { test("fn:adjust-time-to-timezone('10:00:00'^^xsd:time,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeNode("10:00:00-10:00",XSDDatatype.XSDtime));}

    @Test public void exprAdjustTimeToTz_04() { test("fn:adjust-time-to-timezone('10:00:00-07:00'^^xsd:time,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeNode("07:00:00-10:00",XSDDatatype.XSDtime));}

    @Test public void exprAdjustTimeToTz_05() { test("fn:adjust-time-to-timezone('10:00:00'^^xsd:time,'')",NodeValue.makeNode("10:00:00",XSDDatatype.XSDtime));}

    @Test public void exprAdjustTimeToTz_06() { test("fn:adjust-time-to-timezone('10:00:00-07:00'^^xsd:time,'')",NodeValue.makeNode("10:00:00",XSDDatatype.XSDtime));}

    @Test public void exprAdjustTimeToTz_07() { test("fn:adjust-time-to-timezone('10:00:00-07:00'^^xsd:time,'PT10H'^^xsd:dayTimeDuration)",NodeValue.makeNode("03:00:00+10:00",XSDDatatype.XSDtime));}
    //@Test public void exprStrJoin()      { test("fn:string-join('a', 'b')", NodeValue.makeString("ab")) ; }

    @Test public void localTimezone_1() { test("fn:implicit-timezone()", nv->nv.isDayTimeDuration()); }

    private String getDynamicDurationString(){
        int tzOffset = TimeZone.getDefault().getOffset(new Date().getTime()) / (1000*60);
        String off = "PT"+Math.abs(tzOffset)+"M";
        if(tzOffset < 0)
            off = "-"+off;
        return off;
    }



}
