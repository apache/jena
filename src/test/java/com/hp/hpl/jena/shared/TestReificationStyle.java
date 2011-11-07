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

package com.hp.hpl.jena.shared;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

/**
	TestReificationStyle: test that ReificationStyle sets its fields correctly from its
    constructor arguments, and that the defined constants have the correct fields.

	@author kers
*/
public class TestReificationStyle extends ModelTestBase 
    {
    public TestReificationStyle( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestReificationStyle.class ); }
    
    public void testConstructorIntercepts()
        { assertEquals( true, new ReificationStyle( true, false ).intercepts() );
        assertEquals( false, new ReificationStyle( false, false ).intercepts() );   
        assertEquals( true, new ReificationStyle( true, true ).intercepts() );
        assertEquals( false, new ReificationStyle( false, true ).intercepts() ); }

    public void testConstructorConceals()
        { assertEquals( false, new ReificationStyle( true, false ).conceals() );
        assertEquals( false, new ReificationStyle( false, false ).conceals() );   
        assertEquals( true, new ReificationStyle( true, true ).conceals() );
        assertEquals( true, new ReificationStyle( false, true ).conceals() ); }
        
    public void testConstants()
        { assertEquals( false, ReificationStyle.Minimal.intercepts() );
        assertEquals( true, ReificationStyle.Minimal.conceals() );
        assertEquals( true, ReificationStyle.Standard.intercepts() );
        assertEquals( false, ReificationStyle.Standard.conceals() );
        assertEquals( true, ReificationStyle.Convenient.intercepts() );
        assertEquals( true, ReificationStyle.Convenient.conceals() ); }
    
    public void testPrettyPrinting()
        { assertEquals( "Minimal", ReificationStyle.Minimal.toString() );
        assertEquals( "Convenient", ReificationStyle.Convenient.toString() );
        assertEquals( "Standard", ReificationStyle.Standard.toString() );
        }
    }
