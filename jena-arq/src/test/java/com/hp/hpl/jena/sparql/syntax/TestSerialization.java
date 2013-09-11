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

package com.hp.hpl.jena.sparql.syntax;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;


public class TestSerialization extends BaseTest
{
    static PrefixMapping pmap1 = new PrefixMappingImpl() ;
    {
        pmap1.setNsPrefix("", "http://default/") ;
        pmap1.setNsPrefix("ex", "http://example/x#") ;
        pmap1.setNsPrefix("x", "x:") ;
    }
    
    // Simple stuff
    @Test public void test_URI_1() // Not in the map
    { fmtURI_Prefix("http://elsewhere/", "<http://elsewhere/>", pmap1) ; }

    @Test public void test_URI_2() // Too short
    { fmtURI_Prefix("http://example/", "<http://example/>", pmap1) ; }
    
    @Test public void test_URI_3() // No prefix mapping
    { fmtURI_Prefix("http://default/", "<http://default/>", (PrefixMapping)null) ; }

    @Test public void test_URI_4()
    { fmtURI_Base("http://example/", (String)null, "<http://example/>" ) ; }
    
    @Test public void test_URI_5()
    { fmtURI_Base("http://example/x", "http://example/", "<x>") ; }

    @Test public void test_URI_6()
    { fmtURI_Base("http://example/x", "http://example/ns#","<x>") ; }
    
    @Test public void test_URI_7()
    { fmtURI_Base("http://example/ns#x", "http://example/ns#", "<#x>") ; }
    
    @Test public void test_URI_8()
    { fmtURI_Base("http://example/ns#x", "http://example/ns", "<#x>") ; }
    
    @Test public void test_URI_9()
    { fmtURI_Base("http://example/x/y", "http://example/x/", "<y>") ; }
    
    @Test public void test_URI_10()
    { fmtURI_Base("http://example/x/y", "http://example/x", "<x/y>") ; }
    
    @Test public void test_URI_11()
    { fmtURI_Base("http://example/x/y", "http://example/", "<x/y>") ; }

//    @Test public void test_URI_12()
//    { fmtURI_Base("http://example/x/y", "http://example/z", "<x/y>") ; }

    @Test public void test_PName_1() 
    { fmtURI_Prefix("http://example/x#abc", "ex:abc", pmap1) ; }

    @Test public void test_PName_2() 
    { fmtURI_Prefix("http://example/x#", "ex:", pmap1) ; }

    @Test public void test_PName_3()
    { fmtURI_Prefix("http://default/x", ":x", pmap1) ; }

    @Test public void test_PName_4()
    { fmtURI_Prefix("http://default/", ":", pmap1) ; }

    @Test public void test_PName_5() // Prefixed names with a leading number in the local part
    { fmtURI_Prefix("http://default/0", ":0", pmap1) ; }
    
    @Test public void test_PName_6()
    { fmtURI_Prefix("http://example/x#x-1", "ex:x-1", pmap1) ; }
    
    // Things that can't be prefixed names

    @Test public void test_PName_Bad_1()
    { fmtURI_Prefix("http://other/x", "<http://other/x>", pmap1) ; }

    @Test public void test_PName_Bad_2()
    { fmtURI_Prefix("http://other/x#a", "<http://other/x#a>", pmap1) ; }
    
    @Test public void test_PName_Bad_3()
    { fmtURI_Prefix("http://example/x##", "<http://example/x##>", pmap1) ; }

    @Test public void test_PName_Bad_4() 
    { fmtURI_Prefix("http://default/x#a", "<http://default/x#a>", pmap1) ; }

    @Test public void test_PName_Bad_5() 
    { fmtURI_Prefix("http://default/#a", "<http://default/#a>", pmap1) ; }

    @Test public void test_PName_Bad_6()
    { fmtURI_Prefix("http://example/x/a", "<http://example/x/a>", pmap1) ; }
    
    @Test public void test_PName_Bad_7() 
    { fmtURI_Prefix("http://example/x.", "<http://example/x.>", pmap1) ; }
    
    // Dots
    @Test public void test_Dots_1() // Internal DOT 
    { fmtURI_Prefix("http://example/x#a.b", "ex:a.b", pmap1) ; }
    
    @Test public void test_Dots_2() // Trailing DOT
    { fmtURI_Prefix("http://example/x#a.b.", "<http://example/x#a.b.>", pmap1) ; }

    @Test public void test_Dots_3() // Leading DOT
    { fmtURI_Prefix("http://example/x#.b", "<http://example/x#.b>", pmap1) ; }

    @Test public void testQueryPattern1()
    { test("SELECT * { ?s ?p ?o }", 
           "SELECT * { ?s ?p ?o }",
           true) ;
    }
    
    @Test public void testQueryPattern2()
    { test("SELECT * { ?s ?p ?o }", 
           "SELECT *       { ?s ?p ?o }",
           true) ;
    }
    
    @Test public void testQueryComment1()
    { test("SELECT * { ?s ?p ?o }", 
           "SELECT *  # Comment\n" +
           " { ?s ?p ?o }",
           true) ;
    }
    
    @Test public void testQuery1()
    { test("SELECT * { ?s ?p ?o . [] ?p ?o }",
           "SELECT ?x { ?s ?p ?o . [] ?p ?o }",
           false) ;
    }
    
    @Test public void testQueryExpr1()
    { test("SELECT * { ?s ?p ?o . FILTER (?o)}", 
           "SELECT * { ?s ?p ?o   FILTER (?o)}", // No DOT - same
           true) ;
    }
    
    @Test public void testQueryExpr2()
    { test("SELECT * { FILTER (?x = 3)}", 
           "SELECT * { FILTER (?x = 3)}",
           true) ;
    }

    @Test public void testQueryExpr3()
    { test("SELECT * { FILTER (?x != 3)}", 
           "SELECT * { FILTER (?x = 3)}",
           false) ;
    }
    
    @Test public void testQueryExpr4()
    { test("SELECT * { FILTER (?z && ?x != 3)}", 
           "SELECT * { FILTER (?z && ?x = 3)}",
           false) ;
    }
    
    @Test public void testOpToSyntax_01()
    {
        testOpToSyntax("(bgp (triple ?s ?p ?o))", "SELECT * {?s ?p ?o}") ;
    }
    
    @Test public void testOpToSyntax_02()
    {
        testOpToSyntax("(bgp (triple ?s ?p ?o) (<urn:x> <urn:p> <urn:z>) )", 
                       "SELECT * {?s ?p ?o . <urn:x> <urn:p> <urn:z> }") ;
    }

    @Test public void testOpToSyntax_03()
    {
        testOpToSyntax("(table unit)", 
                       "SELECT * {}") ;
    }

    @Test public void testOpToSyntax_04()
    {
        testOpToSyntax("(leftjoin (bgp (triple ?s ?p ?o)) (bgp (triple ?a ?b ?c)))",
                       "SELECT * { ?s ?p ?o OPTIONAL { ?a ?b ?c }}") ;
    }

    @Test public void testOpToSyntax_05()
    {
        testOpToSyntax("(leftjoin (bgp (triple ?s ?p ?o)) (bgp (triple ?a ?b ?c)) (> ?z 5))",
                       "SELECT * { ?s ?p ?o OPTIONAL { ?a ?b ?c FILTER(?z > 5) }}") ;
    }
    
    @Test public void testOpToSyntax_06()
    {
        testOpToSyntax("(path ?s (path* <urn:p>) ?o)",
                       "SELECT * { ?s <urn:p>* ?o }") ;
    }
    
    @Test public void testOpToSyntax_07()
    {
        testOpToSyntax("(path ?s (path? (alt (path+ <urn:p1>) <urn:p2>)) ?o)" ,
                       "SELECT * { ?s (<urn:p1>+|<urn:p2>)? ?o }") ;
    }

    private void testOpToSyntax(String opStr, String queryString)
    {
        Op op = SSE.parseOp(opStr) ;
        Query queryConverted = OpAsQuery.asQuery(op) ;
        
        Query queryExpected = QueryFactory.create(queryString, queryConverted.getSyntax()) ;
        
//        if ( ! queryExpected.equals(queryConverted) )
//        {
//            System.err.println("Query Expected: "+queryExpected.getSyntax()) ;
//            System.err.println(queryExpected) ;
//            
//            System.err.println("Query Converted: "+queryConverted.getSyntax()) ;
//            System.err.println(queryConverted) ;
//            System.err.println() ;
//        }
        
        assertEquals(queryExpected, queryConverted) ;
    }

    private  void test(String qs1, String qs2, boolean result)
    {
        Query q1 = null ;
        Query q2 = null ;
        try { q1 = QueryFactory.create(qs1, Syntax.syntaxSPARQL) ; }
        catch (Exception ex) { fail("Building query 1") ; }
        
        try { q2 = QueryFactory.create(qs2, Syntax.syntaxSPARQL) ; }
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
    
    private void fmtURI_Base(String uriStr, String base, String expected)
    {
        String actual = FmtUtils.stringForURI(uriStr, base, null) ;
        //assertEquals(expected, actual) ;
        
        // Better error message this way?
        if ( ! expected.equals(actual) )
            fail(uriStr + "["+base+"] => Got: "+actual+" Expected: "+expected) ;
    }
}
