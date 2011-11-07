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
import com.hp.hpl.jena.assembler.assemblers.ModelSourceAssembler;
import com.hp.hpl.jena.assembler.exceptions.PropertyRequiredException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.MemoryModelGetter;

public class TestModelSourceAssembler extends AssemblerTestBase
    {
    public TestModelSourceAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return ModelSourceAssembler.class; }

    public void testModelSourceAssemblerType()
        { testDemandsMinimalType( new ModelSourceAssembler(), JA.ModelSource );  }
   
    public void testModelSourceVocabulary()
        {
        assertDomain( JA.Connectable, JA.connection );
        assertRange( JA.Connection, JA.connection );
        assertSubclassOf( JA.Connectable, JA.Object );
        assertSubclassOf( JA.RDBModelSource, JA.Connectable );
        assertSubclassOf( JA.RDBModelSource, JA.ModelSource );
        }
    
    public void testDBSourceDemandsConnection()
        {
        Resource root = resourceInModel( "x rdf:type ja:ModelSource; x rdf:type ja:RDBModelSource" );
        Assembler a = new ModelSourceAssembler();
        try 
            { a.open( root ); fail( "should catch missing connection" ); }
        catch (PropertyRequiredException e) 
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( JA.connection, e.getProperty() );
            }
        }
    
    public void testMemModelMakerSource()
        {
        Assembler a = new ModelSourceAssembler();
        ModelGetter g = (ModelGetter) a.open( resourceInModel( "mg rdf:type ja:ModelSource" ) );
        assertInstanceOf( MemoryModelGetter.class, g );
        }
    
    public void testRDBModelMakerSource()
        {
        final ConnectionDescription c = new ConnectionDescription( "eh:/subject", "url", "user", "password", "type" );
        final List<String> history = new ArrayList<String>();
        Assembler a = new ModelSourceAssembler() 
            {
            @Override
            protected ModelGetter createRDBGetter( ConnectionDescription cGiven )
                {
                assertSame( c, cGiven );
                history.add( "created" );
                return ModelFactory.createMemModelMaker();
                }
            };
        Assembler mock = new NamedObjectAssembler( resource( "C" ), c );
        Resource root = resourceInModel( "mg rdf:type ja:RDBModelSource; mg rdf:type ja:ModelSource; mg ja:connection C" );
        assertInstanceOf( ModelGetter.class, a.open( mock, root ) );
        assertEquals( listOfOne( "created" ), history );
        }
    }
