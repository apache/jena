/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class TS_Functions extends TestCase
{
	static final String testSetName = "Functions" ;

    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TS_Functions.class) ;
        ts.setName(testSetName) ;
        return ts ;
    }

    private static final NodeValue INT_ZERO = NodeValue.makeInteger(0) ;
    private static final NodeValue INT_ONE  = NodeValue.makeInteger(1) ;
    private static final NodeValue INT_TWO  = NodeValue.makeInteger(2) ;
    private static final NodeValue TRUE     = NodeValue.TRUE ;
    private static final NodeValue FALSE     = NodeValue.FALSE ;

    
    public void test1() { test("1", NodeValue.makeInteger(1)) ; }

    public void testStrLen1() { test("fn:string-length('')", INT_ZERO) ; }
    public void testStrLen2() { test("fn:string-length('a')", INT_ONE) ; }

    // F&O strings are one-based, ansd substring takes a length
    public void testSubstring1() { test("fn:substring('',0)", NodeValue.makeString("")) ; }
    public void testSubstring2() { test("fn:substring('',1)", NodeValue.makeString("")) ; }
    public void testSubstring3() { test("fn:substring('',1,0)", NodeValue.makeString("")) ; }
    public void testSubstring4() { test("fn:substring('',1,1)", NodeValue.makeString("")) ; }

    public void testSubstring5() { test("fn:substring('abc',1)", NodeValue.makeString("abc")) ; }
    public void testSubstring6() { test("fn:substring('abc',2)", NodeValue.makeString("bc")) ; }
    public void testSubstring7() { test("fn:substring('a',1,1)", NodeValue.makeString("a")) ; }
    public void testSubstring8() { test("fn:substring('a',1,2)", NodeValue.makeString("a")) ; }
    public void testSubstring9() { test("fn:substring('a',0)", NodeValue.makeString("a")) ; }
    
    // Uses round()
    public void testSubstring10() { test("fn:substring('abc',1.6,1.33)", NodeValue.makeString("b")) ; }
    // This test was added because the test suite had 1199 tests in. 
    public void testSubstring11() { test("fn:substring('abc',-1, -15.3)", NodeValue.makeString("")) ; }
    
    public void testJavaSubstring1() { test("afn:substr('abc',0,0)", NodeValue.makeString("")) ; }
    public void testJavaSubstring2() { test("afn:substr('abc',0,1)", NodeValue.makeString("a")) ; }

    public void testJavaSubstring3() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,0)", NodeValue.makeString("")) ; }
    public void testJavaSubstring4() { test("<"+ARQConstants.ARQFunctionLibrary+"substr>('abc',0,1)", NodeValue.makeString("a")) ; }
    
    public void testStrStart0() { test("fn:starts-with('abc', '')", TRUE) ; }
    public void testStrStart1() { test("fn:starts-with('abc', 'a')", TRUE) ; }
    public void testStrStart2() { test("fn:starts-with('abc', 'ab')", TRUE) ; }
    public void testStrStart3() { test("fn:starts-with('abc', 'abc')", TRUE) ; }
    public void testStrStart4() { test("fn:starts-with('abc', 'abcd')", FALSE) ; }
    
    public void testStrEnds0() { test("fn:ends-with('abc', '')", TRUE) ; }
    public void testStrEnds1() { test("fn:ends-with('abc', 'c')", TRUE) ; }
    public void testStrEnds2() { test("fn:ends-with('abc', 'bc')", TRUE) ; }
    public void testStrEnds3() { test("fn:ends-with('abc', 'abc')", TRUE) ; }
    public void testStrEnds4() { test("fn:ends-with('abc', 'zabc')", FALSE) ; }
    
    public void testStrCase1() { test("fn:lower-case('aBc')", NodeValue.makeString("abc")) ; }
    public void testStrCase2() { test("fn:lower-case('abc')", NodeValue.makeString("abc")) ; }
    public void testStrCase3() { test("fn:upper-case('abc')", NodeValue.makeString("ABC")) ; }
    public void testStrCase4() { test("fn:upper-case('ABC')", NodeValue.makeString("ABC")) ; }
    

    public void testStrContains0() { test("fn:contains('abc', '')", TRUE) ; }
    public void testStrContains1() { test("fn:contains('abc', 'a')", TRUE) ; }
    public void testStrContains2() { test("fn:contains('abc', 'b')", TRUE) ; }
    public void testStrContains3() { test("fn:contains('abc', 'c')", TRUE) ; }
    
    public void testStrContains4() { test("fn:contains('abc', 'ab')", TRUE) ; }
    public void testStrContains5() { test("fn:contains('abc', 'bc')", TRUE) ; }
    public void testStrContains6() { test("fn:contains('abc', 'abc')", TRUE) ; }
    public void testStrContains7() { test("fn:contains('abc', 'Xc')", FALSE) ; }
    public void testStrContains8() { test("fn:contains('abc', 'Xa')", FALSE) ; }
    
    public void testBoolean1()    { test("fn:boolean('')", FALSE) ; }
    public void testBoolean2()    { test("fn:boolean(0)", FALSE) ; }
    public void testBoolean3()    { test("fn:boolean(''^^xsd:string)", FALSE) ; }
    
    public void testBoolean4()    { test("fn:boolean('X')", TRUE) ; }
    public void testBoolean5()    { test("fn:boolean('X'^^xsd:string)", TRUE) ; }
    public void testBoolean6()    { test("fn:boolean(1)", TRUE) ; }

    public void testBoolean7()    { test("fn:not('')", TRUE) ; }
    public void testBoolean8()    { test("fn:not('X')", FALSE) ; }
    public void testBoolean9()    { test("fn:not(1)", FALSE) ; }
    public void testBoolean10()   { test("fn:not(0)", TRUE) ; }
    
    public void testStrJoin()      { test("fn:string-join('a', 'b')", NodeValue.makeString("ab")) ; }
    
    public void testSameTerm1()     { test("sameTerm(1,1)",           TRUE) ; }
    public void testSameTerm2()     { test("sameTerm(1,1.0)",         FALSE) ; }
    public void testSameTerm3()     { test("sameTerm(1,1e0)",         FALSE) ; }
    public void testSameTerm4()     { test("sameTerm(<_:a>, <_:a>)",  TRUE) ; }
    public void testSameTerm5()     { test("sameTerm(<x>, <x>)",      TRUE) ; }
    public void testSameTerm6()     { test("sameTerm(<x>, <y>)",      FALSE) ; }

    private void test(String exprStr, NodeValue result)
    {
        Expr expr = ExprUtils.parse(exprStr) ;
        NodeValue r = expr.eval(null, FunctionEnvBase.createTest()) ;
        assertEquals(result, r) ;
    }
    
    private void testEvalExceptiion(String exprStr)
    {
        Expr expr = ExprUtils.parse(exprStr) ;
        try {
             NodeValue r = expr.eval(null, FunctionEnvBase.createTest()) ;
             fail("No exception raised") ;
        } catch (ExprEvalException ex) {}
            
    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
