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
import com.hp.hpl.jena.assembler.assemblers.PrefixMappingAssembler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
   Tests for prefix-mapping assembler. Note that the basic atom is a
   <i>single</i> prefix-mapping specified with a ja:prefix and ja:namespace
   pair. If a prefix-mapping needs multiple bindings, ja:includes must be
   used to include multiple single mappings:
   
   <pre>
   whatever ja:includes [ja:prefix 'A'; ja:namespace 'namespaceForA']
       ; ja:includes [ja:prefix 'B'; ja:namespace 'namespaceForB']
       ...
   </pre>

    See also <i>TestBuiltinAssembler</i>.
*/

public class TestPrefixMappingAssembler extends AssemblerTestBase
    {
    public TestPrefixMappingAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return PrefixMappingAssembler.class; }

    public void testPrefixMappingAssemblerType()
        { testDemandsMinimalType( new PrefixMappingAssembler(), JA.PrefixMapping );  }
    
    public void testConstructEmptyPrefixMapping()
        {
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel( "pm rdf:type ja:PrefixMapping" );
        Object pm = a.open( root );
        assertInstanceOf( PrefixMapping.class, pm );
        }
    
    public void testSimplePrefixMapping()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "pre", "some:prefix/" );
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel( "pm rdf:type ja:PrefixMapping; pm ja:prefix 'pre'; pm ja:namespace 'some:prefix/'" );
        PrefixMapping pm = (PrefixMapping) a.open( root );
        assertSamePrefixMapping( wanted, pm );
        }
    
    public void testIncludesSingleMapping()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "pre", "some:prefix/" );
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel
            ( "root rdf:type ja:PrefixMapping; root ja:includes pm"
            + "; pm rdf:type ja:PrefixMapping; pm ja:prefix 'pre'; pm ja:namespace 'some:prefix/'" );
        PrefixMapping pm = (PrefixMapping) a.open( root );
        assertSamePrefixMapping( wanted, pm );
        }
    
    public void testIncludesMultipleMappings()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "p1", "some:prefix/" )
            .setNsPrefix( "p2", "other:prefix/" )
            .setNsPrefix( "p3", "simple:prefix#" );
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel
            ( "root rdf:type ja:PrefixMapping"
            + "; root ja:includes pm1; pm1 rdf:type ja:PrefixMapping; pm1 ja:prefix 'p1'; pm1 ja:namespace 'some:prefix/'"
            + "; root ja:includes pm2; pm2 rdf:type ja:PrefixMapping; pm2 ja:prefix 'p2'; pm2 ja:namespace 'other:prefix/'"
            + "; root ja:prefix 'p3'; root ja:namespace 'simple:prefix#'" );
        PrefixMapping pm = (PrefixMapping) a.open( root );
        assertSamePrefixMapping( wanted, pm );
        }
    }
