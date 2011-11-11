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

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.assembler.assemblers.ModelSourceAssembler ;
import com.hp.hpl.jena.rdf.model.ModelGetter ;
import com.hp.hpl.jena.rdf.model.impl.MemoryModelGetter ;

public class TestModelSourceAssembler extends AssemblerTestBase
    {
    public TestModelSourceAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return ModelSourceAssembler.class; }

    public void testModelSourceAssemblerType()
        { testDemandsMinimalType( new ModelSourceAssembler(), JA.ModelSource );  }
   
    public void testMemModelMakerSource()
        {
        Assembler a = new ModelSourceAssembler();
        ModelGetter g = (ModelGetter) a.open( resourceInModel( "mg rdf:type ja:ModelSource" ) );
        assertInstanceOf( MemoryModelGetter.class, g );
        }
    
    }
