/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelSpec.java,v 1.38 2005-04-08 10:05:55 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.mem.*;
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
//        TestSuite s = new TestSuite();
//        s.addTest( new TestModelSpec( "testDefaultMaker" ) );
//        return s; }
        
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
        assertNotNull( ModelMakerCreatorRegistry.findCreator( JMS.FileMakerSpec ) );   
        assertNotNull( ModelMakerCreatorRegistry.findCreator( JMS.MemMakerSpec ) );   
        assertNotNull( ModelMakerCreatorRegistry.findCreator( JMS.RDBMakerSpec ) );    
        }
    
    public void testDefaultMaker()
        {
        Model spec = modelWithStatements( "_x jms:maker _y; _y jms:reificationMode jms:rsMinimal" );
        ModelSpec ms = ModelFactory.createSpec( spec );
        Model m = ModelFactory.createModel( ms ) ;
        assertTrue( m.getGraph() instanceof GraphMem );
        }
    
    public void testAbsentDefaultMaker()
        {
        Model spec = modelWithStatements( "_x rdf:type jms:DefaultModelSpec" );
        ModelSpec ms = ModelFactory.createSpec( spec );
        Model m = ModelFactory.createModel( ms ) ;
        assertTrue( m.getGraph() instanceof GraphMem );
        }
        
//    /** a spec with no maker should throw an exception 
//    */
//    public void testMakerlessException()
//        {
//        Model spec = modelWithStatements( "_x rdf:type jms:MemModelSpec; _x rdf:type jms:PlainModelSpec; _x rdf:type jms:ModelSpec" );
//        try 
//            { ModelSpec ms = ModelFactory.createSpec( spec ); 
//            fail( "makerless spec should throw a BadDescription exception" ); }
//        catch (BadDescriptionException e) { pass(); }
//        }
    
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
        
    public void testHasStandardCreators()
        {
        assertNotNull( ModelSpecCreatorRegistry.findCreator( JMS.InfModelSpec ) );  
        assertNotNull( ModelSpecCreatorRegistry.findCreator( JMS.PlainModelSpec ) );   
        assertNotNull( ModelSpecCreatorRegistry.findCreator( JMS.OntModelSpec ) );     
        }
    
    public void testNamedCreatePlain()
        {
        ModelSpec ms = ModelSpecFactory.createSpec( createPlainModelDesc() );    
        Model m = ms.createModelOver( "aName" );
        assertTrue( m.getGraph() instanceof GraphMem );
        }   

    public void testNamedCreateInf()
        {
        String URI = DAMLMicroReasonerFactory.URI;
        ModelSpec ms = ModelSpecFactory.createSpec( createInfModelDesc( URI ) );    
        Model m = ms.createModelOver( "iName" );
        assertTrue( m.getGraph() instanceof InfGraph );
        }   
        
    public void testDetectRootAmbiguity()
        {
        Model desc = createPlainModelDesc().add( createPlainModelDesc() );
        try { ModelSpecFactory.createSpec( desc ); fail( "must trap ambiguous description" ); }
        catch (BadDescriptionException b) { pass(); }
        }
                                  
    public void testCreateByName()
        {
        Resource plain = resource();
        Model desc = createPlainModelDesc( plain );
        ModelSpec ms = ModelSpecFactory.createSpec( ModelSpecFactory.withSchema( desc ), plain );  
        assertTrue( ms.createModel().getGraph() instanceof GraphMem );  
        }
        
    public void testCreateByNameChoice()
        {
        Resource plain = resource();
        Resource inf = resource();
        String URI = DAMLMicroReasonerFactory.URI;
        Model desc = createPlainModelDesc( plain ).add( createInfModelDesc( inf, URI ) );
        ModelSpec ms = ModelSpecFactory.createSpec( ModelSpecFactory.withSchema( desc ), plain );  
        assertTrue( ms.createModel().getGraph() instanceof GraphMem );  
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
    /* */
        assertTrue( "", d.contains( null, JMS.ontLanguage, TestModelFactory.DAMLLangResource ) );
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
        Model makerSpec = oms.getImportModelMaker().getDescription();
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
        Resource lang = spec.createResource( oms.getLanguage() );
        Resource me = resource();
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
        Resource dm = ModelSpecImpl.createValue( odm );
        spec.add( me, JMS.docManager, dm );
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
        spec.add( me, JMS.ontLanguage, lang );
        Resource r = spec.createResource();
        spec.add( r, JMS.reasoner, factory );
        spec.add( me, JMS.reasonsWith, r );
        OntDocumentManager odm = oms.getDocumentManager();
        Resource dm = ModelSpecImpl.createValue( odm );
        spec.add( me, JMS.docManager, dm );
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
        spec.add( me, JMS.ontLanguage, lang );
        Resource m = spec.createResource();
        Model modelMaker = ModelFactory.createDefaultModel();
        modelMaker.add( m, RDF.type, JMS.MemMakerSpec );
        modelMaker.add( m, JMS.reificationMode, JMS.rsStandard );
        spec.add( me, JMS.importMaker, m );
        spec.add( modelMaker );
        OntDocumentManager odm = oms.getDocumentManager();
        Resource dm = ModelSpecImpl.createValue( odm );
        spec.add( me, JMS.docManager, dm );
    /* */
        OntModelSpec ms = new OntModelSpec( spec );
        assertEquals( lang.getURI(), ms.getLanguage() );
        assertIsoModels( modelMaker, ms.getImportModelMaker().getDescription() );
        assertSame( odm, ms.getDocumentManager() );
        }

    public void testCreateFailingMaker()
        {
        try
            { ModelSpecImpl.createMaker( modelWithStatements( "" ) );
            fail( "should generate BadDescriptionException" ); }   
        catch (BadDescriptionException e)
            { pass(); } 
        }
        
    protected static void writeModel( File f, Model m ) throws FileNotFoundException, IOException
        {
        FileOutputStream fos = new FileOutputStream( f );
        m.write( fos );
        fos.close();    
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
        Resource me = resource();
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
        Resource me = resource();
        ReificationStyle wanted = JMS.findStyle( style );
        Model spec = modelWithStatements( "" )
            .add( me, RDF.type, cl )
            .add( me, JMS.reificationMode, style );
        ModelMaker maker = ModelSpecImpl.createMaker( spec );
        assertTrue( required.isInstance( maker.getGraphMaker() ) );
        assertEquals( wanted, maker.getGraphMaker().getReificationStyle() );
        }
                
    public void testCreatePlainMemModel()
        {
        Resource me = resource();
        Model spec = createPlainModelDesc( me );
        PlainModelSpec pms = new PlainModelSpec( me, spec );
        ModelMaker mm = pms.getModelMaker();
        Model desc = mm.getDescription( me );
        assertTrue( desc.contains( me, RDF.type, JMS.MemMakerSpec ) );
        assertTrue( desc.listStatements( null, JMS.reificationMode, JMS.rsMinimal ).hasNext() );
        assertTrue( mm.getGraphMaker() instanceof SimpleGraphMaker );
        assertEquals( ReificationStyle.Minimal , mm.getGraphMaker().getReificationStyle() );
        }
        
    public void testCreatePlainFileModel()
        {
        Resource me = resource();
        Resource maker = resource();
        Model spec = createPlainModelDesc( me, maker, JMS.FileMakerSpec ); 
        PlainModelSpec pms = new PlainModelSpec( me, spec );
        ModelMaker mm = pms.getModelMaker();
        Model desc = mm.getDescription( me );
        assertTrue( desc.listStatements( null, RDF.type, JMS.FileMakerSpec ).hasNext() );
        assertTrue( desc.listStatements( null, JMS.reificationMode, JMS.rsMinimal ).hasNext() );
        assertTrue( mm.getGraphMaker() instanceof FileGraphMaker );
        assertEquals( ReificationStyle.Minimal , mm.getGraphMaker().getReificationStyle() );
        }

	/**
	    Answer a description of a plain memory Model with Minimal reification; the root
        resource is a fresh bnode.
	*/
	public static Model createPlainModelDesc()
	    { return createPlainModelDesc( resource() ); }

    /**
        Answer a description of a plain memory Model with Minimal reification; the root
        resource is supplied.
    */        
    public static Model createPlainModelDesc( Resource root )
        { return createPlainModelDesc( root, resource() ); }
        
    public static Model createPlainModelDesc( Resource root, Resource maker )
        { return createPlainModelDesc( root, maker, JMS.MemMakerSpec ); }
        
    public static Model createPlainModelDesc( Resource root, Resource maker, Resource spec )
        {
        return ModelFactory.createDefaultModel()
            .add( root, JMS.maker, maker )
            .add( maker, RDF.type, spec )
            .add( maker, JMS.reificationMode, JMS.rsMinimal );
        }
                                                                
    public static Model createInfModelDesc( String URI )
        { return createInfModelDesc( resource(), URI ); }
        
    public static Model createInfModelDesc( Resource root, String URI )
        {
        Resource maker = resource();
        Resource reasoner = resource();
        Resource res = resource( URI );
        return ModelFactory.createDefaultModel()
            .add( root, JMS.reasonsWith, reasoner )
            .add( reasoner, JMS.reasoner, res )
            .add( root, JMS.maker, maker )
            .add( maker, RDF.type, JMS.MemMakerSpec )
            .add( maker, JMS.reificationMode, JMS.rsMinimal )
            ;
        }
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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