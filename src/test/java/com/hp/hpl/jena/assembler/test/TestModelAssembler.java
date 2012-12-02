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
import com.hp.hpl.jena.assembler.exceptions.UnknownStyleException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

public class TestModelAssembler extends AssemblerTestBase
    {
    protected static final class FakeModelAssembler extends ModelAssembler
        {
        @Override protected Model openEmptyModel( Assembler a, Resource root, Mode mode )
            { return ModelFactory.createDefaultModel(); }
        }

    public TestModelAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return null; }
    
    public void testModelAssemblerVocabulary()
        {
        assertDomain( JA.Model, JA.reificationMode );
        assertRange( JA.ReificationMode, JA.reificationMode );
        assertType( JA.ReificationMode, JA.minimal );
        assertType( JA.ReificationMode, JA.standard );
        assertType( JA.ReificationMode, JA.convenient );
        }

    public void testContent()
        {
        Resource root = resourceInModel( "x rdf:type ja:DefaultModel; x ja:initialContent c; c ja:quotedContent A; A P B" );
//        root.getModel().write( System.err, "N3"  );
        Model m = (Model) new FakeModelAssembler().open( new ContentAssembler(), root, Mode.ANY );
        assertIsoModels( modelWithStatements( "A P B" ), m );
        }
    
    public void testGetsPrefixMappings()
        { 
        Assembler a = new FakeModelAssembler();
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "my", "urn:secret:42/" )
            .setNsPrefix( "your", "urn:public:17#" );
        Resource root = resourceInModel
            ( "x rdf:type ja:DefaultModel; x ja:prefixMapping p1; x ja:prefixMapping p2"
            + "; p1 rdf:type ja:PrefixMapping; p1 ja:prefix 'my'; p1 ja:namespace 'urn:secret:42/'"
            + "; p2 rdf:type ja:PrefixMapping; p2 ja:prefix 'your'; p2 ja:namespace 'urn:public:17#'" );
        Model m = (Model) a.open( Assembler.prefixMapping, root );
        assertSamePrefixMapping( wanted, m );
        }
    
    public void testGetsStandardReificationMode()
        {
        final List<ReificationStyle> style = new ArrayList<ReificationStyle>();
        Assembler a = new ModelAssembler() 
            {
            @Override
            protected Model openEmptyModel( Assembler a, Resource root, Mode irrelevant )
                {
                style.add( getReificationStyle( root ) );
                return ModelFactory.createDefaultModel(); 
                }
            };
        a.openModel( resourceInModel( "a rdf:type ja:Model" ) );
        assertEquals( listOfOne( ReificationStyle.Standard ), style );
        }
    
    @SuppressWarnings("deprecation")
    public void testGetsExplicitReificationMode()
        {
        testGetsStyle( "ja:minimal", ReificationStyle.Minimal );
        testGetsStyle( "ja:standard", ReificationStyle.Standard );
        testGetsStyle( "ja:convenient", ReificationStyle.Convenient );
        }
    
    public void testUnknownStyleFails()
        {
        try
            { 
            testGetsStyle( "unknown", ReificationStyle.Standard );
            fail( "should trap unknown reification style" );
            }
        catch (UnknownStyleException e)
            {
            assertEquals( resource( "unknown" ), e.getStyle() );
            assertEquals( resource( "a" ), e.getRoot() );
            }
        }

    private void testGetsStyle( String styleString, ReificationStyle style )
        {
        final List<ReificationStyle> styles = new ArrayList<ReificationStyle>();
        Assembler a = new ModelAssembler() 
            {
            @Override
            protected Model openEmptyModel( Assembler a, Resource root, Mode irrelevant )
                {
                styles.add( getReificationStyle( root ) );
                return ModelFactory.createDefaultModel(); 
                }
            };
        a.openModel( resourceInModel( "a rdf:type ja:Model; a ja:reificationMode " + styleString ) );
        assertEquals( listOfOne( style ), styles );
        }
    }
