/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestEntityOutput.java,v 1.5 2006-09-14 17:14:50 chris-dollin Exp $
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
    
    public void testUsing()
        {
//        Model m = modelWithStatements( "x rdf:type rdf:Property" );
//        m.setNsPrefix(  "foo", "eh:/" );
//        m.write( System.out, "RDF/XML" );
        }
    
    public void testEntityProperty()
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

