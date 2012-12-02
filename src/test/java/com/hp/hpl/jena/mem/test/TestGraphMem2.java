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

package com.hp.hpl.jena.mem.test;

import java.util.Iterator ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.test.AbstractTestGraph ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class TestGraphMem2 extends AbstractTestGraph
    {
    public TestGraphMem2(String name)
        { super( name ); }
    
    public static TestSuite suite()
    { return new TestSuite( TestGraphMem2.class ); }
    
    @Override
    public Graph getGraph() { return Factory.createGraphMem(); }
    
    public void testBrokenIndexes()
        {
        Graph g = getGraphWith( "x R y; x S z" );
        ExtendedIterator<Triple> it = g.find( Node.ANY, Node.ANY, Node.ANY );
        it.removeNext(); it.removeNext();
        assertFalse( g.find( node( "x" ), Node.ANY, Node.ANY ).hasNext() );
        assertFalse( g.find( Node.ANY, node( "R" ), Node.ANY ).hasNext() );
        assertFalse( g.find( Node.ANY, Node.ANY, node( "y" ) ).hasNext() );
        }   
            
    public void testBrokenSubject()
        {
        Graph g = getGraphWith( "x brokenSubject y" );
        ExtendedIterator<Triple> it = g.find( node( "x" ), Node.ANY, Node.ANY );
        it.removeNext();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
        
    public void testBrokenPredicate()
        {
        Graph g = getGraphWith( "x brokenPredicate y" );
        ExtendedIterator<Triple> it = g.find( Node.ANY, node( "brokenPredicate"), Node.ANY );
        it.removeNext();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
        
    public void testBrokenObject()
        {
        Graph g = getGraphWith( "x brokenObject y" );
        ExtendedIterator<Triple> it = g.find( Node.ANY, Node.ANY, node( "y" ) );
        it.removeNext();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
    
    public void testSizeAfterRemove() 
        {
        Graph g = getGraphWith( "x p y" );
        ExtendedIterator<Triple> it = g.find( triple( "x ?? ??" ) );
        it.removeNext();
        assertEquals( 0, g.size() );        
        }
    
    public void testUnnecessaryMatches() 
        {
        Node special = new Node_URI( "eg:foo" ) 
            {
            @Override
            public boolean matches( Node s ) 
                {
                fail( "Matched called superfluously." );
                return true;
                }
            };
        Graph g = getGraphWith( "x p y" );
        g.add( new Triple( special, special, special ) );
        exhaust( g.find( special, Node.ANY, Node.ANY ) );
        exhaust( g.find( Node.ANY, special, Node.ANY ) );
        exhaust( g.find( Node.ANY, Node.ANY, special ) );
    }
    
    protected void exhaust( Iterator<?> it )
        { while (it.hasNext()) it.next(); }
    }
