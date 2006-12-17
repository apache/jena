/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestLiteralEncoding.java,v 1.4 2006-12-17 19:24:03 andy_seaborne Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import java.io.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.CannotEncodeCharacterException;

/**
     Tests to ensure that certain literals are either encoded properly or reported
     as exceptions.
    @author kers
*/
public class TestLiteralEncoding extends ModelTestBase
    {
    public TestLiteralEncoding( String name )
        { super( name ); }
    
    public void testX()
        {
        assertEquals( "", Util.substituteEntitiesInElementContent( "" ) );
        assertEquals( "abc", Util.substituteEntitiesInElementContent( "abc" ) );
        assertEquals( "a&lt;b", Util.substituteEntitiesInElementContent( "a<b" ) );
        assertEquals( "a&gt;b", Util.substituteEntitiesInElementContent( "a>b" ) );
        assertEquals( "a&amp;b", Util.substituteEntitiesInElementContent( "a&b" ) );
        assertEquals( "a;b", Util.substituteEntitiesInElementContent( "a;b" ) );
        assertEquals( "a b", Util.substituteEntitiesInElementContent( "a b" ) );
        assertEquals( "a\nb", Util.substituteEntitiesInElementContent( "a\nb" ) );
        assertEquals( "a'b", Util.substituteEntitiesInElementContent( "a'b" ) );
    //
        assertEquals( "a&lt;b&lt;c", Util.substituteEntitiesInElementContent( "a<b<c" ) );
        assertEquals( "a&lt;b&gt;c", Util.substituteEntitiesInElementContent( "a<b>c" ) );
        assertEquals( "a&lt;b&amp;c", Util.substituteEntitiesInElementContent( "a<b&c" ) );
        assertEquals( "a&amp;b&amp;c", Util.substituteEntitiesInElementContent( "a&b&c" ) );
        assertEquals( "a&amp;b&gt;c", Util.substituteEntitiesInElementContent( "a&b>c" ) );
        assertEquals( "a&amp;b&lt;c", Util.substituteEntitiesInElementContent( "a&b<c" ) );
    //
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "&lt;", Util.substituteStandardEntities( "<" ) );
        assertEquals( "&gt;", Util.substituteStandardEntities( ">" ) );
        assertEquals( "&amp;", Util.substituteStandardEntities( "&" ) );
        assertEquals( "&apos;", Util.substituteStandardEntities( "\'" ) );
        assertEquals( "&quot;", Util.substituteStandardEntities( "\"" ) );
        assertEquals( "&#xA;", Util.substituteStandardEntities( "\n" ) );
        assertEquals( "&#xD;", Util.substituteStandardEntities( "\r" ) );
        assertEquals( "&#9;", Util.substituteStandardEntities( "\t" ) );
    //
        assertEquals( "a&lt;b&amp;c&gt;d", Util.substituteStandardEntities( "a<b&c>d" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        assertEquals( "", Util.substituteStandardEntities( "" ) );
        }
    
    public void testLexicalEncodingException()
        {
        for (char ch = 0; ch < 32; ch += 1) 
            if (ch != '\n' && ch != '\t' && ch != '\r')
                testThrowsBadCharacterException( ch );
        }

    private void testThrowsBadCharacterException( char badChar )
        {
        String badString = "" + badChar;
        try 
            { 
            Util.substituteEntitiesInElementContent( badString ); 
            fail( "should trap bad character: (char)" + (int) badChar ); 
            }
        catch (CannotEncodeCharacterException e) 
            { 
            assertEquals( badChar, e.getBadChar() ); 
            assertEquals( "XML", e.getEncodingContext() );
            }
        }
    
    public void testNoApparentCData()
        {
        Model m = modelWithStatements( "a R ']]>'" );
        StringWriter s = new StringWriter();
        m.write( s, "RDF/XML-ABBREV" );
        Model m2 = modelWithStatements( "" );
        m2.read( new StringReader( s.toString() ), null, "RDF/XML" );
        assertIsoModels( m, m2 );
        //assertTrue( s.toString().contains( "]]&gt;" ) );  // Java 1.5-ism
        //assertFalse( s.toString().contains( "]]>" ) );
        assertTrue( s.toString().indexOf( "]]&gt;" ) >= 0 );
        assertFalse( s.toString().indexOf( "]]>" ) >= 0 );
        }
    }

