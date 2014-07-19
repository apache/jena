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

package com.hp.hpl.jena.n3;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.n3.JenaURIException;
@SuppressWarnings("deprecation")
public class TestResolver extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestResolver.class) ;
        ts.setName("TestURI") ;
        return ts ;
    }
    
    public void testBase1()
    {
        IRIResolver resolver = new IRIResolver() ;
        assertNotNull(resolver.getBaseIRI()) ;
        String base = resolver.getBaseIRI() ;
        assertTrue(base.indexOf(':') > 0 ) ; 
    }
    
    public void testBase2()
    {
        IRIResolver resolver = new IRIResolver("x") ;
        assertNotNull(resolver.getBaseIRI()) ;
        // Active when IRI library integrated - currently the resolver takes a raw base string.
//        String base = resolver.getBaseIRI() ;
//        assertTrue(base.indexOf(':') > 0 ) ; 
    }

    public void testBase3()
    {
        String b = IRIResolver.resolveGlobal("x") ;
        IRIResolver resolver = new IRIResolver(b) ;
        assertNotNull(resolver.getBaseIRI()) ;
        String base = resolver.getBaseIRI() ;
        assertTrue(base.indexOf(':') > 0 ) ; 
    }
    
    public void testBadBase1() {
    	execException("%G",JenaURIException.class);
    }
    public void testBadBase2() {
    	execException("/%G",JenaURIException.class);
    }
    public void testBadBase3() {
    	execException("file:/%/",JenaURIException.class);
    }
    public void testBadBase4() {
    	execException("http://example.org/%",JenaURIException.class);
    }

    public void testBadChoice1() {
    	chooseException("%G",JenaURIException.class);
    }
    public void testBadChoice2() {
    	chooseException("/%G",JenaURIException.class);
    }
    public void testBadChoice3() {
    	chooseException("file:/%/",JenaURIException.class);
    }
    public void testChoice1() {
    	choose("file:a");
    }
    public void testChoice2() {
    	choose("file:a");
    }
    // ---- Basic
    
    public void testURI_1()   { execTest("", "http://example.org/", "http://example.org/"); }
    public void testURI_2()   { execTest("", "http://example.org/xyz_2007", "http://example.org/xyz_2007"); }
    public void testURI_3()   { execTest("", "http://example.org/xyz 2007", "http://example.org/xyz 2007"); }
    public void testURI_4()   { execTest("", "http://example.org/xyz__2007", "http://example.org/xyz__2007"); }
    public void testURI_5()   { execTest("", "http://example.org/xyz__abc", "http://example.org/xyz__abc"); }
    
    // ---- Relative URIs
    
    public void testURI_relX_1()  { execTest("x", "http://example.org/ns", "http://example.org/x"); }

    public void testURI_relX_2()  { execTest("x", "http://example.org/", "http://example.org/x"); }
    
    public void testURI2_relHashX_1()  { execTest("#x", "http://example.org/ns", "http://example.org/ns#x"); }
    
    public void testURI2_relHashX_2()  { execTest("#x", "http://example.org/ns/", "http://example.org/ns/#x"); }

    public void testURI_blank_1()  { execTest("", "http://example.org/ns", "http://example.org/ns") ; }

    public void testURI_blank_2()  { execTest("", "http://example.org/ns/", "http://example.org/ns/") ; }

    public void testURI_hash_1()  { execTest("#", "http://example.org/ns", "http://example.org/ns#") ; }

    public void testURI_hash_2()  { execTest("#", "http://example.org/ns/", "http://example.org/ns/#") ; }

    public void testBaseHash_1() { execTest("x", "http://example.org/ns#", "http://example.org/x") ; }

    public void testBaseHash_2() { execTest("x", "http://example.org#", "http://example.org/x") ; }
    
    public void testBaseHash_3() { execTest("#", "base:x", "base:x#") ; }

//    // Java5: exception
//    // Java6 & GNUclasspath: correctly get "base:#"
//    public void testBaseHash_4() { execTest("#", "base:", "base:#") ; }

    public void testScheme_1() { execTest("x", "base:", "base:x") ; }

    public void testScheme_2() { execTest("/x", "base:", "base:/x") ; }

    public void testScheme_3() { execTestMatch("x", "file:", "^file:///.*/x$") ; }
    
    public void testScheme_4() { execTestMatch("file:x", null, "^file:///.*/x$") ; }
    
//    public void testURI_file_1()  { execTestMatch("file:x", "http://example.org/ns", "^file:///.*/x$") ; }

    public void testURI_file_2()  { execTest("x", "file:///A/B/C", "file:///A/B/x") ; }

    public void testURI_file_3()  { execTest("x", "file:///A/B/", "file:///A/B/x") ; }


    // ---- Absolute URIs are left alone 
    
    public void testURI_abs_1()   { execTest("http://host/x", "http://example.org/ns", "http://host/x") ; }
    
    public void testURI_abs_2()   { execTest("file:///x", "http://example.org/ns", "file:///x") ; }

    public void testURI_abs_3()   { execTest("tag:foo", "http://example.org/ns", "tag:foo") ; }

    public void testURI_abs_4()   { execTest("tag:/foo", "http://example.org/ns", "tag:/foo") ; }

    public void testURI_abs_5()   { execTest("tag:/foo/", "http://example.org/ns", "tag:/foo/") ; }

    public void testURI_abs_6()   { execTest("scheme99:/foo/", "http://example.org/ns", "scheme99:/foo/") ; }
    
    // Null base
    
    public void testURI_nullBase_1()   { execTest("scheme99:/foo/", null, "scheme99:/foo/") ; }
    
    // resolved against current directory
    public void testURI_nullBase_2()   { execTestMatch("foo", null, ".*/foo") ; }

    // ---- Hierarchical URIs

    public void testHierURI_1()   { execTest("../foo", "file:///dir/file", "file:///foo") ; }

    public void testHierURI_2()   { execTest("../foo", "http://host/dir/file.html", "http://host/foo") ; }
    
    public void testHierURI_3()   { execTest("../foo", "http://host/dir/", "http://host/foo") ; }

    public void testHierURI_4()   { execTest("../foo", "http://host/", "http://host/foo") ; }
    
    public void testHierURI_5()   { execTest("../foo", "http://host/xyz", "http://host/foo") ; }
    
    public void testHierURI_6()   { execTest(".", "http://host/xyz", "http://host/") ; }

    public void testHierURI_7()   { execTest(".", "http://host/xyz/", "http://host/xyz/") ; }
    
    public void testHierURI_8()   { execTest(".", "http://host/", "http://host/") ; }
    
    public void testHierURI_9()   { execTest(".", "file:///dir/file", "file:///dir/") ; }
    // ---- File URIs
    
    public void testFileURI_1()   { execFileTest("file:///foo", "file:///foo") ; }

    public void testFileURI_2()   { execFileTest("file://foo", "file://foo") ; }

    public void testFileURI_3()   { execFileTest("file:/foo", "file:///foo") ; }

    
 // Bad.
    public void testBad_1()   { execException("%G", "http://example.org/", JenaURIException.class); }
    public void testBad_2()   { execException("foo", "http://example.org/%HH", JenaURIException.class); }
    public void testBad_3()   { execException("bar", "http://example.org/%3", JenaURIException.class); }
    
    
    public void testBaseEmpty() { execTestMatch("x", "", "^file:///.*/x$") ; }

    // Resolved against current directory.
    public void testBaseNull() { execTestMatch("x", null, ".*/x" ) ; }

    public void testRelBase_1() {execTestMatch("x", "ns", ".*/x" );  }
    
    public void testRelBase_2() { execTestMatch("x", "/ns", ".*/x" ); }

    // ---- Opaque
    
    public void testURI_opaque_1()  { execTest("#x", "tag:A", "tag:A#x") ; }

    public void testURI_opaque_2()  { execTest("#x", "urn:x-jena:A", "urn:x-jena:A#x") ; }
    
//    public void testURI_opaque_3()  { execException("#x", "urn:x-jena:A", RelativeURIException.class) ; }

    // ---- Opaque file URLs
    
    // Should these be errors? Yes.
    //public void testURI_file_4()  { execTest("x", "file:A", "file:Ax") ; }
    public void testURI_file_4()  {  execTestMatch("x", "file:A","^file:///.*/x") ;}
    
    public void testURI_file_5()  { execTestMatch("#x", "file:A","^file:///.*/A#x") ; }
    //public void testURI_file_5()  { execException("#x", "file:A", RelativeURIException.class) ; }
    
    //public void testURI_file_6()   { execTest("foo", "file:///xyz abc/", "file:///xyz abc/foo" ) ; }
    
    public void testURI_file_7()   { execTestMatch("file:foo", "file:xyz", "^file:///.*foo$") ; }

    public void testURI_file_8()   { execTestMatch("file:foo", "file:a b", "^file:///.*foo$") ; }
    
    
    // File URLs - test aren't exact as the depend where they are run.
    
    public void testFileURI_rel_1() { execTestFileRelURI("file:foo") ; }
    
    public void testFileURI_rel_2() { execTestFileRelURI("file:foo/bar") ; }
    
    public void testFileURI_rel_3() { execTestFileRelURI("file:foo/") ; }
    
    public void testFileURI_rel_4() { execTestFileRelURI("file:foo/bar/") ; }
    

    private void execTest(String u, String base, String result)
    {
        IRIResolver resolver = new IRIResolver(base) ;
        String res = resolver.resolve(u) ;

        if (result == null )
        {
            assertNull("("+u+","+base+") => <null> :: Got: "+res, res) ;
            return ;
        }
        
        assertNotNull("("+u+","+base+") => "+result+" :: Got: <null>", res) ;
        assertTrue("("+u+","+base+") => "+result+" :: Got: "+res, res.equals(result)) ;
    }
    
    // A test for resolved names that depend on where the tests are run.
    private void execTestMatch(String u, String base, String resultPattern)
    {
        IRIResolver resolver = new IRIResolver(base) ;
        String res = resolver.resolve(u) ;

        if (resultPattern == null )
        {
            assertNull("("+u+","+base+") => <null> :: Got: "+res, res) ;
            return ;
        }
        
        boolean r = res.matches(resultPattern) ;
        assertTrue("Does not match: "+res+" -- "+resultPattern, r) ;
    }
    
    private void execFileTest(String fn1, String fn2)
    {
        String s = IRIResolver.resolveFileURL(fn1) ;
        assertEquals(s,fn2) ;
    }
    
    private void execTestFileRelURI(String fn)
    {
        String relName = fn.substring("file:".length()) ;
        String s = IRIResolver.resolveFileURL(fn) ;
        assertTrue("Lost relative name: ("+fn+"=>"+s+")", s.endsWith(relName) ) ;
        assertTrue("Not absolute: ("+fn+"=>"+s+")", s.startsWith("file:///") ) ;
    }
    
    private void execException(String u, String base, Class<?> ex)
    {
        String s = ex.getSimpleName() ;
        try {
            IRIResolver resolver = new IRIResolver(base) ;
            String res = resolver.resolve(u) ;
            if ( res == null )
                fail("("+u+","+base+") => <null> :: Expected exception: " +s) ;
            else
                fail("("+u+","+base+") => "+res+" :: Expected exception: " +s) ;
        } catch (Exception ex2)
        {
            // Shoudl test whether ex2 is a subclass of ex
            assertEquals(ex, ex2.getClass()) ;
        }
    }
    private void execException(String base, Class<?> ex)
    {
        String s = ex.getSimpleName() ;
        try {
            new IRIResolver(base) ;
                 fail("("+base+") => OK :: Expected exception: " +s) ;
        } catch (Exception ex2)
        {
            // Shoudl test whether ex2 is a subclass of ex
            assertEquals(ex, ex2.getClass()) ;
        }
    }
    private void choose(String base)
    {
        
            IRIResolver.chooseBaseURI(base) ;
        
    }
    private void chooseException(String base, Class<?> ex)
    {
        String s = ex.getSimpleName() ;
        try {
            IRIResolver.chooseBaseURI(base) ;
                 fail("("+base+") => OK :: Expected exception: " +s) ;
        } catch (Exception ex2)
        {
            // Shoudl test whether ex2 is a subclass of ex
            assertEquals(ex, ex2.getClass()) ;
        }
    }
//    private void execTestGlobal(String u, String result)
//    {
//        String res = IRIResolver.resolveGlobal(u) ;
//        if (result == null )
//        {
//            assertNull("("+u+") => <null> :: Got: "+res, res) ;
//            return ;
//        }
//        
//        assertNotNull("("+u+") => "+result+" :: Got: <null>", res) ;
//        assertTrue("("+u+") => "+result+" :: Got: "+res, res.equals(result)) ;
//    }
}
