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

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.*;

import java.util.*;
import junit.framework.*;

/**
 	@author kers
*/
public class TestSimpleTripleSorter extends GraphTestBase
    {
    public TestSimpleTripleSorter(String name)
        {super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestSimpleTripleSorter.class ); }
        
    private TripleSorter sorter = new SimpleTripleSorter();
        
    /**
        Test that the empty triple array sorts into an empty triple array
    */
    public void testEmpty()
        {
        Triple [] triples = new Triple [] {};
        assertEquals( 0, sorter.sort( triples ).length ); 
        }
       
    /**
        Test that a singleton triple array sorts into that same singleton array
        for various different search-styles of triples
    */ 
    public void testSingle()
        {
        testSingle( "S P O" );
        testSingle( "S ?P O" );
        testSingle( "S P ?O" );
        testSingle( "?S ?P O" );
        testSingle( "?S P ?O" );
        testSingle( "S ?P ?O" );
        testSingle( "?S ?P ?O" );
        testSingle( "?? P O" );
        testSingle( "S ?? O" );
        testSingle( "S P ??O" );
        testSingle( "?? ?? O" );
        testSingle( "?? P ??" );
        testSingle( "S ?? ??" );
        testSingle( "?? ?? ??" );
        }
        
    public void testSingle(String ts )
        {
        Triple t = NodeCreateUtils.createTriple( ts );
        assertEquals( Arrays.asList( new Triple[] {t} ), Arrays.asList( sorter.sort( new Triple[] {t} ) ) );
        }
        
    /**
        Test that concrete nodes get sorted to the beginning of the result
    */
    public void testConcreteFirst()
        {
        testReordersTo( "S P O; ?s ?p ?o", "S P O; ?s ?p ?o" );    
        testReordersTo( "S P O; ?s ?p ?o", "?s ?p ?o; S P O" );    
        testReordersTo( "S P O; ?s ?p ?o; ?a ?b ?c", "?s ?p ?o; ?a ?b ?c; S P O" );
        testReordersTo( "S P O; ?s ?p ?o; ?a ?b ?c", "?s ?p ?o; S P O; ?a ?b ?c" );
        }
        
    /**
        Test that bound variables get sorted nearer the beginning than unbound ones
    */
    public void testBoundFirst()
        {
        testReordersTo( "?s R a; ?s ?p ?o", "?s ?p ?o; ?s R a" );    
        testReordersTo( "?s R a; ?s ?p b;", "?s ?p b; ?s R a" );
        testReordersTo( "?a P b; ?c Q d; ?a P ?c", "?a P b; ?a P ?c; ?c Q d" );
        }
        
    /**
        Test that ANY is heavier than one variable but lighter than two
    */
    public void testANY()
        {
        testReordersTo( "?? C d; ?a X ?b",  "?a X ?b; ?? C d" );    
        testReordersTo( "?a B c; ?? D e", "?? D e; ?a B c" );
        }
       
    /**
        Test that binding a variable makes it lighter than an unbound variable
    */ 
    public void testInteraction()
        {
        testReordersTo( "?a P b; ?a Q ?b; ?b R ?c", "?b R ?c; ?a Q ?b; ?a P b" );    
        }
        
    /**
        Test that a triple that binds more things gets sorted earlier than a equally-light
        triple that binds fewer things
    */
    public void testSortByMass()
        {
        testReordersTo( "?b c d; ?a b c; ?b ?c d; ?a ?b ?d", "?a b c; ?b c d; ?b ?c d; ?a ?b ?d" );    
        }
        
    /**
        Utility: test that the triple array described by <code>original</code> gets reordered
        to the triple array described by <code>desired</code>.
    */
    public void testReordersTo( String desired, String original )
        {
        Triple [] o = tripleArray( original ), d = tripleArray( desired );    
        assertEquals( Arrays.asList( d ), Arrays.asList( sorter.sort( o ) ) );
        }
    }
