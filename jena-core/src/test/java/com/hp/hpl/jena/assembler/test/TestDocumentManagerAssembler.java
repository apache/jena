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

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.DocumentManagerAssembler;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

public class TestDocumentManagerAssembler extends AssemblerTestBase
    {
    public TestDocumentManagerAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return DocumentManagerAssembler.class; }

    public void testDocumentManagerAssemblerType()
        { testDemandsMinimalType( new DocumentManagerAssembler(), JA.DocumentManager );  }

    public void testDocumentManagerVocabulary()
        {
        assertSubclassOf( JA.DocumentManager, JA.Object );
        assertSubclassOf( JA.DocumentManager,  JA.HasFileManager);
        assertRange( JA.FileManager, JA.fileManager );
        assertDomain( JA.DocumentManager, JA.policyPath );
        }
    
    public void testCreatesDocumentManager() 
        { 
        Resource root = resourceInModel( "x rdf:type ja:DocumentManager" );
        Assembler a = new DocumentManagerAssembler();
        Object x = a.open( root );
        assertInstanceOf( OntDocumentManager.class, x );
        }
    
    public void testUsesFileManager()
        {
        Resource root = resourceInModel( "x rdf:type ja:DocumentManager; x ja:fileManager f" );
        Assembler a = new DocumentManagerAssembler();
        FileManager fm = new FileManager();
        Assembler mock = new NamedObjectAssembler( resource( "f" ), fm );
        Object x = a.open( mock, root );
        assertInstanceOf( OntDocumentManager.class, x );
        assertSame( fm, ((OntDocumentManager) x).getFileManager() );
        }
    
    public void testSetsPolicyPath()
        {
        Resource root = resourceInModel( "x rdf:type ja:DocumentManager; x ja:policyPath 'somePath'" );
        final List<String> history = new ArrayList<>();
        Assembler a = new DocumentManagerAssembler()
            {
            @Override
            protected OntDocumentManager createDocumentManager()
                {
                return new OntDocumentManager( "" )
                    {
                    @Override
                    public void setMetadataSearchPath( String path, boolean replace ) 
                        {
                        assertEquals( false, replace );
                        history.add( path );
                        super.setMetadataSearchPath( path, replace ); }
                    };
                }
            };
        OntDocumentManager d = (OntDocumentManager) a.open( root );
        assertEquals( listOfOne( "somePath" ), history );
        }    
    
    public void testTrapsPolicyPathNotString()
        {
        testTrapsBadPolicyPath( "aResource" );
        testTrapsBadPolicyPath( "17" );
        testTrapsBadPolicyPath( "'char'en" );
        testTrapsBadPolicyPath( "'cafe'xsd:integer" );
        }

    private void testTrapsBadPolicyPath( String path )
        {
        Resource root = resourceInModel( "x rdf:type ja:DocumentManager; x ja:policyPath <policy>".replaceAll( "<policy>", path ) );
        Assembler a = new DocumentManagerAssembler();
        try
            { a.open( root );
            fail( "should trap illegal policy path object " + path ); }
        catch (BadObjectException e)
            { assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( rdfNode( root.getModel(), path ), e.getObject() ); }
        }
    
    public void testSetsMetadata()
        { // we set policyPath to avoid Ont default models being thrown at us
        Resource root = resourceInModel( "x rdf:type ja:DocumentManager; x ja:policyPath ''; x P a; a Q b; y R z" );
        final Model expected = model( "x rdf:type ja:DocumentManager; x ja:policyPath ''; x P a; a Q b" );
        final List<String> history = new ArrayList<>();
        Assembler a = new DocumentManagerAssembler()
            {
            @Override
            protected OntDocumentManager createDocumentManager()
                {
                return new OntDocumentManager( "" )
                    {                    
                    @Override
                    public void processMetadata( Model m ) 
                        {
                        assertIsoModels( expected, m );
                        history.add( "called" );
                        super.processMetadata( m ); }
                    };
                }
            };
        OntDocumentManager d = (OntDocumentManager) a.open( root );
        assertEquals( listOfOne( "called" ), history );
        }
    }
