/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            4 Mar 2003
 * Filename           $RCSfile: TestMultiUnion.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-27 13:01:01 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.graph.compose.test;


// Imports
///////////////

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import junit.framework.*;


/**
 * <p>
 * Unit tests for multi-union graph.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestMultiUnion.java,v 1.5 2003-08-27 13:01:01 andy_seaborne Exp $
 */
public class TestMultiUnion
    extends GraphTestBase
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    public TestMultiUnion( String s ) {
        super( s );
    }
    

    // External signature methods
    //////////////////////////////////

    public static TestSuite suite()
        { return new TestSuite( TestMultiUnion.class ); }   


    public void testEmptyGraph() {
        Graph m = new MultiUnion();
        Graph g0 = graphWith( "x p y");
        
        assertEquals( "Empty model should have size zero", 0, m.size() );
        assertFalse( "Empty model should not contain another graph", m.dependsOn( g0 ) );
    }
    

    public void testGraphSize1() {
        Graph g0 = graphWith( "x p y" );
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        Graph g2 = graphWith( "x p y; z p a" );         // intersects with g1
        
        Graph m01 = new MultiUnion( new Graph[] {g0, g1} );
        Graph m10 = new MultiUnion( new Graph[] {g1, g0} );
        Graph m12 = new MultiUnion( new Graph[] {g1, g2} );
        Graph m21 = new MultiUnion( new Graph[] {g2, g1} );
        Graph m02 = new MultiUnion( new Graph[] {g0, g2} );
        Graph m20 = new MultiUnion( new Graph[] {g2, g0} );
        
        Graph m00 = new MultiUnion( new Graph[] {g0, g0} );
        
        int s0 = g0.size();
        int s1 = g1.size();
        int s2 = g2.size();
        
        assertEquals( "Size of union of g0 and g1 not correct", s0+s1, m01.size() );
        assertEquals( "Size of union of g1 and g0 not correct", s0+s1, m10.size() );
        
        assertEquals( "Size of union of g1 and g2 not correct", s1+s2, m12.size() );
        assertEquals( "Size of union of g2 and g1 not correct", s1+s2, m21.size() );

        assertEquals( "Size of union of g0 and g2 not correct", s0+s2 - 1, m02.size() );
        assertEquals( "Size of union of g2 and g0 not correct", s0+s2 - 1, m20.size() );
        
        assertEquals( "Size of union of g0 with itself not correct", s0, m00.size() );
    }
    
    
    public void testGraphSize2() {
        Graph g0 = graphWith( "x p y" );
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        Graph g2 = graphWith( "x p y; z p a" );         // intersects with g1
        
        Graph m01 = new MultiUnion( iterateOver( g0, g1 ) );
        Graph m10 = new MultiUnion( iterateOver( g1, g0 ) );
        Graph m12 = new MultiUnion( iterateOver( g1, g2 ) );
        Graph m21 = new MultiUnion( iterateOver( g2, g1 ) );
        Graph m02 = new MultiUnion( iterateOver( g0, g2 ) );
        Graph m20 = new MultiUnion( iterateOver( g2, g0 ) );
        
        Graph m00 = new MultiUnion( iterateOver( g0, g0 ) );
        
        int s0 = g0.size();
        int s1 = g1.size();
        int s2 = g2.size();
        
        assertEquals( "Size of union of g0 and g1 not correct", s0+s1, m01.size() );
        assertEquals( "Size of union of g1 and g0 not correct", s0+s1, m10.size() );
        
        assertEquals( "Size of union of g1 and g2 not correct", s1+s2, m12.size() );
        assertEquals( "Size of union of g2 and g1 not correct", s1+s2, m21.size() );

        assertEquals( "Size of union of g0 and g2 not correct", s0+s2 - 1, m02.size() );
        assertEquals( "Size of union of g2 and g0 not correct", s0+s2 - 1, m20.size() );
        
        assertEquals( "Size of union of g0 with itself not correct", s0, m00.size() );
    }
    
    
    public void testGraphAddSize() {
        Graph g0 = graphWith( "x p y" );
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        Graph g2 = graphWith( "x p y; z p a" );         // intersects with g1
        
        int s0 = g0.size();
        int s1 = g1.size();
        int s2 = g2.size();
        
        MultiUnion m0 = new MultiUnion( new Graph[] {g0} );
        
        assertEquals( "Size of union of g0 not correct", s0, m0.size() );
        m0.addGraph( g1 );
        assertEquals( "Size of union of g1 and g0 not correct", s0+s1, m0.size() );
        
        m0.addGraph( g2 );
        assertEquals( "Size of union of g0, g1 and g2 not correct", s0+s1+s2 -1, m0.size() );
        
        m0.removeGraph( g1 );
        assertEquals( "Size of union of g0 and g2 not correct", s0+s2 -1, m0.size() );
        
        m0.removeGraph( g0 );
        assertEquals( "Size of union of g2 not correct", s2, m0.size() );
        
        // remove again
        m0.removeGraph( g0 );
        assertEquals( "Size of union of g2 not correct", s2, m0.size() );
        
        m0.removeGraph( g2 );
        assertEquals( "Size of empty union not correct", 0, m0.size() );
        
    }
    
    
    public void testAdd() {
        Graph g0 = graphWith( "x p y" );
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        Graph g2 = graphWith( "x p y; z p a" );         // intersects with g1
        
        MultiUnion m = new MultiUnion( new Graph[] {g0, g1} );
        
        int s0 = g0.size();
        int s1 = g1.size();
        int s2 = g2.size();
        int m0 = m.size();

        // add a triple to the union
        m.add( triple( "a q b" ) );
        
        assertEquals( "m.size should have increased by one", m0 + 1, m.size() );
        assertEquals( "g0.size should have increased by one", s0 + 1, g0.size() );
        assertEquals( "g1 size should be constant", s1, g1.size() );
        
        // change the designated receiver and try again
        m.setBaseGraph( g1 );
        
        s0 = g0.size();
        s1 = g1.size();
        s2 = g2.size();
        m0 = m.size();
        
        m.add( triple( "a1 q b1" ));

        assertEquals( "m.size should have increased by one", m0 + 1, m.size() );
        assertEquals( "g0 size should be constant", s0, g0.size() );
        assertEquals( "g1.size should have increased by one", s1 + 1, g1.size() );
        
        // check that we can't make g2 the designated updater
        boolean expected = false;
        try {
            m.setBaseGraph( g2 );
        }
        catch (IllegalArgumentException e) {
            expected = true;
        }
        assertTrue( "Should not have been able to make g2 the updater", expected );
    }
    
    
    public void testDelete() {
        Graph g0 = graphWith( "x p y" );
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        
        MultiUnion m = new MultiUnion( new Graph[] {g0, g1} );
        
        checkDeleteSizes( 1, 2, 3, g0, g1, m );
        
        m.delete( triple( "x p y") );
        checkDeleteSizes( 0, 2, 2, g0, g1, m );

        m.delete( triple( "x p y") );
        checkDeleteSizes( 0, 2, 2, g0, g1, m );

        m.setBaseGraph( g1 );

        m.delete( triple( "x p z") );
        checkDeleteSizes( 0, 1, 1, g0, g1, m );

        m.delete( triple( "z p zz") );
        checkDeleteSizes( 0, 0, 0, g0, g1, m );
    }
    
    
    public void testContains() {
        Graph g0 = graphWith( "x p y" );
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        
        MultiUnion m = new MultiUnion( new Graph[] {g0, g1} );
 
        assertTrue( "m should contain triple", m.contains( triple( "x p y ")));       
        assertTrue( "m should contain triple", m.contains( triple( "x p z ")));       
        assertTrue( "m should contain triple", m.contains( triple( "z p zz ")));       
        
        assertFalse( "m should not contain triple", m.contains( triple( "zz p z ")));       
    }
    
    
    /* Test using a model to wrap a multi union */
    public void testModel() {
        Graph g0 = graphWith( "x p y" );
        MultiUnion u = new MultiUnion( new Graph[] {g0} );
        
        Model m = ModelFactory.createModelForGraph( u );
        
        assertEquals( "Model size not correct", 1, m.size() );
        
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        u.addGraph( g1 );        
        
        assertEquals( "Model size not correct", 3, m.size() );
        
        // adds one more statement to the model
        m.read( "file:testing/ontology/list0.rdf" );
        assertEquals( "Model size not correct", 4, m.size() );
        
        // debug m.write( System.out );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    protected void checkDeleteSizes( int s0, int s1, int m0, Graph g0, Graph g1, Graph m ) {
        assertEquals( "Delete check: g0 size", s0, g0.size() );
        assertEquals( "Delete check: g1 size", s1, g1.size() );
        assertEquals( "Delete check: m size", m0, m.size() );
    }
    
    protected Iterator iterateOver( Object x0 ) {
        List l = new ArrayList();
        l.add( x0 );
        return l.iterator();
    }
    protected Iterator iterateOver( Object x0, Object x1 ) {
        List l = new ArrayList();
        l.add( x0 );
        l.add( x1 );
        return l.iterator();
    }
    protected Iterator iterateOver( Object x0, Object x1, Object x2 ) {
        List l = new ArrayList();
        l.add( x0 );
        l.add( x1 );
        l.add( x2 );
        return l.iterator();
    }
    
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
