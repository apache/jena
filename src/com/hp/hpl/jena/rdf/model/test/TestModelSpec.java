/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestModelSpec.java,v 1.13 2003-08-25 11:54:24 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.ontology.*;

import junit.framework.*;

/**
 	@author kers
*/

public class TestModelSpec extends ModelTestBase
    {
    public TestModelSpec( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestModelSpec.class ); }
        
    public void testNotFindCreator()
        {
        Model aModel = ModelFactory.createDefaultModel();
        Resource type = resource( aModel, "jms:SomeType" );    
        assertSame( null, ModelSpecCreatorRegistry.findCreator( type ) );    
        }
        
    public void testFindCreator()
        {
        Model aModel = ModelFactory.createDefaultModel();
        Resource type = resource( aModel, "jms:SomeType" );    
        ModelSpecCreator c = new ModelSpecCreator() 
            { public ModelSpec create( Model m ) { return null; } };
        ModelSpecCreatorRegistry.register( type, c );
        assertSame( c, ModelSpecCreatorRegistry.findCreator( type ) );    
        }
        
    public void testFindCreatorChoice()
        {
        Model aModel = ModelFactory.createDefaultModel();
        Resource type1 = resource( aModel, "jms:SomeType1" );    
        Resource type2 = resource( aModel, "jms:SomeType2" );    
        ModelSpecCreator c1 = new ModelSpecCreator()
            { public ModelSpec create( Model m ) { return null; } };
        ModelSpecCreator c2 = new ModelSpecCreator() 
            { public ModelSpec create( Model m ) { return null; } };
        ModelSpecCreatorRegistry.register( type1, c1 );
        ModelSpecCreatorRegistry.register( type2, c2 );
        assertSame( c1, ModelSpecCreatorRegistry.findCreator( type1 ) );   
        assertSame( c2, ModelSpecCreatorRegistry.findCreator( type2 ) );   
        } 
        
    public void testHasStandardCreators()
        {
        assertNotNull( ModelSpecCreatorRegistry.findCreator( JMS.InfModelSpec ) );  
        assertNotNull( ModelSpecCreatorRegistry.findCreator( JMS.PlainModelSpec ) );   
        assertNotNull( ModelSpecCreatorRegistry.findCreator( JMS.OntModelSpec ) );     
        }
            
    public void testOntModeSpecIsaModelSpec()
        {
        assertTrue( OntModelSpec.DAML_MEM_RULE_INF instanceof ModelSpec );
        }
        
    public void testOntModelSpecCreatesOntModels()
        {
        Model m = OntModelSpec.DAML_MEM_RULE_INF.createModel();
        assertTrue( m instanceof OntModel );    
        }
        
    public void testOntModelSpecDescription()
        {
        OntModelSpec oms = OntModelSpec.DAML_MEM_RULE_INF;
        Model d = oms.getDescription();
        String daml = ProfileRegistry.DAML_LANG;
    /* */
        assertTrue( "", d.listStatements( null, JMS.ontLanguage, daml ).hasNext() );
    /* */
        StmtIterator si = d.listStatements( null, JMS.docManager, (RDFNode) null );
        Resource manager = si.nextStatement().getResource();
        assertSame( oms.getDocumentManager(), ModelSpecImpl.getValue( manager ) );
        }
        
    public void testOntModelSpecMaker()
        {
        OntModelSpec oms = OntModelSpec.DAML_MEM_RULE_INF;
        Model d = oms.getDescription();
        Statement s = d.getRequiredProperty( null, JMS.importMaker );
        Model makerSpec = oms.getModelMaker().getDescription();
        assertNotNull( s );
        assertIsoModels( makerSpec, subModel( d, s.getObject() ) );
        }
        
    public void testOntModelReasoner()
        {
        OntModelSpec oms = OntModelSpec.DAML_MEM_RULE_INF;
        Model d = oms.getDescription();
        Resource reasonerURI = d.createResource( oms.getReasonerFactory().getURI() );
        Statement s = d.getRequiredProperty( null, JMS.reasonsWith );
        Model reasonerSpec = ModelFactory.createDefaultModel()
            .add( d.createResource(), JMS.reasoner, reasonerURI );
        assertIsoModels( reasonerSpec, subModel( d, s.getObject() ) );
        }

    public Model subModel( Model m, RDFNode root )
        {
        Model result = ModelFactory.createDefaultModel();
        if (root instanceof Resource)
            result.add( m.listStatements( (Resource) root, null, (RDFNode) null ) );
        return result;    
        }
        
    public void testCreateOntSpec()
        {
        OntModelSpec oms = OntModelSpec.OWL_MEM_RULE_INF;
        Model spec = ModelFactory.createDefaultModel();
        String lang = oms.getLanguage();
        Resource me = ResourceFactory.createResource();
        Resource factory = spec.createResource( oms.getReasonerFactory().getURI() );
        spec.add( me, JMS.ontLanguage, lang );
        Resource r = spec.createResource();
        spec.add( r, JMS.reasoner, factory );
        spec.add( me, JMS.reasonsWith, r );
        Resource m = spec.createResource();
        Model modelMaker = ModelFactory.createDefaultModel();
        modelMaker.add( m, RDF.type, JMS.MemMakerSpec );
        modelMaker.add( m, JMS.reificationMode, JMS.rsStandard );
        spec.add( me, JMS.importMaker, m );
        spec.add( modelMaker );
        OntDocumentManager odm = oms.getDocumentManager();
        // Literal dm = spec.createTypedLiteral( odm, "jms:types/DocumentManager" );
        Resource dm = ModelSpecImpl.createValue( odm );
        spec.add( me, JMS.docManager, dm );
    /* */
        OntModelSpec ms = new OntModelSpec( spec );
        assertEquals( lang, ms.getLanguage() );
        assertEquals( factory.getURI(), ms.getReasonerFactory().getURI() );
        assertIsoModels( modelMaker, ms.getModelMaker().getDescription() );
        assertSame( odm, ms.getDocumentManager() );
        }

    public void testCreateFailingMaker()
        {
        try
            {
            ModelSpecImpl.createMaker( modelWithStatements( "" ) );
            fail( "oops" );
            }   
        catch (Exception e)
            {} 
        }
        
    public void testCreateMemModelMaker()
        {
        Resource mem = JMS.MemMakerSpec;
        testCreateModelMaker( JMS.rsStandard, mem, SimpleGraphMaker.class );
        testCreateModelMaker( JMS.rsMinimal, mem, SimpleGraphMaker.class );
        testCreateModelMaker( JMS.rsConvenient, mem, SimpleGraphMaker.class );
        }

    public void testCreateFileModelMaker()
        {
        Resource file =JMS.FileMakerSpec;
        testCreateModelMaker( JMS.rsStandard, file, FileGraphMaker.class );
        testCreateModelMaker( JMS.rsMinimal, file, FileGraphMaker.class );
        testCreateModelMaker( JMS.rsConvenient, file, FileGraphMaker.class );
        }
        
    public void testCreateFileModelMakerRooted()
        {
        String fileBase = "/somewhere";
        Resource me = ResourceFactory.createResource();
        Model spec = ModelFactory.createDefaultModel()
            .add( me, RDF.type, JMS.FileMakerSpec )
            .add( me, JMS.fileBase, fileBase )
            ;
        ModelMaker maker = ModelSpecImpl.createMaker( spec );
        FileGraphMaker fgm = (FileGraphMaker) maker.getGraphMaker();
        assertEquals( fileBase, fgm.getFileBase() );
    /* */
        Model desc = ModelFactory.createModelForGraph( fgm.getDescription() );
        assertTrue( desc.listStatements( null, JMS.fileBase, fileBase ).hasNext() );
        
        }
        
    public void testCreateModelMaker( Resource style, Resource cl, Class required )
        {
        Resource me = ResourceFactory.createResource();
        Reifier.Style wanted = JMS.findStyle( style );
        Model spec = modelWithStatements( "" )
            .add( me, RDF.type, cl )
            .add( me, JMS.reificationMode, style );
        ModelMaker maker = ModelSpecImpl.createMaker( spec );
        assertTrue( required.isInstance( maker.getGraphMaker() ) );
        assertEquals( wanted, maker.getGraphMaker().getReificationStyle() );
        }
                
    public void testCreatePlainMemModel()
        {
        Resource me = ResourceFactory.createResource();
        Model spec = modelWithStatements( "" )
            .add( me, RDF.type, JMS.MemMakerSpec )
            .add( me, JMS.reificationMode, JMS.rsStandard )
            ;    
        PlainModelSpec pms = new PlainModelSpec( spec );
        ModelMaker mm = pms.getModelMaker();
        Model desc = mm.getDescription( me );
        assertTrue( desc.contains( me, RDF.type, JMS.MemMakerSpec ) );
        assertTrue( desc.contains( me, JMS.reificationMode, JMS.rsStandard ) );
        assertTrue( mm.getGraphMaker() instanceof SimpleGraphMaker );
        assertEquals( Reifier.Standard , mm.getGraphMaker().getReificationStyle() );
        }
        
    public void testCreatePlainFileModel()
        {
        Resource me = ResourceFactory.createResource();
        Model spec = modelWithStatements( "" )
            .add( me, RDF.type, JMS.FileMakerSpec )
            .add( me, JMS.reificationMode, JMS.rsMinimal )
            ;    
        PlainModelSpec pms = new PlainModelSpec( spec );
        ModelMaker mm = pms.getModelMaker();
        Model desc = mm.getDescription( me );
        assertTrue( desc.contains( me, RDF.type, JMS.FileMakerSpec ) );
        assertTrue( desc.contains( me, JMS.reificationMode, JMS.rsMinimal ) );
        assertTrue( mm.getGraphMaker() instanceof FileGraphMaker );
        assertEquals( Reifier.Minimal , mm.getGraphMaker().getReificationStyle() );
        }
        
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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