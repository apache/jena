/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

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
    { fmtURI_Prefix("http://elsewhere/", "<http://elsewhere/>", pmap1) ; }

    public void test_URI_2() // Too short
    { fmtURI_Prefix("http://example/", "<http://example/>", pmap1) ; }
    
    public void test_URI_3() // No prefix mapping
    { fmtURI_Prefix("http://default/", "<http://default/>", (PrefixMapping)null) ; }

    public void test_URI_4()
    { fmtURI_Base("http://example/", "<http://example/>", (String)null ) ; }
    
    public void test_URI_5()
    { fmtURI_Base("http://example/x", "<x>", "http://example/") ; }

    public void test_URI_6()
    { fmtURI_Base("http://example/x", "<http://example/x>","http://example/ns#") ; }
    
    public void test_URI_7()
    { fmtURI_Base("http://example/ns#x", "<x>", "http://example/ns#") ; }
    
    public void test_URI_8()
    { fmtURI_Base("http://example/ns#x", "<#x>", "http://example/ns") ; }
    
    public void test_URI_9()
    { fmtURI_Base("http://example/x/y", "<y>", "http://example/x/") ; }
    
    public void test_URI_10()
    { fmtURI_Base("http://example/x/y", "<http://example/x/y>", "http://example/x") ; }
    
    public void test_URI_11()
    { fmtURI_Base("urn:x", "<urn:x>", "http://example/ns#") ; }
    
    public void test_URI_12()
    { fmtURI_Base("urn:x#foo", "<#foo>", "urn:x") ; }
    
    public void test_URI_13()
    { fmtURI_Base("urn:x/y", "<urn:x/y>",  "urn:x") ; }
    
    public void test_URI_14()
    { fmtURI_Base("urn:x:y", "<y>",  "urn:x:") ; }
    
    public void test_PName_1() 
    { fmtURI_Prefix("http://example/x#abc", "ex:abc", pmap1) ; }

    public void test_PName_2() 
    { fmtURI_Prefix("http://example/x#", "ex:", pmap1) ; }

    public void test_PName_3()
    { fmtURI_Prefix("http://default/x", ":x", pmap1) ; }

    public void test_PName_4()
    { fmtURI_Prefix("http://default/", ":", pmap1) ; }

    public void test_PName_5() // Prefixed names with a leading number in the local part
    { fmtURI_Prefix("http://default/0", ":0", pmap1) ; }
    
    public void test_PName_6()
    { fmtURI_Prefix("http://example/x#x-1", "ex:x-1", pmap1) ; }
    
    // Things that can't be prefixed names

    public void test_PName_Bad_1()
    { fmtURI_Prefix("http://other/x", "<http://other/x>", pmap1) ; }

    public void test_PName_Bad_2()
    { fmtURI_Prefix("http://other/x#a", "<http://other/x#a>", pmap1) ; }
    
    public void test_PName_Bad_3()
    { fmtURI_Prefix("http://example/x##", "<http://example/x##>", pmap1) ; }

    public void test_PName_Bad_4() 
    { fmtURI_Prefix("http://default/x#a", "<http://default/x#a>", pmap1) ; }

    public void test_PName_Bad_5() 
    { fmtURI_Prefix("http://default/#a", "<http://default/#a>", pmap1) ; }

    public void test_PName_Bad_6()
    { fmtURI_Prefix("http://example/x/a", "<http://example/x/a>", pmap1) ; }
    
    public void test_PName_Bad_7() 
    { fmtURI_Prefix("http://example/x.", "<http://example/x.>", pmap1) ; }
    
    // Dots
    public void test_Dots_1() // Internal DOT 
    { fmtURI_Prefix("http://example/x#a.b", "ex:a.b", pmap1) ; }
    
    public void test_Dots_2() // Trailing DOT
    { fmtURI_Prefix("http://example/x#a.b.", "<http://example/x#a.b.>", pmap1) ; }

    public void test_Dots_3() // Leading DOT
    { fmtURI_Prefix("http://example/x#.b", "<http://example/x#.b>", pmap1) ; }

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
    
    private void fmtURI_Prefix(String uriStr, String expected, PrefixMapping pmap)
    {
        String actual = FmtUtils.stringForURI(uriStr, pmap) ;
        //assertEquals(expected, actual) ;
        
        // Better error message this way?
        if ( ! expected.equals(actual) )
            fail(expected + " => " +actual) ;
    }
    
    private void fmtURI_Base(String uriStr, String expected, String base)
    {
        String actual = FmtUtils.stringForURI(uriStr, base, null) ;
        //assertEquals(expected, actual) ;
        
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