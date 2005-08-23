package com.hp.hpl.jena.rdf.model.spec.test;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecImpl;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

/**
    Tests to ensure that ModelSpecImpl always runs loadFiles over any model
    it gets/creates.
*/
public class TestModelSpecImplLoadFiles extends ModelTestBase
    {
    public TestModelSpecImplLoadFiles(String name)
        { super( name ); }

    public static Test suite()
        { return new TestSuite( TestModelSpecImplLoadFiles.class ); }

    protected static class TestingModelSpec extends ModelSpecImpl
        {        
        public TestingModelSpec( Resource root, Model description )
            { super( root, description ); }

        public Model implementCreateModelOver( String name )
            { return ModelFactory.createDefaultModel(); }
        
        protected Model doCreateModel()
            { return ModelFactory.createDefaultModel(); }

        public Property getMakerProperty()
            { return null; }
        
        protected Model loadFile( Model m, Resource fileName )
            { return m.add( anchor, loaded, fileName ); }
        }
    
    protected static final Resource anchor = resource( "anchor" );
    
    protected static final Property loaded = property( "loaded" );
    
    protected ModelSpec getImpl()
        { 
        Model description = modelWithStatements( "root jms:loadWith file:quinx" );
        Resource root = resource( description, "root" );
        ModelSpecImpl result = new TestingModelSpec( root, description ); 
        result.getModelMaker().createModel( "aName" );
        result.getModelMaker().getGraphMaker().getGraph();
        return result;
        }
    
    public void testCreateModelOver()
        {
        checkLoaded( getImpl().createModelOver( "aName" ) );
        }
    
    public void testOpenModel()
        {
        checkLoaded( getImpl().openModel() );
        }
    
    public void testOpenModelWithArg()
        {
        checkLoaded( getImpl().openModel( "aName" ) );
        }
    
    public void testCreateDefaultModel()
        {
        checkLoaded( getImpl().createDefaultModel() );
        }

    public void testCreateFreshModel()
        {
        checkLoaded( getImpl().createFreshModel() );
        }
    
    public void testOpenModelIfPresent()
        {
        checkLoaded( getImpl().openModelIfPresent( "aName" ) );
        }

    public void testGetModel()
        {
        checkLoaded( getImpl().getModel() );
        }
    
    public void testCreateModel()
        {
        checkLoaded( getImpl().createModel() );
        }
    
    protected void checkLoaded( Model model )
        {
        assertNotNull( model );
        assertIsoModels( modelWithStatements( "anchor loaded file:quinx" ), model );
        } 
    }
