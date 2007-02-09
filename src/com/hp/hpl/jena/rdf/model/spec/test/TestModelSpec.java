/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelSpec.java,v 1.6 2007-02-09 13:19:14 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.spec.test;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.test.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.ontology.*;

import junit.framework.*;

/**
    Testing ModelSpec. The code is horrid and needs considerable tidying-up, as the
    essence of the tests is not obvious.
    
 	@author kers
*/

public class TestModelSpec extends ModelTestBase
    {
    public TestModelSpec( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestModelSpec.class ); }
        
    public void testNotFindMaker()
        {
        Resource type = resource( "jms:xyz" );
        assertSame( null, ModelMakerCreatorRegistry.findCreator( type ) );    
        }
        
    public void testFindMakerChoice()
        {
        Resource type1 = resource( "jms:type1" ), type2 = resource( "jms:type2" );
        ModelMakerCreator mmc1 = new ModelMakerCreator() 
            { public ModelMaker create( Model desc, Resource root ) { return null; } };
        ModelMakerCreator mmc2 = new ModelMakerCreator() 
            { public ModelMaker create( Model desc, Resource root ) { return null; } };
        ModelMakerCreatorRegistry.register( type1, mmc1 );
        ModelMakerCreatorRegistry.register( type2, mmc2 );
        assertSame( mmc1, ModelMakerCreatorRegistry.findCreator( type1 ) );
        assertSame( mmc2, ModelMakerCreatorRegistry.findCreator( type2 ) );
        }
        
    public void testFindStandardMakers()
        {
        assertNotNull( ModelMakerCreatorRegistry.findCreator( JenaModelSpec.FileMakerSpec ) );   
        assertNotNull( ModelMakerCreatorRegistry.findCreator( JenaModelSpec.MemMakerSpec ) );   
        assertNotNull( ModelMakerCreatorRegistry.findCreator( JenaModelSpec.RDBMakerSpec ) );    
        }
    
    
    public void testNotFindCreator()
        {
        Resource type = resource( "jms:SomeType" );    
        assertSame( null, ModelSpecCreatorRegistry.findCreator( type ) );    
        }
        
    public void testFindCreator()
        {
        Resource type = resource( "jms:SomeType" );    
        ModelSpecCreator c = new ModelSpecCreator() 
            { public ModelSpec create( Resource root, Model m ) { return null; } };
        ModelSpecCreatorRegistry.register( type, c );
        assertSame( c, ModelSpecCreatorRegistry.findCreator( type ) );    
        }
        
    public void testFindCreatorChoice()
        {
        Resource type1 = resource( "jms:SomeType1" );    
        Resource type2 = resource( "jms:SomeType2" );    
        ModelSpecCreator c1 = new ModelSpecCreator()
             { public ModelSpec create( Resource root, Model m ) { return null; }  };
        ModelSpecCreator c2 = new ModelSpecCreator() 
             { public ModelSpec create( Resource root, Model m ) { return null; }  };
        ModelSpecCreatorRegistry.register( type1, c1 );
        ModelSpecCreatorRegistry.register( type2, c2 );
        assertSame( c1, ModelSpecCreatorRegistry.findCreator( type1 ) );   
        assertSame( c2, ModelSpecCreatorRegistry.findCreator( type2 ) );   
        } 
        
    public void testDetectRootAmbiguity()
        {
        Model desc = createPlainModelDesc().add( createPlainModelDesc() );
        try { ModelSpecFactory.createSpec( desc ); fail( "must trap ambiguous description" ); }
        catch (BadDescriptionException b) { pass(); }
        }
                          
    public void testOntModeSpecIsaModelSpec()
        {
        assertInstanceOf( ModelSpec.class, OntModelSpec.DAML_MEM_RULE_INF );
        }
        
    public void testOntModelSpecCreatesOntModels()
        {
        Model m = OntModelSpec.DAML_MEM_RULE_INF.createFreshModel();
        assertInstanceOf( OntModel.class, m );    
        }
        
    public void testOntModelSpecDescription()
        {
        OntModelSpec oms = OntModelSpec.DAML_MEM_RULE_INF;
        Model d = oms.getDescription();
    /* */
        assertTrue( "", d.contains( null, JenaModelSpec.ontLanguage, TestModelFactory.DAMLLangResource ) );
    /* */
        StmtIterator si = d.listStatements( null, JenaModelSpec.docManager, (RDFNode) null );
        Resource manager = si.nextStatement().getResource();
        assertSame( oms.getDocumentManager(), ModelSpecImpl.getValue( manager ) );
        }
        
    public void testOntModelSpecMaker()
        {
        OntModelSpec oms = OntModelSpec.DAML_MEM_RULE_INF;
        Model d = oms.getDescription();
        Statement s = d.getRequiredProperty( null, JenaModelSpec.importMaker );
        Model makerSpec = oms.getImportModelMaker().getDescription();
        assertNotNull( s );
        assertIsoModels( makerSpec, subModel( d, s.getObject() ) );
        }
        
    public void testOntModelReasoner()
        {
        OntModelSpec oms = OntModelSpec.DAML_MEM_RULE_INF;
        Model d = oms.getDescription();
        Resource reasonerURI = d.createResource( oms.getReasonerFactory().getURI() );
        Statement s = d.getRequiredProperty( null, JenaModelSpec.reasonsWith );
        Model reasonerSpec = ModelFactory.createDefaultModel()
            .add( d.createResource(), JenaModelSpec.reasoner, reasonerURI );
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
        Resource lang = spec.createResource( oms.getLanguage() );
        Resource me = resource();
        Resource factory = spec.createResource( oms.getReasonerFactory().getURI() );
        spec.add( me, JenaModelSpec.ontLanguage, lang );
        Resource r = spec.createResource();
        spec.add( r, JenaModelSpec.reasoner, factory );
        spec.add( me, JenaModelSpec.reasonsWith, r );
        Resource m = spec.createResource();
        Model modelMaker = ModelFactory.createDefaultModel();
        modelMaker.add( m, RDF.type, JenaModelSpec.MemMakerSpec );
        modelMaker.add( m, JenaModelSpec.reificationMode, JenaModelSpec.rsStandard );
        spec.add( me, JenaModelSpec.importMaker, m );
        spec.add( modelMaker );
        OntDocumentManager odm = oms.getDocumentManager();
        Resource dm = ModelSpecImpl.createValue( odm );
        spec.add( me, JenaModelSpec.docManager, dm );
    /* */
        OntModelSpec ms = new OntModelSpec( spec );
        assertEquals( lang.getURI(), ms.getLanguage() );
        assertEquals( factory.getURI(), ms.getReasonerFactory().getURI() );
        assertIsoModels( modelMaker, ms.getImportModelMaker().getDescription() );
        assertSame( odm, ms.getDocumentManager() );
        }
    
    public void testCreateOntSpecWithoutMaker()
        {
        OntModelSpec oms = OntModelSpec.OWL_MEM_RULE_INF;
        Model spec = ModelFactory.createDefaultModel();
        Resource lang = spec.createResource( oms.getLanguage() );
        Resource me = resource();
        Resource factory = spec.createResource( oms.getReasonerFactory().getURI() );
        spec.add( me, JenaModelSpec.ontLanguage, lang );
        Resource r = spec.createResource();
        spec.add( r, JenaModelSpec.reasoner, factory );
        spec.add( me, JenaModelSpec.reasonsWith, r );
        OntDocumentManager odm = oms.getDocumentManager();
        Resource dm = ModelSpecImpl.createValue( odm );
        spec.add( me, JenaModelSpec.docManager, dm );
    /* */
        OntModelSpec ms = new OntModelSpec( spec );
        assertEquals( lang.getURI(), ms.getLanguage() );
        assertEquals( factory.getURI(), ms.getReasonerFactory().getURI() );
        assertSame( odm, ms.getDocumentManager() );
        }
    
    public void testCreateOntSpecWithoutDocmanager()
        {
        OntModelSpec oms = OntModelSpec.OWL_MEM_RULE_INF;
        Model spec = ModelFactory.createDefaultModel();
        Resource lang = spec.createResource( oms.getLanguage() );
        Resource me = resource();
        Resource factory = spec.createResource( oms.getReasonerFactory().getURI() );
        spec.add( me, JenaModelSpec.ontLanguage, lang );
        Resource r = spec.createResource();
        spec.add( r, JenaModelSpec.reasoner, factory );
        spec.add( me, JenaModelSpec.reasonsWith, r );
        Resource m = spec.createResource();
        Model modelMaker = ModelFactory.createDefaultModel();
        modelMaker.add( m, RDF.type, JenaModelSpec.MemMakerSpec );
        modelMaker.add( m, JenaModelSpec.reificationMode, JenaModelSpec.rsStandard );
        spec.add( me, JenaModelSpec.importMaker, m );
        spec.add( modelMaker );
    /* */
        OntModelSpec ms = new OntModelSpec( spec );
        assertEquals( lang.getURI(), ms.getLanguage() );
        assertEquals( factory.getURI(), ms.getReasonerFactory().getURI() );
        assertIsoModels( modelMaker, ms.getImportModelMaker().getDescription() );
        }
    
    public void testCreateOntSpecWithoutReasoner()
        {
        OntModelSpec oms = OntModelSpec.OWL_MEM_RULE_INF;
        Model spec = ModelFactory.createDefaultModel();
        Resource lang = spec.createResource( oms.getLanguage() );
        Resource me = resource();
        spec.add( me, JenaModelSpec.ontLanguage, lang );
        Resource m = spec.createResource();
        Model modelMaker = ModelFactory.createDefaultModel();
        modelMaker.add( m, RDF.type, JenaModelSpec.MemMakerSpec );
        modelMaker.add( m, JenaModelSpec.reificationMode, JenaModelSpec.rsStandard );
        spec.add( me, JenaModelSpec.importMaker, m );
        spec.add( modelMaker );
        OntDocumentManager odm = oms.getDocumentManager();
        Resource dm = ModelSpecImpl.createValue( odm );
        spec.add( me, JenaModelSpec.docManager, dm );
    /* */
        OntModelSpec ms = new OntModelSpec( spec );
        assertEquals( lang.getURI(), ms.getLanguage() );
        assertIsoModels( modelMaker, ms.getImportModelMaker().getDescription() );
        assertSame( odm, ms.getDocumentManager() );
        }
    
    protected List list( String element )
        {
        List result = new ArrayList();
        result.add( element );
        return result;
        }
    
    public void testCreateFailingMaker()
        {
        try
            { ModelSpecImpl.createMaker( modelWithStatements( "" ) );
            fail( "should generate BadDescriptionException" ); }   
        catch (BadDescriptionException e)
            { pass(); } 
        }
                
    public void testCreateMemModelMaker()
        {
        Resource mem = JenaModelSpec.MemMakerSpec;
        testCreateModelMaker( JenaModelSpec.rsStandard, mem, SimpleGraphMaker.class );
        testCreateModelMaker( JenaModelSpec.rsMinimal, mem, SimpleGraphMaker.class );
        testCreateModelMaker( JenaModelSpec.rsConvenient, mem, SimpleGraphMaker.class );
        }

    public void testCreateFileModelMaker()
        {
        Resource file =JenaModelSpec.FileMakerSpec;
        testCreateModelMaker( JenaModelSpec.rsStandard, file, FileGraphMaker.class );
        testCreateModelMaker( JenaModelSpec.rsMinimal, file, FileGraphMaker.class );
        testCreateModelMaker( JenaModelSpec.rsConvenient, file, FileGraphMaker.class );
        }
        
    public void testCreateFileModelMakerRooted()
        {
        String fileBase = "/somewhere";
        Resource me = resource();
        Model spec = ModelFactory.createDefaultModel()
            .add( me, RDF.type, JenaModelSpec.FileMakerSpec )
            .add( me, JenaModelSpec.fileBase, fileBase )
            ;
        ModelMaker maker = ModelSpecImpl.createMaker( spec );
        FileGraphMaker fgm = (FileGraphMaker) maker.getGraphMaker();
        assertEquals( fileBase, fgm.getFileBase() );
    /* */
        Model desc = ModelFactory.createModelForGraph( fgm.getDescription() );
        assertTrue( desc.listStatements( null, JenaModelSpec.fileBase, fileBase ).hasNext() );        
        }
        
    public void testCreateModelMaker( Resource style, Resource cl, Class required )
        {
        Resource me = resource();
        ReificationStyle wanted = JenaModelSpec.findStyle( style );
        Model spec = modelWithStatements( "" )
            .add( me, RDF.type, cl )
            .add( me, JenaModelSpec.reificationMode, style );
        ModelMaker maker = ModelSpecImpl.createMaker( spec );
        assertTrue( required.isInstance( maker.getGraphMaker() ) );
        assertEquals( wanted, maker.getGraphMaker().getReificationStyle() );
        }
                

	/**
	    Answer a description of a plain memory Model with Minimal reification; the root
        resource is a fresh bnode.
	*/
	private static Model createPlainModelDesc()
	    { return createPlainModelDesc( resource() ); }

    /**
        Answer a description of a plain memory Model with Minimal reification; the root
        resource is supplied.
    */        
    private static Model createPlainModelDesc( Resource root )
        { return createPlainModelDesc( root, resource() ); }
        
    private static Model createPlainModelDesc( Resource root, Resource maker )
        { return createPlainModelDesc( root, maker, JenaModelSpec.MemMakerSpec ); }
        
    private static Model createPlainModelDesc( Resource root, Resource maker, Resource spec )
        {
        return ModelFactory.createDefaultModel()
            .add( root, JenaModelSpec.maker, maker )
            .add( maker, RDF.type, spec )
            .add( maker, JenaModelSpec.reificationMode, JenaModelSpec.rsMinimal );
        }
        
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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