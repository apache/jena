/*
 	(c) Copyright 2005, Hewlett-Packard Development Company, LP
 	All rights reserved.
 	[See end of file]
*/

package com.hp.hpl.jena.rdf.model.test;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecFactory;
import com.hp.hpl.jena.shared.*;


/**
    Test cases for ModelSpecFactory to ensure it returns a ModelSpec
    plausibly created by the associated ModelSpecCreator via the registry.
    @author kers
*/
public class TestModelSpecFactory extends ModelTestBase
    {
    public TestModelSpecFactory( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelSpecFactory.class ); }
    
    public void testFindUniqueRootByType()
        {
        Model m = modelWithStatements( "eh:x rdf:type eh:T; eh:y rdf:type eh:U" );
        assertEquals( resource( "eh:x" ), ModelSpecFactory.findRootByType( m, resource( "eh:T" ) ) );
        }
    
    public void testFindMissingRootByType()
        {
        Model m = modelWithStatements( "eh:y rdf:type eh:U" );
        Resource type = resource( "eh:T" );
        try 
            { ModelSpecFactory.findRootByType( m, type ); 
            fail( "should trap missing root" ); }
        catch (BadDescriptionNoRootException e) 
            { assertEquals( type, e.type ); 
            assertSame( m, e.badModel ); }
        }
    
    public void testFindMultipleRootByType()
        {
        Model m = modelWithStatements( "eh:x rdf:type eh:T; eh:y rdf:type eh:T" );
        Resource type = resource( "eh:T" );
        try 
            { ModelSpecFactory.findRootByType( m, type ); 
            fail( "should trap multiple roots" ); }
        catch (BadDescriptionMultipleRootsException e) 
            { assertEquals( type, e.type ); 
            assertSame( m, e.badModel ); }
        }
    
    public void testFindSpecificTypeTrivial()
        {
        Model m = fullModel( "eh:root rdf:type eh:T" );
        Resource T = resource( "eh:T" ), root = m.createResource( "eh:root" );
        assertEquals( T, ModelSpecFactory.findSpecificType( root, T ) );
        }    
    
    public void testFindSpecificTypeWithIrrelevantOtherType()
        {
        Model m = fullModel( "eh:root rdf:type eh:T; eh:root rdf:type eh:Other" );
        Resource T = resource( "eh:T" ), root = m.createResource( "eh:root" );
        assertEquals( T, ModelSpecFactory.findSpecificType( root, T ) );
        }
    
    public void testFindSpecificTypeWithSubtypes()
        {
        Model m = fullModel
            ( "eh:root rdf:type eh:V; eh:V rdfs:subClassOf eh:U; eh:U rdfs:subClassOf eh:T" );
        Resource root = m.createResource( "eh:root" );
        assertEquals( resource( "eh:V" ), ModelSpecFactory.findSpecificType( root, resource( "eh:T") ) );
        }        
    
    public void testFactoryReturnsAModelSpec()
        {
        Model m = modelWithStatements( "eh:Root rdf:type jms:PlainModelSpec" );
        Resource r = m.createResource( "eh:Root" );
        ModelSpec s = ModelSpecFactory.createSpec( m, r );
        }
    
    public void testModelAccessible()
        {
        Model m = modelWithStatements( "eh:Root rdf:type jms:PlainModelSpec" );
        Resource r = m.createResource( "eh:Root" );
        ModelSpec g = ModelSpecFactory.createSpec( m, r );
//        assertIsoModels( trim( r ), trim( g.getRoot() ) );
        // assertIsoModels( trim( r ), g.getDescription() );
        assertNotNull( g.getDescription() );
        }

    public void testFactoryNoRoot()
        {
        Model m = modelWithStatements( "" );
        try { ModelSpecFactory.createSpec( m ); }
        catch (BadDescriptionException e){ pass(); }
        }
    
    public void testGEMSmultipleRoots()
        {
        Model m = modelWithStatements( "eh:Root rdf:type jms:ModelSpec; eh:Fake rdf:type jms:PlainModelSpec" );
        try { ModelSpecFactory.createSpec( m ); }
        catch (BadDescriptionException e) { pass(); }
        }
    
    public void testDefaultCreate()
        {
        Model m = modelWithStatements( "eh:Root rdf:type jms:PlainModelSpec" );
        ModelSpec s = ModelSpecFactory.createSpec( m );
        Model x = s.createModel();
        assertNotNull( x );
        }
    
    public void testMatchingModelSpec()
        {
        Model m = modelWithStatements( "eh:Root rdf:type jms:PlainModelSpec" );
        ModelSpec s = ModelSpecFactory.createSpec( m );
        // TODO assertTrue( s instanceof BaseModelSpecImpl );
        }
    
    public void testLoadsCorrectModelSpec()
        {
        Model m = modelWithStatements( "eh:Root rdf:type eh:MockSpec; eh:MockSpec rdfs:subClassOf jms:ModelSpec" );
        ModelSpec s = ModelSpecFactory.createSpec( ModelSpecCreatorRegistry.registryWith( resource( "eh:MockSpec" ), createMock ), m );
        assertTrue( s instanceof MockSpec );        
        }    
    
    public void testCreateCreator()
        {
        String className = "com.hp.hpl.jena.rdf.model.test.MockModelSpec";
        ModelSpecCreator c = new ModelSpecCreatorWithClass( className );
        ModelSpec s = c.create( resource( "root" ), modelWithStatements( "" ) );
        assertEquals( className, s.getClass().getName() );
        }   
    
    public static class ModelSpecCreatorWithClass implements ModelSpecCreator
        {
        public ModelSpecCreatorWithClass( String className )
            {}
        
        public ModelSpec create( Resource root, Model desc )
            {
            return new MockModelSpec();
            }
        }
    
    protected ModelSpecCreator createMock = new ModelSpecCreator()
        {
        public ModelSpec create( Resource root, Model desc )
            { return new MockSpec(); }
        };
    
    protected static class MockSpec implements ModelSpec
        {
        public Model getModel()
            { return null; }
        
        public Model createModel()
            { return null; }

        public Model createModelOver( String name )
            { return null; }

        public Model getDescription()
            { return null;}

        public Model getDescription( Resource root )
            { return null; }

        public Model addDescription( Model m, Resource self )
            { return null; }

        public Model openModel( String name )
            { return null; }

        public Model openModelIfPresent( String name )
            { return null; }
        }

    /**
        Answer a model which is the RDFS closure of the statements encoded in
        the string <code>statements</code>.
    */
    protected Model fullModel( String statements )
        { return ModelSpecFactory.withSchema( modelWithStatements( statements ) ); }
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