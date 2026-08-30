/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

// Package
///////////////
package org.apache.jena.ontology.impl;

// Imports
///////////////
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.RDFNode;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit tests for the AllDifferent declaration.
 * </p>
 */
@SuppressWarnings("removal")
public class TestAllDifferent extends OntTestBase
{

    static { JenaTestLib.setup(); }

    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "AllDifferent.distinctMembers", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    AllDifferent a = m.createAllDifferent();
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addDistinctMember( b );
                    assertEquals( 1, a.getCardinality( prof.DISTINCT_MEMBERS() ), "Cardinality should be 1" );
                    assertEquals( 1, a.getDistinctMembers().size(), "List size should be 1" );
                    assertTrue( a.hasDistinctMember( b ), "a should have b as distinct" );

                    a.addDistinctMember( c );
                    assertEquals( 1, a.getCardinality( prof.DISTINCT_MEMBERS() ), "Cardinality should be 1" );
                    assertEquals( 2, a.getDistinctMembers().size(), "List size should be 2" );
                    iteratorTest( a.listDistinctMembers(), new Object[] {b, c} );

                    assertTrue( a.hasDistinctMember( b ), "a should have b as distinct" );
                    assertTrue( a.hasDistinctMember( c ), "a should have c as distinct" );

                    a.setDistinctMembers( m.createList( new RDFNode[] {b} ) );
                    assertEquals( 1, a.getCardinality( prof.DISTINCT_MEMBERS() ), "Cardinality should be 1" );
                    assertEquals( 1, a.getDistinctMembers().size(), "List size should be 1" );
                    assertTrue( a.hasDistinctMember( b ), "a should have b as distinct" );
                    assertTrue( !a.hasDistinctMember( c ), "a should not have c as distinct" );

                    a.removeDistinctMember( b );
                    assertTrue( !a.hasDistinctMember( b ), "a should have not b as distinct" );
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
