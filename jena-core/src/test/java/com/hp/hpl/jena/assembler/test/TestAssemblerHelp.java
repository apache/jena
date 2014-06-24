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
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestAssemblerHelp extends AssemblerTestBase
    {
    public TestAssemblerHelp( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { throw new BrokenException( "TestAssemblers does not need this method" ); }
    
    public void testClosureFootprint()
        {
        Resource root = resourceInModel( "x ja:reasoner y" );
        Statement footprint = root.getModel().createStatement( JA.This, RDF.type, JA.Expanded );
        assertFalse( root.getModel().contains( footprint ) );
        Resource expanded = AssemblerHelp.withFullModel( root );
        assertTrue( expanded.getModel().contains( footprint ) );
        }
    
    public void testFootprintPreventsClosure()
        {
        Resource root = resourceInModel( "x ja:reasoner y; ja:this rdf:type ja:Expanded" );
        Model original = model( "" ).add( root.getModel() );
        Resource expanded = AssemblerHelp.withFullModel( root );
        assertSame( root, expanded );
        assertIsoModels( original, expanded.getModel() );
        }
      
    public void testSpecificType()
        {
        testSpecificType( "ja:NamedModel", "x ja:modelName 'name'" );
        testSpecificType( "ja:NamedModel", "x ja:modelName 'name'; x rdf:type irrelevant" );
        }
    
    public void testFindSpecificTypes()
        {
        testFindSpecificTypes( "", "x rdf:type A", "Top" );
        testFindSpecificTypes( "", "x rdf:type A; x rdf:type B", "Top" );
        testFindSpecificTypes( "A", "x rdf:type A; A rdfs:subClassOf Top", "Top" );
        testFindSpecificTypes( "A", "x rdf:type A; x rdf:type B; A rdfs:subClassOf Top", "Top" );
        testFindSpecificTypes( "A B", "x rdf:type A; x rdf:type B; A rdfs:subClassOf Top; B rdfs:subClassOf Top", "Top" );
        testFindSpecificTypes( "B", "x rdf:type A; x rdf:type B; A rdfs:subClassOf Top; B rdfs:subClassOf Top; B rdfs:subClassOf A", "Top" );
        }
    
    private void testFindSpecificTypes( String expectedString, String model, String baseString )
        {
        Resource root = resourceInModel( model );
        Resource baseType = resource( baseString );
        Set<Resource> expected = resourceSet( expectedString );
        Set<Resource> answer = AssemblerHelp.findSpecificTypes( root, baseType );
        assertEquals( expected, answer );
        }

    public void testFindRootByExplicitType()
        {
        Model model = model( "x rdf:type ja:Object; y rdf:type Irrelevant" );
        Set<Resource> roots = AssemblerHelp.findAssemblerRoots( model );
        assertEquals( resourceSet( "x" ), roots );
        }        
    
    public void testFindRootByImplicitType()
        {
        Model model = model( "x ja:reificationMode ja:Standard" );
        Set<Resource> roots = AssemblerHelp.findAssemblerRoots( model );
        assertEquals( resourceSet( "x" ), roots );
        }
    
    public void testFindMultipleRoots()
        {
        Model model = model( "x rdf:type ja:Object; y ja:reificationMode ja:Minimal" );
        Set<Resource> roots = AssemblerHelp.findAssemblerRoots( model );
        assertEquals( resourceSet( "y x" ), roots );
        }
    
    public void testFindRootsWithSpecifiedType()
        {
        Model model = model( "x rdf:type ja:Model; y rdf:type ja:Object" );
        Set<Resource> roots = AssemblerHelp.findAssemblerRoots( model, JA.Model );
        assertEquals( resourceSet( "x" ), roots );
        }
    
    public void testThrowsIfNoRoots()
        {
        try 
            { AssemblerHelp.singleModelRoot( model( "" )  ); 
            fail( "should trap if no roots" ); }
        catch (BadDescriptionNoRootException e) { pass(); }
        }
    
    public void testThrowsIfManyRoots()
        {
        try 
            { AssemblerHelp.singleModelRoot( model( "a rdf:type ja:Model; b rdf:type ja:Model" )  )
            ; fail( "should trap if many roots" ); }
        catch (BadDescriptionMultipleRootsException e) { pass(); }
        }
    
    public void testExtractsSingleRoot()
        {
        Resource it = AssemblerHelp.singleModelRoot( model( "a rdf:type ja:Model" )  );
        assertEquals( resource( "a" ), it );
        }
    
    public void testSpecificTypeFails()
        {
        try
            {
            testSpecificType( "xxx", "x rdf:type ja:Model; x rdf:type ja:PrefixMapping" );
            fail( "should trap multiple types" );
            }
        catch (AmbiguousSpecificTypeException e)
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( resources( e.getRoot(), "ja:Model ja:PrefixMapping" ), new HashSet<>( e.getTypes() ) );
            }
        }

    private Set<Resource> resources( Resource root, String items )
        {
        List<String> L = listOfStrings( items );
        Set<Resource> result = new HashSet<>();
            for ( String aL : L )
            {
                result.add( resource( root.getModel(), aL ) );
            }
        return result;
        }

    private void testSpecificType( String expected, String specification )
        { // TODO relies on fullModel, would be nice to remove this dependency
        Resource root = resourceInModel( specification );
        Resource rooted = root.inModel( AssemblerHelp.fullModel( root.getModel() ) );
        Resource mst = AssemblerHelp.findSpecificType( rooted );
        assertEquals( resource( root.getModel(), expected ), mst );
        }

    public static boolean impIsLoaded = false;
    public static boolean impIsConstructed = false;
    
    public static class Imp extends AssemblerBase
        {
        static { impIsLoaded = true; }

        public Imp()
            { impIsConstructed = true; }
        
        @Override
        public Object open( Assembler a, Resource root, Mode irrelevant )
            { return null; }
        }
    
    static Model gremlinModel = modelWithStatements( "eh:Wossname ja:assembler 'com.hp.hpl.jena.assembler.test.TestAssemblerHelp$Gremlin'" );
    
    static boolean gremlinInvoked = false;
    
    public static class Gremlin extends AssemblerBase
        {
        public Gremlin()
            { fail( "Gremlin no-argument constructor should not be called" ); }
        
        public Gremlin( Resource root )
            {
            assertEquals( resource( "eh:Wossname" ), root );
            assertIsoModels( gremlinModel, root.getModel() );
            gremlinInvoked = true;
            }
        
        @Override
        public Object open( Assembler a, Resource root, Mode irrelevant )
            { return null; }
        }
    
    public void testClassAssociation()
        {
        String className = "com.hp.hpl.jena.assembler.test.TestAssemblerHelp$Imp";
        AssemblerGroup group = AssemblerGroup.create();
        Model m = model( "eh:Wossname ja:assembler '" + className + "'" );
        assertEquals( false, impIsLoaded );
        AssemblerHelp.loadAssemblerClasses( group, m );
        assertEquals( true, impIsLoaded );
        assertEquals( true, impIsConstructed );
        assertEquals( className, group.assemblerFor( resource( "eh:Wossname" ) ).getClass().getName() );
        }    
    
    public void testClassResourceConstructor()
        {
        AssemblerGroup group = AssemblerGroup.create();
        Model m = model( "eh:Wossname ja:assembler 'com.hp.hpl.jena.assembler.test.TestAssemblerHelp$Gremlin'" );
        assertEquals( false, gremlinInvoked );
        AssemblerHelp.loadAssemblerClasses( group, m );
        assertEquals( true, gremlinInvoked );
        }
    }
