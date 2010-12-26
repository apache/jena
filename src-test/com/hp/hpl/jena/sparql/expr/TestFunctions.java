/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.fail ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

public class TestFunctions
{
    private static final NodeValue INT_ZERO = NodeValue.makeInteger(0) ;
    private static final NodeValue INT_ONE  = NodeValue.makeInteger(1) ;
    private static final NodeValue INT_TWO  = NodeValue.makeInteger(2) ;
    private static final NodeValue TRUE     = NodeValue.TRUE ;
    private static final NodeValue FALSE    = NodeValue.FALSE ;
    
    @Test public void expr1() { test("1", NodeValue.makeInteger(1)) ; }

    @Test public void exprStrLen1() { test("fn:string-length('')", INT_ZERO) ; }
    @Test public void exprStrLen2() { test("fn:string-length('a')", INT_ONE) ; }

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
    
    @Test public void exprJavaSubstring1() { test("afn:substr('abc',0,0)", NodeValue.makeString("")) ; }
    @Test public void exprJavaSubstring2() { test("afn:substr('abc',0,1)", NodeValue.makeString("a")) ; }

    @Test public void exprJavaSubstring3() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,0)", NodeValue.makeString("")) ; }
    @Test public void exprJavaSubstring4() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,1)", NodeValue.makeString("a")) ; }
    
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
    
    
    static Node xyz_en = Node.createLiteral("xyz", "en", null) ;
    static NodeValue nv_xyz_en = NodeValue.makeNode(xyz_en) ;

    static Node xyz_xsd_string = Node.createLiteral("xyz", null, XSDDatatype.XSDstring) ;
    static NodeValue nv_xyz_string = NodeValue.makeNode(xyz_xsd_string) ;

    
    @Test public void exprStrLang1()     { test("strlang('xyz', 'en')",             nv_xyz_en) ; } 
    //@Test public void exprStrLang2()      { test("strlang('xyz', 'en')",             nv_xyz_en) ; } 

    @Test public void exprStrDatatype1()    { test("strdt('123', xsd:integer)",    NodeValue.makeInteger(123)) ; }
    @Test public void exprStrDatatype2()    { test("strdt('xyz', xsd:string)",     nv_xyz_string) ; }
    @Test public void exprStrDatatype3()    { testEvalException("strdt('123', 'datatype')") ; }
    
    static Node n_uri = Node.createURI("http://example/") ;
    static NodeValue nv_uri = NodeValue.makeNode(n_uri) ;
    
    @Test public void exprIRI1()            { test("iri('http://example/')", nv_uri ) ; }
    
    /*
    E_IRI
    E_BNode
    */ 
    
    //@Test public void 


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

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
