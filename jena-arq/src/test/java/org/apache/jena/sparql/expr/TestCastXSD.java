/**
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

import org.apache.jena.graph.Node ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.ExprUtils ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestCastXSD {

    private static boolean origValue ;
    @BeforeClass public static void beforeClass() {
        origValue = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false ;
    }
    @AfterClass public static void afterClass() {
        NodeValue.VerboseWarnings = origValue ;
    }

    @Test public void cast_to_integer_01()  { testCast      ("xsd:integer('1e0'^^xsd:double)",      "'1'^^xsd:integer") ; }
    @Test public void cast_to_integer_02()  { testCast      ("xsd:byte('1e0'^^xsd:double)",         "'1'^^xsd:byte") ; }
    @Test public void cast_to_integer_03()  { testNoCast    ("xsd:byte('HaHa'^^xsd:double)") ; }
    @Test public void cast_to_integer_04()  { testCast      ("xsd:byte('-1'^^xsd:double)",          "'-1'^^xsd:byte") ; }
    @Test public void cast_to_integer_05()  { testNoCast    ("xsd:unsignedInt('-1'^^xsd:double)") ; }
    @Test public void cast_to_integer_06()  { testNoCast    ("xsd:byte('500'^^xsd:float)") ; }
    @Test public void cast_to_integer_07()  { testCast      ("xsd:integer('1e20'^^xsd:double)",     "100000000000000000000") ; }
    @Test public void cast_to_integer_08()  { testCast      ("xsd:integer('1e19'^^xsd:double)",     "10000000000000000000") ; }
    @Test public void cast_to_integer_09()  { testCast      ("xsd:integer('1e18'^^xsd:double)",     "1000000000000000000") ; }
    @Test public void cast_to_integer_10()  { testCast      ("xsd:integer('+1'^^xsd:integer)",      "'+1'^^xsd:integer") ; }
    @Test public void cast_to_integer_11()  { testCast      ("xsd:byte('+1'^^xsd:integer)",         "'+1'^^xsd:byte") ; }
    @Test public void cast_to_integer_12()  { testNoCast    ("xsd:byte('HaHa'^^xsd:integer)") ; }
    @Test public void cast_to_integer_13()  { testCast      ("xsd:byte('-1'^^xsd:integer)",         "'-1'^^xsd:byte") ; }
    @Test public void cast_to_integer_14()  { testNoCast    ("xsd:unsignedInt('-1'^^xsd:integer)") ; }
    @Test public void cast_to_integer_15()  { testNoCast    ("xsd:byte('500'^^xsd:integer)") ; }
    @Test public void cast_to_integer_16()  { testCast      ("xsd:decimal('1000000000000'^^xsd:integer)",  "'1000000000000'^^xsd:decimal") ; }
    @Test public void cast_to_integer_17()  { testCast      ("xsd:int('1000'^^xsd:integer)",        "'1000'^^xsd:int") ; }
    @Test public void cast_to_integer_18()  { testCast      ("xsd:integer('1000'^^xsd:int)",        "'1000'^^xsd:integer") ; }
    @Test public void cast_to_integer_19()  { testNoCast    ("xsd:negativeInteger('1000'^^xsd:int)") ; }
    @Test public void cast_to_integer_20()  { testCast      ("xsd:integer('+1'^^xsd:decimal)",      "'1'^^xsd:integer") ; }
    @Test public void cast_to_integer_21()  { testCast      ("xsd:integer('1.4'^^xsd:decimal)",     "'1'^^xsd:integer") ; }
    @Test public void cast_to_integer_22()  { testCast      ("xsd:byte('01.0'^^xsd:decimal)",       "'1'^^xsd:byte") ; }
    @Test public void cast_to_integer_23()  { testNoCast    ("xsd:byte('HaHa'^^xsd:decimal)") ; }
    @Test public void cast_to_integer_24()  { testCast      ("xsd:byte('-1'^^xsd:decimal)",         "'-1'^^xsd:byte") ; }
    @Test public void cast_to_integer_25()  { testNoCast    ("xsd:unsignedInt('-1'^^xsd:decimal)") ; }
    @Test public void cast_to_integer_26()  { testNoCast    ("xsd:byte('500'^^xsd:decimal)") ; }

    @Test public void cast_to_decimal_01()  { testCast      ("xsd:decimal('1e-20'^^xsd:double)",    "0.00000000000000000001") ; }
    @Test public void cast_to_decimal_02()  { testCast      ("xsd:decimal('1e-19'^^xsd:double)",    "0.0000000000000000001") ; }
    @Test public void cast_to_decimal_03()  { testCast      ("xsd:decimal('1e-18'^^xsd:double)",    "0.000000000000000001") ; }
    @Test public void cast_to_decimal_04()  { testCast      ("xsd:decimal('1e0'^^xsd:double)",      "1.0") ; }
    @Test public void cast_to_decimal_05()  { testCast      ("xsd:decimal('11e0'^^xsd:double)",     "11.0") ; }
    @Test public void cast_to_decimal_06()  { testCast      ("xsd:decimal('-0.01'^^xsd:double)",    "-0.01") ; }
    @Test public void cast_to_decimal_07()  { testCast      ("xsd:decimal('1'^^xsd:double)",        "'1.0'^^xsd:decimal") ; }
    
    @Test public void cast_to_boolean_01()  { testCast      ("xsd:boolean('1'^^xsd:double)",        "true") ; }
    @Test public void cast_to_boolean_02()  { testCast      ("xsd:boolean('+1.0e5'^^xsd:double)",   "true") ; }
    @Test public void cast_to_boolean_03()  { testCast      ("xsd:boolean('0'^^xsd:float)",         "false") ; }
    @Test public void cast_to_boolean_04()  { testCast      ("xsd:boolean(0.0e0)",                  "false") ; }
    @Test public void cast_to_boolean_05()  { testCast      ("xsd:boolean(-0.0e0)",                 "false") ; }
    @Test public void cast_to_boolean_06()  { testCast      ("xsd:boolean('NaN'^^xsd:float)",       "false") ; }
    
    @Test public void cast_to_boolean_07()  { testCast      ("xsd:boolean(1.0)",                    "true") ; }
    @Test public void cast_to_boolean_08()  { testCast      ("xsd:boolean(0.0)",                    "false") ; }
    @Test public void cast_to_boolean_09()  { testCast      ("xsd:boolean(-1.00)",                  "true") ; }
    @Test public void cast_to_boolean_10()  { testCast      ("xsd:boolean(0)",                      "false") ; }
    
    @Test public void cast_to_boolean_11()  { testCast      ("xsd:boolean('1')",                    "true") ; }
    @Test public void cast_to_boolean_12()  { testCast      ("xsd:boolean('true')",                 "true") ; }
    @Test public void cast_to_boolean_13()  { testCast      ("xsd:boolean('0')",                    "false") ; }
    @Test public void cast_to_boolean_14()  { testCast      ("xsd:boolean('false')",                "false") ; }

    @Test public void cast_from_string_01() { testCast      ("xsd:integer('+1'^^xsd:string)",           "'+1'^^xsd:integer") ; }
    @Test public void cast_from_string_02() { testNoCast    ("xsd:integer('a'^^xsd:string)") ; }
    @Test public void cast_from_string_03() { testCast      ("xsd:integer('11')",                       "'11'^^xsd:integer") ; }
    @Test public void cast_from_string_04() { testCast      ("xsd:double('12'^^xsd:string)",            "'12'^^xsd:double") ; }
    @Test public void cast_from_string_05() { testNoCast    ("xsd:double('abc'^^xsd:string)") ; }

    @Test public void cast_from_boolean_01() { testCast     ("xsd:boolean('true'^^xsd:boolean)",         "'true'^^xsd:boolean" ) ; }
    @Test public void cast_from_boolean_02() { testCast     ("xsd:boolean('1'^^xsd:boolean)",            "'1'^^xsd:boolean" ) ; }
    @Test public void cast_from_boolean_03() { testCast     ("xsd:integer('1'^^xsd:boolean)",            "1" ) ; }
    @Test public void cast_from_boolean_04() { testCast     ("xsd:decimal('false'^^xsd:boolean)",        "0.0" ) ; }
    @Test public void cast_from_boolean_05() { testCast     ("xsd:double('false'^^xsd:boolean)",         "0.0E0" ) ; }

    @Test public void cast_to_duration_01()  { testCast     ("xsd:duration('PT10S')",                            "'PT10S'^^xsd:duration") ; }
    @Test public void cast_to_duration_02()  { testCast     ("xsd:duration('P1DT10S'^^xsd:dayTimeDuration)",     "'P1DT10S'^^xsd:duration") ; }

    @Test public void cast_to_duration_03()  { testCast     ("xsd:dayTimeDuration('P1Y2M3DT1H2M3S'^^xsd:duration)",   "'P3DT1H2M3S'^^xsd:dayTimeDuration") ; }
    @Test public void cast_to_duration_04()  { testCast     ("xsd:dayTimeDuration('P1Y'^^xsd:duration)",          "'PT0S'^^xsd:dayTimeDuration") ; }
    @Test public void cast_to_duration_05()  { testCast     ("xsd:yearMonthDuration('P1Y2M3DT1H2M3S'^^xsd:duration)", "'P1Y2M'^^xsd:yearMonthDuration") ; }

    // This is what the XSD spec says, not "no cast"
    @Test public void cast_to_duration_06()  { testCast     ("xsd:dayTimeDuration('P1Y2M'^^xsd:yearMonthDuration)",   "'PT0S'^^xsd:dayTimeDuration") ; }
    @Test public void cast_to_duration_07()  { testCast     ("xsd:yearMonthDuration('P1DT10H'^^xsd:dayTimeDuration)", "'P0M'^^xsd:yearMonthDuration") ; }
    @Test public void cast_to_duration_08()  { testCast     ("xsd:yearMonthDuration('P1Y2M')",                      "'P1Y2M'^^xsd:yearMonthDuration") ; }
    @Test public void cast_to_duration_09()  { testCast     ("xsd:dayTimeDuration('P1DT10H')",                      "'P1DT10H'^^xsd:dayTimeDuration") ; }
    @Test public void cast_to_duration_10()  { testCast     ("xsd:duration('P1Y2M3DT1H2M3S')",                      "'P1Y2M3DT1H2M3S'^^xsd:duration") ; }

    @Test public void cast_to_temporal_01() { testCast      ("xsd:date('2015-10-12T15:00:24'^^xsd:dateTime)",         "'2015-10-12'^^xsd:date") ; }
    @Test public void cast_to_temporal_02() { testCast      ("xsd:date('2015-10-12T15:00:24+01:00'^^xsd:dateTime)",   "'2015-10-12+01:00'^^xsd:date") ; }
    @Test public void cast_to_temporal_03() { testCast      ("xsd:dateTime('2015-10-12'^^xsd:date)",                  "'2015-10-12T00:00:00'^^xsd:dateTime") ; }
    @Test public void cast_to_temporal_04() { testCast      ("xsd:dateTime('2015-10-12+01:00'^^xsd:date)",            "'2015-10-12T00:00:00+01:00'^^xsd:dateTime") ; }
    @Test public void cast_to_temporal_05() { testCast      ("xsd:time('2015-10-12T15:00:24'^^xsd:dateTime)",         "'15:00:24'^^xsd:time") ; }
    @Test public void cast_to_temporal_06() { testCast      ("xsd:dateTime('2015-10-12T15:00:24Z')",               "'2015-10-12T15:00:24Z'^^xsd:dateTime") ; }

    @Test public void cast_to_gregorian_01() { testCast     ("xsd:gYear('2015-10-12'^^xsd:date)",        "'2015'^^xsd:gYear") ; }
    @Test public void cast_to_gregorian_02() { testCast     ("xsd:gMonth('2015-10-12'^^xsd:date)",       "'--10'^^xsd:gMonth") ; }
    @Test public void cast_to_gregorian_03() { testCast     ("xsd:gMonthDay('2015-10-12'^^xsd:date)",    "'--10-12'^^xsd:gMonthDay") ; }
    @Test public void cast_to_gregorian_04() { testCast     ("xsd:gYearMonth('2015-10-12'^^xsd:date)",   "'2015-10'^^xsd:gYearMonth") ; }

    private void testNoCast(String input) {
        try {
            cast(input) ;
            Assert.fail("Expected ExprEvalException") ;
        } catch (ExprEvalException ex) {}
    }

    private void testCast(String input, String output) {
        NodeValue nv2 = cast(input) ;
        Node expected = SSE.parseNode(output) ;
        Assert.assertEquals(expected, nv2.asNode()) ;
    }

    private NodeValue cast(String input$) {
        Expr input = ExprUtils.parse(input$) ;
        ARQ.getContext().set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        FunctionEnv env = new ExecutionContext(ARQ.getContext(), null, null, null) ;
        return input.eval(null, env) ;
    }
}
