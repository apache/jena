/*
 	(c) Copyright 2005, Hewlett-Packard Development Company, LP
 	All rights reserved.
 	[See end of file]
*/

package com.hp.hpl.jena.rdf.model.test;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecFactory;
import com.hp.hpl.jena.shared.BadDescriptionException;


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
    
    protected ModelSpecCreator createMock = new ModelSpecCreator()
        {
        public ModelSpec create( Resource root, Model desc )
            { return new MockSpec(); }
        };
    
    protected static class MockSpec implements ModelSpec
        {
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

        public Model getExistingModel( String name )
            { return null; }
        }


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