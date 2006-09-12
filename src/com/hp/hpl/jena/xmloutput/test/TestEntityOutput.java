/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestEntityOutput.java,v 1.2 2006-09-12 14:01:45 chris-dollin Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import java.io.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Tests for entities being created corresponding to prefixes.
    @author kers
*/
public class TestEntityOutput extends ModelTestBase
    {
    public TestEntityOutput( String name )
        { super( name ); }
    
    public void testPlaceHolder()
        {
        Model m = createMemModel();
        m.setNsPrefix( "rdf", RDF.getURI() );
        StringWriter s = new StringWriter();
        m.write( s, "RDF/XML-ABBREV" );
        Model m2 = modelWithStatements( "" );
        m2.read( new StringReader( s.toString() ), null, "RDF/XML" );
        assertTrue( s.toString().contains( "<!DOCTYPE rdf:RDF [" ) );
        }
    }

