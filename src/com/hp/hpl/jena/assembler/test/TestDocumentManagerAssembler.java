/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestDocumentManagerAssembler.java,v 1.5 2007-07-24 15:32:33 chris-dollin Exp $
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

    protected Class getAssemblerClass()
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
        final List history = new ArrayList();
        Assembler a = new DocumentManagerAssembler()
            {
            protected OntDocumentManager createDocumentManager()
                {
                return new OntDocumentManager( "" )
                    {
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
        final List history = new ArrayList();
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
        final List history = new ArrayList();
        Assembler a = new DocumentManagerAssembler()
            {
            protected OntDocumentManager createDocumentManager()
                {
                return new OntDocumentManager( "" )
                    {                    
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


/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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