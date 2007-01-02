/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    [See end of file]
    $Id: TestModelSpecImplLoadFiles.java,v 1.4 2007-01-02 11:49:24 andy_seaborne Exp $
*/
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