/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            05-Jun-2003
 * Filename           $RCSfile: TestOntReasoning.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-06 11:07:02 $
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
import java.util.*;

import junit.framework.TestCase;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * <p>
 * Unit tests on ont models with reasoning
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestOntReasoning.java,v 1.1 2003-06-06 11:07:02 ian_dickinson Exp $
 */
public class TestOntReasoning 
    extends TestCase
{
    // Constants
    //////////////////////////////////
    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntReasoning( String name ) {
        super( name );
    }
    
    // External signature methods
    //////////////////////////////////

    public void setUp() {
    }
    
    public void tearDown() {
    }
    
    public void testSubClassDirectTransInf1a() {
        OntModel m = (OntModel) ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );
        
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );
        
        iteratorTest( A.listSubClasses(), new Object[] {A, B, C, D} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C, A} );
    }
    
    public void testSubClassDirectTransInf1b() {
        OntModel m = (OntModel) ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );
        
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );
        A.addSubClass( D );     // directly asserts a link that could be inferred
        
        iteratorTest( A.listSubClasses(), new Object[] {A, B, C, D} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C, A} );
    }
    
    public void testSubClassDirectTransInf2a() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasoner( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );
        
        iteratorTest( A.listSubClasses(), new Object[] {B, C} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C} );
    }
    
    public void testSubClassDirectTransInf2b() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasoner( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );
        A.addSubClass( D );     // directly asserts a link that could be inferred
        
        iteratorTest( A.listSubClasses(), new Object[] {B, C, D} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C} );
    }
    
    public void testSubPropertyDirectTransInf1a() {
        OntModel m = (OntModel) ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );
        
        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );
        
        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );
        
        iteratorTest( p.listSubProperties(), new Object[] {p,q,r,s} );
        iteratorTest( p.listSubProperties( true ), new Object[] {p,q,r} );
    }
    
    public void testSubPropertyDirectTransInf1b() {
        OntModel m = (OntModel) ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );
        
        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );
        
        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );
        p.addSubProperty( s );     // directly asserts a link that could be inferred
        
        iteratorTest( p.listSubProperties(), new Object[] {p,q,r,s} );
        iteratorTest( p.listSubProperties( true ), new Object[] {p,q,r} );
    }
    
    public void testSubPropertyDirectTransInf2a() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasoner( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        
        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );
        
        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );
        
        iteratorTest( p.listSubProperties(), new Object[] {q,r} );
        iteratorTest( p.listSubProperties( true ), new Object[] {q,r} );
    }
    
    public void testSubPropertyDirectTransInf2b() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasoner( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        
        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );
        
        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );
        p.addSubProperty( s );     // directly asserts a link that could be inferred
        
        iteratorTest( p.listSubProperties(), new Object[] {q,r,s} );
        iteratorTest( p.listSubProperties( true ), new Object[] {q,r} );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    /** Test that an iterator delivers the expected values */
    protected void iteratorTest( Iterator i, Object[] expected ) {
        List expList = new ArrayList();
        for (int j = 0; j < expected.length; j++) {
            expList.add( expected[j] );
        }
        
        while (i.hasNext()) {
            Object next = i.next();
            assertTrue( "Value " + next + " was not expected as a result from this iterator ", expList.contains( next ) );
            assertTrue( "Value " + next + " was not removed from the list ", expList.remove( next ) );
        }
        
        assertEquals( "There were expected elements from the iterator that were not found", 0, expList.size() );
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

