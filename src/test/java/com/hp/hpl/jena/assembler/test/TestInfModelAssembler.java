/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
