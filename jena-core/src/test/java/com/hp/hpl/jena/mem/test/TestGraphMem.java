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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import junit.framework.*;

/**
 	@author kers
*/
public class TestGraphMem extends AbstractTestGraphMem
    {
    public TestGraphMem(String name)
        { super(name); }
    
    public static TestSuite suite()
        { return new TestSuite( TestGraphMem.class ); }
        
    @Override public Graph getGraph()
        { return Factory.createGraphMem(); }
        
    public void testRemoveAllDoesntUseFind()
        {
        Graph g = new GraphMemWithoutFind();
        graphAdd( g, "x P y; a Q b" );
        g.getBulkUpdateHandler().removeAll();
        assertEquals( 0, g.size() );
        }
    
    @Override public void testSizeAfterRemove() 
        {
        Graph g = getGraphWith( "x p y" );
        ExtendedIterator<Triple> it = g.find( triple( "x ?? ??" ) );
        it.removeNext();
        assertEquals( 0, g.size() );        
        }
    
    public void testContainsConcreteDoesntUseFind()
        {
        Graph g = new GraphMemWithoutFind();
        graphAdd( g, "x P y; a Q b" );
        assertTrue( g.contains( triple( "x P y" ) ) );
        assertTrue( g.contains( triple( "a Q b" ) ) );
        assertFalse( g.contains( triple( "a P y" ) ) );
        assertFalse( g.contains( triple( "y R b" ) ) );
        }    
    
    @SuppressWarnings("deprecation")
    protected final class GraphMemWithoutFind extends GraphMem
        {
        @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch t )
            { throw new JenaException( "find is Not Allowed" ); }
        }
    }
