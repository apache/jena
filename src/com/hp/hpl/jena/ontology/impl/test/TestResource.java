/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            23-May-2003
 * Filename           $RCSfile: TestResource.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-23 11:13:05 $
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
 * Unit test cases for ontology resources
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestResource.java,v 1.1 2003-05-23 11:13:05 ian_dickinson Exp $
 */
public class TestResource 
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
        return new TestResource( "TestResource" );
    }
    
    public TestResource( String name ) {
        super( name );
    }
    
    
    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////
    
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "OntResource.sameAs", true, false, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = (OntResource) m.getResource( NS + "c" ).as( OntResource.class );
                    
                    a.addSameAs( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "a should be sameAs b", b, a.getSameAs() );
                    
                    a.addSameAs( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.SAME_AS() ) );
                    iteratorTest( a.listSameAs(), new Object[] {b, c} );
                    
                    a.setSameAs( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "a should be sameAs b", b, a.getSameAs() );
                }
            },
            new OntTestCase( "OntResource.differentFrom", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = (OntResource) m.getResource( NS + "c" ).as( OntResource.class );
                    
                    a.addDifferentFrom( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DIFFERENT_FROM() ) );
                    assertEquals( "a should be differentFrom b", b, a.getDifferentFrom() );
                    
                    a.addDifferentFrom( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.DIFFERENT_FROM() ) );
                    iteratorTest( a.listDifferentFrom(), new Object[] {b, c} );
                    
                    a.setDifferentFrom( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DIFFERENT_FROM() ) );
                    assertEquals( "a should be differentFrom b", b, a.getDifferentFrom() );
                }
            },
            new OntTestCase( "OntResource.seeAlso", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = (OntResource) m.getResource( NS + "c" ).as( OntResource.class );
                    
                    a.addSeeAlso( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SEE_ALSO() ) );
                    assertEquals( "a should be seeAlso b", b, a.getSeeAlso() );
                    
                    a.addSeeAlso( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.SEE_ALSO() ) );
                    iteratorTest( a.listSeeAlso(), new Object[] {b, c} );
                    
                    a.setSeeAlso( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SEE_ALSO() ) );
                    assertEquals( "a should be seeAlso b", b, a.getSeeAlso() );
                }
            },
            new OntTestCase( "OntResource.isDefinedBy", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = (OntResource) m.getResource( NS + "c" ).as( OntResource.class );
                    
                    a.addIsDefinedBy( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.IS_DEFINED_BY() ) );
                    assertEquals( "a should be isDefinedBy b", b, a.getIsDefinedBy() );
                    
                    a.addIsDefinedBy( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.IS_DEFINED_BY() ) );
                    iteratorTest( a.listIsDefinedBy(), new Object[] {b, c} );
                    
                    a.setIsDefinedBy( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.IS_DEFINED_BY() ) );
                    assertEquals( "a should be isDefinedBy b", b, a.getIsDefinedBy() );
                }
            },
            new OntTestCase( "OntResource.versionInfo", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    
                    a.addVersionInfo( "some info" );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.VERSION_INFO() ) );
                    assertEquals( "a has wrong version info", "some info", a.getVersionInfo() );
                    
                    a.addVersionInfo( "more info" );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.VERSION_INFO() ) );
                    iteratorTest( a.listVersionInfo(), new Object[] {"some info", "more info"} );
                    
                    a.setVersionInfo( "new info" );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.VERSION_INFO() ) );
                    assertEquals( "a has wrong version info", "new info", a.getVersionInfo() );
                }
            },
            new OntTestCase( "OntResource.label.nolang", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    
                    a.addLabel( "some info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.LABEL() ) );
                    assertEquals( "a has wrong label", "some info", a.getLabel( null ) );
                    
                    a.addLabel( "more info", null );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.LABEL() ) );
                    iteratorTest( a.listLabels(), new Object[] {m.createTypedLiteral( "some info" ), m.createTypedLiteral( "more info" )} );
                    
                    a.setLabel( "new info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.LABEL() ) );
                    assertEquals( "a has wrong label", "new info", a.getLabel( null ) );
                }
            },
            new OntTestCase( "OntResource.label.lang", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    
                    a.addLabel( "good", "EN" );
                    assertEquals( "wrong label", "good", a.getLabel( null ) );

                    a.addLabel( "bon", "FR" );
                    
                    assertEquals( "wrong label", "good", a.getLabel( "EN" ) );
                    assertEquals( "wrong label", null, a.getLabel( "EN-GB" ) );  // no literal with a specific enough language
                    assertEquals( "wrong label", "bon", a.getLabel( "FR" ) );
                    
                    a.addLabel( "spiffing", "EN-GB" );
                    a.addLabel( "duude", "EN-US" );
                    
                    assertEquals( "wrong label", "spiffing", a.getLabel( "EN-GB" ) );
                    assertEquals( "wrong label", "duude", a.getLabel( "EN-US" ) );
                    assertEquals( "wrong label", null, a.getLabel( "DE" ) );
                    
                    a.addLabel( "abcdef", "AB-CD" );
                    assertEquals( "wrong label", "abcdef", a.getLabel( "AB" ) );
                    assertEquals( "wrong label", null, a.getLabel( "AB-XY" ) );
                }
            },
            new OntTestCase( "OntResource.comment.nolang", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    
                    a.addComment( "some info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.COMMENT() ) );
                    assertEquals( "a has wrong comment", "some info", a.getComment( null ) );
                    
                    a.addComment( "more info", null );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.COMMENT() ) );
                    iteratorTest( a.listComments(), new Object[] {m.createTypedLiteral( "some info" ), m.createTypedLiteral( "more info" )} );
                    
                    a.setComment( "new info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.COMMENT() ) );
                    assertEquals( "a has wrong comment", "new info", a.getComment( null ) );
                }
            },
            new OntTestCase( "OntResource.comment.lang", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    
                    a.addComment( "good", "EN" );
                    assertEquals( "wrong comment", "good", a.getComment( null ) );

                    a.addComment( "bon", "FR" );
                    
                    assertEquals( "wrong comment", "good", a.getComment( "EN" ) );
                    assertEquals( "wrong comment", null, a.getComment( "EN-GB" ) );  // no literal with a specific enough language
                    assertEquals( "wrong comment", "bon", a.getComment( "FR" ) );
                    
                    a.addComment( "spiffing", "EN-GB" );
                    a.addComment( "duude", "EN-US" );
                    
                    assertEquals( "wrong comment", "spiffing", a.getComment( "EN-GB" ) );
                    assertEquals( "wrong comment", "duude", a.getComment( "EN-US" ) );
                    assertEquals( "wrong comment", null, a.getComment( "DE" ) );
                    
                    a.addComment( "abcdef", "AB-CD" );
                    assertEquals( "wrong comment", "abcdef", a.getComment( "AB" ) );
                    assertEquals( "wrong comment", null, a.getComment( "AB-XY" ) );
                }
            },
        };
    }

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


