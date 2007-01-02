/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestFileModelAssembler.java,v 1.6 2007-01-02 11:52:50 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.io.File;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.FileModelAssembler;
import com.hp.hpl.jena.graph.impl.FileGraph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileUtils;

public class TestFileModelAssembler extends ModelAssemblerTestBase
    {
    public TestFileModelAssembler( String name )
        { super( name );  }

    protected Class getAssemblerClass()
        { return FileModelAssembler.class; }

    public void testFileModelAssemblerType()
        { testDemandsMinimalType( new FileModelAssembler(), JA.FileModel );  }

    public void testFileAssemblerExists()
        {
        assertInstanceOf( Assembler.class, Assembler.fileModel );
        assertInstanceOf( FileModelAssembler.class, Assembler.fileModel );
        }
    
    public void testFileAssemblerVocabulary()
        {
        assertSubclassOf( JA.FileModel, JA.NamedModel );
        assertDomain( JA.FileModel, JA.fileEncoding );
        assertDomain( JA.FileModel, JA.directory );
        assertDomain( JA.FileModel, JA.mapName );
        }
    
    public void testFileModelAssemblerCreatesFileModels()
        {
        FileModelAssembler a = new FileModelAssembler();
        File x = FileUtils.tempFileName( "fileModelAssembler", ".n3" );
        Model m = a.createFileModel( x, "N3", true, false, ReificationStyle.Convenient );
        assertInstanceOf( FileGraph.class, m.getGraph() );
        FileGraph fg = (FileGraph) m.getGraph();
        assertEquals( x, fg.name );
        assertSame( ReificationStyle.Convenient, fg.getReifier().getStyle() );
        }
    
    public void testFileModelAssemblerUsesSpecialisedMethod()
        {
        final Model model = ModelFactory.createDefaultModel();
        FileModelAssembler a = new FileModelAssembler()
            {
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict, ReificationStyle style )
                { return model; }
            };
        Resource root = resourceInModel( "x rdf:type ja:FileModel; x ja:modelName 'junk'; x ja:directory file:" );
        Model m = a.openModel( root  );
        assertSame( model, m );
        }

    public void testFileModelAssemblerUsesLanguage()
        {
        final Model model = ModelFactory.createDefaultModel();
        Resource root = resourceInModel( "x rdf:type ja:FileModel; x ja:modelName 'junk'; x ja:directory file:; x ja:fileEncoding 'LANG'" );
        FileModelAssembler a = new FileModelAssembler()
            {
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict, ReificationStyle style )
                {
                assertEquals( "LANG", lang );
                return model; 
                }
            };
        Model m = a.openModel( root  );
        assertSame( model, m );
        }
    
    public void testFileModelAssemblerTrapsBadLanguage()
        {
        testTrapsBadLanguage( "badLanguage" );
        testTrapsBadLanguage( "17" );
        testTrapsBadLanguage( "'invalid'xsd:rhubarb" );
        }

    private void testTrapsBadLanguage( String lang )
        {
        final Model model = ModelFactory.createDefaultModel();
        Resource root = resourceInModel( "x rdf:type ja:FileModel; x ja:modelName 'junk'; x ja:directory file:; x ja:fileEncoding <lang>".replaceAll( "<lang>", lang ) );
        FileModelAssembler a = new FileModelAssembler()
            {
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict, ReificationStyle style )
                { return model; }
            };
        try 
            { a.openModel( root  ); 
            fail( "should trap bad fileEncoding object" ); }
        catch (BadObjectException e)
            { Model m = e.getRoot().getModel();
            assertEquals( resource( "x" ), e.getRoot() ); 
            assertEquals( rdfNode( m, lang ), e.getObject() ); }
        }
    
    public void testFileModelAssemblerUsesStyle()
        {
        testUsesStyle( "ja:minimal", ReificationStyle.Minimal );
        testUsesStyle( "ja:standard", ReificationStyle.Standard );
        testUsesStyle( "ja:convenient", ReificationStyle.Convenient );
        }

    private void testUsesStyle( String styleString, final ReificationStyle style )
        {
        final Model model = ModelFactory.createDefaultModel();
        FileModelAssembler a = new FileModelAssembler()
            {
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict, ReificationStyle s )
                { 
                assertSame( style, s );
                return model; 
                }
            };
        Resource root = resourceInModel( "x rdf:type ja:FileModel; x ja:modelName 'junk'; x ja:directory file:" + "; x ja:reificationMode " + styleString );
        Model m = a.openModel( root  );
        assertSame( model, m );
        }
    
    public void testFileModelAssemblerUsesMode()
        {
        testMode( true, true );
        testMode( false, true );
        testMode( true, false );
        try { testMode( false, false ); fail( "should trap, can nver create" ); }
        catch (JenaException e ) { pass(); }
        }

    private void testMode( final boolean mayCreate, final boolean mayReuse )
        {
        final Model model = ModelFactory.createDefaultModel();
        Mode mode = new Mode( mayCreate, mayReuse );
        FileModelAssembler a = new FileModelAssembler()
            {
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict, ReificationStyle s )
                { 
                if (mayCreate && mayReuse) 
                    {
                    assertEquals( "mayCreate && mayReuse implies non-strict", false, strict );
                    }
                if (mayCreate && !mayReuse) 
                    {
                    assertEquals( true, create );
                    assertEquals( true, strict );
                    }
                if (!mayCreate && mayReuse) 
                    {
                    assertEquals( false, create );
                    assertEquals( true, strict );
                    }
                if (!mayCreate && !mayReuse) 
                    throw new JenaException( "cannot create" );
                return model; 
                }
            };
        Resource root = resourceInModel( "x rdf:type ja:FileModel; x ja:modelName 'junk'; x ja:directory file:" );
        Model m = a.openModel( root, mode  );
        assertSame( model, m );
        }
    
    public void testCorrectSimpleModelName()
        {
        testCorrectModelName( "root/spoo", "root", "spoo", empty );
        testCorrectModelName( "root/branch/spoo", "root/branch", "spoo", empty );
        testCorrectModelName( "/root/spoo", "/root", "spoo", empty );
        testCorrectModelName( "root/spoo", "root", "spoo", empty );
        }
    
    public void testCorrectURIModelName()
        {
        testCorrectModelName( "root/name/subname", "root", "name/subname", empty );
        testCorrectModelName( "root/name_Ssubname", "root", "name/subname", model( "x ja:mapName ja:true" ) );
        testCorrectModelName( "root/name_Usubname", "root", "name_subname", model( "x ja:mapName ja:true" ) );
        testCorrectModelName( "root/name_Csubname", "root", "name:subname", model( "x ja:mapName ja:true" ) );
        testCorrectModelName( "root/http_C_S_Sdomain_Sdir_Sname", "root", "http://domain/dir/name", model( "x ja:mapName ja:true" ) );
        }

    private void testCorrectModelName( String expectedName, String directoryName, String modelName, Model extras )
        {
        final Model model = ModelFactory.createDefaultModel();
        final File wantedFullName = new File( expectedName );
        final ReificationStyle wantedStyle = ReificationStyle.Standard;
        final boolean wantedCreate = Mode.DEFAULT.permitCreateNew( null, null );
        final boolean wantedStrict = Mode.DEFAULT.permitUseExisting( null, null );
        Resource root = resourceInModel( "x rdf:type ja:FileModel; x ja:modelName '" + modelName + "'; x ja:directory file:" + directoryName );
        root.getModel().add( extras );
        FileModelAssembler a = new FileModelAssembler()
            {
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict, ReificationStyle style )
                {
                assertEquals( wantedFullName, fullName );
                assertEquals( wantedStyle, style );
                assertEquals( wantedCreate, create );
                assertEquals( wantedStrict, strict );
                return model; 
                }
            };
        Model m = a.openModel( root  );
        assertSame( model, m );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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