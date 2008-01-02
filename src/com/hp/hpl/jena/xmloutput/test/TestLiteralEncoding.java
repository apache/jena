/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestLiteralEncoding.java,v 1.7 2008-01-02 12:06:48 andy_seaborne Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import java.io.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.CannotEncodeCharacterException;
import com.hp.hpl.jena.vocabulary.RDF;

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
    
    public void testLexicalEncodingException(String lang)
        {
        for (char ch = 0; ch < 32; ch += 1) 
            if (ch != '\n' && ch != '\t' && ch != '\r')
                testThrowsBadCharacterException( ch, lang );
        testThrowsBadCharacterException( (char)0xFFFF, lang );
        testThrowsBadCharacterException( (char)0xFFFE, lang );
        
        }
    
    
    public void testBasicLexicalEncodingException()
    {
    	testLexicalEncodingException("RDF/XML");
    }
    
    // TODO: add test for bad char in property attribute.
    public void testPrettyLexicalEncodingException()
    {
    	testLexicalEncodingException("RDF/XML-ABBREV");
    }
    private void testThrowsBadCharacterException( char badChar, String lang )
        {
        String badString = "" + badChar;

        Model m = ModelFactory.createDefaultModel();
        m.createResource().addProperty(RDF.value, badString);
        Writer w = new Writer(){
			public void close() throws IOException {}
			public void flush() throws IOException {}
			public void write(char[] arg0, int arg1, int arg2) throws IOException {}
        };
        try 
            { 
        	m.write(w,lang);
//            Util.substituteEntitiesInElementContent( badString ); 
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

/*
 *  (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 *
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
 */
