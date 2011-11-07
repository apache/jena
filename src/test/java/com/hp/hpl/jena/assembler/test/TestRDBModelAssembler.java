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
import com.hp.hpl.jena.assembler.assemblers.RDBModelAssembler;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ReificationStyle;

public class TestRDBModelAssembler extends AssemblerTestBase
    {
    public TestRDBModelAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return RDBModelAssembler.class; }

    public void testRDBModelAssemblerType()
        { testDemandsMinimalType( new RDBModelAssembler(), JA.RDBModel );  }

    public void testRDBModelVocabulary()
        {
        Model m = model( "x rdf:type ja:Connectable; x rdf:type ja:NamedModel" );
        Model answer = ModelExpansion.withSchema( m, JA.getSchema() );
        assertTrue( "should infer x rdf:type ja:RDBModel", answer.contains( statement( "x rdf:type ja:RDBModel" ) ) );
        }
    
    public void testInvokesCreateModel()
        {
        Resource root = resourceInModel( "x rdf:type ja:RDBModel; x ja:modelName 'spoo'; x ja:connection C" );
        final ConnectionDescription C = ConnectionDescription.create( "eh:/x", "A", "B", "C", "D" );
        final Model fake = ModelFactory.createDefaultModel();
        final Mode theMode = new Mode( true, true );
        Assembler a = new RDBModelAssembler()
            {
            @Override
            public Model openModel( Resource root, ConnectionDescription c, String name, ReificationStyle style, Content initial, Mode mode )
                {
                assertSame( C, c );
                assertSame( theMode, mode );
                return fake;
                }
            };
        Assembler foo = new NamedObjectAssembler( resource( "C" ), C );
        assertSame( fake, a.open( foo, root, theMode ) );
        }
    }
