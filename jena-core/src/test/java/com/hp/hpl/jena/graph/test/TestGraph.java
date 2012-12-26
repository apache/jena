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
*/

import junit.framework.Test ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.impl.WrappedGraph ;
import com.hp.hpl.jena.mem.GraphMem ;

public class TestGraph extends GraphTestBase
    { 
	public TestGraph( String name )
		{ super( name ); }
        
    /**
        Answer a test suite that runs the Graph tests on GraphMem and on
        WrappedGraphMem, the latter standing in for testing WrappedGraph.
     */
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite( TestGraph.class );
        result.addTest( suite( MetaTestGraph.class, GraphMem.class ) );
        result.addTest( suite( TestReifier.class, GraphMem.class ) );
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
    */
    public static class WrappedGraphMem extends WrappedGraph
        {
        public WrappedGraphMem( ) 
            { super( Factory.createGraphMem( ) ); }  
        }    
    }
