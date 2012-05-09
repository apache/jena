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

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.ContentAssembler;
import com.hp.hpl.jena.assembler.exceptions.UnknownEncodingException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;

public class TestContentAssembler extends AssemblerTestBase
    {
    protected static String Testing = "testing/assemblers";
    
    public TestContentAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return ContentAssembler.class; }

    public void testContentAssemblerType()
        { testDemandsMinimalType( new ContentAssembler(), JA.Content );  }
    
    public void testContentVocabulary()
        {
        assertSubclassOf( JA.Content, JA.HasFileManager );
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
    
    public void testContentTrapsBadObjects()
        {
        testContentTrapsBadObjects( "ja:content", "17" );
        // testContentTrapsBadObjects( "ja:externalContent", "17" );
        testContentTrapsBadObjects( "ja:quotedContent", "17" );
        testContentTrapsBadObjects( "ja:literalContent", "aResource" );
        testContentTrapsBadObjects( "ja:literalContent", "17" );
        testContentTrapsBadObjects( "ja:literalContent", "'plume'fr" );
        }

    private void testContentTrapsBadObjects( String property, String value )
        {
        Assembler a = new ContentAssembler();
        Resource root = resourceInModel
            ( "x rdf:type ja:Content; x <property> <value>"
            .replaceAll( "<property>", property ).replaceAll( "<value>", value ) );
        try { a.open( root ); fail( "should trap bad content resource" ); }
        catch (BadObjectException e) 
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( rdfNode( empty, value ), e.getObject() );
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
    
    /*
        -- ContentAssembler FileManager tests ----------------------------------
    */
    
    public void testContentAssemblerHasNoDefaultFileManager()
        {
        assertNull( "by default, ContentAssemblers have no FileManager", new ContentAssembler().getFileManager() );
        }    
    
    public void testContentAssemblerHasSuppliedFileManager()
        {
        FileManager fm = new FileManager();
        assertSame( fm, new ContentAssembler( fm ).getFileManager() );
        }
    
    public void testUsesSuppliedFileManager()
        {
        final boolean [] used = {false};
        FileManager fm = new FileManager()
            {
            @Override
            public Model loadModel( String filenameOrURI )
                {
                used[0] = true;
                return FileManager.get().loadModel( filenameOrURI );
                }
            };
        Assembler a = new ContentAssembler( fm );
        String source = Testing + "/schema.n3";
        Resource root = resourceInModel( "x rdf:type ja:Content; x rdf:type ja:ExternalContent; x ja:externalContent file:" + source );
        Content c = (Content) a.open( root );
        assertIsoModels( FileManager.get().loadModel( "file:" + source ), c.fill( model( "" ) ) );
        assertTrue( "the supplied file manager must have been used", used[0] );
        }
    
    public void testContentAssemblerUsesFileManagerProperty()
        {
        Model expected = model( "a P b" );
        String fileName = "file:spoo";
        FixedFileManager fm = new FixedFileManager( expected, fileName );
        NamedObjectAssembler noa = new NamedObjectAssembler( resource( "F" ), fm );
        Resource root = resourceInModel
            ( "x rdf:type ja:Content; x rdf:type ja:ExternalContent; x ja:externalContent <F>; x ja:fileManager F"
            .replaceAll( "<F>", fileName ) 
            );
        Assembler a = new ContentAssembler();        
        Content c = (Content) a.open( noa, root );
        assertTrue( fm.wasUsed() );
        assertIsoModels( expected, c.fill( model() ) );
        }    
    
    private final class FixedFileManager extends FileManager
        {
        private final Model expected;
        private final String fileName;
        private boolean used;
        
        private FixedFileManager( Model expected, String fileName )
            { this.expected = expected; this.fileName = fileName; }

        @Override
        public Model loadModel( String filenameOrURI )
            {
            used = true;
            assertEquals( fileName, filenameOrURI );
            return expected;
            }
        
        public boolean wasUsed()
            { return used; }
        }

    }
