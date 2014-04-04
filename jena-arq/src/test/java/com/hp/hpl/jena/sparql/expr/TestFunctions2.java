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

package com.hp.hpl.jena.sparql.expr;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

public class TestFunctions2 extends BaseTest
{
    // 3->2
    // Some overlap with TestFunctions except those are direct function calls and these are via SPARQL 1.1 syntax.
    // Better too many tests than too few.
    
    static boolean warnOnBadLexicalForms = true ; 
    
    @BeforeClass public static void beforeClass()
    {
        warnOnBadLexicalForms = NodeValue.VerboseWarnings ;
        NodeValue.VerboseWarnings = false ;    
    }
    @AfterClass  public static void afterClass()
    {
        NodeValue.VerboseWarnings = warnOnBadLexicalForms ;
    }
    
    
    // tests for strings. strlen, substr, strucase, strlcase, contains, concat
    /*
    | 'CONCAT' ExpressionList
    | SubstringExpression
    | 'STRLEN' '(' Expression ')'
    | 'UCASE' '(' Expression ')'
    | 'LCASE' '(' Expression ')'
    | 'ENCODE_FOR_URI' '(' Expression ')'
    | 'CONTAINS' '(' Expression ',' Expression ')'
    | 'STARTS' '(' Expression ',' Expression ')'
    | 'ENDS' '(' Expression ',' Expression ')'
    | 'YEAR' '(' Expression ')'
    | 'MONTH' '(' Expression ')'
    | 'DAY' '(' Expression ')'
    | 'HOURS' '(' Expression ')'
    | 'MINUTES' '(' Expression ')'
    | 'SECONDS' '(' Expression ')'
    | 'TIMEZONE' '(' Expression ')'
    | 'NOW' NIL
    | 'MD5' '(' Expression ')'
    | 'SHA1' '(' Expression ')'
    | SHA224 '(' Expression ')'
    | 'SHA256' '(' Expression ')'
    | 'SHA384' '(' Expression ')'
    | 'SHA512' '(' Expression ')'
    | 'COALESCE' ExpressionList
    | 'IF' '(' Expression ',' Expression ',' Expression ')'
    | 'STRLANG' '(' Expression ',' Expression ')'
    | 'STRDT' '(' Expression ',' Expression ')'
    */
    
    // Note in these tests, the rsult is written exactly as expected
    // Any same value would do - we test for the exact lexcial form
    // of the implementation.
    
    @Test public void round_01()    { test("round(123)",    "123") ; }
    @Test public void round_02()    { test("round(123.5)",  "'124'^^xsd:decimal") ; }
    @Test public void round_03()    { test("round(-0.5e0)", "0.0e0") ; }
    @Test public void round_04()    { test("round(-1.5)",   "'-1'^^xsd:decimal") ; }
    @Test public void round_05()    { test("round(-0)",     "-0") ; }
    
    @Test public void abs_01()    { test("abs(1)",      "1") ; }
    @Test public void abs_02()    { test("abs(1.0)",    "1.0") ; }
    @Test public void abs_03()    { test("abs(1.0e0)",  "1.0e0") ; }
    @Test public void abs_04()    { test("abs(-1)",     "1") ; }
    @Test public void abs_05()    { test("abs(+0)",     "0") ; }
    @Test public void abs_06()    { test("abs(-0)",     "0") ; }
    
    // CEIL
    @Test public void ceil_01()    { test("ceil(1)",        "1") ; }
    @Test public void ceil_02()    { test("ceil(1.0)",      "'1'^^xsd:decimal") ; }
    @Test public void ceil_03()    { test("ceil(1e0)",      "1.0e0") ; }
    @Test public void ceil_04()    { test("ceil(1.5e0)",    "2.0e0") ; }
    @Test public void ceil_05()    { test("ceil(-0.9)",     "'0'^^xsd:decimal") ; }
    @Test public void ceil_06()    { test("ceil(-9)",       "-9") ; }
    @Test public void ceil_07()    { test("ceil(-9.5)",     "'-9'^^xsd:decimal") ; }
    @Test public void ceil_08()    { test("ceil(0)",        "0") ; }

    // FLOOR
    @Test public void floor_01()    { test("floor(1)",      "1") ; }
    @Test public void floor_02()    { test("floor(1.0)",    "'1'^^xsd:decimal") ; }
    @Test public void floor_03()    { test("floor(1e0)",    "1.0e0") ; }
    @Test public void floor_04()    { test("floor(1.5e0)",  "1.0e0") ; }
    @Test public void floor_05()    { test("floor(-0.9)",   "'-1'^^xsd:decimal") ; }
    @Test public void floor_06()    { test("floor(-9)",     "-9") ; }
    @Test public void floor_07()    { test("floor(-9.5)",   "'-10'^^xsd:decimal") ; }
    @Test public void floor_08()    { test("floor(0)",      "0") ; }

    // simple, PLWL, xsd:string.
    
    // CONCAT
    @Test public void concat_01()   { test("concat('a')",       "'a'") ; }
    @Test public void concat_02()   { test("concat('a', 'b')",  "'ab'") ; }
    @Test public void concat_03()   { test("concat('a'@en, 'b')",  "'ab'") ; }
    @Test public void concat_04()   { test("concat('a'@en, 'b'@en)",  "'ab'@en") ; }
    //@Test public void concat_05()   { test("concat('a'^^xsd:string, 'b')",  "'ab'^^xsd:string") ; }
    @Test public void concat_05()   { test("concat('a'^^xsd:string, 'b')",  "'ab'") ; }
    @Test public void concat_06()   { test("concat('a'^^xsd:string, 'b'^^xsd:string)",  "'ab'^^xsd:string") ; }
    @Test public void concat_07()   { test("concat('a'^^xsd:string, 'b'^^xsd:string)",  "'ab'^^xsd:string") ; }
    //@Test public void concat_08()   { test("concat('a', 'b'^^xsd:string)",  "'ab'^^xsd:string") ; }
    @Test public void concat_08()   { test("concat('a', 'b'^^xsd:string)",  "'ab'") ; }
    @Test public void concat_09()   { test("concat('a'@en, 'b'^^xsd:string)",  "'ab'") ; }
    @Test public void concat_10()   { test("concat('a'^^xsd:string, 'b'@en)",  "'ab'") ; }
    @Test public void concat_11()   { test("concat()",  "''") ; }
    
    @Test(expected=ExprEvalException.class)
    public void concat_90()          { test("concat(1)",      "1") ; }
    
    @Test //(expected=ExprEvalException.class)
    public void concat_91()         { test("concat('a'@en, 'b'@fr)",  "'ab'") ; }
    
    // SUBSTR
    @Test public void substr_01()    { test("substr('abc',1)",      "'abc'") ; }
    @Test public void substr_02()    { test("substr('abc',2)",      "'bc'") ; }
    @Test public void substr_03()    { test("substr('abc',2,1)",    "'b'") ; }
    @Test public void substr_04()    { test("substr('abc',2,0)",    "''") ; }
    @Test public void substr_05()    { test("substr('12345',0,3)",  "'12'") ; }
    @Test public void substr_06()    { test("substr('12345',-1,3)", "'1'") ; }

    // These are the examples in F&O
    @Test public void substr_10()   { test("substr('motor car', 6)",      "' car'") ; }
    @Test public void substr_11()   { test("substr('metadata', 4, 3)",    "'ada'") ; }
    @Test public void substr_12()   { test("substr('12345', 1.5, 2.6)",   "'234'") ; }
    @Test public void substr_13()   { test("substr('12345', 0, 3)",       "'12'") ; }
    @Test public void substr_14()   { test("substr('12345', 5, -3)",      "''") ; }
    @Test public void substr_15()   { test("substr('12345', -3, 5)",      "'1'") ; }
    @Test public void substr_16()   { test("substr('12345', 0/0E0, 3)",   "''") ; }
    @Test public void substr_17()   { test("substr('12345', 1, 0/0E0)",   "''") ; }
    @Test public void substr_18()   { test("substr('', 1, 3)",            "''") ; }

    @Test(expected=ExprEvalException.class)
    public void substr_20()         { test("substr(1, 1, 3)",            "''") ; }
    @Test(expected=ExprEvalException.class)
    public void substr_21()         { test("substr('', 'one', 3)",            "''") ; }
    @Test(expected=ExprEvalException.class)
    public void substr_22()         { test("substr('', 1, 'three')",            "''") ; }

    // STRLEN
    @Test public void strlen_01()   { test("strlen('abc')",    "3") ; }
    @Test public void strlen_02()   { test("strlen('')",       "0") ; }

    // UCASE
    @Test public void ucase_01()    { test("ucase('abc')",  "'ABC'") ; }
    @Test public void ucase_02()    { test("ucase('ABC')",  "'ABC'") ; }
    @Test public void ucase_03()    { test("ucase('Ab 123 Cd')", "'AB 123 CD'") ; }
    @Test public void ucase_04()    { test("ucase('')",     "''") ; }
    
    // LCASE
    @Test public void lcase_01()    { test("lcase('abc')",  "'abc'") ; }
    @Test public void lcase_02()    { test("lcase('ABC')",  "'abc'") ; }
    @Test public void lcase_03()    { test("lcase('Ab 123 Cd')", "'ab 123 cd'") ; }
    @Test public void lcase_04()    { test("lcase('')",     "''") ; }
    
    // ENCODE_FOR_URI
    @Test public void encodeURI_01()    { test("encode_for_uri('a:b cd/~')",  "'a%3Ab%20cd%2F~'") ; }
    @Test public void encodeURI_02()    { test("encode_for_uri('\\n')",  "'%0A'") ; }
    @Test public void encodeURI_03()    { test("encode_for_uri('\\t')",  "'%09'") ; }
    
    @Test(expected=ExprEvalException.class)
    public void encodeURI_04()          { test("encode_for_uri(1234)",  "'1234'") ; }
    
    /* Compatibility rules
    # pairs of simple literals,
    # pairs of xsd:string typed literals
    # pairs of plain literals with identical language tags
    # pairs of an xsd:string typed literal (arg1 or arg2) and a simple literal (arg2 or arg1)
    # pairs of a plain literal with language tag (arg1) and a simple literal (arg2)
    # pairs of a plain literal with language tag (arg1) and an xsd:string typed literal (arg2)
    */
    
    // CONTAINS
    @Test public void contains_01()    { test("contains('abc', 'a')", "true") ; }
    @Test public void contains_02()    { test("contains('abc', 'b')", "true") ; }
    @Test public void contains_03()    { test("contains('ABC', 'a')", "false") ; }
    @Test public void contains_04()    { test("contains('abc', '')",  "true") ; }
    @Test public void contains_05()    { test("contains('', '')",     "true") ; }
    @Test public void contains_06()    { test("contains('', 'a')",    "false") ; }
    @Test public void contains_07()    { test("contains('12345', '34')",        "true") ; }
    @Test public void contains_08()    { test("contains('12345', '123456')",    "false") ; }
    
    @Test public void contains_10()    { test("contains('abc', 'a'^^xsd:string)",          "true") ; }
    @Test(expected=ExprEvalException.class)
    public void contains_11()    { test("contains('abc', 'a'@en)",          "true") ; }
    
    @Test public void contains_12()    { test("contains('abc'@en, 'a')",          "true") ; }
    @Test public void contains_13()    { test("contains('abc'@en, 'a'^^xsd:string)",          "true") ; }
    @Test public void contains_14()    { test("contains('abc'@en, 'a'@en)",       "true") ; }
    @Test(expected=ExprEvalException.class)
    public void contains_15()          { test("contains('abc'@en, 'a'@fr)",       "true") ; }

    @Test public void contains_16()    { test("contains('abc'^^xsd:string, 'a')", "true") ; }
    
    @Test(expected=ExprEvalException.class)
    public void contains_17()          { test("contains('abc'^^xsd:string, 'a'@en)", "true") ; }
    @Test public void contains_18()    { test("contains('abc'^^xsd:string, 'a'^^xsd:string)", "true") ; }
    
    @Test(expected=ExprEvalException.class)
    public void contains_20()    { test("contains(1816, 'a'^^xsd:string)", "true") ; }
    @Test(expected=ExprEvalException.class)
    public void contains_21()    { test("contains('abc', 1066)", "true") ; }
    
    @Test public void strstarts_01()    { test("strstarts('abc', 'a')", "true") ; }
    @Test public void strstarts_02()    { test("strstarts('abc', 'b')", "false") ; }
    @Test public void strstarts_03()    { test("strstarts('ABC', 'a')", "false") ; }
    @Test public void strstarts_04()    { test("strstarts('abc', '')",  "true") ; }
    @Test public void strstarts_05()    { test("strstarts('', '')",     "true") ; }
    @Test public void strstarts_06()    { test("strstarts('', 'a')",    "false") ; }
    
    @Test public void strstarts_10()    { test("strstarts('abc', 'a'^^xsd:string)",          "true") ; }
    @Test(expected=ExprEvalException.class)
    public void strstarts_11()    { test("strstarts('abc', 'a'@en)",          "true") ; }
    
    @Test public void strstarts_12()    { test("strstarts('abc'@en, 'a')",          "true") ; }
    @Test public void strstarts_13()    { test("strstarts('abc'@en, 'a'^^xsd:string)",          "true") ; }
    @Test public void strstarts_14()    { test("strstarts('abc'@en, 'a'@en)",       "true") ; }
    @Test(expected=ExprEvalException.class)
    public void strstarts_15()          { test("strstarts('abc'@en, 'a'@fr)",       "true") ; }

    @Test public void strstarts_16()    { test("strstarts('abc'^^xsd:string, 'a')", "true") ; }
    
    @Test(expected=ExprEvalException.class)
    public void strstarts_17()          { test("strstarts('abc'^^xsd:string, 'a'@en)", "true") ; }
    @Test public void strstarts_18()    { test("strstarts('abc'^^xsd:string, 'a'^^xsd:string)", "true") ; }
    
    @Test(expected=ExprEvalException.class)
    public void strstarts_20()    { test("strstarts(1816, 'a'^^xsd:string)", "true") ; }
    @Test(expected=ExprEvalException.class)
    public void strstarts_21()    { test("strstarts('abc', 1066)", "true") ; }
    
    // STRENDS
    @Test public void strends_01()      { test("strends('abc', 'c')", "true") ; }
    @Test public void strends_02()      { test("strends('abc', 'b')", "false") ; }
    @Test public void strends_03()      { test("strends('ABC', 'c')", "false") ; }
    @Test public void strends_04()      { test("strends('abc', '')",  "true") ; }
    @Test public void strends_05()      { test("strends('', '')",     "true") ; }
    @Test public void strends_06()      { test("strends('', 'a')",    "false") ; }
    
    @Test public void strends_10()      { test("strends('abc', 'c'^^xsd:string)",          "true") ; }
    @Test(expected=ExprEvalException.class)
    public void strends11()             { test("strends('abc', 'c'@en)",          "true") ; }
    
    @Test public void strends_12()      { test("strends('abc'@en, 'c')",          "true") ; }
    @Test public void strends_13()      { test("strends('abc'@en, 'c'^^xsd:string)",          "true") ; }
    @Test public void strends_14()      { test("strends('abc'@en, 'c'@en)",       "true") ; }
    @Test(expected=ExprEvalException.class)
    public void strends_15()            { test("strends('abc'@en, 'c'@fr)",       "true") ; }

    @Test public void strends_16()      { test("strends('abc'^^xsd:string, 'bc')", "true") ; }
    @Test(expected=ExprEvalException.class)
    public void strends_17()            { test("strends('abc'^^xsd:string, 'a'@en)", "true") ; }
    @Test public void strends_18()      { test("strends('abc'^^xsd:string, 'abc'^^xsd:string)", "true") ; }
    
    @Test(expected=ExprEvalException.class)
    public void strends_20()            { test("strends(1816, '6'^^xsd:string)", "true") ; }
    @Test(expected=ExprEvalException.class)
    public void strends_21()            { test("strends('abc', 1066)", "true") ; }
    
    // YEAR
    @Test public void year_01()         { test("year('2010-12-24T16:24:01.123'^^xsd:dateTime)", "2010") ; }
    @Test public void year_02()         { test("year('2010-12-24'^^xsd:date)", "2010") ; }
    @Test public void year_03()         { test("year('2010'^^xsd:gYear)", "2010") ; }
    @Test public void year_04()         { test("year('2010-12'^^xsd:gYearMonth)", "2010") ; }
    
    @Test(expected=ExprEvalException.class)
    public void year_05()               { test("year('--12'^^xsd:gMonth)", "2010") ; }
    @Test(expected=ExprEvalException.class)
    public void year_06()               { test("year('--12-24'^^xsd:gMonthDay)", "2010") ; }
    @Test(expected=ExprEvalException.class)
    public void year_07()               { test("year('---24'^^xsd:gDay)", "2010") ; }

    @Test public void year_11()         { test("year('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "2010") ; }
    @Test public void year_12()         { test("year('2010-12-24Z'^^xsd:date)", "2010") ; }
    @Test public void year_13()         { test("year('2010Z'^^xsd:gYear)", "2010") ; }
    @Test public void year_14()         { test("year('2010-12Z'^^xsd:gYearMonth)", "2010") ; }
    
    @Test(expected=ExprEvalException.class)
    public void year_15()               { test("year('--12Z'^^xsd:gMonth)", "2010") ; }
    @Test(expected=ExprEvalException.class)
    public void year_16()               { test("year('--12-24Z'^^xsd:gMonthDay)", "2010") ; }
    @Test(expected=ExprEvalException.class)
    public void year_17()               { test("year('---24Z'^^xsd:gDay)", "2010") ; }

    @Test public void year_21()         { test("year('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "2010") ; }
    @Test public void year_22()         { test("year('2010-12-24-08:00'^^xsd:date)", "2010") ; }
    @Test public void year_23()         { test("year('2010-08:00'^^xsd:gYear)", "2010") ; }
    @Test public void year_24()         { test("year('2010-12-08:00'^^xsd:gYearMonth)", "2010") ; }
    @Test(expected=ExprEvalException.class)
    public void year_25()               { test("year('--12-08:00'^^xsd:gMonth)", "2010") ; }
    @Test(expected=ExprEvalException.class)
    public void year_26()               { test("year('--12-24-08:00'^^xsd:gMonthDay)", "2010") ; }
    @Test(expected=ExprEvalException.class)
    public void year_27()               { test("year('---24-08:00'^^xsd:gDay)", "2010") ; }
    
    @Test public void year_dur_01()     { test("year('P1Y2M3DT4H5M6S'^^xsd:duration)", "1") ; }


    // MONTH
    @Test public void month_01()        { test("month('2010-12-24T16:24:01.123'^^xsd:dateTime)", "12") ; }
    @Test public void month_02()        { test("month('2010-12-24'^^xsd:date)", "12") ; }
    @Test(expected=ExprEvalException.class)
    public void month_03()              { test("month('2010'^^xsd:gYear)", "12") ; }
    @Test public void month_04()        { test("month('2010-12'^^xsd:gYearMonth)", "12") ; }
    
    @Test public void month_05()        { test("month('--12'^^xsd:gMonth)", "12") ; }
    @Test public void month_06()        { test("month('--12-24'^^xsd:gMonthDay)", "12") ; }
    @Test(expected=ExprEvalException.class)
    public void month_07()              { test("month('---24'^^xsd:gDay)", "12") ; }

    @Test public void month_11()        { test("month('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "12") ; }
    @Test public void month_12()        { test("month('2010-12-24Z'^^xsd:date)", "12") ; }
    @Test(expected=ExprEvalException.class)
    public void month_13()              { test("month('2010Z'^^xsd:gYear)", "12") ; }
    @Test public void month_14()        { test("month('2010-12Z'^^xsd:gYearMonth)", "12") ; }
    
    @Test public void month_15()        { test("month('--12Z'^^xsd:gMonth)", "12") ; }
    @Test public void month_16()        { test("month('--12-24Z'^^xsd:gMonthDay)", "12") ; }
    @Test(expected=ExprEvalException.class)
    public void month_17()              { test("month('---24Z'^^xsd:gDay)", "12") ; }

    @Test public void month_21()        { test("month('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "12") ; }
    @Test public void month_22()        { test("month('2010-12-24-08:00'^^xsd:date)", "12") ; }
    @Test(expected=ExprEvalException.class)
    public void month_23()              { test("month('2010-08:00'^^xsd:gYear)", "12") ; }
    @Test public void month_24()        { test("month('2010-12-08:00'^^xsd:gYearMonth)", "12") ; }
    @Test public void month_25()        { test("month('--12-08:00'^^xsd:gMonth)", "12") ; }
    public void month_26()              { test("month('--12-24-08:00'^^xsd:gMonthDay)", "12") ; }
    @Test(expected=ExprEvalException.class)
    public void month_27()              { test("month('---24-08:00'^^xsd:gDay)", "12") ; }

    @Test public void month_dur_01()    { test("month('P1Y2M3DT4H5M6S'^^xsd:duration)", "2") ; }

    // DAY
    @Test public void day_01()          { test("day('2010-12-24T16:24:01.123'^^xsd:dateTime)", "24") ; }
    @Test public void day_02()          { test("day('2010-12-24'^^xsd:date)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void day_03()                { test("day('2010'^^xsd:gYear)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void day_04()                { test("day('2010-12'^^xsd:gYearMonth)", "24") ; }
    
    @Test(expected=ExprEvalException.class) 
    public void day_05()                { test("day('--12'^^xsd:gMonth)", "24") ; }
    @Test public void day_06()          { test("day('--12-24'^^xsd:gMonthDay)", "24") ; }
    @Test public void day_07()          { test("day('---24'^^xsd:gDay)", "24") ; }

    @Test public void day_11()          { test("day('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "24") ; }
    @Test public void day_12()          { test("day('2010-12-24Z'^^xsd:date)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void day_13()                { test("day('2010Z'^^xsd:gYear)", "24") ; }
    @Test(expected=ExprEvalException.class) 
    public void day_14()                { test("day('2010-12Z'^^xsd:gYearMonth)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void day_15()                { test("day('--12Z'^^xsd:gMonth)", "24") ; }
    @Test public void day_16()          { test("day('--12-24Z'^^xsd:gMonthDay)", "24") ; }
    @Test public void day_17()          { test("day('---24Z'^^xsd:gDay)", "24") ; }

    @Test public void day_21()          { test("day('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "24") ; }
    @Test public void day_22()          { test("day('2010-12-24-08:00'^^xsd:date)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void day_23()                { test("day('2010-08:00'^^xsd:gYear)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void day_24()                { test("day('2010-12-08:00'^^xsd:gYearMonth)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void day_25()                { test("day('--12-08:00'^^xsd:gMonth)", "24") ; }
    @Test public void day_26()          { test("day('--12-24-08:00'^^xsd:gMonthDay)", "24") ; }
    @Test public void day_27()          { test("day('---24-08:00'^^xsd:gDay)", "24") ; }

    @Test public void day_dur_01()      { test("day('P1Y2M3DT4H5M6S'^^xsd:duration)", "3") ; }

    // HOURS
    
    @Test public void hours_01()        { test("hours('2010-12-24T16:24:01.123'^^xsd:dateTime)", "16") ; }
    @Test(expected=ExprEvalException.class)
    public void hours_02()              { test("hours('2010-12-24'^^xsd:date)", "16") ; }
    @Test public void hours_03()        { test("hours('16:24:01'^^xsd:time)", "16") ; }

    @Test public void hours_10()        { test("hours('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "16") ; }
    @Test public void hours_11()        { test("hours('16:24:24Z'^^xsd:time)", "16") ; }

    @Test public void hours_20()        { test("hours('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "16") ; }
    @Test public void hours_21()        { test("hours('16:24:24-08:00'^^xsd:time)", "16") ; }

    @Test public void hours_dur_01()     { test("hours('P1Y2M3DT4H5M6S'^^xsd:duration)", "4") ; }
    
    // MINUTES
    @Test public void minutes_01()        { test("minutes('2010-12-24T16:24:01.123'^^xsd:dateTime)", "24") ; }
    @Test(expected=ExprEvalException.class)
    public void minutes_02()              { test("minutes('2010-12-24'^^xsd:date)", "") ; }
    @Test public void minutes_03()        { test("minutes('16:24:01'^^xsd:time)", "24") ; }

    @Test public void minutes_10()        { test("minutes('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "24") ; }
    @Test public void minutes_11()        { test("minutes('16:24:01.1Z'^^xsd:time)", "24") ; }

    @Test public void minutes_20()        { test("minutes('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "24") ; }
    @Test public void minutes_21()        { test("minutes('16:24:01.01-08:00'^^xsd:time)", "24") ; }

    @Test public void minutes_dur_01()    { test("minutes('P1Y2M3DT4H5M6S'^^xsd:duration)", "5") ; }

    // SECONDS
    @Test public void seconds_01()        { test("seconds('2010-12-24T16:24:01.123'^^xsd:dateTime)", "01.123") ; }
    @Test(expected=ExprEvalException.class)
    public void seconds_02()              { test("seconds('2010-12-24'^^xsd:date)", "") ; }
    @Test public void seconds_03()        { test("seconds('16:24:01'^^xsd:time)", "'01'^^xsd:decimal") ; }

    @Test public void seconds_10()        { test("seconds('2010-12-24T16:24:31.123Z'^^xsd:dateTime)", "31.123") ; }
    @Test public void seconds_11()        { test("seconds('16:24:01.1Z'^^xsd:time)", "'01.1'^^xsd:decimal") ; }

    @Test public void seconds_20()        { test("seconds('2010-12-24T16:24:35.123-08:00'^^xsd:dateTime)", "35.123") ; }
    @Test public void seconds_21()        { test("seconds('16:24:01.01-08:00'^^xsd:time)", "'01.01'^^xsd:decimal") ; }
    
    @Test public void seconds_dur_01()    { test("seconds('P1Y2M3DT4H5M6S'^^xsd:duration)", "'6'^^xsd:decimal") ; }

    // TIMEZONE
    @Test public void timezone_01()       { test("timezone('2010-12-24T16:24:35.123Z'^^xsd:dateTime)", "'PT0S'^^xsd:dayTimeDuration") ; }
    @Test public void timezone_02()       { test("timezone('2010-12-24T16:24:35.123-08:00'^^xsd:dateTime)", "'-PT8H'^^xsd:dayTimeDuration") ; }
    @Test public void timezone_03()       { test("timezone('2010-12-24T16:24:35.123+01:00'^^xsd:dateTime)", "'PT1H'^^xsd:dayTimeDuration") ; }
    @Test public void timezone_04()       { test("timezone('2010-12-24T16:24:35.123-00:00'^^xsd:dateTime)", "'-PT0S'^^xsd:dayTimeDuration") ; }
    @Test public void timezone_05()       { test("timezone('2010-12-24T16:24:35.123+00:00'^^xsd:dateTime)", "'PT0S'^^xsd:dayTimeDuration") ; }
    
    @Test(expected=ExprEvalException.class)
    public void timezone_09()             { test("timezone('2010-12-24T16:24:35'^^xsd:dateTime)", "'PT0S'^^xsd:dayTimeDuration") ; }
    @Test(expected=ExprEvalException.class)
    public void timezone_10()             { test("timezone(2010)", "'PT0S'^^xsd:dayTimeDuration") ; }
    @Test(expected=ExprEvalException.class)
    public void timezone_11()             { test("timezone('2010-junk'^^xsd:gYear)", "'PT0S'^^xsd:dayTimeDuration") ; }
    
    // TZ
    @Test public void tz_01()             { test("tz('2010-12-24T16:24:35.123Z'^^xsd:dateTime)", "'Z'") ; }
    @Test public void tz_02()             { test("tz('2010-12-24T16:24:35.123-08:00'^^xsd:dateTime)", "'-08:00'") ; }
    @Test public void tz_03()             { test("tz('2010-12-24T16:24:35.123+01:00'^^xsd:dateTime)", "'+01:00'") ; }
    @Test public void tz_04()             { test("tz('2010-12-24T16:24:35.123-00:00'^^xsd:dateTime)", "'-00:00'") ; }
    @Test public void tz_05()             { test("tz('2010-12-24T16:24:35.123+00:00'^^xsd:dateTime)", "'+00:00'") ; }
    @Test public void tz_06()             { test("tz('2010-12-24T16:24:35.123'^^xsd:dateTime)", "''") ; }

    @Test(expected=ExprEvalException.class)
    public void tz_10()                   { test("tz(2010)", "''") ; }
    @Test(expected=ExprEvalException.class)
    public void tz_11()                   { test("tz('2010-junk'^^xsd:gYear)", "''") ; }

    // NOW
    //@Test public void now_01()        { test("now() > '2010-12-24T16:24:35.123-08:00'^^xsd:dateTime", "true") ; }
    
    
    // MD5
    @Test public void md5_01()      { test("md5('abcd')","'e2fc714c4727ee9395f324cd2e7f331f'") ; }
    @Test public void md5_02()            { test("md5('abcd'^^xsd:string)","'e2fc714c4727ee9395f324cd2e7f331f'") ; }
    @Test(expected=ExprEvalException.class)
    public void md5_03()            { test("md5('abcd'@en)","'e2fc714c4727ee9395f324cd2e7f331f'") ; }
    @Test(expected=ExprEvalException.class)
    public void md5_04()            { test("md5(1234)","'e2fc714c4727ee9395f324cd2e7f331f'") ; }
    
    // SHA1
    
    @Test public void sha1_01()      { test("sha1('abcd')","'81fe8bfe87576c3ecb22426f8e57847382917acf'") ; }
    @Test public void sha1_02()            { test("sha1('abcd'^^xsd:string)","'81fe8bfe87576c3ecb22426f8e57847382917acf'") ; }
    @Test(expected=ExprEvalException.class)
    public void sha1_03()            { test("sha1('abcd'@en)","'81fe8bfe87576c3ecb22426f8e57847382917acf'") ; }
    @Test(expected=ExprEvalException.class)
    public void sha1_04()            { test("sha1(123)","'81fe8bfe87576c3ecb22426f8e57847382917acf'") ; }

    // SHA224
//    @Test public void sha224_01()      { test("sha224('abcd')","'e2fc714c4727ee9395f324cd2e7f331f'") ; }
//    
//    @Test(expected=ExprEvalException.class)
//    public void sha224_02()            { test("sha224('abcd'^^xsd:string)","'e2fc714c4727ee9395f324cd2e7f331f'") ; }
//    
//    @Test(expected=ExprEvalException.class)
//    public void sha224_03()            { test("sha224('abcd'@en)","'e2fc714c4727ee9395f324cd2e7f331f'") ; }
//  
//  @Test(expected=ExprEvalException.class)
//  public void sha224_04()            { test("sha224(1234)","'e2fc714c4727ee9395f324cd2e7f331f'") ; }

    // SHA256

    @Test public void sha256_01()      { test("sha256('abcd')","'88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589'") ; }
    
    @Test public void sha256_02()      { test("sha256('abcd'^^xsd:string)","'88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589'") ; }
    
    @Test(expected=ExprEvalException.class)
    public void sha256_03()            { test("sha256('abcd'@en)","'88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589'") ; }
    
    @Test(expected=ExprEvalException.class)
    public void sha256_04()            { test("sha256(<uri>)","'88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589'") ; }
    
    // SHA384
    @Test public void sha384_01()      { test("sha384('abcd')","'1165b3406ff0b52a3d24721f785462ca2276c9f454a116c2b2ba20171a7905ea5a026682eb659c4d5f115c363aa3c79b'") ; }
    
    
    @Test public void sha384_02()      { test("sha384('abcd'^^xsd:string)","'1165b3406ff0b52a3d24721f785462ca2276c9f454a116c2b2ba20171a7905ea5a026682eb659c4d5f115c363aa3c79b'") ; }
    
    @Test(expected=ExprEvalException.class)
    public void sha384_03()            { test("sha384('abcd'@en)","'1165b3406ff0b52a3d24721f785462ca2276c9f454a116c2b2ba20171a7905ea5a026682eb659c4d5f115c363aa3c79b'") ; }

    @Test(expected=ExprEvalException.class)
    public void sha384_04()            { test("sha384(123.45)","'1165b3406ff0b52a3d24721f785462ca2276c9f454a116c2b2ba20171a7905ea5a026682eb659c4d5f115c363aa3c79b'") ; }

    // SHA512
    @Test public void sha512_01()      { test("sha512('abcd')","'d8022f2060ad6efd297ab73dcc5355c9b214054b0d1776a136a669d26a7d3b14f73aa0d0ebff19ee333368f0164b6419a96da49e3e481753e7e96b716bdccb6f'") ; }
    
    @Test public void sha512_02()      { test("sha512('abcd'^^xsd:string)","'d8022f2060ad6efd297ab73dcc5355c9b214054b0d1776a136a669d26a7d3b14f73aa0d0ebff19ee333368f0164b6419a96da49e3e481753e7e96b716bdccb6f'") ; }
    
    @Test(expected=ExprEvalException.class)
    public void sha512_03()            { test("md5('abcd'@en)","'d8022f2060ad6efd297ab73dcc5355c9b214054b0d1776a136a669d26a7d3b14f73aa0d0ebff19ee333368f0164b6419a96da49e3e481753e7e96b716bdccb6f'") ; }
    
    @Test(expected=ExprEvalException.class)
    public void sha512_04()            { test("md5(0.0e0)","'d8022f2060ad6efd297ab73dcc5355c9b214054b0d1776a136a669d26a7d3b14f73aa0d0ebff19ee333368f0164b6419a96da49e3e481753e7e96b716bdccb6f'") ; }

    // --------
    
    private static PrefixMapping pmap = ARQConstants.getGlobalPrefixMap() ;
    
    private static void test(String string, String result)
    {
        Expr expr = ExprUtils.parse(string, pmap) ;
        NodeValue nv = expr.eval(null, new FunctionEnvBase()) ;
        Node r = NodeFactoryExtra.parseNode(result) ;
        NodeValue nvr = NodeValue.makeNode(r) ;
        
        assertTrue("Not same value: Expected: "+nvr+" : Actual = "+nv, NodeValue.sameAs(nvr, nv)) ;  
        // test result must be lexical form exact. 
        assertEquals(r, nv.asNode()) ;
    }
    
    // ROUND to TestXSDFuncOps.
    
}
