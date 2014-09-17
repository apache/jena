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

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.fail ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

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
    @Test public void exprReplace13()  { testEvalException("REPLACE('abc', '.*', '$1')") ; }

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
    
    //@Test public void exprStrJoin()      { test("fn:string-join('a', 'b')", NodeValue.makeString("ab")) ; }
    
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
    
    
    static Node xyz_en = NodeFactory.createLiteral("xyz", "en", null) ;
    static NodeValue nv_xyz_en = NodeValue.makeNode(xyz_en) ;

    static Node xyz_xsd_string = NodeFactory.createLiteral("xyz", null, XSDDatatype.XSDstring) ;
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
    
    private void test(String exprStr, NodeValue result)
    {
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, FunctionEnvBase.createTest()) ;
        assertEquals(result, r) ;
    }
    
    private void testEvalException(String exprStr)
    {
        Expr expr = ExprUtils.parse(exprStr) ;
        try {
             NodeValue r = expr.eval(null, FunctionEnvBase.createTest()) ;
             fail("No exception raised") ;
        } catch (ExprEvalException ex) {}
            
    }
}
