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

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertTrue ;
import static org.junit.Assert.fail ;

import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.TimeZone ;
import java.util.function.Predicate;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.util.ExprUtils ;
import org.junit.Assert ;
import org.junit.Test ;

public class TestFunctions
{
    private static final NodeValue INT_ZERO = NodeValue.makeInteger(0) ;
    private static final NodeValue INT_ONE  = NodeValue.makeInteger(1) ;
    //private static final NodeValue INT_TWO  = NodeValue.makeInteger(2) ;
    private static final NodeValue TRUE     = NodeValue.TRUE ;
    private static final NodeValue FALSE    = NodeValue.FALSE ;
    
    @Test public void expr1() { test("1", NodeValue.makeInteger(1)) ; }

    @Test public void exprStrLen1() { test("fn:string-length('')", INT_ZERO) ; }
    @Test public void exprStrLen2() { test("fn:string-length('a')", INT_ONE) ; }
    // Test from JENA-785
    @Test public void exprStrLen3() { test("fn:string-length('𐐈𐑌𐐻𐐪𐑉𐐿𐐻𐐮𐐿𐐲')", NodeValue.makeInteger(10l)) ; }

    // F&O strings are one-based, and substring takes a length
    @Test public void exprSubstring1() { test("fn:substring('',0)", NodeValue.makeString("")) ; }
    @Test public void exprSubstring2() { test("fn:substring('',1)", NodeValue.makeString("")) ; }
    @Test public void exprSubstring3() { test("fn:substring('',1,0)", NodeValue.makeString("")) ; }
    @Test public void exprSubstring4() { test("fn:substring('',1,1)", NodeValue.makeString("")) ; }

    @Test public void exprSubstring5() { test("fn:substring('abc',1)", NodeValue.makeString("abc")) ; }
    @Test public void exprSubstring6() { test("fn:substring('abc',2)", NodeValue.makeString("bc")) ; }
    @Test public void exprSubstring7() { test("fn:substring('a',1,1)", NodeValue.makeString("a")) ; }
    @Test public void exprSubstring8() { test("fn:substring('a',1,2)", NodeValue.makeString("a")) ; }
    @Test public void exprSubstring9() { test("fn:substring('a',0)", NodeValue.makeString("")) ; }
    
    // Uses round()
    @Test public void exprSubstring10() { test("fn:substring('abc',1.6,1.33)", NodeValue.makeString("b")) ; }
    // This test was added because the test suite had 1199 tests in. 
    @Test public void exprSubstring11() { test("fn:substring('abc',-1, -15.3)", NodeValue.makeString("")) ; }
    // Test from JENA-785
    @Test public void exprSubstring12() { test("fn:substring('𐐈𐑌𐐻𐐪𐑉𐐿𐐻𐐮𐐿𐐲', 1, 1)", NodeValue.makeString("𐐈")) ; } 
    
    @Test public void exprJavaSubstring1() { test("afn:substr('abc',0,0)", NodeValue.makeString("")) ; }
    @Test public void exprJavaSubstring2() { test("afn:substr('abc',0,1)", NodeValue.makeString("a")) ; }
    @Test public void exprJavaSubstring3() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,0)", NodeValue.makeString("")) ; }
    @Test public void exprJavaSubstring4() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,1)", NodeValue.makeString("a")) ; }
    // Test from JENA-785
    @Test public void exprJavaSubstring5() { test("afn:substr('𐐈𐑌𐐻𐐪𐑉𐐿𐐻𐐮𐐿𐐲', 0, 1)", NodeValue.makeString("𐐈")) ; }

    // SPRINTF
    @Test public void exprSprintf_01()      { test("afn:sprintf('%06d', 11)",NodeValue.makeString("000011")) ; }
    @Test public void exprSprintf_02()      { test("afn:sprintf('%s', 'abcdefghi')",NodeValue.makeString("abcdefghi")) ; }
    @Test public void exprSprintf_03()      { test("afn:sprintf('sometext %s', 'abcdefghi')",NodeValue.makeString("sometext abcdefghi")) ; }
    @Test public void exprSprintf_04()      { test("afn:sprintf('%1$tm %1$te,%1$tY', '2016-03-17'^^xsd:date)",NodeValue.makeString("03 17,2016")) ; }


    @Test public void exprSprintf_06()      { test("afn:sprintf('this is %s', 'false'^^xsd:boolean)",NodeValue.makeString("this is false")) ; }
    @Test public void exprSprintf_07()      { test("afn:sprintf('this number is equal to %.2f', '11.22'^^xsd:decimal)",NodeValue.makeString("this number is equal to "+String.format("%.2f",11.22))) ; }
    @Test public void exprSprintf_08()      { test("afn:sprintf('%.3f', '1.23456789'^^xsd:float)",NodeValue.makeString(String.format("%.3f",1.23456789))) ; }
    @Test public void exprSprintf_09()      { test("afn:sprintf('this number is equal to %o in the octal system', '11'^^xsd:integer)",NodeValue.makeString("this number is equal to 13 in the octal system")) ; }
    @Test public void exprSprintf_10()      { test("afn:sprintf('this number is equal to %.5f', '1.23456789'^^xsd:double)",NodeValue.makeString("this number is equal to "+String.format("%.5f",1.23456789))) ; }
    @Test public void exprSprintf_11()      { test("afn:sprintf('%.0f != %s', '12.23456789'^^xsd:double,'15')",NodeValue.makeString("12 != 15")) ; }
    @Test public void exprSprintf_12()      { test("afn:sprintf('(%.0f,%s,%d) %4$tm %4$te,%4$tY', '12.23456789'^^xsd:double,'12',11,'2016-03-17'^^xsd:date)",NodeValue.makeString("(12,12,11) 03 17,2016")) ; }

    // Timezone tests
    
    // Timezone -11:00 to any timezone can be a day ahead
    @Test public void exprSprintf_20() { test_exprSprintf_tz_exact("2005-10-14T14:09:43-11:00") ; }
    // Timezone Z to any timezone can be a day behind or a day ahead
    @Test public void exprSprintf_21() { test_exprSprintf_tz_exact("2005-10-14T12:09:43+00:00") ; }
    // Timezone +11:00 can be a day behind
    @Test public void exprSprintf_22() { test_exprSprintf_tz_exact("2005-10-14T10:09:43+11:00") ; }
    private static void test_exprSprintf_tz_exact(String nodeStr) {
        String exprStr = "afn:sprintf('%1$tm %1$te,%1$tY', "+NodeValue.makeDateTime(nodeStr).toString()+")" ;
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        assertTrue(r.isString()) ;
        String s = r.getString() ;
        // Parse the date
        String dtFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";
        SimpleDateFormat sdtFormat = new SimpleDateFormat(dtFormat);
        Date dtDate = null;
        try {
            dtDate = sdtFormat.parse(nodeStr);
        } catch (ParseException e) {
            assertFalse("Cannot parse the input date string. Message:"+e.getMessage(),false);
        }
        // print the date based on the current timeZone.
        SimpleDateFormat stdFormatOut = new SimpleDateFormat("MM dd,yyyy");
        stdFormatOut.setTimeZone(TimeZone.getDefault());
        String outDate = stdFormatOut.format(dtDate);
        assertEquals(s,outDate);
    }
    
    private static void test_exprSprintf_tz_possibilites(String nodeStr, String... possible) {
        String exprStr = "afn:sprintf('%1$tm %1$te,%1$tY', "+NodeValue.makeDateTime(nodeStr).toString()+")" ;
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        assertTrue(r.isString()) ;
        String s = r.getString() ;
        // Timezones! The locale data can be -1, 0, +1 from the Z day.
        boolean b = false ;
        for (String poss : possible ) {
            if ( poss.equals(s) )
                b = true ;
        }
        assertTrue(b) ;
    }
    
    // Timezone -11:00 to any timezone can be a day ahead
    @Test public void exprSprintf_23() { test_exprSprintf_tz_possibilites("2005-10-14T14:09:43-11:00",  "10 14,2005", "10 15,2005") ; }
    // Timezone Z to any timezone can be a day behind or a day ahead
    @Test public void exprSprintf_24() { test_exprSprintf_tz_possibilites("2005-10-14T12:09:43Z",       "10 13,2005", "10 14,2005", "10 15,2005") ; }
    // Timezone +11:00 can be a day behind
    @Test public void exprSprintf_25() { test_exprSprintf_tz_possibilites("2005-10-14T10:09:43+11:00",  "10 13,2005", "10 14,2005") ; }
    
    @Test public void exprStrStart0() { test("fn:starts-with('abc', '')", TRUE) ; }
    @Test public void exprStrStart1() { test("fn:starts-with('abc', 'a')", TRUE) ; }
    @Test public void exprStrStart2() { test("fn:starts-with('abc', 'ab')", TRUE) ; }
    @Test public void exprStrStart3() { test("fn:starts-with('abc', 'abc')", TRUE) ; }
    @Test public void exprStrStart4() { test("fn:starts-with('abc', 'abcd')", FALSE) ; }
    
    @Test public void exprStrStart10() { test("STRSTARTS('abc', 'abcd')", FALSE) ; }
    @Test public void exprStrStart11() { test("STRSTARTS('abc'@en, 'ab')", TRUE) ; }
    @Test public void exprStrStart12() { test("STRSTARTS('abc'^^xsd:string, 'ab')", TRUE) ; }
    @Test public void exprStrStart13() { test("STRSTARTS('abc'^^xsd:string, 'ab'^^xsd:string)", TRUE) ; }
    @Test public void exprStrStart14() { test("STRSTARTS('abc', 'ab'^^xsd:string)", TRUE) ; }
    @Test public void exprStrStart15() { test("STRSTARTS('abc'@en, 'ab'@en)", TRUE) ; }
    
    @Test public void exprStrStart16() { testEvalException("STRSTARTS('ab'@en, 'ab'@fr)") ; }
    @Test public void exprStrStart17() { testEvalException("STRSTARTS(123, 'ab'@fr)") ; }
    @Test public void exprStrStart18() { testEvalException("STRSTARTS('123'^^xsd:string, 12.3)") ; }

    @Test public void exprStrBefore0() { test("STRBEFORE('abc', 'abcd')", NodeValue.nvEmptyString) ; }
    @Test public void exprStrBefore1() { test("STRBEFORE('abc'@en, 'b')", NodeValue.makeNode("a", "en", (String)null)) ; }
    @Test public void exprStrBefore2() { test("STRBEFORE('abc'^^xsd:string, 'c')", NodeValue.makeNode("ab", XSDDatatype.XSDstring)) ; }
    @Test public void exprStrBefore3() { test("STRBEFORE('abc'^^xsd:string, ''^^xsd:string)", NodeValue.makeNode("", XSDDatatype.XSDstring)) ; }
    @Test public void exprStrBefore4() { test("STRBEFORE('abc', 'ab'^^xsd:string)", NodeValue.nvEmptyString) ; }
    @Test public void exprStrBefore5() { test("STRBEFORE('abc'@en, 'b'@en)", NodeValue.makeNode("a", "en", (String)null)) ; }
    
    @Test public void exprStrBefore6() { testEvalException("STRBEFORE('ab'@en, 'ab'@fr)") ; }
    @Test public void exprStrBefore7() { testEvalException("STRBEFORE(123, 'ab'@fr)") ; }
    @Test public void exprStrBefore8() { testEvalException("STRBEFORE('123'^^xsd:string, 12.3)") ; }
    // No match case
    @Test public void exprStrBefore9() { test("STRBEFORE('abc'^^xsd:string, 'z')", NodeValue.nvEmptyString) ; }
    // Empty string case
    @Test public void exprStrBefore10() { test("STRBEFORE('abc'^^xsd:string, '')", NodeValue.makeNode("", XSDDatatype.XSDstring)) ; }

    @Test public void exprStrAfter0() { test("STRAFTER('abc', 'abcd')", NodeValue.nvEmptyString) ; }
    @Test public void exprStrAfter1() { test("STRAFTER('abc'@en, 'b')", NodeValue.makeNode("c", "en", (String)null)) ; }
    @Test public void exprStrAfter2() { test("STRAFTER('abc'^^xsd:string, 'a')", NodeValue.makeNode("bc", XSDDatatype.XSDstring)) ; }
    @Test public void exprStrAfter3() { test("STRAFTER('abc'^^xsd:string, ''^^xsd:string)", NodeValue.makeNode("abc", XSDDatatype.XSDstring)) ; }
    @Test public void exprStrAfter4() { test("STRAFTER('abc', 'bc'^^xsd:string)", NodeValue.nvEmptyString) ; }
    @Test public void exprStrAfter5() { test("STRAFTER('abc'@en, 'b'@en)", NodeValue.makeNode("c", "en", (String)null)) ; }
    
    @Test public void exprStrAfter6() { testEvalException("STRAFTER('ab'@en, 'ab'@fr)") ; }
    @Test public void exprStrAfter7() { testEvalException("STRAFTER(123, 'ab'@fr)") ; }
    @Test public void exprStrAfter8() { testEvalException("STRAFTER('123'^^xsd:string, 12.3)") ; }
    // No match case
    @Test public void exprStrAfter9() { test("STRAFTER('abc'^^xsd:string, 'z')", NodeValue.nvEmptyString) ; }
    // Empty string case
    @Test public void exprStrAfter10() { test("STRAFTER('abc'^^xsd:string, '')", NodeValue.makeNode("abc", XSDDatatype.XSDstring)) ; }

    @Test public void exprStrEnds0() { test("fn:ends-with('abc', '')", TRUE) ; }
    @Test public void exprStrEnds1() { test("fn:ends-with('abc', 'c')", TRUE) ; }
    @Test public void exprStrEnds2() { test("fn:ends-with('abc', 'bc')", TRUE) ; }
    @Test public void exprStrEnds3() { test("fn:ends-with('abc', 'abc')", TRUE) ; }
    @Test public void exprStrEnds4() { test("fn:ends-with('abc', 'zabc')", FALSE) ; }
    
    @Test public void exprStrEnds10() { test("STRENDS('abc', 'abcd')", FALSE) ; }
    @Test public void exprStrEnds11() { test("STRENDS('abc'@en, 'bc')", TRUE) ; }
    @Test public void exprStrEnds12() { test("STRENDS('abc'^^xsd:string, 'c')", TRUE) ; }
    @Test public void exprStrEnds13() { test("STRENDS('abc'^^xsd:string, 'c'^^xsd:string)", TRUE) ; }
    @Test public void exprStrEnds14() { test("STRENDS('abc', 'ab'^^xsd:string)", FALSE) ; }
    @Test public void exprStrEnds15() { test("STRENDS('abc'@en, 'abc'@en)", TRUE) ; }
    
    @Test public void exprStrEnds16() { testEvalException("STRENDS('ab'@en, 'ab'@fr)") ; }
    @Test public void exprStrEnds17() { testEvalException("STRENDS(123, 'ab'@fr)") ; }
    @Test public void exprStrEnds18() { testEvalException("STRENDS('123'^^xsd:string, 12.3)") ; }

    @Test public void exprStrCase1() { test("fn:lower-case('aBc')", NodeValue.makeString("abc")) ; }
    @Test public void exprStrCase2() { test("fn:lower-case('abc')", NodeValue.makeString("abc")) ; }
    @Test public void exprStrCase3() { test("fn:upper-case('abc')", NodeValue.makeString("ABC")) ; }
    @Test public void exprStrCase4() { test("fn:upper-case('ABC')", NodeValue.makeString("ABC")) ; }
    

    @Test public void exprStrContains0() { test("fn:contains('abc', '')", TRUE) ; }
    @Test public void exprStrContains1() { test("fn:contains('abc', 'a')", TRUE) ; }
    @Test public void exprStrContains2() { test("fn:contains('abc', 'b')", TRUE) ; }
    @Test public void exprStrContains3() { test("fn:contains('abc', 'c')", TRUE) ; }
    
    @Test public void exprStrContains4() { test("fn:contains('abc', 'ab')", TRUE) ; }
    @Test public void exprStrContains5() { test("fn:contains('abc', 'bc')", TRUE) ; }
    @Test public void exprStrContains6() { test("fn:contains('abc', 'abc')", TRUE) ; }
    @Test public void exprStrContains7() { test("fn:contains('abc', 'Xc')", FALSE) ; }
    @Test public void exprStrContains8() { test("fn:contains('abc', 'Xa')", FALSE) ; }

    @Test public void exprContains10() { test("Contains('abc', 'abcd')", FALSE) ; }
    @Test public void exprContains11() { test("Contains('abc'@en, 'bc')", TRUE) ; }
    @Test public void exprContains12() { test("Contains('abc'^^xsd:string, 'c')", TRUE) ; }
    @Test public void exprContains13() { test("Contains('abc'^^xsd:string, 'c'^^xsd:string)", TRUE) ; }
    @Test public void exprContains14() { test("Contains('abc', 'z'^^xsd:string)", FALSE) ; }
    @Test public void exprContains15() { test("Contains('abc'@en, 'abc'@en)", TRUE) ; }
    
    @Test public void exprContains16() { testEvalException("Contains('ab'@en, 'ab'@fr)") ; }
    @Test public void exprContains17() { testEvalException("Contains(123, 'ab'@fr)") ; }
    @Test public void exprContains18() { testEvalException("STRENDS('123'^^xsd:string, 12.3)") ; }

    @Test public void exprStrNormalizeSpace0() { test("fn:normalize-space(' The    wealthy curled darlings                                         of    our    nation. ')",
            NodeValue.makeString("The wealthy curled darlings of our nation.")) ; }
    @Test public void exprStrNormalizeSpace1() { test("fn:normalize-space('')",NodeValue.nvEmptyString) ; }
    @Test public void exprStrNormalizeSpace2() { test("fn:normalize-space('   Aaa     ')",NodeValue.makeString("Aaa")) ; }
    @Test public void exprStrNormalizeSpace3() { test("fn:normalize-space('A a   a    a a    ')",NodeValue.makeString("A a a a a")) ; }

    // https://www.w3.org/TR/xpath-functions-30/#func-normalize-unicode
    // and
    // from http://www.unicode.org/reports/tr15/
    //l
    @Test public void exprStrNormalizeUnicode0() { test("fn:normalize-unicode('Äffin','nfd')",NodeValue.makeString("Äffin")) ; }
    @Test public void exprStrNormalizeUnicode1() { test("fn:normalize-unicode('Äffin','nfc')",NodeValue.makeString("Äffin")) ; }
    //m
    @Test public void exprStrNormalizeUnicode2() { test("fn:normalize-unicode('Ä\\uFB03n','nfd')",NodeValue.makeString("Äﬃn")) ; }
    @Test public void exprStrNormalizeUnicode3() { test("fn:normalize-unicode('Ä\\uFB03n','nfc')",NodeValue.makeString("Äﬃn")) ; }
    //n
    @Test public void exprStrNormalizeUnicode4() { test("fn:normalize-unicode('Henry IV','nfd')",NodeValue.makeString("Henry IV")) ; }
    @Test public void exprStrNormalizeUnicode5() { test("fn:normalize-unicode('Henry IV','nfc')",NodeValue.makeString("Henry IV")) ; }
    //l'
    @Test public void exprStrNormalizeUnicode6() { test("fn:normalize-unicode('Äffin','nfkd')",NodeValue.makeString("Äffin")) ; }
    @Test public void exprStrNormalizeUnicode7() { test("fn:normalize-unicode('Äffin','nfkc')",NodeValue.makeString("Äffin")) ; }
    // r
    String hw_ka="\uFF76";
    String hw_ten="\uFF9F";
    @Test public void exprStrNormalizeUnicode8() { test("fn:normalize-unicode('"+hw_ka+hw_ten+"','nfd')",NodeValue.makeString(hw_ka+hw_ten)) ; }
    @Test public void exprStrNormalizeUnicode9() {
        test("fn:normalize-unicode('"+hw_ka+hw_ten+"','nfc')",NodeValue.makeString(hw_ka+hw_ten)) ;
    }
    // Not sure why the following tests are not passing
    // both examples are taken from the http://www.unicode.org/reports/tr15/ (Table 8 r')
    // the translation of hw_ka,hw_ten,ka and ten are taken from Table 4 of the same document
    // 
    // I (Alessandro Seganti) took the ga translation by association (it was not defined in the unicode report)
    // and chosen to be: KATAKANA LETTER GA U+30AC
    // Everything seems ok to me so there are two options in my opinion:
    // 1) the java implementation of the nfkd has some flaws
    // 2) the unicode example is wrong (I cannot judge as I do not know japanese or unicode enough :))
    // The test is failing because the expected string has code when looking in the debugger (UTF-16?) (12459 | 12442) 
    // while the Nomalizer.normalize is giving  (12459 | 12441)
//    @Test public void exprStrNormalizeUnicode10() {
//        String ka = "\u30AB";
//        String ten="\u3099";
//        test("fn:normalize-unicode('"+hw_ka+hw_ten+"','nfkd')", NodeValue.makeString(ka+ten)) ;
//    }
//    @Test public void exprStrNormalizeUnicode11() {
//        String ga="\u30AC";
//        test("fn:normalize-unicode('"+hw_ka+hw_ten+"','nfkc')",NodeValue.makeString(ga)) ;
//    }

    // empty argument <-> returns the input string
    @Test public void exprStrNormalizeUnicode12() { test("fn:normalize-unicode('some word','')",NodeValue.makeString("some word")) ; }
    // one argument <-> NFC
    @Test public void exprStrNormalizeUnicode13() { test("fn:normalize-unicode('Äffin')",NodeValue.makeString("Äffin")) ; }


    @Test public void exprReplace01()  { test("REPLACE('abc', 'b', 'Z')", NodeValue.makeString("aZc")) ; }
    @Test public void exprReplace02()  { test("REPLACE('abc', 'b.', 'Z')", NodeValue.makeString("aZ")) ; }
    @Test public void exprReplace03()  { test("REPLACE('abcbd', 'b.', 'Z')", NodeValue.makeString("aZZ")) ; }
    
    @Test public void exprReplace04()  { test("REPLACE('abcbd'^^xsd:string, 'b.', 'Z')", NodeValue.makeNode("aZZ", XSDDatatype.XSDstring)) ; }
    @Test public void exprReplace05()  { test("REPLACE('abcbd'@en, 'b.', 'Z')", NodeValue.makeNode("aZZ", "en", (String)null)) ; }
    @Test public void exprReplace06()  { test("REPLACE('abcbd', 'B.', 'Z', 'i')", NodeValue.makeString("aZZ")) ; }
    
    // See JENA-740
    // ARQ provides replacement of the potentially empty string.
    @Test public void exprReplace07()  { test("REPLACE('abc', '.*', 'Z')", NodeValue.makeString("Z")) ; }
    @Test public void exprReplace08()  { test("REPLACE('', '.*', 'Z')",    NodeValue.makeString("Z")) ; }
    @Test public void exprReplace09()  { test("REPLACE('abc', '.?', 'Z')", NodeValue.makeString("ZZZ")) ; }
    
    @Test public void exprReplace10()  { test("REPLACE('abc', 'XXX', 'Z')", NodeValue.makeString("abc")) ; }
    @Test public void exprReplace11()  { test("REPLACE('', '.', 'Z')",      NodeValue.makeString("")) ; }
    @Test public void exprReplace12()  { test("REPLACE('', '(a|b)?', 'Z')", NodeValue.makeString("Z")) ; }

    @Test public void exprFnReplace01()  { test("fn:replace('abc', 'b', 'Z')", NodeValue.makeString("aZc")) ; }
    @Test public void exprFnReplace02()  { test("fn:replace('abc', 'b.', 'Z')", NodeValue.makeString("aZ")) ; }
    @Test public void exprFnReplace03()  { test("fn:replace('abcbd', 'b.', 'Z')", NodeValue.makeString("aZZ")) ; }
    
    @Test public void exprFnReplace04()  { test("fn:replace('abcbd'^^xsd:string, 'b.', 'Z')", NodeValue.makeNode("aZZ", XSDDatatype.XSDstring)) ; }
    @Test public void exprFnReplace05()  { test("fn:replace('abcbd'@en, 'b.', 'Z')", NodeValue.makeNode("aZZ", "en", (String)null)) ; }
    @Test public void exprFnReplace06()  { test("fn:replace('abcbd', 'B.', 'Z', 'i')", NodeValue.makeString("aZZ")) ; }

    // Bad group
    @Test
    public void exprReplace13() {
        testEvalException("REPLACE('abc', '.*', '$1')");
    }
    
    // Bad pattern ; static (parse or build time) compilation.
    @Test(expected = ExprException.class)
    public void exprReplace14() {
        ExprUtils.parse("REPLACE('abc', '^(a){-9}', 'ABC')");
    }
    
    // Bad pattern : dynamic (eval time) exception. 
    // The pattern for fn:replace is not compiled on build - if that changes, this test will fail.
    // See exprReplace14.
    @Test
    public void exprReplace15() {
        testEvalException("fn:replace('abc', '^(a){-9}', 'ABC')");
    }

    @Test public void exprBoolean1()    { test("fn:boolean('')", FALSE) ; }
    @Test public void exprBoolean2()    { test("fn:boolean(0)", FALSE) ; }
    @Test public void exprBoolean3()    { test("fn:boolean(''^^xsd:string)", FALSE) ; }
    
    @Test public void exprBoolean4()    { test("fn:boolean('X')", TRUE) ; }
    @Test public void exprBoolean5()    { test("fn:boolean('X'^^xsd:string)", TRUE) ; }
    @Test public void exprBoolean6()    { test("fn:boolean(1)", TRUE) ; }

    @Test public void exprBoolean7()    { test("fn:not('')", TRUE) ; }
    @Test public void exprBoolean8()    { test("fn:not('X')", FALSE) ; }
    @Test public void exprBoolean9()    { test("fn:not(1)", FALSE) ; }
    @Test public void exprBoolean10()   { test("fn:not(0)", TRUE) ; }

    @Test public void exprRound_01()    { test("fn:round(123)",   NodeValue.makeInteger(123)) ; }
    @Test public void exprRound_02()    { test("fn:round(123.5)",  NodeValue.makeDecimal(124)) ; }
    @Test public void exprRound_03()    { test("fn:round(-0.5e0)", NodeValue.makeDouble(0.0e0)) ; }
    @Test public void exprRound_04()    { test("fn:round(-1.5)",   NodeValue.makeDecimal(-1)) ; }
    // !! I don't think that this is working correctly also if the test is passing... need to check!
    @Test public void exprRound_05()    { test("fn:round(-0)",     NodeValue.makeInteger("-0")) ; }
    @Test public void exprRound_06()    { test("fn:round(1.125, 2)",     NodeValue.makeDecimal(1.13)) ; }
    @Test public void exprRound_07()    { test("fn:round(8452, -2)",     NodeValue.makeInteger(8500)) ; }
    @Test public void exprRound_08()    { test("fn:round(3.1415e0, 2)",     NodeValue.makeDouble(3.14e0)) ; }
    // counter-intuitive -- would fail if float/double not translated to decimal
    @Test public void exprRound_09()    { test("fn:round(35.425e0, 2)",     NodeValue.makeDouble(35.42)) ; }

    @Test public void exprRoundHalfEven_01()    { test("fn:round-half-to-even(0.5)",   NodeValue.makeDecimal(0)) ; }
    @Test public void exprRoundHalfEven_02()    { test("fn:round-half-to-even(1.5)",  NodeValue.makeDecimal(2)) ; }
    @Test public void exprRoundHalfEven_03()    { test("fn:round-half-to-even(2.5)", NodeValue.makeDecimal(2)) ; }
    @Test public void exprRoundHalfEven_04()    { test("fn:round-half-to-even(3.567812e+3, 2)",   NodeValue.makeDouble(3567.81e0)) ; }
    // !! I don't think that this is working correctly also if the test is passing... need to check!
    @Test public void exprRoundHalfEven_05()    { test("fn:round-half-to-even(-0)",     NodeValue.makeInteger(-0)) ; }
    @Test public void exprRoundHalfEven_06()    { test("fn:round-half-to-even(4.7564e-3, 2)",     NodeValue.makeDouble(0.0e0)) ; }
    @Test public void exprRoundHalfEven_07()    { test("fn:round-half-to-even(35612.25, -2)",     NodeValue.makeDecimal(35600)) ; }
    // counter-intuitive -- would fail if float/double not translated to decimal
    @Test public void exprRoundHalfEven_08()    { test("fn:round-half-to-even('150.015'^^xsd:float, 2)",     NodeValue.makeFloat((float)150.01)) ; }

    private String getDynamicDurationString(){
        int tzOffset = TimeZone.getDefault().getOffset(new Date().getTime()) / (1000*60);
        String off = "PT"+Math.abs(tzOffset)+"M";
        if(tzOffset < 0)
            off = "-"+off;
        return off;
    }

    @Test public void exprAdjustDatetimeToTz_01(){
        testEqual(
                "fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00'^^xsd:dateTime)",
                "fn:adjust-dateTime-to-timezone('2002-03-07T10:00:00'^^xsd:dateTime,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustDatetimeToTz_02(){
        testEqual(
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
        testEqual(
                "fn:adjust-date-to-timezone('2002-03-07'^^xsd:date)",
                "fn:adjust-date-to-timezone('2002-03-07'^^xsd:date,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustDateToTz_02(){
        testEqual(
                "fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date)",
                "fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustDateToTz_03() { test("fn:adjust-date-to-timezone('2002-03-07'^^xsd:date,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeDate("2002-03-07-10:00"));}

    @Test public void exprAdjustDateToTz_04() { test("fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date,'-PT10H'^^xsd:dayTimeDuration)",NodeValue.makeDate("2002-03-06-10:00"));}

    @Test public void exprAdjustDateToTz_05() { test("fn:adjust-date-to-timezone('2002-03-07'^^xsd:date,'')",NodeValue.makeDate("2002-03-07"));}

    @Test public void exprAdjustDateToTz_06() { test("fn:adjust-date-to-timezone('2002-03-07-07:00'^^xsd:date,'')",NodeValue.makeDate("2002-03-07"));}

    @Test public void exprAdjustTimeToTz_01(){
        testEqual(
                "fn:adjust-time-to-timezone('10:00:00'^^xsd:time)",
                "fn:adjust-time-to-timezone('10:00:00'^^xsd:time,'"+getDynamicDurationString()+"'^^xsd:dayTimeDuration)");
    }

    @Test public void exprAdjustTimeToTz_02(){
        testEqual(
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
    
    // Better name!
    @Test public void localTimezone_2() { test("afn:timezone()", nv->nv.isDayTimeDuration()); }
    
    @Test public void localDateTime_1() { test("afn:nowtz()", nv-> nv.isDateTime()); }
    // Test field defined.
    @Test public void localDateTime_2() { test("afn:nowtz()", nv-> nv.getDateTime().getTimezone() >= -14 * 60 ); }

    @Test public void localDateTime_3() { test("afn:nowtz() = NOW()", NodeValue.TRUE); }
    
    private static void testNumberFormat(String expression, String expected) {
        Expr expr = ExprUtils.parse(expression) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        Assert.assertTrue(r.isString());
        Assert.assertEquals(expected, r.getString()) ;
    }
    
    @Test public void formatNumber_01()     { testNumberFormat("fn:format-number(0,'#')", "0") ; }
    @Test public void formatNumber_02()     { testNumberFormat("fn:format-number(1234, '#')", "1234") ; }
    @Test public void formatNumber_03()     { testNumberFormat("fn:format-number(1234, '#,###')", "1,234") ; }
    @Test public void formatNumber_04()     { testNumberFormat("fn:format-number(1e3, '#,###,###.#')", "1,000") ; }
    @Test public void formatNumber_05()     { testNumberFormat("fn:format-number(10.5, '##.#')", "10.5") ; }
    @Test public void formatNumber_06()     { testNumberFormat("fn:format-number(-10.5, '##.##')", "-10.5") ; }
    @Test public void formatNumber_08()     { testNumberFormat("fn:format-number(123, 'NotAPattern')", "NotAPattern123") ; }
    
    @Test public void formatNumber_11()     { testNumberFormat("fn:format-number(0, '#', 'fr')", "0") ; }
    // No-break space
    @Test public void formatNumber_12()     { testNumberFormat("fn:format-number(1234.5,'#,###.#', 'fr')", "1\u00A0234,5") ; }
    @Test public void formatNumber_13()     { testNumberFormat("fn:format-number(1234.5,'#,###.#', 'de')", "1.234,5") ; }
    
    @Test public void formatNumber_14()     { testNumberFormat("fn:format-number(12, '0,000.0', 'en')", "0,012.0") ; }
    @Test public void formatNumber_15()     { testNumberFormat("fn:format-number(0, '00,000', 'fr')", "00\u00A0000") ; }

    @Test(expected=ExprEvalException.class)
    public void formatNumber_20() {
        // String as number
        testNumberFormat("fn:format-number('String', '#')", null) ;
    }
    @Test(expected=ExprEvalException.class)
    public void formatNumber_21() {
        // Pattern is not a string
        testNumberFormat("fn:format-number(123, <uri>)", null) ; 
    }
    @Test(expected=ExprEvalException.class)
    public void formatNumber_22() {
        // Locale is not a string
        testNumberFormat("fn:format-number(123, '###', 123)", null) ; 
    }

    public void formatNumber_23() {
        // Not a locale - default to Locale.ROOT
        testNumberFormat("fn:format-number(123, '###', 'WhereAmI?')", null) ; 
    }
    
    @Test public void exprSameTerm1()     { test("sameTerm(1,1)",           TRUE) ; }
    @Test public void exprSameTerm2()     { test("sameTerm(1,1.0)",         FALSE) ; }
    @Test public void exprSameTerm3()     { test("sameTerm(1,1e0)",         FALSE) ; }
    @Test public void exprSameTerm4()     { test("sameTerm(<_:a>, <_:a>)",  TRUE) ; }
    @Test public void exprSameTerm5()     { test("sameTerm(<x>, <x>)",      TRUE) ; }
    @Test public void exprSameTerm6()     { test("sameTerm(<x>, <y>)",      FALSE) ; }
    
    @Test public void exprOneOf_01()     { test("57 in (xsd:integer, '123')",   FALSE) ; }
    @Test public void exprOneOf_02()     { test("57 in (57)",                   TRUE) ; }
    @Test public void exprOneOf_03()     { test("57 in (123, 57)",              TRUE) ; }
    @Test public void exprOneOf_04()     { test("57 in (57, 456)",              TRUE) ; }
    @Test public void exprOneOf_05()     { test("57 in (123, 57, 456)",         TRUE) ; }
    @Test public void exprOneOf_06()     { test("57 in (1,2,3)",                FALSE) ; }
    
    @Test public void exprNotOneOf_01()  { test("57 not in (xsd:integer, '123')",   TRUE) ; }
    @Test public void exprNotOneOf_02()  { test("57 not in (57)",                   FALSE) ; }
    @Test public void exprNotOneOf_03()  { test("57 not in (123, 57)",              FALSE) ; }
    @Test public void exprNotOneOf_04()  { test("57 not in (57, 456)",              FALSE) ; }
    @Test public void exprNotOneOf_05()  { test("57 not in (123, 57, 456)",         FALSE) ; }
    @Test public void exprNotOneOf_06()  { test("57 not in (1,2,3)",                TRUE) ; }
    
    
    static Node xyz_en = NodeFactory.createLiteral("xyz", "en") ;
    static NodeValue nv_xyz_en = NodeValue.makeNode(xyz_en) ;

    static Node xyz_xsd_string = NodeFactory.createLiteral("xyz", XSDDatatype.XSDstring) ;
    static NodeValue nv_xyz_string = NodeValue.makeNode(xyz_xsd_string) ;

    
    @Test public void exprStrLang1()     { test("strlang('xyz', 'en')",             nv_xyz_en) ; } 
    //@Test public void exprStrLang2()      { test("strlang('xyz', 'en')",             nv_xyz_en) ; } 

    @Test public void exprStrDatatype1()    { test("strdt('123', xsd:integer)",    NodeValue.makeInteger(123)) ; }
    @Test public void exprStrDatatype2()    { test("strdt('xyz', xsd:string)",     nv_xyz_string) ; }
    @Test public void exprStrDatatype3()    { testEvalException("strdt('123', 'datatype')") ; }
    
    static Node n_uri = NodeFactory.createURI("http://example/") ;
    static NodeValue nv_uri = NodeValue.makeNode(n_uri) ;
    
    @Test public void exprIRI1()            { test("iri('http://example/')", nv_uri ) ; }
    
    /*
    E_IRI
    E_BNode
    */ 

    private void test(String exprStr, NodeValue result) {
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        assertEquals(result, r) ;
    }
    
    private void test(String exprStr, Predicate<NodeValue> test) {
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        assertTrue(exprStr, test.test(r));
    }

    private void testEqual(String exprStr, String exprStrExpected) {
        Expr expr = ExprUtils.parse(exprStrExpected) ;
        NodeValue rExpected = expr.eval(null, LibTestExpr.createTest()) ;
        test(exprStr, rExpected) ;
    }
    
    private void testEvalException(String exprStr) {
        Expr expr = ExprUtils.parse(exprStr) ;
        try {
            NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
            fail("No exception raised") ;
        }
        catch (ExprEvalException ex) {}
    }
}
