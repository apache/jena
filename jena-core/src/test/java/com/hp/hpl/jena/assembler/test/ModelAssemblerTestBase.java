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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ReificationStyle;

/**
    Test base for ModelAssemblers - provides parameterised method for
    reification testing.
    
    @author kers
*/
public abstract class ModelAssemblerTestBase extends AssemblerTestBase
    {
    public ModelAssemblerTestBase( String name )
        { super( name ); }

    /**
         Assert that the assembler <code>a</code> will create models with all
         the specified reification styles on the specification <code>base</code>.
    */
    protected final void testCreatesWithStyle( Assembler a, String base )
        {
        testCreateWithStyle( a, base, "ja:minimal", ReificationStyle.Minimal );
        testCreateWithStyle( a, base, "ja:standard", ReificationStyle.Standard );
        testCreateWithStyle( a, base, "ja:convenient", ReificationStyle.Convenient );
        }

    protected final void testCreateWithStyle( Assembler a, String base, String styleString, ReificationStyle style )
        {
        Resource root = resourceInModel( base );
        root.addProperty( JA.reificationMode, resource( root.getModel(), styleString ) );
        Model m = a.openModel( root );
        assertEquals( style, m.getGraph().getReifier().getStyle() );
        }
    }
