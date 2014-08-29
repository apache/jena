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

package com.hp.hpl.jena.rdfxml.xmloutput;

// Imports
///////////////
import java.io.StringWriter;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.BadURIException;

/**
 * JUnit regression tests for output
 */
public class TestPackage extends TestCase{

    /**
     * Answer a suite of all the tests defined here
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest( TestMacEncodings.suite() );
        // add all the tests defined in this class to the suite
        /* */
        suite.addTestSuite( PrettyWriterTest.class );
        suite.addTest(new TestWriterInterface("testInterface", null)); 
        /* */
        suite.addTest(new TestWriterInterface("testNoWriter", null)); 
        /* */
        suite.addTest(new TestWriterInterface("testAnotherWriter", null));
        /* */
        if (false) suite.addTest( BigAbbrevTestPackage.suite() ); // TODO may be obsolete. Ask Jeremy.
        suite.addTest( testWriterAndReader.suiteXML() );
        suite.addTest( testWriterAndReader.suiteXML_ABBREV() );
        suite.addTest( testWriterAndReader.suiteN_TRIPLE() );
        suite.addTestSuite( TestURIExceptions.class );
        suite.addTestSuite( TestEntityOutput.class );
        suite.addTestSuite( TestLiteralEncoding.class );
        suite.addTestSuite( TestWriterFeatures.class ) ;
        return suite;
    }
    
    /**
         Added as a place to put the test(s) which ensure that thrown URI exceptions
         carry the bad URI with them.
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
