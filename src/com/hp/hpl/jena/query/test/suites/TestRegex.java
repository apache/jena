/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.test.suites;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.expr.E_Regex;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.NodeValue;

/** com.hp.hpl.jena.query.test.TestMisc
 * 
 * @author Andy Seaborne
 * @version $Id: TestRegex.java,v 1.5 2007/01/02 11:18:17 andy_seaborne Exp $
 */

public class TestRegex extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestRegex.class) ;
        ts.setName("TestRegex") ;
        if ( false )
            ARQ.getContext().set(ARQ.regexImpl, ARQ.xercesRegex) ;
        return ts ;
    }
    
    public void testRegex1() { regexTest("ABC", "ABC", null, true) ; }
    public void testRegex2() { regexTest("ABC", "abc", null, false) ; }
    public void testRegex3() { regexTest("ABC", "abc", "", false) ; }
    public void testRegex4() { regexTest("ABC", "abc", "i", true) ; }
    public void testRegex5() { regexTest("abc", "B", "i", true) ; }
    public void testRegex6() { regexTest("ABC", "^ABC", null, true) ; }
    public void testRegex7() { regexTest("ABC", "BC", null, true) ; }
    public void testRegex8() { regexTest("ABC", "^BC", null, false) ; }

    public void regexTest(String value, String pattern, String flags, boolean expected)
    {
        Expr s = NodeValue.makeString(value) ;
        
        E_Regex r = new E_Regex(s, pattern, flags) ;
        NodeValue nv = r.eval(null, null) ;
        boolean b = nv.getBoolean() ;
        if ( b != expected )
            fail(fmtTest(value, pattern, flags)+" ==> "+b+" expected "+expected) ;
    }

    private String fmtTest(String value, String pattern, String flags)
    {
        String tmp = "regex(\""+value+"\", \""+pattern+"\"" ;
        if ( flags != null )
            tmp = tmp + "\""+flags+"\"" ;
        tmp = tmp + ")" ;
        return tmp ; 
    }
    
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
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