/*
    (c) Copyright 2001, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.
    [See end of file]
    $Id: TestPackage.java,v 1.13 2005-02-21 12:22:52 andy_seaborne Exp $
*/
package com.hp.hpl.jena.xmloutput.test;

// Imports
///////////////
import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.BadURIException;

import junit.framework.*;

/**
 * JUnit regression tests for output
 *
 * @author Jeremy Carroll
 * @version CVS info: $Id: TestPackage.java,v 1.13 2005-02-21 12:22:52 andy_seaborne Exp $,
 */
public class TestPackage {

    /**
     * Answer a suite of all the tests defined here
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        String langs[] =
            new String[] { "RDF/XML", "RDF/XML-ABBREV", 
                //
               "N-TRIPLE",
            //"N3" 
        };
        // add all the tests defined in this class to the suite
        /* */
        suite.addTest(new PrettyWriterTest("testAnonDamlClass"));
        /* */
        suite.addTest(new PrettyWriterTest("testLi"));
		suite.addTest(new PrettyWriterTest("test803804"));
        
    	//if ( true ) return suite;
        /* */
        suite.addTest(new PrettyWriterTest("testRDFCollection"));
        /* */
        suite.addTest(new PrettyWriterTest("testOWLPrefix"));
        /* */
        suite.addTest(new testWriterInterface("testInterface", null)); 
        /* */
        suite.addTest(new testWriterInterface("testNoWriter", null)); 
        /* */
        suite.addTest(new testWriterInterface("testAnotherWriter", null));
        /* */
        for (int i = 0; i < langs.length
              ; i++) {
            suite.addTest(testWriterAndReader.suite(langs[i]));
        }

        suite.addTest( new TestSuite( TestURIExceptions.class ) );
        return suite;
    }
    
    /**
         Added as a place to put the test(s) which ensure that thrown URI exceptions
         carry the bad URI with them. I (Chris) would embed them in the other tests,
         but I can't work out how to do so ...
        @author kers
    */
    public static class TestURIExceptions extends TestCase
        {
        public TestURIExceptions( String name )
            { super( name ); }
        
        public void testBadURIExceptionContainsBadURIInMessage()
            {
            String badURI = "http:";            
            Model m = ModelFactory.createDefaultModel();
            m.add( m.createResource( badURI ), m.createProperty( "eg:B C" ), m.createResource( "eg:C D" ) );
            try { m.write( new StringWriter() ); fail( "should detect bad URI " + badURI ); } 
            catch (BadURIException e) { assertTrue( "message must contain failing URI", e.getMessage().indexOf( badURI ) > 0 ); }
            }
        }

}

/*
 *  (c)   Copyright 2001,2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *    All rights reserved.
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
 *
 * $Id: TestPackage.java,v 1.13 2005-02-21 12:22:52 andy_seaborne Exp $
 */
