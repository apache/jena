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
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;

public class TestAssemblerGroupTracing extends AssemblerTestBase
    {
    public TestAssemblerGroupTracing( String name )
        { super( name ); }

    public void testFail() 
        {
        Resource root = resourceInModel( "x rdf:type A" );
        AssemblerGroup g = AssemblerGroup.create();
        g.implementWith( resource( "A" ), new ShantAssemble() );
        try 
            { 
            g.open( root ); 
            fail( "shouldn't get past exception" ); 
            }
        catch (AssemblerException e) 
            {
            AssemblerGroup.Frame frame = new AssemblerGroup.Frame( resource( "x" ), resource( "A" ), ShantAssemble.class );
            assertEquals( listOfOne( frame ), e.getDoing() );
            }
        }
    
    static class ShantAssemble extends AssemblerBase
        {
        @Override
        public Object open( Assembler a, Resource root, Mode mode )
            {            
            throw new JenaException( "shan't" );
            }
    
        }
    }
