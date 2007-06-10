/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.n3.RelURI;

/** com.hp.hpl.jena.query.util.test.TestCaseURI
 * 
 * @author Andy Seaborne
 * @version $Id: TestRelURI.java,v 1.4 2007-06-10 13:51:13 andy_seaborne Exp $
 */

public class TestRelURI extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestRelURI.class) ;
        ts.setName("TestURI") ;
        return ts ;
    }
    
    public void testCodec01()
    { execCodecTest("") ; }
    
    public void testCodec02()
    { execCodecTest("a") ; }

    public void testCodec03()
    { execCodecTest("a_") ; }

    public void testCodec04()
    { execCodecTest("_a") ; }

    public void testCodec05()
    { execCodecTest(" ") ; }

    public void testCodec06()
    { execCodecTest("a b") ; }

    public void testCodec07()
    { execCodecTest("a ") ; }

    public void testCodec08()
    { execCodecTest(" a") ; }

    public void testCodec09()
    { execCodecTest("__") ; }
    
    public void testCodec10()
    { execCodecTest("_20") ; }
    
    public void testCodec11()
    { execCodecTest("ab_20xy") ; }
    
    public void testCodec12()
    { execCodecTest("ab_5F20xy") ; }
    
    public void testCodec13()
    { execCodecTest("ab_5Fxy") ; }
    
    private void execCodecTest(String s)
    {
        String a = RelURI.CodecHex.encode(s) ;
        String b = RelURI.CodecHex.decode(a) ;
        assertEquals("Input: ("+s+") Encoded: ("+a+") Decoded: ("+b+")", s, b) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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