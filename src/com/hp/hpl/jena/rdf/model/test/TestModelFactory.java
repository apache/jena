/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelFactory.java,v 1.21 2004-06-21 15:00:18 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.mem.*;

import junit.framework.*;

/**
    Tests the ModelFactory code. Very skeletal at the moment. It's really
    testing that the methods actually exists, but it doesn't check much in
    the way of behaviour.
    
    @author kers
*/

public class TestModelFactory extends ModelTestBase
    {
    public TestModelFactory(String name)
        { super(name); }
        
    public static TestSuite suite()
        { return new TestSuite( TestModelFactory.class ); }   
        
    /**
        Test that ModelFactory.createDefaultModel() exists. [Should check that the Model
        is truly a "default" model.]
     */
    public void testCreateDefaultModel()
        { ModelFactory.createDefaultModel().close(); }    

    public void testGetDefaultPrefixMapping()
        {
        assertSame( ModelCom.getDefaultModelPrefixes(), ModelFactory.getDefaultModelPrefixes() );
        }
    
    public void testSetDefaultPrefixMapping()
        {
        PrefixMapping original = ModelCom.getDefaultModelPrefixes();
        PrefixMapping pm = PrefixMapping.Factory.create();
        ModelFactory.setDefaultModelPrefixes( pm );
        assertSame( pm, ModelCom.getDefaultModelPrefixes() );
        assertSame( pm, ModelFactory.getDefaultModelPrefixes() );
        ModelCom.setDefaultModelPrefixes( original );
        }
    
    public void testCreateSpecFails()
        {
        try
            {
            ModelFactory.createSpec( ModelFactory.createDefaultModel() );    
            fail( "empty descriptions should throw the appropriate exception" );
            }    
        catch (BadDescriptionException e) { pass(); }
        }
        
    public void testCreatePlainSpec()
        {
        Model desc = TestModelSpec.createPlainModelDesc();
        ModelSpec spec = ModelFactory.createSpec( desc ); 
        assertIsoModels( desc, spec.getDescription() );
        assertTrue( spec instanceof PlainModelSpec );
        assertTrue( spec.createModel().getGraph() instanceof GraphMem );
        }
        
    public void testCreateOntSpec()
        {
        Resource root = ResourceFactory.createResource();
        Resource importsMaker = ResourceFactory.createResource();
        Resource baseMaker = ResourceFactory.createResource();
        Resource reasoner = ResourceFactory.createResource();
        OntDocumentManager docManager = new OntDocumentManager();
        Resource reasonerURI = ResourceFactory.createResource( DAMLMicroReasonerFactory.URI );
        Model desc = ModelFactory.createDefaultModel()
        	.add( root, JMS.maker, baseMaker )
        	.add( root, JMS.importMaker, importsMaker )
            .add( baseMaker, RDF.type, JMS.FileMakerSpec )
            .add( baseMaker, JMS.fileBase, "/tmp/example" )
            .add( baseMaker, JMS.reificationMode, JMS.rsMinimal )
            .add( importsMaker, RDF.type, JMS.MemMakerSpec )
            .add( importsMaker, JMS.reificationMode, JMS.rsMinimal )
            .add( root, JMS.ontLanguage, ProfileRegistry.DAML_LANG )
            .add( root, JMS.docManager, ModelSpecImpl.createValue( docManager ) )
            .add( root, JMS.reasonsWith, reasoner )
            .add( reasoner, JMS.reasoner, reasonerURI );
        ModelSpec spec = ModelFactory.createSpec( desc ); 
        assertTrue( spec instanceof OntModelSpec );           
        assertIsoModels( desc, spec.getDescription() );
        assertTrue( spec.createModel() instanceof OntModel );
        }
        
    public void testCreateOntologyModelFromSpecOnly()
        {
        Resource root = ResourceFactory.createResource();
        Model desc = ModelFactory.createDefaultModel()
            .add( root, JMS.ontLanguage, ProfileRegistry.DAML_LANG )
        	;
        OntModelSpec spec = (OntModelSpec) ModelFactory.createSpec( desc );
        OntModel m = ModelFactory.createOntologyModel( spec );
        }
    
    public void testCreateInfSpec()
        {
        Model desc = TestModelSpec.createInfModelDesc( DAMLMicroReasonerFactory.URI );
        ModelSpec spec = ModelFactory.createSpec( desc );
        assertTrue( spec instanceof InfModelSpec );    
        assertIsoModels( desc, spec.getDescription() );
        assertTrue( spec.createModel() instanceof InfModel );
        }
        
    /**
        Test that ModelFactory.createModel exists and returns models.
    */
    public void testMFCreate()
        {
        Model desc = TestModelSpec.createPlainModelDesc();
        ModelSpec spec = ModelFactory.createSpec( desc );
        Model m = ModelFactory.createModel( spec );    
        }
        
    public void testMFCreateNamed()
        {
        Model desc = TestModelSpec.createPlainModelDesc();
        ModelSpec spec = ModelFactory.createSpec( desc );
        Model m = ModelFactory.createModelOver( spec, "aName" );    
        }        
        
    public void testCreateNamed()
        {
        Resource root = ResourceFactory.createResource();
        Model desc = TestModelSpec.createPlainModelDesc( root );
        ModelSpec spec = ModelFactory.createSpec( root, desc );    
        }
    }

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
