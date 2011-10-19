/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestInfModelAssembler.java,v 1.1 2009-06-29 08:55:53 castagna Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.InfModelAssembler;
import com.hp.hpl.jena.assembler.exceptions.NotUniqueException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;

public class TestInfModelAssembler extends AssemblerTestBase
    {
    public TestInfModelAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return InfModelAssembler.class; }

    public void testLocationMapperAssemblerType()
        { testDemandsMinimalType( new InfModelAssembler(), JA.InfModel );  }
    
    public void testMockReasonersDifferent()
        { 
        Reasoner R = GenericRuleReasonerFactory.theInstance().create( null );
        assertNotSame( mockReasonerFactory( R ), mockReasonerFactory( R ) ); 
        }
    
    public void testInfModel()
        {
        Assembler a = Assembler.infModel;
        Model m = a.openModel( resourceInModel( "x rdf:type ja:InfModel" ) );
        assertInstanceOf( InfModel.class, m );
        }
    
    public void testInfModelType()
        { testDemandsMinimalType( Assembler.infModel, JA.InfModel ); }
    
    public void testGetsReasoner()
        {
        Reasoner R = GenericRuleReasonerFactory.theInstance().create( null );
        final ReasonerFactory RF = mockReasonerFactory( R );
        Assembler mock = new FixedObjectAssembler( RF );
        Resource root = resourceInModel( "x rdf:type ja:InfModel; x ja:reasoner R" );
        InfModel m = (InfModel) Assembler.infModel.open( mock, root );
        assertSame( R, m.getReasoner() );        
        }

    protected ReasonerFactory mockReasonerFactory( final Reasoner R )
        { 
        return new ReasonerFactory() 
            {
            @Override
            public Reasoner create( Resource configuration )
                { return R; }

            @Override
            public Model getCapabilities()
                { throw new RuntimeException( "mock doesn't do getCapabilities" ); }

            @Override
            public String getURI()
                { throw new RuntimeException( "mock doesn't do getURI" ); }
            };
        }
    
    public void testGetsSpecifiedModel()
        {
        Model base = ModelFactory.createDefaultModel();
        Resource root = resourceInModel( "x rdf:type ja:InfModel; x ja:baseModel M" );
        Assembler mock = new NamedObjectAssembler( resource( "M" ), base );
        InfModel inf = (InfModel) Assembler.infModel.open( mock, root );
        assertSame( base.getGraph(), inf.getRawModel().getGraph() );
        }
    
    public void testDetectsMultipleBaseModels()
        {
        Model base = ModelFactory.createDefaultModel();
        Resource root = resourceInModel( "x rdf:type ja:InfModel; x ja:baseModel M; x ja:baseModel M2" );
        Assembler mock = new FixedObjectAssembler( base );
        try 
            { Assembler.infModel.open( mock, root ); 
            fail( "should detect multiple baseModels" ); }
        catch (NotUniqueException e) 
            { assertEquals( JA.baseModel, e.getProperty() ); 
            assertEquals( resource( "x" ), e.getRoot() ); }
        }
    
    public void testDetectsMultipleReasoners()
        {
        Resource root = resourceInModel( "x rdf:type ja:InfModel; x ja:reasoner R; x ja:reasoner R2" );
        Assembler mock = new FixedObjectAssembler( null );
        try 
            { Assembler.infModel.open( mock, root ); 
            fail( "should detect multiple reasoners" ); }
        catch (NotUniqueException e) 
            { assertEquals( JA.reasoner, e.getProperty() ); 
            assertEquals( resource( "x" ), e.getRoot() ); }
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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