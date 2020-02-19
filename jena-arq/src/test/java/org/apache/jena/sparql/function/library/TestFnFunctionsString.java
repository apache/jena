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

import static org.apache.jena.sparql.expr.NodeValue.FALSE;
import static org.apache.jena.sparql.expr.NodeValue.TRUE;
import static org.apache.jena.sparql.expr.NodeValue.nvONE;
import static org.apache.jena.sparql.expr.NodeValue.nvZERO;
import static org.apache.jena.sparql.expr.LibTestExpr.test;
import static org.junit.Assert.fail;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.LibTestExpr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sys.JenaSystem ;
import org.junit.Test ;

public class TestFnFunctionsString {

    static { JenaSystem.init(); }

    @Test public void exprStrLen1() { test("fn:string-length('')", nvZERO) ; }
    @Test public void exprStrLen2() { test("fn:string-length('a')", nvONE) ; }
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

    @Test public void exprStrStart0() { test("fn:starts-with('abc', '')", TRUE) ; }
    @Test public void exprStrStart1() { test("fn:starts-with('abc', 'a')", TRUE) ; }
    @Test public void exprStrStart2() { test("fn:starts-with('abc', 'ab')", TRUE) ; }
    @Test public void exprStrStart3() { test("fn:starts-with('abc', 'abc')", TRUE) ; }
    @Test public void exprStrStart4() { test("fn:starts-with('abc', 'abcd')", FALSE) ; }

        @Test public void exprStrEnds0() { test("fn:ends-with('abc', '')", TRUE) ; }
    @Test public void exprStrEnds1() { test("fn:ends-with('abc', 'c')", TRUE) ; }
    @Test public void exprStrEnds2() { test("fn:ends-with('abc', 'bc')", TRUE) ; }
    @Test public void exprStrEnds3() { test("fn:ends-with('abc', 'abc')", TRUE) ; }
    @Test public void exprStrEnds4() { test("fn:ends-with('abc', 'zabc')", FALSE) ; }

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

    @Test public void exprFnReplace01()  { test("fn:replace('abc', 'b', 'Z')", NodeValue.makeString("aZc")) ; }
    @Test public void exprFnReplace02()  { test("fn:replace('abc', 'b.', 'Z')", NodeValue.makeString("aZ")) ; }
    @Test public void exprFnReplace03()  { test("fn:replace('abcbd', 'b.', 'Z')", NodeValue.makeString("aZZ")) ; }

    @Test public void exprFnReplace04()  { test("fn:replace('abcbd'^^xsd:string, 'b.', 'Z')", NodeValue.makeNode("aZZ", XSDDatatype.XSDstring)) ; }
    @Test public void exprFnReplace05()  { test("fn:replace('abcbd'@en, 'b.', 'Z')", NodeValue.makeNode("aZZ", "en", (String)null)) ; }
    @Test public void exprFnReplace06()  { test("fn:replace('abcbd', 'B.', 'Z', 'i')", NodeValue.makeString("aZZ")) ; }


    // Bad pattern : dynamic (eval time) exception.
    // The pattern for fn:replace is not compiled on build - if that changes, this test will fail.
    // See exprReplace14.
    @Test
    public void exprReplace15() {
        testEvalException("fn:replace('abc', '^(a){-9}', 'ABC')");
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
