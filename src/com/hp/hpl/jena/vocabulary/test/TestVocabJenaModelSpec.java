/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	[See end of file]
*/

package com.hp.hpl.jena.vocabulary.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.*;

/**
    Test the modelspec vocabulary, including ensuring that it
    has the necessary typing information. 
    @author kers
*/
public class TestVocabJenaModelSpec extends ModelTestBase
    {
    public TestVocabJenaModelSpec( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestVocabJenaModelSpec.class ); }
    
    public void testURI()
        {
        assertEquals( "http://jena.hpl.hp.com/2003/08/jms#", JenaModelSpec.getURI() );
        assertEquals( JenaModelSpec.baseURI, JenaModelSpec.getURI() );
        }
    
    public void testProperties()
        {
        assertEquals( jmsProperty( "loadWith" ), JenaModelSpec.loadWith );
        assertEquals( jmsProperty( "ontLanguage" ), JenaModelSpec.ontLanguage );
        assertEquals( jmsProperty( "docManager" ), JenaModelSpec.docManager );
        assertEquals( jmsProperty( "importMaker" ), JenaModelSpec.importMaker );
        assertEquals( jmsProperty( "reasonsWith" ), JenaModelSpec.reasonsWith );
        assertEquals( jmsProperty( "ruleSetURL" ), JenaModelSpec.ruleSetURL );
        assertEquals( jmsProperty( "ruleSet" ), JenaModelSpec.ruleSet );
        assertEquals( jmsProperty( "schemaURL" ), JenaModelSpec.schemaURL );
        assertEquals( jmsProperty( "hasRule" ), JenaModelSpec.hasRule );
        assertEquals( jmsProperty( "policyPath" ), JenaModelSpec.policyPath );
        assertEquals( jmsProperty( "hasConnection" ), JenaModelSpec.hasConnection );
        assertEquals( jmsProperty( "dbUser" ), JenaModelSpec.dbUser );
        assertEquals( jmsProperty( "dbPassword" ), JenaModelSpec.dbPassword );
        assertEquals( jmsProperty( "dbURL" ), JenaModelSpec.dbURL );
        assertEquals( jmsProperty( "dbType" ), JenaModelSpec.dbType );
        assertEquals( jmsProperty( "dbClass" ), JenaModelSpec.dbClass );
        assertEquals( jmsProperty( "maker" ), JenaModelSpec.maker );
        assertEquals( jmsProperty( "reificationMode" ), JenaModelSpec.reificationMode );
        assertEquals( jmsProperty( "reasoner" ), JenaModelSpec.reasoner );
        assertEquals( jmsProperty( "fileBase" ), JenaModelSpec.fileBase );
        assertEquals( jmsProperty( "typeCreatedBy" ), JenaModelSpec.typeCreatedBy );
        assertEquals( jmsProperty( "modelName" ), JenaModelSpec.modelName );
        }
    
    public void testResource()
        {
        assertEquals( jmsResource( "MakerSpec" ), JenaModelSpec.MakerSpec );
        assertEquals( jmsResource( "FileMakerSpec" ), JenaModelSpec.FileMakerSpec );
        assertEquals( jmsResource( "MemMakerSpec" ), JenaModelSpec.MemMakerSpec );
        assertEquals( jmsResource( "RDBMakerSpec" ), JenaModelSpec.RDBMakerSpec );
        assertEquals( jmsResource( "ModelSpec" ), JenaModelSpec.ModelSpec );
        assertEquals( jmsResource( "DefaultModelSpec" ), JenaModelSpec.DefaultModelSpec );
        assertEquals( jmsResource( "PlainModelSpec" ), JenaModelSpec.PlainModelSpec );
        assertEquals( jmsResource( "InfModelSpec" ), JenaModelSpec.InfModelSpec );
        assertEquals( jmsResource( "OntModelSpec" ), JenaModelSpec.OntModelSpec );
        assertEquals( jmsResource( "FileModelSpec" ), JenaModelSpec.FileModelSpec );
        assertEquals( jmsResource( "rsStandard" ), JenaModelSpec.rsStandard );
        assertEquals( jmsResource( "rsMinimal" ), JenaModelSpec.rsMinimal );
        assertEquals( jmsResource( "rsConvenient" ), JenaModelSpec.rsConvenient );
        }
    
    public void testMakerSubclasses()
        {
        ensure( JenaModelSpec.MemMakerSpec, RDFS.subClassOf, JenaModelSpec.MakerSpec );
        ensure( JenaModelSpec.FileMakerSpec, RDFS.subClassOf, JenaModelSpec.MakerSpec );
        ensure( JenaModelSpec.RDBMakerSpec, RDFS.subClassOf, JenaModelSpec.MakerSpec );
        }
    
    public void testSpecSubclasses()
        {        
        ensure( JenaModelSpec.DefaultModelSpec, RDFS.subClassOf, JenaModelSpec.ModelSpec );
        ensure( JenaModelSpec.PlainModelSpec, RDFS.subClassOf, JenaModelSpec.ModelSpec );
        ensure( JenaModelSpec.InfModelSpec, RDFS.subClassOf, JenaModelSpec.PlainModelSpec );
        ensure( JenaModelSpec.FileModelSpec, RDFS.subClassOf, JenaModelSpec.PlainModelSpec );
        ensure( JenaModelSpec.OntModelSpec, RDFS.subClassOf, JenaModelSpec.InfModelSpec );
        }
    
    public void testDomains()
        {        
        ensure( JenaModelSpec.reificationMode, RDFS.domain, JenaModelSpec.MakerSpec );
        ensure( JenaModelSpec.maker, RDFS.domain, JenaModelSpec.PlainModelSpec );
        ensure( JenaModelSpec.modelName, RDFS.domain, JenaModelSpec.ModelSpec );
        ensure( JenaModelSpec.loadWith, RDFS.domain, JenaModelSpec.ModelSpec );
    //    
        ensure( JenaModelSpec.importMaker, RDFS.domain, JenaModelSpec.OntModelSpec );
        ensure( JenaModelSpec.ontLanguage, RDFS.domain, JenaModelSpec.OntModelSpec );
        ensure( JenaModelSpec.reasonsWith, RDFS.domain, JenaModelSpec.InfModelSpec );
        ensure( JenaModelSpec.fileBase, RDFS.domain, JenaModelSpec.FileMakerSpec );
        }
    
    protected void ensure( Resource S, Property P, RDFNode O )
        {
        if (!JenaModelSpec.getSchema().contains( S, P, O ))
            fail( "schema omits (" + nice( S ) + " " + nice( P ) + " " + nice( O ) + ")" );
        }

    protected Resource jmsResource( String string )
        { return resource( JenaModelSpec.getURI() + string ); }

    protected Property jmsProperty( String string )
        { return property( JenaModelSpec.getURI() + string );}

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