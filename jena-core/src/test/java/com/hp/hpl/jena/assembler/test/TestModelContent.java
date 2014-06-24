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
import com.hp.hpl.jena.assembler.exceptions.TransactionAbortedException;
import com.hp.hpl.jena.rdf.model.*;

public class TestModelContent extends AssemblerTestBase
    {
    public TestModelContent( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return null; }
    
    public void testMemoryModelLoadsSingleContent()
        {
        testModelLoadsSingleContent( Assembler.memoryModel, JA.MemoryModel );
        }
    
    public void testMemoryModelLoadsMultipleContent()
        {
        testModelLoadsMultipleContent( Assembler.memoryModel, JA.MemoryModel );
        }
    
    public void testDefaultModelLoadsSingleContent()
        {
        testModelLoadsSingleContent( Assembler.defaultModel, JA.DefaultModel );
        }
    
    public void testDefaultModelLoadsMultipleContent()
        {
        testModelLoadsMultipleContent( Assembler.defaultModel, JA.DefaultModel );
        }
    
    public void testInfModelLoadsContent()
        {
        testModelLoadsMultipleContent( Assembler.infModel, JA.InfModel );
        }

    public void testContentTransactionsNone()
        {
        final List<String> history = new ArrayList<>();
        final Model expected = model( "_x rdf:value '17'xsd:integer" );
        Assembler a = new MockTransactionModel( history, expected, false, true );
        Resource root = resourceInModel
            ( "x rdf:type ja:Model; x ja:content y; y rdf:type ja:Content; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17\\s.'" );
        try { a.open( Assembler.content, root  ); }
        catch (RuntimeException e) {}
        }
    
    public void testContentTransactionsCommit()
        {
        final List<String> history = new ArrayList<>();
        final Model expected = model( "_x rdf:value '17'xsd:integer" );
        Assembler a = new MockTransactionModel( history, expected, true, false );
        Resource root = resourceInModel
            ( "x rdf:type ja:Model; x ja:content y; y rdf:type ja:Content; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17\\s.'" );
        Model m = (Model) a.open( Assembler.content, root  );
        assertEquals( listOfStrings( "supports[true] begin add commit" ), history );
        assertIsoModels( expected, m );
        }

    public void testContentTransactionsAbort()
        {
        final List<String> history = new ArrayList<>();
        final Model expected = model( "_x rdf:value '17'xsd:integer" );
        final Model toDeliver = model( "" ).add( expected );
        Assembler a = new MockTransactionModel( history, toDeliver, true, true );
        try
            {
            Resource root = resourceInModel
                ( "x rdf:type ja:Model; x ja:content y; y rdf:type ja:Content"
                + "; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17\\s.'" );
            a.open( Assembler.content, root  );
            fail( "should throw (wrapped) failing exception" );
            }
        catch (TransactionAbortedException  e)
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( listOfStrings( "supports[true] begin add abort" ), history );
            assertIsoModels( expected, toDeliver );
            }        
        }
    
    protected void testModelLoadsSingleContent( Assembler a, Resource type )
        {
        Resource root = resourceInModel
            ( "x rdf:type " + type + "; x ja:content y; y rdf:type ja:Content"
            + "; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17\\s.'" );
        Model m = (Model) a.open( Assembler.content, root );
        assertIsoModels( model( "_x rdf:value '17'xsd:integer" ), m );
        }

    protected void testModelLoadsMultipleContent( Assembler a, Resource type )
        {
        Model m = (Model) a.open( Assembler.content, resourceInModel
                ( "x rdf:type " + type + "; x ja:content y; x ja:content z"
                + "; y rdf:type ja:Content; y rdf:type ja:LiteralContent; y ja:literalContent '_:x\\srdf:value\\s17\\s.'"
                + "; z rdf:type ja:Content; z rdf:type ja:LiteralContent; z ja:literalContent '_:x\\srdf:value\\s42\\s.'" ) );
        assertIsoModels( model( "_x rdf:value '17'xsd:integer; _y rdf:value '42'xsd:integer" ), m );
        }
    }
