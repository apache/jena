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

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit test cases for the Ontology class
 * </p>
 */
@SuppressWarnings("removal")
public class TestOntology extends OntTestBase
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
            new OntTestCase( "Ontology.imports", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );

                    x.addImport( y );
                    assertEquals( 1, x.getCardinality( prof.IMPORTS() ), "Cardinality should be 1" );
                    assertEquals( y, x.getImport(), "x should import y" );

                    x.addImport( z );
                    assertEquals( 2, x.getCardinality( prof.IMPORTS() ), "Cardinality should be 2" );
                    iteratorTest( x.listImports(), new Object[] {y,z} );

                    x.setImport( z );
                    assertEquals( 1, x.getCardinality( prof.IMPORTS() ), "Cardinality should be 1" );
                    assertEquals( z, x.getImport(), "x should import z" );

                    x.removeImport( y );
                    assertEquals( 1, x.getCardinality( prof.IMPORTS() ), "Cardinality should be 1" );
                    x.removeImport( z );
                    assertEquals( 0, x.getCardinality( prof.IMPORTS() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "Ontology.backwardCompatibleWith", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );

                    x.addBackwardCompatibleWith( y );
                    assertEquals( 1, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ), "Cardinality should be 1" );
                    assertEquals( y, x.getBackwardCompatibleWith(), "x should be back comp with y" );

                    x.addBackwardCompatibleWith( z );
                    assertEquals( 2, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ), "Cardinality should be 2" );
                    iteratorTest( x.listBackwardCompatibleWith(), new Object[] {y,z} );

                    x.setBackwardCompatibleWith( z );
                    assertEquals( 1, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ), "Cardinality should be 1" );
                    assertEquals( z, x.getBackwardCompatibleWith(), "x should be back comp with z" );

                    x.removeBackwardCompatibleWith( y );
                    assertEquals( 1, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ), "Cardinality should be 1" );
                    x.removeBackwardCompatibleWith( z );
                    assertEquals( 0, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "Ontology.priorVersion", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );

                    x.addPriorVersion( y );
                    assertEquals( 1, x.getCardinality( prof.PRIOR_VERSION() ), "Cardinality should be 1" );
                    assertEquals( y, x.getPriorVersion(), "x should have prior y" );

                    x.addPriorVersion( z );
                    assertEquals( 2, x.getCardinality( prof.PRIOR_VERSION() ), "Cardinality should be 2" );
                    iteratorTest( x.listPriorVersion(), new Object[] {y,z} );

                    x.setPriorVersion( z );
                    assertEquals( 1, x.getCardinality( prof.PRIOR_VERSION() ), "Cardinality should be 1" );
                    assertEquals( z, x.getPriorVersion(), "x should have prior z" );

                    x.removePriorVersion( y );
                    assertEquals( 1, x.getCardinality( prof.PRIOR_VERSION() ), "Cardinality should be 1" );
                    x.removePriorVersion( z );
                    assertEquals( 0, x.getCardinality( prof.PRIOR_VERSION() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "Ontology.incompatibleWith", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );

                    x.addIncompatibleWith( y );
                    assertEquals( 1, x.getCardinality( prof.INCOMPATIBLE_WITH() ), "Cardinality should be 1" );
                    assertEquals( y, x.getIncompatibleWith(), "x should be in comp with y" );

                    x.addIncompatibleWith( z );
                    assertEquals( 2, x.getCardinality( prof.INCOMPATIBLE_WITH() ), "Cardinality should be 2" );
                    iteratorTest( x.listIncompatibleWith(), new Object[] {y,z} );

                    x.setIncompatibleWith( z );
                    assertEquals( 1, x.getCardinality( prof.INCOMPATIBLE_WITH() ), "Cardinality should be 1" );
                    assertEquals( z, x.getIncompatibleWith(), "x should be incomp with z" );

                    x.removeIncompatibleWith( y );
                    assertEquals( 1, x.getCardinality( prof.INCOMPATIBLE_WITH() ), "Cardinality should be 1" );
                    x.removeIncompatibleWith( z );
                    assertEquals( 0, x.getCardinality( prof.INCOMPATIBLE_WITH() ), "Cardinality should be 0" );
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
