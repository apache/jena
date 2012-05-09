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

package com.hp.hpl.jena.graph.test;

/**
    Tests that check GraphMem and WrappedGraph for correctness against the Graph
    and reifier test suites.
 
    @author kers
*/

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;

import java.util.*;

import junit.framework.*;

public class TestGraph extends GraphTestBase
    { 
	public TestGraph( String name )
		{ super( name ); }
        
    /**
        Answer a test suite that runs the Graph and Reifier tests on GraphMem and on
        WrappedGraphMem, the latter standing in for testing WrappedGraph.
     */
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite( TestGraph.class );
        result.addTest( suite( MetaTestGraph.class, GraphMem.class ) );
        result.addTest( suite( TestReifier.class, GraphMem.class ) );
        result.addTest( suite( MetaTestGraph.class, SmallGraphMem.class ) );
        result.addTest( suite( TestReifier.class, SmallGraphMem.class ) );
        result.addTest( suite( MetaTestGraph.class, WrappedGraphMem.class ) );
        result.addTest( suite( TestReifier.class, WrappedGraphMem.class ) );
        result.addTest( TestGraphListener.suite() );
        result.addTestSuite( TestRegisterGraphListener.class );
        return result;
        }
        
    public static TestSuite suite( Class<? extends Test> classWithTests, Class<? extends Graph> graphClass )
        { return MetaTestGraph.suite( classWithTests, graphClass ); }
        
    /**
        Trivial [incomplete] test that a Wrapped graph pokes through to the underlying
        graph. Really want something using mock classes. Will think about it. 
    */
    public void testWrappedSame()
        {
        Graph m = Factory.createGraphMem();
        Graph w = new WrappedGraph( m );
        graphAdd( m, "a trumps b; c eats d" );
        assertIsomorphic( m, w );
        graphAdd( w, "i write this; you read that" );
        assertIsomorphic( w, m );
        }        
        
    /**
        Class to provide a constructor that produces a wrapper round a GraphMem.    
    	@author kers
    */
    public static class WrappedGraphMem extends WrappedGraph
        {
        public WrappedGraphMem( ReificationStyle style ) 
            { super( Factory.createGraphMem( style ) ); }  
        }    
    
    public void testListSubjectsDoesntUseFind()
        {
        final boolean [] called = {false};
        Graph g = Factory.createGraphMem();
        ExtendedIterator<Node> subjects = g.queryHandler().subjectsFor( null, null );
        Set<Node> s = CollectionFactory.createHashedSet();
        while (subjects.hasNext()) s.add( subjects.next() );
        assertFalse( "find should not have been called", called[0] );
        }   
    
    public void testListPredicatesDoesntUseFind()
        {
        final boolean [] called = {false};
        Graph g = Factory.createGraphMem();
        ExtendedIterator<Node> predicates = g.queryHandler().predicatesFor( null, null );
        Set<Node> s = CollectionFactory.createHashedSet();
        while (predicates.hasNext()) s.add( predicates.next() );
        assertFalse( "find should not have been called", called[0] );
        }
    
    public void testListObjectsDoesntUseFind()
        {
        final boolean [] called = {false};
        Graph g = Factory.createGraphMem();
        ExtendedIterator<Node> subjects = g.queryHandler().objectsFor( null, null );
        Set<Node> s = CollectionFactory.createHashedSet();
        while (subjects.hasNext()) s.add( subjects.next() );
        assertFalse( "find should not have been called", called[0] );
        }   
    }
