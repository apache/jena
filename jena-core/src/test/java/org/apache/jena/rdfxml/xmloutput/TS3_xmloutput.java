/*
t * Licensed to the Apache Software Foundation (ASF) under one
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

package org.apache.jena.rdfxml.xmloutput;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.test.X_RDFReaderF;

/**
 * JUnit regression tests for output
 */
public class TS3_xmloutput extends TestCase{

    /**
     * Answer a suite of all the tests defined here
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("RDF/XML Output");

        RDFReaderFImpl.alternative(new X_RDFReaderF());

        suite.addTestSuite( PrettyWriterTest.class );
        suite.addTest(new TestWriterInterface("testInterface", null));
        suite.addTest( TestWriterAndReader.suiteXML() );
        suite.addTest( TestWriterAndReader.suiteXML_ABBREV() );
        suite.addTest( TestWriterAndReader.suiteN_TRIPLE() );
        suite.addTestSuite( TestWriterURIExceptions.class );
        suite.addTestSuite( TestEntityOutput.class );
        suite.addTestSuite( TestLiteralEncoding.class );
        suite.addTestSuite( TestWriterFeatures.class ) ;
        suite.addTestSuite( TestWriterURIExceptions.class ) ;
        return suite;
    }
}
