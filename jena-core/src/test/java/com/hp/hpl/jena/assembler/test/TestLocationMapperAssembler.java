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
import com.hp.hpl.jena.assembler.assemblers.LocationMapperAssembler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab;

public class TestLocationMapperAssembler extends AssemblerTestBase
    {
    public TestLocationMapperAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return LocationMapperAssembler.class; }

    public void testLocationMapperAssemblerType()
        { testDemandsMinimalType( new LocationMapperAssembler(), JA.LocationMapper );  }
    
    public void testLocationMapperVocabulary()
        {
        assertSubclassOf( JA.LocationMapper, JA.Object );
        assertDomain( JA.LocationMapper, LocationMappingVocab.mapping );
        }
    
    public void testCreatesLocationMapper()
        {
        Resource root = resourceInModel( "r rdf:type ja:LocationMapper" );
        Assembler a = new LocationMapperAssembler();
        Object x = a.open( root );
        assertInstanceOf( LocationMapper.class, x );
        }
    
    public void testCreatesWithCorrectContent()
        { // TODO should really have some mroe of these
        Resource root = resourceInModel( "r rdf:type ja:LocationMapper; r lm:mapping _m; _m lm:name 'alpha'; _m lm:altName 'beta'" );
        Assembler a = new LocationMapperAssembler();
        Object x = a.open( root );
        assertInstanceOf( LocationMapper.class, x );
        assertEqualMaps( new LocationMapper( root.getModel() ), (LocationMapper) x );
        }

    private void assertEqualMaps( LocationMapper expected, LocationMapper got )
        {
        Set<String> eAltEntryKeys = IteratorCollection.iteratorToSet( expected.listAltEntries() );
        Set<String> gAltEntryKeys = IteratorCollection.iteratorToSet( got.listAltEntries() );
        Set<String> eAltPrefixKeys = IteratorCollection.iteratorToSet( expected.listAltPrefixes() );
        Set<String> gAltPrefixKeys = IteratorCollection.iteratorToSet( got.listAltPrefixes() );
        assertEquals( "altEntry keys dhould be equal", eAltEntryKeys, gAltEntryKeys );
        assertEquals( "prefixEntry keys should be equal", eAltPrefixKeys, gAltPrefixKeys );
            for ( String key : eAltEntryKeys )
            {
                assertEquals( "alt entrys should be equal", expected.getAltEntry( key ), got.getAltEntry( key ) );
            }
            for ( String key : eAltPrefixKeys )
            {
                assertEquals( "prefix entiries should be equal", expected.getAltPrefix( key ),
                              got.getAltPrefix( key ) );
            }
        }
    }
