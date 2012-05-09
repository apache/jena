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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestBuiltinAssemblerGroup extends AssemblerTestBase
    {
    public TestBuiltinAssemblerGroup( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return null; }

    public void testGeneralRegistration()
        {
        assertAssemblerClass( JA.DefaultModel, DefaultModelAssembler.class );
        assertAssemblerClass( JA.PrefixMapping, PrefixMappingAssembler.class );      
        assertAssemblerClass( JA.SinglePrefixMapping, PrefixMappingAssembler.class );      
        assertAssemblerClass( JA.FileModel, FileModelAssembler.class );       
        assertAssemblerClass( JA.OntModel, OntModelAssembler.class );       
        assertAssemblerClass( JA.OntModelSpec, OntModelSpecAssembler.class );     
        assertAssemblerClass( JA.Content, ContentAssembler.class );         
        assertAssemblerClass( JA.ContentItem, ContentAssembler.class );       
        assertAssemblerClass( JA.ReasonerFactory, ReasonerFactoryAssembler.class );  
        assertAssemblerClass( JA.InfModel, InfModelAssembler.class );       
        assertAssemblerClass( JA.MemoryModel, MemoryModelAssembler.class );       
        assertAssemblerClass( JA.RuleSet, RuleSetAssembler.class );
        assertAssemblerClass( JA.LocationMapper, LocationMapperAssembler.class );
        assertAssemblerClass( JA.FileManager, FileManagerAssembler.class );
        assertAssemblerClass( JA.DocumentManager, DocumentManagerAssembler.class );
        assertAssemblerClass( JA.UnionModel, UnionModelAssembler.class );
        assertAssemblerClass( JA.ModelSource, ModelSourceAssembler.class );
        }
    
    public void testVariables()
        {
        assertInstanceOf( DefaultModelAssembler.class, Assembler.defaultModel );
        assertInstanceOf( PrefixMappingAssembler.class, Assembler.prefixMapping );
        assertInstanceOf( FileModelAssembler.class, Assembler.fileModel );
        assertInstanceOf( OntModelAssembler.class, Assembler.ontModel );
        assertInstanceOf( OntModelSpecAssembler.class, Assembler.ontModelSpec );
        assertInstanceOf( ContentAssembler.class, Assembler.content );
        assertInstanceOf( ReasonerFactoryAssembler.class, Assembler.reasonerFactory );
        assertInstanceOf( InfModelAssembler.class, Assembler.infModel );
        assertInstanceOf( MemoryModelAssembler.class, Assembler.memoryModel );
        assertInstanceOf( RuleSetAssembler.class, Assembler.ruleSet );
        assertInstanceOf( LocationMapperAssembler.class, Assembler.locationMapper );
        assertInstanceOf( FileManagerAssembler.class, Assembler.fileManager );
        assertInstanceOf( DocumentManagerAssembler.class, Assembler.documentManager );
        assertInstanceOf( UnionModelAssembler.class, Assembler.unionModel );
        }
    
    public void testRecognisesAndAssemblesSinglePrefixMapping()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create().setNsPrefix( "P", "spoo:/" );
        Resource r = resourceInModel( "x ja:prefix 'P'; x ja:namespace 'spoo:/'" );
        assertEquals( wanted, Assembler.general.open( r ) );
        }
    
    public void testRecognisesAndAssemblesMultiplePrefixMappings()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "P", "spoo:/" ).setNsPrefix( "Q", "flarn:/" );
        Resource r = resourceInModel
            ( "x ja:includes y; x ja:includes z; y ja:prefix 'P'; y ja:namespace 'spoo:/'; z ja:prefix 'Q'; z ja:namespace 'flarn:/'" );
        assertEquals( wanted, Assembler.general.open( r ) );
        }

    public static void assertEquals( PrefixMapping wanted, Object got )
        {
        if (got instanceof PrefixMapping && wanted.samePrefixMappingAs( (PrefixMapping) got ))
            pass();
        else
            fail( "expected " + wanted + " but was: " + got );
        }
    
    private void assertAssemblerClass( Resource type, Class<?> C )
        {
        assertInstanceOf( C, Assembler.general.assemblerFor( type ) );
        }
    }
