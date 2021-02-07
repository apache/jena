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

package jena;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;

import java.io.ByteArrayOutputStream ;
import java.io.OutputStream ;
import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

@SuppressWarnings("deprecation")
public class Test_rdfcat
{
    // Switch off the banner during testing.
    @BeforeClass
    public static void setUp() {
        jena.rdfcat.suppressDeprecationBanner = true ;
    }

    @AfterClass
    public static void tearDown() {
        jena.rdfcat.suppressDeprecationBanner = false ;
    }

    @Test
    public void testAbbreviationTable()
        {
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "x" ) );
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "rdf" ) );
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "rdfxml" ) );
        assertEquals( "RDF/XML", jena.rdfcat.unabbreviate.get( "xml" ) );
        assertEquals( "N3", jena.rdfcat.unabbreviate.get( "n3" ) );
        assertEquals( "N3", jena.rdfcat.unabbreviate.get( "n" ) );
        assertEquals( "N3", jena.rdfcat.unabbreviate.get( "ttl" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.unabbreviate.get( "ntriples" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.unabbreviate.get( "ntriple" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.unabbreviate.get( "t" ) );
        assertEquals( "RDF/XML-ABBREV", jena.rdfcat.unabbreviate.get( "owl" ) );
        assertEquals( "RDF/XML-ABBREV", jena.rdfcat.unabbreviate.get( "abbrev" ) );
        }

    @Test
    public void testExistingLanguage()
        {
        assertEquals( "RDF/XML", jena.rdfcat.getCheckedLanguage( "x" ) );
        assertEquals( "RDF/XML", jena.rdfcat.getCheckedLanguage( "xml" ) );
        assertEquals( "RDF/XML-ABBREV", jena.rdfcat.getCheckedLanguage( "owl" ) );
        assertEquals( "N3", jena.rdfcat.getCheckedLanguage( "N3" ) );
        assertEquals( "N-TRIPLE", jena.rdfcat.getCheckedLanguage( "N-TRIPLE" ) );
        }

    /**
     * Test the identity transform - RDF/XML to RDF/XML
     */
    @Test
    public void testRdfcatIdentity() {
        Model source = ModelFactory.createDefaultModel();
        source.read( "file:testing/cmd/rdfcat.xml", "RDF/XML" );

        OutputStream so = new ByteArrayOutputStream();

        rdfcatFixture rc = new rdfcatFixture( so );
        rc.testGo( new String[] {"file:testing/cmd/rdfcat.xml"} );

        Model output = asModel( so, "RDF/XML" );
        assertTrue( output.isIsomorphicWith( source ));
    }

    /**
     * Test the basic concatenation
     */
    @Test
    public void testRdfcatConcat() {
        Model source = ModelFactory.createDefaultModel();
        source.read( "file:testing/cmd/rdfcat.xml", "RDF/XML" );

        OutputStream so = new ByteArrayOutputStream();

        rdfcatFixture rc = new rdfcatFixture( so );
        rc.testGo( new String[] {"file:testing/cmd/rdfcat_1.xml", "file:testing/cmd/rdfcat_2.xml"} );

        Model output = asModel( so, "RDF/XML" );
        assertTrue( output.isIsomorphicWith( source ));
    }

    /**
     * Change the default input language
     */
    @Test
    public void testRdfcatConcat1() {
        Model source = ModelFactory.createDefaultModel();
        source.read( "file:testing/cmd/rdfcat.xml", "RDF/XML" );

        OutputStream so = new ByteArrayOutputStream();

        rdfcatFixture rc = new rdfcatFixture( so );
        rc.testGo( new String[] {"-in", "N3", "file:testing/cmd/rdfcat_1_n3", "file:testing/cmd/rdfcat_2_n3"} );

        Model output = asModel( so, "RDF/XML" );
        assertTrue( output.isIsomorphicWith( source ));
    }

    @Test
    public void testRdfcatN3ToRDFXML_0() {
        doTestRdfcatOutput( "-n", "file:testing/cmd/rdfcat.n3", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatN3ToRDFXML_1() {
        doTestRdfcatOutput( "-n3", "file:testing/cmd/rdfcat.n3", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatN3ToRDFXML_2() {
        doTestRdfcatOutput( "-ttl", "file:testing/cmd/rdfcat.n3", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatN3ToRDFXML_3() {
        doTestRdfcatOutput( "-N3", "file:testing/cmd/rdfcat.n3", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatN3ToNtriple() {
        doTestRdfcatOutput( "-n", "file:testing/cmd/rdfcat.n3", "N-TRIPLE", "N-TRIPLE" );
    }

    @Test
    public void testRdfcatN3ToN3() {
        doTestRdfcatOutput( "-n", "file:testing/cmd/rdfcat.n3", "N3", "N3" );
    }

    @Test
    public void testRdfcatN3ToRDFXMLDefault() {
        doTestRdfcatOutput( null, "file:testing/cmd/rdfcat.n3", "RDF/XML", "RDF/XML" );
    }


    @Test
    public void testRdfcatRDFXMLToRDFXML_0() {
        doTestRdfcatOutput( "-x", "file:testing/cmd/rdfcat.xml", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatRDFXMLToRDFXML_1() {
        doTestRdfcatOutput( "-xml", "file:testing/cmd/rdfcat.xml", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatRDFXMLToRDFXML_2() {
        doTestRdfcatOutput( "-rdfxml", "file:testing/cmd/rdfcat.xml", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatRDFXMLToRDFXML_3() {
        doTestRdfcatOutput( "-rdf", "file:testing/cmd/rdfcat.xml", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatRDFXMLToNtriple() {
        doTestRdfcatOutput( "-x", "file:testing/cmd/rdfcat.xml", "N-TRIPLE", "N-TRIPLE" );
    }

    @Test
    public void testRdfcatRDFXMLToN3() {
        doTestRdfcatOutput( "-x", "file:testing/cmd/rdfcat.xml", "N3", "N3" );
    }

    @Test
    public void testRdfcatRDFXMLToRDFXMLDefault() {
        doTestRdfcatOutput( null, "file:testing/cmd/rdfcat.xml", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatNtripleToRDFXML_0() {
        doTestRdfcatOutput( "-t", "file:testing/cmd/rdfcat.nt", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatNtripleToRDFXML_1() {
        doTestRdfcatOutput( "-ntriple", "file:testing/cmd/rdfcat.nt", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatNtripleToRDFXML_2() {
        doTestRdfcatOutput( "-ntriples", "file:testing/cmd/rdfcat.nt", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatNtripleToRDFXML_3() {
        doTestRdfcatOutput( "-n-triple", "file:testing/cmd/rdfcat.nt", "RDF/XML", "RDF/XML" );
    }

    @Test
    public void testRdfcatNtripleToNtriple() {
        doTestRdfcatOutput( "-t", "file:testing/cmd/rdfcat.nt", "N-TRIPLE", "N-TRIPLE" );
    }

    @Test
    public void testRdfcatNtripleToN3() {
        doTestRdfcatOutput( "-t", "file:testing/cmd/rdfcat.nt", "N3", "N3" );
    }

    @Test
    public void testRdfcatNtripleToRDFXMLDefault() {
        doTestRdfcatOutput( null, "file:testing/cmd/rdfcat.nt", "RDF/XML", "RDF/XML" );
    }

    /**
     * Utility to do a basic cat operation
     */
    public static void doTestRdfcatOutput( String inFormArg, String inputArg, String outFormArg, String parseAs ) {
        Model source = ModelFactory.createDefaultModel();
        source.read( "file:testing/cmd/rdfcat.xml" );

        OutputStream so = new ByteArrayOutputStream();

        rdfcatFixture rc = new rdfcatFixture( so );

        List<String> l = new ArrayList<>();
        if (outFormArg != null) {
            l.add(  "-out" );
            l.add(  outFormArg );
        }
        if (inFormArg != null) l.add( inFormArg );
        l.add( inputArg );

        String[] args = new String[l.size()];
        for (int i = 0; i < l.size(); i++) {
            args[i] = l.get(i);
        }
        // use file extension guessing
        rc.testGo( args );

        Model output = asModel( so, parseAs );
        assertTrue( output.isIsomorphicWith( source ));
    }

    /** Convert an output stream holding a model content to a model */
    protected static Model asModel( OutputStream so, String syntax ) {
        String out = so.toString();
        Model output = ModelFactory.createDefaultModel();
        output.read( new StringReader( out ), "http://example.com/foo", syntax );
        return output;
    }

    /**
     * A minimal extension to rdfcat to provide a test fixture
     */
    protected static class rdfcatFixture
        extends rdfcat
    {
        private OutputStream m_so;
        protected rdfcatFixture( OutputStream so ) {
            m_so = so;
        }
        @Override
        protected OutputStream getOutputStream() {
            return m_so;
        }
        protected void testGo( String[] args ) {
            go( args );
        }
    }
}
