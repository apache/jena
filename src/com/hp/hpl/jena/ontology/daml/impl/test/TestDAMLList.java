/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            17-Jun-2003
 * Filename           $RCSfile: TestDAMLList.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-17 13:47:37 $
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
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;


/**
 * <p>
 * Unit tests for DAML List
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestDAMLList.java,v 1.1 2003-06-17 13:47:37 ian_dickinson Exp $
 */
public class TestDAMLList 
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
        return new TestDAMLList( "TestDAMLList" );
    }
    
    public TestDAMLList( String name ) {
        super( name );
    }
    
    
    // External signature methods
    //////////////////////////////////

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "DAMLList.getAll" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A,B,C} );
                    
                    iteratorTest( l.getAll(), new Object[] {A,B,C} );
                }
            },
            new OntTestCase( "DAMLList.getFirst" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A,B,C} );
                    
                    assertEquals( "DAMLList.getFirst()", A, l.getFirst() );
                }
            },
            new OntTestCase( "DAMLList.cons" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {B,C} );
                    DAMLList l2 = l.cons( A );
                    
                    iteratorTest( l2.getAll(), new Object[] {A,B,C} );
                }
            },
            new OntTestCase( "DAMLList.getRest" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A,B,C} );
                    
                    iteratorTest( l.getRest().getAll(), new Object[] {B,C} );
                }
            },
            new OntTestCase( "DAMLList.getCount" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A,B,C} );
                    
                    assertEquals( "count", 3, l.getCount() );
                }
            },
            new OntTestCase( "DAMLList.setFirst" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {B,B,C} );
                    l.setFirst( A );
                    
                    iteratorTest( l.getAll(), new Object[] {A,B,C} );
                }
            },
            new OntTestCase( "DAMLList.setRest" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A} );
                    DAMLList l2 = m.createDAMLList( new RDFNode[] {B,C} );
                    l.setRest( l2 );
                    
                    iteratorTest( l.getAll(), new Object[] {A,B,C} );
                }
            },
            new OntTestCase( "DAMLList.setRestNil" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A,B,C} );
                    l.getRest().setRestNil();
                    
                    iteratorTest( l.getAll(), new Object[] {A,B} );
                }
            },
            new OntTestCase( "DAMLList.nil" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLList l = m.createDAMLList();
                    
                    assertTrue( "nil is nil", l.isNil( l.getNil() ) );
                    assertFalse( "A is not nil", l.isNil( A ) );
                }
            },
            new OntTestCase( "DAMLList.findLast" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A,B,C} );
                    
                    iteratorTest( l.findLast().getAll(), new Object[] {C} );
                }
            },
            new OntTestCase( "DAMLList.getItem" ) {
                public void doTest( DAMLModel m ) throws Exception {
                    DAMLClass A = m.createDAMLClass( NS + "A" );
                    DAMLClass B = m.createDAMLClass( NS + "B" );
                    DAMLClass C = m.createDAMLClass( NS + "C" );
                    
                    DAMLList l = m.createDAMLList( new RDFNode[] {A,B,C} );
                    
                    assertEquals( "A", A, l.getItem( 0 ) );
                    assertEquals( "B", B, l.getItem( 1 ) );
                    assertEquals( "C", C, l.getItem( 2 ) );
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

