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

import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestMode extends AssemblerTestBase
    {
    public TestMode( String name )
        { super( name ); }

    public void testConstantsExist()
        {
        Mode a = Mode.CREATE, b = Mode.DEFAULT;
        Mode c = Mode.REUSE, d = Mode.ANY;
        assertDiffer( Mode.CREATE, Mode.DEFAULT );
        assertDiffer( Mode.CREATE, Mode.REUSE );
        assertDiffer( Mode.CREATE, Mode.ANY );
        assertDiffer( Mode.DEFAULT, Mode.REUSE );
        assertDiffer( Mode.DEFAULT, Mode.ANY );
        assertDiffer( Mode.REUSE, Mode.ANY );
        }
    
    static final String someName = "aName";
    static final Resource someRoot = resource( "aRoot" );
    
    public void testCreate()
        {
        assertEquals( true, Mode.CREATE.permitCreateNew( someRoot, someName ) );
        assertEquals( false, Mode.CREATE.permitUseExisting( someRoot, someName ) );
        }    
    
    public void testReuse()
        {
        assertEquals( false, Mode.REUSE.permitCreateNew( someRoot, someName ) );
        assertEquals( true, Mode.REUSE.permitUseExisting( someRoot, someName ) );
        }    
    
    public void testAny()
        {
        assertEquals( true, Mode.ANY.permitCreateNew( someRoot, someName ) );
        assertEquals( true, Mode.ANY.permitUseExisting( someRoot, someName ) );
        }    
    
    public void testDefault()
        {
        assertEquals( false, Mode.DEFAULT.permitCreateNew( someRoot, someName ) );
        assertEquals( true, Mode.DEFAULT.permitUseExisting( someRoot, someName ) );
        }
    }
