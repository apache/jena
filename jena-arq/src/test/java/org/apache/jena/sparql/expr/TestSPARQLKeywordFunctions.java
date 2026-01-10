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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sys.JenaSystem;

public class TestSPARQLKeywordFunctions
{
    static { JenaSystem.init(); }
    // Some overlap with TestFunctions except those are direct function calls and these are via SPARQL 1.1 syntax.
    // Better too many tests than too few.

    static boolean warnOnBadLexicalForms = true;

    @BeforeAll
    public static void beforeClass() {
        warnOnBadLexicalForms = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false;
    }

    @AfterAll
    public static void afterClass() {
        NodeValue.VerboseWarnings = warnOnBadLexicalForms;
    }

    private final static String kwTRUE = "true";
    private final static String kwFALSE = "false";
    private final static String kwEmptyString = "''";;

    // tests for strings. strlen, substr, strucase, strlcase, contains, concat
    // Some overlap with NodeFunctions.
    // Tests for IRI(..) are in TestUpdateOperations.insert_with_iri_function_resolution*
    // so we can observe the parser effect.

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
    | 'SHA224' '(' Expression ')'
    | 'SHA256' '(' Expression ')'
    | 'SHA384' '(' Expression ')'
    | 'SHA512' '(' Expression ')'
    | 'COALESCE' ExpressionList
    | 'IF' '(' Expression ',' Expression ',' Expression ')'
    | 'STRLANG' '(' Expression ',' Expression ')'
    | 'STRDT' '(' Expression ',' Expression ')'
    */

    // Note in these tests, the result is written exactly as expected
    // Any same value would do - we test for the exact lexical form
    // of the implementation.

    // ROUND
    @Test public void round_01()  { test("round(123)",    "123"); }
    @Test public void round_02()  { test("round(123.5)",  "'124.0'^^xsd:decimal"); }
    @Test public void round_03()  { test("round(-0.5e0)", "-0.0e0"); }
    @Test public void round_04()  { test("round(-1.5)",   "'-1.0'^^xsd:decimal"); }
    @Test public void round_05()  { test("round(-0)",     "-0"); }

    @Test public void round_10()  { test("round('NaN'^^xsd:double)",    "'NaN'^^xsd:double"); }
    @Test public void round_11()  { test("round('NaN'^^xsd:float)",     "'NaN'^^xsd:float"); }
    @Test public void round_12()  { test("round('-0'^^xsd:double)",     "'-0.0e0'^^xsd:double"); }
    @Test public void round_13()  { test("round('-0'^^xsd:float)",      "'-0.0'^^xsd:float"); }
    @Test public void round_14()  { test("round('-0'^^xsd:double)",     "'-0.0e0'^^xsd:double"); }

    // ABS
    @Test public void abs_01()    { test("abs(1)",        "1"); }
    @Test public void abs_02()    { test("abs(1.0)",      "1.0"); }
    @Test public void abs_03()    { test("abs(1.0e0)",    "1.0e0"); }
    @Test public void abs_04()    { test("abs(-1)",       "1"); }
    @Test public void abs_05()    { test("abs(+0)",       "0"); }
    @Test public void abs_06()    { test("abs(-0)",       "0"); }

    // CEIL
    @Test public void ceil_01()   { test("ceil(1)",       "1"); }
    @Test public void ceil_02()   { test("ceil(1.0)",     "'1.0'^^xsd:decimal"); }
    @Test public void ceil_03()   { test("ceil(1e0)",     "1.0e0"); }
    @Test public void ceil_04()   { test("ceil(1.5e0)",   "2.0e0"); }
    @Test public void ceil_05()   { test("ceil(-0.9)",    "'0.0'^^xsd:decimal"); }
    @Test public void ceil_06()   { test("ceil(-9)",      "-9"); }
    @Test public void ceil_07()   { test("ceil(-9.5)",    "'-9.0'^^xsd:decimal"); }
    @Test public void ceil_08()   { test("ceil(0)",       "0"); }

    // FLOOR
    @Test public void floor_01()  { test("floor(1)",      "1"); }
    @Test public void floor_02()  { test("floor(1.0)",    "'1.0'^^xsd:decimal"); }
    @Test public void floor_03()  { test("floor(1e0)",    "1.0e0"); }
    @Test public void floor_04()  { test("floor(1.5e0)",  "1.0e0"); }
    @Test public void floor_05()  { test("floor(-0.9)",   "'-1.0'^^xsd:decimal"); }
    @Test public void floor_06()  { test("floor(-9)",     "-9"); }
    @Test public void floor_07()  { test("floor(-9.5)",   "'-10.0'^^xsd:decimal"); }
    @Test public void floor_08()  { test("floor(0)",      "0"); }

    // CONCAT
    @Test public void concat_00()    { test("concat()",       kwEmptyString); }
    @Test public void concat_01()    { test("concat('a')",       "'a'"); }
    @Test public void concat_02()    { test("concat('a', 'b')",  "'ab'"); }
    @Test public void concat_03()    { test("concat('a'@en, 'b')",  "'ab'"); }
    @Test public void concat_04()    { test("concat('a'@en, 'b'@en)",  "'ab'@en"); }
    @Test public void concat_05()    { test("concat('a'@en, 'b'@fr)",  "'ab'"); }
    @Test public void concat_06()    { test("concat('a'^^xsd:string, 'b')",  "'ab'"); }
    @Test public void concat_07()    { test("concat('a'^^xsd:string, 'b'^^xsd:string)",  "'ab'^^xsd:string"); }
    @Test public void concat_08()    { test("concat('a'^^xsd:string, 'b'^^xsd:string)",  "'ab'^^xsd:string"); }
    @Test public void concat_09()    { test("concat('a', 'b'^^xsd:string)",  "'ab'"); }
    @Test public void concat_10()    { test("concat('a'@en, 'b'^^xsd:string)",  "'ab'"); }
    @Test public void concat_11()    { test("concat('a'^^xsd:string, 'b'@en)",  "'ab'"); }
    @Test public void concat_12()    { test("concat()",  kwEmptyString); }

    @Test public void concat_20()    { test("concat('a', 'b', 'c')",  "'abc'"); }
    @Test public void concat_21()    { test("concat('a'@en, 'b'@en, 'c')",  "'abc'"); }
    @Test public void concat_22()    { test("concat('a', 'b'@en, 'c')",  "'abc'"); }
    @Test public void concat_23()    { test("concat('a', 'b', 'c'@en)",  "'abc'"); }

    @Test public void concat_30()    { test("concat('a'@en--ltr, 'b'@en--ltr)",  "'ab'@en--ltr"); }
    @Test public void concat_31()    { test("concat('a'@en--ltr, 'b'@en--rtl)",  "'ab'"); }
    @Test public void concat_32()    { test("concat('a'@en--ltr, 'b'@fr--ltr)",  "'ab'"); }
    @Test public void concat_33()    { test("concat('a'@en--ltr, 'b'@fr--ltr)",  "'ab'"); }
    @Test public void concat_34()    { test("concat('a'@en--ltr, 'b')",  "'ab'"); }
    @Test public void concat_35()    { test("concat('a'@en--ltr, 'b'@en)",  "'ab'"); }
    @Test public void concat_36()    { test("concat('a', 'b'@en--ltr)",  "'ab'"); }
    @Test public void concat_37()    { test("concat('a'@en, 'b'@en--ltr)",  "'ab'"); }

    // SUBSTR
    @Test public void substr_01()    { test("substr('abc',1)",      "'abc'"); }
    @Test public void substr_02()    { test("substr('abc',2)",      "'bc'"); }
    @Test public void substr_03()    { test("substr('abc',2,1)",    "'b'"); }
    @Test public void substr_04()    { test("substr('abc',2,0)",    kwEmptyString); }
    @Test public void substr_05()    { test("substr('12345',0,3)",  "'12'"); }
    @Test public void substr_06()    { test("substr('12345',-1,3)", "'1'"); }

    // These are the examples in F&O
    @Test public void substr_10()    { test("substr('motor car', 6)",      "' car'"); }
    @Test public void substr_11()    { test("substr('metadata', 4, 3)",    "'ada'"); }
    @Test public void substr_12()    { test("substr('12345', 1.5, 2.6)",   "'234'"); }
    @Test public void substr_13()    { test("substr('12345', 0, 3)",       "'12'"); }
    @Test public void substr_14()    { test("substr('12345', 5, -3)",      kwEmptyString); }
    @Test public void substr_15()    { test("substr('12345', -3, 5)",      "'1'"); }
    @Test public void substr_16()    { test("substr('12345', 0/0E0, 3)",   kwEmptyString); }
    @Test public void substr_17()    { test("substr('12345', 1, 0/0E0)",   kwEmptyString); }
    @Test public void substr_18()    { test("substr('', 1, 3)",            kwEmptyString); }

    @Test public void substr_20()    { testEvalException("substr(1, 1, 3)"); }
    @Test public void substr_21()    { testEvalException("substr('', 'one', 3)"); }
    @Test public void substr_22()    { testEvalException("substr('', 1, 'three')"); }

    // Codepoint outside UTF-16.
    // These are  U+0001F46A 👪 - FAMILY
    // As surrogate pair: 0xD83D 0xDC6A
    // Written here in forms which protect against binary file corruption.

    //@Test public void substr_30()   { test("substr('👪', 1)",           "'👪'"); }

    // Written using \-u escapes in SPARQL.
    @Test public void substr_30()   { test("substr('\\uD83D\\uDC6A', 1)",           "'\\uD83D\\uDC6A'"); }
    // Same using Java string escapes.
    @Test public void substr_30b()  { test("substr('\uD83D\uDC6A', 1)",             "'\uD83D\uDC6A'"); }
    @Test public void substr_31()   { test("substr('\\uD83D\\uDC6A', 2)",                       kwEmptyString); }

    @Test public void substr_32()   { test("substr('ABC\\uD83D\\uDC6ADEF', 4, 1)",  "'\\uD83D\\uDC6A'"); }
    @Test public void substr_33()   { test("substr('\\uD83D\\uDC6A!', -1, 3)",      "'\\uD83D\\uDC6A'"); }
    @Test public void substr_34()   { test("substr('\\uD83D\\uDC6A!', -1, 4)",      "'\\uD83D\\uDC6A!'"); }

    // STRLEN
    @Test public void strlen_01()   { test("strlen('abc')",    "3"); }
    @Test public void strlen_02()   { test("strlen('')",       "0"); }

    // UCASE
    @Test public void ucase_01()    { test("ucase('abc')",  "'ABC'"); }
    @Test public void ucase_02()    { test("ucase('ABC')",  "'ABC'"); }
    @Test public void ucase_03()    { test("ucase('Ab 123 Cd')", "'AB 123 CD'"); }
    @Test public void ucase_04()    { test("ucase('')",     kwEmptyString); }

    // LCASE
    @Test public void lcase_01()    { test("lcase('abc')",  "'abc'"); }
    @Test public void lcase_02()    { test("lcase('ABC')",  "'abc'"); }
    @Test public void lcase_03()    { test("lcase('Ab 123 Cd')", "'ab 123 cd'"); }
    @Test public void lcase_04()    { test("lcase('')",     kwEmptyString); }

    // ENCODE_FOR_URI
    @Test public void encodeURI_01()    { test("encode_for_uri('a:b cd/~')",  "'a%3Ab%20cd%2F~'"); }
    @Test public void encodeURI_02()    { test("encode_for_uri('\\n')",  "'%0A'"); }
    @Test public void encodeURI_03()    { test("encode_for_uri('\\t')",  "'%09'"); }
    @Test public void encodeURI_04()    { test("encode_for_uri('abc')",     "'abc'"); }
    @Test public void encodeURI_05()    { test("encode_for_uri('abc'@en)",  "'abc'"); }

    @Test
    public void encodeURI_09()          { testEvalException("encode_for_uri(1234)"); }

    /* Compatibility rules */

    // CONTAINS
    @Test public void contains_01()     { test("contains('abc', 'a')", kwTRUE); }
    @Test public void contains_02()     { test("contains('abc', 'b')", kwTRUE); }
    @Test public void contains_03()     { test("contains('ABC', 'a')", kwFALSE); }
    @Test public void contains_04()     { test("contains('abc', '')",  kwTRUE); }
    @Test public void contains_05()     { test("contains('', '')",     kwTRUE); }
    @Test public void contains_06()     { test("contains('', 'a')",    kwFALSE); }
    @Test public void contains_07()     { test("contains('12345', '34')",        kwTRUE); }
    @Test public void contains_08()     { test("contains('12345', '123456')",    kwFALSE); }

    @Test public void contains_10() { test("contains('abc', 'abcd')", kwFALSE); }
    @Test public void contains_11() { test("contains('abc'@en, 'bc')", kwTRUE); }
    @Test public void contains_12() { test("contains('abc'^^xsd:string, 'c')", kwTRUE); }
    @Test public void contains_13() { test("contains('abc'^^xsd:string, 'c'^^xsd:string)", kwTRUE); }
    @Test public void contains_14() { test("contains('abc', 'z'^^xsd:string)", kwFALSE); }
    @Test public void contains_15() { test("contains('abc'@en, 'abc'@en)", kwTRUE); }

    @Test public void contains_16() { testEvalException("contains('ab'@en, 'ab'@fr)"); }
    @Test public void contains_17() { testEvalException("contains(123, 'ab'@fr)"); }

    @Test public void contains_20()     { test("contains('abc', 'a'^^xsd:string)", kwTRUE); }
    @Test public void contains_21()     { testEvalException("contains('abc', 'a'@en)"); }

    @Test public void contains_22()     { test("contains('abc'@en, 'a')",          kwTRUE); }
    @Test public void contains_23()     { test("contains('abc'@en, 'a'^^xsd:string)",          kwTRUE); }
    @Test public void contains_24()     { test("contains('abc'@en, 'a'@en)",       kwTRUE); }
    @Test public void contains_25()     { testEvalException("contains('abc'@en, 'a'@fr)"); }

    @Test public void contains_26()     { test("contains('abc'^^xsd:string, 'a')", kwTRUE); }

    @Test public void contains_27()     { testEvalException("contains('abc'^^xsd:string, 'a'@en)"); }
    @Test public void contains_28()     { test("contains('abc'^^xsd:string, 'a'^^xsd:string)", kwTRUE); }

    @Test public void contains_30()     { testEvalException("contains(1816, 'a'^^xsd:string)"); }
    @Test public void contains_31()     { testEvalException("contains('abc', 1066)"); }

    @Test public void contains_40()     { test("contains('abc'@en--ltr, 'a')", kwTRUE); }
    @Test public void contains_41()     { testEvalException("contains('abc'@en--ltr, 'a'@en)"); }
    @Test public void contains_42()     { testEvalException("contains('abc'@en, 'a'@en--ltr)"); }
    @Test public void contains_43()     { testEvalException("contains('abc', 'a'@en--ltr)"); }

    // STRSTARTS
    @Test public void strstarts_01()    { test("strstarts('abc', 'a')", kwTRUE); }
    @Test public void strstarts_02()    { test("strstarts('abc', 'b')", kwFALSE); }
    @Test public void strstarts_03()    { test("strstarts('ABC', 'a')", kwFALSE); }
    @Test public void strstarts_04()    { test("strstarts('abc', '')",  kwTRUE); }
    @Test public void strstarts_05()    { test("strstarts('', '')",     kwTRUE); }
    @Test public void strstarts_06()    { test("strstarts('', 'a')",    kwFALSE); }
    @Test public void strstarts_07()    { test("STRSTARTS('abc', 'abcd')", kwFALSE); }

    @Test public void strstarts_10()    { test("strstarts('abc', 'a'^^xsd:string)",          kwTRUE); }
    @Test public void strstarts_11()    { testEvalException("strstarts('abc', 'a'@en)"); }
    @Test public void strstarts_12()    { test("strstarts('abc'@en, 'a')",          kwTRUE); }
    @Test public void strstarts_13()    { test("strstarts('abc'@en, 'a'^^xsd:string)",          kwTRUE); }
    @Test public void strstarts_14()    { test("strstarts('abc'@en, 'a'@en)",       kwTRUE); }
    @Test public void strstarts_15()    { testEvalException("strstarts('abc'@en, 'a'@fr)"); }

    @Test public void strstarts_16()    { test("strstarts('abc'^^xsd:string, 'a')", kwTRUE); }
    @Test public void strstarts_17()    { testEvalException("strstarts('abc'^^xsd:string, 'a'@en)"); }
    @Test public void strstarts_18()    { test("strstarts('abc'^^xsd:string, 'a'^^xsd:string)", kwTRUE); }

    @Test public void strstarts_20()    { testEvalException("strstarts(1816, 'a'^^xsd:string)"); }
    @Test public void strstarts_21()    { testEvalException("strstarts('abc', 1066)"); }

    @Test public void strstarts_22()    { testEvalException("STRSTARTS('ab'@en, 'ab'@fr)"); }
    @Test public void strstarts_23()    { testEvalException("STRSTARTS(123, 'ab'@fr)"); }
    @Test public void strstarts_24()    { testEvalException("STRSTARTS('123'^^xsd:string, 12.3)"); }

    @Test public void strstarts_30()    { test("STRSTARTS('ab'@en--ltr, 'z')", kwFALSE); }
    @Test public void strstarts_31()    { test("STRSTARTS('ab'@en--ltr, 'z'@en--ltr)", kwFALSE); }
    @Test public void strstarts_32()    { testEvalException("STRSTARTS('ab'@en, 'z'@en--rtl)"); }

    // STRENDS
    @Test public void strends_01()      { test("strends('abc', 'c')", kwTRUE); }
    @Test public void strends_02()      { test("strends('abc', 'b')", kwFALSE); }
    @Test public void strends_03()      { test("strends('ABC', 'c')", kwFALSE); }
    @Test public void strends_04()      { test("strends('abc', '')",  kwTRUE); }
    @Test public void strends_05()      { test("strends('', '')",     kwTRUE); }
    @Test public void strends_06()      { test("strends('', 'a')",    kwFALSE); }

    @Test public void strends_10()      { test("STRENDS('abc', 'abcd')", kwFALSE); }
    @Test public void strends_11()      { test("STRENDS('abc'@en, 'bc')", kwTRUE); }
    @Test public void strends_12()      { test("STRENDS('abc'^^xsd:string, 'c')", kwTRUE); }
    @Test public void strends_13()      { test("STRENDS('abc'^^xsd:string, 'c'^^xsd:string)", kwTRUE); }
    @Test public void strends_14()      { test("STRENDS('abc', 'ab'^^xsd:string)", kwFALSE); }
    @Test public void strends_15()      { test("STRENDS('abc'@en, 'abc'@en)", kwTRUE); }

    @Test public void strends_16()      { testEvalException("STRENDS('ab'@en, 'ab'@fr)"); }
    @Test public void strends_17()      { testEvalException("STRENDS(123, 'ab'@fr)"); }
    @Test public void strends_18()      { testEvalException("STRENDS('123'^^xsd:string, 12.3)"); }

    @Test public void strends_20()      { test("strends('abc', 'c'^^xsd:string)",          kwTRUE); }
    @Test public void strends_21()      { testEvalException("strends('abc', 'c'@en)"); }

    @Test public void strends_22()      { testEvalException("STRSTARTS('ab'@en, 'ab'@fr)"); }
    @Test public void strends_23()      { testEvalException("STRSTARTS(123, 'ab'@fr)"); }
    @Test public void strends_24()      { testEvalException("STRSTARTS('123'^^xsd:string, 12.3)"); }

    @Test public void strends_30()      { test("STRENDS('ab'@en--ltr, 'z')", kwFALSE); }
    @Test public void strends_31()      { test("STRENDS('ab'@en--ltr, 'z'@en--ltr)", kwFALSE); }
    @Test public void strends_32()      { testEvalException("STRENDS('ab'@en, 'z'@en--rtl)"); }

    // STRBEFORE
    @Test public void strbefore_01()    { test("STRBEFORE('abc', 'abcd')", kwEmptyString); }
    @Test public void strbefore_02()    { test("STRBEFORE('abc'@en, 'b')", "'a'@en"); }
    @Test public void strbefore_03()    { test("STRBEFORE('abc'^^xsd:string, 'c')", "'ab'^^xsd:string"); }
    @Test public void strbefore_04()    { test("STRBEFORE('abc'^^xsd:string, ''^^xsd:string)", "''^^xsd:string"); }
    @Test public void strbefore_05()    { test("STRBEFORE('abc', 'ab'^^xsd:string)", kwEmptyString); }
    @Test public void strbefore_06()    { test("STRBEFORE('abc'@en, 'b'@en)", "'a'@en"); }

    @Test public void strbefore_07()    { testEvalException("STRBEFORE('ab'@en, 'ab'@fr)"); }
    @Test public void strbefore_08()    { testEvalException("STRBEFORE(123, 'ab'@fr)"); }
    @Test public void strbefore_09()    { testEvalException("STRBEFORE('123'^^xsd:string, 12.3)"); }
    // No match case
    @Test public void strbefore_10()    { test("STRBEFORE('abc'^^xsd:string, 'z')", kwEmptyString); }
    // Empty string case
    @Test public void strbefore_11()    { test("STRBEFORE('abc'^^xsd:string, '')", kwEmptyString); }
    @Test public void strbefore_12()    { testEvalException("STRBEFORE('abc', ''@en)"); }

    @Test public void strbefore_30()    { test("STRBEFORE('abc'@en--ltr, 'a')", "''@en--ltr"); }
    @Test public void strbefore_31()    { test("STRBEFORE('abc'@en--ltr, 'a'@en--ltr)", "''@en--ltr"); }
    @Test public void strbefore_32()    { testEvalException("STRBEFORE('abc'@en--ltr, 'a'@en)"); }

    // STRAFTER
    @Test public void strafter_01()     { test("STRAFTER('abc', 'abcd')", kwEmptyString); }
    @Test public void strafter_02()     { test("STRAFTER('abc'@en, 'b')", "'c'@en"); }
    @Test public void strafter_03()     { test("STRAFTER('abc'^^xsd:string, 'a')", "'bc'^^xsd:string"); }
    @Test public void strafter_04()     { test("STRAFTER('abc'^^xsd:string, ''^^xsd:string)", "'abc'"); }
    @Test public void strafter_05()     { test("STRAFTER('abc', 'bc'^^xsd:string)", kwEmptyString); }
    @Test public void strafter_06()     { test("STRAFTER('abc'@en, 'b'@en)", "'c'@en"); }

    @Test public void strafter_07()     { testEvalException("STRAFTER('ab'@en, 'ab'@fr)"); }
    @Test public void strafter_08()     { testEvalException("STRAFTER(123, 'ab'@fr)"); }
    @Test public void strafter_09()     { testEvalException("STRAFTER('123'^^xsd:string, 12.3)"); }
    // No match case
    @Test public void strafter_10()     { test("STRAFTER('abc'^^xsd:string, 'z')", kwEmptyString); }
    // Empty string case
    @Test public void strafter_11()     { test("STRAFTER('abc'^^xsd:string, '')", "'abc'"); }
    @Test public void strafter_12()     { testEvalException("STRAFTER('abc', ''@en)"); }

    @Test public void strafter_30()     { test("STRAFTER('abc'@en--ltr, '')", "'abc'@en--ltr"); }
    @Test public void strafter_31()     { test("STRAFTER('abc'@en--ltr, 'a'@en--ltr)", "'bc'@en--ltr"); }
    @Test public void strafter_32()     { testEvalException("STRAFTER('abc'@en--ltr, 'a'@en)"); }
    @Test public void strafter_33()     { testEvalException("STRAFTER('abc'@en, 'a'@en--ltr)"); }

    // STRREPLACE
    @Test public void replace01()       { test("REPLACE('abc', 'b', 'Z')", "'aZc'"); }
    @Test public void replace02()       { test("REPLACE('abc', 'b.', 'Z')", "'aZ'"); }
    @Test public void replace03()       { test("REPLACE('abcbd', 'b.', 'Z')", "'aZZ'"); }

    @Test public void replace04()       { test("REPLACE('abcbd'^^xsd:string, 'b.', 'Z')", "'aZZ'"); }
    @Test public void replace05()       { test("REPLACE('abcbd'@en, 'b.', 'Z')", "'aZZ'@en"); }
    @Test public void replace06()       { test("REPLACE('abcbd', 'B.', 'Z', 'i')", "'aZZ'"); }

    // See JENA-740
    // ARQ provides replacement of the potentially empty string.
    @Test public void replace07()       { test("REPLACE('abc', '.*', 'Z')", "'Z'"); }
    @Test public void replace08()       { test("REPLACE('', '.*', 'Z')",    "'Z'"); }
    @Test public void replace09()       { test("REPLACE('abc', '.?', 'Z')", "'ZZZ'"); }

    @Test public void replace10()       { test("REPLACE('abc', 'XXX', 'Z')", "'abc'"); }
    @Test public void replace11()       { test("REPLACE('', '.', 'Z')",      kwEmptyString); }
    @Test public void replace12()       { test("REPLACE('', '(a|b)?', 'Z')", "'Z'"); }
    @Test public void replace13()       { test("REPLACE('abc'@en, 'XXX', 'Z')", "'abc'@en"); }

    // Bad group
    @Test public void replace20()       { testEvalException("REPLACE('abc', '.*', '$1')"); }
    // Bad pattern; static (parse or build time) compilation.
    @Test public void replace21()       { assertThrows(ExprException.class, ()-> ExprUtils.parse("REPLACE('abc', '^(a){-9}', 'ABC')")); }

    @Test public void replace30()       { test("REPLACE('b'@en--ltr, '(a|b)?', 'Z')", "'Z'@en--ltr"); }
    @Test public void replace31()       { test("REPLACE('b'@en--ltr, '(a|b)?', 'Z'@en--ltr)", "'Z'@en--ltr"); }

    @Test public void sameTerm_01()     { test("sameTerm(1,1)",           kwTRUE); }
    @Test public void sameTerm_02()     { test("sameTerm(1,1.0)",         kwFALSE); }
    @Test public void sameTerm_03()     { test("sameTerm(1,1e0)",         kwFALSE); }
    @Test public void sameTerm_04()     { test("sameTerm(<_:a>, <_:a>)",  kwTRUE); }
    @Test public void sameTerm_05()     { test("sameTerm(<x>, <x>)",      kwTRUE); }
    @Test public void sameTerm_06()     { test("sameTerm(<x>, <y>)",      kwFALSE); }

    @Test public void sameTerm_07()     { test("sameTerm('abc'@en, 'abc'@EN)", kwTRUE); }
    @Test public void sameTerm_08()     { test("sameTerm('abc'@en--ltr, 'abc'@EN--ltr)", kwTRUE); }
    @Test public void sameTerm_09()     { test("sameTerm('abc'@en--ltr, 'abc'@en--rtl)", kwFALSE); }
    @Test public void sameTerm_10()     { test("sameTerm(<<( <x> <p> 123 )>>, <<( <x> <p> 123 )>>)", kwTRUE); }

    // 'SameValue' is not in SPARQL 1.2 as a keyword.
    // However, ARQ provides access to the function.

    @Test public void sameValue_01()    { test("sameValue(<x>, <x>)",      kwTRUE); }
    @Test public void sameValue_02()    { test("sameValue(<x>, <y>)",      kwFALSE); }
    @Test public void sameValue_03()    { test("sameValue(1, 1.0e0)",      kwTRUE); }
    @Test public void sameValue_04()    { test("sameValue('NaN'^^xsd:double, 'NaN'^^xsd:double)",     kwTRUE); }
    @Test public void sameValue_05()    { test("sameValue('NaN'^^xsd:float, 'NaN'^^xsd:double)",      kwTRUE); }
    @Test public void sameValue_06()    { test("sameValue('NaN'^^xsd:double, 'NaN'^^xsd:float)",      kwTRUE); }
    @Test public void sameValue_07()    { test("sameValue('NaN'^^xsd:float, 'NaN'^^xsd:float)",       kwTRUE); }
    @Test public void sameValue_08()    { test("sameValue('NaN'^^xsd:float, 'INF'^^xsd:float)",       kwFALSE); }
    @Test public void sameValue_09()    { test("sameValue(<<( <x> <p> 123 )>>, <<( <x> <p> 123.0e0 )>>)", kwTRUE); }
    @Test public void sameValue_10()    { test("sameValue(<<( <x> <p> 'abc'@en )>>, <<( <x> <p> 'abc'@en--ltr )>>)", kwFALSE); }
    @Test public void sameValue_11()    { test("sameValue(<<( <x> <p> 'abc'@en--ltr )>>, <<( <x> <p> 'abc'@en--rtl )>>)", kwFALSE); }

    @Test public void OneOf_01()        { test("57 in (xsd:integer, '123')",   kwFALSE); }
    @Test public void OneOf_02()        { test("57 in (57)",                   kwTRUE); }
    @Test public void OneOf_03()        { test("57 in (123, 57)",              kwTRUE); }
    @Test public void OneOf_04()        { test("57 in (57, 456)",              kwTRUE); }
    @Test public void OneOf_05()        { test("57 in (123, 57, 456)",         kwTRUE); }
    @Test public void OneOf_06()        { test("57 in (1,2,3)",                kwFALSE); }

    @Test public void NotOneOf_01()     { test("57 not in (xsd:integer, '123')",   kwTRUE); }
    @Test public void NotOneOf_02()     { test("57 not in (57)",                   kwFALSE); }
    @Test public void NotOneOf_03()     { test("57 not in (123, 57)",              kwFALSE); }
    @Test public void NotOneOf_04()     { test("57 not in (57, 456)",              kwFALSE); }
    @Test public void NotOneOf_05()     { test("57 not in (123, 57, 456)",         kwFALSE); }
    @Test public void NotOneOf_06()     { test("57 not in (1,2,3)",                kwTRUE); }

    @Test public void StrLang1()        { test("strlang('xyz', 'en')", "'xyz'@en"); }
    @Test public void StrLang2()        { testEvalException("strlang('xyz', '')"); }

    @Test public void StrDatatype1()    { test("strdt('123', xsd:integer)",     "123"); }
    @Test public void StrDatatype2()    { test("strdt('xyz', xsd:string)",  "'xyz'"); }
    @Test public void StrDatatype3()    { testEvalException("strdt('123',       'datatype')"); }

    // YEAR
    @Test public void year_01()         { test("year('2010-12-24T16:24:01.123'^^xsd:dateTime)", "2010"); }
    @Test public void year_02()         { test("year('2010-12-24'^^xsd:date)", "2010"); }
    @Test public void year_03()         { test("year('2010'^^xsd:gYear)", "2010"); }
    @Test public void year_04()         { test("year('2010-12'^^xsd:gYearMonth)", "2010"); }

    @Test public void year_05()         { testEvalException("year('--12'^^xsd:gMonth)"); }
    @Test public void year_06()         { testEvalException("year('--12-24'^^xsd:gMonthDay)"); }
    @Test public void year_07()         { testEvalException("year('---24'^^xsd:gDay)"); }

    @Test public void year_11()         { test("year('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "2010"); }
    @Test public void year_12()         { test("year('2010-12-24Z'^^xsd:date)", "2010"); }
    @Test public void year_13()         { test("year('2010Z'^^xsd:gYear)", "2010"); }
    @Test public void year_14()         { test("year('2010-12Z'^^xsd:gYearMonth)", "2010"); }

    @Test public void year_15()         { testEvalException("year('--12Z'^^xsd:gMonth)"); }
    @Test public void year_16()         { testEvalException("year('--12-24Z'^^xsd:gMonthDay)"); }
    @Test public void year_17()         { testEvalException("year('---24Z'^^xsd:gDay)"); }

    @Test public void year_21()         { test("year('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "2010"); }
    @Test public void year_22()         { test("year('2010-12-24-08:00'^^xsd:date)", "2010"); }
    @Test public void year_23()         { test("year('2010-08:00'^^xsd:gYear)", "2010"); }
    @Test public void year_24()         { test("year('2010-12-08:00'^^xsd:gYearMonth)", "2010"); }
    @Test public void year_25()         { testEvalException("year('--12-08:00'^^xsd:gMonth)"); }
    @Test public void year_26()         { testEvalException("year('--12-24-08:00'^^xsd:gMonthDay)"); }
    @Test public void year_27()         { testEvalException("year('---24-08:00'^^xsd:gDay)"); }

    @Test public void year_dur_01()     { test("year('P1Y2M3DT4H5M6S'^^xsd:duration)", "1"); }

    // MONTH
    @Test public void month_01()        { test("month('2010-12-24T16:24:01.123'^^xsd:dateTime)", "12"); }
    @Test public void month_02()        { test("month('2010-12-24'^^xsd:date)", "12"); }
    @Test public void month_03()        { testEvalException("month('2010'^^xsd:gYear)"); }
    @Test public void month_04()        { test("month('2010-12'^^xsd:gYearMonth)", "12"); }

    @Test public void month_05()        { test("month('--12'^^xsd:gMonth)", "12"); }
    @Test public void month_06()        { test("month('--12-24'^^xsd:gMonthDay)", "12"); }
    @Test public void month_07()        { testEvalException("month('---24'^^xsd:gDay)"); }

    @Test public void month_11()        { test("month('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "12"); }
    @Test public void month_12()        { test("month('2010-12-24Z'^^xsd:date)", "12"); }
    @Test public void month_13()        { testEvalException("month('2010Z'^^xsd:gYear)"); }
    @Test public void month_14()        { test("month('2010-12Z'^^xsd:gYearMonth)", "12"); }

    @Test public void month_15()        { test("month('--12Z'^^xsd:gMonth)", "12"); }
    @Test public void month_16()        { test("month('--12-24Z'^^xsd:gMonthDay)", "12"); }
    @Test public void month_17()        { testEvalException("month('---24Z'^^xsd:gDay)"); }

    @Test public void month_21()        { test("month('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "12"); }
    @Test public void month_22()        { test("month('2010-12-24-08:00'^^xsd:date)", "12"); }
    @Test public void month_23()        { testEvalException("month('2010-08:00'^^xsd:gYear)"); }
    @Test public void month_24()        { test("month('2010-12-08:00'^^xsd:gYearMonth)", "12"); }
    @Test public void month_25()        { test("month('--12-08:00'^^xsd:gMonth)", "12"); }
    @Test public void month_26()        { test("month('--12-24-08:00'^^xsd:gMonthDay)", "12"); }
    @Test public void month_27()        { testEvalException("month('---24-08:00'^^xsd:gDay)"); }

    @Test public void month_dur_01()    { test("month('P1Y2M3DT4H5M6S'^^xsd:duration)", "2"); }

    // DAY
    @Test public void day_01()          { test("day('2010-12-24T16:24:01.123'^^xsd:dateTime)", "24"); }
    @Test public void day_02()          { test("day('2010-12-24'^^xsd:date)", "24"); }
    @Test public void day_03()          { testEvalException("day('2010'^^xsd:gYear)"); }
    @Test public void day_04()          { testEvalException("day('2010-12'^^xsd:gYearMonth)"); }

    @Test public void day_05()          { testEvalException("day('--12'^^xsd:gMonth)"); }
    @Test public void day_06()          { test("day('--12-24'^^xsd:gMonthDay)", "24"); }
    @Test public void day_07()          { test("day('---24'^^xsd:gDay)", "24"); }

    @Test public void day_11()          { test("day('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "24"); }
    @Test public void day_12()          { test("day('2010-12-24Z'^^xsd:date)", "24"); }

    @Test public void day_13()          { testEvalException("day('2010Z'^^xsd:gYear)"); }
    @Test public void day_14()          { testEvalException("day('2010-12Z'^^xsd:gYearMonth)"); }
    @Test public void day_15()          { testEvalException("day('--12Z'^^xsd:gMonth)"); }
    @Test public void day_16()          { test("day('--12-24Z'^^xsd:gMonthDay)", "24"); }
    @Test public void day_17()          { test("day('---24Z'^^xsd:gDay)", "24"); }

    @Test public void day_21()          { test("day('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "24"); }
    @Test public void day_22()          { test("day('2010-12-24-08:00'^^xsd:date)", "24"); }
    @Test public void day_23()          { testEvalException("day('2010-08:00'^^xsd:gYear)"); }
    @Test public void day_24()          { testEvalException("day('2010-12-08:00'^^xsd:gYearMonth)"); }
    @Test public void day_25()          { testEvalException("day('--12-08:00'^^xsd:gMonth)"); }
    @Test public void day_26()          { test("day('--12-24-08:00'^^xsd:gMonthDay)", "24"); }
    @Test public void day_27()          { test("day('---24-08:00'^^xsd:gDay)", "24"); }

    @Test public void day_dur_01()      { test("day('P1Y2M3DT4H5M6S'^^xsd:duration)", "3"); }

    // HOURS

    @Test public void hours_01()        { test("hours('2010-12-24T16:24:01.123'^^xsd:dateTime)", "16"); }
    @Test public void hours_02()        { testEvalException("hours('2010-12-24'^^xsd:date)"); }
    @Test public void hours_03()        { test("hours('16:24:01'^^xsd:time)", "16"); }

    @Test public void hours_10()        { test("hours('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "16"); }
    @Test public void hours_11()        { test("hours('16:24:24Z'^^xsd:time)", "16"); }

    @Test public void hours_20()        { test("hours('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "16"); }
    @Test public void hours_21()        { test("hours('16:24:24-08:00'^^xsd:time)", "16"); }

    @Test public void hours_dur_01()    { test("hours('P1Y2M3DT4H5M6S'^^xsd:duration)", "4"); }

    // MINUTES
    @Test public void minutes_01()      { test("minutes('2010-12-24T16:24:01.123'^^xsd:dateTime)", "24"); }
    @Test public void minutes_02()      { testEvalException("minutes('2010-12-24'^^xsd:date)"); }
    @Test public void minutes_03()      { test("minutes('16:24:01'^^xsd:time)", "24"); }

    @Test public void minutes_10()      { test("minutes('2010-12-24T16:24:01.123Z'^^xsd:dateTime)", "24"); }
    @Test public void minutes_11()      { test("minutes('16:24:01.1Z'^^xsd:time)", "24"); }

    @Test public void minutes_20()      { test("minutes('2010-12-24T16:24:01.123-08:00'^^xsd:dateTime)", "24"); }
    @Test public void minutes_21()      { test("minutes('16:24:01.01-08:00'^^xsd:time)", "24"); }

    @Test public void minutes_dur_01()  { test("minutes('P1Y2M3DT4H5M6S'^^xsd:duration)", "5"); }

    // SECONDS
    @Test public void seconds_01()      { test("seconds('2010-12-24T16:24:01.123'^^xsd:dateTime)", "01.123"); }
    @Test public void seconds_02()            { testEvalException("seconds('2010-12-24'^^xsd:date)"); }
    @Test public void seconds_03()      { test("seconds('16:24:01'^^xsd:time)", "'01'^^xsd:decimal"); }

    @Test public void seconds_10()      { test("seconds('2010-12-24T16:24:31.123Z'^^xsd:dateTime)", "31.123"); }
    @Test public void seconds_11()      { test("seconds('16:24:01.1Z'^^xsd:time)", "'01.1'^^xsd:decimal"); }

    @Test public void seconds_20()      { test("seconds('2010-12-24T16:24:35.123-08:00'^^xsd:dateTime)", "35.123"); }
    @Test public void seconds_21()      { test("seconds('16:24:01.01-08:00'^^xsd:time)", "'01.01'^^xsd:decimal"); }

    @Test public void seconds_dur_01()  { test("seconds('P1Y2M3DT4H5M6S'^^xsd:duration)", "'6.0'^^xsd:decimal"); }

    // TIMEZONE
    @Test public void timezone_01()     { test("timezone('2010-12-24T16:24:35.123Z'^^xsd:dateTime)", "'PT0S'^^xsd:dayTimeDuration"); }
    @Test public void timezone_02()     { test("timezone('2010-12-24T16:24:35.123-08:00'^^xsd:dateTime)", "'-PT8H'^^xsd:dayTimeDuration"); }
    @Test public void timezone_03()     { test("timezone('2010-12-24T16:24:35.123+01:00'^^xsd:dateTime)", "'PT1H'^^xsd:dayTimeDuration"); }
    @Test public void timezone_04()     { test("timezone('2010-12-24T16:24:35.123-00:00'^^xsd:dateTime)", "'-PT0S'^^xsd:dayTimeDuration"); }
    @Test public void timezone_05()     { test("timezone('2010-12-24T16:24:35.123+00:00'^^xsd:dateTime)", "'PT0S'^^xsd:dayTimeDuration"); }

    @Test public void timezone_09()     { testEvalException("timezone('2010-12-24T16:24:35'^^xsd:dateTime)"); }
    @Test public void timezone_10()     { testEvalException("timezone(2010)"); }
    @Test public void timezone_11()     { testEvalException("timezone('2010-junk'^^xsd:gYear)"); }

    // General "adjust-to-timezone"
    @Test public void adjust_1() {
        test("adjust(xsd:dateTime('2022-12-21T05:05:07'), '-PT10H'^^xsd:duration)", "'2022-12-21T05:05:07-10:00'^^xsd:dateTime");
    }

    @Test
    public void adjust_2() {
        test("adjust('2022-12-21T05:05:07'^^xsd:dateTime, 'PT08H'^^xsd:duration)", "'2022-12-21T05:05:07+08:00'^^xsd:dateTime");
    }

    // General "adjust-to-timezone"
    @Test public void adjust_3() {
        test("adjust(xsd:date('2022-12-21'), 'PT1H'^^xsd:duration)", "'2022-12-21+01:00'^^xsd:date");
    }

    @Test public void adjust_4() {
        test("adjust(xsd:dateTime('2022-12-21T05:05:07'))", "'2022-12-21T05:05:07Z'^^xsd:dateTime");
    }

    // TZ
    @Test public void tz_01()           { test("tz('2010-12-24T16:24:35.123Z'^^xsd:dateTime)", "'Z'"); }
    @Test public void tz_02()           { test("tz('2010-12-24T16:24:35.123-08:00'^^xsd:dateTime)", "'-08:00'"); }
    @Test public void tz_03()           { test("tz('2010-12-24T16:24:35.123+01:00'^^xsd:dateTime)", "'+01:00'"); }
    @Test public void tz_04()           { test("tz('2010-12-24T16:24:35.123-00:00'^^xsd:dateTime)", "'-00:00'"); }
    @Test public void tz_05()           { test("tz('2010-12-24T16:24:35.123+00:00'^^xsd:dateTime)", "'+00:00'"); }
    @Test public void tz_06()           { test("tz('2010-12-24T16:24:35.123'^^xsd:dateTime)", kwEmptyString); }

    @Test public void tz_10()           { testEvalException("tz(2010)"); }
    @Test public void tz_11()           { testEvalException("tz('2010-junk'^^xsd:gYear)"); }

    // NOW
    //@Test public void now_01()        { test("now() > '2010-12-24T16:24:35.123-08:00'^^xsd:dateTime", kwTRUE); }

    // MD5
    @Test public void md5_01()          { test("md5('abcd')","'e2fc714c4727ee9395f324cd2e7f331f'"); }
    @Test public void md5_02()          { test("md5('abcd'^^xsd:string)","'e2fc714c4727ee9395f324cd2e7f331f'"); }
    @Test public void md5_03()          { testEvalException("md5('abcd'@en)"); }
    @Test public void md5_04()          { testEvalException("md5(1234)"); }

    // SHA1
    @Test public void sha1_01()         { test("sha1('abcd')","'81fe8bfe87576c3ecb22426f8e57847382917acf'"); }
    @Test public void sha1_02()         { test("sha1('abcd'^^xsd:string)","'81fe8bfe87576c3ecb22426f8e57847382917acf'"); }
    @Test public void sha1_03()         { testEvalException("sha1('abcd'@en)"); }
    @Test public void sha1_04()         { testEvalException("sha1(123)"); }

//    // SHA224
//    @Test public void sha224_01()      { test("sha224('abcd')","'e2fc714c4727ee9395f324cd2e7f331f'"); }
//
//    @Test
//    public void sha224_02()            { testEvalException("sha224('abcd'^^xsd:string)"); }
//
//    @Test
//    public void sha224_03()            { testEvalException("sha224('abcd'@en)"); }
//
//  @Test
//  public void sha224_04()            { testEvalException("sha224(1234)"); }

    // SHA256

    @Test public void sha256_01()       { test("sha256('abcd')","'88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589'"); }
    @Test public void sha256_02()       { test("sha256('abcd'^^xsd:string)","'88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589'"); }
    @Test public void sha256_03()       { testEvalException("sha256('abcd'@en)"); }
    @Test public void sha256_04()       { testEvalException("sha256(<uri>)"); }

    // SHA384
    @Test public void sha384_01()       { test("sha384('abcd')","'1165b3406ff0b52a3d24721f785462ca2276c9f454a116c2b2ba20171a7905ea5a026682eb659c4d5f115c363aa3c79b'"); }
    @Test public void sha384_02()       { test("sha384('abcd'^^xsd:string)","'1165b3406ff0b52a3d24721f785462ca2276c9f454a116c2b2ba20171a7905ea5a026682eb659c4d5f115c363aa3c79b'"); }
    @Test public void sha384_03()       { testEvalException("sha384('abcd'@en)"); }
    @Test public void sha384_04()       { testEvalException("sha384(123.45)"); }

    // SHA512
    @Test public void sha512_01()       { test("sha512('abcd')","'d8022f2060ad6efd297ab73dcc5355c9b214054b0d1776a136a669d26a7d3b14f73aa0d0ebff19ee333368f0164b6419a96da49e3e481753e7e96b716bdccb6f'"); }
    @Test public void sha512_02()       { test("sha512('abcd'^^xsd:string)","'d8022f2060ad6efd297ab73dcc5355c9b214054b0d1776a136a669d26a7d3b14f73aa0d0ebff19ee333368f0164b6419a96da49e3e481753e7e96b716bdccb6f'"); }
    @Test public void sha512_03()       { testEvalException("md5('abcd'@en)"); }
    @Test public void sha512_04()       { testEvalException("md5(0.0e0)"); }

    // --------

    private static PrefixMapping pmap = ARQConstants.getGlobalPrefixMap();

    private static void test(String expressionStr, String result) {
        Expr expr = ExprUtils.parse(expressionStr, pmap);
        NodeValue nv = expr.eval(null, new FunctionEnvBase());
        Node r = NodeFactoryExtra.parseNode(result);
        NodeValue nvr = NodeValue.makeNode(r);

        // Check datatypes.
        RDFDatatype dtActual = nv.asNode().getLiteralDatatype();
        RDFDatatype dtExpected = nvr.asNode().getLiteralDatatype();
        assertEquals(dtExpected, dtActual, "Differing datatpes");

        // Same term works for NaNs
        if ( nv.asNode().sameTermAs(nvr.asNode()) )
            return;

        // Not NaNs!
        assertTrue(NodeValue.sameValueAs(nvr, nv), "Not same value: Expected: " + nvr + " : Actual = " + nv);
        // test result must be lexical form exact.
        assertEquals(r, nv.asNode());
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
