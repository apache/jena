/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            17-Jun-2003
 * Filename           $RCSfile: TestDAMLProperty.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-07-31 17:49:47 $
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
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.vocabulary.DAML_OIL;

import junit.framework.*;


/**
 * <p>
 * Unit tests for DAML property
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestDAMLProperty.java,v 1.4 2003-07-31 17:49:47 ian_dickinson Exp $
 */
public class TestDAMLProperty 
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
        return new TestDAMLProperty( "TestDAMLProperty" );
    }
    
    public TestDAMLProperty( String name ) {
        super( name );
    }
    

    // External signature methods
    //////////////////////////////////

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "DAMLProperty.unique" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLProperty p = m.createDAMLProperty( NS + "p" );
                    
                    assertFalse( "unique", p.isUnique() );
                    p.setIsUnique( true );
                    assertTrue( "unique", p.isUnique() );
                }
            },
            new OntTestCase( "DAMLProperty.prop_domain" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLProperty p = m.createDAMLProperty( NS + "p" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    assertEquals( "prop_domain property", DAML_OIL.domain, p.prop_domain().getProperty() );
                    
                    assertEquals( "domain cardinality", 0, p.prop_domain().count() );
                    p.prop_domain().add( B );
                    assertEquals( "domain cardinality", 1, p.prop_domain().count() );
                    p.prop_domain().add( C );
                    assertEquals( "domain cardinality", 2, p.prop_domain().count() );
                    
                    iteratorTest( p.prop_domain().getAll(), new Object[] {B,C} );
                    
                    p.prop_domain().remove( C );
                    assertEquals( "domain cardinality", 1, p.prop_domain().count() );
                    
                    iteratorTest( p.prop_domain().getAll(), new Object[] {B} );
                    
                    assertTrue( "hasValue", p.prop_domain().hasValue( B ) );
                    assertTrue( "hasValue", !p.prop_domain().hasValue( C ) );
                }
            },
            new OntTestCase( "DAMLProperty.prop_range" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLProperty p = m.createDAMLProperty( NS + "p" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    assertEquals( "prop_range property", DAML_OIL.range, p.prop_range().getProperty() );
                    
                    assertEquals( "range cardinality", 0, p.prop_range().count() );
                    p.prop_range().add( B );
                    assertEquals( "range cardinality", 1, p.prop_range().count() );
                    p.prop_range().add( C );
                    assertEquals( "range cardinality", 2, p.prop_range().count() );
                    
                    iteratorTest( p.prop_range().getAll(), new Object[] {B,C} );
                    
                    p.prop_range().remove( C );
                    assertEquals( "range cardinality", 1, p.prop_range().count() );
                    
                    iteratorTest( p.prop_range().getAll(), new Object[] {B} );
                    
                    assertTrue( "hasValue", p.prop_range().hasValue( B ) );
                    assertTrue( "hasValue", !p.prop_range().hasValue( C ) );
                }
            },
            new OntTestCase( "DAMLProperty.prop_subPropertyOf" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLProperty p = m.createDAMLProperty( NS + "p" );
                    DAMLProperty q = m.createDAMLProperty( NS + "q" );
                    DAMLProperty r = m.createDAMLProperty( NS + "r" );
                    
                    assertEquals( "prop_subPropertyOf property", DAML_OIL.subPropertyOf, p.prop_subPropertyOf().getProperty() );
                    
                    assertEquals( "subPropertyOf cardinality", 0, p.prop_subPropertyOf().count() );
                    p.prop_subPropertyOf().add( q );
                    assertEquals( "subPropertyOf cardinality", 1, p.prop_subPropertyOf().count() );
                    p.prop_subPropertyOf().add( r );
                    assertEquals( "subPropertyOf cardinality", 2, p.prop_subPropertyOf().count() );
                    
                    iteratorTest( p.prop_subPropertyOf().getAll(), new Object[] {q,r} );
                    
                    p.prop_subPropertyOf().remove( r );
                    assertEquals( "subPropertyOf cardinality", 1, p.prop_subPropertyOf().count() );
                    
                    iteratorTest( p.prop_subPropertyOf().getAll(), new Object[] {q} );
                    
                    assertTrue( "hasValue", p.prop_subPropertyOf().hasValue( q ) );
                    assertTrue( "hasValue", !p.prop_subPropertyOf().hasValue( r ) );
                }
            },
            new OntTestCase( "DAMLProperty.getSubProperties" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLProperty p = m.createDAMLProperty( NS + "p" );
                    DAMLProperty q = m.createDAMLProperty( NS + "q" );
                    DAMLProperty r = m.createDAMLProperty( NS + "r" );
                   
                    r.prop_subPropertyOf().add( q );
                    q.prop_subPropertyOf().add( p );
                    
                    assertEquals( "subPropertyOf p", q, p.getSubProperty() );
                    
                    // no inference
                    iteratorTest( p.getSubProperties(), new Object[] {q} );
                    iteratorTest( p.getSubProperties( false ), new Object[] {q} );
                    iteratorTest( p.getSubProperties( true ), new Object[] {q} );
                    
                    r.prop_subPropertyOf().add( p );   // could be inferred
                    
                    iteratorTest( p.getSubProperties(), new Object[] {q,r} );
                    iteratorTest( p.getSubProperties( false ), new Object[] {q} );
                    iteratorTest( p.getSubProperties( true ), new Object[] {q,r} );
                }
            },
            new OntTestCase( "DAMLProperty.getSuperProperties" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLProperty p = m.createDAMLProperty( NS + "p" );
                    DAMLProperty q = m.createDAMLProperty( NS + "q" );
                    DAMLProperty r = m.createDAMLProperty( NS + "r" );
                   
                    p.prop_subPropertyOf().add( q );
                    q.prop_subPropertyOf().add( r );
                    
                    assertEquals( "superPropertyOf p", q, p.getSuperProperty() );
                    
                    // no inference
                    iteratorTest( p.getSuperProperties(), new Object[] {q} );
                    iteratorTest( p.getSuperProperties( false ), new Object[] {q} );
                    iteratorTest( p.getSuperProperties( true ), new Object[] {q} );
                    
                    p.prop_subPropertyOf().add( r );   // could be inferred
                    
                    iteratorTest( p.getSuperProperties(), new Object[] {q,r} );
                    iteratorTest( p.getSuperProperties( false ), new Object[] {q} );
                    iteratorTest( p.getSuperProperties( true ), new Object[] {q,r} );
                }
            },
            new OntTestCase( "DAMLProperty.getSameProperties" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLProperty p = m.createDAMLProperty( NS + "p" );
                    DAMLProperty q = m.createDAMLProperty( NS + "q" );
                    DAMLProperty r = m.createDAMLProperty( NS + "r" );
                   
                    p.prop_samePropertyAs().add( q );
                    q.prop_samePropertyAs().add( r );
                    
                    // no inference
                    iteratorTest( p.getSameProperties(), new Object[] {q} );
                    
                    p.prop_samePropertyAs().add( r );   // could be inferred
                    
                    iteratorTest( p.getSameProperties(), new Object[] {q,r} );
                }
            },
            new OntTestCase( "Datatype property" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLDatatypeProperty p = m.createDAMLDatatypeProperty( NS + "p" );
                    assertNotNull( p );
                }
            },
            new OntTestCase( "unambiguous property" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLObjectProperty p = m.createDAMLObjectProperty( NS + "p" );
                    assertFalse( "p not unambiguous", p.isUnambiguous() );
                    p.setIsUnambiguous( true );
                    assertTrue( "p not unambiguous", p.isUnambiguous() );
                    p.setIsUnambiguous( false );
                    assertFalse( "p not unambiguous", p.isUnambiguous() );
                }
            },
            new OntTestCase( "Transitive property" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLObjectProperty p = m.createDAMLObjectProperty( NS + "p" );
                    assertFalse( "p not Transitive", p.isTransitive() );
                    p.setIsTransitive( true );
                    assertTrue( "p not Transitive", p.isTransitive() );
                    p.setIsTransitive( false );
                    assertFalse( "p not Transitive", p.isTransitive() );
                }
            },
            new OntTestCase( "DAMLObjectProperty.prop_inverseOf" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLObjectProperty p = m.createDAMLObjectProperty( NS + "p" );
                    DAMLObjectProperty q = m.createDAMLObjectProperty( NS + "q" );
                   
                    p.prop_inverseOf().add( q );
                    assertEquals( "inverse", q, p.prop_inverseOf().get() );
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
