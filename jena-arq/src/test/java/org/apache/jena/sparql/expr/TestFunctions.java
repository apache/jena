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
import org.junit.Test ;

public class TestFunctions
{
    // Test of fn;* are in org.apache.jena.sparql.function.library.*
    
    private static final NodeValue TRUE     = NodeValue.TRUE ;
    private static final NodeValue FALSE    = NodeValue.FALSE ;
    
    @Test public void expr1() { test("1", NodeValue.makeInteger(1)) ; }

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

    @Test public void exprStrEnds10() { test("STRENDS('abc', 'abcd')", FALSE) ; }
    @Test public void exprStrEnds11() { test("STRENDS('abc'@en, 'bc')", TRUE) ; }
    @Test public void exprStrEnds12() { test("STRENDS('abc'^^xsd:string, 'c')", TRUE) ; }
    @Test public void exprStrEnds13() { test("STRENDS('abc'^^xsd:string, 'c'^^xsd:string)", TRUE) ; }
    @Test public void exprStrEnds14() { test("STRENDS('abc', 'ab'^^xsd:string)", FALSE) ; }
    @Test public void exprStrEnds15() { test("STRENDS('abc'@en, 'abc'@en)", TRUE) ; }
    
    @Test public void exprStrEnds16() { testEvalException("STRENDS('ab'@en, 'ab'@fr)") ; }
    @Test public void exprStrEnds17() { testEvalException("STRENDS(123, 'ab'@fr)") ; }
    @Test public void exprStrEnds18() { testEvalException("STRENDS('123'^^xsd:string, 12.3)") ; }

    @Test public void exprContains10() { test("Contains('abc', 'abcd')", FALSE) ; }
    @Test public void exprContains11() { test("Contains('abc'@en, 'bc')", TRUE) ; }
    @Test public void exprContains12() { test("Contains('abc'^^xsd:string, 'c')", TRUE) ; }
    @Test public void exprContains13() { test("Contains('abc'^^xsd:string, 'c'^^xsd:string)", TRUE) ; }
    @Test public void exprContains14() { test("Contains('abc', 'z'^^xsd:string)", FALSE) ; }
    @Test public void exprContains15() { test("Contains('abc'@en, 'abc'@en)", TRUE) ; }
    
    @Test public void exprContains16() { testEvalException("Contains('ab'@en, 'ab'@fr)") ; }
    @Test public void exprContains17() { testEvalException("Contains(123, 'ab'@fr)") ; }
    @Test public void exprContains18() { testEvalException("STRENDS('123'^^xsd:string, 12.3)") ; }

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

    // Better name!
    @Test public void localTimezone_2() { test("afn:timezone()", nv->nv.isDayTimeDuration()); }
    
    @Test public void localDateTime_1() { test("afn:nowtz()", nv-> nv.isDateTime()); }
    // Test field defined.
    @Test public void localDateTime_2() { test("afn:nowtz()", nv-> nv.getDateTime().getTimezone() >= -14 * 60 ); }

    @Test public void localDateTime_3() { test("afn:nowtz() = NOW()", NodeValue.TRUE); }
    

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
