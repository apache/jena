/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test.suites;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.Utils;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestSerialization extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestSerialization.class) ;
        ts.setName(Utils.classShortName(TestSerialization.class)) ;
        return ts ;
    }
    
    PrefixMapping pmap1 = new PrefixMappingImpl() ;
    {
        pmap1.setNsPrefix("", "http://default/") ;
        pmap1.setNsPrefix("ex", "http://example/x#") ;
        pmap1.setNsPrefix("x", "x:") ;
    }
    
    // Simple stuff
    public void test_URI_1() // Not in the map
    { fmtURI("http://elsewhere/", "<http://elsewhere/>", pmap1) ; }

    public void test_URI_2() // Too short
    { fmtURI("http://example/", "<http://example/>", pmap1) ; }
    
    public void test_QName_1() 
    { fmtURI("http://example/x#abc", "ex:abc", pmap1) ; }

    public void test_QName_2() 
    { fmtURI("http://example/x#", "ex:", pmap1) ; }

    public void test_QName_3()
    { fmtURI("http://default/x", ":x", pmap1) ; }

    public void test_QName_4()
    { fmtURI("http://default/", ":", pmap1) ; }

    public void test_QName_5() // Prefixed names with a leading number in the local part
    { fmtURI("http://default/0", "<http://default/0>", pmap1) ; }
    
    public void test_QName_6()
    { fmtURI("http://example/x#_1", "ex:_1", pmap1) ; }
    
    
    // Dots
    public void test_Dots_1() // Internal DOT 
    { fmtURI("http://example/x#a.b", "ex:a.b", pmap1) ; }
    
    public void test_Dots_2() // Trailing DOT
    { fmtURI("http://example/x#a.b.", "<http://example/x#a.b.>", pmap1) ; }

    public void test_Dots_3() // Leading DOT
    { fmtURI("http://example/x#.b", "<http://example/x#.b>", pmap1) ; }

    // Things that can't be qnames
    
    public void test_URI_NoQName_1() // local part wrong 
    { fmtURI("http://example/x#-", "<http://example/x#->", pmap1) ; }

    public void test_URI_NoQName_2() // local part wrong
    { fmtURI("http://example/x#-a", "<http://example/x#-a>", pmap1) ; }
    
    public void test_URI_NoQName_3() 
    { fmtURI("http://example/x#.", "<http://example/x#.>", pmap1) ; }
    
    public void testQueryPattern1()
    { test("SELECT * { ?s ?p ?o }", 
           "SELECT * { ?s ?p ?o }",
           true) ;
    }
    
    public void testQueryPattern2()
    { test("SELECT * { ?s ?p ?o }", 
           "SELECT *       { ?s ?p ?o }",
           true) ;
    }
    
    public void testQueryComment1()
    { test("SELECT * { ?s ?p ?o }", 
           "SELECT *  # Comment\n" +
           " { ?s ?p ?o }",
           true) ;
    }
    
    public void testQuery1()
    { test("SELECT * { ?s ?p ?o . [] ?p ?o }",
           "SELECT ?x { ?s ?p ?o . [] ?p ?o }",
           false) ;
    }
    
    public void testQueryExpr1()
    { test("SELECT * { ?s ?p ?o . FILTER (?o)}", 
           "SELECT * { ?s ?p ?o   FILTER (?o)}", // No DOT - same
           true) ;
    }
    
    public void testQueryExpr2()
    { test("SELECT * { FILTER (?x = 3)}", 
           "SELECT * { FILTER (?x = 3)}",
           true) ;
    }

    public void testQueryExpr3()
    { test("SELECT * { FILTER (?x != 3)}", 
           "SELECT * { FILTER (?x = 3)}",
           false) ;
    }
    
    public void testQueryExpr4()
    { test("SELECT * { FILTER (?z && ?x != 3)}", 
           "SELECT * { FILTER (?z && ?x = 3)}",
           false) ;
    }
    
    private  void test(String qs1, String qs2, boolean result)
    {
        Query q1 = null ;
        Query q2 = null ;
        try { q1 = QueryFactory.create(qs1) ; }
        catch (Exception ex) { fail("Building query 1") ; }
        
        try { q2 = QueryFactory.create(qs2) ; }
        catch (Exception ex) { fail("Building query 2") ; }

        boolean b = false ;
        try { b = q1.equals(q2) ; }
        catch (Exception ex) { 
            ex.printStackTrace(System.err) ;
            
            fail("Evaluating .equals") ; }
            
        if ( result )
            assertTrue(b) ;
         else
            assertFalse(b) ;
         
    }
    
    private void fmtURI(String uriStr, String expected, PrefixMapping pmap)
    {
        String actual = FmtUtils.stringForURI(uriStr, pmap) ;
        assertEquals(expected, actual) ;
        
        // Better error message this way?
        if ( ! expected.equals(actual) )
            fail(expected + " => " +actual) ;
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