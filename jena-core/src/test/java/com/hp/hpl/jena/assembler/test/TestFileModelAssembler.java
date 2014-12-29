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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.BadObjectException;
import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.FileModelAssembler;
import com.hp.hpl.jena.graph.impl.FileGraph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;

public class TestFileModelAssembler extends AssemblerTestBase
    {

    @Rule
    private final TemporaryFolder tempFolder = new TemporaryFolder();

    public TestFileModelAssembler( String name )
        { super( name );  }

    @Override protected Class<? extends Assembler> getAssemblerClass()
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
        Model m = a.createFileModel( x, "N3", true, false);
        assertInstanceOf( FileGraph.class, m.getGraph() );
        FileGraph fg = (FileGraph) m.getGraph();
        assertEquals( x, fg.name );
        }
    
    public void testFileModelAssemblerUsesSpecialisedMethod()
        {
        final Model model = ModelFactory.createDefaultModel();
        FileModelAssembler a = new FileModelAssembler()
            {
            @Override
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict)
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
            @Override
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict)
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
            @Override
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict)
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
    
    public void testStrictAndCreateCanBeSetFromProperties()
        {       
        NoteAssemblerBooleans a = new NoteAssemblerBooleans();
        a.openModel( assemble( "x ja:create 'true'" ) );  assertEquals( true, a.create );
        a.openModel( assemble( "x ja:create 'false'" ) );  assertEquals( false, a.create );
        a.openModel( assemble( "x ja:strict 'true'" ) );  assertEquals( true, a.strict );
        a.openModel( assemble( "x ja:strict 'false'" ) );  assertEquals( false, a.strict );
        }

    private Resource assemble( String details )
        {
        return resourceInModel( "x rdf:type ja:FileModel; x ja:modelName 'junk'; <D>; x ja:directory file:spoo".replaceAll( "<D>", details ) );
        }   
    
    private final class NoteAssemblerBooleans extends FileModelAssembler
        {
        private final Model model;
        public boolean create;
        public boolean strict;
        
        private NoteAssemblerBooleans()
            { this.model = model(); }

        @Override
        public Model createFileModel( File fullName, String lang, boolean create, boolean strict )
            { 
            this.create = create;
            this.strict = strict;
            return model; 
            }
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
            @Override
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict)
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
        final boolean wantedCreate = Mode.DEFAULT.permitCreateNew( null, null );
        final boolean wantedStrict = Mode.DEFAULT.permitUseExisting( null, null );
        Resource root = resourceInModel( "x rdf:type ja:FileModel; x ja:modelName '" + modelName + "'; x ja:directory file:" + directoryName );
        root.getModel().add( extras );
        FileModelAssembler a = new FileModelAssembler()
            {
            @Override
            public Model createFileModel( File fullName, String lang, boolean create, boolean strict)
                {
                assertEquals( wantedFullName, fullName );
                assertEquals( wantedCreate, create );
                assertEquals( wantedStrict, strict );
                return model; 
                }
            };
        Model m = a.openModel( root  );
        assertSame( model, m );
        }

    @Test
    public void testDecodeDirectoryName() throws IOException
        {
            // TODO: JenaTestBase is mixing JUnit3 and JUnit4 classes, and the
            // rule doesn't get called correctly.
            tempFolder.create();
            assertTrue(tempFolder.getRoot().isDirectory());
            File folderWithSpace = tempFolder.newFolder("folder with space");
            folderWithSpace.mkdir();
            File modelFile = new File(folderWithSpace, "file with space.ttl");
            modelFile.createNewFile();
            String graphs = "";
            graphs += "@prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> . ";
            graphs += "<http://example.org/file> a ja:FileModel ; ";
            graphs += "ja:directory <file:///" + tempFolder.getRoot().getAbsolutePath() + "/folder%20with%20space/> ; ";
            graphs += "ja:modelName \"" + modelFile.getName() + "\" . ";

            InputStream is = new ByteArrayInputStream(graphs.getBytes());

            Model model = ModelFactory.createDefaultModel();
            model.read(is, "http://example.org/", "TURTLE");
            Resource r = model.createResource("http://example.org/file");
            Model assemblerModel = (Model) Assembler.general.open(r);
            assertNotNull(assemblerModel);
            tempFolder.delete();
        }
    }
