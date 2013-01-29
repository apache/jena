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
import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;


/**
 * <p>
 * Unit test cases for the Ontology class
 * </p>
 */
public class TestOntology
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
        return new TestOntology( "TestOntology" );
    }
    
    public TestOntology( String name ) {
        super( name );
    }
    
    
    
    
    // External signature methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "Ontology.imports", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );
                        
                    x.addImport( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.IMPORTS() ) );
                    assertEquals( "x should import y", y, x.getImport() );
                        
                    x.addImport( z );
                    assertEquals( "Cardinality should be 2", 2, x.getCardinality( prof.IMPORTS() ) );
                    iteratorTest( x.listImports(), new Object[] {y,z} );
                        
                    x.setImport( z );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.IMPORTS() ) );
                    assertEquals( "x should import z", z, x.getImport() );
                    
                    x.removeImport( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.IMPORTS() ) );
                    x.removeImport( z );
                    assertEquals( "Cardinality should be 0", 0, x.getCardinality( prof.IMPORTS() ) );
                }
            },
            new OntTestCase( "Ontology.backwardCompatibleWith", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );
                        
                    x.addBackwardCompatibleWith( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ) );
                    assertEquals( "x should be back comp with y", y, x.getBackwardCompatibleWith() );
                        
                    x.addBackwardCompatibleWith( z );
                    assertEquals( "Cardinality should be 2", 2, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ) );
                    iteratorTest( x.listBackwardCompatibleWith(), new Object[] {y,z} );
                        
                    x.setBackwardCompatibleWith( z );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ) );
                    assertEquals( "x should be back comp with z", z, x.getBackwardCompatibleWith() );
                    
                    x.removeBackwardCompatibleWith( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ) );
                    x.removeBackwardCompatibleWith( z );
                    assertEquals( "Cardinality should be 0", 0, x.getCardinality( prof.BACKWARD_COMPATIBLE_WITH() ) );
                }
            },
            new OntTestCase( "Ontology.priorVersion", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );
                        
                    x.addPriorVersion( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.PRIOR_VERSION() ) );
                    assertEquals( "x should have prior y", y, x.getPriorVersion() );
                        
                    x.addPriorVersion( z );
                    assertEquals( "Cardinality should be 2", 2, x.getCardinality( prof.PRIOR_VERSION() ) );
                    iteratorTest( x.listPriorVersion(), new Object[] {y,z} );
                        
                    x.setPriorVersion( z );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.PRIOR_VERSION() ) );
                    assertEquals( "x should have prior z", z, x.getPriorVersion() );
                    
                    x.removePriorVersion( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.PRIOR_VERSION() ) );
                    x.removePriorVersion( z );
                    assertEquals( "Cardinality should be 0", 0, x.getCardinality( prof.PRIOR_VERSION() ) );
                }
            },
            new OntTestCase( "Ontology.incompatibleWith", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    Ontology x = m.createOntology( NS + "x" );
                    Ontology y = m.createOntology( NS + "y" );
                    Ontology z = m.createOntology( NS + "z" );
                        
                    x.addIncompatibleWith( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.INCOMPATIBLE_WITH() ) );
                    assertEquals( "x should be in comp with y", y, x.getIncompatibleWith() );
                        
                    x.addIncompatibleWith( z );
                    assertEquals( "Cardinality should be 2", 2, x.getCardinality( prof.INCOMPATIBLE_WITH() ) );
                    iteratorTest( x.listIncompatibleWith(), new Object[] {y,z} );
                        
                    x.setIncompatibleWith( z );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.INCOMPATIBLE_WITH() ) );
                    assertEquals( "x should be incomp with z", z, x.getIncompatibleWith() );
                    
                    x.removeIncompatibleWith( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.INCOMPATIBLE_WITH() ) );
                    x.removeIncompatibleWith( z );
                    assertEquals( "Cardinality should be 0", 0, x.getCardinality( prof.INCOMPATIBLE_WITH() ) );
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
