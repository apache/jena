/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.test.suites;

import com.hp.hpl.jena.query.util.RelURI ;
import com.hp.hpl.jena.query.util.RelativeURIException;
import com.hp.hpl.jena.query.util.JenaURIException;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** com.hp.hpl.jena.query.util.test.TestCaseURI
 * 
 * @author Andy Seaborne
 * @version $Id: TestURI.java,v 1.6 2007/01/02 11:18:17 andy_seaborne Exp $
 */

public class TestURI extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestURI.class) ;
        ts.setName("TestURI") ;
        return ts ;
    }
    
    // ---- Basic
    
    public void testURI()   { execTest("", "http://example.org/", "http://example.org/"); }
    
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
    
    public void testBaseHash_3() { execException("#", "base:x", RelativeURIException.class) ; }

//    // Java5: exception
//    // Java6 & GNUclasspath: correctly get "base:#"
//    public void testBaseHash_4() { execTest("#", "base:", "base:#") ; }

    public void testScheme_1() { execTest("x", "base:", "base:x") ; }

    public void testScheme_2() { execTest("/x", "base:", "base:/x") ; }

    public void testScheme_3() { execTestMatch("x", "file:", "^file:///.*/x$") ; }
    
    public void testScheme_4() { execTestMatch("file:x", null, "^file:///.*/x$") ; }
    
    public void testURI_file_1()  { execTestMatch("file:x", "http://example.org/ns", "^file:///.*/x$") ; }

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
    
    public void testURI_nullBase_2()   { execException("foo", null, JenaURIException.class) ; }
    

    // ---- Hierarchical URIs

    public void testHierURI_1()   { execTest("../foo", "file:///dir/file", "file:///foo") ; }

    public void testHierURI_2()   { execTest("../foo", "http://host/dir/file.html", "http://host/foo") ; }
    
    public void testHierURI_3()   { execTest("../foo", "http://host/dir/", "http://host/foo") ; }

    public void testHierURI_4()   { execTest("../foo", "http://host/", "http://host/../foo") ; }
    
    public void testHierURI_5()   { execTest("../foo", "http://host/xyz", "http://host/../foo") ; }
    
    public void testHierURI_6()   { execTest(".", "http://host/xyz", "http://host/") ; }

    public void testHierURI_7()   { execTest(".", "http://host/xyz/", "http://host/xyz/") ; }
    
    public void testHierURI_8()   { execTest(".", "http://host/", "http://host/") ; }
    
    public void testHierURI_9()   { execTest(".", "file:///dir/file", "file:///dir/") ; }
    // ---- File URIs
    
    public void testFileURI_1()   { execFileTest("file:///foo", "file:///foo") ; }

    public void testFileURI_2()   { execFileTest("file://foo", "file://foo") ; }

    public void testFileURI_3()   { execFileTest("file:/foo", "file:///foo") ; }

    // ---- Error conditions
    
    public void testBaseEmpty() { execException("x", "", JenaURIException.class) ; }

    public void testBaseNull() { execException("x", null, JenaURIException.class) ; }

    public void testRelBase_1() { execException("x", "ns", RelativeURIException.class) ; }
    
    public void testRelBase_2() { execException("x", "/ns", RelativeURIException.class) ; }

    // ---- Opaque
    
    public void testURI_opaque_1()  { execException("#x", "tag:A", RelativeURIException.class) ; }

    public void testURI_opaque_2()  { execException("#x", "urn:x-jena:A", RelativeURIException.class) ; }
    
    public void testURI_opaque_3()  { execException("#x", "urn:x-jena:A", RelativeURIException.class) ; }

    // ---- Opaque file URLs
    
    // Should these be errors? Yes.
    //public void testURI_file_4()  { execTest("x", "file:A", "file:Ax") ; }
    public void testURI_file_4()  { execException("x", "file:A", RelativeURIException.class) ; }
    
    public void testURI_file_5()  { execTest("#x", "file:A", "file:A#x") ; }
    //public void testURI_file_5()  { execException("#x", "file:A", RelativeURIException.class) ; }
    
    //public void testURI_file_6()   { execTest("foo", "file:///xyz abc/", "file:///xyz abc/foo" ) ; }
    
    public void testURI_file_7()   { execTestMatch("file:foo", "file:xyz", "^file:///.*foo$") ; }

    public void testURI_file_8()   { execTestMatch("file:foo", "file:a b", "^file:///.*foo$") ; }
    
    
    // File URLs - test aren't exact as the depend where they are run.
    
    public void testFileURI_rel_1() { execTestFileRelURI("file:foo") ; }
    
    public void testFileURI_rel_2() { execTestFileRelURI("file:foo/bar") ; }
    
    public void testFileURI_rel_3() { execTestFileRelURI("file:foo/") ; }
    
    public void testFileURI_rel_4() { execTestFileRelURI("file:foo/bar/") ; }
    
    // ---- Test global base
    
    public void testURI_global_null_1()
    {
        String tmp = RelURI.getBaseURI() ;
        try {
            RelURI.setBaseURI("rel") ;
            execTestGlobal("x", "rel1/rel2") ;
            fail("Didn't get RelativeURIException") ;
        } catch (JenaURIException ex) {}
        RelURI.setBaseURI(tmp) ;
    }
    
    public void testURI_global_null_2()
    {
        String tmp = RelURI.getBaseURI() ;
        RelURI.setBaseURI("file:///A/B/") ;
        execTestGlobal("x", "file:///A/B/x") ;
        RelURI.setBaseURI(tmp) ;
    }
    
    // ---- Workers
    
    private void execTest(String u, String base, String result)
    {
        String res = RelURI.resolve(u, base) ;
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
        String res = RelURI.resolve(u, base) ;

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
        String s = RelURI.resolveFileURL(fn1) ;
        assertEquals(s,fn2) ;
    }
    
    private void execTestFileRelURI(String fn)
    {
        String relName = fn.substring("file:".length()) ;
        String s = RelURI.resolveFileURL(fn) ;
        assertTrue("Lost relative name: ("+fn+"=>"+s+")", s.endsWith(relName) ) ;
        assertTrue("Not absolute: ("+fn+"=>"+s+")", s.startsWith("file:///") ) ;
    }
    
    private void execException(String u, String base, Class ex)
    {
        // 1.5.0-ism
        //String s = ex.getSimpleName() ;
        String s = ex.getName() ;
        
        // Tidy it up.
        int i = s.lastIndexOf('.') ;
        if ( i >= 0 )
            s = s.substring(i+1) ;
        
        try {
            String res = RelURI.resolve(u, base) ;
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
    
    private void execTestGlobal(String u, String result)
    {
        String res = RelURI.resolve(u) ;
        if (result == null )
        {
            assertNull("("+u+") => <null> :: Got: "+res, res) ;
            return ;
        }
        
        assertNotNull("("+u+") => "+result+" :: Got: <null>", res) ;
        assertTrue("("+u+") => "+result+" :: Got: "+res, res.equals(result)) ;
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