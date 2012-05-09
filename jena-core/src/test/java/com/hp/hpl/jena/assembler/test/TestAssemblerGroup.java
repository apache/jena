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
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup.ExpandingAssemblerGroup;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.mem.GraphMemBase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestAssemblerGroup extends AssemblerTestBase
    {
    public TestAssemblerGroup( String name )
        { super( name );  }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return AssemblerGroup.class; }
    
    public void testEmptyAssemblerGroup()
        {
        AssemblerGroup a = AssemblerGroup.create();
        assertInstanceOf( AssemblerGroup.class, a );
        assertEquals( null, a.assemblerFor( resource( "ja:Anything" ) ) );
        checkFailsType( a, "rdf:Resource" );
        }

    protected void checkFailsType( Assembler a, String type )
        {
        try 
            { 
            a.open( resourceInModel( "x rdf:type " + type ) ); 
            fail( "should trap missing implementation" ); 
            }
        catch (NoSpecificTypeException e)
            {
            assertEquals( resource( "x" ), e.getRoot() );
            }
//        catch (NoImplementationException e) 
//            { 
//            assertEquals( resource( "x" ), e.getRoot() ); 
//            assertEquals( JA.Object, e.getType() );
//            assertNotNull( e.getAssembler() );
//            }
        }
    
    public static boolean loaded = false;
    
    public static class Trivial
        {
        static { loaded = true; }
        }
    
    public void testLoadsClasses()
        {
        AssemblerGroup a = AssemblerGroup.create();
        a.implementWith( resource( "T" ), new MockAssembler() );
        Resource root = resourceInModel( "x rdf:type T; _c ja:loadClass '" + TestAssemblerGroup.class.getName() + "$Trivial'" );
        assertFalse( "something has pre-loaded Trivial, so we can't test if it gets loaded", loaded );
        assertEquals( "mockmockmock", a.open( root ) );
        assertTrue( "the assembler group did not obey the ja:loadClass directive", loaded );
        }
    
    static class MockAssembler extends AssemblerBase
        {
        @Override
        public Object open( Assembler a, Resource root, Mode mode )
            { return "mockmockmock"; }
        }
    
    public void testSingletonAssemblerGroup()
        {
        AssemblerGroup a = AssemblerGroup.create();
        assertSame( a, a.implementWith( JA.InfModel, Assembler.infModel ) );
        a.openModel( resourceInModel( "x rdf:type ja:InfModel" ) );
        checkFailsType( a, "js:DefaultModel" );
        }
    
    public void testMultipleAssemblerGroup()
        {
        AssemblerGroup a = AssemblerGroup.create();
        assertSame( a, a.implementWith( JA.InfModel, Assembler.infModel ) );
        assertSame( a, a.implementWith( JA.MemoryModel, Assembler.memoryModel ) );
        assertInstanceOf( InfModel.class, a.openModel( resourceInModel( "x rdf:type ja:InfModel" ) ) );
        assertFalse( a.openModel( resourceInModel( "y rdf:type ja:MemoryModel" ) ) instanceof InfModel );
        checkFailsType( a, "js:DefaultModel" );
        }
    
    public void testImpliedType()
        {
        AssemblerGroup a = AssemblerGroup.create();
        Resource root = resourceInModel( "x ja:reasoner y" );
        Object expected = new Object();
        a.implementWith( JA.InfModel, new NamedObjectAssembler( resource( "x" ), expected ) );
        assertSame( expected, a.open( root ) );
        }
    
    public void testBuiltinGroup()
        {
        AssemblerGroup g = Assembler.general;
        assertInstanceOf( Model.class, g.open( resourceInModel( "x rdf:type ja:DefaultModel" ) ) );
        assertInstanceOf( InfModel.class, g.open( resourceInModel( "x rdf:type ja:InfModel" ) ) );
        assertMemoryModel( g.open( resourceInModel( "x rdf:type ja:MemoryModel" ) ) );
        }
    
    private static Assembler mockAssembler = new AssemblerBase() 
        {
        @Override
        public Object open( Assembler a, Resource root, Mode mode )
            { return null; }
        };
    
    public void testAddingImplAddsSubclass()
        {
        final Model [] fullModel = new Model[1];
        AssemblerGroup g = new AssemblerGroup.ExpandingAssemblerGroup()
            {
            @Override
            public void loadClasses( Model full ) { fullModel[0] = full; }
            };
        Resource root = resourceInModel( "root rdf:type typeA" );
        Resource typeA = resource( "typeA" ), typeB = resource( "typeB" );
        g.implementWith( typeA, mockAssembler );
        g.implementWith( typeB, mockAssembler );
        g.open( root );    
        assertTrue( fullModel[0].contains( typeA, RDFS.subClassOf, JA.Object ) );
        assertTrue( fullModel[0].contains( typeB, RDFS.subClassOf, JA.Object ) );
        }
    
    public static class ImplementsSPOO 
        {
        public static void whenRequiredByAssembler( AssemblerGroup g )
            {
            g.implementWith( resource( "SPOO" ), mockAssembler );
            }
        }
    
    public void testClassesLoadedBeforeAddingTypes()
        {
        String className = ImplementsSPOO.class.getName();
        Resource root = resourceInModel( "_root rdf:type ja:MemoryModel; _x ja:loadClass '" + className + "'" );
        ExpandingAssemblerGroup g = new AssemblerGroup.ExpandingAssemblerGroup();
        g.implementWith( resource( "ja:MemoryModel" ), mockAssembler );
        g.open( root );
        assertEquals( resourceSet( "SPOO ja:MemoryModel" ), g.implementsTypes() );
        }
    
    protected void assertMemoryModel( Object object )
        {
        if (object instanceof Model)
            {
            Graph g = ((Model) object).getGraph();
            assertInstanceOf( GraphMemBase.class, g );
            }
        else
            fail( "expected a Model, but got a " + object.getClass() );
        }
    
    public void testPassesSelfIn()
        {
        final AssemblerGroup group = AssemblerGroup.create();
        final Object result = new Object();
        Assembler fake = new AssemblerBase() 
            {
            @Override
            public Object open( Assembler a, Resource root, Mode irrelevant )
                {
                assertSame( "nested call should pass in assembler group:", group, a );
                return result;
                }
            };
        group.implementWith( JA.Object, fake );
        assertSame( result, group.open( resourceInModel( "x rdf:type ja:Object" ) ) );
        }
    
    public void testCopyPreservesMapping()
        {
        AssemblerGroup initial = AssemblerGroup
            .create()
            .implementWith(  JA.InfModel, new InfModelAssembler() )
            ;
        AssemblerGroup copy = initial.copy();
        assertSame( initial.assemblerFor( JA.InfModel ), copy.assemblerFor( JA.InfModel ) );
        }
    
    }
