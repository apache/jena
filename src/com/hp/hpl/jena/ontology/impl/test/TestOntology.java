/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            26-Mar-2003
 * Filename           $RCSfile: TestOntology.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-22 19:20:44 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;


/**
 * <p>
 * Unit test cases for the Ontology class
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestOntology.java,v 1.7 2003-06-22 19:20:44 ian_dickinson Exp $
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

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "Ontology.imports", true, true, true, false ) {
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
            new OntTestCase( "Ontology.backwardCompatibleWith", true, true, false, false ) {
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
            new OntTestCase( "Ontology.priorVersion", true, true, false, false ) {
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
            new OntTestCase( "Ontology.incompatibleWith", true, true, false, false ) {
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


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


