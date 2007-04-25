/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.sse.RDFTermFactory;
import com.hp.hpl.jena.sparql.sse.SSEParseException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestRDFTermFactory extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestRDFTermFactory.class) ;
        ts.setName("TestMisc") ;
        return ts ;
    }
    
    public void testLit_01() { test("'foo'") ; } 
    public void testLit_02() { test("\"foo\"") ; } 
    public void testLit_03() { test("''") ; }
    public void testLit_04() { test("\"\"") ; }
    public void testLit_05() { test("'foo'@en") ; } 
    public void testLit_06() { testBad("'foo' @en") ; } 
    public void testLit_07() { testBad("'") ; }
    public void testLit_08() { testBad("'\"") ; }
    public void testLit_09() { testBad("'''") ; } 
    public void testLit_10() { testBad("''@") ; }
    public void testLit_11() { testBad("abc") ; }
    
    public void testNum_1() { test("1") ; }
    public void testNum_2() { test("1.1") ; }
    public void testNum_3() { test("1.0e6") ; }
    
    
    public void testNum_5() { testBad("1 1") ; }
 
    public void testURI_1() { test("<http://example/base>") ; }
    public void testURI_2() { testBad("http://example/base") ; }
    public void testURI_3() { testBad("<http://example/ space>") ; }
    
    private void test(String str)
    {
        Node node = RDFTermFactory.parseString(str) ;
    }
    
    private void testBad(String str)
    {
        try {
            Node node = RDFTermFactory.parseString(str) ;
            fail("Did not get a parse failure") ;
        } catch (SSEParseException ex)
        {}
    }

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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