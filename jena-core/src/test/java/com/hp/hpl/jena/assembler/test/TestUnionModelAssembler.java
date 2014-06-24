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

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.AddDeniedException;

public class TestUnionModelAssembler extends AssemblerTestBase
    {
    public TestUnionModelAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return UnionModelAssembler.class; }

    public void testUnionModelAssemblerType()
        { testDemandsMinimalType( new UnionModelAssembler(), JA.UnionModel );  }

    public void testUnionVocabulary()
        {
        assertSubclassOf( JA.UnionModel, JA.Model );
        assertDomain( JA.UnionModel, JA.subModel );
        assertRange( JA.Model, JA.subModel );
        assertDomain( JA.UnionModel, JA.rootModel );
        assertRange( JA.Model, JA.rootModel );
        }

    public void testCreatesMultiUnion() 
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel" );
        Assembler a = new UnionModelAssembler();
        Model m = a.openModel( root );
        assertInstanceOf( MultiUnion.class, m.getGraph() );
        checkImmutable( m );
        }

    private void checkImmutable( Model m )
        {
        try { m.add( statement( "S P O" ) ); fail( "should be immutable" ); }
        catch (AddDeniedException e) { pass(); }
        }
    
    static class SmudgeAssembler extends AssemblerBase 
        {
        Map<Resource, Model> map = new HashMap<>();
        
        public SmudgeAssembler add( String name, Model m )
            {
            map.put( resource( name ), m );
            return this;
            }

        @Override
        public Model openModel( Resource root, Mode mode )
            { return (Model) open( this, root, mode ); }
        
        @Override
        public Object open( Assembler a, Resource root, Mode irrelevant )
            { return map.get( root ); }
        }
    
    public void testCreatesUnionWithSubModels()
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel; x ja:subModel A; x ja:subModel B" );
        Assembler a = new UnionModelAssembler();
        Model modelA = model( "" ), modelB = model( "" );
        Set<Graph> expected = new HashSet<>(); expected.add( modelA.getGraph() ); expected.add( modelB.getGraph() );
        Assembler mock = new SmudgeAssembler().add( "A", modelA ).add( "B", modelB );
        Model m = (Model) a.open( mock, root );
        assertInstanceOf( MultiUnion.class, m.getGraph() );
        MultiUnion mu = (MultiUnion) m.getGraph();
        List<Graph> L = mu.getSubGraphs();
        assertEquals( expected, new HashSet<>( L ) );
        checkImmutable( m );
        }
    
    public void testSubModelsCheckObject()
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel; x ja:subModel 'A'" );
        Assembler a = new UnionModelAssembler();
        try 
            { 
            a.open( root ); 
            fail( "should trap unsuitable object" );
            }
        catch (BadObjectException e) 
            { 
            assertEquals( resource( "x" ), e.getRoot() ); 
            assertEquals( rdfNode( empty, "'A'" ), e.getObject() );
            }
        }
    
    public void testCreatesUnionWithBaseModel()
        {
        Resource root = resourceInModel( "x rdf:type ja:UnionModel; x ja:subModel A; x ja:rootModel B" );
        Assembler a = new UnionModelAssembler();
        Model modelA = model( "" ), modelB = model( "" );
        Set<Graph> expected = new HashSet<>(); expected.add( modelA.getGraph() );
        Assembler mock = new SmudgeAssembler().add( "A", modelA ).add( "B", modelB );
        Model m = (Model) a.open( mock, root );
        assertInstanceOf( MultiUnion.class, m.getGraph() );
    //
        MultiUnion mu = (MultiUnion) m.getGraph();
        assertSame( modelB.getGraph(), mu.getBaseGraph() );
        assertEquals( listOfOne( modelA.getGraph() ), mu.getSubGraphs() );
        m.add( statement( "a P b" ) );
        assertIsoModels( model( "a P b" ), modelB );
        }
    }
