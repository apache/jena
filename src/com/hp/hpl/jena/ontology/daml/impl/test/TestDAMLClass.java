/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            13-Jun-2003
 * Filename           $RCSfile: TestDAMLClass.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-13 20:45:52 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl.test;



// Imports
///////////////
import junit.framework.*;

import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.vocabulary.DAML_OIL;



/**
 * <p>
 * unit tests for DAML Class
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestDAMLClass.java,v 1.2 2003-06-13 20:45:52 ian_dickinson Exp $
 */
public class TestDAMLClass 
    extends DAMLTestBase
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
        return new TestDAMLClass( "TestDAMLClass" );
    }
    
    public TestDAMLClass( String name ) {
        super( name );
    }
    
    
    // External signature methods
    //////////////////////////////////

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "DAMLClass.prop_subClassOf" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    assertEquals( "prop_subClassOf property", DAML_OIL.subClassOf, A.prop_subClassOf().getProperty() );
                    
                    assertEquals( "sub-class cardinality", 0, A.prop_subClassOf().count() );
                    A.prop_subClassOf().add( B );
                    assertEquals( "sub-class cardinality", 1, A.prop_subClassOf().count() );
                    A.prop_subClassOf().add( C );
                    assertEquals( "sub-class cardinality", 2, A.prop_subClassOf().count() );
                    
                    iteratorTest( A.prop_subClassOf().getAll(), new Object[] {B,C} );
                    
                    A.prop_subClassOf().remove( C );
                    assertEquals( "sub-class cardinality", 1, A.prop_subClassOf().count() );
                    
                    iteratorTest( A.prop_subClassOf().getAll(), new Object[] {B} );
                    
                    assertTrue( "getDAMLValue instanceOf DAMLCommon", A.prop_subClassOf().getDAMLValue() instanceof DAMLCommon );
                    assertTrue( "hasValue", A.prop_subClassOf().hasValue( B ) );
                    assertTrue( "hasValue", !A.prop_subClassOf().hasValue( C ) );
                }
            },
            new OntTestCase( "DAMLClass.prop_disjointWith" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                   
                    assertEquals( "prop_disjointwith property", DAML_OIL.disjointWith, A.prop_disjointWith().getProperty() );
                    
                    assertEquals( "disjointWith cardinality", 0, A.prop_disjointWith().count() );
                    A.prop_disjointWith().add( B );
                    assertEquals( "disjointwith cardinality", 1, A.prop_disjointWith().count() );
                    A.prop_disjointWith().add( C );
                    assertEquals( "disjointWith cardinality", 2, A.prop_disjointWith().count() );
                    
                    iteratorTest( A.prop_disjointWith().getAll(), new Object[] {B,C} );
                    
                    A.prop_disjointWith().remove( C );
                    assertEquals( "disjointwith cardinality", 1, A.prop_disjointWith().count() );
                    
                    iteratorTest( A.prop_disjointWith().getAll(), new Object[] {B} );
                    
                    assertTrue( "getDAMLValue instanceOf DAMLCommon", A.prop_disjointWith().getDAMLValue() instanceof DAMLCommon );
                    assertTrue( "hasValue", A.prop_disjointWith().hasValue( B ) );
                    assertTrue( "hasValue", !A.prop_disjointWith().hasValue( C ) );
                }
            },
            new OntTestCase( "DAMLClass.prop_sameClassAs" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                   
                    assertEquals( "prop_sameClassAs property", DAML_OIL.sameClassAs, A.prop_sameClassAs().getProperty() );
                    
                    assertEquals( "sameClassAs cardinality", 0, A.prop_sameClassAs().count() );
                    A.prop_sameClassAs().add( B );
                    assertEquals( "sameClassAs cardinality", 1, A.prop_sameClassAs().count() );
                    A.prop_sameClassAs().add( C );
                    assertEquals( "sameClassAs cardinality", 2, A.prop_sameClassAs().count() );
                    
                    iteratorTest( A.prop_sameClassAs().getAll(), new Object[] {B,C} );
                    
                    A.prop_sameClassAs().remove( C );
                    assertEquals( "sameClassAs cardinality", 1, A.prop_sameClassAs().count() );
                    
                    iteratorTest( A.prop_sameClassAs().getAll(), new Object[] {B} );
                    
                    assertTrue( "getDAMLValue instanceOf DAMLCommon", A.prop_sameClassAs().getDAMLValue() instanceof DAMLCommon );
                    assertTrue( "hasValue", A.prop_sameClassAs().hasValue( B ) );
                    assertTrue( "hasValue", !A.prop_sameClassAs().hasValue( C ) );
                }
            },
            new OntTestCase( "DAMLClass.prop_complementOf" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                   
                    assertEquals( "prop_complementOf property", DAML_OIL.complementOf, A.prop_complementOf().getProperty() );
                    
                    assertEquals( "complementOf cardinality", 0, A.prop_complementOf().count() );
                    A.prop_complementOf().add( B );
                    assertEquals( "complementOf cardinality", 1, A.prop_complementOf().count() );
                    A.prop_complementOf().add( C );
                    assertEquals( "complementOf cardinality", 2, A.prop_complementOf().count() );
                    
                    iteratorTest( A.prop_complementOf().getAll(), new Object[] {B,C} );
                    
                    A.prop_complementOf().remove( C );
                    assertEquals( "complementOf cardinality", 1, A.prop_complementOf().count() );
                    
                    iteratorTest( A.prop_complementOf().getAll(), new Object[] {B} );
                    
                    assertTrue( "getDAMLValue instanceOf DAMLCommon", A.prop_complementOf().getDAMLValue() instanceof DAMLCommon );
                    assertTrue( "hasValue", A.prop_complementOf().hasValue( B ) );
                    assertTrue( "hasValue", !A.prop_complementOf().hasValue( C ) );
                }
            },
            new OntTestCase( "DAMLClass.getSubClasses" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                   
                    C.prop_subClassOf().add( B );
                    B.prop_subClassOf().add( A );
                    
                    assertEquals( "subClassOf A", B, A.getSubClass() );
                    
                    // no inference
                    iteratorTest( A.getSubClasses(), new Object[] {B} );
                    iteratorTest( A.getSubClasses( false ), new Object[] {B} );
                    iteratorTest( A.getSubClasses( true ), new Object[] {B} );
                    
                    C.prop_subClassOf().add( A );   // could be inferred
                    
                    iteratorTest( A.getSubClasses(), new Object[] {B,C} );
                    iteratorTest( A.getSubClasses( false ), new Object[] {B} );
                    iteratorTest( A.getSubClasses( true ), new Object[] {B,C} );
                }
            },
            new OntTestCase( "DAMLClass.getSuperClasses" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                   
                    A.prop_subClassOf().add( B );
                    B.prop_subClassOf().add( C );
                    
                    assertEquals( "subClassOf A", B, A.getSuperClass() );
                    
                    // no inference
                    iteratorTest( A.getSuperClasses(), new Object[] {B} );
                    iteratorTest( A.getSuperClasses( false ), new Object[] {B} );
                    iteratorTest( A.getSuperClasses( true ), new Object[] {B} );
                    
                    A.prop_subClassOf().add( C );   // could be inferred
                    
                    iteratorTest( A.getSuperClasses(), new Object[] {B,C} );
                    iteratorTest( A.getSuperClasses( false ), new Object[] {B} );
                    iteratorTest( A.getSuperClasses( true ), new Object[] {B,C} );
                }
            },
            new OntTestCase( "DAMLClass.getSameClasses" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                   
                    A.prop_sameClassAs().add( B );
                    B.prop_sameClassAs().add( C );
                    
                    // no inference
                    iteratorTest( A.getSameClasses(), new Object[] {B} );
                    
                    A.prop_sameClassAs().add( C );   // could be inferred
                    
                    iteratorTest( A.getSameClasses(), new Object[] {B,C} );
                }
            },
            new OntTestCase( "DAMLClass.getInstances" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLInstance a = m.createDAMLInstance( A, NS + "a" );
                    DAMLInstance b = m.createDAMLInstance( A, NS + "b" );
                    DAMLInstance c = m.createDAMLInstance( A, NS + "c" );
                   
                    iteratorTest( A.getInstances(), new Object[] {a,b,c} );
                }
            },
            new OntTestCase( "DAMLClass.getDefinedProperties" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLObjectProperty p = m.createDAMLObjectProperty( NS + "p" );
                    DAMLObjectProperty q = m.createDAMLObjectProperty( NS + "q" );
                    DAMLObjectProperty r = m.createDAMLObjectProperty( NS + "r" );

                    // TODO once daml property has been migrated          
                             
                    iteratorTest( A.getDefinedProperties(), new Object[] {} );
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

/* TODO delete me
public class TestDAMLClass{

}

*/