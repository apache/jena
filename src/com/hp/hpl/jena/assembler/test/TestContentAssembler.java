/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestContentAssembler.java,v 1.3 2006-01-06 11:04:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.ContentAssembler;
import com.hp.hpl.jena.assembler.exceptions.UnknownEncodingException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

public class TestContentAssembler extends AssemblerTestBase
    {
    protected static String Testing = "testing/assemblers";
    
    public TestContentAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return ContentAssembler.class; }

    public void testContentAssemblerType()
        { testDemandsMinimalType( new ContentAssembler(), JA.Content );  }
    
    public void testContentVocabulary()
        {
        assertSubclassOf( JA.Content, JA.Object );
        assertSubclassOf( JA.ContentItem, JA.Content );
        // assertSubclassOf( JA.LiteralContent, JA.Content );
        }
    
    public void testContent()
        {
        Assembler a = new ContentAssembler();
        Content c = (Content) a.open( resourceInModel( "x rdf:type ja:Content" ) );
        assertNotNull( c );
        Model m = ModelFactory.createDefaultModel();
        c.fill( m );
        assertEquals( 0, m.size() );
        }
    
    public void testMultipleLiteralsWorks()
        {
        Assembler a = new ContentAssembler();
        String A = "<eh:/A> a <eh:/Type>.".replaceAll( " ", "\\\\s" );
        String B = "<eh:/Type> a rdfs:Class.".replaceAll( " ", "\\\\s" );
        Resource root = resourceInModel( "x rdf:type ja:Content; x rdf:type ja:LiteralContent; x ja:literalContent '" + A + "'; x ja:literalContent '" + B + "'" );
        Content C = (Content) a.open( root ); 
        assertIsoModels( model( "Type rdf:type rdfs:Class; A rdf:type Type" ), C.fill( model( "" ) ) );
        }
    
    public void testN3StringContentSingleTriples()
        {
        testStringContent( "_x rdf:value '17'xsd:integer", "_:x rdf:value 17 ." );
        testStringContent( "_x rdf:value '42'xsd:integer", "_:x rdf:value 42 ." );
        testStringContent( "_x rdfs:label 'cosmetic'", "_:x rdfs:label 'cosmetic' ." );
        testStringContent( "_x owl:sameAs spoo", "_:x owl:sameAs <eh:/spoo> ." );
        testStringContent( "_x rdf:value '17'xsd:something", "_:x rdf:value '17'^^xsd:something ." );
        testStringContent( "_x dc:title 'A\\sTitle'", "_:x dc:title 'A Title' ." );
        }
    
    public void testN3StringContentMultipleTriples()
        {
        testStringContent( "x rdf:value 5; y owl:sameAs x", "<eh:/x> rdf:value 5 . <eh:/y> owl:sameAs <eh:/x> ." );
        }
    
    public void testRDFXMLContent()
        {
        Assembler a = new ContentAssembler();
        String Stuff = "<owl:Class></owl:Class>".replaceAll( " ", "\\\\s" );
        Resource root = resourceInModel( "x rdf:type ja:Content; x rdf:type ja:LiteralContent; x ja:literalContent '" + Stuff + "'; x ja:contentEncoding 'RDF/XML'" );
        Content c = (Content) a.open( root );
        assertIsoModels( model( "_x rdf:type owl:Class" ), c.fill( model( "" ) ) );
        }
    
    public void testSingleExternalContent()
        {
        Assembler a = new ContentAssembler();
        String source = Testing + "/schema.n3";
        Resource root = resourceInModel( "x rdf:type ja:Content; x rdf:type ja:ExternalContent; x ja:externalContent file:" + source );
        Content c = (Content) a.open( root );
        assertIsoModels( FileManager.get().loadModel( "file:" + source ), c.fill( model( "" ) ) );
        }    
    
    public void testMultipleExternalContent()
        {
        Assembler a = new ContentAssembler();
        String sourceA = Testing + "/schema.n3";
        String sourceB = Testing + "/schema2.n3";
        Resource root = resourceInModel
            ( "x rdf:type ja:Content; x rdf:type ja:ExternalContent"
            + "; x ja:externalContent file:" + sourceA + "; x ja:externalContent file:" + sourceB );
        Content c = (Content) a.open( root );
        Model wanted = FileManager.get().loadModel( "file:" + sourceA ).add( FileManager.get().loadModel( "file:" + sourceB ) );
        assertIsoModels( wanted, c.fill( model( "" ) ) );
        }
    
    public void testIndirectContent()
        {
        Assembler a = new ContentAssembler();
        Resource root = resourceInModel
            ( "x rdf:type ja:Content; x ja:content y"
            + "; y rdf:type ja:Content; y ja:content z"
            + "; z rdf:type ja:Content; z ja:quotedContent A; A P B"  );
        Content c = (Content) a.open( root );
        Model wanted = model( "A P B" );
        assertIsoModels( wanted, c.fill( model( "" ) ) );    
        }
    
    public void testTrapsBadEncodings()
        {
        Assembler a = new ContentAssembler();
        Resource root = resourceInModel( "x rdf:type ja:Content; x ja:contentEncoding 'bogus'; x ja:literalContent 'sham'" );
        try 
            { a.open( root ); fail( "should trap bad encoding" ); }
        catch (UnknownEncodingException e)
            {
            assertEquals( "bogus", e.getEncoding() );
            assertEquals( resource( "x" ), e.getRoot() );
            }
        }
    
    public void testMixedContent()
        {
        Assembler a = new ContentAssembler();
        String source = Testing + "/schema.n3";
        Resource root = resourceInModel
            ( "x rdf:type ja:Content; x rdf:type ja:LiteralContent; x rdf:type ja:ExternalContent" 
            + "; x ja:literalContent '<eh:/eggs>\\srdf:type\\srdf:Property.'"
            + "; x ja:externalContent file:" + source );
        Content c = (Content) a.open( root );
        Model wanted = FileManager.get().loadModel( "file:" + source ).add( model( "eggs rdf:type rdf:Property" ) );
        assertIsoModels( wanted, c.fill( model( "" ) ) );
        }
    
    public void testSingleContentQuotation()
        {
        Assembler a = new ContentAssembler();
        Resource root = resourceInModel( "c rdf:type ja:Content; c rdf:type ja:QuotedContent; c ja:quotedContent x; x P A; x Q B" );
        Content c = (Content) a.open( root );
        assertIsoModels( model( "x P A; x Q B" ), c.fill( model( "" ) ) );
        }
    
    public void testMultipleContentQuotation()
        {
        Assembler a = new ContentAssembler();
        Resource root = resourceInModel
            ( "c rdf:type ja:Content; c rdf:type ja:QuotedContent; c ja:quotedContent x"
            + "; c ja:quotedContent y; x P A; x Q B; y R C" );
        Content c = (Content) a.open( root );
        assertIsoModels( model( "x P A; x Q B; y R C" ), c.fill( model( "" ) ) );
        }
    
    public void testContentLoadsPrefixMappings()
        {
        Assembler a = new ContentAssembler();
        String content = 
            "@prefix foo: <eh:/foo#>. <eh:/eggs> rdf:type rdf:Property."
            .replaceAll( " ", "\\\\s" );
        Resource root = resourceInModel( "x rdf:type ja:Content; x rdf:type ja:LiteralContent; x ja:literalContent '" + content + "'" );
        Content c = (Content) a.open( root );
        Model m = ModelFactory.createDefaultModel();
        c.fill( m );
        assertEquals( "eh:/foo#", m.getNsPrefixURI( "foo" ) );
        }
    
    protected void testStringContent( String expected, String n3 )
        {
        Assembler a = new ContentAssembler();
        Resource root = resourceInModel( "x rdf:type ja:Content; x rdf:type ja:LiteralContent; x ja:literalContent '" + n3.replaceAll( " ", "\\\\s" ) + "'" );
        Content c = (Content) a.open( root );
        Model m = ModelFactory.createDefaultModel();
        c.fill( m );
        assertIsoModels( model( expected ), m );
        }
    
    
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
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