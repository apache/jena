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

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;



// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

import junit.framework.*;


/**
 * <p>
 * Unit tests for the AllDifferent declaration.
 * </p>
 */
public class TestAllDifferent
    extends OntTestBase 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////
    
    static public TestSuite suite() {
        return new TestAllDifferent( "TestAllDifferent" );
    }
    
    public TestAllDifferent( String name ) {
        super( name );
    }
    

    // External signature methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "AllDifferent.distinctMembers", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    AllDifferent a = m.createAllDifferent();
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );
                    
                    a.addDistinctMember( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DISTINCT_MEMBERS() ) );
                    assertEquals( "List size should be 1", 1, a.getDistinctMembers().size() );
                    assertTrue( "a should have b as distinct", a.hasDistinctMember( b ) );
                    
                    a.addDistinctMember( c );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DISTINCT_MEMBERS() ) );
                    assertEquals( "List size should be 2", 2, a.getDistinctMembers().size() );
                    iteratorTest( a.listDistinctMembers(), new Object[] {b, c} );
                    
                    assertTrue( "a should have b as distinct", a.hasDistinctMember( b ) );
                    assertTrue( "a should have c as distinct", a.hasDistinctMember( c ) );
                    
                    a.setDistinctMembers( m.createList( new RDFNode[] {b} ) );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DISTINCT_MEMBERS() ) );
                    assertEquals( "List size should be 1", 1, a.getDistinctMembers().size() );
                    assertTrue( "a should have b as distinct", a.hasDistinctMember( b ) );
                    assertTrue( "a should not have c as distinct", !a.hasDistinctMember( c ) );
                    
                    a.removeDistinctMember( b );
                    assertTrue( "a should have not b as distinct", !a.hasDistinctMember( b ) );
                }
            },
        };
    }
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
