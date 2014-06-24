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
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TestImportManager extends AssemblerTestBase
    {
    public TestImportManager( String name )
        { super( name ); }    
    
    static class FixedFileManager extends FileManager
        {
        Map<String, Model> map = new HashMap<>();
        
        @Override public Model loadModel( String URL )
            {
            Model result = map.get( URL );
            if (result == null) fail( "no model for " + URL );
            return result;
            }
        
        public FixedFileManager add( String URL, Model m )
            {
            map.put( URL, m );
            return this;
            }
        }

    public void testFollowOwlImports()
        {
        final Model modelToLoad = model( "this hasMarker B5" );
        Model  m = model( "x ja:reasoner y; _x owl:imports eh:/loadMe" );
        FileManager fm = new FixedFileManager().add( "eh:/loadMe", modelToLoad ); 
        Model m2 = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, m2.getGraph() );
        assertIsoModels( modelToLoad.union( m ), m2 );
        }
    
    public void testFollowJAImports()
        {
        final Model modelToLoad = model( "this hasMarker B5" );
        Model  m = model( "x ja:reasoner y; _x ja:imports eh:/loadMe" );
        FileManager fm = new FixedFileManager().add( "eh:/loadMe", modelToLoad ); 
        Model m2 = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, m2.getGraph() );
        assertIsoModels( modelToLoad.union( m ), m2 );
        }
    
    public void testImportMayBeLiteral()
        {
        final Model modelToLoad = model( "this hasMarker B5" );
        Model  m = model( "x ja:reasoner y; _x ja:imports 'eh:/loadMe'" );
        FileManager fm = new FixedFileManager().add( "eh:/loadMe", modelToLoad ); 
        Model m2 = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, m2.getGraph() );
        assertIsoModels( modelToLoad.union( m ), m2 );
        }
    
    public void testBadImportObjectFails()
        {
        testBadImportObjectFails( "_bnode" );
        testBadImportObjectFails( "17" );
        testBadImportObjectFails( "'chat'fr" );
        testBadImportObjectFails( "'chat'xsd:wrong" );
        }

    private void testBadImportObjectFails( String object )
        {
        String string = "x ja:imports " + object;
        Model m = model( string );
        try 
            { 
            new ImportManager().withImports( m );
            fail( "should trap bad import specification " + string );
            }
        catch (BadObjectException e)
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( rdfNode( m, object ), e.getObject() );
            }
        }
    
    public void testFollowOwlImportsDeeply()
        {
        final Model 
            m1 = model( "this hasMarker M1; _x owl:imports M2" ),
            m2 = model( "this hasMarker M2" );
        Model  m = model( "x ja:reasoner y; _x owl:imports M1" );
        FileManager fm = new FixedFileManager() 
            .add( "eh:/M1", m1 ).add( "eh:/M2", m2 );
        Model result = new ImportManager().withImports( fm, m );
        assertInstanceOf( MultiUnion.class, result.getGraph() );
        assertIsoModels( m1.union(m2).union(m), result );
        }
    
    public void testCatchesCircularity()
        {
        final Model 
            m1 = model( "this hasMarker Mx; _x owl:imports My" ),
            m2 = model( "this hasMarker My; _x owl:imports Mx" );
        FileManager fm = new FixedFileManager()
            .add( "eh:/Mx", m1 ).add( "eh:/My", m2 );
        Model result = new ImportManager().withImports( fm, m1 );
        assertIsoModels( m1.union( m2 ), result );
        }
    
    public void testCacheModels()
        {
        ImportManager im = new ImportManager();
        Model spec = model( "_x owl:imports M1" );
        Model m1 = model( "this isModel M1" );
        FileManager withM1 = new FixedFileManager().add( "eh:/M1", m1 );
        Model A = im.withImports( withM1, spec );
        FileManager none = new FixedFileManager();
        Model B = im.withImports( none, spec );
        assertIsoModels( A, B );
        }
    }
