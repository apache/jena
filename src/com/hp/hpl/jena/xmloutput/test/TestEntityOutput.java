/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestEntityOutput.java,v 1.6 2006-09-20 09:51:25 chris-dollin Exp $
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
    
    public void testPlaceHolder()
        {
        Model m = createMemModel();
        m.setNsPrefix( "rdf", RDF.getURI() );
        StringWriter s = new StringWriter();
        RDFWriter w = m.getWriter( "RDF/XML-ABBREV" );
        w.setProperty( "showDoctypeDeclaration", Boolean.TRUE );
        w.write( m, s, null );
        // m.write( s, "RDF/XML-ABBREV" );
        Model m2 = modelWithStatements( "" );
        m2.read( new StringReader( s.toString() ), null, "RDF/XML" );
        assertTrue( s.toString().contains( "<!DOCTYPE rdf:RDF [" ) );
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
    
    public void testCatchesBadEntities()
        {
        Model m = modelWithStatements( "ampsersand spelt '&'" );
        m.setNsPrefix( "rdf", RDF.getURI() );
        m.setNsPrefix( "amp", "goo:spoo" );
        m.setNsPrefix( "eh", "eh:/" );
        StringWriter s = new StringWriter();
        RDFWriter w = m.getWriter( "RDF/XML-ABBREV" );
        w.setProperty( "showDoctypeDeclaration", Boolean.TRUE );
        w.write( m, s, null );
        // m.write( s, "RDF/XML-ABBREV" );
        // System.err.println( ">> " + s.toString() );
        Model m2 = modelWithStatements( "" );
        m2.read( new StringReader( s.toString() ), null, "RDF/XML" );
        assertTrue( s.toString().contains( "<!DOCTYPE rdf:RDF [" ) );
        assertIsoModels( m, m2 );
        }
    
    public void testEntityPropertySetting()
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
    
    private final static class FakeBaseWriter extends BaseXMLWriter
        {
        protected void unblockAll() {}

        protected void blockRule( Resource r ) {}

        protected void writeBody( Model mdl, PrintWriter pw, String baseUri, boolean inclXMLBase ) {}

        protected boolean getShowDoctypeDeclaration() { return showDoctypeDeclaration.booleanValue(); }
        }
    }

