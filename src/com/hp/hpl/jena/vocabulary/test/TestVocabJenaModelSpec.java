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
        assertEquals( "http://jena.hpl.hp.com/2003/08/jms#", JMS.getURI() );
        assertEquals( JMS.baseURI, JMS.getURI() );
        }
    
    public void testProperties()
        {
        assertEquals( jmsProperty( "loadWith" ), JMS.loadWith );
        assertEquals( jmsProperty( "ontLanguage" ), JMS.ontLanguage );
        assertEquals( jmsProperty( "docManager" ), JMS.docManager );
        assertEquals( jmsProperty( "importMaker" ), JMS.importMaker );
        assertEquals( jmsProperty( "reasonsWith" ), JMS.reasonsWith );
        assertEquals( jmsProperty( "ruleSetURL" ), JMS.ruleSetURL );
        assertEquals( jmsProperty( "ruleSet" ), JMS.ruleSet );
        assertEquals( jmsProperty( "schemaURL" ), JMS.schemaURL );
        assertEquals( jmsProperty( "hasRule" ), JMS.hasRule );
        assertEquals( jmsProperty( "policyPath" ), JMS.policyPath );
        assertEquals( jmsProperty( "dbUser" ), JMS.dbUser );
        assertEquals( jmsProperty( "dbPassword" ), JMS.dbPassword );
        assertEquals( jmsProperty( "dbURL" ), JMS.dbURL );
        assertEquals( jmsProperty( "dbType" ), JMS.dbType );
        assertEquals( jmsProperty( "dbClass" ), JMS.dbClass );
        assertEquals( jmsProperty( "maker" ), JMS.maker );
        assertEquals( jmsProperty( "reificationMode" ), JMS.reificationMode );
        assertEquals( jmsProperty( "reasoner" ), JMS.reasoner );
        assertEquals( jmsProperty( "fileBase" ), JMS.fileBase );
        assertEquals( jmsProperty( "typeCreatedBy" ), JMS.typeCreatedBy );
        }
    
    public void testResource()
        {
        assertEquals( jmsResource( "MakerSpec" ), JMS.MakerSpec );
        assertEquals( jmsResource( "FileMakerSpec" ), JMS.FileMakerSpec );
        assertEquals( jmsResource( "MemMakerSpec" ), JMS.MemMakerSpec );
        assertEquals( jmsResource( "RDBMakerSpec" ), JMS.RDBMakerSpec );
        assertEquals( jmsResource( "ModelSpec" ), JMS.ModelSpec );
        assertEquals( jmsResource( "DefaultModelSpec" ), JMS.DefaultModelSpec );
        assertEquals( jmsResource( "PlainModelSpec" ), JMS.PlainModelSpec );
        assertEquals( jmsResource( "InfModelSpec" ), JMS.InfModelSpec );
        assertEquals( jmsResource( "OntModelSpec" ), JMS.OntModelSpec );
        assertEquals( jmsResource( "rsStandard" ), JMS.rsStandard );
        assertEquals( jmsResource( "rsMinimal" ), JMS.rsMinimal );
        assertEquals( jmsResource( "rsConvenient" ), JMS.rsConvenient );
        }
    
    public void testMakerSubclasses()
        {
        ensure( JMS.MemMakerSpec, RDFS.subClassOf, JMS.MakerSpec );
        ensure( JMS.FileMakerSpec, RDFS.subClassOf, JMS.MakerSpec );
        ensure( JMS.RDBMakerSpec, RDFS.subClassOf, JMS.MakerSpec );
        }
    
    public void testSpecSubclasses()
        {        
        ensure( JMS.DefaultModelSpec, RDFS.subClassOf, JMS.ModelSpec );
        ensure( JMS.PlainModelSpec, RDFS.subClassOf, JMS.ModelSpec );
        ensure( JMS.InfModelSpec, RDFS.subClassOf, JMS.PlainModelSpec );
        ensure( JMS.OntModelSpec, RDFS.subClassOf, JMS.InfModelSpec );
        }
    
    public void testDomains()
        {        
        ensure( JMS.reificationMode, RDFS.domain, JMS.MakerSpec );
        ensure( JMS.maker, RDFS.domain, JMS.PlainModelSpec );
    //    
        ensure( JMS.importMaker, RDFS.domain, JMS.OntModelSpec );
        ensure( JMS.ontLanguage, RDFS.domain, JMS.OntModelSpec );
        ensure( JMS.reasonsWith, RDFS.domain, JMS.InfModelSpec );
        }
    
    protected void ensure( Resource S, Property P, RDFNode O )
        {
        if (!JMS.schema.contains( S, P, O ))
            fail( "schema omits (" + nice( S ) + " " + nice( P ) + " " + nice( O ) + ")" );
        }

    protected Resource jmsResource( String string )
        { return resource( JMS.getURI() + string ); }

    protected Property jmsProperty( String string )
        { return property( JMS.getURI() + string );}

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