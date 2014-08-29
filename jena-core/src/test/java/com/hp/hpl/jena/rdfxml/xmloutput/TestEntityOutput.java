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

import java.io.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.rdfxml.xmloutput.impl.BaseXMLWriter ;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Tests for entities being created corresponding to prefixes.
*/
public class TestEntityOutput extends ModelTestBase
    {
    public TestEntityOutput( String name )
        { super( name ); }
    
    public void testSettingWriterEntityProperty()
        {
        FakeBaseWriter w = new FakeBaseWriter();
        assertEquals( false, w.getShowDoctypeDeclaration() );
        assertEquals( "false", w.setProperty( "showDoctypeDeclaration", "true" ) );
        assertEquals( true, w.getShowDoctypeDeclaration() );
        assertEquals( "true", w.setProperty( "showDoctypeDeclaration", "false" ) );
        assertEquals( false, w.getShowDoctypeDeclaration() );
    //
        assertEquals( "false", w.setProperty( "showDoctypeDeclaration", Boolean.TRUE ) );
        assertEquals( true, w.getShowDoctypeDeclaration() );
        assertEquals( "true", w.setProperty( "showDoctypeDeclaration", Boolean.FALSE ) );
        assertEquals( false, w.getShowDoctypeDeclaration() );
        }    
    
    public void testKnownEntityNames()
        {
        BaseXMLWriter w = new FakeBaseWriter();
        assertEquals( true, w.isPredefinedEntityName( "lt" ) );
        assertEquals( true, w.isPredefinedEntityName( "gt" ) );
        assertEquals( true, w.isPredefinedEntityName( "amp" ) );
        assertEquals( true, w.isPredefinedEntityName( "apos" ) );
        assertEquals( true, w.isPredefinedEntityName( "quot" ) );
    //
        assertEquals( false, w.isPredefinedEntityName( "alt" ) );
        assertEquals( false, w.isPredefinedEntityName( "amper" ) );
        assertEquals( false, w.isPredefinedEntityName( "tapost" ) );
        assertEquals( false, w.isPredefinedEntityName( "gte" ) );
    //
        assertEquals( false, w.isPredefinedEntityName( "rdf" ) );
        assertEquals( false, w.isPredefinedEntityName( "smerp" ) );
        assertEquals( false, w.isPredefinedEntityName( "nl" ) );
        assertEquals( false, w.isPredefinedEntityName( "acute" ) );
        }

    public void testRDFNamespaceMissing()
        {
        Model m = createMemModel();
        modelAdd( m, "x R fake:uri#bogus" );
        m.setNsPrefix( "spoo", "fake:uri#" );
        m.setNsPrefix( "eh", "eh:/" );
        String s = checkedModelToString( m );
        assertMatches( "<!DOCTYPE rdf:RDF \\[", s );
        assertMatches( "<!ENTITY spoo 'fake:uri#'>", s );
        assertMatches( "rdf:resource=\"&spoo;bogus\"", s );
        }
    public void testUsesEntityForPrefix()
        {
        Model m = modelWithStatements( "x R fake:uri#bogus" );
        m.setNsPrefix( "spoo", "fake:uri#" );
        m.setNsPrefix( "eh", "eh:/" );
        String s = checkedModelToString( m );
        assertMatches( "<!DOCTYPE rdf:RDF \\[", s );
        assertMatches( "<!ENTITY spoo 'fake:uri#'>", s );
        assertMatches( "rdf:resource=\"&spoo;bogus\"", s );
        }

    public void testCatchesBadEntities()
        {
        testCatchesBadEntity( "amp" );
        testCatchesBadEntity( "lt" );
        testCatchesBadEntity( "gt" );
        testCatchesBadEntity( "apos" );
        testCatchesBadEntity( "quot" );
        }
    
    /* Old code produced:
<!DOCTYPE rdf:RDF [
  <!ENTITY dd 'http://www.example.org/a"b#'>
  <!ENTITY ampersand 'http://www.example.org/a?a&b#'>
  <!ENTITY espace 'http://www.example.org/a%20space#'>
  <!ENTITY zz 'http://www.example.org/a'b#'>
  <!ENTITY rdf 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>]>
     * 
     */
    /**
     * See
     * http://www.w3.org/TR/xml/#NT-EntityValue
     * " & and % ' are all legal URI chars, but illegal
     * in entity defn.
     * @throws IOException 
     */
    public void testDifficultChars() throws IOException 
    {
        Model m = createMemModel();
        m.read("file:testing/abbreviated/entities.rdf");
        try ( StringWriter w = new StringWriter() ) {
            RDFWriter wr = m.getWriter();
            wr.setProperty("showDoctypeDeclaration", "true");
            wr.write(m, w, "http://example.org/");
            Reader r = new StringReader(w.toString());
            Model m2 = createMemModel();
            m2.read(r,"http://example.org/");
            assertIsoModels("showDoctypeDeclaration problem", m, m2);
        }
    }
    
    public void testCRinLiterals() 
    {
        Model m = createMemModel();
        Resource r = m.createResource("http://example/r") ;
        Property p = m.createProperty("http://example/p") ;
        m.add(r, p, "abc\r\nxyz") ;
        StringWriter w = new StringWriter();
        m.write(w) ;
        Model m2 = createMemModel();
        m2.read(new StringReader(w.toString()), null) ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }

    private void testCatchesBadEntity( String bad )
        {
        Model m = modelWithStatements( "ampsersand spelt '&'; x R goo:spoo/noo" );
        m.setNsPrefix( "rdf", RDF.getURI() );
        m.setNsPrefix( bad, "goo:spoo" );
        m.setNsPrefix( "eh", "eh:/" );
        String s = checkedModelToString( m );
        //assertTrue( s.toString().contains( "<!DOCTYPE rdf:RDF [" ) ); // java5-ism
        assertTrue( s.toString().contains( "<!DOCTYPE rdf:RDF [" ) );
        assertMismatches( "<!ENTITY " + bad + " ", s );
        assertMismatches( "rdf:resource=\"&" + bad + ";noo\"", s );
        }

    private void checkModelFromXML( Model shouldBe, String s )
        {
        Model m = createMemModel();
        m.read( new StringReader( s ), null, "RDF/XML" );
        assertIsoModels( "model should be read back correctly", shouldBe, m );
        }

    private String checkedModelToString( Model m )
        {
        String result = modelToString( m );
        checkModelFromXML( m, result );
        return result;
        }

    private String modelToString( Model m )
        {
        StringWriter s = new StringWriter();
        RDFWriter w = m.getWriter( "RDF/XML-ABBREV" );
        w.setProperty( "showDoctypeDeclaration", Boolean.TRUE );
        w.write( m, s, null );
        return s.toString();
        }
    
    private void assertMatches( String pattern, String x )
        {
        if (!x.matches( "(?s).*(" + pattern + ").*" ) )
                fail( "pattern {" + pattern + "} does not match string {" + x + "}" );
        }
    
    private void assertMismatches( String pattern, String x )
        {
        if (x.matches( "(?s).*(" + pattern + ").*" ) )
                fail( "pattern {" + pattern + "} should not match string {" + x + "}" );
        }
    
    private final static class FakeBaseWriter extends BaseXMLWriter
        {
        @Override
        protected void unblockAll() {}

        @Override
        protected void blockRule( Resource r ) {}

        @Override
        protected void writeBody( Model mdl, PrintWriter pw, String baseUri, boolean inclXMLBase ) {}

        protected boolean getShowDoctypeDeclaration() { return showDoctypeDeclaration.booleanValue(); }
        }
    }
