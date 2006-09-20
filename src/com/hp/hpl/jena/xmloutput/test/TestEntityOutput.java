/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestEntityOutput.java,v 1.7 2006-09-20 13:56:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import java.io.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.xmloutput.impl.BaseXMLWriter;

/**
    Tests for entities being created corresponding to prefixes.
    @author kers
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

    private void testCatchesBadEntity( String bad )
        {
        Model m = modelWithStatements( "ampsersand spelt '&'; x R goo:spoo/noo" );
        m.setNsPrefix( "rdf", RDF.getURI() );
        m.setNsPrefix( bad, "goo:spoo" );
        m.setNsPrefix( "eh", "eh:/" );
        String s = checkedModelToString( m );
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
        protected void unblockAll() {}

        protected void blockRule( Resource r ) {}

        protected void writeBody( Model mdl, PrintWriter pw, String baseUri, boolean inclXMLBase ) {}

        protected boolean getShowDoctypeDeclaration() { return showDoctypeDeclaration.booleanValue(); }
        }
    }

