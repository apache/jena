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
package com.hp.hpl.jena.graph.compose.test;


// Imports
///////////////

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.MultiUnion.MultiUnionStatisticsHandler;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import junit.framework.TestSuite;


/**
 * <p>
 * Unit tests for multi-union graph.
 * </p>
 */
public class TestMultiUnion extends AbstractTestGraph
{

    public TestMultiUnion( String s ) {
        super( s );
    }
    
    // External signature methods
    //////////////////////////////////

    public static TestSuite suite()
        { return new TestSuite( TestMultiUnion.class ); }   
    
    @Override
    public Graph getGraph()
        {
        Graph gBase = graphWith( "" ), g1 = graphWith( "" );
        return new MultiUnion( new Graph[] {gBase, g1} ); 
        }

    public void testEmptyGraph() {
        Graph m = new MultiUnion();
        Graph g0 = graphWith( "x p y");
        
        assertEquals( "Empty model should have size zero", 0, m.size() );
        assertFalse( "Empty model should not contain another graph", m.dependsOn( g0 ) );
    }
    
    /**
        A MultiUnion graph should have a MultiUnionStatisticsHandler, and that
        handler should point right back to that graph.
    */
    public void testMultiUnionHasMultiUnionStatisticsHandler()
        {
        MultiUnion mu = new MultiUnion();
        GraphStatisticsHandler sh = mu.getStatisticsHandler();
        assertInstanceOf( MultiUnionStatisticsHandler.class, sh );
        assertSame( mu, ((MultiUnionStatisticsHandler) sh).getUnion() );
        }
    
//    public void testDeferredReifier()
//        {
//        Graph g1 = graphWith( "" ), g2 = graphWith( "" );
//        MultiUnion m = new MultiUnion( new Graph[] {g1, g2} );
//        m.setBaseGraph( g1 );
//        assertSame( m.getReifier(), g1.getReifier() );
//        }
    

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
    public void testModel()  {
        Graph g0 = graphWith( "x p y" );
        MultiUnion u = new MultiUnion( new Graph[] {g0} );
        
        Model m = ModelFactory.createModelForGraph( u );
        
        assertEquals( "Model size not correct", 1, m.size() );
        
        Graph g1 = graphWith( "x p z; z p zz" );        // disjoint with g0
        u.addGraph( g1 );        
        
        assertEquals( "Model size not correct", 3, m.size() );
        
        // adds one more statement to the model
        m.read( getFileName("ontology/list0.rdf") );
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
    
    protected <T> Iterator<T> iterateOver( T x0 ) {
        List<T> l = new ArrayList<>();
        l.add( x0 );
        return l.iterator();
    }
    protected <T> Iterator<T> iterateOver( T x0, T x1 ) {
        List<T> l = new ArrayList<>();
        l.add( x0 );
        l.add( x1 );
        return l.iterator();
    }
    protected <T> Iterator<T> iterateOver( T x0, T x1, T x2 ) {
        List<T> l = new ArrayList<>();
        l.add( x0 );
        l.add( x1 );
        l.add( x2 );
        return l.iterator();
    }
    
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
